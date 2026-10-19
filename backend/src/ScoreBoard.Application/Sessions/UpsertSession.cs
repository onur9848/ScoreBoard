using Microsoft.EntityFrameworkCore;
using ScoreBoard.Application.Abstractions;
using ScoreBoard.Application.Common;
using ScoreBoard.Domain.Devices;
using ScoreBoard.Domain.Sessions;

namespace ScoreBoard.Application.Sessions;

public enum UpsertOutcome
{
    Created,
    Updated,
    Unchanged,
}

public sealed record UpsertSessionResult(string Id, UpsertOutcome Outcome);

/// <summary>
/// Uygulamanın gönderdiği oyun halini kaydeder. Aynı hali tekrar göndermek güvenlidir;
/// daha eski bir hal saklanan kaydı değiştirmez.
/// </summary>
public sealed class UpsertSessionHandler(IApplicationDbContext db, TimeProvider clock)
{
    public async Task<UpsertSessionResult> HandleAsync(GameSnapshot game, DeviceInfo device,
        CancellationToken cancellationToken)
    {
        var errors = GameSnapshotValidator.Validate(game, device);
        if (errors.Count > 0) throw new ValidationException(errors);

        try
        {
            return await SaveAsync(game, device, cancellationToken);
        }
        catch (DbUpdateException)
        {
            // Aynı oyun aynı anda iki kez ilk kez gönderildiyse biri anahtar çakışmasına düşer;
            // ikinci deneme kaydı bulur ve normal yoldan ilerler.
            return await SaveAsync(game, device, cancellationToken);
        }
    }

    private Task<UpsertSessionResult> SaveAsync(GameSnapshot game, DeviceInfo device,
        CancellationToken cancellationToken)
    {
        var now = clock.GetUtcNow().UtcDateTime;
        var incoming = GameSnapshotMapper.ToDomain(game, device.DeviceId, now);

        return db.InTransactionAsync(async ct =>
        {
            db.ClearTracked();
            await TouchDeviceAsync(device, now, ct);

            var stored = await db.Sessions
                .Where(s => s.Id == incoming.Id)
                .Select(s => new { s.DeviceId, s.UpdatedAt, s.EventCount })
                .FirstOrDefaultAsync(ct);

            UpsertOutcome outcome;
            if (stored is null)
            {
                outcome = UpsertOutcome.Created;
            }
            else if (stored.DeviceId != incoming.DeviceId)
            {
                throw new ConflictException("Bu oyun başka bir cihaza ait.");
            }
            else if (!incoming.Supersedes(stored.UpdatedAt, stored.EventCount))
            {
                await db.SaveChangesAsync(ct);
                return new UpsertSessionResult(incoming.Id, UpsertOutcome.Unchanged);
            }
            else
            {
                // Oyunun tamamı yeniden yazılır; eller ve olaylar küçük olduğu için farkını almaya değmez.
                await db.DeleteSessionAsync(incoming.Id, ct);
                outcome = UpsertOutcome.Updated;
            }

            db.Sessions.Add(incoming);
            await db.SaveChangesAsync(ct);
            return new UpsertSessionResult(incoming.Id, outcome);
        }, cancellationToken);
    }

    private async Task TouchDeviceAsync(DeviceInfo info, DateTime now, CancellationToken ct)
    {
        var device = await db.Devices.FirstOrDefaultAsync(d => d.Id == info.DeviceId, ct);
        if (device is null)
        {
            db.Devices.Add(new Device(info.DeviceId, now, info.Platform, info.AppVersion));
        }
        else
        {
            device.Seen(now, info.Platform, info.AppVersion);
        }
    }
}

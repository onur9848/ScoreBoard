using System.Text.Json;
using Microsoft.EntityFrameworkCore;
using ScoreBoard.Application.Abstractions;

namespace ScoreBoard.Application.Sessions;

public sealed class GetSessionDetailHandler(IApplicationDbContext db)
{
    public async Task<SessionDetailDto?> HandleAsync(string id, CancellationToken cancellationToken)
    {
        var session = await db.Sessions
            .AsNoTracking()
            .Include(s => s.Players)
            .Include(s => s.Rules)
            .Include(s => s.Rounds).ThenInclude(r => r.Entries)
            .Include(s => s.Events)
            .FirstOrDefaultAsync(s => s.Id == id, cancellationToken);
        if (session is null) return null;

        var device = await db.Devices.AsNoTracking()
            .Where(d => d.Id == session.DeviceId)
            .Select(d => new DeviceDto(d.Id, d.Platform, d.AppVersion, d.FirstSeenAt, d.LastSeenAt))
            .FirstOrDefaultAsync(cancellationToken);

        var totals = session.Totals();
        var lowestWins = session.WinCondition == "lowest_total";
        var ordered = totals.Values.OrderBy(v => lowestWins ? v : -v).ToList();
        var winners = session.WinnerPlayerIds.ToHashSet();

        var players = session.Players
            .OrderBy(p => p.Seat)
            .Select(p => new SessionPlayerDto(
                p.Id,
                p.Name,
                p.Seat,
                totals[p.Id],
                // Eşit puanlar aynı sırayı paylaşır.
                ordered.IndexOf(totals[p.Id]) + 1,
                winners.Contains(p.Id)))
            .ToList();

        return new SessionDetailDto(
            ListSessionsHandler.ToSummary(session),
            players,
            session.Rules.Select(r => new SessionRuleDto(r.Key, r.Value, r.Customized)).ToList(),
            session.Rounds
                .OrderBy(r => r.Position)
                .Select(r => new RoundDto(r.Id, r.Position, r.HandNumber, r.Kind, r.RuleKey, r.ActorPlayerId,
                    r.CreatedAt, r.Entries.Select(e => new RoundEntryDto(e.PlayerId, e.Points, e.Source)).ToList()))
                .ToList(),
            session.Events
                .OrderBy(e => e.Sequence)
                .Select(e => new SessionEventDto(e.Id, e.Sequence, e.Type, e.OccurredAt, e.RoundId, e.RuleKey,
                    e.PlayerId, Parse(e.DataJson)))
                .ToList(),
            device);
    }

    private static JsonElement Parse(string json)
    {
        using var doc = JsonDocument.Parse(string.IsNullOrEmpty(json) ? "{}" : json);
        return doc.RootElement.Clone();
    }
}

using Microsoft.EntityFrameworkCore;
using ScoreBoard.Domain.Devices;
using ScoreBoard.Domain.Sessions;

namespace ScoreBoard.Application.Abstractions;

public interface IApplicationDbContext
{
    DbSet<GameSession> Sessions { get; }
    DbSet<SessionPlayer> Players { get; }
    DbSet<SessionRule> Rules { get; }
    DbSet<Round> Rounds { get; }
    DbSet<RoundEntry> RoundEntries { get; }
    DbSet<SessionEvent> Events { get; }
    DbSet<RackScan> RackScans { get; }
    DbSet<Device> Devices { get; }

    /// <summary>Oyunu ve ona bağlı tüm satırları siler.</summary>
    Task DeleteSessionAsync(string sessionId, CancellationToken cancellationToken);

    Task<int> SaveChangesAsync(CancellationToken cancellationToken = default);

    /// <summary>İşi tek bir veritabanı işleminde çalıştırır (sağlayıcının yeniden deneme stratejisiyle).</summary>
    Task<T> InTransactionAsync<T>(Func<CancellationToken, Task<T>> work, CancellationToken cancellationToken);

    /// <summary>Takip edilen nesneleri bırakır; başarısız bir denemeden sonra temiz başlamak için.</summary>
    void ClearTracked();
}

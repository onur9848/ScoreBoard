using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Storage.ValueConversion;
using ScoreBoard.Application.Abstractions;
using ScoreBoard.Domain.Devices;
using ScoreBoard.Domain.Sessions;

namespace ScoreBoard.Infrastructure.Persistence;

public sealed class ScoreBoardDbContext(DbContextOptions<ScoreBoardDbContext> options)
    : DbContext(options), IApplicationDbContext
{
    public DbSet<GameSession> Sessions => Set<GameSession>();
    public DbSet<SessionPlayer> Players => Set<SessionPlayer>();
    public DbSet<SessionRule> Rules => Set<SessionRule>();
    public DbSet<Round> Rounds => Set<Round>();
    public DbSet<RoundEntry> RoundEntries => Set<RoundEntry>();
    public DbSet<SessionEvent> Events => Set<SessionEvent>();
    public DbSet<RackScan> RackScans => Set<RackScan>();
    public DbSet<Device> Devices => Set<Device>();

    public Task DeleteSessionAsync(string sessionId, CancellationToken cancellationToken) =>
        Sessions.Where(s => s.Id == sessionId).ExecuteDeleteAsync(cancellationToken);

    public Task<T> InTransactionAsync<T>(Func<CancellationToken, Task<T>> work, CancellationToken cancellationToken)
    {
        var strategy = Database.CreateExecutionStrategy();
        return strategy.ExecuteAsync(async ct =>
        {
            await using var transaction = await Database.BeginTransactionAsync(ct);
            var result = await work(ct);
            await transaction.CommitAsync(ct);
            return result;
        }, cancellationToken);
    }

    public void ClearTracked() => ChangeTracker.Clear();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.ApplyConfigurationsFromAssembly(typeof(ScoreBoardDbContext).Assembly);

        if (Database.IsSqlite())
        {
            // SQLite tarih türü tutmaz; okunan değerler UTC olarak işaretlenir.
            var utc = new ValueConverter<DateTime, DateTime>(v => v, v => DateTime.SpecifyKind(v, DateTimeKind.Utc));
            var utcNullable = new ValueConverter<DateTime?, DateTime?>(
                v => v, v => v.HasValue ? DateTime.SpecifyKind(v.Value, DateTimeKind.Utc) : v);
            foreach (var property in modelBuilder.Model.GetEntityTypes().SelectMany(t => t.GetProperties()))
            {
                if (property.ClrType == typeof(DateTime)) property.SetValueConverter(utc);
                else if (property.ClrType == typeof(DateTime?)) property.SetValueConverter(utcNullable);
            }
        }
    }
}

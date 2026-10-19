using Microsoft.EntityFrameworkCore;

namespace ScoreBoard.Infrastructure.Persistence;

/// <summary>
/// PostgreSQL'de migration'ları uygular. SQLite yalnızca yerel geliştirme ve testler içindir;
/// şema doğrudan modelden oluşturulur.
/// </summary>
public sealed class DatabaseInitializer(ScoreBoardDbContext db)
{
    public async Task InitializeAsync(CancellationToken cancellationToken = default)
    {
        if (db.Database.IsSqlite())
        {
            await db.Database.EnsureCreatedAsync(cancellationToken);
        }
        else
        {
            await db.Database.MigrateAsync(cancellationToken);
        }
    }
}

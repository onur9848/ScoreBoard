using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Design;

namespace ScoreBoard.Infrastructure.Persistence;

/// <summary>
/// <c>dotnet ef migrations add</c> için. Migration'lar PostgreSQL içindir; bağlantı kurulmaz.
/// </summary>
internal sealed class DesignTimeDbContextFactory : IDesignTimeDbContextFactory<ScoreBoardDbContext>
{
    public ScoreBoardDbContext CreateDbContext(string[] args)
    {
        var options = new DbContextOptionsBuilder<ScoreBoardDbContext>();
        DependencyInjection.Configure(options, DatabaseProvider.Postgres, "Host=localhost;Database=scoreboard");
        return new ScoreBoardDbContext(options.Options);
    }
}

using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using ScoreBoard.Application.Abstractions;
using ScoreBoard.Infrastructure.Persistence;

namespace ScoreBoard.Infrastructure;

public static class DependencyInjection
{
    /// <summary>
    /// <c>Database:Provider</c> "Postgres" (varsayılan) ya da "Sqlite";
    /// bağlantı <c>ConnectionStrings:ScoreBoard</c> ile verilir.
    /// </summary>
    public static IServiceCollection AddInfrastructure(this IServiceCollection services, IConfiguration configuration)
    {
        var provider = DatabaseProviderConfig.From(configuration);
        var connectionString = configuration.GetConnectionString("ScoreBoard")
            ?? throw new InvalidOperationException("ConnectionStrings:ScoreBoard ayarlanmalı.");

        services.AddDbContext<ScoreBoardDbContext>(options => Configure(options, provider, connectionString));
        services.AddScoped<IApplicationDbContext>(sp => sp.GetRequiredService<ScoreBoardDbContext>());
        services.AddScoped<DatabaseInitializer>();
        return services;
    }

    public static void Configure(DbContextOptionsBuilder options, DatabaseProvider provider, string connectionString)
    {
        if (provider == DatabaseProvider.Sqlite)
        {
            options.UseSqlite(connectionString, o => o.UseQuerySplittingBehavior(QuerySplittingBehavior.SplitQuery))
                .UseSnakeCaseNamingConvention();
        }
        else
        {
            options.UseNpgsql(connectionString, o => o
                .UseQuerySplittingBehavior(QuerySplittingBehavior.SplitQuery)
                .EnableRetryOnFailure())
                .UseSnakeCaseNamingConvention();
        }
    }
}

public enum DatabaseProvider
{
    Postgres,
    Sqlite,
}

internal static class DatabaseProviderConfig
{
    public static DatabaseProvider From(IConfiguration configuration) =>
        configuration["Database:Provider"]?.Trim().ToLowerInvariant() switch
        {
            null or "" or "postgres" or "postgresql" or "npgsql" => DatabaseProvider.Postgres,
            "sqlite" => DatabaseProvider.Sqlite,
            var other => throw new InvalidOperationException($"Bilinmeyen veritabanı sağlayıcısı: {other}"),
        };
}

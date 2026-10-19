using Microsoft.Extensions.DependencyInjection;
using ScoreBoard.Application.Sessions;
using ScoreBoard.Application.Stats;

namespace ScoreBoard.Application;

public static class DependencyInjection
{
    public static IServiceCollection AddApplication(this IServiceCollection services)
    {
        services.AddSingleton(TimeProvider.System);
        services.AddScoped<UpsertSessionHandler>();
        services.AddScoped<ListSessionsHandler>();
        services.AddScoped<GetSessionDetailHandler>();
        services.AddScoped<GetOverviewStatsHandler>();
        return services;
    }
}

using ScoreBoard.Api.Security;
using ScoreBoard.Application.Sessions;
using ScoreBoard.Application.Stats;

namespace ScoreBoard.Api.Endpoints;

public static class PanelEndpoints
{
    public static void MapPanelEndpoints(this RouteGroupBuilder api)
    {
        var panel = api.MapGroup("").RequireApiKey(ApiKeyScope.Panel).WithTags("Panel");

        // Panelin giriş ekranı anahtarı bununla doğrular.
        panel.MapGet("/auth/check", () => Results.NoContent());

        panel.MapGet("/sessions", (string? gameType, string? status, string? deviceId, DateTime? from,
                DateTime? to, string? q, int? page, int? pageSize, ListSessionsHandler handler,
                CancellationToken ct) =>
            handler.HandleAsync(
                new SessionListQuery(gameType, status, deviceId, Utc(from), Utc(to), q, page ?? 1, pageSize ?? 25),
                ct));

        panel.MapGet("/sessions/{id}", async (string id, GetSessionDetailHandler handler, CancellationToken ct) =>
            await handler.HandleAsync(id, ct) is { } detail ? Results.Ok(detail) : Results.NotFound());

        panel.MapGet("/stats/overview", (DateTime? from, DateTime? to, string? gameType, int? utcOffsetMinutes,
                int? days, GetOverviewStatsHandler handler, CancellationToken ct) =>
            handler.HandleAsync(new StatsQuery(Utc(from), Utc(to), gameType, utcOffsetMinutes ?? 0, days ?? 30), ct));
    }

    private static DateTime? Utc(DateTime? value) => value?.Kind switch
    {
        null => null,
        DateTimeKind.Utc => value,
        DateTimeKind.Local => value.Value.ToUniversalTime(),
        _ => DateTime.SpecifyKind(value.Value, DateTimeKind.Utc),
    };
}

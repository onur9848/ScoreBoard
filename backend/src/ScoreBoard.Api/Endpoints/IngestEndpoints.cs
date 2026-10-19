using ScoreBoard.Api.Security;
using ScoreBoard.Application.Common;
using ScoreBoard.Application.Sessions;

namespace ScoreBoard.Api.Endpoints;

public static class IngestEndpoints
{
    public const long MaxBodyBytes = 2 * 1024 * 1024;

    public static void MapIngestEndpoints(this RouteGroupBuilder api)
    {
        // Uygulama oyunun tam halini gönderir; aynı isteği tekrarlamak güvenlidir.
        api.MapPut("/sessions/{id}", async (string id, GameSnapshot game, HttpRequest request,
                UpsertSessionHandler handler, CancellationToken ct) =>
            {
                if (game.Id != id)
                {
                    return Results.ValidationProblem(new Dictionary<string, string[]>
                    {
                        ["id"] = ["Adres ile gövdedeki oyun kimliği aynı olmalı."],
                    });
                }

                var device = new DeviceInfo(
                    request.Headers[ApiKeyHeaders.DeviceId].ToString(),
                    Optional(request, ApiKeyHeaders.Platform),
                    Optional(request, ApiKeyHeaders.AppVersion));
                UpsertSessionResult result;
                try
                {
                    result = await handler.HandleAsync(game, device, ct);
                }
                catch (ValidationException e)
                {
                    return Results.ValidationProblem(e.Errors.ToDictionary(kv => kv.Key, kv => kv.Value),
                        title: e.Message);
                }
                catch (ConflictException e)
                {
                    return Results.Problem(statusCode: StatusCodes.Status409Conflict, title: e.Message);
                }

                var body = new { id = result.Id, outcome = result.Outcome.ToString().ToLowerInvariant() };
                return result.Outcome == UpsertOutcome.Created
                    ? Results.Created($"/api/v1/sessions/{result.Id}", body)
                    : Results.Ok(body);
            })
            .RequireApiKey(ApiKeyScope.Ingest)
            .WithMetadata(new Microsoft.AspNetCore.Mvc.RequestSizeLimitAttribute(MaxBodyBytes))
            .WithName("UpsertSession")
            .WithTags("Ingest");
    }

    private static string? Optional(HttpRequest request, string header)
    {
        var value = request.Headers[header].ToString().Trim();
        return value.Length == 0 ? null : value[..Math.Min(value.Length, 32)];
    }
}

using System.Text.Json;
using ScoreBoard.Domain.Sessions;

namespace ScoreBoard.Application.Sessions;

internal static class GameSnapshotMapper
{
    public static GameSession ToDomain(GameSnapshot game, string deviceId, DateTime receivedAt)
    {
        var events = new List<SessionEvent>();
        var rackScans = new List<RackScan>();
        foreach (var e in game.Events ?? [])
        {
            var data = e.Data is { ValueKind: JsonValueKind.Object } d ? d : default;
            var occurredAt = Utc(e.OccurredAt);
            events.Add(new SessionEvent(
                e.Id,
                e.Type,
                occurredAt,
                data.ValueKind == JsonValueKind.Object ? data.GetRawText() : "{}",
                roundId: Text(data, "round_id"),
                ruleKey: Text(data, "rule_key"),
                playerId: Text(data, "player_id") ?? Text(data, "actor_player_id")));

            if (e.Type == SessionEventTypes.RackScanUsed && Text(data, "player_id") is { } playerId)
            {
                rackScans.Add(new RackScan(
                    e.Id,
                    Text(data, "round_id"),
                    playerId,
                    occurredAt,
                    Int(data, "points"),
                    Int(data, "tile_count"),
                    Int(data, "recognized_tile_count"),
                    Int(data, "corrected_tile_count"),
                    Bool(data, "used_camera")));
            }
        }

        var settings = game.Settings;
        return new GameSession(
            id: game.Id,
            deviceId: deviceId,
            schemaVersion: game.SchemaVersion,
            title: string.IsNullOrWhiteSpace(game.Title) ? "İsimsiz Oyun" : game.Title.Trim(),
            gameType: game.GameType,
            status: game.Status == SessionStatuses.Finished ? SessionStatuses.Finished : SessionStatuses.Active,
            isQuickGame: game.IsQuickGame,
            isPartnered: settings?.IsPartnered ?? false,
            winCondition: settings?.WinCondition ?? "highest_total",
            createdAt: Utc(game.CreatedAt),
            updatedAt: Utc(game.UpdatedAt),
            finishedAt: game.FinishedAt is { } f ? Utc(f) : null,
            winnerPlayerIds: game.WinnerPlayerIds ?? [],
            players: (game.Players ?? []).Select(p => new SessionPlayer(p.Id, p.Name?.Trim() ?? "", p.Seat)),
            rules: (settings?.Rules ?? []).Select(r => new SessionRule(r.Key, r.Value, r.Customized)),
            rounds: (game.Rounds ?? []).Select(r => new Round(
                r.Id,
                r.Kind ?? RoundKinds.Hand,
                Utc(r.CreatedAt),
                r.RuleKey,
                r.ActorPlayerId,
                (r.Entries ?? []).Select(e => new RoundEntry(e.PlayerId, e.Points, e.Source ?? ScoreSources.Manual)))),
            events: events,
            rackScans: rackScans,
            receivedAt: receivedAt);
    }

    private static DateTime Utc(DateTime value) => value.Kind switch
    {
        DateTimeKind.Utc => value,
        DateTimeKind.Local => value.ToUniversalTime(),
        _ => DateTime.SpecifyKind(value, DateTimeKind.Utc),
    };

    private static string? Text(JsonElement data, string name) =>
        data.ValueKind == JsonValueKind.Object && data.TryGetProperty(name, out var v) && v.ValueKind == JsonValueKind.String
            ? v.GetString()
            : null;

    private static int Int(JsonElement data, string name) =>
        data.ValueKind == JsonValueKind.Object && data.TryGetProperty(name, out var v) && v.TryGetInt32(out var i) ? i : 0;

    private static bool Bool(JsonElement data, string name) =>
        data.ValueKind == JsonValueKind.Object && data.TryGetProperty(name, out var v) && v.ValueKind == JsonValueKind.True;
}

using System.Text.Json;

namespace ScoreBoard.Application.Sessions;

// Mobil uygulamanın gönderdiği oyun JSON'u (schema_version 2). Alan adları
// API'nin snake_case JSON ayarıyla eşleşir; mobile/lib/domain/models/game.dart ile birlikte değişir.

public sealed record GameSnapshot(
    int SchemaVersion,
    string Id,
    string? Title,
    string GameType,
    string? Status,
    bool IsQuickGame,
    DateTime CreatedAt,
    DateTime UpdatedAt,
    DateTime? FinishedAt,
    IReadOnlyList<string>? WinnerPlayerIds,
    SettingsSnapshot? Settings,
    IReadOnlyList<PlayerSnapshot>? Players,
    IReadOnlyList<RoundSnapshot>? Rounds,
    IReadOnlyList<EventSnapshot>? Events);

public sealed record SettingsSnapshot(bool IsPartnered, string? WinCondition, IReadOnlyList<RuleSnapshot>? Rules);

public sealed record RuleSnapshot(string Key, int Value, bool Customized);

public sealed record PlayerSnapshot(string Id, string? Name, int Seat);

public sealed record RoundSnapshot(
    string Id,
    string? Kind,
    string? RuleKey,
    string? ActorPlayerId,
    DateTime CreatedAt,
    IReadOnlyList<EntrySnapshot>? Entries);

public sealed record EntrySnapshot(string PlayerId, int Points, string? Source);

public sealed record EventSnapshot(string Id, string Type, DateTime OccurredAt, JsonElement? Data);

public sealed record DeviceInfo(string DeviceId, string? Platform, string? AppVersion);

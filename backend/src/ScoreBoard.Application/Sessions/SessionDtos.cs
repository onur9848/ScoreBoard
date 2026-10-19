using System.Text.Json;

namespace ScoreBoard.Application.Sessions;

public sealed record SessionSummaryDto(
    string Id,
    string Title,
    string GameType,
    string Status,
    bool IsQuickGame,
    bool IsPartnered,
    string WinCondition,
    IReadOnlyList<string> PlayerNames,
    IReadOnlyList<string> WinnerNames,
    int PlayerCount,
    int HandCount,
    int PenaltyCount,
    int RackScanCount,
    int CustomizedRuleCount,
    DateTime CreatedAt,
    DateTime UpdatedAt,
    DateTime? FinishedAt,
    long? DurationSeconds,
    string DeviceId);

public sealed record SessionDetailDto(
    SessionSummaryDto Summary,
    IReadOnlyList<SessionPlayerDto> Players,
    IReadOnlyList<SessionRuleDto> Rules,
    IReadOnlyList<RoundDto> Rounds,
    IReadOnlyList<SessionEventDto> Events,
    DeviceDto? Device);

public sealed record SessionPlayerDto(string Id, string Name, int Seat, int Total, int Rank, bool IsWinner);

public sealed record SessionRuleDto(string Key, int Value, bool Customized);

public sealed record RoundDto(
    string Id,
    int Position,
    int? HandNumber,
    string Kind,
    string? RuleKey,
    string? ActorPlayerId,
    DateTime CreatedAt,
    IReadOnlyList<RoundEntryDto> Entries);

public sealed record RoundEntryDto(string PlayerId, int Points, string Source);

public sealed record SessionEventDto(
    string Id,
    int Sequence,
    string Type,
    DateTime OccurredAt,
    string? RoundId,
    string? RuleKey,
    string? PlayerId,
    JsonElement Data);

public sealed record DeviceDto(string Id, string? Platform, string? AppVersion, DateTime FirstSeenAt, DateTime LastSeenAt);

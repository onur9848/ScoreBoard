namespace ScoreBoard.Domain.Sessions;

/// <summary>
/// Mobil uygulamanın gönderdiği sabit değerler. Bilinmeyen değerler reddedilmez;
/// uygulamanın yeni sürümleri yeni türler ekleyebilir.
/// </summary>
public static class GameTypes
{
    public const string Okey = "okey";
    public const string YuzBirOkey = "yuzbir_okey";
    public const string Generic = "generic";
}

public static class SessionStatuses
{
    public const string Active = "active";
    public const string Finished = "finished";
}

public static class RoundKinds
{
    public const string Hand = "hand";
    public const string Penalty = "penalty";
    public const string Finish = "finish";
}

public static class ScoreSources
{
    public const string Manual = "manual";
    public const string Rule = "rule";
    public const string RackScan = "rack_scan";
}

public static class SessionEventTypes
{
    public const string GameCreated = "game_created";
    public const string RoundAdded = "round_added";
    public const string RoundEdited = "round_edited";
    public const string RoundDeleted = "round_deleted";
    public const string RuleApplied = "rule_applied";
    public const string RackScanUsed = "rack_scan_used";
    public const string GameFinished = "game_finished";
    public const string GameReopened = "game_reopened";
    public const string GameResumed = "game_resumed";
}

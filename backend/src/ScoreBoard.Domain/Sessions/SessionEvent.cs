namespace ScoreBoard.Domain.Sessions;

/// <summary>
/// Oyun günlüğündeki bir kayıt. Ham veri <see cref="DataJson"/> içinde saklanır;
/// sık sorgulanan alanlar ayrıca sütun olarak tutulur.
/// </summary>
public sealed class SessionEvent
{
    private SessionEvent() { }

    public SessionEvent(string id, string type, DateTime occurredAt, string dataJson,
        string? roundId = null, string? ruleKey = null, string? playerId = null)
    {
        Id = id;
        Type = type;
        OccurredAt = occurredAt;
        DataJson = dataJson;
        RoundId = roundId;
        RuleKey = ruleKey;
        PlayerId = playerId;
    }

    public string SessionId { get; private set; } = default!;
    public string Id { get; private set; } = default!;
    public int Sequence { get; private set; }
    public string Type { get; private set; } = default!;
    public DateTime OccurredAt { get; private set; }
    public string? RoundId { get; private set; }
    public string? RuleKey { get; private set; }
    public string? PlayerId { get; private set; }
    public string DataJson { get; private set; } = "{}";

    internal void Place(int sequence) => Sequence = sequence;
}

namespace ScoreBoard.Domain.Sessions;

public sealed class Round
{
    private readonly List<RoundEntry> _entries = [];

    private Round() { }

    public Round(string id, string kind, DateTime createdAt, string? ruleKey, string? actorPlayerId,
        IEnumerable<RoundEntry> entries)
    {
        Id = id;
        Kind = kind;
        CreatedAt = createdAt;
        RuleKey = ruleKey;
        ActorPlayerId = actorPlayerId;
        _entries.AddRange(entries);
    }

    public string SessionId { get; private set; } = default!;
    public string Id { get; private set; } = default!;

    /// <summary>Tablodaki sıra (0'dan başlar).</summary>
    public int Position { get; private set; }

    /// <summary>Ceza satırlarında boştur; el sayısına dahil edilmezler.</summary>
    public int? HandNumber { get; private set; }

    public string Kind { get; private set; } = default!;
    public string? RuleKey { get; private set; }
    public string? ActorPlayerId { get; private set; }
    public DateTime CreatedAt { get; private set; }
    public IReadOnlyList<RoundEntry> Entries => _entries;

    public bool CountsAsHand => Kind != RoundKinds.Penalty;

    internal void Place(int position, int? handNumber)
    {
        Position = position;
        HandNumber = handNumber;
    }
}

public sealed class RoundEntry
{
    private RoundEntry() { }

    public RoundEntry(string playerId, int points, string source)
    {
        PlayerId = playerId;
        Points = points;
        Source = source;
    }

    public string SessionId { get; private set; } = default!;
    public string RoundId { get; private set; } = default!;
    public string PlayerId { get; private set; } = default!;
    public int Points { get; private set; }
    public string Source { get; private set; } = default!;
}

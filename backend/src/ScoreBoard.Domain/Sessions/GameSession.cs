namespace ScoreBoard.Domain.Sessions;

/// <summary>
/// Bir kullanıcının oyun türünü seçmesinden oyunun bitişine kadar olan masa.
/// Mobil uygulama oyunun tam halini gönderir; sunucu bu hali olduğu gibi saklar
/// ve panel için özet alanları hesaplar.
/// </summary>
public sealed class GameSession
{
    private readonly List<SessionPlayer> _players = [];
    private readonly List<SessionRule> _rules = [];
    private readonly List<Round> _rounds = [];
    private readonly List<SessionEvent> _events = [];
    private readonly List<RackScan> _rackScans = [];

    private GameSession() { }

    public GameSession(
        string id,
        string deviceId,
        int schemaVersion,
        string title,
        string gameType,
        string status,
        bool isQuickGame,
        bool isPartnered,
        string winCondition,
        DateTime createdAt,
        DateTime updatedAt,
        DateTime? finishedAt,
        IEnumerable<string> winnerPlayerIds,
        IEnumerable<SessionPlayer> players,
        IEnumerable<SessionRule> rules,
        IEnumerable<Round> rounds,
        IEnumerable<SessionEvent> events,
        IEnumerable<RackScan> rackScans,
        DateTime receivedAt)
    {
        Id = id;
        DeviceId = deviceId;
        SchemaVersion = schemaVersion;
        Title = title;
        GameType = gameType;
        Status = status;
        IsQuickGame = isQuickGame;
        IsPartnered = isPartnered;
        WinCondition = winCondition;
        CreatedAt = createdAt;
        UpdatedAt = updatedAt;
        FinishedAt = status == SessionStatuses.Finished ? finishedAt : null;
        WinnerPlayerIds = status == SessionStatuses.Finished ? winnerPlayerIds.ToList() : [];
        ReceivedAt = receivedAt;

        _players.AddRange(players.OrderBy(p => p.Seat));
        _rules.AddRange(rules);
        _rounds.AddRange(rounds);
        _events.AddRange(events);
        _rackScans.AddRange(rackScans);

        var hand = 0;
        for (var i = 0; i < _rounds.Count; i++)
        {
            var round = _rounds[i];
            round.Place(i, round.CountsAsHand ? ++hand : null);
        }

        // Olaylar zaman sırasına konur; aynı andakiler gönderildiği sırayı korur.
        var ordered = _events.Select((e, i) => (e, i)).OrderBy(x => x.e.OccurredAt).ThenBy(x => x.i).ToList();
        for (var i = 0; i < ordered.Count; i++) ordered[i].e.Place(i);

        PlayerCount = _players.Count;
        HandCount = hand;
        PenaltyCount = _rounds.Count(r => r.Kind == RoundKinds.Penalty);
        EventCount = _events.Count;
        CustomizedRuleCount = _rules.Count(r => r.Customized);
        RackScanCount = _rackScans.Count;
        DurationSeconds = FinishedAt is { } end ? (long)Math.Max(0, (end - CreatedAt).TotalSeconds) : null;
    }

    public string Id { get; private set; } = default!;
    public string DeviceId { get; private set; } = default!;
    public int SchemaVersion { get; private set; }
    public string Title { get; private set; } = default!;
    public string GameType { get; private set; } = default!;
    public string Status { get; private set; } = default!;
    public bool IsQuickGame { get; private set; }
    public bool IsPartnered { get; private set; }
    public string WinCondition { get; private set; } = default!;
    public DateTime CreatedAt { get; private set; }
    public DateTime UpdatedAt { get; private set; }
    public DateTime? FinishedAt { get; private set; }
    public DateTime ReceivedAt { get; private set; }
    public List<string> WinnerPlayerIds { get; private set; } = [];

    public int PlayerCount { get; private set; }
    public int HandCount { get; private set; }
    public int PenaltyCount { get; private set; }
    public int EventCount { get; private set; }
    public int CustomizedRuleCount { get; private set; }
    public int RackScanCount { get; private set; }
    public long? DurationSeconds { get; private set; }

    public IReadOnlyList<SessionPlayer> Players => _players;
    public IReadOnlyList<SessionRule> Rules => _rules;
    public IReadOnlyList<Round> Rounds => _rounds;
    public IReadOnlyList<SessionEvent> Events => _events;
    public IReadOnlyList<RackScan> RackScans => _rackScans;

    public bool IsFinished => Status == SessionStatuses.Finished;

    /// <summary>
    /// Aynı oyunun tekrar gönderilen halinin saklanmaya değer olup olmadığı.
    /// Olay günlüğü yalnızca büyüdüğü için eski bir kopya yeni kaydı ezemez.
    /// </summary>
    public bool Supersedes(DateTime storedUpdatedAt, int storedEventCount) =>
        EventCount > storedEventCount || (EventCount == storedEventCount && UpdatedAt > storedUpdatedAt);

    /// <summary>Oyuncu id -> toplam puan.</summary>
    public IReadOnlyDictionary<string, int> Totals()
    {
        var totals = _players.ToDictionary(p => p.Id, _ => 0);
        foreach (var entry in _rounds.SelectMany(r => r.Entries))
        {
            if (totals.ContainsKey(entry.PlayerId)) totals[entry.PlayerId] += entry.Points;
        }
        return totals;
    }
}

namespace ScoreBoard.Domain.Sessions;

/// <summary>Istaka okuma ekranından bir oyuncunun puanına aktarılan sonuç.</summary>
public sealed class RackScan
{
    private RackScan() { }

    public RackScan(string eventId, string? roundId, string playerId, DateTime occurredAt, int points,
        int tileCount, int recognizedTileCount, int correctedTileCount, bool usedCamera)
    {
        EventId = eventId;
        RoundId = roundId;
        PlayerId = playerId;
        OccurredAt = occurredAt;
        Points = points;
        TileCount = tileCount;
        RecognizedTileCount = recognizedTileCount;
        CorrectedTileCount = correctedTileCount;
        UsedCamera = usedCamera;
    }

    public string SessionId { get; private set; } = default!;
    public string EventId { get; private set; } = default!;
    public string? RoundId { get; private set; }
    public string PlayerId { get; private set; } = default!;
    public DateTime OccurredAt { get; private set; }
    public int Points { get; private set; }
    public int TileCount { get; private set; }
    public int RecognizedTileCount { get; private set; }
    public int CorrectedTileCount { get; private set; }
    public bool UsedCamera { get; private set; }
}

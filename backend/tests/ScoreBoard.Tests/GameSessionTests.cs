using ScoreBoard.Domain.Sessions;

namespace ScoreBoard.Tests;

public sealed class GameSessionTests
{
    private static readonly DateTime Start = new(2026, 10, 18, 19, 0, 0, DateTimeKind.Utc);

    private static GameSession Session(string status = SessionStatuses.Finished, int events = 2) => new(
        id: "g",
        deviceId: "d",
        schemaVersion: 2,
        title: "Masa",
        gameType: GameTypes.YuzBirOkey,
        status: status,
        isQuickGame: false,
        isPartnered: false,
        winCondition: "lowest_total",
        createdAt: Start,
        updatedAt: Start.AddMinutes(30),
        finishedAt: Start.AddMinutes(30),
        winnerPlayerIds: ["a"],
        players: [new SessionPlayer("b", "B", 1), new SessionPlayer("a", "A", 0)],
        rules: [new SessionRule("penalty", 150, customized: true)],
        rounds:
        [
            new Round("r1", RoundKinds.Penalty, Start, "penalty", "b", [new RoundEntry("a", 0, ScoreSources.Rule), new RoundEntry("b", 150, ScoreSources.Rule)]),
            new Round("r2", RoundKinds.Hand, Start, null, null, [new RoundEntry("a", 10, ScoreSources.Manual), new RoundEntry("b", 20, ScoreSources.Manual)]),
        ],
        events: Enumerable.Range(0, events)
            .Select(i => new SessionEvent($"e{i}", SessionEventTypes.RoundAdded, Start.AddMinutes(events - i), "{}")),
        rackScans: [],
        receivedAt: Start.AddHours(1));

    [Fact]
    public void Derives_counts_hand_numbers_and_event_order()
    {
        var session = Session();
        Assert.Equal(["A", "B"], session.Players.Select(p => p.Name));
        Assert.Equal([null, 1], session.Rounds.Select(r => r.HandNumber));
        Assert.Equal((1, 1, 1), (session.HandCount, session.PenaltyCount, session.CustomizedRuleCount));
        Assert.Equal(1800, session.DurationSeconds);
        // Olaylar zaman sırasına konur.
        Assert.Equal(["e1", "e0"], session.Events.OrderBy(e => e.Sequence).Select(e => e.Id));
        Assert.Equal(new Dictionary<string, int> { ["a"] = 10, ["b"] = 170 }, session.Totals());
    }

    [Fact]
    public void Active_session_has_no_winner_or_duration()
    {
        var session = Session(SessionStatuses.Active);
        Assert.Empty(session.WinnerPlayerIds);
        Assert.Null(session.FinishedAt);
        Assert.Null(session.DurationSeconds);
    }

    [Fact]
    public void Only_a_longer_log_or_later_update_supersedes()
    {
        var session = Session(events: 3);
        Assert.True(session.Supersedes(session.UpdatedAt, 2));
        Assert.False(session.Supersedes(session.UpdatedAt, 3));
        Assert.True(session.Supersedes(session.UpdatedAt.AddMinutes(-1), 3));
        Assert.False(session.Supersedes(session.UpdatedAt.AddMinutes(1), 4));
    }
}

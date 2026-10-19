namespace ScoreBoard.Domain.Sessions;

public sealed class SessionPlayer
{
    private SessionPlayer() { }

    public SessionPlayer(string id, string name, int seat)
    {
        Id = id;
        Name = name;
        Seat = seat;
    }

    public string SessionId { get; private set; } = default!;
    public string Id { get; private set; } = default!;
    public string Name { get; private set; } = default!;
    public int Seat { get; private set; }
}

namespace ScoreBoard.Domain.Sessions;

/// <summary>Oyunda kullanılan kural değeri; varsayılandan farklıysa <see cref="Customized"/>.</summary>
public sealed class SessionRule
{
    private SessionRule() { }

    public SessionRule(string key, int value, bool customized)
    {
        Key = key;
        Value = value;
        Customized = customized;
    }

    public string SessionId { get; private set; } = default!;
    public string Key { get; private set; } = default!;
    public int Value { get; private set; }
    public bool Customized { get; private set; }
}

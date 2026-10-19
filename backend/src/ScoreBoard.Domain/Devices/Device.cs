namespace ScoreBoard.Domain.Devices;

/// <summary>Uygulamanın kurulu olduğu cihaz. Kimlik uygulama tarafından bir kez üretilir.</summary>
public sealed class Device
{
    private Device() { }

    public Device(string id, DateTime seenAt, string? platform, string? appVersion)
    {
        Id = id;
        FirstSeenAt = seenAt;
        LastSeenAt = seenAt;
        Platform = platform;
        AppVersion = appVersion;
    }

    public string Id { get; private set; } = default!;
    public DateTime FirstSeenAt { get; private set; }
    public DateTime LastSeenAt { get; private set; }
    public string? Platform { get; private set; }
    public string? AppVersion { get; private set; }

    public void Seen(DateTime at, string? platform, string? appVersion)
    {
        if (at > LastSeenAt) LastSeenAt = at;
        Platform = platform ?? Platform;
        AppVersion = appVersion ?? AppVersion;
    }
}

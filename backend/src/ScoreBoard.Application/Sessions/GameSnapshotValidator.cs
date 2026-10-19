namespace ScoreBoard.Application.Sessions;

public static class GameSnapshotValidator
{
    public const int SupportedSchemaVersion = 2;
    public const int MaxPlayers = 12;
    public const int MaxRounds = 2000;
    public const int MaxEvents = 10000;
    private const int MaxIdLength = 64;
    private const int MaxTextLength = 200;

    public static Dictionary<string, string[]> Validate(GameSnapshot game, DeviceInfo device)
    {
        var errors = new Dictionary<string, List<string>>();
        void Add(string field, string message)
        {
            if (!errors.TryGetValue(field, out var list)) errors[field] = list = [];
            list.Add(message);
        }

        if (!IsId(device.DeviceId)) Add("device_id", "Geçerli bir cihaz kimliği gerekli.");
        if (game.SchemaVersion != SupportedSchemaVersion)
            Add("schema_version", $"Desteklenen sürüm {SupportedSchemaVersion}.");
        if (!IsId(game.Id)) Add("id", "Geçerli bir oyun kimliği gerekli.");
        if (string.IsNullOrWhiteSpace(game.GameType) || game.GameType.Length > MaxIdLength)
            Add("game_type", "Oyun türü gerekli.");
        if ((game.Title?.Length ?? 0) > MaxTextLength) Add("title", "Başlık çok uzun.");
        if (game.UpdatedAt < game.CreatedAt) Add("updated_at", "Güncelleme zamanı oluşturmadan önce olamaz.");

        var players = game.Players ?? [];
        if (players.Count is 0 or > MaxPlayers) Add("players", $"1 ile {MaxPlayers} arası oyuncu olmalı.");
        if (players.Any(p => !IsId(p.Id) || (p.Name?.Length ?? 0) > MaxTextLength))
            Add("players", "Oyuncu kimliği ya da adı geçersiz.");
        var playerIds = players.Select(p => p.Id).ToHashSet();
        if (playerIds.Count != players.Count) Add("players", "Oyuncu kimlikleri tekrar ediyor.");

        var rounds = game.Rounds ?? [];
        if (rounds.Count > MaxRounds) Add("rounds", "Çok fazla el var.");
        if (rounds.Select(r => r.Id).Distinct().Count() != rounds.Count || rounds.Any(r => !IsId(r.Id)))
            Add("rounds", "El kimlikleri geçersiz ya da tekrar ediyor.");
        foreach (var round in rounds)
        {
            var entries = round.Entries ?? [];
            if (entries.Any(e => !playerIds.Contains(e.PlayerId)) ||
                entries.Select(e => e.PlayerId).Distinct().Count() != entries.Count)
            {
                Add("rounds", $"'{round.Id}' elinde bilinmeyen ya da tekrar eden oyuncu var.");
            }
        }

        var events = game.Events ?? [];
        if (events.Count > MaxEvents) Add("events", "Çok fazla olay var.");
        if (events.Select(e => e.Id).Distinct().Count() != events.Count ||
            events.Any(e => !IsId(e.Id) || string.IsNullOrWhiteSpace(e.Type) || e.Type.Length > MaxIdLength))
        {
            Add("events", "Olay kimlikleri ya da türleri geçersiz.");
        }

        var rules = game.Settings?.Rules ?? [];
        if (rules.Select(r => r.Key).Distinct().Count() != rules.Count ||
            rules.Any(r => string.IsNullOrWhiteSpace(r.Key) || r.Key.Length > MaxIdLength))
        {
            Add("settings.rules", "Kural anahtarları geçersiz ya da tekrar ediyor.");
        }

        return errors.ToDictionary(kv => kv.Key, kv => kv.Value.ToArray());
    }

    private static bool IsId(string? value) => !string.IsNullOrWhiteSpace(value) && value.Length <= MaxIdLength;
}

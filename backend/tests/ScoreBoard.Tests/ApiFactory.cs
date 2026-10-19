using System.Net.Http.Json;
using System.Text.Json;
using System.Text.Json.Nodes;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;
using ScoreBoard.Infrastructure.Persistence;

namespace ScoreBoard.Tests;

/// <summary>
/// API'yi gerçek veritabanıyla ayağa kaldırır. Varsayılan geçici bir SQLite dosyasıdır;
/// <c>SCOREBOARD_TEST_POSTGRES</c> bir bağlantı cümlesi içeriyorsa PostgreSQL ve migration'lar kullanılır.
/// </summary>
public sealed class ApiFactory : WebApplicationFactory<Program>
{
    public const string IngestKey = "test-ingest-key-0123456789";
    public const string PanelKey = "test-panel-key-0123456789";

    private readonly string? _sqlitePath;
    private readonly string _connectionString;
    private readonly bool _postgres;

    public ApiFactory()
    {
        var postgres = Environment.GetEnvironmentVariable("SCOREBOARD_TEST_POSTGRES");
        if (!string.IsNullOrWhiteSpace(postgres))
        {
            _postgres = true;
            // Her fabrika kendi veritabanını kullanır; testler paralel çalışabilir.
            _connectionString = $"{postgres};Database=scoreboard_test_{Guid.NewGuid():N}";
        }
        else
        {
            _sqlitePath = Path.Combine(Path.GetTempPath(), $"scoreboard-test-{Guid.NewGuid():N}.db");
            _connectionString = $"Data Source={_sqlitePath};Pooling=False";
        }
    }

    protected override void ConfigureWebHost(IWebHostBuilder builder)
    {
        builder.UseEnvironment("Testing");
        builder.UseSetting("Database:Provider", _postgres ? "Postgres" : "Sqlite");
        builder.UseSetting("ConnectionStrings:ScoreBoard", _connectionString);
        builder.UseSetting("Auth:IngestKey", IngestKey);
        builder.UseSetting("Auth:PanelKey", PanelKey);
    }

    public HttpClient Device(string deviceId = "device-1")
    {
        var client = CreateClient();
        client.DefaultRequestHeaders.Add("X-Api-Key", IngestKey);
        client.DefaultRequestHeaders.Add("X-Device-Id", deviceId);
        client.DefaultRequestHeaders.Add("X-Platform", "android");
        client.DefaultRequestHeaders.Add("X-App-Version", "2.0.0+9");
        return client;
    }

    public HttpClient Panel()
    {
        var client = CreateClient();
        client.DefaultRequestHeaders.Add("X-Panel-Key", PanelKey);
        return client;
    }

    public override async ValueTask DisposeAsync()
    {
        if (_postgres)
        {
            await using var scope = Services.CreateAsyncScope();
            await scope.ServiceProvider.GetRequiredService<ScoreBoardDbContext>().Database.EnsureDeletedAsync();
        }
        await base.DisposeAsync();
        if (_sqlitePath is not null)
        {
            foreach (var file in new[] { _sqlitePath, $"{_sqlitePath}-wal", $"{_sqlitePath}-shm" })
            {
                if (File.Exists(file)) File.Delete(file);
            }
        }
    }
}

public static class Fixtures
{
    /// <summary>
    /// Mobil uygulamanın GameEngine'i ile üretilmiş örnek 101 oyunu
    /// (mobile/tool/export_sample_game.dart). Mobil testler de aynı dosyayı okur.
    /// </summary>
    public static JsonObject SampleGame() =>
        JsonNode.Parse(File.ReadAllText(Path.Combine(AppContext.BaseDirectory, "Fixtures", "sample-game.v2.json")))!
            .AsObject();

    public static Task<HttpResponseMessage> PutGame(this HttpClient client, JsonObject game) =>
        client.PutAsJsonAsync($"/api/v1/sessions/{game["id"]}", game);

    public static async Task<JsonElement> Json(this HttpResponseMessage response)
    {
        var text = await response.Content.ReadAsStringAsync();
        Assert.True(response.IsSuccessStatusCode, $"{(int)response.StatusCode}: {text}");
        return JsonDocument.Parse(text).RootElement;
    }
}

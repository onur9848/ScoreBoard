using System.Net;
using System.Net.Http.Json;
using System.Text;
using System.Text.Json.Nodes;

namespace ScoreBoard.Tests;

public sealed class SessionIngestTests : IClassFixture<ApiFactory>
{
    private readonly ApiFactory _factory;

    public SessionIngestTests(ApiFactory factory) => _factory = factory;

    private static JsonObject NewGame()
    {
        var game = Fixtures.SampleGame();
        game["id"] = Guid.NewGuid().ToString();
        return game;
    }

    [Fact]
    public async Task Same_snapshot_is_idempotent()
    {
        var client = _factory.Device();
        var game = NewGame();

        var created = await client.PutGame(game);
        Assert.Equal(HttpStatusCode.Created, created.StatusCode);
        Assert.Equal("created", (await created.Json()).GetProperty("outcome").GetString());

        var again = await client.PutGame(game);
        Assert.Equal("unchanged", (await again.Json()).GetProperty("outcome").GetString());
    }

    [Fact]
    public async Task Newer_snapshot_replaces_and_older_one_is_ignored()
    {
        var client = _factory.Device();
        var game = NewGame();
        var id = game["id"]!.GetValue<string>();

        // Önce oyunun ilk hali: yalnızca oluşturma olayı, el yok.
        var early = game.DeepClone().AsObject();
        early["status"] = "active";
        early["finished_at"] = null;
        early["winner_player_ids"] = new JsonArray();
        early["updated_at"] = game["created_at"]!.DeepClone();
        early["rounds"] = new JsonArray();
        early["events"] = new JsonArray(game["events"]![0]!.DeepClone());
        Assert.Equal(HttpStatusCode.Created, (await client.PutGame(early)).StatusCode);

        Assert.Equal("updated", (await (await client.PutGame(game)).Json()).GetProperty("outcome").GetString());
        // Geç gelen eski kopya kaydı geri almaz.
        Assert.Equal("unchanged", (await (await client.PutGame(early)).Json()).GetProperty("outcome").GetString());

        var detail = await (await _factory.Panel().GetAsync($"/api/v1/sessions/{id}")).Json();
        Assert.Equal("finished", detail.GetProperty("summary").GetProperty("status").GetString());
        Assert.Equal(3, detail.GetProperty("rounds").GetArrayLength());
    }

    [Fact]
    public async Task Game_of_another_device_is_rejected()
    {
        var game = NewGame();
        await _factory.Device("device-a").PutGame(game);
        var response = await _factory.Device("device-b").PutGame(game);
        Assert.Equal(HttpStatusCode.Conflict, response.StatusCode);
    }

    [Fact]
    public async Task Keys_are_required_and_not_interchangeable()
    {
        var game = NewGame();
        var anonymous = _factory.CreateClient();
        Assert.Equal(HttpStatusCode.Unauthorized, (await anonymous.PutGame(game)).StatusCode);
        Assert.Equal(HttpStatusCode.Unauthorized, (await anonymous.GetAsync("/api/v1/sessions")).StatusCode);

        var panel = _factory.Panel();
        Assert.Equal(HttpStatusCode.Unauthorized, (await panel.PutGame(game)).StatusCode);
        var device = _factory.Device();
        Assert.Equal(HttpStatusCode.Unauthorized, (await device.GetAsync("/api/v1/stats/overview")).StatusCode);
        Assert.Equal(HttpStatusCode.NoContent, (await panel.GetAsync("/api/v1/auth/check")).StatusCode);
    }

    [Fact]
    public async Task Invalid_snapshots_get_field_errors()
    {
        var client = _factory.Device();

        var wrongVersion = NewGame();
        wrongVersion["schema_version"] = 1;
        var response = await client.PutGame(wrongVersion);
        Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
        Assert.Contains("schema_version", await response.Content.ReadAsStringAsync(), StringComparison.Ordinal);

        var unknownPlayer = NewGame();
        unknownPlayer["rounds"]![0]!["entries"]![0]!["player_id"] = "nobody";
        Assert.Equal(HttpStatusCode.BadRequest, (await client.PutGame(unknownPlayer)).StatusCode);

        var mismatch = NewGame();
        var other = await client.PutAsJsonAsync($"/api/v1/sessions/{Guid.NewGuid()}", mismatch);
        Assert.Equal(HttpStatusCode.BadRequest, other.StatusCode);

        var broken = await client.PutAsync($"/api/v1/sessions/x",
            new StringContent("{not json", Encoding.UTF8, "application/json"));
        Assert.Equal(HttpStatusCode.BadRequest, broken.StatusCode);

        var noDevice = _factory.CreateClient();
        noDevice.DefaultRequestHeaders.Add("X-Api-Key", ApiFactory.IngestKey);
        Assert.Equal(HttpStatusCode.BadRequest, (await noDevice.PutGame(NewGame())).StatusCode);
    }

    [Fact]
    public async Task Unknown_event_types_from_newer_apps_are_kept()
    {
        var game = NewGame();
        game["events"]!.AsArray().Add(new JsonObject
        {
            ["id"] = "future-1",
            ["type"] = "tile_photo_shared",
            ["occurred_at"] = "2026-10-18T19:21:00.000Z",
            ["data"] = new JsonObject { ["count"] = 2 },
        });
        await _factory.Device().PutGame(game);

        var detail = await (await _factory.Panel().GetAsync($"/api/v1/sessions/{game["id"]}")).Json();
        var last = detail.GetProperty("events").EnumerateArray().Last();
        Assert.Equal("tile_photo_shared", last.GetProperty("type").GetString());
        Assert.Equal(2, last.GetProperty("data").GetProperty("count").GetInt32());
    }
}

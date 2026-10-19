using System.Net;
using System.Text.Json;
using System.Text.Json.Nodes;

namespace ScoreBoard.Tests;

/// <summary>Her test kendi veritabanıyla başlar; sayılar diğer testlerden etkilenmez.</summary>
public sealed class PanelQueryTests : IAsyncLifetime
{
    private readonly ApiFactory _factory = new();
    private string _yuzBirId = "";

    public async Task InitializeAsync()
    {
        var device = _factory.Device();
        var game = Fixtures.SampleGame();
        _yuzBirId = game["id"]!.GetValue<string>();
        (await device.PutGame(game)).EnsureSuccessStatusCode();

        // Aynı cihazdan devam eden bir genel oyun.
        var generic = new JsonObject
        {
            ["schema_version"] = 2,
            ["id"] = "generic-1",
            ["title"] = "Tavla Akşamı",
            ["game_type"] = "generic",
            ["status"] = "active",
            ["is_quick_game"] = true,
            ["created_at"] = "2026-10-19T08:00:00Z",
            ["updated_at"] = "2026-10-19T08:10:00Z",
            ["settings"] = new JsonObject { ["is_partnered"] = false, ["win_condition"] = "highest_total" },
            ["players"] = new JsonArray(
                new JsonObject { ["id"] = "a", ["name"] = "Ayşe", ["seat"] = 0 },
                new JsonObject { ["id"] = "b", ["name"] = "Bora", ["seat"] = 1 },
                new JsonObject { ["id"] = "c", ["name"] = "Cem", ["seat"] = 2 }),
            ["rounds"] = new JsonArray(new JsonObject
            {
                ["id"] = "r1",
                ["kind"] = "hand",
                ["created_at"] = "2026-10-19T08:10:00Z",
                ["entries"] = new JsonArray(
                    new JsonObject { ["player_id"] = "a", ["points"] = 5, ["source"] = "manual" },
                    new JsonObject { ["player_id"] = "b", ["points"] = 9, ["source"] = "manual" },
                    new JsonObject { ["player_id"] = "c", ["points"] = 9, ["source"] = "manual" }),
            }),
            ["events"] = new JsonArray(),
        };
        (await _factory.Device("device-2").PutGame(generic)).EnsureSuccessStatusCode();
    }

    public Task DisposeAsync() => _factory.DisposeAsync().AsTask();

    [Fact]
    public async Task Detail_has_totals_ranks_and_ordered_timeline()
    {
        var detail = await (await _factory.Panel().GetAsync($"/api/v1/sessions/{_yuzBirId}")).Json();

        var summary = detail.GetProperty("summary");
        Assert.Equal("Cuma Masası", summary.GetProperty("title").GetString());
        Assert.Equal(2, summary.GetProperty("hand_count").GetInt32());
        Assert.Equal(1, summary.GetProperty("penalty_count").GetInt32());
        Assert.Equal(1200, summary.GetProperty("duration_seconds").GetInt64());
        Assert.Equal(["xyz"], summary.GetProperty("winner_names").EnumerateArray().Select(n => n.GetString()));

        // 101'de en düşük toplam kazanır: xyz 44 - 202 = -158, abc 150 + 12 + 404 = 566.
        var players = detail.GetProperty("players").EnumerateArray().ToList();
        Assert.Equal((-158, 1, true), Player(players[0]));
        Assert.Equal((566, 2, false), Player(players[1]));

        var rounds = detail.GetProperty("rounds").EnumerateArray().ToList();
        Assert.Equal(["penalty", "hand", "finish"], rounds.Select(r => r.GetProperty("kind").GetString()));
        Assert.Equal(JsonValueKind.Null, rounds[0].GetProperty("hand_number").ValueKind);
        Assert.Equal(2, rounds[2].GetProperty("hand_number").GetInt32());
        Assert.Equal("handFinish", rounds[2].GetProperty("rule_key").GetString());

        var events = detail.GetProperty("events").EnumerateArray().Select(e => e.GetProperty("type").GetString());
        Assert.Equal(
            ["game_created",
                "round_added",
                "rule_applied",
                "round_added",
                "rack_scan_used",
                "round_edited",
                "round_added",
                "rule_applied",
                "game_finished"],
            events);
        var created = detail.GetProperty("events")[0].GetProperty("data");
        Assert.Equal("penalty", created.GetProperty("customized_rules")[0].GetProperty("key").GetString());

        Assert.Equal("android", detail.GetProperty("device").GetProperty("platform").GetString());
        Assert.Equal(HttpStatusCode.NotFound, (await _factory.Panel().GetAsync("/api/v1/sessions/none")).StatusCode);

        static (int, int, bool) Player(JsonElement p) => (p.GetProperty("total").GetInt32(),
            p.GetProperty("rank").GetInt32(), p.GetProperty("is_winner").GetBoolean());
    }

    [Fact]
    public async Task List_filters_searches_and_pages()
    {
        var panel = _factory.Panel();

        var all = await (await panel.GetAsync("/api/v1/sessions")).Json();
        Assert.Equal(2, all.GetProperty("total_count").GetInt32());
        // En son güncellenen önce gelir.
        Assert.Equal("generic-1", all.GetProperty("items")[0].GetProperty("id").GetString());

        var yuzBir = await (await panel.GetAsync("/api/v1/sessions?gameType=yuzbir_okey&status=finished")).Json();
        Assert.Equal(_yuzBirId, yuzBir.GetProperty("items").EnumerateArray().Single().GetProperty("id").GetString());

        var byPlayer = await (await panel.GetAsync("/api/v1/sessions?q=BORA")).Json();
        Assert.Equal("generic-1", byPlayer.GetProperty("items").EnumerateArray().Single().GetProperty("id").GetString());

        var paged = await (await panel.GetAsync("/api/v1/sessions?page=2&pageSize=1")).Json();
        Assert.Equal(_yuzBirId, paged.GetProperty("items").EnumerateArray().Single().GetProperty("id").GetString());

        var ranged = await (await panel.GetAsync("/api/v1/sessions?from=2026-10-19T00:00:00Z")).Json();
        Assert.Equal(1, ranged.GetProperty("total_count").GetInt32());
    }

    [Fact]
    public async Task Overview_measures_real_usage()
    {
        var stats = await (await _factory.Panel()
            .GetAsync("/api/v1/stats/overview?to=2026-10-19T21:00:00Z&days=3&utcOffsetMinutes=180")).Json();

        var totals = stats.GetProperty("totals");
        Assert.Equal(2, totals.GetProperty("sessions").GetInt32());
        Assert.Equal(1, totals.GetProperty("finished").GetInt32());
        Assert.Equal(1, totals.GetProperty("quick_games").GetInt32());
        Assert.Equal(2, totals.GetProperty("devices").GetInt32());
        Assert.Equal(3, totals.GetProperty("hands").GetInt32());
        Assert.Equal(1200, totals.GetProperty("average_duration_seconds").GetDouble());

        var rules = stats.GetProperty("rule_usage").EnumerateArray()
            .ToDictionary(r => r.GetProperty("rule_key").GetString()!, r => r.GetProperty("count").GetInt32());
        Assert.Equal(new Dictionary<string, int> { ["penalty"] = 1, ["handFinish"] = 1 }, rules);

        var customized = stats.GetProperty("customized_rules").EnumerateArray().Single();
        Assert.Equal(("penalty", 150), (customized.GetProperty("rule_key").GetString(),
            customized.GetProperty("max_value").GetInt32()));

        var rack = stats.GetProperty("rack_scan");
        Assert.Equal(1, rack.GetProperty("scans").GetInt32());
        Assert.Equal(0.333, rack.GetProperty("correction_rate").GetDouble());
        Assert.Equal(1, rack.GetProperty("camera_share").GetDouble());

        var sources = stats.GetProperty("score_sources").EnumerateArray()
            .ToDictionary(s => s.GetProperty("key").GetString()!, s => s.GetProperty("count").GetInt32());
        Assert.Equal(new Dictionary<string, int> { ["manual"] = 5, ["rule"] = 3, ["rack_scan"] = 1 }, sources);

        // Türkiye saatiyle 18 Ekim 22:00 ve 19 Ekim 11:00; aralık 20 Ekim yerel gece yarısında biter.
        var daily = stats.GetProperty("daily").EnumerateArray()
            .Select(d => (d.GetProperty("date").GetString(), d.GetProperty("created").GetInt32())).ToList();
        Assert.Equal([("2026-10-17", 0), ("2026-10-18", 1), ("2026-10-19", 1)], daily);

        var types = stats.GetProperty("game_types").EnumerateArray()
            .Select(t => t.GetProperty("game_type").GetString()).Order().ToList();
        Assert.Equal(["generic", "yuzbir_okey"], types);
    }
}

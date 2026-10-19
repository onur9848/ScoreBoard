using System.Globalization;
using Microsoft.EntityFrameworkCore;
using ScoreBoard.Application.Abstractions;
using ScoreBoard.Domain.Sessions;

namespace ScoreBoard.Application.Stats;

public sealed record StatsQuery(
    DateTime? From = null,
    DateTime? To = null,
    string? GameType = null,
    int UtcOffsetMinutes = 0,
    int Days = 30);

public sealed record OverviewStatsDto(
    TotalsDto Totals,
    IReadOnlyList<GameTypeStatsDto> GameTypes,
    IReadOnlyList<RuleUsageDto> RuleUsage,
    IReadOnlyList<CustomizedRuleDto> CustomizedRules,
    RackScanStatsDto RackScan,
    IReadOnlyList<CountDto> ScoreSources,
    IReadOnlyList<CountDto> PlayerCounts,
    IReadOnlyList<DailyCountDto> Daily);

public sealed record TotalsDto(
    int Sessions,
    int Finished,
    int Active,
    int QuickGames,
    int Partnered,
    int Devices,
    int Hands,
    double AverageHands,
    double AveragePlayers,
    double? AverageDurationSeconds);

public sealed record GameTypeStatsDto(
    string GameType,
    int Sessions,
    int Finished,
    double AverageHands,
    double? AverageDurationSeconds,
    int Partnered,
    int Customized);

public sealed record RuleUsageDto(string GameType, string RuleKey, string Kind, int Count, int Sessions);

public sealed record CustomizedRuleDto(string GameType, string RuleKey, int Sessions, int MinValue, int MaxValue,
    double AverageValue);

public sealed record RackScanStatsDto(
    int Scans,
    int Sessions,
    int Tiles,
    int RecognizedTiles,
    int CorrectedTiles,
    double CorrectionRate,
    double CameraShare);

public sealed record CountDto(string Key, int Count);

public sealed record DailyCountDto(DateOnly Date, int Created, int Finished);

/// <summary>Panelin ana sayfasındaki gerçek kullanım özetleri.</summary>
public sealed class GetOverviewStatsHandler(IApplicationDbContext db, TimeProvider clock)
{
    public const int MaxDays = 180;

    public async Task<OverviewStatsDto> HandleAsync(StatsQuery query, CancellationToken ct)
    {
        var sessions = db.Sessions.AsNoTracking();
        if (query.From is { } from) sessions = sessions.Where(s => s.CreatedAt >= from);
        if (query.To is { } to) sessions = sessions.Where(s => s.CreatedAt < to);
        if (!string.IsNullOrWhiteSpace(query.GameType)) sessions = sessions.Where(s => s.GameType == query.GameType);

        // Oturum başına küçük bir izdüşüm; toplamlar bellekte hesaplanır ve sağlayıcıdan bağımsız kalır.
        var rows = await sessions
            .Select(s => new SessionRow(s.Id, s.DeviceId, s.GameType, s.Status, s.IsQuickGame, s.IsPartnered,
                s.PlayerCount, s.HandCount, s.CustomizedRuleCount, s.DurationSeconds, s.CreatedAt, s.FinishedAt))
            .ToListAsync(ct);
        var finished = rows.Where(r => r.Status == SessionStatuses.Finished).ToList();

        var totals = new TotalsDto(
            Sessions: rows.Count,
            Finished: finished.Count,
            Active: rows.Count - finished.Count,
            QuickGames: rows.Count(r => r.IsQuickGame),
            Partnered: rows.Count(r => r.IsPartnered),
            Devices: rows.Select(r => r.DeviceId).Distinct().Count(),
            Hands: rows.Sum(r => r.HandCount),
            AverageHands: Average(finished.Select(r => (double)r.HandCount)) ?? 0,
            AveragePlayers: Average(rows.Select(r => (double)r.PlayerCount)) ?? 0,
            AverageDurationSeconds: Average(finished.Where(r => r.DurationSeconds is not null)
                .Select(r => (double)r.DurationSeconds!.Value)));

        var gameTypes = rows
            .GroupBy(r => r.GameType)
            .Select(g =>
            {
                var done = g.Where(r => r.Status == SessionStatuses.Finished).ToList();
                return new GameTypeStatsDto(
                    g.Key,
                    g.Count(),
                    done.Count,
                    Average(done.Select(r => (double)r.HandCount)) ?? 0,
                    Average(done.Where(r => r.DurationSeconds is not null).Select(r => (double)r.DurationSeconds!.Value)),
                    g.Count(r => r.IsPartnered),
                    g.Count(r => r.CustomizedRuleCount > 0));
            })
            .OrderByDescending(g => g.Sessions)
            .ToList();

        var ruleRounds = await (
                from r in db.Rounds.AsNoTracking()
                join s in sessions on r.SessionId equals s.Id
                where r.RuleKey != null
                select new { s.GameType, RuleKey = r.RuleKey!, r.Kind, r.SessionId })
            .ToListAsync(ct);
        var ruleUsage = ruleRounds
            .GroupBy(r => (r.GameType, r.RuleKey, r.Kind))
            .Select(g => new RuleUsageDto(g.Key.GameType, g.Key.RuleKey, g.Key.Kind, g.Count(),
                g.Select(x => x.SessionId).Distinct().Count()))
            .OrderByDescending(r => r.Count)
            .ToList();

        var customizedRows = await (
                from rule in db.Rules.AsNoTracking()
                join s in sessions on rule.SessionId equals s.Id
                where rule.Customized
                select new { s.GameType, rule.Key, rule.Value })
            .ToListAsync(ct);
        var customized = customizedRows
            .GroupBy(r => (r.GameType, r.Key))
            .Select(g => new CustomizedRuleDto(g.Key.GameType, g.Key.Key, g.Count(), g.Min(x => x.Value),
                g.Max(x => x.Value), Math.Round(g.Average(x => x.Value), 1)))
            .OrderByDescending(r => r.Sessions)
            .ToList();

        var scans = await (
                from scan in db.RackScans.AsNoTracking()
                join s in sessions on scan.SessionId equals s.Id
                select new { scan.SessionId, scan.TileCount, scan.RecognizedTileCount, scan.CorrectedTileCount, scan.UsedCamera })
            .ToListAsync(ct);
        var tiles = scans.Sum(s => s.TileCount);
        var corrected = scans.Sum(s => s.CorrectedTileCount);
        var rackScan = new RackScanStatsDto(
            scans.Count,
            scans.Select(s => s.SessionId).Distinct().Count(),
            tiles,
            scans.Sum(s => s.RecognizedTileCount),
            corrected,
            tiles == 0 ? 0 : Math.Round((double)corrected / tiles, 3),
            scans.Count == 0 ? 0 : Math.Round((double)scans.Count(s => s.UsedCamera) / scans.Count, 3));

        var scoreSources = await (
                from e in db.RoundEntries.AsNoTracking()
                join s in sessions on e.SessionId equals s.Id
                group e by e.Source into g
                select new CountDto(g.Key, g.Count()))
            .ToListAsync(ct);

        var playerCounts = rows
            .GroupBy(r => r.PlayerCount)
            .OrderBy(g => g.Key)
            .Select(g => new CountDto(g.Key.ToString(CultureInfo.InvariantCulture), g.Count()))
            .ToList();

        return new OverviewStatsDto(totals, gameTypes, ruleUsage, customized, rackScan,
            scoreSources.OrderByDescending(s => s.Count).ToList(), playerCounts, Daily(rows, query));
    }

    private List<DailyCountDto> Daily(List<SessionRow> rows, StatsQuery query)
    {
        var offset = TimeSpan.FromMinutes(Math.Clamp(query.UtcOffsetMinutes, -14 * 60, 14 * 60));
        var days = Math.Clamp(query.Days, 1, MaxDays);
        var end = DateOnly.FromDateTime((query.To?.AddTicks(-1) ?? clock.GetUtcNow().UtcDateTime) + offset);
        var start = end.AddDays(1 - days);
        DateOnly Local(DateTime utc) => DateOnly.FromDateTime(utc + offset);

        var created = rows.GroupBy(r => Local(r.CreatedAt)).ToDictionary(g => g.Key, g => g.Count());
        var finished = rows.Where(r => r.FinishedAt is not null)
            .GroupBy(r => Local(r.FinishedAt!.Value)).ToDictionary(g => g.Key, g => g.Count());
        return Enumerable.Range(0, days)
            .Select(i => start.AddDays(i))
            .Select(d => new DailyCountDto(d, created.GetValueOrDefault(d), finished.GetValueOrDefault(d)))
            .ToList();
    }

    private static double? Average(IEnumerable<double> values)
    {
        var list = values.ToList();
        return list.Count == 0 ? null : Math.Round(list.Average(), 1);
    }

    private sealed record SessionRow(string Id, string DeviceId, string GameType, string Status, bool IsQuickGame,
        bool IsPartnered, int PlayerCount, int HandCount, int CustomizedRuleCount, long? DurationSeconds,
        DateTime CreatedAt, DateTime? FinishedAt);
}

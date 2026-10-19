using Microsoft.EntityFrameworkCore;
using ScoreBoard.Application.Abstractions;
using ScoreBoard.Application.Common;
using ScoreBoard.Domain.Sessions;

namespace ScoreBoard.Application.Sessions;

public sealed record SessionListQuery(
    string? GameType = null,
    string? Status = null,
    string? DeviceId = null,
    DateTime? From = null,
    DateTime? To = null,
    string? Search = null,
    int Page = 1,
    int PageSize = 25);

public sealed class ListSessionsHandler(IApplicationDbContext db)
{
    public const int MaxPageSize = 100;

    public async Task<PagedResult<SessionSummaryDto>> HandleAsync(SessionListQuery query,
        CancellationToken cancellationToken)
    {
        var page = Math.Max(1, query.Page);
        var pageSize = Math.Clamp(query.PageSize, 1, MaxPageSize);

        var sessions = db.Sessions.AsNoTracking();
        if (!string.IsNullOrWhiteSpace(query.GameType)) sessions = sessions.Where(s => s.GameType == query.GameType);
        if (!string.IsNullOrWhiteSpace(query.Status)) sessions = sessions.Where(s => s.Status == query.Status);
        if (!string.IsNullOrWhiteSpace(query.DeviceId)) sessions = sessions.Where(s => s.DeviceId == query.DeviceId);
        if (query.From is { } from) sessions = sessions.Where(s => s.CreatedAt >= from);
        if (query.To is { } to) sessions = sessions.Where(s => s.CreatedAt < to);
        if (!string.IsNullOrWhiteSpace(query.Search))
        {
            var term = query.Search.Trim().ToLowerInvariant();
            // Sorgu SQL'e çevrilir (LOWER); kültür ayarı burada etkisizdir.
#pragma warning disable CA1304, CA1311, CA1862
            sessions = sessions.Where(s =>
                s.Title.ToLower().Contains(term) || s.Players.Any(p => p.Name.ToLower().Contains(term)));
#pragma warning restore CA1304, CA1311, CA1862
        }

        var total = await sessions.CountAsync(cancellationToken);
        var items = await sessions
            .OrderByDescending(s => s.UpdatedAt)
            .ThenBy(s => s.Id)
            .Skip((page - 1) * pageSize)
            .Take(pageSize)
            .Include(s => s.Players)
            .ToListAsync(cancellationToken);

        return new PagedResult<SessionSummaryDto>(items.Select(ToSummary).ToList(), page, pageSize, total);
    }

    internal static SessionSummaryDto ToSummary(GameSession s)
    {
        var names = s.Players.ToDictionary(p => p.Id, p => p.Name);
        return new SessionSummaryDto(
            s.Id,
            s.Title,
            s.GameType,
            s.Status,
            s.IsQuickGame,
            s.IsPartnered,
            s.WinCondition,
            s.Players.OrderBy(p => p.Seat).Select(p => p.Name).ToList(),
            s.WinnerPlayerIds.Select(id => names.GetValueOrDefault(id, id)).ToList(),
            s.PlayerCount,
            s.HandCount,
            s.PenaltyCount,
            s.RackScanCount,
            s.CustomizedRuleCount,
            s.CreatedAt,
            s.UpdatedAt,
            s.FinishedAt,
            s.DurationSeconds,
            s.DeviceId);
    }
}

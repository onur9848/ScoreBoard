namespace ScoreBoard.Application.Common;

public sealed class ValidationException(IReadOnlyDictionary<string, string[]> errors)
    : Exception("Gönderilen veri geçersiz.")
{
    public IReadOnlyDictionary<string, string[]> Errors { get; } = errors;
}

/// <summary>Başka bir cihaza ait oyunun üzerine yazılmak istendiğinde.</summary>
public sealed class ConflictException(string message) : Exception(message);

public sealed record PagedResult<T>(IReadOnlyList<T> Items, int Page, int PageSize, int TotalCount);

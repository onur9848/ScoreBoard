using System.Security.Cryptography;
using System.Text;
using Microsoft.Extensions.Options;
using Microsoft.OpenApi.Models;
using Swashbuckle.AspNetCore.SwaggerGen;

namespace ScoreBoard.Api.Security;

public static class ApiKeyHeaders
{
    /// <summary>Mobil uygulamanın oyun gönderirken kullandığı anahtar.</summary>
    public const string Ingest = "X-Api-Key";

    /// <summary>Panelin okuma için kullandığı anahtar.</summary>
    public const string Panel = "X-Panel-Key";

    public const string DeviceId = "X-Device-Id";
    public const string Platform = "X-Platform";
    public const string AppVersion = "X-App-Version";
}

public sealed class ApiKeyOptions
{
    public string IngestKey { get; set; } = "";
    public string PanelKey { get; set; } = "";
}

public enum ApiKeyScope
{
    Ingest,
    Panel,
}

public static class ApiKeyExtensions
{
    public static IServiceCollection AddApiKeys(this IServiceCollection services, IConfiguration configuration)
    {
        services.AddOptions<ApiKeyOptions>()
            .Bind(configuration.GetSection("Auth"))
            .Validate(o => o.IngestKey.Length >= 16 && o.PanelKey.Length >= 16,
                "Auth:IngestKey ve Auth:PanelKey en az 16 karakter olmalı.")
            .Validate(o => o.IngestKey != o.PanelKey, "Auth:IngestKey ve Auth:PanelKey farklı olmalı.")
            .ValidateOnStart();
        return services;
    }

    public static TBuilder RequireApiKey<TBuilder>(this TBuilder builder, ApiKeyScope scope)
        where TBuilder : IEndpointConventionBuilder =>
        builder.AddEndpointFilter(new ApiKeyFilter(scope));

    public static void AddApiKeySecurity(this SwaggerGenOptions o)
    {
        foreach (var header in new[] { ApiKeyHeaders.Ingest, ApiKeyHeaders.Panel })
        {
            o.AddSecurityDefinition(header, new OpenApiSecurityScheme
            {
                Type = SecuritySchemeType.ApiKey,
                In = ParameterLocation.Header,
                Name = header,
            });
        }
    }
}

internal sealed class ApiKeyFilter(ApiKeyScope scope) : IEndpointFilter
{
    public ValueTask<object?> InvokeAsync(EndpointFilterInvocationContext context, EndpointFilterDelegate next)
    {
        var http = context.HttpContext;
        var options = http.RequestServices.GetRequiredService<IOptions<ApiKeyOptions>>().Value;
        var (header, expected) = scope == ApiKeyScope.Ingest
            ? (ApiKeyHeaders.Ingest, options.IngestKey)
            : (ApiKeyHeaders.Panel, options.PanelKey);

        var provided = http.Request.Headers[header].ToString();
        if (!FixedTimeEquals(provided, expected))
        {
            return ValueTask.FromResult<object?>(Results.Problem(statusCode: StatusCodes.Status401Unauthorized,
                title: "Geçersiz ya da eksik anahtar."));
        }
        return next(context);
    }

    private static bool FixedTimeEquals(string provided, string expected)
    {
        var a = SHA256.HashData(Encoding.UTF8.GetBytes(provided));
        var b = SHA256.HashData(Encoding.UTF8.GetBytes(expected));
        return CryptographicOperations.FixedTimeEquals(a, b) && expected.Length > 0;
    }
}

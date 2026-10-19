using System.Text.Json;
using Microsoft.AspNetCore.Http.Json;
using ScoreBoard.Api.Endpoints;
using ScoreBoard.Api.Security;
using ScoreBoard.Application;
using ScoreBoard.Infrastructure;
using ScoreBoard.Infrastructure.Persistence;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddApplication();
builder.Services.AddInfrastructure(builder.Configuration);
builder.Services.AddApiKeys(builder.Configuration);

// Mobil sözleşme snake_case'tir; panel de aynı adları kullanır.
builder.Services.Configure<JsonOptions>(o =>
{
    o.SerializerOptions.PropertyNamingPolicy = JsonNamingPolicy.SnakeCaseLower;
    o.SerializerOptions.DictionaryKeyPolicy = JsonNamingPolicy.SnakeCaseLower;
});
builder.Services.AddProblemDetails();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(o => o.AddApiKeySecurity());
builder.Services.AddHealthChecks();

var origins = builder.Configuration.GetSection("Cors:AllowedOrigins").Get<string[]>() ?? [];
builder.Services.AddCors(o => o.AddDefaultPolicy(p => p
    .WithOrigins(origins)
    .WithHeaders(ApiKeyHeaders.Panel, "Content-Type")
    .WithMethods("GET")));

var app = builder.Build();

app.UseExceptionHandler();
app.UseStatusCodePages();
if (app.Configuration.GetValue("Swagger:Enabled", app.Environment.IsDevelopment()))
{
    app.UseSwagger();
    app.UseSwaggerUI();
}
app.UseCors();

if (app.Configuration.GetValue("Database:MigrateOnStartup", true))
{
    await using var scope = app.Services.CreateAsyncScope();
    await scope.ServiceProvider.GetRequiredService<DatabaseInitializer>().InitializeAsync();
}

app.MapHealthChecks("/health");
var api = app.MapGroup("/api/v1");
api.MapIngestEndpoints();
api.MapPanelEndpoints();

app.Run();

public partial class Program;

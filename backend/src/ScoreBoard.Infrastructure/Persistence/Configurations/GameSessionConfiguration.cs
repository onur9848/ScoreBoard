using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using ScoreBoard.Domain.Sessions;

namespace ScoreBoard.Infrastructure.Persistence.Configurations;

internal sealed class GameSessionConfiguration : IEntityTypeConfiguration<GameSession>
{
    public void Configure(EntityTypeBuilder<GameSession> b)
    {
        b.ToTable("game_sessions");
        b.HasKey(s => s.Id);
        b.Property(s => s.Id).HasMaxLength(64);
        b.Property(s => s.DeviceId).HasMaxLength(64).IsRequired();
        b.Property(s => s.Title).HasMaxLength(200).IsRequired();
        b.Property(s => s.GameType).HasMaxLength(64).IsRequired();
        b.Property(s => s.Status).HasMaxLength(16).IsRequired();
        b.Property(s => s.WinCondition).HasMaxLength(32).IsRequired();
        b.Property(s => s.WinnerPlayerIds);
        b.Ignore(s => s.IsFinished);

        b.HasIndex(s => s.UpdatedAt);
        b.HasIndex(s => s.CreatedAt);
        b.HasIndex(s => new { s.GameType, s.CreatedAt });
        b.HasIndex(s => s.DeviceId);

        b.HasMany(s => s.Players).WithOne().HasForeignKey(p => p.SessionId).OnDelete(DeleteBehavior.Cascade);
        b.HasMany(s => s.Rules).WithOne().HasForeignKey(r => r.SessionId).OnDelete(DeleteBehavior.Cascade);
        b.HasMany(s => s.Rounds).WithOne().HasForeignKey(r => r.SessionId).OnDelete(DeleteBehavior.Cascade);
        b.HasMany(s => s.Events).WithOne().HasForeignKey(e => e.SessionId).OnDelete(DeleteBehavior.Cascade);
        b.HasMany(s => s.RackScans).WithOne().HasForeignKey(r => r.SessionId).OnDelete(DeleteBehavior.Cascade);
        foreach (var navigation in new[] { "Players", "Rules", "Rounds", "Events", "RackScans" })
        {
            b.Navigation(navigation).UsePropertyAccessMode(PropertyAccessMode.Field);
        }
    }
}

internal sealed class SessionPlayerConfiguration : IEntityTypeConfiguration<SessionPlayer>
{
    public void Configure(EntityTypeBuilder<SessionPlayer> b)
    {
        b.ToTable("session_players");
        b.HasKey(p => new { p.SessionId, p.Id });
        b.Property(p => p.Id).HasMaxLength(64);
        b.Property(p => p.Name).HasMaxLength(200).IsRequired();
    }
}

internal sealed class SessionRuleConfiguration : IEntityTypeConfiguration<SessionRule>
{
    public void Configure(EntityTypeBuilder<SessionRule> b)
    {
        b.ToTable("session_rules");
        b.HasKey(r => new { r.SessionId, r.Key });
        b.Property(r => r.Key).HasMaxLength(64);
    }
}

internal sealed class RoundConfiguration : IEntityTypeConfiguration<Round>
{
    public void Configure(EntityTypeBuilder<Round> b)
    {
        b.ToTable("rounds");
        b.HasKey(r => new { r.SessionId, r.Id });
        b.Property(r => r.Id).HasMaxLength(64);
        b.Property(r => r.Kind).HasMaxLength(16).IsRequired();
        b.Property(r => r.RuleKey).HasMaxLength(64);
        b.Property(r => r.ActorPlayerId).HasMaxLength(64);
        b.Ignore(r => r.CountsAsHand);
        b.HasIndex(r => r.RuleKey);
        b.HasMany(r => r.Entries).WithOne()
            .HasForeignKey(e => new { e.SessionId, e.RoundId })
            .OnDelete(DeleteBehavior.Cascade);
        b.Navigation(r => r.Entries).UsePropertyAccessMode(PropertyAccessMode.Field);
    }
}

internal sealed class RoundEntryConfiguration : IEntityTypeConfiguration<RoundEntry>
{
    public void Configure(EntityTypeBuilder<RoundEntry> b)
    {
        b.ToTable("round_entries");
        b.HasKey(e => new { e.SessionId, e.RoundId, e.PlayerId });
        b.Property(e => e.RoundId).HasMaxLength(64);
        b.Property(e => e.PlayerId).HasMaxLength(64);
        b.Property(e => e.Source).HasMaxLength(16).IsRequired();
    }
}

internal sealed class SessionEventConfiguration : IEntityTypeConfiguration<SessionEvent>
{
    public void Configure(EntityTypeBuilder<SessionEvent> b)
    {
        b.ToTable("session_events");
        b.HasKey(e => new { e.SessionId, e.Id });
        b.Property(e => e.Id).HasMaxLength(64);
        b.Property(e => e.Type).HasMaxLength(64).IsRequired();
        b.Property(e => e.RoundId).HasMaxLength(64);
        b.Property(e => e.RuleKey).HasMaxLength(64);
        b.Property(e => e.PlayerId).HasMaxLength(64);
        b.Property(e => e.DataJson).IsRequired();
        b.HasIndex(e => e.Type);
    }
}

internal sealed class RackScanConfiguration : IEntityTypeConfiguration<RackScan>
{
    public void Configure(EntityTypeBuilder<RackScan> b)
    {
        b.ToTable("rack_scans");
        b.HasKey(r => new { r.SessionId, r.EventId });
        b.Property(r => r.EventId).HasMaxLength(64);
        b.Property(r => r.RoundId).HasMaxLength(64);
        b.Property(r => r.PlayerId).HasMaxLength(64).IsRequired();
    }
}

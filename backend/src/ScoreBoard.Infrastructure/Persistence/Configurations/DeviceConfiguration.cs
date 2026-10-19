using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using ScoreBoard.Domain.Devices;

namespace ScoreBoard.Infrastructure.Persistence.Configurations;

internal sealed class DeviceConfiguration : IEntityTypeConfiguration<Device>
{
    public void Configure(EntityTypeBuilder<Device> b)
    {
        b.ToTable("devices");
        b.HasKey(d => d.Id);
        b.Property(d => d.Id).HasMaxLength(64);
        b.Property(d => d.Platform).HasMaxLength(32);
        b.Property(d => d.AppVersion).HasMaxLength(32);
    }
}

using System;
using System.Collections.Generic;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace ScoreBoard.Infrastructure.Persistence.Migrations
{
    /// <inheritdoc />
    public partial class InitialCreate : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.CreateTable(
                name: "devices",
                columns: table => new
                {
                    id = table.Column<string>(type: "character varying(64)", maxLength: 64, nullable: false),
                    first_seen_at = table.Column<DateTime>(type: "timestamp with time zone", nullable: false),
                    last_seen_at = table.Column<DateTime>(type: "timestamp with time zone", nullable: false),
                    platform = table.Column<string>(type: "character varying(32)", maxLength: 32, nullable: true),
                    app_version = table.Column<string>(type: "character varying(32)", maxLength: 32, nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("pk_devices", x => x.id);
                });

            migrationBuilder.CreateTable(
                name: "game_sessions",
                columns: table => new
                {
                    id = table.Column<string>(type: "character varying(64)", maxLength: 64, nullable: false),
                    device_id = table.Column<string>(type: "character varying(64)", maxLength: 64, nullable: false),
                    schema_version = table.Column<int>(type: "integer", nullable: false),
                    title = table.Column<string>(type: "character varying(200)", maxLength: 200, nullable: false),
                    game_type = table.Column<string>(type: "character varying(64)", maxLength: 64, nullable: false),
                    status = table.Column<string>(type: "character varying(16)", maxLength: 16, nullable: false),
                    is_quick_game = table.Column<bool>(type: "boolean", nullable: false),
                    is_partnered = table.Column<bool>(type: "boolean", nullable: false),
                    win_condition = table.Column<string>(type: "character varying(32)", maxLength: 32, nullable: false),
                    created_at = table.Column<DateTime>(type: "timestamp with time zone", nullable: false),
                    updated_at = table.Column<DateTime>(type: "timestamp with time zone", nullable: false),
                    finished_at = table.Column<DateTime>(type: "timestamp with time zone", nullable: true),
                    received_at = table.Column<DateTime>(type: "timestamp with time zone", nullable: false),
                    winner_player_ids = table.Column<List<string>>(type: "text[]", nullable: false),
                    player_count = table.Column<int>(type: "integer", nullable: false),
                    hand_count = table.Column<int>(type: "integer", nullable: false),
                    penalty_count = table.Column<int>(type: "integer", nullable: false),
                    event_count = table.Column<int>(type: "integer", nullable: false),
                    customized_rule_count = table.Column<int>(type: "integer", nullable: false),
                    rack_scan_count = table.Column<int>(type: "integer", nullable: false),
                    duration_seconds = table.Column<long>(type: "bigint", nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("pk_game_sessions", x => x.id);
                });

            migrationBuilder.CreateTable(
                name: "rack_scans",
                columns: table => new
                {
                    session_id = table.Column<string>(type: "character varying(64)", nullable: false),
                    event_id = table.Column<string>(type: "character varying(64)", maxLength: 64, nullable: false),
                    round_id = table.Column<string>(type: "character varying(64)", maxLength: 64, nullable: true),
                    player_id = table.Column<string>(type: "character varying(64)", maxLength: 64, nullable: false),
                    occurred_at = table.Column<DateTime>(type: "timestamp with time zone", nullable: false),
                    points = table.Column<int>(type: "integer", nullable: false),
                    tile_count = table.Column<int>(type: "integer", nullable: false),
                    recognized_tile_count = table.Column<int>(type: "integer", nullable: false),
                    corrected_tile_count = table.Column<int>(type: "integer", nullable: false),
                    used_camera = table.Column<bool>(type: "boolean", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("pk_rack_scans", x => new { x.session_id, x.event_id });
                    table.ForeignKey(
                        name: "fk_rack_scans_game_sessions_session_id",
                        column: x => x.session_id,
                        principalTable: "game_sessions",
                        principalColumn: "id",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateTable(
                name: "rounds",
                columns: table => new
                {
                    session_id = table.Column<string>(type: "character varying(64)", nullable: false),
                    id = table.Column<string>(type: "character varying(64)", maxLength: 64, nullable: false),
                    position = table.Column<int>(type: "integer", nullable: false),
                    hand_number = table.Column<int>(type: "integer", nullable: true),
                    kind = table.Column<string>(type: "character varying(16)", maxLength: 16, nullable: false),
                    rule_key = table.Column<string>(type: "character varying(64)", maxLength: 64, nullable: true),
                    actor_player_id = table.Column<string>(type: "character varying(64)", maxLength: 64, nullable: true),
                    created_at = table.Column<DateTime>(type: "timestamp with time zone", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("pk_rounds", x => new { x.session_id, x.id });
                    table.ForeignKey(
                        name: "fk_rounds_game_sessions_session_id",
                        column: x => x.session_id,
                        principalTable: "game_sessions",
                        principalColumn: "id",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateTable(
                name: "session_events",
                columns: table => new
                {
                    session_id = table.Column<string>(type: "character varying(64)", nullable: false),
                    id = table.Column<string>(type: "character varying(64)", maxLength: 64, nullable: false),
                    sequence = table.Column<int>(type: "integer", nullable: false),
                    type = table.Column<string>(type: "character varying(64)", maxLength: 64, nullable: false),
                    occurred_at = table.Column<DateTime>(type: "timestamp with time zone", nullable: false),
                    round_id = table.Column<string>(type: "character varying(64)", maxLength: 64, nullable: true),
                    rule_key = table.Column<string>(type: "character varying(64)", maxLength: 64, nullable: true),
                    player_id = table.Column<string>(type: "character varying(64)", maxLength: 64, nullable: true),
                    data_json = table.Column<string>(type: "text", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("pk_session_events", x => new { x.session_id, x.id });
                    table.ForeignKey(
                        name: "fk_session_events_game_sessions_session_id",
                        column: x => x.session_id,
                        principalTable: "game_sessions",
                        principalColumn: "id",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateTable(
                name: "session_players",
                columns: table => new
                {
                    session_id = table.Column<string>(type: "character varying(64)", nullable: false),
                    id = table.Column<string>(type: "character varying(64)", maxLength: 64, nullable: false),
                    name = table.Column<string>(type: "character varying(200)", maxLength: 200, nullable: false),
                    seat = table.Column<int>(type: "integer", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("pk_session_players", x => new { x.session_id, x.id });
                    table.ForeignKey(
                        name: "fk_session_players_game_sessions_session_id",
                        column: x => x.session_id,
                        principalTable: "game_sessions",
                        principalColumn: "id",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateTable(
                name: "session_rules",
                columns: table => new
                {
                    session_id = table.Column<string>(type: "character varying(64)", nullable: false),
                    key = table.Column<string>(type: "character varying(64)", maxLength: 64, nullable: false),
                    value = table.Column<int>(type: "integer", nullable: false),
                    customized = table.Column<bool>(type: "boolean", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("pk_session_rules", x => new { x.session_id, x.key });
                    table.ForeignKey(
                        name: "fk_session_rules_game_sessions_session_id",
                        column: x => x.session_id,
                        principalTable: "game_sessions",
                        principalColumn: "id",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateTable(
                name: "round_entries",
                columns: table => new
                {
                    session_id = table.Column<string>(type: "character varying(64)", nullable: false),
                    round_id = table.Column<string>(type: "character varying(64)", maxLength: 64, nullable: false),
                    player_id = table.Column<string>(type: "character varying(64)", maxLength: 64, nullable: false),
                    points = table.Column<int>(type: "integer", nullable: false),
                    source = table.Column<string>(type: "character varying(16)", maxLength: 16, nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("pk_round_entries", x => new { x.session_id, x.round_id, x.player_id });
                    table.ForeignKey(
                        name: "fk_round_entries_rounds_session_id_round_id",
                        columns: x => new { x.session_id, x.round_id },
                        principalTable: "rounds",
                        principalColumns: new[] { "session_id", "id" },
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateIndex(
                name: "ix_game_sessions_created_at",
                table: "game_sessions",
                column: "created_at");

            migrationBuilder.CreateIndex(
                name: "ix_game_sessions_device_id",
                table: "game_sessions",
                column: "device_id");

            migrationBuilder.CreateIndex(
                name: "ix_game_sessions_game_type_created_at",
                table: "game_sessions",
                columns: new[] { "game_type", "created_at" });

            migrationBuilder.CreateIndex(
                name: "ix_game_sessions_updated_at",
                table: "game_sessions",
                column: "updated_at");

            migrationBuilder.CreateIndex(
                name: "ix_rounds_rule_key",
                table: "rounds",
                column: "rule_key");

            migrationBuilder.CreateIndex(
                name: "ix_session_events_type",
                table: "session_events",
                column: "type");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "devices");

            migrationBuilder.DropTable(
                name: "rack_scans");

            migrationBuilder.DropTable(
                name: "round_entries");

            migrationBuilder.DropTable(
                name: "session_events");

            migrationBuilder.DropTable(
                name: "session_players");

            migrationBuilder.DropTable(
                name: "session_rules");

            migrationBuilder.DropTable(
                name: "rounds");

            migrationBuilder.DropTable(
                name: "game_sessions");
        }
    }
}

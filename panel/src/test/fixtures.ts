import type { SessionDetail } from '../api/types';

/**
 * Backend'in örnek 101 oyunu (backend/tests/ScoreBoard.Tests/Fixtures/sample-game.v2.json)
 * için döndürdüğü ayrıntı yanıtı.
 */
export const sampleDetail: SessionDetail = {
  summary: {
    id: 'id-003',
    title: 'Cuma Masası',
    game_type: 'yuzbir_okey',
    status: 'finished',
    is_quick_game: false,
    is_partnered: false,
    win_condition: 'lowest_total',
    player_names: ['xyz', 'abc'],
    winner_names: ['xyz'],
    player_count: 2,
    hand_count: 2,
    penalty_count: 1,
    rack_scan_count: 1,
    customized_rule_count: 1,
    created_at: '2026-10-18T19:00:00Z',
    updated_at: '2026-10-18T19:20:00Z',
    finished_at: '2026-10-18T19:20:00Z',
    duration_seconds: 1200,
    device_id: 'device-1',
  },
  players: [
    { id: 'id-001', name: 'xyz', seat: 0, total: -158, rank: 1, is_winner: true },
    { id: 'id-002', name: 'abc', seat: 1, total: 566, rank: 2, is_winner: false },
  ],
  rules: [
    { key: 'penalty', value: 150, customized: true },
    { key: 'handFinish', value: -202, customized: false },
  ],
  rounds: [
    {
      id: 'id-005', position: 0, hand_number: null, kind: 'penalty', rule_key: 'penalty', actor_player_id: 'id-002',
      created_at: '2026-10-18T19:04:00Z',
      entries: [
        { player_id: 'id-001', points: 0, source: 'manual' },
        { player_id: 'id-002', points: 150, source: 'rule' },
      ],
    },
    {
      id: 'id-008', position: 1, hand_number: 1, kind: 'hand', rule_key: null, actor_player_id: null,
      created_at: '2026-10-18T19:08:00Z',
      entries: [
        { player_id: 'id-001', points: 44, source: 'manual' },
        { player_id: 'id-002', points: 12, source: 'rack_scan' },
      ],
    },
    {
      id: 'id-012', position: 2, hand_number: 2, kind: 'finish', rule_key: 'handFinish', actor_player_id: 'id-001',
      created_at: '2026-10-18T19:16:00Z',
      entries: [
        { player_id: 'id-001', points: -202, source: 'rule' },
        { player_id: 'id-002', points: 404, source: 'rule' },
      ],
    },
  ],
  events: [
    ev('e1', 'game_created', '19:00', {
      game_type: 'yuzbir_okey', is_partnered: false, is_quick_game: false,
      players: [{ id: 'id-001', name: 'xyz', seat: 0 }, { id: 'id-002', name: 'abc', seat: 1 }],
      customized_rules: [{ key: 'penalty', default: 101, value: 150 }],
    }),
    ev('e2', 'round_added', '19:04', { kind: 'penalty', hand_number: null, rule_key: 'penalty', actor_player_id: 'id-002', scores: { 'id-001': 0, 'id-002': 150 } }),
    ev('e3', 'rule_applied', '19:04', { rule_key: 'penalty', rule_label: 'Ceza Puanı', player_id: 'id-002', value: 150 }),
    ev('e4', 'round_added', '19:08', { kind: 'hand', hand_number: 1, scores: { 'id-001': 40, 'id-002': 12 } }),
    ev('e5', 'rack_scan_used', '19:08', { player_id: 'id-002', points: 12, tile_count: 3, recognized_tile_count: 3, corrected_tile_count: 1, used_camera: true }),
    ev('e6', 'round_edited', '19:12', { previous_scores: { 'id-001': 40, 'id-002': 12 }, scores: { 'id-001': 44, 'id-002': 12 } }),
    ev('e7', 'round_added', '19:16', { kind: 'finish', hand_number: 2, rule_key: 'handFinish', actor_player_id: 'id-001', scores: { 'id-002': 404, 'id-001': -202 } }),
    ev('e8', 'rule_applied', '19:16', { rule_key: 'handFinish', rule_label: 'Elden Bitme', player_id: 'id-001', value: -202 }),
    ev('e9', 'game_finished', '19:20', { winner_names: ['xyz'], hand_count: 2, penalty_count: 1, duration_seconds: 1200 }),
  ],
  device: { id: 'device-1', platform: 'android', app_version: '2.0.0+9', first_seen_at: '2026-10-18T19:20:00Z', last_seen_at: '2026-10-18T19:20:00Z' },
};

function ev(id: string, type: string, time: string, data: Record<string, unknown>) {
  return {
    id, sequence: Number(id.slice(1)) - 1, type, occurred_at: `2026-10-18T${time}:00Z`,
    round_id: null, rule_key: (data.rule_key as string | undefined) ?? null, player_id: null, data,
  };
}

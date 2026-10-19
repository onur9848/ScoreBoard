// Backend yanıtları (snake_case). backend/src/ScoreBoard.Application altındaki DTO'larla aynıdır.

export interface Paged<T> {
  items: T[];
  page: number;
  page_size: number;
  total_count: number;
}

export interface SessionSummary {
  id: string;
  title: string;
  game_type: string;
  status: 'active' | 'finished' | string;
  is_quick_game: boolean;
  is_partnered: boolean;
  win_condition: string;
  player_names: string[];
  winner_names: string[];
  player_count: number;
  hand_count: number;
  penalty_count: number;
  rack_scan_count: number;
  customized_rule_count: number;
  created_at: string;
  updated_at: string;
  finished_at: string | null;
  duration_seconds: number | null;
  device_id: string;
}

export interface SessionPlayer {
  id: string;
  name: string;
  seat: number;
  total: number;
  rank: number;
  is_winner: boolean;
}

export interface SessionRule {
  key: string;
  value: number;
  customized: boolean;
}

export interface RoundEntry {
  player_id: string;
  points: number;
  source: 'manual' | 'rule' | 'rack_scan' | string;
}

export interface Round {
  id: string;
  position: number;
  hand_number: number | null;
  kind: 'hand' | 'penalty' | 'finish' | string;
  rule_key: string | null;
  actor_player_id: string | null;
  created_at: string;
  entries: RoundEntry[];
}

export interface SessionEvent {
  id: string;
  sequence: number;
  type: string;
  occurred_at: string;
  round_id: string | null;
  rule_key: string | null;
  player_id: string | null;
  data: Record<string, unknown>;
}

export interface Device {
  id: string;
  platform: string | null;
  app_version: string | null;
  first_seen_at: string;
  last_seen_at: string;
}

export interface SessionDetail {
  summary: SessionSummary;
  players: SessionPlayer[];
  rules: SessionRule[];
  rounds: Round[];
  events: SessionEvent[];
  device: Device | null;
}

export interface OverviewStats {
  totals: {
    sessions: number;
    finished: number;
    active: number;
    quick_games: number;
    partnered: number;
    devices: number;
    hands: number;
    average_hands: number;
    average_players: number;
    average_duration_seconds: number | null;
  };
  game_types: {
    game_type: string;
    sessions: number;
    finished: number;
    average_hands: number;
    average_duration_seconds: number | null;
    partnered: number;
    customized: number;
  }[];
  rule_usage: { game_type: string; rule_key: string; kind: string; count: number; sessions: number }[];
  customized_rules: {
    game_type: string;
    rule_key: string;
    sessions: number;
    min_value: number;
    max_value: number;
    average_value: number;
  }[];
  rack_scan: {
    scans: number;
    sessions: number;
    tiles: number;
    recognized_tiles: number;
    corrected_tiles: number;
    correction_rate: number;
    camera_share: number;
  };
  score_sources: { key: string; count: number }[];
  player_counts: { key: string; count: number }[];
  daily: { date: string; created: number; finished: number }[];
}

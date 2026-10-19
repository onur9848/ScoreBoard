import type { SessionDetail, SessionEvent } from '../api/types';
import { formatDuration, dative } from './format';
import { gameTypeLabel, ruleLabel } from './labels';

export type Tone = 'start' | 'round' | 'rule' | 'scan' | 'edit' | 'finish' | 'other';

export interface TimelineItem {
  id: string;
  at: string;
  tone: Tone;
  title: string;
  detail?: string;
}

type Scores = Record<string, number>;

const num = (v: unknown): number | undefined => (typeof v === 'number' ? v : undefined);
const str = (v: unknown): string | undefined => (typeof v === 'string' ? v : undefined);
const scores = (v: unknown): Scores => (v && typeof v === 'object' ? (v as Scores) : {});

/** Oyunun olay günlüğünü okunur satırlara çevirir. */
export function buildTimeline(detail: SessionDetail): TimelineItem[] {
  const names = new Map(detail.players.map((p) => [p.id, p.name]));
  const seats = new Map(detail.players.map((p) => [p.id, p.seat]));
  const name = (id: unknown) => names.get(String(id)) ?? '?';
  const scoreList = (s: Scores) =>
    Object.entries(s)
      .sort(([a], [b]) => (seats.get(a) ?? 99) - (seats.get(b) ?? 99))
      .map(([id, points]) => `${name(id)} ${points}`)
      .join(', ');

  return detail.events.map((event) => {
    const d = event.data;
    const base = { id: event.id, at: event.occurred_at };
    switch (event.type) {
      case 'game_created': {
        const players = Array.isArray(d.players) ? (d.players as { name?: string }[]).map((p) => p.name ?? '?') : [];
        const customized = Array.isArray(d.customized_rules)
          ? (d.customized_rules as { key: string; default?: number; value?: number }[])
          : [];
        const parts = [
          d.is_partnered ? 'Eşli' : 'Tekli',
          d.is_quick_game ? 'hızlı oyun' : null,
          `${players.length} oyuncu: ${players.join(', ')}`,
        ].filter(Boolean);
        return {
          ...base,
          tone: 'start' as const,
          title: d.imported_from_legacy
            ? `${gameTypeLabel(str(d.game_type) ?? detail.summary.game_type)} eski uygulamadan aktarıldı`
            : `${gameTypeLabel(str(d.game_type) ?? detail.summary.game_type)} seçildi`,
          detail: [
            parts.join(' · '),
            customized.length > 0
              ? `Ayar değişti: ${customized.map((c) => `${ruleLabel(c.key)} ${c.default ?? '?'} → ${c.value ?? '?'}`).join(', ')}`
              : null,
          ]
            .filter(Boolean)
            .join('\n'),
        };
      }
      case 'round_added': {
        const s = scores(d.scores);
        const actor = str(d.actor_player_id);
        const rule = str(d.rule_key);
        if (d.kind === 'penalty' && actor) {
          return { ...base, tone: 'rule' as const, title: `${dative(name(actor))} ceza yazıldı (${s[actor] ?? '?'})` };
        }
        if (d.kind === 'finish' && actor) {
          return {
            ...base,
            tone: 'rule' as const,
            title: `${num(d.hand_number) ?? '?'}. el: ${name(actor)} bitirdi (${ruleLabel(rule)})`,
            detail: scoreList(s),
          };
        }
        return { ...base, tone: 'round' as const, title: `${num(d.hand_number) ?? '?'}. el yazıldı`, detail: scoreList(s) };
      }
      case 'rule_applied':
        return {
          ...base,
          tone: 'rule' as const,
          title: `Kural kullanıldı: ${str(d.rule_label) ?? ruleLabel(str(d.rule_key))}`,
          detail: `${name(d.player_id)} · ${num(d.value) ?? '?'}`,
        };
      case 'rack_scan_used': {
        const corrected = num(d.corrected_tile_count) ?? 0;
        return {
          ...base,
          tone: 'scan' as const,
          title: `${name(d.player_id)} için ıstaka hesaplandı: ${num(d.points) ?? '?'} puan`,
          detail: [
            `${num(d.tile_count) ?? 0} taş`,
            d.used_camera ? `kameradan ${num(d.recognized_tile_count) ?? 0} taş okundu` : 'elle dizildi',
            corrected > 0 ? `${corrected} düzeltme` : 'düzeltme yok',
          ].join(' · '),
        };
      }
      case 'round_edited': {
        const before = scores(d.previous_scores);
        const after = scores(d.scores);
        const changes = Object.keys(after)
          .filter((id) => before[id] !== after[id])
          .map((id) => `${name(id)} ${before[id] ?? '?'} → ${after[id]}`);
        return { ...base, tone: 'edit' as const, title: 'Satır düzeltildi', detail: changes.join(', ') || undefined };
      }
      case 'round_deleted':
        return {
          ...base,
          tone: 'edit' as const,
          title: d.kind === 'penalty' ? 'Ceza satırı silindi' : 'Satır silindi',
          detail: scoreList(scores(d.scores)),
        };
      case 'game_finished': {
        const winners = Array.isArray(d.winner_names) ? (d.winner_names as string[]).join(', ') : '';
        return {
          ...base,
          tone: 'finish' as const,
          title: winners ? `Oyun bitti, ${winners} kazandı` : 'Oyun bitti',
          detail: `${num(d.hand_count) ?? 0} el · ${num(d.penalty_count) ?? 0} ceza · ${formatDuration(num(d.duration_seconds))}`,
        };
      }
      case 'game_reopened':
        return { ...base, tone: 'other' as const, title: 'Biten oyun yeniden açıldı' };
      case 'game_resumed':
        return { ...base, tone: 'other' as const, title: 'Oyuna geri dönüldü', detail: `${num(d.hand_count) ?? 0}. elden devam` };
      default:
        return { ...base, tone: 'other' as const, title: event.type };
    }
  });
}

/**
 * Oyunun tek paragraflık özeti; örneğin "101 Okey seçildi, Ceza Puanı 101 → 150 yapıldı.
 * xyz ve abc ile oyun kuruldu. 2 el oynandı, xyz kazandı. Kullanılanlar: …".
 */
export function summarize(detail: SessionDetail): string[] {
  const { summary } = detail;
  const created = detail.events.find((e) => e.type === 'game_created');
  const sentences: string[] = [];

  const mode = [summary.is_partnered ? 'eşli' : null, summary.is_quick_game ? 'hızlı oyun olarak' : null].filter(Boolean);
  sentences.push(`${gameTypeLabel(summary.game_type)} ${mode.length ? `${mode.join(', ')} ` : ''}seçildi.`);

  const customized = Array.isArray(created?.data.customized_rules)
    ? (created.data.customized_rules as { key: string; default?: number; value?: number }[]).map(
        (c) => `${ruleLabel(c.key)} ${c.default ?? '?'} → ${c.value ?? '?'}`,
      )
    : detail.rules.filter((r) => r.customized).map((r) => `${ruleLabel(r.key)} ${r.value}`);
  if (customized.length > 0) sentences.push(`Ayarlar değiştirildi: ${customized.join(', ')}.`);

  sentences.push(`${summary.player_count} kişi (${summary.player_names.join(', ')}) ile oyun kuruldu.`);

  const played = [`${summary.hand_count} el oynandı`];
  if (summary.penalty_count > 0) played.push(`${summary.penalty_count} ceza yazıldı`);
  if (summary.status === 'finished') {
    played.push(summary.winner_names.length ? `${summary.winner_names.join(' ve ')} kazandı` : 'oyun bitti');
    sentences.push(`${played.join(', ')} (${formatDuration(summary.duration_seconds)}).`);
  } else {
    sentences.push(`${played.join(', ')}; oyun devam ediyor.`);
  }

  const used = featureUsage(detail.events);
  if (used.length > 0) sentences.push(`Kullanılanlar: ${used.join(', ')}.`);
  return sentences;
}

function featureUsage(events: SessionEvent[]): string[] {
  const counts = new Map<string, number>();
  const bump = (label: string) => counts.set(label, (counts.get(label) ?? 0) + 1);
  let corrected = 0;
  for (const e of events) {
    if (e.type === 'rule_applied') bump(str(e.data.rule_label) ?? ruleLabel(e.rule_key));
    if (e.type === 'rack_scan_used') {
      bump('Istaka okuma');
      corrected += num(e.data.corrected_tile_count) ?? 0;
    }
    if (e.type === 'round_edited') bump('Satır düzeltme');
    if (e.type === 'round_deleted') bump('Satır silme');
  }
  return [...counts].map(([label, count]) => {
    const suffix = label === 'Istaka okuma' && corrected > 0 ? `, ${corrected} taş düzeltildi` : '';
    return `${label} ×${count}${suffix}`;
  });
}

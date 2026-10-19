import type { SessionDetail } from '../api/types';
import { ruleLabel } from '../lib/labels';

const SOURCE_MARK: Record<string, string> = { rule: 'kural', rack_scan: 'ıstaka' };

/** Uygulamadaki skor tablosunun aynısı: eller, ceza satırları ve toplamlar. */
export function ScoreTable({ detail }: { detail: SessionDetail }) {
  const players = [...detail.players].sort((a, b) => a.seat - b.seat);
  return (
    <div className="table-wrap">
      <table className="table score">
        <thead>
          <tr>
            <th>El</th>
            {players.map((p) => (
              <th key={p.id} className="num">
                {p.name}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {detail.rounds.map((round) => {
            const byPlayer = new Map(round.entries.map((e) => [e.player_id, e]));
            return (
              <tr key={round.id} className={`kind-${round.kind}`}>
                <td>
                  <span className="round-no">{round.hand_number ? `#${round.hand_number}` : '–'}</span>
                  {round.rule_key && <span className="rule-chip">{ruleLabel(round.rule_key)}</span>}
                </td>
                {players.map((p) => {
                  const entry = byPlayer.get(p.id);
                  const mark = entry ? SOURCE_MARK[entry.source] : undefined;
                  return (
                    <td
                      key={p.id}
                      className={`num${round.actor_player_id === p.id ? ' actor' : ''}`}
                      title={mark ? `Puan ${mark} ile yazıldı` : undefined}
                    >
                      {entry?.points ?? '–'}
                      {mark && <span className={`source source-${entry?.source}`} aria-label={mark} />}
                    </td>
                  );
                })}
              </tr>
            );
          })}
        </tbody>
        <tfoot>
          <tr>
            <td>Σ</td>
            {players.map((p) => (
              <td key={p.id} className={`num${p.is_winner ? ' winner' : ''}`}>
                {p.total}
                {p.is_winner && <span aria-label="kazanan"> 🏆</span>}
              </td>
            ))}
          </tr>
        </tfoot>
      </table>
      <div className="score-legend">
        <span>
          <span className="source source-rule" />
          kuraldan
        </span>
        <span>
          <span className="source source-rack_scan" />
          ıstakadan hesaplandı
        </span>
      </div>
    </div>
  );
}

import { useQuery } from '@tanstack/react-query';
import { Link, useParams } from 'react-router-dom';
import { useApi } from '../auth';
import { Badge, Empty, ErrorBox, Loading, Section } from '../components/Common';
import { ScoreTable } from '../components/ScoreTable';
import { Timeline } from '../components/Timeline';
import { formatDateTime, formatDuration } from '../lib/format';
import { RULES, WIN_CONDITIONS, gameTypeLabel, ruleLabel } from '../lib/labels';
import { buildTimeline, summarize } from '../lib/narrative';

export function SessionDetailPage() {
  const { id = '' } = useParams();
  const api = useApi();
  const query = useQuery({ queryKey: ['session', id], queryFn: () => api.session(id) });

  if (query.isPending) return <Loading />;
  if (query.isError) return <ErrorBox error={query.error} />;
  const detail = query.data;
  if (!detail) return <Empty>Oyun bulunamadı.</Empty>;
  const s = detail.summary;
  // Kurallar uygulamadaki sırayla; bilinmeyenler sona.
  const order = Object.keys(RULES);
  const rank = (key: string) => (order.includes(key) ? order.indexOf(key) : order.length);
  const rules = [...detail.rules].sort((a, b) => rank(a.key) - rank(b.key));

  return (
    <div className="page">
      <Link to="/oyunlar" className="back">
        ← Oyunlar
      </Link>
      <div className="page-head">
        <div>
          <h1>{s.title}</h1>
          <div className="tags">
            <Badge tone="primary">{gameTypeLabel(s.game_type)}</Badge>
            <Badge tone={s.status === 'finished' ? 'success' : 'accent'}>
              {s.status === 'finished' ? 'Bitti' : 'Devam ediyor'}
            </Badge>
            <Badge>{s.is_partnered ? 'Eşli' : 'Tekli'}</Badge>
            {s.is_quick_game && <Badge>Hızlı oyun</Badge>}
          </div>
        </div>
      </div>

      <section className="card summary">
        {summarize(detail).map((sentence) => (
          <p key={sentence}>{sentence}</p>
        ))}
      </section>

      <div className="detail-grid">
        <div className="detail-main">
          <Section title="Skor tablosu">
            {detail.rounds.length === 0 ? <Empty>Henüz el yazılmadı.</Empty> : <ScoreTable detail={detail} />}
          </Section>
          <Section title="Oyun günlüğü">
            <Timeline items={buildTimeline(detail)} />
          </Section>
        </div>
        <aside className="detail-side">
          <Section title="Bilgiler">
            <dl className="facts">
              <dt>Başladı</dt>
              <dd>{formatDateTime(s.created_at)}</dd>
              <dt>{s.finished_at ? 'Bitti' : 'Son değişiklik'}</dt>
              <dd>{formatDateTime(s.finished_at ?? s.updated_at)}</dd>
              <dt>Süre</dt>
              <dd>{formatDuration(s.duration_seconds)}</dd>
              <dt>Kazanma</dt>
              <dd>{WIN_CONDITIONS[s.win_condition] ?? s.win_condition}</dd>
              <dt>Cihaz</dt>
              <dd>
                <Link to={`/oyunlar?deviceId=${encodeURIComponent(s.device_id)}`} title={s.device_id}>
                  {detail.device?.platform ?? 'cihaz'} {detail.device?.app_version ?? ''}
                </Link>
              </dd>
            </dl>
          </Section>
          {rules.length > 0 && (
            <Section title="Kurallar">
              <ul className="rules">
                {rules.map((r) => (
                  <li key={r.key} className={r.customized ? 'customized' : undefined}>
                    <span>{ruleLabel(r.key)}</span>
                    <strong>{r.value}</strong>
                    {r.customized && <Badge tone="accent">değişti</Badge>}
                  </li>
                ))}
              </ul>
            </Section>
          )}
        </aside>
      </div>
    </div>
  );
}

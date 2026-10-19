import { useQuery } from '@tanstack/react-query';
import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { Bar, BarChart, CartesianGrid, Legend, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import type { OverviewStats } from '../api/types';
import { useApi } from '../auth';
import { BarList } from '../components/BarList';
import { Empty, ErrorBox, Loading, Section, StatCard } from '../components/Common';
import { formatDay, formatDuration, formatNumber, formatPercent } from '../lib/format';
import { GAME_TYPES, SCORE_SOURCES, gameTypeLabel, ruleLabel } from '../lib/labels';

const PERIODS = [
  { days: 7, label: '7 gün' },
  { days: 30, label: '30 gün' },
  { days: 90, label: '90 gün' },
  { days: 0, label: 'Tümü' },
];

export function DashboardPage() {
  const api = useApi();
  const [days, setDays] = useState(30);
  const [gameType, setGameType] = useState('');

  const from = useMemo(() => {
    if (days === 0) return undefined;
    const d = new Date();
    d.setHours(0, 0, 0, 0);
    d.setDate(d.getDate() - (days - 1));
    return d.toISOString();
  }, [days]);

  const query = useQuery({
    queryKey: ['overview', days, gameType],
    queryFn: () => api.overview({ from, gameType: gameType || undefined, days: days || 30 }),
  });

  return (
    <div className="page">
      <div className="page-head">
        <div>
          <h1>Gerçek kullanım</h1>
          <p className="muted">Uygulamada kurulan oyunlar, kullanılan kurallar ve ıstaka okuma.</p>
        </div>
        <div className="filters">
          <div className="segmented" role="group" aria-label="Dönem">
            {PERIODS.map((p) => (
              <button key={p.days} className={p.days === days ? 'on' : ''} onClick={() => setDays(p.days)}>
                {p.label}
              </button>
            ))}
          </div>
          <select value={gameType} onChange={(e) => setGameType(e.target.value)} aria-label="Oyun türü">
            <option value="">Tüm oyunlar</option>
            {Object.entries(GAME_TYPES).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </select>
        </div>
      </div>

      {query.isPending && <Loading />}
      {query.isError && <ErrorBox error={query.error} />}
      {query.data && <Overview stats={query.data} />}
    </div>
  );
}

function Overview({ stats }: { stats: OverviewStats }) {
  const t = stats.totals;
  if (t.sessions === 0) return <Empty>Bu dönemde oyun yok.</Empty>;
  const sourceTotal = stats.score_sources.reduce((sum, s) => sum + s.count, 0);
  return (
    <>
      <div className="stats-grid">
        <StatCard label="Oyun" value={formatNumber(t.sessions)} hint={`${t.finished} bitti · ${t.active} sürüyor`} />
        <StatCard label="Cihaz" value={formatNumber(t.devices)} hint={`oyun başına ${formatNumber(t.average_players)} oyuncu`} />
        <StatCard label="Oynanan el" value={formatNumber(t.hands)} hint={`biten oyunda ortalama ${formatNumber(t.average_hands)} el`} />
        <StatCard label="Ortalama süre" value={formatDuration(t.average_duration_seconds)} hint="biten oyunlarda" />
        <StatCard label="Hızlı oyun" value={formatPercent(t.quick_games / t.sessions)} hint={`${t.quick_games} oyun`} />
        <StatCard label="Eşli" value={formatPercent(t.partnered / t.sessions)} hint={`${t.partnered} oyun`} />
      </div>

      <Section title="Günlük oyunlar">
        <div className="chart">
          <ResponsiveContainer width="100%" height={240}>
            <BarChart data={stats.daily} margin={{ top: 8, right: 8, left: -16, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="var(--outline-variant)" vertical={false} />
              <XAxis dataKey="date" tickFormatter={formatDay} tick={{ fontSize: 12, fill: 'var(--muted)' }} minTickGap={16} />
              <YAxis allowDecimals={false} tick={{ fontSize: 12, fill: 'var(--muted)' }} />
              <Tooltip
                labelFormatter={(v) => formatDay(String(v))}
                contentStyle={{ background: 'var(--surface)', border: '1px solid var(--outline-variant)', borderRadius: 12 }}
              />
              <Legend />
              <Bar dataKey="created" name="Kurulan" fill="var(--primary)" radius={[4, 4, 0, 0]} />
              <Bar dataKey="finished" name="Biten" fill="var(--secondary)" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </Section>

      <div className="two-col">
        <Section title="Oyun türleri">
          <BarList
            items={stats.game_types.map((g) => ({
              key: g.game_type,
              label: (
                <Link to={`/oyunlar?gameType=${g.game_type}`}>{gameTypeLabel(g.game_type)}</Link>
              ),
              value: g.sessions,
              hint: `${g.finished} bitti · ort. ${formatNumber(g.average_hands)} el · ${formatDuration(g.average_duration_seconds)} · ${g.customized} oyunda ayar değişti`,
            }))}
          />
        </Section>

        <Section title="Kural kullanımı">
          {stats.rule_usage.length === 0 ? (
            <Empty>Kural düğmeleri henüz kullanılmadı.</Empty>
          ) : (
            <BarList
              items={stats.rule_usage.map((r) => ({
                key: `${r.game_type}-${r.rule_key}`,
                label: `${ruleLabel(r.rule_key)} · ${gameTypeLabel(r.game_type)}`,
                value: r.count,
                hint: `${r.sessions} oyunda`,
              }))}
            />
          )}
        </Section>

        <Section title="Istaka okuma">
          {stats.rack_scan.scans === 0 ? (
            <Empty>Istakadan puan hesaplama henüz kullanılmadı.</Empty>
          ) : (
            <div className="mini-stats">
              <StatCard label="Hesaplama" value={formatNumber(stats.rack_scan.scans)} hint={`${stats.rack_scan.sessions} oyunda`} />
              <StatCard label="Kamera" value={formatPercent(stats.rack_scan.camera_share)} hint="fotoğrafla okunan" />
              <StatCard label="Taş" value={formatNumber(stats.rack_scan.tiles)} hint={`${stats.rack_scan.recognized_tiles} otomatik okundu`} />
              <StatCard
                label="Düzeltme oranı"
                value={formatPercent(stats.rack_scan.correction_rate)}
                hint={`${stats.rack_scan.corrected_tiles} taş elle düzeltildi`}
              />
            </div>
          )}
        </Section>

        <Section title="Puanlar nereden geldi">
          <BarList
            format={(v) => formatPercent(sourceTotal ? v / sourceTotal : 0)}
            items={stats.score_sources.map((s) => ({
              key: s.key,
              label: SCORE_SOURCES[s.key] ?? s.key,
              value: s.count,
              hint: `${formatNumber(s.count)} puan`,
            }))}
          />
        </Section>

        <Section title="Değiştirilen ayarlar">
          {stats.customized_rules.length === 0 ? (
            <Empty>Varsayılan kurallar değiştirilmedi.</Empty>
          ) : (
            <div className="table-wrap">
              <table className="table">
                <thead>
                  <tr>
                    <th>Kural</th>
                    <th className="num">Oyun</th>
                    <th className="num">Değerler</th>
                    <th className="num">Ortalama</th>
                  </tr>
                </thead>
                <tbody>
                  {stats.customized_rules.map((r) => (
                    <tr key={`${r.game_type}-${r.rule_key}`}>
                      <td>
                        {ruleLabel(r.rule_key)} <span className="muted">· {gameTypeLabel(r.game_type)}</span>
                      </td>
                      <td className="num">{r.sessions}</td>
                      <td className="num">{r.min_value === r.max_value ? r.min_value : `${r.min_value} – ${r.max_value}`}</td>
                      <td className="num">{formatNumber(r.average_value)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </Section>

        <Section title="Masadaki oyuncu sayısı">
          <BarList
            items={stats.player_counts.map((p) => ({ key: p.key, label: `${p.key} oyuncu`, value: p.count }))}
          />
        </Section>
      </div>
    </>
  );
}

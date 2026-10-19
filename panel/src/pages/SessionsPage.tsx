import { keepPreviousData, useQuery } from '@tanstack/react-query';
import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { useApi } from '../auth';
import { Badge, Empty, ErrorBox, Loading } from '../components/Common';
import { formatDateTime, formatDuration } from '../lib/format';
import { GAME_TYPES, STATUSES, gameTypeLabel } from '../lib/labels';

const PAGE_SIZE = 25;

export function SessionsPage() {
  const api = useApi();
  const [params, setParams] = useSearchParams();
  const gameType = params.get('gameType') ?? '';
  const status = params.get('status') ?? '';
  const deviceId = params.get('deviceId') ?? '';
  const q = params.get('q') ?? '';
  const page = Math.max(1, Number(params.get('page') ?? '1') || 1);
  const [search, setSearch] = useState(q);

  // Arama kutusu yazmayı bitirince adrese yazılır.
  useEffect(() => {
    if (search === q) return;
    const timer = setTimeout(() => update({ q: search }), 300);
    return () => clearTimeout(timer);
  });

  function update(changes: Record<string, string>) {
    const next = new URLSearchParams(params);
    for (const [k, v] of Object.entries(changes)) {
      if (v) next.set(k, v);
      else next.delete(k);
    }
    if (!('page' in changes)) next.delete('page');
    setParams(next, { replace: true });
  }

  const query = useQuery({
    queryKey: ['sessions', gameType, status, deviceId, q, page],
    queryFn: () => api.sessions({ gameType, status, deviceId, q, page, pageSize: PAGE_SIZE }),
    placeholderData: keepPreviousData,
  });
  const data = query.data;
  const pages = data ? Math.max(1, Math.ceil(data.total_count / PAGE_SIZE)) : 1;

  return (
    <div className="page">
      <div className="page-head">
        <div>
          <h1>Oyunlar</h1>
          <p className="muted">
            {data ? `${data.total_count} oyun` : ' '}
            {deviceId && (
              <button className="chip" onClick={() => update({ deviceId: '' })}>
                Tek cihaz ✕
              </button>
            )}
          </p>
        </div>
        <div className="filters">
          <input
            type="search"
            placeholder="Başlık ya da oyuncu ara"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            aria-label="Ara"
          />
          <select value={gameType} onChange={(e) => update({ gameType: e.target.value })} aria-label="Oyun türü">
            <option value="">Tüm türler</option>
            {Object.entries(GAME_TYPES).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </select>
          <select value={status} onChange={(e) => update({ status: e.target.value })} aria-label="Durum">
            <option value="">Tüm durumlar</option>
            {Object.entries(STATUSES).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </select>
        </div>
      </div>

      {query.isPending && <Loading />}
      {query.isError && <ErrorBox error={query.error} />}
      {data && data.items.length === 0 && <Empty>Bu filtrelerle oyun bulunamadı.</Empty>}
      {data && data.items.length > 0 && (
        <div className="card table-wrap">
          <table className="table sessions">
            <thead>
              <tr>
                <th>Oyun</th>
                <th>Oyuncular</th>
                <th className="num">El</th>
                <th>Kazanan</th>
                <th>Süre</th>
                <th>Son değişiklik</th>
              </tr>
            </thead>
            <tbody>
              {data.items.map((s) => (
                <tr key={s.id}>
                  <td>
                    <Link to={`/oyunlar/${encodeURIComponent(s.id)}`} className="row-link">
                      {s.title}
                    </Link>
                    <div className="tags">
                      <Badge tone="primary">{gameTypeLabel(s.game_type)}</Badge>
                      {s.status === 'active' && <Badge tone="accent">Devam ediyor</Badge>}
                      {s.is_partnered && <Badge>Eşli</Badge>}
                      {s.is_quick_game && <Badge>Hızlı</Badge>}
                      {s.customized_rule_count > 0 && <Badge>Ayar değişti</Badge>}
                      {s.rack_scan_count > 0 && <Badge>Istaka</Badge>}
                    </div>
                  </td>
                  <td data-label="Oyuncular">{s.player_names.join(', ')}</td>
                  <td className="num" data-label="El">
                    {s.hand_count}
                  </td>
                  <td data-label="Kazanan">{s.winner_names.join(', ') || '–'}</td>
                  <td data-label="Süre">{formatDuration(s.duration_seconds)}</td>
                  <td className="nowrap" data-label="Son değişiklik">
                    {formatDateTime(s.updated_at)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      {data && pages > 1 && (
        <div className="pager">
          <button className="ghost" disabled={page <= 1} onClick={() => update({ page: String(page - 1) })}>
            Önceki
          </button>
          <span className="muted">
            {page} / {pages}
          </span>
          <button className="ghost" disabled={page >= pages} onClick={() => update({ page: String(page + 1) })}>
            Sonraki
          </button>
        </div>
      )}
    </div>
  );
}

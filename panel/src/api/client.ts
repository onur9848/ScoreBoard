import type { OverviewStats, Paged, SessionDetail, SessionSummary } from './types';

const BASE = (import.meta.env.VITE_API_BASE_URL as string | undefined)?.replace(/\/$/, '') ?? '';
const KEY_STORAGE = 'panel.key';

export class ApiError extends Error {
  constructor(
    readonly status: number,
    message: string,
  ) {
    super(message);
  }
}

export function storedKey(): string | null {
  try {
    return localStorage.getItem(KEY_STORAGE);
  } catch {
    return null;
  }
}

export function storeKey(key: string | null) {
  try {
    if (key) localStorage.setItem(KEY_STORAGE, key);
    else localStorage.removeItem(KEY_STORAGE);
  } catch {
    // Tarayıcı depolamaya izin vermiyorsa anahtar yalnızca bu sekmede yaşar.
  }
}

/** 401 alındığında çağrılır; uygulama girişe döner. */
let onUnauthorized: () => void = () => {};
export function setUnauthorizedHandler(handler: () => void) {
  onUnauthorized = handler;
}

type Query = Record<string, string | number | undefined | null>;

async function get<T>(path: string, key: string, query: Query = {}): Promise<T | null> {
  const params = new URLSearchParams();
  for (const [name, value] of Object.entries(query)) {
    if (value !== undefined && value !== null && value !== '') params.set(name, String(value));
  }
  const qs = params.toString();
  const response = await fetch(`${BASE}/api/v1${path}${qs ? `?${qs}` : ''}`, {
    headers: { 'X-Panel-Key': key },
  });
  if (response.status === 401) {
    onUnauthorized();
    throw new ApiError(401, 'Anahtar geçersiz.');
  }
  if (response.status === 204) return null;
  if (!response.ok) throw new ApiError(response.status, `Sunucu hatası (${response.status}).`);
  return (await response.json()) as T;
}

export interface SessionFilters {
  gameType?: string;
  status?: string;
  deviceId?: string;
  q?: string;
  page?: number;
  pageSize?: number;
  from?: string;
  to?: string;
}

export interface StatsFilters {
  from?: string;
  gameType?: string;
  days?: number;
}

export function createApi(key: string) {
  return {
    check: () => get<null>('/auth/check', key),
    sessions: (f: SessionFilters) => get<Paged<SessionSummary>>('/sessions', key, { ...f }) as Promise<Paged<SessionSummary>>,
    session: async (id: string) => {
      try {
        return (await get<SessionDetail>(`/sessions/${encodeURIComponent(id)}`, key)) as SessionDetail;
      } catch (e) {
        if (e instanceof ApiError && e.status === 404) return null;
        throw e;
      }
    },
    overview: (f: StatsFilters) =>
      get<OverviewStats>('/stats/overview', key, {
        ...f,
        utcOffsetMinutes: -new Date().getTimezoneOffset(),
      }) as Promise<OverviewStats>,
  };
}

export type Api = ReturnType<typeof createApi>;

import { QueryClientProvider } from '@tanstack/react-query';
import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { createQueryClient, AppRoutes } from '../App';
import { AuthProvider } from '../auth';
import { sampleDetail } from './fixtures';

const PANEL_KEY = 'panel-key-0123456789';

function mockApi() {
  const calls: { url: string; key: string | null }[] = [];
  vi.stubGlobal('fetch', async (input: string, init?: RequestInit) => {
    const key = new Headers(init?.headers).get('X-Panel-Key');
    calls.push({ url: input, key });
    if (key !== PANEL_KEY) return new Response(null, { status: 401 });
    const path = new URL(input, 'http://x').pathname;
    if (path === '/api/v1/auth/check') return new Response(null, { status: 204 });
    if (path === '/api/v1/sessions/id-003') return Response.json(sampleDetail);
    if (path === '/api/v1/sessions')
      return Response.json({ items: [sampleDetail.summary], page: 1, page_size: 25, total_count: 1 });
    return new Response(null, { status: 404 });
  });
  return calls;
}

function renderAt(path: string) {
  return render(
    <QueryClientProvider client={createQueryClient()}>
      <AuthProvider>
        <MemoryRouter initialEntries={[path]}>
          <AppRoutes />
        </MemoryRouter>
      </AuthProvider>
    </QueryClientProvider>,
  );
}

beforeEach(() => localStorage.clear());
afterEach(() => vi.unstubAllGlobals());

describe('panel', () => {
  it('yanlış anahtarı reddeder, doğru anahtarla istenen sayfaya gider', async () => {
    const calls = mockApi();
    renderAt('/oyunlar');
    const user = userEvent.setup();

    await user.type(screen.getByLabelText('Panel anahtarı'), 'yanlis');
    await user.click(screen.getByRole('button', { name: 'Giriş' }));
    expect(await screen.findByText('Anahtar geçersiz.')).toBeInTheDocument();

    await user.clear(screen.getByLabelText('Panel anahtarı'));
    await user.type(screen.getByLabelText('Panel anahtarı'), PANEL_KEY);
    await user.click(screen.getByRole('button', { name: 'Giriş' }));

    expect(await screen.findByRole('link', { name: 'Cuma Masası' })).toBeInTheDocument();
    expect(screen.getByText('1 oyun')).toBeInTheDocument();
    expect(localStorage.getItem('panel.key')).toBe(PANEL_KEY);
    expect(calls.at(-1)!.url).toContain('/api/v1/sessions?page=1&pageSize=25');
  });

  it('oyun ayrıntısında skor tablosu, özet ve günlük görünür', async () => {
    mockApi();
    localStorage.setItem('panel.key', PANEL_KEY);
    renderAt('/oyunlar/id-003');

    expect(await screen.findByRole('heading', { name: 'Cuma Masası' })).toBeInTheDocument();
    expect(screen.getByText('2 el oynandı, 1 ceza yazıldı, xyz kazandı (20 dk).')).toBeInTheDocument();

    const table = screen.getByRole('table');
    const rows = within(table).getAllByRole('row');
    // Başlık, 3 satır, toplam.
    expect(rows).toHaveLength(5);
    expect(within(rows[1]!).getByText('–')).toBeInTheDocument();
    expect(within(rows[3]!).getByText('Elden Bitme')).toBeInTheDocument();
    expect(within(rows[4]!).getByText('-158')).toBeInTheDocument();
    expect(within(rows[4]!).getByLabelText('kazanan')).toBeInTheDocument();

    expect(screen.getByText('Oyun bitti, xyz kazandı')).toBeInTheDocument();
    expect(screen.getByText('değişti')).toBeInTheDocument();
  });

  it('süresi biten anahtarla girişe döner', async () => {
    mockApi();
    localStorage.setItem('panel.key', 'eski-anahtar');
    renderAt('/');
    expect(await screen.findByLabelText('Panel anahtarı')).toBeInTheDocument();
    expect(localStorage.getItem('panel.key')).toBeNull();
  });
});

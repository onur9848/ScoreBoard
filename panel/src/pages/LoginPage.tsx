import { useState, type FormEvent } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { ApiError, createApi } from '../api/client';
import { useAuth } from '../auth';

export function LoginPage() {
  const { api, signIn } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [key, setKey] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const from = (location.state as { from?: string } | null)?.from ?? '/';

  if (api) return <Navigate to={from} replace />;

  async function submit(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      await createApi(key.trim()).check();
      signIn(key.trim());
      navigate(from, { replace: true });
    } catch (err) {
      setError(err instanceof ApiError && err.status === 401 ? 'Anahtar geçersiz.' : 'Sunucuya ulaşılamadı.');
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="login">
      <form className="card login-card" onSubmit={submit}>
        <img src="/logo.png" alt="" width={56} height={56} />
        <h1>Puan Tablosu Paneli</h1>
        <p className="muted">Uygulamanın gerçek kullanımını izlemek için panel anahtarını gir.</p>
        <label htmlFor="key">Panel anahtarı</label>
        <input
          id="key"
          type="password"
          autoComplete="current-password"
          value={key}
          onChange={(e) => setKey(e.target.value)}
          required
        />
        {error && <div className="error-box">{error}</div>}
        <button className="primary" disabled={busy || key.trim().length === 0}>
          {busy ? 'Kontrol ediliyor…' : 'Giriş'}
        </button>
      </form>
    </div>
  );
}

import type { ReactNode } from 'react';
import { ApiError } from '../api/client';

export function StatCard({ label, value, hint }: { label: string; value: ReactNode; hint?: ReactNode }) {
  return (
    <div className="card stat">
      <div className="stat-label">{label}</div>
      <div className="stat-value">{value}</div>
      {hint !== undefined && <div className="stat-hint">{hint}</div>}
    </div>
  );
}

export function Section({ title, actions, children }: { title: string; actions?: ReactNode; children: ReactNode }) {
  return (
    <section className="card section">
      <div className="section-head">
        <h2>{title}</h2>
        {actions}
      </div>
      {children}
    </section>
  );
}

export function Loading() {
  return <div className="muted pad">Yükleniyor…</div>;
}

export function ErrorBox({ error }: { error: unknown }) {
  const message = error instanceof ApiError ? error.message : 'Sunucuya ulaşılamadı.';
  return <div className="error-box">{message}</div>;
}

export function Empty({ children }: { children: ReactNode }) {
  return <div className="muted pad">{children}</div>;
}

export function Badge({ tone = 'neutral', children }: { tone?: 'neutral' | 'primary' | 'accent' | 'success'; children: ReactNode }) {
  return <span className={`badge badge-${tone}`}>{children}</span>;
}

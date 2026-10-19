import type { ReactNode } from 'react';

export interface BarItem {
  key: string;
  label: ReactNode;
  value: number;
  hint?: ReactNode;
}

/** Yatay oran çubukları; küçük kategorik veriler için grafikten daha okunur. */
export function BarList({ items, format = String }: { items: BarItem[]; format?: (v: number) => string }) {
  const max = Math.max(1, ...items.map((i) => i.value));
  return (
    <ul className="barlist">
      {items.map((item) => (
        <li key={item.key}>
          <div className="barlist-row">
            <span className="barlist-label">{item.label}</span>
            <span className="barlist-value">{format(item.value)}</span>
          </div>
          <div className="barlist-track">
            <div className="barlist-fill" style={{ width: `${(item.value / max) * 100}%` }} />
          </div>
          {item.hint && <div className="barlist-hint">{item.hint}</div>}
        </li>
      ))}
    </ul>
  );
}

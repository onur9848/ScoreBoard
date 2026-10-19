import type { TimelineItem } from '../lib/narrative';
import { formatTime } from '../lib/format';

export function Timeline({ items }: { items: TimelineItem[] }) {
  return (
    <ol className="timeline">
      {items.map((item) => (
        <li key={item.id} className={`tone-${item.tone}`}>
          <span className="dot" aria-hidden />
          <div className="timeline-body">
            <div className="timeline-title">
              <span>{item.title}</span>
              <time dateTime={item.at}>{formatTime(item.at)}</time>
            </div>
            {item.detail && <div className="timeline-detail">{item.detail}</div>}
          </div>
        </li>
      ))}
    </ol>
  );
}

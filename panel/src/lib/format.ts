const dateTime = new Intl.DateTimeFormat('tr-TR', { dateStyle: 'medium', timeStyle: 'short' });
const time = new Intl.DateTimeFormat('tr-TR', { hour: '2-digit', minute: '2-digit' });
const day = new Intl.DateTimeFormat('tr-TR', { day: 'numeric', month: 'short' });
const number = new Intl.NumberFormat('tr-TR', { maximumFractionDigits: 1 });
const percent = new Intl.NumberFormat('tr-TR', { style: 'percent', maximumFractionDigits: 1 });

export const formatDateTime = (iso: string) => dateTime.format(new Date(iso));
export const formatTime = (iso: string) => time.format(new Date(iso));
export const formatNumber = (value: number) => number.format(value);
export const formatPercent = (ratio: number) => percent.format(ratio);
/** `2026-10-18` biçimindeki günü yerel takvim günü olarak gösterir. */
export const formatDay = (date: string) => day.format(new Date(`${date}T12:00:00`));

export function formatDuration(seconds: number | null | undefined): string {
  if (seconds === null || seconds === undefined) return '–';
  if (seconds < 60) return '1 dk\'dan kısa';
  const minutes = Math.round(seconds / 60);
  if (minutes < 60) return `${minutes} dk`;
  const hours = Math.floor(minutes / 60);
  const rest = minutes % 60;
  return rest === 0 ? `${hours} sa` : `${hours} sa ${rest} dk`;
}

export function formatPoints(points: number): string {
  return points > 0 ? `+${points}` : String(points);
}

/** Türkçe isimden sonra gelen yönelme eki: "abc'ye", "Ali'ye", "Veli'ye", "Cem'e". */
export function dative(name: string): string {
  const vowels = name.toLocaleLowerCase('tr-TR').match(/[aeıioöuü]/g);
  const last = vowels?.[vowels.length - 1];
  const back = last !== undefined && 'aıou'.includes(last);
  const endsWithVowel = /[aeıioöuü]$/i.test(name);
  return `${name}'${endsWithVowel ? 'y' : ''}${back ? 'a' : 'e'}`;
}

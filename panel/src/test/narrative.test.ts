import { buildTimeline, summarize } from '../lib/narrative';
import { dative, formatDuration } from '../lib/format';
import { sampleDetail } from './fixtures';

describe('summarize', () => {
  it('oyunu baştan sona tek paragrafta anlatır', () => {
    expect(summarize(sampleDetail)).toEqual([
      '101 Okey seçildi.',
      'Ayarlar değiştirildi: Ceza Puanı 101 → 150.',
      '2 kişi (xyz, abc) ile oyun kuruldu.',
      '2 el oynandı, 1 ceza yazıldı, xyz kazandı (20 dk).',
      'Kullanılanlar: Ceza Puanı ×1, Istaka okuma ×1, 1 taş düzeltildi, Satır düzeltme ×1, Elden Bitme ×1.',
    ]);
  });

  it('devam eden oyunu belirtir', () => {
    const active = { ...sampleDetail, summary: { ...sampleDetail.summary, status: 'active', is_partnered: true }, events: [] };
    const text = summarize(active);
    expect(text[0]).toBe('101 Okey eşli seçildi.');
    expect(text).toContain('2 el oynandı, 1 ceza yazıldı; oyun devam ediyor.');
  });
});

describe('buildTimeline', () => {
  it('her olayı okunur bir satıra çevirir', () => {
    const titles = buildTimeline(sampleDetail).map((i) => i.title);
    expect(titles).toEqual([
      '101 Okey seçildi',
      "abc'a ceza yazıldı (150)",
      'Kural kullanıldı: Ceza Puanı',
      '1. el yazıldı',
      'abc için ıstaka hesaplandı: 12 puan',
      'Satır düzeltildi',
      '2. el: xyz bitirdi (Elden Bitme)',
      'Kural kullanıldı: Elden Bitme',
      'Oyun bitti, xyz kazandı',
    ]);
    const items = buildTimeline(sampleDetail);
    expect(items[0]!.detail).toBe('Tekli · 2 oyuncu: xyz, abc\nAyar değişti: Ceza Puanı 101 → 150');
    expect(items[4]!.detail).toBe('3 taş · kameradan 3 taş okundu · 1 düzeltme');
    expect(items[5]!.detail).toBe('xyz 40 → 44');
    // Puanlar masadaki sırayla yazılır.
    expect(items[6]!.detail).toBe('xyz -202, abc 404');
  });

  it('bilinmeyen olay türünü adıyla gösterir', () => {
    const detail = { ...sampleDetail, events: [{ ...sampleDetail.events[0]!, type: 'tile_photo_shared', data: {} }] };
    expect(buildTimeline(detail)[0]!.title).toBe('tile_photo_shared');
  });
});

describe('format', () => {
  it('Türkçe yönelme eki', () => {
    expect(['Ali', 'Bora', 'Cem', 'Ayşe', 'Oğuz'].map(dative)).toEqual(["Ali'ye", "Bora'ya", "Cem'e", "Ayşe'ye", "Oğuz'a"]);
  });

  it('süre', () => {
    expect([30, 1200, 3600, 3900, null].map(formatDuration)).toEqual(["1 dk'dan kısa", '20 dk', '1 sa', '1 sa 5 dk', '–']);
  });
});

// Uygulamadaki adlarla aynı (mobile/lib/domain/models/game_type.dart, rules/rule_catalog.dart).

export const GAME_TYPES: Record<string, string> = {
  yuzbir_okey: '101 Okey',
  okey: 'Okey',
  generic: 'Genel Oyun',
};

export const RULES: Record<string, string> = {
  penalty: 'Ceza Puanı',
  normalFinish: 'Normal Bitiş',
  noOpenPenalty: 'Açmama Cezası',
  handFinish: 'Elden Bitme',
  handNoOpenPenalty: 'Elden Bitme Açmama Cezası',
  handOkeyFinish: 'Okeyle Elden Bitme',
  handOkeyNoOpenPenalty: 'Okeyle Elden Bitme Açmama Cezası',
};

export const STATUSES: Record<string, string> = {
  active: 'Devam ediyor',
  finished: 'Bitti',
};

export const SCORE_SOURCES: Record<string, string> = {
  manual: 'Elle girildi',
  rule: 'Kuraldan',
  rack_scan: 'Istakadan',
};

export const WIN_CONDITIONS: Record<string, string> = {
  lowest_total: 'En düşük puan kazanır',
  highest_total: 'En yüksek puan kazanır',
};

export const gameTypeLabel = (type: string) => GAME_TYPES[type] ?? type;
export const ruleLabel = (key: string | null | undefined) => (key ? (RULES[key] ?? key) : '');

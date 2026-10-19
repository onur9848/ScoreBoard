import '../models/game_type.dart';

/// Kuralın tahtadaki davranışı.
enum RuleCategory {
  /// Tek bir oyuncuya ceza yazar (örn. 101 ceza).
  penalty('penalty'),

  /// Bir oyuncunun bitirdiği el. Bitiren kuralın değerini alır, diğerleri
  /// [RuleDefinition.pairedKey] ile belirtilen açmama cezasını veya elindeki
  /// taşların toplamını alır.
  finish('finish'),

  /// Bir bitiş kuralına bağlı açmama cezası; tahtada ayrı buton olarak çıkmaz.
  finishPenalty('finish_penalty');

  const RuleCategory(this.wireName);

  final String wireName;
}

class RuleDefinition {
  const RuleDefinition({
    required this.key,
    required this.label,
    required this.category,
    required this.defaultValue,
    this.pairedKey,
  });

  final String key;
  final String label;
  final RuleCategory category;
  final int defaultValue;
  final String? pairedKey;
}

/// Oyun türlerine göre varsayılan kurallar. Anahtarlar eski uygulamayla ve
/// backend ile aynıdır.
abstract final class RuleCatalog {
  static const yuzBirOkey = <RuleDefinition>[
    RuleDefinition(key: 'penalty', label: 'Ceza Puanı', category: RuleCategory.penalty, defaultValue: 101),
    RuleDefinition(
      key: 'normalFinish',
      label: 'Normal Bitiş',
      category: RuleCategory.finish,
      defaultValue: -101,
      pairedKey: 'noOpenPenalty',
    ),
    RuleDefinition(
      key: 'noOpenPenalty',
      label: 'Açmama Cezası',
      category: RuleCategory.finishPenalty,
      defaultValue: 202,
    ),
    RuleDefinition(
      key: 'handFinish',
      label: 'Elden Bitme',
      category: RuleCategory.finish,
      defaultValue: -202,
      pairedKey: 'handNoOpenPenalty',
    ),
    RuleDefinition(
      key: 'handNoOpenPenalty',
      label: 'Elden Bitme Açmama Cezası',
      category: RuleCategory.finishPenalty,
      defaultValue: 404,
    ),
    RuleDefinition(
      key: 'handOkeyFinish',
      label: 'Okeyle Elden Bitme',
      category: RuleCategory.finish,
      defaultValue: -404,
      pairedKey: 'handOkeyNoOpenPenalty',
    ),
    RuleDefinition(
      key: 'handOkeyNoOpenPenalty',
      label: 'Okeyle Elden Bitme Açmama Cezası',
      category: RuleCategory.finishPenalty,
      defaultValue: 808,
    ),
  ];

  static List<RuleDefinition> forType(GameType type) => switch (type) {
    GameType.yuzBirOkey => yuzBirOkey,
    GameType.okey || GameType.generic => const [],
  };

  static RuleDefinition? find(GameType type, String key) {
    for (final rule in forType(type)) {
      if (rule.key == key) return rule;
    }
    return null;
  }

  static Map<String, int> defaultValues(GameType type) => {
    for (final rule in forType(type)) rule.key: rule.defaultValue,
  };

  static WinCondition defaultWinCondition(GameType type) => switch (type) {
    GameType.yuzBirOkey => WinCondition.lowestTotal,
    GameType.okey || GameType.generic => WinCondition.highestTotal,
  };

  /// Eşli/tekli seçimi olan oyunlar.
  static bool supportsPartnerMode(GameType type) => type != GameType.generic;
}

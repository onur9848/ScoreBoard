/// Oyun türleri. [wireName] backend ile paylaşılan sabit değerdir, değiştirilmemeli.
enum GameType {
  okey('okey', 'Okey'),
  yuzBirOkey('yuzbir_okey', '101 Okey'),
  generic('generic', 'Genel Oyun');

  const GameType(this.wireName, this.label);

  final String wireName;
  final String label;

  static GameType fromWire(String? value) =>
      GameType.values.firstWhere((t) => t.wireName == value, orElse: () => GameType.generic);

  /// Eski Kotlin uygulamasının enum adlarından dönüşüm.
  static GameType fromLegacy(String? value) => switch (value) {
    'Okey' => GameType.okey,
    'YuzBirOkey' => GameType.yuzBirOkey,
    _ => GameType.generic,
  };
}

/// Kazananın nasıl belirlendiği.
enum WinCondition {
  lowestTotal('lowest_total', 'En düşük puan kazanır'),
  highestTotal('highest_total', 'En yüksek puan kazanır');

  const WinCondition(this.wireName, this.label);

  final String wireName;
  final String label;

  static WinCondition fromWire(String? value) =>
      WinCondition.values.firstWhere((w) => w.wireName == value, orElse: () => WinCondition.highestTotal);
}

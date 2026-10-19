import 'tile.dart';

/// Elde kalan okeyin nasıl sayılacağı; masadan masaya değişir.
enum OkeyInHandRule {
  /// Her okey için ceza puanı (genellikle 101) yazılır.
  penalty('Ceza yazılır'),

  /// Okeyin kendi sayısı kadar sayılır.
  faceValue('Taş değeri kadar');

  const OkeyInHandRule(this.label);

  final String label;
}

class RackScoreOptions {
  const RackScoreOptions({
    this.indicator,
    this.opened = true,
    this.doubled = false,
    this.okeyInHand = OkeyInHandRule.penalty,
    this.okeyPenalty = 101,
    this.noOpenPenalty = 202,
  });

  /// Gösterge taşı; verilmezse okey bilinmez ve sahte okey 0 sayılır.
  final Tile? indicator;

  /// Oyuncu elini açtı mı? Açmadıysa taş toplamı yerine açmama cezası yazılır.
  final bool opened;

  /// Çifte gidildiyse ceza iki katıdır.
  final bool doubled;
  final OkeyInHandRule okeyInHand;
  final int okeyPenalty;
  final int noOpenPenalty;

  RackScoreOptions copyWith({
    Tile? indicator,
    bool clearIndicator = false,
    bool? opened,
    bool? doubled,
    OkeyInHandRule? okeyInHand,
    int? okeyPenalty,
    int? noOpenPenalty,
  }) => RackScoreOptions(
    indicator: clearIndicator ? null : (indicator ?? this.indicator),
    opened: opened ?? this.opened,
    doubled: doubled ?? this.doubled,
    okeyInHand: okeyInHand ?? this.okeyInHand,
    okeyPenalty: okeyPenalty ?? this.okeyPenalty,
    noOpenPenalty: noOpenPenalty ?? this.noOpenPenalty,
  );
}

class RackScore {
  const RackScore({
    required this.total,
    required this.tileSum,
    required this.okeyCount,
    required this.okeyPoints,
    required this.multiplier,
    required this.usedNoOpenPenalty,
  });

  final int total;

  /// Okeyler hariç taşların toplamı.
  final int tileSum;
  final int okeyCount;
  final int okeyPoints;
  final int multiplier;
  final bool usedNoOpenPenalty;
}

abstract final class RackScoring {
  static bool isOkey(Tile tile, Tile? indicator) {
    if (indicator == null || tile.isFakeOkey) return false;
    final okey = okeyFromIndicator(indicator);
    return tile.color == okey.color && tile.number == okey.number;
  }

  /// Sahte okey, okeyin yerine geçen taştır; değeri okeyin sayısıdır.
  static int valueOf(Tile tile, Tile? indicator) {
    if (!tile.isFakeOkey) return tile.number!;
    if (indicator == null) return 0;
    return okeyFromIndicator(indicator).number;
  }

  static RackScore score(List<Tile> tiles, RackScoreOptions options) {
    final multiplier = options.doubled ? 2 : 1;
    if (!options.opened) {
      return RackScore(
        total: options.noOpenPenalty * multiplier,
        tileSum: 0,
        okeyCount: 0,
        okeyPoints: 0,
        multiplier: multiplier,
        usedNoOpenPenalty: true,
      );
    }
    var tileSum = 0;
    var okeyCount = 0;
    var okeyPoints = 0;
    for (final tile in tiles) {
      if (isOkey(tile, options.indicator)) {
        okeyCount++;
        okeyPoints += switch (options.okeyInHand) {
          OkeyInHandRule.penalty => options.okeyPenalty,
          OkeyInHandRule.faceValue => tile.number!,
        };
      } else {
        tileSum += valueOf(tile, options.indicator);
      }
    }
    return RackScore(
      total: (tileSum + okeyPoints) * multiplier,
      tileSum: tileSum,
      okeyCount: okeyCount,
      okeyPoints: okeyPoints,
      multiplier: multiplier,
      usedNoOpenPenalty: false,
    );
  }
}

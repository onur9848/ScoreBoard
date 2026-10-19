import 'dart:math' as math;

import 'tile.dart';

/// Görüntüde bulunan bir metin parçası (ML Kit elemanı) ve kutusu.
class TextGlyph {
  const TextGlyph({
    required this.text,
    required this.left,
    required this.top,
    required this.right,
    required this.bottom,
  });

  final String text;
  final double left;
  final double top;
  final double right;
  final double bottom;

  double get centerX => (left + right) / 2;
  double get centerY => (top + bottom) / 2;
  double get height => bottom - top;
}

class DetectedTile {
  const DetectedTile({required this.number, required this.color, required this.glyph});

  final int number;
  final TileColor color;
  final TextGlyph glyph;
}

/// OCR çıktısını taş sayısına çevirir. Okey yazı tiplerinde sık karışan
/// karakterler düzeltilir (I/l/| -> 1, O/o -> 0, S -> 5, B -> 8, Z -> 2).
int? parseTileNumber(String raw) {
  const fixes = {
    'I': '1',
    'l': '1',
    '|': '1',
    'i': '1',
    '!': '1',
    'O': '0',
    'o': '0',
    'S': '5',
    's': '5',
    'B': '8',
    'Z': '2',
    'z': '2',
  };
  final cleaned = raw.trim().split('').map((c) => fixes[c] ?? c).join();
  if (!RegExp(r'^\d{1,2}$').hasMatch(cleaned)) return null;
  final value = int.parse(cleaned);
  return value >= 1 && value <= 13 ? value : null;
}

/// Bir kutudaki piksellerden (RGB, 0-255) rakamın rengini tahmin eder.
/// Taş yüzeyi açık ve doygunluğu düşük olduğu için, yüzeyden belirgin şekilde
/// koyu ya da doygun pikseller rakamın çizgisi kabul edilir.
TileColor classifyTileColor(List<(int, int, int)> pixels) {
  if (pixels.isEmpty) return TileColor.black;
  final hsv = [for (final p in pixels) _toHsv(p.$1, p.$2, p.$3)];

  final values = [for (final c in hsv) c.v]..sort();
  final background = values[(values.length * 0.9).floor().clamp(0, values.length - 1)];

  var strokes = [
    for (final c in hsv)
      if (c.v < background * 0.78 || (c.s > 0.35 && c.v > 0.2)) c,
  ];
  // Ayırt edilebilen çizgi pikseli çok azsa en koyu %5'lik dilim kullanılır.
  if (strokes.length < math.max(3, pixels.length * 0.005)) {
    final sorted = [...hsv]..sort((a, b) => a.v.compareTo(b.v));
    strokes = sorted.take(math.max(1, (sorted.length * 0.05).round())).toList();
  }

  final meanS = strokes.fold<double>(0, (s, c) => s + c.s) / strokes.length;
  final meanV = strokes.fold<double>(0, (s, c) => s + c.v) / strokes.length;
  if (meanS < 0.28 || meanV < 0.18) return TileColor.black;

  // Ton ortalaması dairesel alınır; doygunluk ağırlık olarak kullanılır.
  var x = 0.0;
  var y = 0.0;
  for (final c in strokes) {
    final rad = c.h * math.pi / 180;
    x += math.cos(rad) * c.s;
    y += math.sin(rad) * c.s;
  }
  var hue = math.atan2(y, x) * 180 / math.pi;
  if (hue < 0) hue += 360;

  if (hue < 22 || hue >= 300) return TileColor.red;
  if (hue < 75) return TileColor.yellow;
  if (hue >= 160 && hue < 300) return TileColor.blue;
  // Yeşil tonlar bazı takımlarda siyahın yerini alır.
  return TileColor.black;
}

/// Taşları ıstaka sıralarına ayırır (üst sıra önce) ve her sırayı soldan
/// sağa dizer. Dikey boşluk taş yüksekliğinin yarısından büyükse yeni sıradır.
List<List<T>> groupIntoRows<T>(List<T> items, TextGlyph Function(T) glyphOf) {
  if (items.isEmpty) return const [];
  final sorted = [...items]..sort((a, b) => glyphOf(a).centerY.compareTo(glyphOf(b).centerY));
  final heights = [for (final i in sorted) glyphOf(i).height]..sort();
  final typicalHeight = heights[heights.length ~/ 2];

  final rows = <List<T>>[
    [sorted.first],
  ];
  for (final item in sorted.skip(1)) {
    final row = rows.last;
    final rowY = row.fold<double>(0, (s, i) => s + glyphOf(i).centerY) / row.length;
    if (glyphOf(item).centerY - rowY > typicalHeight * 1.5) {
      rows.add([item]);
    } else {
      row.add(item);
    }
  }
  for (final row in rows) {
    row.sort((a, b) => glyphOf(a).centerX.compareTo(glyphOf(b).centerX));
  }
  return rows;
}

({double h, double s, double v}) _toHsv(int r8, int g8, int b8) {
  final r = r8 / 255;
  final g = g8 / 255;
  final b = b8 / 255;
  final maxC = math.max(r, math.max(g, b));
  final minC = math.min(r, math.min(g, b));
  final delta = maxC - minC;
  double h;
  if (delta == 0) {
    h = 0;
  } else if (maxC == r) {
    h = 60 * (((g - b) / delta) % 6);
  } else if (maxC == g) {
    h = 60 * ((b - r) / delta + 2);
  } else {
    h = 60 * ((r - g) / delta + 4);
  }
  if (h < 0) h += 360;
  return (h: h, s: maxC == 0 ? 0 : delta / maxC, v: maxC);
}

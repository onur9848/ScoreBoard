import 'package:flutter_test/flutter_test.dart';
import 'package:puan_tablosu/domain/tiles/tile.dart';
import 'package:puan_tablosu/domain/tiles/tile_detection.dart';

/// Taş yüzeyi (fildişi) üzerinde belirli renkte rakam çizgisi.
List<(int, int, int)> tilePixels((int, int, int) stroke, {double strokeRatio = 0.2}) {
  const face = (250, 244, 228);
  const total = 400;
  final strokes = (total * strokeRatio).round();
  return [for (var i = 0; i < total; i++) i < strokes ? stroke : face];
}

TextGlyph glyph(double x, double y, [String text = '1']) =>
    TextGlyph(text: text, left: x, top: y, right: x + 20, bottom: y + 30);

void main() {
  group('parseTileNumber', () {
    test('1-13 arası sayıları kabul eder', () {
      expect(parseTileNumber('7'), 7);
      expect(parseTileNumber(' 13 '), 13);
      expect(parseTileNumber('0'), isNull);
      expect(parseTileNumber('14'), isNull);
      expect(parseTileNumber('abc'), isNull);
      expect(parseTileNumber('123'), isNull);
    });

    test('sık karışan karakterleri düzeltir', () {
      expect(parseTileNumber('I'), 1);
      expect(parseTileNumber('l2'), 12);
      expect(parseTileNumber('1O'), 10);
      expect(parseTileNumber('S'), 5);
      expect(parseTileNumber('B'), 8);
    });
  });

  group('classifyTileColor', () {
    test('kırmızı, sarı, mavi ve siyah rakamları ayırır', () {
      expect(classifyTileColor(tilePixels((200, 30, 35))), TileColor.red);
      expect(classifyTileColor(tilePixels((235, 160, 20))), TileColor.yellow);
      expect(classifyTileColor(tilePixels((25, 90, 190))), TileColor.blue);
      expect(classifyTileColor(tilePixels((30, 30, 32))), TileColor.black);
    });

    test('karanlık ışıkta da ton korunur', () {
      List<(int, int, int)> dim((int, int, int) stroke) => [
        for (final (r, g, b) in tilePixels(stroke)) ((r * 0.55).round(), (g * 0.55).round(), (b * 0.55).round()),
      ];
      expect(classifyTileColor(dim((200, 30, 35))), TileColor.red);
      expect(classifyTileColor(dim((25, 90, 190))), TileColor.blue);
    });

    test('ince çizgide de en koyu pikseller kullanılır', () {
      expect(classifyTileColor(tilePixels((20, 70, 170), strokeRatio: 0.02)), TileColor.blue);
    });

    test('boş girdi siyah döner', () {
      expect(classifyTileColor(const []), TileColor.black);
    });
  });

  group('groupIntoRows', () {
    test('taşları iki sıraya ayırır ve soldan sağa dizer', () {
      final items = [glyph(100, 205), glyph(10, 12), glyph(60, 8), glyph(5, 200), glyph(30, 10)];
      final rows = groupIntoRows(items, (g) => g);
      expect(rows.length, 2);
      expect(rows[0].map((g) => g.left), [10, 30, 60]);
      expect(rows[1].map((g) => g.left), [5, 100]);
    });

    test('hafif eğik çekilmiş tek sıra bölünmez', () {
      final items = [for (var i = 0; i < 10; i++) glyph(i * 25.0, 100 + i * 3.0)];
      expect(groupIntoRows(items, (g) => g).length, 1);
    });
  });
}

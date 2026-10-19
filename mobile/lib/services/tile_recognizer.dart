import 'dart:io';
import 'dart:math' as math;

import 'package:flutter/foundation.dart';
import 'package:google_mlkit_text_recognition/google_mlkit_text_recognition.dart';
import 'package:image/image.dart' as img;

import '../domain/tiles/tile.dart';
import '../domain/tiles/tile_detection.dart';

/// Fotoğraftaki ıstaka taşlarını okur. Sonuç ıstaka sıralarıdır (üst sıra önce).
abstract interface class TileRecognizer {
  bool get isSupported;
  Future<List<List<DetectedTile>>> recognize(String imagePath);
}

/// Cihaz üzerinde, internetsiz çalışan okuyucu: rakamları ML Kit metin
/// tanıma bulur, rengi rakamın piksellerinden [classifyTileColor] çıkarır.
class MlKitTileRecognizer implements TileRecognizer {
  @override
  bool get isSupported =>
      !kIsWeb && (defaultTargetPlatform == TargetPlatform.android || defaultTargetPlatform == TargetPlatform.iOS);

  @override
  Future<List<List<DetectedTile>>> recognize(String imagePath) async {
    final recognizer = TextRecognizer(script: TextRecognitionScript.latin);
    try {
      final text = await recognizer.processImage(InputImage.fromFilePath(imagePath));
      final glyphs = <TextGlyph>[];
      for (final block in text.blocks) {
        for (final line in block.lines) {
          for (final element in line.elements) {
            final box = element.boundingBox;
            // "12 3" gibi birleşik okunan parçaları tek tek sayılara ayır.
            final parts = element.text.split(RegExp(r'\s+')).where((p) => p.isNotEmpty).toList();
            if (parts.length == 1) {
              glyphs.add(
                TextGlyph(text: parts.single, left: box.left, top: box.top, right: box.right, bottom: box.bottom),
              );
            } else {
              final width = box.width / parts.length;
              for (var i = 0; i < parts.length; i++) {
                glyphs.add(
                  TextGlyph(
                    text: parts[i],
                    left: box.left + width * i,
                    top: box.top,
                    right: box.left + width * (i + 1),
                    bottom: box.bottom,
                  ),
                );
              }
            }
          }
        }
      }
      final numbered = [
        for (final g in glyphs)
          if (parseTileNumber(g.text) case final n?) (glyph: g, number: n),
      ];
      if (numbered.isEmpty) return const [];

      final bytes = await File(imagePath).readAsBytes();
      final colors = await compute(_classifyColors, (
        bytes: bytes,
        boxes: [for (final n in numbered) (n.glyph.left, n.glyph.top, n.glyph.right, n.glyph.bottom)],
      ));

      final tiles = [
        for (var i = 0; i < numbered.length; i++)
          DetectedTile(number: numbered[i].number, color: colors[i], glyph: numbered[i].glyph),
      ];
      return groupIntoRows(tiles, (t) => t.glyph);
    } finally {
      await recognizer.close();
    }
  }
}

List<TileColor> _classifyColors(({Uint8List bytes, List<(double, double, double, double)> boxes}) input) {
  final decoded = img.decodeImage(input.bytes);
  if (decoded == null) return [for (final _ in input.boxes) TileColor.black];
  final image = img.bakeOrientation(decoded);
  return [
    for (final (left, top, right, bottom) in input.boxes)
      classifyTileColor(_samplePixels(image, left, top, right, bottom)),
  ];
}

List<(int, int, int)> _samplePixels(img.Image image, double l, double t, double r, double b) {
  final x0 = l.floor().clamp(0, image.width - 1);
  final x1 = r.ceil().clamp(0, image.width - 1);
  final y0 = t.floor().clamp(0, image.height - 1);
  final y1 = b.ceil().clamp(0, image.height - 1);
  // Kutu başına en fazla ~2500 piksel örnekle.
  final area = (x1 - x0 + 1) * (y1 - y0 + 1);
  final stride = math.max(1, math.sqrt(area / 2500).ceil());
  final pixels = <(int, int, int)>[];
  for (var y = y0; y <= y1; y += stride) {
    for (var x = x0; x <= x1; x += stride) {
      final p = image.getPixel(x, y);
      pixels.add((p.r.toInt(), p.g.toInt(), p.b.toInt()));
    }
  }
  return pixels;
}

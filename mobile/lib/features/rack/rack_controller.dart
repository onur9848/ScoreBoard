import 'package:flutter/foundation.dart';
import 'package:uuid/uuid.dart';

import '../../domain/tiles/rack_scoring.dart';
import '../../domain/tiles/tile.dart';
import '../../domain/tiles/tile_detection.dart';

class RackSlot {
  const RackSlot({required this.tile, required this.scanned, this.edited = false});

  final Tile tile;

  /// Kameradan okundu mu?
  final bool scanned;

  /// Okunduktan sonra kullanıcı değiştirdi mi?
  final bool edited;

  bool get isCorrection => !scanned || edited;
}

/// Sanal ıstakanın durumu: iki sıra taş, gösterge ve puanlama seçenekleri.
class RackController extends ChangeNotifier {
  RackController({required this._options, String Function()? newId}) : _newId = newId ?? const Uuid().v4;

  static const rowCount = 2;

  final String Function() _newId;
  final List<List<RackSlot>> _rows = List.generate(rowCount, (_) => <RackSlot>[]);
  RackScoreOptions _options;
  int _deletedScanned = 0;
  int _recognized = 0;
  bool _usedCamera = false;

  List<List<RackSlot>> get rows => [for (final r in _rows) List.unmodifiable(r)];
  RackScoreOptions get options => _options;
  bool get usedCamera => _usedCamera;
  int get recognizedTileCount => _recognized;
  int get tileCount => _rows.fold(0, (s, r) => s + r.length);

  /// Eklenen, düzeltilen ve silinen okunmuş taşların toplamı.
  int get correctedTileCount =>
      _deletedScanned + _rows.fold(0, (s, r) => s + r.where((slot) => slot.isCorrection).length);

  List<Tile> get tiles => [
    for (final r in _rows)
      for (final s in r) s.tile,
  ];

  RackScore get score => RackScoring.score(tiles, _options);

  String newTileId() => _newId();

  void setOptions(RackScoreOptions options) {
    _options = options;
    notifyListeners();
  }

  /// Kamera sonucunu ıstakaya dizer; mevcut taşların yerine geçer.
  void loadDetections(List<List<DetectedTile>> detectedRows) {
    for (final r in _rows) {
      r.clear();
    }
    _deletedScanned = 0;
    _usedCamera = true;
    // Üçten fazla sıra okunduysa fazlası alt sıraya eklenir.
    for (final (i, row) in detectedRows.indexed) {
      final target = _rows[i < rowCount ? i : rowCount - 1];
      for (final d in row) {
        target.add(
          RackSlot(
            tile: Tile(id: _newId(), color: d.color, number: d.number),
            scanned: true,
          ),
        );
      }
    }
    _recognized = tileCount;
    notifyListeners();
  }

  void add(int row, Tile tile) {
    _rows[row].add(RackSlot(tile: tile, scanned: false));
    notifyListeners();
  }

  void replace(int row, int index, Tile tile) {
    final old = _rows[row][index];
    if (old.tile.sameFace(tile)) return;
    _rows[row][index] = RackSlot(tile: tile, scanned: old.scanned, edited: old.scanned);
    notifyListeners();
  }

  void remove(int row, int index) {
    final old = _rows[row].removeAt(index);
    if (old.scanned) _deletedScanned++;
    notifyListeners();
  }

  void moveToOtherRow(int row, int index) {
    final slot = _rows[row].removeAt(index);
    _rows[(row + 1) % rowCount].add(slot);
    notifyListeners();
  }

  /// [newIndex], [oldIndex]'teki taş çıkarıldıktan sonraki konumdur.
  void reorder(int row, int oldIndex, int newIndex) {
    final list = _rows[row];
    list.insert(newIndex, list.removeAt(oldIndex));
    notifyListeners();
  }

  void clear() {
    for (final r in _rows) {
      _deletedScanned += r.where((s) => s.scanned).length;
      r.clear();
    }
    notifyListeners();
  }
}

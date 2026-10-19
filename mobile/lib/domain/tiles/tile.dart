enum TileColor {
  red('Kırmızı'),
  yellow('Sarı'),
  blue('Mavi'),
  black('Siyah');

  const TileColor(this.label);

  final String label;
}

/// Okey taşı. Sahte okeyin rengi/sayısı yoktur; değeri göstergeye göre belirlenir.
class Tile {
  const Tile({required this.id, this.color, this.number, this.isFakeOkey = false})
    : assert(isFakeOkey || (color != null && number != null && number >= 1 && number <= 13));

  const Tile.fakeOkey({required this.id}) : color = null, number = null, isFakeOkey = true;

  final String id;
  final TileColor? color;
  final int? number;
  final bool isFakeOkey;

  bool sameFace(Tile other) => isFakeOkey == other.isFakeOkey && color == other.color && number == other.number;

  Tile copyWith({TileColor? color, int? number}) =>
      Tile(id: id, color: color ?? this.color, number: number ?? this.number);

  @override
  String toString() => isFakeOkey ? 'Tile(sahte okey)' : 'Tile(${color!.name} $number)';
}

/// Göstergenin bir üstü okeydir; 13'ün üstü 1'dir.
({TileColor color, int number}) okeyFromIndicator(Tile indicator) {
  assert(!indicator.isFakeOkey);
  final next = indicator.number! == 13 ? 1 : indicator.number! + 1;
  return (color: indicator.color!, number: next);
}

import 'package:flutter/material.dart';

import '../../core/theme.dart';
import '../../domain/tiles/tile.dart';

/// Tek bir okey taşının çizimi.
class TileView extends StatelessWidget {
  const TileView({
    super.key,
    required this.tile,
    this.isOkey = false,
    this.highlight = false,
    this.width = 40,
    this.onTap,
  });

  final Tile? tile;
  final bool isOkey;

  /// Kullanıcının düzelttiği/eklediği taşlar için küçük işaret.
  final bool highlight;
  final double width;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    final height = width * 1.42;
    final t = tile;
    const face = Palette.tileFace;
    final Widget content;
    if (t == null) {
      content = Icon(Icons.add, color: Palette.woodDark, size: width * 0.5);
    } else if (t.isFakeOkey) {
      content = Icon(Icons.star_rounded, color: const Color(0xFF2E7D32), size: width * 0.62);
    } else {
      final color = AppTheme.tileColor(t.color!);
      content = Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Text(
            '${t.number}',
            style: TextStyle(color: color, fontWeight: FontWeight.w800, fontSize: width * 0.5, height: 1),
          ),
          SizedBox(height: width * 0.08),
          Container(
            width: width * 0.22,
            height: width * 0.22,
            decoration: BoxDecoration(color: color, shape: BoxShape.circle),
          ),
        ],
      );
    }

    return Semantics(
      button: onTap != null,
      label: t == null ? 'Boş yuva' : (t.isFakeOkey ? 'Sahte okey' : '${t.color!.label} ${t.number}'),
      child: GestureDetector(
        onTap: onTap,
        child: Container(
          width: width,
          height: height,
          decoration: BoxDecoration(
            color: t == null ? face.withValues(alpha: 0.75) : face,
            borderRadius: BorderRadius.circular(width * 0.16),
            border: Border.all(
              color: isOkey ? const Color(0xFFFFB300) : const Color(0xFFE2D6BE),
              width: isOkey ? 3 : 1,
            ),
            boxShadow: t == null
                ? null
                : const [BoxShadow(color: Color(0x33000000), blurRadius: 2, offset: Offset(0, 2))],
          ),
          child: Stack(
            children: [
              Center(child: content),
              if (highlight)
                Positioned(
                  top: 3,
                  right: 3,
                  child: Container(
                    width: 6,
                    height: 6,
                    decoration: const BoxDecoration(color: Palette.wood, shape: BoxShape.circle),
                  ),
                ),
            ],
          ),
        ),
      ),
    );
  }
}

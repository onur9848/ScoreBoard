import 'package:flutter/material.dart';

import 'theme.dart';

/// Tebeşir yazısı stili.
TextStyle chalkStyle(double size, {Color color = Palette.chalk, double? height}) =>
    TextStyle(fontFamily: AppTheme.chalkFont, fontSize: size, color: color, height: height);

/// Logodaki gibi ahşap çerçeveli kara tahta.
class Chalkboard extends StatelessWidget {
  const Chalkboard({
    super.key,
    required this.child,
    this.padding = const EdgeInsets.all(16),
    this.frame = 8,
    this.radius = 20,
    this.onTap,
  });

  final Widget child;
  final EdgeInsetsGeometry padding;

  /// Ahşap çerçeve kalınlığı.
  final double frame;
  final double radius;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    final inner = BorderRadius.circular((radius - frame / 2).clamp(4, radius));
    Widget board = DecoratedBox(
      decoration: BoxDecoration(gradient: Palette.boardGradient, borderRadius: inner),
      child: Padding(padding: padding, child: child),
    );
    if (onTap != null) {
      board = Material(
        color: Colors.transparent,
        borderRadius: inner,
        clipBehavior: Clip.antiAlias,
        child: InkWell(onTap: onTap, splashColor: Palette.chalk.withValues(alpha: 0.08), child: board),
      );
    }
    return DecoratedBox(
      decoration: BoxDecoration(
        gradient: Palette.woodGradient,
        borderRadius: BorderRadius.circular(radius),
        border: Border.all(color: Palette.woodDeep.withValues(alpha: 0.6)),
        boxShadow: const [BoxShadow(color: Color(0x33000000), blurRadius: 12, offset: Offset(0, 6))],
      ),
      child: Padding(padding: EdgeInsets.all(frame), child: board),
    );
  }
}

/// Ahşap dolgulu kart (ıstaka ve tahta çerçevesi rengi).
class WoodCard extends StatelessWidget {
  const WoodCard({super.key, required this.child, this.onTap, this.padding = const EdgeInsets.all(16)});

  final Widget child;
  final VoidCallback? onTap;
  final EdgeInsetsGeometry padding;

  @override
  Widget build(BuildContext context) => DecoratedBox(
    decoration: BoxDecoration(
      gradient: Palette.woodGradient,
      borderRadius: BorderRadius.circular(20),
      border: Border.all(color: Palette.woodDeep.withValues(alpha: 0.5)),
      boxShadow: const [BoxShadow(color: Color(0x2E000000), blurRadius: 10, offset: Offset(0, 5))],
    ),
    child: Material(
      color: Colors.transparent,
      borderRadius: BorderRadius.circular(20),
      clipBehavior: Clip.antiAlias,
      child: InkWell(
        onTap: onTap,
        child: Padding(padding: padding, child: child),
      ),
    ),
  );
}

/// Okey taşı biçiminde ikon rozeti.
class TileBadge extends StatelessWidget {
  const TileBadge({super.key, this.icon, this.label, this.color = Palette.tileRed, this.size = 44});

  final IconData? icon;
  final String? label;
  final Color color;
  final double size;

  @override
  Widget build(BuildContext context) => Container(
    width: size,
    height: size * 1.18,
    alignment: Alignment.center,
    decoration: BoxDecoration(
      color: Palette.tileFace,
      borderRadius: BorderRadius.circular(size * 0.2),
      border: Border.all(color: const Color(0xFFE2D6BE)),
      boxShadow: const [BoxShadow(color: Color(0x40000000), blurRadius: 3, offset: Offset(0, 2))],
    ),
    child: label != null
        ? Text(
            label!,
            style: TextStyle(color: color, fontWeight: FontWeight.w900, fontSize: size * 0.5, height: 1),
          )
        : Icon(icon, color: color, size: size * 0.56),
  );
}

/// Tebeşirle çizilmiş gibi yatay çizgi.
class ChalkLine extends StatelessWidget {
  const ChalkLine({super.key, this.thickness = 2, this.color = Palette.chalk, this.opacity = 0.55});

  final double thickness;
  final Color color;
  final double opacity;

  @override
  Widget build(BuildContext context) => Container(
    height: thickness,
    decoration: BoxDecoration(
      color: color.withValues(alpha: opacity),
      borderRadius: BorderRadius.circular(thickness),
    ),
  );
}

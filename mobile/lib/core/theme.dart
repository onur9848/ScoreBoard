import 'package:flutter/material.dart';

import '../domain/tiles/tile.dart';

/// Eski Compose temasının (ui/compose/theme/Theme.kt) birebir renkleri.
abstract final class AppTheme {
  static const _light = ColorScheme(
    brightness: Brightness.light,
    primary: Color(0xFF1565C0),
    onPrimary: Colors.white,
    primaryContainer: Color(0xFFD4E4F7),
    onPrimaryContainer: Color(0xFF0D47A1),
    secondary: Color(0xFFFF7043),
    onSecondary: Colors.white,
    secondaryContainer: Color(0xFFFFE0B2),
    onSecondaryContainer: Color(0xFFE65100),
    tertiary: Color(0xFF673AB7),
    onTertiary: Colors.white,
    tertiaryContainer: Color(0xFFEDE7F6),
    onTertiaryContainer: Color(0xFF311B92),
    error: Color(0xFFBA1A1A),
    onError: Colors.white,
    errorContainer: Color(0xFFFFDAD6),
    onErrorContainer: Color(0xFF410002),
    surface: Color(0xFFFFFBFE),
    onSurface: Color(0xFF1C1B1F),
    surfaceContainerHighest: Color(0xFFE2E2E9),
    onSurfaceVariant: Color(0xFF45464F),
    outline: Color(0xFF757575),
    outlineVariant: Color(0xFFCAC5CD),
    scrim: Colors.black,
  );

  static const _dark = ColorScheme(
    brightness: Brightness.dark,
    primary: Color(0xFF90CAF9),
    onPrimary: Color(0xFF0D47A1),
    primaryContainer: Color(0xFF1565C0),
    onPrimaryContainer: Colors.white,
    secondary: Color(0xFFFFB5A0),
    onSecondary: Color(0xFF5C1900),
    secondaryContainer: Color(0xFF7D2900),
    onSecondaryContainer: Color(0xFFFFDBC0),
    tertiary: Color(0xFFB39DDB),
    onTertiary: Color(0xFF311B92),
    tertiaryContainer: Color(0xFF512DA8),
    onTertiaryContainer: Color(0xFFEDE7F6),
    error: Color(0xFFFFB4AB),
    onError: Color(0xFF690005),
    errorContainer: Color(0xFF93000A),
    onErrorContainer: Color(0xFFFFDAD6),
    surface: Color(0xFF1C1B1F),
    onSurface: Color(0xFFE6E1E5),
    surfaceContainerHighest: Color(0xFF45464F),
    onSurfaceVariant: Color(0xFFCAC5CD),
    outline: Color(0xFF949599),
    outlineVariant: Color(0xFF45464F),
    scrim: Colors.black,
  );

  static ThemeData light() => _build(_light);
  static ThemeData dark() => _build(_dark);

  static ThemeData _build(ColorScheme scheme) {
    final base = ThemeData(colorScheme: scheme, useMaterial3: true);
    return base.copyWith(
      scaffoldBackgroundColor: scheme.surface,
      appBarTheme: AppBarTheme(
        centerTitle: true,
        backgroundColor: scheme.surface,
        foregroundColor: scheme.onSurface,
        surfaceTintColor: Colors.transparent,
        titleTextStyle: base.textTheme.titleLarge?.copyWith(color: scheme.onSurface, fontWeight: FontWeight.w600),
      ),
      cardTheme: CardThemeData(shape: RoundedCornerShape.large, clipBehavior: Clip.antiAlias, margin: EdgeInsets.zero),
      dialogTheme: const DialogThemeData(shape: RoundedCornerShape.dialog),
      inputDecorationTheme: InputDecorationTheme(border: OutlineInputBorder(borderRadius: BorderRadius.circular(12))),
      filledButtonTheme: FilledButtonThemeData(
        style: FilledButton.styleFrom(
          minimumSize: const Size.fromHeight(52),
          shape: RoundedCornerShape.large,
          textStyle: base.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w600),
        ),
      ),
      outlinedButtonTheme: OutlinedButtonThemeData(
        style: OutlinedButton.styleFrom(minimumSize: const Size.fromHeight(52), shape: RoundedCornerShape.large),
      ),
      chipTheme: base.chipTheme.copyWith(shape: RoundedCornerShape.medium),
      bottomSheetTheme: const BottomSheetThemeData(showDragHandle: true),
    );
  }

  /// Okey taşı rakam renkleri.
  static Color tileColor(TileColor color) => switch (color) {
    TileColor.red => const Color(0xFFD32F2F),
    TileColor.yellow => const Color(0xFFF9A825),
    TileColor.blue => const Color(0xFF1565C0),
    TileColor.black => const Color(0xFF212121),
  };
}

/// Compose tarafındaki ScoreBoardShapes karşılıkları.
abstract final class RoundedCornerShape {
  static const small = RoundedRectangleBorder(borderRadius: BorderRadius.all(Radius.circular(8)));
  static const medium = RoundedRectangleBorder(borderRadius: BorderRadius.all(Radius.circular(12)));
  static const large = RoundedRectangleBorder(borderRadius: BorderRadius.all(Radius.circular(16)));
  static const card = RoundedRectangleBorder(borderRadius: BorderRadius.all(Radius.circular(20)));
  static const dialog = RoundedRectangleBorder(borderRadius: BorderRadius.all(Radius.circular(24)));
}

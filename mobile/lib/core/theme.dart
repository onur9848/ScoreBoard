import 'package:flutter/material.dart';

import '../domain/tiles/tile.dart';

/// Logodaki kara tahta, ahşap çerçeve, tebeşir ve okey taşı renkleri.
abstract final class Palette {
  static const board = Color(0xFF2B3A33);
  static const boardDeep = Color(0xFF1F2A25);
  static const boardLine = Color(0xFF3E5048);
  static const chalk = Color(0xFFF3F1E7);
  static const chalkDim = Color(0xFF9AA79F);
  static const chalkPink = Color(0xFFF4A3C0);
  static const chalkYellow = Color(0xFFF6D57A);
  static const woodLight = Color(0xFFD88A45);
  static const wood = Color(0xFFB86B2E);
  static const woodDark = Color(0xFF8A4A1C);
  static const woodDeep = Color(0xFF5A2C0C);
  static const paper = Color(0xFFF7F1E5);
  static const paperDeep = Color(0xFFEDE3D1);
  static const tileFace = Color(0xFFFFFBF0);
  static const ink = Color(0xFF26201A);
  static const tileRed = Color(0xFFD32F2F);
  static const tileBlue = Color(0xFF1565C0);
  static const tileYellow = Color(0xFFE0A100);
  static const tileBlack = Color(0xFF212121);

  static const woodGradient = LinearGradient(
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
    colors: [woodLight, wood, woodDark],
  );

  static const boardGradient = LinearGradient(
    begin: Alignment.topCenter,
    end: Alignment.bottomCenter,
    colors: [Color(0xFF34433C), board, boardDeep],
  );
}

abstract final class AppTheme {
  /// Yuvarlak hatlı, okunaklı yazı tipi (Türkçe karakterler dahil).
  static const fontFamily = 'Nunito';

  static const _light = ColorScheme(
    brightness: Brightness.light,
    primary: Color(0xFF2E4A3E),
    onPrimary: Palette.chalk,
    primaryContainer: Color(0xFFD5E3D9),
    onPrimaryContainer: Color(0xFF14261E),
    secondary: Palette.wood,
    onSecondary: Colors.white,
    secondaryContainer: Color(0xFFF5DCC2),
    onSecondaryContainer: Color(0xFF4A2408),
    tertiary: Color(0xFFC62828),
    onTertiary: Colors.white,
    tertiaryContainer: Color(0xFFFADBD6),
    onTertiaryContainer: Color(0xFF5C0D0D),
    error: Color(0xFFBA1A1A),
    onError: Colors.white,
    errorContainer: Color(0xFFFFDAD6),
    onErrorContainer: Color(0xFF410002),
    surface: Palette.paper,
    onSurface: Palette.ink,
    surfaceContainerLowest: Colors.white,
    surfaceContainerLow: Color(0xFFFCF8F0),
    surfaceContainer: Palette.tileFace,
    surfaceContainerHigh: Color(0xFFF1E9DA),
    surfaceContainerHighest: Palette.paperDeep,
    onSurfaceVariant: Color(0xFF5E544A),
    outline: Color(0xFF9C8B78),
    outlineVariant: Color(0xFFDCCFBC),
    scrim: Colors.black,
  );

  static const _dark = ColorScheme(
    brightness: Brightness.dark,
    primary: Color(0xFFA9D3BC),
    onPrimary: Color(0xFF10291E),
    primaryContainer: Color(0xFF34503F),
    onPrimaryContainer: Palette.chalk,
    secondary: Color(0xFFE9A86B),
    onSecondary: Color(0xFF3D1C04),
    secondaryContainer: Color(0xFF6B3A15),
    onSecondaryContainer: Color(0xFFFFDCC0),
    tertiary: Palette.chalkPink,
    onTertiary: Color(0xFF4E1027),
    tertiaryContainer: Color(0xFF6E2A40),
    onTertiaryContainer: Color(0xFFFFD9E3),
    error: Color(0xFFFFB4AB),
    onError: Color(0xFF690005),
    errorContainer: Color(0xFF93000A),
    onErrorContainer: Color(0xFFFFDAD6),
    surface: Color(0xFF151C19),
    onSurface: Palette.chalk,
    surfaceContainerLowest: Color(0xFF101614),
    surfaceContainerLow: Color(0xFF1A221E),
    surfaceContainer: Color(0xFF1E2823),
    surfaceContainerHigh: Color(0xFF243029),
    surfaceContainerHighest: Color(0xFF2B3831),
    onSurfaceVariant: Color(0xFFB9C4BD),
    outline: Color(0xFF7C8A82),
    outlineVariant: Color(0xFF34423B),
    scrim: Colors.black,
  );

  static ThemeData light() => _build(_light);
  static ThemeData dark() => _build(_dark);

  static ThemeData _build(ColorScheme scheme) {
    final base = ThemeData(colorScheme: scheme, useMaterial3: true, fontFamily: fontFamily);
    final text = base.textTheme;
    final isDark = scheme.brightness == Brightness.dark;
    return base.copyWith(
      scaffoldBackgroundColor: scheme.surface,
      appBarTheme: AppBarTheme(
        centerTitle: true,
        backgroundColor: scheme.surface,
        foregroundColor: scheme.onSurface,
        surfaceTintColor: Colors.transparent,
        scrolledUnderElevation: 0,
        titleTextStyle: text.titleLarge?.copyWith(color: scheme.onSurface, fontWeight: FontWeight.w700),
      ),
      cardTheme: CardThemeData(
        color: scheme.surfaceContainer,
        elevation: 0,
        shape: RoundedRectangleBorder(
          borderRadius: const BorderRadius.all(Radius.circular(20)),
          side: BorderSide(color: scheme.outlineVariant),
        ),
        clipBehavior: Clip.antiAlias,
        margin: EdgeInsets.zero,
      ),
      dialogTheme: DialogThemeData(shape: RoundedCornerShape.dialog, backgroundColor: scheme.surfaceContainer),
      bottomSheetTheme: BottomSheetThemeData(showDragHandle: true, backgroundColor: scheme.surfaceContainer),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: isDark ? scheme.surfaceContainerLow : Colors.white,
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: BorderSide(color: scheme.outlineVariant),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: BorderSide(color: scheme.outlineVariant),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: BorderSide(color: scheme.primary, width: 2),
        ),
      ),
      filledButtonTheme: FilledButtonThemeData(
        style: FilledButton.styleFrom(
          minimumSize: const Size.fromHeight(52),
          shape: RoundedCornerShape.large,
          textStyle: text.titleMedium?.copyWith(fontWeight: FontWeight.w700),
        ),
      ),
      outlinedButtonTheme: OutlinedButtonThemeData(
        style: OutlinedButton.styleFrom(
          minimumSize: const Size.fromHeight(52),
          shape: RoundedCornerShape.large,
          side: BorderSide(color: scheme.outline),
          textStyle: text.titleMedium?.copyWith(fontWeight: FontWeight.w600),
        ),
      ),
      floatingActionButtonTheme: FloatingActionButtonThemeData(
        backgroundColor: scheme.secondary,
        foregroundColor: scheme.onSecondary,
        shape: RoundedCornerShape.large,
      ),
      chipTheme: base.chipTheme.copyWith(
        shape: RoundedCornerShape.medium,
        side: BorderSide(color: scheme.outlineVariant),
        backgroundColor: scheme.surfaceContainer,
        selectedColor: scheme.primaryContainer,
        checkmarkColor: scheme.onPrimaryContainer,
      ),
      segmentedButtonTheme: SegmentedButtonThemeData(
        style: SegmentedButton.styleFrom(
          selectedBackgroundColor: scheme.primary,
          selectedForegroundColor: scheme.onPrimary,
          side: BorderSide(color: scheme.outline),
        ),
      ),
      switchTheme: SwitchThemeData(
        thumbColor: WidgetStateProperty.resolveWith((s) => s.contains(WidgetState.selected) ? scheme.onPrimary : null),
        trackColor: WidgetStateProperty.resolveWith((s) => s.contains(WidgetState.selected) ? scheme.primary : null),
      ),
      dividerTheme: DividerThemeData(color: scheme.outlineVariant),
      snackBarTheme: const SnackBarThemeData(
        behavior: SnackBarBehavior.floating,
        backgroundColor: Palette.board,
        contentTextStyle: TextStyle(color: Palette.chalk),
      ),
    );
  }

  /// Okey taşı rakam renkleri.
  static Color tileColor(TileColor color) => switch (color) {
    TileColor.red => Palette.tileRed,
    TileColor.yellow => Palette.tileYellow,
    TileColor.blue => Palette.tileBlue,
    TileColor.black => Palette.tileBlack,
  };
}

abstract final class RoundedCornerShape {
  static const small = RoundedRectangleBorder(borderRadius: BorderRadius.all(Radius.circular(8)));
  static const medium = RoundedRectangleBorder(borderRadius: BorderRadius.all(Radius.circular(12)));
  static const large = RoundedRectangleBorder(borderRadius: BorderRadius.all(Radius.circular(16)));
  static const card = RoundedRectangleBorder(borderRadius: BorderRadius.all(Radius.circular(20)));
  static const dialog = RoundedRectangleBorder(borderRadius: BorderRadius.all(Radius.circular(24)));
}

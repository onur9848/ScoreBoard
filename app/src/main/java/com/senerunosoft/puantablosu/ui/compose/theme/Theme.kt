package com.senerunosoft.puantablosu.ui.compose.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

// Extended Material 3 color scheme for ScoreBoard app
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1976D2),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF90CAF9),
    onPrimaryContainer = Color(0xFF0D47A1),
    secondary = Color(0xFF00796B), // Teal for backgrounds
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB2DFDB),
    onSecondaryContainer = Color(0xFF004D40),
    tertiary = Color(0xFF03DAC6),
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF80F7F1),
    onTertiaryContainer = Color(0xFF002625),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE2E2E9),
    onSurfaceVariant = Color(0xFF45464F),
    outline = Color(0xFF757575),
    outlineVariant = Color(0xFFCAC5CD),
    scrim = Color(0xFF000000),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF0D47A1),
    primaryContainer = Color(0xFF1976D2),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF26A69A), // Lighter teal for dark mode
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF00695C),
    onSecondaryContainer = Color(0xFFB2DFDB),
    tertiary = Color(0xFF03DAC6),
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF005B57),
    onTertiaryContainer = Color(0xFF80F7F1),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF45464F),
    onSurfaceVariant = Color(0xFFCAC5CD),
    outline = Color(0xFF949599),
    outlineVariant = Color(0xFF45464F),
    scrim = Color(0xFF000000),
)

// Custom extended colors for the ScoreBoard app
object ScoreBoardColors {
    val GameBackground = Color(0xFF00796B) // Teal 700
    val GameBackgroundDark = Color(0xFF004D40) // Teal 900
    val PlayerCardBackground = Color.White.copy(alpha = 0.1f)
    val ScoreBoardDivider = Color.Black
    val ErrorRed = Color(0xFFE57373)
    val SuccessGreen = Color(0xFF81C784)
}

@Composable
fun ScoreBoardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable dynamic colors for consistent branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ScoreBoardTypography,
        shapes = ScoreBoardShapes,
        content = content
    )
}

// Animation utilities for transitions
@Composable
fun rememberScoreAnimation(): Animatable<Float, *> {
    val animatable = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        animatable.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 300,
                easing = LinearEasing
            )
        )
    }
    
    return animatable
}

// Custom animation durations
object ScoreBoardAnimations {
    const val SHORT_DURATION = 150
    const val MEDIUM_DURATION = 300
    const val LONG_DURATION = 500
    
    const val SCORE_ENTRY_DURATION = 200
    const val DIALOG_ENTER_DURATION = 250
    const val PAGE_TRANSITION_DURATION = 400
}
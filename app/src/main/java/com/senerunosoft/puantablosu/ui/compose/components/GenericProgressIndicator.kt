package com.senerunosoft.puantablosu.ui.compose.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.senerunosoft.puantablosu.ui.compose.theme.ScoreBoardTheme

/**
 * Generic Progress Indicator component that can show both linear and circular progress
 * Supports both determinate and indeterminate states
 */
@Composable
fun GenericProgressIndicator(
    value: Float = 0f,
    isIndeterminate: Boolean = false,
    isCircular: Boolean = true,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = ProgressIndicatorDefaults.linearColor,
    trackColor: androidx.compose.ui.graphics.Color = ProgressIndicatorDefaults.linearTrackColor,
    strokeWidth: androidx.compose.ui.unit.Dp = ProgressIndicatorDefaults.CircularStrokeWidth
) {
    if (isCircular) {
        if (isIndeterminate) {
            CircularProgressIndicator(
                modifier = modifier,
                color = color,
                strokeWidth = strokeWidth,
                trackColor = trackColor
            )
        } else {
            CircularProgressIndicator(
                progress = { value },
                modifier = modifier,
                color = color,
                strokeWidth = strokeWidth,
                trackColor = trackColor
            )
        }
    } else {
        if (isIndeterminate) {
            LinearProgressIndicator(
                modifier = modifier,
                color = color,
                trackColor = trackColor
            )
        } else {
            LinearProgressIndicator(
                progress = { value },
                modifier = modifier,
                color = color,
                trackColor = trackColor
            )
        }
    }
}

// Preview functions
@Preview(showBackground = true)
@Composable
fun GenericProgressIndicatorCircularIndeterminatePreview() {
    ScoreBoardTheme {
        GenericProgressIndicator(
            isIndeterminate = true,
            isCircular = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GenericProgressIndicatorCircularDeterminatePreview() {
    ScoreBoardTheme {
        GenericProgressIndicator(
            value = 0.7f,
            isIndeterminate = false,
            isCircular = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GenericProgressIndicatorLinearIndeterminatePreview() {
    ScoreBoardTheme {
        GenericProgressIndicator(
            isIndeterminate = true,
            isCircular = false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GenericProgressIndicatorLinearDeterminatePreview() {
    ScoreBoardTheme {
        GenericProgressIndicator(
            value = 0.4f,
            isIndeterminate = false,
            isCircular = false
        )
    }
}
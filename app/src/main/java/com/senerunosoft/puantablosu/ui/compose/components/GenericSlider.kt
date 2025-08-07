package com.senerunosoft.puantablosu.ui.compose.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.senerunosoft.puantablosu.ui.compose.theme.ScoreBoardTheme

/**
 * Generic Slider component for numeric values
 * Provides reusable slider functionality with customizable range and step
 */
@Composable
fun GenericSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    minValue: Float,
    maxValue: Float,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    steps: Int = 0,
    colors: SliderColors = SliderDefaults.colors(),
    onValueChangeFinished: (() -> Unit)? = null
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        valueRange = minValue..maxValue,
        steps = steps,
        onValueChangeFinished = onValueChangeFinished,
        colors = colors
    )
}

/**
 * Generic Range Slider component for selecting a range of values
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericRangeSlider(
    value: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    minValue: Float,
    maxValue: Float,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    steps: Int = 0,
    colors: SliderColors = SliderDefaults.colors(),
    onValueChangeFinished: (() -> Unit)? = null
) {
    RangeSlider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        valueRange = minValue..maxValue,
        steps = steps,
        onValueChangeFinished = onValueChangeFinished,
        colors = colors
    )
}

// Preview functions
@Preview(showBackground = true)
@Composable
fun GenericSliderPreview() {
    ScoreBoardTheme {
        var sliderValue by remember { mutableStateOf(0.5f) }
        GenericSlider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            minValue = 0f,
            maxValue = 1f
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GenericRangeSliderPreview() {
    ScoreBoardTheme {
        var rangeValue by remember { mutableStateOf(0.2f..0.8f) }
        GenericRangeSlider(
            value = rangeValue,
            onValueChange = { rangeValue = it },
            minValue = 0f,
            maxValue = 1f
        )
    }
}
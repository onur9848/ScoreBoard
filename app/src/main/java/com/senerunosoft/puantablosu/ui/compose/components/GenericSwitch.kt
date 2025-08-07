package com.senerunosoft.puantablosu.ui.compose.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.senerunosoft.puantablosu.ui.compose.theme.ScoreBoardTheme

/**
 * Generic Switch component for boolean state management
 * Provides reusable switch functionality with customizable styling
 */
@Composable
fun GenericSwitch(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: SwitchColors = SwitchDefaults.colors()
) {
    Switch(
        checked = isChecked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = colors
    )
}

/**
 * Generic Checkbox component for boolean state management
 */
@Composable
fun GenericCheckbox(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: CheckboxColors = CheckboxDefaults.colors()
) {
    Checkbox(
        checked = isChecked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = colors
    )
}

/**
 * Generic TriState Checkbox component for nullable boolean state
 */
@Composable
fun GenericTriStateCheckbox(
    state: androidx.compose.ui.state.ToggleableState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: CheckboxColors = CheckboxDefaults.colors()
) {
    TriStateCheckbox(
        state = state,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = colors
    )
}

/**
 * Generic Radio Button component for single selection from group
 */
@Composable
fun GenericRadioButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: RadioButtonColors = RadioButtonDefaults.colors()
) {
    RadioButton(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = colors
    )
}

// Preview functions
@Preview(showBackground = true)
@Composable
fun GenericSwitchPreview() {
    ScoreBoardTheme {
        var isChecked by remember { mutableStateOf(false) }
        GenericSwitch(
            isChecked = isChecked,
            onCheckedChange = { isChecked = it }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GenericCheckboxPreview() {
    ScoreBoardTheme {
        var isChecked by remember { mutableStateOf(false) }
        GenericCheckbox(
            isChecked = isChecked,
            onCheckedChange = { isChecked = it }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GenericTriStateCheckboxPreview() {
    ScoreBoardTheme {
        var state by remember { 
            mutableStateOf(androidx.compose.ui.state.ToggleableState.Off) 
        }
        GenericTriStateCheckbox(
            state = state,
            onClick = {
                state = when (state) {
                    androidx.compose.ui.state.ToggleableState.Off -> 
                        androidx.compose.ui.state.ToggleableState.Indeterminate
                    androidx.compose.ui.state.ToggleableState.Indeterminate -> 
                        androidx.compose.ui.state.ToggleableState.On
                    androidx.compose.ui.state.ToggleableState.On -> 
                        androidx.compose.ui.state.ToggleableState.Off
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GenericRadioButtonPreview() {
    ScoreBoardTheme {
        var selected by remember { mutableStateOf(false) }
        GenericRadioButton(
            selected = selected,
            onClick = { selected = !selected }
        )
    }
}
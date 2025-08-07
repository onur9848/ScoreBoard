package com.senerunosoft.puantablosu.ui.compose.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.senerunosoft.puantablosu.ui.compose.theme.ScoreBoardTheme

/**
 * Generic Text Field component that can handle any data type T
 * Provides reusable text input functionality with type-safe value handling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> GenericTextField(
    value: T,
    onValueChange: (T) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    valueToString: (T) -> String = { it.toString() },
    stringToValue: (String) -> T? = null,
    supportingText: (@Composable () -> Unit)? = null,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = valueToString(value),
        onValueChange = { newStringValue ->
            stringToValue?.let { converter ->
                converter(newStringValue)?.let { newValue ->
                    onValueChange(newValue)
                }
            } ?: run {
                // Fallback: try to handle basic types
                @Suppress("UNCHECKED_CAST")
                when (value) {
                    is String -> onValueChange(newStringValue as T)
                    is Int -> newStringValue.toIntOrNull()?.let { onValueChange(it as T) }
                    is Double -> newStringValue.toDoubleOrNull()?.let { onValueChange(it as T) }
                    is Float -> newStringValue.toFloatOrNull()?.let { onValueChange(it as T) }
                    else -> Unit // No conversion available
                }
            }
        },
        label = { Text(label) },
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        keyboardOptions = keyboardOptions,
        colors = colors,
        supportingText = supportingText,
        isError = isError
    )
}

// Preview functions
@Preview(showBackground = true)
@Composable
fun GenericTextFieldStringPreview() {
    ScoreBoardTheme {
        var text by remember { mutableStateOf("") }
        GenericTextField(
            value = text,
            onValueChange = { text = it },
            label = "Enter text"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GenericTextFieldIntPreview() {
    ScoreBoardTheme {
        var number by remember { mutableStateOf(0) }
        GenericTextField(
            value = number,
            onValueChange = { number = it },
            label = "Enter number",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}
package com.senerunosoft.puantablosu.ui.compose.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import com.senerunosoft.puantablosu.ui.compose.theme.ScoreBoardTheme

/**
 * Generic Button component that can handle any data type T
 * Provides reusable button functionality with customizable click handling
 */
@Composable
fun <T> GenericButton(
    onClick: (T) -> Unit,
    text: String,
    value: T,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors()
) {
    Button(
        onClick = { onClick(value) },
        modifier = modifier,
        enabled = enabled,
        colors = colors
    ) {
        Text(text = text)
    }
}

/**
 * Generic Icon Button component that can handle any data type T
 * Provides reusable icon button functionality with customizable click handling
 */
@Composable
fun <T> GenericIconButton(
    onClick: (T) -> Unit,
    icon: ImageVector,
    value: T,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    contentDescription: String? = null
) {
    IconButton(
        onClick = { onClick(value) },
        modifier = modifier,
        enabled = enabled,
        colors = colors
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription
        )
    }
}

// Preview functions
@Preview(showBackground = true)
@Composable
fun GenericButtonPreview() {
    ScoreBoardTheme {
        GenericButton(
            onClick = { value: String -> 
                // Handle click with value
            },
            text = "Click Me",
            value = "test-value"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GenericIconButtonPreview() {
    ScoreBoardTheme {
        GenericIconButton(
            onClick = { value: Int -> 
                // Handle click with value
            },
            icon = Icons.Default.Add,
            value = 42,
            contentDescription = "Add button"
        )
    }
}
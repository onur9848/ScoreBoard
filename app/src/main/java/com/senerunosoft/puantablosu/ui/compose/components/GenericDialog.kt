package com.senerunosoft.puantablosu.ui.compose.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.senerunosoft.puantablosu.ui.compose.theme.ScoreBoardTheme

/**
 * Generic Dialog component that can display any data type T
 * Provides reusable dialog functionality with customizable content and actions
 */
@Composable
fun <T> GenericDialog(
    openDialog: Boolean,
    onDismissRequest: () -> Unit,
    title: String,
    content: @Composable (T) -> Unit,
    item: T,
    modifier: Modifier = Modifier,
    confirmButton: @Composable ((T) -> Unit)? = null,
    dismissButton: @Composable ((T) -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    properties: DialogProperties = DialogProperties()
) {
    if (openDialog) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = { Text(title) },
            text = { content(item) },
            modifier = modifier,
            icon = icon,
            confirmButton = {
                confirmButton?.invoke(item) ?: TextButton(onClick = onDismissRequest) {
                    Text("OK")
                }
            },
            dismissButton = dismissButton?.let { { it(item) } },
            properties = properties
        )
    }
}

/**
 * Generic Custom Dialog with full customization
 */
@Composable
fun <T> GenericCustomDialog(
    openDialog: Boolean,
    onDismissRequest: () -> Unit,
    content: @Composable (T, () -> Unit) -> Unit,
    item: T,
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties()
) {
    if (openDialog) {
        Dialog(
            onDismissRequest = onDismissRequest,
            properties = properties
        ) {
            content(item, onDismissRequest)
        }
    }
}

/**
 * Data class for dialog preview
 */
data class DialogData(
    val message: String,
    val details: String
)

// Preview functions
@Preview(showBackground = true)
@Composable
fun GenericDialogStringPreview() {
    ScoreBoardTheme {
        GenericDialog(
            openDialog = true,
            onDismissRequest = { },
            title = "Sample Dialog",
            content = { text ->
                Text(text = text)
            },
            item = "This is a sample dialog content",
            confirmButton = { _ ->
                TextButton(onClick = { }) {
                    Text("Confirm")
                }
            },
            dismissButton = { _ ->
                TextButton(onClick = { }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GenericDialogDataPreview() {
    ScoreBoardTheme {
        val dialogData = DialogData(
            message = "Important Message",
            details = "This is additional detail information"
        )
        
        GenericDialog(
            openDialog = true,
            onDismissRequest = { },
            title = "Data Dialog",
            content = { data ->
                Column {
                    Text(
                        text = data.message,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = data.details,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            item = dialogData,
            confirmButton = { data ->
                TextButton(onClick = { 
                    // Handle confirm with data
                }) {
                    Text("Process ${data.message}")
                }
            }
        )
    }
}
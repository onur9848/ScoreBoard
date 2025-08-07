package com.senerunosoft.puantablosu.ui.compose.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.senerunosoft.puantablosu.ui.compose.theme.ScoreBoardTheme

/**
 * Generic List Item component that can display any data type T
 * Provides reusable list item functionality with customizable content
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> GenericListItem(
    item: T,
    content: @Composable (T) -> Unit,
    modifier: Modifier = Modifier,
    colors: ListItemColors = ListItemDefaults.colors(),
    onClick: ((T) -> Unit)? = null,
    onLongClick: ((T) -> Unit)? = null,
    leadingContent: (@Composable (T) -> Unit)? = null,
    trailingContent: (@Composable (T) -> Unit)? = null
) {
    ListItem(
        headlineContent = { content(item) },
        modifier = modifier,
        colors = colors,
        leadingContent = leadingContent?.let { { it(item) } },
        trailingContent = trailingContent?.let { { it(item) } }
    )
}

/**
 * Generic Clickable List Item with gesture support
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> GenericClickableListItem(
    item: T,
    content: @Composable (T) -> Unit,
    onClick: (T) -> Unit,
    modifier: Modifier = Modifier,
    colors: ListItemColors = ListItemDefaults.colors(),
    onLongClick: ((T) -> Unit)? = null,
    leadingContent: (@Composable (T) -> Unit)? = null,
    trailingContent: (@Composable (T) -> Unit)? = null
) {
    ListItem(
        headlineContent = { content(item) },
        modifier = modifier,
        colors = colors,
        leadingContent = leadingContent?.let { { it(item) } },
        trailingContent = trailingContent?.let { { it(item) } }
    )
}

/**
 * Data class for list item preview
 */
data class SampleListItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val value: Int
)

// Preview functions
@Preview(showBackground = true)
@Composable
fun GenericListItemPreview() {
    ScoreBoardTheme {
        val sampleItem = SampleListItem(
            id = "1",
            title = "Sample Title",
            subtitle = "Sample Subtitle",
            value = 123
        )
        
        GenericListItem(
            item = sampleItem,
            content = { item ->
                Column {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            trailingContent = { item ->
                Text(
                    text = item.value.toString(),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GenericListItemStringPreview() {
    ScoreBoardTheme {
        GenericListItem(
            item = "Simple text item",
            content = { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        )
    }
}
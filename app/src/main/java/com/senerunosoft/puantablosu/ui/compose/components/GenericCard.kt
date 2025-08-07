package com.senerunosoft.puantablosu.ui.compose.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.senerunosoft.puantablosu.ui.compose.theme.ScoreBoardTheme

/**
 * Generic Card component that can display any data type T
 * Provides reusable card functionality with customizable content
 */
@Composable
fun <T> GenericCard(
    content: @Composable (T) -> Unit,
    item: T,
    modifier: Modifier = Modifier,
    colors: CardColors = CardDefaults.cardColors(),
    elevation: CardElevation = CardDefaults.cardElevation(),
    onClick: ((T) -> Unit)? = null
) {
    if (onClick != null) {
        Card(
            onClick = { onClick(item) },
            modifier = modifier,
            colors = colors,
            elevation = elevation
        ) {
            content(item)
        }
    } else {
        Card(
            modifier = modifier,
            colors = colors,
            elevation = elevation
        ) {
            content(item)
        }
    }
}

/**
 * Data class for preview demonstration
 */
data class SampleCardData(
    val title: String,
    val description: String,
    val value: Int
)

// Preview functions
@Preview(showBackground = true)
@Composable
fun GenericCardStringPreview() {
    ScoreBoardTheme {
        GenericCard(
            content = { text ->
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            },
            item = "This is a generic card with string content"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GenericCardDataClassPreview() {
    ScoreBoardTheme {
        val sampleData = SampleCardData(
            title = "Sample Title",
            description = "This is a sample description",
            value = 42
        )
        
        GenericCard(
            content = { data ->
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = data.title,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = data.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Value: ${data.value}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            item = sampleData,
            onClick = { data ->
                // Handle card click with data
            }
        )
    }
}
package com.senerunosoft.puantablosu.ui.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senerunosoft.puantablosu.ui.compose.theme.ScoreBoardTheme

/**
 * Generic Badge component that can display count or custom content
 * Provides reusable badge functionality for notifications and indicators
 */
@Composable
fun GenericBadge(
    count: Int,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.error,
    contentColor: Color = MaterialTheme.colorScheme.onError,
    shape: Shape = CircleShape,
    showZero: Boolean = false
) {
    BadgedBox(
        badge = {
            if (count > 0 || (count == 0 && showZero)) {
                Badge(
                    containerColor = containerColor,
                    contentColor = contentColor,
                    modifier = Modifier.clip(shape)
                ) {
                    Text(
                        text = if (count > 99) "99+" else count.toString(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        modifier = modifier
    ) {
        content()
    }
}

/**
 * Generic Custom Badge with fully customizable content
 */
@Composable
fun <T> GenericCustomBadge(
    data: T,
    content: @Composable () -> Unit,
    badgeContent: @Composable (T) -> Unit,
    showBadge: (T) -> Boolean = { true },
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.error,
    contentColor: Color = MaterialTheme.colorScheme.onError,
    shape: Shape = CircleShape
) {
    BadgedBox(
        badge = {
            if (showBadge(data)) {
                Badge(
                    containerColor = containerColor,
                    contentColor = contentColor,
                    modifier = Modifier.clip(shape)
                ) {
                    badgeContent(data)
                }
            }
        },
        modifier = modifier
    ) {
        content()
    }
}

/**
 * Generic Dot Badge for simple notification indicator
 */
@Composable
fun GenericDotBadge(
    show: Boolean,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.error,
    size: androidx.compose.ui.unit.Dp = 8.dp
) {
    BadgedBox(
        badge = {
            if (show) {
                Box(
                    modifier = Modifier
                        .size(size)
                        .background(color, CircleShape)
                )
            }
        },
        modifier = modifier
    ) {
        content()
    }
}

/**
 * Data class for custom badge preview
 */
data class NotificationData(
    val count: Int,
    val type: String,
    val isImportant: Boolean
)

// Preview functions
@Preview(showBackground = true)
@Composable
fun GenericBadgePreview() {
    ScoreBoardTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            GenericBadge(
                count = 5,
                content = {
                    Button(onClick = {}) {
                        Text("Messages")
                    }
                }
            )
            
            GenericBadge(
                count = 0,
                showZero = true,
                content = {
                    Button(onClick = {}) {
                        Text("No Items")
                    }
                }
            )
            
            GenericBadge(
                count = 150,
                content = {
                    Button(onClick = {}) {
                        Text("Many Items")
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GenericCustomBadgePreview() {
    ScoreBoardTheme {
        val notificationData = NotificationData(
            count = 3,
            type = "urgent",
            isImportant = true
        )
        
        GenericCustomBadge(
            data = notificationData,
            content = {
                Button(onClick = {}) {
                    Text("Notifications")
                }
            },
            badgeContent = { data ->
                Text(
                    text = data.count.toString(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            showBadge = { data -> data.count > 0 },
            containerColor = if (notificationData.isImportant) {
                Color.Red
            } else {
                MaterialTheme.colorScheme.primary
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GenericDotBadgePreview() {
    ScoreBoardTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            GenericDotBadge(
                show = true,
                content = {
                    Button(onClick = {}) {
                        Text("Online")
                    }
                },
                color = Color.Green
            )
            
            GenericDotBadge(
                show = false,
                content = {
                    Button(onClick = {}) {
                        Text("Offline")
                    }
                }
            )
        }
    }
}
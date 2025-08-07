package com.senerunosoft.puantablosu.ui.compose.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import com.senerunosoft.puantablosu.ui.compose.theme.ScoreBoardTheme

/**
 * Generic Bottom Navigation component that can handle any data type T
 * Provides reusable bottom navigation functionality with customizable items
 */
@Composable
fun <T> GenericBottomNavigation(
    items: List<T>,
    onItemClick: (T) -> Unit,
    content: @Composable (T, Boolean) -> Unit,
    selectedItem: T? = null,
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color = NavigationBarDefaults.containerColor,
    contentColor: androidx.compose.ui.graphics.Color = contentColorFor(containerColor)
) {
    NavigationBar(
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor
    ) {
        items.forEach { item ->
            val isSelected = selectedItem == item
            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemClick(item) },
                icon = { content(item, isSelected) }
            )
        }
    }
}

/**
 * Generic Navigation Rail component for larger screens
 */
@Composable
fun <T> GenericNavigationRail(
    items: List<T>,
    onItemClick: (T) -> Unit,
    content: @Composable (T, Boolean) -> Unit,
    selectedItem: T? = null,
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color = NavigationRailDefaults.ContainerColor,
    contentColor: androidx.compose.ui.graphics.Color = contentColorFor(containerColor),
    header: @Composable (ColumnScope.() -> Unit)? = null
) {
    NavigationRail(
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        header = header
    ) {
        items.forEach { item ->
            val isSelected = selectedItem == item
            NavigationRailItem(
                selected = isSelected,
                onClick = { onItemClick(item) },
                icon = { content(item, isSelected) }
            )
        }
    }
}

/**
 * Data class for navigation item preview
 */
data class NavigationItem(
    val id: String,
    val label: String,
    val icon: ImageVector
)

// Preview functions
@Preview(showBackground = true)
@Composable
fun GenericBottomNavigationPreview() {
    ScoreBoardTheme {
        val navItems = listOf(
            NavigationItem("home", "Home", Icons.Default.Home),
            NavigationItem("search", "Search", Icons.Default.Search),
            NavigationItem("settings", "Settings", Icons.Default.Settings)
        )
        
        var selectedItem by remember { mutableStateOf(navItems.first()) }
        
        GenericBottomNavigation(
            items = navItems,
            onItemClick = { selectedItem = it },
            selectedItem = selectedItem,
            content = { item, isSelected ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                    if (isSelected) {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GenericBottomNavigationStringPreview() {
    ScoreBoardTheme {
        val stringItems = listOf("Home", "Profile", "Settings")
        var selectedItem by remember { mutableStateOf(stringItems.first()) }
        
        GenericBottomNavigation(
            items = stringItems,
            onItemClick = { selectedItem = it },
            selectedItem = selectedItem,
            content = { item, isSelected ->
                Text(
                    text = item,
                    style = if (isSelected) {
                        MaterialTheme.typography.labelMedium
                    } else {
                        MaterialTheme.typography.labelSmall
                    }
                )
            }
        )
    }
}

@Preview(showBackground = true, widthDp = 120, heightDp = 400)
@Composable
fun GenericNavigationRailPreview() {
    ScoreBoardTheme {
        val navItems = listOf(
            NavigationItem("home", "Home", Icons.Default.Home),
            NavigationItem("search", "Search", Icons.Default.Search),
            NavigationItem("settings", "Settings", Icons.Default.Settings)
        )
        
        var selectedItem by remember { mutableStateOf(navItems.first()) }
        
        GenericNavigationRail(
            items = navItems,
            onItemClick = { selectedItem = it },
            selectedItem = selectedItem,
            content = { item, _ ->
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label
                )
            }
        )
    }
}
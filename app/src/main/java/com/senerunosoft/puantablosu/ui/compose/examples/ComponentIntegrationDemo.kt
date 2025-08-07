package com.senerunosoft.puantablosu.ui.compose.examples

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.senerunosoft.puantablosu.ui.compose.theme.ScoreBoardTheme
import com.senerunosoft.puantablosu.ui.compose.components.*
import com.senerunosoft.puantablosu.ui.compose.components.styles.DesignSystem

/**
 * Comprehensive Demo Screen showcasing all generic components
 * This demonstrates real-world usage patterns and integration
 */
@Composable
fun ComponentIntegrationDemo(
    designStyle: DesignSystem.Style = DesignSystem.Style.FLAT_DESIGN
) {
    var selectedTab by remember { mutableStateOf("dashboard") }
    var playerName by remember { mutableStateOf("") }
    var gameVolume by remember { mutableStateOf(0.5f) }
    var enableNotifications by remember { mutableStateOf(true) }
    var gameInProgress by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var notificationCount by remember { mutableStateOf(3) }
    
    // Sample data for demonstrations
    val navigationItems = listOf(
        NavigationItem("dashboard", "Dashboard", Icons.Default.Dashboard),
        NavigationItem("players", "Players", Icons.Default.People),
        NavigationItem("settings", "Settings", Icons.Default.Settings)
    )
    
    val samplePlayers = listOf("Alice", "Bob", "Charlie", "Diana")
    val gameProgress = remember { mutableStateOf(0.7f) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar with Badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ScoreBoard Pro",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Notification badge
            GenericBadge(
                count = notificationCount,
                content = {
                    GenericIconButton(
                        onClick = { action: String ->
                            when (action) {
                                "open_notifications" -> {
                                    notificationCount = 0
                                    // Handle notification click
                                }
                            }
                        },
                        icon = Icons.Default.Notifications,
                        value = "open_notifications",
                        contentDescription = "Notifications"
                    )
                }
            )
        }
        
        // Main content area
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            when (selectedTab) {
                "dashboard" -> DashboardContent(
                    gameInProgress = gameInProgress,
                    gameProgress = gameProgress.value,
                    players = samplePlayers,
                    designStyle = designStyle,
                    onStartGame = { gameInProgress = true },
                    onEndGame = { gameInProgress = false }
                )
                
                "players" -> PlayersContent(
                    playerName = playerName,
                    onPlayerNameChange = { playerName = it },
                    players = samplePlayers,
                    designStyle = designStyle
                )
                
                "settings" -> SettingsContent(
                    gameVolume = gameVolume,
                    onVolumeChange = { gameVolume = it },
                    enableNotifications = enableNotifications,
                    onNotificationsChange = { enableNotifications = it },
                    onShowDialog = { showSettingsDialog = true },
                    designStyle = designStyle
                )
            }
        }
        
        // Bottom Navigation
        GenericBottomNavigation(
            items = navigationItems,
            selectedItem = navigationItems.find { it.id == selectedTab },
            onItemClick = { selectedTab = it.id },
            content = { item, isSelected ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    if (isSelected) {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        )
    }
    
    // Settings Dialog
    GenericDialog(
        openDialog = showSettingsDialog,
        onDismissRequest = { showSettingsDialog = false },
        title = "Advanced Settings",
        content = { message ->
            Column {
                Text(message)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Configure your game preferences here.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        item = "Game settings will affect all future games.",
        confirmButton = { _ ->
            GenericButton(
                onClick = { _: String -> showSettingsDialog = false },
                text = "Apply",
                value = "apply_settings"
            )
        },
        dismissButton = { _ ->
            TextButton(onClick = { showSettingsDialog = false }) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun DashboardContent(
    gameInProgress: Boolean,
    gameProgress: Float,
    players: List<String>,
    designStyle: DesignSystem.Style,
    onStartGame: () -> Unit,
    onEndGame: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Dashboard",
            style = MaterialTheme.typography.headlineMedium
        )
        
        // Game Status Card
        GenericCard(
            content = { status ->
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Game Status",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (status) "Game in Progress" else "Ready to Play",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (status) Color.Green else MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (status) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Progress",
                            style = MaterialTheme.typography.bodySmall
                        )
                        GenericProgressIndicator(
                            value = gameProgress,
                            isIndeterminate = false,
                            isCircular = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            item = gameInProgress,
            colors = when (designStyle) {
                DesignSystem.Style.FLAT_DESIGN -> DesignSystem.FlatDesign.cardColors()
                DesignSystem.Style.NEUMORPHISM -> DesignSystem.Neumorphism.cardColors()
            },
            elevation = when (designStyle) {
                DesignSystem.Style.FLAT_DESIGN -> DesignSystem.FlatDesign.cardElevation()
                DesignSystem.Style.NEUMORPHISM -> DesignSystem.Neumorphism.cardElevation()
            }
        )
        
        // Game Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GenericButton(
                onClick = { action: String ->
                    when (action) {
                        "start_game" -> onStartGame()
                        "end_game" -> onEndGame()
                    }
                },
                text = if (gameInProgress) "End Game" else "Start Game",
                value = if (gameInProgress) "end_game" else "start_game",
                modifier = Modifier.weight(1f),
                colors = when (designStyle) {
                    DesignSystem.Style.FLAT_DESIGN -> DesignSystem.FlatDesign.buttonColors()
                    DesignSystem.Style.NEUMORPHISM -> DesignSystem.Neumorphism.buttonColors()
                }
            )
        }
        
        // Players List
        Text(
            text = "Active Players",
            style = MaterialTheme.typography.titleMedium
        )
        
        players.forEach { player ->
            GenericListItem(
                item = player,
                content = { name ->
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                leadingContent = { _ ->
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null
                    )
                },
                trailingContent = { _ ->
                    GenericDotBadge(
                        show = true,
                        content = { },
                        color = Color.Green
                    )
                }
            )
        }
    }
}

@Composable
private fun PlayersContent(
    playerName: String,
    onPlayerNameChange: (String) -> Unit,
    players: List<String>,
    designStyle: DesignSystem.Style
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Players",
            style = MaterialTheme.typography.headlineMedium
        )
        
        // Add Player Form
        GenericCard(
            content = { _ ->
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Add New Player",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    GenericTextField(
                        value = playerName,
                        onValueChange = onPlayerNameChange,
                        label = "Player Name"
                    )
                    
                    GenericButton(
                        onClick = { action: String ->
                            // Handle add player
                        },
                        text = "Add Player",
                        value = "add_player",
                        enabled = playerName.isNotEmpty()
                    )
                }
            },
            item = Unit,
            colors = when (designStyle) {
                DesignSystem.Style.FLAT_DESIGN -> DesignSystem.FlatDesign.cardColors()
                DesignSystem.Style.NEUMORPHISM -> DesignSystem.Neumorphism.cardColors()
            }
        )
        
        // Player List
        Text(
            text = "Current Players",
            style = MaterialTheme.typography.titleMedium
        )
        
        players.forEachIndexed { index, player ->
            GenericCard(
                content = { name ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GenericBadge(
                            count = index + 1,
                            showZero = true,
                            content = {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null
                                )
                            }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = name,
                            modifier = Modifier.weight(1f)
                        )
                        GenericIconButton(
                            onClick = { _: String ->
                                // Handle remove player
                            },
                            icon = Icons.Default.Remove,
                            value = "remove_$index",
                            contentDescription = "Remove player"
                        )
                    }
                },
                item = player
            )
        }
    }
}

@Composable
private fun SettingsContent(
    gameVolume: Float,
    onVolumeChange: (Float) -> Unit,
    enableNotifications: Boolean,
    onNotificationsChange: (Boolean) -> Unit,
    onShowDialog: () -> Unit,
    designStyle: DesignSystem.Style
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium
        )
        
        GenericCard(
            content = { _ ->
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Game Settings",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    // Volume Slider
                    Column {
                        Text(
                            text = "Game Volume: ${(gameVolume * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        GenericSlider(
                            value = gameVolume,
                            onValueChange = onVolumeChange,
                            minValue = 0f,
                            maxValue = 1f
                        )
                    }
                    
                    // Notification Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Enable Notifications",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        GenericSwitch(
                            isChecked = enableNotifications,
                            onCheckedChange = onNotificationsChange
                        )
                    }
                    
                    // Advanced Settings Button
                    GenericButton(
                        onClick = { _: String -> onShowDialog() },
                        text = "Advanced Settings",
                        value = "show_advanced"
                    )
                }
            },
            item = Unit,
            colors = when (designStyle) {
                DesignSystem.Style.FLAT_DESIGN -> DesignSystem.FlatDesign.cardColors()
                DesignSystem.Style.NEUMORPHISM -> DesignSystem.Neumorphism.cardColors()
            }
        )
    }
}

// Preview functions
@Preview(showBackground = true, heightDp = 800)
@Composable
fun ComponentIntegrationDemoPreview() {
    ScoreBoardTheme {
        ComponentIntegrationDemo(DesignSystem.Style.FLAT_DESIGN)
    }
}
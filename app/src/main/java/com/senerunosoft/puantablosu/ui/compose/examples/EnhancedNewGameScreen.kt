package com.senerunosoft.puantablosu.ui.compose.examples

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.senerunosoft.puantablosu.model.Player
import com.senerunosoft.puantablosu.ui.compose.theme.ScoreBoardTheme
import com.senerunosoft.puantablosu.ui.compose.components.*
import com.senerunosoft.puantablosu.ui.compose.components.styles.DesignSystem

/**
 * Enhanced New Game Screen using Generic Components
 * Demonstrates the use of the generic component system
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedNewGameScreen(
    onStartGame: (String, List<Player>) -> Unit = { _, _ -> },
    onNavigateBack: () -> Unit = {},
    designStyle: DesignSystem.Style = DesignSystem.Style.FLAT_DESIGN
) {
    var gameTitle by remember { mutableStateOf("") }
    var gameTitleError by remember { mutableStateOf(false) }
    var players by remember { mutableStateOf(listOf<Player>()) }
    var playerErrors by remember { mutableStateOf(mapOf<Int, Boolean>()) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var maxPlayersReached by remember { mutableStateOf(false) }

    // Background color matching original teal_700
    val backgroundColor = Color(0xFF00796B)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Game Title Input using Generic TextField
        GenericTextField(
            value = gameTitle,
            onValueChange = { 
                gameTitle = it
                gameTitleError = it.isEmpty()
            },
            label = "Game Title",
            isError = gameTitleError,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Add Player Button using Generic Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GenericButton(
                onClick = { action: String ->
                    when (action) {
                        "add_player" -> {
                            if (players.size < 6) {
                                players = players + Player("")
                                maxPlayersReached = false
                            } else {
                                maxPlayersReached = true
                                showError = true
                                errorMessage = "Maximum 6 players allowed"
                            }
                        }
                    }
                },
                text = "Add Player",
                value = "add_player",
                modifier = Modifier.weight(1f),
                colors = when (designStyle) {
                    DesignSystem.Style.FLAT_DESIGN -> DesignSystem.FlatDesign.buttonColors()
                    DesignSystem.Style.NEUMORPHISM -> DesignSystem.Neumorphism.buttonColors()
                },
                enabled = players.size < 6
            )

            // Progress indicator for players count
            GenericBadge(
                count = players.size,
                content = {
                    GenericIconButton(
                        onClick = { _: Unit -> /* Show info */ },
                        icon = Icons.Default.Person,
                        value = Unit,
                        contentDescription = "Players count"
                    )
                }
            )
        }

        // Progress indicator for maximum players
        if (players.isNotEmpty()) {
            val progress = players.size / 6f
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Players: ${players.size}/6",
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.width(8.dp))
                GenericProgressIndicator(
                    value = progress,
                    isIndeterminate = false,
                    isCircular = false,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Material Divider
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = Color.White.copy(alpha = 0.7f)
        )

        // Player List using Generic Cards and List Items
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(players) { index, player ->
                EnhancedPlayerItem(
                    player = player,
                    index = index,
                    onPlayerNameChange = { newName ->
                        players = players.toMutableList().apply {
                            this[index] = player.copy(name = newName)
                        }
                        playerErrors = playerErrors.toMutableMap().apply {
                            this[index] = newName.isEmpty()
                        }
                    },
                    onDeletePlayer = {
                        players = players.toMutableList().apply {
                            removeAt(index)
                        }
                        playerErrors = playerErrors.toMutableMap().apply {
                            remove(index)
                            // Reindex the remaining errors
                            val newErrors = mutableMapOf<Int, Boolean>()
                            this.forEach { (oldIndex, error) ->
                                when {
                                    oldIndex < index -> newErrors[oldIndex] = error
                                    oldIndex > index -> newErrors[oldIndex - 1] = error
                                }
                            }
                            clear()
                            putAll(newErrors)
                        }
                    },
                    isError = playerErrors[index] ?: false,
                    designStyle = designStyle
                )
            }
        }

        // Start Game Button using Generic Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            GenericButton(
                onClick = { action: String ->
                    when (action) {
                        "start_game" -> {
                            // Validate inputs
                            val titleEmpty = gameTitle.isEmpty()
                            val emptyPlayerNames = players.mapIndexedNotNull { index, player ->
                                if (player.name.isEmpty()) index else null
                            }

                            gameTitleError = titleEmpty
                            playerErrors = emptyPlayerNames.associateWith { true }

                            if (titleEmpty || emptyPlayerNames.isNotEmpty() || players.isEmpty()) {
                                val errorFields = mutableListOf<String>()
                                if (titleEmpty) errorFields.add("Game Title")
                                if (players.isEmpty()) errorFields.add("At least one player required")
                                if (emptyPlayerNames.isNotEmpty()) {
                                    errorFields.addAll(emptyPlayerNames.map { "Player ${it + 1}" })
                                }
                                showError = true
                                errorMessage = "${errorFields.joinToString(", ")} fields cannot be empty."
                            } else {
                                // All validation passed, start the game
                                onStartGame(gameTitle, players)
                            }
                        }
                    }
                },
                text = "Start Game",
                value = "start_game",
                colors = when (designStyle) {
                    DesignSystem.Style.FLAT_DESIGN -> DesignSystem.FlatDesign.buttonColors()
                    DesignSystem.Style.NEUMORPHISM -> DesignSystem.Neumorphism.buttonColors()
                },
                enabled = players.isNotEmpty() && gameTitle.isNotEmpty()
            )
        }
    }

    // Error Dialog using Generic Dialog
    GenericDialog(
        openDialog = showError,
        onDismissRequest = { showError = false },
        title = "Validation Error",
        content = { message ->
            Text(message)
        },
        item = errorMessage,
        confirmButton = { _ ->
            TextButton(onClick = { showError = false }) {
                Text("OK")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnhancedPlayerItem(
    player: Player,
    index: Int,
    onPlayerNameChange: (String) -> Unit,
    onDeletePlayer: () -> Unit,
    isError: Boolean = false,
    designStyle: DesignSystem.Style = DesignSystem.Style.FLAT_DESIGN
) {
    // Using Generic Card for player item
    GenericCard(
        content = { playerData ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Player number badge
                GenericBadge(
                    count = index + 1,
                    showZero = true,
                    content = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Player name input using Generic TextField
                GenericTextField(
                    value = playerData.name,
                    onValueChange = onPlayerNameChange,
                    label = "Player ${index + 1}",
                    isError = isError,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        errorBorderColor = Color.Red,
                        errorLabelColor = Color.Red
                    )
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Delete button using Generic Icon Button
                GenericIconButton(
                    onClick = { _: String -> onDeletePlayer() },
                    icon = Icons.Default.Delete,
                    value = "delete_player_$index",
                    contentDescription = "Delete player"
                )
            }
        },
        item = player,
        colors = when (designStyle) {
            DesignSystem.Style.FLAT_DESIGN -> DesignSystem.FlatDesign.cardColors(
                containerColor = Color.White.copy(alpha = 0.1f)
            )
            DesignSystem.Style.NEUMORPHISM -> DesignSystem.Neumorphism.cardColors(
                containerColor = Color.White.copy(alpha = 0.1f)
            )
        },
        elevation = when (designStyle) {
            DesignSystem.Style.FLAT_DESIGN -> DesignSystem.FlatDesign.cardElevation()
            DesignSystem.Style.NEUMORPHISM -> DesignSystem.Neumorphism.cardElevation()
        }
    )
}

// Preview functions
@Preview(showBackground = true)
@Composable
fun EnhancedNewGameScreenFlatPreview() {
    ScoreBoardTheme {
        EnhancedNewGameScreen(
            designStyle = DesignSystem.Style.FLAT_DESIGN
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EnhancedNewGameScreenNeumorphismPreview() {
    ScoreBoardTheme {
        EnhancedNewGameScreen(
            designStyle = DesignSystem.Style.NEUMORPHISM
        )
    }
}
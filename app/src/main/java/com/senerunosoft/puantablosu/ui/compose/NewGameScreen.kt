package com.senerunosoft.puantablosu.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.senerunosoft.puantablosu.R
import com.senerunosoft.puantablosu.model.Player
import com.senerunosoft.puantablosu.ui.compose.theme.ScoreBoardTheme

/**
 * New Game Setup Screen Composable
 * Replaces fragment_new_game_setting.xml and NewGameSettingFragment
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewGameScreen(
    onStartGame: (String, List<Player>) -> Unit = { _, _ -> },
    onNavigateBack: () -> Unit = {}
) {
    var gameTitle by remember { mutableStateOf("") }
    var gameTitleError by remember { mutableStateOf(false) }
    var players by remember { mutableStateOf(listOf<Player>()) }
    var playerErrors by remember { mutableStateOf(mapOf<Int, Boolean>()) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Background color matching original teal_700
    val backgroundColor = Color(0xFF00796B)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Game Title Input
        OutlinedTextField(
            value = gameTitle,
            onValueChange = { 
                gameTitle = it
                gameTitleError = it.isEmpty()
            },
            label = { Text(stringResource(R.string.game_title)) },
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

        // Add Player Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = {
                    if (players.size < 6) {
                        players = players + Player("")
                    } else {
                        showError = true
                        errorMessage = "Maksimum oyuncu sayısına ulaştınız."
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.add_player))
            }
        }

        // Material Divider
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 5.dp),
            color = Color.White.copy(alpha = 0.7f)
        )

        // Player List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(players) { index, player ->
                PlayerItem(
                    player = player,
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
                    isError = playerErrors[index] ?: false
                )
            }
        }

        // Start Game Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = {
                    // Validate inputs
                    val titleEmpty = gameTitle.isEmpty()
                    val emptyPlayerNames = players.mapIndexedNotNull { index, player ->
                        if (player.name.isEmpty()) index else null
                    }

                    gameTitleError = titleEmpty
                    playerErrors = emptyPlayerNames.associateWith { true }

                    if (titleEmpty || emptyPlayerNames.isNotEmpty()) {
                        val errorFields = mutableListOf<String>()
                        if (titleEmpty) errorFields.add("Oyun Başlığı")
                        if (emptyPlayerNames.isNotEmpty()) {
                            errorFields.addAll(emptyPlayerNames.map { "Oyuncu ${it + 1}" })
                        }
                        showError = true
                        errorMessage = "${errorFields.joinToString(", ")} alanları boş bırakılamaz."
                    } else {
                        // All validation passed, start the game
                        onStartGame(gameTitle, players)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(stringResource(R.string.start_game))
            }
        }
    }

    // Error Dialog
    if (showError) {
        AlertDialog(
            onDismissRequest = { showError = false },
            title = { Text("Hata") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showError = false }) {
                    Text("Tamam")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerItem(
    player: Player,
    onPlayerNameChange: (String) -> Unit,
    onDeletePlayer: () -> Unit,
    isError: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            OutlinedTextField(
                value = player.name,
                onValueChange = onPlayerNameChange,
                label = { Text(stringResource(R.string.oyuncu)) },
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
            
            IconButton(onClick = onDeletePlayer) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.oyuncu_sil),
                    tint = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NewGameScreenPreview() {
    ScoreBoardTheme {
        NewGameScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun NewGameScreenWithPlayersPreview() {
    ScoreBoardTheme {
        NewGameScreen()
    }
}
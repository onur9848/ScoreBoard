package com.senerunosoft.puantablosu.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.senerunosoft.puantablosu.R
import com.senerunosoft.puantablosu.model.Player
import com.senerunosoft.puantablosu.model.enums.GameType
import com.senerunosoft.puantablosu.model.config.IConfig
import com.senerunosoft.puantablosu.model.config.YuzBirOkeyConfig
import com.senerunosoft.puantablosu.model.config.OkeyConfig
import com.senerunosoft.puantablosu.ui.compose.theme.ScoreBoardTheme

/**
 * New Game Setup Screen Composable
 * Replaces fragment_new_game_setting.xml and NewGameSettingFragment
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewGameScreen(
    gameType: GameType,
    onStartGame: (String, List<Player>, IConfig?) -> Unit = { _, _, _ -> },
    onNavigateBack: () -> Unit = {},
    initialConfig: IConfig? = null
) {
    var gameTitle by remember { mutableStateOf("") }
    var gameTitleError by remember { mutableStateOf(false) }
    var players by remember { mutableStateOf(listOf<Player>()) }
    var playerErrors by remember { mutableStateOf(mapOf<Int, Boolean>()) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Config state (only one, type-safe)
    val yuzBirConfig = remember { (initialConfig as? YuzBirOkeyConfig) ?: YuzBirOkeyConfig() }
    val okeyConfig = remember { (initialConfig as? OkeyConfig) ?: OkeyConfig() }

    // Add state for game mode selection
    var isPaired by remember { mutableStateOf(false) }

    // Helper to get isPaired from correct config
    fun getIsPaired(): Boolean {
        return when (gameType) {
            GameType.YuzBirOkey -> yuzBirConfig.isPartnered
            GameType.Okey -> okeyConfig.isPartnered
            else -> false
        }
    }

    // Helper to update players based on mode
    fun updatePlayersForMode() {
        isPaired = getIsPaired()
        players = if (isPaired) {
            listOf(Player(name = ""), Player(name = ""))
        } else {
            listOf(Player(name = ""), Player(name = ""), Player(name = ""), Player(name = ""))
        }
        // Reset errors
        playerErrors = players.mapIndexed { idx, _ -> idx to false }.toMap()
    }

    // On first load or when config changes, set correct isPaired and update players
    DisposableEffect(gameType, yuzBirConfig.isPartnered, okeyConfig.isPartnered) {
        if (gameType == GameType.YuzBirOkey || gameType == GameType.Okey) {
            updatePlayersForMode()
        }
        onDispose { }
    }

    Surface(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column {

                // Main Card content (title, players)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 120.dp), // leave space for bottom bar
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally)
                            .statusBarsPadding(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.game_title),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
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
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            if (gameTitleError) {
                                Text(
                                    text = "Oyun başlığı boş bırakılamaz.",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.align(Alignment.Start)
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = stringResource(R.string.add_player),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 600.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(players) { index, player ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            OutlinedTextField(
                                                value = player.name,
                                                onValueChange = { newName ->
                                                    players = players.toMutableList().apply {
                                                        this[index] = player.copy(name = newName)
                                                    }
                                                    playerErrors = playerErrors.toMutableMap().apply {
                                                        this[index] = newName.isEmpty()
                                                    }
                                                },
                                                label = { Text("Oyuncu ${index + 1}") },
                                                isError = playerErrors[index] ?: false,
                                                modifier = Modifier.weight(1f),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                                )
                                            )
                                            IconButton(
                                                onClick = {
                                                    players = players.toMutableList().apply { removeAt(index) }
                                                    playerErrors = playerErrors.toMutableMap().apply {
                                                        remove(index)
                                                        // Reindex errors
                                                        val newErrors = mutableMapOf<Int, Boolean>()
                                                        this.forEach { (oldIndex, error) ->
                                                            if (oldIndex < index) newErrors[oldIndex] = error
                                                            if (oldIndex > index) newErrors[oldIndex - 1] = error
                                                        }
                                                        clear()
                                                        putAll(newErrors)
                                                    }
                                                },
                                                enabled = players.size > 1
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = stringResource(R.string.action_delete),
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                        if (playerErrors[index] == true) {
                                            Text(
                                                text = "İsim boş bırakılamaz.",
                                                color = MaterialTheme.colorScheme.error,
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(start = 40.dp, bottom = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            // Oyun tipi ayarları
                            when (gameType) {
                                GameType.YuzBirOkey -> {
                                    // 101 Okey ayarları UI (kısa, örnek)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Eşli/Tekli: ${if (yuzBirConfig.isPartnered) "Eşli" else "Tekli"}")
                                    // ...daha fazla ayar UI eklenebilir...
                                }
                                GameType.Okey -> {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Eşli/Tekli: ${if (okeyConfig.isPartnered) "Eşli" else "Tekli"}")
                                }
                                else -> {}
                            }
                        }
                    }
                }
                // Bottom action bar
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        modifier = Modifier.fillMaxWidth(),
                        enabled = players.size < 6,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.add_player))
                    }
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
                                val config: IConfig? = when (gameType) {
                                    GameType.YuzBirOkey -> yuzBirConfig
                                    GameType.Okey -> okeyConfig
                                    else -> null
                                }
                                onStartGame(gameTitle, players, config)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(stringResource(R.string.start_game))
                    }
                }
            }
        }
    }
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


@Preview(showBackground = true)
@Composable
fun NewGameScreenPreview() {
    ScoreBoardTheme {
        NewGameScreen(GameType.GenelOyun)
    }
}

@Preview(showBackground = true)
@Composable
fun NewGameScreenWithPlayersPreview() {
    ScoreBoardTheme {
        NewGameScreen(GameType.GenelOyun)
    }
}
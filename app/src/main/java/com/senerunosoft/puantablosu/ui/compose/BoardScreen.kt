package com.senerunosoft.puantablosu.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senerunosoft.puantablosu.R
import com.senerunosoft.puantablosu.model.Game
import com.senerunosoft.puantablosu.model.Player
import com.senerunosoft.puantablosu.model.Score
import com.senerunosoft.puantablosu.model.SingleScore
import com.senerunosoft.puantablosu.model.config.RuleConfig
import com.senerunosoft.puantablosu.model.enums.RuleType
import com.senerunosoft.puantablosu.ui.compose.theme.ScoreBoardTheme

/**
 * Board Screen Composable
 * Replaces fragment_board_screen.xml and BoardScreenFragment
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardScreen(
    game: Game,
    onAddScore: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onSaveGame: (Game) -> Unit = {}
) {
    var showBackDialog by remember { mutableStateOf(false) }
    var showScoreDialog by remember { mutableStateOf(false) }
    var calculatedScores by remember { mutableStateOf(listOf<SingleScore>()) }
    var showRuleDialog by remember { mutableStateOf<RuleConfig?>(null) }
    var pairedInputValue by remember { mutableStateOf("") }
    var selectedPlayerId by remember { mutableStateOf<String?>(null) }
    var pairedRuleForInput by remember { mutableStateOf<RuleConfig?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
            ) {
                // Top App Bar
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = game.gameTitle,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    actions = {
                        IconButton(onClick = {
                            // Calculate scores
                            val scores = mutableListOf<SingleScore>()
                            game.playerList.forEach { player ->
                                var totalScore: Int = 0
                                game.score.forEach { roundScore ->
                                    val playerScore: Int = roundScore.scoreMap[player.id] ?: 0
                                    totalScore += playerScore
                                }
                                scores.add(SingleScore(player.id, totalScore))
                            }
                            calculatedScores = scores.sortedByDescending { it.score }
                            showScoreDialog = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = stringResource(R.string.error_incomplete_score),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { showBackDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBackIosNew,
                                contentDescription = stringResource(R.string.close_dialog_description),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .weight(1f),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        // Player header row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(vertical = 8.dp)
                                .clip(MaterialTheme.shapes.large),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.turn),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .width(56.dp)
                                    .padding(horizontal = 4.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            game.playerList.forEachIndexed { index, player ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 2.dp)
                                        .clip(MaterialTheme.shapes.medium)
                                        .background(
                                            if (index % 2 == 0)
                                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f)
                                            else
                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                        )
                                ) {
                                    Text(
                                        text = player.name,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp, horizontal = 4.dp),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                                if (index < game.playerList.size - 1) {
                                    Spacer(modifier = Modifier.width(2.dp))
                                }
                            }
                        }
                        Divider(
                            color = MaterialTheme.colorScheme.outline,
                            thickness = 2.dp,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        // Score rows
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            var roundNumber = 1
                            itemsIndexed(game.score) { _, roundScore ->
                                val isPenalty = roundScore.scoreMap.values.any { it != 0 } && roundScore.scoreMap.values.count { it != 0 } == 1 && roundScore.scoreMap.values.any { it < 0 || it > 0 }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (roundNumber % 2 == 1) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isPenalty) "-" else "#${roundNumber}",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(56.dp),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    game.playerList.forEachIndexed { index, player ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(horizontal = 2.dp)
                                                .clip(MaterialTheme.shapes.small)
                                                .background(
                                                    if (index % 2 == 0)
                                                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                                    else
                                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                                )
                                        ) {
                                            val score = roundScore.scoreMap[player.id] ?: 0
                                            Text(
                                                text = score.toString(),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                        if (index < game.playerList.size - 1) {
                                            Spacer(modifier = Modifier.width(2.dp))
                                        }
                                    }
                                }
                                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                if (!isPenalty) roundNumber++
                            }
                        }
                        // FAB for score adding (above bottom bar)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 80.dp), // leave space for bottom bar
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            FloatingActionButton(
                                onClick = { onAddScore() },
                                containerColor = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Puan Ekle")
                            }
                        }
                    }
                }
            }
            // Bottom Bar for rules
            val config = game.config
            val rules = when (config) {
                is com.senerunosoft.puantablosu.model.config.OkeyConfig -> config.rules
                is com.senerunosoft.puantablosu.model.config.YuzBirOkeyConfig -> config.rules
                else -> emptyList()
            }
            if (rules.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.large
                        )
                        .padding(bottom = 4.dp)
                ) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp) // Default Material3 height
                    ) {
                        rules.forEach { rule ->
                            val showButton = when (rule.types.first()) {
                                RuleType.PlayerPenaltyScore -> true
                                RuleType.FinishScore -> rule.pairedKey != null
                                else -> false
                            }
                            if (showButton) {
                                val icon = when (rule.types.first()) {
                                    RuleType.PlayerPenaltyScore -> Icons.Default.Face
                                    RuleType.FinishScore -> Icons.Default.Calculate
                                    else -> Icons.Default.Edit
                                }
                                NavigationBarItem(
                                    selected = false,
                                    onClick = {
                                        if (rule.types.first() == RuleType.FinishScore && rule.pairedKey != null) {
                                            pairedRuleForInput = rules.find { it.key == rule.pairedKey }
                                            showRuleDialog = rule
                                        } else {
                                            showRuleDialog = rule
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            icon,
                                            contentDescription = rule.label,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            rule.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            softWrap = false,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    },
                                    alwaysShowLabel = true
                                )
                            }
                        }
                    }
                    // Dialog for PlayerPenaltyScore (direct input for player)
                    if (showRuleDialog != null && (showRuleDialog!!.types.first() == RuleType.PlayerPenaltyScore)) {
                        AlertDialog(
                            onDismissRequest = {
                                showRuleDialog = null
                                selectedPlayerId = null
                                pairedInputValue = ""
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    val rule = showRuleDialog!!
                                    if (selectedPlayerId != null) {
                                        game.score.add(
                                            Score(
                                                scoreOrder = game.score.size + 1,
                                                scoreMap = game.playerList.associate { player ->
                                                    player.id to if (player.id == selectedPlayerId) rule.value.toIntOrNull() ?: 0 else 0
                                                }.toMutableMap() as HashMap<String, Int>
                                            )
                                        )
                                    }
                                    showRuleDialog = null
                                    selectedPlayerId = null
                                    pairedInputValue = ""
                                }) { Text("Kaydet") }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    showRuleDialog = null
                                    selectedPlayerId = null
                                    pairedInputValue = ""
                                }) { Text("İptal") }
                            },
                            title = { Text("${showRuleDialog!!.label} - Oyuncu Seç") },
                            text = {
                                Column {
                                    Text("Oyuncu Seçin:")
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        game.playerList.forEach { player ->
                                            FilterChip(
                                                selected = selectedPlayerId == player.id,
                                                onClick = { selectedPlayerId = player.id },
                                                label = { Text(player.name) }
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                    // Dialog for FinishScore with pairedKey (input for paired rule value)
                    if (showRuleDialog != null && showRuleDialog!!.types.first() == RuleType.FinishScore && pairedRuleForInput != null) {
                        // pairedInputValue should be initialized to pairedRuleForInput!!.value only when dialog is first shown
                        val initialPairedValue = pairedRuleForInput!!.value
                        var localPairedInputValue by remember(showRuleDialog, pairedRuleForInput) {
                            mutableStateOf(initialPairedValue)
                        }
                        AlertDialog(
                            onDismissRequest = {
                                showRuleDialog = null
                                pairedRuleForInput = null
                                pairedInputValue = ""
                                selectedPlayerId = null
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    val selectedRule = showRuleDialog!!
                                    val pairedRule = pairedRuleForInput!!
                                    if (selectedPlayerId != null) {
                                        game.score.add(
                                            Score(
                                                scoreOrder = game.score.size + 1,
                                                scoreMap = game.playerList.associate { player ->
                                                    player.id to when (player.id) {
                                                        selectedPlayerId -> selectedRule.value.toIntOrNull() ?: 0
                                                        else -> localPairedInputValue.toIntOrNull() ?: 0
                                                    }
                                                }.toMutableMap() as HashMap<String, Int>
                                            )
                                        )
                                    }
                                    onSaveGame(game)
                                    showRuleDialog = null
                                    pairedRuleForInput = null
                                    pairedInputValue = ""
                                    selectedPlayerId = null
                                }) { Text("Kaydet") }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    showRuleDialog = null
                                    pairedRuleForInput = null
                                    pairedInputValue = ""
                                    selectedPlayerId = null
                                }) { Text("İptal") }
                            },
                            title = { Text("${showRuleDialog!!.label} - Oyuncu Seç ve ${pairedRuleForInput!!.label} Değeri Gir") },
                            text = {
                                Column {
                                    Text("Oyuncu Seçin:")
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        game.playerList.forEach { player ->
                                            FilterChip(
                                                selected = selectedPlayerId == player.id,
                                                onClick = { selectedPlayerId = player.id },
                                                label = { Text(player.name) }
                                            )
                                        }
                                    }
                                    OutlinedTextField(
                                        value = localPairedInputValue,
                                        onValueChange = { localPairedInputValue = it },
                                        label = { Text("${pairedRuleForInput!!.label}") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Back navigation confirmation dialog
    if (showBackDialog) {
        AlertDialog(
            onDismissRequest = { showBackDialog = false },
            title = { Text("Oyundan çıkmak istediğinize emin misiniz?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showBackDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("Evet")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackDialog = false }) {
                    Text("Hayır")
                }
            }
        )
    }

    // Score calculation dialog
    if (showScoreDialog) {
        ScoreCalculationDialog(
            calculatedScores = calculatedScores,
            players = game.playerList,
            onDismiss = { showScoreDialog = false }
        )
    }
}


@Composable
private fun ScoreCalculationDialog(
    calculatedScores: List<SingleScore>,
    players: List<Player>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Skorlar", fontWeight = FontWeight.Bold, fontSize = 22.sp) },
        icon = {
            Icon(
                imageVector = Icons.Default.Face,
                contentDescription = null,
                tint = Color(0XFF333333), // dark gray
                modifier = Modifier.size(28.dp)
            )
        },
        text = {
            Column {
                calculatedScores.forEachIndexed { index, singleScore ->
                    val player = players.find { it.id == singleScore.playerId }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .safeContentPadding(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Kupa ikonu ile ilk 3 oyuncu vurgusu
                        if (index == 0) {
                            Icon(
                                imageVector = Icons.Default.Face,
                                contentDescription = "Birinci",
                                tint = Color(0XFF333333), // dark gray
                                modifier = Modifier.size(28.dp)
                            )
                        } else if (index == 1) {
                            Icon(
                                imageVector = Icons.Default.Face,
                                contentDescription = "İkinci",
                                tint = Color(0xFFC0C0C0), // Gümüş
                                modifier = Modifier.size(24.dp)
                            )
                        } else if (index == 2) {
                            Icon(
                                imageVector = Icons.Default.Face,
                                contentDescription = "Üçüncü",
                                tint = Color(0xFFCD7F32), // Bronz
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Spacer(modifier = Modifier.width(28.dp))
                        }
                        Text(
                            text = player?.name ?: "",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(2f).padding(start = 8.dp)
                        )
                        Text(
                            text = singleScore.score.toString(),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0XFF333333),
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewBoardScreen() {
    val sampleGame = Game(
        gameId = "sample",
        gameTitle = "Test Oyunu",
        playerList = listOf(
            Player("1", "Oyuncu 1"),
            Player("2", "Oyuncu 2"),
            Player("3", "Oyuncu 3")
        ),
        score = mutableListOf(
            Score(
                scoreOrder = 1,
                scoreMap = hashMapOf("1" to 10, "2" to 20, "3" to 30)
            ),
            Score(
                scoreOrder = 2,
                scoreMap = hashMapOf("1" to 15, "2" to 25, "3" to 35)
            )
        )
    )
    ScoreBoardTheme {
        BoardScreen(game = sampleGame,)
    }
}

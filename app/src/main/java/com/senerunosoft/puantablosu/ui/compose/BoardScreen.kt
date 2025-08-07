package com.senerunosoft.puantablosu.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senerunosoft.puantablosu.R
import com.senerunosoft.puantablosu.model.Game
import com.senerunosoft.puantablosu.model.Player
import com.senerunosoft.puantablosu.model.SingleScore
import com.senerunosoft.puantablosu.ui.compose.theme.ScoreBoardTheme
import kotlin.collections.forEach

/**
 * Board Screen Composable
 * Replaces fragment_board_screen.xml and BoardScreenFragment
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardScreen(
    game: Game,
    onAddScore: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    var showBackDialog by remember { mutableStateOf(false) }
    var showScoreDialog by remember { mutableStateOf(false) }
    var calculatedScores by remember { mutableStateOf(listOf<SingleScore>()) }

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
                            itemsIndexed(game.score) { roundIndex, roundScore ->
                                val isEven = roundIndex % 2 == 0
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (isEven) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "#${roundIndex + 1}",
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
                            }
                        }
                    }
                }
            }
            // Floating Action Button
            FloatingActionButton(
                onClick = onAddScore,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_score),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
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
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Skorlar",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                calculatedScores.forEachIndexed { index, singleScore ->
                    val playerName = players.first { it.id == singleScore.playerId }.name
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = when (index) {
                            0 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            1 -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f)
                            2 -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.10f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        tonalElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Kupa ikonu ile ilk 3 oyuncu vurgusu
                            if (index == 0) {
                                Icon(
                                    imageVector = Icons.Default.Face,
                                    contentDescription = "Birinci",
                                    tint = Color(0XFF333333),
                                    modifier = Modifier.size(28.dp)
                                )
                            } else if (index == 1) {
                                Icon(
                                    imageVector = Icons.Default.Face,
                                    contentDescription = "İkinci",
                                    tint = Color(0xFFC0C0C0),
                                    modifier = Modifier.size(24.dp)
                                )
                            } else if (index == 2) {
                                Icon(
                                    imageVector = Icons.Default.Face,
                                    contentDescription = "Üçüncü",
                                    tint = Color(0xFFCD7F32),
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Spacer(modifier = Modifier.width(28.dp))
                            }
                            Text(
                                text = playerName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(2f).padding(start = 8.dp)
                            )
                            Text(
                                text = singleScore.score.toString(),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    if (singleScore != calculatedScores.last()) {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    "Tamam",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun BoardScreenPreview() {
    ScoreBoardTheme {
        val samplePlayers = listOf(
            Player("1", "Oyuncu 1"),
            Player("2", "Oyuncu 2"),
            Player("3", "Oyuncu 3")
        )
        val sampleGame = Game(
            gameId = "sample",
            gameTitle = "Test Oyunu",
            playerList = samplePlayers,
            score = mutableListOf()
        )
        BoardScreen(game = sampleGame)
    }
}
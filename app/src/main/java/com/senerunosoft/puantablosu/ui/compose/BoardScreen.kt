package com.senerunosoft.puantablosu.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    // Background color matching original teal_700
    val backgroundColor = Color(0xFF00796B)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with game title and calculate button
            GameHeader(
                gameTitle = game.gameTitle,
                onCalculateClick = {
                    // Calculate scores (this would normally use the GameService)
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
                }
            )

            HorizontalDivider(
                thickness = 3.dp,
                color = Color.Black
            )

            // Player list header
            PlayerListHeader(
                players = game.playerList
            )

            HorizontalDivider(
                thickness = 3.dp,
                color = Color.Black
            )

            // Score board
            ScoreBoard(
                game = game,
                modifier = Modifier.weight(1f)
            )
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onAddScore,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.add_score)
            )
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
private fun GameHeader(
    gameTitle: String,
    onCalculateClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = gameTitle.uppercase(),
            color = Color.White,
            fontSize = 30.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(6f)
        )
        
        IconButton(
            onClick = onCalculateClick,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.skor_hesapla),
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun PlayerListHeader(
    players: List<Player>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Turn number header
        Text(
            text = stringResource(R.string.turn),
            color = Color.White,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(40.dp)
        )
        
        VerticalDivider(
            thickness = 1.dp,
            color = Color.Black,
            modifier = Modifier.fillMaxHeight()
        )
        
        // Player name headers
        players.forEachIndexed { index, player ->
            Text(
                text = player.name,
                color = Color.White,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            
            if (index < players.size - 1) {
                VerticalDivider(
                    thickness = 1.dp,
                    color = Color.Black,
                    modifier = Modifier.fillMaxHeight()
                )
            }
        }
    }
}

@Composable
private fun ScoreBoard(
    game: Game,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
    ) {
        itemsIndexed(game.score) { roundIndex, roundScore ->
            ScoreRow(
                round = roundIndex + 1,
                players = game.playerList,
                roundScore = roundScore.scoreMap
            )
        }
    }
}

@Composable
private fun ScoreRow(
    round: Int,
    players: List<Player>,
    roundScore: Map<String, Int>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Round number
        Text(
            text = "#$round",
            color = Color.White,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(40.dp)
        )
        
        VerticalDivider(
            thickness = 1.dp,
            color = Color.Black,
            modifier = Modifier.fillMaxHeight()
        )
        
        // Player scores
        players.forEachIndexed { index, player ->
            val score = roundScore[player.id] ?: 0
            Text(
                text = score.toString(),
                color = Color.White,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            
            if (index < players.size - 1) {
                VerticalDivider(
                    thickness = 1.dp,
                    color = Color.Black,
                    modifier = Modifier.fillMaxHeight()
                )
            }
        }
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
        title = { Text("Skorlar") },
        text = {
            Column {
                calculatedScores.forEach { singleScore ->
                    val playerName = players.first { it.id == singleScore.playerId }.name
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = playerName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(2f)
                        )
                        Text(
                            text = singleScore.score.toString(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.End,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (singleScore != calculatedScores.last()) {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = Color.Black.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tamam")
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
            score = listOf()
        )
        BoardScreen(game = sampleGame)
    }
}
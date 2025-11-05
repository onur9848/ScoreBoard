package com.senerunosoft.puantablosu.ui.compose.board

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.senerunosoft.puantablosu.model.Game
import com.senerunosoft.puantablosu.model.Player
import com.senerunosoft.puantablosu.model.Score
import com.senerunosoft.puantablosu.model.config.OkeyConfig
import com.senerunosoft.puantablosu.model.config.YuzBirOkeyConfig
import com.senerunosoft.puantablosu.ui.compose.theme.ScoreBoardTheme
import com.senerunosoft.puantablosu.ui.state.BoardUiEvent
import com.senerunosoft.puantablosu.viewmodel.GameViewModel

/**
 * Refactored Board Screen following Modern Android Architecture
 * 
 * Key improvements:
 * - No business logic in composable
 * - Uses UI State from ViewModel
 * - Event-driven architecture
 * - Decomposed into reusable components
 * - Max ~40 lines per function
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardScreenRefactored(
    viewModel: GameViewModel,
    onAddScore: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onNavigateToScoreBoard: () -> Unit = {}
) {
    val uiState by viewModel.boardUiState.collectAsState()
    val game = uiState.game ?: return
    
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
                BoardTopBar(
                    gameTitle = game.gameTitle,
                    onBackClick = { viewModel.onBoardEvent(BoardUiEvent.NavigateBack) },
                    onCalculateClick = { 
                        viewModel.onBoardEvent(BoardUiEvent.CalculateScores)
                        onNavigateToScoreBoard()
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ScoreTable(
                    game = game,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .weight(1f)
                )
            }
            
            // Rule bottom bar
            val config = game.config
            val rules = when (config) {
                is OkeyConfig -> config.rules
                is YuzBirOkeyConfig -> config.rules
                else -> emptyList()
            }
            
            if (rules.isNotEmpty()) {
                RuleBottomBar(
                    rules = rules,
                    gameType = game.gameType,
                    onRuleClick = { rule, pairedRule ->
                        viewModel.onBoardEvent(BoardUiEvent.ShowRuleDialog(rule, pairedRule))
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
    
    // Dialogs
    uiState.showRuleDialog?.let { rule ->
        RuleDialog(
            rule = rule,
            pairedRule = uiState.pairedRuleForInput,
            players = game.playerList,
            selectedPlayerId = uiState.selectedPlayerId,
            pairedInputValue = uiState.pairedInputValue,
            onPlayerSelected = { playerId ->
                viewModel.onBoardEvent(BoardUiEvent.SelectPlayer(playerId))
            },
            onPairedValueChanged = { value ->
                viewModel.onBoardEvent(BoardUiEvent.UpdatePairedInputValue(value))
            },
            onConfirm = {
                viewModel.onBoardEvent(
                    BoardUiEvent.SaveRuleScore(
                        rule = rule,
                        pairedRule = uiState.pairedRuleForInput,
                        selectedPlayerId = uiState.selectedPlayerId,
                        pairedInputValue = uiState.pairedInputValue
                    )
                )
            },
            onDismiss = {
                viewModel.onBoardEvent(BoardUiEvent.DismissRuleDialog())
            }
        )
    }
    
    if (uiState.showBackDialog) {
        ConfirmBackDialog(
            onConfirm = {
                viewModel.onBoardEvent(BoardUiEvent.ConfirmBack)
                onNavigateBack()
            },
            onDismiss = {
                viewModel.onBoardEvent(BoardUiEvent.DismissBackDialog)
            }
        )
    }
}

/**
 * Score table component with player header and score rows
 */
@Composable
private fun ScoreTable(
    game: Game,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            PlayerHeaderRow(players = game.playerList)
            
            Divider(
                color = MaterialTheme.colorScheme.outline,
                thickness = 2.dp,
                modifier = Modifier.padding(bottom = 2.dp)
            )
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                var roundNumber = 1
                itemsIndexed(game.score) { _, roundScore ->
                    val isPenalty = isPenaltyRound(roundScore)
                    
                    ScoreRow(
                        roundScore = roundScore,
                        players = game.playerList,
                        roundNumber = roundNumber,
                        isPenalty = isPenalty
                    )
                    
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    
                    if (!isPenalty) roundNumber++
                }
            }
        }
    }
}

/**
 * Determines if a score round is a penalty
 */
private fun isPenaltyRound(roundScore: Score): Boolean {
    val values = roundScore.scoreMap.values
    return values.any { it != 0 } && 
           values.count { it != 0 } == 1 && 
           values.any { it < 0 || it > 0 }
}

/**
 * Confirmation dialog for back navigation
 */
@Composable
private fun ConfirmBackDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Oyundan çıkmak istediğinize emin misiniz?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Evet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hayır")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewBoardScreenRefactored() {
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
        // Preview requires mock ViewModel
        // BoardScreenRefactored(viewModel = mockViewModel)
    }
}

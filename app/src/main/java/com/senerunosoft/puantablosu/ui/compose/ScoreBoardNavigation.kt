package com.senerunosoft.puantablosu.ui.compose

import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.senerunosoft.puantablosu.IGameService
import com.senerunosoft.puantablosu.model.Game
import com.senerunosoft.puantablosu.model.Player
import com.senerunosoft.puantablosu.service.GameService
import com.senerunosoft.puantablosu.ui.compose.theme.ScoreBoardTheme
import com.senerunosoft.puantablosu.viewmodel.GameViewModel

/**
 * Main Compose Navigation
 * Handles navigation between different screens in Compose
 */
@Composable
fun ScoreBoardNavigation(
    navController: NavHostController = rememberNavController(),
    gameViewModel: GameViewModel = viewModel()
) {
    val gameService: IGameService = GameService()
    
    ScoreBoardTheme {
        NavHost(
            navController = navController,
            startDestination = "home"
        ) {
            composable("home") {
                HomeScreen(
                    onNewGameClick = {
                        navController.navigate("new_game")
                    },
                    onOldGameClick = {
                        // Navigate to latest games - this would be implemented later
                        // For now, we'll just navigate to new game
                        navController.navigate("new_game")
                    }
                )
            }
            
            composable("new_game") {
                NewGameScreen(
                    onStartGame = { gameTitle, players ->
                        // Create the game using the service
                        val game = gameService.createGame(gameTitle)
                        players.forEach { player ->
                            gameService.addPlayer(game, player.name)
                        }
                        
                        // Update the ViewModel
                        gameViewModel.setGameInfo(game)
                        
                        // Navigate to board screen
                        navController.navigate("board") {
                            // Clear the back stack to prevent going back to new game
                            popUpTo("home") { inclusive = false }
                        }
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable("board") {
                val gameInfo by gameViewModel.gameInfo.observeAsState()
                
                gameInfo?.let { game ->
                    var showAddScoreDialog by remember { mutableStateOf(false) }
                    
                    BoardScreen(
                        game = game,
                        onAddScore = {
                            showAddScoreDialog = true
                        },
                        onCalculateScore = {
                            // This is handled within BoardScreen itself
                        },
                        onNavigateBack = {
                            navController.popBackStack("home", inclusive = false)
                        }
                    )
                    
                    // Add Score Dialog
                    if (showAddScoreDialog) {
                        AddScoreDialog(
                            players = game.playerList,
                            onSaveScore = { singleScoreList ->
                                // Add scores to the game
                                val success = gameService.addScore(game, singleScoreList)
                                if (success) {
                                    // Update the ViewModel with the modified game
                                    gameViewModel.setGameInfo(game)
                                    
                                    // TODO: Save to SharedPreferences
                                    // This would be done in a real implementation
                                }
                                showAddScoreDialog = false
                            },
                            onDismiss = {
                                showAddScoreDialog = false
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Convenience composable for testing individual screens
 */
@Composable
fun ScoreBoardApp() {
    ScoreBoardNavigation()
}
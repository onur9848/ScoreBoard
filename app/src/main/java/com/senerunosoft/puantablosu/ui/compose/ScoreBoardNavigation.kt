package com.senerunosoft.puantablosu.ui.compose

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.senerunosoft.puantablosu.IGameService
import com.senerunosoft.puantablosu.model.Game
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
    gameViewModel: GameViewModel? = null
) {
    val viewModel = gameViewModel ?: viewModel<GameViewModel>()
    val gameService: IGameService = GameService()
    val context = LocalContext.current
    
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
                    navController.navigate("latest_games")
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
                    viewModel.setGameInfo(game)
                    
                    // Save game to SharedPreferences
                    saveGameToPreferences(context, game, gameService)
                    
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
        
        composable("latest_games") {
            val games = remember { mutableStateOf<List<Game>>(emptyList()) }
            
            // Load games from SharedPreferences
            LaunchedEffect(Unit) {
                games.value = loadGamesFromPreferences(context, gameService)
            }
            
            LatestGamesScreen(
                games = games.value,
                onGameSelected = { selectedGame ->
                    // Load the selected game and navigate to board
                    viewModel.setGameInfo(selectedGame)
                    navController.navigate("board") {
                        popUpTo("home") { inclusive = false }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("board") {
            val gameInfo by viewModel.gameInfo.observeAsState()
            
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
                                viewModel.setGameInfo(game)
                                
                                // Save updated game to SharedPreferences
                                saveGameToPreferences(context, game, gameService)
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

/**
 * Helper function to save game to SharedPreferences
 */
private fun saveGameToPreferences(context: Context, game: Game, gameService: IGameService) {
    try {
        val sharedPreferences = context.getSharedPreferences("game", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        
        // Update game IDs list
        val gameIds = sharedPreferences.getString("gameIds", "") ?: ""
        val updatedGameIds = if (gameIds.isNotEmpty()) {
            if (!gameIds.contains(game.gameId)) {
                "$gameIds,${game.gameId}"
            } else {
                gameIds
            }
        } else {
            game.gameId
        }
        editor.putString("gameIds", updatedGameIds)
        
        // Save game data
        val serializedGameData = gameService.serializeGame(game)
        editor.putString(game.gameId, serializedGameData)
        editor.apply()
    } catch (e: Exception) {
        // Log error or handle gracefully
        e.printStackTrace()
    }
}

/**
 * Helper function to load games from SharedPreferences
 */
private fun loadGamesFromPreferences(context: Context, gameService: IGameService): List<Game> {
    return try {
        val sharedPreferences = context.getSharedPreferences("game", Context.MODE_PRIVATE)
        val gameIds = sharedPreferences.getString("gameIds", "") ?: ""
        
        if (gameIds.isEmpty()) {
            emptyList()
        } else {
            gameIds.split(",").mapNotNull { gameId ->
                val gameData = sharedPreferences.getString(gameId.trim(), null)
                gameData?.let { data ->
                    try {
                        gameService.deserializeGame(data)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

/**
 * Convenience composable for testing individual screens
 */
@Composable
fun ScoreBoardApp() {
    ScoreBoardTheme {
        ScoreBoardNavigation()
    }
}
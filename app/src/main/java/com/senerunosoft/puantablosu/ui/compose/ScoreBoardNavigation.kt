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
    val gameInfo by viewModel.getGameInfo().observeAsState()

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
                    val game = gameService.createGame(gameTitle)
                    players.forEach { player ->
                        gameService.addPlayer(game, player.name)
                    }
                    viewModel.setGameInfo(game)
                    saveGameToPreferences(context, game, gameService)
                    navController.navigate("board") {
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
            LaunchedEffect(Unit) {
                games.value = loadGamesFromPreferences(context, gameService)
            }
            LatestGamesScreen(
                games = games.value,
                onGameSelected = { selectedGame ->
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
            val currentGame = gameInfo
            if (currentGame != null) {
                var showAddScoreDialog by remember { mutableStateOf(false) }
                BoardScreen(
                    game = currentGame as Game,
                    onAddScore = {
                        showAddScoreDialog = true
                    },
                    onNavigateBack = {
                        navController.popBackStack("home", inclusive = false)
                    }
                )
                if (showAddScoreDialog) {
                    AddScoreDialog(
                        players = currentGame.playerList,
                        onSaveScore = { singleScoreList ->
                            val success = gameService.addScore(currentGame, singleScoreList)
                            if (success) {
                                viewModel.setGameInfo(currentGame)
                                saveGameToPreferences(context, currentGame, gameService)
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
private fun saveGameToPreferences(context: Context, game: Game?, gameService: IGameService) {
    try {
        val sharedPreferences = context.getSharedPreferences("game", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        // Update game IDs list
        val gameIds = sharedPreferences.getString("gameIds", "") ?: ""
        val updatedGameIds = if (gameIds.isNotEmpty()) {
            game?.gameId?.let {
                if (!gameIds.contains(it)) {
                    "$gameIds,${game.gameId}"
                } else {
                    gameIds
                }
            }
        } else {
            game?.gameId
        }
        editor.putString("gameIds", updatedGameIds)
        // Save game data
        val serializedGameData = gameService.serializeGame(game)
        editor.putString(game?.gameId, serializedGameData)
        editor.apply()
    } catch (e: Exception) {
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
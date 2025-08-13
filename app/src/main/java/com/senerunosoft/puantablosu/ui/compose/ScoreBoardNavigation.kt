package com.senerunosoft.puantablosu.ui.compose

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.senerunosoft.puantablosu.IGameService
import com.senerunosoft.puantablosu.model.Game
import com.senerunosoft.puantablosu.model.config.RuleConfig
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
    val gameInfo by viewModel.gameInfo.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onNewGameClick = {
                    navController.navigate("select_game_type")
                },
                onOldGameClick = {
                    navController.navigate("latest_games")
                }
            )
        }
        
        composable("select_game_type") {
            GameTypeSelectScreen(
                onGameTypeSelected = { selectedType, selectedConfig ->
                    viewModel.setSelectedConfig(selectedConfig)
                    viewModel.setSelectedGameType(selectedType)
                    navController.navigate("new_game")
                }
            )
        }
        composable("new_game") {
            val selectedConfig by viewModel.selectedConfig.collectAsState()
            val selectedGameType by viewModel.selectedGameType.collectAsState()
            NewGameScreen(
                gameType = selectedGameType ?: com.senerunosoft.puantablosu.model.enums.GameType.Okey,
                initialConfig = selectedConfig,
                onStartGame = { gameTitle, players, config ->
                    val game = gameService.createGame(gameTitle, selectedGameType ?: com.senerunosoft.puantablosu.model.enums.GameType.Okey, config)
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
            val selectedGameTypeFilter = remember { mutableStateOf<com.senerunosoft.puantablosu.model.enums.GameType?>(null) }
            LaunchedEffect(Unit) {
                games.value = loadGamesFromPreferences(context, gameService)
            }
            LatestGamesScreen(
                games = games.value,
                gameTypeFilter = selectedGameTypeFilter.value,
                onGameTypeFilterChanged = { selectedGameTypeFilter.value = it },
                onGameSelected = { selectedGame ->
                    viewModel.setGameInfo(selectedGame)
                    navController.navigate("board") {
                        popUpTo("home") { inclusive = false }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onGameDelete = { gameToDelete ->
                    games.value = games.value.filterNot { it.gameId == gameToDelete.gameId }
                    removeGameFromPreferences(context, gameToDelete.gameId)
                }
            )
        }
        
        composable("board") {
            val currentGame = gameInfo
            if (currentGame != null) {
                var showAddScoreDialog by remember { mutableStateOf(false) }
                var selectedRuleForDialog by remember { mutableStateOf<RuleConfig?>(null) }
                var pairedRuleForDialog by remember { mutableStateOf<RuleConfig?>(null) }
                BoardScreen(
                    game = currentGame,
                    onAddScore = { showAddScoreDialog = true },
                    onNavigateBack = {
                        navController.popBackStack("home", inclusive = false)
                    },
                    onSaveGame = {
                        saveGameToPreferences(context, currentGame, gameService)
                    },
                    onScoreBoardClick = {
                        navController.navigate("score_board_screen")
                    },
                )
                if (showAddScoreDialog) {
                    AddScoreDialog(
                        players = currentGame.playerList,
                        gameType = currentGame.gameType,
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
                            selectedRuleForDialog = null
                            pairedRuleForDialog = null
                        }
                    )
                }
            }
        }

        composable("score_board_screen") {
            ScoreBoardScreen(
                game = gameInfo!!
            )
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

private fun removeGameFromPreferences(context: Context, gameId: String) {
    try {
        val sharedPreferences = context.getSharedPreferences("game", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gameIds = sharedPreferences.getString("gameIds", "") ?: ""
        val updatedGameIds = gameIds.split(",").filter { it.trim() != gameId }.joinToString(",")
        editor.putString("gameIds", updatedGameIds)
        editor.remove(gameId)
        editor.apply()
    } catch (e: Exception) {
        e.printStackTrace()
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
package com.senerunosoft.puantablosu.navigation

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.senerunosoft.puantablosu.IGameService
import com.senerunosoft.puantablosu.model.Game
import com.senerunosoft.puantablosu.model.config.RuleConfig
import com.senerunosoft.puantablosu.service.GameService
import com.senerunosoft.puantablosu.ui.compose.*
import com.senerunosoft.puantablosu.viewmodel.GameViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Refactored Navigation Graph
 * Centralized navigation with NavigationHandler
 * Cleaner separation of concerns
 */
@Composable
fun NavigationGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: NavigationDestination = NavigationDestination.Home
) {
    val viewModel: GameViewModel = koinViewModel()
    val navigationHandler = remember(navController) { NavigationHandler(navController) }
    val gameService: IGameService = remember { GameService() }
    val context = LocalContext.current
    val gameInfo by viewModel.gameInfo.collectAsState()
    
    NavHost(
        navController = navController,
        startDestination = startDestination.route
    ) {
        composable(NavigationDestination.Home.route) {
            HomeScreen(
                onNewGameClick = {
                    navigationHandler.navigateTo(NavigationDestination.SelectGameType)
                },
                onOldGameClick = {
                    navigationHandler.navigateTo(NavigationDestination.LatestGames)
                }
            )
        }
        
        composable(NavigationDestination.SelectGameType.route) {
            GameTypeSelectScreen(
                onGameTypeSelected = { selectedType, selectedConfig ->
                    viewModel.setSelectedConfig(selectedConfig)
                    viewModel.setSelectedGameType(selectedType)
                    navigationHandler.navigateTo(NavigationDestination.NewGame)
                }
            )
        }
        
        composable(NavigationDestination.NewGame.route) {
            val selectedConfig by viewModel.selectedConfig.collectAsState()
            val selectedGameType by viewModel.selectedGameType.collectAsState()
            NewGameScreen(
                gameType = selectedGameType ?: com.senerunosoft.puantablosu.model.enums.GameType.Okey,
                initialConfig = selectedConfig,
                onStartGame = { gameTitle, players, config ->
                    val game = gameService.createGame(
                        selectedGameType ?: com.senerunosoft.puantablosu.model.enums.GameType.Okey,
                        config
                    )
                    players.forEach { player ->
                        gameService.addPlayer(game, player.name)
                    }
                    viewModel.setGameInfo(game)
                    saveGameToPreferences(context, game, gameService)
                    navigationHandler.navigateToWithPopUp(
                        NavigationDestination.Board,
                        NavigationDestination.Home,
                        inclusive = false
                    )
                },
                onNavigateBack = {
                    navigationHandler.navigateBack()
                }
            )
        }
        
        composable(NavigationDestination.LatestGames.route) {
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
                    navigationHandler.navigateToWithPopUp(
                        NavigationDestination.Board,
                        NavigationDestination.Home,
                        inclusive = false
                    )
                },
                onNavigateBack = {
                    navigationHandler.navigateBack()
                },
                onGameDelete = { gameToDelete ->
                    games.value = games.value.filterNot { it.gameId == gameToDelete.gameId }
                    removeGameFromPreferences(context, gameToDelete.gameId)
                }
            )
        }
        
        composable(NavigationDestination.Board.route) {
            val currentGame = gameInfo
            if (currentGame != null) {
                var showAddScoreDialog by remember { mutableStateOf(false) }
                BoardScreen(
                    game = currentGame,
                    onAddScore = { showAddScoreDialog = true },
                    onNavigateBack = {
                        navigationHandler.navigateBackTo(
                            NavigationDestination.Home,
                            inclusive = false
                        )
                    },
                    onSaveGame = {
                        saveGameToPreferences(context, currentGame, gameService)
                    },
                    onScoreBoardClick = {
                        navigationHandler.navigateTo(NavigationDestination.ScoreBoard)
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
                        }
                    )
                }
            }
        }

        composable(NavigationDestination.ScoreBoard.route) {
            ScoreBoardScreen(
                game = gameInfo!!
            )
        }
    }
}

// Helper functions (to be moved to repository layer eventually)
private fun saveGameToPreferences(context: Context, game: Game?, gameService: IGameService) {
    try {
        val sharedPreferences = context.getSharedPreferences("game", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
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
        val serializedGameData = gameService.serializeGame(game)
        editor.putString(game?.gameId, serializedGameData)
        editor.apply()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

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

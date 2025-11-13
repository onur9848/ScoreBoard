package com.senerunosoft.puantablosu.data.repository

import com.senerunosoft.puantablosu.data.source.GameDataSource
import com.senerunosoft.puantablosu.model.Game
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * Implementation of GamesRepository
 * Manages game data using GameDataSource abstraction
 */
class GamesRepositoryImpl(
    private val dataSource: GameDataSource
) : GamesRepository {
    
    private val _allGames = MutableStateFlow<List<Game>>(emptyList())
    private val _currentGame = MutableStateFlow<Game?>(null)
    
    override suspend fun saveGame(game: Game): Result<Unit> {
        return dataSource.saveGame(game).onSuccess {
            // Update in-memory cache
            refreshAllGames()
        }
    }
    
    override suspend fun loadGame(gameId: String): Result<Game?> {
        return dataSource.loadGame(gameId)
    }
    
    override fun getAllGames(): Flow<List<Game>> {
        return _allGames.asStateFlow()
    }
    
    override suspend fun deleteGame(gameId: String): Result<Unit> {
        return dataSource.deleteGame(gameId).onSuccess {
            // Update in-memory cache
            refreshAllGames()
            
            // Clear current game if it was deleted
            if (_currentGame.value?.gameId == gameId) {
                _currentGame.value = null
                dataSource.setCurrentGameId(null)
            }
        }
    }
    
    override suspend fun updateGame(game: Game): Result<Unit> {
        return saveGame(game).onSuccess {
            // Update current game if it matches
            if (_currentGame.value?.gameId == game.gameId) {
                _currentGame.value = game
            }
        }
    }
    
    override fun getCurrentGame(): Flow<Game?> {
        return _currentGame.asStateFlow()
    }
    
    override suspend fun setCurrentGame(game: Game?): Result<Unit> {
        _currentGame.value = game
        return dataSource.setCurrentGameId(game?.gameId)
    }
    
    /**
     * Refresh the in-memory list of all games
     */
    suspend fun refreshAllGames() {
        dataSource.getAllGameIds().onSuccess { gameIds ->
            val games = gameIds.mapNotNull { id ->
                dataSource.loadGame(id).getOrNull()
            }
            _allGames.value = games
        }
    }
    
    /**
     * Initialize repository by loading current game and all games
     */
    suspend fun initialize() {
        // Load all games
        refreshAllGames()
        
        // Load current game
        dataSource.getCurrentGameId().onSuccess { currentGameId ->
            currentGameId?.let { id ->
                dataSource.loadGame(id).onSuccess { game ->
                    _currentGame.value = game
                }
            }
        }
    }
}

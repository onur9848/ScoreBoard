package com.senerunosoft.puantablosu.data.source

import android.content.Context
import android.content.SharedPreferences
import com.senerunosoft.puantablosu.IGameService
import com.senerunosoft.puantablosu.model.Game
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * SharedPreferences implementation of GameDataSource
 * Handles persistence using Android SharedPreferences
 */
class SharedPreferencesDataSource(
    private val context: Context,
    private val gameService: IGameService
) : GameDataSource {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    override suspend fun saveGame(game: Game): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val editor = sharedPreferences.edit()
            
            // Update game IDs list
            val gameIds = sharedPreferences.getString(KEY_GAME_IDS, "") ?: ""
            val updatedGameIds = if (gameIds.isNotEmpty()) {
                if (!gameIds.contains(game.gameId)) {
                    "$gameIds,${game.gameId}"
                } else {
                    gameIds
                }
            } else {
                game.gameId
            }
            editor.putString(KEY_GAME_IDS, updatedGameIds)
            
            // Save game data
            val serializedGameData = gameService.serializeGame(game)
            editor.putString(game.gameId, serializedGameData)
            editor.apply()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun loadGame(gameId: String): Result<Game?> = withContext(Dispatchers.IO) {
        try {
            val gameData = sharedPreferences.getString(gameId, null)
            val game = gameData?.let { gameService.deserializeGame(it) }
            Result.success(game)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getAllGameIds(): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val gameIds = sharedPreferences.getString(KEY_GAME_IDS, "") ?: ""
            val ids = if (gameIds.isEmpty()) {
                emptyList()
            } else {
                gameIds.split(",").map { it.trim() }
            }
            Result.success(ids)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteGame(gameId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val editor = sharedPreferences.edit()
            val gameIds = sharedPreferences.getString(KEY_GAME_IDS, "") ?: ""
            val updatedGameIds = gameIds.split(",")
                .filter { it.trim() != gameId }
                .joinToString(",")
            editor.putString(KEY_GAME_IDS, updatedGameIds)
            editor.remove(gameId)
            editor.apply()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getCurrentGameId(): Result<String?> = withContext(Dispatchers.IO) {
        try {
            val currentGameId = sharedPreferences.getString(KEY_CURRENT_GAME, null)
            Result.success(currentGameId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun setCurrentGameId(gameId: String?): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val editor = sharedPreferences.edit()
            if (gameId != null) {
                editor.putString(KEY_CURRENT_GAME, gameId)
            } else {
                editor.remove(KEY_CURRENT_GAME)
            }
            editor.apply()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    companion object {
        private const val PREFS_NAME = "game"
        private const val KEY_GAME_IDS = "gameIds"
        private const val KEY_CURRENT_GAME = "currentGame"
    }
}

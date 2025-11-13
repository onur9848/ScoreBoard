package com.senerunosoft.puantablosu.data.source

import com.senerunosoft.puantablosu.model.Game

/**
 * Data source interface for game persistence
 * Abstracts the underlying storage mechanism (SharedPreferences, DataStore, etc.)
 */
interface GameDataSource {
    /**
     * Save a game
     */
    suspend fun saveGame(game: Game): Result<Unit>
    
    /**
     * Load a game by ID
     */
    suspend fun loadGame(gameId: String): Result<Game?>
    
    /**
     * Get all game IDs
     */
    suspend fun getAllGameIds(): Result<List<String>>
    
    /**
     * Delete a game
     */
    suspend fun deleteGame(gameId: String): Result<Unit>
    
    /**
     * Get current game ID
     */
    suspend fun getCurrentGameId(): Result<String?>
    
    /**
     * Set current game ID
     */
    suspend fun setCurrentGameId(gameId: String?): Result<Unit>
}

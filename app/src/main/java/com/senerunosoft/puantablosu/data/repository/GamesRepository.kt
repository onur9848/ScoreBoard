package com.senerunosoft.puantablosu.data.repository

import com.senerunosoft.puantablosu.model.Game
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for game data operations
 * Abstracts data sources and provides clean API for ViewModels
 * Follows Repository Pattern and Dependency Inversion Principle
 */
interface GamesRepository {
    /**
     * Save a game to persistent storage
     */
    suspend fun saveGame(game: Game): Result<Unit>
    
    /**
     * Load a game by its ID
     */
    suspend fun loadGame(gameId: String): Result<Game?>
    
    /**
     * Get all saved games
     */
    fun getAllGames(): Flow<List<Game>>
    
    /**
     * Delete a game by its ID
     */
    suspend fun deleteGame(gameId: String): Result<Unit>
    
    /**
     * Update an existing game
     */
    suspend fun updateGame(game: Game): Result<Unit>
    
    /**
     * Get the current active game
     */
    fun getCurrentGame(): Flow<Game?>
    
    /**
     * Set the current active game
     */
    suspend fun setCurrentGame(game: Game?): Result<Unit>
}

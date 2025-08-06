package com.senerunosoft.puantablosu.service.interfaces

import com.senerunosoft.puantablosu.model.Game

/**
 * Interface for game lifecycle management operations.
 * Follows Interface Segregation Principle (ISP) - focused on game operations only.
 */
interface IGameManager {
    /**
     * Creates a new game with the given title.
     * @param gameTitle The title of the game to create
     * @return The created game or null if creation fails
     */
    fun createGame(gameTitle: String): Game?
    
    /**
     * Validates if a game is in a valid state.
     * @param game The game to validate
     * @return true if game is valid, false otherwise
     */
    fun validateGame(game: Game?): Boolean
}
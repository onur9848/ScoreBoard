package com.senerunosoft.puantablosu.service.interfaces

import com.senerunosoft.puantablosu.model.Game
import com.senerunosoft.puantablosu.model.Player

/**
 * Interface for player management operations.
 * Follows Interface Segregation Principle (ISP) - focused on player operations only.
 */
interface IPlayerManager {
    /**
     * Adds a player to the game.
     * @param game The game to add the player to
     * @param playerName The name of the player to add
     * @return true if player was added successfully, false otherwise
     */
    fun addPlayer(game: Game?, playerName: String): Boolean
    
    /**
     * Removes a player from the game.
     * @param game The game to remove the player from
     * @param playerId The ID of the player to remove
     * @return true if player was removed successfully, false otherwise
     */
    fun removePlayer(game: Game?, playerId: String): Boolean
    
    /**
     * Validates if a player name is valid.
     * @param playerName The player name to validate
     * @return true if name is valid, false otherwise
     */
    fun validatePlayerName(playerName: String): Boolean
    
    /**
     * Gets all players in a game.
     * @param game The game to get players from
     * @return List of players in the game
     */
    fun getPlayers(game: Game?): List<Player>
}
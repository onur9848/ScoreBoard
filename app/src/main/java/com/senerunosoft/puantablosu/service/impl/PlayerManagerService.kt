package com.senerunosoft.puantablosu.service.impl

import android.util.Log
import com.senerunosoft.puantablosu.model.Game
import com.senerunosoft.puantablosu.model.Player
import com.senerunosoft.puantablosu.service.interfaces.IPlayerManager

/**
 * Implementation of player management operations.
 * Follows Single Responsibility Principle (SRP) - only handles player operations.
 */
class PlayerManagerService : IPlayerManager {
    
    companion object {
        private const val TAG = "PlayerManagerService"
    }
    
    override fun addPlayer(game: Game?, playerName: String): Boolean {
        if (game == null) {
            Log.w(TAG, "addPlayer: Game is null")
            return false
        }
        
        if (!validatePlayerName(playerName)) {
            Log.w(TAG, "addPlayer: Player name is invalid")
            return false
        }
        
        // Check for duplicate names
        if (hasPlayerWithName(game, playerName.trim())) {
            Log.w(TAG, "addPlayer: Player with name '${playerName.trim()}' already exists")
            return false
        }
        
        return try {
            (game.playerList as MutableList).add(Player(playerName.trim()))
            Log.d(TAG, "addPlayer: Player '${playerName.trim()}' added successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "addPlayer: Error adding player", e)
            false
        }
    }
    
    override fun removePlayer(game: Game?, playerId: String): Boolean {
        if (game == null) {
            Log.w(TAG, "removePlayer: Game is null")
            return false
        }
        
        if (playerId.isBlank()) {
            Log.w(TAG, "removePlayer: Player ID is blank")
            return false
        }
        
        return try {
            val removed = (game.playerList as MutableList).removeIf { it.id == playerId }
            if (removed) {
                Log.d(TAG, "removePlayer: Player with ID '$playerId' removed successfully")
            } else {
                Log.w(TAG, "removePlayer: Player with ID '$playerId' not found")
            }
            removed
        } catch (e: Exception) {
            Log.e(TAG, "removePlayer: Error removing player", e)
            false
        }
    }
    
    override fun validatePlayerName(playerName: String): Boolean {
        return playerName.isNotBlank() && 
               playerName.trim().length >= 2 && 
               playerName.trim().length <= 20 &&
               playerName.trim().matches(Regex("^[a-zA-Z0-9\\s]+$"))
    }
    
    override fun getPlayers(game: Game?): List<Player> {
        return game?.playerList ?: emptyList()
    }
    
    /**
     * Checks if a player with the given name already exists in the game.
     * Private method following SRP - specific validation logic.
     */
    private fun hasPlayerWithName(game: Game, playerName: String): Boolean {
        return game.playerList.any { 
            it.name.equals(playerName, ignoreCase = true) 
        }
    }
}
package com.senerunosoft.puantablosu.service.impl

import android.util.Log
import com.senerunosoft.puantablosu.model.Game
import com.senerunosoft.puantablosu.model.Player
import com.senerunosoft.puantablosu.model.config.IConfig
import com.senerunosoft.puantablosu.model.enums.GameType
import com.senerunosoft.puantablosu.service.interfaces.IGameManager

/**
 * Implementation of game management operations.
 * Follows Single Responsibility Principle (SRP) - only handles game lifecycle.
 */
class GameManagerService : IGameManager {
    
    companion object {
        private const val TAG = "GameManagerService"
    }
    
    override fun createGame(gameTitle: String, gameType: GameType, config: IConfig?): Game? {
        if (!validateGameTitle(gameTitle)) {
            Log.w(TAG, "createGame: Game title is invalid")
            return null
        }
        val playerList = mutableListOf<Player>()
        return Game(gameTitle.trim(), playerList, gameType, config)
    }
    
    override fun validateGame(game: Game?): Boolean {
        if (game == null) {
            Log.w(TAG, "validateGame: Game is null")
            return false
        }
        
        if (game.gameTitle.isBlank()) {
            Log.w(TAG, "validateGame: Game title is blank")
            return false
        }
        
        if (game.playerList.isEmpty()) {
            Log.w(TAG, "validateGame: Game has no players")
            return false
        }
        
        return true
    }
    
    /**
     * Validates if a game title is valid.
     * Private method following SRP - specific validation logic.
     */
    private fun validateGameTitle(gameTitle: String): Boolean {
        return gameTitle.isNotBlank() && gameTitle.trim().length >= 2
    }
}
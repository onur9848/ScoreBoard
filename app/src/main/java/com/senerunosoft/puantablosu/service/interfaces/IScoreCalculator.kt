package com.senerunosoft.puantablosu.service.interfaces

import com.senerunosoft.puantablosu.model.Game
import com.senerunosoft.puantablosu.model.SingleScore

/**
 * Interface for score calculation and management operations.
 * Follows Interface Segregation Principle (ISP) - focused on scoring operations only.
 */
interface IScoreCalculator {
    /**
     * Adds a score round to the game.
     * @param game The game to add scores to
     * @param scoreList List of scores for each player
     * @return true if scores were added successfully, false otherwise
     */
    fun addScore(game: Game?, scoreList: List<SingleScore>?): Boolean
    
    /**
     * Gets the score for a specific player in a specific round.
     * @param game The game to get the score from
     * @param playerId The ID of the player
     * @param round The round number
     * @return The score for that player in that round
     */
    fun getPlayerRoundScore(game: Game, playerId: String, round: Int): Int
    
    /**
     * Calculates the total scores for all players in the game.
     * @param game The game to calculate scores for
     * @return List of calculated total scores for each player
     */
    fun getCalculatedScore(game: Game): List<SingleScore>
    
    /**
     * Gets the current leader(s) of the game.
     * @param game The game to get leaders from
     * @return List of players who are currently leading
     */
    fun getGameLeaders(game: Game): List<SingleScore>
}
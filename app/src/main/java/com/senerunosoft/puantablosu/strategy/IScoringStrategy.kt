package com.senerunosoft.puantablosu.strategy

import com.senerunosoft.puantablosu.model.Game
import com.senerunosoft.puantablosu.model.SingleScore

/**
 * Strategy interface for different scoring calculation methods.
 * Follows Open/Closed Principle (OCP) - allows adding new scoring strategies without modifying existing code.
 * Follows Strategy Pattern - enables different scoring algorithms.
 */
interface IScoringStrategy {
    /**
     * Calculates the final scores for all players in the game.
     * @param game The game to calculate scores for
     * @return List of calculated scores for each player
     */
    fun calculateScores(game: Game): List<SingleScore>
    
    /**
     * Gets the name of this scoring strategy.
     * @return Human-readable name of the strategy
     */
    fun getStrategyName(): String
    
    /**
     * Gets a description of how this scoring strategy works.
     * @return Description of the scoring method
     */
    fun getDescription(): String
}
package com.senerunosoft.puantablosu.strategy

import com.senerunosoft.puantablosu.model.Game
import com.senerunosoft.puantablosu.model.SingleScore

/**
 * Standard scoring strategy that sums all round scores for each player.
 * Follows Open/Closed Principle (OCP) - implements strategy without modifying existing code.
 */
class StandardScoringStrategy : IScoringStrategy {
    
    override fun calculateScores(game: Game): List<SingleScore> {
        return game.playerList.map { player ->
            val totalScore = game.score.sumOf { score ->
                score.scoreMap[player.id] ?: 0
            }
            SingleScore(player.id, totalScore)
        }.sortedByDescending { it.score }
    }
    
    override fun getStrategyName(): String = "Standard Scoring"
    
    override fun getDescription(): String = 
        "Calculates total score by summing all round scores for each player"
}

/**
 * Average scoring strategy that calculates the average score per round for each player.
 * Demonstrates extensibility of the scoring system.
 */
class AverageScoringStrategy : IScoringStrategy {
    
    override fun calculateScores(game: Game): List<SingleScore> {
        val roundCount = game.score.size
        if (roundCount == 0) return emptyList()
        
        return game.playerList.map { player ->
            val totalScore = game.score.sumOf { score ->
                score.scoreMap[player.id] ?: 0
            }
            val averageScore = totalScore / roundCount
            SingleScore(player.id, averageScore)
        }.sortedByDescending { it.score }
    }
    
    override fun getStrategyName(): String = "Average Scoring"
    
    override fun getDescription(): String = 
        "Calculates average score per round for each player"
}

/**
 * Best rounds scoring strategy that only counts the best X rounds for each player.
 * Another example of extensible scoring strategy.
 */
class BestRoundsScoringStrategy(private val bestRoundsCount: Int = 3) : IScoringStrategy {
    
    override fun calculateScores(game: Game): List<SingleScore> {
        return game.playerList.map { player ->
            val playerScores = game.score.mapNotNull { score ->
                score.scoreMap[player.id]
            }
            
            val bestScores = playerScores
                .sortedDescending()
                .take(bestRoundsCount)
            
            val totalScore = bestScores.sum()
            SingleScore(player.id, totalScore)
        }.sortedByDescending { it.score }
    }
    
    override fun getStrategyName(): String = "Best $bestRoundsCount Rounds"
    
    override fun getDescription(): String = 
        "Calculates score using only the best $bestRoundsCount rounds for each player"
}
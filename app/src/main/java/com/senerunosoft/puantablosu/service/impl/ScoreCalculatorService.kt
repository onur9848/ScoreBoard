package com.senerunosoft.puantablosu.service.impl

import android.util.Log
import com.senerunosoft.puantablosu.model.Game
import com.senerunosoft.puantablosu.model.Score
import com.senerunosoft.puantablosu.model.SingleScore
import com.senerunosoft.puantablosu.service.interfaces.IScoreCalculator
import com.senerunosoft.puantablosu.strategy.IScoringStrategy
import com.senerunosoft.puantablosu.strategy.StandardScoringStrategy

/**
 * Implementation of score calculation operations.
 * Follows Single Responsibility Principle (SRP) - only handles scoring logic.
 * Follows Open/Closed Principle (OCP) - extensible for different scoring strategies.
 * Follows Dependency Inversion Principle (DIP) - depends on IScoringStrategy abstraction.
 */
class ScoreCalculatorService(
    private val scoringStrategy: IScoringStrategy = StandardScoringStrategy()
) : IScoreCalculator {
    
    companion object {
        private const val TAG = "ScoreCalculatorService"
    }
    
    override fun addScore(game: Game?, scoreList: List<SingleScore>?): Boolean {
        if (game == null) {
            Log.w(TAG, "addScore: Game is null")
            return false
        }
        
        if (scoreList == null) {
            Log.w(TAG, "addScore: Score list is null")
            return false
        }
        
        val gamePlayerCount = game.playerList.size
        val scorePlayerCount = scoreList.size
        
        if (gamePlayerCount != scorePlayerCount) {
            Log.w(TAG, "addScore: Player count mismatch - Game: $gamePlayerCount, Scores: $scorePlayerCount")
            return false
        }
        
        // Validate all scores are for valid players
        if (!validateScoreList(game, scoreList)) {
            Log.w(TAG, "addScore: Score list validation failed")
            return false
        }
        
        return try {
            val scoreMap = createScoreMap(scoreList)
            val nextRound = game.score.size + 1
            (game.score as MutableList).add(Score(nextRound, scoreMap))
            Log.d(TAG, "addScore: Round $nextRound scores added successfully using ${scoringStrategy.getStrategyName()}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "addScore: Error adding score", e)
            false
        }
    }
    
    override fun getPlayerRoundScore(game: Game, playerId: String, round: Int): Int {
        if (round <= 0 || round > game.score.size) {
            Log.w(TAG, "getPlayerRoundScore: Invalid round number $round")
            return 0
        }
        
        return try {
            val scoreEntry = game.score.find { it.scoreOrder == round }
            scoreEntry?.scoreMap?.get(playerId) ?: 0
        } catch (e: Exception) {
            Log.e(TAG, "getPlayerRoundScore: Error getting player round score", e)
            0
        }
    }
    
    override fun getCalculatedScore(game: Game): List<SingleScore> {
        return try {
            val calculatedScores = scoringStrategy.calculateScores(game)
            Log.d(TAG, "getCalculatedScore: Scores calculated using ${scoringStrategy.getStrategyName()}")
            calculatedScores
        } catch (e: Exception) {
            Log.e(TAG, "getCalculatedScore: Error calculating scores", e)
            emptyList()
        }
    }
    
    override fun getGameLeaders(game: Game): List<SingleScore> {
        val calculatedScores = getCalculatedScore(game)
        if (calculatedScores.isEmpty()) {
            return emptyList()
        }
        
        val highestScore = calculatedScores.first().score
        return calculatedScores.filter { it.score == highestScore }
    }
    
    /**
     * Gets the current scoring strategy being used.
     */
    fun getScoringStrategy(): IScoringStrategy = scoringStrategy
    
    /**
     * Validates that all scores in the list correspond to valid players in the game.
     * Private method following SRP - specific validation logic.
     */
    private fun validateScoreList(game: Game, scoreList: List<SingleScore>): Boolean {
        val playerIds = game.playerList.map { it.id }.toSet()
        return scoreList.all { score ->
            playerIds.contains(score.playerId)
        }
    }
    
    /**
     * Creates a score map from a list of single scores.
     * Private method following SRP - specific data transformation logic.
     */
    private fun createScoreMap(scoreList: List<SingleScore>): HashMap<String, Int> {
        return HashMap<String, Int>().apply {
            scoreList.forEach { singleScore ->
                put(singleScore.playerId, singleScore.score)
            }
        }
    }
}
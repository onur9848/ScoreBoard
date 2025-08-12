package com.senerunosoft.puantablosu.service

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.senerunosoft.puantablosu.IGameService
import com.senerunosoft.puantablosu.model.Game
import com.senerunosoft.puantablosu.model.Player
import com.senerunosoft.puantablosu.model.Score
import com.senerunosoft.puantablosu.model.SingleScore
import com.senerunosoft.puantablosu.model.config.IConfig
import com.senerunosoft.puantablosu.model.enums.GameType

class GameService : IGameService {

    private val TAG = "GameService"
    override fun createGame(gameTitle: String, gameType: GameType, config: IConfig?): Game? {
        if (gameTitle.isBlank()) {
            Log.w(TAG, "createGame: Game title is null or empty")
            return null
        }
        val playerList = mutableListOf<Player>()
        return Game(gameTitle.trim(), playerList, gameType, config)
    }

    override fun addPlayer(game: Game?, playerName: String) {
        if (playerName.isBlank()) {
            Log.w(TAG, "addPlayer: Player name is null or empty")
            return
        }
        (game?.playerList as MutableList).add(Player(playerName.trim()))
    }

    override fun addScore(game: Game?, scoreList: List<SingleScore>?): Boolean {
        val gamePlayerCount = game?.playerList?.size
        val scorePlayerCount = scoreList?.size
        
        if (gamePlayerCount != scorePlayerCount) {
            Log.d(TAG, "addScore: GamePlayer:$gamePlayerCount")
            Log.d(TAG, "addScore: ScorePlayer:$scorePlayerCount")
            return false
        }
        
        // GameType-specific validation
        val isValidForGameType = when (game?.gameType) {
            GameType.YuzBirOkey -> {
                // 101 Okey: validate that scores follow game rules
                validateYuzBirOkeyScores(scoreList)
            }
            GameType.Okey -> {
                // Okey: validate that scores are reasonable for Okey game
                validateOkeyScores(scoreList)
            }
            GameType.GenelOyun -> {
                // General game: allow any scores
                true
            }
            null -> true
        }
        
        if (!isValidForGameType) {
            Log.w(TAG, "addScore: Invalid scores for game type ${game?.gameType}")
            return false
        }
        
        return try {
            val scoreMap = getScoreMap(scoreList)
            (game?.score as MutableList).add(Score(game.score.size + 1, scoreMap))
            true
        } catch (e: Exception) {
            Log.e(TAG, "addScore: Error adding score", e)
            false
        }
    }

    override fun getPlayerRoundScore(game: Game, playerId: String, round: Int): Int {
        for (score in game.score) {
            if (score.scoreOrder == round) {
                return score.scoreMap[playerId] ?: 0
            }
        }
        return 0
    }

    override fun getCalculatedScore(game: Game): List<SingleScore> {
        val calculatedScoreList = mutableListOf<SingleScore>()
        
        for (player in game.playerList) {
            var totalScore = 0
            for (score in game.score) {
                totalScore += score.scoreMap[player.id] ?: 0
            }
            
            // Apply GameType-specific score calculation logic
            val finalScore = when (game.gameType) {
                GameType.YuzBirOkey -> {
                    // In 101 Okey, typically the goal is to reach exactly 101 or stay below
                    // Apply any game-specific bonuses or penalties
                    applyYuzBirOkeyScoring(totalScore)
                }
                GameType.Okey -> {
                    // In Okey, scores are typically accumulated differently
                    applyOkeyScoring(totalScore)
                }
                GameType.GenelOyun -> {
                    // General game: use total score as-is
                    totalScore
                }
            }
            
            calculatedScoreList.add(SingleScore(player.id, finalScore))
        }
        
        return calculatedScoreList
    }

    override fun serializeGame(game: Game?): String? {
        return try {
            val gson = Gson()
            gson.toJson(game)
        } catch (e: Exception) {
            Log.e(TAG, "serializeGame: Error serializing game", e)
            null
        }
    }

    override fun deserializeGame(gameString: String): Game? {
        if (gameString.isBlank()) {
            return null
        }
        return try {
            val gson = Gson()
            gson.fromJson(gameString, Game::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "deserializeGame: Error deserializing game", e)
            null
        }
    }

    private fun getScoreMap(scoreList: List<SingleScore>?): HashMap<String, Int> {
        val scoreMap = HashMap<String, Int>()
        if (scoreList != null) {
            for (singleScore in scoreList) {
                scoreMap[singleScore.playerId] = singleScore.score
            }
        }
        return scoreMap
    }

    /**
     * Apply 101 Okey specific scoring rules
     */
    private fun applyYuzBirOkeyScoring(totalScore: Int): Int {
        // In 101 Okey, players typically aim to stay at or below 101
        // Add any specific game logic here
        return totalScore
    }

    /**
     * Apply Okey specific scoring rules  
     */
    private fun applyOkeyScoring(totalScore: Int): Int {
        // In Okey, different scoring rules might apply
        // Add any specific game logic here
        return totalScore
    }

    /**
     * Validate scores for 101 Okey game
     * In 101 Okey, scores are typically positive and follow specific patterns
     */
    private fun validateYuzBirOkeyScores(scoreList: List<SingleScore>?): Boolean {
        if (scoreList == null) return false
        
        return scoreList.all { singleScore ->
            // Allow 0 scores (no points this round) and positive scores
            // Negative scores are typically not allowed in 101 Okey unless it's a penalty
            singleScore.score >= -50 && singleScore.score <= 1000
        }
    }

    /**
     * Validate scores for Okey game
     * In Okey, both positive and negative scores are common
     */
    private fun validateOkeyScores(scoreList: List<SingleScore>?): Boolean {
        if (scoreList == null) return false
        
        return scoreList.all { singleScore ->
            // Allow reasonable range for Okey scores
            singleScore.score >= -500 && singleScore.score <= 500
        }
    }
}
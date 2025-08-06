package com.senerunosoft.puantablosu.examples

import com.senerunosoft.puantablosu.model.Game
import com.senerunosoft.puantablosu.model.Player
import com.senerunosoft.puantablosu.model.SingleScore
import com.senerunosoft.puantablosu.service.interfaces.IGameManager
import com.senerunosoft.puantablosu.service.interfaces.IPlayerManager
import com.senerunosoft.puantablosu.service.interfaces.IScoreCalculator
import com.senerunosoft.puantablosu.service.interfaces.IGameSerializer
import com.senerunosoft.puantablosu.extensions.*
import com.senerunosoft.puantablosu.strategy.AverageScoringStrategy
import com.senerunosoft.puantablosu.strategy.BestRoundsScoringStrategy

/**
 * Example usage of the SOLID architecture implementation.
 * Demonstrates how to use the focused services and extension functions.
 */
class SolidArchitectureExample {

    fun demonstrateNewArchitecture(
        gameManager: IGameManager,
        playerManager: IPlayerManager,
        scoreCalculator: IScoreCalculator,
        gameSerializer: IGameSerializer
    ) {
        // === SINGLE RESPONSIBILITY PRINCIPLE ===
        // Each service has a focused responsibility
        
        // 1. Game Management Service
        val game = gameManager.createGame("Championship Game")
        println("Game created: ${game?.gameTitle}")
        
        // 2. Player Management Service  
        game?.let { g ->
            playerManager.addPlayer(g, "Alice")
            playerManager.addPlayer(g, "Bob")
            playerManager.addPlayer(g, "Charlie")
            
            val players = playerManager.getPlayers(g)
            println("Players added: ${players.map { it.name }}")
        }
        
        // === OPEN/CLOSED PRINCIPLE ===
        // Extension functions add functionality without modifying existing classes
        
        game?.let { g ->
            // Using extension functions
            println("Game ready to play: ${g.isReadyToPlay()}")
            println("Has minimum players: ${g.hasMinimumPlayers()}")
            println("Player names: ${g.getPlayerNames()}")
            
            // Player extensions
            val firstPlayer = g.playerList.first()
            println("Player display name: ${firstPlayer.getDisplayName()}")
            println("Player initials: ${firstPlayer.getInitials()}")
            println("Formatted name: ${firstPlayer.formatForDisplay()}")
        }
        
        // === INTERFACE SEGREGATION PRINCIPLE ===
        // Using only the interfaces we need for specific operations
        
        game?.let { g ->
            // 3. Score Calculation Service
            val round1Scores = listOf(
                SingleScore(g.playerList[0].id, 10),
                SingleScore(g.playerList[1].id, 8),
                SingleScore(g.playerList[2].id, 12)
            )
            
            scoreCalculator.addScore(g, round1Scores)
            
            val round2Scores = listOf(
                SingleScore(g.playerList[0].id, 15),
                SingleScore(g.playerList[1].id, 11),
                SingleScore(g.playerList[2].id, 9)
            )
            
            scoreCalculator.addScore(g, round2Scores)
            
            // Calculate final scores
            val finalScores = scoreCalculator.getCalculatedScore(g)
            println("Final scores: ${finalScores.map { "${it.playerId}: ${it.score}" }}")
            
            // Using extension functions on scores
            val winners = finalScores.getWinners()
            println("Winners: ${winners.map { it.playerId }}")
            
            val sortedScores = finalScores.sortByScore()
            println("Sorted scores: ${sortedScores.map { it.score }}")
        }
        
        // === DEPENDENCY INVERSION PRINCIPLE ===
        // Services depend on abstractions, not concrete implementations
        
        game?.let { g ->
            // 4. Serialization Service
            val serializedGame = gameSerializer.serializeGame(g)
            println("Game serialized: ${serializedGame != null}")
            
            serializedGame?.let { data ->
                val deserializedGame = gameSerializer.deserializeGame(data)
                println("Game deserialized: ${deserializedGame?.gameTitle}")
            }
        }
    }
    
    fun demonstrateScoringStrategies() {
        // === STRATEGY PATTERN DEMONSTRATION ===
        // Different scoring strategies can be used interchangeably
        
        println("\n=== Scoring Strategies Demo ===")
        
        // Sample game data
        val game = Game("Strategy Demo", listOf(
            Player("Alice"),
            Player("Bob")
        ))
        
        // Add some rounds
        game.score.addAll(listOf(
            com.senerunosoft.puantablosu.model.Score(1, hashMapOf(
                game.playerList[0].id to 10,
                game.playerList[1].id to 8
            )),
            com.senerunosoft.puantablosu.model.Score(2, hashMapOf(
                game.playerList[0].id to 5,
                game.playerList[1].id to 12
            )),
            com.senerunosoft.puantablosu.model.Score(3, hashMapOf(
                game.playerList[0].id to 15,
                game.playerList[1].id to 6
            ))
        ))
        
        // Strategy 1: Standard scoring (sum all rounds)
        val standardStrategy = com.senerunosoft.puantablosu.strategy.StandardScoringStrategy()
        val standardScores = standardStrategy.calculateScores(game)
        println("${standardStrategy.getStrategyName()}: ${standardScores.map { it.score }}")
        
        // Strategy 2: Average scoring
        val averageStrategy = AverageScoringStrategy()
        val averageScores = averageStrategy.calculateScores(game)
        println("${averageStrategy.getStrategyName()}: ${averageScores.map { it.score }}")
        
        // Strategy 3: Best rounds scoring
        val bestRoundsStrategy = BestRoundsScoringStrategy(2)
        val bestRoundsScores = bestRoundsStrategy.calculateScores(game)
        println("${bestRoundsStrategy.getStrategyName()}: ${bestRoundsScores.map { it.score }}")
    }
    
    fun demonstrateExtensibility() {
        println("\n=== Extensibility Demo ===")
        
        // Create a new custom scoring strategy without modifying existing code
        val customStrategy = object : com.senerunosoft.puantablosu.strategy.IScoringStrategy {
            override fun calculateScores(game: Game): List<SingleScore> {
                // Custom logic: Only count scores > 10
                return game.playerList.map { player ->
                    val highScores = game.score.mapNotNull { score ->
                        val playerScore = score.scoreMap[player.id] ?: 0
                        if (playerScore > 10) playerScore else null
                    }
                    SingleScore(player.id, highScores.sum())
                }.sortedByDescending { it.score }
            }
            
            override fun getStrategyName(): String = "High Scores Only"
            override fun getDescription(): String = "Only counts rounds where player scored > 10"
        }
        
        println("Custom strategy available: ${customStrategy.getStrategyName()}")
        println("Description: ${customStrategy.getDescription()}")
    }
}

/**
 * Example of how to create a new service that uses the focused interfaces.
 * Demonstrates the benefits of the new architecture.
 */
class GameAnalyticsService(
    private val scoreCalculator: IScoreCalculator,
    private val playerManager: IPlayerManager
) {
    
    fun generateGameReport(game: Game): String {
        val players = playerManager.getPlayers(game)
        val scores = scoreCalculator.getCalculatedScore(game)
        val leaders = scoreCalculator.getGameLeaders(game)
        
        return buildString {
            appendLine("=== Game Report ===")
            appendLine("Game: ${game.gameTitle}")
            appendLine("Players: ${players.size}")
            appendLine("Rounds played: ${game.getRoundCount()}")
            appendLine("Current leaders: ${leaders.map { score -> 
                players.find { it.id == score.playerId }?.name ?: "Unknown"
            }}")
            appendLine("Scores:")
            scores.forEach { score ->
                val playerName = players.find { it.id == score.playerId }?.name ?: "Unknown"
                appendLine("  $playerName: ${score.score}")
            }
        }
    }
}
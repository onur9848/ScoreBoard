package com.senerunosoft.puantablosu.extensions

import com.senerunosoft.puantablosu.model.Game
import com.senerunosoft.puantablosu.model.Player
import com.senerunosoft.puantablosu.model.SingleScore

/**
 * Extension functions for Game model.
 * Follows Open/Closed Principle (OCP) - extends functionality without modifying existing code.
 */

/**
 * Extension function to check if a game has enough players to start.
 */
fun Game.hasMinimumPlayers(): Boolean = playerList.size >= 2

/**
 * Extension function to get the number of rounds played.
 */
fun Game.getRoundCount(): Int = score.size

/**
 * Extension function to check if the game has any scores recorded.
 */
fun Game.hasScores(): Boolean = score.isNotEmpty()

/**
 * Extension function to get a player by ID.
 */
fun Game.getPlayer(playerId: String): Player? = 
    playerList.find { it.id == playerId }

/**
 * Extension function to get player names as a list.
 */
fun Game.getPlayerNames(): List<String> = 
    playerList.map { it.name }

/**
 * Extension function to check if game is ready to play.
 */
fun Game.isReadyToPlay(): Boolean = 
    gameTitle.isNotBlank() && hasMinimumPlayers()

/**
 * Extension function to get the latest round number.
 */
fun Game.getLatestRound(): Int = 
    if (score.isEmpty()) 0 else score.maxOf { it.scoreOrder }

/**
 * Extension function to find the winning score.
 */
fun List<SingleScore>.getWinningScore(): Int? = 
    if (isEmpty()) null else maxOf { it.score }

/**
 * Extension function to get winners (players with highest score).
 */
fun List<SingleScore>.getWinners(): List<SingleScore> {
    val winningScore = getWinningScore() ?: return emptyList()
    return filter { it.score == winningScore }
}

/**
 * Extension function to sort scores by descending order.
 */
fun List<SingleScore>.sortByScore(): List<SingleScore> = 
    sortedByDescending { it.score }
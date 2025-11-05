package com.senerunosoft.puantablosu.service

import com.senerunosoft.puantablosu.model.Game
import com.senerunosoft.puantablosu.model.Player
import com.senerunosoft.puantablosu.model.Score
import com.senerunosoft.puantablosu.model.SingleScore
import com.senerunosoft.puantablosu.model.enums.GameType
import com.senerunosoft.puantablosu.service.impl.ScoreCalculatorService
import com.senerunosoft.puantablosu.strategy.StandardScoringStrategy
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ScoreCalculatorService
 * Tests SOLID-compliant score calculation service
 */
class ScoreCalculatorServiceTest {

    private lateinit var scoreCalculator: ScoreCalculatorService
    private lateinit var testGame: Game

    @Before
    fun setUp() {
        scoreCalculator = ScoreCalculatorService(StandardScoringStrategy())
        testGame = createTestGame()
    }

    // ==================== Add Score Tests ====================

    @Test
    fun `addScore with valid scores returns true`() {
        val scores = createScoreList(10, 20)
        
        val result = scoreCalculator.addScore(testGame, scores)
        
        assertTrue(result)
        assertEquals(1, testGame.score.size)
    }

    @Test
    fun `addScore with null game returns false`() {
        val scores = createScoreList(10, 20)
        
        val result = scoreCalculator.addScore(null, scores)
        
        assertFalse(result)
    }

    @Test
    fun `addScore with null scores returns false`() {
        val result = scoreCalculator.addScore(testGame, null)
        
        assertFalse(result)
    }

    @Test
    fun `addScore with mismatched player count returns false`() {
        val scores = listOf(SingleScore(testGame.playerList[0].id, 10))
        
        val result = scoreCalculator.addScore(testGame, scores)
        
        assertFalse(result)
    }

    @Test
    fun `addScore with invalid player ID returns false`() {
        val scores = listOf(
            SingleScore("invalid-id", 10),
            SingleScore(testGame.playerList[1].id, 20)
        )
        
        val result = scoreCalculator.addScore(testGame, scores)
        
        assertFalse(result)
    }

    @Test
    fun `addScore increments round order correctly`() {
        scoreCalculator.addScore(testGame, createScoreList(10, 20))
        scoreCalculator.addScore(testGame, createScoreList(15, 25))
        scoreCalculator.addScore(testGame, createScoreList(5, 30))
        
        assertEquals(3, testGame.score.size)
        assertEquals(1, testGame.score[0].scoreOrder)
        assertEquals(2, testGame.score[1].scoreOrder)
        assertEquals(3, testGame.score[2].scoreOrder)
    }

    @Test
    fun `addScore stores scores correctly in scoreMap`() {
        val player1Id = testGame.playerList[0].id
        val player2Id = testGame.playerList[1].id
        val scores = listOf(SingleScore(player1Id, 42), SingleScore(player2Id, 73))
        
        scoreCalculator.addScore(testGame, scores)
        
        val scoreMap = testGame.score[0].scoreMap
        assertEquals(42, scoreMap[player1Id])
        assertEquals(73, scoreMap[player2Id])
    }

    // ==================== Get Player Round Score Tests ====================

    @Test
    fun `getPlayerRoundScore returns correct score`() {
        val player1Id = testGame.playerList[0].id
        scoreCalculator.addScore(testGame, createScoreList(10, 20))
        scoreCalculator.addScore(testGame, createScoreList(15, 25))
        
        val score = scoreCalculator.getPlayerRoundScore(testGame, player1Id, 1)
        
        assertEquals(10, score)
    }

    @Test
    fun `getPlayerRoundScore with invalid round returns zero`() {
        val player1Id = testGame.playerList[0].id
        scoreCalculator.addScore(testGame, createScoreList(10, 20))
        
        assertEquals(0, scoreCalculator.getPlayerRoundScore(testGame, player1Id, 0))
        assertEquals(0, scoreCalculator.getPlayerRoundScore(testGame, player1Id, 5))
    }

    @Test
    fun `getPlayerRoundScore with negative round returns zero`() {
        val player1Id = testGame.playerList[0].id
        
        val score = scoreCalculator.getPlayerRoundScore(testGame, player1Id, -1)
        
        assertEquals(0, score)
    }

    @Test
    fun `getPlayerRoundScore with nonexistent player returns zero`() {
        scoreCalculator.addScore(testGame, createScoreList(10, 20))
        
        val score = scoreCalculator.getPlayerRoundScore(testGame, "nonexistent-id", 1)
        
        assertEquals(0, score)
    }

    // ==================== Get Calculated Score Tests ====================

    @Test
    fun `getCalculatedScore returns correct total scores`() {
        val player1Id = testGame.playerList[0].id
        val player2Id = testGame.playerList[1].id
        
        scoreCalculator.addScore(testGame, listOf(SingleScore(player1Id, 10), SingleScore(player2Id, 20)))
        scoreCalculator.addScore(testGame, listOf(SingleScore(player1Id, 15), SingleScore(player2Id, 25)))
        scoreCalculator.addScore(testGame, listOf(SingleScore(player1Id, 5), SingleScore(player2Id, 10)))
        
        val calculatedScores = scoreCalculator.getCalculatedScore(testGame)
        
        assertEquals(2, calculatedScores.size)
        assertEquals(30, calculatedScores.find { it.playerId == player1Id }?.score)
        assertEquals(55, calculatedScores.find { it.playerId == player2Id }?.score)
    }

    @Test
    fun `getCalculatedScore with no rounds returns zero for all players`() {
        val calculatedScores = scoreCalculator.getCalculatedScore(testGame)
        
        assertEquals(2, calculatedScores.size)
        assertTrue(calculatedScores.all { it.score == 0 })
    }

    @Test
    fun `getCalculatedScore handles negative scores correctly`() {
        val player1Id = testGame.playerList[0].id
        val player2Id = testGame.playerList[1].id
        
        scoreCalculator.addScore(testGame, listOf(SingleScore(player1Id, -10), SingleScore(player2Id, 20)))
        scoreCalculator.addScore(testGame, listOf(SingleScore(player1Id, 15), SingleScore(player2Id, -5)))
        
        val calculatedScores = scoreCalculator.getCalculatedScore(testGame)
        
        assertEquals(5, calculatedScores.find { it.playerId == player1Id }?.score)
        assertEquals(15, calculatedScores.find { it.playerId == player2Id }?.score)
    }

    @Test
    fun `getCalculatedScore returns sorted scores`() {
        val player1Id = testGame.playerList[0].id
        val player2Id = testGame.playerList[1].id
        
        scoreCalculator.addScore(testGame, listOf(SingleScore(player1Id, 10), SingleScore(player2Id, 50)))
        
        val calculatedScores = scoreCalculator.getCalculatedScore(testGame)
        
        // Scores should be sorted in descending order
        assertTrue(calculatedScores[0].score >= calculatedScores[1].score)
    }

    // ==================== Get Game Leaders Tests ====================

    @Test
    fun `getGameLeaders returns player with highest score`() {
        val player1Id = testGame.playerList[0].id
        val player2Id = testGame.playerList[1].id
        
        scoreCalculator.addScore(testGame, listOf(SingleScore(player1Id, 10), SingleScore(player2Id, 50)))
        
        val leaders = scoreCalculator.getGameLeaders(testGame)
        
        assertEquals(1, leaders.size)
        assertEquals(player2Id, leaders[0].playerId)
        assertEquals(50, leaders[0].score)
    }

    @Test
    fun `getGameLeaders returns multiple players with tied highest score`() {
        val player1Id = testGame.playerList[0].id
        val player2Id = testGame.playerList[1].id
        
        scoreCalculator.addScore(testGame, listOf(SingleScore(player1Id, 50), SingleScore(player2Id, 50)))
        
        val leaders = scoreCalculator.getGameLeaders(testGame)
        
        assertEquals(2, leaders.size)
        assertTrue(leaders.all { it.score == 50 })
    }

    @Test
    fun `getGameLeaders with no scores returns empty list`() {
        val leaders = scoreCalculator.getGameLeaders(testGame)
        
        assertTrue(leaders.isEmpty())
    }

    // ==================== Strategy Tests ====================

    @Test
    fun `getScoringStrategy returns correct strategy`() {
        val strategy = scoreCalculator.getScoringStrategy()
        
        assertNotNull(strategy)
        assertTrue(strategy is StandardScoringStrategy)
    }

    // ==================== Edge Case Tests ====================

    @Test
    fun `addScore with zero scores works correctly`() {
        val scores = createScoreList(0, 0)
        
        val result = scoreCalculator.addScore(testGame, scores)
        
        assertTrue(result)
        assertEquals(1, testGame.score.size)
    }

    @Test
    fun `addScore with large scores works correctly`() {
        val scores = createScoreList(9999, 8888)
        
        val result = scoreCalculator.addScore(testGame, scores)
        
        assertTrue(result)
        val calculatedScores = scoreCalculator.getCalculatedScore(testGame)
        assertEquals(9999, calculatedScores.find { it.playerId == testGame.playerList[0].id }?.score)
    }

    @Test
    fun `multiple rounds accumulate correctly`() {
        val player1Id = testGame.playerList[0].id
        val player2Id = testGame.playerList[1].id
        
        // Add 10 rounds
        repeat(10) { round ->
            scoreCalculator.addScore(testGame, listOf(
                SingleScore(player1Id, round + 1),
                SingleScore(player2Id, (round + 1) * 2)
            ))
        }
        
        assertEquals(10, testGame.score.size)
        
        val calculatedScores = scoreCalculator.getCalculatedScore(testGame)
        // Sum of 1+2+3...+10 = 55
        assertEquals(55, calculatedScores.find { it.playerId == player1Id }?.score)
        // Sum of 2+4+6...+20 = 110
        assertEquals(110, calculatedScores.find { it.playerId == player2Id }?.score)
    }

    // ==================== Helper Methods ====================

    private fun createTestGame(): Game {
        val players = listOf(
            Player("Player 1"),
            Player("Player 2")
        )
        return Game(
            gameTitle = "Test Game",
            playerList = players,
            gameType = GameType.GenelOyun
        )
    }

    private fun createScoreList(score1: Int, score2: Int): List<SingleScore> {
        return listOf(
            SingleScore(testGame.playerList[0].id, score1),
            SingleScore(testGame.playerList[1].id, score2)
        )
    }
}

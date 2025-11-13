package com.senerunosoft.puantablosu.extensions

import com.senerunosoft.puantablosu.model.Game
import com.senerunosoft.puantablosu.model.Player
import com.senerunosoft.puantablosu.model.Score
import com.senerunosoft.puantablosu.model.SingleScore
import com.senerunosoft.puantablosu.model.enums.GameType
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Game extension functions
 * Tests utility functions that extend Game model functionality
 */
class GameExtensionsTest {

    // ==================== hasMinimumPlayers Tests ====================

    @Test
    fun `hasMinimumPlayers returns true with 2 players`() {
        val game = createGameWithPlayers(2)
        
        assertTrue(game.hasMinimumPlayers())
    }

    @Test
    fun `hasMinimumPlayers returns true with more than 2 players`() {
        val game = createGameWithPlayers(4)
        
        assertTrue(game.hasMinimumPlayers())
    }

    @Test
    fun `hasMinimumPlayers returns false with 1 player`() {
        val game = createGameWithPlayers(1)
        
        assertFalse(game.hasMinimumPlayers())
    }

    @Test
    fun `hasMinimumPlayers returns false with 0 players`() {
        val game = createGameWithPlayers(0)
        
        assertFalse(game.hasMinimumPlayers())
    }

    // ==================== getRoundCount Tests ====================

    @Test
    fun `getRoundCount returns correct count`() {
        val game = createGameWithPlayers(2)
        addScoreRounds(game, 3)
        
        assertEquals(3, game.getRoundCount())
    }

    @Test
    fun `getRoundCount returns 0 with no scores`() {
        val game = createGameWithPlayers(2)
        
        assertEquals(0, game.getRoundCount())
    }

    // ==================== hasScores Tests ====================

    @Test
    fun `hasScores returns true when scores exist`() {
        val game = createGameWithPlayers(2)
        addScoreRounds(game, 1)
        
        assertTrue(game.hasScores())
    }

    @Test
    fun `hasScores returns false when no scores`() {
        val game = createGameWithPlayers(2)
        
        assertFalse(game.hasScores())
    }

    // ==================== getPlayer Tests ====================

    @Test
    fun `getPlayer returns correct player by ID`() {
        val game = createGameWithPlayers(2)
        val playerId = game.playerList[0].id
        
        val player = game.getPlayer(playerId)
        
        assertNotNull(player)
        assertEquals(playerId, player?.id)
    }

    @Test
    fun `getPlayer returns null for invalid ID`() {
        val game = createGameWithPlayers(2)
        
        val player = game.getPlayer("invalid-id")
        
        assertNull(player)
    }

    // ==================== getPlayerNames Tests ====================

    @Test
    fun `getPlayerNames returns all player names`() {
        val game = Game(
            gameTitle = "Test",
            playerList = listOf(
                Player("Alice"),
                Player("Bob"),
                Player("Charlie")
            ),
            gameType = GameType.GenelOyun
        )
        
        val names = game.getPlayerNames()
        
        assertEquals(3, names.size)
        assertTrue(names.contains("Alice"))
        assertTrue(names.contains("Bob"))
        assertTrue(names.contains("Charlie"))
    }

    @Test
    fun `getPlayerNames returns empty list for no players`() {
        val game = createGameWithPlayers(0)
        
        val names = game.getPlayerNames()
        
        assertTrue(names.isEmpty())
    }

    // ==================== isReadyToPlay Tests ====================

    @Test
    fun `isReadyToPlay returns true with title and players`() {
        val game = Game(
            gameTitle = "Test Game",
            playerList = listOf(Player("P1"), Player("P2")),
            gameType = GameType.GenelOyun
        )
        
        assertTrue(game.isReadyToPlay())
    }

    @Test
    fun `isReadyToPlay returns false with blank title`() {
        val game = Game(
            gameTitle = "   ",
            playerList = listOf(Player("P1"), Player("P2")),
            gameType = GameType.GenelOyun
        )
        
        assertFalse(game.isReadyToPlay())
    }

    @Test
    fun `isReadyToPlay returns false with not enough players`() {
        val game = Game(
            gameTitle = "Test Game",
            playerList = listOf(Player("P1")),
            gameType = GameType.GenelOyun
        )
        
        assertFalse(game.isReadyToPlay())
    }

    // ==================== getLatestRound Tests ====================

    @Test
    fun `getLatestRound returns 0 with no scores`() {
        val game = createGameWithPlayers(2)
        
        assertEquals(0, game.getLatestRound())
    }

    @Test
    fun `getLatestRound returns highest round number`() {
        val game = createGameWithPlayers(2)
        game.score.add(Score(1, hashMapOf()))
        game.score.add(Score(3, hashMapOf()))
        game.score.add(Score(2, hashMapOf()))
        
        assertEquals(3, game.getLatestRound())
    }

    // ==================== SingleScore List Extensions Tests ====================

    @Test
    fun `getWinningScore returns highest score`() {
        val scores = listOf(
            SingleScore("p1", 10),
            SingleScore("p2", 50),
            SingleScore("p3", 30)
        )
        
        assertEquals(50, scores.getWinningScore())
    }

    @Test
    fun `getWinningScore returns null for empty list`() {
        val scores = emptyList<SingleScore>()
        
        assertNull(scores.getWinningScore())
    }

    @Test
    fun `getWinners returns all players with highest score`() {
        val scores = listOf(
            SingleScore("p1", 50),
            SingleScore("p2", 50),
            SingleScore("p3", 30)
        )
        
        val winners = scores.getWinners()
        
        assertEquals(2, winners.size)
        assertTrue(winners.all { it.score == 50 })
    }

    @Test
    fun `getWinners returns single winner`() {
        val scores = listOf(
            SingleScore("p1", 10),
            SingleScore("p2", 50),
            SingleScore("p3", 30)
        )
        
        val winners = scores.getWinners()
        
        assertEquals(1, winners.size)
        assertEquals("p2", winners[0].playerId)
    }

    @Test
    fun `getWinners returns empty list for empty input`() {
        val scores = emptyList<SingleScore>()
        
        val winners = scores.getWinners()
        
        assertTrue(winners.isEmpty())
    }

    @Test
    fun `sortByScore sorts in descending order`() {
        val scores = listOf(
            SingleScore("p1", 10),
            SingleScore("p2", 50),
            SingleScore("p3", 30)
        )
        
        val sorted = scores.sortByScore()
        
        assertEquals(50, sorted[0].score)
        assertEquals(30, sorted[1].score)
        assertEquals(10, sorted[2].score)
    }

    @Test
    fun `sortByScore handles equal scores`() {
        val scores = listOf(
            SingleScore("p1", 50),
            SingleScore("p2", 50),
            SingleScore("p3", 50)
        )
        
        val sorted = scores.sortByScore()
        
        assertEquals(3, sorted.size)
        assertTrue(sorted.all { it.score == 50 })
    }

    @Test
    fun `sortByScore handles negative scores`() {
        val scores = listOf(
            SingleScore("p1", -10),
            SingleScore("p2", 20),
            SingleScore("p3", -30)
        )
        
        val sorted = scores.sortByScore()
        
        assertEquals(20, sorted[0].score)
        assertEquals(-10, sorted[1].score)
        assertEquals(-30, sorted[2].score)
    }

    // ==================== Helper Methods ====================

    private fun createGameWithPlayers(count: Int): Game {
        val players = (1..count).map { Player("Player $it") }
        return Game(
            gameTitle = "Test Game",
            playerList = players,
            gameType = GameType.GenelOyun
        )
    }

    private fun addScoreRounds(game: Game, rounds: Int) {
        repeat(rounds) { round ->
            val scoreMap = hashMapOf<String, Int>()
            game.playerList.forEach { player ->
                scoreMap[player.id] = (round + 1) * 10
            }
            game.score.add(Score(round + 1, scoreMap))
        }
    }
}

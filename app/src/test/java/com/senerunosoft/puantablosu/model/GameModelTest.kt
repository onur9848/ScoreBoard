package com.senerunosoft.puantablosu.model

import com.senerunosoft.puantablosu.model.enums.GameType
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Game data model
 * Tests model initialization, data integrity, and Kotlin data class features
 */
class GameModelTest {

    @Test
    fun `Game default constructor creates valid game`() {
        val game = Game()
        
        assertNotNull(game.gameId)
        assertEquals("", game.gameTitle)
        assertTrue(game.playerList.isEmpty())
        assertTrue(game.score.isEmpty())
        assertEquals(GameType.GenelOyun, game.gameType)
        assertNull(game.config)
    }

    @Test
    fun `Game constructor with parameters creates valid game`() {
        val players = listOf(Player("Player 1"), Player("Player 2"))
        val game = Game(
            gameTitle = "Test Game",
            playerList = players,
            gameType = GameType.YuzBirOkey
        )
        
        assertNotNull(game.gameId)
        assertEquals("Test Game", game.gameTitle)
        assertEquals(2, game.playerList.size)
        assertEquals(GameType.YuzBirOkey, game.gameType)
    }

    @Test
    fun `Game generates unique IDs`() {
        val game1 = Game("Game 1", emptyList(), GameType.GenelOyun, null)
        val game2 = Game("Game 2", emptyList(), GameType.GenelOyun, null)
        
        assertNotEquals(game1.gameId, game2.gameId)
    }

    @Test
    fun `Game data class copy works correctly`() {
        val original = Game(
            gameTitle = "Original",
            playerList = listOf(Player("P1")),
            gameType = GameType.Okey
        )
        
        val copy = original.copy(gameTitle = "Modified")
        
        assertEquals("Modified", copy.gameTitle)
        assertEquals(original.gameId, copy.gameId)
        assertEquals(original.playerList, copy.playerList)
    }

    @Test
    fun `Player default name is set correctly`() {
        val player = Player("Test Player")
        
        assertEquals("Test Player", player.name)
        assertNotNull(player.id)
    }

    @Test
    fun `Player generates unique IDs`() {
        val player1 = Player("Player 1")
        val player2 = Player("Player 2")
        
        assertNotEquals(player1.id, player2.id)
    }

    @Test
    fun `Score stores scoreOrder correctly`() {
        val scoreMap = hashMapOf("p1" to 10, "p2" to 20)
        val score = Score(1, scoreMap)
        
        assertEquals(1, score.scoreOrder)
        assertEquals(2, score.scoreMap.size)
    }

    @Test
    fun `Score scoreMap can be modified`() {
        val scoreMap = hashMapOf("p1" to 10)
        val score = Score(1, scoreMap)
        
        score.scoreMap["p2"] = 20
        
        assertEquals(2, score.scoreMap.size)
        assertEquals(20, score.scoreMap["p2"])
    }

    @Test
    fun `SingleScore stores player score correctly`() {
        val singleScore = SingleScore("player-id", 42)
        
        assertEquals("player-id", singleScore.playerId)
        assertEquals(42, singleScore.score)
    }

    @Test
    fun `SingleScore can store negative scores`() {
        val singleScore = SingleScore("player-id", -25)
        
        assertEquals(-25, singleScore.score)
    }

    @Test
    fun `Game score list can be modified`() {
        val game = Game()
        
        game.score.add(Score(1, hashMapOf()))
        game.score.add(Score(2, hashMapOf()))
        
        assertEquals(2, game.score.size)
    }

    @Test
    fun `Game playerList can contain multiple players`() {
        val players = listOf(
            Player("Alice"),
            Player("Bob"),
            Player("Charlie"),
            Player("Dave")
        )
        val game = Game(playerList = players)
        
        assertEquals(4, game.playerList.size)
        assertEquals("Alice", game.playerList[0].name)
        assertEquals("Dave", game.playerList[3].name)
    }
}

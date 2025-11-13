package com.senerunosoft.puantablosu.service

import com.senerunosoft.puantablosu.model.Game
import com.senerunosoft.puantablosu.model.Player
import com.senerunosoft.puantablosu.model.SingleScore
import com.senerunosoft.puantablosu.model.config.OkeyConfig
import com.senerunosoft.puantablosu.model.config.YuzBirOkeyConfig
import com.senerunosoft.puantablosu.model.enums.GameType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for GameService
 * Tests core game logic: game creation, player management, scoring, serialization
 */
class GameServiceTest {

    private lateinit var gameService: GameService

    @Before
    fun setUp() {
        gameService = GameService()
    }

    // ==================== Game Creation Tests ====================

    @Test
    fun `createGame with valid title creates game successfully`() {
        val game = gameService.createGame("Test Game", GameType.GenelOyun, null)
        
        assertNotNull(game)
        assertEquals("Test Game", game?.gameTitle)
        assertEquals(GameType.GenelOyun, game?.gameType)
        assertTrue(game?.playerList?.isEmpty() == true)
        assertTrue(game?.score?.isEmpty() == true)
    }

    @Test
    fun `createGame with blank title returns null`() {
        val game = gameService.createGame("   ", GameType.GenelOyun, null)
        
        assertNull(game)
    }

    @Test
    fun `createGame with empty title returns null`() {
        val game = gameService.createGame("", GameType.GenelOyun, null)
        
        assertNull(game)
    }

    @Test
    fun `createGame trims whitespace from title`() {
        val game = gameService.createGame("  Test Game  ", GameType.GenelOyun, null)
        
        assertNotNull(game)
        assertEquals("Test Game", game?.gameTitle)
    }

    @Test
    fun `createGame with YuzBirOkey type includes config`() {
        val config = YuzBirOkeyConfig()
        val game = gameService.createGame("101 Okey", GameType.YuzBirOkey, config)
        
        assertNotNull(game)
        assertEquals(GameType.YuzBirOkey, game?.gameType)
        assertEquals(config, game?.config)
    }

    @Test
    fun `createGame with Okey type includes config`() {
        val config = OkeyConfig()
        val game = gameService.createGame("Okey", GameType.Okey, config)
        
        assertNotNull(game)
        assertEquals(GameType.Okey, game?.gameType)
        assertEquals(config, game?.config)
    }

    // ==================== Player Management Tests ====================

    @Test
    fun `addPlayer with valid name adds player successfully`() {
        val game = gameService.createGame("Test Game", GameType.GenelOyun, null)
        
        gameService.addPlayer(game, "Player 1")
        
        assertEquals(1, game?.playerList?.size)
        assertEquals("Player 1", game?.playerList?.get(0)?.name)
    }

    @Test
    fun `addPlayer with blank name does not add player`() {
        val game = gameService.createGame("Test Game", GameType.GenelOyun, null)
        
        gameService.addPlayer(game, "   ")
        
        assertEquals(0, game?.playerList?.size)
    }

    @Test
    fun `addPlayer trims whitespace from name`() {
        val game = gameService.createGame("Test Game", GameType.GenelOyun, null)
        
        gameService.addPlayer(game, "  Player 1  ")
        
        assertEquals("Player 1", game?.playerList?.get(0)?.name)
    }

    @Test
    fun `addPlayer adds multiple players`() {
        val game = gameService.createGame("Test Game", GameType.GenelOyun, null)
        
        gameService.addPlayer(game, "Player 1")
        gameService.addPlayer(game, "Player 2")
        gameService.addPlayer(game, "Player 3")
        
        assertEquals(3, game?.playerList?.size)
    }

    // ==================== Score Management Tests ====================

    @Test
    fun `addScore with matching player count returns true`() {
        val game = createGameWithPlayers(2)
        val scores = listOf(
            SingleScore(game.playerList[0].id, 10),
            SingleScore(game.playerList[1].id, 20)
        )
        
        val result = gameService.addScore(game, scores)
        
        assertTrue(result)
        assertEquals(1, game.score.size)
    }

    @Test
    fun `addScore with mismatched player count returns false`() {
        val game = createGameWithPlayers(2)
        val scores = listOf(
            SingleScore(game.playerList[0].id, 10)
        )
        
        val result = gameService.addScore(game, scores)
        
        assertFalse(result)
        assertEquals(0, game.score.size)
    }

    @Test
    fun `addScore increments round number correctly`() {
        val game = createGameWithPlayers(2)
        val player1Id = game.playerList[0].id
        val player2Id = game.playerList[1].id
        
        gameService.addScore(game, listOf(SingleScore(player1Id, 10), SingleScore(player2Id, 20)))
        gameService.addScore(game, listOf(SingleScore(player1Id, 15), SingleScore(player2Id, 25)))
        
        assertEquals(2, game.score.size)
        assertEquals(1, game.score[0].scoreOrder)
        assertEquals(2, game.score[1].scoreOrder)
    }

    @Test
    fun `addScore for YuzBirOkey validates scores within range`() {
        val config = YuzBirOkeyConfig()
        val game = createGameWithPlayers(2, GameType.YuzBirOkey, config)
        val player1Id = game.playerList[0].id
        val player2Id = game.playerList[1].id
        
        // Valid scores
        val validScores = listOf(SingleScore(player1Id, 50), SingleScore(player2Id, 75))
        assertTrue(gameService.addScore(game, validScores))
        
        // Invalid scores (out of range)
        val invalidScores = listOf(SingleScore(player1Id, 2000), SingleScore(player2Id, -100))
        assertFalse(gameService.addScore(game, invalidScores))
    }

    @Test
    fun `addScore for Okey allows negative scores`() {
        val config = OkeyConfig()
        val game = createGameWithPlayers(2, GameType.Okey, config)
        val player1Id = game.playerList[0].id
        val player2Id = game.playerList[1].id
        
        val scores = listOf(SingleScore(player1Id, -50), SingleScore(player2Id, 75))
        
        assertTrue(gameService.addScore(game, scores))
    }

    // ==================== Score Calculation Tests ====================

    @Test
    fun `getCalculatedScore returns correct total scores`() {
        val game = createGameWithPlayers(2)
        val player1Id = game.playerList[0].id
        val player2Id = game.playerList[1].id
        
        gameService.addScore(game, listOf(SingleScore(player1Id, 10), SingleScore(player2Id, 20)))
        gameService.addScore(game, listOf(SingleScore(player1Id, 15), SingleScore(player2Id, 25)))
        
        val calculatedScores = gameService.getCalculatedScore(game)
        
        assertEquals(2, calculatedScores.size)
        assertEquals(25, calculatedScores.find { it.playerId == player1Id }?.score)
        assertEquals(45, calculatedScores.find { it.playerId == player2Id }?.score)
    }

    @Test
    fun `getCalculatedScore with no scores returns zero`() {
        val game = createGameWithPlayers(2)
        
        val calculatedScores = gameService.getCalculatedScore(game)
        
        assertEquals(2, calculatedScores.size)
        assertTrue(calculatedScores.all { it.score == 0 })
    }

    @Test
    fun `getPlayerRoundScore returns correct score for specific round`() {
        val game = createGameWithPlayers(2)
        val player1Id = game.playerList[0].id
        val player2Id = game.playerList[1].id
        
        gameService.addScore(game, listOf(SingleScore(player1Id, 10), SingleScore(player2Id, 20)))
        gameService.addScore(game, listOf(SingleScore(player1Id, 15), SingleScore(player2Id, 25)))
        
        assertEquals(10, gameService.getPlayerRoundScore(game, player1Id, 1))
        assertEquals(25, gameService.getPlayerRoundScore(game, player2Id, 2))
    }

    @Test
    fun `getPlayerRoundScore with invalid round returns zero`() {
        val game = createGameWithPlayers(2)
        val player1Id = game.playerList[0].id
        
        assertEquals(0, gameService.getPlayerRoundScore(game, player1Id, 0))
        assertEquals(0, gameService.getPlayerRoundScore(game, player1Id, 10))
    }

    // ==================== Serialization Tests ====================

    @Test
    fun `serializeGame with valid game returns JSON string`() {
        val game = createGameWithPlayers(2)
        
        val json = gameService.serializeGame(game)
        
        assertNotNull(json)
        assertTrue(json!!.contains("gameTitle"))
        assertTrue(json.contains("playerList"))
    }

    @Test
    fun `serializeGame with null game returns null`() {
        val json = gameService.serializeGame(null)
        
        assertNull(json)
    }

    @Test
    fun `deserializeGame with valid JSON returns game`() {
        val originalGame = createGameWithPlayers(2)
        gameService.addScore(originalGame, listOf(
            SingleScore(originalGame.playerList[0].id, 10),
            SingleScore(originalGame.playerList[1].id, 20)
        ))
        
        val json = gameService.serializeGame(originalGame)
        val deserializedGame = gameService.deserializeGame(json!!)
        
        assertNotNull(deserializedGame)
        assertEquals(originalGame.gameTitle, deserializedGame?.gameTitle)
        assertEquals(originalGame.playerList.size, deserializedGame?.playerList?.size)
        assertEquals(originalGame.score.size, deserializedGame?.score?.size)
    }

    @Test
    fun `deserializeGame with blank JSON returns null`() {
        val game = gameService.deserializeGame("   ")
        
        assertNull(game)
    }

    @Test
    fun `deserializeGame with invalid JSON returns null`() {
        val game = gameService.deserializeGame("invalid json")
        
        assertNull(game)
    }

    @Test
    fun `serialize and deserialize game with YuzBirOkey config preserves config`() {
        val config = YuzBirOkeyConfig()
        val game = gameService.createGame("101 Okey", GameType.YuzBirOkey, config)
        gameService.addPlayer(game, "Player 1")
        
        val json = gameService.serializeGame(game)
        val deserializedGame = gameService.deserializeGame(json!!)
        
        assertNotNull(deserializedGame)
        assertEquals(GameType.YuzBirOkey, deserializedGame?.gameType)
        assertNotNull(deserializedGame?.config)
        assertTrue(deserializedGame?.config is YuzBirOkeyConfig)
    }

    // ==================== Helper Methods ====================

    private fun createGameWithPlayers(
        playerCount: Int,
        gameType: GameType = GameType.GenelOyun,
        config: com.senerunosoft.puantablosu.model.config.IConfig? = null
    ): Game {
        val game = gameService.createGame("Test Game", gameType, config)!!
        for (i in 1..playerCount) {
            gameService.addPlayer(game, "Player $i")
        }
        return game
    }
}

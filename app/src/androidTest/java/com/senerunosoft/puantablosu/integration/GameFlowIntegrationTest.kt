package com.senerunosoft.puantablosu.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.senerunosoft.puantablosu.model.Game
import com.senerunosoft.puantablosu.model.Player
import com.senerunosoft.puantablosu.model.SingleScore
import com.senerunosoft.puantablosu.model.enums.GameType
import com.senerunosoft.puantablosu.service.GameService
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for complete game flow
 * Tests end-to-end scenarios: create game → add players → add scores → serialize → deserialize
 */
@RunWith(AndroidJUnit4::class)
class GameFlowIntegrationTest {

    private lateinit var gameService: GameService

    @Before
    fun setUp() {
        gameService = GameService()
    }

    @Test
    fun completeGameFlow_createToFinish_worksCorrectly() {
        // Step 1: Create a new game
        val game = gameService.createGame("Integration Test Game", GameType.GenelOyun, null)
        assertNotNull(game)
        assertEquals("Integration Test Game", game?.gameTitle)

        // Step 2: Add players
        gameService.addPlayer(game, "Alice")
        gameService.addPlayer(game, "Bob")
        gameService.addPlayer(game, "Charlie")
        assertEquals(3, game?.playerList?.size)

        // Step 3: Play multiple rounds
        val aliceId = game?.playerList?.get(0)?.id!!
        val bobId = game?.playerList?.get(1)?.id!!
        val charlieId = game?.playerList?.get(2)?.id!!

        // Round 1
        val round1Scores = listOf(
            SingleScore(aliceId, 10),
            SingleScore(bobId, 15),
            SingleScore(charlieId, 20)
        )
        assertTrue(gameService.addScore(game, round1Scores))

        // Round 2
        val round2Scores = listOf(
            SingleScore(aliceId, 25),
            SingleScore(bobId, 30),
            SingleScore(charlieId, 5)
        )
        assertTrue(gameService.addScore(game, round2Scores))

        // Round 3
        val round3Scores = listOf(
            SingleScore(aliceId, 40),
            SingleScore(bobId, 10),
            SingleScore(charlieId, 35)
        )
        assertTrue(gameService.addScore(game, round3Scores))

        // Step 4: Verify scores
        assertEquals(3, game.score.size)

        // Step 5: Calculate total scores
        val calculatedScores = gameService.getCalculatedScore(game)
        assertEquals(3, calculatedScores.size)

        // Verify totals: Alice=75, Bob=55, Charlie=60
        val aliceTotal = calculatedScores.find { it.playerId == aliceId }?.score
        val bobTotal = calculatedScores.find { it.playerId == bobId }?.score
        val charlieTotal = calculatedScores.find { it.playerId == charlieId }?.score

        assertEquals(75, aliceTotal)
        assertEquals(55, bobTotal)
        assertEquals(60, charlieTotal)

        // Step 6: Serialize game
        val serialized = gameService.serializeGame(game)
        assertNotNull(serialized)
        assertTrue(serialized!!.contains("Integration Test Game"))

        // Step 7: Deserialize and verify
        val deserializedGame = gameService.deserializeGame(serialized)
        assertNotNull(deserializedGame)
        assertEquals(game.gameTitle, deserializedGame?.gameTitle)
        assertEquals(game.playerList.size, deserializedGame?.playerList?.size)
        assertEquals(game.score.size, deserializedGame?.score?.size)

        // Verify deserialized scores match original
        val deserializedCalculated = gameService.getCalculatedScore(deserializedGame!!)
        assertEquals(calculatedScores.size, deserializedCalculated.size)
    }

    @Test
    fun gameFlow_withYuzBirOkey_respectsGameRules() {
        // Create 101 Okey game
        val config = com.senerunosoft.puantablosu.model.config.YuzBirOkeyConfig()
        val game = gameService.createGame("101 Okey Game", GameType.YuzBirOkey, config)
        assertNotNull(game)

        // Add players
        gameService.addPlayer(game, "Player 1")
        gameService.addPlayer(game, "Player 2")

        val p1Id = game?.playerList?.get(0)?.id!!
        val p2Id = game?.playerList?.get(1)?.id!!

        // Add valid scores (within range for 101 Okey)
        val validScores = listOf(
            SingleScore(p1Id, 50),
            SingleScore(p2Id, 75)
        )
        assertTrue(gameService.addScore(game, validScores))

        // Try to add invalid scores (out of range)
        val invalidScores = listOf(
            SingleScore(p1Id, 2000),
            SingleScore(p2Id, -100)
        )
        assertFalse(gameService.addScore(game, invalidScores))

        // Verify only valid round was added
        assertEquals(1, game.score.size)
    }

    @Test
    fun gameFlow_serializationPreservesAllData() {
        // Create complex game
        val game = gameService.createGame("Complex Game", GameType.Okey, null)
        gameService.addPlayer(game, "Player A")
        gameService.addPlayer(game, "Player B")

        val pAId = game?.playerList?.get(0)?.id!!
        val pBId = game?.playerList?.get(1)?.id!!

        // Add multiple rounds with various scores
        gameService.addScore(game, listOf(SingleScore(pAId, -10), SingleScore(pBId, 20)))
        gameService.addScore(game, listOf(SingleScore(pAId, 50), SingleScore(pBId, -5)))
        gameService.addScore(game, listOf(SingleScore(pAId, 0), SingleScore(pBId, 100)))

        // Serialize
        val json = gameService.serializeGame(game)
        assertNotNull(json)

        // Deserialize
        val restoredGame = gameService.deserializeGame(json!!)
        assertNotNull(restoredGame)

        // Verify all data preserved
        assertEquals(game.gameTitle, restoredGame?.gameTitle)
        assertEquals(game.gameType, restoredGame?.gameType)
        assertEquals(game.playerList.size, restoredGame?.playerList?.size)
        assertEquals(game.score.size, restoredGame?.score?.size)

        // Verify specific round data
        assertEquals(
            game.score[0].scoreMap[pAId],
            restoredGame?.score?.get(0)?.scoreMap?.get(pAId)
        )
        assertEquals(
            game.score[2].scoreMap[pBId],
            restoredGame?.score?.get(2)?.scoreMap?.get(pBId)
        )
    }

    @Test
    fun gameFlow_multipleGamesIndependently() {
        // Create first game
        val game1 = gameService.createGame("Game 1", GameType.GenelOyun, null)
        gameService.addPlayer(game1, "G1P1")
        gameService.addPlayer(game1, "G1P2")

        // Create second game
        val game2 = gameService.createGame("Game 2", GameType.YuzBirOkey, null)
        gameService.addPlayer(game2, "G2P1")
        gameService.addPlayer(game2, "G2P2")

        // Add scores to both games
        val g1p1 = game1?.playerList?.get(0)?.id!!
        val g1p2 = game1.playerList[1].id
        val g2p1 = game2?.playerList?.get(0)?.id!!
        val g2p2 = game2.playerList[1].id

        gameService.addScore(game1, listOf(SingleScore(g1p1, 10), SingleScore(g1p2, 20)))
        gameService.addScore(game2, listOf(SingleScore(g2p1, 30), SingleScore(g2p2, 40)))

        // Verify games are independent
        assertNotEquals(game1.gameId, game2.gameId)
        assertEquals(1, game1.score.size)
        assertEquals(1, game2.score.size)
        assertEquals(10, game1.score[0].scoreMap[g1p1])
        assertEquals(30, game2.score[0].scoreMap[g2p1])
    }
}

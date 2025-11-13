package com.senerunosoft.puantablosu.integration

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import com.senerunosoft.puantablosu.model.Game
import com.senerunosoft.puantablosu.model.SingleScore
import com.senerunosoft.puantablosu.model.enums.GameType
import com.senerunosoft.puantablosu.service.GameService
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for data persistence
 * Tests SharedPreferences storage, retrieval, and data integrity
 */
@RunWith(AndroidJUnit4::class)
class DataPersistenceIntegrationTest {

    private lateinit var context: Context
    private lateinit var gameService: GameService
    private val testPrefsName = "test_game_prefs"

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        gameService = GameService()
        // Clear test preferences
        context.getSharedPreferences(testPrefsName, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    @After
    fun tearDown() {
        // Clean up test preferences
        context.getSharedPreferences(testPrefsName, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    @Test
    fun persistGame_saveAndReload_preservesData() {
        val prefs = context.getSharedPreferences(testPrefsName, Context.MODE_PRIVATE)

        // Create and setup game
        val game = gameService.createGame("Persistence Test", GameType.GenelOyun, null)
        gameService.addPlayer(game, "Player 1")
        gameService.addPlayer(game, "Player 2")

        val p1Id = game?.playerList?.get(0)?.id!!
        val p2Id = game?.playerList?.get(1)?.id!!

        gameService.addScore(game, listOf(
            SingleScore(p1Id, 10),
            SingleScore(p2Id, 20)
        ))

        // Serialize and save
        val json = gameService.serializeGame(game)
        prefs.edit().putString("current_game", json).apply()

        // Reload from SharedPreferences
        val loadedJson = prefs.getString("current_game", null)
        assertNotNull(loadedJson)

        val loadedGame = gameService.deserializeGame(loadedJson!!)
        assertNotNull(loadedGame)

        // Verify data integrity
        assertEquals(game.gameTitle, loadedGame?.gameTitle)
        assertEquals(game.playerList.size, loadedGame?.playerList?.size)
        assertEquals(game.score.size, loadedGame?.score?.size)
    }

    @Test
    fun persistMultipleGames_saveList_preservesAll() {
        val prefs = context.getSharedPreferences(testPrefsName, Context.MODE_PRIVATE)

        // Create multiple games
        val game1 = gameService.createGame("Game 1", GameType.GenelOyun, null)
        gameService.addPlayer(game1, "P1")
        gameService.addPlayer(game1, "P2")

        val game2 = gameService.createGame("Game 2", GameType.Okey, null)
        gameService.addPlayer(game2, "P3")
        gameService.addPlayer(game2, "P4")

        val game3 = gameService.createGame("Game 3", GameType.YuzBirOkey, null)
        gameService.addPlayer(game3, "P5")
        gameService.addPlayer(game3, "P6")

        // Serialize game list
        val gameList = listOf(game1, game2, game3)
        val gson = Gson()
        val json = gson.toJson(gameList)

        // Save to SharedPreferences
        prefs.edit().putString("game_list", json).apply()

        // Reload
        val loadedJson = prefs.getString("game_list", null)
        assertNotNull(loadedJson)

        // Deserialize (simplified - normally would use proper type adapter)
        assertNotNull(loadedJson)
        assertTrue(loadedJson!!.contains("Game 1"))
        assertTrue(loadedJson.contains("Game 2"))
        assertTrue(loadedJson.contains("Game 3"))
    }

    @Test
    fun persistGame_withComplexScores_maintainsAccuracy() {
        val prefs = context.getSharedPreferences(testPrefsName, Context.MODE_PRIVATE)

        // Create game with multiple rounds and various scores
        val game = gameService.createGame("Complex Score Game", GameType.GenelOyun, null)
        gameService.addPlayer(game, "Alice")
        gameService.addPlayer(game, "Bob")
        gameService.addPlayer(game, "Charlie")

        val aliceId = game?.playerList?.get(0)?.id!!
        val bobId = game?.playerList?.get(1)?.id!!
        val charlieId = game?.playerList?.get(2)?.id!!

        // Add 5 rounds with different score patterns
        val rounds = listOf(
            listOf(SingleScore(aliceId, 10), SingleScore(bobId, 20), SingleScore(charlieId, 30)),
            listOf(SingleScore(aliceId, -5), SingleScore(bobId, 15), SingleScore(charlieId, 25)),
            listOf(SingleScore(aliceId, 100), SingleScore(bobId, 0), SingleScore(charlieId, 50)),
            listOf(SingleScore(aliceId, 3), SingleScore(bobId, 7), SingleScore(charlieId, 11)),
            listOf(SingleScore(aliceId, 42), SingleScore(bobId, 73), SingleScore(charlieId, 99))
        )

        rounds.forEach { round ->
            gameService.addScore(game, round)
        }

        // Calculate original totals
        val originalScores = gameService.getCalculatedScore(game)

        // Serialize and save
        val json = gameService.serializeGame(game)
        prefs.edit().putString("complex_game", json).apply()

        // Reload
        val loadedJson = prefs.getString("complex_game", null)
        val loadedGame = gameService.deserializeGame(loadedJson!!)
        assertNotNull(loadedGame)

        // Calculate loaded totals
        val loadedScores = gameService.getCalculatedScore(loadedGame!!)

        // Verify scores match exactly
        assertEquals(originalScores.size, loadedScores.size)
        originalScores.forEachIndexed { index, originalScore ->
            val loadedScore = loadedScores.find { it.playerId == originalScore.playerId }
            assertNotNull(loadedScore)
            assertEquals(originalScore.score, loadedScore?.score)
        }
    }

    @Test
    fun persistGame_emptyGame_handlesCorrectly() {
        val prefs = context.getSharedPreferences(testPrefsName, Context.MODE_PRIVATE)

        // Create empty game (no players, no scores)
        val game = gameService.createGame("Empty Game", GameType.GenelOyun, null)

        // Serialize and save
        val json = gameService.serializeGame(game)
        prefs.edit().putString("empty_game", json).apply()

        // Reload
        val loadedJson = prefs.getString("empty_game", null)
        val loadedGame = gameService.deserializeGame(loadedJson!!)

        assertNotNull(loadedGame)
        assertEquals("Empty Game", loadedGame?.gameTitle)
        assertTrue(loadedGame?.playerList?.isEmpty() == true)
        assertTrue(loadedGame?.score?.isEmpty() == true)
    }

    @Test
    fun persistGame_largeNumberOfRounds_performsWell() {
        val prefs = context.getSharedPreferences(testPrefsName, Context.MODE_PRIVATE)

        // Create game with many rounds
        val game = gameService.createGame("Long Game", GameType.GenelOyun, null)
        gameService.addPlayer(game, "Player 1")
        gameService.addPlayer(game, "Player 2")

        val p1Id = game?.playerList?.get(0)?.id!!
        val p2Id = game?.playerList?.get(1)?.id!!

        // Add 50 rounds
        repeat(50) { round ->
            gameService.addScore(game, listOf(
                SingleScore(p1Id, round),
                SingleScore(p2Id, round * 2)
            ))
        }

        assertEquals(50, game.score.size)

        // Measure serialization time
        val startTime = System.currentTimeMillis()
        val json = gameService.serializeGame(game)
        val serializeTime = System.currentTimeMillis() - startTime

        prefs.edit().putString("long_game", json).apply()

        // Measure deserialization time
        val loadedJson = prefs.getString("long_game", null)
        val deserializeStart = System.currentTimeMillis()
        val loadedGame = gameService.deserializeGame(loadedJson!!)
        val deserializeTime = System.currentTimeMillis() - deserializeStart

        assertNotNull(loadedGame)
        assertEquals(50, loadedGame?.score?.size)

        // Performance assertions (should complete in reasonable time)
        assertTrue("Serialization took too long: ${serializeTime}ms", serializeTime < 1000)
        assertTrue("Deserialization took too long: ${deserializeTime}ms", deserializeTime < 1000)
    }

    @Test
    fun persistGame_specialCharacters_handlesCorrectly() {
        val prefs = context.getSharedPreferences(testPrefsName, Context.MODE_PRIVATE)

        // Create game with special characters in names
        val game = gameService.createGame("Özel Karakterler & Testı 123!", GameType.GenelOyun, null)
        gameService.addPlayer(game, "Åsa Müller")
        gameService.addPlayer(game, "José García")
        gameService.addPlayer(game, "李明 (Li Ming)")

        // Serialize and save
        val json = gameService.serializeGame(game)
        prefs.edit().putString("special_game", json).apply()

        // Reload
        val loadedJson = prefs.getString("special_game", null)
        val loadedGame = gameService.deserializeGame(loadedJson!!)

        assertNotNull(loadedGame)
        assertEquals("Özel Karakterler & Testı 123!", loadedGame?.gameTitle)
        assertEquals("Åsa Müller", loadedGame?.playerList?.get(0)?.name)
        assertEquals("José García", loadedGame?.playerList?.get(1)?.name)
        assertEquals("李明 (Li Ming)", loadedGame?.playerList?.get(2)?.name)
    }
}

package com.senerunosoft.puantablosu.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.senerunosoft.puantablosu.IGameService
import com.senerunosoft.puantablosu.data.repository.GamesRepository
import com.senerunosoft.puantablosu.model.Game
import com.senerunosoft.puantablosu.model.Player
import com.senerunosoft.puantablosu.model.Score
import com.senerunosoft.puantablosu.model.SingleScore
import com.senerunosoft.puantablosu.model.config.YuzBirOkeyConfig
import com.senerunosoft.puantablosu.model.enums.GameType
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for GameViewModel
 * Tests ViewModel behavior, state management, and business logic integration
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: GameViewModel
    private lateinit var mockGameService: IGameService
    private lateinit var mockRepository: GamesRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        
        mockGameService = mockk(relaxed = true)
        mockRepository = mockk(relaxed = true)
        
        // Setup default repository behavior
        coEvery { mockRepository.initialize() } just Runs
        every { mockRepository.getCurrentGame() } returns flowOf(null)
        every { mockRepository.getAllGames() } returns flowOf(emptyList())
        
        viewModel = GameViewModel(mockGameService, mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    // ==================== Game Creation Tests ====================

    @Test
    fun `createGame with valid data creates game successfully`() {
        val expectedGame = createTestGame()
        every { mockGameService.createGame(any(), any(), any()) } returns expectedGame
        
        val result = viewModel.createGame("Test Game", GameType.GenelOyun, null)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertNotNull(result)
        assertEquals("Test Game", result?.gameTitle)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.errorMessage.value)
        verify { mockGameService.createGame("Test Game", GameType.GenelOyun, null) }
    }

    @Test
    fun `createGame sets loading state correctly`() {
        val game = createTestGame()
        every { mockGameService.createGame(any(), any(), any()) } returns game
        
        viewModel.createGame("Test Game", GameType.GenelOyun, null)
        
        // Loading should be false after operation completes
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `createGame with service failure sets error message`() {
        every { mockGameService.createGame(any(), any(), any()) } returns null
        
        val result = viewModel.createGame("Test Game", GameType.GenelOyun, null)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertNull(result)
        assertEquals("Failed to create game", viewModel.errorMessage.value)
    }

    @Test
    fun `createGame with exception sets error message`() {
        every { mockGameService.createGame(any(), any(), any()) } throws RuntimeException("Test exception")
        
        val result = viewModel.createGame("Test Game", GameType.GenelOyun, null)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertNull(result)
        assertTrue(viewModel.errorMessage.value?.contains("Error creating game") == true)
    }

    @Test
    fun `createGame updates repository with new game`() {
        val game = createTestGame()
        every { mockGameService.createGame(any(), any(), any()) } returns game
        coEvery { mockRepository.setCurrentGame(any()) } just Runs
        
        viewModel.createGame("Test Game", GameType.GenelOyun, null)
        testDispatcher.scheduler.advanceUntilIdle()
        
        coVerify { mockRepository.setCurrentGame(game) }
    }

    // ==================== Game Info Management Tests ====================

    @Test
    fun `setGameInfo updates gameInfo state`() {
        val game = createTestGame()
        
        viewModel.setGameInfo(game)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals(game, viewModel.gameInfo.value)
    }

    @Test
    fun `setGameInfo updates repository`() {
        val game = createTestGame()
        coEvery { mockRepository.setCurrentGame(any()) } just Runs
        
        viewModel.setGameInfo(game)
        testDispatcher.scheduler.advanceUntilIdle()
        
        coVerify { mockRepository.setCurrentGame(game) }
    }

    @Test
    fun `setGameInfo with null clears current game`() {
        viewModel.setGameInfo(null)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertNull(viewModel.gameInfo.value)
    }

    // ==================== Serialization Tests ====================

    @Test
    fun `serializeCurrentGame returns JSON string`() {
        val game = createTestGame()
        val expectedJson = """{"gameTitle":"Test Game"}"""
        every { mockGameService.serializeGame(any()) } returns expectedJson
        viewModel.setGameInfo(game)
        testDispatcher.scheduler.advanceUntilIdle()
        
        val result = viewModel.serializeCurrentGame()
        
        assertEquals(expectedJson, result)
    }

    @Test
    fun `serializeCurrentGame with no game returns null`() {
        every { mockGameService.serializeGame(null) } returns null
        
        val result = viewModel.serializeCurrentGame()
        
        assertNull(result)
    }

    @Test
    fun `loadGameFromString with valid JSON loads game`() {
        val game = createTestGame()
        val json = """{"gameTitle":"Test Game"}"""
        every { mockGameService.deserializeGame(json) } returns game
        coEvery { mockRepository.setCurrentGame(any()) } just Runs
        
        val result = viewModel.loadGameFromString(json)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertTrue(result)
        assertEquals(game, viewModel.gameInfo.value)
    }

    @Test
    fun `loadGameFromString with invalid JSON sets error`() {
        every { mockGameService.deserializeGame(any()) } returns null
        
        val result = viewModel.loadGameFromString("invalid")
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertFalse(result)
        assertEquals("Failed to load game", viewModel.errorMessage.value)
    }

    @Test
    fun `loadGameFromString with exception sets error`() {
        every { mockGameService.deserializeGame(any()) } throws RuntimeException("Parse error")
        
        val result = viewModel.loadGameFromString("json")
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertFalse(result)
        assertTrue(viewModel.errorMessage.value?.contains("Error loading game") == true)
    }

    // ==================== Game Type and Config Tests ====================

    @Test
    fun `setSelectedGameType updates game type`() {
        viewModel.setSelectedGameType(GameType.YuzBirOkey)
        
        assertEquals(GameType.YuzBirOkey, viewModel.selectedGameType.value)
    }

    @Test
    fun `setSelectedConfig updates config`() {
        val config = YuzBirOkeyConfig()
        
        viewModel.setSelectedConfig(config)
        
        assertEquals(config, viewModel.selectedConfig.value)
    }

    @Test
    fun `selectedRules returns rules from YuzBirOkeyConfig`() {
        val config = YuzBirOkeyConfig()
        
        viewModel.setSelectedConfig(config)
        
        assertNotNull(viewModel.selectedRules)
        assertTrue(viewModel.selectedRules!!.isNotEmpty())
    }

    @Test
    fun `selectedRules returns null when config is not YuzBirOkeyConfig`() {
        viewModel.setSelectedConfig(null)
        
        assertNull(viewModel.selectedRules)
    }

    // ==================== Error Handling Tests ====================

    @Test
    fun `clearError clears error message`() {
        every { mockGameService.createGame(any(), any(), any()) } returns null
        viewModel.createGame("Test", GameType.GenelOyun, null)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertNotNull(viewModel.errorMessage.value)
        
        viewModel.clearError()
        
        assertNull(viewModel.errorMessage.value)
    }

    // ==================== Repository Integration Tests ====================

    @Test
    fun `saveGame calls repository saveGame`() {
        val game = createTestGame()
        coEvery { mockRepository.saveGame(any()) } returns Result.success(Unit)
        
        viewModel.saveGame(game)
        testDispatcher.scheduler.advanceUntilIdle()
        
        coVerify { mockRepository.saveGame(game) }
        assertEquals(game, viewModel.gameInfo.value)
    }

    @Test
    fun `saveGame failure sets error message`() {
        val game = createTestGame()
        val exception = Exception("Save failed")
        coEvery { mockRepository.saveGame(any()) } returns Result.failure(exception)
        
        viewModel.saveGame(game)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertTrue(viewModel.errorMessage.value?.contains("Failed to save game") == true)
    }

    @Test
    fun `deleteGame calls repository deleteGame`() {
        val gameId = "test-id"
        coEvery { mockRepository.deleteGame(gameId) } returns Result.success(Unit)
        
        viewModel.deleteGame(gameId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        coVerify { mockRepository.deleteGame(gameId) }
    }

    @Test
    fun `deleteGame failure sets error message`() {
        val gameId = "test-id"
        val exception = Exception("Delete failed")
        coEvery { mockRepository.deleteGame(gameId) } returns Result.failure(exception)
        
        viewModel.deleteGame(gameId)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertTrue(viewModel.errorMessage.value?.contains("Failed to delete game") == true)
    }

    // ==================== State Flow Tests ====================

    @Test
    fun `gameInfo state flow emits updates`() {
        val game = createTestGame()
        
        viewModel.setGameInfo(game)
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals(game, viewModel.gameInfo.value)
    }

    @Test
    fun `isLoading state starts as false`() {
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `errorMessage state starts as null`() {
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `selectedGameType state starts as null`() {
        assertNull(viewModel.selectedGameType.value)
    }

    // ==================== Helper Methods ====================

    private fun createTestGame(): Game {
        val players = listOf(
            Player("Player 1"),
            Player("Player 2")
        )
        return Game(
            gameId = "test-id",
            gameTitle = "Test Game",
            playerList = players,
            score = mutableListOf(),
            gameType = GameType.GenelOyun,
            config = null
        )
    }
}

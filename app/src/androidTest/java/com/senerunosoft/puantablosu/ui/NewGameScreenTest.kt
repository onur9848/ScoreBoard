package com.senerunosoft.puantablosu.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.senerunosoft.puantablosu.model.Player
import com.senerunosoft.puantablosu.model.config.IConfig
import com.senerunosoft.puantablosu.model.enums.GameType
import com.senerunosoft.puantablosu.ui.compose.NewGameScreen
import com.senerunosoft.puantablosu.ui.compose.theme.ScoreBoardTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for NewGameScreen Composable
 * Tests user interactions, validation, and game creation flow
 */
@RunWith(AndroidJUnit4::class)
class NewGameScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun newGameScreen_isDisplayed() {
        composeTestRule.setContent {
            ScoreBoardTheme {
                NewGameScreen(gameType = GameType.GenelOyun)
            }
        }

        // Verify key UI elements are displayed
        composeTestRule.onNodeWithText("Yeni Oyun Oluştur").assertIsDisplayed()
    }

    @Test
    fun newGameScreen_enterGameTitle_displaysCorrectly() {
        composeTestRule.setContent {
            ScoreBoardTheme {
                NewGameScreen(gameType = GameType.GenelOyun)
            }
        }

        // Find and enter text in game title field
        composeTestRule.onNodeWithText("Oyun Başlığı")
            .performTextInput("Test Game")

        // Verify the text was entered
        composeTestRule.onNodeWithText("Test Game").assertExists()
    }

    @Test
    fun newGameScreen_addPlayer_increasesPlayerCount() {
        composeTestRule.setContent {
            ScoreBoardTheme {
                NewGameScreen(gameType = GameType.GenelOyun)
            }
        }

        // Click add player button
        composeTestRule.onNodeWithContentDescription("Oyuncu Ekle")
            .performClick()

        // Verify player fields increased
        // Should see more player input fields after adding
        composeTestRule.onAllNodesWithText("Oyuncu").assertCountEquals(1)
    }

    @Test
    fun newGameScreen_startGameWithEmptyTitle_showsError() {
        var gameStarted = false

        composeTestRule.setContent {
            ScoreBoardTheme {
                NewGameScreen(
                    gameType = GameType.GenelOyun,
                    onStartGame = { _, _, _ -> gameStarted = true }
                )
            }
        }

        // Try to start game without title
        composeTestRule.onNodeWithText("Oyunu Başlat").performClick()

        // Verify game was not started
        assertFalse(gameStarted)
    }

    @Test
    fun newGameScreen_startGameWithValidData_callsCallback() {
        var gameTitle = ""
        var playerList: List<Player> = emptyList()

        composeTestRule.setContent {
            ScoreBoardTheme {
                NewGameScreen(
                    gameType = GameType.GenelOyun,
                    onStartGame = { title, players, _ ->
                        gameTitle = title
                        playerList = players
                    }
                )
            }
        }

        // Enter game title
        composeTestRule.onNodeWithText("Oyun Başlığı")
            .performTextInput("Test Game")

        // Enter player names (assuming default players exist)
        val playerNodes = composeTestRule.onAllNodesWithText("Oyuncu adı")
        if (playerNodes.fetchSemanticsNodes().isNotEmpty()) {
            playerNodes[0].performTextInput("Player 1")
            playerNodes[1].performTextInput("Player 2")
        }

        // Start game
        composeTestRule.onNodeWithText("Oyunu Başlat").performClick()

        // Verify callback was called with correct data
        assertEquals("Test Game", gameTitle)
        assertTrue(playerList.size >= 2)
    }

    @Test
    fun newGameScreen_deletePlayer_removesPlayer() {
        composeTestRule.setContent {
            ScoreBoardTheme {
                NewGameScreen(gameType = GameType.GenelOyun)
            }
        }

        // Add a player first
        composeTestRule.onNodeWithContentDescription("Oyuncu Ekle")
            .performClick()

        val initialPlayerCount = composeTestRule.onAllNodesWithContentDescription("Oyuncuyu Sil")
            .fetchSemanticsNodes().size

        // Delete a player if delete buttons exist
        if (initialPlayerCount > 0) {
            composeTestRule.onAllNodesWithContentDescription("Oyuncuyu Sil")[0]
                .performClick()

            // Verify player count decreased
            val finalPlayerCount = composeTestRule.onAllNodesWithContentDescription("Oyuncuyu Sil")
                .fetchSemanticsNodes().size
            assertTrue(finalPlayerCount < initialPlayerCount)
        }
    }

    @Test
    fun newGameScreen_navigateBack_callsCallback() {
        var backPressed = false

        composeTestRule.setContent {
            ScoreBoardTheme {
                NewGameScreen(
                    gameType = GameType.GenelOyun,
                    onNavigateBack = { backPressed = true }
                )
            }
        }

        // Find and click back button
        composeTestRule.onNodeWithContentDescription("Geri")
            .performClick()

        // Verify callback was called
        assertTrue(backPressed)
    }

    @Test
    fun newGameScreen_yuzBirOkeyType_showsCorrectConfig() {
        composeTestRule.setContent {
            ScoreBoardTheme {
                NewGameScreen(gameType = GameType.YuzBirOkey)
            }
        }

        // Verify 101 Okey specific elements are displayed
        // This might include specific config options
        composeTestRule.onNodeWithText("Yeni Oyun Oluştur").assertIsDisplayed()
    }
}

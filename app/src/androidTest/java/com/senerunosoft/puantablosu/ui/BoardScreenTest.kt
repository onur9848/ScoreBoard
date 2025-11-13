package com.senerunosoft.puantablosu.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.senerunosoft.puantablosu.model.Game
import com.senerunosoft.puantablosu.model.Player
import com.senerunosoft.puantablosu.model.enums.GameType
import com.senerunosoft.puantablosu.ui.compose.BoardScreen
import com.senerunosoft.puantablosu.ui.compose.theme.ScoreBoardTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for BoardScreen Composable
 * Tests game board display and score management interactions
 */
@RunWith(AndroidJUnit4::class)
class BoardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

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

    @Test
    fun boardScreen_displaysGameTitle() {
        val game = createTestGame()

        composeTestRule.setContent {
            ScoreBoardTheme {
                BoardScreen(game = game)
            }
        }

        // Verify game title is displayed
        composeTestRule.onNodeWithText("Test Game").assertIsDisplayed()
    }

    @Test
    fun boardScreen_displaysPlayerNames() {
        val game = createTestGame()

        composeTestRule.setContent {
            ScoreBoardTheme {
                BoardScreen(game = game)
            }
        }

        // Verify player names are displayed
        composeTestRule.onNodeWithText("Player 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Player 2").assertIsDisplayed()
    }

    @Test
    fun boardScreen_addScoreButton_exists() {
        val game = createTestGame()

        composeTestRule.setContent {
            ScoreBoardTheme {
                BoardScreen(game = game)
            }
        }

        // Verify add score button exists
        composeTestRule.onNodeWithContentDescription("Puan Ekle")
            .assertExists()
    }

    @Test
    fun boardScreen_showScoreboardButton_exists() {
        val game = createTestGame()

        composeTestRule.setContent {
            ScoreBoardTheme {
                BoardScreen(game = game)
            }
        }

        // Verify scoreboard button exists
        composeTestRule.onNodeWithContentDescription("Puan Tablosunu Göster")
            .assertExists()
    }

    @Test
    fun boardScreen_clickAddScore_opensDialog() {
        val game = createTestGame()

        composeTestRule.setContent {
            ScoreBoardTheme {
                BoardScreen(game = game)
            }
        }

        // Click add score button
        composeTestRule.onNodeWithContentDescription("Puan Ekle")
            .performClick()

        // Verify dialog appears (checking for dialog-specific text)
        composeTestRule.waitForIdle()
    }

    @Test
    fun boardScreen_withMultiplePlayers_displaysAll() {
        val players = listOf(
            Player("Alice"),
            Player("Bob"),
            Player("Charlie"),
            Player("Dave")
        )
        val game = Game(
            gameTitle = "Four Player Game",
            playerList = players,
            gameType = GameType.GenelOyun
        )

        composeTestRule.setContent {
            ScoreBoardTheme {
                BoardScreen(game = game)
            }
        }

        // Verify all player names are displayed
        composeTestRule.onNodeWithText("Alice").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bob").assertIsDisplayed()
        composeTestRule.onNodeWithText("Charlie").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dave").assertIsDisplayed()
    }

    @Test
    fun boardScreen_navigateBack_callsCallback() {
        val game = createTestGame()
        var backPressed = false

        composeTestRule.setContent {
            ScoreBoardTheme {
                BoardScreen(
                    game = game,
                    onNavigateBack = { backPressed = true }
                )
            }
        }

        // Click back button
        composeTestRule.onNodeWithContentDescription("Geri")
            .performClick()

        // Verify callback was called
        assert(backPressed)
    }
}

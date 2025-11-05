package com.senerunosoft.puantablosu.ui.state

import com.senerunosoft.puantablosu.model.Game
import com.senerunosoft.puantablosu.model.SingleScore
import com.senerunosoft.puantablosu.model.config.RuleConfig

/**
 * Immutable UI State for BoardScreen
 * Follows Modern Android Architecture with single source of truth
 */
data class BoardUiState(
    val game: Game? = null,
    val calculatedScores: List<SingleScore> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showBackDialog: Boolean = false,
    val showScoreDialog: Boolean = false,
    val showRuleDialog: RuleConfig? = null,
    val selectedPlayerId: String? = null,
    val pairedRuleForInput: RuleConfig? = null,
    val pairedInputValue: String = ""
) {
    val hasError: Boolean get() = errorMessage != null
    val isGameReady: Boolean get() = game != null && !isLoading
}

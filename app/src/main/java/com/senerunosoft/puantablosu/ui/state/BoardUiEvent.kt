package com.senerunosoft.puantablosu.ui.state

import com.senerunosoft.puantablosu.model.SingleScore
import com.senerunosoft.puantablosu.model.config.RuleConfig

/**
 * UI Events for BoardScreen
 * Represents all user actions that can happen on the board screen
 */
sealed class BoardUiEvent {
    object NavigateBack : BoardUiEvent()
    object ShowAddScoreDialog : BoardUiEvent()
    object CalculateScores : BoardUiEvent()
    object DismissBackDialog : BoardUiEvent()
    object ConfirmBack : BoardUiEvent()
    object DismissScoreDialog : BoardUiEvent()
    object NavigateToScoreBoard : BoardUiEvent()
    
    data class ShowRuleDialog(val rule: RuleConfig, val pairedRule: RuleConfig?) : BoardUiEvent()
    data class DismissRuleDialog(val save: Boolean = false) : BoardUiEvent()
    data class SelectPlayer(val playerId: String) : BoardUiEvent()
    data class UpdatePairedInputValue(val value: String) : BoardUiEvent()
    data class SaveScore(val scores: List<SingleScore>) : BoardUiEvent()
    data class SaveRuleScore(
        val rule: RuleConfig,
        val pairedRule: RuleConfig?,
        val selectedPlayerId: String?,
        val pairedInputValue: String?
    ) : BoardUiEvent()
}

package com.senerunosoft.puantablosu.ui.state

import com.senerunosoft.puantablosu.model.Player
import com.senerunosoft.puantablosu.model.config.IConfig
import com.senerunosoft.puantablosu.model.enums.GameType

/**
 * Immutable UI State for NewGameScreen
 */
data class NewGameUiState(
    val gameTitle: String = "",
    val gameTitleError: Boolean = false,
    val players: List<Player> = emptyList(),
    val playerErrors: Map<Int, Boolean> = emptyMap(),
    val gameType: GameType = GameType.GenelOyun,
    val config: IConfig? = null,
    val isPaired: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showError: Boolean = false
) {
    val hasError: Boolean get() = errorMessage != null
    val canAddPlayer: Boolean get() = players.size < 6
    val isValid: Boolean get() = gameTitle.isNotEmpty() && 
        players.isNotEmpty() && 
        players.all { it.name.isNotEmpty() }
}

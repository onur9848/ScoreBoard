package com.senerunosoft.puantablosu.ui.state

import com.senerunosoft.puantablosu.model.Player
import com.senerunosoft.puantablosu.model.config.IConfig

/**
 * UI Events for NewGameScreen
 */
sealed class NewGameUiEvent {
    data class UpdateGameTitle(val title: String) : NewGameUiEvent()
    object AddPlayer : NewGameUiEvent()
    data class RemovePlayer(val index: Int) : NewGameUiEvent()
    data class UpdatePlayerName(val index: Int, val name: String) : NewGameUiEvent()
    data class UpdateConfig(val config: IConfig?) : NewGameUiEvent()
    data class StartGame(val gameTitle: String, val players: List<Player>, val config: IConfig?) : NewGameUiEvent()
    object NavigateBack : NewGameUiEvent()
    object DismissError : NewGameUiEvent()
}

package com.senerunosoft.puantablosu.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.senerunosoft.puantablosu.model.Game
import com.senerunosoft.puantablosu.IGameService
import com.senerunosoft.puantablosu.model.enums.GameType
import com.senerunosoft.puantablosu.model.config.IConfig
import com.senerunosoft.puantablosu.model.enums.RuleType

/**
 * ViewModel for managing game state in the UI.
 * Follows Dependency Inversion Principle (DIP) - depends on abstraction (IGameService).
 * Follows Single Responsibility Principle (SRP) - only handles UI state management.
 */
class GameViewModel(
    private val gameService: IGameService
) : ViewModel() {

    private val _gameInfo = MutableStateFlow<Game?>(null)
    val gameInfo: StateFlow<Game?> = _gameInfo.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _selectedGameType = MutableStateFlow<GameType?>(null)
    val selectedGameType: StateFlow<GameType?> = _selectedGameType.asStateFlow()
    fun setSelectedGameType(type: GameType?) {
        _selectedGameType.value = type
    }

    private val _selectedConfig = MutableStateFlow<IConfig?>(null)
    val selectedConfig: StateFlow<IConfig?> = _selectedConfig.asStateFlow()

    // Dinamik olarak seçili oyunun RuleConfig listesini expose et
    val selectedRules: List<com.senerunosoft.puantablosu.model.config.RuleConfig>?
        get() = (selectedConfig.value as? com.senerunosoft.puantablosu.model.config.YuzBirOkeyConfig)?.rules

    fun setGameInfo(gameInfo: Game?) {
        _gameInfo.value = gameInfo
    }
    
    fun createGame(gameTitle: String, gameType: GameType, config: IConfig?): Game? {
        _isLoading.value = true
        _errorMessage.value = null

        return try {
            val game = gameService.createGame(gameTitle, gameType, config)
            if (game != null) {
                _gameInfo.value = game
            } else {
                _errorMessage.value = "Failed to create game"
            }
            game
        } catch (e: Exception) {
            _errorMessage.value = "Error creating game: ${e.message}"
            null
        } finally {
            _isLoading.value = false
        }
    }

    fun serializeCurrentGame(): String? {
        return gameService.serializeGame(_gameInfo.value)
    }
    
    fun loadGameFromString(gameString: String): Boolean {
        _isLoading.value = true
        _errorMessage.value = null
        
        return try {
            val game = gameService.deserializeGame(gameString)
            if (game != null) {
                _gameInfo.value = game
                true
            } else {
                _errorMessage.value = "Failed to load game"
                false
            }
        } catch (e: Exception) {
            _errorMessage.value = "Error loading game: ${e.message}"
            false
        } finally {
            _isLoading.value = false
        }
    }
    
    fun setSelectedConfig(config: IConfig?) {
        _selectedConfig.value = config
    }

    fun clearError() {
        _errorMessage.value = null
    }
    /**
     * Add a score for a rule, handling PlayerPenaltyScore and FinishScore+pairedKey logic.
     * @param rule The selected rule (PlayerPenaltyScore or FinishScore)
     * @param pairedRule The paired rule (if FinishScore and pairedKey is not null)
     * @param selectedPlayerId The player who triggered the rule
     * @param pairedInputValue The value for the paired rule (from input, if needed)
     */
    fun addScoreForRule(
        rule: com.senerunosoft.puantablosu.model.config.RuleConfig,
        pairedRule: com.senerunosoft.puantablosu.model.config.RuleConfig?,
        selectedPlayerId: String?,
        pairedInputValue: String?
    ) {
        val game = _gameInfo.value ?: return
        val scoreMap = mutableMapOf<String, Int>()
        when (rule.types.first()) {
            RuleType.PlayerPenaltyScore -> {
                game.playerList.forEach { player ->
                    scoreMap[player.id] = if (player.id == selectedPlayerId) rule.value.toIntOrNull() ?: 0 else 0
                }
            }
            RuleType.FinishScore -> {
                if (pairedRule != null && selectedPlayerId != null) {
                    game.playerList.forEach { player ->
                        scoreMap[player.id] = when (player.id) {
                            selectedPlayerId -> rule.value.toIntOrNull() ?: 0
                            else -> pairedInputValue?.toIntOrNull() ?: 0
                        }
                    }
                }
            }
            else -> return
        }
        val newScore = com.senerunosoft.puantablosu.model.Score(
            scoreOrder = (game.score.lastOrNull()?.scoreOrder ?: 0) + 1,
            scoreMap = scoreMap as HashMap<String, Int>
        )
        game.score.add(newScore)
        // Trigger UI update
        _gameInfo.value = game.copy(score = game.score)
    }
}
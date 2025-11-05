package com.senerunosoft.puantablosu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senerunosoft.puantablosu.IGameService
import com.senerunosoft.puantablosu.data.repository.GamesRepository
import com.senerunosoft.puantablosu.model.Game
import com.senerunosoft.puantablosu.model.Score
import com.senerunosoft.puantablosu.model.SingleScore
import com.senerunosoft.puantablosu.model.config.IConfig
import com.senerunosoft.puantablosu.model.enums.GameType
import com.senerunosoft.puantablosu.model.enums.RuleType
import com.senerunosoft.puantablosu.ui.state.BoardUiEvent
import com.senerunosoft.puantablosu.ui.state.BoardUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Refactored ViewModel following Modern Android Architecture
 * - Uses immutable UI State
 * - Event-driven UI actions
 * - Repository pattern for data layer
 * - Unidirectional data flow
 * Follows SOLID principles:
 * - Single Responsibility: Only manages UI state
 * - Dependency Inversion: Depends on abstractions (Repository, Service)
 */
class GameViewModel(
    private val gameService: IGameService,
    private val gamesRepository: GamesRepository
) : ViewModel() {

    // Legacy state for backward compatibility
    private val _gameInfo = MutableStateFlow<Game?>(null)
    val gameInfo: StateFlow<Game?> = _gameInfo.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _selectedGameType = MutableStateFlow<GameType?>(null)
    val selectedGameType: StateFlow<GameType?> = _selectedGameType.asStateFlow()

    private val _selectedConfig = MutableStateFlow<IConfig?>(null)
    val selectedConfig: StateFlow<IConfig?> = _selectedConfig.asStateFlow()

    val selectedRules: List<com.senerunosoft.puantablosu.model.config.RuleConfig>?
        get() = (selectedConfig.value as? com.senerunosoft.puantablosu.model.config.YuzBirOkeyConfig)?.rules

    private val _allGames = MutableStateFlow<List<Game>>(emptyList())
    val allGames: StateFlow<List<Game>> = _allGames.asStateFlow()

    private val _filteredGames = MutableStateFlow<List<Game>>(emptyList())
    val filteredGames: StateFlow<List<Game>> = _filteredGames.asStateFlow()
    
    // Modern UI State for BoardScreen
    private val _boardUiState = MutableStateFlow(BoardUiState())
    val boardUiState: StateFlow<BoardUiState> = _boardUiState.asStateFlow()
    
    init {
        // Initialize repository
        viewModelScope.launch {
            gamesRepository.initialize()
            // Observe current game from repository
            gamesRepository.getCurrentGame().collect { game ->
                _gameInfo.value = game
                _boardUiState.value = _boardUiState.value.copy(game = game)
            }
        }
        
        // Observe all games from repository
        viewModelScope.launch {
            gamesRepository.getAllGames().collect { games ->
                _allGames.value = games
            }
        }
    }

    // Legacy methods for backward compatibility
    fun setGameInfo(gameInfo: Game?) {
        _gameInfo.value = gameInfo
        viewModelScope.launch {
            if (gameInfo != null) {
                gamesRepository.setCurrentGame(gameInfo)
            }
        }
    }

    fun setSelectedGameType(type: GameType?) {
        _selectedGameType.value = type
    }

    fun createGame(gameTitle: String, gameType: GameType, config: IConfig?): Game? {
        _isLoading.value = true
        _errorMessage.value = null

        return try {
            val game = gameService.createGame(gameTitle, gameType, config)
            if (game != null) {
                _gameInfo.value = game
                viewModelScope.launch {
                    gamesRepository.setCurrentGame(game)
                }
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
                viewModelScope.launch {
                    gamesRepository.setCurrentGame(game)
                }
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
    
    // Modern event handlers for BoardScreen
    fun onBoardEvent(event: BoardUiEvent) {
        when (event) {
            is BoardUiEvent.CalculateScores -> calculateScores()
            is BoardUiEvent.ShowRuleDialog -> showRuleDialog(event.rule, event.pairedRule)
            is BoardUiEvent.SelectPlayer -> selectPlayer(event.playerId)
            is BoardUiEvent.UpdatePairedInputValue -> updatePairedInputValue(event.value)
            is BoardUiEvent.SaveRuleScore -> saveRuleScore(
                event.rule, 
                event.pairedRule, 
                event.selectedPlayerId, 
                event.pairedInputValue
            )
            is BoardUiEvent.DismissRuleDialog -> dismissRuleDialog()
            BoardUiEvent.NavigateBack -> _boardUiState.value = _boardUiState.value.copy(showBackDialog = true)
            BoardUiEvent.DismissBackDialog -> _boardUiState.value = _boardUiState.value.copy(showBackDialog = false)
            BoardUiEvent.ConfirmBack -> { /* Navigation handled by UI */ }
            BoardUiEvent.DismissScoreDialog -> _boardUiState.value = _boardUiState.value.copy(showScoreDialog = false)
            BoardUiEvent.ShowAddScoreDialog -> { /* Handled by UI directly */ }
            BoardUiEvent.NavigateToScoreBoard -> _boardUiState.value = _boardUiState.value.copy(showScoreDialog = true)
            is BoardUiEvent.SaveScore -> saveScore(event.scores)
        }
    }
    
    private fun calculateScores() {
        val game = _boardUiState.value.game ?: return
        val scores = mutableListOf<SingleScore>()
        
        game.playerList.forEach { player ->
            var totalScore = 0
            game.score.forEach { roundScore ->
                totalScore += roundScore.scoreMap[player.id] ?: 0
            }
            scores.add(SingleScore(player.id, totalScore))
        }
        
        _boardUiState.value = _boardUiState.value.copy(
            calculatedScores = scores.sortedByDescending { it.score },
            showScoreDialog = true
        )
    }
    
    private fun showRuleDialog(rule: com.senerunosoft.puantablosu.model.config.RuleConfig, pairedRule: com.senerunosoft.puantablosu.model.config.RuleConfig?) {
        _boardUiState.value = _boardUiState.value.copy(
            showRuleDialog = rule,
            pairedRuleForInput = pairedRule,
            pairedInputValue = pairedRule?.value ?: ""
        )
    }
    
    private fun selectPlayer(playerId: String) {
        _boardUiState.value = _boardUiState.value.copy(selectedPlayerId = playerId)
    }
    
    private fun updatePairedInputValue(value: String) {
        _boardUiState.value = _boardUiState.value.copy(pairedInputValue = value)
    }
    
    private fun dismissRuleDialog() {
        _boardUiState.value = _boardUiState.value.copy(
            showRuleDialog = null,
            selectedPlayerId = null,
            pairedRuleForInput = null,
            pairedInputValue = ""
        )
    }
    
    private fun saveScore(scores: List<SingleScore>) {
        val game = _boardUiState.value.game ?: return
        val success = gameService.addScore(game, scores)
        if (success) {
            viewModelScope.launch {
                gamesRepository.updateGame(game)
            }
        }
    }
    
    private fun saveRuleScore(
        rule: com.senerunosoft.puantablosu.model.config.RuleConfig,
        pairedRule: com.senerunosoft.puantablosu.model.config.RuleConfig?,
        selectedPlayerId: String?,
        pairedInputValue: String?
    ) {
        val game = _boardUiState.value.game ?: return
        val scoreMap = mutableMapOf<String, Int>()
        
        when (rule.types.first()) {
            RuleType.PlayerPenaltyScore -> {
                game.playerList.forEach { player ->
                    scoreMap[player.id] = if (player.id == selectedPlayerId) {
                        rule.value.toIntOrNull() ?: 0
                    } else {
                        0
                    }
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
        
        val newScore = Score(
            scoreOrder = (game.score.lastOrNull()?.scoreOrder ?: 0) + 1,
            scoreMap = scoreMap as HashMap<String, Int>
        )
        game.score.add(newScore)
        
        viewModelScope.launch {
            gamesRepository.updateGame(game)
        }
        
        _boardUiState.value = _boardUiState.value.copy(
            game = game,
            showRuleDialog = null,
            selectedPlayerId = null,
            pairedRuleForInput = null,
            pairedInputValue = ""
        )
    }
    
    /**
     * Save current game to repository
     */
    fun saveGame(game: Game) {
        viewModelScope.launch {
            gamesRepository.saveGame(game).onSuccess {
                _gameInfo.value = game
            }.onFailure { e ->
                _errorMessage.value = "Failed to save game: ${e.message}"
            }
        }
    }
    
    /**
     * Load all games from repository
     */
    fun loadAllGames() {
        viewModelScope.launch {
            _isLoading.value = true
            (gamesRepository as? com.senerunosoft.puantablosu.data.repository.GamesRepositoryImpl)?.refreshAllGames()
            _isLoading.value = false
        }
    }
    
    /**
     * Delete a game from repository
     */
    fun deleteGame(gameId: String) {
        viewModelScope.launch {
            gamesRepository.deleteGame(gameId).onFailure { e ->
                _errorMessage.value = "Failed to delete game: ${e.message}"
            }
        }
    }
}
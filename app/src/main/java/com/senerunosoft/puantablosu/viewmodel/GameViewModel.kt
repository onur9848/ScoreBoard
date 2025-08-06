package com.senerunosoft.puantablosu.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.senerunosoft.puantablosu.model.Game
import com.senerunosoft.puantablosu.IGameService

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
    
    fun setGameInfo(gameInfo: Game?) {
        _gameInfo.value = gameInfo
    }
    
    fun createGame(gameTitle: String): Game? {
        _isLoading.value = true
        _errorMessage.value = null
        
        return try {
            val game = gameService.createGame(gameTitle)
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
    
    fun clearError() {
        _errorMessage.value = null
    }
}
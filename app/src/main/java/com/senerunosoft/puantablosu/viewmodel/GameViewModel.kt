package com.senerunosoft.puantablosu.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.senerunosoft.puantablosu.model.Game

class GameViewModel : ViewModel() {
    
    private val _gameInfo = MutableStateFlow<Game?>(null)
    val gameInfo: StateFlow<Game?> = _gameInfo.asStateFlow()
    
    fun setGameInfo(gameInfo: Game?) {
        _gameInfo.value = gameInfo
    }
}
package com.senerunosoft.puantablosu.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.senerunosoft.puantablosu.model.Game

class GameViewModel : ViewModel() {
    
    private val gameInfoMutableLiveData = MutableLiveData<Game>()
    
    fun getGameInfo(): MutableLiveData<Game> = gameInfoMutableLiveData
    
    fun setGameInfo(gameInfo: Game) {
        gameInfoMutableLiveData.value = gameInfo
    }
}
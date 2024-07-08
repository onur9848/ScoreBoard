package com.senerunosoft.puantablosu.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.senerunosoft.puantablosu.model.Game;

public class GameViewModel extends ViewModel {

    MutableLiveData<Game> gameInfoMutableLiveData;

    public GameViewModel() {
        gameInfoMutableLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<Game> getGameInfo() {
        return gameInfoMutableLiveData;
    }

    public void setGameInfo(Game gameInfo) {
        gameInfoMutableLiveData.setValue(gameInfo);
    }
}

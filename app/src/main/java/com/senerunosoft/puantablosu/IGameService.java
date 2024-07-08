package com.senerunosoft.puantablosu;

import com.senerunosoft.puantablosu.model.Game;
import com.senerunosoft.puantablosu.model.SingleScore;

import java.util.List;

public interface IGameService {

    Game createGame(String gameTitle);
    void addPlayer(Game game, String playerName);
    void addScore(Game game, List<SingleScore> scoreList);
    int getPlayerRoundScore(Game game, String playerId, int round);
}

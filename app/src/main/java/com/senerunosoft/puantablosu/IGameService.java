package com.senerunosoft.puantablosu;

import com.senerunosoft.puantablosu.model.Game;
import com.senerunosoft.puantablosu.model.SingleScore;

import java.util.List;

public interface IGameService {

    Game createGame(String gameTitle);
    void addPlayer(Game game, String playerName);
    boolean addScore(Game game, List<SingleScore> scoreList);
    int getPlayerRoundScore(Game game, String playerId, int round);
    List<SingleScore> getCalculatedScore(Game game);
    String serializeGame(Game game);
    Game deserializeGame(String gameString);
}

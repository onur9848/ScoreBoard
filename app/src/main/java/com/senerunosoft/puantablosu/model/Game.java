package com.senerunosoft.puantablosu.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Game {

    private String gameId;
    private String gameTitle;
    private List<Player> playerList;
    private List<Score> score;

    public Game() {
    }

    public Game(String gameTitle, List<Player> playerList) {
        this.gameId = UUID.randomUUID().toString();
        this.gameTitle = gameTitle;
        this.playerList = playerList;
        this.score = new ArrayList<>();
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getGameTitle() {
        return gameTitle;
    }

    public void setGameTitle(String gameTitle) {
        this.gameTitle = gameTitle;
    }

    public List<Player> getPlayerList() {
        return playerList;
    }

    public void setPlayerList(List<Player> playerList) {
        this.playerList = playerList;
    }

    public List<Score> getScore() {
        return score;
    }

    public void setScore(List<Score> score) {
        this.score = score;
    }
}

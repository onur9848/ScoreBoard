package com.senerunosoft.puantablosu.model;

public class SingleScore {
    private String playerId;
    private int score;

    public SingleScore() {
    }

    public SingleScore(String playerName, int score) {
        this.playerId = playerName;
        this.score = score;
    }

    public String playerId() {
        return playerId;
    }

    public void playerId(String playerName) {
        this.playerId = playerName;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}

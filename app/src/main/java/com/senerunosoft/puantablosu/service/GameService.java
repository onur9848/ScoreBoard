package com.senerunosoft.puantablosu.service;

import com.google.gson.Gson;
import com.senerunosoft.puantablosu.IGameService;
import com.senerunosoft.puantablosu.model.Game;
import com.senerunosoft.puantablosu.model.Player;
import com.senerunosoft.puantablosu.model.Score;
import com.senerunosoft.puantablosu.model.SingleScore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameService implements IGameService {

    @Override
    public Game createGame(String gameTitle) {
        List<Player> playerList = new ArrayList<>();
        return new Game(gameTitle, playerList);
    }

    @Override
    public void addPlayer(Game game, String playerName) {
        game.getPlayerList().add(new Player(playerName));
    }

    @Override
    public void addScore(Game game, List<SingleScore> scoreList) {
        int gamePlayerCount = game.getPlayerList().size();
        int scorePlayerCount = scoreList.size();
        if (gamePlayerCount != scorePlayerCount) {
            throw new IllegalArgumentException("Player count does not match");
        }
        HashMap<String, Integer> scoreMap = getScoreMap(scoreList);
        game.getScore().add(new Score(game.getScore().size() + 1, scoreMap));

    }

    @Override
    public int getPlayerRoundScore(Game game, String playerId, int round) {
        List<Score> scoreList = game.getScore();
        for (Score score : scoreList) {
            if (score.getScoreOrder() == round) {
                return score.getScoreMap().get(playerId);
            }
        }
        return 0;
    }

    @Override
    public List<SingleScore> getCalculatedScore(Game game) {
        List<SingleScore> calculatedScoreList = new ArrayList<>();
        List<Player> playerList = game.getPlayerList();
        List<Score> scoreList = game.getScore();
        for (Player player : playerList) {
            int totalScore = 0;
            for (Score score : scoreList) {
                totalScore += score.getScoreMap().get(player.getId());
            }
            calculatedScoreList.add(new SingleScore(player.getId(), totalScore));
        }
        return calculatedScoreList;

    }

    @Override
    public String serializeGame(Game game) {
        Gson gson = new Gson();
        return gson.toJson(game);
    }

    @Override
    public Game deserializeGame(String gameString) {
        Gson gson = new Gson();
        return gson.fromJson(gameString, Game.class);
    }

    private HashMap<String, Integer> getScoreMap(List<SingleScore> scoreList) {
        HashMap<String, Integer> scoreMap = new HashMap<>();
        for (SingleScore singleScore : scoreList) {
            scoreMap.put(singleScore.playerId(), singleScore.getScore());
        }
        return scoreMap;
    }
}

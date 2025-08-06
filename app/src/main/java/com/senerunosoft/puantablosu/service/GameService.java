package com.senerunosoft.puantablosu.service;

import android.util.Log;
import com.google.gson.Gson;
import com.senerunosoft.puantablosu.IGameService;
import com.senerunosoft.puantablosu.model.Game;
import com.senerunosoft.puantablosu.model.Player;
import com.senerunosoft.puantablosu.model.Score;
import com.senerunosoft.puantablosu.model.SingleScore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.ContentValues.TAG;

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
    public boolean addScore(Game game, List<SingleScore> scoreList) {
        if (game == null || game.getPlayerList() == null || game.getScore() == null || scoreList == null) {
            Log.d(TAG, "addScore: Null parameters provided");
            return false;
        }
        
        int gamePlayerCount = game.getPlayerList().size();
        int scorePlayerCount = scoreList.size();
        if (gamePlayerCount != scorePlayerCount) {
            Log.d(TAG, "addScore: GamePlayer:" + gamePlayerCount);
            Log.d(TAG, "addScore: ScorePlayer:" + scorePlayerCount);
            return false;
        }
        
        try {
            HashMap<String, Integer> scoreMap = getScoreMap(scoreList);
            game.getScore().add(new Score(game.getScore().size() + 1, scoreMap));
            return true;
        } catch (Exception e) {
            Log.e(TAG, "addScore: Error adding score", e);
            return false;
        }
    }

    @Override
    public int getPlayerRoundScore(Game game, String playerId, int round) {
        if (game == null || playerId == null || game.getScore() == null) {
            return 0;
        }
        List<Score> scoreList = game.getScore();
        for (Score score : scoreList) {
            if (score != null && score.getScoreOrder() == round && score.getScoreMap() != null) {
                Integer playerScore = score.getScoreMap().get(playerId);
                return playerScore != null ? playerScore : 0;
            }
        }
        return 0;
    }

    @Override
    public List<SingleScore> getCalculatedScore(Game game) {
        List<SingleScore> calculatedScoreList = new ArrayList<>();
        if (game == null || game.getPlayerList() == null || game.getScore() == null) {
            return calculatedScoreList;
        }
        List<Player> playerList = game.getPlayerList();
        List<Score> scoreList = game.getScore();
        for (Player player : playerList) {
            if (player != null && player.getId() != null) {
                int totalScore = 0;
                for (Score score : scoreList) {
                    if (score != null && score.getScoreMap() != null) {
                        Integer playerScore = score.getScoreMap().get(player.getId());
                        if (playerScore != null) {
                            totalScore += playerScore;
                        }
                    }
                }
                calculatedScoreList.add(new SingleScore(player.getId(), totalScore));
            }
        }
        return calculatedScoreList;
    }

    @Override
    public String serializeGame(Game game) {
        if (game == null) {
            return null;
        }
        try {
            Gson gson = new Gson();
            return gson.toJson(game);
        } catch (Exception e) {
            Log.e(TAG, "serializeGame: Error serializing game", e);
            return null;
        }
    }

    @Override
    public Game deserializeGame(String gameString) {
        if (gameString == null || gameString.trim().isEmpty()) {
            return null;
        }
        try {
            Gson gson = new Gson();
            return gson.fromJson(gameString, Game.class);
        } catch (Exception e) {
            Log.e(TAG, "deserializeGame: Error deserializing game", e);
            return null;
        }
    }

    private HashMap<String, Integer> getScoreMap(List<SingleScore> scoreList) {
        HashMap<String, Integer> scoreMap = new HashMap<>();
        if (scoreList != null) {
            for (SingleScore singleScore : scoreList) {
                if (singleScore != null && singleScore.playerId() != null) {
                    scoreMap.put(singleScore.playerId(), singleScore.getScore());
                }
            }
        }
        return scoreMap;
    }
}

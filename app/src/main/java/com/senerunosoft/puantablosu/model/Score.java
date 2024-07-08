package com.senerunosoft.puantablosu.model;

import java.util.HashMap;

public class Score {
    private int scoreOrder;
    private HashMap<String, Integer> scoreMap;

    public Score() {
    }

    public Score(int scoreOrder, HashMap<String, Integer> scoreMap) {
        this.scoreOrder = scoreOrder;
        this.scoreMap = scoreMap;
    }


    public int getScoreOrder() {
        return scoreOrder;
    }

    public HashMap<String, Integer> getScoreMap() {
        return scoreMap;
    }

    public void setScoreMap(HashMap<String, Integer> scoreMap) {
        this.scoreMap = scoreMap;
    }
}

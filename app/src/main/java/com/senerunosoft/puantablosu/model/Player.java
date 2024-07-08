package com.senerunosoft.puantablosu.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Player {

    private String id;
    private String name;

    public Player() {
    }

    public Player(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}

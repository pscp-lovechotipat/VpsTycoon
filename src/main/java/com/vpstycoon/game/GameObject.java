package com.vpstycoon.game;

import java.io.Serializable;

public class GameObject implements Serializable {
    private String name;

    public GameObject() {
        this.name = "";
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}

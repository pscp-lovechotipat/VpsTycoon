package com.vpstycoon.application;

public final class Bootstrap {
    private Bootstrap() {
    }

    public static void main(String[] args) {
        GameApplication.launch(GameApplication.class, args);
    }
}

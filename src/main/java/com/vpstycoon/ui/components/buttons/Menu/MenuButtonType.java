package com.vpstycoon.ui.components.buttons.Menu;

public enum MenuButtonType {
    PLAY("play"),
    SETTINGS("settings"),
    DELETEGAME("delete"),
    QUIT("quit"),
    CONTINUE("continue"),
    NEW_GAME("new_game"),
    BACK("back"),
    APPLY("apply"),
    RESUME("resume"),
    MAIN_MENU("Mainmenu");

    private final String value;

    
    MenuButtonType(String value) {
        this.value = value;
    }

    
    public String getValue() {
        return value;
    }
}

package com.vpstycoon.ui.base;

import com.vpstycoon.audio.AudioManager;
import com.vpstycoon.config.GameConfig;
import com.vpstycoon.screen.ScreenManager;
import com.vpstycoon.screen.ScreenResolution;
import com.vpstycoon.ui.SceneController;
import javafx.scene.layout.Region;

public abstract class GameScreen {
    protected final GameConfig config;
    protected final ScreenManager screenManager;

    public GameScreen(GameConfig config, ScreenManager screenManager) {
        this.config = config;
        this.screenManager = screenManager;
    }

    public void show() {
        Region content = createContent();
        enforceResolution(content);
        SceneController.getInstance().setContent(content);
    }

    public void hide() {
        SceneController.getInstance().setContent(null);
    }

    protected abstract Region createContent();

    protected void enforceResolution(Region root) {
        ScreenResolution resolution = config.getResolution();
        root.setPrefWidth(resolution.getWidth());
        root.setPrefHeight(resolution.getHeight());
        root.setMinWidth(resolution.getWidth());
        root.setMinHeight(resolution.getHeight());
        root.setMaxWidth(resolution.getWidth());
        root.setMaxHeight(resolution.getHeight());
    }
} 
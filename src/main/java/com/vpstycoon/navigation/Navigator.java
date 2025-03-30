package com.vpstycoon.navigation;

import com.vpstycoon.navigation.interfaces.INavigator;
import com.vpstycoon.screen.ScreenManager;
import com.vpstycoon.view.base.GameScreen;

public class Navigator implements INavigator {
    
    private ScreenManager screenManager;
    private GameScreen mainMenuScreen;
    private GameScreen gameScreen;
    private GameScreen settingsScreen;
    private GameScreen cutsceneScreen;
    

    public Navigator(ScreenManager screenManager) {
        this.screenManager = screenManager;
    }
    

    public void setMainMenuScreen(GameScreen mainMenuScreen) {
        this.mainMenuScreen = mainMenuScreen;
    }
    

    public void setGameScreen(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
    }
    

    public void setSettingsScreen(GameScreen settingsScreen) {
        this.settingsScreen = settingsScreen;
    }
    

    public void setCutsceneScreen(GameScreen cutsceneScreen) {
        this.cutsceneScreen = cutsceneScreen;
    }

    @Override
    public void navigateToMainMenu() {
        if (mainMenuScreen != null) {
            screenManager.switchScreen(mainMenuScreen);
        }
    }

    @Override
    public void navigateToGame() {
        if (gameScreen != null) {
            screenManager.switchScreen(gameScreen);
        }
    }

    @Override
    public void navigateToSettings() {
        if (settingsScreen != null) {
            screenManager.switchScreen(settingsScreen);
        }
    }

    @Override
    public void navigateToCutscene() {
        if (cutsceneScreen != null) {
            screenManager.switchScreen(cutsceneScreen);
        }
    }
    
    @Override
    public void saveAndExitToMainMenu() {
        
        
        navigateToMainMenu();
    }
}

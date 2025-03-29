package com.vpstycoon.navigation;

import com.vpstycoon.navigation.interfaces.INavigator;
import com.vpstycoon.screen.ScreenManager;
import com.vpstycoon.view.base.GameScreen;

/**
 * คลาสสำหรับการนำทางระหว่างหน้าจอต่างๆ ในแอปพลิเคชัน
 */
public class Navigator implements INavigator {
    
    private ScreenManager screenManager;
    private GameScreen mainMenuScreen;
    private GameScreen gameScreen;
    private GameScreen settingsScreen;
    private GameScreen cutsceneScreen;
    
    /**
     * สร้าง Navigator ด้วย ScreenManager และหน้าจอหลักต่างๆ
     */
    public Navigator(ScreenManager screenManager) {
        this.screenManager = screenManager;
    }
    
    /**
     * ตั้งค่าหน้าเมนูหลัก
     */
    public void setMainMenuScreen(GameScreen mainMenuScreen) {
        this.mainMenuScreen = mainMenuScreen;
    }
    
    /**
     * ตั้งค่าหน้าเกม
     */
    public void setGameScreen(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
    }
    
    /**
     * ตั้งค่าหน้าตั้งค่า
     */
    public void setSettingsScreen(GameScreen settingsScreen) {
        this.settingsScreen = settingsScreen;
    }
    
    /**
     * ตั้งค่าหน้า cutscene
     */
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
        // บันทึกเกม (จะถูก implement ต่อไป)
        
        // กลับไปที่เมนูหลัก
        navigateToMainMenu();
    }
} 
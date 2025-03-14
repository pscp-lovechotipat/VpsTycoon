package com.vpstycoon.ui.settings;

import com.vpstycoon.audio.AudioManager;
import com.vpstycoon.config.GameConfig;
import com.vpstycoon.event.GameEventBus;
import com.vpstycoon.event.SettingsChangedEvent;
import com.vpstycoon.screen.ScreenManager;
import com.vpstycoon.ui.components.buttons.Menu.MenuButton;
import com.vpstycoon.ui.components.buttons.Menu.MenuButtonType;
import com.vpstycoon.ui.navigation.Navigator;

public class SettingScreenInGame extends SettingsScreen {
    private AudioManager audioManager;
    public SettingScreenInGame(GameConfig config, ScreenManager screenManager, Navigator navigator) {
        super(config, screenManager, navigator);
        this.audioManager = AudioManager.getInstance();
    }

    @Override
    protected MenuButton createBackButton() {
        MenuButton backButton = new MenuButton(MenuButtonType.BACK);
        backButton.setOnAction(e -> {
            audioManager.playSoundEffect("click.wav");
            navigator.continueGame();
        }); // เปลี่ยนให้กลับไปที่หน้าจอเกม
        return backButton;
    }

    @Override
    protected MenuButton createApplyButton() {
        MenuButton applyButton = new MenuButton(MenuButtonType.APPLY);
        applyButton.setOnAction(e -> {
            audioManager.playSoundEffect("click.wav");

            config.save();
            GameEventBus.getInstance().publish(new SettingsChangedEvent(config));

            // Force UI refresh after resolution change to prevent white borders
            if (config.getResolution() != null) {
                // Let's use a simple approach - navigate back to main menu and then to settings again
                // This ensures the screen is completely rebuilt with the new resolution
                navigator.showMainMenu();

                // Use runLater to ensure navigation completes before returning to settings
                javafx.application.Platform.runLater(() -> {
                    navigator.showInGameSettings();
                });
            }
        });
        return applyButton;
    }
}

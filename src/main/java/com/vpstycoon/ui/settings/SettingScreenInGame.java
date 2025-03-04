package com.vpstycoon.ui.settings;

import com.vpstycoon.config.GameConfig;
import com.vpstycoon.screen.ScreenManager;
import com.vpstycoon.ui.components.buttons.Menu.MenuButton;
import com.vpstycoon.ui.components.buttons.Menu.MenuButtonType;
import com.vpstycoon.ui.navigation.Navigator;

public class SettingScreenInGame extends SettingsScreen {
    public SettingScreenInGame(GameConfig config, ScreenManager screenManager, Navigator navigator) {
        super(config, screenManager, navigator);
    }

    @Override
    protected MenuButton createBackButton() {
        MenuButton backButton = new MenuButton(MenuButtonType.BACK);
        backButton.setOnAction(e -> navigator.continueGame()); // เปลี่ยนให้กลับไปที่หน้าจอเกม
        return backButton;
    }
}

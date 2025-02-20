package com.vpstycoon.ui.screen;

import com.vpstycoon.config.GameConfig;
import com.vpstycoon.game.GameState;
import com.vpstycoon.ui.navigation.Navigator;
import javafx.scene.layout.BorderPane;

public class GameScreen extends BorderPane {
    private final Navigator navigator;
    private final GameConfig gameConfig;
    private final GameState gameState;

    public GameScreen(Navigator navigator, GameConfig gameConfig, GameState gameState) {
        this.navigator = navigator;
        this.gameConfig = gameConfig;
        this.gameState = gameState;
        
        initializeUI();
    }

    private void initializeUI() {
        // TODO: เพิ่ม UI components ของเกม
        // เช่น แสดงเงิน, ทรัพยากร, ปุ่มต่างๆ
    }
} 
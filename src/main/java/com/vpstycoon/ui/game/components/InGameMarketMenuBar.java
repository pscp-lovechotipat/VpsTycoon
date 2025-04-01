package com.vpstycoon.ui.game.components;

import com.vpstycoon.game.manager.VPSManager;
import com.vpstycoon.ui.game.GameplayContentPane;
import com.vpstycoon.ui.game.desktop.MarketWindow;
import com.vpstycoon.ui.game.utils.UIUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

public class InGameMarketMenuBar extends StackPane {
    private final GameplayContentPane parent;
    private final VPSManager vpsManager;

    public InGameMarketMenuBar(GameplayContentPane parent, VPSManager vpsManager) {
        this.parent = parent;
        this.vpsManager = vpsManager;

        Button openMarketButton = UIUtils.createModernButton("Open Market", "#FF9800");
        setPadding(new Insets(40));
        openMarketButton.setOnAction(e -> {
            
            setVisible(false);

            
            parent.getMenuBar().setVisible(false);
            parent.getMoneyUI().setVisible(false);
            parent.getDateView().setVisible(false);

            openMarketWindow();
        });

        
        StackPane.setAlignment(openMarketButton, Pos.BOTTOM_RIGHT);

        
        
    }

    private void openMarketWindow() {
        MarketWindow marketWindow = new MarketWindow(
                () -> {
                    parent.getGameArea().getChildren().removeIf(node -> node instanceof MarketWindow);
                    parent.getMenuBar().setVisible(true);
                    setVisible(true);
                    parent.getMoneyUI().setVisible(true);
                    parent.getDateView().setVisible(true);
                },
                () -> {
                    parent.getGameArea().getChildren().removeIf(node -> node instanceof MarketWindow);
                    parent.getMenuBar().setVisible(true);
                    setVisible(true);
                    parent.getMoneyUI().setVisible(true);
                    parent.getDateView().setVisible(true);
                },
                vpsManager,
                parent
        );
        StackPane.setAlignment(marketWindow, Pos.CENTER);
        parent.getGameArea().getChildren().add(marketWindow);
    }
}


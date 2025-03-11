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
            // ซ่อนปุ่ม Open Market
            setVisible(false);

            // ซ่อน MenuBar
            parent.getMenuBar().setVisible(false);

            openMarketWindow();
        });

        // วางปุ่มที่ด้านล่างขวา
        StackPane.setAlignment(openMarketButton, Pos.BOTTOM_RIGHT);

        // เพิ่มปุ่มเข้า StackPane
        this.getChildren().add(openMarketButton);
    }

    private void openMarketWindow() {
        MarketWindow marketWindow = new MarketWindow(
                () -> {
                    parent.getGameArea().getChildren().removeIf(node -> node instanceof MarketWindow);
                    parent.getMenuBar().setVisible(true);
                    setVisible(true);
                },
                vpsManager,
                null // Assuming MarketWindow doesn't need GameplayContentPane anymore
        );
        StackPane.setAlignment(marketWindow, Pos.CENTER);
        parent.getGameArea().getChildren().add(marketWindow);
    }
}
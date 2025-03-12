package com.vpstycoon.ui.game.market;

import com.vpstycoon.ui.game.GameplayContentPane;
import com.vpstycoon.ui.game.desktop.MarketWindow;
import javafx.scene.layout.BorderPane;

public class MarketUI {
    private final GameplayContentPane parent;

    public MarketUI(GameplayContentPane parent) {
        this.parent = parent;
    }

    public void openMarket() {
        // Create main container
        BorderPane marketPane = new BorderPane();
        marketPane.setPrefSize(800, 600);
        marketPane.setStyle("-fx-background-color: linear-gradient(to bottom, #2E3B4E, #1A252F); -fx-padding: 20px;");
        
        // Hide the menu bars
        parent.getMenuBar().setVisible(false);
        parent.getInGameMarketMenuBar().setVisible(false);
        
        MarketWindow marketWindow = new MarketWindow(
                () -> {
                    parent.getGameArea().getChildren().removeIf(node -> node instanceof MarketWindow);
                    parent.openRackInfo();
                },
                () -> parent.getGameArea().getChildren().removeIf(node -> node instanceof MarketWindow),
                parent.getVpsManager(),
                parent
        );
        parent.getGameArea().getChildren().add(marketWindow);
        parent.getRootStack().getChildren().remove(parent.getGameArea());
        parent.getRootStack().getChildren().add(parent.getGameArea()); // นำ gameArea ไปไว้ด้านบนสุด
    }
}
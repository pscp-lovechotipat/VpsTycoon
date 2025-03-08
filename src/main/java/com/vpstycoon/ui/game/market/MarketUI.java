package com.vpstycoon.ui.game.market;

import com.vpstycoon.ui.game.GameplayContentPane;
import com.vpstycoon.ui.game.desktop.MarketWindow;

public class MarketUI {
    private final GameplayContentPane parent;

    public MarketUI(GameplayContentPane parent) {
        this.parent = parent;
    }

    public void openMarket() {
        MarketWindow marketWindow = new MarketWindow(
                () -> {
                    parent.getGameArea().getChildren().removeIf(node -> node instanceof MarketWindow);
                    parent.openRackInfo();
                },
                parent.getVpsManager(),
                parent
        );
        parent.getGameArea().getChildren().add(marketWindow);
    }
}
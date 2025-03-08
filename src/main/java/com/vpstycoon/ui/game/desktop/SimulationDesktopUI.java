package com.vpstycoon.ui.game.desktop;

import com.vpstycoon.ui.game.GameplayContentPane;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;

public class SimulationDesktopUI {
    private final GameplayContentPane parent;

    public SimulationDesktopUI(GameplayContentPane parent) {
        this.parent = parent;
    }

    public synchronized void openSimulationDesktop() {
        DesktopScreen desktop = new DesktopScreen(
                parent.getCompany().getRating(),
                parent.getCompany().getMarketingPoints(),
                parent.getChatSystem(),
                parent.getRequestManager(),
                parent.getVpsManager(),
                parent.getCompany()
        );
        StackPane.setAlignment(desktop, Pos.CENTER);
        desktop.setMaxSize(parent.getGameArea().getWidth() * 0.8, parent.getGameArea().getHeight() * 0.8);

        parent.getGameArea().getChildren().clear();
        parent.getGameArea().getChildren().add(desktop);
        parent.getRootStack().getChildren().remove(1);

        desktop.addExitButton(parent::returnToRoom);
    }
}
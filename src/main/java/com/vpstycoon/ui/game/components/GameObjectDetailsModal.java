package com.vpstycoon.ui.game.components;

import com.vpstycoon.game.GameObject;
import com.vpstycoon.ui.game.flow.GameFlowManager;
import com.vpstycoon.ui.utils.ButtonUtils;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Modal dialog for showing GameObject details.
 */
public class GameObjectDetailsModal {

    public static void show(StackPane gameArea, GameObject obj, GameFlowManager gameFlowManager) {
        // Create modal container
        VBox modalContainer = new VBox(10);
        modalContainer.setStyle("""
                -fx-background-color: rgba(0, 0, 0, 0.7);
                -fx-padding: 20;
                """);
        modalContainer.setAlignment(Pos.CENTER);
        modalContainer.setPrefSize(gameArea.getWidth(), gameArea.getHeight());

        // Create modal content
        VBox modalContent = new VBox(15);
        modalContent.setStyle("""
                -fx-background-color: white;
                -fx-padding: 20;
                -fx-background-radius: 5;
                -fx-min-width: 300;
                -fx-max-width: 300;
                """);
        modalContent.setAlignment(Pos.CENTER);

        // Title
        Label titleLabel = new Label(obj.getName());
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Status and level
        Label statusLabel = new Label("Status: " + obj.getStatus());
        Label levelLabel = new Label("Level: " + obj.getLevel());

        // Upgrade button
        Button upgradeButton = ButtonUtils.createModalButton("Upgrade");
        upgradeButton.setOnAction(e -> {
            obj.upgrade(null);
            levelLabel.setText("Level: " + obj.getLevel());
            statusLabel.setText("Status: " + obj.getStatus());
            gameFlowManager.saveGame();
        });

        // Close button
        Button closeButton = ButtonUtils.createModalButton("Close");
        closeButton.setOnAction(e -> gameArea.getChildren().remove(modalContainer));

        // Add everything to modal
        modalContent.getChildren().addAll(titleLabel, new Separator(), statusLabel, levelLabel, upgradeButton, closeButton);
        modalContainer.getChildren().add(modalContent);

        // Close on background click
        modalContainer.setOnMouseClicked(e -> {
            if (e.getTarget() == modalContainer) {
                gameArea.getChildren().remove(modalContainer);
            }
        });

        // Add to game area
        gameArea.getChildren().add(modalContainer);
    }
} 
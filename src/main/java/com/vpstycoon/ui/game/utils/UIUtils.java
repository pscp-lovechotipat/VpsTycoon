package com.vpstycoon.ui.game.utils;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class UIUtils {
    public static HBox createCard() {
        HBox card = new HBox(20);
        card.setStyle("-fx-background-color: #37474F; -fx-padding: 15px; -fx-background-radius: 10px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);");
        card.setAlignment(Pos.CENTER);
        return card;
    }

    public static VBox createSection(String title) {
        VBox section = new VBox(10);
        Label label = new Label(title);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        section.getChildren().add(label);
        return section;
    }

    public static Button createModernButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: derive(" + color + ", -10%); -fx-text-fill: white; -fx-padding: 10px 20px; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);"));
        return button;
    }
}
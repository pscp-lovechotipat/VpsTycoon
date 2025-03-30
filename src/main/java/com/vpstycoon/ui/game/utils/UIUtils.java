package com.vpstycoon.ui.game.utils;

import com.vpstycoon.application.FontLoader;
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

        button.setFont(FontLoader.loadFont(32));
        
        
        String baseStyle = "-fx-background-color: #2a0a3a; " +
                           "-fx-border-color: #8a2be2; " +
                           "-fx-border-width: 2px; " +
                           "-fx-border-style: solid; " +
                           "-fx-text-fill: #e0b0ff; " + 

                           "-fx-font-weight: bold; " +
                           "-fx-padding: 10px 20px; " +
                           "-fx-background-radius: 0; " +
                           "-fx-border-radius: 0; " +
                           "-fx-effect: dropshadow(gaussian, #9370db, 10, 0.5, 0, 0), innershadow(gaussian, #9370db, 5, 0.5, 0, 0);";
        
        
        String hoverStyle = "-fx-background-color: #3a1a4a; " +
                            "-fx-border-color: #b041ff; " +
                            "-fx-border-width: 2px; " +
                            "-fx-border-style: solid; " +
                            "-fx-text-fill: #f0d0ff; " +

                            "-fx-font-weight: bold; " +
                            "-fx-padding: 10px 20px; " +
                            "-fx-background-radius: 0; " +
                            "-fx-border-radius: 0; " +
                            "-fx-effect: dropshadow(gaussian, #b041ff, 15, 0.7, 0, 0), innershadow(gaussian, #b041ff, 7, 0.7, 0, 0);";
        
        
        String pressedStyle = "-fx-background-color: #4a2a5a; " +
                              "-fx-border-color: #c061ff; " +
                              "-fx-border-width: 2px; " +
                              "-fx-border-style: solid; " +
                              "-fx-text-fill: #ffffff; " +

                              "-fx-font-weight: bold; " +
                              "-fx-padding: 10px 20px; " +
                              "-fx-background-radius: 0; " +
                              "-fx-border-radius: 0; " +
                              "-fx-effect: dropshadow(gaussian, #c061ff, 5, 0.3, 0, 0), innershadow(gaussian, #c061ff, 10, 0.5, 0, 0);";
        
        button.setStyle(baseStyle);
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(baseStyle));
        button.setOnMousePressed(e -> button.setStyle(pressedStyle));
        button.setOnMouseReleased(e -> button.setStyle(hoverStyle));
        
        return button;
    }
}

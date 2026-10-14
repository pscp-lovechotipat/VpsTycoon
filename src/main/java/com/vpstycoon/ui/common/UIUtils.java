package com.vpstycoon.ui.common;

import javafx.scene.control.Button;

public class UIUtils {
    public static Button createModernButton(String text) {
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

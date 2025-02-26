package com.vpstycoon.ui.game.desktop;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;

public class MarketWindow extends VBox {
    private final Runnable onClose;

    public MarketWindow(Runnable onClose) {
        this.onClose = onClose;

        setupUI();
        styleWindow();
    }

    private void setupUI() {
        setPrefSize(600, 400);

        // Title bar
        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_RIGHT);
        titleBar.setPadding(new Insets(5, 10, 5, 10));
        titleBar.setStyle("-fx-background-color: #ff8c00;");

        Button closeButton = new Button("X");
        closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
        closeButton.setOnAction(e -> onClose.run());

        titleBar.getChildren().add(closeButton);

        // Market content
        Label titleLabel = new Label("Market");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: black;");

        VBox content = new VBox(titleLabel);
        content.setPadding(new Insets(20));
        VBox.setVgrow(content, Priority.ALWAYS);

        getChildren().addAll(titleBar, content);
    }

    private void styleWindow() {
        setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0);");
    }
}

package com.vpstycoon.ui;

import com.vpstycoon.Config;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GameMenu {
    private Stage primaryStage;

    public GameMenu(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void show() {
        primaryStage.setTitle("Game Menu");

        Label titleLabel = new Label("Game Menu");

        Button newGameButton = new Button("New Game");
        newGameButton.setOnAction(e -> startNewGame());

        Button resumeButton = new Button("Resume");
        resumeButton.setDisable(!Config.hasSavedGame()); // ✅ Disable if no saved game
        resumeButton.setOnAction(e -> startGame());

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> new MainMenu(primaryStage).show());

        VBox vbox = new VBox(20, titleLabel, newGameButton, resumeButton, backButton);
        vbox.setAlignment(Pos.CENTER);

        BorderPane root = new BorderPane();
        root.setCenter(vbox);

        primaryStage.setScene(new Scene(root, Config.getResolution().getWidth(), Config.getResolution().getHeight()));
        primaryStage.setFullScreen(Config.isFullscreen());
    }

    private void startNewGame() {
        Config.deleteSave();
        Config.saveGame();
        startGame();
    }

    private void startGame() {
        new Screen(primaryStage).show();
    }
}

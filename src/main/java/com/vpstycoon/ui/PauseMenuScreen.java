package com.vpstycoon.ui;

import com.vpstycoon.Config;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Objects;

public class PauseMenuScreen {
    private Stage primaryStage;
    private Screen gameScreen;

    public PauseMenuScreen(Stage primaryStage, Screen gameScreen) {
        this.primaryStage = primaryStage;
        this.gameScreen = gameScreen;
    }

    public void show() {
        primaryStage.setTitle("Paused");

        // Load Pause Menu Background
        Image bgImage = new Image(Objects.requireNonNull(getClass().getResource("/images/pause_bg.png")).toExternalForm());
        ImageView bgImageView = new ImageView(bgImage);
        bgImageView.setFitWidth(Config.getResolution().getWidth());
        bgImageView.setFitHeight(Config.getResolution().getHeight());
        bgImageView.setPreserveRatio(false);

        // Pause Menu Buttons
        Button resumeButton = new Button("Resume");
        resumeButton.setOnAction(event -> gameScreen.show());

        Button mainMenuButton = new Button("Main Menu");
        mainMenuButton.setOnAction(event -> new MainMenu(primaryStage).show());

        Button quitButton = new Button("Quit");
        quitButton.setOnAction(event -> System.exit(0));

        VBox pauseMenu = new VBox(20, resumeButton, mainMenuButton, quitButton);
        pauseMenu.setAlignment(Pos.CENTER);

        StackPane root = new StackPane(bgImageView, pauseMenu);

        primaryStage.setScene(new Scene(root, Config.getResolution().getWidth(), Config.getResolution().getHeight()));
        primaryStage.setFullScreen(Config.isFullscreen());
    }
}

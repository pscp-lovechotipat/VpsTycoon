package com.vpstycoon.ui;

import com.vpstycoon.Config;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Objects;

public class Screen {
    private Stage primaryStage;

    public Screen(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void show() {
        primaryStage.setTitle("Game Screen");

        // Load Game Background
        Image bgImage = new Image(Objects.requireNonNull(getClass().getResource("/images/game_bg.png")).toExternalForm());
        ImageView bgImageView = new ImageView(bgImage);
        bgImageView.setFitWidth(Config.getResolution().getWidth());
        bgImageView.setFitHeight(Config.getResolution().getHeight());
        bgImageView.setPreserveRatio(false);

        StackPane root = new StackPane(bgImageView);

        Scene scene = new Scene(root, Config.getResolution().getWidth(), Config.getResolution().getHeight());

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                new PauseMenuScreen(primaryStage, this).show();
            }
        });

        primaryStage.setScene(scene);
        primaryStage.setFullScreen(Config.isFullscreen());
    }
}

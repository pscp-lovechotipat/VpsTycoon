package com.vpstycoon.ui;

import com.vpstycoon.Config;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Objects;

public class MainMenu {
    private Stage primaryStage;

    public MainMenu(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void show() {
        primaryStage.setTitle("VPS Hosting Tycoon");

        // Load logo
        Image logo = new Image(Objects.requireNonNull(getClass().getResource("/images/logo.png")).toExternalForm());
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(300);
        logoView.setPreserveRatio(true);

        // Play Button
        Button playButton = new Button("Play");
        playButton.setOnAction(e -> new GameMenu(primaryStage).show());

        // Settings Button
        Button settingsButton = new Button("Settings");
        settingsButton.setOnAction(e -> new SettingsMenu(primaryStage).show());

        // Quit Button
        Button quitButton = new Button("Quit");
        quitButton.setOnAction(e -> System.exit(0));

        VBox vbox = new VBox(20, logoView, playButton, settingsButton, quitButton);
        vbox.setAlignment(Pos.CENTER);

        BorderPane root = new BorderPane();
        root.setCenter(vbox);

        primaryStage.setScene(new Scene(root, Config.getResolution().getWidth(), Config.getResolution().getHeight()));
        primaryStage.setFullScreen(Config.isFullscreen());
        primaryStage.show();
    }
}

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
    private NeonShadow neonShadow = new NeonShadow();

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
        Image resumeGif = new Image(Objects.requireNonNull(getClass().getResource("/images/resume.gif")).toExternalForm());
        ImageView resumeImageView = new ImageView(resumeGif);
        resumeImageView.setFitWidth(43*4);
        resumeImageView.setFitHeight(11*4);
        Button resumeButton = new Button("", resumeImageView);
        resumeButton.setStyle("-fx-background-color: transparent;");
        resumeButton.setOnAction(event -> gameScreen.show());
        //hover
        resumeButton.setOnMouseEntered(event -> resumeImageView.setEffect(neonShadow.neon()));
        resumeButton.setOnMouseExited(event -> resumeImageView.setEffect(null));

        Image mainMenuGif = new Image(Objects.requireNonNull(getClass().getResource("/images/mainmenu.gif")).toExternalForm());
        ImageView mainMenuImageView = new ImageView(mainMenuGif);
        mainMenuImageView.setFitWidth(43*4);
        mainMenuImageView.setFitHeight(11*4);
        Button mainMenuButton = new Button("", mainMenuImageView);
        mainMenuButton.setStyle("-fx-background-color: transparent;");
        mainMenuButton.setOnAction(event -> new MainMenu(primaryStage).show());
        //hover
        mainMenuButton.setOnMouseEntered(event -> mainMenuImageView.setEffect(neonShadow.neon()));
        mainMenuButton.setOnMouseExited(event -> mainMenuImageView.setEffect(null));

        // Quit Button
        Image imageGif = new Image(Objects.requireNonNull(getClass().getResource("/images/quit.gif")).toExternalForm());
        ImageView quitImageView = new ImageView(imageGif);
        quitImageView.setFitWidth(43*4);
        quitImageView.setFitHeight(11*4);
        Button quitButton = new Button("", quitImageView);
        quitButton.setStyle("-fx-background-color: transparent;");
        quitButton.setOnAction(e -> System.exit(0));
        //hover
        quitButton.setOnMouseEntered(event -> quitImageView.setEffect(neonShadow.neon()));
        quitButton.setOnMouseExited(event -> quitImageView.setEffect(null));

        VBox pauseMenu = new VBox(20, resumeButton, mainMenuButton, quitButton);
        pauseMenu.setAlignment(Pos.CENTER);

        StackPane root = new StackPane(bgImageView, pauseMenu);

        primaryStage.setScene(new Scene(root, Config.getResolution().getWidth(), Config.getResolution().getHeight()));
        primaryStage.setFullScreen(Config.isFullscreen());
    }
}

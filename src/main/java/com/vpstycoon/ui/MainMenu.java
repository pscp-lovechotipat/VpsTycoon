package com.vpstycoon.ui;

import com.vpstycoon.Config;
import com.vpstycoon.ScreenResolution;
import com.vpstycoon.utils.StageUtils;
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
    private NeonShadow neonShadow = new NeonShadow();
    private ScreenResolution selectedResolution;

    public MainMenu(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.selectedResolution = ScreenResolution.getAvailableResolutions().get(0);
    }

    public void show() {
        primaryStage.setTitle("Main Menu");
        StageUtils.setFixedSize(primaryStage, selectedResolution);
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/images/logo.png")).toExternalForm()));


        // Load logo
        Image logo = new Image(Objects.requireNonNull(getClass().getResource("/images/logo.png")).toExternalForm());
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(300);
        logoView.setPreserveRatio(true);

        // Play Button
        Image playGif = new Image("url:https://piskel-imgstore-b.appspot.com/img/3246b851-eed3-11ef-92ec-394195046342.gif");
        ImageView playImageView = new ImageView(playGif);
        playImageView.setFitWidth(43*4);
        playImageView.setFitHeight(11*4);
        Button playButton = new Button("", playImageView);
        playButton.setStyle("-fx-background-color: transparent;");
        playButton.setOnAction(e -> new GameMenu(primaryStage).show());

        //hover
        playButton.setOnMouseEntered(event -> playImageView.setEffect(neonShadow.neon()));
        playButton.setOnMouseExited(event -> playImageView.setEffect(null));

        // Settings Button
        Image settingsGif = new Image("url:https://piskel-imgstore-b.appspot.com/img/32b02251-eed2-11ef-a5d3-394195046342.gif");
        ImageView settingsImageView = new ImageView(settingsGif);
        settingsImageView.setFitWidth(43*4);
        settingsImageView.setFitHeight(11*4);
        Button settingsButton = new Button("", settingsImageView);
        settingsButton.setStyle("-fx-background-color: transparent;");
        settingsButton.setOnAction(e -> new SettingsMenu(primaryStage).show());
        //Hover
        settingsButton.setOnMouseEntered(event -> settingsImageView.setEffect(neonShadow.neon()));
        settingsButton.setOnMouseExited(event -> settingsImageView.setEffect(null));

        // Quit Button
        Image imageGif = new Image("url:https://piskel-imgstore-b.appspot.com/img/4484816e-eed0-11ef-8022-394195046342.gif");
        ImageView quitImageView = new ImageView(imageGif);
        quitImageView.setFitWidth(43*4);
        quitImageView.setFitHeight(11*4);
        Button quitButton = new Button("", quitImageView);
        quitButton.setStyle("-fx-background-color: transparent;");
        quitButton.setOnAction(e -> System.exit(0));
        //hover
        quitButton.setOnMouseEntered(event -> quitImageView.setEffect(neonShadow.neon()));
        quitButton.setOnMouseExited(event -> quitImageView.setEffect(null));

        VBox vbox = new VBox(20, logoView, playButton, settingsButton, quitButton);
        vbox.setAlignment(Pos.CENTER);


        BorderPane root = new BorderPane();
        root.setCenter(vbox);

        primaryStage.setScene(new Scene(root, Config.getResolution().getWidth(), Config.getResolution().getHeight()));
        primaryStage.setFullScreen(Config.isFullscreen());
        primaryStage.show();
    }
}

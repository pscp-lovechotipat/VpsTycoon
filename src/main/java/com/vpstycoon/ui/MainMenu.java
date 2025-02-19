package com.vpstycoon.ui;

import com.vpstycoon.ScreenResolution;
import com.vpstycoon.utils.StageUtils;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.effect.ColorAdjust;

import java.net.URL;
import java.util.Objects;
import java.io.File;

public class MainMenu {
    private Stage primaryStage;
    private NeonShadow neonShadow = new NeonShadow();
    private ScreenResolution selectedResolution;
    private GameMenu gameMenu;
    private SettingsMenu settingsMenu;

    public MainMenu(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.selectedResolution = ScreenResolution.getAvailableResolutions().getFirst();
        this.gameMenu = new GameMenu(primaryStage);
        this.settingsMenu = new SettingsMenu(primaryStage);
    }

    public void show() {
        primaryStage.setTitle("Main Menu");
        StageUtils.setFixedSize(primaryStage, selectedResolution);

        try {
            // Check if save file exists
            File saveFile = new File("savegame.dat");
            boolean hasSaveGame = saveFile.exists();

            // Load logo
            URL logoUrl = getClass().getResource("/images/logo.png");
            if (logoUrl != null) {
                primaryStage.getIcons().add(new Image(logoUrl.toExternalForm()));
            }

            // Load button GIFs
            Image playGif = new Image(Objects.requireNonNull(getClass().getResource("/images/play.gif")).toExternalForm());
            ImageView playImageView = new ImageView(playGif);
            playImageView.setFitWidth(43*4);
            playImageView.setFitHeight(11*4);
            
            Image settingsGif = new Image(Objects.requireNonNull(getClass().getResource("/images/settings.gif")).toExternalForm());
            ImageView settingsImageView = new ImageView(settingsGif);
            settingsImageView.setFitWidth(43*4);
            settingsImageView.setFitHeight(11*4);

            Image quitGif = new Image(Objects.requireNonNull(getClass().getResource("/images/quit.gif")).toExternalForm());
            ImageView quitImageView = new ImageView(quitGif);
            quitImageView.setFitWidth(43*4);
            quitImageView.setFitHeight(11*4);

            // Play Buttons
            Button playButton = new Button();
            playButton.setGraphic(playImageView);
            playButton.setStyle("-fx-background-color: transparent;");
            playButton.setOnAction(e -> gameMenu.show());
            //hover
            playButton.setOnMouseEntered(event -> playImageView.setEffect(neonShadow.neon()));
            playButton.setOnMouseExited(event -> playImageView.setEffect(null));

            Button settingsButton = new Button();
            settingsButton.setGraphic(settingsImageView);
            settingsButton.setStyle("-fx-background-color: transparent;");
            settingsButton.setOnAction(e -> settingsMenu.show());
            //hover
            settingsButton.setOnMouseEntered(event -> settingsImageView.setEffect(neonShadow.neon()));
            settingsButton.setOnMouseExited(event -> settingsImageView.setEffect(null));

            Button quitButton = new Button();
            quitButton.setGraphic(quitImageView);
            quitButton.setStyle("-fx-background-color: transparent;");
            quitButton.setOnAction(e -> Platform.exit());
            //hover
            quitButton.setOnMouseEntered(event -> quitImageView.setEffect(neonShadow.neon()));
            quitButton.setOnMouseExited(event -> quitImageView.setEffect(null));

            // Layout
            VBox layout = new VBox(20);
            layout.setAlignment(Pos.CENTER);
            layout.getChildren().addAll(playButton, settingsButton, quitButton);

            Scene scene = new Scene(layout);
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

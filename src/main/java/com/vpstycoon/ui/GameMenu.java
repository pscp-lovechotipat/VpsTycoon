package com.vpstycoon.ui;

import com.vpstycoon.Config;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.Objects;
import java.io.File;

public class GameMenu {
    private Stage primaryStage;
    private NeonShadow neonShadow = new NeonShadow();
    private MainMenu mainMenu;

    public GameMenu(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void show() {
        primaryStage.setTitle("Game Menu");

        Label titleLabel = new Label("Game Menu");

        Image newGameGIF = new Image(Objects.requireNonNull(getClass().getResource("/images/new.gif")).toExternalForm());
        ImageView newGameImageView = new ImageView(newGameGIF);
        newGameImageView.setFitWidth(43*4);
        newGameImageView.setFitHeight(11*4);
        Button newGameButton = new Button("",newGameImageView);
        newGameButton.setStyle("-fx-background-color: transparent;");
        newGameButton.setOnAction(e -> startNewGame());
        //hover
        newGameButton.setOnMouseEntered(event -> newGameImageView.setEffect(neonShadow.neon()));
        newGameButton.setOnMouseExited(event -> newGameImageView.setEffect(null));

        Image conGIF = new Image(Objects.requireNonNull(getClass().getResource("/images/continue.gif")).toExternalForm());
        ImageView continueImageView = new ImageView(conGIF);
        continueImageView.setFitWidth(43*4);
        continueImageView.setFitHeight(11*4);
        Button continueButton = new Button("",continueImageView);
        continueButton.setStyle("-fx-background-color: transparent;");
        continueButton.setDisable(!Config.hasSavedGame());
        continueButton.setOnAction(e -> startGame());
        //hover
        continueButton.setOnMouseEntered(event -> continueImageView.setEffect(neonShadow.neon()));
        continueButton.setOnMouseExited(event -> continueImageView.setEffect(null));

        Image backGIF = new Image(Objects.requireNonNull(getClass().getResource("/images/back.gif")).toExternalForm());
        ImageView backImageView = new ImageView(backGIF);
        backImageView.setFitWidth(43*4);
        backImageView.setFitHeight(11*4);
        Button backButton = new Button("", backImageView);
        backButton.setStyle("-fx-background-color: transparent;");
        backButton.setOnAction(e -> new MainMenu(primaryStage).show());
        //hover
        backButton.setOnMouseEntered(event -> backImageView.setEffect(neonShadow.neon()));
        backButton.setOnMouseExited(event -> backImageView.setEffect(null));

        VBox vbox = new VBox(20, titleLabel, continueButton , newGameButton, backButton);
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

    public void loadGame() {
        try {
            // Load saved game data
            File saveFile = new File("savegame.dat");
            if (saveFile.exists()) {
                // TODO: Implement your save game loading logic aka กูไม่อยากทำ
                // ตัวเกมยังไม่เสร็จค่อยทำ
                // GameState savedState = SaveGameManager.load();
                // game.loadState(savedState);
                
                // Show the game screen
                show();
            } else {
                System.err.println("Save file not found!");
            }
        } catch (Exception e) {
            System.err.println("Error loading saved game: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

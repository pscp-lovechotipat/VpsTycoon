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

public class GameMenu {
    private Stage primaryStage;

    public GameMenu(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void show() {
        primaryStage.setTitle("Game Menu");

        Label titleLabel = new Label("Game Menu");

        Image newGameGIF = new Image("url:https://piskel-imgstore-b.appspot.com/img/3b5b651e-eec4-11ef-b5d0-394195046342.gif");
        ImageView newGameImageView = new ImageView(newGameGIF);
        newGameImageView.setFitWidth(43*4);
        newGameImageView.setFitHeight(11*4);
        Button newGameButton = new Button("",newGameImageView);
        newGameButton.setStyle("-fx-background-color: transparent;");
        newGameButton.setOnAction(e -> startNewGame());

        Image conGIF = new Image("url:https://piskel-imgstore-b.appspot.com/img/4dc4d421-eec6-11ef-b542-394195046342.gif");
        ImageView continueImageView = new ImageView(conGIF);
        continueImageView.setFitWidth(43*4);
        continueImageView.setFitHeight(11*4);
        Button continueButton = new Button("",continueImageView);
        continueButton.setStyle("-fx-background-color: transparent;");
        continueButton.setDisable(!Config.hasSavedGame()); // ✅ Disable if no saved game
        continueButton.setOnAction(e -> startGame());

        Image backGIF = new Image("url:https://piskel-imgstore-b.appspot.com/img/35e760ca-eecd-11ef-8c4c-394195046342.gif");
        ImageView backImageView = new ImageView(backGIF);
        backImageView.setFitWidth(43*4);
        backImageView.setFitHeight(11*4);
        Button backButton = new Button("", backImageView);
        backButton.setStyle("-fx-background-color: transparent;");
        backButton.setOnAction(e -> new MainMenu(primaryStage).show());

        VBox vbox = new VBox(20, titleLabel, newGameButton, continueButton, backButton);
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

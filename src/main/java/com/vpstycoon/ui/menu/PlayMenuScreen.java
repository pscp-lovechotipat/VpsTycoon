package com.vpstycoon.ui.menu;

import com.vpstycoon.game.GameSaveManager;
import com.vpstycoon.resource.ResourceManager;
import com.vpstycoon.ui.components.buttons.Menu.MenuButton;
import com.vpstycoon.ui.components.buttons.Menu.MenuButtonType;
import com.vpstycoon.ui.navigation.Navigator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PlayMenuScreen extends VBox {
    private static final String SAVE_FILE = "savegame.dat";
    private static final double SPACING = 20;

    private final Navigator navigator;
    private Button continueButton;
    private final GameSaveManager saveManager;
    private StackPane root;

    public PlayMenuScreen(Navigator navigator) {
        super();
        this.navigator = navigator;
        this.saveManager = new GameSaveManager();
        
        super.setAlignment(Pos.CENTER);
        super.setSpacing(SPACING);

        MenuButton continueButton = new MenuButton(MenuButtonType.CONTINUE);
        MenuButton newGameButton = new MenuButton(MenuButtonType.NEW_GAME);
        MenuButton backButton = new MenuButton(MenuButtonType.BACK);

        continueButton.setOnAction(e -> navigator.showLoadGame());
        newGameButton.setOnAction(e -> navigator.startNewGame());
        backButton.setOnAction(e -> navigator.showMainMenu());

        if (saveManager.saveExists()) {
            System.out.println(saveManager.saveExists());
            super.getChildren().add(continueButton);
        }

        super.getChildren().addAll(newGameButton, backButton);
    }

    private void showNewGameConfirmation() {
        VBox modalContainer = new VBox(10);
        modalContainer.setStyle("""
            -fx-background-color: rgba(0, 0, 0, 0.7);
            -fx-padding: 20;
            """);
        modalContainer.setAlignment(Pos.CENTER);
        modalContainer.setPrefSize(root.getWidth(), root.getHeight());
        
        VBox modalContent = new VBox(15);
        modalContent.setStyle("""
            -fx-background-color: white;
            -fx-padding: 20;
            -fx-background-radius: 5;
            -fx-min-width: 400;
            -fx-max-width: 400;
            """);
        modalContent.setAlignment(Pos.CENTER);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("New Game");
        titleLabel.setStyle("""
            -fx-font-size: 16px;
            -fx-font-weight: bold;
            """);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button helpButton = new Button("?");
        helpButton.setStyle("""
            -fx-background-color: #3498DB;
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-min-width: 24;
            -fx-min-height: 24;
            -fx-background-radius: 12;
            """);

        header.getChildren().addAll(titleLabel, spacer, helpButton);

        Label messageLabel = new Label("Starting a new game will overwrite your existing save file. Are you sure you want to continue?");
        messageLabel.setStyle("""
            -fx-font-size: 14px;
            -fx-wrap-text: true;
            """);
        messageLabel.setMaxWidth(350);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button okButton = new Button("OK");
        okButton.setStyle("""
            -fx-background-color: #3498DB;
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-padding: 5 20;
            -fx-background-radius: 3;
            -fx-min-width: 80;
            """);
        okButton.setOnAction(e -> {
            root.getChildren().remove(modalContainer);
            navigator.startNewGame();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("""
            -fx-background-color: #95A5A6;
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-padding: 5 20;
            -fx-background-radius: 3;
            -fx-min-width: 80;
            """);
        cancelButton.setOnAction(e -> root.getChildren().remove(modalContainer));

        buttonBox.getChildren().addAll(okButton, cancelButton);

        modalContent.getChildren().addAll(
            header,
            messageLabel,
            buttonBox
        );

        modalContainer.getChildren().add(modalContent);

        root.getChildren().add(modalContainer);
    }

    protected Region createContent() {
        root = new StackPane();
        
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #2C3E50;");

        Label titleLabel = new Label("Play Game");
        titleLabel.setStyle("-fx-font-size: 32px; -fx-text-fill: white; -fx-font-weight: bold;");

        continueButton = new MenuButton(MenuButtonType.CONTINUE);
        continueButton.setDisable(!saveManager.saveExists());
        continueButton.setOnAction(e -> {
            if (saveManager.saveExists()) {
                navigator.continueGame();
            }
        });

        MenuButton newGameButton = new MenuButton(MenuButtonType.NEW_GAME);
        newGameButton.setOnAction(e -> {
            if (saveManager.saveExists()) {
                showNewGameConfirmation();
            } else {
                navigator.startNewGame();
            }
        });

        MenuButton backButton = new MenuButton(MenuButtonType.BACK);
        backButton.setOnAction(e -> navigator.showMainMenu());

        content.getChildren().addAll(
            titleLabel,
            continueButton,
            newGameButton,
            backButton
        );

        root.getChildren().add(content);
        return root;
    }

    private boolean saveGameExists() {
        Path savePath = Paths.get(SAVE_FILE);
        return Files.exists(savePath);
    }

    private void startNewGame() {
        System.out.println("Starting new game...");
    }

    private void continueGame() {
        System.out.println("Continuing game...");
    }
} 
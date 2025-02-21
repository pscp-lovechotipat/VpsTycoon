package com.vpstycoon.ui.game;

import com.vpstycoon.config.GameConfig;
import com.vpstycoon.game.GameObject;
import com.vpstycoon.game.GameSaveManager;
import com.vpstycoon.game.GameState;
import com.vpstycoon.game.VPSObject;
import com.vpstycoon.screen.ScreenManager;
import com.vpstycoon.ui.base.GameScreen;
import com.vpstycoon.ui.navigation.Navigator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;

public class GameplayScreen extends GameScreen {
    private final Navigator navigator;
    private final GameSaveManager saveManager;
    private List<GameObject> gameObjects;
    private StackPane gameArea;

    public GameplayScreen(GameConfig config, ScreenManager screenManager, Navigator navigator) {
        super(config, screenManager);
        this.navigator = navigator;
        this.saveManager = new GameSaveManager();
        this.gameObjects = new ArrayList<>();
        loadGame();
    }

    private void loadGame() {
        if (saveManager.saveExists()) {
            GameState state = saveManager.loadGame();
            if (state.getGameObjects() != null && !state.getGameObjects().isEmpty()) {
                this.gameObjects = state.getGameObjects();
            } else {
                initializeGameObjects();
            }
        } else {
            initializeGameObjects();
        }
    }

    private void initializeGameObjects() {
        gameObjects.clear();
        gameObjects.add(new VPSObject("server", "Server", 500, 500));
        gameObjects.add(new VPSObject("database", "Database", 600, 600));
        gameObjects.add(new VPSObject("network", "Network", 700, 700));
        saveGame(); // Save initial state
    }

    private void showConfirmationDialog(String title, String message, Runnable onConfirm) {
        // Create modal container with semi-transparent background
        VBox modalContainer = new VBox(10);
        modalContainer.setStyle("""
                -fx-background-color: rgba(0, 0, 0, 0.7);
                -fx-padding: 20;
                """);
        modalContainer.setAlignment(Pos.CENTER);
        modalContainer.setPrefSize(gameArea.getWidth(), gameArea.getHeight());

        // Create modal content
        VBox modalContent = new VBox(15);
        modalContent.setStyle("""
                -fx-background-color: white;
                -fx-padding: 20;
                -fx-background-radius: 5;
                -fx-min-width: 300;
                -fx-max-width: 300;
                """);
        modalContent.setAlignment(Pos.CENTER);

        // Title
        Label titleLabel = new Label(title);
        titleLabel.setStyle("""
                -fx-font-size: 20px;
                -fx-font-weight: bold;
                """);

        // Message
        Label messageLabel = new Label(message);
        messageLabel.setStyle("""
                -fx-font-size: 14px;
                -fx-wrap-text: true;
                -fx-text-alignment: center;
                """);
        messageLabel.setMaxWidth(250);

        // Buttons container
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        // Confirm Button
        Button confirmButton = createModalButton("Yes");
        confirmButton.setOnAction(e -> {
            onConfirm.run();
            gameArea.getChildren().remove(modalContainer);
        });

        // Cancel Button
        Button cancelButton = createModalButton("No");
        cancelButton.setStyle("""
                -fx-background-color: #95A5A6;
                -fx-text-fill: white;
                -fx-font-size: 14px;
                -fx-padding: 8 20;
                -fx-background-radius: 5;
                -fx-min-width: 120;
                """);
        cancelButton.setOnAction(e -> gameArea.getChildren().remove(modalContainer));

        buttonBox.getChildren().addAll(confirmButton, cancelButton);

        // Add all elements to modal content
        modalContent.getChildren().addAll(
                titleLabel,
                new Separator(),
                messageLabel,
                buttonBox
        );

        // Add modal content to container
        modalContainer.getChildren().add(modalContent);

        // Add click handler to close modal when clicking outside
        modalContainer.setOnMouseClicked(e -> {
            if (e.getTarget() == modalContainer) {
                gameArea.getChildren().remove(modalContainer);
            }
        });

        // Add modal to game area
        gameArea.getChildren().add(modalContainer);
    }

    @Override
    protected Region createContent() {
        BorderPane root = new BorderPane();
        root.setStyle("""
            -fx-background-color: #000000;
            """);

        // สร้างพื้นที่เกม
        gameArea = new StackPane();
        gameArea.setPrefSize(800, 600);

        // สร้าง background layer
        Pane backgroundLayer = new Pane();
        backgroundLayer.setStyle("""
            -fx-background-image: url("/images/rooms/room.png");
            -fx-background-color: transparent;
            -fx-background-size: contain;
            -fx-background-repeat: no-repeat; 
            -fx-background-position: center;
            """);
        backgroundLayer.prefWidthProperty().bind(gameArea.widthProperty());
        backgroundLayer.prefHeightProperty().bind(gameArea.heightProperty());

        // Create game objects container
        Pane objectsContainer = new Pane();
        for (GameObject obj : gameObjects) {
            GameObjectView view = new GameObjectView(obj);
            view.setOnMouseClicked(e -> showObjectDetails(obj));
            objectsContainer.getChildren().add(view);
        }

        // WorldGroup
        Group worldGroup = new Group(backgroundLayer, objectsContainer);
        gameArea.getChildren().add(worldGroup);

        // Create top menu bar
        HBox menuBar = new HBox(20);
        menuBar.setPadding(new Insets(20));
        menuBar.setAlignment(Pos.CENTER_LEFT);
        menuBar.setStyle("-fx-background-color: #2C3E50;");

        Button saveButton = createButton("Save Game");
        saveButton.setOnAction(e -> {
            showConfirmationDialog(
                    "Save Game",
                    "Do you want to save your current progress?",
                    this::saveGame
            );
        });

        Button menuButton = createButton("Main Menu");
        menuButton.setOnAction(e -> {
            showConfirmationDialog(
                    "Return to Main Menu",
                    "Do you want to save and return to the main menu?",
                    () -> {
                        saveGame();
                        navigator.showMainMenu();
                    }
            );
        });

        menuBar.getChildren().addAll(saveButton, menuButton);

        // Add zoom functionality
        gameArea.setOnScroll(e -> {
            double zoomFactor = 1.05;
            if (e.getDeltaY() < 0) {
                zoomFactor = 1.0 / zoomFactor;
            }
            worldGroup.setScaleX(worldGroup.getScaleX() * zoomFactor);
            worldGroup.setScaleY(worldGroup.getScaleY() * zoomFactor);
            e.consume();
        });

        // Set layout
        root.setCenter(gameArea);
        root.setTop(menuBar);  // ย้าย menuBar มาไว้ท้ายสุด

        // Add key event handler
        root.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                saveGame();
                navigator.showPlayMenu();
            }
        });

        root.setFocusTraversable(true);
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                root.requestFocus();
            }
        });

        return root;
    }

    private void showObjectDetails(GameObject obj) {
        // Create modal container
        VBox modalContainer = new VBox(10);
        modalContainer.setStyle("""
                -fx-background-color: rgba(0, 0, 0, 0.7);
                -fx-padding: 20;
                """);
        modalContainer.setAlignment(Pos.CENTER);
        modalContainer.setPrefSize(gameArea.getWidth(), gameArea.getHeight());

        // Create modal content
        VBox modalContent = new VBox(15);
        modalContent.setStyle("""
                -fx-background-color: white;
                -fx-padding: 20;
                -fx-background-radius: 5;
                -fx-min-width: 300;
                -fx-max-width: 300;
                """);
        modalContent.setAlignment(Pos.CENTER);

        // Title
        Label titleLabel = new Label(obj.getName());
        titleLabel.setStyle("""
                -fx-font-size: 24px;
                -fx-font-weight: bold;
                """);

        // Status
        Label statusLabel = new Label("Status: " + obj.getStatus());
        statusLabel.setStyle("-fx-font-size: 16px;");

        // Level
        Label levelLabel = new Label("Level: " + obj.getLevel());
        levelLabel.setStyle("-fx-font-size: 16px;");

        // Upgrade Button
        Button upgradeButton = createModalButton("Upgrade");
        upgradeButton.setOnAction(e -> {
            obj.upgrade(saveManager.loadGame());
            levelLabel.setText("Level: " + obj.getLevel());
            statusLabel.setText("Status: " + obj.getStatus());
            saveGame();
        });

        // Close Button
        Button closeButton = createModalButton("Close");
        closeButton.setOnAction(e -> gameArea.getChildren().remove(modalContainer));

        // Add all elements to modal content
        modalContent.getChildren().addAll(
                titleLabel,
                new Separator(),
                statusLabel,
                levelLabel,
                upgradeButton,
                closeButton
        );

        // Add modal content to container
        modalContainer.getChildren().add(modalContent);

        // Add click handler to close modal when clicking outside
        modalContainer.setOnMouseClicked(e -> {
            if (e.getTarget() == modalContainer) {
                gameArea.getChildren().remove(modalContainer);
            }
        });

        // Add modal to game area
        gameArea.getChildren().add(modalContainer);
    }

    private Button createModalButton(String text) {
        Button button = new Button(text);
        button.setStyle("""
                -fx-background-color: #3498DB;
                -fx-text-fill: white;
                -fx-font-size: 14px;
                -fx-padding: 8 20;
                -fx-background-radius: 5;
                -fx-min-width: 120;
                """);

        button.setOnMouseEntered(e ->
                button.setStyle(button.getStyle().replace("#3498DB", "#2980B9"))
        );

        button.setOnMouseExited(e ->
                button.setStyle(button.getStyle().replace("#2980B9", "#3498DB"))
        );

        return button;
    }

    private Button createButton(String text) {
        Button button = new Button(text);
        button.setStyle("""
                -fx-background-color: #3498DB;
                -fx-text-fill: white;
                -fx-font-size: 14px;
                -fx-padding: 8px 15px;
                -fx-background-radius: 5;
                """);
        return button;
    }

    private void saveGame() {
        GameState state = new GameState(gameObjects);
        saveManager.saveGame(state);
    }
} 
package com.vpstycoon.application;

import com.vpstycoon.config.DefaultGameConfig;
import com.vpstycoon.config.GameConfig;
import com.vpstycoon.event.GameEventBus;
import com.vpstycoon.event.SettingsChangedEvent;
import com.vpstycoon.manager.GameManager;
import com.vpstycoon.manager.ResourceManager;
import com.vpstycoon.model.GameObject;
import com.vpstycoon.model.GameState;
import com.vpstycoon.model.company.Company;
import com.vpstycoon.screen.JavaFXScreenManager;
import com.vpstycoon.screen.ScreenManager;
import com.vpstycoon.thread.RequestGenerator;
import com.vpstycoon.ui.common.Navigator;
import com.vpstycoon.ui.common.SceneController;
import com.vpstycoon.ui.cutscene.CutsceneScreen;
import com.vpstycoon.ui.game.GameplayScreen;
import com.vpstycoon.ui.menu.MainMenuScreen;
import com.vpstycoon.ui.settings.SettingsScreen;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.util.List;

public class GameApplication extends Application implements Navigator, ResourceManager.ResourceLoadingListener {
    private Stage primaryStage;
    private GameConfig gameConfig;
    private ScreenManager screenManager;
    private MainMenuScreen mainMenuScreen;
    private SettingsScreen settingsScreen;
    private GameplayScreen gameplayScreen;
    private Stage loadingStage;
    private Label statusLabel;

    @Override
    public void start(Stage primaryStage) {
        System.out.println("Debug: Starting GameApplication");

        this.primaryStage = primaryStage;
        this.gameConfig = createGameConfig();
        this.screenManager = new JavaFXScreenManager(gameConfig, primaryStage);

        try {
            System.out.println("กำลังโหลดไอคอนแอพพลิเคชัน...");
            Image appIcon = new Image(getClass().getResourceAsStream("/images/logo/GameIcon.PNG"));
            if (appIcon.isError()) {
                System.err.println("เกิดข้อผิดพลาดขณะโหลดไอคอน: " + appIcon.getException().getMessage());
            } else {
                System.out.println("โหลดไอคอนสำเร็จ ขนาด: " + appIcon.getWidth() + "x" + appIcon.getHeight());
                primaryStage.getIcons().add(appIcon);
                System.out.println("เพิ่มไอคอนให้ Stage เรียบร้อยแล้ว");
            }
        } catch (Exception e) {
            System.err.println("ไม่สามารถโหลดไอคอนแอพพลิเคชันได้: " + e.getMessage());
            e.printStackTrace();
        }

        initializeLoadingScreen();

        Thread initThread = new Thread(() -> {
            try {
                ResourceManager resourceManager = ResourceManager.getInstance();
                resourceManager.setResourceLoadingListener(GameApplication.this);

                resourceManager.preloadAssets();

                boolean preloadComplete = resourceManager.waitForPreload(60000);

                if (!preloadComplete) {
                    System.err.println("Warning: Resource preloading timed out after 60 seconds");
                }

                Platform.runLater(() -> {
                    if (statusLabel != null) {
                        statusLabel.setText("เริ่มเตรียมเกม กรุณารอสักครู่...");
                    }
                });

                finishInitialization();
            } catch (Exception e) {
                System.err.println("Error during game initialization: " + e.getMessage());
                e.printStackTrace();

                Platform.runLater(() -> {
                    hideLoadingScreen();
                    showAlert("Error", "Failed to initialize game: " + e.getMessage());
                    primaryStage.show();
                });
            }
        });

        initThread.setDaemon(true);
        initThread.start();
    }

    private void finishInitialization() {
        try {
            initializeGame();

            Platform.runLater(() -> {
                try {
                    ResourceManager.getInstance().getAudioManager().playMusic("Buckshot_Roulette_OST.mp3");

                    if (statusLabel != null) {
                        statusLabel.setText("โหลดเสร็จสมบูรณ์ กำลังเริ่มเข้าสู่เกม...");
                        statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2ECC71;");
                    }

                    System.out.println("กำลังเตรียม CutsceneScreen...");
                    CutsceneScreen cutsceneScreen = new CutsceneScreen(this);

                    System.out.println("CutsceneScreen พร้อมแล้ว");

                    PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
                    pause.setOnFinished(e -> {
                        screenManager.prepareScreen(cutsceneScreen);
                        primaryStage.show();

                        if (loadingStage != null && loadingStage.isShowing()) {
                            System.out.println("กำลังซ่อนหน้า Loading หลังจากแสดง CutsceneScreen...");

                            Stage startupLoadingStage = loadingStage;
                            javafx.scene.layout.StackPane root = (javafx.scene.layout.StackPane) startupLoadingStage.getScene().getRoot();
                            javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(Duration.millis(1000), root);
                            fadeOut.setFromValue(1.0);
                            fadeOut.setToValue(0.0);
                            fadeOut.setOnFinished(event -> {
                                startupLoadingStage.close();
                                if (loadingStage == startupLoadingStage) {
                                    loadingStage = null;
                                }
                                System.out.println("ปิดหน้าจอโหลดเรียบร้อย");
                                System.out.println("โหลดทรัพยากรเสร็จสมบูรณ์");
                            });
                            fadeOut.play();
                        }
                    });
                    pause.play();
                } catch (Exception e) {
                    System.err.println("Error in JavaFX thread: " + e.getMessage());
                    e.printStackTrace();
                    hideLoadingScreen();
                    showAlert("Error", "Failed to initialize game UI: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            System.err.println("Error during game initialization: " + e.getMessage());
            e.printStackTrace();

            Platform.runLater(() -> {
                hideLoadingScreen();
                showAlert("Error", "Failed to initialize game: " + e.getMessage());
                primaryStage.show();
            });
        }
    }

    private GameConfig createGameConfig() {
        return DefaultGameConfig.getInstance();
    }

    private void initializeGame() {
        gameConfig.load();

        Platform.runLater(() -> {
            try {
                SceneController.initialize(primaryStage, gameConfig, screenManager);

                createScreens();

                primaryStage.setTitle("VPS Tycoon");
                primaryStage.setResizable(false);
                primaryStage.setOnCloseRequest(e -> shutdown());

                GameEventBus.getInstance().subscribe(
                    SettingsChangedEvent.class,
                    event -> Platform.runLater(() ->
                        SceneController.getInstance().updateResolution()
                    )
                );
            } catch (Exception e) {
                System.err.println("Error initializing game UI: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void createScreens() {
        mainMenuScreen = new MainMenuScreen(gameConfig, screenManager, this);
        settingsScreen = new SettingsScreen(gameConfig, screenManager, this::showMainMenu);
    }

    @Override
    public void showMainMenu() {
        if (ResourceManager.getInstance().getGameTimeController() != null) {
            System.out.println("หยุด GameTimeController ก่อนไปเมนูหลัก");
            ResourceManager.getInstance().getGameTimeController().stopTime();
        }

        mainMenuScreen.show();
    }

    @Override
    public void showSettings() {
        settingsScreen.show();
    }

    @Override
    public void startNewGame() {
        System.out.println("=========== STARTING NEW GAME ===========");

        showLoadingScreen("Starting New Game...");

        if (ResourceManager.getInstance().getGameTimeController() != null) {
            System.out.println("หยุด GameTimeController ก่อนเริ่มเกมใหม่");
            ResourceManager.getInstance().getGameTimeController().stopTime();
        }

        new Thread(() -> {
            try {
                GameManager.resetInstance();

                ResourceManager.getInstance().resetRackAndInventory();

                ResourceManager.getInstance().getSaveManager().deleteSave();
                System.out.println("ลบไฟล์เซฟเดิมเรียบร้อย");

                ResourceManager.getInstance().resetMessengerData();

                ResourceManager.getInstance().resetGameTime();

                GameState existingState = ResourceManager.getInstance().getCurrentState();
                if (existingState != null) {
                    existingState.clearState();
                    System.out.println("ล้างค่า GameState ปัจจุบันเรียบร้อย");
                }

                GameState newState = new GameState();

                Company newCompany = new Company();
                newCompany.setMoney(25_000);
                newCompany.setRating(1.0);
                newState.setCompany(newCompany);

                newState.setLocalDateTime(LocalDateTime.of(2000, 1, 1, 0, 0, 0));

                ResourceManager.getInstance().setCurrentState(newState);
                System.out.println("อัพเดท GameState ใน ResourceManager แล้ว");

                System.out.println("สร้าง SkillPointsSystem ใหม่");
                ResourceManager resourceManager = ResourceManager.getInstance();
                resourceManager.resetSkillPointsSystem();

                if (resourceManager.getSkillPointsSystem() != null) {
                    newState.setSkillLevels(resourceManager.getSkillPointsSystem().getSkillLevelsMap());
                    System.out.println("บันทึก SkillLevels ลงใน GameState เรียบร้อย");
                }

                if (ResourceManager.getInstance().getRequestGenerator() != null) {
                    ResourceManager.getInstance().getRequestGenerator().stopGenerator();
                }
                RequestGenerator requestGenerator = new RequestGenerator(ResourceManager.getInstance().getRequestManager());
                ResourceManager.getInstance().setRequestGenerator(requestGenerator);
                requestGenerator.start();
                System.out.println("เริ่มการทำงานของ RequestGenerator แล้ว");

                final GameState finalState = newState;
                Platform.runLater(() -> {
                    if (gameplayScreen != null) {
                        gameplayScreen.release();
                    }

                    gameplayScreen = new GameplayScreen(gameConfig, screenManager, this, finalState);
                    gameplayScreen.show();

                    hideLoadingScreen();

                    System.out.println("เริ่มเกมใหม่เรียบร้อย");
                });
            } catch (Exception e) {
                e.printStackTrace();

                Platform.runLater(() -> {
                    hideLoadingScreen();
                    showAlert("Error", "Failed to start new game: " + e.getMessage());
                });
            }
        }).start();
    }

    private void showLoadingScreen(String message) {
        Platform.runLater(() -> {
            try {
                if (loadingStage == null) {
                    loadingStage = new Stage();
                    loadingStage.initOwner(primaryStage);
                    loadingStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
                    loadingStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

                    StackPane root = new StackPane();
                    root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7); -fx-padding: 20;");

                    VBox content = new VBox(15);
                    content.setAlignment(Pos.CENTER);

                    Label loadingLabel = new Label(message);
                    loadingLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");

                    javafx.scene.control.ProgressIndicator progress = new javafx.scene.control.ProgressIndicator();
                    progress.setMaxSize(50, 50);

                    content.getChildren().addAll(loadingLabel, progress);
                    root.getChildren().add(content);

                    Scene scene = new Scene(root, 300, 150);
                    scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
                    loadingStage.setScene(scene);

                    loadingStage.setX(primaryStage.getX() + (primaryStage.getWidth() - 300) / 2);
                    loadingStage.setY(primaryStage.getY() + (primaryStage.getHeight() - 150) / 2);
                }

                loadingStage.show();
            } catch (Exception e) {
                System.err.println("Error showing loading screen: " + e.getMessage());
            }
        });
    }

    private void hideLoadingScreen() {
        Platform.runLater(() -> {
            try {
                if (loadingStage != null && loadingStage.isShowing()) {
                    System.out.println("ปิดหน้าจอโหลด...");
                    loadingStage.close();
                    loadingStage = null;
                    System.out.println("ปิดหน้าจอโหลดเรียบร้อย");
                    System.out.println("โหลดทรัพยากรเสร็จสมบูรณ์");
                }
            } catch (Exception e) {
                System.err.println("เกิดข้อผิดพลาดในการปิดหน้าจอโหลด: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @Override
    public void showLoadGame() {
        try {
            if (!ResourceManager.getInstance().getSaveManager().saveExists()) {
                showAlert("No saved game found", "There is no saved game to continue.");
                return;
            }
            GameState savedState = ResourceManager.getInstance().loadGameState();
            gameplayScreen = new GameplayScreen(gameConfig, screenManager, this, savedState);
            gameplayScreen.show();
        } catch (Exception e) {
            showAlert("Error", "Could not load saved game: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void shutdown() {
        try {
            System.out.println("กำลังบันทึกเกมก่อนปิดแอพพลิเคชัน...");

            if (gameplayScreen != null) {
                try {
                    Company company = ResourceManager.getInstance().getCompany();
                    if (company != null) {
                        List<GameObject> gameObjects =
                            ResourceManager.getInstance().getCurrentState().getGameObjects();

                        GameState state = new GameState(company, gameObjects);
                        state.setLocalDateTime(ResourceManager.getInstance().getGameTimeController().getGameTimeManager().getGameDateTime());

                        ResourceManager.getInstance().saveGameState(state);
                        System.out.println("บันทึกเกมเรียบร้อยแล้ว");
                    }
                } catch (Exception saveEx) {
                    System.err.println("เกิดข้อผิดพลาดในการบันทึกเกม: " + saveEx.getMessage());
                    saveEx.printStackTrace();
                }
            }

            gameConfig.save();
            Platform.exit();
        } catch (Exception e) {
            System.err.println("Error during shutdown: " + e.getMessage());
            System.exit(1);
        }
    }

    private void initializeLoadingScreen() {
        Platform.runLater(() -> {
            try {
                StackPane root = new StackPane();
                root.setStyle("-fx-background-color: #121212;");

                javafx.scene.layout.Region darkBackground = new javafx.scene.layout.Region();
                darkBackground.setStyle("""
                    -fx-background-color: black;
                    -fx-background-radius: 0;
                """);

                javafx.scene.layout.GridPane gridLines = new javafx.scene.layout.GridPane();
                gridLines.setHgap(20);
                gridLines.setVgap(20);
                gridLines.setStyle("""
                    -fx-background-color: rgba(0, 0, 0, 0);
                    -fx-grid-lines-visible: true;
                    -fx-border-color: rgba(58, 19, 97, 0.3);
                    -fx-border-width: 1;
                """);

                int cols = 20;
                int rows = 20;

                for (int i = 0; i < cols; i++) {
                    javafx.scene.layout.ColumnConstraints colConstraint = new javafx.scene.layout.ColumnConstraints();
                    colConstraint.setPercentWidth(100.0 / cols);
                    gridLines.getColumnConstraints().add(colConstraint);
                }

                for (int i = 0; i < rows; i++) {
                    javafx.scene.layout.RowConstraints rowConstraint = new javafx.scene.layout.RowConstraints();
                    rowConstraint.setPercentHeight(100.0 / rows);
                    gridLines.getRowConstraints().add(rowConstraint);
                }

                VBox content = new VBox(20);
                content.setAlignment(Pos.CENTER);
                content.setPadding(new Insets(50));

                Label titleLabel = new Label("VPS Tycoon");
                titleLabel.setStyle("-fx-font-size: 28px; -fx-text-fill: white; -fx-font-weight: bold;");

                javafx.scene.effect.DropShadow glow = new javafx.scene.effect.DropShadow();
                glow.setColor(javafx.scene.paint.Color.web("#8A2BE2"));
                glow.setRadius(20);
                glow.setSpread(0.2);
                titleLabel.setEffect(glow);

                Label loadingLabel = new Label("กำลังโหลดทรัพยากร...");
                loadingLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white;");

                javafx.scene.control.ProgressIndicator progressIndicator = new javafx.scene.control.ProgressIndicator();
                progressIndicator.setProgress(-1);
                progressIndicator.setStyle("""
                    -fx-progress-color: #8A2BE2;
                """);
                progressIndicator.setPrefSize(50, 50);

                statusLabel = new Label("กำลังเริ่มเกม...");
                statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2ecc71;");

                content.getChildren().addAll(titleLabel, loadingLabel, progressIndicator, statusLabel);

                root.getChildren().addAll(darkBackground, gridLines, content);

                Scene scene = new Scene(root, 500, 300);
                scene.setFill(javafx.scene.paint.Color.TRANSPARENT);

                if (loadingStage == null) {
                    loadingStage = new Stage();
                    loadingStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
                    loadingStage.setResizable(false);
                }

                loadingStage.setScene(scene);
                loadingStage.show();

                javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
                loadingStage.setX((screenBounds.getWidth() - 500) / 2);
                loadingStage.setY((screenBounds.getHeight() - 300) / 2);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(500), root);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            } catch (Exception e) {
                System.err.println("Error showing loading screen: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onResourceLoading(String resourcePath) {
        if (resourcePath != null && !resourcePath.trim().isEmpty() && statusLabel != null) {
            statusLabel.setText("กำลังโหลด: " + getSimpleFileName(resourcePath));
        }
    }

    @Override
    public void onResourceLoadingComplete(int totalLoaded) {
        if (statusLabel != null) {
            statusLabel.setText("โหลดทรัพยากรครบถ้วน " + totalLoaded + " รายการ");
            statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2ECC71;");
        }
    }

    private String getSimpleFileName(String path) {
        if (path == null) return "";
        int lastSlash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        if (lastSlash >= 0 && lastSlash < path.length() - 1) {
            return path.substring(lastSlash + 1);
        }
        return path;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

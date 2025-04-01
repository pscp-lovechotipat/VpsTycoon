package com.vpstycoon.ui.game;

import com.vpstycoon.application.FontLoader;
import com.vpstycoon.audio.AudioManager;
import com.vpstycoon.game.GameObject;
import com.vpstycoon.game.GameState;
import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.ui.components.buttons.Menu.MenuButton;
import com.vpstycoon.ui.components.buttons.Menu.MenuButtonType;
import com.vpstycoon.ui.navigation.Navigator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.util.List;

public class ResumeScreen extends StackPane {
    private final Navigator navigator;
    private final Runnable onResumeGame;
    private final Runnable onOpenSettings;
    private AudioManager audioManager;

    public ResumeScreen(Navigator navigator, Runnable onResumeGame) {
        this.navigator = navigator;
        this.onResumeGame = onResumeGame;
        this.onOpenSettings = null;
        this.audioManager = ResourceManager.getInstance().getAudioManager();

        setViewOrder(-1000);

        pauseAllGameThreads();

        setupUI();
    }
    
    public ResumeScreen(Navigator navigator, Runnable onResumeGame, Runnable onOpenSettings) {
        this.navigator = navigator;
        this.onResumeGame = onResumeGame;
        this.onOpenSettings = onOpenSettings;
        this.audioManager = ResourceManager.getInstance().getAudioManager();
        

        setViewOrder(-1000);

        pauseAllGameThreads();

        setupUI();
    }

    private void setupUI() {

        Rectangle background = new Rectangle();
        background.widthProperty().bind(widthProperty());
        background.heightProperty().bind(heightProperty());
        

        background.setFill(Color.rgb(30, 15, 50, 0.9));
        

        background.setStroke(Color.rgb(180, 50, 255, 0.2));
        background.setStrokeWidth(1);


        VBox menuBox = new VBox(20);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setPadding(new Insets(30));
        menuBox.setMaxWidth(350);
        menuBox.setMaxHeight(450);
        menuBox.setMinWidth(350);
        menuBox.setMinHeight(450);
        

        menuBox.setStyle(
            "-fx-background-color: rgba(30, 15, 50, 0.8);" +
            "-fx-background-radius: 2;" +
            "-fx-border-color: #ff00ff, #00ffff;" +
            "-fx-border-width: 2, 1;" +
            "-fx-border-radius: 2;" +
            "-fx-border-insets: 0, 3;" +
            "-fx-effect: dropshadow(gaussian, #ff00ff, 5, 0.2, 0, 0);"
        );
        

        Text titleText = new Text("PAUSED");
        titleText.setFont(FontLoader.SUBTITLE_FONT);

        titleText.setFill(Color.WHITE);
        

        Glow glow = new Glow(0.8);
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(255, 0, 255, 0.7));
        shadow.setRadius(5);
        titleText.setEffect(shadow);
        

        MenuButton resumeButton = new MenuButton(MenuButtonType.RESUME);
        resumeButton.setOnAction(e -> {
            audioManager.playSoundEffect("click.wav");

            resumeAllGameThreads();

            onResumeGame.run();
        });
        styleButton(resumeButton);


        MenuButton settingsButton = new MenuButton(MenuButtonType.SETTINGS);
        settingsButton.setOnAction(e -> {
            audioManager.playSoundEffect("click.wav");

            resumeAllGameThreads();

            onResumeGame.run();

            if (onOpenSettings != null) {
                onOpenSettings.run();
            } else {
                navigator.showInGameSettings();
            }
        });
        styleButton(settingsButton);


        MenuButton mainMenuButton = new MenuButton(MenuButtonType.MAIN_MENU);
        mainMenuButton.setOnAction(e ->{
            audioManager.playSoundEffect("click.wav");
            

            try {
                System.out.println("กำลังบันทึกเกมก่อนออกไปเมนูหลัก...");
                ResourceManager.getInstance().pushNotification("บันทึกเกม", "กำลังบันทึกความก้าวหน้าของคุณ...");

                stopAllGameThreads();
                

                Company company = ResourceManager.getInstance().getCompany();
                List<GameObject> gameObjects = 
                    ResourceManager.getInstance().getCurrentState().getGameObjects();
                
                GameState state = new GameState(company, gameObjects);
                state.setLocalDateTime(ResourceManager.getInstance().getGameTimeController().getGameTimeManager().getGameDateTime());
                

                ResourceManager.getInstance().saveGameState(state);
                System.out.println("บันทึกเกมเรียบร้อยแล้ว");
            } catch (Exception ex) {
                System.err.println("เกิดข้อผิดพลาดในการบันทึกเกม: " + ex.getMessage());
                ex.printStackTrace();
            }
            
            audioManager.resumeMusic();
            navigator.showMainMenu();
        });
        styleButton(mainMenuButton);


        MenuButton quitButton = new MenuButton(MenuButtonType.QUIT);
        quitButton.setOnAction(e -> {
            audioManager.playSoundEffect("click.wav");
            System.exit(0);
        });
        styleButton(quitButton);

        menuBox.getChildren().addAll(titleText, resumeButton, settingsButton, mainMenuButton, quitButton);


        BorderPane centerPane = new BorderPane();
        centerPane.setCenter(menuBox);

        getChildren().addAll(background, centerPane);


        background.setOnMouseClicked(e -> {
            if (e.getTarget() == background) {
                resumeAllGameThreads();

                onResumeGame.run();
            }
        });
    }
    

    private void styleButton(MenuButton button) {

        DropShadow buttonGlow = new DropShadow();
        buttonGlow.setColor(Color.rgb(180, 50, 255, 0.7));
        buttonGlow.setRadius(5);
        button.setEffect(buttonGlow);
        

        button.setPrefWidth(250);
    }

    private void pauseAllGameThreads() {
        if (ResourceManager.getInstance().getGameTimeController() != null) {
            System.out.println("หยุด GameTimeController เมื่อแสดง Pause Menu");
            ResourceManager.getInstance().getGameTimeController().stopTime();
        }

        if (ResourceManager.getInstance().getGameEvent() != null &&
            ResourceManager.getInstance().getGameEvent().isRunning()) {
            System.out.println("หยุด GameEvent เมื่อแสดง Pause Menu");
            ResourceManager.getInstance().getGameEvent().pauseEvent();
        }

        if (ResourceManager.getInstance().getRequestGenerator() != null) {
            System.out.println("หยุด RequestGenerator เมื่อแสดง Pause Menu");
            ResourceManager.getInstance().getRequestGenerator().pauseGenerator();
        }
    }

    private void stopAllGameThreads() {
        if (ResourceManager.getInstance().getGameTimeController() != null) {
            System.out.println("หยุด GameTimeController อย่างถาวรก่อนออกไปเมนูหลัก");
            ResourceManager.getInstance().getGameTimeController().stopTime();
        }

        if (ResourceManager.getInstance().getGameEvent() != null &&
            ResourceManager.getInstance().getGameEvent().isRunning()) {
            System.out.println("หยุด GameEvent อย่างถาวรก่อนออกไปเมนูหลัก");
            ResourceManager.getInstance().getGameEvent().stopEvent(); 
        }

        if (ResourceManager.getInstance().getRequestGenerator() != null) {
            System.out.println("หยุด RequestGenerator อย่างถาวรก่อนออกไปเมนูหลัก");
            ResourceManager.getInstance().getRequestGenerator().stopGenerator(); 
        }
    }

    private void resumeAllGameThreads() {
        if (ResourceManager.getInstance().getGameTimeController() != null) {
            System.out.println("เริ่ม GameTimeController เมื่อกลับไปเล่นเกม");
            ResourceManager.getInstance().getGameTimeController().startTime();
        }

        if (ResourceManager.getInstance().getGameEvent() != null) {
            System.out.println("เริ่ม GameEvent เมื่อกลับไปเล่นเกม");
            ResourceManager.getInstance().getGameEvent().resumeEvent();
        }

        if (ResourceManager.getInstance().getRequestGenerator() != null) {
            System.out.println("เริ่ม RequestGenerator เมื่อกลับไปเล่นเกม");
            ResourceManager.getInstance().getRequestGenerator().resumeGenerator();
        }
    }
}


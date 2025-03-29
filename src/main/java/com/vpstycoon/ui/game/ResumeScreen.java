package com.vpstycoon.ui.game;

import com.vpstycoon.FontLoader;
import com.vpstycoon.audio.AudioManager;
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
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.GameState;
import java.util.List;
import com.vpstycoon.game.GameObject;

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
        
        // Set a high z-index to ensure this screen is displayed on top
        setViewOrder(-1000);
        
        // Stop all game threads when pause menu is shown
        pauseAllGameThreads();
        
        setupUI();
    }
    
    public ResumeScreen(Navigator navigator, Runnable onResumeGame, Runnable onOpenSettings) {
        this.navigator = navigator;
        this.onResumeGame = onResumeGame;
        this.onOpenSettings = onOpenSettings;
        this.audioManager = ResourceManager.getInstance().getAudioManager();
        
        // Set a high z-index to ensure this screen is displayed on top
        setViewOrder(-1000);
        
        // Stop all game threads when pause menu is shown
        pauseAllGameThreads();
        
        setupUI();
    }

    private void setupUI() {
        // Cyberpunk gradient background
        Rectangle background = new Rectangle();
        background.widthProperty().bind(widthProperty());
        background.heightProperty().bind(heightProperty());
        
        // Replace gradient with solid color to eliminate shaking
        background.setFill(Color.rgb(30, 15, 50, 0.9));  // Dark purple solid color
        
        // Add some pixel-like noise effect to the background (optional)
        background.setStroke(Color.rgb(180, 50, 255, 0.2));
        background.setStrokeWidth(1);

        // Create a cyberpunk-styled menu box
        VBox menuBox = new VBox(20);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setPadding(new Insets(30));
        menuBox.setMaxWidth(350);
        menuBox.setMaxHeight(450);
        
        // Cyberpunk styled menu background with pixel-like border
        menuBox.setStyle(
            "-fx-background-color: rgba(30, 15, 50, 0.8);" +
            "-fx-background-radius: 2;" +
            "-fx-border-color: #ff00ff, #00ffff;" +
            "-fx-border-width: 2, 1;" +
            "-fx-border-radius: 2;" +
            "-fx-border-insets: 0, 3;" +
            "-fx-effect: dropshadow(gaussian, #ff00ff, 5, 0.2, 0, 0);"
        );
        
        // Add a title to the menu
        Text titleText = new Text("PAUSED");
        titleText.setFont(FontLoader.SUBTITLE_FONT);
//        titleText.setFont(Font.font("Monospace", FontWeight.BOLD, 28));
        titleText.setFill(Color.WHITE);
        
        // Add glow effect to the title
        Glow glow = new Glow(0.8);
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(255, 0, 255, 0.7));
        shadow.setRadius(5);
        titleText.setEffect(shadow);
        
        // ปุ่ม Resume (กลับไปเล่นเกม)
        MenuButton resumeButton = new MenuButton(MenuButtonType.RESUME);
        resumeButton.setOnAction(e -> {
            audioManager.playSoundEffect("click.wav");
            
            // Start all game threads when resuming game
            resumeAllGameThreads();
            
            onResumeGame.run();
        });
        styleButton(resumeButton);

        // Settings Button
        MenuButton settingsButton = new MenuButton(MenuButtonType.SETTINGS);
        settingsButton.setOnAction(e -> {
            audioManager.playSoundEffect("click.wav");
            
            // Start all game threads when closing pause menu
            resumeAllGameThreads();
            
            // Close resume screen first
            onResumeGame.run();
            // Then open settings
            if (onOpenSettings != null) {
                onOpenSettings.run();
            } else {
                navigator.showInGameSettings();
            }
        });
        styleButton(settingsButton);

        // ปุ่ม Main Menu (กลับไปหน้าหลัก)
        MenuButton mainMenuButton = new MenuButton(MenuButtonType.MAIN_MENU);
        mainMenuButton.setOnAction(e ->{
            audioManager.playSoundEffect("click.wav");
            
            // บันทึกเกมก่อนออกไปเมนูหลัก
            try {
                System.out.println("กำลังบันทึกเกมก่อนออกไปเมนูหลัก...");
                ResourceManager.getInstance().pushNotification("บันทึกเกม", "กำลังบันทึกความก้าวหน้าของคุณ...");
                
                // หยุดการทำงานของทุก thread อย่างถาวรก่อนออกไปเมนูหลัก
                stopAllGameThreads();
                
                // สร้าง GameState จากข้อมูลปัจจุบัน
                Company company = ResourceManager.getInstance().getCompany();
                List<GameObject> gameObjects = 
                    ResourceManager.getInstance().getCurrentState().getGameObjects();
                
                GameState state = new GameState(company, gameObjects);
                state.setLocalDateTime(ResourceManager.getInstance().getGameTimeController().getGameTimeManager().getGameDateTime());
                
                // บันทึกเกม
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

        // ปุ่ม Quit (ออกจากเกม)
        MenuButton quitButton = new MenuButton(MenuButtonType.QUIT);
        quitButton.setOnAction(e -> {
            audioManager.playSoundEffect("click.wav");
            System.exit(0);
        });
        styleButton(quitButton);

        menuBox.getChildren().addAll(titleText, resumeButton, settingsButton, mainMenuButton, quitButton);

        // Add a border pane to center the menu box
        BorderPane centerPane = new BorderPane();
        centerPane.setCenter(menuBox);

        getChildren().addAll(background, centerPane);

        // คลิกนอกเมนูเพื่อกลับไปเล่นเกม
        background.setOnMouseClicked(e -> {
            if (e.getTarget() == background) {
                // Start all game threads when resuming game
                resumeAllGameThreads();
                
                onResumeGame.run();
            }
        });
    }
    
    // Helper method to style buttons with cyberpunk theme
    private void styleButton(MenuButton button) {
        // Add glow effect to buttons
        DropShadow buttonGlow = new DropShadow();
        buttonGlow.setColor(Color.rgb(180, 50, 255, 0.7));
        buttonGlow.setRadius(5);
        button.setEffect(buttonGlow);
        
        // Make buttons slightly larger
        button.setPrefWidth(250);
    }
    
    /**
     * หยุดการทำงานของทุก thread ในเกมเมื่อแสดง Pause Menu
     */
    private void pauseAllGameThreads() {
        // หยุด GameTimeController
        if (ResourceManager.getInstance().getGameTimeController() != null) {
            System.out.println("หยุด GameTimeController เมื่อแสดง Pause Menu");
            ResourceManager.getInstance().getGameTimeController().stopTime();
        }
        
        // หยุด GameEvent
        if (ResourceManager.getInstance().getGameEvent() != null &&
            ResourceManager.getInstance().getGameEvent().isRunning()) {
            System.out.println("หยุด GameEvent เมื่อแสดง Pause Menu");
            ResourceManager.getInstance().getGameEvent().pauseEvent();
        }
        
        // หยุด RequestGenerator ถ้ามี
        if (ResourceManager.getInstance().getRequestGenerator() != null) {
            System.out.println("หยุด RequestGenerator เมื่อแสดง Pause Menu");
            ResourceManager.getInstance().getRequestGenerator().pauseGenerator();
        }
    }
    
    /**
     * หยุดการทำงานของทุก thread ในเกมอย่างถาวรก่อนออกไปเมนูหลัก
     */
    private void stopAllGameThreads() {
        // หยุด GameTimeController อย่างถาวร
        if (ResourceManager.getInstance().getGameTimeController() != null) {
            System.out.println("หยุด GameTimeController อย่างถาวรก่อนออกไปเมนูหลัก");
            ResourceManager.getInstance().getGameTimeController().stopTime();
        }
        
        // หยุด GameEvent อย่างถาวร
        if (ResourceManager.getInstance().getGameEvent() != null &&
            ResourceManager.getInstance().getGameEvent().isRunning()) {
            System.out.println("หยุด GameEvent อย่างถาวรก่อนออกไปเมนูหลัก");
            ResourceManager.getInstance().getGameEvent().stopEvent(); // ใช้ stopEvent แทน pauseEvent
        }
        
        // หยุด RequestGenerator อย่างถาวร
        if (ResourceManager.getInstance().getRequestGenerator() != null) {
            System.out.println("หยุด RequestGenerator อย่างถาวรก่อนออกไปเมนูหลัก");
            ResourceManager.getInstance().getRequestGenerator().stopGenerator(); // ใช้ stopGenerator แทน pauseGenerator ถ้ามี
        }
    }
    
    /**
     * เริ่มการทำงานของทุก thread ในเกมเมื่อกลับไปเล่นเกม
     */
    private void resumeAllGameThreads() {
        // เริ่ม GameTimeController
        if (ResourceManager.getInstance().getGameTimeController() != null) {
            System.out.println("เริ่ม GameTimeController เมื่อกลับไปเล่นเกม");
            ResourceManager.getInstance().getGameTimeController().startTime();
        }
        
        // เริ่ม GameEvent
        if (ResourceManager.getInstance().getGameEvent() != null) {
            System.out.println("เริ่ม GameEvent เมื่อกลับไปเล่นเกม");
            ResourceManager.getInstance().getGameEvent().resumeEvent();
        }
        
        // เริ่ม RequestGenerator ถ้ามี
        if (ResourceManager.getInstance().getRequestGenerator() != null) {
            System.out.println("เริ่ม RequestGenerator เมื่อกลับไปเล่นเกม");
            ResourceManager.getInstance().getRequestGenerator().resumeGenerator();
        }
    }
}
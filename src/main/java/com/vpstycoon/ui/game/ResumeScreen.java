package com.vpstycoon.ui.game;

import com.vpstycoon.FontLoader;
import com.vpstycoon.audio.AudioManager;
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

public class ResumeScreen extends StackPane {
    private final Navigator navigator;
    private final Runnable onResumeGame;
    private AudioManager audioManager;

    public ResumeScreen(Navigator navigator, Runnable onResumeGame) {
        this.navigator = navigator;
        this.onResumeGame = onResumeGame;
        this.audioManager = AudioManager.getInstance();
        setupUI();
    }

    private void setupUI() {
        // Cyberpunk gradient background
        Rectangle background = new Rectangle();
        background.widthProperty().bind(widthProperty());
        background.heightProperty().bind(heightProperty());
        
        // Dark purple cyberpunk gradient
        LinearGradient gradient = new LinearGradient(
            0, 0, 1, 1, true, null,
            new Stop(0, Color.rgb(25, 10, 41, 0.9)),  // Dark purple
            new Stop(0.5, Color.rgb(45, 20, 80, 0.9)), // Medium purple
            new Stop(1, Color.rgb(20, 5, 30, 0.9))    // Very dark purple
        );
        background.setFill(gradient);
        
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
            "-fx-effect: dropshadow(gaussian, #ff00ff, 15, 0.2, 0, 0);"
        );
        
        // Add a title to the menu
        Text titleText = new Text("PAUSED");
        titleText.setFont(FontLoader.SUBTITLE_FONT);
//        titleText.setFont(Font.font("Monospace", FontWeight.BOLD, 28));
        titleText.setFill(Color.WHITE);
        
        // Add glow effect to the title
        Glow glow = new Glow(1.0);
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(255, 0, 255, 0.7));
        shadow.setRadius(10);
        titleText.setEffect(shadow);
        
        // ปุ่ม Resume (กลับไปเล่นเกม)
        MenuButton resumeButton = new MenuButton(MenuButtonType.RESUME);
        resumeButton.setOnAction(e -> {
            audioManager.playSoundEffect("click.wav");
            onResumeGame.run();
        });
        styleButton(resumeButton);

        // Settings Button
        MenuButton settingsButton = new MenuButton(MenuButtonType.SETTINGS);
        settingsButton.setOnAction(e -> {
            audioManager.playSoundEffect("click.wav");
            navigator.showInGameSettings();
        });
        styleButton(settingsButton);

        // ปุ่ม Main Menu (กลับไปหน้าหลัก)
        MenuButton mainMenuButton = new MenuButton(MenuButtonType.MAIN_MENU);
        mainMenuButton.setOnAction(e ->{
            audioManager.playSoundEffect("click.wav");
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
                onResumeGame.run();
            }
        });
    }
    
    // Helper method to style buttons with cyberpunk theme
    private void styleButton(MenuButton button) {
        // Add glow effect to buttons
        DropShadow buttonGlow = new DropShadow();
        buttonGlow.setColor(Color.rgb(180, 50, 255, 0.7));
        buttonGlow.setRadius(15);
        button.setEffect(buttonGlow);
        
        // Make buttons slightly larger
        button.setPrefWidth(250);
    }
}
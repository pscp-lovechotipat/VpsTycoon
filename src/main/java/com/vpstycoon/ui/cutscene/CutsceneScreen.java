package com.vpstycoon.ui.cutscene;

import com.vpstycoon.config.GameConfig;
import com.vpstycoon.screen.ScreenManager;
import com.vpstycoon.ui.navigation.Navigator;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import com.vpstycoon.FontLoader;
import javafx.geometry.Pos;
import java.util.Arrays;
import java.util.List;

public class CutsceneScreen extends StackPane {
    private final GameConfig gameConfig;
    private final ScreenManager screenManager;
    private final Navigator navigator;
    private Label logoLabel;
    private Label subtitleLabel;
    private VBox creditsBox;
    private Button skipButton;
    private SequentialTransition sequentialTransition;
    
    // รายชื่อผู้พัฒนา
    private final List<String> developers = Arrays.asList(
        "Nathapong Sopapol",
        "Thanatpat Promthong",
        "Kongpop Panchai",
        "Tanapat Chamted",
        "Yaowapa Thawornwiriyanan",
        "Supakorn Pipithgul",
        "Thanapon Sukpiboon",
        "Phichada Kaewsiri"
    );

    public CutsceneScreen(GameConfig gameConfig, ScreenManager screenManager, Navigator navigator) {
        this.gameConfig = gameConfig;
        this.screenManager = screenManager;
        this.navigator = navigator;
        
        // Apply the enhanced background first
        CutsceneBackground.applyDynamicBackground(this);
        
        setupUI();
        setupSkipButton();
        playCutscene();
    }

    private void setupUI() {
        // Create logo label instead of text title
        logoLabel = new Label();
        logoLabel.setStyle("""
                            -fx-background-image: url("/images/logo/vps_tycoon_logo.png");
                            -fx-background-position: center;
                            -fx-background-size: contain;
                            -fx-background-repeat: no-repeat;
                            -fx-alignment: center;
                            -fx-pref-width: 300px;
                            -fx-pref-height: 200px;
                            """);
        logoLabel.setTranslateY(-50);
        logoLabel.setOpacity(0);
        
        // Add glow effect to logo
        javafx.scene.effect.DropShadow glow = new javafx.scene.effect.DropShadow();
        glow.setColor(Color.web("#8A2BE2"));
        glow.setRadius(20);
        glow.setSpread(0.2);
        logoLabel.setEffect(glow);

        // Create subtitle label
        subtitleLabel = new Label("Your Journey Begins...");
        subtitleLabel.setFont(FontLoader.TITLE_FONT);
        subtitleLabel.setTextFill(Color.WHITE);
        subtitleLabel.setStyle("-fx-font-size: 24px;");
        subtitleLabel.setTextAlignment(TextAlignment.CENTER);
        subtitleLabel.setTranslateY(120);
        subtitleLabel.setOpacity(0);
        
        // สร้าง VBox สำหรับแสดงรายชื่อผู้พัฒนา
        creditsBox = new VBox(10); // ระยะห่างระหว่างชื่อ 10px
        creditsBox.setAlignment(javafx.geometry.Pos.CENTER);
        creditsBox.setOpacity(0);
        
        // สร้าง Label แสดงหัวข้อ "DEVELOPERS"
        Label creditsTitle = new Label("DEVELOPERS");
        creditsTitle.setFont(FontLoader.TITLE_FONT);
        creditsTitle.setTextFill(Color.WHITE);
        creditsTitle.setStyle("-fx-font-size: 28px;");
        creditsTitle.setTextAlignment(TextAlignment.CENTER);
        
        // Add neon glow effect to title
        javafx.scene.effect.Glow titleGlow = new javafx.scene.effect.Glow(0.5);
        javafx.scene.effect.DropShadow titleShadow = new javafx.scene.effect.DropShadow(10, Color.web("#00FFFF"));
        titleGlow.setInput(titleShadow);
        creditsTitle.setEffect(titleGlow);
        
        // เพิ่มหัวข้อเข้าไปใน VBox
        creditsBox.getChildren().add(creditsTitle);
        
        // สร้าง Label สำหรับแต่ละชื่อและเพิ่มเข้าไปใน VBox
        for (String developer : developers) {
            Label nameLabel = new Label(developer);
            nameLabel.setFont(FontLoader.TITLE_FONT);
            nameLabel.setTextFill(Color.WHITE);
            nameLabel.setStyle("-fx-font-size: 18px;");
            nameLabel.setTextAlignment(TextAlignment.CENTER);
            
            creditsBox.getChildren().add(nameLabel);
        }

        // Add elements to the scene
        getChildren().addAll(logoLabel, subtitleLabel, creditsBox);
        
        // No need for background color as we have a dynamic background
    }
    
    private void setupSkipButton() {
        // Create skip button for cutscene
        skipButton = new Button("SKIP >");
        skipButton.setFont(FontLoader.TITLE_FONT);
        skipButton.setStyle("""
                -fx-background-color: rgba(58, 19, 97, 0.5);
                -fx-text-fill: white;
                -fx-font-size: 18px;
                -fx-border-color: #8A2BE2;
                -fx-border-width: 1px;
                -fx-padding: 5px 10px;
                """);
        
        // Position button at bottom right
        StackPane.setAlignment(skipButton, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(skipButton, new javafx.geometry.Insets(0, 20, 20, 0));
        
        // Add hover effect
        skipButton.setOnMouseEntered(e -> 
            skipButton.setStyle("""
                -fx-background-color: rgba(138, 43, 226, 0.5);
                -fx-text-fill: white;
                -fx-font-size: 18px;
                -fx-border-color: #00FFFF;
                -fx-border-width: 1px;
                -fx-padding: 5px 10px;
                """)
        );
        
        skipButton.setOnMouseExited(e -> 
            skipButton.setStyle("""
                -fx-background-color: rgba(58, 19, 97, 0.5);
                -fx-text-fill: white;
                -fx-font-size: 18px;
                -fx-border-color: #8A2BE2;
                -fx-border-width: 1px;
                -fx-padding: 5px 10px;
                """)
        );
        
        // Add click action to skip the cutscene
        skipButton.setOnAction(e -> skipCutscene());
        
        // Add to the scene
        getChildren().add(skipButton);
    }
    
    private void skipCutscene() {
        // Stop any ongoing animations
        if (sequentialTransition != null) {
            sequentialTransition.stop();
        }
        
        // Skip directly to main menu
        navigator.showMainMenu();
    }

    private void playCutscene() {
        // First scene: Logo and subtitle
        
        // Create fade in for logo
        FadeTransition logoFadeIn = new FadeTransition(Duration.seconds(1.5), logoLabel);
        logoFadeIn.setFromValue(0);
        logoFadeIn.setToValue(1);
        
        // Add scale transition for logo
        ScaleTransition logoScale = new ScaleTransition(Duration.seconds(2), logoLabel);
        logoScale.setFromX(0.8);
        logoScale.setFromY(0.8);
        logoScale.setToX(1.0);
        logoScale.setToY(1.0);

        // Create fade in for subtitle
        FadeTransition subtitleFadeIn = new FadeTransition(Duration.seconds(1), subtitleLabel);
        subtitleFadeIn.setFromValue(0);
        subtitleFadeIn.setToValue(1);
        subtitleFadeIn.setDelay(Duration.seconds(0.5));

        // Create parallel transition for both elements
        ParallelTransition firstSceneIn = new ParallelTransition(logoFadeIn, logoScale, subtitleFadeIn);
        
        // Add spotlight effect to logo during first scene
        firstSceneIn.setOnFinished(e -> CutsceneBackground.applySpotlight(logoLabel, this));
        
        // First scene to second scene transition
        FadeTransition firstSceneOut = new FadeTransition(Duration.seconds(1));
        firstSceneOut.setFromValue(1);
        firstSceneOut.setToValue(0);
        firstSceneOut.setNode(logoLabel);
        
        FadeTransition subtitleOut = new FadeTransition(Duration.seconds(1));
        subtitleOut.setFromValue(1);
        subtitleOut.setToValue(0);
        subtitleOut.setNode(subtitleLabel);
        
        ParallelTransition firstSceneTransition = new ParallelTransition(firstSceneOut, subtitleOut);
        
        // Second scene: Credits animation
        FadeTransition creditsBoxFadeIn = new FadeTransition(Duration.seconds(1), creditsBox);
        creditsBoxFadeIn.setFromValue(0);
        creditsBoxFadeIn.setToValue(1);
        
        // สร้าง Animation แยกสำหรับแต่ละชื่อให้เด่นชัด
        SequentialTransition nameHighlights = new SequentialTransition();
        
        // เริ่มจากตำแหน่งที่ 1 (ข้ามหัวข้อ "DEVELOPERS")
        for (int i = 1; i < creditsBox.getChildren().size(); i++) {
            Label nameLabel = (Label) creditsBox.getChildren().get(i);
            
            // เริ่มต้นให้ตัวอักษรเป็นสีเทา
            nameLabel.setTextFill(Color.GRAY);
            
            // Highlight animation
            FadeTransition highlight = new FadeTransition(Duration.seconds(0.3), nameLabel);
            highlight.setFromValue(0.7);
            highlight.setToValue(1.0);
            
            // Add slight movement for each name
            TranslateTransition moveIn = new TranslateTransition(Duration.seconds(0.4), nameLabel);
            moveIn.setFromX(-5);
            moveIn.setToX(0);
            
            // สร้าง sequenceลำดับการเปลี่ยนสี
            SequentialTransition nameSequence = new SequentialTransition(
                new javafx.animation.PauseTransition(Duration.seconds(0.3)),
                new ParallelTransition(highlight, moveIn)
            );
            
            // ตั้งค่าให้เปลี่ยนสีเป็นสีขาวเมื่อ highlight
            nameSequence.setOnFinished(event -> {
                nameLabel.setTextFill(Color.WHITE);
                // Add subtle glow effect to highlighted names
                javafx.scene.effect.Glow glow = new javafx.scene.effect.Glow(0.3);
                nameLabel.setEffect(glow);
            });
            
            // เพิ่มลงในลำดับการแสดงชื่อ
            nameHighlights.getChildren().add(nameSequence);
        }
        
        // รวม animation การแสดงผลรายชื่อ
        ParallelTransition creditsScene = new ParallelTransition(creditsBoxFadeIn);
        
        // Final fade out to main game
        FadeTransition finalFadeOut = new FadeTransition(Duration.seconds(1.5), this);
        finalFadeOut.setFromValue(1);
        finalFadeOut.setToValue(0);
        finalFadeOut.setOnFinished(e -> navigator.showMainMenu());

        // Create entire sequential transition
        sequentialTransition = new SequentialTransition(
            firstSceneIn,
            new javafx.animation.PauseTransition(Duration.seconds(2.0)),
            firstSceneTransition,
            creditsScene,
            nameHighlights,
            new javafx.animation.PauseTransition(Duration.seconds(1.0)),
            finalFadeOut
        );

        // Start the animation
        sequentialTransition.play();
    }
} 
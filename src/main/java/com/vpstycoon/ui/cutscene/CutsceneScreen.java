package com.vpstycoon.ui.cutscene;

import com.vpstycoon.application.FontLoader;
import com.vpstycoon.config.GameConfig;
import com.vpstycoon.screen.ScreenManager;
import com.vpstycoon.ui.navigation.Navigator;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public class CutsceneScreen extends StackPane {
    private final GameConfig gameConfig;
    private final ScreenManager screenManager;
    private final Navigator navigator;
    private Label logoLabel;
    private Label subtitleLabel;
    private VBox creditsBox;
    private Button skipButton;
    private SequentialTransition sequentialTransition;


    public CutsceneScreen(GameConfig gameConfig, ScreenManager screenManager, Navigator navigator) {
        this.gameConfig = gameConfig;
        this.screenManager = screenManager;
        this.navigator = navigator;
        
        
        CutsceneBackground.applyDynamicBackground(this);
        
        setupUI();
        setupSkipButton();
        playCutscene();
    }

    private void setupUI() {
        
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
        
        
        javafx.scene.effect.DropShadow glow = new javafx.scene.effect.DropShadow();
        glow.setColor(Color.web("#8A2BE2"));
        glow.setRadius(20);
        glow.setSpread(0.2);
        logoLabel.setEffect(glow);

        
        subtitleLabel = new Label("Your Journey Begins...");
        subtitleLabel.setFont(FontLoader.TITLE_FONT);
        subtitleLabel.setTextFill(Color.WHITE);
        subtitleLabel.setStyle("-fx-font-size: 24px;");
        subtitleLabel.setTextAlignment(TextAlignment.CENTER);
        subtitleLabel.setTranslateY(120);
        subtitleLabel.setOpacity(0);
        
        
        javafx.scene.image.Image creditsImage = new javafx.scene.image.Image(getClass().getResourceAsStream("/images/others/Credits.gif"));
            
        
        if (creditsImage.isError()) {
            System.err.println("Error loading Credits.gif: " + creditsImage.getException().getMessage());
        }
        
        javafx.scene.image.ImageView creditsImageView = new javafx.scene.image.ImageView(creditsImage);
        creditsImageView.setFitWidth(960);
        creditsImageView.setPreserveRatio(true);
        
        
        creditsBox = new VBox();
        creditsBox.setAlignment(Pos.CENTER);
        creditsBox.getChildren().add(creditsImageView);
        creditsBox.setOpacity(0); 

        
        getChildren().addAll(logoLabel, subtitleLabel, creditsBox);
        
        
    }
    
    private void setupSkipButton() {
        
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
        
        
        StackPane.setAlignment(skipButton, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(skipButton, new javafx.geometry.Insets(0, 20, 20, 0));
        
        
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
        
        
        skipButton.setOnAction(e -> skipCutscene());
        
        
        getChildren().add(skipButton);
    }
    
    private void skipCutscene() {
        
        if (sequentialTransition != null) {
            sequentialTransition.stop();
        }
        
        
        navigator.showMainMenu();
    }

    private void playCutscene() {
        
        
        
        FadeTransition logoFadeIn = new FadeTransition(Duration.seconds(1.5), logoLabel);
        logoFadeIn.setFromValue(0);
        logoFadeIn.setToValue(1);
        
        
        ScaleTransition logoScale = new ScaleTransition(Duration.seconds(2), logoLabel);
        logoScale.setFromX(0.8);
        logoScale.setFromY(0.8);
        logoScale.setToX(1.0);
        logoScale.setToY(1.0);

        
        FadeTransition subtitleFadeIn = new FadeTransition(Duration.seconds(1), subtitleLabel);
        subtitleFadeIn.setFromValue(0);
        subtitleFadeIn.setToValue(1);
        subtitleFadeIn.setDelay(Duration.seconds(0.5));

        
        ParallelTransition firstSceneIn = new ParallelTransition(logoFadeIn, logoScale, subtitleFadeIn);
        
        
        firstSceneIn.setOnFinished(e -> CutsceneBackground.applySpotlight(logoLabel, this));
        
        
        FadeTransition firstSceneOut = new FadeTransition(Duration.seconds(1));
        firstSceneOut.setFromValue(1);
        firstSceneOut.setToValue(0);
        firstSceneOut.setNode(logoLabel);
        
        FadeTransition subtitleOut = new FadeTransition(Duration.seconds(1));
        subtitleOut.setFromValue(1);
        subtitleOut.setToValue(0);
        subtitleOut.setNode(subtitleLabel);
        
        ParallelTransition firstSceneTransition = new ParallelTransition(firstSceneOut, subtitleOut);
        
        
        FadeTransition creditsBoxFadeIn = new FadeTransition(Duration.seconds(1), creditsBox);
        creditsBoxFadeIn.setFromValue(0);
        creditsBoxFadeIn.setToValue(1);
        
        
        FadeTransition finalFadeOut = new FadeTransition(Duration.seconds(1.5), this);
        finalFadeOut.setFromValue(1);
        finalFadeOut.setToValue(0);
        finalFadeOut.setOnFinished(e -> navigator.showMainMenu());

        
        sequentialTransition = new SequentialTransition(
            firstSceneIn,
            new javafx.animation.PauseTransition(Duration.seconds(2.0)),
            firstSceneTransition,
            creditsBoxFadeIn,
            new javafx.animation.PauseTransition(Duration.seconds(3.0)),
            finalFadeOut
        );

        
        sequentialTransition.play();
    }
} 


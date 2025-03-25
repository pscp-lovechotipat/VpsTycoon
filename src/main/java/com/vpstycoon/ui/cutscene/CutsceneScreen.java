package com.vpstycoon.ui.cutscene;

import com.vpstycoon.config.GameConfig;
import com.vpstycoon.screen.ScreenManager;
import com.vpstycoon.ui.navigation.Navigator;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import com.vpstycoon.FontLoader;

public class CutsceneScreen extends StackPane {
    private final GameConfig gameConfig;
    private final ScreenManager screenManager;
    private final Navigator navigator;
    private Label titleLabel;
    private Label subtitleLabel;

    public CutsceneScreen(GameConfig gameConfig, ScreenManager screenManager, Navigator navigator) {
        this.gameConfig = gameConfig;
        this.screenManager = screenManager;
        this.navigator = navigator;
        
        setupUI();
        playCutscene();
    }

    private void setupUI() {
        // Create title label
        titleLabel = new Label("VPS Tycoon");
        titleLabel.setFont(FontLoader.TITLE_FONT);
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setStyle("-fx-font-size: 48px;");
        titleLabel.setTextAlignment(TextAlignment.CENTER);
        titleLabel.setTranslateY(-50);
        titleLabel.setOpacity(0);

        // Create subtitle label
        subtitleLabel = new Label("Your Journey Begins...");
        subtitleLabel.setFont(FontLoader.TITLE_FONT);
        subtitleLabel.setTextFill(Color.WHITE);
        subtitleLabel.setStyle("-fx-font-size: 24px;");
        subtitleLabel.setTextAlignment(TextAlignment.CENTER);
        subtitleLabel.setTranslateY(50);
        subtitleLabel.setOpacity(0);

        // Add labels to the scene
        getChildren().addAll(titleLabel, subtitleLabel);
        
        // Set background color
        setStyle("-fx-background-color: black;");
    }

    private void playCutscene() {
        // Create fade in for title
        FadeTransition titleFadeIn = new FadeTransition(Duration.seconds(1), titleLabel);
        titleFadeIn.setFromValue(0);
        titleFadeIn.setToValue(1);

        // Create fade in for subtitle
        FadeTransition subtitleFadeIn = new FadeTransition(Duration.seconds(1), subtitleLabel);
        subtitleFadeIn.setFromValue(0);
        subtitleFadeIn.setToValue(1);

        // Create parallel transition for both labels
        ParallelTransition parallelTransition = new ParallelTransition(titleFadeIn, subtitleFadeIn);

        // Create fade out transition
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), this);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> navigator.showMainMenu());

        // Create sequential transition
        SequentialTransition sequentialTransition = new SequentialTransition(
            parallelTransition,
            new javafx.animation.PauseTransition(Duration.seconds(2)),
            fadeOut
        );

        // Start the animation
        sequentialTransition.play();
    }
} 
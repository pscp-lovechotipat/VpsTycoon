package com.vpstycoon.game.thread.task;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.Bloom;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.Random;

/**
 * Utility class providing cyberpunk-themed visual effects and styles for game tasks.
 */
public class CyberpunkEffects {
    private static final Random random = new Random();
    
    // Color schemes
    public static final Color[] NEON_COLORS = {
        Color.web("#FF00A0"), // Neon Pink
        Color.web("#00FFFF"), // Cyan
        Color.web("#FF9500"), // Neon Orange
        Color.web("#39FF14"), // Neon Green
        Color.web("#FE01B1"), // Hot Pink
        Color.web("#FFFF00"), // Yellow
        Color.web("#FF0000"), // Red
        Color.web("#8A2BE2")  // Violet (Updated from Blue to match VM theme)
    };
    
    // Fonts
    public static final String CYBERPUNK_FONT_PRIMARY = "Orbitron";
    public static final String CYBERPUNK_FONT_SECONDARY = "Share Tech Mono";
    public static final String CYBERPUNK_FONT_FALLBACK = "Courier New";
    
    /**
     * Apply cyberpunk styling to a task pane
     * @param pane The pane to style
     */
    public static void styleTaskPane(BorderPane pane) {
        pane.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #1A0033, #380066);" +
            "-fx-border-color: #8A2BE2;" +
            "-fx-border-width: 2px;" +
            "-fx-background-radius: 5px;" +
            "-fx-border-radius: 5px;" +
            "-fx-effect: dropshadow(gaussian, rgba(120, 0, 255, 0.4), 15, 0, 0, 7);"
        );
        
        // Add holo grid lines to the pane
        addHoloGridLines(pane, 20, 20);
    }
    
    /**
     * Create a cyberpunk-styled title
     * @param title The title text
     * @return Styled text node
     */
    public static Text createTaskTitle(String title) {
        Text titleText = new Text(">> " + title + " <<");
        titleText.setFont(Font.font(CYBERPUNK_FONT_PRIMARY, FontWeight.BOLD, 24));
        titleText.setFill(Color.web("#E4FBFF"));
        
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#00F6FF"));
        glow.setRadius(15);
        glow.setSpread(0.7);
        titleText.setEffect(glow);
        
        pulseText(titleText);
        
        return titleText;
    }
    
    /**
     * Create a cyberpunk-styled description
     * @param description The description text
     * @return Styled text node
     */
    public static Text createTaskDescription(String description) {
        Text descText = new Text(description);
        descText.setFont(Font.font(CYBERPUNK_FONT_SECONDARY, FontWeight.NORMAL, 16));
        descText.setFill(Color.web("#00F6FF"));
        
        // Add a subtle glow effect
        Glow glow = new Glow(0.3);
        descText.setEffect(glow);
        
        return descText;
    }
    
    /**
     * Create a cyberpunk-styled button
     * @param text Button text
     * @param primary Whether this is a primary action button
     * @return Styled button
     */
    public static Button createCyberpunkButton(String text, boolean primary) {
        Button button = new Button(text);
        button.setFont(Font.font(CYBERPUNK_FONT_SECONDARY, FontWeight.BOLD, 14));
        
        String baseColor, hoverColor, textColor;
        if (primary) {
            baseColor = "#8A2BE2";
            hoverColor = "#9e33ff";
            textColor = "#FFFFFF";
        } else {
            baseColor = "#F44336";
            hoverColor = "#FF6659";
            textColor = "#FFFFFF";
        }
        
        String baseStyle = 
            "-fx-background-color: " + baseColor + ";" +
            "-fx-text-fill: " + textColor + ";" +
            "-fx-border-color: #00F6FF;" +
            "-fx-border-width: 1px;" +
            "-fx-background-radius: 5px;" +
            "-fx-border-radius: 5px;" +
            "-fx-padding: 10px 25px;" +
            "-fx-font-size: 14px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 0);" +
            "-fx-cursor: hand;";
        
        String hoverStyle = 
            "-fx-background-color: " + hoverColor + ";" +
            "-fx-text-fill: " + textColor + ";" +
            "-fx-border-color: #00F6FF;" +
            "-fx-border-width: 1px;" +
            "-fx-background-radius: 5px;" +
            "-fx-border-radius: 5px;" +
            "-fx-padding: 10px 25px;" +
            "-fx-font-size: 14px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,255,255,0.5), 10, 0, 0, 0);" +
            "-fx-cursor: hand;";
        
        button.setStyle(baseStyle);
        
        // Add hover effect
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(baseStyle));
        
        // Add click animation
        button.setOnMousePressed(e -> button.setStyle(baseStyle + "-fx-scale-y: 0.9; -fx-scale-x: 0.9;"));
        button.setOnMouseReleased(e -> {
            if (button.isHover()) {
                button.setStyle(hoverStyle);
            } else {
                button.setStyle(baseStyle);
            }
        });
        
        return button;
    }
    
    /**
     * Create a glowing label with cyberpunk style
     * @param text Label text
     * @param color Text color (hex code)
     * @return Styled label
     */
    public static Label createGlowingLabel(String text, String color) {
        Label label = new Label(text);
        label.setFont(Font.font(CYBERPUNK_FONT_SECONDARY, FontWeight.BOLD, 16));
        label.setTextFill(Color.web(color));
        
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web(color));
        glow.setRadius(10);
        glow.setSpread(0.5);
        label.setEffect(glow);
        
        return label;
    }
    
    /**
     * Add animated background effects to a pane
     * @param pane The pane to apply effects to
     */
    public static void addAnimatedBackground(Pane pane) {
        // Create a background with gradient
        LinearGradient gradient = new LinearGradient(
            0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#1A0033")),
            new Stop(1, Color.web("#380066"))
        );
        
        pane.setBackground(new Background(new BackgroundFill(
            gradient, CornerRadii.EMPTY, Insets.EMPTY
        )));
        
        // Add animated glitch effects randomly
        Timeline glitchEffect = new Timeline(
            new KeyFrame(Duration.seconds(2), event -> {
                if (random.nextDouble() < 0.2) { // 20% chance of glitch
                    // Create a glitch rectangle
                    double width = random.nextDouble() * pane.getWidth() * 0.3;
                    double height = random.nextDouble() * 20 + 5;
                    double x = random.nextDouble() * (pane.getWidth() - width);
                    double y = random.nextDouble() * (pane.getHeight() - height);
                    
                    Rectangle glitch = new Rectangle(x, y, width, height);
                    glitch.setFill(NEON_COLORS[random.nextInt(NEON_COLORS.length)].deriveColor(1, 1, 1, 0.3));
                    
                    pane.getChildren().add(glitch);
                    
                    // Fade out
                    FadeTransition fade = new FadeTransition(Duration.millis(300), glitch);
                    fade.setFromValue(0.7);
                    fade.setToValue(0);
                    fade.setOnFinished(e -> pane.getChildren().remove(glitch));
                    fade.play();
                }
            })
        );
        glitchEffect.setCycleCount(Timeline.INDEFINITE);
        glitchEffect.play();
    }
    
    /**
     * Add holographic grid lines to a pane for cyberpunk look
     * @param pane The pane to add grid lines to
     * @param hSpacing Horizontal spacing between lines
     * @param vSpacing Vertical spacing between lines
     */
    public static void addHoloGridLines(Pane pane, int hSpacing, int vSpacing) {
        // Create a transparent pane for the grid
        Pane gridPane = new Pane();
        gridPane.setMouseTransparent(true);
        gridPane.setPrefSize(pane.getPrefWidth(), pane.getPrefHeight());
        
        // Add horizontal lines
        for (int y = vSpacing; y < pane.getPrefHeight(); y += vSpacing) {
            Line line = new Line(0, y, pane.getPrefWidth(), y);
            line.setStroke(Color.web("#00FFFF", 0.1));
            line.setStrokeWidth(0.5);
            gridPane.getChildren().add(line);
        }
        
        // Add vertical lines
        for (int x = hSpacing; x < pane.getPrefWidth(); x += hSpacing) {
            Line line = new Line(x, 0, x, pane.getPrefHeight());
            line.setStroke(Color.web("#00FFFF", 0.1));
            line.setStrokeWidth(0.5);
            gridPane.getChildren().add(line);
        }
        
        // Add the grid to the pane
        if (pane instanceof BorderPane) {
            ((BorderPane) pane).setCenter(gridPane);
        } else {
            pane.getChildren().add(gridPane);
        }
    }
    
    /**
     * Make a node pulse (scale animation)
     * @param node The node to animate
     */
    public static void pulseNode(Node node) {
        ScaleTransition pulse = new ScaleTransition(Duration.seconds(1.5), node);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.05);
        pulse.setToY(1.05);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(ScaleTransition.INDEFINITE);
        pulse.play();
    }
    
    /**
     * Make text pulse with glow animation
     * @param text The text to animate
     */
    public static void pulseText(Text text) {
        // Get the current effect
        DropShadow baseEffect = (DropShadow) text.getEffect();
        Color baseColor = (Color) baseEffect.getColor();
        
        Timeline pulse = new Timeline(
            new KeyFrame(Duration.ZERO, 
                new KeyValue(baseEffect.radiusProperty(), 10),
                new KeyValue(baseEffect.spreadProperty(), 0.4)
            ),
            new KeyFrame(Duration.seconds(1.5),
                new KeyValue(baseEffect.radiusProperty(), 20),
                new KeyValue(baseEffect.spreadProperty(), 0.7)
            )
        );
        
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Timeline.INDEFINITE);
        pulse.play();
    }
    
    /**
     * Create a scanning effect (top to bottom line)
     * @param pane The pane to add the effect to
     */
    public static void addScanningEffect(Pane pane) {
        Rectangle scanner = new Rectangle(0, 0, pane.getPrefWidth(), 3);
        scanner.setFill(Color.web("#00FFFF", 0.7));
        
        Bloom bloom = new Bloom(0.7);
        scanner.setEffect(bloom);
        
        pane.getChildren().add(scanner);
        
        TranslateTransition scan = new TranslateTransition(Duration.seconds(3), scanner);
        scan.setFromY(0);
        scan.setToY(pane.getPrefHeight());
        scan.setCycleCount(TranslateTransition.INDEFINITE);
        scan.setAutoReverse(false);
        scan.setOnFinished(e -> {
            scanner.setTranslateY(0);
            scan.play();
        });
        scan.play();
    }
    
    /**
     * Create a cyberpunk-styled data panel
     * @param title Panel title
     * @return A styled pane for displaying data
     */
    public static Pane createDataPanel(String title) {
        BorderPane panel = new BorderPane();
        panel.setPadding(new Insets(15));
        panel.setStyle(
            "-fx-background-color: rgba(10, 20, 40, 0.8);" +
            "-fx-border-color: #00FFFF;" +
            "-fx-border-width: 1px;" +
            "-fx-background-radius: 5px;" +
            "-fx-border-radius: 5px;"
        );
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font(CYBERPUNK_FONT_SECONDARY, FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.web("#00FFFF"));
        titleLabel.setStyle("-fx-background-color: rgba(0, 20, 40, 0.8); -fx-padding: 5 10;");
        
        panel.setTop(titleLabel);
        
        return panel;
    }
    
    /**
     * Get a random neon color
     * @return A random bright neon color
     */
    public static Color getRandomNeonColor() {
        return NEON_COLORS[random.nextInt(NEON_COLORS.length)];
    }
    
    /**
     * Apply cyberpunk styling to a completed task UI component
     * @param node The node to style for completion
     */
    public static void styleCompletionEffect(Node node) {
        // Flash the node with a success color
        Timeline flash = new Timeline(
            new KeyFrame(Duration.ZERO, 
                new KeyValue(node.opacityProperty(), 1.0)
            ),
            new KeyFrame(Duration.millis(100),
                new KeyValue(node.opacityProperty(), 0.7)
            ),
            new KeyFrame(Duration.millis(200),
                new KeyValue(node.opacityProperty(), 1.0)
            ),
            new KeyFrame(Duration.millis(300),
                new KeyValue(node.opacityProperty(), 0.7)
            ),
            new KeyFrame(Duration.millis(400),
                new KeyValue(node.opacityProperty(), 1.0)
            )
        );
        
        flash.setCycleCount(3);
        flash.play();
    }
    
    /**
     * Apply a failure effect to a UI component
     * @param node The node to apply effect to
     */
    public static void styleFailureEffect(Node node) {
        // Flash the node with a failure color
        Timeline flash = new Timeline(
            new KeyFrame(Duration.ZERO, 
                new KeyValue(node.opacityProperty(), 1.0)
            ),
            new KeyFrame(Duration.millis(100),
                new KeyValue(node.opacityProperty(), 0.5)
            ),
            new KeyFrame(Duration.millis(200),
                new KeyValue(node.opacityProperty(), 1.0)
            )
        );
        
        flash.setCycleCount(5);
        flash.play();
    }
    
    /**
     * Create a notification popup for task results
     * @param title Notification title
     * @param message Message text
     * @param success Whether it's a success notification
     * @return A styled pane for the notification
     */
    public static Pane createNotificationPopup(String title, String message, boolean success) {
        BorderPane popup = new BorderPane();
        popup.setPadding(new Insets(20));
        
        String baseColor = success ? "#00F6FF" : "#F44336";
        
        popup.setStyle(
            "-fx-background-color: rgba(58, 28, 90, 0.7);" +
            "-fx-border-color: " + baseColor + ";" +
            "-fx-border-width: 2px;" +
            "-fx-background-radius: 5px;" +
            "-fx-border-radius: 5px;" +
            "-fx-effect: dropshadow(gaussian, rgba(138, 43, 226, 0.4), 15, 0, 0, 7);"
        );
        
        Label titleLabel = createGlowingLabel(">> " + title + " <<", baseColor);
        Text messageText = new Text(message);
        messageText.setFont(Font.font(CYBERPUNK_FONT_SECONDARY, FontWeight.NORMAL, 14));
        messageText.setFill(Color.WHITE);
        
        popup.setTop(titleLabel);
        BorderPane.setMargin(titleLabel, new Insets(0, 0, 10, 0));
        popup.setCenter(messageText);
        
        return popup;
    }
    
    /**
     * Create a cyberpunk-styled section with a title
     * @param title Section title
     * @return Styled VBox for the section
     */
    public static VBox createCyberSection(String title) {
        VBox section = new VBox(15);
        section.setPadding(new Insets(15));
        section.setStyle("-fx-background-color: rgba(58, 28, 90, 0.4); -fx-background-radius: 5px; " +
                "-fx-border-color: #8A2BE2; -fx-border-width: 1px; -fx-border-radius: 5px; " +
                "-fx-effect: dropshadow(gaussian, rgba(120, 0, 255, 0.2), 10, 0, 0, 3);");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #00F6FF; -fx-font-size: 16px; -fx-font-weight: bold; " +
                "-fx-font-family: 'Monospace'; -fx-padding: 0 0 5 0; " + 
                "-fx-border-color: #8A2BE2; -fx-border-width: 0 0 1 0;");
        titleLabel.setPrefWidth(Double.MAX_VALUE);
                
        section.getChildren().add(titleLabel);
        return section;
    }
    
    /**
     * Create a cyberpunk-styled card with a title
     * @param title Card title
     * @return Styled HBox for the card
     */
    public static HBox createCyberCard(String title) {
        HBox card = new HBox(15);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: rgba(58, 28, 90, 0.4); -fx-background-radius: 5px; " +
                "-fx-border-color: #00F6FF; -fx-border-width: 1px; -fx-border-radius: 5px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0, 246, 255, 0.2), 10, 0, 0, 3);");
        
        VBox titleBox = new VBox(5);
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #00F6FF; -fx-font-size: 16px; -fx-font-weight: bold; " +
                "-fx-font-family: 'Monospace';");
        titleBox.getChildren().add(titleLabel);
        
        card.getChildren().add(titleBox);
        return card;
    }
} 
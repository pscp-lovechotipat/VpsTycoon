package com.vpstycoon.ui.game.status;

import com.vpstycoon.FontLoader;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.HashMap;

/**
 * Creates a circular status button with number and label in cyberpunk style.
 */
public class CircleStatusButton {
    private final VBox container;
    private Runnable onClickAction;
    private int skillLevel;
    private int skillPoints;
    private Label numberLabel;
    private String skillName;
    private static HashMap<String, Integer> skillLevels = new HashMap<>();
    private static HashMap<String, Integer> skillPointsMap = new HashMap<>();

    // Cyberpunk theme colors
    private static final Color CYBER_PURPLE = Color.rgb(200, 50, 255);
    private static final Color CYBER_DARK = Color.rgb(20, 10, 30);
    private static final Color CYBER_GLOW = Color.rgb(255, 0, 255, 0.7);

    static {
        skillLevels.put("Deploy", 1);
        skillLevels.put("Network", 1);
        skillLevels.put("Security", 1);
        skillLevels.put("Marketing", 1);

        skillPointsMap.put("Deploy", 1000);
        skillPointsMap.put("Network", 1000);
        skillPointsMap.put("Security", 1000);
        skillPointsMap.put("Marketing", 1000);
    }

    public CircleStatusButton(String labelText, int number, Color topColor, Color bottomColor) {
        this.skillName = labelText;
        this.skillLevel = skillLevels.getOrDefault(skillName, 1);
        this.skillPoints = skillPointsMap.getOrDefault(skillName, number);
        this.container = createContainer(skillName, topColor, bottomColor);
    }

    private VBox createContainer(String skillName, Color topColor, Color bottomColor) {
        VBox container = new VBox(12);
        container.setAlignment(Pos.CENTER);

        // Create pixel-art style button
        StackPane buttonFrame = createCyberButton(topColor, bottomColor);
        
        // Add number with pixel-like font and make it BOLDER
        numberLabel = new Label(String.valueOf(skillPoints));
        numberLabel.setTextFill(Color.WHITE);
        // numberLabel.setFont(Font.font("Monospace", FontWeight.EXTRA_BOLD, 24)); // Increased size and weight
        numberLabel.setFont(FontLoader.loadFont(42));
        
        // Add glow effect to number
        Glow textGlow = new Glow(0.8);
        numberLabel.setEffect(textGlow);

        // Stack elements
        StackPane buttonStack = new StackPane();
        buttonStack.getChildren().addAll(buttonFrame, numberLabel);

        // Simplified hover effect - just a slight scale without glow changes
        buttonStack.setOnMouseEntered(e -> {
            // Only scale effect, no glow change
            buttonStack.setScaleX(1.05);
            buttonStack.setScaleY(1.05);
        });
        
        buttonStack.setOnMouseExited(e -> {
            // Reset scale
            buttonStack.setScaleX(1.0);
            buttonStack.setScaleY(1.0);
        });

        buttonStack.setOnMouseClicked((MouseEvent e) -> openUpgradePanel());

        // Create cyber-style label with pixel art aesthetic
        Label textLabel = createCyberLabel(skillName, topColor);
        
        // Add all to VBox with proper spacing for pixel art look
        container.getChildren().addAll(buttonStack, textLabel);
        return container;
    }
    
    private StackPane createCyberButton(Color topColor, Color bottomColor) {
        // Create a pixel-art style button instead of a circle
        StackPane buttonStack = new StackPane();
        
        // Create main square button with pixelated corners
        Rectangle mainSquare = new Rectangle(70, 70);
        mainSquare.setArcWidth(0);  // Sharp corners for pixel look
        mainSquare.setArcHeight(0);
        
        // Create pixel-art style gradient
        Stop[] stops = new Stop[] {
                new Stop(0, topColor),
                new Stop(0.7, bottomColor),
                new Stop(1, CYBER_DARK)
        };
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);
        mainSquare.setFill(gradient);
        
        // Add pixel border
        mainSquare.setStroke(topColor);
        mainSquare.setStrokeWidth(2);
        
        // Add glow effect with the button's own color, but reduced intensity
        DropShadow glow = new DropShadow();
        glow.setRadius(8);  // Reduced from 15
        glow.setColor(topColor);
        glow.setSpread(0.15);  // Reduced from 0.3
        mainSquare.setEffect(glow);
        
        // Create pixel corners to give it a more pixelated look - ADJUSTED POSITIONING
        Pane pixelCorners = new Pane();
        // Move the entire pixelCorners pane to the right
        
        // Top-left pixel - adjusted positioning
        Rectangle topLeft = new Rectangle(5, 5);
        topLeft.setFill(topColor);
        topLeft.setTranslateX(9);
        topLeft.setTranslateY(-4);
        
        // Top-right pixel - adjusted positioning
        Rectangle topRight = new Rectangle(5, 5);
        topRight.setFill(topColor);
        topRight.setTranslateX(86);
        topRight.setTranslateY(-4);
        
        // Bottom-left pixel - adjusted positioning
        Rectangle bottomLeft = new Rectangle(5, 5);
        bottomLeft.setFill(topColor);
        bottomLeft.setTranslateX(9);
        bottomLeft.setTranslateY(70);
        
        // Bottom-right pixel - adjusted positioning
        Rectangle bottomRight = new Rectangle(5, 5);
        bottomRight.setFill(topColor);
        bottomRight.setTranslateX(86);
        bottomRight.setTranslateY(70);
        
        // Add pixel details inside
        Pane pixelDetails = new Pane();
        // Move the entire pixelDetails pane to the right
        pixelDetails.setTranslateX(5);  // Added offset to move everything right
        
        // Add horizontal scan lines for CRT effect
        for (int i = 10; i < 65; i += 10) {
            Rectangle scanLine = new Rectangle(60, 1);
            scanLine.setFill(Color.rgb(255, 255, 255, 0.1));
            scanLine.setTranslateX(15);
            scanLine.setTranslateY(i);
            pixelDetails.getChildren().add(scanLine);
        }

        
        pixelCorners.getChildren().addAll(topLeft, topRight, bottomLeft, bottomRight);
        
        // Add all elements to the button stack
        buttonStack.getChildren().addAll(mainSquare, pixelCorners, pixelDetails);
        
        return buttonStack;
    }
    
    private Label createCyberLabel(String text, Color color) {
        Label label = new Label(text);
        label.setPrefWidth(100); // Increased width to prevent truncation
        label.setAlignment(Pos.CENTER);
        
        // Create pixel-art style background
        BackgroundFill bgFill = new BackgroundFill(
            CYBER_DARK, 
            new CornerRadii(0), 
            Insets.EMPTY
        );
        
        // Add pixel-style border
        BorderStroke borderStroke = new BorderStroke(
            color, 
            BorderStrokeStyle.SOLID, 
            new CornerRadii(0), 
            new BorderWidths(2)
        );
        
        // Add pixel corners to the border
        Border pixelBorder = new Border(borderStroke);
        
        label.setBackground(new Background(bgFill));
        label.setBorder(pixelBorder);
        label.setPadding(new Insets(6));
        
        // Add reduced glow effect with the label's own color
        DropShadow glow = new DropShadow();
        glow.setColor(color);
        glow.setRadius(5);  // Reduced from 10
        glow.setSpread(0.1);  // Reduced from 0.2
        label.setEffect(glow);
        
        // Set text style with pixel-like font
        label.setTextFill(Color.WHITE);
        label.setFont(Font.font("Monospace", FontWeight.BOLD, 12));
        
        return label;
    }
    
    public VBox getContainer() {
        return container;
    }
    
    private void openUpgradePanel() {
        Stage upgradeStage = new Stage();
        upgradeStage.initModality(Modality.APPLICATION_MODAL);
        upgradeStage.initStyle(StageStyle.TRANSPARENT);
        upgradeStage.setResizable(false);

        VBox upgradeLayout = new VBox(15);
        upgradeLayout.setAlignment(Pos.CENTER);
        upgradeLayout.setPadding(new Insets(20));
        
        // Get the appropriate color for this skill
        Color skillColor = getColorForSkill(skillName);
        
        // Pixel-art style background - fully black for contrast
        upgradeLayout.setBackground(new Background(new BackgroundFill(
            Color.rgb(0, 0, 0, 0.95), 
            new CornerRadii(0), 
            Insets.EMPTY
        )));
        
        // Add pixel-art border with the skill's color
        upgradeLayout.setBorder(new Border(new BorderStroke(
            skillColor, 
            BorderStrokeStyle.SOLID, 
            new CornerRadii(0), 
            new BorderWidths(2)
        )));
        
        // Add scanlines for CRT effect
        for (int i = 0; i < 320; i += 4) {
            Rectangle scanLine = new Rectangle(320, 1);
            scanLine.setFill(Color.rgb(255, 255, 255, 0.03));
            scanLine.setTranslateY(i);
            upgradeLayout.getChildren().add(scanLine);
        }
        
        // Add reduced glow effect with the skill's color
        DropShadow panelGlow = new DropShadow();
        panelGlow.setColor(skillColor);
        panelGlow.setRadius(10);  // Reduced from 20
        panelGlow.setSpread(0.2);  // Reduced from 0.4
        upgradeLayout.setEffect(panelGlow);

        // Create pixel-art style title bar
        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER);
        titleBar.setSpacing(50);
        
        // Pixel-art style title with the skill's color
        Label titleLabel = new Label("UPGRADE " + skillName.toUpperCase() + " [LV:" + skillLevel + "]");
        titleLabel.setTextFill(skillColor);
        titleLabel.setFont(Font.font("Monospace", FontWeight.BOLD, 16));
        
        // Add glitch effect to title (occasional color change)
        Timeline glitchTimeline = new Timeline(
            new KeyFrame(Duration.seconds(0.8), evt -> titleLabel.setTextFill(skillColor)),
            new KeyFrame(Duration.seconds(0.9), evt -> titleLabel.setTextFill(Color.WHITE)),
            new KeyFrame(Duration.seconds(1.0), evt -> titleLabel.setTextFill(skillColor))
        );
        glitchTimeline.setCycleCount(Timeline.INDEFINITE);
        glitchTimeline.play();

        // Pixel-art style exit button with the skill's color
        Button exitButton = new Button("X");
        exitButton.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + toRgbString(skillColor) + ";" +
            "-fx-font-family: 'Monospace';" +
            "-fx-font-weight: bold;" +
            "-fx-border-color: " + toRgbString(skillColor) + ";" +
            "-fx-border-width: 2px;"
        );
        
        // Add hover effect to exit button
        exitButton.setOnMouseEntered(e -> {
            exitButton.setStyle(
                "-fx-background-color: " + toRgbString(skillColor) + ";" +
                "-fx-text-fill: #000000;" +
                "-fx-font-family: 'Monospace';" +
                "-fx-font-weight: bold;" +
                "-fx-border-color: " + toRgbString(skillColor) + ";" +
                "-fx-border-width: 2px;"
            );
        });
        
        exitButton.setOnMouseExited(e -> {
            exitButton.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: " + toRgbString(skillColor) + ";" +
                "-fx-font-family: 'Monospace';" +
                "-fx-font-weight: bold;" +
                "-fx-border-color: " + toRgbString(skillColor) + ";" +
                "-fx-border-width: 2px;"
            );
        });
        
        exitButton.setOnAction(e -> upgradeStage.close());

        titleBar.getChildren().addAll(titleLabel, exitButton);

        // Pixel-art style points display
        Label pointsLabel = new Label("AVAILABLE POINTS: " + skillPoints);
        pointsLabel.setTextFill(Color.LIGHTGRAY);
        pointsLabel.setFont(Font.font("Monospace", FontWeight.NORMAL, 14));
        
        // Add pixel-art border to points display
        pointsLabel.setPadding(new Insets(5, 10, 5, 10));
        pointsLabel.setBorder(new Border(new BorderStroke(
            Color.LIGHTGRAY, 
            BorderStrokeStyle.SOLID, 
            new CornerRadii(0), 
            new BorderWidths(1)
        )));

        // Pixel-art style upgrade button with the skill's color
        Button upgradeButton = new Button("[ UPGRADE ]");
        upgradeButton.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + toRgbString(skillColor) + ";" +
            "-fx-font-family: 'Monospace';" +
            "-fx-font-weight: bold;" +
            "-fx-border-color: " + toRgbString(skillColor) + ";" +
            "-fx-border-width: 2px;" +
            "-fx-padding: 8px 16px;"
        );
        
        // Add hover effect with pixel-art animation
        upgradeButton.setOnMouseEntered(e -> {
            upgradeButton.setStyle(
                "-fx-background-color: " + toRgbString(skillColor) + ";" +
                "-fx-text-fill: #000000;" +
                "-fx-font-family: 'Monospace';" +
                "-fx-font-weight: bold;" +
                "-fx-border-color: " + toRgbString(skillColor) + ";" +
                "-fx-border-width: 2px;" +
                "-fx-padding: 8px 16px;"
            );
        });
        
        upgradeButton.setOnMouseExited(e -> {
            upgradeButton.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: " + toRgbString(skillColor) + ";" +
                "-fx-font-family: 'Monospace';" +
                "-fx-font-weight: bold;" +
                "-fx-border-color: " + toRgbString(skillColor) + ";" +
                "-fx-border-width: 2px;" +
                "-fx-padding: 8px 16px;"
            );
        });
        
        upgradeButton.setOnAction(e -> upgradeSkill(upgradeStage));

        // Create a container for the main content (excluding scanlines)
        VBox contentBox = new VBox(15);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.getChildren().addAll(titleBar, pointsLabel, upgradeButton);
        
        // Add the content on top of the scanlines
        StackPane mainContent = new StackPane();
        mainContent.getChildren().addAll(contentBox);

        // Set the main content as the only child of the layout
        upgradeLayout.getChildren().clear();  // Clear the scanlines
        upgradeLayout.getChildren().add(mainContent);

        Scene scene = new Scene(upgradeLayout, 400, 180);
        scene.setFill(Color.TRANSPARENT);
        upgradeStage.setScene(scene);
        upgradeStage.showAndWait();
    }

    private void upgradeSkill(Stage upgradeStage) {
        int cost = (skillLevel == 1) ? 100 : (skillLevel == 2) ? 500 : -1;

        if (cost == -1) {
            System.out.println(skillName + " is already at max level.");
            return;
        }

        if (skillPoints >= cost) {
            skillPoints -= cost;
            skillLevel += 1;
            skillLevels.put(skillName, skillLevel);
            skillPointsMap.put(skillName, skillPoints);
            numberLabel.setText(String.valueOf(skillPoints));
            upgradeStage.close();
        } else {
            System.out.println("Not enough points to upgrade " + skillName);
        }
    }
    
    // Helper method to get the appropriate color for each skill
    private Color getColorForSkill(String skillName) {
        switch (skillName) {
            case "Deploy":
                return Color.rgb(255, 50, 180); // Pink
            case "Network":
                return Color.rgb(50, 200, 255); // Blue
            case "Security":
                return Color.rgb(200, 50, 255); // Purple
            case "Marketing":
                return Color.rgb(0, 255, 170);  // Green
            default:
                return CYBER_PURPLE;
        }
    }
    
    // Helper method to convert Color to CSS RGB string
    private String toRgbString(Color color) {
        return String.format("#%02X%02X%02X", 
            (int)(color.getRed() * 255), 
            (int)(color.getGreen() * 255), 
            (int)(color.getBlue() * 255));
    }
} 
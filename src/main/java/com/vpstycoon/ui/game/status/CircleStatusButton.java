package com.vpstycoon.ui.game.status;

import com.vpstycoon.FontLoader;
import com.vpstycoon.audio.AudioManager;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.ui.game.GameplayContentPane;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import javafx.util.Duration;
import com.vpstycoon.game.company.SkillPointsSystem;

/**
 * Creates a circular status button with number and label in cyberpunk style.
 */
public class CircleStatusButton {
    private final VBox container;
    private VBox upgradeLayout;
    private Runnable onClickAction;
    private SkillPointsSystem skillPointsSystem;
    private int skillLevel;
    private int skillPoints;
    private Label numberLabel;
    private String skillName;
    private SkillPointsSystem.SkillType skillType;
    private final GameplayContentPane parent;
    private AudioManager audioManager;

    // Cyberpunk theme colors
    private static final Color CYBER_PURPLE = Color.rgb(200, 50, 255);
    private static final Color CYBER_DARK = Color.rgb(20, 10, 30);
    private static final Color CYBER_GLOW = Color.rgb(255, 0, 255, 0.7);

    private SkillPointsSystem.SkillType resolveSkillType(String name) {
        switch (name.toLowerCase()) {
            case "rack slots":
                return SkillPointsSystem.SkillType.RACK_SLOTS;
            case "network":
            case "network speed":
                return SkillPointsSystem.SkillType.NETWORK_SPEED;
            case "deploy":
                return SkillPointsSystem.SkillType.DEPLOY;
            case "server efficiency":
                return SkillPointsSystem.SkillType.SERVER_EFFICIENCY;
            case "marketing":
                return SkillPointsSystem.SkillType.MARKETING;
            case "security":
                return SkillPointsSystem.SkillType.SECURITY;
            case "management":
                return SkillPointsSystem.SkillType.MANAGEMENT;
            default:
                throw new IllegalArgumentException("Unknown skill type: " + name);
        }
    }

    public CircleStatusButton(String labelText, Color topColor, Color bottomColor, GameplayContentPane parent) {
        this.skillName = labelText;
        this.parent = parent;
        this.skillPointsSystem = ResourceManager.getInstance().getSkillPointsSystem();
        this.skillType = resolveSkillType(labelText);
        this.skillLevel = skillPointsSystem.getSkillLevel(skillType);
        this.skillPoints = skillPointsSystem.getAvailablePoints();
        this.container = createContainer(skillName, topColor, bottomColor);
        this.audioManager = ResourceManager.getInstance().getAudioManager();
    }

    private VBox createContainer(String skillName, Color topColor, Color bottomColor) {
        VBox container = new VBox(12);
        container.setAlignment(Pos.CENTER);

        StackPane buttonFrame = createCyberButton(topColor, bottomColor);
        numberLabel = new Label("Lv" + skillLevel);
        numberLabel.setTextFill(Color.WHITE);
        numberLabel.setFont(FontLoader.loadFont(42));

        Glow textGlow = new Glow(0.8);
        numberLabel.setEffect(textGlow);

        StackPane buttonStack = new StackPane();
        buttonStack.getChildren().addAll(buttonFrame, numberLabel);

        buttonStack.setOnMouseEntered(e -> {
            buttonStack.setScaleX(1.05);
            buttonStack.setScaleY(1.05);
            audioManager.playSoundEffect("hover.wav"); // Added hover sound
        });

        buttonStack.setOnMouseExited(e -> {
            buttonStack.setScaleX(1.0);
            buttonStack.setScaleY(1.0);
        });

        buttonStack.setOnMouseClicked((MouseEvent e) -> {
            openUpgradePanel();
            audioManager.playSoundEffect("click.wav");
        });

        Label textLabel = createCyberLabel(skillName, topColor);
        container.getChildren().addAll(buttonStack, textLabel);
        return container;
    }

    private StackPane createCyberButton(Color topColor, Color bottomColor) {
        StackPane buttonStack = new StackPane();

        Rectangle mainSquare = new Rectangle(70, 70);
        mainSquare.setArcWidth(0);
        mainSquare.setArcHeight(0);

        Stop[] stops = new Stop[] {
                new Stop(0, topColor),
                new Stop(0.7, bottomColor),
                new Stop(1, CYBER_DARK)
        };
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);
        mainSquare.setFill(gradient);

        mainSquare.setStroke(topColor);
        mainSquare.setStrokeWidth(2);

        DropShadow glow = new DropShadow();
        glow.setRadius(8);
        glow.setColor(topColor);
        glow.setSpread(0.15);
        mainSquare.setEffect(glow);

        Pane pixelCorners = new Pane();

        Rectangle topLeft = new Rectangle(5, 5, topColor);
        topLeft.setTranslateX(9);
        topLeft.setTranslateY(-4);

        Rectangle topRight = new Rectangle(5, 5, topColor);
        topRight.setTranslateX(86);
        topRight.setTranslateY(-4);

        Rectangle bottomLeft = new Rectangle(5, 5, topColor);
        bottomLeft.setTranslateX(9);
        bottomLeft.setTranslateY(70);

        Rectangle bottomRight = new Rectangle(5, 5, topColor);
        bottomRight.setTranslateX(86);
        bottomRight.setTranslateY(70);

        Pane pixelDetails = new Pane();
        pixelDetails.setTranslateX(5);

        for (int i = 10; i < 65; i += 10) {
            Rectangle scanLine = new Rectangle(60, 1);
            scanLine.setFill(Color.rgb(255, 255, 255, 0.1));
            scanLine.setTranslateX(15);
            scanLine.setTranslateY(i);
            pixelDetails.getChildren().add(scanLine);
        }

        pixelCorners.getChildren().addAll(topLeft, topRight, bottomLeft, bottomRight);
        buttonStack.getChildren().addAll(mainSquare, pixelCorners, pixelDetails);

        return buttonStack;
    }

    private Label createCyberLabel(String text, Color color) {
        Label label = new Label(text);
        label.setPrefWidth(100);
        label.setAlignment(Pos.CENTER);

        BackgroundFill bgFill = new BackgroundFill(CYBER_DARK, new CornerRadii(0), Insets.EMPTY);
        BorderStroke borderStroke = new BorderStroke(color, BorderStrokeStyle.SOLID,
                new CornerRadii(0), new BorderWidths(2));
        Border pixelBorder = new Border(borderStroke);

        label.setBackground(new Background(bgFill));
        label.setBorder(pixelBorder);
        label.setPadding(new Insets(6));

        DropShadow glow = new DropShadow();
        glow.setColor(color);
        glow.setRadius(5);
        glow.setSpread(0.1);
        label.setEffect(glow);

        label.setTextFill(Color.WHITE);
        label.setFont(Font.font("Monospace", FontWeight.BOLD, 12));

        return label;
    }

    private void openUpgradePanel() {
        // Refresh current values
        this.skillLevel = skillPointsSystem.getSkillLevel(skillType);
        this.skillPoints = skillPointsSystem.getAvailablePoints();

        Rectangle backgroundOverlay = new Rectangle();
        backgroundOverlay.setFill(Color.rgb(0, 0, 0, 0.7));
        backgroundOverlay.widthProperty().bind(parent.getRootStack().widthProperty());
        backgroundOverlay.heightProperty().bind(parent.getRootStack().heightProperty());

        upgradeLayout = new VBox(15);
        upgradeLayout.setAlignment(Pos.CENTER);
        upgradeLayout.setPadding(new Insets(20));
        upgradeLayout.setPrefSize(400, 250); // Increased height to accommodate new info
        upgradeLayout.setMaxSize(400, 250);

        upgradeLayout.setBackground(new Background(new BackgroundFill(
                Color.rgb(0, 0, 0, 0.95), new CornerRadii(0), Insets.EMPTY)));

        Color skillColor = getColorForSkill(skillName);
        upgradeLayout.setBorder(new Border(new BorderStroke(
                skillColor, BorderStrokeStyle.SOLID, new CornerRadii(0), new BorderWidths(2))));

        for (int i = 0; i < 250; i += 4) {
            Rectangle scanLine = new Rectangle(400, 1);
            scanLine.setFill(Color.rgb(255, 255, 255, 0.03));
            scanLine.setTranslateY(i);
            upgradeLayout.getChildren().add(scanLine);
        }

        DropShadow panelGlow = new DropShadow();
        panelGlow.setColor(skillColor);
        panelGlow.setRadius(10);
        panelGlow.setSpread(0.2);
        upgradeLayout.setEffect(panelGlow);

        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER);
        titleBar.setSpacing(50);

        Label titleLabel = new Label("UPGRADE " + skillName.toUpperCase() + " Lv" + skillLevel);
        titleLabel.setTextFill(skillColor);
        titleLabel.setFont(Font.font("Monospace", FontWeight.BOLD, 16));

        Timeline glitchTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0.8), evt -> titleLabel.setTextFill(skillColor)),
                new KeyFrame(Duration.seconds(0.9), evt -> titleLabel.setTextFill(Color.WHITE)),
                new KeyFrame(Duration.seconds(1.0), evt -> titleLabel.setTextFill(skillColor))
        );
        glitchTimeline.setCycleCount(Timeline.INDEFINITE);
        glitchTimeline.play();

        Button exitButton = new Button("X");
        styleButton(exitButton, skillColor, false);
        exitButton.setOnAction(e -> {
            parent.getRootStack().getChildren().removeAll(upgradeLayout, backgroundOverlay);
        });

        titleBar.getChildren().addAll(titleLabel, exitButton);

        Label pointsLabel = new Label("AVAILABLE POINTS: " + skillPoints);
        pointsLabel.setTextFill(Color.LIGHTGRAY);
        pointsLabel.setFont(Font.font("Monospace", FontWeight.NORMAL, 14));
        pointsLabel.setPadding(new Insets(5, 10, 5, 10));
        pointsLabel.setBorder(new Border(new BorderStroke(
                Color.LIGHTGRAY, BorderStrokeStyle.SOLID, new CornerRadii(0), new BorderWidths(1))));

        // Add skill description
        Label descLabel = new Label(skillPointsSystem.getSkillLevelDescription(skillType, skillLevel + 1));
        descLabel.setTextFill(Color.WHITE);
        descLabel.setFont(Font.font("Monospace", FontWeight.NORMAL, 12));
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(360);
        descLabel.setAlignment(Pos.CENTER);  // Center text within the label
        HBox descContainer = new HBox(descLabel);  // Wrap in HBox for horizontal centering
        descContainer.setAlignment(Pos.CENTER);   // Center the label in the HBox
        VBox.setMargin(descContainer, new Insets(10, 0, 10, 0));  // Add vertical spacing

        Label costLabel = new Label("UPGRADE COST: " +
                skillPointsSystem.calculateUpgradeCost(skillLevel) + " points");
        costLabel.setTextFill(skillLevel < skillType.getMaxLevel() ? Color.CYAN : Color.RED);
        costLabel.setFont(Font.font("Monospace", FontWeight.NORMAL, 12));

        Button upgradeButton = new Button("[ UPGRADE ]");
        styleButton(upgradeButton, skillColor, true);

        // Disable button if at max level
        upgradeButton.setDisable(skillLevel >= skillType.getMaxLevel());

        upgradeButton.setOnAction(e -> {
            boolean upgraded = skillPointsSystem.upgradeSkill(skillType);
            if (upgraded) {
                skillLevel = skillPointsSystem.getSkillLevel(skillType);
                skillPoints = skillPointsSystem.getAvailablePoints();
                numberLabel.setText("Lv" + skillLevel);
                titleLabel.setText("UPGRADE " + skillName.toUpperCase() + " Lv" + skillLevel);
                pointsLabel.setText("AVAILABLE POINTS: " + skillPoints);
                descLabel.setText(skillPointsSystem.getSkillLevelDescription(skillType, skillLevel + 1));
                costLabel.setText("UPGRADE COST: " +
                        skillPointsSystem.calculateUpgradeCost(skillLevel) + " points");
                costLabel.setTextFill(skillLevel < skillType.getMaxLevel() ? Color.CYAN : Color.RED);
                upgradeButton.setDisable(skillLevel >= skillType.getMaxLevel());
                parent.pushNotification("Skill Upgrade", "Upgraded " + skillName + " to Lv" + skillLevel);
                audioManager.playSoundEffect("upgrade.wav");
            } else {
                parent.pushNotification("Upgrade Failed", "Not enough points to upgrade " + skillName);
                audioManager.playSoundEffect("error.wav");
            }
        });

        VBox contentBox = new VBox(15);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.getChildren().addAll(titleBar, pointsLabel, descLabel, costLabel, upgradeButton);

        StackPane mainContent = new StackPane();
        mainContent.getChildren().add(contentBox);

        upgradeLayout.getChildren().clear();
        upgradeLayout.getChildren().add(mainContent);

        parent.getRootStack().getChildren().addAll(backgroundOverlay, upgradeLayout);
        upgradeLayout.toFront();
        StackPane.setAlignment(upgradeLayout, Pos.CENTER);

        parent.getRootStack().addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            if (!upgradeLayout.getBoundsInParent().contains(event.getX(), event.getY())) {
                parent.getRootStack().getChildren().removeAll(upgradeLayout, backgroundOverlay);
            }
        });
    }

    private void styleButton(Button button, Color skillColor, boolean isUpgradeButton) {
        String baseStyle = "-fx-background-color: transparent;" +
                "-fx-text-fill: " + toRgbString(skillColor) + ";" +
                "-fx-font-family: 'Monospace';" +
                "-fx-font-weight: bold;" +
                "-fx-border-color: " + toRgbString(skillColor) + ";" +
                "-fx-border-width: 2px;";
        String hoverStyle = "-fx-background-color: " + toRgbString(skillColor) + ";" +
                "-fx-text-fill: #000000;" +
                "-fx-font-family: 'Monospace';" +
                "-fx-font-weight: bold;" +
                "-fx-border-color: " + toRgbString(skillColor) + ";" +
                "-fx-border-width: 2px;";

        if (isUpgradeButton) {
            baseStyle += "-fx-padding: 8px 16px;";
            hoverStyle += "-fx-padding: 8px 16px;";
        }

        button.setStyle(baseStyle);

        String finalHoverStyle = hoverStyle;
        button.setOnMouseEntered(e -> button.setStyle(finalHoverStyle));
        String finalBaseStyle = baseStyle;
        button.setOnMouseExited(e -> button.setStyle(finalBaseStyle));
    }

    private Color getColorForSkill(String skillName) {
        switch (skillName.toLowerCase()) {
            case "deploy":
            case "server efficiency":
                return Color.rgb(255, 50, 180); // Pink
            case "network":
            case "network speed":
                return Color.rgb(50, 200, 255); // Blue
            case "security":
                return Color.rgb(200, 50, 255); // Purple
            case "marketing":
                return Color.rgb(0, 255, 170);  // Green
            case "rack slots":
                return Color.rgb(255, 200, 50); // Yellow
            case "management":
                return Color.rgb(100, 255, 100); // Light Green
            default:
                return CYBER_PURPLE;
        }
    }

    private String toRgbString(Color color) {
        return String.format("#%02X%02X%02X",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }

    public VBox getContainer() {
        return container;
    }

    // Getter methods for external access if needed
    public int getSkillLevel() {
        return skillLevel;
    }

    public int getSkillPoints() {
        return skillPoints;
    }

    public SkillPointsSystem.SkillType getSkillType() {
        return skillType;
    }

    public void setOnClickAction(Runnable action) {
        this.onClickAction = action;
    }
}
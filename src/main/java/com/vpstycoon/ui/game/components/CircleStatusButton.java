package com.vpstycoon.ui.game.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Modality;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.geometry.Insets;


import java.util.HashMap;

/**
 * Creates a circular status button with number and label.
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
        this.skillName = labelText; // กำหนดค่า skillName ก่อนดึงค่าจาก HashMap
        this.skillLevel = skillLevels.getOrDefault(skillName, 1);
        this.skillPoints = skillPointsMap.getOrDefault(skillName, number); // ใช้ number เป็นค่าเริ่มต้นถ้าไม่มีค่าใน HashMap
        this.container = createContainer(skillName, topColor, bottomColor);
    }

    private VBox createContainer(String skillName, Color topColor, Color bottomColor) {
        VBox container = new VBox(12);
        container.setAlignment(Pos.CENTER);

        // Create outer white circle (shadow)
        Circle outerCircle = new Circle(38);
        outerCircle.setEffect(new DropShadow(10, Color.BLACK));
        Stop[] outerCircleStops = new Stop[] {
                new Stop(0, Color.rgb(255, 255, 255)),
                new Stop(1, Color.rgb(220, 220, 220))
        };
        LinearGradient outerCircleGradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, outerCircleStops);
        outerCircle.setFill(outerCircleGradient);

        // Create inner colored circle (gradient)
        Circle innerCircle = new Circle(30);
        DropShadow innerShadow = new DropShadow();
        innerShadow.setRadius(2);
        innerShadow.setColor(Color.rgb(0, 0, 0, 0.4));
        innerShadow.setOffsetY(2);
        innerCircle.setEffect(innerShadow);
        
        // Create gradient from specified colors
        Stop[] stops = new Stop[] {
                new Stop(0, topColor),
                new Stop(1, bottomColor)
        };
        LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);
        innerCircle.setFill(gradient);

        // Add number
        numberLabel = new Label(String.valueOf(skillPoints));
        numberLabel.setTextFill(Color.WHITE);
        numberLabel.setFont(Font.font("System", FontWeight.BOLD, 20));

        // Stack circles and number
        StackPane circleStack = new StackPane();
        circleStack.getChildren().addAll(outerCircle, innerCircle, numberLabel);

        circleStack.setOnMouseClicked((MouseEvent e) -> openUpgradePanel());

        // Create label below
        Label textLabel = new Label(skillName);
        textLabel.setPrefWidth(80);
        textLabel.setAlignment(Pos.CENTER);
        textLabel.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, new CornerRadii(4), Insets.EMPTY)));
        textLabel.setPadding(new Insets(6));
        
        // Create gradient for label
        Stop[] labelStops = new Stop[] {
                new Stop(0, Color.rgb(255, 255, 255)),
                new Stop(1, Color.rgb(220, 220, 220))
        };
        LinearGradient labelGradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, labelStops);
        
        // Set background and border
        textLabel.setBackground(new Background(new BackgroundFill(
                labelGradient, new CornerRadii(4), Insets.EMPTY)));
        
        // Add padding to label
        textLabel.setPadding(new Insets(6));
        
        // Set shadow
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.3));
        shadow.setRadius(10);
        shadow.setOffsetY(1);
        textLabel.setEffect(shadow);
        
        // Set text
        textLabel.setTextFill(Color.rgb(100, 100, 100));
        textLabel.setFont(Font.font("System", FontWeight.NORMAL, 12));
        textLabel.setAlignment(Pos.CENTER);

        // Add all to VBox
        container.getChildren().addAll(circleStack, textLabel);
        return container;
    }
    
    public VBox getContainer() {
        return container;
    }
    private void openUpgradePanel() {
        Stage upgradeStage = new Stage();
        upgradeStage.initModality(Modality.APPLICATION_MODAL); // Set as modal
        upgradeStage.initStyle(StageStyle.TRANSPARENT); // Make the window transparent
        upgradeStage.setResizable(false); // Disable resizing

        VBox upgradeLayout = new VBox(10);
        upgradeLayout.setAlignment(Pos.CENTER);
        upgradeLayout.setPadding(new Insets(15));
        upgradeLayout.setBackground(new Background(new BackgroundFill(Color.web("#1E2A38"), new CornerRadii(15), Insets.EMPTY)));
        upgradeLayout.setEffect(new DropShadow(15, Color.BLACK)); // Stronger shadow to blend with background

        // Exit Button in top right corner
        HBox titleBar = new HBox();
        Label titleLabel = new Label("Upgrade " + skillName + " (Lv: " + skillLevel + ")");
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        Button exitButton = new Button("X");
        exitButton.setStyle("-fx-background-color: #e94560; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        exitButton.setOnAction(e -> upgradeStage.close());

        titleBar.getChildren().addAll(titleLabel, exitButton);
        titleBar.setSpacing(50);
        titleBar.setAlignment(Pos.CENTER);

        // Show available points
        Label pointsLabel = new Label("Available Points: " + skillPoints);
        pointsLabel.setTextFill(Color.LIGHTGRAY);
        pointsLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));

        Button upgradeButton = new Button("Upgrade");
        upgradeButton.setStyle("-fx-background-color: #e94560; -fx-text-fill: white; -fx-background-radius: 8;");
        upgradeButton.setOnAction(e -> upgradeSkill(upgradeStage));

        upgradeLayout.getChildren().addAll(titleBar, pointsLabel, upgradeButton);

        Scene scene = new Scene(upgradeLayout, 320, 180);
        scene.setFill(Color.TRANSPARENT); // Make scene background transparent
        upgradeStage.setScene(scene);
        upgradeStage.showAndWait(); // Use showAndWait to enforce modal behavior
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
} 
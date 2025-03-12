package com.vpstycoon.ui.game.desktop;

import com.vpstycoon.game.SkillPointsSystem;
import com.vpstycoon.game.SkillPointsSystem.SkillType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Window for displaying and managing skill points
 */
public class SkillPointsWindow extends VBox {
    private final SkillPointsSystem skillPointsSystem;
    private final Runnable onClose;
    private Label availablePointsLabel;
    
    public SkillPointsWindow(SkillPointsSystem skillPointsSystem, Runnable onClose) {
        this.skillPointsSystem = skillPointsSystem;
        this.onClose = onClose;
        
        setupUI();
        styleWindow();
    }
    
    private void setupUI() {
        setPrefSize(700, 600);
        
        // Title Bar
        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_RIGHT);
        titleBar.setPadding(new Insets(8, 15, 8, 15));
        titleBar.setStyle("-fx-background-color: linear-gradient(to right, #2c3e50, #3498db);");

        Label titleLabel = new Label("Skill Points");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-text-fill: white; -fx-font-weight: bold;");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        titleLabel.setAlignment(Pos.CENTER_LEFT);

        Button closeButton = new Button("✕");
        closeButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 2 8;");
        closeButton.setOnAction(e -> onClose.run());

        titleBar.getChildren().addAll(titleLabel, closeButton);
        
        // Available Points Section
        HBox pointsBox = new HBox();
        pointsBox.setAlignment(Pos.CENTER);
        pointsBox.setPadding(new Insets(15));
        pointsBox.setStyle("-fx-background-color: #ecf0f1; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        Label pointsTitle = new Label("Available Skill Points: ");
        pointsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        availablePointsLabel = new Label(String.valueOf(skillPointsSystem.getAvailablePoints()));
        availablePointsLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2980b9;");
        
        pointsBox.getChildren().addAll(pointsTitle, availablePointsLabel);
        
        // Skills Section
        VBox skillsContainer = new VBox(15);
        skillsContainer.setPadding(new Insets(15));
        
        // Add each skill type
        for (SkillType skillType : SkillType.values()) {
            skillsContainer.getChildren().add(createSkillCard(skillType));
        }
        
        // Help Section
        VBox helpBox = new VBox(10);
        helpBox.setPadding(new Insets(15));
        helpBox.setStyle("-fx-background-color: #ecf0f1; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        Label helpTitle = new Label("How to Earn Skill Points");
        helpTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label helpText = new Label("• Complete customer requests to earn skill points\n" +
                "• Higher difficulty requests give more skill points\n" +
                "• Maintain high company rating for bonus points\n" +
                "• Upgrade skills to unlock new features and improve performance");
        helpText.setStyle("-fx-font-size: 14px;");
        
        helpBox.getChildren().addAll(helpTitle, helpText);
        
        // Add all sections to the main container
        getChildren().addAll(titleBar, pointsBox, skillsContainer, helpBox);
    }
    
    private BorderPane createSkillCard(SkillType skillType) {
        BorderPane card = new BorderPane();
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-width: 1; " +
                "-fx-border-radius: 5; -fx-background-radius: 5; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        // Left side - Skill info
        VBox infoBox = new VBox(5);
        Label nameLabel = new Label(skillType.getName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label descLabel = new Label(skillType.getDescription());
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        
        int currentLevel = skillPointsSystem.getSkillLevel(skillType);
        Label levelLabel = new Label("Level: " + currentLevel + "/" + skillType.getMaxLevel());
        levelLabel.setStyle("-fx-font-size: 14px;");
        
        // Progress bar
        ProgressBar progressBar = new ProgressBar((double) currentLevel / skillType.getMaxLevel());
        progressBar.setPrefWidth(200);
        
        // Current bonus
        String bonusText = "";
        switch (skillType) {
            case RACK_SLOTS:
                bonusText = "Current bonus: +" + (currentLevel * 2) + " rack slots";
                break;
            case NETWORK_SPEED:
                bonusText = "Current bonus: +" + (currentLevel * 20) + "% network speed";
                break;
            case SERVER_EFFICIENCY:
                bonusText = "Current bonus: +" + (currentLevel * 15) + "% server efficiency";
                break;
            case MARKETING:
                bonusText = "Current bonus: +" + (currentLevel * 25) + "% customer acquisition";
                break;
            case SECURITY:
                bonusText = "Current bonus: +" + (currentLevel * 50) + "% security level";
                if (currentLevel >= 2) {
                    bonusText += " (Firewall management unlocked)";
                }
                break;
            case MANAGEMENT:
                bonusText = "Current bonus: +" + (currentLevel * 20) + "% management efficiency";
                break;
        }
        
        Label bonusLabel = new Label(bonusText);
        bonusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #27ae60;");
        
        infoBox.getChildren().addAll(nameLabel, descLabel, levelLabel, progressBar, bonusLabel);
        
        // Right side - Upgrade button
        Button upgradeButton = new Button("Upgrade");
        upgradeButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        
        // Disable button if max level or not enough points
        boolean canUpgrade = currentLevel < skillType.getMaxLevel() && 
                skillPointsSystem.getAvailablePoints() >= (currentLevel + 1);
        upgradeButton.setDisable(!canUpgrade);
        
        // Set tooltip with upgrade info
        if (currentLevel < skillType.getMaxLevel()) {
            int cost = currentLevel + 1;
            String nextLevelDesc = skillPointsSystem.getSkillLevelDescription(skillType, currentLevel + 1);
            Tooltip tooltip = new Tooltip("Cost: " + cost + " points\n" + nextLevelDesc);
            Tooltip.install(upgradeButton, tooltip);
        } else {
            Tooltip tooltip = new Tooltip("Maximum level reached");
            Tooltip.install(upgradeButton, tooltip);
        }
        
        // Handle upgrade
        upgradeButton.setOnAction(e -> {
            if (skillPointsSystem.upgradeSkill(skillType)) {
                // Update UI after successful upgrade
                updateUI();
            }
        });
        
        card.setCenter(infoBox);
        card.setRight(upgradeButton);
        
        return card;
    }
    
    private void styleWindow() {
        setStyle("-fx-background-color: #f5f6fa; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 5); " +
                "-fx-border-radius: 10; -fx-background-radius: 10;");
    }
    
    /**
     * Update the UI to reflect current skill points and levels
     */
    public void updateUI() {
        // Update available points
        availablePointsLabel.setText(String.valueOf(skillPointsSystem.getAvailablePoints()));
        
        // Recreate all skill cards
        VBox skillsContainer = (VBox) getChildren().get(2);
        skillsContainer.getChildren().clear();
        
        for (SkillType skillType : SkillType.values()) {
            skillsContainer.getChildren().add(createSkillCard(skillType));
        }
    }
} 
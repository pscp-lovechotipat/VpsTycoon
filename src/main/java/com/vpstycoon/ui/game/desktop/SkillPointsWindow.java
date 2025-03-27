package com.vpstycoon.ui.game.desktop;

import com.vpstycoon.game.company.SkillPointsSystem;
import com.vpstycoon.game.company.SkillPointsSystem.SkillType;
import com.vpstycoon.game.resource.ResourceManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class SkillPointsWindow extends VBox {
    private final SkillPointsSystem skillPointsSystem;
    private final Runnable onClose;
    private Label availablePointsLabel;

    public SkillPointsWindow(SkillPointsSystem skillPointsSystem, Runnable onClose) {
        if (skillPointsSystem == null) {
            throw new IllegalArgumentException("SkillPointsSystem cannot be null");
        }
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
        setMargin(titleBar, new Insets(30,0,0,0));
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

        availablePointsLabel = new Label();
        availablePointsLabel.setText(String.valueOf(skillPointsSystem.getAvailablePoints()));
        availablePointsLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2980b9;");

        pointsBox.getChildren().addAll(pointsTitle, availablePointsLabel);

        // Scrollable Content
        ScrollPane scrollPane = (ScrollPane) getChildren().get(2);
        scrollPane.setFitToWidth(true);  // Makes content stretch to window width
        scrollPane.setStyle("-fx-background: #f5f6fa; -fx-border-color: transparent;"); // Match window background

        VBox scrollContent = (VBox) scrollPane.getContent();
        scrollContent.setPadding(new Insets(0, 15, 15, 15)); // Match original padding

        // Skills Section
        VBox skillsContainer = (VBox) scrollContent.getChildren().get(0);

        skillsContainer.getChildren().clear();

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

        // Add skills and help to scrollable content
        scrollContent.getChildren().addAll(skillsContainer, helpBox);
        scrollPane.setContent(scrollContent);

        // Add all sections to the main container
        getChildren().addAll(titleBar, pointsBox, scrollPane);

        // Make scrollPane take remaining space
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
    }

    private BorderPane createSkillCard(SkillType skillType) {
        // [Original createSkillCard method remains unchanged]
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

        ProgressBar progressBar = new ProgressBar((double) currentLevel / skillType.getMaxLevel());
        progressBar.setPrefWidth(200);

        String bonusText = "";
        switch (skillType) {
            case RACK_SLOTS:
                bonusText = "Current bonus: +" + (currentLevel * 2) + " rack slots";
                if (currentLevel > 1) {
                    bonusText += ", +" + ((currentLevel - 1) * 10) + " Gbps network speed per rack";
                }
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

        Button upgradeButton = new Button("Upgrade");
        upgradeButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");

        boolean canUpgrade = currentLevel < skillType.getMaxLevel() &&
                skillPointsSystem.getAvailablePoints() >= (currentLevel + 1);
        upgradeButton.setDisable(!canUpgrade);

        if (currentLevel < skillType.getMaxLevel()) {
            int cost = currentLevel + 1;
            String nextLevelDesc = skillPointsSystem.getSkillLevelDescription(skillType, currentLevel + 1);
            Tooltip tooltip = new Tooltip("Cost: " + cost + " points\n" + nextLevelDesc);
            Tooltip.install(upgradeButton, tooltip);
        } else {
            Tooltip tooltip = new Tooltip("Maximum level reached");
            Tooltip.install(upgradeButton, tooltip);
        }

        upgradeButton.setOnAction(e -> {
            if (skillPointsSystem.upgradeSkill(skillType)) {
                updateUI();
                
                // If upgrading RACK_SLOTS, notify parent to update rack UI
                if (skillType == SkillType.RACK_SLOTS) {
                    // Update any open RackManagementUI instances
                    ResourceManager.getInstance().notifyRackUIUpdate();
                }
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

    public void updateUI() {
        availablePointsLabel.setText(String.valueOf(skillPointsSystem.getAvailablePoints()));

        // Update skills container within scroll pane
        ScrollPane scrollPane = (ScrollPane) getChildren().get(2);
        VBox scrollContent = (VBox) scrollPane.getContent();
        VBox skillsContainer = (VBox) scrollContent.getChildren().get(0);
        skillsContainer.getChildren().clear();

        for (SkillType skillType : SkillType.values()) {
            skillsContainer.getChildren().add(createSkillCard(skillType));
        }
    }
}
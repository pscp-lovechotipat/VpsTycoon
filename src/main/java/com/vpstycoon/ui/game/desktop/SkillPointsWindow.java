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

        
        ScrollPane scrollPane = (ScrollPane) getChildren().get(2);
        scrollPane.setFitToWidth(true);  
        scrollPane.setStyle("-fx-background: #f5f6fa; -fx-border-color: transparent;"); 

        VBox scrollContent = (VBox) scrollPane.getContent();
        scrollContent.setPadding(new Insets(0, 15, 15, 15)); 

        
        VBox skillsContainer = (VBox) scrollContent.getChildren().get(0);

        skillsContainer.getChildren().clear();

        
        for (SkillType skillType : SkillType.values()) {
            skillsContainer.getChildren().add(createSkillCard(skillType));
        }

        
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

        
        scrollContent.getChildren().addAll(skillsContainer, helpBox);
        scrollPane.setContent(scrollContent);

        
        getChildren().addAll(titleBar, pointsBox, scrollPane);

        
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
    }

    private BorderPane createSkillCard(SkillType skillType) {
        
        BorderPane card = new BorderPane();
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-width: 1; " +
                "-fx-border-radius: 5; -fx-background-radius: 5; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        
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
                if (currentLevel > 1) {
                    bonusText += ", " + ((currentLevel - 1) * 10) + "% discount on market purchases";
                }
                break;
            case SECURITY:
                bonusText = "Current bonus: +" + (currentLevel * 50) + "% security level";
                if (currentLevel >= 2) {
                    bonusText += " (Firewall management unlocked)";
                    
                    
                    if (currentLevel == 2) {
                        bonusText += ", +3% VM payment bonus";
                    } else if (currentLevel == 3) {
                        bonusText += ", +5% VM payment bonus";
                    } else if (currentLevel == 4) {
                        bonusText += ", +10% VM payment bonus";
                    }
                }
                break;
            case MANAGEMENT:
                bonusText = "Current bonus: +" + (currentLevel * 20) + "% management efficiency";
                break;
            case DEPLOY:
                if (currentLevel == 1) {
                    bonusText = "Current bonus: No deployment time reduction";
                } else {
                    int reductionPercent = switch (currentLevel) {
                        case 2 -> 20;
                        case 3 -> 40;
                        case 4 -> 60;
                        default -> 0;
                    };
                    bonusText = "Current bonus: " + reductionPercent + "% VM deployment time reduction";
                }
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
                
                
                if (skillType == SkillType.RACK_SLOTS) {
                    
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

        
        ScrollPane scrollPane = (ScrollPane) getChildren().get(2);
        VBox scrollContent = (VBox) scrollPane.getContent();
        VBox skillsContainer = (VBox) scrollContent.getChildren().get(0);
        skillsContainer.getChildren().clear();

        for (SkillType skillType : SkillType.values()) {
            skillsContainer.getChildren().add(createSkillCard(skillType));
        }
    }
}


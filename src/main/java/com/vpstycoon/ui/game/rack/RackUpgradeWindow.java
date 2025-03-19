package com.vpstycoon.ui.game.rack;

import com.vpstycoon.game.SkillPointsSystem;
import com.vpstycoon.game.company.Company;
import com.vpstycoon.ui.game.GameplayContentPane;
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
 * Window for upgrading racks and room capacity
 */
public class RackUpgradeWindow extends VBox {
    private final GameplayContentPane parent;
    private final Runnable onClose;
    private final Company company;
    private final SkillPointsSystem skillPointsSystem;
    
    // Current rack and room stats
    private int numberOfRacks;
    private int maxNumberOfRacks;
    private int rackSlots;
    
    // UI components
    private Label moneyLabel;
    private Label racksLabel;
    private Label slotsLabel;
    private Button buyRackButton;
    private Button upgradeRoomButton;
    
    // Costs
    private final long rackCost = 5000;
    private final long roomUpgradeCost = 20000;
    
    public RackUpgradeWindow(GameplayContentPane parent, Runnable onClose) {
        this.parent = parent;
        this.onClose = onClose;
        this.company = parent.getCompany();
        this.skillPointsSystem = parent.getSkillPointsSystem();
        
        // Initialize current stats
        this.numberOfRacks = 1;
        this.maxNumberOfRacks = 10; // Default max racks
        this.rackSlots = skillPointsSystem != null ?
                skillPointsSystem.getAvailableRackSlots() : 4; // Default slots per rack
        
        setupUI();
        styleWindow();
    }
    
    private void setupUI() {
        setPrefSize(700, 500);
        
        // Title Bar
        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_RIGHT);
        titleBar.setPadding(new Insets(8, 15, 8, 15));
        titleBar.setStyle("-fx-background-color: linear-gradient(to right, #2c3e50, #3498db);");

        Label titleLabel = new Label("Rack & Room Management");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-text-fill: white; -fx-font-weight: bold;");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        titleLabel.setAlignment(Pos.CENTER_LEFT);

        Button closeButton = new Button("✕");
        closeButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 2 8;");
        closeButton.setOnAction(e -> onClose.run());

        titleBar.getChildren().addAll(titleLabel, closeButton);
        
        // Current Stats Section
        VBox statsBox = new VBox(15);
        statsBox.setPadding(new Insets(15));
        statsBox.setStyle("-fx-background-color: #ecf0f1; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        // Money display
        HBox moneyBox = new HBox(10);
        moneyBox.setAlignment(Pos.CENTER_LEFT);
        Label moneyTitleLabel = new Label("Available Funds:");
        moneyTitleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        moneyLabel = new Label("$" + company.getMoney());
        moneyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
        
        moneyBox.getChildren().addAll(moneyTitleLabel, moneyLabel);
        
        // Racks display
        HBox racksBox = new HBox(10);
        racksBox.setAlignment(Pos.CENTER_LEFT);
        Label racksTitleLabel = new Label("Racks:");
        racksTitleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        racksLabel = new Label(numberOfRacks + " / " + maxNumberOfRacks);
        racksLabel.setStyle("-fx-font-size: 16px;");

        ProgressBar racksProgressBar = new ProgressBar((double) numberOfRacks / maxNumberOfRacks);
        racksProgressBar.setPrefWidth(200);
        
        racksBox.getChildren().addAll(racksTitleLabel, racksLabel, racksProgressBar);
        
        // Slots display
        HBox slotsBox = new HBox(10);
        slotsBox.setAlignment(Pos.CENTER_LEFT);
        Label slotsTitleLabel = new Label("Slots per Rack:");
        slotsTitleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        slotsLabel = new Label(String.valueOf(rackSlots));
        slotsLabel.setStyle("-fx-font-size: 16px;");
        
        slotsBox.getChildren().addAll(slotsTitleLabel, slotsLabel);
        
        statsBox.getChildren().addAll(moneyBox, racksBox, slotsBox);
        
        // Upgrade Options Section
        VBox upgradeBox = new VBox(20);
        upgradeBox.setPadding(new Insets(15));
        
        // Buy Rack Option
        BorderPane buyRackPane = createUpgradeOption(
                "Buy New Rack",
                "Purchase a new server rack to increase your hosting capacity.",
                "$" + rackCost,
                numberOfRacks < maxNumberOfRacks && company.getMoney() >= rackCost
        );
        
        buyRackButton = (Button) buyRackPane.getRight();
        buyRackButton.setOnAction(e -> buyNewRack());
        
        // Upgrade Room Option
        BorderPane upgradeRoomPane = createUpgradeOption(
                "Upgrade Server Room",
                "Expand your server room to accommodate more racks.",
                "$" + roomUpgradeCost,
                company.getMoney() >= roomUpgradeCost
        );
        
        upgradeRoomButton = (Button) upgradeRoomPane.getRight();
        upgradeRoomButton.setOnAction(e -> upgradeRoom());
        
        // Skill Points Info
        BorderPane skillPointsPane = createUpgradeOption(
                "Upgrade Rack Slots",
                "Use skill points to increase the number of slots per rack.",
                "Via Skill Points",
                true
        );
        
        Button skillPointsButton = (Button) skillPointsPane.getRight();
        skillPointsButton.setText("Go to Skills");
        skillPointsButton.setOnAction(e -> parent.openSkillPointsWindow());
        
        upgradeBox.getChildren().addAll(buyRackPane, upgradeRoomPane, skillPointsPane);
        
        // Help Section
        VBox helpBox = new VBox(10);
        helpBox.setPadding(new Insets(15));
        helpBox.setStyle("-fx-background-color: #ecf0f1; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        Label helpTitle = new Label("Rack & Room Management");
        helpTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label helpText = new Label(
                "• Each rack can hold multiple VPS servers\n" +
                "• The number of slots per rack can be increased with skill points\n" +
                "• Upgrading your server room allows for more racks\n" +
                "• More racks and slots means more potential revenue"
        );
        helpText.setStyle("-fx-font-size: 14px;");
        
        helpBox.getChildren().addAll(helpTitle, helpText);
        
        // Add all sections to the main container
        getChildren().addAll(titleBar, statsBox, upgradeBox, helpBox);
    }
    
    private BorderPane createUpgradeOption(String title, String description, String cost, boolean enabled) {
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(10));
        pane.setStyle("-fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-width: 1; " +
                "-fx-border-radius: 5; -fx-background-radius: 5; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        // Left side - Info
        VBox infoBox = new VBox(5);
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        
        Label costLabel = new Label("Cost: " + cost);
        costLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #e74c3c;");
        
        infoBox.getChildren().addAll(titleLabel, descLabel, costLabel);
        
        // Right side - Button
        Button actionButton = new Button("Purchase");
        actionButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        actionButton.setDisable(!enabled);
        
        if (!enabled) {
            Tooltip tooltip = new Tooltip("Not enough money or maximum reached");
            Tooltip.install(actionButton, tooltip);
        }
        
        pane.setCenter(infoBox);
        pane.setRight(actionButton);
        
        return pane;
    }
    
    private void styleWindow() {
        setStyle("-fx-background-color: #f5f6fa; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 5); " +
                "-fx-border-radius: 10; -fx-background-radius: 10;");
    }
    
    /**
     * Buy a new rack
     */
    private void buyNewRack() {
        if (numberOfRacks < maxNumberOfRacks && company.getMoney() >= rackCost) {
            company.setMoney(company.getMoney() - rackCost);
            numberOfRacks++;
            updateUI();
            parent.pushNotification("New Rack Purchased",
                    "You have successfully purchased a new server rack.");
        }
    }
    
    /**
     * Upgrade the server room
     */
    private void upgradeRoom() {
        if (company.getMoney() >= roomUpgradeCost) {
            company.setMoney(company.getMoney() - roomUpgradeCost);
            maxNumberOfRacks += 5;
            updateUI();
            parent.pushNotification("Server Room Upgraded",
                    "Your server room has been expanded. You can now install up to " +
                            maxNumberOfRacks + " racks.");
        }
    }
    
    /**
     * Update the UI to reflect current stats
     */
    public void updateUI() {
        moneyLabel.setText("$" + company.getMoney());
        racksLabel.setText(numberOfRacks + " / " + maxNumberOfRacks);
        rackSlots = skillPointsSystem != null ?
                skillPointsSystem.getAvailableRackSlots() : 4;
        slotsLabel.setText(String.valueOf(rackSlots));
        buyRackButton.setDisable(!(numberOfRacks < maxNumberOfRacks && company.getMoney() >= rackCost));
        upgradeRoomButton.setDisable(!(company.getMoney() >= roomUpgradeCost));
    }
}
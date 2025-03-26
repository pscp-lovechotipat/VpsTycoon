package com.vpstycoon.ui.game.rack;

import com.vpstycoon.game.vps.VPSOptimization;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import java.util.List;

public class RackStatus extends VBox {
    private final Rack rack;
    private final Label rackInfoLabel;
    private final Label vpsListLabel;
    private final VBox vpsList;
    private final Button prevRackButton;
    private final Button nextRackButton;
    private final Button upgradeButton;
    private final Label upgradeCostLabel;
    private final Label upgradeInfoLabel;
    private final Label serverCountLabel;
    private final Label networkUsageLabel;
    private final Label userCountLabel;

    public RackStatus(Rack rack) {
        this.rack = rack;
        setSpacing(10);
        setPadding(new Insets(10));
        setStyle("""
            -fx-background-color: #1a1a1a;
            -fx-padding: 10px;
            -fx-background-radius: 5px;
            -fx-border-color: #00ff00;
            -fx-border-width: 1px;
            -fx-border-style: solid;
            """);

        // Rack navigation
        HBox navigationBox = new HBox(10);
        navigationBox.setAlignment(Pos.CENTER);
        
        prevRackButton = new Button("← PREV RACK");
        nextRackButton = new Button("NEXT RACK →");
        
        String buttonStyle = """
            -fx-background-color: #1a1a1a;
            -fx-text-fill: #00ff00;
            -fx-font-weight: bold;
            -fx-padding: 10px;
            -fx-background-radius: 5px;
            -fx-border-color: #00ff00;
            -fx-border-width: 1px;
            -fx-border-style: solid;
            """;
        
        prevRackButton.setStyle(buttonStyle);
        nextRackButton.setStyle(buttonStyle);
        
        // Add hover effects
        prevRackButton.setOnMouseEntered(e -> 
            prevRackButton.setStyle(buttonStyle + "-fx-background-color: #00ff00; -fx-text-fill: #000000;")
        );
        prevRackButton.setOnMouseExited(e -> 
            prevRackButton.setStyle(buttonStyle)
        );
        
        nextRackButton.setOnMouseEntered(e -> 
            nextRackButton.setStyle(buttonStyle + "-fx-background-color: #00ff00; -fx-text-fill: #000000;")
        );
        nextRackButton.setOnMouseExited(e -> 
            nextRackButton.setStyle(buttonStyle)
        );

        navigationBox.getChildren().addAll(prevRackButton, nextRackButton);
        getChildren().add(navigationBox);

        // Rack info
        rackInfoLabel = new Label();
        rackInfoLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-weight: bold;");
        getChildren().add(rackInfoLabel);

        // Server count
        serverCountLabel = new Label();
        serverCountLabel.setStyle("-fx-text-fill: #00ff00;");
        getChildren().add(serverCountLabel);

        // Network usage
        networkUsageLabel = new Label();
        networkUsageLabel.setStyle("-fx-text-fill: #00ff00;");
        getChildren().add(networkUsageLabel);

        // User count
        userCountLabel = new Label();
        userCountLabel.setStyle("-fx-text-fill: #00ff00;");
        getChildren().add(userCountLabel);

        // VPS list
        vpsListLabel = new Label("INSTALLED VPS");
        vpsListLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-weight: bold;");
        getChildren().add(vpsListLabel);

        vpsList = new VBox(5);
        vpsList.setStyle("""
            -fx-background-color: #2a2a2a;
            -fx-padding: 10px;
            -fx-background-radius: 5px;
            -fx-border-color: #00ff00;
            -fx-border-width: 1px;
            -fx-border-style: solid;
            """);

        ScrollPane scrollPane = new ScrollPane(vpsList);
        scrollPane.setStyle("""
            -fx-background-color: transparent;
            -fx-background: transparent;
            -fx-background-insets: 0;
            -fx-padding: 0;
            """);
        scrollPane.setFitToWidth(true);
        getChildren().add(scrollPane);

        // Upgrade section
        upgradeButton = new Button("UPGRADE RACK");
        upgradeButton.setStyle(buttonStyle);
        upgradeButton.setOnMouseEntered(e -> 
            upgradeButton.setStyle(buttonStyle + "-fx-background-color: #00ff00; -fx-text-fill: #000000;")
        );
        upgradeButton.setOnMouseExited(e -> 
            upgradeButton.setStyle(buttonStyle)
        );
        getChildren().add(upgradeButton);

        upgradeCostLabel = new Label();
        upgradeCostLabel.setStyle("-fx-text-fill: #00ff00;");
        getChildren().add(upgradeCostLabel);

        upgradeInfoLabel = new Label();
        upgradeInfoLabel.setStyle("-fx-text-fill: #00ff00;");
        getChildren().add(upgradeInfoLabel);

        // Set up button actions
        prevRackButton.setOnAction(e -> {
            if (rack.prevRack()) {
                updateUI();
            }
        });

        nextRackButton.setOnAction(e -> {
            if (rack.nextRack()) {
                updateUI();
            }
        });

        upgradeButton.setOnAction(e -> {
            if (rack.upgrade()) {
                updateUI();
            }
        });

        // Initial UI update
        updateUI();
    }

    private void updateUI() {
        // Update rack info
        rackInfoLabel.setText(String.format("RACK %d/%d", rack.getRackIndex() + 1, rack.getMaxRacks()));
        
        // Update server count
        List<VPSOptimization> installedVPS = rack.getInstalledVPS();
        serverCountLabel.setText(String.format("SERVER COUNT: %d", installedVPS.size()));
        
        // Calculate total network usage
        double totalNetworkUsage = installedVPS.stream()
            .mapToDouble(VPSOptimization::getNetworkUsage)
            .sum();
        networkUsageLabel.setText(String.format("NETWORK USAGE: %.1f%%", totalNetworkUsage));
        
        // Calculate total users (assuming each vCPU can handle 10 users)
        int totalUsers = installedVPS.stream()
            .mapToInt(vps -> vps.getVCPUs() * 10)
            .sum();
        userCountLabel.setText(String.format("ACTIVE USERS: %d", totalUsers));
        
        // Update VPS list
        vpsList.getChildren().clear();
        
        if (installedVPS.isEmpty()) {
            Label emptyLabel = new Label("NO VPS INSTALLED");
            emptyLabel.setStyle("-fx-text-fill: #00ff00;");
            vpsList.getChildren().add(emptyLabel);
        } else {
            for (VPSOptimization vps : installedVPS) {
                VBox vpsBox = new VBox(5);
                vpsBox.setStyle("""
                    -fx-background-color: #1a1a1a;
                    -fx-padding: 10px;
                    -fx-background-radius: 5px;
                    -fx-border-color: #00ff00;
                    -fx-border-width: 1px;
                    -fx-border-style: solid;
                    """);
                
                Label nameLabel = new Label("Server " + vps.getVCPUs() + "vCPU");
                nameLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-weight: bold;");
                
                Label specsLabel = new Label(vps.getRamInGB() + "GB RAM");
                specsLabel.setStyle("-fx-text-fill: #00ff00;");
                
                Label sizeLabel = new Label(vps.getSize().getDisplayName());
                sizeLabel.setStyle("-fx-text-fill: #00ff00;");
                
                Label networkLabel = new Label(String.format("NETWORK: %.1f%%", vps.getNetworkUsage()));
                networkLabel.setStyle("-fx-text-fill: #00ff00;");
                
                vpsBox.getChildren().addAll(nameLabel, specsLabel, sizeLabel, networkLabel);
                vpsList.getChildren().add(vpsBox);
            }
        }
        
        // Update upgrade info
        int availableSlots = rack.getAvailableSlotUnits();
        int totalSlots = rack.getMaxSlotUnits();
        upgradeInfoLabel.setText(String.format("AVAILABLE SLOTS: %d/%d", availableSlots, totalSlots));
        
        // Update upgrade button state
        upgradeButton.setDisable(rack.getUnlockedSlotUnits() >= rack.getMaxSlotUnits());
        
        // Update upgrade cost
        int upgradeCost = calculateUpgradeCost();
        upgradeCostLabel.setText(String.format("UPGRADE COST: $%d", upgradeCost));
    }

    private int calculateUpgradeCost() {
        int currentUnlocked = rack.getUnlockedSlotUnits();
        return currentUnlocked * 1000; // Cost increases with each upgrade
    }
} 
package com.vpstycoon.ui.game.inventory;

import com.vpstycoon.game.vps.VPSOptimization;
import com.vpstycoon.ui.game.GameplayContentPane;
import com.vpstycoon.ui.game.utils.UIUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Map;

/**
 * UI for displaying and managing the VPS inventory
 */
public class VPSInventoryUI {
    private final GameplayContentPane parent;
    
    public VPSInventoryUI(GameplayContentPane parent) {
        this.parent = parent;
    }
    
    /**
     * Open the VPS inventory UI
     */
    public void openInventory() {
        // Create main container
        BorderPane inventoryPane = new BorderPane();
        inventoryPane.setPrefSize(800, 600);
        inventoryPane.setStyle("-fx-background-color: linear-gradient(to bottom, #1a0033, #000022); -fx-padding: 20px;");
        
        // Hide the menu bars
        parent.getMenuBar().setVisible(false);
        parent.getInGameMarketMenuBar().setVisible(false);
        
        // Create title bar
        HBox titleBar = new HBox(20);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setStyle("-fx-background-color: #2a0a3a; -fx-padding: 10px; -fx-background-radius: 5px; " +
                          "-fx-border-color: #8a2be2; -fx-border-width: 2px; " +
                          "-fx-effect: dropshadow(gaussian, rgba(110,0,220,0.4), 10, 0, 0, 5);");
        
        Label titleLabel = new Label("SERVER INVENTORY");
        titleLabel.setStyle("-fx-text-fill: #e0b0ff; -fx-font-size: 28px; -fx-font-weight: bold; " +
                           "-fx-effect: dropshadow(gaussian, #9370db, 2, 0.3, 0, 0);");
        
        Button backButton = UIUtils.createModernButton("Back", "#F44336");
        backButton.setOnAction(e -> {
            parent.openRackInfo();
        });
        
        titleBar.getChildren().addAll(backButton, titleLabel);
        
        // Create inventory content
        VBox contentBox = new VBox(20);
        contentBox.setPadding(new Insets(20));
        
        // Inventory stats
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER_LEFT);
        statsBox.setPadding(new Insets(15));
        statsBox.setStyle("-fx-background-color: #2a0a3a; -fx-background-radius: 5px; " +
                         "-fx-border-color: #8a2be2; -fx-border-width: 1px; " +
                         "-fx-effect: dropshadow(gaussian, rgba(110,0,220,0.3), 5, 0, 0, 2);");
        
        Label inventoryCountLabel = new Label("SERVERS IN INVENTORY: " + parent.getVpsInventory().getSize());
        inventoryCountLabel.setStyle("-fx-text-fill: #e0b0ff; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label rackStatsLabel = new Label("RACK SLOTS: " + parent.getRack().getOccupiedSlotUnits() + "/" + parent.getRack().getMaxSlotUnits());
        rackStatsLabel.setStyle("-fx-text-fill: #e0b0ff; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        statsBox.getChildren().addAll(inventoryCountLabel, rackStatsLabel);
        
        // Inventory list
        VBox inventoryList = createInventoryList();
        
        // Add to scroll pane for large inventories
        ScrollPane scrollPane = new ScrollPane(inventoryList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; " +
                           "-fx-control-inner-background: #1a0033; -fx-border-color: #8a2be2; -fx-border-width: 1px;");
        
        // Add components to content box
        contentBox.getChildren().addAll(statsBox, scrollPane);
        
        // Add components to main pane
        inventoryPane.setTop(titleBar);
        inventoryPane.setCenter(contentBox);
        
        // Add to game area
        parent.getGameArea().getChildren().clear();
        parent.getGameArea().getChildren().add(inventoryPane);
    }
    
    /**
     * Create the inventory list
     * @return VBox containing the inventory items
     */
    private VBox createInventoryList() {
        VBox inventoryList = new VBox(10);
        inventoryList.setPadding(new Insets(10));
        
        // Get inventory items
        Map<String, VPSOptimization> inventory = parent.getVpsInventory().getInventoryMap();
        
        if (inventory.isEmpty()) {
            Label emptyLabel = new Label("YOUR INVENTORY IS EMPTY. PURCHASE SERVERS FROM THE MARKET.");
            emptyLabel.setStyle("-fx-text-fill: #e0b0ff; -fx-font-size: 16px; -fx-font-style: italic;");
            inventoryList.getChildren().add(emptyLabel);
            return inventoryList;
        }
        
        // Add header
        HBox header = new HBox(20);
        header.setPadding(new Insets(10, 10, 10, 10));
        header.setStyle("-fx-background-color: #3a1a4a; -fx-background-radius: 5px; " +
                       "-fx-border-color: #b041ff; -fx-border-width: 1px;");
        
        Label idHeader = new Label("SERVER ID");
        idHeader.setMinWidth(150);
        idHeader.setStyle("-fx-text-fill: #f0d0ff; -fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label specsHeader = new Label("SPECIFICATIONS");
        specsHeader.setMinWidth(200);
        specsHeader.setStyle("-fx-text-fill: #f0d0ff; -fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label sizeHeader = new Label("SIZE");
        sizeHeader.setMinWidth(80);
        sizeHeader.setStyle("-fx-text-fill: #f0d0ff; -fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label actionsHeader = new Label("ACTIONS");
        actionsHeader.setMinWidth(150);
        actionsHeader.setStyle("-fx-text-fill: #f0d0ff; -fx-font-weight: bold; -fx-font-size: 14px;");
        
        header.getChildren().addAll(idHeader, specsHeader, sizeHeader, actionsHeader);
        inventoryList.getChildren().add(header);
        
        // Add inventory items
        for (Map.Entry<String, VPSOptimization> entry : inventory.entrySet()) {
            String vpsId = entry.getKey();
            VPSOptimization vps = entry.getValue();
            
            HBox itemRow = createInventoryItemRow(vpsId, vps);
            inventoryList.getChildren().add(itemRow);
        }
        
        return inventoryList;
    }
    
    /**
     * Create a row for an inventory item
     * @param vpsId The VPS ID
     * @param vps The VPS object
     * @return HBox containing the item information
     */
    private HBox createInventoryItemRow(String vpsId, VPSOptimization vps) {
        HBox itemRow = new HBox(20);
        itemRow.setPadding(new Insets(10));
        itemRow.setStyle("-fx-background-color: #2a0a3a; -fx-background-radius: 5px; " +
                        "-fx-border-color: #8a2be2; -fx-border-width: 1px;");
        
        // Add hover effect
        itemRow.setOnMouseEntered(e -> 
            itemRow.setStyle("-fx-background-color: #3a1a4a; -fx-background-radius: 5px; " +
                           "-fx-border-color: #b041ff; -fx-border-width: 2px; " +
                           "-fx-effect: dropshadow(gaussian, #b041ff, 10, 0.3, 0, 0);")
        );
        
        itemRow.setOnMouseExited(e -> 
            itemRow.setStyle("-fx-background-color: #2a0a3a; -fx-background-radius: 5px; " +
                           "-fx-border-color: #8a2be2; -fx-border-width: 1px;")
        );
        
        // VPS ID
        Label idLabel = new Label(vpsId);
        idLabel.setMinWidth(150);
        idLabel.setStyle("-fx-text-fill: #e0b0ff; -fx-font-weight: bold;");
        
        // Specifications
        Label specsLabel = new Label(String.format(
                "vCPUs: %d, RAM: %d GB, Disk: %d GB",
                vps.getVCPUs(), vps.getRamInGB(), vps.getDiskInGB()
        ));
        specsLabel.setMinWidth(200);
        specsLabel.setStyle("-fx-text-fill: #e0b0ff;");
        
        // Size
        Label sizeLabel = new Label(vps.getSize().getDisplayName() + " (" + vps.getSlotsRequired() + " slots)");
        sizeLabel.setMinWidth(80);
        sizeLabel.setStyle("-fx-text-fill: #e0b0ff;");
        
        // Actions
        HBox actionsBox = new HBox(10);
        
        Button installButton = UIUtils.createModernButton("Install", "#27ae60");
        installButton.setOnAction(e -> installVPS(vpsId, vps));
        
        Button detailsButton = UIUtils.createModernButton("Details", "#3498db");
        detailsButton.setOnAction(e -> showVPSDetails(vpsId, vps));
        
        actionsBox.getChildren().addAll(installButton, detailsButton);
        
        // Add all to row
        itemRow.getChildren().addAll(idLabel, specsLabel, sizeLabel, actionsBox);
        
        return itemRow;
    }
    
    /**
     * Install a VPS from inventory to rack
     * @param vpsId The VPS ID
     * @param vps The VPS object
     */
    private void installVPS(String vpsId, VPSOptimization vps) {
        // Check if there are enough slots available
        if (vps.getSlotsRequired() > parent.getRack().getAvailableSlotUnits()) {
            parent.pushNotification("Installation Failed",
                    "Not enough slots available in the rack. You need " + vps.getSlotsRequired() +
                            " slots, but only " + parent.getRack().getAvailableSlotUnits() + " are available.");
            return;
        }

        // Install the VPS
        boolean success = parent.installVPSFromInventory(vpsId);
        
        if (success) {
            parent.pushNotification("Server Installed", 
                    "Successfully installed " + vpsId + " into the rack.");
            
            // Go directly to rack view instead of refreshing inventory
            parent.openRackInfo();
        } else {
            parent.pushNotification("Installation Failed", 
                    "Failed to install " + vpsId + " into the rack.");
        }
    }
    
    /**
     * Show details for a VPS
     * @param vpsId The VPS ID
     * @param vps The VPS object
     */
    private void showVPSDetails(String vpsId, VPSOptimization vps) {
        // Create a popup with VPS details
        BorderPane detailsPane = new BorderPane();
        detailsPane.setPrefSize(400, 300);
        detailsPane.setStyle("-fx-background-color: linear-gradient(to bottom, #1a0033, #000022); " +
                           "-fx-padding: 20px; -fx-background-radius: 10px; -fx-border-color: #8a2be2; " +
                           "-fx-border-width: 2px; -fx-effect: dropshadow(gaussian, rgba(110,0,220,0.4), 10, 0, 0, 5);");
        
        // Title
        Label titleLabel = new Label("SERVER DETAILS: " + vpsId);
        titleLabel.setStyle("-fx-text-fill: #e0b0ff; -fx-font-size: 20px; -fx-font-weight: bold; " +
                          "-fx-effect: dropshadow(gaussian, #9370db, 2, 0.3, 0, 0);");
        
        // Details
        VBox detailsBox = new VBox(10);
        detailsBox.setPadding(new Insets(20, 0, 20, 0));
        detailsBox.setStyle("-fx-background-color: #2a0a3a; -fx-padding: 15px; -fx-background-radius: 5px; " +
                          "-fx-border-color: #8a2be2; -fx-border-width: 1px;");
        
        Label vcpuLabel = new Label("vCPUs: " + vps.getVCPUs());
        vcpuLabel.setStyle("-fx-text-fill: #e0b0ff; -fx-font-size: 14px;");
        
        Label ramLabel = new Label("RAM: " + vps.getRamInGB() + " GB");
        ramLabel.setStyle("-fx-text-fill: #e0b0ff; -fx-font-size: 14px;");
        
        Label diskLabel = new Label("Disk: " + vps.getDiskInGB() + " GB");
        diskLabel.setStyle("-fx-text-fill: #e0b0ff; -fx-font-size: 14px;");
        
        Label sizeLabel = new Label("Size: " + vps.getSize().getDisplayName());
        sizeLabel.setStyle("-fx-text-fill: #e0b0ff; -fx-font-size: 14px;");
        
        Label slotsLabel = new Label("Slots Required: " + vps.getSlotsRequired());
        slotsLabel.setStyle("-fx-text-fill: #e0b0ff; -fx-font-size: 14px;");
        
        Label statusLabel = new Label("Status: " + vps.getStatus());
        statusLabel.setStyle("-fx-text-fill: #e0b0ff; -fx-font-size: 14px;");
        
        detailsBox.getChildren().addAll(vcpuLabel, ramLabel, diskLabel, sizeLabel, slotsLabel, statusLabel);
        
        // Close button
        Button closeButton = UIUtils.createModernButton("Close", "#e74c3c");
        closeButton.setOnAction(e -> openInventory());
        
        // Add components to details pane
        detailsPane.setTop(titleLabel);
        detailsPane.setCenter(detailsBox);
        detailsPane.setBottom(closeButton);
        BorderPane.setAlignment(closeButton, Pos.CENTER);
        BorderPane.setMargin(titleLabel, new Insets(0, 0, 10, 0));
        BorderPane.setMargin(closeButton, new Insets(10, 0, 0, 0));
        
        // Add to game area
        parent.getGameArea().getChildren().clear();
        parent.getGameArea().getChildren().add(detailsPane);
    }
} 
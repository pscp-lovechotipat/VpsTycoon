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
        inventoryPane.setStyle("-fx-background-color: linear-gradient(to bottom, #2E3B4E, #1A252F); -fx-padding: 20px;");
        
        // Hide the menu bars
        parent.getMenuBar().setVisible(false);
        parent.getInGameMarketMenuBar().setVisible(false);
        
        // Create title bar
        HBox titleBar = new HBox(20);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setStyle("-fx-background-color: #37474F; -fx-padding: 10px; -fx-background-radius: 10px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);");
        
        Label titleLabel = new Label("VPS Inventory");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");
        
        Button backButton = UIUtils.createModernButton("Back", "#F44336");
        backButton.setOnAction(e -> parent.returnToRoom());
        
        titleBar.getChildren().addAll(backButton, titleLabel);
        
        // Create inventory content
        VBox contentBox = new VBox(20);
        contentBox.setPadding(new Insets(20));
        
        // Inventory stats
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER_LEFT);
        statsBox.setPadding(new Insets(10));
        statsBox.setStyle("-fx-background-color: #37474F; -fx-background-radius: 5px;");
        
        Label inventoryCountLabel = new Label("VPS in Inventory: " + parent.getVpsInventory().getSize());
        inventoryCountLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        
        Label rackStatsLabel = new Label("Rack Slots: " + parent.getOccupiedSlots() + "/" + parent.getTotalSlots());
        rackStatsLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        
        statsBox.getChildren().addAll(inventoryCountLabel, rackStatsLabel);
        
        // Inventory list
        VBox inventoryList = createInventoryList();
        
        // Add to scroll pane for large inventories
        ScrollPane scrollPane = new ScrollPane(inventoryList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
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
            Label emptyLabel = new Label("Your inventory is empty. Purchase VPS servers from the market.");
            emptyLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
            inventoryList.getChildren().add(emptyLabel);
            return inventoryList;
        }
        
        // Add header
        HBox header = new HBox(20);
        header.setPadding(new Insets(5, 10, 5, 10));
        header.setStyle("-fx-background-color: #2c3e50; -fx-background-radius: 5px;");
        
        Label idHeader = new Label("VPS ID");
        idHeader.setMinWidth(150);
        idHeader.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        
        Label specsHeader = new Label("Specifications");
        specsHeader.setMinWidth(200);
        specsHeader.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        
        Label sizeHeader = new Label("Size");
        sizeHeader.setMinWidth(80);
        sizeHeader.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        
        Label actionsHeader = new Label("Actions");
        actionsHeader.setMinWidth(150);
        actionsHeader.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        
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
        itemRow.setStyle("-fx-background-color: #34495e; -fx-background-radius: 5px;");
        
        // VPS ID
        Label idLabel = new Label(vpsId);
        idLabel.setMinWidth(150);
        idLabel.setStyle("-fx-text-fill: white;");
        
        // Specifications
        Label specsLabel = new Label(String.format(
                "vCPUs: %d, RAM: %d GB, Disk: %d GB",
                vps.getVCPUs(), vps.getRamInGB(), vps.getDiskInGB()
        ));
        specsLabel.setMinWidth(200);
        specsLabel.setStyle("-fx-text-fill: white;");
        
        // Size
        Label sizeLabel = new Label(vps.getSize().getDisplayName() + " (" + vps.getSlotsRequired() + " slots)");
        sizeLabel.setMinWidth(80);
        sizeLabel.setStyle("-fx-text-fill: white;");
        
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
        if (parent.getOccupiedSlots() + vps.getSlotsRequired() > parent.getTotalSlots()) {
            parent.pushNotification("Installation Failed", 
                    "Not enough slots available in the rack. You need " + vps.getSlotsRequired() + 
                    " slots, but only " + (parent.getTotalSlots() - parent.getOccupiedSlots()) + " are available.");
            return;
        }
        
        // Install the VPS
        boolean success = parent.installVPSFromInventory(vpsId);
        
        if (success) {
            parent.pushNotification("VPS Installed", 
                    "Successfully installed " + vpsId + " into the rack.");
            
            // Refresh the inventory UI
            openInventory();
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
        detailsPane.setStyle("-fx-background-color: #2c3e50; -fx-padding: 20px; -fx-background-radius: 10px;");
        
        // Title
        Label titleLabel = new Label("VPS Details: " + vpsId);
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        
        // Details
        VBox detailsBox = new VBox(10);
        detailsBox.setPadding(new Insets(20, 0, 20, 0));
        
        Label vcpuLabel = new Label("vCPUs: " + vps.getVCPUs());
        vcpuLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        
        Label ramLabel = new Label("RAM: " + vps.getRamInGB() + " GB");
        ramLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        
        Label diskLabel = new Label("Disk: " + vps.getDiskInGB() + " GB");
        diskLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        
        Label sizeLabel = new Label("Size: " + vps.getSize().getDisplayName());
        sizeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        
        Label slotsLabel = new Label("Slots Required: " + vps.getSlotsRequired());
        slotsLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        
        Label statusLabel = new Label("Status: " + vps.getStatus());
        statusLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        
        detailsBox.getChildren().addAll(vcpuLabel, ramLabel, diskLabel, sizeLabel, slotsLabel, statusLabel);
        
        // Close button
        Button closeButton = UIUtils.createModernButton("Close", "#e74c3c");
        closeButton.setOnAction(e -> openInventory());
        
        // Add components to details pane
        detailsPane.setTop(titleLabel);
        detailsPane.setCenter(detailsBox);
        detailsPane.setBottom(closeButton);
        BorderPane.setAlignment(closeButton, Pos.CENTER);
        
        // Add to game area
        parent.getGameArea().getChildren().clear();
        parent.getGameArea().getChildren().add(detailsPane);
    }
} 
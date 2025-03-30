package com.vpstycoon.ui.game.inventory;

import com.vpstycoon.game.GameObject;
import com.vpstycoon.game.GameState;
import com.vpstycoon.game.resource.ResourceManager;
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


public class VPSInventoryUI {
    private final GameplayContentPane parent;
    
    public VPSInventoryUI(GameplayContentPane parent) {
        this.parent = parent;
    }
    
    
    public void openInventory() {
        
        BorderPane inventoryPane = new BorderPane();
        inventoryPane.setPrefSize(800, 600);
        inventoryPane.setStyle("-fx-background-color: linear-gradient(to bottom, #1a0033, #000022); -fx-padding: 20px;");
        
        
        parent.getMenuBar().setVisible(false);
        parent.getMoneyUI().setVisible(false);
        parent.getInGameMarketMenuBar().setVisible(false);
        
        
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
        
        
        VBox contentBox = new VBox(20);
        contentBox.setPadding(new Insets(20));
        
        
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER_LEFT);
        statsBox.setPadding(new Insets(15));
        statsBox.setStyle("-fx-background-color: #2a0a3a; -fx-background-radius: 5px; " +
                         "-fx-border-color: #8a2be2; -fx-border-width: 1px; " +
                         "-fx-effect: dropshadow(gaussian, rgba(110,0,220,0.3), 5, 0, 0, 2);");
        
        
        boolean inventoryUpdated = false;
        GameState currentState = ResourceManager.getInstance().getCurrentState();
        if (currentState != null) {
            
            for (GameObject obj : currentState.getGameObjects()) {
                if (obj instanceof VPSOptimization) {
                    VPSOptimization vps = (VPSOptimization) obj;
                    if (!vps.isInstalled()) {
                        
                        if (!parent.getVpsInventory().getInventoryMap().containsKey(vps.getVpsId())) {
                            parent.getVpsInventory().addVPS(vps.getVpsId(), vps);
                            inventoryUpdated = true;
                        }
                    }
                }
            }
            
            if (inventoryUpdated) {
                System.out.println("อัปเดตคลัง VPS จาก GameState เรียบร้อยแล้ว");
            }
        }
        
        Label inventoryCountLabel = new Label("SERVERS IN INVENTORY: " + parent.getVpsInventory().getSize());
        inventoryCountLabel.setStyle("-fx-text-fill: #e0b0ff; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label rackStatsLabel = new Label("RACK SLOTS: " + parent.getRack().getOccupiedSlotUnits() + "/" + parent.getRack().getMaxSlotUnits());
        rackStatsLabel.setStyle("-fx-text-fill: #e0b0ff; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        
        Label moneyLabel = new Label("BALANCE: $" + ResourceManager.getInstance().getCompany().getMoney());
        moneyLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        statsBox.getChildren().addAll(inventoryCountLabel, rackStatsLabel, moneyLabel);
        
        
        VBox inventoryList = createInventoryList();
        
        
        ScrollPane scrollPane = new ScrollPane(inventoryList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; " +
                           "-fx-control-inner-background: #1a0033; -fx-border-color: #8a2be2; -fx-border-width: 1px;");
        
        
        contentBox.getChildren().addAll(statsBox, scrollPane);
        
        
        inventoryPane.setTop(titleBar);
        inventoryPane.setCenter(contentBox);
        
        
        parent.getGameArea().getChildren().clear();
        parent.getGameArea().getChildren().add(inventoryPane);
    }
    
    
    private VBox createInventoryList() {
        VBox inventoryList = new VBox(10);
        inventoryList.setPadding(new Insets(10));
        
        
        Map<String, VPSOptimization> inventory = parent.getVpsInventory().getInventoryMap();
        
        if (inventory.isEmpty()) {
            Label emptyLabel = new Label("YOUR INVENTORY IS EMPTY. PURCHASE SERVERS FROM THE MARKET.");
            emptyLabel.setStyle("-fx-text-fill: #e0b0ff; -fx-font-size: 16px; -fx-font-style: italic;");
            inventoryList.getChildren().add(emptyLabel);
            return inventoryList;
        }
        
        
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
        
        Label statusHeader = new Label("STATUS");
        statusHeader.setMinWidth(100);
        statusHeader.setStyle("-fx-text-fill: #f0d0ff; -fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label actionsHeader = new Label("ACTIONS");
        actionsHeader.setMinWidth(150);
        actionsHeader.setStyle("-fx-text-fill: #f0d0ff; -fx-font-weight: bold; -fx-font-size: 14px;");
        
        header.getChildren().addAll(idHeader, specsHeader, sizeHeader, statusHeader, actionsHeader);
        inventoryList.getChildren().add(header);
        
        
        GameState currentState = ResourceManager.getInstance().getCurrentState();
        
        
        for (Map.Entry<String, VPSOptimization> entry : inventory.entrySet()) {
            String vpsId = entry.getKey();
            VPSOptimization vps = entry.getValue();
            
            
            boolean syncedWithGameState = false;
            if (currentState != null && currentState.getGameObjects() != null) {
                for (GameObject obj : currentState.getGameObjects()) {
                    if (obj instanceof VPSOptimization) {
                        VPSOptimization stateVps = (VPSOptimization) obj;
                        if (stateVps.getVpsId().equals(vpsId)) {
                            syncedWithGameState = true;
                            break;
                        }
                    }
                }
            }
            
            HBox itemRow = createInventoryItemRow(vpsId, vps, syncedWithGameState);
            inventoryList.getChildren().add(itemRow);
        }
        
        return inventoryList;
    }
    
    
    private HBox createInventoryItemRow(String vpsId, VPSOptimization vps, boolean syncedWithGameState) {
        HBox itemRow = new HBox(20);
        itemRow.setPadding(new Insets(10));
        itemRow.setStyle("-fx-background-color: #2a0a3a; -fx-background-radius: 5px; " +
                        "-fx-border-color: #8a2be2; -fx-border-width: 1px;");
        
        
        itemRow.setOnMouseEntered(e -> 
            itemRow.setStyle("-fx-background-color: #3a1a4a; -fx-background-radius: 5px; " +
                           "-fx-border-color: #b041ff; -fx-border-width: 2px; " +
                           "-fx-effect: dropshadow(gaussian, #b041ff, 10, 0.3, 0, 0);")
        );
        
        itemRow.setOnMouseExited(e -> 
            itemRow.setStyle("-fx-background-color: #2a0a3a; -fx-background-radius: 5px; " +
                           "-fx-border-color: #8a2be2; -fx-border-width: 1px;")
        );
        
        
        Label idLabel = new Label(vpsId + (syncedWithGameState ? "" : " [SYNCING]"));
        idLabel.setMinWidth(150);
        idLabel.setStyle("-fx-text-fill: #e0b0ff; -fx-font-weight: bold;");
        
        
        Label specsLabel = new Label(String.format(
                "vCPUs: %d, RAM: %d GB, Disk: %d GB",
                vps.getVCPUs(), vps.getRamInGB(), vps.getDiskInGB()
        ));
        specsLabel.setMinWidth(200);
        specsLabel.setStyle("-fx-text-fill: #e0b0ff;");
        
        
        Label sizeLabel = new Label(vps.getSize().getDisplayName() + " (" + vps.getSlotsRequired() + " slots)");
        sizeLabel.setMinWidth(80);
        sizeLabel.setStyle("-fx-text-fill: #e0b0ff;");
        
        
        Label statusLabel = new Label(syncedWithGameState ? "READY" : "SYNCING");
        statusLabel.setMinWidth(100);
        statusLabel.setStyle("-fx-text-fill: " + (syncedWithGameState ? "#00ff00;" : "#ffaa00;") + 
                           "-fx-font-weight: bold;");
        
        
        HBox actionsBox = new HBox(10);
        
        Button installButton = UIUtils.createModernButton("Install", "#27ae60");
        installButton.setOnAction(e -> installVPS(vpsId, vps));
        
        Button detailsButton = UIUtils.createModernButton("Details", "#3498db");
        detailsButton.setOnAction(e -> showVPSDetails(vpsId, vps));
        
        
        if (!syncedWithGameState) {
            installButton.setDisable(true);
            installButton.setStyle(installButton.getStyle() + "-fx-opacity: 0.5;");
        }
        
        actionsBox.getChildren().addAll(installButton, detailsButton);
        
        
        itemRow.getChildren().addAll(idLabel, specsLabel, sizeLabel, statusLabel, actionsBox);
        
        return itemRow;
    }
    
    
    private void installVPS(String vpsId, VPSOptimization vps) {
        
        if (vps.getSlotsRequired() > parent.getRack().getAvailableSlotUnits()) {
            parent.pushNotification("Installation Failed",
                    "Not enough slots available in the rack. You need " + vps.getSlotsRequired() +
                            " slots, but only " + parent.getRack().getAvailableSlotUnits() + " are available.");
            return;
        }

        
        boolean success = parent.installVPSFromInventory(vpsId);
        
        if (success) {
            parent.pushNotification("Server Installed", 
                    "Successfully installed " + vpsId + " into the rack.");
            
            
            parent.openRackInfo();
        } else {
            parent.pushNotification("Installation Failed", 
                    "Failed to install " + vpsId + " into the rack.");
        }
    }
    
    
    private void showVPSDetails(String vpsId, VPSOptimization vps) {
        
        BorderPane detailsPane = new BorderPane();
        detailsPane.setPrefSize(400, 300);
        detailsPane.setStyle("-fx-background-color: linear-gradient(to bottom, #1a0033, #000022); " +
                           "-fx-padding: 20px; -fx-background-radius: 10px; -fx-border-color: #8a2be2; " +
                           "-fx-border-width: 2px; -fx-effect: dropshadow(gaussian, rgba(110,0,220,0.4), 10, 0, 0, 5);");
        
        
        Label titleLabel = new Label("SERVER DETAILS: " + vpsId);
        titleLabel.setStyle("-fx-text-fill: #e0b0ff; -fx-font-size: 20px; -fx-font-weight: bold; " +
                          "-fx-effect: dropshadow(gaussian, #9370db, 2, 0.3, 0, 0);");
        
        
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
        
        
        Button closeButton = UIUtils.createModernButton("Close", "#e74c3c");
        closeButton.setOnAction(e -> openInventory());
        
        
        detailsPane.setTop(titleLabel);
        detailsPane.setCenter(detailsBox);
        detailsPane.setBottom(closeButton);
        BorderPane.setAlignment(closeButton, Pos.CENTER);
        BorderPane.setMargin(titleLabel, new Insets(0, 0, 10, 0));
        BorderPane.setMargin(closeButton, new Insets(10, 0, 0, 0));
        
        
        parent.getGameArea().getChildren().clear();
        parent.getGameArea().getChildren().add(detailsPane);
    }
} 

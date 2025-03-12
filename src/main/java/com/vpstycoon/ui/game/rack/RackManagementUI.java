package com.vpstycoon.ui.game.rack;

import com.vpstycoon.game.vps.VPSOptimization;
import com.vpstycoon.ui.game.GameplayContentPane;
import com.vpstycoon.ui.game.utils.UIUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class RackManagementUI {
    private final GameplayContentPane parent;
    private final List<Pane> slotPanes = new ArrayList<>();
    private static final int MAX_SLOTS = 10;

    public RackManagementUI(GameplayContentPane parent) {
        this.parent = parent;
    }

    public synchronized void openRackInfo() {
        // สร้างหน้าหลักสำหรับ Rack Management
        BorderPane rackPane = new BorderPane();
        rackPane.setPrefSize(800, 600);
        rackPane.setStyle("-fx-background-color: linear-gradient(to bottom, #2E3B4E, #1A252F); -fx-padding: 20px;");

        // Hide the menu bars
        parent.getMenuBar().setVisible(false);
        parent.getInGameMarketMenuBar().setVisible(false);
        parent.getMoneyUI().setVisible(false);
        
        // ส่วนหัว (Top Bar)
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #37474F; -fx-padding: 10px; -fx-background-radius: 10px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);");
        Label titleLabel = new Label("Rack Management");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");
        Button closeButton = UIUtils.createModernButton("Close", "#F44336");

        closeButton.setOnAction(e -> parent.returnToRoom());

        topBar.getChildren().addAll(titleLabel, closeButton);

        // ส่วนเนื้อหาหลัก
        HBox contentBox = UIUtils.createCard();
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(15));

        // ส่วนแสดง Rack Slots
        GridPane rackBox = new GridPane();
        rackBox.setPrefSize(150, 400);
        rackBox.setPadding(new Insets(5));
        rackBox.setStyle("-fx-background-color: #37474F; -fx-border-color: white; -fx-border-width: 2px; -fx-background-radius: 8px;");
        rackBox.setAlignment(Pos.TOP_CENTER);

        GridPane rackSlots = new GridPane();
        rackSlots.setAlignment(Pos.CENTER);
        rackSlots.setPadding(new Insets(5));
        rackSlots.setHgap(5);
        rackSlots.setVgap(5);

        ColumnConstraints column = new ColumnConstraints();
        column.setPercentWidth(100);
        rackSlots.getColumnConstraints().add(column);

        for (int i = 0; i < MAX_SLOTS; i++) {
            RowConstraints row = new RowConstraints();
            row.setPrefHeight(400.0 / MAX_SLOTS);
            row.setVgrow(Priority.ALWAYS);
            rackSlots.getRowConstraints().add(row);
        }

        // Update parent's total slots
        parent.setTotalSlots(MAX_SLOTS);

        // Create and display rack slots
        createRackSlots(rackSlots);
        rackBox.getChildren().add(rackSlots);

        // ส่วนข้อมูล Rack
        VBox infoPane = new VBox(10);
        infoPane.setAlignment(Pos.CENTER);
        Label infoTitle = new Label("Rack Status");

        infoTitle.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        
        // Calculate used and available slots
        int usedSlots = parent.getOccupiedSlots();
        int availableSlots = parent.getTotalSlots() - usedSlots;
        
        Label serverCount = new Label("VPS: " + parent.getVpsList().size());
        serverCount.setStyle("-fx-text-fill: #B0BEC5; -fx-font-size: 16px;");
        
        Label slotCount = new Label("Slots: " + usedSlots + "/" + parent.getTotalSlots() + " (" + availableSlots + " available)");
        slotCount.setStyle("-fx-text-fill: #B0BEC5; -fx-font-size: 16px;");

        Label networkUsage = new Label("Network: 10 Gbps");
        networkUsage.setStyle("-fx-text-fill: #B0BEC5; -fx-font-size: 16px;");
        
        Label userCount = new Label("Active Users: 10");
        userCount.setStyle("-fx-text-fill: #B0BEC5; -fx-font-size: 16px;");
        
        // Add inventory button
        Button inventoryButton = UIUtils.createModernButton("VPS Inventory", "#3498db");
        inventoryButton.setOnAction(e -> parent.openVPSInventory());
        
        // Add upgrade button
        Button upgradeButton = UIUtils.createModernButton("Upgrade Rack", "#4CAF50");
        upgradeButton.setOnAction(e -> {
            if (parent.getOccupiedSlots() < MAX_SLOTS) {
                parent.setOccupiedSlots(parent.getOccupiedSlots() + 1);
                System.out.println("Rack upgraded to " + parent.getOccupiedSlots() + " slots");
                openRackInfo(); // รีเฟรชหน้า
            } else {
                System.out.println("Max slots reached, cannot upgrade.");
            }
        });

        infoPane.getChildren().addAll(infoTitle, serverCount, slotCount, networkUsage, userCount, inventoryButton, upgradeButton);

        contentBox.getChildren().addAll(rackBox, infoPane);

        rackPane.setTop(topBar);
        rackPane.setCenter(contentBox);

        parent.getGameArea().getChildren().clear();
        parent.getGameArea().getChildren().add(rackPane);
    }

    /**
     * Create and display rack slots with installed VPS servers
     * @param rackSlots The GridPane to add slots to
     */
    private void createRackSlots(GridPane rackSlots) {
        slotPanes.clear();
        
        // Track current slot position
        int currentSlot = 0;
        
        // Create slots for installed VPS servers
        for (VPSOptimization vps : parent.getVpsList()) {
            int slotsRequired = vps.getSlotsRequired();
            
            // Create a slot that spans multiple rows based on VPS size
            Pane slot = createRackSlot(currentSlot, vps, true);
            rackSlots.add(slot, 0, currentSlot, 1, slotsRequired);
            slotPanes.add(slot);
            
            // Update current slot position
            currentSlot += slotsRequired;
        }
        
        // Fill remaining slots
        for (int i = currentSlot; i < MAX_SLOTS; i++) {
            Pane slot = createRackSlot(i, null, i < parent.getOccupiedSlots());
            rackSlots.add(slot, 0, i);
            slotPanes.add(slot);
        }
    }

    private Pane createRackSlot(int index, VPSOptimization vps, boolean isSlotAvailable) {
        Pane slot = new Pane();
        
        // Adjust height based on VPS size
        int slotHeight = 25;
        if (vps != null) {
            slotHeight = vps.getSlotsRequired() * 25;
        }
        
        slot.setPrefSize(100, slotHeight);
        Rectangle rect = new Rectangle(100, slotHeight);
        rect.setFill(vps != null ? Color.web("#42A5F5") : (isSlotAvailable ? Color.LIGHTGRAY : Color.DARKGRAY));
        rect.setStroke(Color.WHITE);
        rect.setArcHeight(5);
        rect.setArcWidth(5);
        rect.setEffect(new DropShadow(5, Color.BLACK));

        // Add label for VPS size if applicable
        if (vps != null) {
            Label sizeLabel = new Label(vps.getSize().getDisplayName());
            sizeLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            sizeLabel.setLayoutX(5);
            sizeLabel.setLayoutY(5);
            slot.getChildren().add(sizeLabel);
        }

        // เอฟเฟกต์เมื่อเมาส์ hover
        rect.setOnMouseEntered(e -> {
            if (vps != null) rect.setFill(Color.web("#90CAF9"));
        });
        rect.setOnMouseExited(e -> {
            if (vps != null) rect.setFill(Color.web("#42A5F5"));
        });

        // การคลิกสล็อต
        if (vps != null) {
            slot.setOnMouseClicked(e -> parent.openVPSInfoPage(vps));
        } else if (isSlotAvailable) {
            slot.setOnMouseClicked(e -> parent.openVPSInventory());
        } else {
            slot.setOnMouseClicked(e -> System.out.println("Slot " + (index + 1) + " clicked - Not available yet"));
        }

        slot.getChildren().add(rect);
        return slot;
    }
}
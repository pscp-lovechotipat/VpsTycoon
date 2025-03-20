package com.vpstycoon.ui.game.rack;

import com.vpstycoon.game.vps.VPSOptimization;
import com.vpstycoon.ui.game.GameplayContentPane;
import com.vpstycoon.ui.game.utils.UIUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class RackManagementUI {
    private final GameplayContentPane parent;
    private final List<Pane> slotPanes = new ArrayList<>();
    private final int MAX_SLOTS = 10;

    public RackManagementUI(GameplayContentPane parent) {
        this.parent = parent;
    }

    public synchronized void openRackInfo() {
        BorderPane rackPane = new BorderPane();
        rackPane.setPrefSize(800, 600);
        rackPane.getStyleClass().add("rack-pane");

        // ซ่อนเมนูอื่น ๆ
        parent.getMenuBar().setVisible(false);
        parent.getInGameMarketMenuBar().setVisible(false);
        parent.getMoneyUI().setVisible(false);
        parent.getDateView().setVisible(false);

        // Top Bar
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("top-bar");

        Label titleLabel = new Label("Rack Management");
        titleLabel.getStyleClass().add("title-label");

        Button closeButton = UIUtils.createModernButton("Close", "#F44336");
        closeButton.getStyleClass().add("close-button");
        closeButton.setOnAction(e -> parent.returnToRoom());

        topBar.getChildren().addAll(titleLabel, closeButton);

        // Content Box
        HBox contentBox = UIUtils.createCard();
        contentBox.getStyleClass().add("content-box");
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(15));

        // Rack Box
        GridPane rackBox = new GridPane();
        rackBox.setPrefSize(150, 400);
        rackBox.setPadding(new Insets(5));
        rackBox.getStyleClass().add("rack-box");
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

//        parent.setTotalSlots(MAX_SLOTS);
        createRackSlots(rackSlots);
        rackBox.getChildren().add(rackSlots);

        // Info Pane
        VBox infoPane = new VBox(10);
        infoPane.getStyleClass().add("info-pane");

        Label infoTitle = new Label("Rack Status");
        infoTitle.getStyleClass().add("info-title");

        int usedSlots = parent.getRack().getOccupiedSlotUnits();
        int availableSlots = parent.getRack().getMaxSlotUnits() - usedSlots;

        Label serverCount = new Label("VPS: " + parent.getVpsList().size());
        serverCount.getStyleClass().add("info-label");

        Label slotCount = new Label("Slots: " + usedSlots + "/" + parent.getRack().getMaxSlotUnits() + " (" + parent.getRack().getAvailableSlotUnits() + " available)");
        slotCount.getStyleClass().add("info-label");

        Label networkUsage = new Label("Network: 10 Gbps");
        networkUsage.getStyleClass().add("info-label");

        Label userCount = new Label("Active Users: 10");
        userCount.getStyleClass().add("info-label");

        Button inventoryButton = UIUtils.createModernButton("VPS Inventory", "#3498db");
        inventoryButton.getStyleClass().add("inventory-button");
        inventoryButton.setOnAction(e -> parent.openVPSInventory());

        Button upgradeButton = UIUtils.createModernButton("Upgrade Rack", "#4CAF50");
        upgradeButton.getStyleClass().add("upgrade-button");
        upgradeButton.setOnAction(e -> {
            if (parent.getRack().upgrade()) {
                parent.pushNotification("Rack upgraded", "Rack upgraded to " + parent.getRack().getUnlockedSlotUnits() + " slots");
                System.out.println("Rack upgraded to " + parent.getRack().getUnlockedSlotUnits() + " slots");
                openRackInfo();
            } else {
                parent.pushNotification("Rack upgraded", "Max slots reached, cannot upgrade.");
                System.out.println("Max slots reached, cannot upgrade.");
            }
        });

        infoPane.getChildren().addAll(infoTitle, serverCount, slotCount, networkUsage, userCount, inventoryButton, upgradeButton);

        contentBox.getChildren().addAll(rackBox, infoPane);

        rackPane.setTop(topBar);
        rackPane.setCenter(contentBox);
        BorderPane.setMargin(contentBox, new Insets(10, 0, 10, 0));

        parent.getGameArea().getChildren().clear();
        parent.getGameArea().getChildren().add(rackPane);

        // โหลด CSS
        rackPane.getStylesheets().add(getClass().getResource("/css/rackinfo-pane.css").toExternalForm());
    }

    private void createRackSlots(GridPane rackSlots) {
        slotPanes.clear();
        int currentSlot = 0;

        // Create slots for installed VPS servers
        for (VPSOptimization vps : parent.getVpsList()) {
            int slotsRequired = vps.getSlotsRequired();
            Pane slot = createRackSlot(currentSlot, vps, true);
            rackSlots.add(slot, 0, currentSlot, 1, slotsRequired);
            slotPanes.add(slot);
            currentSlot += slotsRequired;
        }

        // Fill remaining slots
        for (int i = currentSlot; i < parent.getRack().getMaxSlotUnits(); i++) {
            Pane slot = createRackSlot(i, null, i < parent.getRack().getUnlockedSlotUnits());
            rackSlots.add(slot, 0, i);
            slotPanes.add(slot);
        }
    }

    private Pane createRackSlot(int index, VPSOptimization vps, boolean isSlotAvailable) {
        Pane slot = new Pane();
        int slotHeight = 25;
        if (vps != null) {
            slotHeight = vps.getSlotsRequired() * 25;
        }
        slot.setPrefSize(100, slotHeight);

        Rectangle rect = new Rectangle(100, slotHeight);
        if (vps != null) {
            rect.getStyleClass().add("vps-slot");
        } else if (isSlotAvailable) {
            rect.getStyleClass().add("available-slot");
        } else {
            rect.getStyleClass().add("not-available-slot");
        }

        if (vps != null) {
            Label sizeLabel = new Label(vps.getSize().getDisplayName());
            sizeLabel.getStyleClass().add("size-label");
            sizeLabel.setLayoutX(5);
            sizeLabel.setLayoutY(5);
            slot.getChildren().add(sizeLabel);
        }

        rect.setOnMouseEntered(e -> {
            if (vps != null) rect.setFill(Color.web("#66FFFF"));
        });
        rect.setOnMouseExited(e -> {
            if (vps != null) rect.setFill(Color.web("#00FFFF"));
        });

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

    public int getMAX_SLOTS() {
        return parent.getRack().getMaxSlotUnits();
    }
}
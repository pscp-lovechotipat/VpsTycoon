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

        slotPanes.clear();
        for (int i = 0; i < MAX_SLOTS; i++) {
            VPSOptimization vps = (i < parent.getVpsList().size()) ? parent.getVpsList().get(i) : null;
            Pane slot = createRackSlot(i, vps, i < parent.getOccupiedSlots());
            slotPanes.add(slot);
            rackSlots.add(slot, 0, i);
        }
        rackBox.getChildren().add(rackSlots);

        // ส่วนข้อมูล Rack
        VBox infoPane = new VBox(10);
        infoPane.setAlignment(Pos.CENTER);
        Label infoTitle = new Label("Rack Status");

        infoTitle.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        Label serverCount = new Label("VPS: " + parent.getVpsList().size() + "/" + parent.getOccupiedSlots());

        serverCount.setStyle("-fx-text-fill: #B0BEC5; -fx-font-size: 16px;");
        Label networkUsage = new Label("Network: 10 Gbps");

        networkUsage.setStyle("-fx-text-fill: #B0BEC5; -fx-font-size: 16px;");
        Label userCount = new Label("Active Users: 10");

        userCount.setStyle("-fx-text-fill: #B0BEC5; -fx-font-size: 16px;");
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
        infoPane.getChildren().addAll(infoTitle, serverCount, networkUsage, userCount, upgradeButton);

        contentBox.getChildren().addAll(rackBox, infoPane);
        rackPane.setCenter(contentBox);

        // ส่วนปุ่มด้านล่าง
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10));
        Button openMarketButton = UIUtils.createModernButton("Open Market", "#FF9800");

        openMarketButton.setOnAction(e -> parent.openMarket());

        buttonBox.getChildren().addAll(openMarketButton);
        rackPane.setBottom(buttonBox);

        rackPane.setTop(topBar);

        // แสดงผลใน gameArea
        parent.getGameArea().getChildren().clear();
        parent.getGameArea().getChildren().add(rackPane);
        parent.getMenuBar().setVisible(false);
        parent.getInGameMarketMenuBar().setVisible(false);
    }

    private Pane createRackSlot(int index, VPSOptimization vps, boolean isSlotAvailable) {
        Pane slot = new Pane();
        slot.setPrefSize(100, 25);
        Rectangle rect = new Rectangle(100, 25);
        rect.setFill(vps != null ? Color.web("#42A5F5") : (isSlotAvailable ? Color.LIGHTGRAY : Color.DARKGRAY));
        rect.setStroke(Color.WHITE);
        rect.setArcHeight(5);
        rect.setArcWidth(5);
        rect.setEffect(new DropShadow(5, Color.BLACK));

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
            slot.setOnMouseClicked(e -> parent.openCreateVPSPage());
        } else {
            slot.setOnMouseClicked(e -> System.out.println("Slot " + (index + 1) + " clicked - Not available yet"));
        }

        slot.getChildren().add(rect);
        return slot;
    }
}
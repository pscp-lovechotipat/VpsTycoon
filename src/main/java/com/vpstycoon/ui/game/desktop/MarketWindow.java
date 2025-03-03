package com.vpstycoon.ui.game.desktop;

import com.vpstycoon.game.manager.VPSManager;
import com.vpstycoon.ui.game.GameplayContentPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.util.ArrayList;

public class MarketWindow extends VBox {
    private final Runnable onClose;
    private final VPSManager vpsManager; // เพิ่ม VPSManager
    private final GameplayContentPane gameplayContentPane; // เพิ่ม GameplayContentPane

    public MarketWindow(Runnable onClose, VPSManager vpsManager, GameplayContentPane gameplayContentPane) {
        this.onClose = onClose;
        this.vpsManager = vpsManager;
        this.gameplayContentPane = gameplayContentPane;

        setupUI();
        styleWindow();
    }

    private VBox createProductList() {
        VBox productList = new VBox(10);
        productList.setPadding(new Insets(10));

        String[][] products = {
                {"Basic VPS", "1 CPU, 1GB RAM, 20GB SSD", "$5/month"},
                {"Standard VPS", "2 CPU, 4GB RAM, 50GB SSD", "$10/month"},
                {"Premium VPS", "4 CPU, 8GB RAM, 100GB SSD", "$20/month"},
                {"Enterprise VPS", "8 CPU, 16GB RAM, 200GB SSD", "$50/month"}
        };

        for (String[] product : products) {
            productList.getChildren().add(createProductCard(product[0], product[1], product[2]));
        }

        return productList;
    }

    private HBox createProductCard(String name, String description, String price) {
        HBox card = new HBox(10);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: #f8f8f8; -fx-border-color: #ddd; -fx-border-radius: 5;");
        card.setAlignment(Pos.CENTER_LEFT);

        VBox details = new VBox();
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-text-fill: gray;");

        Label priceLabel = new Label(price);
        priceLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");

        details.getChildren().addAll(nameLabel, descLabel, priceLabel);

        Button buyButton = new Button("Buy");
        buyButton.setOnAction(e -> {
            // สร้าง VPS ใหม่เมื่อกดซื้อ
            GameplayContentPane.VPS newVPS = new GameplayContentPane.VPS(
                    "103.216.158." + (gameplayContentPane.getVpsList().size() + 235), // IP ตัวอย่าง
                    name,
                    new ArrayList<>() // เริ่มต้นด้วย VM ว่าง
            );
            gameplayContentPane.getVpsList().add(newVPS); // เพิ่ม VPS เข้าไปใน vpsList
            System.out.println("Purchased and added to rack: " + name);
            gameplayContentPane.openRackInfo(); // รีเฟรชหน้า Rack
            onClose.run(); // ปิด MarketWindow
        });

        card.getChildren().addAll(details, buyButton);
        HBox.setHgrow(details, Priority.ALWAYS);

        return card;
    }

    private void setupUI() {
        setPrefSize(600, 400);

        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_RIGHT);
        titleBar.setPadding(new Insets(5, 10, 5, 10));
        titleBar.setStyle("-fx-background-color: #ff8c00;");

        Button closeButton = new Button("X");
        closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
        closeButton.setOnAction(e -> onClose.run());

        titleBar.getChildren().add(closeButton);

        Label titleLabel = new Label("Market");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: black;");

        VBox productList = createProductList();

        ScrollPane scrollPane = new ScrollPane(productList);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);

        VBox content = new VBox(10.0, titleLabel, scrollPane);
        content.setPadding(new Insets(20));
        VBox.setVgrow(content, Priority.ALWAYS);

        getChildren().addAll(titleBar, content);
    }

    private void styleWindow() {
        setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0);");
    }
}
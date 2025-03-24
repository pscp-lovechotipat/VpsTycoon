package com.vpstycoon.ui.game.market;

import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.ui.game.GameplayContentPane;
import com.vpstycoon.ui.game.desktop.MarketWindow;
import javafx.scene.layout.BorderPane;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.control.ScrollPane;
import javafx.geometry.Pos;
import javafx.scene.control.Button;

import com.vpstycoon.game.vps.enums.VPSProduct;


public class MarketUI {
    private final GameplayContentPane parent;

    public MarketUI(GameplayContentPane parent) {
        this.parent = parent;
    }

    public void openMarket() {
        // Create main container
        BorderPane marketPane = new BorderPane();
        marketPane.setPrefSize(800, 600);
        marketPane.setStyle("-fx-background-color: linear-gradient(to bottom, #2E3B4E, #1A252F); -fx-padding: 20px;");
        
        // Hide the menu bars
        parent.getMenuBar().setVisible(false);
        parent.getInGameMarketMenuBar().setVisible(false);

        VBox productList = new VBox(20);
        productList.setPadding(new Insets(20));
        productList.setStyle("-fx-background-color: rgba(30, 15, 40, 0.8);");

        for (VPSProduct product : VPSProduct.values()) {
            if ( ResourceManager.getInstance().getSkillPointsSystem().canUnlockVPS(product)) {
                productList.getChildren().add(createProductCard(product)); // เพิ่มเฉพาะ VPS ที่สามารถซื้อได้
            }
        }

        ScrollPane scrollPane = new ScrollPane(productList);
        scrollPane.setFitToWidth(true);
        // เปลี่ยนจาก getPrefHeight() เป็น scrollPane.getPrefHeight()
        scrollPane.setPrefHeight(600);  // ตั้งความสูงให้ตรงกับขนาดที่ต้องการ
        scrollPane.setStyle(
                "-fx-background: rgb(20, 10, 30);" +
                        "-fx-background-color: rgb(20, 10, 30);" +
                        "-fx-control-inner-background: rgb(20, 10, 30);"
        );

        Label subtitleLabel = new Label("SELECT A VPS PACKAGE TO DEPLOY");
        subtitleLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 14));
        subtitleLabel.setTextFill(Color.LIGHTGRAY);
        subtitleLabel.setPadding(new Insets(10, 0, 10, 0));

        VBox content = new VBox(10.0, subtitleLabel, scrollPane);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: rgb(20, 10, 30);");
        VBox.setVgrow(content, Priority.ALWAYS);

        parent.getGameArea().getChildren().addAll(marketPane, content);  // ใช้ getChildren() บน parent.getGameArea()

        MarketWindow marketWindow = new MarketWindow(
                () -> {
                    parent.getGameArea().getChildren().removeIf(node -> node instanceof MarketWindow);
                    parent.openRackInfo();
                },
                () -> parent.getGameArea().getChildren().removeIf(node -> node instanceof MarketWindow),
                parent.getVpsManager(),
                parent
        );
        parent.getGameArea().getChildren().add(marketWindow);
        parent.getRootStack().getChildren().remove(parent.getGameArea());
        parent.getRootStack().getChildren().add(parent.getGameArea()); // นำ gameArea ไปไว้ด้านบนสุด
    }

    private BorderPane createProductCard(VPSProduct product) {
        BorderPane card = new BorderPane();
        card.setPadding(new Insets(15));
        card.setStyle("""
            -fx-background-color: rgba(40, 10, 60, 0.8);
            -fx-border-color: #6a00ff;
            -fx-border-width: 2px;
            -fx-border-radius: 5;
            -fx-background-radius: 5;
            -fx-effect: dropshadow(gaussian, rgba(106, 0, 255, 0.6), 10, 0, 0, 0);
        """);

        VBox details = new VBox(10);
        details.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("-fx-font-family: 'Courier New'; -fx-text-fill: #00ffff; -fx-font-size: 18px; -fx-font-weight: bold;");

        Label descriptionLabel = new Label(product.getDescription());
        descriptionLabel.setStyle("-fx-font-family: 'Courier New'; -fx-text-fill: white; -fx-font-size: 14px;");

        Label priceLabel = new Label("Price: " + product.getPriceDisplay());
        priceLabel.setStyle("-fx-font-family: 'Courier New'; -fx-text-fill: #ff00ff; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label keepUpLabel = new Label("Monthly: " + product.getKeepUpDisplay());
        keepUpLabel.setStyle("-fx-font-family: 'Courier New'; -fx-text-fill: #ff9900; -fx-font-size: 14px;");

        details.getChildren().addAll(nameLabel, descriptionLabel, priceLabel, keepUpLabel);

        Button buyButton = new Button("Purchase");
        buyButton.setStyle("""
            -fx-background-color: #ff00ff;
            -fx-font-family: 'Courier New'; 
            -fx-font-weight: bold;
            -fx-font-size: 20px;
            -fx-text-fill: white;
            -fx-padding: 8 15;
            -fx-background-radius: 5;
            -fx-effect: dropshadow(gaussian, rgba(255, 0, 255, 0.7), 10, 0, 0, 0);
        """);

        buyButton.setOnAction(e -> {
            // Add purchase logic here
            System.out.println("Purchase clicked for: " + product.getName());
        });

        VBox buttonContainer = new VBox();
        buttonContainer.setAlignment(Pos.CENTER_RIGHT);
        buttonContainer.getChildren().add(buyButton);

        card.setCenter(details);
        card.setRight(buttonContainer);

        return card;
    }


}
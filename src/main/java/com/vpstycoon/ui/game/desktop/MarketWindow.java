package com.vpstycoon.ui.game.desktop;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class MarketWindow extends VBox {
    private final Runnable onClose;

    public MarketWindow(Runnable onClose) {
        this.onClose = onClose;

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
        buyButton.setOnAction(e -> System.out.println("Purchased: " + name));

        card.getChildren().addAll(details, buyButton);
        HBox.setHgrow(details, Priority.ALWAYS);

        return card;
    }


    private void setupUI() {
        setPrefSize(600, 400);

        // Title bar
        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_RIGHT);
        titleBar.setPadding(new Insets(5, 10, 5, 10));
        titleBar.setStyle("-fx-background-color: #ff8c00;");

        Button closeButton = new Button("X");
        closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
        closeButton.setOnAction(e -> onClose.run());

        titleBar.getChildren().add(closeButton);

        // Market Title
        Label titleLabel = new Label("Market");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: black;");

        // Create Product List
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

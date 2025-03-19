package com.vpstycoon.ui.game.desktop;

import com.vpstycoon.audio.AudioManager;
import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.manager.VPSManager;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.game.vps.VPSOptimization;
import com.vpstycoon.game.vps.enums.VPSSize;
import com.vpstycoon.ui.game.GameplayContentPane;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public class MarketWindow extends VBox {
    private final Runnable onClose, onCloseAfterPurchase;
    private final VPSManager vpsManager;
    private final GameplayContentPane parent;

    private AudioManager audioManager;

    // Cyberpunk theme colors
    private static final Color CYBER_PURPLE = Color.rgb(200, 50, 255);
    private static final Color CYBER_DARK = Color.rgb(20, 10, 30);
    private static final Color CYBER_GLOW = Color.rgb(255, 0, 255, 0.7);
    private static final Color CYBER_BLUE = Color.rgb(0, 200, 255);
    private static final Color CYBER_PINK = Color.rgb(255, 0, 128);

    public MarketWindow(Runnable onClose, Runnable onCloseAfterPurchase, VPSManager vpsManager, GameplayContentPane gameplayContentPane) {
        this.onClose = onClose;
        this.onCloseAfterPurchase = onCloseAfterPurchase;
        this.vpsManager = vpsManager;
        this.parent = gameplayContentPane;
        this.audioManager = AudioManager.getInstance();

        setupUI();
        styleWindow();
    }

    private VBox createProductList() {
        VBox productList = new VBox(20);
        productList.setPadding(new Insets(20));
        productList.setStyle("-fx-background-color: rgba(30, 15, 40, 0.8);");

        // Updated product list with size information
        String[][] products = {
                {"Basic VPS (1U)", "1 CPU, 1GB RAM, 20GB SSD", "$5/month", "SIZE_1U"},
                {"Standard VPS (1U)", "2 CPU, 4GB RAM, 50GB SSD", "$10/month", "SIZE_1U"},
                {"Premium VPS (2U)", "4 CPU, 8GB RAM, 100GB SSD", "$20/month", "SIZE_2U"},
                {"Enterprise VPS (3U)", "8 CPU, 16GB RAM, 200GB SSD", "$50/month", "SIZE_3U"},
                {"Blade Server", "4 CPU, 8GB RAM, 100GB SSD", "$25/month", "BLADE"},
                {"Tower Server (4U)", "16 CPU, 32GB RAM, 500GB SSD", "$100/month", "TOWER"}
        };

        for (String[] product : products) {
            productList.getChildren().add(createProductCard(product[0], product[1], product[2], product[3]));
        }

        return productList;
    }

    private BorderPane createProductCard(String name, String description, String price, String sizeCode) {
        // Main card container with pixel art style
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

        // Product details
        VBox details = new VBox(10);
        details.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-family: 'Courier New'; -fx-text-fill: #00ffff; -fx-font-size: 18px; -fx-font-weight: bold;");

        Label descriptionLabel = new Label(description);
        descriptionLabel.setStyle("-fx-font-family: 'Courier New'; -fx-text-fill: white; -fx-font-size: 14px;");

        Label priceLabel = new Label(price);
        priceLabel.setStyle("-fx-font-family: 'Courier New'; -fx-text-fill: #ff00ff; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Size information
        VPSSize size = VPSSize.valueOf(sizeCode);
        Label sizeLabel = new Label("Size: " + size.getDisplayName() + " (" + size.getSlotsRequired() + " slots)");
        sizeLabel.setStyle("-fx-font-family: 'Courier New'; -fx-text-fill: #00ff99; -fx-font-size: 14px;");

        details.getChildren().addAll(nameLabel, descriptionLabel, priceLabel, sizeLabel);

        // Buy button with cyberpunk style
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
            // Parse specs from description
            audioManager.playSoundEffect("cash.mp3");
            String[] specs = description.split(", ");

            int vCPUs = Integer.parseInt(specs[0].split(" ")[0]);
            int ramInGB = Integer.parseInt(specs[1].split("GB")[0]);
            int diskInGB = Integer.parseInt(specs[2].split("GB")[0]);

            int keepUp = Integer.parseInt(price.split("\\$")[1].split("/")[0]);

            Company company = ResourceManager.getInstance().getCompany();

            if (company.getMoney() < keepUp) {
                System.out.println("no money to pay this vps");
                parent.pushNotification("Purchase Failed", "Not enough money to purchase this VPS.");
                return;
            }
            company.setMoney(company.getMoney() - keepUp);

            // Create new VPSOptimization instance
            VPSOptimization newVPS = new VPSOptimization();
            newVPS.setVCPUs(vCPUs);
            newVPS.setRamInGB(ramInGB);
            newVPS.setDiskInGB(diskInGB);
            
            // Set the VPS size
            newVPS.setSize(size);

            // Generate a unique ID (similar to your IP logic)
            String vpsId = "103.216.158." + (parent.getVpsInventory().getSize() + 235) + "-" + name.replace(" ", "");

            // Add to VPSManager and inventory
            vpsManager.createVPS(vpsId);
            vpsManager.getVPSMap().put(vpsId, newVPS);
            parent.getVpsInventory().addVPS(vpsId, newVPS);

            System.out.println("Purchased and added to inventory: " + name + " (ID: " + vpsId + ")");
            parent.pushNotification("VPS Purchased", 
                    "Successfully purchased " + name + ". It has been added to your inventory.");
            
            onCloseAfterPurchase.run();
        });

        // Create a VBox for the button to align it vertically
        VBox buttonContainer = new VBox();
        buttonContainer.setAlignment(Pos.CENTER_RIGHT);
        buttonContainer.getChildren().add(buyButton);

        // Add components to the card
        card.setCenter(details);
        card.setRight(buttonContainer);

        return card;
    }

    private void setupUI() {
        setPrefSize(700, 500);

        // Create a cyberpunk-style title bar
        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_RIGHT);
        titleBar.setPadding(new Insets(30, 15, 15, 15));
        
        // Gradient background for title bar
        LinearGradient titleGradient = new LinearGradient(
            0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(80, 20, 120)),
            new Stop(1, Color.rgb(40, 10, 60))
        );
        
        titleBar.setStyle(
            "-fx-background-color: rgb(40, 10, 60);" +
            "-fx-border-color: #ff00ff;" +
            "-fx-border-width: 0 0 2 0;"
        );

        // Create a prominent close button with X
        Button closeButton = new Button("X");
        closeButton.setFont(Font.font("Courier New", FontWeight.BOLD, 16));
        closeButton.setTextFill(Color.WHITE);
        closeButton.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-color: #ff00ff;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 0;" +
            "-fx-background-radius: 0;" +
            "-fx-min-width: 30px;" +
            "-fx-min-height: 30px;"
        );
        
        // Add hover effect to close button
        closeButton.setOnMouseEntered(e -> {
            closeButton.setStyle(
                "-fx-background-color: #ff00ff;" +
                "-fx-border-color: #ff50ff;" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 0;" +
                "-fx-background-radius: 0;" +
                "-fx-min-width: 30px;" +
                "-fx-min-height: 30px;"
            );
        });
        
        closeButton.setOnMouseExited(e -> {
            closeButton.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-border-color: #ff00ff;" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 0;" +
                "-fx-background-radius: 0;" +
                "-fx-min-width: 30px;" +
                "-fx-min-height: 30px;"
            );
        });
        
        closeButton.setOnAction(e -> onClose.run());

        // Create a large, prominent MARKET title
        Label titleLabel = new Label("VPS MARKET");
        titleLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setTextAlignment(TextAlignment.CENTER);
        
        // Add glow effect to title
        Glow titleGlow = new Glow(0.8);
        DropShadow titleShadow = new DropShadow();
        titleShadow.setColor(CYBER_GLOW);
        titleShadow.setRadius(15);
        titleShadow.setInput(titleGlow);
        titleLabel.setEffect(titleShadow);
        
        // Create pulsing animation for the title
        Timeline pulse = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(titleGlow.levelProperty(), 0.5)),
            new KeyFrame(Duration.seconds(1.5), new KeyValue(titleGlow.levelProperty(), 0.8)),
            new KeyFrame(Duration.seconds(3), new KeyValue(titleGlow.levelProperty(), 0.5))
        );
        pulse.setCycleCount(Timeline.INDEFINITE);
        pulse.play();

        // Add title to left side and close button to right side
        HBox titleContent = new HBox();
        titleContent.setAlignment(Pos.CENTER_LEFT);
        titleContent.getChildren().add(titleLabel);
        titleContent.setPadding(new Insets(0, 0, 0, 15));
        HBox.setHgrow(titleContent, Priority.ALWAYS);
        
        titleBar.getChildren().addAll(titleContent, closeButton);

        // Create product list with cyberpunk styling
        VBox productList = createProductList();

        // Create a styled scroll pane
        ScrollPane scrollPane = new ScrollPane(productList);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(getPrefHeight());
        scrollPane.setStyle(
            "-fx-background: rgb(20, 10, 30);" +
            "-fx-background-color: rgb(20, 10, 30);" +
            "-fx-control-inner-background: rgb(20, 10, 30);"
        );
        
        // Add a subtitle explaining the market
        Label subtitleLabel = new Label("SELECT A VPS PACKAGE TO DEPLOY");
        subtitleLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 14));
        subtitleLabel.setTextFill(Color.LIGHTGRAY);
        subtitleLabel.setPadding(new Insets(10, 0, 10, 0));

        // Main content container
        VBox content = new VBox(10.0, subtitleLabel, scrollPane);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: rgb(20, 10, 30);");
        VBox.setVgrow(content, Priority.ALWAYS);

        getChildren().addAll(titleBar, content);
    }

    private void styleWindow() {
        // Apply cyberpunk styling to the entire window
        setStyle(
            "-fx-background-color: rgb(20, 10, 30);" +
            "-fx-border-color: #ff00ff;" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 0;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(255, 0, 255, 0.7), 15, 0, 0, 0);"
        );
    }
}
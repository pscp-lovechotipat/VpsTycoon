package com.vpstycoon.ui.game.desktop;

import com.vpstycoon.audio.AudioManager;
import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.manager.VPSManager;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.game.vps.VPSOptimization;
import com.vpstycoon.game.vps.enums.VPSProduct;
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

        for (VPSProduct product : VPSProduct.values()) {
            productList.getChildren().add(createProductCard(product));
        }

        return productList;
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

        // แสดงราคาซื้อครั้งเดียว
        Label priceLabel = new Label("Price: " + product.getPriceDisplay());
        priceLabel.setStyle("-fx-font-family: 'Courier New'; -fx-text-fill: #ff00ff; -fx-font-size: 16px; -fx-font-weight: bold;");

        // แสดงค่าบำรุงต่อเดือน
        Label keepUpLabel = new Label("Monthly: " + product.getKeepUpDisplay());
        keepUpLabel.setStyle("-fx-font-family: 'Courier New'; -fx-text-fill: #ff9900; -fx-font-size: 14px;");

        VPSSize size = product.getSize();
        Label sizeLabel = new Label("Size: " + size.getDisplayName() + " (" + size.getSlotsRequired() + " slots)");
        sizeLabel.setStyle("-fx-font-family: 'Courier New'; -fx-text-fill: #00ff99; -fx-font-size: 14px;");

        details.getChildren().addAll(nameLabel, descriptionLabel, priceLabel, keepUpLabel, sizeLabel);

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
            // ตรวจสอบว่า Marketing Level เพียงพอหรือไม่ในการซื้อ VPS นี้
            if (!parent.getSkillPointsSystem().canUnlockVPS(product)) {
                parent.pushNotification("Purchase Failed", "Marketing level too low to unlock this VPS.");
                return;
            }

            audioManager.playSoundEffect("cash.mp3");

            Company company = ResourceManager.getInstance().getCompany();

            // ตรวจสอบเงินสำหรับราคาซื้อครั้งเดียว
            if (company.getMoney() < product.getPrice()) {
                System.out.println("no money to purchase this vps");
                parent.pushNotification("Purchase Failed", "Not enough money to purchase this VPS.");
                return;
            }
            company.setMoney(company.getMoney() - product.getPrice());

            VPSOptimization newVPS = new VPSOptimization();
            newVPS.setVCPUs(product.getCpu());
            newVPS.setRamInGB(product.getRam());
            newVPS.setDiskInGB(product.getStorage());
            newVPS.setSize(product.getSize());

            String vpsId = "103.216.158." + (parent.getVpsInventory().getSize() + 235) + "-" + product.getName().replace(" ", "");

            vpsManager.createVPS(vpsId);
            vpsManager.getVPSMap().put(vpsId, newVPS);
            parent.getVpsInventory().addVPS(vpsId, newVPS);

            System.out.println("Purchased and added to inventory: " + product.getName() + " (ID: " + vpsId + ")");
            parent.pushNotification("VPS Purchased",
                    "Successfully purchased " + product.getName() + ". It has been added to your inventory.");

            onCloseAfterPurchase.run();
        });

        VBox buttonContainer = new VBox();
        buttonContainer.setAlignment(Pos.CENTER_RIGHT);
        buttonContainer.getChildren().add(buyButton);

        card.setCenter(details);
        card.setRight(buttonContainer);

        return card;
    }

    private void setupUI() {
        setPrefSize(700, 500);

        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_RIGHT);
        titleBar.setPadding(new Insets(30, 15, 15, 15));

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

        Label titleLabel = new Label("SERVER MARKET");
        titleLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setTextAlignment(TextAlignment.CENTER);

        Glow titleGlow = new Glow(0.8);
        DropShadow titleShadow = new DropShadow();
        titleShadow.setColor(CYBER_GLOW);
        titleShadow.setRadius(15);
        titleShadow.setInput(titleGlow);
        titleLabel.setEffect(titleShadow);

        Timeline pulse = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(titleGlow.levelProperty(), 0.5)),
                new KeyFrame(Duration.seconds(1.5), new KeyValue(titleGlow.levelProperty(), 0.8)),
                new KeyFrame(Duration.seconds(3), new KeyValue(titleGlow.levelProperty(), 0.5))
        );
        pulse.setCycleCount(Timeline.INDEFINITE);
        pulse.play();

        HBox titleContent = new HBox();
        titleContent.setAlignment(Pos.CENTER_LEFT);
        titleContent.getChildren().add(titleLabel);
        titleContent.setPadding(new Insets(0, 0, 0, 15));
        HBox.setHgrow(titleContent, Priority.ALWAYS);

        titleBar.getChildren().addAll(titleContent, closeButton);

        VBox productList = createProductList();

        ScrollPane scrollPane = new ScrollPane(productList);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(getPrefHeight());
        scrollPane.setStyle(
                "-fx-background: rgb(20, 10, 30);" +
                        "-fx-background-color: rgb(20, 10, 30);" +
                        "-fx-control-inner-background: rgb(20, 10, 30);"
        );

        Label subtitleLabel = new Label("SELECT A SERVER PACKAGE TO DEPLOY");
        subtitleLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 14));
        subtitleLabel.setTextFill(Color.LIGHTGRAY);
        subtitleLabel.setPadding(new Insets(10, 0, 10, 0));

        VBox content = new VBox(10.0, subtitleLabel, scrollPane);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: rgb(20, 10, 30);");
        VBox.setVgrow(content, Priority.ALWAYS);

        getChildren().addAll(titleBar, content);
    }

    private void styleWindow() {
        setStyle(
                "-fx-background-color: rgb(20, 10, 30);" +
                        "-fx-border-color: #ff00ff;" +
                        "-fx-border-width: 2px;" +
                        "-fx-border-radius: 0;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(255, 0, 255, 0.7), 15, 0, 0, 0);"
        );
    }
}
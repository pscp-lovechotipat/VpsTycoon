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
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;

public class RackManagementUI extends VBox {
    private final GameplayContentPane parent;
    private final List<Pane> slotPanes = new ArrayList<>();
    private final int MAX_SLOTS = 10;
    private final Rack rack;
    private final Label rackInfoLabel;
    private final Label vpsListLabel;
    private final VBox vpsList;
    private final Button prevRackButton;
    private final Button nextRackButton;
    private final Button upgradeButton;
    private final Label upgradeCostLabel;
    private final Label upgradeInfoLabel;

    public RackManagementUI(GameplayContentPane parent) {
        this.parent = parent;
        this.rack = parent.getRack();
        setSpacing(10);
        setPadding(new Insets(10));
        setStyle("""
            -fx-background-color: #1a1a1a;
            -fx-padding: 10px;
            -fx-background-radius: 5px;
            -fx-border-color: #00ff00;
            -fx-border-width: 1px;
            -fx-border-style: solid;
            """);

        // Rack navigation
        HBox navigationBox = new HBox(10);
        navigationBox.setAlignment(Pos.CENTER);
        
        prevRackButton = new Button("← PREV RACK");
        nextRackButton = new Button("NEXT RACK →");
        
        String buttonStyle = """
            -fx-background-color: #1a1a1a;
            -fx-text-fill: #00ff00;
            -fx-font-weight: bold;
            -fx-padding: 10px;
            -fx-background-radius: 5px;
            -fx-border-color: #00ff00;
            -fx-border-width: 1px;
            -fx-border-style: solid;
            """;
        
        prevRackButton.setStyle(buttonStyle);
        nextRackButton.setStyle(buttonStyle);
        
        // Add hover effects
        prevRackButton.setOnMouseEntered(e -> 
            prevRackButton.setStyle(buttonStyle + "-fx-background-color: #00ff00; -fx-text-fill: #000000;")
        );
        prevRackButton.setOnMouseExited(e -> 
            prevRackButton.setStyle(buttonStyle)
        );
        
        nextRackButton.setOnMouseEntered(e -> 
            nextRackButton.setStyle(buttonStyle + "-fx-background-color: #00ff00; -fx-text-fill: #000000;")
        );
        nextRackButton.setOnMouseExited(e -> 
            nextRackButton.setStyle(buttonStyle)
        );

        navigationBox.getChildren().addAll(prevRackButton, nextRackButton);
        getChildren().add(navigationBox);

        // Rack info
        rackInfoLabel = new Label();
        rackInfoLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-weight: bold;");
        getChildren().add(rackInfoLabel);

        // VPS list
        vpsListLabel = new Label("INSTALLED VPS");
        vpsListLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-weight: bold;");
        getChildren().add(vpsListLabel);

        vpsList = new VBox(5);
        vpsList.setStyle("""
            -fx-background-color: #2a2a2a;
            -fx-padding: 10px;
            -fx-background-radius: 5px;
            -fx-border-color: #00ff00;
            -fx-border-width: 1px;
            -fx-border-style: solid;
            """);

        ScrollPane scrollPane = new ScrollPane(vpsList);
        scrollPane.setStyle("""
            -fx-background-color: transparent;
            -fx-background: transparent;
            -fx-background-insets: 0;
            -fx-padding: 0;
            """);
        scrollPane.setFitToWidth(true);
        getChildren().add(scrollPane);

        // Upgrade section
        upgradeButton = new Button("UPGRADE RACK");
        upgradeButton.setStyle(buttonStyle);
        upgradeButton.setOnMouseEntered(e -> 
            upgradeButton.setStyle(buttonStyle + "-fx-background-color: #00ff00; -fx-text-fill: #000000;")
        );
        upgradeButton.setOnMouseExited(e -> 
            upgradeButton.setStyle(buttonStyle)
        );
        getChildren().add(upgradeButton);

        upgradeCostLabel = new Label();
        upgradeCostLabel.setStyle("-fx-text-fill: #00ff00;");
        getChildren().add(upgradeCostLabel);

        upgradeInfoLabel = new Label();
        upgradeInfoLabel.setStyle("-fx-text-fill: #00ff00;");
        getChildren().add(upgradeInfoLabel);

        // Set up button actions
        prevRackButton.setOnAction(e -> {
            if (rack.prevRack()) {
                updateUI();
            }
        });

        nextRackButton.setOnAction(e -> {
            if (rack.nextRack()) {
                updateUI();
            }
        });

        upgradeButton.setOnAction(e -> {
            if (rack.upgrade()) {
                updateUI();
            }
        });

        // Initial UI update
        updateUI();
    }

    private void updateUI() {
        // Update rack info
        rackInfoLabel.setText(String.format("RACK %d/%d", rack.getRackIndex() + 1, rack.getMaxRacks()));
        
        // Update VPS list
        vpsList.getChildren().clear();
        List<VPSOptimization> installedVPS = rack.getInstalledVPS();
        
        if (installedVPS.isEmpty()) {
            Label emptyLabel = new Label("NO VPS INSTALLED");
            emptyLabel.setStyle("-fx-text-fill: #00ff00;");
            vpsList.getChildren().add(emptyLabel);
        } else {
            for (VPSOptimization vps : installedVPS) {
                VBox vpsBox = new VBox(5);
                vpsBox.setStyle("""
                    -fx-background-color: #1a1a1a;
                    -fx-padding: 10px;
                    -fx-background-radius: 5px;
                    -fx-border-color: #00ff00;
                    -fx-border-width: 1px;
                    -fx-border-style: solid;
                    """);
                
                Label nameLabel = new Label("VPS " + vps.getVCPUs() + "vCPU");
                nameLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-weight: bold;");
                
                Label specsLabel = new Label(vps.getRamInGB() + "GB RAM");
                specsLabel.setStyle("-fx-text-fill: #00ff00;");
                
                Label sizeLabel = new Label(vps.getSize().getDisplayName());
                sizeLabel.setStyle("-fx-text-fill: #00ff00;");
                
                vpsBox.getChildren().addAll(nameLabel, specsLabel, sizeLabel);
                vpsList.getChildren().add(vpsBox);
            }
        }
        
        // Update upgrade info
        int availableSlots = rack.getAvailableSlotUnits();
        int totalSlots = rack.getMaxSlotUnits();
        upgradeInfoLabel.setText(String.format("AVAILABLE SLOTS: %d/%d", availableSlots, totalSlots));
        
        // Update upgrade button state
        upgradeButton.setDisable(rack.getUnlockedSlotUnits() >= rack.getMaxSlotUnits());
        
        // Update upgrade cost
        int upgradeCost = calculateUpgradeCost();
        upgradeCostLabel.setText(String.format("UPGRADE COST: $%d", upgradeCost));
    }

    private int calculateUpgradeCost() {
        int currentUnlocked = rack.getUnlockedSlotUnits();
        return currentUnlocked * 1000; // Cost increases with each upgrade
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

        // Add navigation buttons
        Button prevRackButton = new Button("Prev Rack");
        prevRackButton.setStyle("""
            -fx-background-color: #6a00ff;
            -fx-font-family: 'Courier New';
            -fx-font-size: 14px;
            -fx-text-fill: white;
            -fx-padding: 5 10;
            -fx-background-radius: 3;
            -fx-effect: dropshadow(gaussian, rgba(106, 0, 255, 0.6), 5, 0, 0, 0);
        """);

        Button nextRackButton = new Button("Next Rack");
        nextRackButton.setStyle("""
            -fx-background-color: #6a00ff;
            -fx-font-family: 'Courier New';
            -fx-font-size: 14px;
            -fx-text-fill: white;
            -fx-padding: 5 10;
            -fx-background-radius: 3;
            -fx-effect: dropshadow(gaussian, rgba(106, 0, 255, 0.6), 5, 0, 0, 0);
        """);

        // Add rack index label
        Label rackIndexLabel = new Label("Rack " + (parent.getRack().getRackIndex() + 1));
        rackIndexLabel.setStyle("""
            -fx-font-family: 'Courier New';
            -fx-font-size: 16px;
            -fx-text-fill: #00ffff;
            -fx-font-weight: bold;
        """);

        prevRackButton.setOnAction(e -> {
            if (parent.getRack().prevRack()) {
                rackIndexLabel.setText("Rack " + (parent.getRack().getRackIndex() + 1));
                openRackInfo();
            } else {
                parent.pushNotification("Navigation", "You are at the first rack.");
            }
        });

        nextRackButton.setOnAction(e -> {
            if (parent.getRack().nextRack()) {
                rackIndexLabel.setText("Rack " + (parent.getRack().getRackIndex() + 1));
                openRackInfo();
            } else {
                parent.pushNotification("Navigation", "You are at the last rack.");
            }
        });

        HBox navigationBox = new HBox(10);
        navigationBox.setAlignment(Pos.CENTER_RIGHT);
        navigationBox.getChildren().addAll(prevRackButton, rackIndexLabel, nextRackButton);

        topBar.getChildren().addAll(titleLabel, navigationBox, closeButton);

        // Content Box
        HBox contentBox = UIUtils.createCard();
        contentBox.getStyleClass().add("content-box");
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(15));

        // Check if rack is purchased
        if (parent.getRack().getMaxSlotUnits() == 0) {
            VBox noRackMessage = new VBox(20);
            noRackMessage.setAlignment(Pos.CENTER);
            noRackMessage.setSpacing(20);

            Label messageLabel = new Label("No Rack Purchased");
            messageLabel.setStyle("""
                -fx-font-family: 'Courier New';
                -fx-font-size: 24px;
                -fx-text-fill: #ff00ff;
                -fx-font-weight: bold;
            """);

            Label descriptionLabel = new Label("Visit the Market to purchase your first rack!");
            descriptionLabel.setStyle("""
                -fx-font-family: 'Courier New';
                -fx-font-size: 16px;
                -fx-text-fill: white;
            """);

            Button marketButton = UIUtils.createModernButton("Go to Market", "#6a00ff");
            marketButton.setOnAction(e -> {
                parent.returnToRoom();
                parent.openMarket();
            });

            noRackMessage.getChildren().addAll(messageLabel, descriptionLabel, marketButton);
            contentBox.getChildren().add(noRackMessage);
        } else {
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

            for (int i = 0; i < parent.getRack().getMaxSlotUnits(); i++) {
                RowConstraints row = new RowConstraints();
                row.setPrefHeight(400.0 / parent.getRack().getMaxSlotUnits());
                row.setVgrow(Priority.ALWAYS);
                rackSlots.getRowConstraints().add(row);
            }

            createRackSlots(rackSlots);
            rackBox.getChildren().add(rackSlots);

            // Info Pane
            VBox infoPane = new VBox(10);
            infoPane.getStyleClass().add("info-pane");

            Label infoTitle = new Label("Rack Status");
            infoTitle.getStyleClass().add("info-title");

            int usedSlots = parent.getRack().getOccupiedSlotUnits();
            int availableSlots = parent.getRack().getMaxSlotUnits() - usedSlots;

            Label serverCount = new Label("Server: " + parent.getVpsList().size());
            serverCount.getStyleClass().add("info-label");

            Label slotCount = new Label("Slots: " + usedSlots + "/" + parent.getRack().getMaxSlotUnits() + " (" + parent.getRack().getAvailableSlotUnits() + " available)");
            slotCount.getStyleClass().add("info-label");

            Label networkUsage = new Label("Network: 10 Gbps");
            networkUsage.getStyleClass().add("info-label");

            Label userCount = new Label("Active Users: 10");
            userCount.getStyleClass().add("info-label");

            Button inventoryButton = UIUtils.createModernButton("Server Inventory", "#3498db");
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
        }

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
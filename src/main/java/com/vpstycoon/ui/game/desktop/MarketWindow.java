package com.vpstycoon.ui.game.desktop;

import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.company.SkillPointsSystem;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.game.vps.VPSOptimization;
import com.vpstycoon.game.vps.enums.RackProduct;
import com.vpstycoon.game.vps.enums.VPSProduct;
import com.vpstycoon.ui.game.GameplayContentPane;
import com.vpstycoon.game.manager.VPSManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.UUID;

public class MarketWindow extends BorderPane {
    private final GameplayContentPane parent;
    private final VPSManager vpsManager;
    private final Runnable onClose;
    private Label moneyDisplay;

    public MarketWindow(Runnable onClose, Runnable onClose2, VPSManager vpsManager, GameplayContentPane parent) {
        this.parent = parent;
        this.vpsManager = vpsManager;
        this.onClose = onClose;

        parent.getDateView().setVisible(false);
        parent.getMoneyUI().setVisible(false);
        parent.getMenuBar().setVisible(false);
        
        // Hide parent menus when opening the market window
        parent.hideMenus();
        
        // Main container setup with enhanced Cyberpunk theme
        setStyle("""
            -fx-background-color: #0a0a0a;
            -fx-border-color: #9a30fa;
            -fx-border-width: 2px;
            -fx-border-style: solid;
            """);
        setPadding(new Insets(20, 20, 20, 20));

        // Left menu with Cyberpunk theme
        VBox leftMenu = createLeftMenu();
        setLeft(leftMenu);

        // Content area with Cyberpunk theme
        ScrollPane contentArea = new ScrollPane();
        contentArea.setFitToWidth(true);
        contentArea.setStyle("""
            -fx-background: transparent;
            -fx-background-color: transparent;
            -fx-border-color: #9a30fa;
            -fx-border-width: 1px;
            -fx-border-style: solid;
            """);
        contentArea.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        contentArea.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // Main content
        VBox mainContent = new VBox(20);
        mainContent.setAlignment(Pos.TOP_CENTER);
        mainContent.setPadding(new Insets(20, 20, 20, 20));

        // VPS Products Section
        Label vpsTitle = new Label("SERVER PRODUCTS");
        vpsTitle.setStyle("""
            -fx-text-fill: #9a30fa;
            -fx-font-size: 28px;
            -fx-font-family: 'monospace';
            -fx-font-weight: bold;
            -fx-effect: dropshadow(gaussian, #9a30fa, 10, 0, 0, 0);
            """);
        mainContent.getChildren().add(vpsTitle);

        // Products grid
        GridPane productsGrid = new GridPane();
        productsGrid.setHgap(20);
        productsGrid.setVgap(20);
        productsGrid.setAlignment(Pos.CENTER);

        int column = 0;
        int row = 0;
        for (VPSProduct product : VPSProduct.values()) {
            VBox productCard = createVPSProductCard(product);
            productsGrid.add(productCard, column, row);
            column++;
            if (column > 2) {
                column = 0;
                row++;
            }
        }

        mainContent.getChildren().add(productsGrid);

        // Rack Products Section
        Label rackTitle = new Label("RACK PRODUCTS");
        rackTitle.setStyle("""
            -fx-text-fill: #9a30fa;
            -fx-font-size: 28px;
            -fx-font-family: 'monospace';
            -fx-font-weight: bold;
            -fx-effect: dropshadow(gaussian, #9a30fa, 10, 0, 0, 0);
            """);
        mainContent.getChildren().add(rackTitle);

        // Rack Products grid
        GridPane rackProductsGrid = new GridPane();
        rackProductsGrid.setHgap(20);
        rackProductsGrid.setVgap(20);
        rackProductsGrid.setAlignment(Pos.CENTER);

        column = 0;
        row = 0;
        for (RackProduct product : RackProduct.values()) {
            VBox productCard = createRackProductCard(product);
            rackProductsGrid.add(productCard, column, row);
            column++;
            if (column > 2) {
                column = 0;
                row++;
            }
        }

        mainContent.getChildren().add(rackProductsGrid);
        contentArea.setContent(mainContent);
        setCenter(contentArea);
    }

    private VBox createLeftMenu() {
        VBox menu = new VBox(10);
        menu.setStyle("""
            -fx-background-color: #1a1a1a;
            -fx-border-color: #9a30fa;
            -fx-border-width: 1px;
            -fx-border-style: solid;
            """);
        menu.setPadding(new Insets(25));
        menu.setPrefWidth(200);
        menu.setAlignment(Pos.TOP_CENTER);

        // Menu title
        Label menuTitle = new Label("MARKET MENU");
        menuTitle.setStyle("""
            -fx-text-fill: #9a30fa;
            -fx-font-size: 18px;
            -fx-font-family: 'monospace';
            -fx-font-weight: bold;
            -fx-effect: dropshadow(gaussian, #9a30fa, 5, 0, 0, 0);
            """);
        menu.getChildren().add(menuTitle);
        
        // Money display
        HBox moneyContainer = new HBox(5);
        moneyContainer.setAlignment(Pos.CENTER);
        moneyContainer.setPadding(new Insets(10, 0, 15, 0));
        
        Label moneyLabel = new Label("BALANCE:");
        moneyLabel.setStyle("""
            -fx-text-fill: #9a30fa;
            -fx-font-size: 14px;
            -fx-font-family: 'monospace';
            -fx-font-weight: bold;
            """);
            
        moneyDisplay = new Label("$" + parent.getCompany().getMoney());
        moneyDisplay.setStyle("""
            -fx-text-fill: #00ff00;
            -fx-font-size: 14px;
            -fx-font-family: 'monospace';
            -fx-font-weight: bold;
            -fx-effect: dropshadow(gaussian, #00ff00, 3, 0, 0, 0);
            """);
            
        moneyContainer.getChildren().addAll(moneyLabel, moneyDisplay);
        menu.getChildren().add(moneyContainer);

        // Menu items
        Button vpsButton = new Button("SERVERS PRODUCTS");
        vpsButton.setStyle("""
            -fx-background-color: #2a2a2a;
            -fx-text-fill: #9a30fa;
            -fx-font-size: 14px;
            -fx-font-family: 'monospace';
            -fx-padding: 10px;
            -fx-background-radius: 5px;
            -fx-border-color: #9a30fa;
            -fx-border-width: 1px;
            -fx-border-style: solid;
            -fx-min-width: 150px;
            """);
        vpsButton.setOnMouseEntered(e -> 
            vpsButton.setStyle("""
                -fx-background-color: #9a30fa;
                -fx-text-fill: #000000;
                -fx-font-size: 14px;
                -fx-font-family: 'monospace';
                -fx-padding: 10px;
                -fx-background-radius: 5px;
                -fx-border-color: #9a30fa;
                -fx-border-width: 1px;
                -fx-border-style: solid;
                -fx-min-width: 150px;
                """)
        );
        vpsButton.setOnMouseExited(e -> 
            vpsButton.setStyle("""
                -fx-background-color: #2a2a2a;
                -fx-text-fill: #9a30fa;
                -fx-font-size: 14px;
                -fx-font-family: 'monospace';
                -fx-padding: 10px;
                -fx-background-radius: 5px;
                -fx-border-color: #9a30fa;
                -fx-border-width: 1px;
                -fx-border-style: solid;
                -fx-min-width: 150px;
                """)
        );

        Button rackButton = new Button("RACK PRODUCTS");
        rackButton.setStyle("""
            -fx-background-color: #2a2a2a;
            -fx-text-fill: #9a30fa;
            -fx-font-size: 14px;
            -fx-font-family: 'monospace';
            -fx-padding: 10px;
            -fx-background-radius: 5px;
            -fx-border-color: #9a30fa;
            -fx-border-width: 1px;
            -fx-border-style: solid;
            -fx-min-width: 150px;
            """);
        rackButton.setOnMouseEntered(e -> 
            rackButton.setStyle("""
                -fx-background-color: #9a30fa;
                -fx-text-fill: #000000;
                -fx-font-size: 14px;
                -fx-font-family: 'monospace';
                -fx-padding: 10px;
                -fx-background-radius: 5px;
                -fx-border-color: #9a30fa;
                -fx-border-width: 1px;
                -fx-border-style: solid;
                -fx-min-width: 150px;
                """)
        );
        rackButton.setOnMouseExited(e -> 
            rackButton.setStyle("""
                -fx-background-color: #2a2a2a;
                -fx-text-fill: #9a30fa;
                -fx-font-size: 14px;
                -fx-font-family: 'monospace';
                -fx-padding: 10px;
                -fx-background-radius: 5px;
                -fx-border-color: #9a30fa;
                -fx-border-width: 1px;
                -fx-border-style: solid;
                -fx-min-width: 150px;
                """)
        );

        Button closeButton = new Button("CLOSE MARKET");
        closeButton.setStyle("""
            -fx-background-color: #2a2a2a;
            -fx-text-fill: #9a30fa;
            -fx-font-size: 14px;
            -fx-font-family: 'monospace';
            -fx-padding: 10px;
            -fx-background-radius: 5px;
            -fx-border-color: #9a30fa;
            -fx-border-width: 1px;
            -fx-border-style: solid;
            -fx-min-width: 150px;
            """);
        closeButton.setOnMouseEntered(e -> 
            closeButton.setStyle("""
                -fx-background-color: #9a30fa;
                -fx-text-fill: #000000;
                -fx-font-size: 14px;
                -fx-font-family: 'monospace';
                -fx-padding: 10px;
                -fx-background-radius: 5px;
                -fx-border-color: #9a30fa;
                -fx-border-width: 1px;
                -fx-border-style: solid;
                -fx-min-width: 150px;
                """)
        );
        closeButton.setOnMouseExited(e -> 
            closeButton.setStyle("""
                -fx-background-color: #2a2a2a;
                -fx-text-fill: #9a30fa;
                -fx-font-size: 14px;
                -fx-font-family: 'monospace';
                -fx-padding: 10px;
                -fx-background-radius: 5px;
                -fx-border-color: #9a30fa;
                -fx-border-width: 1px;
                -fx-border-style: solid;
                -fx-min-width: 150px;
                """)
        );
        closeButton.setOnAction(e -> onClose.run());

        menu.getChildren().addAll(vpsButton, rackButton, closeButton);
        return menu;
    }

    private VBox createVPSSection() {
        VBox vpsSection = new VBox(15);
        vpsSection.setAlignment(Pos.TOP_CENTER);
        vpsSection.setPadding(new Insets(20));
        vpsSection.setStyle("""
            -fx-background-color: #2a2a2a;
            -fx-background-radius: 10px;
            -fx-border-color: #9a30fa;
            -fx-border-width: 1px;
            -fx-border-style: solid;
            """);

        // Section title
        Label title = new Label("Server Products");
        title.setStyle("""
            -fx-text-fill: #9a30fa;
            -fx-font-size: 28px;
            -fx-font-family: 'monospace';
            -fx-font-weight: bold;
            -fx-effect: dropshadow(gaussian, #9a30fa, 5, 0, 0, 0);
            """);
        vpsSection.getChildren().add(title);

        // Products grid
        GridPane productsGrid = new GridPane();
        productsGrid.setHgap(20);
        productsGrid.setVgap(20);
        productsGrid.setAlignment(Pos.CENTER);

        int column = 0;
        int row = 0;
        for (VPSProduct product : VPSProduct.values()) {
            if (product.isUnlocked(ResourceManager.getInstance().getSkillPointsSystem().getAvailablePoints())) {
                VBox productCard = createVPSProductCard(product);
                productsGrid.add(productCard, column, row);
                column++;
                if (column > 2) {
                    column = 0;
                    row++;
                }
            }
        }

        vpsSection.getChildren().add(productsGrid);
        return vpsSection;
    }

    private VBox createRackSection() {
        VBox rackSection = new VBox(15);
        rackSection.setAlignment(Pos.TOP_CENTER);
        rackSection.setPadding(new Insets(20));
        rackSection.setStyle("""
            -fx-background-color: #2a2a2a;
            -fx-background-radius: 10px;
            -fx-border-color: #9a30fa;
            -fx-border-width: 1px;
            -fx-border-style: solid;
            """);

        // Section title
        Label title = new Label("Rack Products");
        title.setStyle("""
            -fx-text-fill: #9a30fa;
            -fx-font-size: 28px;
            -fx-font-family: 'monospace';
            -fx-font-weight: bold;
            -fx-effect: dropshadow(gaussian, #9a30fa, 5, 0, 0, 0);
            """);
        rackSection.getChildren().add(title);

        // Products grid
        GridPane productsGrid = new GridPane();
        productsGrid.setHgap(20);
        productsGrid.setVgap(20);
        productsGrid.setAlignment(Pos.CENTER);

        int column = 0;
        int row = 0;
        for (RackProduct product : RackProduct.values()) {
            VBox productCard = createRackProductCard(product);
            productsGrid.add(productCard, column, row);
            column++;
            if (column > 2) {
                column = 0;
                row++;
            }
        }

        rackSection.getChildren().add(productsGrid);
        return rackSection;
    }

    private VBox createVPSProductCard(VPSProduct product) {
        VBox card = new VBox(10);
        card.setStyle("""
            -fx-background-color: #1a1a1a;
            -fx-padding: 15px;
            -fx-background-radius: 5px;
            -fx-border-color: #9a30fa;
            -fx-border-width: 1px;
            -fx-border-style: solid;
            -fx-min-width: 250px;
            -fx-effect: dropshadow(gaussian, #9a30fa, 5, 0, 0, 0);
            """);

        // Product name
        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("""
            -fx-text-fill: #9a30fa;
            -fx-font-size: 18px;
            -fx-font-family: 'monospace';
            -fx-font-weight: bold;
            -fx-effect: dropshadow(gaussian, #9a30fa, 5, 0, 0, 0);
            """);

        // Product description
        Label descLabel = new Label(product.getDescription());
        descLabel.setStyle("""
            -fx-text-fill: #b19cd9;
            -fx-font-family: 'monospace';
            -fx-opacity: 0.9;
            """);
        descLabel.setWrapText(true);

        // Price
        Label priceLabel = new Label(product.getPriceDisplay());
        priceLabel.setStyle("""
            -fx-text-fill: #ff00ff;
            -fx-font-size: 16px;
            -fx-font-family: 'monospace';
            -fx-font-weight: bold;
            -fx-effect: dropshadow(gaussian, #ff00ff, 3, 0, 0, 0);
            """);

        // Buy button
        Button buyButton = new Button("BUY NOW");
        buyButton.setStyle("""
            -fx-background-color: #1a1a1a;
            -fx-text-fill: #9a30fa;
            -fx-font-family: 'monospace';
            -fx-font-weight: bold;
            -fx-padding: 8px 16px;
            -fx-background-radius: 5px;
            -fx-border-color: #9a30fa;
            -fx-border-width: 1px;
            -fx-border-style: solid;
            """);
        buyButton.setOnMouseEntered(e -> 
            buyButton.setStyle("""
                -fx-background-color: #9a30fa;
                -fx-text-fill: #000000;
                -fx-font-family: 'monospace';
                -fx-font-weight: bold;
                -fx-padding: 8px 16px;
                -fx-background-radius: 5px;
                -fx-border-color: #9a30fa;
                -fx-border-width: 1px;
                -fx-border-style: solid;
                """)
        );
        buyButton.setOnMouseExited(e -> 
            buyButton.setStyle("""
                -fx-background-color: #1a1a1a;
                -fx-text-fill: #9a30fa;
                -fx-font-family: 'monospace';
                -fx-font-weight: bold;
                -fx-padding: 8px 16px;
                -fx-background-radius: 5px;
                -fx-border-color: #9a30fa;
                -fx-border-width: 1px;
                -fx-border-style: solid;
                """)
        );

        buyButton.setOnAction(e -> {
            if (parent.getCompany().getMoney() < product.getPrice()) {
                parent.pushNotification("PURCHASE FAILED", "INSUFFICIENT FUNDS");
                return;
            }

            parent.getCompany().setMoney(parent.getCompany().getMoney() - product.getPrice());
            // Update money display after purchase
            updateMoneyDisplay();

            // สร้าง VPSOptimization instance จาก VPSProduct
            VPSOptimization vps = new VPSOptimization();
            vps.setName(UUID.randomUUID() +"-" + product.getName());
            vps.setVCPUs(product.getCpu());
            vps.setRamInGB(product.getRam());
            vps.setDiskInGB(product.getStorage());
            vps.setSize(product.getSize());

            // เพิ่ม VPS เข้าไปใน inventory
            parent.getVpsInventory().getInventoryMap().put(vps.getName(), vps);

            // แสดง notification
            parent.pushNotification("PURCHASE SUCCESSFUL",
                    "ACQUIRED " + product.getName() + " FOR " + product.getPriceDisplay());

            // ปิด market window และเปิด inventory
            onClose.run();
        });

        card.getChildren().addAll(nameLabel, descLabel, priceLabel, buyButton);
        return card;
    }

    private VBox createRackProductCard(RackProduct product) {
        VBox card = new VBox(10);
        card.setStyle("""
            -fx-background-color: #1a1a1a;
            -fx-padding: 15px;
            -fx-background-radius: 5px;
            -fx-border-color: #9a30fa;
            -fx-border-width: 1px;
            -fx-border-style: solid;
            -fx-min-width: 250px;
            -fx-effect: dropshadow(gaussian, #9a30fa, 5, 0, 0, 0);
            """);

        // Product name
        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("""
            -fx-text-fill: #9a30fa;
            -fx-font-size: 18px;
            -fx-font-family: 'monospace';
            -fx-font-weight: bold;
            -fx-effect: dropshadow(gaussian, #9a30fa, 5, 0, 0, 0);
            """);

        // Product description
        Label descLabel = new Label(product.getDescription());
        descLabel.setStyle("""
            -fx-text-fill: #b19cd9;
            -fx-font-family: 'monospace';
            -fx-opacity: 0.9;
            """);
        descLabel.setWrapText(true);

        // Price
        Label priceLabel = new Label(product.getPriceDisplay());
        priceLabel.setStyle("""
            -fx-text-fill: #ff00ff;
            -fx-font-size: 16px;
            -fx-font-family: 'monospace';
            -fx-font-weight: bold;
            -fx-effect: dropshadow(gaussian, #ff00ff, 3, 0, 0, 0);
            """);

        // Buy button
        Button buyButton = new Button("BUY NOW");
        buyButton.setStyle("""
            -fx-background-color: #1a1a1a;
            -fx-text-fill: #9a30fa;
            -fx-font-family: 'monospace';
            -fx-font-weight: bold;
            -fx-padding: 8px 16px;
            -fx-background-radius: 5px;
            -fx-border-color: #9a30fa;
            -fx-border-width: 1px;
            -fx-border-style: solid;
            """);
        buyButton.setOnMouseEntered(e -> 
            buyButton.setStyle("""
                -fx-background-color: #9a30fa;
                -fx-text-fill: #000000;
                -fx-font-family: 'monospace';
                -fx-font-weight: bold;
                -fx-padding: 8px 16px;
                -fx-background-radius: 5px;
                -fx-border-color: #9a30fa;
                -fx-border-width: 1px;
                -fx-border-style: solid;
                """)
        );
        buyButton.setOnMouseExited(e -> 
            buyButton.setStyle("""
                -fx-background-color: #1a1a1a;
                -fx-text-fill: #9a30fa;
                -fx-font-family: 'monospace';
                -fx-font-weight: bold;
                -fx-padding: 8px 16px;
                -fx-background-radius: 5px;
                -fx-border-color: #9a30fa;
                -fx-border-width: 1px;
                -fx-border-style: solid;
                """)
        );

        buyButton.setOnAction(e -> {
            if (parent.getCompany().getMoney() < product.getPrice()) {
                parent.pushNotification("PURCHASE FAILED", "INSUFFICIENT FUNDS");
                return;
            }

            parent.getCompany().setMoney(parent.getCompany().getMoney() - product.getPrice());
            // Update money display after purchase
            updateMoneyDisplay();
            
            // Add new rack to the list
            parent.getRack().addRack(product.getSlots());
            
            // Navigate to the newest rack (the one we just purchased)
            parent.getRack().goToLatestRack();

            parent.pushNotification("PURCHASE SUCCESSFUL", 
                "ACQUIRED " + product.getName() + ". NEW RACK INSTALLED WITH " + 
                product.getSlots() + " SLOTS.");

            // Close market window and open rack management
            onClose.run();
            parent.openRackInfo();
        });

        card.getChildren().addAll(nameLabel, descLabel, priceLabel, buyButton);
        return card;
    }
    
    // Helper method to update money display
    private void updateMoneyDisplay() {
        moneyDisplay.setText("$" + parent.getCompany().getMoney());
    }
}
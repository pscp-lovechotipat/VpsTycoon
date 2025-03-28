package com.vpstycoon.ui.game.desktop;

import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.company.SkillPointsSystem;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.game.vps.VPSOptimization;
import com.vpstycoon.game.vps.enums.RackProduct;
import com.vpstycoon.game.vps.enums.VPSProduct;
import com.vpstycoon.ui.game.GameplayContentPane;
import com.vpstycoon.game.manager.VPSManager;
import com.vpstycoon.game.GameManager;
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
    private VBox mainContent;
    private ScrollPane contentArea;
    private String currentFilter = "ALL"; // Default to show all products

    public MarketWindow(Runnable onClose, Runnable onClose2, VPSManager vpsManager, GameplayContentPane parent) {
        this.parent = parent;
        this.vpsManager = vpsManager;
        
        // เก็บสถานะการแสดงผลปัจจุบันของ UI elements
        final boolean dateViewWasVisible = parent.getDateView().isVisible();
        final boolean moneyUIWasVisible = parent.getMoneyUI().isVisible(); 
        final boolean menuBarWasVisible = parent.getMenuBar().isVisible();
        final boolean marketMenuBarWasVisible = parent.getInGameMarketMenuBar().isVisible();
        
        // บันทึก runnable ที่ใช้เมื่อปิด window และจะคืนค่า UI ตามสถานะเดิม
        this.onClose = () -> {
            // 1. ลบ MarketWindow ออกจาก gameArea
            parent.getGameArea().getChildren().removeIf(node -> node instanceof MarketWindow);
            
            // 2. คืนค่าสถานะการแสดงผลของ UI elements
            parent.getDateView().setVisible(dateViewWasVisible);
            parent.getMoneyUI().setVisible(moneyUIWasVisible);
            parent.getMenuBar().setVisible(menuBarWasVisible);
            parent.getInGameMarketMenuBar().setVisible(marketMenuBarWasVisible);
            
            // 3. ให้ UI ค่อยๆ ปรากฏด้วย fade-in
            javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(
                javafx.util.Duration.millis(300), parent.getGameArea());
            fadeIn.setFromValue(0.8);
            fadeIn.setToValue(1.0);
            fadeIn.play();
            
            // 4. เรียก callback เดิมถ้ามี
            if (onClose != null) {
                onClose.run();
            }
        };

        // ซ่อน parent menus เมื่อเปิด market window
        parent.hideMenus();
        
        // Main container setup with enhanced Cyberpunk theme
        setStyle("""
            -fx-background-color: #0a0a0a;
            -fx-border-color: #9a30fa;
            -fx-border-width: 2px;
            -fx-border-style: solid;
            """);
        
        // Set maximum size to use full available space
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        setPrefSize(1200, 800); // Set preferred size larger
        
        // Reduce padding to utilize more space
        setPadding(new Insets(10, 10, 10, 10));

        // Left menu with Cyberpunk theme
        VBox leftMenu = createLeftMenu();
        setLeft(leftMenu);

        // Content area with Cyberpunk theme
        contentArea = new ScrollPane();
        contentArea.setFitToWidth(true);
        contentArea.setFitToHeight(true); // Make content area fit height
        contentArea.setPrefViewportWidth(900); // Set preferred width larger
        contentArea.setStyle("""
            -fx-background: transparent;
            -fx-background-color: transparent;
            -fx-border-color: #9a30fa;
            -fx-border-width: 1px;
            -fx-border-style: solid;
            """);
        contentArea.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        contentArea.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // Create main content container
        mainContent = new VBox(20);
        mainContent.setAlignment(Pos.TOP_CENTER);
        mainContent.setPadding(new Insets(20, 20, 20, 20));
        mainContent.setMaxWidth(Double.MAX_VALUE); // Allow content to expand horizontally

        // Load all products initially
        updateProductDisplay();
        
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
        menu.setPrefWidth(250); // Increase menu width slightly
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

        // Create all buttons first so they can reference each other
        Button allButton = new Button("ALL PRODUCTS");
        Button vpsButton = new Button("SERVER PRODUCTS");
        Button rackButton = new Button("RACK PRODUCTS");
        Button closeButton = new Button("CLOSE MARKET");
        
        // Style and configure buttons
        styleButton(allButton);
        allButton.setOnAction(e -> {
            currentFilter = "ALL";
            updateProductDisplay();
            highlightActiveButton(allButton, vpsButton, rackButton);
        });

        styleButton(vpsButton);
        vpsButton.setOnAction(e -> {
            currentFilter = "SERVER";
            updateProductDisplay();
            highlightActiveButton(vpsButton, allButton, rackButton);
        });

        styleButton(rackButton);
        rackButton.setOnAction(e -> {
            currentFilter = "RACK";
            updateProductDisplay();
            highlightActiveButton(rackButton, allButton, vpsButton);
        });

        styleButton(closeButton);
        closeButton.setOnAction(e -> onClose.run());

        menu.getChildren().addAll(allButton, vpsButton, rackButton, closeButton);
        
        // Set the initial "ALL" button as active
        highlightActiveButton(allButton, vpsButton, rackButton);
        
        return menu;
    }
    
    private void styleButton(Button button) {
        button.setStyle("""
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
        button.setOnMouseEntered(e -> 
            button.setStyle("""
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
        button.setOnMouseExited(e -> {
            if (button.getUserData() == "active") {
                button.setStyle("""
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
                    """);
            } else {
                button.setStyle("""
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
            }
        });
    }
    
    private void highlightActiveButton(Button activeButton, Button... otherButtons) {
        activeButton.setUserData("active");
        activeButton.setStyle("""
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
            """);
            
        for (Button button : otherButtons) {
            button.setUserData("inactive");
            button.setStyle("""
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
        }
    }
    
    private void updateProductDisplay() {
        mainContent.getChildren().clear();
        
        if (currentFilter.equals("ALL") || currentFilter.equals("SERVER")) {
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
            productsGrid.setMaxWidth(Double.MAX_VALUE); // Allow grid to expand horizontally

            int column = 0;
            int row = 0;
            int columnCount = 3; // Display 3 items per row by default
            
            // Check window width and adjust column count if needed
            if (getWidth() > 1400) {
                columnCount = 4; // Display 4 items per row on wider screens
            }
            
            for (VPSProduct product : VPSProduct.values()) {
                VBox productCard = createVPSProductCard(product);
                productsGrid.add(productCard, column, row);
                column++;
                if (column >= columnCount) {
                    column = 0;
                    row++;
                }
            }

            mainContent.getChildren().add(productsGrid);
        }
        
        if (currentFilter.equals("ALL") || currentFilter.equals("RACK")) {
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
            rackProductsGrid.setMaxWidth(Double.MAX_VALUE); // Allow grid to expand horizontally

            int column = 0;
            int row = 0;
            int columnCount = 3; // Display 3 items per row by default
            
            // Check window width and adjust column count if needed
            if (getWidth() > 1400) {
                columnCount = 4; // Display 4 items per row on wider screens
            }
            
            for (RackProduct product : RackProduct.values()) {
                VBox productCard = createRackProductCard(product);
                rackProductsGrid.add(productCard, column, row);
                column++;
                if (column >= columnCount) {
                    column = 0;
                    row++;
                }
            }

            mainContent.getChildren().add(rackProductsGrid);
        }
        
        // Scroll to top
        contentArea.setVvalue(0);
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

        // Get marketing discount
        SkillPointsSystem skillPointsSystem = ResourceManager.getInstance().getSkillPointsSystem();
        int discountPercent = skillPointsSystem.getMarketDiscount();
        double discountMultiplier = 1.0 - (discountPercent / 100.0);
        double finalPrice = Math.round(product.getPrice() * discountMultiplier);

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
        String priceText = discountPercent > 0 
            ? "$" + finalPrice + " (" + product.getPriceDisplay() + " - " + discountPercent + "%)"
            : product.getPriceDisplay();
            
        Label priceLabel = new Label(priceText);
        priceLabel.setStyle("""
            -fx-text-fill: #ff00ff;
            -fx-font-size: 16px;
            -fx-font-family: 'monospace';
            -fx-font-weight: bold;
            -fx-effect: dropshadow(gaussian, #ff00ff, 3, 0, 0, 0);
            """);

        // Buy button
        String buyButtonText = discountPercent > 0 
            ? "BUY NOW - " + discountPercent + "%"
            : "BUY NOW";
            
        Button buyButton = new Button(buyButtonText);
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
            // Apply discount to final price
            long actualPrice = Math.round(finalPrice);
            
            if (parent.getCompany().getMoney() < actualPrice) {
                parent.pushNotification("PURCHASE FAILED", "INSUFFICIENT FUNDS");
                return;
            }

            parent.getCompany().setMoney(parent.getCompany().getMoney() - actualPrice);
            // Update money display after purchase
            updateMoneyDisplay();

            // สร้าง VPSOptimization instance จาก VPSProduct
            VPSOptimization vps = new VPSOptimization();
            // ใช้ ID ที่มีรูปแบบที่แน่นอนมากขึ้น ไม่ใช้ name เป็น ID
            String vpsId = "vps-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
            vps.setVpsId(vpsId);
            vps.setName(product.getName());
            vps.setVCPUs(product.getCpu());
            vps.setRamInGB(product.getRam());
            vps.setDiskInGB(product.getStorage());
            vps.setSize(product.getSize());
            vps.setInstalled(false); // ยืนยันว่าเป็น VPS ที่ยังไม่ติดตั้ง

            // เพิ่ม VPS เข้าไปใน inventory ของ GameplayContentPane
            parent.getVpsInventory().addVPS(vpsId, vps);
            System.out.println("เพิ่ม VPS เข้า GameplayContentPane inventory: " + vpsId);
            
            // อัปเดต GameManager ด้วย (เพื่อให้ข้อมูลถูกบันทึกไว้)
            GameManager.getInstance().getVpsInventory().addVPS(vpsId, vps);
            System.out.println("เพิ่ม VPS เข้า GameManager inventory: " + vpsId);
            
            // บันทึกสถานะเกมทันที เพื่อให้แน่ใจว่าข้อมูลจะไม่หาย
            GameManager.getInstance().saveState();
            System.out.println("บันทึกเกมหลังซื้อ VPS เรียบร้อย");

            // แสดง notification
            parent.pushNotification("PURCHASE SUCCESSFUL",
                    "ACQUIRED " + product.getName() + " FOR " + product.getPriceDisplay() + 
                    "\nVPS added to your inventory!");

            // ปิด market window (ซึ่งจะทำให้ menubar กลับมาแสดง)
            onClose.run();
            
            // ซ่อน MenuBar ก่อนเปิดหน้า inventory
            parent.getMenuBar().setVisible(false);
            parent.getInGameMarketMenuBar().setVisible(false);
            
            // เปิดหน้า inventory ทันทีหลังซื้อ เพื่อให้ผู้เล่นเห็นว่าได้ VPS มาแล้ว
            parent.openVPSInventory();
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

        // Get marketing discount
        SkillPointsSystem skillPointsSystem = ResourceManager.getInstance().getSkillPointsSystem();
        int discountPercent = skillPointsSystem.getMarketDiscount();
        double discountMultiplier = 1.0 - (discountPercent / 100.0);
        double finalPrice = Math.round(product.getPrice() * discountMultiplier);

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
        String priceText = discountPercent > 0 
            ? "$" + finalPrice + " (" + product.getPriceDisplay() + " - " + discountPercent + "%)"
            : product.getPriceDisplay();
            
        Label priceLabel = new Label(priceText);
        priceLabel.setStyle("""
            -fx-text-fill: #ff00ff;
            -fx-font-size: 16px;
            -fx-font-family: 'monospace';
            -fx-font-weight: bold;
            -fx-effect: dropshadow(gaussian, #ff00ff, 3, 0, 0, 0);
            """);

        // Buy button
        String buyButtonText = discountPercent > 0 
            ? "BUY NOW - " + discountPercent + "%"
            : "BUY NOW";
            
        Button buyButton = new Button(buyButtonText);
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
            // Apply discount to final price
            long actualPrice = Math.round(finalPrice);
            
            if (parent.getCompany().getMoney() < actualPrice) {
                parent.pushNotification("PURCHASE FAILED", "INSUFFICIENT FUNDS");
                return;
            }

            parent.getCompany().setMoney(parent.getCompany().getMoney() - actualPrice);
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
            
            // Hide menu bars before opening rack info
            parent.getMenuBar().setVisible(false);
            parent.getInGameMarketMenuBar().setVisible(false);
            
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
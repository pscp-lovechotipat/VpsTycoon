package com.vpstycoon.ui.game.desktop;

import com.vpstycoon.game.GameManager;
import com.vpstycoon.game.company.SkillPointsSystem;
import com.vpstycoon.game.manager.VPSManager;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.game.vps.VPSOptimization;
import com.vpstycoon.game.vps.enums.RackProduct;
import com.vpstycoon.game.vps.enums.VPSProduct;
import com.vpstycoon.ui.game.GameplayContentPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.UUID;

public class MarketWindow extends BorderPane {
    private final GameplayContentPane parent;
    private final VPSManager vpsManager;
    private final Runnable onClose;
    private Label moneyDisplay;
    private VBox mainContent;
    private ScrollPane contentArea;
    private String currentFilter = "ALL"; 

    public MarketWindow(Runnable onClose, Runnable onClose2, VPSManager vpsManager, GameplayContentPane parent) {
        this.parent = parent;
        this.vpsManager = vpsManager;
        
        
        final boolean dateViewWasVisible = parent.getDateView().isVisible();
        final boolean moneyUIWasVisible = parent.getMoneyUI().isVisible(); 
        final boolean menuBarWasVisible = parent.getMenuBar().isVisible();
        final boolean marketMenuBarWasVisible = parent.getInGameMarketMenuBar().isVisible();
        
        
        this.onClose = () -> {
            
            javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(
                javafx.util.Duration.millis(300), this);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(event -> {
                
                parent.getGameArea().getChildren().removeIf(node -> node instanceof MarketWindow);
                
                
                parent.getDateView().setVisible(dateViewWasVisible);
                parent.getMoneyUI().setVisible(moneyUIWasVisible);
                parent.getMenuBar().setVisible(menuBarWasVisible);
                parent.getInGameMarketMenuBar().setVisible(marketMenuBarWasVisible);
                
                
                javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(
                    javafx.util.Duration.millis(300), parent.getGameArea());
                fadeIn.setFromValue(0.8);
                fadeIn.setToValue(1.0);
                fadeIn.play();
                
                
                if (onClose2 != null) {
                    onClose2.run();
                }
            });
            fadeOut.play();
        };

        
        parent.hideMenus();
        
        
        setStyle("""
            -fx-background-color: #0a0a0a;
            -fx-border-color: #9a30fa;
            -fx-border-width: 2px;
            -fx-border-style: solid;
            """);
        
        
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        setPrefSize(1200, 800); 
        
        
        setViewOrder(-10);
        
        
        setPadding(new Insets(10, 10, 10, 10));

        
        VBox leftMenu = createLeftMenu();
        setLeft(leftMenu);

        
        contentArea = new ScrollPane();
        contentArea.setFitToWidth(true);
        contentArea.setFitToHeight(true); 
        contentArea.setPrefViewportWidth(900); 
        contentArea.setStyle("""
            -fx-background: transparent;
            -fx-background-color: transparent;
            -fx-border-color: #9a30fa;
            -fx-border-width: 1px;
            -fx-border-style: solid;
            """);
        contentArea.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        contentArea.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        
        mainContent = new VBox(20);
        mainContent.setAlignment(Pos.TOP_CENTER);
        mainContent.setPadding(new Insets(20, 20, 20, 20));
        mainContent.setMaxWidth(Double.MAX_VALUE); 

        
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
        menu.setPrefWidth(250); 
        menu.setAlignment(Pos.TOP_CENTER);

        
        Label menuTitle = new Label("MARKET MENU");
        menuTitle.setStyle("""
            -fx-text-fill: #9a30fa;
            -fx-font-size: 18px;
            -fx-font-family: 'monospace';
            -fx-font-weight: bold;
            -fx-effect: dropshadow(gaussian, #9a30fa, 5, 0, 0, 0);
            """);
        menu.getChildren().add(menuTitle);
        
        
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

        
        Button allButton = new Button("ALL PRODUCTS");
        Button vpsButton = new Button("SERVER PRODUCTS");
        Button rackButton = new Button("RACK PRODUCTS");
        Button closeButton = new Button("CLOSE MARKET");
        
        
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
            
            Label vpsTitle = new Label("SERVER PRODUCTS");
            vpsTitle.setStyle("""
                -fx-text-fill: #9a30fa;
                -fx-font-size: 28px;
                -fx-font-family: 'monospace';
                -fx-font-weight: bold;
                -fx-effect: dropshadow(gaussian, #9a30fa, 10, 0, 0, 0);
                """);
            mainContent.getChildren().add(vpsTitle);

            
            GridPane productsGrid = new GridPane();
            productsGrid.setHgap(20);
            productsGrid.setVgap(20);
            productsGrid.setAlignment(Pos.CENTER);
            productsGrid.setMaxWidth(Double.MAX_VALUE); 

            int column = 0;
            int row = 0;
            int columnCount = 3; 
            
            
            if (getWidth() > 1400) {
                columnCount = 4; 
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
            
            Label rackTitle = new Label("RACK PRODUCTS");
            rackTitle.setStyle("""
                -fx-text-fill: #9a30fa;
                -fx-font-size: 28px;
                -fx-font-family: 'monospace';
                -fx-font-weight: bold;
                -fx-effect: dropshadow(gaussian, #9a30fa, 10, 0, 0, 0);
                """);
            mainContent.getChildren().add(rackTitle);

            
            GridPane rackProductsGrid = new GridPane();
            rackProductsGrid.setHgap(20);
            rackProductsGrid.setVgap(20);
            rackProductsGrid.setAlignment(Pos.CENTER);
            rackProductsGrid.setMaxWidth(Double.MAX_VALUE); 

            int column = 0;
            int row = 0;
            int columnCount = 3; 
            
            
            if (getWidth() > 1400) {
                columnCount = 4; 
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

        
        Label title = new Label("Server Products");
        title.setStyle("""
            -fx-text-fill: #9a30fa;
            -fx-font-size: 28px;
            -fx-font-family: 'monospace';
            -fx-font-weight: bold;
            -fx-effect: dropshadow(gaussian, #9a30fa, 5, 0, 0, 0);
            """);
        vpsSection.getChildren().add(title);

        
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

        
        Label title = new Label("Rack Products");
        title.setStyle("""
            -fx-text-fill: #9a30fa;
            -fx-font-size: 28px;
            -fx-font-family: 'monospace';
            -fx-font-weight: bold;
            -fx-effect: dropshadow(gaussian, #9a30fa, 5, 0, 0, 0);
            """);
        rackSection.getChildren().add(title);

        
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

        
        SkillPointsSystem skillPointsSystem = ResourceManager.getInstance().getSkillPointsSystem();
        int discountPercent = skillPointsSystem.getMarketDiscount();
        double discountMultiplier = 1.0 - (discountPercent / 100.0);
        double finalPrice = Math.round(product.getPrice() * discountMultiplier);

        
        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("""
            -fx-text-fill: #9a30fa;
            -fx-font-size: 18px;
            -fx-font-family: 'monospace';
            -fx-font-weight: bold;
            -fx-effect: dropshadow(gaussian, #9a30fa, 5, 0, 0, 0);
            """);

        
        Label descLabel = new Label(product.getDescription());
        descLabel.setStyle("""
            -fx-text-fill: #b19cd9;
            -fx-font-family: 'monospace';
            -fx-opacity: 0.9;
            """);
        descLabel.setWrapText(true);

        
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
            
            long actualPrice = Math.round(finalPrice);
            
            if (parent.getCompany().getMoney() < actualPrice) {
                parent.pushNotification("PURCHASE FAILED", "INSUFFICIENT FUNDS");
                return;
            }

            parent.getCompany().setMoney(parent.getCompany().getMoney() - actualPrice);
            
            updateMoneyDisplay();

            
            VPSOptimization vps = new VPSOptimization();
            
            String vpsId = "vps-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
            vps.setVpsId(vpsId);
            vps.setName(product.getName());
            vps.setVCPUs(product.getCpu());
            vps.setRamInGB(product.getRam());
            vps.setDiskInGB(product.getStorage());
            vps.setSize(product.getSize());
            vps.setInstalled(false); 

            
            parent.getVpsInventory().addVPS(vpsId, vps);
            System.out.println("เพิ่ม VPS เข้า GameplayContentPane inventory: " + vpsId);
            
            
            GameManager.getInstance().getVpsInventory().addVPS(vpsId, vps);
            System.out.println("เพิ่ม VPS เข้า GameManager inventory: " + vpsId);
            
            
            GameManager.getInstance().saveState();
            System.out.println("บันทึกเกมหลังซื้อ VPS เรียบร้อย");

            
            parent.pushNotification("PURCHASE SUCCESSFUL",
                    "ACQUIRED " + product.getName() + " FOR " + product.getPriceDisplay() + 
                    "\nVPS added to your inventory!");

            
            onClose.run();
            
            
            parent.getMenuBar().setVisible(false);
            parent.getInGameMarketMenuBar().setVisible(false);
            
            
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

        
        SkillPointsSystem skillPointsSystem = ResourceManager.getInstance().getSkillPointsSystem();
        int discountPercent = skillPointsSystem.getMarketDiscount();
        double discountMultiplier = 1.0 - (discountPercent / 100.0);
        double finalPrice = Math.round(product.getPrice() * discountMultiplier);

        
        Label nameLabel = new Label(product.getName());
        nameLabel.setStyle("""
            -fx-text-fill: #9a30fa;
            -fx-font-size: 18px;
            -fx-font-family: 'monospace';
            -fx-font-weight: bold;
            -fx-effect: dropshadow(gaussian, #9a30fa, 5, 0, 0, 0);
            """);

        
        Label descLabel = new Label(product.getDescription());
        descLabel.setStyle("""
            -fx-text-fill: #b19cd9;
            -fx-font-family: 'monospace';
            -fx-opacity: 0.9;
            """);
        descLabel.setWrapText(true);

        
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
            
            long actualPrice = Math.round(finalPrice);
            
            if (parent.getCompany().getMoney() < actualPrice) {
                parent.pushNotification("PURCHASE FAILED", "INSUFFICIENT FUNDS");
                return;
            }

            parent.getCompany().setMoney(parent.getCompany().getMoney() - actualPrice);
            
            updateMoneyDisplay();
            
            
            parent.getRack().addRack(product.getSlots());
            
            
            parent.getRack().goToLatestRack();

            parent.pushNotification("PURCHASE SUCCESSFUL", 
                "ACQUIRED " + product.getName() + ". NEW RACK INSTALLED WITH " + 
                product.getSlots() + " SLOTS.");

            
            onClose.run();
            
            
            parent.getMenuBar().setVisible(false);
            parent.getInGameMarketMenuBar().setVisible(false);
            
            parent.openRackInfo();
        });

        card.getChildren().addAll(nameLabel, descLabel, priceLabel, buyButton);
        return card;
    }
    
    
    private void updateMoneyDisplay() {
        moneyDisplay.setText("$" + parent.getCompany().getMoney());
    }
}

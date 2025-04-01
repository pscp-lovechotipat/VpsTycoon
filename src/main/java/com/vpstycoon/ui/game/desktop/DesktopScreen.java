package com.vpstycoon.ui.game.desktop;

import com.vpstycoon.config.DefaultGameConfig;
import com.vpstycoon.game.chat.ChatSystem;
import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.game.manager.VPSManager;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.game.thread.GameTimeManager;
import com.vpstycoon.screen.ScreenResolution;
import com.vpstycoon.ui.game.GameplayContentPane;
import com.vpstycoon.ui.game.desktop.messenger.controllers.MessengerController;
import com.vpstycoon.ui.game.desktop.messenger.models.ChatHistoryManager;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.util.HashMap;
import java.util.Map;

public class DesktopScreen extends StackPane {
    private final double companyRating;
    private final int marketingPoints;
    private final ChatSystem chatSystem;
    private final RequestManager requestManager;
    private final VPSManager vpsManager;
    private final Company company;
    private final GameplayContentPane parent;
    private final GameTimeManager gameTimeManager;
    private final Runnable onExit;

    private MessengerWindow chatWindow;
    private MessengerController chatController;
    
    
    private static Map<String, Image> imageCache = new HashMap<>();
    private static boolean imagesPreloaded = false;
    
    
    private static final String[] DESKTOP_IMAGES = {
        "/images/wallpaper/Desktop.gif",
        "/images/buttons/MessengerDesktop.png",
        "/images/buttons/MarketDesktop.png",
        "/images/buttons/RoomDesktop.gif",
        "/images/buttons/ServerDesktop.gif"
    };
    
    static {
        preloadImages();
    }
    
    public static synchronized void preloadImages() {
        if (imagesPreloaded) {
            System.out.println("Desktop images already preloaded, skipping");
            return;
        }

        System.out.println("Preloading desktop images");
        for (String imagePath : DESKTOP_IMAGES) {
            loadImage(imagePath);
        }
        
        imagesPreloaded = true;
        System.out.println("Desktop images preloading complete");
    }
    
    public static Image loadImage(String path) {
        if (!imageCache.containsKey(path)) {
            try {
                Image image = new Image(path, true);
                imageCache.put(path, image);
                return image;
            } catch (Exception e) {
                System.err.println("Error loading image: " + path + " - " + e.getMessage());
                return null;
            }
        }
        return imageCache.get(path);
    }

    public DesktopScreen(double companyRating, int marketingPoints,
                         ChatSystem chatSystem, RequestManager requestManager,
                         VPSManager vpsManager, Company company, GameplayContentPane parent, GameTimeManager gameTimeManager, Runnable onExit) {
        this.companyRating = companyRating;
        this.marketingPoints = marketingPoints;
        this.chatSystem = chatSystem;
        this.requestManager = requestManager;
        this.vpsManager = vpsManager;
        this.company = company;
        this.parent = parent;
        this.gameTimeManager = gameTimeManager;
        this.onExit = onExit;

        setupUI();
    }

    private void setupUI() {
        ScreenResolution resolution = DefaultGameConfig.getInstance().getResolution();

        
        setStyle("-fx-background-color: black;");
        
        
        javafx.scene.image.Image backgroundImage = loadImage("/images/wallpaper/Desktop.gif");
        
        
        Pane backgroundLayer = createBackgroundLayer(backgroundImage);
        
        
        double fixedScaleFactor = 0.3;
        Pane messengerButton = createMessengerButton(fixedScaleFactor);
        Pane marketButton = createMarketButton(fixedScaleFactor);
        Pane roomButton = createRoomButton(fixedScaleFactor);
        Pane serverButton = createServerButton(fixedScaleFactor);
        
        
        Group desktopGroup = new Group(backgroundLayer, messengerButton, marketButton, roomButton, serverButton);
        
        
        double centerX = (resolution.getWidth() - backgroundLayer.getPrefWidth()) / 2.0;
        double centerY = (resolution.getHeight() - backgroundLayer.getPrefHeight()) / 2.0;
        desktopGroup.setLayoutX(centerX);
        desktopGroup.setLayoutY(centerY);
        
        
        getChildren().add(desktopGroup);
        
        
        System.out.println("Screen resolution: " + resolution.getWidth() + "x" + resolution.getHeight());
    }

    private Pane createBackgroundLayer(Image backgroundImage) {
        Pane backgroundLayer = new Pane();
        
        
        double fixedScaleFactor = 0.3;
        double fixedWidth = backgroundImage != null ? backgroundImage.getWidth() * fixedScaleFactor : 1920 * fixedScaleFactor;
        double fixedHeight = backgroundImage != null ? backgroundImage.getHeight() * fixedScaleFactor : 1080 * fixedScaleFactor;
        
        
        backgroundLayer.setPrefWidth(fixedWidth);
        backgroundLayer.setPrefHeight(fixedHeight);
        backgroundLayer.setMinWidth(fixedWidth);
        backgroundLayer.setMinHeight(fixedHeight);
        backgroundLayer.setMaxWidth(fixedWidth);
        backgroundLayer.setMaxHeight(fixedHeight);
        
        
        String imageUrl = "/images/wallpaper/Desktop.gif";
        
        
        backgroundLayer.setStyle(String.format("""
            -fx-background-image: url("%s");
            -fx-background-size: %fpx %fpx;
            -fx-background-repeat: no-repeat;
            -fx-background-position: center;
            """, imageUrl, fixedWidth, fixedHeight));
        
        System.out.println("กำหนดขนาดภาพพื้นหลังคงที่: " + fixedWidth + "x" + fixedHeight);
        
        return backgroundLayer;
    }

    private Pane createMessengerButton(double fixedScaleFactor) {
        Pane messengerButton = new Pane();
        messengerButton.setPrefWidth(640);
        messengerButton.setPrefHeight(160);
        messengerButton.setScaleX(fixedScaleFactor);
        messengerButton.setScaleY(fixedScaleFactor);
        messengerButton.setTranslateX(-176);
        messengerButton.setTranslateY(127);
        
        
        String imageUrl = "/images/buttons/MessengerDesktop.png";
        loadImage(imageUrl); 
        
        String normalStyle = String.format("""
            -fx-background-image: url('%s');
            -fx-background-size: contain;
            -fx-background-repeat: no-repeat;
            -fx-background-position: center;
        """, imageUrl);
        
        String hoverStyle = String.format("""
            -fx-background-image: url('%s');
            -fx-background-size: contain;
            -fx-background-repeat: no-repeat;
            -fx-background-position: center;
            -fx-effect: dropshadow(gaussian, #07edf5, 100, 0.1, 0, 0);
        """, imageUrl);
        
        messengerButton.setStyle(normalStyle);
        messengerButton.setOnMouseEntered(event -> messengerButton.setStyle(hoverStyle));
        messengerButton.setOnMouseExited(event -> messengerButton.setStyle(normalStyle));
        messengerButton.setOnMouseClicked(e -> this.openChatWindow());
        
        return messengerButton;
    }

    private Pane createMarketButton(double fixedScaleFactor) {
        Pane marketButton = new Pane();
        marketButton.setPrefWidth(640);
        marketButton.setPrefHeight(160);
        marketButton.setScaleX(fixedScaleFactor);
        marketButton.setScaleY(fixedScaleFactor);
        marketButton.setTranslateX(-176);
        marketButton.setTranslateY(192);
        
        
        String imageUrl = "/images/buttons/MarketDesktop.png";
        loadImage(imageUrl); 
        
        String normalStyle = String.format("""
            -fx-background-image: url('%s');
            -fx-background-size: contain;
            -fx-background-repeat: no-repeat;
            -fx-background-position: center;
        """, imageUrl);
        
        String hoverStyle = String.format("""
            -fx-background-image: url('%s');
            -fx-background-size: contain;
            -fx-background-repeat: no-repeat;
            -fx-background-position: center;
            -fx-effect: dropshadow(gaussian, #07edf5, 100, 0.1, 0, 0);
        """, imageUrl);
        
        marketButton.setStyle(normalStyle);
        marketButton.setOnMouseEntered(event -> marketButton.setStyle(hoverStyle));
        marketButton.setOnMouseExited(event -> marketButton.setStyle(normalStyle));
        marketButton.setOnMouseClicked(e -> this.openMarketWindow());
        
        return marketButton;
    }

    private Pane createRoomButton(double fixedScaleFactor) {
        Pane roomButton = new Pane();
        roomButton.setPrefWidth(1850);
        roomButton.setPrefHeight(1070);
        roomButton.setScaleX(fixedScaleFactor);
        roomButton.setScaleY(fixedScaleFactor);
        roomButton.setTranslateX(-365);
        roomButton.setTranslateY(-290);
        
        
        String imageUrl = "/images/buttons/RoomDesktop.gif";
        loadImage(imageUrl); 
        
        String normalStyle = String.format("""
            -fx-background-image: url('%s');
            -fx-background-size: contain;
            -fx-background-repeat: no-repeat;
            -fx-background-position: center;
        """, imageUrl);
        
        String hoverStyle = String.format("""
            -fx-background-image: url('%s');
            -fx-background-size: contain;
            -fx-background-repeat: no-repeat;
            -fx-background-position: center;
            -fx-effect: dropshadow(gaussian, #07edf5, 100, 0.1, 0, 0);
        """, imageUrl);
        
        roomButton.setStyle(normalStyle);
        roomButton.setOnMouseEntered(event -> roomButton.setStyle(hoverStyle));
        roomButton.setOnMouseExited(event -> roomButton.setStyle(normalStyle));
        roomButton.setOnMouseClicked(e -> this.onExit.run());
        
        return roomButton;
    }

    private Pane createServerButton(double fixedScaleFactor) {
        Pane serverButton = new Pane();
        serverButton.setPrefWidth(690);
        serverButton.setPrefHeight(1610);
        serverButton.setScaleX(fixedScaleFactor);
        serverButton.setScaleY(fixedScaleFactor);
        serverButton.setTranslateX(644);
        serverButton.setTranslateY(-475);
        
        
        String imageUrl = "/images/buttons/ServerDesktop.gif";
        loadImage(imageUrl); 
        
        String normalStyle = String.format("""
            -fx-background-image: url('%s');
            -fx-background-size: contain;
            -fx-background-repeat: no-repeat;
            -fx-background-position: center;
        """, imageUrl);
        
        String hoverStyle = String.format("""
            -fx-background-image: url('%s');
            -fx-background-size: contain;
            -fx-background-repeat: no-repeat;
            -fx-background-position: center;
            -fx-effect: dropshadow(gaussian, #07edf5, 100, 0.1, 0, 0);
        """, imageUrl);
        
        serverButton.setStyle(normalStyle);
        serverButton.setOnMouseEntered(event -> serverButton.setStyle(hoverStyle));
        serverButton.setOnMouseExited(event -> serverButton.setStyle(normalStyle));
        serverButton.setOnMouseClicked(e -> this.parent.openRackInfo());
        
        return serverButton;
    }

    private void openChatWindow() {
        if (chatWindow == null) {
            ChatHistoryManager chatHistoryManager = ResourceManager.getInstance().getChatHistory();
            chatController = new MessengerController(requestManager, vpsManager, company, chatHistoryManager,
                    parent.getRootStack(), gameTimeManager, this::closeChatWindow);
            chatWindow = chatController.getMessengerWindow();
            
            chatWindow.getDashboardView().updateDashboard(
                company.getRating(),
                requestManager.getRequests().size(),
                company.getAvailableVMs(),  
                vpsManager.getVPSMap().size()
            );
            
            chatWindow.getCloseButton().setOnAction(e -> {
                chatController.close();
                closeChatWindow();
            });
        }
        
        if (!getChildren().contains(chatWindow)) {
            chatWindow.getDashboardView().updateDashboard(
                company.getRating(),
                requestManager.getRequests().size(),
                company.getAvailableVMs(),
                vpsManager.getVPSMap().size()
            );
            
            chatWindow.setPrefSize(900, 700);
            chatWindow.setMaxSize(1200, 800);
            
            StackPane.setAlignment(chatWindow, Pos.CENTER);
            getChildren().add(chatWindow);
        }
    }

    private void closeChatWindow() {
        if (chatWindow != null && getChildren().contains(chatWindow)) {
            getChildren().remove(chatWindow);
            chatWindow = null; 
            chatController = null; 
        }
    }

    private void openMarketWindow() {
        MarketWindow marketWindow = new MarketWindow(() -> getChildren().removeIf(node -> node instanceof MarketWindow),
                () -> getChildren().removeIf(node -> node instanceof MarketWindow), vpsManager, parent);
        
        marketWindow.setPrefSize(1200, 800);
        marketWindow.setMaxSize(1600, 900);
        
        StackPane.setAlignment(marketWindow, Pos.CENTER);
        getChildren().add(marketWindow);
    }

    private void openDashboardWindow() {
        DashboardWindow dashboardWindow = new DashboardWindow(company, vpsManager, requestManager,
                () -> getChildren().removeIf(node -> node instanceof DashboardWindow));
        
        dashboardWindow.setPrefSize(1200, 800);
        dashboardWindow.setMaxSize(1600, 900);
        
        StackPane.setAlignment(dashboardWindow, Pos.CENTER);
        getChildren().add(dashboardWindow);
    }
}


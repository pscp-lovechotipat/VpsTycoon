package com.vpstycoon.ui.game.desktop;

import com.vpstycoon.config.DefaultGameConfig;
import com.vpstycoon.manager.RequestManager;
import com.vpstycoon.manager.ResourceManager;
import com.vpstycoon.manager.VPSManager;
import com.vpstycoon.model.company.Company;
import com.vpstycoon.persistence.ChatHistoryManager;
import com.vpstycoon.screen.ScreenResolution;
import com.vpstycoon.thread.GameTimeManager;
import com.vpstycoon.ui.common.ImageCache;
import com.vpstycoon.ui.game.GameplayContentPane;
import com.vpstycoon.ui.game.messenger.MessengerController;
import com.vpstycoon.ui.game.messenger.MessengerWindow;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class DesktopScreen extends StackPane {
    private final RequestManager requestManager;
    private final VPSManager vpsManager;
    private final Company company;
    private final GameplayContentPane parent;
    private final GameTimeManager gameTimeManager;
    private final Runnable onExit;

    private MessengerWindow chatWindow;
    private MessengerController chatController;

    public DesktopScreen(RequestManager requestManager, VPSManager vpsManager, Company company, GameplayContentPane parent, GameTimeManager gameTimeManager, Runnable onExit) {
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

        Image backgroundImage = ImageCache.get("/images/wallpaper/Desktop.gif");

        Pane backgroundLayer = createBackgroundLayer(backgroundImage);

        double fixedScaleFactor = 0.3;
        Pane messengerButton = createDesktopIcon("/images/buttons/MessengerDesktop.png", 640, 160, -176, 127, fixedScaleFactor, this::openChatWindow);
        Pane marketButton = createDesktopIcon("/images/buttons/MarketDesktop.png", 640, 160, -176, 192, fixedScaleFactor, this::openMarketWindow);
        Pane roomButton = createDesktopIcon("/images/buttons/RoomDesktop.gif", 1850, 1070, -365, -290, fixedScaleFactor, onExit);
        Pane serverButton = createDesktopIcon("/images/buttons/ServerDesktop.gif", 690, 1610, 644, -475, fixedScaleFactor, parent::openRackInfo);

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

    private Pane createDesktopIcon(String imageUrl, double width, double height,
                                   double translateX, double translateY, double scale, Runnable onClick) {
        Pane icon = new Pane();
        icon.setPrefWidth(width);
        icon.setPrefHeight(height);
        icon.setScaleX(scale);
        icon.setScaleY(scale);
        icon.setTranslateX(translateX);
        icon.setTranslateY(translateY);

        String normalStyle = String.format("""
            -fx-background-image: url('%s');
            -fx-background-size: contain;
            -fx-background-repeat: no-repeat;
            -fx-background-position: center;
        """, imageUrl);

        String hoverStyle = normalStyle + "-fx-effect: dropshadow(gaussian, #07edf5, 100, 0.1, 0, 0);";

        icon.setStyle(normalStyle);
        icon.setOnMouseEntered(event -> icon.setStyle(hoverStyle));
        icon.setOnMouseExited(event -> icon.setStyle(normalStyle));
        icon.setOnMouseClicked(e -> onClick.run());

        return icon;
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
        MarketWindow marketWindow = new MarketWindow(() -> getChildren().removeIf(node -> node instanceof MarketWindow), parent);

        marketWindow.setPrefSize(1200, 800);
        marketWindow.setMaxSize(1600, 900);

        StackPane.setAlignment(marketWindow, Pos.CENTER);
        getChildren().add(marketWindow);
    }
}

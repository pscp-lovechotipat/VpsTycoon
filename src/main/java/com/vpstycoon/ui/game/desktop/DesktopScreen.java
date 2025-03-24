package com.vpstycoon.ui.game.desktop;

import com.vpstycoon.game.chat.ChatSystem;
import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.game.manager.VPSManager;
import com.vpstycoon.game.thread.GameTimeManager;
import com.vpstycoon.ui.game.GameplayContentPane;
import com.vpstycoon.ui.game.desktop.messenger.controllers.MessengerController;
import com.vpstycoon.ui.game.desktop.messenger.models.ChatHistoryManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

public class DesktopScreen extends StackPane {
    private final double companyRating;
    private final int marketingPoints;
    private final ChatSystem chatSystem;
    private final RequestManager requestManager;
    private final VPSManager vpsManager;
    private final Company company;
    private final GameplayContentPane parent;
    private final GameTimeManager gameTimeManager;

    private MessengerWindow chatWindow;
    private MessengerController chatController;

    public DesktopScreen(double companyRating, int marketingPoints,
                         ChatSystem chatSystem, RequestManager requestManager,
                         VPSManager vpsManager, Company company, GameplayContentPane parent, GameTimeManager gameTimeManager) {
        this.companyRating = companyRating;
        this.marketingPoints = marketingPoints;
        this.chatSystem = chatSystem;
        this.requestManager = requestManager;
        this.vpsManager = vpsManager;
        this.company = company;
        this.parent = parent;
        this.gameTimeManager = gameTimeManager;

        setupUI();
    }

    private void setupUI() {
        setStyle("-fx-background-color: #1e1e1e; -fx-background-image: url(/images/wallpaper/desktop_wallpaper_bambam.png); -fx-background-size: cover; -fx-background-position: center; -fx-background-repeat: no-repeat;");
        setPrefSize(800, 600);

        FlowPane iconsContainer = new FlowPane(10, 10);
        iconsContainer.setPadding(new Insets(20));
        iconsContainer.setAlignment(Pos.TOP_LEFT);

        DesktopIcon messengerIcon = new DesktopIcon(FontAwesomeSolid.COMMENTS.toString(), "Messenger", this::openChatWindow);
        iconsContainer.getChildren().addAll(messengerIcon,
                new DesktopIcon(FontAwesomeSolid.SHOPPING_CART.toString(), "Market", this::openMarketWindow),
                new DesktopIcon(FontAwesomeSolid.CHART_LINE.toString(), "Dashboard", this::openDashboardWindow));

        getChildren().add(iconsContainer);
    }

    private void openChatWindow() {
        if (chatWindow == null) {
            // สร้าง ChatHistoryManager
            ChatHistoryManager chatHistoryManager = new ChatHistoryManager();

            // สร้าง MessengerController และส่ง ChatHistoryManager เข้าไป
            chatController = new MessengerController(requestManager, vpsManager, company, chatHistoryManager,
                    parent.getRootStack(), gameTimeManager, this::closeChatWindow);

            // ดึง MessengerWindow จาก MessengerController
            chatWindow = chatController.getMessengerWindow();

            // ตั้งค่าปุ่มปิดให้ทำงานอย่างถูกต้อง
            chatWindow.getCloseButton().setOnAction(e -> {
                chatController.close();
                closeChatWindow();
            });
        }
        if (!getChildren().contains(chatWindow)) {
            StackPane.setAlignment(chatWindow, Pos.CENTER);
            getChildren().add(chatWindow);
        }
    }

    private void closeChatWindow() {
        if (chatWindow != null && getChildren().contains(chatWindow)) {
            getChildren().remove(chatWindow);
            chatWindow = null; // รีเซ็ต chatWindow เพื่อให้สามารถสร้างใหม่ได้
            chatController = null; // รีเซ็ต chatController
        }
    }

    private void openMarketWindow() {
        MarketWindow marketWindow = new MarketWindow(() -> getChildren().removeIf(node -> node instanceof MarketWindow),
                () -> getChildren().removeIf(node -> node instanceof MarketWindow), vpsManager, parent);
        StackPane.setAlignment(marketWindow, Pos.CENTER);
        getChildren().add(marketWindow);
    }

    private void openDashboardWindow() {
        DashboardWindow dashboardWindow = new DashboardWindow(company, vpsManager, requestManager,
                () -> getChildren().removeIf(node -> node instanceof DashboardWindow));
        StackPane.setAlignment(dashboardWindow, Pos.CENTER);
        getChildren().add(dashboardWindow);
    }

    public void addExitButton(Runnable onExit) {
        Button exitButton = new Button("Exit");
        exitButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 1);");
        exitButton.setOnAction(e -> { if (onExit != null) onExit.run(); });
        StackPane.setAlignment(exitButton, Pos.TOP_RIGHT);
        StackPane.setMargin(exitButton, new Insets(20));
        getChildren().add(exitButton);
    }
}
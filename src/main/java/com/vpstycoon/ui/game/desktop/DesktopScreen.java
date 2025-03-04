package com.vpstycoon.ui.game.desktop;

import com.vpstycoon.game.chat.ChatSystem;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.game.manager.VPSManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Button;
import javafx.stage.Popup;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

public class DesktopScreen extends StackPane {
    private final double companyRating;
    private final int marketingPoints;
    private final ChatSystem chatSystem;
    private final RequestManager requestManager;
    private final VPSManager vpsManager;
    private Popup chatWindow;

    public DesktopScreen(double companyRating, int marketingPoints,
                        ChatSystem chatSystem, RequestManager requestManager,
                        VPSManager vpsManager) {
        this.companyRating = companyRating;
        this.marketingPoints = marketingPoints;
        this.chatSystem = chatSystem;
        this.requestManager = requestManager;
        this.vpsManager = vpsManager;

        setupUI();
    }

    private synchronized void setupUI() {
        // Set desktop background
        setStyle("""
                -fx-background-color: #1e1e1e;
                -fx-background-image: url(/images/wallpaper/cyber-cat-yuumi-skin-splash-art-lol-hd-wallpaper-uhdpaper.com-236@3@b.jpg);
                -fx-background-position: center;
                -fx-background-repeat: no-repeat;
                -fx-background-size: contain;
                """);
        setPrefSize(800, 600);

        // Create desktop icons container
        FlowPane iconsContainer = new FlowPane(10, 10);
        iconsContainer.setPadding(new Insets(20));
        iconsContainer.setAlignment(Pos.TOP_LEFT);

        // Add Messenger icon using FontAwesome
        DesktopIcon messengerIcon = new DesktopIcon(
            FontAwesomeSolid.COMMENTS.toString(),
            "Messenger",
            this::openChatWindow
        );

        iconsContainer.getChildren().add(messengerIcon);

        DesktopIcon marketIcon = new DesktopIcon(
                FontAwesomeSolid.SHOPPING_CART.toString(),
                "Market",
                this::openMarketWindow
        );

        iconsContainer.getChildren().add(marketIcon);

        // Add Dashboard icon using FontAwesome
        DesktopIcon dashboardIcon = new DesktopIcon(
                FontAwesomeSolid.CHART_LINE.toString(),
                "Dashboard",
                this::openDashboardWindow
        );

        iconsContainer.getChildren().add(dashboardIcon);


        getChildren().add(iconsContainer);
    }

    private void openChatWindow() {
        if (chatWindow == null) {
            chatWindow = new Popup();
            chatWindow.setAutoHide(true);

            // ✅ ลบ ChatSystem ออก และใช้แค่ RequestManager กับ Runnable
            MessengerWindow messengerContent = new MessengerWindow(
                    requestManager,  // ✅ ใช้แค่ requestManager ตาม constructor ใหม่
                    () -> chatWindow.hide()
            );

            chatWindow.getContent().add(messengerContent);
        }

        if (!chatWindow.isShowing()) {
            chatWindow.show(getScene().getWindow());
            chatWindow.setX(getScene().getWindow().getX() +
                    (getScene().getWindow().getWidth() - chatWindow.getWidth()) / 2);
            chatWindow.setY(getScene().getWindow().getY() +
                    (getScene().getWindow().getHeight() - chatWindow.getHeight()) / 2);
        }
    }

    private Popup marketWindow;

    private void openMarketWindow() {
        if (marketWindow == null) {
            marketWindow = new Popup();
            marketWindow.setAutoHide(true);

//            MarketWindow marketContent = new MarketWindow(() -> marketWindow.hide());

//            marketWindow.getContent().add(marketContent);
        }

        if (!marketWindow.isShowing()) {
            // Show popup centered on the screen
            marketWindow.show(getScene().getWindow());
            marketWindow.setX(getScene().getWindow().getX() +
                    (getScene().getWindow().getWidth() - marketWindow.getWidth()) / 2);
            marketWindow.setY(getScene().getWindow().getY() +
                    (getScene().getWindow().getHeight() - marketWindow.getHeight()) / 2);
        }
    }

    private Popup dashboardWindow;

    private double calculateMonthlyRevenue() {
        // return vpsManager.getTotalRevenue(); // ใช้ vpsManager คำนวณจริง ๆ
        return 1000.0;
    }

    private void openDashboardWindow() {
        if (dashboardWindow == null) {
            dashboardWindow = new Popup();
            dashboardWindow.setAutoHide(true);

            DashboardWindow dashboardContent = new DashboardWindow(
                    companyRating, marketingPoints, calculateMonthlyRevenue(),
                    () -> dashboardWindow.hide()
            );

            dashboardWindow.getContent().add(dashboardContent);
        }

        if (!dashboardWindow.isShowing()) {
            dashboardWindow.show(getScene().getWindow());
            dashboardWindow.setX(getScene().getWindow().getX() +
                    (getScene().getWindow().getWidth() - dashboardWindow.getWidth()) / 2);
            dashboardWindow.setY(getScene().getWindow().getY() +
                    (getScene().getWindow().getHeight() - dashboardWindow.getHeight()) / 2);
        }
    }

    /**
     * Adds an exit button to the desktop that triggers the provided action when clicked
     * @param onExit Runnable to execute when exit button is clicked
     */
    public void addExitButton(Runnable onExit) {
        // Create a button in the top-right corner
        Button exitButton = new Button("Exit");
        exitButton.setStyle("""
            -fx-background-color: #e74c3c;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-padding: 8 15;
            -fx-background-radius: 5;
            -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 1);
        """);
        
        // Set button action
        exitButton.setOnAction(e -> {
            if (onExit != null) {
                onExit.run();
            }
        });
        
        // Position in top-right corner
        StackPane.setAlignment(exitButton, Pos.TOP_RIGHT);
        StackPane.setMargin(exitButton, new Insets(20));
        
        // Add to the display
        getChildren().add(exitButton);
    }
}

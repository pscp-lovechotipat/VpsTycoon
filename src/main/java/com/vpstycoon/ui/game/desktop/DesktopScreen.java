package com.vpstycoon.ui.game.desktop;

import com.vpstycoon.game.chat.ChatSystem;
import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.game.manager.VPSManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label; // Added missing import
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
    private MessengerWindow chatWindow; // Store as a field to reuse

    public DesktopScreen(double companyRating, int marketingPoints,
                         ChatSystem chatSystem, RequestManager requestManager,
                         VPSManager vpsManager, Company company) {
        this.companyRating = companyRating;
        this.marketingPoints = marketingPoints;
        this.chatSystem = chatSystem;
        this.requestManager = requestManager;
        this.vpsManager = vpsManager;
        this.company = company;

        setupUI();
    }

    private synchronized void setupUI() {
        setStyle("""
                -fx-background-color: #1e1e1e;
                -fx-background-image: url(/images/wallpaper/cyber-cat-yuumi-skin-splash-art-lol-hd-wallpaper-uhdpaper.com-236@3@b.jpg);
                -fx-background-size: contain;
                -fx-background-position: center;
                -fx-background-repeat: no-repeat;
                """);
        setPrefSize(800, 600);

        FlowPane iconsContainer = new FlowPane(10, 10);
        iconsContainer.setPadding(new Insets(20));
        iconsContainer.setAlignment(Pos.TOP_LEFT);

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
            chatWindow = new MessengerWindow(
                    requestManager,
                    vpsManager, // Pass VPSManager instead of GameplayContentPane
                    company,
                    this::closeChatWindow // Callback to close the window
            );
        }
        if (!getChildren().contains(chatWindow)) {
            StackPane.setAlignment(chatWindow, Pos.CENTER);
            getChildren().add(chatWindow);
        }
    }

    private void closeChatWindow() {
        if (chatWindow != null && getChildren().contains(chatWindow)) {
            getChildren().remove(chatWindow);
        }
    }

    private void openMarketWindow() {
        MarketWindow marketWindow = new MarketWindow(
                () -> getChildren().removeIf(node -> node instanceof MarketWindow),
                vpsManager,
                null // Assuming MarketWindow doesn't need GameplayContentPane anymore
        );
        StackPane.setAlignment(marketWindow, Pos.CENTER);
        getChildren().add(marketWindow);
    }

    private void openDashboardWindow() {
        // Placeholder for dashboard functionality
        Label dashboardLabel = new Label("Dashboard: Rating = " + companyRating + ", Marketing Points = " + marketingPoints);
        dashboardLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        StackPane.setAlignment(dashboardLabel, Pos.CENTER);
        getChildren().add(dashboardLabel);

        Button closeDashboard = new Button("Close Dashboard");
        closeDashboard.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        closeDashboard.setOnAction(e -> getChildren().remove(dashboardLabel));
        StackPane.setAlignment(closeDashboard, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(closeDashboard, new Insets(0, 20, 20, 0));
        getChildren().add(closeDashboard);
    }

    public void addExitButton(Runnable onExit) {
        Button exitButton = new Button("Exit");
        exitButton.setStyle("""
            -fx-background-color: #e74c3c;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-padding: 8 15;
            -fx-background-radius: 5;
            -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 1);
        """);

        exitButton.setOnAction(e -> {
            if (onExit != null) onExit.run();
        });

        StackPane.setAlignment(exitButton, Pos.TOP_RIGHT);
        StackPane.setMargin(exitButton, new Insets(20));
        getChildren().add(exitButton);
    }
}
package com.vpstycoon.ui.game.desktop;

import com.vpstycoon.ui.game.desktop.messenger.models.ChatHistoryManager;
import com.vpstycoon.ui.game.desktop.messenger.views.ChatAreaView;
import com.vpstycoon.ui.game.desktop.messenger.views.DashboardView;
import com.vpstycoon.ui.game.desktop.messenger.views.RequestListView;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class MessengerWindow extends VBox {
    private RequestListView requestListView;
    private ChatAreaView chatAreaView;
    private DashboardView dashboardView;
    private Timeline glowAnimation;

    
    public MessengerWindow(ChatHistoryManager chatHistoryManager) {
        setPrefSize(900, 650);
        getStylesheets().add(getClass().getResource("/css/messenger-window.css").toExternalForm());
        getStyleClass().add("messenger-window");

        HBox titleBar = createTitleBar();
        requestListView = new RequestListView();
        chatAreaView = new ChatAreaView(chatHistoryManager); 
        dashboardView = new DashboardView();

        
        VBox chatContainer = new VBox(dashboardView, chatAreaView);
        VBox.setVgrow(chatAreaView, Priority.ALWAYS);

        
        HBox content = new HBox(requestListView, chatContainer);
        HBox.setHgrow(chatContainer, Priority.ALWAYS);
        content.setPadding(new Insets(0));

        
        VBox.setVgrow(content, Priority.ALWAYS);

        getChildren().addAll(titleBar, content);
    }

    private HBox createTitleBar() {
        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(10, 15, 10, 15));
        titleBar.getStyleClass().add("title-bar");

        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        
        Rectangle iconBg = new Rectangle(24, 24);
        iconBg.setArcWidth(5);
        iconBg.setArcHeight(5);
        
        
        Stop[] stops = new Stop[] {
            new Stop(0, Color.web("#00c3ff")),
            new Stop(1, Color.web("#9e33ff"))
        };
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
        iconBg.setFill(gradient);
        
        
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.web("#00c3ff"));
        dropShadow.setRadius(10);
        
        Glow glow = new Glow(0.8);
        iconBg.setEffect(glow);
        
        
        glowAnimation = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(glow.levelProperty(), 0.5)),
            new KeyFrame(Duration.seconds(1.5), new KeyValue(glow.levelProperty(), 0.8))
        );
        glowAnimation.setAutoReverse(true);
        glowAnimation.setCycleCount(Timeline.INDEFINITE);
        glowAnimation.play();
        
        
        Text matrixText = new Text("M");
        matrixText.setFill(Color.WHITE);
        matrixText.setStyle("-fx-font-weight: bold;");
        HBox iconContainer = new HBox(iconBg);
        iconContainer.setAlignment(Pos.CENTER);
        iconContainer.getChildren().add(matrixText);
        iconContainer.setTranslateX(-12); 

        
        Label titleLabel = new Label("VPS TYCOON MESSENGER");
        titleLabel.getStyleClass().add("title-text");
        
        
        Label versionLabel = new Label("v2.0");
        versionLabel.setStyle("-fx-text-fill: #00ffff; -fx-font-size: 10px; -fx-font-family: 'Monospace', 'Courier New', monospace;");
        versionLabel.setTranslateY(4); 
        
        
        VBox titleVersionBox = new VBox(0);
        titleVersionBox.setAlignment(Pos.CENTER_LEFT);
        titleVersionBox.getChildren().addAll(titleLabel, versionLabel);

        titleBox.getChildren().addAll(iconContainer, titleVersionBox);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        
        Circle statusIndicator = new Circle(6);
        statusIndicator.setFill(Color.web("#00ff88"));
        statusIndicator.setEffect(new Glow(0.8));
        
        Label statusLabel = new Label("ONLINE");
        statusLabel.setStyle("-fx-text-fill: #00ff88; -fx-font-size: 10px; -fx-font-family: 'Monospace', 'Courier New', monospace;");
        
        HBox statusBox = new HBox(5);
        statusBox.setAlignment(Pos.CENTER);
        statusBox.getChildren().addAll(statusIndicator, statusLabel);
        
        
        Button closeButton = new Button("×");
        closeButton.getStyleClass().add("close-button");

        titleBar.getChildren().addAll(titleBox, statusBox, closeButton);
        return titleBar;
    }

    public RequestListView getRequestListView() { return requestListView; }
    public ChatAreaView getChatAreaView() { return chatAreaView; }
    public DashboardView getDashboardView() { return dashboardView; }
    public Button getCloseButton() {
        HBox titleBar = (HBox) getChildren().get(0);
        return (Button) titleBar.getChildren().get(2);
    }
}

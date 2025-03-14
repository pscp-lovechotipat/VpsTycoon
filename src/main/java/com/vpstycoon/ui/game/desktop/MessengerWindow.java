package com.vpstycoon.ui.game.desktop;

import com.vpstycoon.ui.game.desktop.messenger.ChatAreaView;
import com.vpstycoon.ui.game.desktop.messenger.DashboardView;
import com.vpstycoon.ui.game.desktop.messenger.RequestListView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class MessengerWindow extends VBox {
    private RequestListView requestListView;
    private ChatAreaView chatAreaView;
    private DashboardView dashboardView;

    public MessengerWindow() {
        setPrefSize(900, 650);
        getStylesheets().add(getClass().getResource("/css/messenger-window.css").toExternalForm());
        getStyleClass().add("messenger-window");

        HBox titleBar = createTitleBar();
        requestListView = new RequestListView();
        chatAreaView = new ChatAreaView();
        dashboardView = new DashboardView();

        // สร้าง chatContainer และตั้งค่า Vgrow ให้ chatAreaView ขยายเต็มที่ในแนวตั้ง
        VBox chatContainer = new VBox(dashboardView, chatAreaView);
        VBox.setVgrow(chatAreaView, Priority.ALWAYS); // ให้ chatAreaView ขยายเต็มพื้นที่แนวตั้ง

        // สร้าง HBox content และตั้งค่า Hgrow ให้ chatContainer ขยายเต็มที่ในแนวนอน
        HBox content = new HBox(requestListView, chatContainer);
        HBox.setHgrow(chatContainer, Priority.ALWAYS); // ให้ chatContainer ขยายเต็มพื้นที่แนวนอนที่เหลือ
        content.setPadding(new Insets(0));

        // ตั้งค่า Vgrow ให้ content ขยายเต็มที่ใน MessengerWindow
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

        Circle messengerIcon = new Circle(12);
        messengerIcon.setFill(Color.WHITE);

        Label titleLabel = new Label("VPS Tycoon Messenger");
        titleLabel.getStyleClass().add("title-text");

        titleBox.getChildren().addAll(messengerIcon, titleLabel);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Button closeButton = new Button("×");
        closeButton.getStyleClass().add("close-button");

        titleBar.getChildren().addAll(titleBox, closeButton);
        return titleBar;
    }

    public RequestListView getRequestListView() { return requestListView; }
    public ChatAreaView getChatAreaView() { return chatAreaView; }
    public DashboardView getDashboardView() { return dashboardView; }
    public Button getCloseButton() {
        HBox titleBar = (HBox) getChildren().get(0); // ดึง titleBar ที่ตำแหน่ง 0
        return (Button) titleBar.getChildren().get(1); // ดึง closeButton ที่ตำแหน่ง 1 ใน titleBar
    }
}
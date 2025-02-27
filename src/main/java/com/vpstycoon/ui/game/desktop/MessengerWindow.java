package com.vpstycoon.ui.game.desktop;

import com.vpstycoon.game.manager.CustomerRequest;
import com.vpstycoon.game.manager.RequestManager;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class MessengerWindow extends VBox {
    private final RequestManager requestManager;
    private final Runnable onClose;
    private final ListView<String> requestView;

    public MessengerWindow(RequestManager requestManager, Runnable onClose) {
        this.requestManager = requestManager;
        this.onClose = onClose;

        this.requestView = new ListView<>();

        setupUI();
        styleWindow();
        setupListeners();
    }

    private void setupUI() {
        setPrefSize(800, 600);

        HBox titleBar = createTitleBar();

        HBox content = new HBox();
        content.setPadding(new Insets(0));
        VBox.setVgrow(content, Priority.ALWAYS);

        VBox requestList = createRequestList();
        VBox chatArea = createChatArea();

        content.getChildren().addAll(requestList, chatArea);
        getChildren().addAll(titleBar, content);

        updateRequestList(); // ✅ โหลดข้อมูลเริ่มต้น
    }

    private HBox createTitleBar() {
        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_RIGHT);
        titleBar.setPadding(new Insets(5, 10, 5, 10));
        titleBar.setStyle("-fx-background-color: #0084ff;");

        Button closeButton = new Button("X");
        closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
        closeButton.setOnAction(e -> onClose.run());

        titleBar.getChildren().add(closeButton);
        return titleBar;
    }

    private VBox createRequestList() {
        VBox requestList = new VBox(10);
        requestList.setPadding(new Insets(10));
        requestList.setStyle("-fx-background-color: #f8f8f8;");
        requestList.setPrefWidth(300);

        Label title = new Label("Customer Requests");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button acceptButton = new Button("Accept Request");
        acceptButton.setOnAction(e -> {
            String selected = requestView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                requestManager.acceptRequest(selected);
            }
        });

        requestList.getChildren().addAll(title, requestView, acceptButton);
        return requestList;
    }

    private void setupListeners() {
        requestManager.getRequests().addListener((ListChangeListener<CustomerRequest>) change -> {
            while (change.next()) {
                if (change.wasAdded() || change.wasRemoved()) {
                    Platform.runLater(this::updateRequestList); // ✅ อัปเดต UI เมื่อมีการเปลี่ยนแปลง
                }
            }
        });
    }


    private void updateRequestList() {
        requestView.getItems().clear();
        requestView.getItems().addAll(
                requestManager.getRequests().stream()
                        .map(req -> req.getTitle()) // ✅ แปลง CustomerRequest เป็น String
                        .toList()
        );
    }


    private VBox createChatArea() {
        VBox chatArea = new VBox(10);
        chatArea.setPadding(new Insets(10));
        HBox.setHgrow(chatArea, Priority.ALWAYS);

        ScrollPane messagesScroll = new ScrollPane();
        messagesScroll.setFitToWidth(true);
        VBox.setVgrow(messagesScroll, Priority.ALWAYS);

        VBox messagesBox = new VBox(10);
        messagesBox.setPadding(new Insets(10));

        messagesScroll.setContent(messagesBox);

        HBox inputArea = new HBox(10);
        inputArea.setAlignment(Pos.CENTER);

        TextField messageInput = new TextField();
        messageInput.setPromptText("Type a message...");
        HBox.setHgrow(messageInput, Priority.ALWAYS);

        Button sendButton = new Button("Send");

        inputArea.getChildren().addAll(messageInput, sendButton);

        chatArea.getChildren().addAll(messagesScroll, inputArea);
        return chatArea;
    }

    private void styleWindow() {
        setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0);");
    }
}

package com.vpstycoon.ui.game.desktop;

import com.vpstycoon.game.manager.CustomerRequest;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.ui.game.GameplayContentPane;
import com.vpstycoon.ui.game.GameplayContentPane.VM;
import com.vpstycoon.ui.game.GameplayContentPane.VPS;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.util.List;

public class MessengerWindow extends VBox {
    private final RequestManager requestManager;
    private final Runnable onClose;
    private final ListView<String> requestView;
    private final GameplayContentPane gameplayContentPane = null; // เพิ่มเพื่อเข้าถึง VM list
    private VBox messagesBox;

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

        updateRequestList();
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
                    Platform.runLater(this::updateRequestList);
                }
            }
        });
    }

    private void updateRequestList() {
        requestView.getItems().clear();
        requestView.getItems().addAll(
                requestManager.getRequests().stream()
                        .map(CustomerRequest::getTitle)
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

        messagesBox = new VBox(10);
        messagesBox.setPadding(new Insets(10));

        messagesScroll.setContent(messagesBox);

        HBox inputArea = new HBox(10);
        inputArea.setAlignment(Pos.CENTER);

        TextField messageInput = new TextField();
        messageInput.setPromptText("Type a message...");
        HBox.setHgrow(messageInput, Priority.ALWAYS);

        Button sendButton = new Button("Send");
        sendButton.setOnAction(e -> {
            String message = messageInput.getText();
            if (!message.isEmpty()) {
                Label messageLabel = new Label("You: " + message);
                messagesBox.getChildren().add(messageLabel);
                messageInput.clear();
            }
        });

        // เพิ่มปุ่ม Send Work
        Button sendWorkButton = new Button("Send Work");
        sendWorkButton.setDisable(true); // ปิดใช้งานจนกว่าจะเลือก request
        sendWorkButton.setOnAction(e -> sendWorkToCustomer());

        // ตรวจสอบเมื่อเลือก request
        requestView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            sendWorkButton.setDisable(newVal == null);
            if (newVal != null) {
                updateChatWithRequestDetails(newVal);
            }
        });

        inputArea.getChildren().addAll(messageInput, sendButton, sendWorkButton);

        chatArea.getChildren().addAll(messagesScroll, inputArea);
        return chatArea;
    }

    private void updateChatWithRequestDetails(String requestTitle) {
        messagesBox.getChildren().clear();
        CustomerRequest selectedRequest = requestManager.getRequests().stream()
                .filter(req -> req.getTitle().equals(requestTitle))
                .findFirst()
                .orElse(null);

        if (selectedRequest != null) {
            Label requestDetails = new Label("Request: " + selectedRequest.getTitle() +
                    "\nRequired Specs: " +
                    "\nvCPUs: " + selectedRequest.getRequiredVCPUs() +
                    "\nRAM: " + selectedRequest.getRequiredRam() +
                    "\nDisk: " + selectedRequest.getRequiredDisk());
            messagesBox.getChildren().add(requestDetails);
        }
    }

    private void sendWorkToCustomer() {
        String selectedRequestTitle = requestView.getSelectionModel().getSelectedItem();
        if (selectedRequestTitle == null) return;

        CustomerRequest selectedRequest = requestManager.getRequests().stream()
                .filter(req -> req.getTitle().equals(selectedRequestTitle))
                .findFirst()
                .orElse(null);

        if (selectedRequest == null) return;

        // ตรวจสอบ VM ที่มีสเปคตรงกับ request
        List<VPS> vpsList = gameplayContentPane.getVpsList();
        VM matchingVM = null;

        for (VPS vps : vpsList) {
            for (VM vm : vps.getVms()) {
                if (vm.getvCPUs() >= selectedRequest.getRequiredVCPUs() &&
                        vm.getRam().equals(selectedRequest.getRequiredRam()) &&
                        vm.getDisk().equals(selectedRequest.getRequiredDisk()) &&
                        "Running".equals(vm.getStatus())) {
                    matchingVM = vm;
                    break;
                }
            }
            if (matchingVM != null) break;
        }

        if (matchingVM != null) {
            // ส่งงานสำเร็จ
            Label successMessage = new Label("Work sent to customer using VM: " + matchingVM.getIp());
            messagesBox.getChildren().add(successMessage);
            requestManager.completeRequest(selectedRequest); // สมมติว่ามี method นี้ใน RequestManager
        } else {
            // ไม่พบ VM ที่ตรงสเปค
            Label errorMessage = new Label("No VM found matching the required specs!");
            messagesBox.getChildren().add(errorMessage);
        }
    }

    private void styleWindow() {
        setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0);");
    }
}
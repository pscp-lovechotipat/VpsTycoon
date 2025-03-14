package com.vpstycoon.ui.game.desktop.messenger.views;

import com.vpstycoon.game.manager.CustomerRequest;
import com.vpstycoon.ui.game.desktop.messenger.MessageType;
import com.vpstycoon.ui.game.desktop.messenger.models.ChatHistoryManager;
import com.vpstycoon.ui.game.desktop.messenger.models.ChatMessage;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.TextAlignment;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ChatAreaView extends VBox {
    private VBox messagesBox;
    private TextField messageInput;
    private Button sendButton;
    private Button assignVMButton;
    private Button archiveButton;
    private Circle customerAvatar;
    private Label customerNameLabel;
    private Label customerTypeLabel;
    private Label headerStatusLabel;
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final ChatHistoryManager chatHistoryManager;

    public ChatAreaView(ChatHistoryManager chatHistoryManager) {
        this.chatHistoryManager = chatHistoryManager;
        setPadding(new Insets(10));
        getStyleClass().add("chat-area");
        VBox.setVgrow(this, Priority.ALWAYS);

        // Chat Header
        HBox chatHeader = new HBox(10);
        chatHeader.setAlignment(Pos.CENTER_LEFT);
        chatHeader.setPadding(new Insets(0, 0, 10, 0));
        chatHeader.setStyle("-fx-border-color: transparent transparent #6a00ff transparent; -fx-border-width: 0 0 1 0;");

        customerAvatar = new Circle(20);
        customerAvatar.setFill(Color.rgb(100, 50, 200));

        VBox customerInfoBox = new VBox(3);
        customerNameLabel = new Label("Select a customer");
        customerNameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-family: 'Monospace', 'Courier New', monospace;");

        HBox customerTypeStatusBox = new HBox(10);
        customerTypeStatusBox.setAlignment(Pos.CENTER_LEFT);

        customerTypeLabel = new Label("Customer Type");
        customerTypeLabel.setStyle("-fx-text-fill: rgba(255, 255, 255, 0.7); -fx-font-size: 12px;");

        headerStatusLabel = new Label();
        headerStatusLabel.setStyle("-fx-font-size: 11px; -fx-padding: 2 8; -fx-background-radius: 3;");

        customerTypeStatusBox.getChildren().addAll(customerTypeLabel, headerStatusLabel);
        customerInfoBox.getChildren().addAll(customerNameLabel, customerTypeStatusBox);
        chatHeader.getChildren().addAll(customerAvatar, customerInfoBox);
        HBox.setHgrow(customerInfoBox, Priority.ALWAYS);

        // Messages Area
        ScrollPane messagesScroll = new ScrollPane();
        messagesScroll.setFitToWidth(true);
        messagesScroll.getStyleClass().add("messages-scroll");
        VBox.setVgrow(messagesScroll, Priority.ALWAYS);

        messagesBox = new VBox(10);
        messagesBox.setPadding(new Insets(10));
        messagesBox.getStyleClass().add("messages-box");
        messagesScroll.setContent(messagesBox);

        // Input Area
        HBox inputArea = new HBox(10);
        inputArea.setAlignment(Pos.CENTER);
        inputArea.getStyleClass().add("input-area");

        messageInput = new TextField();
        messageInput.setPromptText("Type a message...");
        messageInput.getStyleClass().add("message-input");
        HBox.setHgrow(messageInput, Priority.ALWAYS);

        sendButton = new Button("Send");
        sendButton.getStyleClass().add("cyber-button");

        assignVMButton = new Button("Assign VM");
        assignVMButton.getStyleClass().add("cyber-button");
        assignVMButton.setDisable(true);

        archiveButton = new Button("Archive");
        archiveButton.getStyleClass().addAll("cyber-button", "danger-button");
        archiveButton.setDisable(true);

        inputArea.getChildren().addAll(messageInput, sendButton, assignVMButton, archiveButton);

        getChildren().addAll(chatHeader, messagesScroll, inputArea);

        // Auto-scroll to bottom when new message is added
        messagesBox.heightProperty().addListener((obs, oldVal, newVal) -> messagesScroll.setVvalue(1.0));
    }

    public void updateChatHeader(CustomerRequest request) {
        if (request != null) {
            customerNameLabel.setText(request.getTitle());
            int nameHash = request.getName().hashCode();
            int r = Math.abs(nameHash % 100) + 100;
            int g = Math.abs((nameHash / 100) % 100);
            int b = Math.abs((nameHash / 10000) % 100) + 100;
            customerAvatar.setFill(Color.rgb(r, g, b));

            if (request.isActive()) {
                headerStatusLabel.setText("✓ VM Assigned");
                headerStatusLabel.setStyle("-fx-font-size: 11px; -fx-padding: 2 8; -fx-background-radius: 3; -fx-background-color: #2ecc71; -fx-text-fill: white;");
            } else if (request.isExpired()) {
                headerStatusLabel.setText("⏱ Contract Expired");
                headerStatusLabel.setStyle("-fx-font-size: 11px; -fx-padding: 2 8; -fx-background-radius: 3; -fx-background-color: #e74c3c; -fx-text-fill: white;");
            } else {
                headerStatusLabel.setText("⌛ Waiting for VM");
                headerStatusLabel.setStyle("-fx-font-size: 11px; -fx-padding: 2 8; -fx-background-radius: 3; -fx-background-color: #3498db; -fx-text-fill: white;");
            }
        } else {
            customerNameLabel.setText("Select a customer");
            customerAvatar.setFill(Color.rgb(100, 50, 200));
            headerStatusLabel.setText("");
        }
    }

    public void loadChatHistory(CustomerRequest request) {
        messagesBox.getChildren().clear(); // ล้าง UI เดิม
        if (request == null) return;

        List<ChatMessage> history = chatHistoryManager.getChatHistory(request);
        for (ChatMessage message : history) {
            switch (message.getType()) {
                case CUSTOMER:
                    addCustomerMessageFromHistory(request, message.getContent());
                    break;
                case USER:
                    addUserMessageFromHistory(message.getContent());
                    break;
                case SYSTEM:
                    if (message.getContent().startsWith("Starting VM provisioning...")) {
                        Map<String, Object> metadata = message.getMetadata();
                        if (metadata.containsKey("isProvisioning") && (boolean) metadata.get("isProvisioning")) {
                            long startTime = (long) metadata.get("startTime");
                            int provisioningDelay = (int) metadata.get("provisioningDelay");
                            long elapsedTime = System.currentTimeMillis() - startTime;
                            int remainingSeconds = (int) Math.max(0, provisioningDelay - (elapsedTime / 1000));

                            HBox progressContainer = new HBox();
                            progressContainer.setAlignment(Pos.CENTER);
                            progressContainer.setPadding(new Insets(10, 0, 10, 0));
                            progressContainer.getStyleClass().add("message-container");

                            VBox progressBox = new VBox(5);
                            progressBox.setAlignment(Pos.CENTER);
                            progressBox.setPadding(new Insets(10));
                            progressBox.setStyle("-fx-background-color: rgba(52, 152, 219, 0.2); -fx-padding: 10px; -fx-border-radius: 5px; -fx-background-radius: 5px;");

                            Label timeRemainingLabel = new Label("Starting VM provisioning in " + remainingSeconds + " seconds...");
                            timeRemainingLabel.setStyle("-fx-text-fill: white;");
                            ProgressBar progressBar = new ProgressBar((double) elapsedTime / (provisioningDelay * 1000));
                            progressBar.setPrefWidth(200);
                            progressBar.setStyle("-fx-accent: #3498db;");

                            progressBox.getChildren().addAll(timeRemainingLabel, progressBar);
                            progressContainer.getChildren().add(progressBox);

                            Platform.runLater(() -> messagesBox.getChildren().add(progressContainer));

                            // อัปเดตแบบ real-time
                            Timer timer = new Timer();
                            timer.scheduleAtFixedRate(new TimerTask() {
                                @Override
                                public void run() {
                                    long currentElapsed = System.currentTimeMillis() - startTime;
                                    int currentRemaining = (int) Math.max(0, provisioningDelay - (currentElapsed / 1000));
                                    double progressValue = (double) currentElapsed / (provisioningDelay * 1000);
                                    Platform.runLater(() -> {
                                        timeRemainingLabel.setText("Starting VM provisioning in " + currentRemaining + " seconds...");
                                        progressBar.setProgress(progressValue);
                                        if (currentRemaining <= 0) {
                                            timeRemainingLabel.setText("VM provisioning completed.");
                                            timer.cancel();
                                        }
                                    });
                                }
                            }, 0, 1000); // อัปเดตทุก 1 วินาที
                        } else {
                            addSystemMessageFromHistory(message.getContent());
                        }
                    } else {
                        addSystemMessageFromHistory(message.getContent());
                    }
                    break;
            }
        }
    }

    private void addCustomerMessageFromHistory(CustomerRequest request, String message) {
        HBox messageContainer = new HBox();
        messageContainer.setAlignment(Pos.CENTER_LEFT);
        messageContainer.setPadding(new Insets(5, 0, 5, 0));
        messageContainer.getStyleClass().add("message-container");

        Circle avatar = new Circle(15);
        int nameHash = request.getName().hashCode();
        int r = Math.abs(nameHash % 100) + 100;
        int g = Math.abs((nameHash / 100) % 100);
        int b = Math.abs((nameHash / 10000) % 100) + 100;
        avatar.setFill(Color.rgb(r, g, b));

        VBox messageBox = new VBox(3);
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.getStyleClass().add("customer-message");
        messageLabel.setMinHeight(Region.USE_PREF_SIZE);

        Label timeLabel = new Label(LocalTime.now().format(timeFormatter));
        timeLabel.setStyle("-fx-text-fill: rgba(255, 255, 255, 0.6); -fx-font-size: 10px;");
        timeLabel.setPadding(new Insets(0, 0, 0, 5));

        messageBox.getChildren().addAll(messageLabel, timeLabel);
        messageContainer.getChildren().addAll(avatar, messageBox);
        HBox.setMargin(messageBox, new Insets(0, 0, 0, 10));

        Platform.runLater(() -> messagesBox.getChildren().add(messageContainer));
    }

    private void addUserMessageFromHistory(String message) {
        HBox messageContainer = new HBox();
        messageContainer.setAlignment(Pos.CENTER_RIGHT);
        messageContainer.setPadding(new Insets(5, 0, 5, 0));
        messageContainer.getStyleClass().add("message-container");

        VBox messageBox = new VBox(3);
        messageBox.setAlignment(Pos.CENTER_RIGHT);

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.getStyleClass().add("user-message");
        messageLabel.setTextAlignment(TextAlignment.RIGHT);
        messageLabel.setMinHeight(Region.USE_PREF_SIZE);

        Label timeLabel = new Label(LocalTime.now().format(timeFormatter));
        timeLabel.setStyle("-fx-text-fill: rgba(255, 255, 255, 0.6); -fx-font-size: 10px;");
        timeLabel.setPadding(new Insets(0, 5, 0, 0));

        messageBox.getChildren().addAll(messageLabel, timeLabel);
        messageContainer.getChildren().add(messageBox);

        Platform.runLater(() -> messagesBox.getChildren().add(messageContainer));
    }

    private void addSystemMessageFromHistory(String message) {
        HBox messageContainer = new HBox();
        messageContainer.setAlignment(Pos.CENTER);
        messageContainer.setPadding(new Insets(10, 0, 10, 0));
        messageContainer.getStyleClass().add("message-container");

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.getStyleClass().add("system-message");
        messageLabel.setTextAlignment(TextAlignment.CENTER);
        messageLabel.setMinHeight(Region.USE_PREF_SIZE);

        messageContainer.getChildren().add(messageLabel);
        Platform.runLater(() -> messagesBox.getChildren().add(messageContainer));
    }

    public void addCustomerMessage(CustomerRequest request, String message) {
        addCustomerMessageFromHistory(request, message);
    }

    public void addUserMessage(String message) {
        addUserMessageFromHistory(message);
    }

    public void addSystemMessage(String message) {
        addSystemMessageFromHistory(message);
    }

    public void clearMessages() {
        messagesBox.getChildren().clear();
    }

    // Getters
    public Button getSendButton() { return sendButton; }
    public Button getAssignVMButton() { return assignVMButton; }
    public Button getArchiveButton() { return archiveButton; }
    public TextField getMessageInput() { return messageInput; }
    public VBox getMessagesBox() { return messagesBox; }
}
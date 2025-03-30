package com.vpstycoon.ui.game.desktop.messenger.views;

import com.vpstycoon.game.manager.CustomerRequest;
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
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import javafx.scene.effect.Glow;

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
    private Rectangle customerAvatar;
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

        
        HBox chatHeader = new HBox(10);
        chatHeader.setAlignment(Pos.CENTER_LEFT);
        chatHeader.setPadding(new Insets(0, 0, 10, 0));
        chatHeader.setStyle("-fx-border-color: transparent transparent #9e33ff transparent; -fx-border-width: 0 0 1 0;");

        
        Rectangle customerAvatar = new Rectangle(40, 40);
        customerAvatar.setArcWidth(5);
        customerAvatar.setArcHeight(5);
        customerAvatar.setFill(Color.rgb(100, 50, 200));
        customerAvatar.setStroke(Color.web("#00c3ff"));
        customerAvatar.setStrokeWidth(1.5);
        
        
        Glow avatarGlow = new Glow(0.5);
        customerAvatar.setEffect(avatarGlow);
        
        
        this.customerAvatar = null; 

        VBox customerInfoBox = new VBox(3);
        customerNameLabel = new Label("SELECT CLIENT");
        customerNameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #E0FFFF; -fx-font-size: 16px; -fx-font-family: 'Monospace', 'Courier New', monospace;");

        HBox customerTypeStatusBox = new HBox(10);
        customerTypeStatusBox.setAlignment(Pos.CENTER_LEFT);

        customerTypeLabel = new Label("[SYS] CONNECTION READY");
        customerTypeLabel.setStyle("-fx-text-fill: #00ffff; -fx-font-size: 12px; -fx-font-family: 'Monospace', 'Courier New', monospace;");

        headerStatusLabel = new Label();
        headerStatusLabel.setStyle("-fx-font-size: 11px; -fx-padding: 2 8; -fx-background-radius: 3;");

        customerTypeStatusBox.getChildren().addAll(customerTypeLabel, headerStatusLabel);
        customerInfoBox.getChildren().addAll(customerNameLabel, customerTypeStatusBox);
        chatHeader.getChildren().addAll(customerAvatar, customerInfoBox);
        HBox.setHgrow(customerInfoBox, Priority.ALWAYS);

        
        HBox statusArea = new HBox(10);
        statusArea.setAlignment(Pos.CENTER_RIGHT);
        
        Rectangle signalIcon = new Rectangle(16, 16);
        signalIcon.setArcWidth(2);
        signalIcon.setArcHeight(2);
        signalIcon.setFill(Color.web("#00ffff"));
        
        Label statusInfoLabel = new Label("PING: 14ms | ENCRYPTION: ACTIVE");
        statusInfoLabel.setStyle("-fx-text-fill: #00ffff; -fx-font-size: 10px; -fx-font-family: 'Monospace', 'Courier New', monospace;");
        
        statusArea.getChildren().addAll(statusInfoLabel, signalIcon);
        chatHeader.getChildren().add(statusArea);

        
        ScrollPane messagesScroll = new ScrollPane();
        messagesScroll.setFitToWidth(true);
        messagesScroll.getStyleClass().add("messages-scroll");
        VBox.setVgrow(messagesScroll, Priority.ALWAYS);

        
        String digitalBorderStyle = "-fx-border-color: #9e33ff; -fx-border-width: 1; " +
                                    "-fx-border-radius: 5; -fx-effect: dropshadow(gaussian, #9e33ff, 5, 0.3, 0, 0);";
        messagesScroll.setStyle(messagesScroll.getStyle() + digitalBorderStyle);

        messagesBox = new VBox(10);
        messagesBox.setPadding(new Insets(10));
        messagesBox.getStyleClass().add("messages-box");
        messagesScroll.setContent(messagesBox);

        
        HBox inputArea = new HBox(10);
        inputArea.setAlignment(Pos.CENTER);
        inputArea.getStyleClass().add("input-area");

        messageInput = new TextField();
        messageInput.setPromptText("| TYPE YOUR MESSAGE HERE... |");
        messageInput.getStyleClass().add("message-input");
        HBox.setHgrow(messageInput, Priority.ALWAYS);

        
        sendButton = new Button("SEND >");
        sendButton.getStyleClass().add("cyber-button");

        assignVMButton = new Button("ASSIGN VM");
        assignVMButton.getStyleClass().add("cyber-button");
        assignVMButton.setDisable(true);

        archiveButton = new Button("ARCHIVE");
        archiveButton.getStyleClass().addAll("cyber-button", "danger-button");
        archiveButton.setDisable(true);

        inputArea.getChildren().addAll(messageInput, sendButton, assignVMButton, archiveButton);

        
        inputArea.setStyle(inputArea.getStyle() + digitalBorderStyle);

        getChildren().addAll(chatHeader, messagesScroll, inputArea);

        
        messagesBox.heightProperty().addListener((obs, oldVal, newVal) -> messagesScroll.setVvalue(1.0));
    }

    public void updateChatHeader(CustomerRequest request) {
        if (request != null) {
            customerNameLabel.setText(request.getTitle().toUpperCase());
            int nameHash = request.getName().hashCode();
            int r = Math.abs(nameHash % 100) + 100;
            int g = Math.abs((nameHash / 100) % 100);
            int b = Math.abs((nameHash / 10000) % 100) + 100;
            
            
            HBox chatHeader = (HBox) getChildren().get(0);
            Rectangle customerAvatar = (Rectangle) chatHeader.getChildren().get(0);
            customerAvatar.setFill(Color.rgb(r, g, b));

            
            customerTypeLabel.setText("[CLIENT:" + request.getName().toUpperCase() + "]");

            if (request.isActive()) {
                headerStatusLabel.setText("✓ VM ASSIGNED");
                headerStatusLabel.setStyle("-fx-font-size: 11px; -fx-padding: 2 8; -fx-background-radius: 3; -fx-background-color: #2ecc71; -fx-text-fill: white;");
            } else if (request.isExpired()) {
                headerStatusLabel.setText("⏱ CONTRACT EXPIRED");
                headerStatusLabel.setStyle("-fx-font-size: 11px; -fx-padding: 2 8; -fx-background-radius: 3; -fx-background-color: #e74c3c; -fx-text-fill: white;");
            } else {
                headerStatusLabel.setText("⌛ WAITING FOR VM");
                headerStatusLabel.setStyle("-fx-font-size: 11px; -fx-padding: 2 8; -fx-background-radius: 3; -fx-background-color: #3498db; -fx-text-fill: white;");
            }
        } else {
            customerNameLabel.setText("SELECT CLIENT");
            
            
            HBox chatHeader = (HBox) getChildren().get(0);
            Rectangle customerAvatar = (Rectangle) chatHeader.getChildren().get(0);
            customerAvatar.setFill(Color.rgb(100, 50, 200));
            
            customerTypeLabel.setText("[SYS] CONNECTION READY");
            headerStatusLabel.setText("");
        }
    }

    public void loadChatHistory(CustomerRequest request) {
        messagesBox.getChildren().clear(); 
        if (request == null) return;

        List<ChatMessage> history = chatHistoryManager.getChatHistory(request);
        if (history.isEmpty()) {
            System.out.println("ไม่พบประวัติแชทสำหรับ: " + request.getName());
            return;
        }
        
        System.out.println("โหลดประวัติแชทสำหรับ: " + request.getName() + " จำนวน " + history.size() + " ข้อความ");
        
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
                            }, 0, 1000); 
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

        
        Rectangle avatar = new Rectangle(30, 30);
        avatar.setArcWidth(5);
        avatar.setArcHeight(5);
        
        int nameHash = request.getName().hashCode();
        int r = Math.abs(nameHash % 100) + 100;
        int g = Math.abs((nameHash / 100) % 100);
        int b = Math.abs((nameHash / 10000) % 100) + 100;
        Color customerColor = Color.rgb(r, g, b);
        avatar.setFill(customerColor);
        avatar.setStroke(Color.web("#9e33ff"));
        avatar.setStrokeWidth(1);

        VBox messageBox = new VBox(3);
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.getStyleClass().add("customer-message");
        messageLabel.setMinHeight(Region.USE_PREF_SIZE);

        
        Label timeLabel = new Label("[" + LocalTime.now().format(timeFormatter) + "]");
        timeLabel.setStyle("-fx-text-fill: rgba(0, 255, 255, 0.8); -fx-font-size: 10px; -fx-font-family: 'Monospace', 'Courier New', monospace;");
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
        messageLabel.setMinHeight(Region.USE_PREF_SIZE);

        
        Label timeLabel = new Label("[" + LocalTime.now().format(timeFormatter) + "]");
        timeLabel.setAlignment(Pos.CENTER_RIGHT);
        timeLabel.setStyle("-fx-text-fill: rgba(0, 255, 255, 0.8); -fx-font-size: 10px; -fx-font-family: 'Monospace', 'Courier New', monospace;");
        timeLabel.setPadding(new Insets(0, 5, 0, 0));

        messageBox.getChildren().addAll(messageLabel, timeLabel);
        
        
        Rectangle avatar = new Rectangle(30, 30);
        avatar.setArcWidth(5);
        avatar.setArcHeight(5);
        avatar.setFill(Color.web("#9e33ff"));
        avatar.setStroke(Color.web("#00c3ff"));
        avatar.setStrokeWidth(1);

        messageContainer.getChildren().addAll(messageBox, avatar);
        HBox.setMargin(messageBox, new Insets(0, 10, 0, 0));

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

    
    public Button getSendButton() { return sendButton; }
    public Button getAssignVMButton() { return assignVMButton; }
    public Button getArchiveButton() { return archiveButton; }
    public TextField getMessageInput() { return messageInput; }
    public VBox getMessagesBox() { return messagesBox; }
}

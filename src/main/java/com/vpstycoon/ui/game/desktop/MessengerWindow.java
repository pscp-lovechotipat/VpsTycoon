package com.vpstycoon.ui.game.desktop;

import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.manager.CustomerRequest;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.game.manager.VPSManager;
import com.vpstycoon.game.vps.VPSOptimization;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MessengerWindow extends VBox {
    private final RequestManager requestManager;
    private final Runnable onClose;
    private final ListView<CustomerRequest> requestView;
    private final VPSManager vpsManager;
    private final Company company;
    private VBox messagesBox;
    private final Map<VPSOptimization.VM, CustomerRequest> vmAssignments;
    private final Map<CustomerRequest, java.util.List<javafx.scene.Node>> customerChatHistory;
    private Label ratingLabel; // For dashboard
    private Label activeRequestsLabel; // For dashboard
    private Label availableVMsLabel; // For dashboard
    private Label totalVPSLabel; // For dashboard
    private Label customerNameLabel; // For chat header
    private Circle customerAvatar; // For chat header
    private Label customerTypeLabel; // For chat header
    private final Random random = new Random();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public MessengerWindow(RequestManager requestManager, VPSManager vpsManager,
                           Company company, Runnable onClose) {
        this.requestManager = requestManager;
        this.vpsManager = vpsManager;
        this.company = company;
        this.onClose = onClose;
        this.requestView = new ListView<>();
        this.vmAssignments = new HashMap<>();
        this.customerChatHistory = new HashMap<>();

        getStylesheets().add(getClass().getResource("/css/messenger-window.css").toExternalForm());
        getStyleClass().add("messenger-window");
        
        setupUI();
        setupListeners();
    }

    private void setupUI() {
        setPrefSize(900, 650);

        HBox titleBar = createTitleBar();

        HBox content = new HBox();
        content.setPadding(new Insets(0));
        VBox.setVgrow(content, Priority.ALWAYS);

        VBox requestList = createRequestList();
        VBox rightPane = new VBox(10);
        rightPane.setPadding(new Insets(10));
        HBox.setHgrow(rightPane, Priority.ALWAYS);

        VBox dashboard = createDashboard();
        VBox chatArea = createChatArea();
        VBox.setVgrow(chatArea, Priority.ALWAYS);

        rightPane.getChildren().addAll(dashboard, chatArea);
        content.getChildren().addAll(requestList, rightPane);

        getChildren().addAll(titleBar, content);

        updateRequestList();
        updateDashboard(); // Initial dashboard update
    }

    private HBox createTitleBar() {
        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(10, 15, 10, 15));
        titleBar.getStyleClass().add("title-bar");
        
        // Title with icon
        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        
        // Create a circle for the messenger icon
        Circle messengerIcon = new Circle(12);
        messengerIcon.setFill(Color.WHITE);
        
        Label titleLabel = new Label("VPS Tycoon Messenger");
        titleLabel.getStyleClass().add("title-text");
        
        titleBox.getChildren().addAll(messengerIcon, titleLabel);
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        
        // Close button
        Button closeButton = new Button("×");
        closeButton.getStyleClass().add("close-button");
        closeButton.setOnAction(e -> onClose.run());
        
        titleBar.getChildren().addAll(titleBox, closeButton);
        return titleBar;
    }

    private VBox createRequestList() {
        VBox requestList = new VBox(10);
        requestList.setPadding(new Insets(10));
        requestList.setPrefWidth(350);
        requestList.getStyleClass().add("request-list");

        Label title = new Label("Customer Requests");
        title.getStyleClass().add("request-list-title");

        // Custom cell factory for the request list
        requestView.setCellFactory(param -> new ListCell<CustomerRequest>() {
            @Override
            protected void updateItem(CustomerRequest request, boolean empty) {
                super.updateItem(request, empty);
                if (empty || request == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox cellContent = new HBox(10);
                    cellContent.setAlignment(Pos.CENTER_LEFT);
                    
                    // Customer avatar (circle)
                    Circle avatar = new Circle(15);
                    // สร้างสีที่คงที่สำหรับลูกค้านี้ตามชื่อ
                    int nameHash = request.getName().hashCode();
                    int r = Math.abs(nameHash % 100) + 100;
                    int g = Math.abs((nameHash / 100) % 100);
                    int b = Math.abs((nameHash / 10000) % 100) + 100;
                    Color customerColor = Color.rgb(r, g, b);
                    avatar.setFill(customerColor);
                    
                    VBox textContent = new VBox(3);
                    
                    // เพิ่ม HBox สำหรับชื่อและสถานะ
                    HBox nameStatusBox = new HBox(5);
                    nameStatusBox.setAlignment(Pos.CENTER_LEFT);
                    
                    Label nameLabel = new Label(request.getTitle());
                    nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-family: 'Monospace', 'Courier New', monospace;");
                    
                    // เพิ่ม Label สำหรับแสดงสถานะ
                    Label statusLabel = new Label();
                    statusLabel.setStyle("-fx-font-size: 10px; -fx-padding: 2 5; -fx-background-radius: 3;");
                    
                    boolean isAssigned = request.isActive();
                    if (isAssigned) {
                        statusLabel.setText("✓ Assigned");
                        statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #2ecc71; -fx-text-fill: white;");
                    } else {
                        statusLabel.setText("⌛ Waiting");
                        statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #3498db; -fx-text-fill: white;");
                    }
                    
                    nameStatusBox.getChildren().addAll(nameLabel, statusLabel);
                    
                    Label previewLabel = new Label("Needs VM: " + request.getRequiredVCPUs() + " vCPUs, " + 
                                                  request.getRequiredRam() + " RAM");
                    previewLabel.setStyle("-fx-text-fill: rgba(255, 255, 255, 0.7); -fx-font-size: 12px; -fx-font-family: 'Monospace', 'Courier New', monospace;");
                    
                    textContent.getChildren().addAll(nameStatusBox, previewLabel);
                    
                    // Status indicator
                    Circle statusIndicator = new Circle(5);
                    statusIndicator.setFill(request.isActive() ? 
                                           Color.rgb(0, 255, 128) : Color.rgb(0, 200, 255));
                    
                    HBox.setMargin(statusIndicator, new Insets(0, 0, 0, 5));
                    
                    cellContent.getChildren().addAll(avatar, textContent, statusIndicator);
                    HBox.setHgrow(textContent, Priority.ALWAYS);
                    
                    setGraphic(cellContent);
                    setText(null);
                }
            }
        });
        
        requestView.getStyleClass().add("request-list-view");

        Button acceptButton = new Button("Accept Request");
        acceptButton.getStyleClass().add("cyber-button");
        acceptButton.setOnAction(e -> {
            CustomerRequest selected = requestView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                // Check if this customer already has a VM assigned
                boolean alreadyAssigned = selected.isActive();
                if (alreadyAssigned) {
                    // Show a message that this customer already has a VM
                    addSystemMessage(selected, "This customer already has a VM assigned.");
                    return;
                }
                
                // Show the simplified VM selection popup
                showVMSelectionPopup();
                
                // Add a system message to the chat
                addSystemMessage(selected, "You accepted the request from " + selected.getName());
            }
        });
        
        // Make the button take full width
        acceptButton.setMaxWidth(Double.MAX_VALUE);

        requestList.getChildren().addAll(title, requestView, acceptButton);
        VBox.setVgrow(requestView, Priority.ALWAYS);
        
        return requestList;
    }

    private VBox createDashboard() {
        VBox dashboard = new VBox(10);
        dashboard.setPadding(new Insets(10));
        dashboard.getStyleClass().add("dashboard");
        dashboard.setMaxHeight(120);

        Label title = new Label("SYSTEM DASHBOARD");
        title.getStyleClass().add("dashboard-title");

        // Create a grid for dashboard cards with 4 columns
        GridPane dashboardGrid = new GridPane();
        dashboardGrid.getStyleClass().add("dashboard-grid");
        dashboardGrid.setAlignment(Pos.CENTER);

        // Company Rating Card
        VBox ratingCard = new VBox(5);
        ratingCard.getStyleClass().add("dashboard-card");
        ratingCard.setAlignment(Pos.CENTER);
        
        ratingLabel = new Label(String.format("%.1f", company.getRating()));
        ratingLabel.getStyleClass().add("dashboard-value");
        
        // Add color class based on rating
        if (company.getRating() >= 4.0) {
            ratingLabel.getStyleClass().add("rating-high");
        } else if (company.getRating() >= 3.0) {
            ratingLabel.getStyleClass().add("rating-medium");
        } else {
            ratingLabel.getStyleClass().add("rating-low");
        }
        
        Label ratingDescLabel = new Label("RATING");
        ratingDescLabel.getStyleClass().add("dashboard-label");
        
        ratingCard.getChildren().addAll(ratingLabel, ratingDescLabel);
        
        // Active Requests Card
        VBox requestsCard = new VBox(5);
        requestsCard.getStyleClass().add("dashboard-card");
        requestsCard.setAlignment(Pos.CENTER);
        
        activeRequestsLabel = new Label(String.valueOf(requestManager.getRequests().size()));
        activeRequestsLabel.getStyleClass().add("dashboard-value");
        
        Label requestsDescLabel = new Label("REQUESTS");
        requestsDescLabel.getStyleClass().add("dashboard-label");
        
        requestsCard.getChildren().addAll(activeRequestsLabel, requestsDescLabel);
        
        // Available VMs Card
        VBox availableVMsCard = new VBox(5);
        availableVMsCard.getStyleClass().add("dashboard-card");
        availableVMsCard.setAlignment(Pos.CENTER);
        
        availableVMsLabel = new Label("0"); // Placeholder, updated later
        availableVMsLabel.getStyleClass().add("dashboard-value");
        
        Label availableVMsDescLabel = new Label("FREE VMs");
        availableVMsDescLabel.getStyleClass().add("dashboard-label");
        
        availableVMsCard.getChildren().addAll(availableVMsLabel, availableVMsDescLabel);
        
        // Total VPS Card
        VBox totalVPSCard = new VBox(5);
        totalVPSCard.getStyleClass().add("dashboard-card");
        totalVPSCard.setAlignment(Pos.CENTER);
        
        totalVPSLabel = new Label(String.valueOf(vpsManager.getVPSMap().size()));
        totalVPSLabel.getStyleClass().add("dashboard-value");
        
        Label totalVPSDescLabel = new Label("TOTAL VPS");
        totalVPSDescLabel.getStyleClass().add("dashboard-label");
        
        totalVPSCard.getChildren().addAll(totalVPSLabel, totalVPSDescLabel);
        
        // Add cards to grid - all in a single row with 4 columns
        dashboardGrid.add(ratingCard, 0, 0);
        dashboardGrid.add(requestsCard, 1, 0);
        dashboardGrid.add(availableVMsCard, 2, 0);
        dashboardGrid.add(totalVPSCard, 3, 0);
        
        // Set column constraints for 4 equal columns
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(25);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(25);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(25);
        ColumnConstraints col4 = new ColumnConstraints();
        col4.setPercentWidth(25);
        dashboardGrid.getColumnConstraints().addAll(col1, col2, col3, col4);

        dashboard.getChildren().addAll(title, dashboardGrid);
        return dashboard;
    }

    private void updateDashboard() {
        ratingLabel.setText(String.format("%.1f", company.getRating()));
        
        // Update rating color
        ratingLabel.getStyleClass().removeAll("rating-high", "rating-medium", "rating-low");
        if (company.getRating() >= 4.0) {
            ratingLabel.getStyleClass().add("rating-high");
        } else if (company.getRating() >= 3.0) {
            ratingLabel.getStyleClass().add("rating-medium");
        } else {
            ratingLabel.getStyleClass().add("rating-low");
        }
        
        activeRequestsLabel.setText(String.valueOf(requestManager.getRequests().size()));

        int availableVMs = 0;
        for (VPSOptimization vps : vpsManager.getVPSMap().values()) {
            availableVMs += vps.getVms().stream()
                    .filter(vm -> "Running".equals(vm.getStatus()) && !vmAssignments.containsKey(vm))
                    .count();
        }
        availableVMsLabel.setText(String.valueOf(availableVMs));

        totalVPSLabel.setText(String.valueOf(vpsManager.getVPSMap().size()));
    }

    private void setupListeners() {
        requestManager.getRequests().addListener((ListChangeListener<CustomerRequest>) change -> {
            while (change.next()) {
                if (change.wasAdded() || change.wasRemoved()) {
                    Platform.runLater(() -> {
                        updateRequestList();
                        updateDashboard();
                    });
                }
            }
        });
    }

    private void updateRequestList() {
        requestView.getItems().clear();
        requestView.getItems().addAll(requestManager.getRequests());
    }

    private VBox createChatArea() {
        VBox chatArea = new VBox(10);
        chatArea.setPadding(new Insets(10));
        chatArea.getStyleClass().add("chat-area");
        VBox.setVgrow(chatArea, Priority.ALWAYS);

        // Chat header with customer info
        HBox chatHeader = new HBox(10);
        chatHeader.setAlignment(Pos.CENTER_LEFT);
        chatHeader.setPadding(new Insets(0, 0, 10, 0));
        chatHeader.setStyle("-fx-border-color: transparent transparent #6a00ff transparent; -fx-border-width: 0 0 1 0;");
        
        customerAvatar = new Circle(20);
        customerAvatar.setFill(Color.rgb(100, 50, 200));
        
        VBox customerInfoBox = new VBox(3);
        customerNameLabel = new Label("Select a customer");
        customerNameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-family: 'Monospace', 'Courier New', monospace;");
        
        // เพิ่ม Label สำหรับแสดงประเภทลูกค้าและสถานะ
        HBox customerTypeStatusBox = new HBox(10);
        customerTypeStatusBox.setAlignment(Pos.CENTER_LEFT);
        
        customerTypeLabel = new Label("Customer Type");
        customerTypeLabel.setStyle("-fx-text-fill: rgba(255, 255, 255, 0.7); -fx-font-size: 12px;");
        
        // เพิ่ม Label สำหรับแสดงสถานะในส่วนหัวแชท
        Label headerStatusLabel = new Label();
        headerStatusLabel.setStyle("-fx-font-size: 11px; -fx-padding: 2 8; -fx-background-radius: 3;");
        
        customerTypeStatusBox.getChildren().addAll(customerTypeLabel, headerStatusLabel);
        
        customerInfoBox.getChildren().addAll(customerNameLabel, customerTypeStatusBox);
        
        chatHeader.getChildren().addAll(customerAvatar, customerInfoBox);
        HBox.setHgrow(customerInfoBox, Priority.ALWAYS);

        // อัปเดตสถานะในส่วนหัวแชทเมื่อเลือกลูกค้า
        requestView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                boolean isAssigned = newVal.isActive();
                if (isAssigned) {
                    headerStatusLabel.setText("✓ VM Assigned");
                    headerStatusLabel.setStyle(headerStatusLabel.getStyle() + "-fx-background-color: #2ecc71; -fx-text-fill: white;");
                } else {
                    headerStatusLabel.setText("⌛ Waiting for VM");
                    headerStatusLabel.setStyle(headerStatusLabel.getStyle() + "-fx-background-color: #3498db; -fx-text-fill: white;");
                }
            } else {
                headerStatusLabel.setText("");
            }
        });

        // Messages area
        ScrollPane messagesScroll = new ScrollPane();
        messagesScroll.setFitToWidth(true);
        messagesScroll.getStyleClass().add("messages-scroll");
        VBox.setVgrow(messagesScroll, Priority.ALWAYS);

        messagesBox = new VBox(10);
        messagesBox.setPadding(new Insets(10));
        messagesBox.getStyleClass().add("messages-box");
        messagesScroll.setContent(messagesBox);

        // Input area
        HBox inputArea = new HBox(10);
        inputArea.setAlignment(Pos.CENTER);
        inputArea.getStyleClass().add("input-area");

        TextField messageInput = new TextField();
        messageInput.setPromptText("Type a message...");
        messageInput.getStyleClass().add("message-input");
        HBox.setHgrow(messageInput, Priority.ALWAYS);

        Button sendButton = new Button("Send");
        sendButton.getStyleClass().add("cyber-button");
        sendButton.setOnAction(e -> {
            String message = messageInput.getText();
            if (!message.isEmpty()) {
                CustomerRequest selected = requestView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    addUserMessage(selected, message);
                    messageInput.clear();
                    
                    // Simulate customer response after a short delay
                    if (!selected.isActive()) {
                        new Thread(() -> {
                            try {
                                Thread.sleep(1000);
                                Platform.runLater(() -> {
                                    addCustomerMessage(selected, "Thanks for your response! Can you help me with my VM request?");
                                });
                            } catch (InterruptedException ex) {
                                Thread.currentThread().interrupt();
                            }
                        }).start();
                    } else {
                        // If the customer already has a VM, give a different response
                        new Thread(() -> {
                            try {
                                Thread.sleep(1000);
                                Platform.runLater(() -> {
                                    addCustomerMessage(selected, "Thank you for checking in! The VM is working great. Let me know if there's anything else I need to do.");
                                });
                            } catch (InterruptedException ex) {
                                Thread.currentThread().interrupt();
                            }
                        }).start();
                    }
                }
            }
        });

        Button sendWorkButton = new Button("Assign VM");
        sendWorkButton.getStyleClass().add("cyber-button");
        sendWorkButton.setDisable(true);
        sendWorkButton.setOnAction(e -> showVMSelectionPopup());

        Button archiveButton = new Button("Archive");
        archiveButton.getStyleClass().addAll("cyber-button", "danger-button");
        archiveButton.setDisable(true);
        archiveButton.setOnAction(e -> {
            CustomerRequest selected = requestView.getSelectionModel().getSelectedItem();
            if (selected != null && selected.isActive()) {
                // We don't remove the customer from the list anymore
                // Instead, we just mark them as inactive or archive them
                
                // Find the assigned VM
                VPSOptimization.VM assignedVM = vmAssignments.entrySet().stream()
                        .filter(entry -> entry.getValue() == selected)
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElse(null);
                
                // Add a system message about archiving
                addSystemMessage(selected, "Customer " + selected.getTitle() + " has been archived.");
                
                // Update chat header
                customerNameLabel.setText("Select a customer");
                customerAvatar.setFill(Color.rgb(100, 50, 200));
                
                // Clear the messages box but keep the history
                messagesBox.getChildren().clear();
            }
        });

        requestView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            sendWorkButton.setDisable(newVal == null || newVal.isActive());
            archiveButton.setDisable(newVal == null || !newVal.isActive());
            
            if (newVal != null) {
                // Update chat header with customer info
                customerNameLabel.setText(newVal.getTitle());
                
                // Generate a consistent color for this customer based on their name
                int nameHash = newVal.getName().hashCode();
                int r = Math.abs(nameHash % 100) + 100;
                int g = Math.abs((nameHash / 100) % 100);
                int b = Math.abs((nameHash / 10000) % 100) + 100;
                Color customerColor = Color.rgb(r, g, b);
                customerAvatar.setFill(customerColor);
                
                updateChatWithRequestDetails(newVal);
            }
        });

        inputArea.getChildren().addAll(messageInput, sendButton, sendWorkButton, archiveButton);
        chatArea.getChildren().addAll(chatHeader, messagesScroll, inputArea);
        
        // Auto-scroll to bottom when new messages are added
        messagesBox.heightProperty().addListener((obs, oldVal, newVal) -> 
            messagesScroll.setVvalue(1.0));
            
        return chatArea;
    }

    private boolean isRequestCompleted(CustomerRequest request) {
        return request.isActive();
    }

    private void updateChatWithRequestDetails(CustomerRequest selectedRequest) {
        // ล้างข้อความในช่องแชทก่อนแสดงประวัติใหม่
        messagesBox.getChildren().clear();
        
        if (selectedRequest != null) {
            // ตรวจสอบว่ามีประวัติแชทสำหรับลูกค้านี้หรือไม่
            if (customerChatHistory.containsKey(selectedRequest)) {
                // แสดงประวัติแชทที่มีอยู่
                messagesBox.getChildren().addAll(customerChatHistory.get(selectedRequest));
            } else {
                // สร้างข้อความเริ่มต้นสำหรับลูกค้าใหม่
                String requestMessage = "Hello! I need a VM with the following specs:\n" +
                        "• " + selectedRequest.getRequiredVCPUs() + " vCPUs\n" +
                        "• " + selectedRequest.getRequiredRam() + " RAM\n" +
                        "• " + selectedRequest.getRequiredDisk() + " Disk\n\n" +
                        "Can you help me set this up?";
                
                addCustomerMessage(selectedRequest, requestMessage);
                
                // ตรวจสอบว่าคำขอนี้เสร็จสมบูรณ์แล้วหรือไม่
                if (selectedRequest.isActive()) {
                    VPSOptimization.VM assignedVM = vmAssignments.entrySet().stream()
                            .filter(entry -> entry.getValue() == selectedRequest)
                            .map(Map.Entry::getKey)
                            .findFirst()
                            .orElse(null);
                    
                    if (assignedVM != null) {
                        addUserMessage(selectedRequest, "I've assigned you a VM with IP: " + assignedVM.getIp());
                        addCustomerMessage(selectedRequest, "Thank you! This works perfectly for my needs.");
                        
                        addSystemMessage(selectedRequest, "VM assignment completed successfully" +
                                (assignedVM != null ? " (VM: " + assignedVM.getIp() + ")" : ""));
                    }
                }
            }
        }
    }
    
    private void addCustomerMessage(CustomerRequest request, String message) {
        HBox messageContainer = new HBox();
        messageContainer.setAlignment(Pos.CENTER_LEFT);
        messageContainer.setPadding(new Insets(5, 0, 5, 0));
        messageContainer.getStyleClass().add("message-container");
        
        // Customer avatar
        Circle avatar = new Circle(15);
        if (request != null) {
            // Generate a consistent color for this customer based on their name
            int nameHash = request.getName().hashCode();
            int r = Math.abs(nameHash % 100) + 100;
            int g = Math.abs((nameHash / 100) % 100);
            int b = Math.abs((nameHash / 10000) % 100) + 100;
            Color customerColor = Color.rgb(r, g, b);
            avatar.setFill(customerColor);
        } else {
            avatar.setFill(Color.rgb(100, 50, 200));
        }
        
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
        
        messagesBox.getChildren().add(messageContainer);
        
        // Store in chat history
        if (request != null) {
            customerChatHistory.computeIfAbsent(request, k -> new java.util.ArrayList<>()).add(messageContainer);
        }
    }
    
    private void addUserMessage(CustomerRequest request, String message) {
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
        
        messagesBox.getChildren().add(messageContainer);
        
        // Store in chat history
        if (request != null) {
            customerChatHistory.computeIfAbsent(request, k -> new java.util.ArrayList<>()).add(messageContainer);
        }
    }
    
    private void addSystemMessage(CustomerRequest request, String message) {
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
        messagesBox.getChildren().add(messageContainer);
        
        // Store in chat history
        if (request != null) {
            customerChatHistory.computeIfAbsent(request, k -> new java.util.ArrayList<>()).add(messageContainer);
        }
    }

    private void showVMSelectionPopup() {
        CustomerRequest selected = requestView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        // ตรวจสอบว่าลูกค้านี้มี VM ที่กำหนดไว้แล้วหรือไม่
        boolean alreadyAssigned = selected.isActive();
        if (alreadyAssigned) {
            // แสดงข้อความว่าลูกค้านี้มี VM แล้ว
            addSystemMessage(selected, "This customer already has a VM assigned.");
            return;
        }
        
        // สร้างหน้าต่าง popup แบบ Stage แทนการใช้ modal overlay
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Assign VM to " + selected.getName());
        popupStage.initOwner(getScene().getWindow());
        
        // สร้าง UI สำหรับ popup
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #2c3e50;");
        content.setMinWidth(400);
        content.setMinHeight(350);
        
        // Title
        Label titleLabel = new Label("Assign VM to " + selected.getName());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        // Requirements info
        VBox requirementsBox = new VBox(5);
        requirementsBox.setStyle("-fx-background-color: rgba(52, 152, 219, 0.2); -fx-padding: 10px; -fx-border-radius: 5px; -fx-background-radius: 5px;");
        
        Label requirementsTitle = new Label("Customer Requirements:");
        requirementsTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #3498db;");
        
        Label vcpusReq = new Label("• vCPUs: " + selected.getRequiredVCPUs());
        vcpusReq.setStyle("-fx-text-fill: white;");
        
        Label ramReq = new Label("• RAM: " + selected.getRequiredRam());
        ramReq.setStyle("-fx-text-fill: white;");
        
        Label diskReq = new Label("• Disk: " + selected.getRequiredDisk());
        diskReq.setStyle("-fx-text-fill: white;");
        
        requirementsBox.getChildren().addAll(requirementsTitle, vcpusReq, ramReq, diskReq);
        
        // สร้าง ComboBox สำหรับเลือก VM โดยตรง
        Label vmLabel = new Label("Select VM to Assign:");
        vmLabel.setStyle("-fx-text-fill: white;");
        
        ComboBox<VPSOptimization.VM> vmComboBox = new ComboBox<>();
        vmComboBox.setMaxWidth(Double.MAX_VALUE);
        vmComboBox.setStyle("-fx-background-color: #34495e; -fx-text-fill: white;");
        
        // รวบรวม VM ที่พร้อมใช้งานจากทุก VPS
        List<VPSOptimization.VM> allAvailableVMs = new ArrayList<>();
        for (VPSOptimization vps : vpsManager.getVPSList()) {
            List<VPSOptimization.VM> availableVMs = vps.getVms().stream()
                    .filter(vm -> "Running".equals(vm.getStatus()) && !vmAssignments.containsKey(vm))
                    .collect(java.util.stream.Collectors.toList());
            allAvailableVMs.addAll(availableVMs);
        }
        
        if (!allAvailableVMs.isEmpty()) {
            vmComboBox.getItems().addAll(allAvailableVMs);
            vmComboBox.setPromptText("Select a VM to assign");
        } else {
            vmComboBox.setPromptText("No available VMs found");
        }
        
        vmComboBox.setCellFactory(param -> new ListCell<VPSOptimization.VM>() {
            @Override
            protected void updateItem(VPSOptimization.VM vm, boolean empty) {
                super.updateItem(vm, empty);
                if (empty || vm == null) {
                    setText(null);
                } else {
                    setText(vm.getName() + " (" + vm.getIp() + ") - " + 
                            vm.getVcpu() + " vCPUs, " + vm.getRam() + ", " + vm.getDisk());
                    setStyle("-fx-text-fill: white;");
                }
            }
        });
        
        vmComboBox.setButtonCell(new ListCell<VPSOptimization.VM>() {
            @Override
            protected void updateItem(VPSOptimization.VM vm, boolean empty) {
                super.updateItem(vm, empty);
                if (empty || vm == null) {
                    setText(null);
                } else {
                    setText(vm.getName() + " (" + vm.getIp() + ") - " + 
                            vm.getVcpu() + " vCPUs, " + vm.getRam() + ", " + vm.getDisk());
                    setStyle("-fx-text-fill: white;");
                }
            }
        });
        
        // Error label (initially hidden)
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-opacity: 0;");
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
        
        Button confirmButton = new Button("Assign VM");
        confirmButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
        
        buttonBox.getChildren().addAll(cancelButton, confirmButton);
        
        // Add all components to the content
        content.getChildren().addAll(
                titleLabel,
                new Separator(),
                requirementsBox,
                new Separator(),
                vmLabel,
                vmComboBox,
                errorLabel,
                new Separator(),
                buttonBox
        );
        
        // การกระทำของปุ่ม
        cancelButton.setOnAction(event -> {
            popupStage.close();
        });
        
        confirmButton.setOnAction(event -> {
            VPSOptimization.VM selectedVM = vmComboBox.getValue();
            
            if (selectedVM == null) {
                // แสดงข้อความแจ้งเตือน
                errorLabel.setText("Please select a VM to assign");
                errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-opacity: 1;");
                return;
            }
            
            // ปิดหน้าต่าง popup ก่อนที่จะเพิ่มข้อความ
            popupStage.close();
            
            // เพิ่มข้อความแจ้งลูกค้าว่าเรากำลังดำเนินการตามคำขอ
            addUserMessage(selected, "I'll assign your VM right away.");
            
            // เพิ่มข้อความรอจากลูกค้า
            addCustomerMessage(selected, "Thank you! I'll wait for the setup to complete.");
            
            // ถ้าเลือก VM ที่มีอยู่แล้ว ให้กำหนด VM นั้นให้กับลูกค้าทันที
            Platform.runLater(() -> {
                // อัปเดต UI หลังจากจัดเตรียม VM เสร็จสิ้น
                updateDashboard();
                
                // สร้างชื่อผู้ใช้และรหัสผ่านแบบสุ่ม
                String username = "user_" + selected.getName().toLowerCase().replaceAll("[^a-z0-9]", "") + random.nextInt(100);
                String password = generateRandomPassword();
                
                // เพิ่มข้อความพร้อมรายละเอียด VM
                String vmDetails = "Your VM has been assigned successfully! Here are your access details:\n\n" +
                        "IP Address: " + selectedVM.getIp() + "\n" +
                        "Username: " + username + "\n" +
                        "Password: " + password + "\n\n" +
                        "You can connect using SSH or RDP depending on your operating system.";
                
                addUserMessage(selected, vmDetails);
                
                // เพิ่มข้อความระบบ
                addSystemMessage(selected, "VM assigned successfully");
                
                // ทำเครื่องหมายคำขอว่าเสร็จสมบูรณ์
                selected.activate();
                
                // เก็บการกำหนด VM
                vmAssignments.put(selectedVM, selected);
                
                // อัปเดตมุมมองคำขอเพื่อแสดงสถานะใหม่
                updateRequestList();
            });
        });
        
        // Set the scene and show the popup
        Scene scene = new Scene(content);
        popupStage.setScene(scene);
        popupStage.showAndWait();
    }
    
    /**
     * Generate a random secure password
     * @return A random password
     */
    private String generateRandomPassword() {
        String upperChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerChars = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String specialChars = "!@#$%^&*()_-+=<>?";
        
        StringBuilder password = new StringBuilder();
        
        // Add at least one character from each category
        password.append(upperChars.charAt(random.nextInt(upperChars.length())));
        password.append(lowerChars.charAt(random.nextInt(lowerChars.length())));
        password.append(numbers.charAt(random.nextInt(numbers.length())));
        password.append(specialChars.charAt(random.nextInt(specialChars.length())));
        
        // Add 6 more random characters
        String allChars = upperChars + lowerChars + numbers + specialChars;
        for (int i = 0; i < 6; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }
        
        // Shuffle the password
        char[] passwordArray = password.toString().toCharArray();
        for (int i = 0; i < passwordArray.length; i++) {
            int j = random.nextInt(passwordArray.length);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }
        
        return new String(passwordArray);
    }
    
    /**
     * Add a renewal option to the chat
     * @param request The customer request
     * @param vm The VM assigned to the customer
     */
    private void addRenewalOption(CustomerRequest request, VPSOptimization.VM vm) {
        // Create a renewal button
        HBox renewalContainer = new HBox();
        renewalContainer.setAlignment(Pos.CENTER);
        renewalContainer.setPadding(new Insets(10, 0, 10, 0));
        renewalContainer.getStyleClass().add("message-container");
        
        VBox renewalBox = new VBox(10);
        renewalBox.setAlignment(Pos.CENTER);
        renewalBox.setPadding(new Insets(10));
        renewalBox.setStyle("-fx-background-color: rgba(52, 152, 219, 0.2); -fx-padding: 10px; -fx-border-radius: 5px; -fx-background-radius: 5px;");
        
        Label renewalLabel = new Label("Customer " + request.getName() + " is eligible for contract renewal");
        renewalLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        
        Button renewButton = new Button("Offer Renewal");
        renewButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
        renewButton.setOnAction(e -> {
            // Remove the renewal option
            messagesBox.getChildren().remove(renewalContainer);
            
            // Add a renewal message
            addUserMessage(request, "Would you like to renew your VM contract? We can offer you the same configuration at the current rate.");
            
            // Add a customer response
            addCustomerMessage(request, "Yes, I'd like to renew my contract. This VM has been working well for me.");
            
            // Add a confirmation message
            addUserMessage(request, "Great! I've renewed your contract for another " + request.getRentalPeriodType().getDisplayName() + ". Your VM will continue to operate without interruption.");
            
            // Add a thank you message
            addCustomerMessage(request, "Thank you for the seamless renewal process!");
            
            // Add a system message
            addSystemMessage(request, "Contract renewed for another " + request.getRentalPeriodType().getDisplayName());
        });
        
        renewalBox.getChildren().addAll(renewalLabel, renewButton);
        renewalContainer.getChildren().add(renewalBox);
        
        // Add to messages
        messagesBox.getChildren().add(renewalContainer);
        
        // Store in chat history
        customerChatHistory.computeIfAbsent(request, k -> new java.util.ArrayList<>()).add(renewalContainer);
    }

    public boolean isVMAvailable(VPSOptimization.VM vm) {
        return !vmAssignments.containsKey(vm);
    }

    public void releaseVM(VPSOptimization.VM vm) {
        vmAssignments.remove(vm);
    }

    // Keep the original methods for backward compatibility, but make them use the new methods
    private void addCustomerMessage(String message) {
        CustomerRequest selected = requestView.getSelectionModel().getSelectedItem();
        addCustomerMessage(selected, message);
    }
    
    private void addUserMessage(String message) {
        CustomerRequest selected = requestView.getSelectionModel().getSelectedItem();
        addUserMessage(selected, message);
    }
    
    private void addSystemMessage(String message) {
        CustomerRequest selected = requestView.getSelectionModel().getSelectedItem();
        addSystemMessage(selected, message);
    }
}
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
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.TextAlignment;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
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
    private final Map<CustomerRequest, Boolean> requestStatus;
    private Label ratingLabel; // For dashboard
    private Label activeRequestsLabel; // For dashboard
    private Label availableVMsLabel; // For dashboard
    private Label totalVPSLabel; // For dashboard
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
        this.requestStatus = new HashMap<>();

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
                    avatar.setFill(Color.rgb(random.nextInt(100) + 100, random.nextInt(100), random.nextInt(200) + 55));
                    
                    VBox textContent = new VBox(3);
                    
                    Label nameLabel = new Label(request.getTitle());
                    nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-family: 'Monospace', 'Courier New', monospace;");
                    
                    Label previewLabel = new Label("Needs VPS: " + request.getRequiredVCPUs() + " vCPUs, " + 
                                                  request.getRequiredRam() + " RAM");
                    previewLabel.setStyle("-fx-text-fill: rgba(255, 255, 255, 0.7); -fx-font-size: 12px; -fx-font-family: 'Monospace', 'Courier New', monospace;");
                    
                    textContent.getChildren().addAll(nameLabel, previewLabel);
                    
                    // Status indicator
                    Circle statusIndicator = new Circle(5);
                    statusIndicator.setFill(requestStatus.getOrDefault(request, false) ? 
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
                requestManager.acceptRequest(selected.getTitle());
                updateDashboard();
                
                // Add a system message to the chat
                addSystemMessage("You accepted the request from " + selected.getTitle());
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
        
        Circle customerAvatar = new Circle(20);
        customerAvatar.setFill(Color.rgb(100, 50, 200));
        
        Label customerNameLabel = new Label("Select a customer");
        customerNameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-family: 'Monospace', 'Courier New', monospace;");
        
        chatHeader.getChildren().addAll(customerAvatar, customerNameLabel);
        HBox.setHgrow(customerNameLabel, Priority.ALWAYS);

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
                addUserMessage(message);
                messageInput.clear();
                
                // Simulate customer response after a short delay
                CustomerRequest selected = requestView.getSelectionModel().getSelectedItem();
                if (selected != null && !requestStatus.getOrDefault(selected, false)) {
                    Platform.runLater(() -> {
                        try {
                            Thread.sleep(1000);
                            addCustomerMessage("Thanks for your response! Can you help me with my VPS request?");
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                    });
                }
            }
        });

        Button sendWorkButton = new Button("Assign VPS");
        sendWorkButton.getStyleClass().add("cyber-button");
        sendWorkButton.setDisable(true);
        sendWorkButton.setOnAction(e -> showVMSelectionPopup());

        Button removeCustomerButton = new Button("Remove");
        removeCustomerButton.getStyleClass().addAll("cyber-button", "danger-button");
        removeCustomerButton.setDisable(true);
        removeCustomerButton.setOnAction(e -> {
            CustomerRequest selected = requestView.getSelectionModel().getSelectedItem();
            if (selected != null && requestStatus.getOrDefault(selected, false)) {
                requestManager.getRequests().remove(selected);
                requestStatus.remove(selected);
                VPSOptimization.VM assignedVM = vmAssignments.entrySet().stream()
                        .filter(entry -> entry.getValue() == selected)
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElse(null);
                if (assignedVM != null) {
                    vmAssignments.remove(assignedVM);
                }
                updateRequestList();
                updateDashboard();
                messagesBox.getChildren().clear();
                addSystemMessage("Customer " + selected.getTitle() + " removed.");
                
                // Update chat header
                customerNameLabel.setText("Select a customer");
                customerAvatar.setFill(Color.rgb(100, 50, 200));
            }
        });

        requestView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            sendWorkButton.setDisable(newVal == null || requestStatus.getOrDefault(newVal, false));
            removeCustomerButton.setDisable(newVal == null || !requestStatus.getOrDefault(newVal, false));
            
            if (newVal != null) {
                // Update chat header with customer info
                customerNameLabel.setText(newVal.getTitle());
                customerAvatar.setFill(Color.rgb(random.nextInt(100) + 100, random.nextInt(100), random.nextInt(200) + 55));
                
                updateChatWithRequestDetails(newVal);
            }
        });

        inputArea.getChildren().addAll(messageInput, sendButton, sendWorkButton, removeCustomerButton);
        chatArea.getChildren().addAll(chatHeader, messagesScroll, inputArea);
        
        // Auto-scroll to bottom when new messages are added
        messagesBox.heightProperty().addListener((obs, oldVal, newVal) -> 
            messagesScroll.setVvalue(1.0));
            
        return chatArea;
    }

    private boolean isRequestCompleted(CustomerRequest request) {
        return requestStatus.getOrDefault(request, false);
    }

    private void updateChatWithRequestDetails(CustomerRequest selectedRequest) {
        messagesBox.getChildren().clear();
        
        if (selectedRequest != null) {
            // Add initial customer message with request details
            String requestMessage = "Hello! I need a VPS with the following specs:\n" +
                    "• " + selectedRequest.getRequiredVCPUs() + " vCPUs\n" +
                    "• " + selectedRequest.getRequiredRam() + " RAM\n" +
                    "• " + selectedRequest.getRequiredDisk() + " Disk\n\n" +
                    "Can you help me set this up?";
            
            addCustomerMessage(requestMessage);
            
            if (requestStatus.getOrDefault(selectedRequest, false)) {
                VPSOptimization.VM assignedVM = vmAssignments.entrySet().stream()
                        .filter(entry -> entry.getValue() == selectedRequest)
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElse(null);
                
                if (assignedVM != null) {
                    addUserMessage("I've assigned you a VPS with IP: " + assignedVM.getIp());
                    addCustomerMessage("Thank you! This works perfectly for my needs.");
                    
                    addSystemMessage("VPS assignment completed successfully" +
                            (assignedVM != null ? " (VM: " + assignedVM.getIp() + ")" : ""));
                }
            }
        }
    }
    
    private void addCustomerMessage(String message) {
        HBox messageContainer = new HBox();
        messageContainer.setAlignment(Pos.CENTER_LEFT);
        messageContainer.setPadding(new Insets(5, 0, 5, 0));
        messageContainer.getStyleClass().add("message-container");
        
        CustomerRequest selected = requestView.getSelectionModel().getSelectedItem();
        
        // Customer avatar
        Circle avatar = new Circle(15);
        if (selected != null) {
            avatar.setFill(Color.rgb(random.nextInt(100) + 100, random.nextInt(100), random.nextInt(200) + 55));
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
    }
    
    private void addUserMessage(String message) {
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
    }
    
    private void addSystemMessage(String message) {
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
    }

    private void showVMSelectionPopup() {
        CustomerRequest selectedRequest = requestView.getSelectionModel().getSelectedItem();
        if (selectedRequest == null) return;

        if (selectedRequest == null || requestStatus.getOrDefault(selectedRequest, false)) return;

        StackPane popupPane = new StackPane();
        popupPane.getStyleClass().add("popup-overlay");
        popupPane.setPrefSize(getWidth(), getHeight());

        VBox popupContent = new VBox(15);
        popupContent.getStyleClass().add("popup-content");
        popupContent.setMaxWidth(500);
        popupContent.setMaxHeight(400);

        Label titleLabel = new Label("Select VM for " + selectedRequest.getTitle());
        titleLabel.getStyleClass().add("popup-title");

        ListView<VPSOptimization.VM> vmListView = new ListView<>();
        vmListView.setPrefHeight(200);
        vmListView.getStyleClass().add("vm-list-view");
        
        // Custom cell factory for VM list
        vmListView.setCellFactory(param -> new ListCell<VPSOptimization.VM>() {
            @Override
            protected void updateItem(VPSOptimization.VM vm, boolean empty) {
                super.updateItem(vm, empty);
                if (empty || vm == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox cellContent = new VBox(3);
                    
                    HBox topRow = new HBox(10);
                    topRow.setAlignment(Pos.CENTER_LEFT);
                    
                    Label ipLabel = new Label("IP: " + vm.getIp());
                    ipLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-family: 'Monospace', 'Courier New', monospace;");
                    
                    Label statusLabel = new Label("• " + vm.getStatus());
                    statusLabel.setStyle("-fx-text-fill: #00ffff; -fx-font-family: 'Monospace', 'Courier New', monospace;");
                    
                    topRow.getChildren().addAll(ipLabel, statusLabel);
                    
                    Label specsLabel = new Label("Specs: " + vm.getVCPUs() + " vCPUs, " + 
                                               vm.getRam() + " RAM, " + vm.getDisk() + " Disk");
                    specsLabel.setStyle("-fx-text-fill: rgba(255, 255, 255, 0.7); -fx-font-family: 'Monospace', 'Courier New', monospace;");
                    
                    // Add a visual indicator for matching requirements
                    if (selectedRequest != null) {
                        boolean meetsSpecs = vm.getVCPUs() >= selectedRequest.getRequiredVCPUs() &&
                                vm.getRam().equals(selectedRequest.getRequiredRam()) &&
                                vm.getDisk().equals(selectedRequest.getRequiredDisk());
                        
                        boolean exceedsSpecs = vm.getVCPUs() > selectedRequest.getRequiredVCPUs() ||
                                Integer.parseInt(vm.getRam().split(" ")[0]) > Integer.parseInt(selectedRequest.getRequiredRam().split(" ")[0]) ||
                                Integer.parseInt(vm.getDisk().split(" ")[0]) > Integer.parseInt(selectedRequest.getRequiredDisk().split(" ")[0]);
                        
                        HBox matchIndicator = new HBox(5);
                        matchIndicator.setAlignment(Pos.CENTER_LEFT);
                        
                        Label matchLabel;
                        if (meetsSpecs && !exceedsSpecs) {
                            matchLabel = new Label("✓ PERFECT MATCH");
                            matchLabel.setStyle("-fx-text-fill: #00ff88; -fx-font-weight: bold; -fx-font-family: 'Monospace', 'Courier New', monospace;");
                        } else if (!meetsSpecs) {
                            matchLabel = new Label("✗ BELOW REQUIREMENTS");
                            matchLabel.setStyle("-fx-text-fill: #ff5555; -fx-font-weight: bold; -fx-font-family: 'Monospace', 'Courier New', monospace;");
                        } else {
                            matchLabel = new Label("! EXCEEDS REQUIREMENTS");
                            matchLabel.setStyle("-fx-text-fill: #ffff00; -fx-font-weight: bold; -fx-font-family: 'Monospace', 'Courier New', monospace;");
                        }
                        
                        matchIndicator.getChildren().add(matchLabel);
                        cellContent.getChildren().addAll(topRow, specsLabel, matchIndicator);
                    } else {
                        cellContent.getChildren().addAll(topRow, specsLabel);
                    }
                    
                    setGraphic(cellContent);
                    setText(null);
                }
            }
        });

        for (VPSOptimization vps : vpsManager.getVPSMap().values()) {
            vmListView.getItems().addAll(vps.getVms().stream()
                    .filter(vm -> "Running".equals(vm.getStatus()) && !vmAssignments.containsKey(vm))
                    .toList());
        }

        if (vmListView.getItems().isEmpty()) {
            Label noVMsLabel = new Label("No available VMs.");
            noVMsLabel.setStyle("-fx-text-fill: white;");
            popupContent.getChildren().addAll(titleLabel, noVMsLabel);
        } else {
            // Add requirements reminder
            VBox requirementsBox = new VBox(5);
            requirementsBox.setStyle("-fx-background-color: rgba(106, 0, 255, 0.2); -fx-padding: 10; -fx-background-radius: 5;");
            
            Label requirementsTitle = new Label("Customer Requirements:");
            requirementsTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #00ffff;");
            
            Label requirementsLabel = new Label(
                "• " + selectedRequest.getRequiredVCPUs() + " vCPUs\n" +
                "• " + selectedRequest.getRequiredRam() + " RAM\n" +
                "• " + selectedRequest.getRequiredDisk() + " Disk"
            );
            requirementsLabel.setStyle("-fx-text-fill: white;");
            
            requirementsBox.getChildren().addAll(requirementsTitle, requirementsLabel);
            
            popupContent.getChildren().addAll(titleLabel, requirementsBox, vmListView);
        }

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button sendButton = new Button("Assign VPS");
        sendButton.getStyleClass().add("cyber-button");
        sendButton.setDisable(vmListView.getItems().isEmpty());
        sendButton.setOnAction(e -> {
            VPSOptimization.VM selectedVM = vmListView.getSelectionModel().getSelectedItem();
            if (selectedVM != null) {
                handleWorkSubmission(selectedRequest, selectedVM);
                getChildren().remove(popupPane);
                updateDashboard();
            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().addAll("cyber-button", "danger-button");
        cancelButton.setOnAction(e -> getChildren().remove(popupPane));

        buttonBox.getChildren().addAll(sendButton, cancelButton);
        popupContent.getChildren().add(buttonBox);

        popupPane.getChildren().add(popupContent);
        StackPane.setAlignment(popupContent, Pos.CENTER);

        getChildren().add(popupPane);

        vmListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) ->
                sendButton.setDisable(newVal == null));
    }

    private void handleWorkSubmission(CustomerRequest request, VPSOptimization.VM vm) {
        vmAssignments.put(vm, request);
        requestStatus.put(request, true);

        boolean meetsSpecs = vm.getVCPUs() >= request.getRequiredVCPUs() &&
                vm.getRam().equals(request.getRequiredRam()) &&
                vm.getDisk().equals(request.getRequiredDisk());

        boolean exceedsSpecs = vm.getVCPUs() > request.getRequiredVCPUs() ||
                Integer.parseInt(vm.getRam().split(" ")[0]) > Integer.parseInt(request.getRequiredRam().split(" ")[0]) ||
                Integer.parseInt(vm.getDisk().split(" ")[0]) > Integer.parseInt(request.getRequiredDisk().split(" ")[0]);

        // Add user message about assigning VM
        addUserMessage("I've assigned you a VPS with IP: " + vm.getIp());
        
        if (meetsSpecs && !exceedsSpecs) {
            company.setRating(company.getRating() + 0.1);
            addCustomerMessage("Thank you! This works perfectly for my needs.");
            addSystemMessage("Perfect match! Rating increased to " + String.format("%.1f", company.getRating()));
        } else if (!meetsSpecs) {
            company.setRating(Math.max(1.0, company.getRating() - 0.2));
            addCustomerMessage("This VPS doesn't meet my requirements. I'll try to make it work, but I'm not happy.");
            addSystemMessage("VM specs too low! Rating decreased to " + String.format("%.1f", company.getRating()));
        } else {
            addCustomerMessage("Thanks! This VPS is actually more powerful than I needed, but I'll take it!");
            addSystemMessage("Over-spec VM assigned. Rating unchanged at " + String.format("%.1f", company.getRating()));
        }
        
        updateRequestList();
    }

    public boolean isVMAvailable(VPSOptimization.VM vm) {
        return !vmAssignments.containsKey(vm);
    }

    public void releaseVM(VPSOptimization.VM vm) {
        vmAssignments.remove(vm);
    }
}
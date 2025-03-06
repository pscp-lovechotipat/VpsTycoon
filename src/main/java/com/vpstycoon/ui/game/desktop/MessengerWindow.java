package com.vpstycoon.ui.game.desktop;

import com.vpstycoon.game.manager.CustomerRequest;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.game.manager.VPSManager;
import com.vpstycoon.game.vps.VPSOptimization;
import com.vpstycoon.game.company.Company;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.util.HashMap;
import java.util.Map;

public class MessengerWindow extends VBox {
    private final RequestManager requestManager;
    private final Runnable onClose;
    private final ListView<String> requestView;
    private final VPSManager vpsManager;
    private final Company company;
    private VBox messagesBox;
    private final Map<VPSOptimization.VM, CustomerRequest> vmAssignments;
    private final Map<CustomerRequest, Boolean> requestStatus;
    private Label ratingLabel; // For dashboard
    private Label activeRequestsLabel; // For dashboard
    private Label availableVMsLabel; // For dashboard
    private Label totalVPSLabel; // For dashboard

    public MessengerWindow(RequestManager requestManager, VPSManager vpsManager,
                           Company company, Runnable onClose) {
        this.requestManager = requestManager;
        this.vpsManager = vpsManager;
        this.company = company;
        this.onClose = onClose;
        this.requestView = new ListView<>();
        this.vmAssignments = new HashMap<>();
        this.requestStatus = new HashMap<>();

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
        VBox rightPane = new VBox(10);
        rightPane.setPadding(new Insets(10));
        HBox.setHgrow(rightPane, Priority.ALWAYS);

        VBox dashboard = createDashboard();
        VBox chatArea = createChatArea();

        rightPane.getChildren().addAll(dashboard, chatArea);
        content.getChildren().addAll(requestList, rightPane);

        getChildren().addAll(titleBar, content);

        updateRequestList();
        updateDashboard(); // Initial dashboard update
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
                requestManager.acceptRequest(selected.replace(" (Completed)", ""));
                updateDashboard(); // Update dashboard after accepting request
            }
        });

        requestList.getChildren().addAll(title, requestView, acceptButton);
        return requestList;
    }

    private VBox createDashboard() {
        VBox dashboard = new VBox(10);
        dashboard.setPadding(new Insets(10));
        dashboard.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #ccc; -fx-border-radius: 5;");

        Label title = new Label("Dashboard");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        ratingLabel = new Label("Company Rating: " + String.format("%.1f", company.getRating()));
        activeRequestsLabel = new Label("Active Requests: " + requestManager.getRequests().size());
        availableVMsLabel = new Label("Available VMs: 0"); // Placeholder, updated later
        totalVPSLabel = new Label("Total VPS: " + vpsManager.getVPSMap().size());

        dashboard.getChildren().addAll(title, ratingLabel, activeRequestsLabel, availableVMsLabel, totalVPSLabel);
        return dashboard;
    }

    private void updateDashboard() {
        ratingLabel.setText("Company Rating: " + String.format("%.1f", company.getRating()));
        activeRequestsLabel.setText("Active Requests: " + requestManager.getRequests().size());

        int availableVMs = 0;
        for (VPSOptimization vps : vpsManager.getVPSMap().values()) {
            availableVMs += vps.getVms().stream()
                    .filter(vm -> "Running".equals(vm.getStatus()) && !vmAssignments.containsKey(vm))
                    .count();
        }
        availableVMsLabel.setText("Available VMs: " + availableVMs);

        totalVPSLabel.setText("Total VPS: " + vpsManager.getVPSMap().size());
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
        for (CustomerRequest request : requestManager.getRequests()) {
            String displayText = request.getTitle() + (requestStatus.getOrDefault(request, false) ? " (Completed)" : "");
            requestView.getItems().add(displayText);
        }
    }

    private VBox createChatArea() {
        VBox chatArea = new VBox(10);
        chatArea.setPadding(new Insets(10));
        VBox.setVgrow(chatArea, Priority.ALWAYS);

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
                messagesBox.getChildren().add(new Label("You: " + message));
                messageInput.clear();
            }
        });

        Button sendWorkButton = new Button("Send Work");
        sendWorkButton.setDisable(true);

        Button removeCustomerButton = new Button("Remove Customer");
        removeCustomerButton.setDisable(true);
        removeCustomerButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
        removeCustomerButton.setOnAction(e -> {
            String selected = requestView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                CustomerRequest request = requestManager.getRequests().stream()
                        .filter(req -> req.getTitle().equals(selected.replace(" (Completed)", "")))
                        .findFirst()
                        .orElse(null);
                if (request != null && requestStatus.getOrDefault(request, false)) {
                    requestManager.getRequests().remove(request);
                    requestStatus.remove(request);
                    VPSOptimization.VM assignedVM = vmAssignments.entrySet().stream()
                            .filter(entry -> entry.getValue() == request)
                            .map(Map.Entry::getKey)
                            .findFirst()
                            .orElse(null);
                    if (assignedVM != null) {
                        vmAssignments.remove(assignedVM);
                    }
                    updateRequestList();
                    updateDashboard(); // Update dashboard after removal
                    messagesBox.getChildren().clear();
                    messagesBox.getChildren().add(new Label("Customer " + request.getTitle() + " removed."));
                }
            }
        });

        sendWorkButton.setOnAction(e -> showVMSelectionPopup());

        requestView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            sendWorkButton.setDisable(newVal == null || isRequestCompleted(newVal));
            removeCustomerButton.setDisable(newVal == null || !isRequestCompleted(newVal));
            if (newVal != null) {
                updateChatWithRequestDetails(newVal.replace(" (Completed)", ""));
            }
        });

        inputArea.getChildren().addAll(messageInput, sendButton, sendWorkButton, removeCustomerButton);
        chatArea.getChildren().addAll(messagesScroll, inputArea);
        return chatArea;
    }

    private boolean isRequestCompleted(String requestTitle) {
        CustomerRequest request = requestManager.getRequests().stream()
                .filter(req -> req.getTitle().equals(requestTitle.replace(" (Completed)", "")))
                .findFirst()
                .orElse(null);
        return request != null && requestStatus.getOrDefault(request, false);
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
            if (requestStatus.getOrDefault(selectedRequest, false)) {
                VPSOptimization.VM assignedVM = vmAssignments.entrySet().stream()
                        .filter(entry -> entry.getValue() == selectedRequest)
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElse(null);
                messagesBox.getChildren().add(new Label("Status: Completed" +
                        (assignedVM != null ? " (Assigned VM: " + assignedVM.getIp() + ")" : "")));
            }
        }
    }

    private void showVMSelectionPopup() {
        String selectedRequestTitle = requestView.getSelectionModel().getSelectedItem();
        if (selectedRequestTitle == null) return;

        CustomerRequest selectedRequest = requestManager.getRequests().stream()
                .filter(req -> req.getTitle().equals(selectedRequestTitle.replace(" (Completed)", "")))
                .findFirst()
                .orElse(null);

        if (selectedRequest == null || requestStatus.getOrDefault(selectedRequest, false)) return;

        StackPane popupPane = new StackPane();
        popupPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        popupPane.setPrefSize(400, 300);

        VBox popupContent = new VBox(10);
        popupContent.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 10;");
        popupContent.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Select VM for " + selectedRequest.getTitle());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        ListView<VPSOptimization.VM> vmListView = new ListView<>();
        vmListView.setPrefHeight(150);

        for (VPSOptimization vps : vpsManager.getVPSMap().values()) {
            vmListView.getItems().addAll(vps.getVms().stream()
                    .filter(vm -> "Running".equals(vm.getStatus()) && !vmAssignments.containsKey(vm))
                    .toList());
        }

        if (vmListView.getItems().isEmpty()) {
            popupContent.getChildren().addAll(titleLabel, new Label("No available VMs."));
        } else {
            popupContent.getChildren().addAll(titleLabel, vmListView);
        }

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button sendButton = new Button("Send Work");
        sendButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        sendButton.setDisable(vmListView.getItems().isEmpty());
        sendButton.setOnAction(e -> {
            VPSOptimization.VM selectedVM = vmListView.getSelectionModel().getSelectedItem();
            if (selectedVM != null) {
                handleWorkSubmission(selectedRequest, selectedVM);
                getChildren().remove(popupPane);
                updateDashboard(); // Update dashboard after assignment
            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");
        cancelButton.setOnAction(e -> getChildren().remove(popupPane));

        buttonBox.getChildren().addAll(sendButton, cancelButton);
        popupContent.getChildren().add(buttonBox);

        popupPane.getChildren().add(popupContent);
        StackPane.setAlignment(popupContent, Pos.CENTER);

        StackPane.setAlignment(popupPane, Pos.CENTER);
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

        Label resultMessage;
        if (meetsSpecs && !exceedsSpecs) {
            company.setRating(company.getRating() + 0.1);
            resultMessage = new Label("Work sent successfully! VM " + vm.getIp() + " assigned. Rating increased to " + String.format("%.1f", company.getRating()));
        } else if (!meetsSpecs) {
            company.setRating(Math.max(1.0, company.getRating() - 0.2));
            resultMessage = new Label("VM specs too low! VM " + vm.getIp() + " assigned. Rating decreased to " + String.format("%.1f", company.getRating()));
        } else {
            resultMessage = new Label("Work sent with over-spec VM " + vm.getIp() + ". Rating unchanged at " + String.format("%.1f", company.getRating()));
        }

        messagesBox.getChildren().add(resultMessage);
        updateRequestList();
    }

    private void styleWindow() {
        setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0);");
    }

    public boolean isVMAvailable(VPSOptimization.VM vm) {
        return !vmAssignments.containsKey(vm);
    }

    public void releaseVM(VPSOptimization.VM vm) {
        vmAssignments.remove(vm);
    }
}
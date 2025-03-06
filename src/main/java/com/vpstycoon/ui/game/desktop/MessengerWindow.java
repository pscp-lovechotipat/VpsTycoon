package com.vpstycoon.ui.game.desktop;

import com.vpstycoon.game.manager.CustomerRequest;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.ui.game.GameplayContentPane;
import com.vpstycoon.ui.game.GameplayContentPane.VM;
import com.vpstycoon.ui.game.GameplayContentPane.VPS;
import com.vpstycoon.game.company.Company;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MessengerWindow extends VBox {
    private final RequestManager requestManager;
    private final Runnable onClose;
    private final ListView<String> requestView;
    private final GameplayContentPane gameplayContentPane;
    private final Company company;
    private VBox messagesBox;
    private final Map<VM, CustomerRequest> vmAssignments; // Track VM-to-customer assignments
    private final Map<CustomerRequest, Boolean> requestStatus; // Track completion status

    public MessengerWindow(RequestManager requestManager, GameplayContentPane gameplayContentPane,
                           Company company, Runnable onClose) {
        this.requestManager = requestManager;
        this.gameplayContentPane = gameplayContentPane;
        this.company = company;
        this.onClose = onClose;
        this.requestView = new ListView<>();
        this.vmAssignments = new HashMap<>();
        this.requestStatus = new HashMap<>(); // true = completed, false = active

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
        for (CustomerRequest request : requestManager.getRequests()) {
            String displayText = request.getTitle() + (requestStatus.getOrDefault(request, false) ? " (Completed)" : "");
            requestView.getItems().add(displayText);
        }
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
                    VM assignedVM = vmAssignments.entrySet().stream()
                            .filter(entry -> entry.getValue() == request)
                            .map(Map.Entry::getKey)
                            .findFirst()
                            .orElse(null);
                    if (assignedVM != null) {
                        vmAssignments.remove(assignedVM);
                    }
                    updateRequestList();
                    messagesBox.getChildren().clear();
                    messagesBox.getChildren().add(new Label("Customer " + request.getTitle() + " removed."));
                }
            }
        });

        sendWorkButton.setOnAction(e -> showVPSSelectionPopup());

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
                VM assignedVM = vmAssignments.entrySet().stream()
                        .filter(entry -> entry.getValue() == selectedRequest)
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElse(null);
                messagesBox.getChildren().add(new Label("Status: Completed" +
                        (assignedVM != null ? " (Assigned VM: " + assignedVM.getIp() + ")" : "")));
            }
        }
    }

    private void showVPSSelectionPopup() {
        String selectedRequestTitle = requestView.getSelectionModel().getSelectedItem();
        if (selectedRequestTitle == null) return;

        CustomerRequest selectedRequest = requestManager.getRequests().stream()
                .filter(req -> req.getTitle().equals(selectedRequestTitle.replace(" (Completed)", "")))
                .findFirst()
                .orElse(null);

        if (selectedRequest == null || requestStatus.getOrDefault(selectedRequest, false)) return;

        Dialog<VM> dialog = new Dialog<>();
        dialog.setTitle("Select VPS for Customer");
        dialog.setHeaderText("Choose an available VM for " + selectedRequest.getTitle());

        VBox dialogContent = new VBox(10);
        ListView<VM> vmListView = new ListView<>();

        for (VPS vps : gameplayContentPane.getVpsList()) {
            vmListView.getItems().addAll(vps.getVms().stream()
                    .filter(vm -> "Running".equals(vm.getStatus()) && !vmAssignments.containsKey(vm))
                    .toList());
        }

        if (vmListView.getItems().isEmpty()) {
            dialogContent.getChildren().add(new Label("No available VMs. All running VMs are assigned."));
        } else {
            dialogContent.getChildren().addAll(new Label("Available VMs:"), vmListView);
        }

        dialog.getDialogPane().setContent(dialogContent);

        ButtonType sendButtonType = new ButtonType("Send Work", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(sendButtonType, ButtonType.CANCEL);

        dialog.getDialogPane().lookupButton(sendButtonType).setDisable(vmListView.getItems().isEmpty());
        vmListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) ->
                dialog.getDialogPane().lookupButton(sendButtonType).setDisable(newVal == null));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == sendButtonType) {
                return vmListView.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        Optional<VM> result = dialog.showAndWait();
        result.ifPresent(vm -> handleWorkSubmission(selectedRequest, vm));
    }

    private void handleWorkSubmission(CustomerRequest request, VM vm) {
        vmAssignments.put(vm, request);
        requestStatus.put(request, true); // Mark as completed

        boolean meetsSpecs = vm.getvCPUs() >= request.getRequiredVCPUs() &&
                vm.getRam().equals(request.getRequiredRam()) &&
                vm.getDisk().equals(request.getRequiredDisk());

        boolean exceedsSpecs = vm.getvCPUs() > request.getRequiredVCPUs() ||
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
        updateRequestList(); // Refresh list to show "(Completed)"
    }

    private void styleWindow() {
        setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0);");
    }

    public boolean isVMAvailable(VM vm) {
        return !vmAssignments.containsKey(vm);
    }

    public void releaseVM(VM vm) {
        vmAssignments.remove(vm);
    }
}
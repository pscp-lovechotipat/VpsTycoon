package com.vpstycoon.ui.game.desktop.messenger;

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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MessengerController {
    private final RequestManager requestManager;
    private final VPSManager vpsManager;
    private final Company company;
    private final ChatHistoryManager chatHistoryManager;
    private final RequestListView requestListView;
    private final ChatAreaView chatAreaView;
    private final DashboardView dashboardView;
    private Runnable onClose;
    private final Map<VPSOptimization.VM, CustomerRequest> vmAssignments = new HashMap<>();
    private final Map<CustomerRequest, Date> rentalEndDates = new HashMap<>();
    private final Map<CustomerRequest, Timer> rentalTimers = new HashMap<>();
    private final Map<CustomerRequest, ProgressBar> provisioningProgressBars = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Random random = new Random();
    private static final long GAME_MONTH_IN_MINUTES = 15;
    private static final long MILLISECONDS_PER_GAME_DAY = (GAME_MONTH_IN_MINUTES * 60 * 1000) / 30;

    public MessengerController(RequestManager requestManager, VPSManager vpsManager, Company company,
                               ChatHistoryManager chatHistoryManager, RequestListView requestListView,
                               ChatAreaView chatAreaView, DashboardView dashboardView, Runnable onClose) {
        this.requestManager = requestManager;
        this.vpsManager = vpsManager;
        this.company = company;
        this.chatHistoryManager = chatHistoryManager;
        this.requestListView = requestListView;
        this.chatAreaView = chatAreaView;
        this.dashboardView = dashboardView;
        this.onClose = () -> {
            chatHistoryManager.saveChatHistory();
            cleanup();
            onClose.run();
        };

        setupListeners();
        updateRequestList();
        updateDashboard();
    }

    private void setupListeners() {
        requestManager.getRequests().addListener((ListChangeListener<CustomerRequest>) change -> {
            Platform.runLater(() -> {
                updateRequestList();
                updateDashboard();
            });
        });

        requestListView.getRequestView().getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            chatAreaView.updateChatHeader(newVal);
            chatAreaView.getAssignVMButton().setDisable(newVal == null || newVal.isActive() || newVal.isExpired());
            chatAreaView.getArchiveButton().setDisable(newVal == null || (!newVal.isActive() && !newVal.isExpired()));
            if (newVal != null) {
                updateChatWithRequestDetails(newVal);
            }
        });

        chatAreaView.getSendButton().setOnAction(e -> {
            String message = chatAreaView.getMessageInput().getText();
            if (!message.isEmpty()) {
                CustomerRequest selected = requestListView.getSelectedRequest();
                if (selected != null) {
                    chatHistoryManager.addMessage(selected, new ChatMessage(MessageType.USER, message));
                    chatAreaView.addUserMessage(message);
                    chatAreaView.getMessageInput().clear();

                    if (!selected.isActive()) {
                        new Thread(() -> {
                            try {
                                Thread.sleep(1000);
                                Platform.runLater(() -> {
                                    chatHistoryManager.addMessage(selected, new ChatMessage(MessageType.CUSTOMER, "Thanks for your response! Can you help me with my VM request?"));
                                    chatAreaView.addCustomerMessage(selected, "Thanks for your response! Can you help me with my VM request?");
                                });
                            } catch (InterruptedException ex) {
                                Thread.currentThread().interrupt();
                            }
                        }).start();
                    } else {
                        new Thread(() -> {
                            try {
                                Thread.sleep(1000);
                                Platform.runLater(() -> {
                                    chatHistoryManager.addMessage(selected, new ChatMessage(MessageType.CUSTOMER, "Thank you for checking in! The VM is working great."));
                                    chatAreaView.addCustomerMessage(selected, "Thank you for checking in! The VM is working great.");
                                });
                            } catch (InterruptedException ex) {
                                Thread.currentThread().interrupt();
                            }
                        }).start();
                    }
                }
            }
        });

        chatAreaView.getAssignVMButton().setOnAction(e -> showVMSelectionPopup());
        chatAreaView.getArchiveButton().setOnAction(e -> archiveRequest());
    }

    private void updateRequestList() {
        requestListView.updateRequestList(requestManager.getRequests());
    }

    private void updateDashboard() {
        int availableVMs = 0;
        for (VPSOptimization vps : vpsManager.getVPSMap().values()) {
            availableVMs += (int) vps.getVms().stream()
                    .filter(vm -> "Running".equals(vm.getStatus()) && !vmAssignments.containsKey(vm))
                    .count();
        }
        dashboardView.updateDashboard(company.getRating(), requestManager.getRequests().size(), availableVMs, vpsManager.getVPSMap().size());
    }

    private void updateChatWithRequestDetails(CustomerRequest request) {
        chatAreaView.clearMessages();
        List<ChatMessage> chatHistory = chatHistoryManager.getChatHistory(request);
        if (chatHistory != null && !chatHistory.isEmpty()) {
            for (ChatMessage message : chatHistory) {
                switch (message.getType()) {
                    case CUSTOMER:
                        chatAreaView.addCustomerMessage(request, message.getContent());
                        break;
                    case USER:
                        chatAreaView.addUserMessage(message.getContent());
                        break;
                    case SYSTEM:
                        chatAreaView.addSystemMessage(message.getContent());
                        break;
                }
            }
        } else {
            String requestMessage = "Hello! I need a VM with the following specs:\n" +
                    "• " + request.getRequiredVCPUs() + " vCPUs\n" +
                    "• " + request.getRequiredRam() + " RAM\n" +
                    "• " + request.getRequiredDisk() + " Disk\n\n" +
                    "Can you help me set this up?";
            chatHistoryManager.addMessage(request, new ChatMessage(MessageType.CUSTOMER, requestMessage));
            chatAreaView.addCustomerMessage(request, requestMessage);
        }
    }

    private void showVMSelectionPopup() {
        CustomerRequest selected = requestListView.getSelectedRequest();
        if (selected == null || selected.isActive()) return;

        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Assign VM to " + selected.getName());

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #2c3e50;");
        content.setMinWidth(400);

        Label titleLabel = new Label("Assign VM to " + selected.getName());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        VBox requirementsBox = new VBox(5);
        requirementsBox.setStyle("-fx-background-color: rgba(52, 152, 219, 0.2); -fx-padding: 10px; -fx-border-radius: 5px;");

        Label requirementsTitle = new Label("Customer Requirements:");
        requirementsTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #3498db;");

        Label vcpusLabel = new Label("• vCPUs: " + selected.getRequiredVCPUs());
        vcpusLabel.setStyle("-fx-text-fill: white;");

        Label ramLabel = new Label("• RAM: " + selected.getRequiredRam());
        ramLabel.setStyle("-fx-text-fill: white;");

        Label diskLabel = new Label("• Disk: " + selected.getRequiredDisk());
        diskLabel.setStyle("-fx-text-fill: white;");

        requirementsBox.getChildren().addAll(requirementsTitle, vcpusLabel, ramLabel, diskLabel);

        ComboBox<VPSOptimization.VM> vmComboBox = new ComboBox<>();
        vmComboBox.setMaxWidth(Double.MAX_VALUE);
        vmComboBox.setStyle("-fx-background-color: #34495e; -fx-text-fill: white;");
        List<VPSOptimization.VM> allAvailableVMs = new ArrayList<>();
        for (VPSOptimization vps : vpsManager.getVPSList()) {
            allAvailableVMs.addAll(vps.getVms().stream()
                    .filter(vm -> "Running".equals(vm.getStatus()) && !vmAssignments.containsKey(vm))
                    .collect(java.util.stream.Collectors.toList()));
        }
        vmComboBox.getItems().addAll(allAvailableVMs);
        vmComboBox.setPromptText(allAvailableVMs.isEmpty() ? "No available VMs" : "Select a VM");

        Button confirmButton = new Button("Assign VM");
        confirmButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
        confirmButton.setOnAction(e -> {
            VPSOptimization.VM selectedVM = vmComboBox.getValue();
            if (selectedVM != null) {
                popupStage.close();
                completeVMProvisioning(selected, selectedVM);
            }
        });

        Label selectVMLabel = new Label("Select VM:");
        selectVMLabel.setStyle("-fx-text-fill: white;");

        content.getChildren().addAll(titleLabel, requirementsBox, selectVMLabel, vmComboBox, confirmButton);
        popupStage.setScene(new Scene(content));
        popupStage.showAndWait();
    }

    private void completeVMProvisioning(CustomerRequest request, VPSOptimization.VM vm) {
        vmAssignments.put(vm, request);
        request.activate();
        chatHistoryManager.addMessage(request, new ChatMessage(MessageType.SYSTEM, "VM assigned successfully"));
        chatAreaView.addSystemMessage("VM assigned successfully");
        updateRequestList();
        updateDashboard();
    }

    private void archiveRequest() {
        CustomerRequest selected = requestListView.getSelectedRequest();
        if (selected != null && (selected.isActive() || selected.isExpired())) {
            VPSOptimization.VM assignedVM = vmAssignments.entrySet().stream()
                    .filter(entry -> entry.getValue() == selected)
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(null);
            if (assignedVM != null) {
                releaseVM(assignedVM, true);
            }
            requestManager.getRequests().remove(selected);
            chatAreaView.clearMessages();
            updateRequestList();
        }
    }

    public void releaseVM(VPSOptimization.VM vm, boolean isArchiving) {
        CustomerRequest request = vmAssignments.get(vm);
        if (request != null) {
            Timer timer = rentalTimers.remove(request);
            if (timer != null) timer.cancel();
            rentalEndDates.remove(request);
            provisioningProgressBars.remove(request);
            if (isArchiving) {
                requestManager.getRequests().remove(request);
            } else {
                request.markAsExpired();
            }
        }
        vmAssignments.remove(vm);
        updateDashboard();
    }

    private void cleanup() {
        for (Timer timer : rentalTimers.values()) {
            timer.cancel();
        }
        rentalTimers.clear();
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    public void close() {
        chatHistoryManager.saveChatHistory();
        cleanup();
    }
}
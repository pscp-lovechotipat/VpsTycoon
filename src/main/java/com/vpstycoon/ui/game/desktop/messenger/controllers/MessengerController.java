package com.vpstycoon.ui.game.desktop.messenger.controllers;

import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.manager.CustomerRequest;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.game.manager.VPSManager;
import com.vpstycoon.game.vps.VPSOptimization;
import com.vpstycoon.ui.game.desktop.messenger.*;
import com.vpstycoon.ui.game.desktop.messenger.models.*;
import com.vpstycoon.ui.game.desktop.messenger.views.ChatAreaView;
import com.vpstycoon.ui.game.desktop.messenger.views.DashboardView;
import com.vpstycoon.ui.game.desktop.messenger.views.RequestListView;
import com.vpstycoon.ui.game.desktop.messenger.views.VMSelectionDialog;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MessengerController {
    private final RequestManager requestManager;
    private final VPSManager vpsManager;
    private final Company company;
    private final ChatHistoryManager chatHistoryManager;
    private final RequestListView requestListView;
    private final ChatAreaView chatAreaView;
    private final DashboardView dashboardView;
    private final StackPane rootStack;
    private Runnable onClose;
    private final Map<VPSOptimization.VM, CustomerRequest> vmAssignments = new HashMap<>();
    private final Map<CustomerRequest, ProgressBar> provisioningProgressBars = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final VMProvisioningManager vmProvisioningManager;
    private final RentalManager rentalManager;
    private final SkillPointsManager skillPointsManager;

    public MessengerController(RequestManager requestManager, VPSManager vpsManager, Company company,
                               ChatHistoryManager chatHistoryManager, RequestListView requestListView,
                               ChatAreaView chatAreaView, DashboardView dashboardView, StackPane rootStack, Runnable onClose) {
        this.requestManager = requestManager;
        this.vpsManager = vpsManager;
        this.company = company;
        this.chatHistoryManager = chatHistoryManager;
        this.requestListView = requestListView;
        this.chatAreaView = chatAreaView;
        this.dashboardView = dashboardView;
        this.rootStack = rootStack;
        this.onClose = () -> {
            chatHistoryManager.saveChatHistory();
            cleanup();
            onClose.run();
        };

        this.vmProvisioningManager = new VMProvisioningManager(chatHistoryManager, chatAreaView, provisioningProgressBars);
        this.rentalManager = new RentalManager(chatHistoryManager, chatAreaView, company);
        this.skillPointsManager = new SkillPointsManager(chatHistoryManager, chatAreaView);

        setupListeners();
        updateRequestList();
        updateDashboard();
        loadSkillLevels();
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

        chatAreaView.getAssignVMButton().setOnAction(e -> {
            CustomerRequest selected = requestListView.getSelectedRequest();
            if (selected != null && !selected.isActive()) {
                List<VPSOptimization.VM> allAvailableVMs = new ArrayList<>();
                for (VPSOptimization vps : vpsManager.getVPSMap().values()) {
                    allAvailableVMs.addAll(vps.getVms().stream()
                            .filter(vm -> "Running".equals(vm.getStatus()) && !vmAssignments.containsKey(vm))
                            .collect(Collectors.toList()));
                }
                VMSelectionDialog dialog = new VMSelectionDialog(allAvailableVMs, rootStack);
                dialog.setOnConfirm(() -> {
                    VPSOptimization.VM selectedVM = dialog.getSelectedVM();
                    if (selectedVM != null) {
                        vmProvisioningManager.startVMProvisioning(selected, selectedVM, () -> {
                            // โค้ดที่ต้องการให้ทำงานหลัง provisioning เสร็จสิ้น
                            System.out.println("Provisioning เสร็จแล้ว! VM ได้ถูก assign เรียบร้อย");
                            // ตัวอย่าง: เรียกเมธอดอื่นๆ หรืออัปเดตสถานะ
                            completeVMProvisioning(selected, selectedVM);
                        });
                    }
                });
            }
        });

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

    private void completeVMProvisioning(CustomerRequest request, VPSOptimization.VM vm) {
        vmAssignments.put(vm, request);
        request.activate();
        chatHistoryManager.addMessage(request, new ChatMessage(MessageType.SYSTEM, "VM assigned successfully"));
        chatAreaView.addSystemMessage("VM assigned successfully");
        updateRequestList();
        updateDashboard();
        rentalManager.setupRentalPeriod(request, vm);
        skillPointsManager.awardSkillPoints(request, 0.2);
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
            rentalManager.cancelRental(request);
            provisioningProgressBars.remove(request);
            if (isArchiving) {
                requestManager.getRequests().remove(request);
                chatHistoryManager.addMessage(request, new ChatMessage(MessageType.SYSTEM,
                        "Request archived and VM released"));
                chatAreaView.addSystemMessage("Request archived and VM released");
            } else {
                request.markAsExpired();
                chatHistoryManager.addMessage(request, new ChatMessage(MessageType.SYSTEM,
                        "Contract expired and VM released"));
                chatAreaView.addSystemMessage("Contract expired and VM released");
            }
        }
        vmAssignments.remove(vm);
        updateDashboard();
    }

    private void loadSkillLevels() {
        try {
            java.lang.reflect.Field skillLevelsField = com.vpstycoon.ui.game.status.CircleStatusButton.class
                    .getDeclaredField("skillLevels");
            skillLevelsField.setAccessible(true);
            HashMap<String, Integer> skillLevels = (HashMap<String, Integer>) skillLevelsField.get(null);
            int deployLevel = skillLevels.getOrDefault("Deploy", 1);
            vmProvisioningManager.setDeployLevel(deployLevel);
            System.out.println("Loaded skill levels: " + skillLevels);
        } catch (Exception e) {
            System.err.println("Error loading skill levels: " + e.getMessage());
            vmProvisioningManager.setDeployLevel(1);
        }
    }

    private void cleanup() {
        for (Timer timer : rentalManager.getRentalTimers().values()) {
            timer.cancel();
        }
        rentalManager.getRentalTimers().clear();
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
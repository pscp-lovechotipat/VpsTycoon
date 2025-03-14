package com.vpstycoon.ui.game.desktop.messenger;

import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.customer.enums.CustomerType;
import com.vpstycoon.game.manager.CustomerRequest;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.game.manager.VPSManager;
import com.vpstycoon.game.vps.VPSOptimization;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

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
    private final StackPane rootStack; // เพิ่มตัวแปรสำหรับ rootStack
    private Runnable onClose;
    private final Map<VPSOptimization.VM, CustomerRequest> vmAssignments = new HashMap<>();
    private final Map<CustomerRequest, Date> rentalEndDates = new HashMap<>();
    private final Map<CustomerRequest, Timer> rentalTimers = new HashMap<>();
    private final Map<CustomerRequest, ProgressBar> provisioningProgressBars = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Random random = new Random();
    private static final long GAME_MONTH_IN_MINUTES = 15;
    private static final long MILLISECONDS_PER_GAME_DAY = (GAME_MONTH_IN_MINUTES * 60 * 1000) / 30;
    private static final long[] DEPLOY_TIMES = {10000, 5000, 2000, 1000};
    private int deployLevel = 1;

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
                            .collect(java.util.stream.Collectors.toList()));
                }
                // สร้าง dialog ก่อน
                final VMSelectionDialog dialog = new VMSelectionDialog(allAvailableVMs, rootStack);
                // กำหนด callback แยก
                dialog.setOnConfirm(() -> {
                    VPSOptimization.VM selectedVM = dialog.getSelectedVM();
                    if (selectedVM != null) {
                        startVMProvisioning(selected, selectedVM);
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

    private void startVMProvisioning(CustomerRequest request, VPSOptimization.VM vm) {
        chatHistoryManager.addMessage(request, new ChatMessage(MessageType.USER,
                "I'll assign your VM right away. Please wait while we set it up for you."));
        chatAreaView.addUserMessage("I'll assign your VM right away. Please wait while we set it up for you.");

        chatHistoryManager.addMessage(request, new ChatMessage(MessageType.CUSTOMER,
                "Thank you! I'll wait for the setup to complete."));
        chatAreaView.addCustomerMessage(request, "Thank you! I'll wait for the setup to complete.");

        HBox progressContainer = new HBox();
        progressContainer.setAlignment(Pos.CENTER);
        progressContainer.setPadding(new Insets(10, 0, 10, 0));
        progressContainer.getStyleClass().add("message-container");

        VBox progressBox = new VBox(5);
        progressBox.setAlignment(Pos.CENTER);
        progressBox.setPadding(new Insets(10));
        progressBox.setStyle("-fx-background-color: rgba(52, 152, 219, 0.2); -fx-padding: 10px; -fx-border-radius: 5px; -fx-background-radius: 5px;");

        Label progressLabel = new Label("Setting up your VM...");
        progressLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(200);
        progressBar.setStyle("-fx-accent: #3498db;");

        progressBox.getChildren().addAll(progressLabel, progressBar);
        progressContainer.getChildren().add(progressBox);

        Platform.runLater(() -> chatAreaView.getMessagesBox().getChildren().add(progressContainer));
        provisioningProgressBars.put(request, progressBar);

        long deploymentTime = getDeploymentTime();
        int minDelay = 5;
        int maxDelay = 60;
        int provisioningDelay = minDelay + random.nextInt(maxDelay - minDelay + 1);
        provisioningDelay = Math.max(minDelay, (int)(provisioningDelay * (deploymentTime / 10000.0)));

        final int[] progress = {0};
        final int totalSteps = provisioningDelay * 10;

        Timer progressTimer = new Timer();
        progressTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                progress[0]++;
                double progressValue = (double) progress[0] / totalSteps;

                Platform.runLater(() -> {
                    progressBar.setProgress(progressValue);
                    if (progress[0] >= totalSteps) {
                        this.cancel();
                        provisioningProgressBars.remove(request);
                        completeVMProvisioning(request, vm);

                        String username = "user_" + request.getName().toLowerCase().replaceAll("[^a-z0-9]", "") + random.nextInt(100);
                        String password = generateRandomPassword();

                        String vmDetails = "Your VM has been assigned successfully! Here are your access details:\n\n" +
                                "IP Address: " + vm.getIp() + "\n" +
                                "Username: " + username + "\n" +
                                "Password: " + password + "\n\n" +
                                "You can connect using SSH or RDP depending on your operating system.";
                        chatHistoryManager.addMessage(request, new ChatMessage(MessageType.USER, vmDetails));
                        chatAreaView.addUserMessage(vmDetails);

                        chatHistoryManager.addMessage(request, new ChatMessage(MessageType.CUSTOMER,
                                "Thank you! I've received the VM details and will start using it right away."));
                        chatAreaView.addCustomerMessage(request, "Thank you! I've received the VM details and will start using it right away.");
                    }
                });
            }
        }, 0, 100);
    }

    private void completeVMProvisioning(CustomerRequest request, VPSOptimization.VM vm) {
        vmAssignments.put(vm, request);
        request.activate();
        chatHistoryManager.addMessage(request, new ChatMessage(MessageType.SYSTEM, "VM assigned successfully"));
        chatAreaView.addSystemMessage("VM assigned successfully");
        updateRequestList();
        updateDashboard();
        setupRentalPeriod(request, vm);
        awardSkillPoints(request, 0.2);
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

    private void setupRentalPeriod(CustomerRequest request, VPSOptimization.VM vm) {
        if (request == null || vm == null) return;

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MILLISECOND, (int)(request.getRentalPeriod() * MILLISECONDS_PER_GAME_DAY));
        Date endDate = calendar.getTime();
        rentalEndDates.put(request, endDate);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    double renewalProbability = calculateRenewalProbability(request);
                    boolean willRenew = random.nextDouble() < renewalProbability;

                    if (willRenew) {
                        chatHistoryManager.addMessage(request, new ChatMessage(MessageType.SYSTEM,
                                "Customer has decided to renew their contract!"));
                        chatAreaView.addSystemMessage("Customer has decided to renew their contract!");
                        setupRentalPeriod(request, vm);
                        double paymentAmount = request.getPaymentAmount();
                        company.addMoney(paymentAmount);
                        chatHistoryManager.addMessage(request, new ChatMessage(MessageType.SYSTEM,
                                "Received payment of $" + String.format("%.2f", paymentAmount) + " for contract renewal."));
                        chatAreaView.addSystemMessage("Received payment of $" + String.format("%.2f", paymentAmount) + " for contract renewal.");
                    } else {
                        chatHistoryManager.addMessage(request, new ChatMessage(MessageType.SYSTEM,
                                "Customer has decided not to renew their contract."));
                        chatAreaView.addSystemMessage("Customer has decided not to renew their contract.");
                        releaseVM(vm, false);
                    }
                });
            }
        }, endDate);

        rentalTimers.put(request, timer);

        String rentalMessage = "VM assigned for " + request.getRentalPeriodType().getDisplayName() +
                " rental period (ends on " + endDate.toString() + ")";
        chatHistoryManager.addMessage(request, new ChatMessage(MessageType.SYSTEM, rentalMessage));
        chatAreaView.addSystemMessage(rentalMessage);

        double paymentAmount = request.getPaymentAmount();
        company.addMoney(paymentAmount);
        chatHistoryManager.addMessage(request, new ChatMessage(MessageType.SYSTEM,
                "Received payment of $" + String.format("%.2f", paymentAmount)));
        chatAreaView.addSystemMessage("Received payment of $" + String.format("%.2f", paymentAmount));
    }

    private double calculateRenewalProbability(CustomerRequest request) {
        double baseProbability = 0.5;
        double ratingFactor = company.getRating() * 0.1;
        double finalProbability = baseProbability + ratingFactor;
        return Math.max(0.1, Math.min(0.95, finalProbability));
    }

    private void awardSkillPoints(CustomerRequest request, double ratingImpact) {
        int basePoints = request.getCustomerType() == CustomerType.ENTERPRISE ? 15 : 5;
        int points = ratingImpact > 0 ? basePoints + 5 : basePoints;

        try {
            java.lang.reflect.Field skillPointsField = com.vpstycoon.ui.game.status.CircleStatusButton.class
                    .getDeclaredField("skillPointsMap");
            skillPointsField.setAccessible(true);
            HashMap<String, Integer> skillPointsMap = (HashMap<String, Integer>) skillPointsField.get(null);

            skillPointsMap.put("Deploy", skillPointsMap.getOrDefault("Deploy", 0) + points);
            chatHistoryManager.addMessage(request, new ChatMessage(MessageType.SYSTEM,
                    "Earned " + points + " Deploy points! Total: " + skillPointsMap.get("Deploy")));
            chatAreaView.addSystemMessage("Earned " + points + " Deploy points! Total: " + skillPointsMap.get("Deploy"));
        } catch (Exception e) {
            System.err.println("Error updating skill points: " + e.getMessage());
        }
    }

    private void loadSkillLevels() {
        try {
            java.lang.reflect.Field skillLevelsField = com.vpstycoon.ui.game.status.CircleStatusButton.class
                    .getDeclaredField("skillLevels");
            skillLevelsField.setAccessible(true);
            HashMap<String, Integer> skillLevels = (HashMap<String, Integer>) skillLevelsField.get(null);
            deployLevel = skillLevels.getOrDefault("Deploy", 1);
            System.out.println("Loaded skill levels: " + skillLevels);
        } catch (Exception e) {
            System.err.println("Error loading skill levels: " + e.getMessage());
            deployLevel = 1;
        }
    }

    private long getDeploymentTime() {
        int level = Math.max(1, Math.min(deployLevel, DEPLOY_TIMES.length));
        return DEPLOY_TIMES[level - 1];
    }

    private String generateRandomPassword() {
        String upperChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerChars = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String specialChars = "!@#$%^&*()-_=+";
        String allChars = upperChars + lowerChars + numbers + specialChars;

        StringBuilder password = new StringBuilder();

        password.append(upperChars.charAt(random.nextInt(upperChars.length())));
        password.append(lowerChars.charAt(random.nextInt(lowerChars.length())));
        password.append(numbers.charAt(random.nextInt(numbers.length())));
        password.append(specialChars.charAt(random.nextInt(specialChars.length())));

        for (int i = 0; i < 6; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        char[] passwordArray = password.toString().toCharArray();
        for (int i = 0; i < passwordArray.length; i++) {
            int j = random.nextInt(passwordArray.length);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }

        return new String(passwordArray);
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
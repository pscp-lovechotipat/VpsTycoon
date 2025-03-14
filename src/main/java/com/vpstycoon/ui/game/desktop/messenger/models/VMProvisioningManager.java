package com.vpstycoon.ui.game.desktop.messenger.models;

import com.vpstycoon.game.manager.CustomerRequest;
import com.vpstycoon.game.vps.VPSOptimization;
import com.vpstycoon.ui.game.desktop.messenger.views.ChatAreaView;
import com.vpstycoon.ui.game.desktop.messenger.MessageType;
import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class VMProvisioningManager {
    private final ChatHistoryManager chatHistoryManager;
    private final ChatAreaView chatAreaView;
    private final Map<CustomerRequest, ProgressBar> provisioningProgressBars;
    private final Random random = new Random();
    private final long[] DEPLOY_TIMES = {10000, 5000, 2000, 1000};
    private int deployLevel = 1;

    public VMProvisioningManager(ChatHistoryManager chatHistoryManager, ChatAreaView chatAreaView,
                                 Map<CustomerRequest, ProgressBar> provisioningProgressBars) {
        this.chatHistoryManager = chatHistoryManager;
        this.chatAreaView = chatAreaView;
        this.provisioningProgressBars = provisioningProgressBars;
    }

    public void startVMProvisioning(CustomerRequest request, VPSOptimization.VM vm, Runnable onComplete) {
        // บันทึกข้อความระบบเมื่อเริ่ม provisioning และแสดงใน UI
        chatAreaView.addSystemMessage("Starting VM provisioning...");

        sendInitialMessages(request);

        // สร้างและแสดง progressBox ใน UI เหมือนเดิม
        HBox progressContainer = new HBox();
        progressContainer.setAlignment(Pos.CENTER);
        progressContainer.setPadding(new Insets(10, 0, 10, 0));
        progressContainer.getStyleClass().add("message-container");

        VBox progressBox = new VBox(5);
        progressBox.setAlignment(Pos.CENTER);
        progressBox.setPadding(new Insets(10));
        progressBox.setStyle("-fx-background-color: rgba(52, 152, 219, 0.2); -fx-padding: 10px; -fx-border-radius: 5px; -fx-background-radius: 5px;");

        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(200);
        progressBar.setStyle("-fx-accent: #3498db;");

        progressBox.getChildren().addAll(new javafx.scene.control.Label("Setting up your VM..."), progressBar);
        progressContainer.getChildren().add(progressBox);

        Platform.runLater(() -> chatAreaView.getMessagesBox().getChildren().add(progressContainer));
        provisioningProgressBars.put(request, progressBar);

        int provisioningDelay = calculateProvisioningDelay();
        final int[] progress = {0};
        final int totalSteps = provisioningDelay * 10;

        chatHistoryManager.addMessage(request, new ChatMessage(MessageType.SYSTEM, "Starting VM provisioning in " + provisioningDelay + " seconds..."));

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
                        sendVMDetails(request, vm);
                        // บันทึกข้อความระบบเมื่อ provisioning เสร็จสิ้น และแสดงใน UI
                        chatHistoryManager.addMessage(request, new ChatMessage(MessageType.SYSTEM, "VM provisioning completed."));
                        chatAreaView.addSystemMessage("VM provisioning completed.");
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    }
                });
            }
        }, 0, 100);
    }

    private void sendInitialMessages(CustomerRequest request) {
        chatHistoryManager.addMessage(request, new ChatMessage(MessageType.USER,
                "I'll assign your VM right away. Please wait while we set it up for you."));
        chatAreaView.addUserMessage("I'll assign your VM right away. Please wait while we set it up for you.");
        chatHistoryManager.addMessage(request, new ChatMessage(MessageType.CUSTOMER,
                "Thank you! I'll wait for the setup to complete."));
        chatAreaView.addCustomerMessage(request, "Thank you! I'll wait for the setup to complete.");
    }

    private int calculateProvisioningDelay() {
        long deploymentTime = DEPLOY_TIMES[Math.max(0, Math.min(deployLevel - 1, DEPLOY_TIMES.length - 1))];
        int minDelay = 15;
        int maxDelay = 60;
        int provisioningDelay = minDelay + random.nextInt(maxDelay - minDelay + 1);
        return Math.max(minDelay, (int)(provisioningDelay * (deploymentTime / 10000.0)));
    }

    private void sendVMDetails(CustomerRequest request, VPSOptimization.VM vm) {
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

    public void setDeployLevel(int deployLevel) {
        this.deployLevel = deployLevel;
    }
}
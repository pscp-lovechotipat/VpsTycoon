package com.vpstycoon.ui.game.desktop.messenger.models;

import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.company.SkillPointsSystem;
import com.vpstycoon.game.manager.CustomerRequest;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.game.vps.VPSOptimization;
import com.vpstycoon.ui.game.desktop.messenger.MessageType;
import com.vpstycoon.ui.game.desktop.messenger.views.ChatAreaView;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.*;

public class VMProvisioningManager {
    private final ChatHistoryManager chatHistoryManager;
    private final ChatAreaView chatAreaView;
    private final Map<CustomerRequest, ProgressBar> provisioningProgressBars;
    private final Random random = new Random();
    private final long[] DEPLOY_TIMES = {10000, 5000, 2000, 1000};
    
    
    private final VMProvisioningManagerImpl gameVMProvisioningManager;
    
    private final SkillPointsSystem skillPointsSystem;

    public VMProvisioningManager(ChatHistoryManager chatHistoryManager, ChatAreaView chatAreaView,
                                 Map<CustomerRequest, ProgressBar> provisioningProgressBars) {
        this.chatHistoryManager = chatHistoryManager;
        this.chatAreaView = chatAreaView;
        this.provisioningProgressBars = provisioningProgressBars;
        
        
        Company company = ResourceManager.getInstance().getCompany();
        this.gameVMProvisioningManager = new VMProvisioningManagerImpl(company);
        
        
        this.skillPointsSystem = ResourceManager.getInstance().getSkillPointsSystem();
    }

    public void startVMProvisioning(CustomerRequest request, VPSOptimization.VM vm, Runnable onComplete) {
        chatAreaView.addSystemMessage("Starting VM provisioning...");
        sendInitialMessages(request);

        
        int provisioningDelay = calculateProvisioningDelay();
        final int[] progress = {0};
        final int totalSteps = provisioningDelay * 10;
        final long startTime = System.currentTimeMillis();

        
        HBox progressContainer = new HBox();
        progressContainer.setAlignment(Pos.CENTER);
        progressContainer.setPadding(new Insets(10, 0, 10, 0));
        progressContainer.getStyleClass().add("message-container");

        VBox progressBox = new VBox(5);
        progressBox.setAlignment(Pos.CENTER);
        progressBox.setPadding(new Insets(10));
        progressBox.setStyle("-fx-background-color: rgba(52, 152, 219, 0.2); -fx-padding: 10px; -fx-border-radius: 5px; -fx-background-radius: 5px;");

        Label timeRemainingLabel = new Label("Starting VM provisioning in " + provisioningDelay + " seconds...");
        timeRemainingLabel.setStyle("-fx-text-fill: white;");
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(200);
        progressBar.setStyle("-fx-accent: #3498db;");

        progressBox.getChildren().addAll(timeRemainingLabel, progressBar);
        progressContainer.getChildren().add(progressBox);

        Platform.runLater(() -> chatAreaView.getMessagesBox().getChildren().add(progressContainer));
        provisioningProgressBars.put(request, progressBar);

        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("startTime", startTime);
        metadata.put("provisioningDelay", provisioningDelay);
        metadata.put("isProvisioning", true);
        chatHistoryManager.addMessage(request, new ChatMessage(MessageType.SYSTEM, "Starting VM provisioning...", metadata));

        
        
        Timer progressTimer = new Timer();
        progressTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long elapsedTime = System.currentTimeMillis() - startTime;
                int remainingSeconds = (int) Math.max(0, provisioningDelay - (elapsedTime / 1000));
                Platform.runLater(() -> {
                    timeRemainingLabel.setText("Starting VM provisioning in " + remainingSeconds + " seconds...");
                    progress[0]++;
                    double progressValue = (double) progress[0] / totalSteps;
                    progressBar.setProgress(progressValue);

                    if (progress[0] >= totalSteps) {
                        this.cancel();
                        provisioningProgressBars.remove(request);
                        
                        
                        sendVMDetails(request, vm);
                        timeRemainingLabel.setText("VM provisioning completed.");
                        chatHistoryManager.addMessage(request, new ChatMessage(MessageType.SYSTEM, "VM provisioning completed.", new HashMap<>()));
                        chatAreaView.addSystemMessage("VM provisioning completed.");
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    }
                });
            }
        }, 0, 100); 
    }
    
    
    private VPSOptimization findVPSForVM(VPSOptimization.VM targetVM) {
        ResourceManager resourceManager = ResourceManager.getInstance();
        for (VPSOptimization vps : resourceManager.getRack().getInstalledVPS()) {
            for (VPSOptimization.VM vm : vps.getVms()) {
                if (vm.getIp().equals(targetVM.getIp()) && vm.getName().equals(targetVM.getName())) {
                    return vps;
                }
            }
        }
        return null;
    }
    
    
    private int parseRAMValue(String ramString) {
        try {
            return Integer.parseInt(ramString.split(" ")[0]);
        } catch (Exception e) {
            return 2; 
        }
    }
    
    
    private int parseDiskValue(String diskString) {
        try {
            return Integer.parseInt(diskString.split(" ")[0]);
        } catch (Exception e) {
            return 20; 
        }
    }

    private void sendInitialMessages(CustomerRequest request) {
        chatHistoryManager.addMessage(request, new ChatMessage(MessageType.USER,
                "I'll assign your VM right away. Please wait while we set it up for you.", new HashMap<>()));
        chatAreaView.addUserMessage("I'll assign your VM right away. Please wait while we set it up for you.");
        chatHistoryManager.addMessage(request, new ChatMessage(MessageType.CUSTOMER,
                "Thank you! I'll wait for the setup to complete.", new HashMap<>()));
        chatAreaView.addCustomerMessage(request, "Thank you! I'll wait for the setup to complete.");
    }

    private int calculateProvisioningDelay() {
        
        int deployLevel = skillPointsSystem.getSkillLevel(SkillPointsSystem.SkillType.DEPLOY);
        
        long deploymentTime = DEPLOY_TIMES[Math.max(0, Math.min(deployLevel - 1, DEPLOY_TIMES.length - 1))];
        
        int minDelay = 5;
        int maxDelay = 30;
        int provisioningDelay = minDelay + random.nextInt(maxDelay - minDelay + 1);
        
        
        double reduction = skillPointsSystem.getDeploymentTimeReduction();
        return Math.max(minDelay, (int)(provisioningDelay * (1.0 - reduction)));
    }

    private void sendVMDetails(CustomerRequest request, VPSOptimization.VM vm) {
        String username = "user_" + request.getName().toLowerCase().replaceAll("[^a-z0-9]", "") + random.nextInt(100);
        String password = generateRandomPassword();
        String vmDetails = "Your VM has been assigned successfully! Here are your access details:\n\n" +
                "IP Address: " + vm.getIp() + "\n" +
                "Username: " + username + "\n" +
                "Password: " + password + "\n\n" +
                "You can connect using SSH or RDP depending on your operating system.";
        chatHistoryManager.addMessage(request, new ChatMessage(MessageType.USER, vmDetails, new HashMap<>()));
        chatAreaView.addUserMessage(vmDetails);
        chatHistoryManager.addMessage(request, new ChatMessage(MessageType.CUSTOMER,
                "Thank you! I've received the VM details and will start using it right away.", new HashMap<>()));
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
        
    }

    
    public double calculateRatingChange(
            CustomerRequest request, 
            int providedVCPUs, 
            int providedRamGB, 
            int providedDiskGB) {
        
        Company company = ResourceManager.getInstance().getCompany();
        
        
        int requiredVCPUs = request.getRequiredVCPUs();
        int requiredRamGB = request.getRequiredRamGB();
        int requiredDiskGB = request.getRequiredDiskGB();
        
        
        double ratingChange = company.calculateVMAssignmentRatingChange(
            requiredVCPUs, requiredRamGB, requiredDiskGB,
            providedVCPUs, providedRamGB, providedDiskGB
        );
        
        
        switch (request.getCustomerType()) {
            case INDIVIDUAL:
                ratingChange *= 1.2; 
                break;
            case SMALL_BUSINESS:
                ratingChange *= 1.1;
                break;
            case MEDIUM_BUSINESS:
                ratingChange *= 1.0; 
                break;
            case LARGE_BUSINESS:
                ratingChange *= 0.9;
                break;
            case ENTERPRISE:
            case BUSINESS:
                ratingChange *= 0.8; 
                break;
        }
        
        
        return Math.max(-0.5, Math.min(0.5, ratingChange));
    }
}

package com.vpstycoon.ui.game.desktop.messenger.models;

import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.manager.CustomerRequest;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.game.thread.GameTimeManager;
import com.vpstycoon.game.vps.VPSOptimization;
import com.vpstycoon.ui.game.desktop.messenger.MessageType;
import com.vpstycoon.ui.game.desktop.messenger.views.ChatAreaView;
import javafx.application.Platform;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RentalManager {
    private final ChatHistoryManager chatHistoryManager;
    private final ChatAreaView chatAreaView;
    private final Company company;
    private final GameTimeManager gameTimeManager;
    private final Random random = new Random();
    private Runnable onArchiveRequest; // Callback to archive request in MessengerController
    private Map<VPSOptimization.VM, CustomerRequest> vmAssignments; // Reference to VM assignments
    private Runnable onUpdateDashboard; // Callback to update dashboard in MessengerController

    public RentalManager(ChatHistoryManager chatHistoryManager, ChatAreaView chatAreaView,
                         Company company, GameTimeManager gameTimeManager) {
        this.chatHistoryManager = chatHistoryManager;
        this.chatAreaView = chatAreaView;
        this.company = company;
        this.gameTimeManager = gameTimeManager;
        setupTimeListener();
    }

    private void setupTimeListener() {
        gameTimeManager.addTimeListener(new GameTimeManager.GameTimeListener() {
            @Override
            public void onTimeChanged(LocalDateTime newTime, long gameTimeMs) {}

            @Override
            public void onRentalPeriodCheck(CustomerRequest request, CustomerRequest.RentalPeriodType period) {
                handleRentalExpiration(request, period);
            }
        });
    }

    public void setupRentalPeriod(CustomerRequest request, VPSOptimization.VM vm) {
        if (request == null || vm == null) return;

        request.activate(ResourceManager.getInstance().getGameTimeManager().getGameTimeMs()); // Activate เริ่มนับเวลา

        String rentalMessage = "VM assigned for " + request.getRentalPeriodType().getDisplayName() + " rental period";
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("startTime", request.getLastPaymentTime());
        chatHistoryManager.addMessage(request, new ChatMessage(MessageType.SYSTEM, rentalMessage, metadata));
        chatAreaView.addSystemMessage(rentalMessage);

        double paymentAmount = request.getPaymentAmount();
        company.addMoney(paymentAmount);
        chatHistoryManager.addMessage(request, new ChatMessage(MessageType.SYSTEM,
                "Received payment of $" + String.format("%.2f", paymentAmount), new HashMap<>()));
        chatAreaView.addSystemMessage("Received payment of $" + String.format("%.2f", paymentAmount));
    }

    private void handleRentalExpiration(CustomerRequest request, CustomerRequest.RentalPeriodType currentPeriod) {
        Platform.runLater(() -> {
            double renewalProbability = calculateRenewalProbability();
            boolean willRenew = random.nextDouble() < renewalProbability;

            if (willRenew) {
                CustomerRequest.RentalPeriodType newPeriod = random.nextBoolean() ?
                        getRandomPeriod() : currentPeriod;

                chatHistoryManager.addMessage(request, new ChatMessage(MessageType.SYSTEM,
                        "Customer has decided to renew their contract with " + newPeriod.getDisplayName() + " period!",
                        new HashMap<>()));
                chatAreaView.addSystemMessage("Customer has decided to renew their contract with " +
                        newPeriod.getDisplayName() + " period!");

                request.setRentalPeriodType(newPeriod); // อัพเดท period ใหม่
                VPSOptimization.VM vm = getAssignedVM(request); // ต้องมีวิธีหา VM
                if (vm != null) {
                    setupRentalPeriod(request, vm);
                }
            } else {
                chatHistoryManager.addMessage(request, new ChatMessage(MessageType.SYSTEM,
                        "Customer has decided not to renew their contract. Request will be archived.",
                        new HashMap<>()));
                chatAreaView.addSystemMessage("Customer has decided not to renew their contract. Request will be archived.");
                
                // Find and release the VM associated with this request 
                if (vmAssignments != null) {
                    VPSOptimization.VM vmToRelease = null;
                    for (Map.Entry<VPSOptimization.VM, CustomerRequest> entry : vmAssignments.entrySet()) {
                        if (entry.getValue() == request) {
                            vmToRelease = entry.getKey();
                            break;
                        }
                    }
                    
                    if (vmToRelease != null) {
                        // Release VM from assignment to make it available
                        vmAssignments.remove(vmToRelease);
                        chatHistoryManager.addMessage(request, new ChatMessage(MessageType.SYSTEM,
                                "VM has been released back to the available pool", new HashMap<>()));
                        chatAreaView.addSystemMessage("VM has been released back to the available pool");
                        
                        // Update dashboard to reflect the newly available VM
                        if (onUpdateDashboard != null) {
                            onUpdateDashboard.run();
                        }
                    }
                }
                
                if (onArchiveRequest != null) {
                    onArchiveRequest.run(); // เรียก callback เพื่อ archive
                }
            }
        });
    }

    private CustomerRequest.RentalPeriodType getRandomPeriod() {
        CustomerRequest.RentalPeriodType[] periods = CustomerRequest.RentalPeriodType.values();
        return periods[random.nextInt(periods.length)];
    }

    private double calculateRenewalProbability() {
        double baseProbability = 0.5;
        double ratingFactor = company.getRating() * 0.1;
        return Math.max(0.1, Math.min(0.95, baseProbability + ratingFactor));
    }

    // Method เพื่อหา VM ที่ถูก assign ให้ request
    private VPSOptimization.VM getAssignedVM(CustomerRequest request) {
        if (vmAssignments != null) {
            for (Map.Entry<VPSOptimization.VM, CustomerRequest> entry : vmAssignments.entrySet()) {
                if (entry.getValue() == request) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    // Setter สำหรับ callback
    public void setOnArchiveRequest(Runnable onArchiveRequest) {
        this.onArchiveRequest = onArchiveRequest;
    }

    // Getter เพื่อให้ MessengerController สามารถหา VM ได้
    public void setVMAssignment(Map<VPSOptimization.VM, CustomerRequest> vmAssignments) {
        this.vmAssignments = vmAssignments;
    }

    // Setter for update dashboard callback
    public void setOnUpdateDashboard(Runnable onUpdateDashboard) {
        this.onUpdateDashboard = onUpdateDashboard;
    }
}
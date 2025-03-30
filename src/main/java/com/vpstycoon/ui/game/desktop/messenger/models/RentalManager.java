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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RentalManager {
    private final ChatHistoryManager chatHistoryManager;
    private final ChatAreaView chatAreaView;
    private final Company company;
    private final GameTimeManager gameTimeManager;
    private final Random random = new Random();
    private GameTimeManager.GameTimeListener timeListener;
    private Runnable onArchiveRequest; 
    private Map<VPSOptimization.VM, CustomerRequest> vmAssignments; 
    private Runnable onUpdateDashboard; 

    public RentalManager(ChatHistoryManager chatHistoryManager, ChatAreaView chatAreaView,
                         Company company, GameTimeManager gameTimeManager) {
        this.chatHistoryManager = chatHistoryManager;
        this.chatAreaView = chatAreaView;
        this.company = company;
        this.gameTimeManager = gameTimeManager;
        setupTimeListener();
    }

    private void setupTimeListener() {
        timeListener = new GameTimeManager.GameTimeListener() {
            @Override
            public void onTimeChanged(LocalDateTime newTime, long gameTimeMs) {}

            @Override
            public void onRentalPeriodCheck(CustomerRequest request, CustomerRequest.RentalPeriodType period) {
                handleRentalExpiration(request, period);
            }
        };
        gameTimeManager.addTimeListener(timeListener);
    }

    public void setupRentalPeriod(CustomerRequest request, VPSOptimization.VM vm) {
        if (request == null || vm == null) return;

        request.activate(ResourceManager.getInstance().getGameTimeManager().getGameTimeMs()); 

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

                request.setRentalPeriodType(newPeriod); 
                VPSOptimization.VM vm = getAssignedVM(request); 
                if (vm != null) {
                    setupRentalPeriod(request, vm);
                }
            } else {
                
                chatHistoryManager.addMessage(request, new ChatMessage(MessageType.SYSTEM,
                        "Customer has decided not to renew their contract.",
                        new HashMap<>()));
                chatAreaView.addSystemMessage("Customer has decided not to renew their contract.");
                
                
                
                
                if (!request.isExpired()) {
                    request.markAsExpired();
                    System.out.println("RentalManager: ทำการ mark request เป็น expired: " + request.getName());
                }
                
                
                
                
                
                
                if (onUpdateDashboard != null) {
                    onUpdateDashboard.run();
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

    
    public void setOnArchiveRequest(Runnable onArchiveRequest) {
        this.onArchiveRequest = onArchiveRequest;
    }

    
    public void setVMAssignment(Map<VPSOptimization.VM, CustomerRequest> vmAssignments) {
        this.vmAssignments = vmAssignments;
    }

    
    public void setOnUpdateDashboard(Runnable onUpdateDashboard) {
        this.onUpdateDashboard = onUpdateDashboard;
    }
    
    
    public void detachFromTimeManager() {
        if (gameTimeManager != null && timeListener != null) {
            gameTimeManager.removeTimeListener(timeListener);
            System.out.println("Removed RentalManager listener from GameTimeManager");
        }
    }
}

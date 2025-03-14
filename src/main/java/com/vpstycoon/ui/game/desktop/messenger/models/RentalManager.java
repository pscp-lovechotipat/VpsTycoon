package com.vpstycoon.ui.game.desktop.messenger.models;

import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.manager.CustomerRequest;
import com.vpstycoon.game.vps.VPSOptimization;
import com.vpstycoon.ui.game.desktop.messenger.views.ChatAreaView;
import com.vpstycoon.ui.game.desktop.messenger.MessageType;
import javafx.application.Platform;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class RentalManager {
    private final ChatHistoryManager chatHistoryManager;
    private final ChatAreaView chatAreaView;
    private final Company company;
    private final Map<CustomerRequest, Date> rentalEndDates = new HashMap<>();
    private final Map<CustomerRequest, Timer> rentalTimers = new HashMap<>();
    private final Random random = new Random();
    private static final long GAME_MONTH_IN_MINUTES = 15;
    private static final long MILLISECONDS_PER_GAME_DAY = (GAME_MONTH_IN_MINUTES * 60 * 1000) / 30;

    public RentalManager(ChatHistoryManager chatHistoryManager, ChatAreaView chatAreaView, Company company) {
        this.chatHistoryManager = chatHistoryManager;
        this.chatAreaView = chatAreaView;
        this.company = company;
    }

    public void setupRentalPeriod(CustomerRequest request, VPSOptimization.VM vm) {
        if (request == null || vm == null) return;

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.add(java.util.Calendar.MILLISECOND, (int)(request.getRentalPeriod() * MILLISECONDS_PER_GAME_DAY));
        Date endDate = calendar.getTime();
        rentalEndDates.put(request, endDate);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    double renewalProbability = calculateRenewalProbability();
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
                        // releaseVM logic จะถูกเรียกจาก controller
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

    private double calculateRenewalProbability() {
        double baseProbability = 0.5;
        double ratingFactor = company.getRating() * 0.1;
        double finalProbability = baseProbability + ratingFactor;
        return Math.max(0.1, Math.min(0.95, finalProbability));
    }

    public void cancelRental(CustomerRequest request) {
        Timer timer = rentalTimers.remove(request);
        if (timer != null) timer.cancel();
        rentalEndDates.remove(request);
    }

    public Map<CustomerRequest, Date> getRentalEndDates() {
        return rentalEndDates;
    }

    public Map<CustomerRequest, Timer> getRentalTimers() {
        return rentalTimers;
    }
}
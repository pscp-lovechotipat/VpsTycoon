package com.vpstycoon.game.thread;

import com.vpstycoon.event.EventType;
import com.vpstycoon.game.GameState;
import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.ui.game.GameplayContentPane;
import java.util.Random;
import javafx.application.Platform;

public class GameEvent implements Runnable {
    public static final int INITIAL_DELAY = 15 * 60 * 1000; // 15 minutes in milliseconds
    private static final int MIN_INTERVAL = 5 * 60 * 1000; // 3 minutes in milliseconds
    private static final int MAX_INTERVAL = 30 * 60 * 1000; // 10 minutes in milliseconds

    private final GameplayContentPane gameplayContentPane;
    private final ResourceManager resourceManager;

    private final GameState gameState;
    private final Random random;
    private volatile boolean isRunning;

    public GameEvent(GameplayContentPane gameplayContentPane, GameState gameState) {
        this.gameplayContentPane = gameplayContentPane;
        this.gameState = gameState;
        this.random = new Random();
        this.isRunning = false;
        this.resourceManager = ResourceManager.getInstance();
    }

    @Override
    public void run() {
        isRunning = true;
        try {
            // Initial delay of 15 minutes
            Thread.sleep(INITIAL_DELAY);

            while (isRunning) {
                triggerEvent();
                // Random interval between 3-10 minutes
                long nextInterval = MIN_INTERVAL + random.nextInt(MAX_INTERVAL - MIN_INTERVAL);
                Thread.sleep(nextInterval);
            }
        } catch (InterruptedException e) {
            isRunning = false;
            Thread.currentThread().interrupt();
        }
    }

    private void triggerEvent() {
        EventType[] events = EventType.values();
        EventType event = events[random.nextInt(events.length)];
        Company company = gameState.getCompany();
        long cost = event.calculateCost(random);

        String result;
        String financialImpact = "";
        if (event == EventType.SPECIAL_EVENT) {
            // Positive event
            long bonus = Math.round(company.getMoney() * 0.1); // 10% bonus
            company.setMoney(company.getMoney() + bonus);
            result = "Gained $" + bonus + " from a special event!";
            financialImpact = "Money Gained: $" + bonus;
        } else {
            // Negative event
            if (company.getMoney() >= cost) {
                company.setMoney(company.getMoney() - cost);
                result = "Lost $" + cost + " due to " + event.getDisplayName();
                financialImpact = "Money Deducted: $" + cost;
            } else {
                // Reduce rating if company can't afford the cost
                double newRating = Math.max(1.0, company.getRating() - 0.5);
                company.setRating(newRating);
                result = "Could not afford $" + cost + ". Rating dropped to " + String.format("%.1f", newRating);
                financialImpact = "Attempted Deduction: $" + cost + "\nRating Dropped to: " + String.format("%.1f", newRating);
            }
        }

        // Show notification
        System.out.println("/images/notification/" + event.getDisplayName() + ".png");
        resourceManager.pushCenterNotification(
                event.getDisplayName(),
                event.getSolution() + "\n\n" + result + "\n" + financialImpact,
                "/images/notification/" + event.getDisplayName() + ".png"
        );
    }

    public void stopEvent() {
        isRunning = false;
    }

    public boolean isRunning() {
        return isRunning;
    }
}
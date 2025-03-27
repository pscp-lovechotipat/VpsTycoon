package com.vpstycoon.game.event;

import com.vpstycoon.game.GameState;
import com.vpstycoon.game.company.Company;
import com.vpstycoon.ui.game.GameplayContentPane;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * System for generating and handling random events in the game
 */
public class RandomEventSystem {
    private final GameplayContentPane gameplayContentPane;
    private final GameState gameState;
    private final Random random = new Random();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private boolean isRunning = false;
    
    // List of possible events
    private final List<RandomEvent> possibleEvents = new ArrayList<>();
    
    public RandomEventSystem(GameplayContentPane gameplayContentPane, GameState gameState) {
        this.gameplayContentPane = gameplayContentPane;
        this.gameState = gameState;
        
        // Initialize possible events
        initializeEvents();
    }
    
    /**
     * Initialize the list of possible random events
     */
    private void initializeEvents() {
        // Security events
        possibleEvents.add(new RandomEvent(
                "Security Breach",
                "Your systems have been compromised! You need to pay for emergency security services.",
                EventType.NEGATIVE,
                (company) -> {
                    long cost = Math.round(company.getMoney() * 0.1); // 10% of current money
                    company.spendMoney(cost);
                    return "Lost $" + cost + " to security breach";
                }
        ));
        
        // Hardware events
        possibleEvents.add(new RandomEvent(
                "Hardware Failure",
                "One of your servers has experienced a hardware failure and needs repairs.",
                EventType.NEGATIVE,
                (company) -> {
                    long cost = Math.round(company.getMoney() * 0.05); // 5% of current money
                    company.spendMoney(cost);
                    return "Lost $" + cost + " to hardware repairs";
                }
        ));
        
        // Power events
        possibleEvents.add(new RandomEvent(
                "Power Outage",
                "A power outage has affected your data center. Emergency generators are running.",
                EventType.NEGATIVE,
                (company) -> {
                    long cost = Math.round(company.getMoney() * 0.03); // 3% of current money
                    company.spendMoney(cost);
                    return "Lost $" + cost + " due to power outage";
                }
        ));
        
        // Network events
        possibleEvents.add(new RandomEvent(
                "Network Congestion",
                "Your network is experiencing severe congestion, affecting service quality.",
                EventType.NEGATIVE,
                (company) -> {
                    // Reduce company rating
                    double newRating = Math.max(1.0, company.getRating() - 0.2);
                    company.setRating(newRating);
                    return "Company rating decreased to " + String.format("%.1f", newRating);
                }
        ));
        
        // Positive events
        possibleEvents.add(new RandomEvent(
                "Industry Award",
                "Your company has received an industry award for excellent service!",
                EventType.POSITIVE,
                (company) -> {
                    // Increase company rating
                    double newRating = Math.min(5.0, company.getRating() + 0.3);
                    company.setRating(newRating);
                    return "Company rating increased to " + String.format("%.1f", newRating);
                }
        ));
        
        possibleEvents.add(new RandomEvent(
                "Investor Interest",
                "An investor is interested in your company and has provided some funding.",
                EventType.POSITIVE,
                (company) -> {
                    long bonus = Math.round(company.getMoney() * 0.15); // 15% of current money
                    company.addMoney(bonus);
                    return "Received $" + bonus + " from investor";
                }
        ));
        
        possibleEvents.add(new RandomEvent(
                "Free Marketing",
                "Your company was featured in a tech magazine, providing free marketing!",
                EventType.POSITIVE,
                (company) -> {
                    int points = random.nextInt(5) + 1; // 1-5 points
                    company.setMarketingPoints(company.getMarketingPoints() + points);
                    return "Gained " + points + " marketing points";
                }
        ));
    }
    
    /**
     * Start the random event system
     */
    public void start() {
        if (!isRunning) {
            isRunning = true;
            
            // Schedule random events to occur periodically
            scheduler.scheduleAtFixedRate(this::triggerRandomEvent, 
                    getInitialDelay(), 
                    getEventInterval(), 
                    TimeUnit.SECONDS);
        }
    }
    
    /**
     * Stop the random event system
     */
    public void stop() {
        if (isRunning) {
            isRunning = false;
            scheduler.shutdown();
        }
    }
    
    /**
     * Trigger a random event
     */
    private void triggerRandomEvent() {
        // Only trigger events with a certain probability
        if (random.nextDouble() < 0.7) { // 70% chance to trigger an event
            // Select a random event
            RandomEvent event = possibleEvents.get(random.nextInt(possibleEvents.size()));
            
            // Apply the event effect
            Company company = gameState.getCompany();
            String result = event.getEffect().apply(company);
            
            // Show notification to the player
            Platform.runLater(() -> {
                gameplayContentPane.pushNotification(
                        event.getTitle(),
                        event.getDescription() + "\n\n" + result
                );
            });
        }
    }
    
    /**
     * Get the initial delay before the first event
     * @return Delay in seconds
     */
    private long getInitialDelay() {
        // Start events after 2-5 minutes of gameplay
        return 120 + random.nextInt(180);
    }
    
    /**
     * Get the interval between events
     * @return Interval in seconds
     */
    private long getEventInterval() {
        // Events occur every 3-10 minutes
        return 180 + random.nextInt(420);
    }
    
    /**
     * Event types
     */
    public enum EventType {
        POSITIVE,
        NEGATIVE,
        NEUTRAL
    }
    
    /**
     * Class representing a random event
     */
    public static class RandomEvent {
        private final String title;
        private final String description;
        private final EventType type;
        private final EventEffect effect;
        
        public RandomEvent(String title, String description, EventType type, EventEffect effect) {
            this.title = title;
            this.description = description;
            this.type = type;
            this.effect = effect;
        }
        
        public String getTitle() {
            return title;
        }
        
        public String getDescription() {
            return description;
        }
        
        public EventType getType() {
            return type;
        }
        
        public EventEffect getEffect() {
            return effect;
        }
    }
    
    /**
     * Functional interface for event effects
     */
    @FunctionalInterface
    public interface EventEffect {
        /**
         * Apply the effect of an event to a company
         * @param company The company to affect
         * @return A description of what happened
         */
        String apply(Company company);
    }
} 
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


public class RandomEventSystem {
    private final GameplayContentPane gameplayContentPane;
    private final GameState gameState;
    private final Random random = new Random();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private boolean isRunning = false;
    
    
    private final List<RandomEvent> possibleEvents = new ArrayList<>();
    
    public RandomEventSystem(GameplayContentPane gameplayContentPane, GameState gameState) {
        this.gameplayContentPane = gameplayContentPane;
        this.gameState = gameState;
        
        
        initializeEvents();
    }
    
    
    private void initializeEvents() {
        
        possibleEvents.add(new RandomEvent(
                "Security Breach",
                "Your systems have been compromised! You need to pay for emergency security services.",
                EventType.NEGATIVE,
                (company) -> {
                    long cost = Math.round(company.getMoney() * 0.1); 
                    company.spendMoney(cost);
                    return "Lost $" + cost + " to security breach";
                }
        ));
        
        
        possibleEvents.add(new RandomEvent(
                "Hardware Failure",
                "One of your servers has experienced a hardware failure and needs repairs.",
                EventType.NEGATIVE,
                (company) -> {
                    long cost = Math.round(company.getMoney() * 0.05); 
                    company.spendMoney(cost);
                    return "Lost $" + cost + " to hardware repairs";
                }
        ));
        
        
        possibleEvents.add(new RandomEvent(
                "Power Outage",
                "A power outage has affected your data center. Emergency generators are running.",
                EventType.NEGATIVE,
                (company) -> {
                    long cost = Math.round(company.getMoney() * 0.03); 
                    company.spendMoney(cost);
                    return "Lost $" + cost + " due to power outage";
                }
        ));
        
        
        possibleEvents.add(new RandomEvent(
                "Network Congestion",
                "Your network is experiencing severe congestion, affecting service quality.",
                EventType.NEGATIVE,
                (company) -> {
                    
                    double newRating = Math.max(1.0, company.getRating() - 0.2);
                    company.setRating(newRating);
                    return "Company rating decreased to " + String.format("%.1f", newRating);
                }
        ));
        
        
        possibleEvents.add(new RandomEvent(
                "Industry Award",
                "Your company has received an industry award for excellent service!",
                EventType.POSITIVE,
                (company) -> {
                    
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
                    long bonus = Math.round(company.getMoney() * 0.15); 
                    company.addMoney(bonus);
                    return "Received $" + bonus + " from investor";
                }
        ));
        
        possibleEvents.add(new RandomEvent(
                "Free Marketing",
                "Your company was featured in a tech magazine, providing free marketing!",
                EventType.POSITIVE,
                (company) -> {
                    int points = random.nextInt(5) + 1; 
                    company.setMarketingPoints(company.getMarketingPoints() + points);
                    return "Gained " + points + " marketing points";
                }
        ));
    }
    
    
    public void start() {
        if (!isRunning) {
            isRunning = true;
            
            
            scheduler.scheduleAtFixedRate(this::triggerRandomEvent, 
                    getInitialDelay(), 
                    getEventInterval(), 
                    TimeUnit.SECONDS);
        }
    }
    
    
    public void stop() {
        if (isRunning) {
            isRunning = false;
            scheduler.shutdown();
        }
    }
    
    
    private void triggerRandomEvent() {
        
        if (random.nextDouble() < 0.7) { 
            
            RandomEvent event = possibleEvents.get(random.nextInt(possibleEvents.size()));
            
            
            Company company = gameState.getCompany();
            String result = event.getEffect().apply(company);
            
            
            Platform.runLater(() -> {
                gameplayContentPane.pushNotification(
                        event.getTitle(),
                        event.getDescription() + "\n\n" + result
                );
            });
        }
    }
    
    
    private long getInitialDelay() {
        
        return 120 + random.nextInt(180);
    }
    
    
    private long getEventInterval() {
        
        return 180 + random.nextInt(420);
    }
    
    
    public enum EventType {
        POSITIVE,
        NEGATIVE,
        NEUTRAL
    }
    
    
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
    
    
    @FunctionalInterface
    public interface EventEffect {
        
        String apply(Company company);
    }
} 

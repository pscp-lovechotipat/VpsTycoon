package com.vpstycoon.model.time;

import com.vpstycoon.model.time.interfaces.IGameTime;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicLong;


public class GameTimeModel implements IGameTime {
    public static final long DEFAULT_GAME_DAY_MS = 30000; 
    
    private final LocalDateTime startDateTime;
    private LocalDateTime currentDateTime;
    private final AtomicLong gameTimeMs = new AtomicLong(0);
    private double timeScale = 86400000.0 / DEFAULT_GAME_DAY_MS; 

    
    public GameTimeModel() {
        this(LocalDateTime.of(2000, 1, 1, 0, 0, 0));
    }

    
    public GameTimeModel(LocalDateTime startTime) {
        this.startDateTime = startTime;
        this.currentDateTime = startTime;
    }

    
    @Override
    public LocalDateTime getCurrentDateTime() {
        return currentDateTime;
    }

    
    @Override
    public void setCurrentDateTime(LocalDateTime dateTime) {
        this.currentDateTime = dateTime;
    }

    
    @Override
    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    
    @Override
    public long getGameTimeMs() {
        return gameTimeMs.get();
    }

    
    @Override
    public void setGameTimeMs(long timeMs) {
        gameTimeMs.set(timeMs);
        currentDateTime = calculateGameTime(timeMs);
    }

    
    @Override
    public LocalDateTime calculateGameTime(long realTimeMs) {
        long gameMs = (long) (realTimeMs * timeScale);
        return startDateTime.plus(gameMs, ChronoUnit.MILLIS);
    }

    
    @Override
    public double getTimeScale() {
        return timeScale;
    }

    
    @Override
    public void setTimeScale(double scale) {
        this.timeScale = scale;
    }

    
    public void addRealTimeMs(long elapsedRealMs) {
        gameTimeMs.addAndGet(elapsedRealMs);
        currentDateTime = calculateGameTime(gameTimeMs.get());
    }

    
    public void resetTime() {
        gameTimeMs.set(0);
        currentDateTime = startDateTime;
    }
} 

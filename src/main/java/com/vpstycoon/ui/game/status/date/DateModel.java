package com.vpstycoon.ui.game.status.date;

import com.vpstycoon.audio.AudioManager;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.ui.game.GameplayContentPane;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.Duration;

import java.time.LocalDateTime;

public class DateModel {
    private final GameplayContentPane parent;
    private final ObjectProperty<LocalDateTime> date = new SimpleObjectProperty<>();
    private final StringProperty timeRemaining = new SimpleStringProperty();
    private int lastMonth = -1; 
    private AudioManager audioManager;

    public DateModel(LocalDateTime initialDate, GameplayContentPane parent) {
        this.parent = parent;
        this.date.set(initialDate);
        this.lastMonth = initialDate.getMonthValue();
        this.audioManager = ResourceManager.getInstance().getAudioManager();
        updateTimeRemaining();
    }

    public LocalDateTime getDate() {
        return date.get();
    }

    public void setDate(LocalDateTime date) {
        int currentMonth = date.getMonthValue();

        
        if  (currentMonth != lastMonth && lastMonth != -1)  {
            audioManager.playSoundEffect("month.mp3");
            System.out.println("Next Month");
            Platform.runLater(() ->
                    parent.pushCenterNotification("NEW MONTH!!", "Welcome to new month!", "/images/others/meme.gif")
            );
            lastMonth = currentMonth; 
        }

        this.date.set(date);
        updateTimeRemaining();
    }

    public ObjectProperty<LocalDateTime> dateProperty() {
        return date;
    }

    public StringProperty timeRemainingProperty() {
        return timeRemaining;
    }

    private void updateTimeRemaining() {
        
        LocalDateTime currentDate = date.get();
        LocalDateTime nextDay = currentDate.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        Duration durationSinceMidnight = Duration.between(currentDate.withHour(0).withMinute(0).withSecond(0).withNano(0), currentDate);
        long secondsSinceMidnight = durationSinceMidnight.getSeconds(); 
        long totalSecondsInGameDay = 86400; 
        long secondsRemainingInGame = totalSecondsInGameDay - secondsSinceMidnight;

        
        double scaleFactor = 30.0 / totalSecondsInGameDay; 
        long secondsRemainingReal = (long) (secondsRemainingInGame * scaleFactor);

        timeRemaining.set(secondsRemainingReal + "s");
    }
}

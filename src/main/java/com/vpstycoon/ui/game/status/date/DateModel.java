package com.vpstycoon.ui.game.status.date;

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
    private int lastMonth = -1; // เก็บเดือนก่อนหน้า

    public DateModel(LocalDateTime initialDate, GameplayContentPane parent) {
        this.parent = parent;
        this.date.set(initialDate);
        this.lastMonth = initialDate.getMonthValue();
        updateTimeRemaining();
    }

    public LocalDateTime getDate() {
        return date.get();
    }

    public void setDate(LocalDateTime date) {
        int currentMonth = date.getMonthValue();

        // ตรวจสอบว่าเดือนในเกมมีการเปลี่ยนแปลง
        if  (currentMonth != lastMonth && lastMonth != -1)  {
            System.out.println("Next Month");
            Platform.runLater(() ->
                    parent.pushCenterNotification("NEXT MONTH", "Welcome to new month", "/images/others/keroro_meme1.jpg")
            );
            lastMonth = currentMonth; // อัปเดตเดือนก่อนหน้า
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
        // คำนวณวินาทีที่เหลือจากจุดสิ้นสุดของวันในเกม (30 วินาทีจริง = 1 วัน)
        LocalDateTime currentDate = date.get();
        LocalDateTime nextDay = currentDate.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        Duration durationSinceMidnight = Duration.between(currentDate.withHour(0).withMinute(0).withSecond(0).withNano(0), currentDate);
        long secondsSinceMidnight = durationSinceMidnight.getSeconds(); // วินาทีที่ผ่านไปตั้งแต่เที่ยงคืนในเกม
        long totalSecondsInGameDay = 86400; // 86,400 วินาที = 1 วันในเกม
        long secondsRemainingInGame = totalSecondsInGameDay - secondsSinceMidnight;

        // แปลงวินาทีในเกมให้สัมพันธ์กับ 30 วินาทีจริง
        double scaleFactor = 30.0 / totalSecondsInGameDay; // 30 วินาทีจริง / 86,400 วินาทีในเกม
        long secondsRemainingReal = (long) (secondsRemainingInGame * scaleFactor);

        timeRemaining.set(secondsRemainingReal + "s");
    }
}
package com.vpstycoon.model.time;

import com.vpstycoon.model.time.interfaces.IGameTime;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ข้อมูลเวลาของเกม เก็บค่าเวลาปัจจุบันและสเกลเวลา
 */
public class GameTimeModel implements IGameTime {
    public static final long DEFAULT_GAME_DAY_MS = 30000; // 30 วินาทีต่อ 1 วันในเกม
    
    private final LocalDateTime startDateTime;
    private LocalDateTime currentDateTime;
    private final AtomicLong gameTimeMs = new AtomicLong(0);
    private double timeScale = 86400000.0 / DEFAULT_GAME_DAY_MS; // 1 วันจริง (ms) / 1 วันในเกม (ms)

    /**
     * สร้าง GameTimeModel ใหม่ด้วยค่าเริ่มต้น
     */
    public GameTimeModel() {
        this(LocalDateTime.of(2000, 1, 1, 0, 0, 0));
    }

    /**
     * สร้าง GameTimeModel ใหม่ด้วยเวลาเริ่มต้นที่กำหนด
     */
    public GameTimeModel(LocalDateTime startTime) {
        this.startDateTime = startTime;
        this.currentDateTime = startTime;
    }

    /**
     * ดึงค่าเวลาปัจจุบันในเกม
     */
    @Override
    public LocalDateTime getCurrentDateTime() {
        return currentDateTime;
    }

    /**
     * กำหนดค่าเวลาปัจจุบันในเกม
     */
    @Override
    public void setCurrentDateTime(LocalDateTime dateTime) {
        this.currentDateTime = dateTime;
    }

    /**
     * ดึงค่าเวลาเริ่มต้นของเกม
     */
    @Override
    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    /**
     * ดึงเวลาเกมในรูปแบบมิลลิวินาที (นับตั้งแต่เริ่มเกม)
     */
    @Override
    public long getGameTimeMs() {
        return gameTimeMs.get();
    }

    /**
     * กำหนดเวลาเกมในรูปแบบมิลลิวินาที
     */
    @Override
    public void setGameTimeMs(long timeMs) {
        gameTimeMs.set(timeMs);
        currentDateTime = calculateGameTime(timeMs);
    }

    /**
     * คำนวณเวลาเกมจากเวลาจริง
     */
    @Override
    public LocalDateTime calculateGameTime(long realTimeMs) {
        long gameMs = (long) (realTimeMs * timeScale);
        return startDateTime.plus(gameMs, ChronoUnit.MILLIS);
    }

    /**
     * ดึงความเร็วของเวลาเกมเทียบกับเวลาจริง
     */
    @Override
    public double getTimeScale() {
        return timeScale;
    }

    /**
     * กำหนดความเร็วของเวลาเกมเทียบกับเวลาจริง
     */
    @Override
    public void setTimeScale(double scale) {
        this.timeScale = scale;
    }

    /**
     * คำนวณเวลาที่จะผ่านไปในเกมเมื่อเวลาจริงผ่านไป elapsedRealMs
     */
    public void addRealTimeMs(long elapsedRealMs) {
        gameTimeMs.addAndGet(elapsedRealMs);
        currentDateTime = calculateGameTime(gameTimeMs.get());
    }

    /**
     * รีเซ็ตเวลาเกม
     */
    public void resetTime() {
        gameTimeMs.set(0);
        currentDateTime = startDateTime;
    }
} 
package com.vpstycoon.game.thread;

import com.vpstycoon.game.GameState;

public class GameTimeUpdater extends Thread{
    private GameState gameState;
    private volatile boolean running = true;

    public GameTimeUpdater(GameState gameState) {
        this.gameState = gameState;
    }

    @Override
    public void run() {
        while (running) {
            try {
                // รอ 15 นาทีจริง (15 * 60 * 1000 มิลลิวินาที)
                System.out.println("⏳ รอ 15 นาทีก่อนเปลี่ยนเดือน...");
                Thread.sleep(15 * 60 * 1000);
                // เปลี่ยนเดือนในเกม
                gameState.setLocalDateTime(gameState.getLocalDateTime().plusMonths(1));
                System.out.println("📅 เดือนในเกมเปลี่ยนเป็น: " + gameState.getLocalDateTime());
            } catch (InterruptedException e) {
                System.out.println("GameTimeUpdater interrupted, stopping...");
                running = false;
                Thread.currentThread().interrupt();
            }
        }
    }

    public void stopUpdater() {
        running = false;
        this.interrupt();
    }
}

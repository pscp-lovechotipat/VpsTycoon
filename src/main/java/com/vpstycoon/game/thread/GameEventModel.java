package com.vpstycoon.game.thread;

public class GameEventModel implements Runnable{
    public static final int GAME_START = 15 * 60 * 1000; // นาทีก่อนเริ่ม
    private int remainTime;

    public GameEventModel(int remainTime) {
        this.remainTime = remainTime;
    }

    @Override
    public void run() {

    }

    public int getRemainTime() {
        return remainTime;
    }
}

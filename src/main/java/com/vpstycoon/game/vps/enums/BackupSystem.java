package com.vpstycoon.game.vps.enums;

public enum BackupSystem {
    NONE(0.0),
    WEEKLY(1.0),
    DAILY(2.0),
    REALTIME(3.0);

    private final double score;

    BackupSystem(double score) {
        this.score = score;
    }

    public double getScore() {
        return score;
    }
} 

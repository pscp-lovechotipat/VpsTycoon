package com.vpstycoon.game.vps.enums;

public enum MonitoringSystem {
    BASIC(1.0),
    STANDARD(1.5),
    ADVANCED(2.0);

    private final double score;

    MonitoringSystem(double score) {
        this.score = score;
    }

    public double getScore() {
        return score;
    }
} 
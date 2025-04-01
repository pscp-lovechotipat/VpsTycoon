package com.vpstycoon.game.vps.enums;

public enum SecurityLevel {
    BASIC(1.0),
    STANDARD(1.5),
    ADVANCED(2.0),
    ENTERPRISE(2.5);

    private final double score;

    SecurityLevel(double score) {
        this.score = score;
    }

    public double getScore() {
        return score;
    }
} 


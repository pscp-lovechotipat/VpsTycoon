package com.vpstycoon.vps.enums;

public enum PerformanceLevel {
    STANDARD(1.0),
    BALANCED(1.5),
    HIGH(2.0);

    private final double score;

    PerformanceLevel(double score) {
        this.score = score;
    }

    public double getScore() {
        return score;
    }
} 
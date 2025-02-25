package com.vpstycoon.event;

import com.vpstycoon.event.enums.EventType;
import com.vpstycoon.vps.VPSOptimization;

public class VPSEvent {
    private EventType type;
    private double severity;
    private String solution;
    private int resolutionTime;
    
    public VPSEvent(EventType type, VPSOptimization optimization) {
        this.type = type;
        this.solution = type.getSolution();
        calculateSeverity(optimization);
        calculateResolutionTime(optimization);
    }
    
    private void calculateSeverity(VPSOptimization optimization) {
        // คำนวณความรุนแรงของปัญหาตามการ Optimize
        double optimizationScore = optimization.calculateOptimizationScore();
        this.severity = 1.0 - (optimizationScore / 100.0);
    }
    
    private void calculateResolutionTime(VPSOptimization optimization) {
        // คำนวณเวลาในการแก้ไขปัญหา
        this.resolutionTime = (int)(severity * 100);
        if (optimization.hasBackupSystem()) {
            resolutionTime /= 2;
        }
    }

    public EventType getType() {
        return type;
    }

    public double getSeverity() {
        return severity;
    }

    public String getSolution() {
        return solution;
    }

    public int getResolutionTime() {
        return resolutionTime;
    }
} 
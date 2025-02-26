package com.vpstycoon.game.vps;

import com.vpstycoon.game.vps.enums.*;

public class VPSOptimization {
    private OperatingSystem os;
    private SecurityLevel security;
    private PerformanceLevel performance;
    private BackupSystem backup;
    private MonitoringSystem monitoring;
    
    public VPSOptimization() {
        // เริ่มต้นด้วยค่าพื้นฐาน
        this.os = OperatingSystem.UBUNTU;
        this.security = SecurityLevel.BASIC;
        this.performance = PerformanceLevel.STANDARD;
        this.backup = BackupSystem.NONE;
        this.monitoring = MonitoringSystem.BASIC;
    }
    
    public void optimizeForRequest(RequestType type) {
        // ปรับแต่ง VPS ตามประเภทงาน
        switch (type) {
            case SPEED_FOCUSED:
                performance = PerformanceLevel.HIGH;
                os = OperatingSystem.CENTOS;
                break;
            case SECURITY_FOCUSED:
                security = SecurityLevel.ADVANCED;
                backup = BackupSystem.DAILY;
                break;
            case STABILITY_FOCUSED:
                monitoring = MonitoringSystem.ADVANCED;
                performance = PerformanceLevel.BALANCED;
                break;
        }
    }
    
    public double calculateOptimizationScore() {
        // คำนวณคะแนนการ Optimize
        return (security.getScore() + performance.getScore() + 
                backup.getScore() + monitoring.getScore()) / 4.0;
    }

    public boolean hasBackupSystem() {
        return backup != BackupSystem.NONE;
    }
} 
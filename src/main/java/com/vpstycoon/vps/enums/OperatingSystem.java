package com.vpstycoon.vps.enums;

public enum OperatingSystem {
    UBUNTU("Ubuntu", 1.0),
    CENTOS("CentOS", 1.2),
    WINDOWS("Windows Server", 1.5),
    DEBIAN("Debian", 1.1);

    private final String name;
    private final double performanceMultiplier;

    OperatingSystem(String name, double performanceMultiplier) {
        this.name = name;
        this.performanceMultiplier = performanceMultiplier;
    }

    public String getName() {
        return name;
    }

    public double getPerformanceMultiplier() {
        return performanceMultiplier;
    }
} 
package com.vpstycoon.game.vps.plan;

public class Plan {
    private String planName;

    private int ram;
    private int cpu;
    private int storage;
    private int network;

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public int getRam() {
        return ram;
    }

    public void setRam(int ram) {
        this.ram = ram;
    }

    public int getCpu() {
        return cpu;
    }

    public void setCpu(int cpu) {
        this.cpu = cpu;
    }

    public int getStorage() {
        return storage;
    }

    public void setStorage(int storage) {
        this.storage = storage;
    }

    public int getNetwork() {
        return network;
    }

    public void setNetwork(int network) {
        this.network = network;
    }
}

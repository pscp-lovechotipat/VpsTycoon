package com.vpstycoon.game.vps.plan;

// extends มาเท่านั้นนะ นี้เป็น class หลัก
public class Plan {
    private String planName;

    private int ram;
    private int cpu;
    private int storage;
    private int network;

    private String os;
    private Boolean ddosProtection;

    private int price;
    private int keepUp;

    public Plan(int keepUp, int network, int storage, int cpu, int ram, String planName) {
        this.keepUp = keepUp;
        this.network = network;
        this.storage = storage;
        this.cpu = cpu;
        this.ram = ram;
        this.planName = planName;
    }

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

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public Boolean getDdosProtection() {
        return ddosProtection;
    }

    public void setDdosProtection(Boolean ddosProtection) {
        this.ddosProtection = ddosProtection;
    }

    public int getKeepUp() {
        return keepUp;
    }

    public void setKeepUp(int keepUp) {
        this.keepUp = keepUp;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}

package com.vpstycoon.game.vps;

import java.util.ArrayList;
import java.util.List;

public class VPSOptimization {
    private int vCPUs;
    private int ramInGB;
    private int diskInGB;
    private boolean hasBackup;
    private List<VM> vms; // Add list of VMs

    public VPSOptimization() {
        this.vCPUs = 2;
        this.ramInGB = 4;
        this.diskInGB = 20;
        this.hasBackup = false;
        this.vms = new ArrayList<>();
    }

    // Existing getters and setters
    public int getVCPUs() { return vCPUs; }
    public int getRamInGB() { return ramInGB; }
    public int getDiskInGB() { return diskInGB; }
    public boolean hasBackupSystem() { return hasBackup; }
    public void setVCPUs(int vCPUs) { this.vCPUs = vCPUs; }
    public void setRamInGB(int ramInGB) { this.ramInGB = ramInGB; }
    public void setDiskInGB(int diskInGB) { this.diskInGB = diskInGB; }
    public void setHasBackup(boolean hasBackup) { this.hasBackup = hasBackup; }

    // New VM-related methods
    public List<VM> getVms() { return vms; }
    public void addVM(VM vm) { vms.add(vm); }
    public void removeVM(VM vm) { vms.remove(vm); }

    public double calculateOptimizationScore() {
        double score = (vCPUs * 10) + (ramInGB * 5) + (diskInGB * 0.5);
        return Math.min(100.0, score);
    }

    // New VM class as an inner class (can be moved to a separate file if preferred)
    public static class VM {
        private String ip;
        private String name;
        private int vCPUs;
        private String ram;
        private String disk;
        private String status;

        public VM(String ip, String name, int vCPUs, String ram, String disk, String status) {
            this.ip = ip;
            this.name = name;
            this.vCPUs = vCPUs;
            this.ram = ram;
            this.disk = disk;
            this.status = status;
        }

        public String getIp() { return ip; }
        public String getName() { return name; }
        public int getVCPUs() { return vCPUs; }
        public String getRam() { return ram; }
        public String getDisk() { return disk; }
        public String getStatus() { return status; }

        public void setName(String name) { this.name = name; }
        public void setVCPUs(int vCPUs) { this.vCPUs = vCPUs; }
        public void setRam(String ram) { this.ram = ram; }
        public void setDisk(String disk) { this.disk = disk; }
        public void setStatus(String status) { this.status = status; }

        @Override
        public String toString() {
            return name + " (" + ip + ") - vCPUs: " + vCPUs + ", RAM: " + ram + ", Disk: " + disk + ", Status: " + status;
        }
    }
}
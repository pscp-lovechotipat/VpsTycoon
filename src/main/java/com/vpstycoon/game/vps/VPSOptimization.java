package com.vpstycoon.game.vps;

import com.vpstycoon.game.GameObject;
import com.vpstycoon.game.vps.enums.VPSSize;
import com.vpstycoon.game.vps.enums.VPSStatus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VPSOptimization extends GameObject implements Serializable {
    private static final long serialVersionUID = 1L;
    private final List<VM> vms;
    private VPSStatus status;

    private int maxVMs;
    private double cpuUsage;
    private double ramUsage;
    private double diskUsage;
    private double networkUsage;

    private int vcpus;
    private int ramInGB;
    private int diskInGB;

    private boolean backupSystemEnabled = false;
    private boolean monitoringSystemEnabled = false;
    private boolean autoScalingEnabled = false;

    private int optimizationLevel = 1; // 1-5 optimization level
    private VPSSize size = VPSSize.SIZE_1U; // Physical size of the VPS
    private boolean installed = false; // Whether the VPS is installed in a rack

    public VPSOptimization() {
        this.vms = new ArrayList<>();
        this.status = VPSStatus.RUNNING;
        this.maxVMs = 5;
        this.cpuUsage = 0.0;
        this.ramUsage = 0.0;
        this.diskUsage = 0.0;
        this.networkUsage = 0.0;
        this.vcpus = 1;
        this.ramInGB = 2;
        this.diskInGB = 20;
        this.size = VPSSize.SIZE_1U;
        this.installed = false;
    }
    
    /**
     * Constructor with vcpus, ram, and size
     * @param vcpus Number of virtual CPUs
     * @param ramInGB Amount of RAM in GB
     * @param size Physical size of the VPS
     */
    public VPSOptimization(int vcpus, int ramInGB, VPSSize size) {
        this();  // Call the default constructor first
        this.vcpus = vcpus;
        this.ramInGB = ramInGB;
        this.size = size;
        // Set a reasonable disk size based on RAM
        this.diskInGB = ramInGB * 10;
    }

    public void addVM(VM vm) {
        if (vms.size() < maxVMs) {
            vms.add(vm);
            updateResourceUsage();
        }
    }

    public void removeVM(VM vm) {
        vms.remove(vm);
        updateResourceUsage();
    }

    public List<VM> getVms() {
        return vms;
    }

    public String getStatus() {
        return status.toString();
    }

    public VPSStatus getVPSStatus() {
        return status;
    }

    public void setStatus(VPSStatus status) {
        this.status = status;
    }

    public int getMaxVMs() {
        return maxVMs;
    }

    public void setMaxVMs(int maxVMs) {
        this.maxVMs = maxVMs;
    }

    public double getCpuUsage() {
        return cpuUsage;
    }

    public double getRamUsage() {
        return ramUsage;
    }

    public double getDiskUsage() {
        return diskUsage;
    }

    public double getNetworkUsage() {
        return networkUsage;
    }

    public void setVCPUs(int vcpus) {
        this.vcpus = vcpus;
    }
    
    public int getVCPUs() {
        return this.vcpus;
    }
    
    public void setRamInGB(int ramInGB) {
        this.ramInGB = ramInGB;
    }
    
    public int getRamInGB() {
        return this.ramInGB;
    }
    
    public void setDiskInGB(int diskInGB) {
        this.diskInGB = diskInGB;
    }
    
    public int getDiskInGB() {
        return this.diskInGB;
    }

    public VPSSize getSize() {
        return size;
    }
    
    public void setSize(VPSSize size) {
        this.size = size;
    }
    
    public int getSlotsRequired() {
        return size.getSlotsRequired();
    }
    
    public boolean isInstalled() {
        return installed;
    }
    
    public void setInstalled(boolean installed) {
        this.installed = installed;
    }

    /**
     * Calculate the optimization score based on resource usage and configuration
     * @return The optimization score (0-100)
     */
    public double calculateOptimizationScore() {
        // คำนวณคะแนนการ optimize จากการใช้ทรัพยากรและการตั้งค่า
        double cpuScore = 100 - cpuUsage;
        double ramScore = 100 - ramUsage;
        double diskScore = 100 - diskUsage;
        double networkScore = 100 - networkUsage;
        
        // ถ่วงน้ำหนักคะแนนตามความสำคัญ
        double baseScore = (cpuScore * 0.4) + (ramScore * 0.3) + (diskScore * 0.2) + (networkScore * 0.1);
        
        // เพิ่มคะแนนตามระบบที่เปิดใช้งาน
        double bonusScore = 0;
        if (backupSystemEnabled) bonusScore += 5;
        if (monitoringSystemEnabled) bonusScore += 5;
        if (autoScalingEnabled) bonusScore += 5;
        
        // เพิ่มคะแนนตามระดับการ optimize
        bonusScore += (optimizationLevel - 1) * 3;
        
        return Math.min(100, baseScore + bonusScore);
    }
    
    /**
     * Check if this VPS has a backup system
     * @return true if backup system is enabled
     */
    public boolean hasBackupSystem() {
        return backupSystemEnabled;
    }
    
    /**
     * Enable or disable backup system
     * @param enabled true to enable, false to disable
     */
    public void setBackupSystem(boolean enabled) {
        this.backupSystemEnabled = enabled;
    }
    
    /**
     * Check if this VPS has a monitoring system
     * @return true if monitoring system is enabled
     */
    public boolean hasMonitoringSystem() {
        return monitoringSystemEnabled;
    }
    
    /**
     * Enable or disable monitoring system
     * @param enabled true to enable, false to disable
     */
    public void setMonitoringSystem(boolean enabled) {
        this.monitoringSystemEnabled = enabled;
        if (enabled) {
            // เมื่อเปิดใช้งานระบบตรวจสอบ จะช่วยลดการใช้ทรัพยากร
            this.cpuUsage = Math.max(0, cpuUsage * 0.9);
            this.ramUsage = Math.max(0, ramUsage * 0.9);
            this.diskUsage = Math.max(0, diskUsage * 0.9);
        }
    }
    
    /**
     * Check if this VPS has auto-scaling enabled
     * @return true if auto-scaling is enabled
     */
    public boolean hasAutoScaling() {
        return autoScalingEnabled;
    }
    
    /**
     * Enable or disable auto-scaling
     * @param enabled true to enable, false to disable
     */
    public void setAutoScaling(boolean enabled) {
        this.autoScalingEnabled = enabled;
    }
    
    /**
     * Get the optimization level
     * @return optimization level (1-5)
     */
    public int getOptimizationLevel() {
        return optimizationLevel;
    }
    
    /**
     * Set the optimization level
     * @param level optimization level (1-5)
     */
    public void setOptimizationLevel(int level) {
        this.optimizationLevel = Math.max(1, Math.min(5, level));
    }
    
    /**
     * Optimize the VPS to reduce resource usage
     * @return true if optimization was successful
     */
    public boolean optimize() {
        if (optimizationLevel < 5) {
            optimizationLevel++;
            
            // ลดการใช้ทรัพยากรตามระดับการ optimize
            double optimizationFactor = 0.95 - ((optimizationLevel - 1) * 0.05);
            this.cpuUsage = Math.max(0, cpuUsage * optimizationFactor);
            this.ramUsage = Math.max(0, ramUsage * optimizationFactor);
            this.diskUsage = Math.max(0, diskUsage * optimizationFactor);
            this.networkUsage = Math.max(0, networkUsage * optimizationFactor);
            
            return true;
        }
        return false;
    }

    private void updateResourceUsage() {
        Random random = new Random();
        this.cpuUsage = random.nextDouble() * 100;
        this.ramUsage = random.nextDouble() * 100;
        this.diskUsage = random.nextDouble() * 100;
        this.networkUsage = random.nextDouble() * 100;
    }

    public static class VM implements Serializable {
        private static final long serialVersionUID = 1L;
        private String ip;
        private String name;
        private int vcpu;
        private String ram;
        private String disk;
        private String status;
        private List<String> firewallRules;
        private boolean firewallEnabled;

        public VM(String ip, String name, int vcpu, String ram, String disk, String status) {
            this.ip = ip;
            this.name = name;
            this.vcpu = vcpu;
            this.ram = ram;
            this.disk = disk;
            this.status = status;
            this.firewallRules = new ArrayList<>();
            this.firewallEnabled = true;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getVcpu() {
            return vcpu;
        }

        public void setVcpu(int vcpu) {
            this.vcpu = vcpu;
        }

        public String getRam() {
            return ram;
        }

        public void setRam(String ram) {
            this.ram = ram;
        }

        public String getDisk() {
            return disk;
        }

        public void setDisk(String disk) {
            this.disk = disk;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
        
        public List<String> getFirewallRules() {
            return firewallRules;
        }
        
        public void setFirewallRules(List<String> firewallRules) {
            this.firewallRules = firewallRules;
        }
        
        public void addFirewallRule(String rule) {
            if (this.firewallRules == null) {
                this.firewallRules = new ArrayList<>();
            }
            this.firewallRules.add(rule);
        }
        
        public void removeFirewallRule(String rule) {
            if (this.firewallRules != null) {
                this.firewallRules.remove(rule);
            }
        }
        
        public boolean isFirewallEnabled() {
            return firewallEnabled;
        }
        
        public void setFirewallEnabled(boolean firewallEnabled) {
            this.firewallEnabled = firewallEnabled;
        }
    }
}
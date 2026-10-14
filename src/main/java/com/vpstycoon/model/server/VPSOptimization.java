package com.vpstycoon.model.server;

import com.vpstycoon.model.GameObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VPSOptimization extends GameObject implements Serializable {
    private final List<VM> vms;
    private VPSStatus status;

    private String vpsId;

    private int maxVMs;

    private int vcpus;
    private int ramInGB;
    private int diskInGB;

    private VPSSize size = VPSSize.SIZE_1U;
    private boolean installed = false;

    public VPSOptimization() {
        this.vms = new ArrayList<>();
        this.status = VPSStatus.RUNNING;
        this.maxVMs = 5;
        this.vcpus = 1;
        this.ramInGB = 2;
        this.diskInGB = 20;
        this.size = VPSSize.SIZE_1U;
        this.installed = false;
        this.vpsId = "Server-" + System.currentTimeMillis();
    }

    public void addVM(VM vm) {
        if (vms.size() < maxVMs) {
            vms.add(vm);
        }
    }

    public void removeVM(VM vm) {
        vms.remove(vm);
    }

    public List<VM> getVms() {
        return vms;
    }

    public String getStatus() {
        return status.toString();
    }

    public int getMaxVMs() {
        return maxVMs;
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

    public static class VM implements Serializable {
        private String name;
        private String id;
        private int vCPUs;
        private int ramInGB;
        private int diskInGB;
        private String status;
        private String ipAddress;
        private String customerId;
        private String customerName;

        public VM(String name, int vCPUs, int ramInGB, int diskInGB) {
            this.name = name;
            this.id = "vm-" + System.currentTimeMillis() + "-" + new Random().nextInt(1000);
            this.vCPUs = vCPUs;
            this.ramInGB = ramInGB;
            this.diskInGB = diskInGB;
            this.status = "Stopped";
            this.ipAddress = generateRandomIp();
            this.customerId = null;
            this.customerName = null;
        }

        private String generateRandomIp() {
            Random random = new Random();
            return "192.168." + (random.nextInt(253) + 1) + "." + (random.nextInt(253) + 1);
        }

        public void assignToCustomer(String customerId, String customerName) {
            this.customerId = customerId;
            this.customerName = customerName;
        }

        public void releaseFromCustomer() {
            this.customerId = null;
            this.customerName = null;
        }

        public boolean isAssignedToCustomer() {
            return customerId != null && !customerId.isEmpty();
        }

        public String getCustomerId() {
            return customerId;
        }

        public String getCustomerName() {
            return customerName;
        }

        public String getIp() {
            return ipAddress;
        }

        public void setIp(String ip) {
            this.ipAddress = ip;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getVcpu() {
            return vCPUs;
        }

        public void setVcpu(int vcpu) {
            this.vCPUs = vcpu;
        }

        public String getRam() {
            return Integer.toString(ramInGB) + "GB";
        }

        public void setRam(String ram) {
            this.ramInGB = Integer.parseInt(ram.replace("GB", ""));
        }

        public String getDisk() {
            return Integer.toString(diskInGB) + "GB";
        }

        public void setDisk(String disk) {
            this.diskInGB = Integer.parseInt(disk.replace("GB", ""));
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getId() {
            return id;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        return this == obj;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    public String getVpsId() {
        return vpsId;
    }

    public void setVpsId(String vpsId) {
        this.vpsId = vpsId;
    }
}

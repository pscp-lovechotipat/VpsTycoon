package com.vpstycoon.ui.game.rack;

import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.game.vps.VPSOptimization;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Rack implements Serializable {
    private final int maxSlotUnits;         // จำนวนสล็อตสูงสุดของ Rack
    private int unlockedSlotUnits;          // จำนวนสล็อตที่ถูกปลดล็อกแล้ว
    private int occupiedSlotUnits;          // จำนวนสล็อตที่ถูกใช้งาน
    private List<VPSOptimization> installedVPS; // รายการ VPS ที่ติดตั้ง

    // Constructor
    public Rack(int maxSlotUnits, int initialUnlockedSlotUnits) {
        this.maxSlotUnits = maxSlotUnits;
        this.unlockedSlotUnits = initialUnlockedSlotUnits;
        this.occupiedSlotUnits = 0;
        this.installedVPS = new ArrayList<>();

    }

    // อัปเกรด Rack เพื่อปลดล็อกสล็อตเพิ่ม
    public boolean upgrade() {
        if (unlockedSlotUnits < maxSlotUnits) {
            unlockedSlotUnits++;
            return true;
        }
        return false;
    }

    // ติดตั้ง VPS เข้า Rack
    public boolean installVPS(VPSOptimization vps) {
        int slotsRequired = vps.getSlotsRequired();
        if (getAvailableSlotUnits() >= slotsRequired) {
            installedVPS.add(vps);
            occupiedSlotUnits += slotsRequired;
            vps.setInstalled(true);
            return true;
        }
        return false;
    }

    // ถอนการติดตั้ง VPS ออกจาก Rack
    public boolean uninstallVPS(VPSOptimization vps) {
        if (installedVPS.remove(vps)) {
            occupiedSlotUnits -= vps.getSlotsRequired();
            vps.setInstalled(false);
            return true;
        }
        return false;
    }

    // คำนวณสล็อตที่ว่างและพร้อมใช้งาน
    public int getAvailableSlotUnits() {
        return unlockedSlotUnits - occupiedSlotUnits;
    }

    // Getters
    public int getOccupiedSlotUnits() {
        return occupiedSlotUnits;
    }

    public int getUnlockedSlotUnits() {
        return unlockedSlotUnits;
    }

    public int getMaxSlotUnits() {
        return maxSlotUnits;
    }

    public List<VPSOptimization> getInstalledVPS() {
        return installedVPS;
    }


}
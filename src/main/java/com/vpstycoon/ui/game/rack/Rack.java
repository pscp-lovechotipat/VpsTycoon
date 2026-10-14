package com.vpstycoon.ui.game.rack;

import com.vpstycoon.game.vps.VPSOptimization;

import java.util.ArrayList;
import java.util.List;

public class Rack {
    private final List<Integer> slotsPerRack = new ArrayList<>();
    private final List<List<VPSOptimization>> rackVPS = new ArrayList<>();
    private final List<Integer> unlockedSlotUnitsList = new ArrayList<>();
    private final List<Integer> occupiedSlotUnitsList = new ArrayList<>();
    private int currentRackIndex = -1;

    public void addRack(int slots) {
        slotsPerRack.add(slots);
        rackVPS.add(new ArrayList<>());
        unlockedSlotUnitsList.add(1);
        occupiedSlotUnitsList.add(0);

        if (currentRackIndex == -1) {
            currentRackIndex = 0;
        }
    }

    public int getMaxRacks() {
        return slotsPerRack.size();
    }

    public int getCurrentRackIndex() {
        return currentRackIndex;
    }

    public void setRackIndex(int index) {
        if (index >= 0 && index < slotsPerRack.size()) {
            currentRackIndex = index;
        }
    }

    public boolean prevRack() {
        if (currentRackIndex > 0) {
            currentRackIndex--;
            return true;
        }
        return false;
    }

    public boolean nextRack() {
        if (currentRackIndex < slotsPerRack.size() - 1) {
            currentRackIndex++;
            return true;
        }
        return false;
    }

    public boolean goToLatestRack() {
        if (!slotsPerRack.isEmpty()) {
            currentRackIndex = slotsPerRack.size() - 1;
            return true;
        }
        return false;
    }

    public boolean installVPS(VPSOptimization vps) {
        if (!hasCurrentRack()) {
            System.out.println("DEBUG: Cannot install VPS, no rack selected");
            return false;
        }

        int availableSlots = getAvailableSlotUnits();
        int requiredSlots = vps.getSlotsRequired();

        System.out.println("DEBUG: Rack " + (currentRackIndex + 1) + " - Available slots: " + availableSlots +
                           ", Required slots: " + requiredSlots +
                           ", Unlocked slots: " + unlockedSlotUnitsList.get(currentRackIndex) +
                           ", Occupied slots: " + occupiedSlotUnitsList.get(currentRackIndex));

        if (availableSlots < requiredSlots) {
            System.out.println("DEBUG: Not enough slots available in rack " + (currentRackIndex + 1));
            return false;
        }

        rackVPS.get(currentRackIndex).add(vps);
        occupiedSlotUnitsList.set(currentRackIndex, occupiedSlotUnitsList.get(currentRackIndex) + requiredSlots);
        vps.setInstalled(true);
        return true;
    }

    public boolean uninstallVPS(VPSOptimization vps) {
        if (!hasCurrentRack()) {
            return false;
        }
        if (rackVPS.get(currentRackIndex).remove(vps)) {
            occupiedSlotUnitsList.set(currentRackIndex, occupiedSlotUnitsList.get(currentRackIndex) - vps.getSlotsRequired());
            vps.setInstalled(false);
            return true;
        }
        return false;
    }

    public List<VPSOptimization> getInstalledVPS() {
        return hasCurrentRack() ? new ArrayList<>(rackVPS.get(currentRackIndex)) : new ArrayList<>();
    }

    public List<VPSOptimization> getAllInstalledVPS() {
        List<VPSOptimization> allVPS = new ArrayList<>();
        for (List<VPSOptimization> rackVPSList : rackVPS) {
            allVPS.addAll(rackVPSList);
        }
        return allVPS;
    }

    public int getMaxSlotUnits() {
        return hasCurrentRack() ? slotsPerRack.get(currentRackIndex) : 0;
    }

    public int getUnlockedSlotUnits() {
        return hasCurrentRack() ? unlockedSlotUnitsList.get(currentRackIndex) : 0;
    }

    public int getOccupiedSlotUnits() {
        return hasCurrentRack() ? occupiedSlotUnitsList.get(currentRackIndex) : 0;
    }

    public int getAvailableSlotUnits() {
        return getUnlockedSlotUnits() - getOccupiedSlotUnits();
    }

    public boolean upgrade() {
        if (!hasCurrentRack()) {
            return false;
        }
        int currentUnlockedSlots = unlockedSlotUnitsList.get(currentRackIndex);
        if (currentUnlockedSlots < slotsPerRack.get(currentRackIndex)) {
            unlockedSlotUnitsList.set(currentRackIndex, currentUnlockedSlots + 1);
            return true;
        }
        return false;
    }

    public List<Integer> getUnlockedSlotUnitsList() {
        return new ArrayList<>(unlockedSlotUnitsList);
    }

    public void setUnlockedSlotUnitsList(List<Integer> unlockedList) {
        if (unlockedList != null) {
            unlockedSlotUnitsList.clear();
            unlockedSlotUnitsList.addAll(unlockedList);
            System.out.println("ตั้งค่า unlockedSlotUnitsList: " + unlockedSlotUnitsList);
        }
    }

    private boolean hasCurrentRack() {
        return currentRackIndex >= 0 && currentRackIndex < slotsPerRack.size()
                && currentRackIndex < unlockedSlotUnitsList.size();
    }
}

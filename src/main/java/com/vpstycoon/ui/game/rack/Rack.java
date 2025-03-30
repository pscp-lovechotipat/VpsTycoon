package com.vpstycoon.ui.game.rack;

import com.vpstycoon.game.GameObject;
import com.vpstycoon.game.GameState;
import com.vpstycoon.game.vps.VPSInventory;
import com.vpstycoon.game.vps.VPSOptimization;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Rack extends StackPane implements Serializable {
    private static final long serialVersionUID = 1L;
    
    
    private transient List<VBox> racks;
    private transient Button prevButton;
    private transient Button nextButton;
    private transient VBox navigationButtons;
    
    
    private final List<List<VPSOptimization>> rackVPS; 
    private final List<Integer> unlockedSlotUnitsList; 
    private final List<Integer> occupiedSlotUnitsList; 
    private int currentRackIndex;
    private int maxSlotUnits;
    private int unlockedSlotUnits;
    private int occupiedSlotUnits;
    
    private List<Integer> slotsPerRack;

    public Rack() {
        initializeTransientFields();
        rackVPS = new ArrayList<>();
        unlockedSlotUnitsList = new ArrayList<>();
        occupiedSlotUnitsList = new ArrayList<>();
        slotsPerRack = new ArrayList<>();
        
        currentRackIndex = -1;
        maxSlotUnits = 0;
        unlockedSlotUnits = 0;
        occupiedSlotUnits = 0;
    }
    
    
    private void initializeTransientFields() {
        racks = new ArrayList<>();
        
        
        prevButton = new Button("←");
        nextButton = new Button("→");
        
        
        String buttonStyle = """
            -fx-background-color: #1a1a1a;
            -fx-text-fill: #00ff00;
            -fx-font-size: 20px;
            -fx-font-weight: bold;
            -fx-padding: 10px;
            -fx-background-radius: 5px;
            -fx-border-color: #00ff00;
            -fx-border-width: 1px;
            -fx-border-style: solid;
            -fx-min-width: 40px;
            -fx-min-height: 40px;
            """;
        
        prevButton.setStyle(buttonStyle);
        nextButton.setStyle(buttonStyle);
        
        
        prevButton.setOnMouseEntered(e -> 
            prevButton.setStyle(buttonStyle + "-fx-background-color: #00ff00; -fx-text-fill: #000000;")
        );
        prevButton.setOnMouseExited(e -> 
            prevButton.setStyle(buttonStyle)
        );
        
        nextButton.setOnMouseEntered(e -> 
            nextButton.setStyle(buttonStyle + "-fx-background-color: #00ff00; -fx-text-fill: #000000;")
        );
        nextButton.setOnMouseExited(e -> 
            nextButton.setStyle(buttonStyle)
        );

        
        navigationButtons = new VBox(10);
        navigationButtons.setAlignment(Pos.CENTER);
        navigationButtons.setPadding(new Insets(10));
        navigationButtons.getChildren().addAll(prevButton, nextButton);
        
        
        prevButton.setOnAction(e -> navigateToPreviousRack());
        nextButton.setOnAction(e -> navigateToNextRack());
        
        
        navigationButtons.setVisible(false);
        
        
        getChildren().add(navigationButtons);
    }
    
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        
        
        initializeTransientFields();
        
        
        for (int i = 0; i < slotsPerRack.size(); i++) {
            
            VBox newRack = createRackUI(slotsPerRack.get(i));
            racks.add(newRack);
        }
        
        
        if (currentRackIndex >= 0 && currentRackIndex < racks.size()) {
            showCurrentRack();
        }
    }
    
    
    private VBox createRackUI(int slots) {
        VBox newRack = new VBox(5);
        newRack.setStyle("""
            -fx-background-color: #2a2a2a;
            -fx-padding: 10px;
            -fx-background-radius: 5px;
            -fx-border-color: #00ff00;
            -fx-border-width: 1px;
            -fx-border-style: solid;
            """);
        newRack.setAlignment(Pos.TOP_CENTER);
        
        
        for (int i = 0; i < slots; i++) {
            VBox slot = createSlot();
            newRack.getChildren().add(slot);
        }
        
        return newRack;
    }

    public void addRack(int slots) {
        
        VBox newRack = createRackUI(slots);
        racks.add(newRack);
        
        
        slotsPerRack.add(slots);
        
        
        rackVPS.add(new ArrayList<>()); 
        unlockedSlotUnitsList.add(1); 
        occupiedSlotUnitsList.add(0); 
        
        
        maxSlotUnits = slots;
        unlockedSlotUnits = 1; 
        
        
        if (currentRackIndex == -1) {
            currentRackIndex = 0;
            showCurrentRack();
        }
        
        
        navigationButtons.setVisible(racks.size() > 1);
    }

    private VBox createSlot() {
        VBox slot = new VBox(5);
        slot.setStyle("""
            -fx-background-color: #1a1a1a;
            -fx-padding: 5px;
            -fx-background-radius: 3px;
            -fx-border-color: #00ff00;
            -fx-border-width: 1px;
            -fx-border-style: solid;
            """);
        slot.setMinHeight(100);
        slot.setAlignment(Pos.CENTER);
        return slot;
    }

    public void addVPSToSlot(VPSOptimization vps, int slotIndex) {
        if (currentRackIndex >= 0 && currentRackIndex < racks.size()) {
            VBox currentRack = racks.get(currentRackIndex);
            if (slotIndex >= 0 && slotIndex < currentRack.getChildren().size()) {
                VBox slot = (VBox) currentRack.getChildren().get(slotIndex);
                
                
                VBox vpsVisual = new VBox(5);
                vpsVisual.setStyle("""
                    -fx-background-color: #2a2a2a;
                    -fx-padding: 5px;
                    -fx-background-radius: 3px;
                    -fx-border-color: #00ff00;
                    -fx-border-width: 1px;
                    -fx-border-style: solid;
                    """);
                vpsVisual.setAlignment(Pos.CENTER);
                
                
                Label nameLabel = new Label("Server " + vps.getVCPUs() + "vCPU");
                nameLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-weight: bold;");
                
                Label specsLabel = new Label(vps.getRamInGB() + "GB RAM");
                specsLabel.setStyle("-fx-text-fill: #00ff00;");
                
                Label sizeLabel = new Label(vps.getSize().getDisplayName());
                sizeLabel.setStyle("-fx-text-fill: #00ff00;");
                
                vpsVisual.getChildren().addAll(nameLabel, specsLabel, sizeLabel);
                slot.getChildren().add(vpsVisual);
            }
        }
    }

    public void removeVPSFromSlot(int slotIndex) {
        if (currentRackIndex >= 0 && currentRackIndex < racks.size()) {
            VBox currentRack = racks.get(currentRackIndex);
            if (slotIndex >= 0 && slotIndex < currentRack.getChildren().size()) {
                VBox slot = (VBox) currentRack.getChildren().get(slotIndex);
                slot.getChildren().clear();
            }
        }
    }

    public int getMaxRacks() {
        return racks.size();
    }

    public int getCurrentRackIndex() {
        return currentRackIndex;
    }

    public int getRackIndex() {
        return currentRackIndex;
    }

    public void setRackIndex(int index) {
        if (index >= 0 && index < racks.size()) {
            currentRackIndex = index;
            showCurrentRack();
        }
    }

    private void showCurrentRack() {
        
        getChildren().clear();
        
        
        getChildren().add(navigationButtons);
        
        
        if (currentRackIndex >= 0 && currentRackIndex < racks.size()) {
            VBox currentRack = racks.get(currentRackIndex);
            getChildren().add(currentRack);
            
            
            unlockedSlotUnits = unlockedSlotUnitsList.get(currentRackIndex);
            
            
            occupiedSlotUnits = occupiedSlotUnitsList.get(currentRackIndex);
            
            
            
            
            
            
            
            
            
            maxSlotUnits = currentRack.getChildren().size();
            
            
            updateVPSDisplay(currentRack);
        }
    }

    private void updateVPSDisplay(VBox rack) {
        
        for (var slot : rack.getChildren()) {
            if (slot instanceof VBox) {
                ((VBox) slot).getChildren().clear();
            }
        }
        
        
        int currentSlot = 0;
        List<VPSOptimization> currentRackVPS = rackVPS.get(currentRackIndex);
        int currentUnlockedSlots = unlockedSlotUnitsList.get(currentRackIndex);

        for (VPSOptimization vps : currentRackVPS) {
            if (currentSlot < currentUnlockedSlots && currentSlot < rack.getChildren().size()) {
                VBox slot = (VBox) rack.getChildren().get(currentSlot);

                VBox vpsVisual = new VBox(5);
                vpsVisual.setStyle("""
                    -fx-background-color: #2a2a2a;
                    -fx-padding: 5px;
                    -fx-background-radius: 3px;
                    -fx-border-color: #00ff00;
                    -fx-border-width: 1px;
                    -fx-border-style: solid;
                    """);
                vpsVisual.setAlignment(Pos.CENTER);

                Label nameLabel = new Label("Server " + vps.getVCPUs() + "vCPU");
                nameLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-weight: bold;");

                Label specsLabel = new Label(vps.getRamInGB() + "GB RAM");
                specsLabel.setStyle("-fx-text-fill: #00ff00;");

                Label sizeLabel = new Label(vps.getSize().getDisplayName());
                sizeLabel.setStyle("-fx-text-fill: #00ff00;");

                vpsVisual.getChildren().addAll(nameLabel, specsLabel, sizeLabel);
                slot.getChildren().add(vpsVisual);

                currentSlot += vps.getSlotsRequired();
            }
        }

        for (int i = currentSlot; i < rack.getChildren().size(); i++) {
            VBox slot = (VBox) rack.getChildren().get(i);
            Label statusLabel;

            if (i < currentUnlockedSlots) {
                statusLabel = new Label("AVAILABLE");
                statusLabel.setStyle("-fx-text-fill: #00ff00;");
            } else {
                statusLabel = new Label("LOCKED");
                statusLabel.setStyle("-fx-text-fill: #ff0000;");
            }

            slot.getChildren().add(statusLabel);
        }
    }

    private void navigateToPreviousRack() {
        if (currentRackIndex > 0) {
            currentRackIndex--;
            showCurrentRack();
        }
    }

    private void navigateToNextRack() {
        if (currentRackIndex < racks.size() - 1) {
            currentRackIndex++;
            showCurrentRack();
        }
    }

    public boolean prevRack() {
        if (currentRackIndex > 0) {
            currentRackIndex--;
            showCurrentRack();
            return true;
        }
        return false;
    }

    public boolean nextRack() {
        if (currentRackIndex < racks.size() - 1) {
            currentRackIndex++;
            showCurrentRack();
            return true;
        }
        return false;
    }

    
    public boolean goToLatestRack() {
        if (!racks.isEmpty()) {
            currentRackIndex = racks.size() - 1;
            showCurrentRack();
            return true;
        }
        return false;
    }

    public VBox getCurrentRack() {
        if (currentRackIndex >= 0 && currentRackIndex < racks.size()) {
            return racks.get(currentRackIndex);
        }
        return null;
    }

    
    public boolean installVPS(VPSOptimization vps) {
        if (currentRackIndex < 0 || currentRackIndex >= unlockedSlotUnitsList.size()) {
            System.out.println("DEBUG: Cannot install VPS, no rack selected");
            return false; 
        }
        
        
        int availableSlots = getAvailableSlotUnits();
        int requiredSlots = vps.getSlotsRequired();
        
        System.out.println("DEBUG: Rack " + (currentRackIndex+1) + " - Available slots: " + availableSlots + 
                           ", Required slots: " + requiredSlots + 
                           ", Unlocked slots: " + unlockedSlotUnitsList.get(currentRackIndex) + 
                           ", Occupied slots: " + occupiedSlotUnitsList.get(currentRackIndex));
        
        if (availableSlots < requiredSlots) {
            System.out.println("DEBUG: Not enough slots available in rack " + (currentRackIndex+1));
            return false; 
        }
        
        
        rackVPS.get(currentRackIndex).add(vps);
        
        
        int currentOccupied = occupiedSlotUnitsList.get(currentRackIndex);
        occupiedSlotUnitsList.set(currentRackIndex, currentOccupied + vps.getSlotsRequired());
        
        
        occupiedSlotUnits = occupiedSlotUnitsList.get(currentRackIndex);
        
        vps.setInstalled(true);

        
        if (currentRackIndex >= 0 && currentRackIndex < racks.size()) {
            updateVPSDisplay(racks.get(currentRackIndex));
        }
        return true;
    }

    public boolean uninstallVPS(VPSOptimization vps) {
        if (currentRackIndex < 0 || currentRackIndex >= rackVPS.size()) {
            return false; 
        }
        List<VPSOptimization> currentRackVPS = rackVPS.get(currentRackIndex);
        if (currentRackVPS.remove(vps)) {
            
            int currentOccupied = occupiedSlotUnitsList.get(currentRackIndex);
            occupiedSlotUnitsList.set(currentRackIndex, currentOccupied - vps.getSlotsRequired());
            
            
            occupiedSlotUnits = occupiedSlotUnitsList.get(currentRackIndex);
            
            vps.setInstalled(false);

            if (currentRackIndex >= 0 && currentRackIndex < racks.size()) {
                updateVPSDisplay(racks.get(currentRackIndex));
            }
            return true;
        }
        return false;
    }

    public List<VPSOptimization> getInstalledVPS() {
        if (currentRackIndex < 0 || currentRackIndex >= rackVPS.size()) {
            return new ArrayList<>(); 
        }
        return new ArrayList<>(rackVPS.get(currentRackIndex));
    }

    public int getMaxSlotUnits() {
        return maxSlotUnits;
    }

    public int getUnlockedSlotUnits() {
        if (currentRackIndex < 0 || currentRackIndex >= unlockedSlotUnitsList.size()) {
            return 0; 
        }
        return unlockedSlotUnitsList.get(currentRackIndex);
    }

    public int getOccupiedSlotUnits() {
        return occupiedSlotUnits;
    }

    public int getAvailableSlotUnits() {
        if (currentRackIndex < 0 || currentRackIndex >= unlockedSlotUnitsList.size()) {
            return 0; 
        }
        int unlockedSlots = unlockedSlotUnitsList.get(currentRackIndex);
        int occupiedSlots = occupiedSlotUnitsList.get(currentRackIndex);
        return unlockedSlots - occupiedSlots;
    }

    public boolean upgrade() {
        if (currentRackIndex < 0 || currentRackIndex >= unlockedSlotUnitsList.size()) {
            return false; 
        }
        
        int currentUnlockedSlots = unlockedSlotUnitsList.get(currentRackIndex);
        
        
        if (currentUnlockedSlots < racks.get(currentRackIndex).getChildren().size()) {
            
            unlockedSlotUnitsList.set(currentRackIndex, currentUnlockedSlots + 1);
            
            
            unlockedSlotUnits = unlockedSlotUnitsList.get(currentRackIndex);
            
            showCurrentRack();
            return true;
        }
        return false;
    }

    
    public List<VPSOptimization> getAllInstalledVPS() {
        List<VPSOptimization> allVPS = new ArrayList<>();
        
        
        for (List<VPSOptimization> rackVPSList : rackVPS) {
            allVPS.addAll(rackVPSList);
        }
        
        return allVPS;
    }
    
    
    public List<Integer> getUnlockedSlotUnitsList() {
        return new ArrayList<>(unlockedSlotUnitsList);
    }

    
    public void setUnlockedSlotUnitsList(List<Integer> unlockedList) {
        if (unlockedList != null) {
            this.unlockedSlotUnitsList.clear();
            this.unlockedSlotUnitsList.addAll(unlockedList);
            System.out.println("ตั้งค่า unlockedSlotUnitsList: " + this.unlockedSlotUnitsList);
        }
    }
    
    
    public boolean loadFromGameState(GameState gameState, VPSInventory inventory) {
        if (gameState == null) {
            System.out.println("ไม่สามารถโหลดข้อมูล Rack ได้: GameState เป็น null");
            return false;
        }
        
        try {
            
            Map<String, Object> rackConfig = gameState.getRackConfiguration();
            if (rackConfig == null || rackConfig.isEmpty()) {
                System.out.println("ไม่พบข้อมูลการตั้งค่า Rack ใน GameState");
                return false;
            }
            
            
            this.racks.clear();
            this.rackVPS.clear();
            this.unlockedSlotUnitsList.clear();
            this.occupiedSlotUnitsList.clear();
            this.slotsPerRack.clear();
            
            
            int maxRacks = rackConfig.containsKey("maxRacks") ? (Integer) rackConfig.get("maxRacks") : 0;
            
            
            List<Integer> slotCounts = null;
            if (rackConfig.containsKey("slotCounts")) {
                slotCounts = (List<Integer>) rackConfig.get("slotCounts");
            }
            
            
            for (int i = 0; i < maxRacks; i++) {
                int slotCount = 10; 
                if (slotCounts != null && i < slotCounts.size()) {
                    slotCount = slotCounts.get(i);
                }
                
                VBox newRack = createRackUI(slotCount);
                this.racks.add(newRack);
                this.slotsPerRack.add(slotCount);
                this.rackVPS.add(new ArrayList<>());
                
                
                this.unlockedSlotUnitsList.add(1); 
                this.occupiedSlotUnitsList.add(0); 
            }
            
            
            if (rackConfig.containsKey("unlockedSlotUnitsList")) {
                List<Integer> unlockedList = (List<Integer>) rackConfig.get("unlockedSlotUnitsList");
                if (unlockedList != null && !unlockedList.isEmpty()) {
                    this.unlockedSlotUnitsList.clear();
                    this.unlockedSlotUnitsList.addAll(unlockedList);
                    System.out.println("โหลดข้อมูล unlockedSlotUnitsList: " + this.unlockedSlotUnitsList);
                }
            }
            
            
            if (rackConfig.containsKey("currentRackIndex")) {
                int index = (Integer) rackConfig.get("currentRackIndex");
                if (index >= 0 && index < maxRacks) {
                    this.currentRackIndex = index;
                } else {
                    this.currentRackIndex = 0;
                }
            } else {
                this.currentRackIndex = 0;
            }
            
            
            if (rackConfig.containsKey("installedVpsIds")) {
                List<String> installedVpsIds = (List<String>) rackConfig.get("installedVpsIds");
                
                
                for (String vpsId : installedVpsIds) {
                    boolean found = false;
                    
                    for (GameObject obj : gameState.getGameObjects()) {
                        if (obj instanceof VPSOptimization) {
                            VPSOptimization vps = (VPSOptimization) obj;
                            if (vps.getVpsId().equals(vpsId)) {
                                
                                int rackIndex = 0; 
                                
                                
                                
                                
                                
                                if (rackIndex >= 0 && rackIndex < this.rackVPS.size()) {
                                    this.rackVPS.get(rackIndex).add(vps);
                                    
                                    
                                    int currentOccupied = this.occupiedSlotUnitsList.get(rackIndex);
                                    this.occupiedSlotUnitsList.set(rackIndex, currentOccupied + vps.getSlotsRequired());
                                    
                                    
                                    vps.setInstalled(true);
                                    
                                    found = true;
                                    System.out.println("ติดตั้ง VPS " + vpsId + " ใน Rack #" + (rackIndex + 1));
                                    break;
                                }
                            }
                        }
                    }
                    
                    if (!found) {
                        System.out.println("ไม่พบ VPS " + vpsId + " ใน GameObjects");
                    }
                }
            }
            
            
            if (this.currentRackIndex >= 0 && this.currentRackIndex < this.unlockedSlotUnitsList.size()) {
                this.unlockedSlotUnits = this.unlockedSlotUnitsList.get(this.currentRackIndex);
                this.occupiedSlotUnits = this.occupiedSlotUnitsList.get(this.currentRackIndex);
            }
            
            
            if (this.currentRackIndex >= 0 && this.currentRackIndex < this.racks.size()) {
                this.maxSlotUnits = this.racks.get(this.currentRackIndex).getChildren().size();
            }
            
            
            showCurrentRack();
            
            
            navigationButtons.setVisible(this.racks.size() > 1);
            
            System.out.println("โหลดข้อมูล Rack สำเร็จ: " + this.racks.size() + " racks, " 
                            + this.rackVPS.get(this.currentRackIndex).size() + " VPS ที่ติดตั้งใน rack ปัจจุบัน");
            return true;
            
        } catch (Exception e) {
            System.err.println("เกิดข้อผิดพลาดในการโหลดข้อมูล Rack: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    
    public void syncRackWithGameState(GameState gameState) {
        if (gameState == null) {
            System.out.println("ไม่สามารถซิงค์ข้อมูล Rack ได้: GameState เป็น null");
            return;
        }
        
        try {
            System.out.println("กำลังซิงค์ข้อมูล Rack กับ GameState...");
            
            
            for (int rackIndex = 0; rackIndex < rackVPS.size(); rackIndex++) {
                List<VPSOptimization> installedVPS = new ArrayList<>(rackVPS.get(rackIndex));
                
                for (VPSOptimization vps : installedVPS) {
                    
                    boolean found = false;
                    for (GameObject obj : gameState.getGameObjects()) {
                        if (obj instanceof VPSOptimization) {
                            VPSOptimization stateVPS = (VPSOptimization) obj;
                            if (stateVPS.getVpsId().equals(vps.getVpsId())) {
                                found = true;
                                
                                
                                
                                
                                break;
                            }
                        }
                    }
                    
                    
                    if (!found) {
                        System.out.println("ลบ VPS " + vps.getVpsId() + " จาก Rack เนื่องจากไม่พบใน GameState");
                        uninstallVPS(vps);
                    }
                }
            }
            
            
            
            Map<String, Object> rackConfig = gameState.getRackConfiguration();
            if (rackConfig != null && rackConfig.containsKey("installedVpsIds")) {
                List<String> installedVpsIds = (List<String>) rackConfig.get("installedVpsIds");
                
                for (String vpsId : installedVpsIds) {
                    
                    boolean alreadyInstalled = false;
                    for (List<VPSOptimization> rackVPSList : rackVPS) {
                        for (VPSOptimization vps : rackVPSList) {
                            if (vps.getVpsId().equals(vpsId)) {
                                alreadyInstalled = true;
                                break;
                            }
                        }
                        if (alreadyInstalled) break;
                    }
                    
                    
                    if (!alreadyInstalled) {
                        for (GameObject obj : gameState.getGameObjects()) {
                            if (obj instanceof VPSOptimization) {
                                VPSOptimization vps = (VPSOptimization) obj;
                                if (vps.getVpsId().equals(vpsId)) {
                                    
                                    if (getAvailableSlotUnits() >= vps.getSlotsRequired()) {
                                        if (installVPS(vps)) {
                                            System.out.println("ติดตั้ง VPS " + vpsId + " ใน Rack ระหว่างการซิงค์");
                                        }
                                    } else {
                                        System.out.println("ไม่สามารถติดตั้ง VPS " + vpsId + 
                                                          " ได้เนื่องจากไม่มีพื้นที่พอ (ต้องการ " + 
                                                          vps.getSlotsRequired() + " slots, มี " + 
                                                          getAvailableSlotUnits() + " slots)");
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            
            
            updateVPSDisplay(racks.get(currentRackIndex));
            
            System.out.println("ซิงค์ข้อมูล Rack กับ GameState สำเร็จ");
            
        } catch (Exception e) {
            System.err.println("เกิดข้อผิดพลาดในการซิงค์ข้อมูล Rack: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
    public Map<Integer, Integer> getAllSlotStatus() {
        Map<Integer, Integer> slotStatusMap = new HashMap<>();
        
        if (currentRackIndex < 0 || currentRackIndex >= racks.size()) {
            return slotStatusMap; 
        }
        
        VBox currentRack = racks.get(currentRackIndex);
        int currentUnlockedSlots = unlockedSlotUnitsList.get(currentRackIndex);
        
        
        int usedSlots = 0;
        for (VPSOptimization vps : rackVPS.get(currentRackIndex)) {
            int slotSize = vps.getSlotsRequired();
            
            for (int i = 0; i < slotSize; i++) {
                if (usedSlots + i < currentRack.getChildren().size()) {
                    slotStatusMap.put(usedSlots + i, 1); 
                }
            }
            usedSlots += slotSize;
        }
        
        
        for (int i = 0; i < currentRack.getChildren().size(); i++) {
            if (!slotStatusMap.containsKey(i)) {
                if (i < currentUnlockedSlots) {
                    slotStatusMap.put(i, 0); 
                } else {
                    slotStatusMap.put(i, -1); 
                }
            }
        }
        
        return slotStatusMap;
    }
    
    
    public Map<Integer, Map<Integer, Integer>> getAllRacksSlotStatus() {
        Map<Integer, Map<Integer, Integer>> allRacksStatus = new HashMap<>();
        
        
        int savedIndex = currentRackIndex;
        
        
        for (int i = 0; i < racks.size(); i++) {
            
            setRackIndex(i);
            
            
            Map<Integer, Integer> rackSlotStatus = getAllSlotStatus();
            
            
            allRacksStatus.put(i, rackSlotStatus);
        }
        
        
        setRackIndex(savedIndex);
        
        return allRacksStatus;
    }
    
    
    public List<Integer> getUninstalledSlots() {
        List<Integer> uninstalledSlots = new ArrayList<>();
        
        Map<Integer, Integer> slotStatusMap = getAllSlotStatus();
        for (Map.Entry<Integer, Integer> entry : slotStatusMap.entrySet()) {
            if (entry.getValue() == 0) { 
                uninstalledSlots.add(entry.getKey());
            }
        }
        
        return uninstalledSlots;
    }
    
    
    public Map<Integer, VPSOptimization> getInstalledSlotsWithVPS() {
        Map<Integer, VPSOptimization> installedSlots = new HashMap<>();
        
        if (currentRackIndex < 0 || currentRackIndex >= rackVPS.size()) {
            return installedSlots; 
        }
        
        
        int usedSlots = 0;
        for (VPSOptimization vps : rackVPS.get(currentRackIndex)) {
            int slotSize = vps.getSlotsRequired();
            
            installedSlots.put(usedSlots, vps);
            usedSlots += slotSize;
        }
        
        return installedSlots;
    }
}

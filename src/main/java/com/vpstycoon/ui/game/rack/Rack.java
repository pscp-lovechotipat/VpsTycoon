package com.vpstycoon.ui.game.rack;

import com.vpstycoon.game.vps.VPSOptimization;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Rack extends StackPane implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // UI components - mark as transient since JavaFX components are not serializable
    private transient List<VBox> racks;
    private transient Button prevButton;
    private transient Button nextButton;
    private transient VBox navigationButtons;
    
    // Serializable data
    private final List<List<VPSOptimization>> rackVPS; // Store VPS for each rack
    private final List<Integer> unlockedSlotUnitsList; // Store unlocked slots for each rack
    private final List<Integer> occupiedSlotUnitsList; // Store occupied slots for each rack
    private int currentRackIndex;
    private int maxSlotUnits;
    private int unlockedSlotUnits;
    private int occupiedSlotUnits;
    // Number of slots per rack (needed for reconstruction)
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
    
    // Initialize transient UI fields
    private void initializeTransientFields() {
        racks = new ArrayList<>();
        
        // Create navigation buttons
        prevButton = new Button("←");
        nextButton = new Button("→");
        
        // Style the navigation buttons
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
        
        // Add hover effects
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

        // Create navigation buttons container
        navigationButtons = new VBox(10);
        navigationButtons.setAlignment(Pos.CENTER);
        navigationButtons.setPadding(new Insets(10));
        navigationButtons.getChildren().addAll(prevButton, nextButton);
        
        // Set up navigation button actions
        prevButton.setOnAction(e -> navigateToPreviousRack());
        nextButton.setOnAction(e -> navigateToNextRack());
        
        // Initially hide navigation buttons
        navigationButtons.setVisible(false);
        
        // Add navigation buttons to the rack
        getChildren().add(navigationButtons);
    }
    
    // Custom serialization
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    // Custom deserialization to reconstruct the UI components
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        
        // Recreate the transient UI components
        initializeTransientFields();
        
        // Recreate all rack UI components based on saved data
        for (int i = 0; i < slotsPerRack.size(); i++) {
            // Create the rack UI without adding to the data structures (they're already loaded)
            VBox newRack = createRackUI(slotsPerRack.get(i));
            racks.add(newRack);
        }
        
        // Show the current rack if any
        if (currentRackIndex >= 0 && currentRackIndex < racks.size()) {
            showCurrentRack();
        }
    }
    
    // Create rack UI component without modifying data structures
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
        
        // Add slots to the rack
        for (int i = 0; i < slots; i++) {
            VBox slot = createSlot();
            newRack.getChildren().add(slot);
        }
        
        return newRack;
    }

    public void addRack(int slots) {
        // Create new rack UI
        VBox newRack = createRackUI(slots);
        racks.add(newRack);
        
        // Store the number of slots for this rack (for serialization)
        slotsPerRack.add(slots);
        
        // Store rack data
        rackVPS.add(new ArrayList<>()); // Add empty VPS list for new rack
        unlockedSlotUnitsList.add(1); // Start with 1 unlocked slot for the new rack
        occupiedSlotUnitsList.add(0); // Initialize occupied slots to 0 for the new rack
        
        // Set the maxSlotUnits for the current rack
        maxSlotUnits = slots;
        unlockedSlotUnits = 1; // Start with 1 unlocked slot
        
        // If this is the first rack, set it as current
        if (currentRackIndex == -1) {
            currentRackIndex = 0;
            showCurrentRack();
        }
        
        // Show navigation buttons if we have more than one rack
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
                
                // Create a visual representation of the VPS
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
                
                // Add VPS information
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
        // Clear current content
        getChildren().clear();
        
        // Add navigation buttons back
        getChildren().add(navigationButtons);
        
        // Add current rack
        if (currentRackIndex >= 0 && currentRackIndex < racks.size()) {
            VBox currentRack = racks.get(currentRackIndex);
            getChildren().add(currentRack);
            
            // Update the class variables to reflect the current rack's properties
            unlockedSlotUnits = unlockedSlotUnitsList.get(currentRackIndex);
            
            // Update occupied slots from the list
            occupiedSlotUnits = occupiedSlotUnitsList.get(currentRackIndex);
            
            // No need to calculate this now as we track per rack
            // Calculate occupied slots for the current rack
            //occupiedSlotUnits = 0;
            //for (VPSOptimization vps : rackVPS.get(currentRackIndex)) {
            //    occupiedSlotUnits += vps.getSlotsRequired();
            //}
            
            // Update maxSlotUnits based on the number of slots in the current rack
            maxSlotUnits = currentRack.getChildren().size();
            
            // Update VPS display in the current rack
            updateVPSDisplay(currentRack);
        }
    }

    private void updateVPSDisplay(VBox rack) {
        // Clear all slots
        for (var slot : rack.getChildren()) {
            if (slot instanceof VBox) {
                ((VBox) slot).getChildren().clear();
            }
        }
        
        // Add VPS to slots
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

    // Navigate to the latest rack (newest purchase)
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

    // New methods for VPS management
    public boolean installVPS(VPSOptimization vps) {
        if (currentRackIndex < 0 || currentRackIndex >= unlockedSlotUnitsList.size()) {
            System.out.println("DEBUG: Cannot install VPS, no rack selected");
            return false; // ไม่สามารถติดตั้งได้ถ้ายังไม่มี rack
        }
        
        // Check if we have enough available slots in the current rack
        int availableSlots = getAvailableSlotUnits();
        int requiredSlots = vps.getSlotsRequired();
        
        System.out.println("DEBUG: Rack " + (currentRackIndex+1) + " - Available slots: " + availableSlots + 
                           ", Required slots: " + requiredSlots + 
                           ", Unlocked slots: " + unlockedSlotUnitsList.get(currentRackIndex) + 
                           ", Occupied slots: " + occupiedSlotUnitsList.get(currentRackIndex));
        
        if (availableSlots < requiredSlots) {
            System.out.println("DEBUG: Not enough slots available in rack " + (currentRackIndex+1));
            return false; // Not enough available slots
        }
        
        // Install the VPS
        rackVPS.get(currentRackIndex).add(vps);
        
        // Update the occupied slots for the current rack
        int currentOccupied = occupiedSlotUnitsList.get(currentRackIndex);
        occupiedSlotUnitsList.set(currentRackIndex, currentOccupied + vps.getSlotsRequired());
        
        // Update class variable
        occupiedSlotUnits = occupiedSlotUnitsList.get(currentRackIndex);
        
        vps.setInstalled(true);

        // Update the visual display
        if (currentRackIndex >= 0 && currentRackIndex < racks.size()) {
            updateVPSDisplay(racks.get(currentRackIndex));
        }
        return true;
    }

    public boolean uninstallVPS(VPSOptimization vps) {
        if (currentRackIndex < 0 || currentRackIndex >= rackVPS.size()) {
            return false; // ไม่สามารถถอนการติดตั้งได้ถ้ายังไม่มี rack
        }
        List<VPSOptimization> currentRackVPS = rackVPS.get(currentRackIndex);
        if (currentRackVPS.remove(vps)) {
            // Update the occupied slots for the current rack
            int currentOccupied = occupiedSlotUnitsList.get(currentRackIndex);
            occupiedSlotUnitsList.set(currentRackIndex, currentOccupied - vps.getSlotsRequired());
            
            // Update class variable
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
            return new ArrayList<>(); // คืนค่ารายการว่างถ้ายังไม่มี rack
        }
        return new ArrayList<>(rackVPS.get(currentRackIndex));
    }

    public int getMaxSlotUnits() {
        return maxSlotUnits;
    }

    public int getUnlockedSlotUnits() {
        if (currentRackIndex < 0 || currentRackIndex >= unlockedSlotUnitsList.size()) {
            return 0; // คืนค่า 0 ถ้ายังไม่มี rack
        }
        return unlockedSlotUnitsList.get(currentRackIndex);
    }

    public int getOccupiedSlotUnits() {
        return occupiedSlotUnits;
    }

    public int getAvailableSlotUnits() {
        if (currentRackIndex < 0 || currentRackIndex >= unlockedSlotUnitsList.size()) {
            return 0; // คืนค่า 0 ถ้ายังไม่มี rack
        }
        int unlockedSlots = unlockedSlotUnitsList.get(currentRackIndex);
        int occupiedSlots = occupiedSlotUnitsList.get(currentRackIndex);
        return unlockedSlots - occupiedSlots;
    }

    public boolean upgrade() {
        if (currentRackIndex < 0 || currentRackIndex >= unlockedSlotUnitsList.size()) {
            return false; // ไม่สามารถ upgrade ได้ถ้ายังไม่มี rack
        }
        
        int currentUnlockedSlots = unlockedSlotUnitsList.get(currentRackIndex);
        
        // Check if the current rack's unlocked slots are less than the maximum slots for this rack
        if (currentUnlockedSlots < racks.get(currentRackIndex).getChildren().size()) {
            // Increment unlocked slots for the current rack only
            unlockedSlotUnitsList.set(currentRackIndex, currentUnlockedSlots + 1);
            
            // Update the class variable to match
            unlockedSlotUnits = unlockedSlotUnitsList.get(currentRackIndex);
            
            showCurrentRack();
            return true;
        }
        return false;
    }

    /**
     * ดึงรายการ VPS ที่ติดตั้งทั้งหมดจากทุก Rack
     * @return List รวมของ VPS ที่ติดตั้งในทุก Rack
     */
    public List<VPSOptimization> getAllInstalledVPS() {
        List<VPSOptimization> allVPS = new ArrayList<>();
        
        // วนลูปทุก rack เพื่อรวบรวม VPS
        for (List<VPSOptimization> rackVPSList : rackVPS) {
            allVPS.addAll(rackVPSList);
        }
        
        return allVPS;
    }
    
    /**
     * ดึงรายการของ unlocked slot units สำหรับทุก rack
     * @return List ของ unlocked slot units สำหรับแต่ละ rack
     */
    public List<Integer> getUnlockedSlotUnitsList() {
        return new ArrayList<>(unlockedSlotUnitsList);
    }

    /**
     * ตั้งค่ารายการ slot unit ที่ปลดล็อคแล้ว
     * @param unlockedList รายการ slot unit ที่ต้องการตั้งค่า
     */
    public void setUnlockedSlotUnitsList(List<Integer> unlockedList) {
        if (unlockedList != null) {
            this.unlockedSlotUnitsList.clear();
            this.unlockedSlotUnitsList.addAll(unlockedList);
            System.out.println("ตั้งค่า unlockedSlotUnitsList: " + this.unlockedSlotUnitsList);
        }
    }
}
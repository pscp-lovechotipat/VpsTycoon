package com.vpstycoon.ui.game.rack;

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
import java.util.List;

public class Rack extends StackPane implements Serializable {
    private transient List<VBox> racks;
    private transient Button prevButton;
    private transient Button nextButton;
    private transient VBox navigationButtons;

    private final List<List<VPSOptimization>> rackVPS;
    private final List<Integer> unlockedSlotUnitsList;
    private final List<Integer> occupiedSlotUnitsList;
    private int currentRackIndex;
    private int maxSlotUnits;
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
}

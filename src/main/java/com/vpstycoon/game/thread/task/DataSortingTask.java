package com.vpstycoon.game.thread.task;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Data Sorting Task - Placeholder class
 * In the full implementation, player would need to sort data packets in correct order
 */
public class DataSortingTask extends GameTask {

    public DataSortingTask() {
        super(
                "Data Sorting",
                "Sort the data packets in the correct order",
                "/images/task/data_sorting_task.png",
                6000, // reward
                20,  // penalty (0.2 * 100)
                2,    // difficulty
                50    // time limit in seconds
        );
    }

    @Override
    protected void initializeTaskSpecifics() {
        VBox placeholderPane = new VBox(20);
        placeholderPane.setAlignment(Pos.CENTER);
        placeholderPane.setPadding(new Insets(30));
        placeholderPane.setStyle("-fx-background-color: rgba(5, 10, 25, 0.9);");
        
//        Text placeholderText = new Text("DATA SORTING SYSTEM");
//        placeholderText.setFont(Font.font("Orbitron", FontWeight.BOLD, 24));
//        placeholderText.setFill(Color.web("#00ffff"));
        
//        Text descText = new Text("Sort the data packets by priority level (1 to 10)");
//        descText.setFont(Font.font("Share Tech Mono", FontWeight.NORMAL, 16));
//        descText.setFill(Color.LIGHTCYAN);
        
        // Create data packets grid
        GridPane dataGrid = new GridPane();
        dataGrid.setHgap(10);
        dataGrid.setVgap(10);
        dataGrid.setAlignment(Pos.CENTER);
        dataGrid.setStyle("-fx-background-color: #0a1520; -fx-border-color: #3a4a5a; -fx-border-width: 2px; -fx-padding: 20px;");
        
        // Create droppable target area
        GridPane targetGrid = new GridPane();
        targetGrid.setHgap(10);
        targetGrid.setVgap(10);
        targetGrid.setAlignment(Pos.CENTER);
        targetGrid.setStyle("-fx-background-color: #152535; -fx-border-color: #00ffff; -fx-border-width: 2px; -fx-padding: 20px;");
        
        // Generate 10 data packets with random priorities
        List<Integer> priorities = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            priorities.add(i);
        }
        Collections.shuffle(priorities, new Random());
        
        log("Created data packets with shuffled priorities: " + priorities);
        
        List<DataPacket> dataPackets = new ArrayList<>();
        
        // Create data packets and add to source grid
        for (int i = 0; i < 10; i++) {
            int priority = priorities.get(i);
            DataPacket packet = new DataPacket(priority);
            dataPackets.add(packet);
            
            // Add to source grid in a 2x5 layout
            dataGrid.add(packet.getNode(), i % 5, i / 5);
        }
        
        // Create target slots
        for (int i = 0; i < 10; i++) {
            Rectangle targetSlot = new Rectangle(60, 60);
            targetSlot.setFill(Color.web("#152535"));
            targetSlot.setStroke(Color.web("#3a4a5a"));
            targetSlot.setStrokeWidth(2);
            
            Text slotNumber = new Text(String.valueOf(i + 1));
            slotNumber.setFont(Font.font("Share Tech Mono", FontWeight.BOLD, 16));
            slotNumber.setFill(Color.LIGHTGRAY);
            
            StackPane slotPane = new StackPane(targetSlot, slotNumber);
            
            // Add to target grid in a 2x5 layout
            targetGrid.add(slotPane, i % 5, i / 5);
            
            // Set up drop handling for this slot
            final int slotIndex = i;
            slotPane.setOnDragOver(event -> {
                if (event.getGestureSource() instanceof StackPane && 
                        ((StackPane)event.getGestureSource()).getUserData() instanceof DataPacket) {
                    event.acceptTransferModes(javafx.scene.input.TransferMode.MOVE);
                }
                event.consume();
            });
            
            slotPane.setOnDragDropped(event -> {
                StackPane source = (StackPane) event.getGestureSource();
                DataPacket packet = (DataPacket) source.getUserData();
                
                // Place packet in this slot
                targetGrid.getChildren().remove(slotPane);
                targetGrid.add(source, slotIndex % 5, slotIndex / 5);
                
                log("Placed packet with priority " + packet.getPriority() + " in slot " + (slotIndex + 1));
                
                // Check if task is complete (all packets in correct slots)
                checkTaskCompletion();
                
                event.setDropCompleted(true);
                event.consume();
            });
        }
        
        VBox sortingArea = new VBox(20);
        sortingArea.setAlignment(Pos.CENTER);
        sortingArea.getChildren().addAll(
            new Text("Unsorted Data Packets:") {{
                setFill(Color.LIGHTCYAN);
                setFont(Font.font("Share Tech Mono", FontWeight.NORMAL, 14));
            }},
            dataGrid,
            new Text("Sort By Priority (1-10):") {{
                setFill(Color.LIGHTCYAN);
                setFont(Font.font("Share Tech Mono", FontWeight.NORMAL, 14));
            }},
            targetGrid
        );
        
        placeholderPane.getChildren().addAll(sortingArea);
        gamePane.getChildren().add(placeholderPane);
    }
    
    // Custom class for data packets
    private class DataPacket {
        private final int priority;
        private final StackPane node;
        
        public DataPacket(int priority) {
            this.priority = priority;
            
            // Create visual representation
            Rectangle packet = new Rectangle(60, 60);
            packet.setFill(getPriorityColor(priority));
            packet.setStroke(Color.WHITE);
            packet.setStrokeWidth(2);
            
            Text priorityText = new Text(String.valueOf(priority));
            priorityText.setFont(Font.font("Share Tech Mono", FontWeight.BOLD, 20));
            priorityText.setFill(Color.WHITE);
            
            node = new StackPane(packet, priorityText);
            node.setUserData(this);
            
            // Set up drag handling
            node.setOnDragDetected(event -> {
                javafx.scene.input.Dragboard db = node.startDragAndDrop(javafx.scene.input.TransferMode.MOVE);
                javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                content.putString(String.valueOf(priority));
                db.setContent(content);
                event.consume();
            });
            
            node.setOnDragDone(event -> {
                if (event.getTransferMode() == javafx.scene.input.TransferMode.MOVE) {
                    // Remove from source grid if moved
                    GridPane parent = (GridPane) node.getParent();
                    if (parent != null) {
                        parent.getChildren().remove(node);
                    }
                }
                event.consume();
            });
        }
        
        public int getPriority() {
            return priority;
        }
        
        public StackPane getNode() {
            return node;
        }
        
        private Color getPriorityColor(int priority) {
            // Color gradient based on priority (1 = green, 10 = red)
            if (priority <= 3) return Color.GREEN;
            if (priority <= 6) return Color.YELLOW;
            if (priority <= 8) return Color.ORANGE;
            return Color.RED;
        }
    }
    
    /**
     * Check if all data packets are in the correct position
     */
    private void checkTaskCompletion() {
        log("Checking task completion...");
        GridPane targetGrid = null;
        
        // Find the target grid from the game pane
        for (javafx.scene.Node node : gamePane.getChildren()) {
            if (node instanceof VBox) {
                VBox vbox = (VBox) node;
                for (javafx.scene.Node child : vbox.getChildren()) {
                    if (child instanceof VBox) {
                        VBox sortingArea = (VBox) child;
                        for (javafx.scene.Node areaChild : sortingArea.getChildren()) {
                            if (areaChild instanceof GridPane && areaChild.getStyle().contains("#152535")) {
                                targetGrid = (GridPane) areaChild;
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        if (targetGrid == null) {
            log("ERROR: Target grid not found!");
            return;
        }
        
        int correctCount = 0;
        StringBuilder statusMessage = new StringBuilder("Current status: ");
        
        // แสดงข้อมูลเบื้องต้นของ GridPane
        log("Target grid children count: " + targetGrid.getChildren().size());
        
        // สร้าง array 2 มิติเพื่อเก็บแพ็กเก็ตที่พบในแต่ละตำแหน่ง
        DataPacket[][] packetGrid = new DataPacket[2][5]; // 2 rows, 5 columns
        
        // ตรวจหาแพ็กเก็ตทั้งหมดใน GridPane และใส่ลงในตำแหน่งที่ถูกต้อง
        for (javafx.scene.Node node : targetGrid.getChildren()) {
            if (node instanceof StackPane && ((StackPane)node).getUserData() instanceof DataPacket) {
                Integer rowIndex = GridPane.getRowIndex(node);
                Integer colIndex = GridPane.getColumnIndex(node);
                
                // ถ้า GridPane ไม่ได้กำหนดค่า index ให้ชัดเจน ให้ใช้ค่า default = 0
                rowIndex = (rowIndex == null) ? 0 : rowIndex;
                colIndex = (colIndex == null) ? 0 : colIndex;
                
                if (rowIndex < 2 && colIndex < 5) { // ป้องกัน index out of bounds
                    DataPacket packet = (DataPacket) ((StackPane)node).getUserData();
                    packetGrid[rowIndex][colIndex] = packet;
                    log("Found packet with priority " + packet.getPriority() + " at position [" + rowIndex + "," + colIndex + "]");
                }
            }
        }
        
        // ตรวจสอบแต่ละตำแหน่งว่ามีแพ็กเก็ตอยู่หรือไม่ และมีค่า priority ถูกต้องหรือไม่
        for (int i = 0; i < 10; i++) {
            int row = i / 5;
            int col = i % 5;
            
            // ตรวจสอบแพ็กเก็ตในตำแหน่งนี้
            DataPacket packet = packetGrid[row][col];
            
            if (packet != null) {
                // แพ็กเก็ตมีค่า priority ถูกต้องหรือไม่
                boolean isCorrect = packet.getPriority() == i + 1;
                if (isCorrect) {
                    correctCount++;
                    statusMessage.append("✓");
                } else {
                    statusMessage.append("✗");
                }
                log("Position " + (i + 1) + ": Found packet with priority " + packet.getPriority() + 
                    (isCorrect ? " (correct)" : " (incorrect)"));
            } else {
                statusMessage.append("_");
                log("Position " + (i + 1) + ": No packet found");
            }
        }
        
        log(statusMessage.toString());
        log("Current sorting progress: " + correctCount + "/10 packets in correct positions");
        
        // Complete task if all packets are in correct order
        if (correctCount == 10) {
            log("All data packets sorted correctly, completing task");
            completeTask();
        }
    }
} 
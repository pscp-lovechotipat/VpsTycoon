package com.vpstycoon.game.thread.task;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.scene.effect.Glow;
import javafx.scene.effect.DropShadow;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.vpstycoon.FontLoader;

/**
 * Firewall Defense Task - Placeholder class
 * In the full implementation, player would need to defend against incoming cyber attacks
 */
public class FirewallDefenseTask extends GameTask {

    private Timeline attackGenerator;
    private List<Timeline> attackTimelines = new ArrayList<>();
    private List<AttackNode> activeAttacks = new ArrayList<>();

    public FirewallDefenseTask() {
        super(
                "Firewall Defense",
                "Defend your network from incoming cyber attacks",
                "/images/task/firewall_task.png",
                7500, // reward
                25,  // penalty (0.25 * 100)
                3,    // difficulty
                60    // time limit in seconds
        );
    }

    @Override
    protected void initializeTaskSpecifics() {
        VBox placeholderPane = new VBox(20);
        placeholderPane.setAlignment(Pos.CENTER);
        placeholderPane.setPadding(new Insets(20));
        placeholderPane.setStyle("-fx-background-color: rgba(5, 10, 25, 0.9);");
        
        Text placeholderText = new Text("FIREWALL DEFENSE SYSTEM");
        placeholderText.setFont(FontLoader.SECTION_FONT);
        placeholderText.setFill(Color.web("#00ffff"));
        
        // Add glowing effect to title
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 255, 255, 0.7));
        shadow.setRadius(10);
        placeholderText.setEffect(shadow);
        
        Text descText = new Text("Scan and neutralize malicious attacks by clicking them.");
        descText.setFont(FontLoader.LABEL_FONT);
        descText.setFill(Color.LIGHTCYAN);
        
        // Main game area
        Pane defenseGrid = new Pane();
        defenseGrid.setPrefSize(600, 400);
        defenseGrid.setPadding(new Insets(20));
        defenseGrid.setStyle("-fx-background-color: #0a1520; -fx-border-color: #3a4a5a; -fx-border-width: 2px;");
        
        // Calculate the full width for the status bar (container width - padding)
        double statusBarWidth = defenseGrid.getPrefWidth() + 115;
        
        // Firewall status bar - Use the full width of the container
        Rectangle statusBar = new Rectangle(statusBarWidth, 30);
        statusBar.setFill(Color.GREEN);
        statusBar.setX(10);
        statusBar.setY(10);
        
        Text statusText = new Text("Firewall Integrity: 100%");
        statusText.setFont(FontLoader.LABEL_FONT);
        statusText.setFill(Color.WHITE);
        statusText.setX(statusBarWidth / 2 - 100); // Center the text
        statusText.setY(30);
        
        defenseGrid.getChildren().addAll(statusBar, statusText);
        
        // Attack counters
        int[] attacksToDefend = {10}; // Number of attacks to neutralize
        int[] attacksNeutralized = {0}; // Counter for neutralized attacks
        
        // Statistics display
        HBox statsBox = new HBox(40);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setPadding(new Insets(20));
        
        Text attacksRemainingText = new Text("Remaining Attacks: " + attacksToDefend[0]);
        attacksRemainingText.setFont(FontLoader.LABEL_FONT);
        attacksRemainingText.setFill(Color.LIGHTCYAN);
        
        Text attacksBlockedText = new Text("Attacks Neutralized: 0");
        attacksBlockedText.setFont(FontLoader.LABEL_FONT);
        attacksBlockedText.setFill(Color.LIGHTCYAN);
        
        statsBox.getChildren().addAll(attacksRemainingText, attacksBlockedText);
        
        // Attack generator timeline
        attackGenerator = new Timeline(
            new KeyFrame(Duration.seconds(1.2), event -> {
                if (attacksToDefend[0] > 0 && activeAttacks.size() < 5) {
                    // Create a new attack at random position
                    AttackNode attack = createRandomAttack(defenseGrid);
                    activeAttacks.add(attack);
                    
                    // Decrease remaining attacks
                    attacksToDefend[0]--;
                    attacksRemainingText.setText("Remaining Attacks: " + attacksToDefend[0]);
                    
                    // Auto-damage timeline for this attack
                    Timeline damageFire = new Timeline(
                        new KeyFrame(Duration.seconds(5), e -> {
                            if (!attack.isNeutralized()) {
                                // Attack hit firewall and caused damage
                                decreaseFirewallIntegrity(statusBar, statusText, statusBarWidth);
                                
                                // Remove attack
                                defenseGrid.getChildren().remove(attack.getStackPane());
                                activeAttacks.remove(attack);
                            }
                        })
                    );
                    damageFire.play();
                    attackTimelines.add(damageFire);
                    
                    // Setup click handler for attack
                    attack.setupClickHandler(defenseGrid, attacksNeutralized, attacksBlockedText, statusText);
                }
            })
        );
        attackGenerator.setCycleCount(Timeline.INDEFINITE);
        attackGenerator.play();
        
        placeholderPane.getChildren().addAll(placeholderText, descText, defenseGrid, statsBox);
        gamePane.getChildren().add(placeholderPane);
    }
    
    private void stopAllTimelines() {
        if (attackGenerator != null) {
            attackGenerator.stop();
        }
        
        for (Timeline timeline : attackTimelines) {
            if (timeline != null) {
                timeline.stop();
            }
        }
        
        // หยุด timelines ของแต่ละ attack
        for (AttackNode attack : activeAttacks) {
            attack.stopPulseAnimation();
        }
    }
    
    // Create a random attack visualization
    private AttackNode createRandomAttack(Pane container) {
        Random rand = random;
        
        // Get the visible area dimensions (accounting for the shape size)
        double attackSize = 40; // Maximum size of an attack node
        double safeMargin = 50; // Safe margin to avoid UI elements
        
        // Restrict the spawn area to avoid UI elements and stay within visible bounds
        double minX = safeMargin;
        double maxX = container.getPrefWidth() - safeMargin - attackSize;
        double minY = safeMargin;
        double maxY = container.getPrefHeight() - safeMargin - attackSize;
        
        // Random position within the safe area
        double x = minX + rand.nextDouble() * (maxX - minX);
        double y = minY + rand.nextDouble() * (maxY - minY);
        
        // Random attack type
        int attackType = rand.nextInt(3);
        
        // Create attack based on type
        AttackNode attack;
        if (attackType == 0) {
            // Virus attack - red square
            Rectangle virus = new Rectangle(30, 30);
            virus.setFill(Color.RED);
            virus.setArcWidth(5);
            virus.setArcHeight(5);
            virus.setRotate(45);
            
            attack = new AttackNode(virus, "VIRUS");
        } else if (attackType == 1) {
            // Malware - purple hexagon
            javafx.scene.shape.Polygon malware = new javafx.scene.shape.Polygon();
            for (int i = 0; i < 6; i++) {
                double angle = 2.0 * Math.PI * i / 6;
                malware.getPoints().add(20 * Math.cos(angle));
                malware.getPoints().add(20 * Math.sin(angle));
            }
            malware.setFill(Color.PURPLE);
            
            attack = new AttackNode(malware, "MALWARE");
        } else {
            // Ransomware - orange circle
            javafx.scene.shape.Circle ransomware = new javafx.scene.shape.Circle(18);
            ransomware.setFill(Color.ORANGE);
            
            attack = new AttackNode(ransomware, "RANSOM");
        }
        
        // Position the attack node
        attack.getStackPane().setLayoutX(x - 20);
        attack.getStackPane().setLayoutY(y - 20);
        
        // Add to container
        container.getChildren().add(attack.getStackPane());
        
        return attack;
    }
    
    // Decrease firewall integrity
    private void decreaseFirewallIntegrity(Rectangle statusBar, Text statusText, double fullWidth) {
        // Current width is proportional to integrity
        double currentWidth = statusBar.getWidth();
        
        // Decrease by 10%
        double newWidth = Math.max(0, currentWidth - (fullWidth * 0.1)); // 10% of full width
        statusBar.setWidth(newWidth);
        
        // Update percentage text
        int percentage = (int) (newWidth / fullWidth * 100);
        statusText.setText("Firewall Integrity: " + percentage + "%");
        
        log("Firewall integrity decreased to " + percentage + "%");
        
        // Change color based on percentage
        if (percentage < 25) {
            statusBar.setFill(Color.RED);
        } else if (percentage < 50) {
            statusBar.setFill(Color.ORANGE);
        } else if (percentage < 75) {
            statusBar.setFill(Color.YELLOW);
        }
        
        // Fail task if integrity reaches 0
        if (percentage <= 0) {
            log("Firewall integrity is zero, task failed");
            stopAllTimelines();
            failTask();
        }
    }
    
    // Class to represent an attack
    private class AttackNode {
        private final javafx.scene.shape.Shape shape;
        private final StackPane stackPane;
        private final String type;
        private boolean neutralized = false;
        private Timeline pulseAnimation;
        
        public AttackNode(javafx.scene.shape.Shape shape, String type) {
            this.shape = shape;
            this.type = type;
            
            // Create label with proper styling
            Text label = new Text(type);
            label.setFont(FontLoader.loadFont(10));
            label.setFill(Color.WHITE);
            
            // Add glow to the label
            Glow glow = new Glow(0.5);
            label.setEffect(glow);
            
            // Create stack pane to hold shape and label
            stackPane = new StackPane(shape, label);
            stackPane.setPrefSize(40, 40);
            
            // Add pulsing effect
            pulseAnimation = new Timeline(
                new KeyFrame(Duration.seconds(0), e -> stackPane.setOpacity(1.0)),
                new KeyFrame(Duration.seconds(0.5), e -> stackPane.setOpacity(0.7)),
                new KeyFrame(Duration.seconds(1.0), e -> stackPane.setOpacity(1.0))
            );
            pulseAnimation.setCycleCount(Timeline.INDEFINITE);
            pulseAnimation.play();
        }
        
        // Add click handler for stack pane
        public void setupClickHandler(Pane defenseGrid, int[] attacksNeutralized, Text attacksBlockedText, Text statusText) {
            stackPane.setOnMouseClicked(e -> {
                // Update counters
                attacksNeutralized[0]++;
                attacksBlockedText.setText("Attacks Neutralized: " + attacksNeutralized[0]);
                
                // Visual feedback
                shape.setFill(Color.GREEN.deriveColor(1, 1, 1, 0.5));
                
                // Neutralize attack
                neutralize();
                
                // Remove after animation
                Timeline removeTimeline = new Timeline(
                    new KeyFrame(Duration.seconds(0.5), ev -> {
                        defenseGrid.getChildren().remove(stackPane);
                        activeAttacks.remove(this);
                    })
                );
                removeTimeline.play();
                attackTimelines.add(removeTimeline);
                
                // Check if task is complete
                if (attacksNeutralized[0] >= 10 && 
                    Double.parseDouble(statusText.getText().replaceAll("[^0-9.]", "")) > 50) {
                    stopAllTimelines();
                    completeTask();
                }
            });
        }
        
        public void stopPulseAnimation() {
            if (pulseAnimation != null) {
                pulseAnimation.stop();
            }
        }
        
        public javafx.scene.shape.Shape getShape() {
            return shape;
        }
        
        public StackPane getStackPane() {
            return stackPane;
        }
        
        public String getType() {
            return type;
        }
        
        public boolean isNeutralized() {
            return neutralized;
        }
        
        public void neutralize() {
            neutralized = true;
        }
    }
} 
package com.vpstycoon.game.thread.task;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
        placeholderPane.setPadding(new Insets(30));
        placeholderPane.setStyle("-fx-background-color: rgba(5, 10, 25, 0.9);");
        
        Text placeholderText = new Text("FIREWALL DEFENSE SYSTEM");
        placeholderText.setFont(Font.font("Orbitron", FontWeight.BOLD, 24));
        placeholderText.setFill(Color.web("#00ffff"));
        
        Text descText = new Text("Scan and neutralize malicious attacks by clicking them.");
        descText.setFont(Font.font("Share Tech Mono", FontWeight.NORMAL, 16));
        descText.setFill(Color.LIGHTCYAN);
        
        // Main game area
        Pane defenseGrid = new Pane();
        defenseGrid.setPrefSize(600, 400);
        defenseGrid.setStyle("-fx-background-color: #0a1520; -fx-border-color: #3a4a5a; -fx-border-width: 2px;");
        
        // Firewall status bar
        Rectangle statusBar = new Rectangle(580, 30);
        statusBar.setFill(Color.GREEN);
        statusBar.setX(10);
        statusBar.setY(10);
        
        Text statusText = new Text("Firewall Integrity: 100%");
        statusText.setFont(Font.font("Share Tech Mono", FontWeight.BOLD, 14));
        statusText.setFill(Color.WHITE);
        statusText.setX(220);
        statusText.setY(30);
        
        defenseGrid.getChildren().addAll(statusBar, statusText);
        
        // Attack counters
        int[] attacksToDefend = {10}; // Number of attacks to neutralize
        int[] attacksNeutralized = {0}; // Counter for neutralized attacks
        
        // Statistics display
        HBox statsBox = new HBox(40);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setPadding(new Insets(10));
        
        Text attacksRemainingText = new Text("Remaining Attacks: " + attacksToDefend[0]);
        attacksRemainingText.setFont(Font.font("Share Tech Mono", FontWeight.NORMAL, 14));
        attacksRemainingText.setFill(Color.LIGHTCYAN);
        
        Text attacksBlockedText = new Text("Attacks Neutralized: 0");
        attacksBlockedText.setFont(Font.font("Share Tech Mono", FontWeight.NORMAL, 14));
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
                                decreaseFirewallIntegrity(statusBar, statusText);
                                
                                // Remove attack
                                defenseGrid.getChildren().remove(attack.getShape());
                                activeAttacks.remove(attack);
                            }
                        })
                    );
                    damageFire.play();
                    attackTimelines.add(damageFire);
                    
                    // Click handler for attack
                    attack.getShape().setOnMouseClicked(e -> {
                        // Neutralize attack
                        attack.neutralize();
                        
                        // Update counters
                        attacksNeutralized[0]++;
                        attacksBlockedText.setText("Attacks Neutralized: " + attacksNeutralized[0]);
                        
                        // Visual feedback
                        attack.getShape().setFill(Color.GREEN.deriveColor(1, 1, 1, 0.5));
                        
                        // Remove after animation
                        Timeline removeTimeline = new Timeline(
                            new KeyFrame(Duration.seconds(0.5), ev -> {
                                defenseGrid.getChildren().remove(attack.getShape());
                                activeAttacks.remove(attack);
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
        
        // Random position within the container
        double x = rand.nextDouble() * (container.getPrefWidth() - 60) + 30;
        double y = rand.nextDouble() * (container.getPrefHeight() - 100) + 50;
        
        // Random attack type
        int attackType = rand.nextInt(3);
        
        // Create attack based on type
        AttackNode attack;
        if (attackType == 0) {
            // Virus attack - red square
            Rectangle virus = new Rectangle(x, y, 30, 30);
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
                malware.getPoints().add(x + 20 * Math.cos(angle));
                malware.getPoints().add(y + 20 * Math.sin(angle));
            }
            malware.setFill(Color.PURPLE);
            
            attack = new AttackNode(malware, "MALWARE");
        } else {
            // Ransomware - orange circle
            javafx.scene.shape.Circle ransomware = new javafx.scene.shape.Circle(x, y, 18);
            ransomware.setFill(Color.ORANGE);
            
            attack = new AttackNode(ransomware, "RANSOM");
        }
        
        // Label
        Text label = new Text(attack.getType());
        label.setFont(Font.font("Share Tech Mono", FontWeight.BOLD, 10));
        label.setFill(Color.WHITE);
        label.setX(x - 15);
        label.setY(y + 5);
        
        // Add to container
        container.getChildren().addAll(attack.getShape(), label);
        
        return attack;
    }
    
    // Decrease firewall integrity
    private void decreaseFirewallIntegrity(Rectangle statusBar, Text statusText) {
        // Current width is proportional to integrity
        double currentWidth = statusBar.getWidth();
        
        // Decrease by 10%
        double newWidth = Math.max(0, currentWidth - 58); // 10% of 580
        statusBar.setWidth(newWidth);
        
        // Update percentage text
        int percentage = (int) (newWidth / 580 * 100);
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
        private final String type;
        private boolean neutralized = false;
        private Timeline pulseAnimation;
        
        public AttackNode(javafx.scene.shape.Shape shape, String type) {
            this.shape = shape;
            this.type = type;
            
            // Add pulsing effect
            pulseAnimation = new Timeline(
                new KeyFrame(Duration.seconds(0), e -> shape.setOpacity(1.0)),
                new KeyFrame(Duration.seconds(0.5), e -> shape.setOpacity(0.7)),
                new KeyFrame(Duration.seconds(1.0), e -> shape.setOpacity(1.0))
            );
            pulseAnimation.setCycleCount(Timeline.INDEFINITE);
            pulseAnimation.play();
        }
        
        public void stopPulseAnimation() {
            if (pulseAnimation != null) {
                pulseAnimation.stop();
            }
        }
        
        public javafx.scene.shape.Shape getShape() {
            return shape;
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
package com.vpstycoon.game.thread.task;

import com.vpstycoon.application.FontLoader;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class FirewallDefenseTask extends GameTask {

    private Timeline attackGenerator;
    private List<Timeline> attackTimelines = new ArrayList<>();
    private List<AttackNode> activeAttacks = new ArrayList<>();

    public FirewallDefenseTask() {
        super(
                "Firewall Defense",
                "Defend your network from incoming cyber attacks",
                "/images/task/firewall_task.png",
                7500, 
                25,  
                3,    
                60    
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
        
        
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 255, 255, 0.7));
        shadow.setRadius(10);
        placeholderText.setEffect(shadow);
        
        Text descText = new Text("Scan and neutralize malicious attacks by clicking them.");
        descText.setFont(FontLoader.LABEL_FONT);
        descText.setFill(Color.LIGHTCYAN);
        
        
        Pane defenseGrid = new Pane();
        defenseGrid.setPrefSize(600, 400);
        defenseGrid.setPadding(new Insets(20));
        defenseGrid.setStyle("-fx-background-color: #0a1520; -fx-border-color: #3a4a5a; -fx-border-width: 2px;");
        
        
        double statusBarWidth = defenseGrid.getPrefWidth() + 80;
        
        
        Rectangle statusBar = new Rectangle(statusBarWidth, 30);
        statusBar.setFill(Color.GREEN);
        statusBar.setX(10);
        statusBar.setY(10);
        
        Text statusText = new Text("Firewall Integrity: 100%");
        statusText.setFont(FontLoader.LABEL_FONT);
        statusText.setFill(Color.WHITE);
        statusText.setX(statusBarWidth / 2 - 100); 
        statusText.setY(30);
        
        defenseGrid.getChildren().addAll(statusBar, statusText);
        
        
        int[] attacksToDefend = {10}; 
        int[] attacksNeutralized = {0}; 
        
        
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
        
        
        attackGenerator = new Timeline(
            new KeyFrame(Duration.seconds(1.2), event -> {
                if (attacksToDefend[0] > 0 && activeAttacks.size() < 5) {
                    
                    AttackNode attack = createRandomAttack(defenseGrid);
                    activeAttacks.add(attack);
                    
                    
                    attacksToDefend[0]--;
                    attacksRemainingText.setText("Remaining Attacks: " + attacksToDefend[0]);
                    
                    
                    Timeline damageFire = new Timeline(
                        new KeyFrame(Duration.seconds(5), e -> {
                            if (!attack.isNeutralized()) {
                                
                                decreaseFirewallIntegrity(statusBar, statusText, statusBarWidth);
                                
                                
                                defenseGrid.getChildren().remove(attack.getStackPane());
                                activeAttacks.remove(attack);
                            }
                        })
                    );
                    damageFire.play();
                    attackTimelines.add(damageFire);
                    
                    
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
        
        
        for (AttackNode attack : activeAttacks) {
            attack.stopPulseAnimation();
        }
    }
    
    
    private AttackNode createRandomAttack(Pane container) {
        Random rand = random;
        
        
        double attackSize = 40; 
        double safeMargin = 50; 
        
        
        double minX = safeMargin;
        double maxX = container.getPrefWidth() - safeMargin - attackSize;
        double minY = safeMargin;
        double maxY = container.getPrefHeight() - safeMargin - attackSize;
        
        
        double x = minX + rand.nextDouble() * (maxX - minX);
        double y = minY + rand.nextDouble() * (maxY - minY);
        
        
        int attackType = rand.nextInt(3);
        
        
        AttackNode attack;
        if (attackType == 0) {
            
            Rectangle virus = new Rectangle(30, 30);
            virus.setFill(Color.RED);
            virus.setArcWidth(5);
            virus.setArcHeight(5);
            virus.setRotate(45);
            
            attack = new AttackNode(virus, "VIRUS");
        } else if (attackType == 1) {
            
            javafx.scene.shape.Polygon malware = new javafx.scene.shape.Polygon();
            for (int i = 0; i < 6; i++) {
                double angle = 2.0 * Math.PI * i / 6;
                malware.getPoints().add(20 * Math.cos(angle));
                malware.getPoints().add(20 * Math.sin(angle));
            }
            malware.setFill(Color.PURPLE);
            
            attack = new AttackNode(malware, "MALWARE");
        } else {
            
            javafx.scene.shape.Circle ransomware = new javafx.scene.shape.Circle(18);
            ransomware.setFill(Color.ORANGE);
            
            attack = new AttackNode(ransomware, "RANSOM");
        }
        
        
        attack.getStackPane().setLayoutX(x - 20);
        attack.getStackPane().setLayoutY(y - 20);
        
        
        container.getChildren().add(attack.getStackPane());
        
        return attack;
    }
    
    
    private void decreaseFirewallIntegrity(Rectangle statusBar, Text statusText, double fullWidth) {
        
        double currentWidth = statusBar.getWidth();
        
        
        double newWidth = Math.max(0, currentWidth - (fullWidth * 0.1)); 
        statusBar.setWidth(newWidth);
        
        
        int percentage = (int) (newWidth / fullWidth * 100);
        statusText.setText("Firewall Integrity: " + percentage + "%");
        
        log("Firewall integrity decreased to " + percentage + "%");
        
        
        if (percentage < 25) {
            statusBar.setFill(Color.RED);
        } else if (percentage < 50) {
            statusBar.setFill(Color.ORANGE);
        } else if (percentage < 75) {
            statusBar.setFill(Color.YELLOW);
        }
        
        
        if (percentage <= 0) {
            log("Firewall integrity is zero, task failed");
            stopAllTimelines();
            failTask();
        }
    }
    
    
    private class AttackNode {
        private final javafx.scene.shape.Shape shape;
        private final StackPane stackPane;
        private final String type;
        private boolean neutralized = false;
        private Timeline pulseAnimation;
        
        public AttackNode(javafx.scene.shape.Shape shape, String type) {
            this.shape = shape;
            this.type = type;
            
            
            Text label = new Text(type);
            label.setFont(FontLoader.loadFont(10));
            label.setFill(Color.WHITE);
            
            
            Glow glow = new Glow(0.5);
            label.setEffect(glow);
            
            
            stackPane = new StackPane(shape, label);
            stackPane.setPrefSize(40, 40);
            
            
            pulseAnimation = new Timeline(
                new KeyFrame(Duration.seconds(0), e -> stackPane.setOpacity(1.0)),
                new KeyFrame(Duration.seconds(0.5), e -> stackPane.setOpacity(0.7)),
                new KeyFrame(Duration.seconds(1.0), e -> stackPane.setOpacity(1.0))
            );
            pulseAnimation.setCycleCount(Timeline.INDEFINITE);
            pulseAnimation.play();
        }
        
        
        public void setupClickHandler(Pane defenseGrid, int[] attacksNeutralized, Text attacksBlockedText, Text statusText) {
            stackPane.setOnMouseClicked(e -> {
                
                attacksNeutralized[0]++;
                attacksBlockedText.setText("Attacks Neutralized: " + attacksNeutralized[0]);
                
                
                shape.setFill(Color.GREEN.deriveColor(1, 1, 1, 0.5));
                
                
                neutralize();
                
                
                Timeline removeTimeline = new Timeline(
                    new KeyFrame(Duration.seconds(0.5), ev -> {
                        defenseGrid.getChildren().remove(stackPane);
                        activeAttacks.remove(this);
                    })
                );
                removeTimeline.play();
                attackTimelines.add(removeTimeline);
                
                
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

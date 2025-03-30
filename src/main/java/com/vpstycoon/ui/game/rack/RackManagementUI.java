package com.vpstycoon.ui.game.rack;

import com.vpstycoon.game.GameObject;
import com.vpstycoon.game.GameState;
import com.vpstycoon.game.company.SkillPointsSystem;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.game.resource.ResourceManager.RackUIUpdateListener;
import com.vpstycoon.game.vps.VPSOptimization;
import com.vpstycoon.ui.game.GameplayContentPane;
import com.vpstycoon.ui.game.utils.UIUtils;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.control.ScrollPane;
import javafx.util.Duration;
import javafx.scene.Node;

import java.util.ArrayList;
import java.util.List;

public class RackManagementUI extends VBox implements RackUIUpdateListener {
    private final GameplayContentPane parent;
    private final List<Pane> slotPanes = new ArrayList<>();
    private final int MAX_SLOTS = 10;
    private final Rack rack;
    private final Label rackInfoLabel;
    private final Label vpsListLabel;
    private final VBox vpsList;
    private final Button prevRackButton;
    private final Button nextRackButton;
    private final Button upgradeButton;
    private final Label upgradeCostLabel;
    private final Label upgradeInfoLabel;
    private final BorderPane mainRackDisplay;
    private final StackPane rackViewport;
    

    private Label networkValueEmpty;
    private Label networkValuePopulated;


    private boolean[] previousUIStates;


    private List<Node> originalRootNodes;

    public RackManagementUI(GameplayContentPane parent) {
        this.parent = parent;
        this.rack = parent.getRack();
        setSpacing(15);
        setPadding(new Insets(15));
        setStyle("""
            -fx-background-color: #12071e;
            -fx-padding: 15px;
            -fx-background-radius: 8px;
            -fx-border-color: #8a2be2;
            -fx-border-width: 2px;
            -fx-border-style: solid;
            -fx-effect: dropshadow(gaussian, #7f00ff, 10, 0, 0, 0);
            """);


        rackViewport = new StackPane();
        rackViewport.setMinHeight(400);
        mainRackDisplay = new BorderPane();
        rackViewport.getChildren().add(mainRackDisplay);
        

        HBox navigationBox = new HBox(15);
        navigationBox.setAlignment(Pos.CENTER);
        
        prevRackButton = createCyberButton("← PREV RACK");
        nextRackButton = createCyberButton("NEXT RACK →");
        

        Glow glowEffect = new Glow(0.8);
        prevRackButton.setOnMouseEntered(e -> {
            prevRackButton.setEffect(glowEffect);
            prevRackButton.setStyle(prevRackButton.getStyle() + "-fx-background-color: #6600cc; -fx-text-fill: #ffffff;");
        });
        prevRackButton.setOnMouseExited(e -> {
            prevRackButton.setEffect(null);
            prevRackButton.setStyle(getCyberButtonStyle());
        });
        
        nextRackButton.setOnMouseEntered(e -> {
            nextRackButton.setEffect(glowEffect);
            nextRackButton.setStyle(nextRackButton.getStyle() + "-fx-background-color: #6600cc; -fx-text-fill: #ffffff;");
        });
        nextRackButton.setOnMouseExited(e -> {
            nextRackButton.setEffect(null);
            nextRackButton.setStyle(getCyberButtonStyle());
        });

        navigationBox.getChildren().addAll(prevRackButton, nextRackButton);
        getChildren().add(navigationBox);


        rackInfoLabel = new Label();
        rackInfoLabel.setStyle("""
            -fx-text-fill: #00ffff; 
            -fx-font-weight: bold;
            -fx-font-family: 'Courier New';
            -fx-font-size: 18px;
            -fx-effect: dropshadow(gaussian, #00ffff, 5, 0, 0, 0);
            """);
        getChildren().add(rackInfoLabel);


        vpsListLabel = new Label("INSTALLED VPS");
        vpsListLabel.setStyle("""
            -fx-text-fill: #ff00ff; 
            -fx-font-weight: bold;
            -fx-font-family: 'Courier New';
            -fx-font-size: 16px;
            -fx-padding: 5px;
            -fx-background-color: #280042;
            -fx-background-radius: 4px;
            -fx-border-color: #ff00ff;
            -fx-border-width: 1px;
            -fx-border-radius: 4px;
            """);
        getChildren().add(vpsListLabel);


        vpsList = new VBox(10);
        vpsList.setStyle("""
            -fx-background-color: #1a0033;
            -fx-padding: 15px;
            -fx-background-radius: 5px;
            -fx-border-color: #8a2be2;
            -fx-border-width: 2px;
            -fx-border-style: dashed;
            -fx-effect: dropshadow(gaussian, #7f00ff, 5, 0, 0, 0);
            """);

        ScrollPane scrollPane = new ScrollPane(vpsList);
        scrollPane.setStyle("""
            -fx-background-color: transparent;
            -fx-background: transparent;
            -fx-background-insets: 0;
            -fx-padding: 0;
            -fx-border-color: #8a2be2;
            -fx-border-width: 1px;
            """);
        scrollPane.setFitToWidth(true);
        getChildren().add(scrollPane);


        int initialUpgradeCost = calculateUpgradeCost();
        upgradeButton = createCyberButton("UPGRADE RACK ($" + initialUpgradeCost + ")");
        upgradeButton.setOnMouseEntered(e -> {
            upgradeButton.setEffect(glowEffect);
            upgradeButton.setStyle(upgradeButton.getStyle() + "-fx-background-color: #6600cc; -fx-text-fill: #ffffff;");
        });
        upgradeButton.setOnMouseExited(e -> {
            upgradeButton.setEffect(null);
            upgradeButton.setStyle(getCyberButtonStyle());
        });
        getChildren().add(upgradeButton);

        upgradeCostLabel = new Label();
        upgradeCostLabel.setStyle("""
            -fx-text-fill: #00ffff;
            -fx-font-family: 'Courier New';
            -fx-font-size: 14px;
            """);
        getChildren().add(upgradeCostLabel);

        upgradeInfoLabel = new Label();
        upgradeInfoLabel.setStyle("""
            -fx-text-fill: #00ffff;
            -fx-font-family: 'Courier New';
            -fx-font-size: 14px;
            -fx-padding: 5px;
            -fx-background-color: #1a0033;
            -fx-background-radius: 4px;
            -fx-border-color: #8a2be2;
            -fx-border-width: 1px;
            -fx-border-radius: 4px;
            """);
        getChildren().add(upgradeInfoLabel);


        prevRackButton.setOnAction(e -> {
            if (rack.prevRack()) {

                updateUI();
            }
        });

        nextRackButton.setOnAction(e -> {
            if (rack.nextRack()) {

                updateUI();
            }
        });

        upgradeButton.setOnAction(e -> {
            Rack currentRack = parent.getRack();
            int currentUnlockedSlots = currentRack.getUnlockedSlotUnits();
            int maxRackSlots = currentRack.getMaxSlotUnits();

            if (currentUnlockedSlots >= maxRackSlots) {
                parent.pushNotification("CANNOT UPGRADE", "This rack already has maximum slots unlocked (" + maxRackSlots + " slots)");
                return;
            }

            int currentUpgradeCost = calculateUpgradeCost();
            if (parent.getCompany().getMoney() >= currentUpgradeCost) {
                // Deduct the cost first
                parent.getCompany().setMoney(parent.getCompany().getMoney() - currentUpgradeCost);
                
                if (parent.getRack().upgrade()) {
                    Timeline pulseAnimation = new Timeline();
                    pulseAnimation.getKeyFrames().addAll(
                        new KeyFrame(Duration.ZERO, 
                            new KeyValue(upgradeButton.scaleXProperty(), 1.0),
                            new KeyValue(upgradeButton.scaleYProperty(), 1.0)
                        ),
                        new KeyFrame(Duration.millis(200), 
                            new KeyValue(upgradeButton.scaleXProperty(), 1.05),
                            new KeyValue(upgradeButton.scaleYProperty(), 1.05)
                        ),
                        new KeyFrame(Duration.millis(400), 
                            new KeyValue(upgradeButton.scaleXProperty(), 1.0),
                            new KeyValue(upgradeButton.scaleYProperty(), 1.0)
                        )
                    );
                    
                    pulseAnimation.setOnFinished(event -> {
                        parent.pushNotification("UPGRADE COMPLETE", "RACK CAPACITY INCREASED TO " + 
                                              parent.getRack().getUnlockedSlotUnits() + " SLOTS");
                        openRackInfo();
                    });
                    
                    pulseAnimation.play();
                }
            } else {
                parent.pushNotification("UPGRADE FAILED", "INSUFFICIENT FUNDS");
            }
        });


        updateUI();
        

        ResourceManager.getInstance().addRackUIUpdateListener(this);
    }
    
    private Button createCyberButton(String text) {
        Button button = new Button(text);
        button.setStyle(getCyberButtonStyle());
        return button;
    }
    
    private String getCyberButtonStyle() {
        return """
            -fx-background-color: #3a015c;
            -fx-text-fill: #00ffff;
            -fx-font-weight: bold;
            -fx-font-family: 'Courier New';
            -fx-padding: 12px;
            -fx-background-radius: 0px;
            -fx-border-color: #ff00ff;
            -fx-border-width: 2px;
            -fx-border-style: solid;
            -fx-effect: dropshadow(gaussian, #7f00ff, 5, 0, 0, 0);
            """;
    }
    
    private void animateRackTransition(boolean isNext) {

        BorderPane newRackDisplay = new BorderPane();
        newRackDisplay.setTranslateX(isNext ? 800 : -800);
        rackViewport.getChildren().add(newRackDisplay);
        

        Timeline exitTimeline = new Timeline();
        KeyValue exitKv = new KeyValue(mainRackDisplay.translateXProperty(), isNext ? -800 : 800);
        KeyFrame exitKf = new KeyFrame(Duration.millis(300), exitKv);
        exitTimeline.getKeyFrames().add(exitKf);
        

        Timeline entryTimeline = new Timeline();
        KeyValue entryKv = new KeyValue(newRackDisplay.translateXProperty(), 0);
        KeyFrame entryKf = new KeyFrame(Duration.millis(300), entryKv);
        entryTimeline.getKeyFrames().add(entryKf);
        
        exitTimeline.setOnFinished(e -> {
            entryTimeline.play();
            rackViewport.getChildren().remove(mainRackDisplay);
            updateUI();
        });
        
        exitTimeline.play();
    }
    
    private void animateRackUpgrade() {

        Glow upgradeGlow = new Glow();
        vpsList.setEffect(upgradeGlow);
        
        Timeline glowTimeline = new Timeline();
        KeyValue startGlow = new KeyValue(upgradeGlow.levelProperty(), 0.0);
        KeyValue peakGlow = new KeyValue(upgradeGlow.levelProperty(), 0.8);
        KeyValue endGlow = new KeyValue(upgradeGlow.levelProperty(), 0.0);
        
        KeyFrame kf1 = new KeyFrame(Duration.ZERO, startGlow);
        KeyFrame kf2 = new KeyFrame(Duration.millis(300), peakGlow);
        KeyFrame kf3 = new KeyFrame(Duration.millis(600), endGlow);
        
        glowTimeline.getKeyFrames().addAll(kf1, kf2, kf3);
        glowTimeline.setOnFinished(e -> {
            vpsList.setEffect(null);
            updateUI();
        });
        
        glowTimeline.play();
    }

    private void updateUI() {
        if (rack.getMaxRacks() == 0) {
            rackInfoLabel.setText("NO RACKS AVAILABLE");
            vpsList.getChildren().clear();
            Label emptyLabel = createCyberLabel("NO RACK PURCHASED");
            vpsList.getChildren().add(emptyLabel);
            upgradeInfoLabel.setText("PLEASE PURCHASE A RACK FROM MARKET");
            upgradeButton.setDisable(true);
            upgradeCostLabel.setText("UPGRADE COST: N/A");
            prevRackButton.setDisable(true);
            nextRackButton.setDisable(true);
        } else {

            rackInfoLabel.setText(String.format("RACK %d/%d", rack.getRackIndex() + 1, rack.getMaxRacks()));


            boolean dataLoaded = false;
            GameState currentState = ResourceManager.getInstance().getCurrentState();
            if (currentState != null && currentState.getGameObjects() != null) {

                vpsList.getChildren().clear();
                List<VPSOptimization> installedVPS = rack.getInstalledVPS();

                if (installedVPS.isEmpty()) {
                    Label emptyLabel = createCyberLabel("NO VPS INSTALLED");
                    vpsList.getChildren().add(emptyLabel);
                } else {
                    for (VPSOptimization vps : installedVPS) {

                        boolean vpsFoundInState = false;
                        for (GameObject obj : currentState.getGameObjects()) {
                            if (obj instanceof VPSOptimization) {
                                VPSOptimization stateVps = (VPSOptimization) obj;
                                if (stateVps.getVpsId().equals(vps.getVpsId())) {
                                    vpsFoundInState = true;
                                    break;
                                }
                            }
                        }
                        
                        VBox vpsBox = new VBox(5);
                        vpsBox.setStyle("""
                        -fx-background-color: #2d0052;
                        -fx-padding: 10px;
                        -fx-background-radius: 0px;
                        -fx-border-color: #00ffff;
                        -fx-border-width: 2px;
                        -fx-border-style: solid;
                        -fx-effect: dropshadow(gaussian, #00ffff, 3, 0, 0, 0);
                        """);

                        Label nameLabel = new Label("SERVER " + vps.getVCPUs() + "vCPU" + (vpsFoundInState ? "" : " [SYNCING]"));
                        nameLabel.setStyle("""
                            -fx-text-fill: #ff00ff; 
                            -fx-font-weight: bold;
                            -fx-font-family: 'Courier New';
                            """);

                        Label specsLabel = new Label(vps.getRamInGB() + "GB RAM");
                        specsLabel.setStyle("""
                            -fx-text-fill: #00ffff;
                            -fx-font-family: 'Courier New';
                            """);

                        Label sizeLabel = new Label(vps.getSize().getDisplayName());
                        sizeLabel.setStyle("""
                            -fx-text-fill: #00ffff;
                            -fx-font-family: 'Courier New';
                            """);
                            
                        Label statusLabel = new Label("STATUS: " + vps.getStatus());
                        statusLabel.setStyle("""
                            -fx-text-fill: #00ffff;
                            -fx-font-family: 'Courier New';
                            """);
                            
                        Label vmsLabel = new Label("VMs: " + vps.getVms().size() + "/" + vps.getMaxVMs());
                        vmsLabel.setStyle("""
                            -fx-text-fill: #00ffff;
                            -fx-font-family: 'Courier New';
                            """);

                        vpsBox.getChildren().addAll(nameLabel, specsLabel, sizeLabel, statusLabel, vmsLabel);
                        

                        vpsBox.setOnMouseEntered(e -> {
                            DropShadow glow = new DropShadow();
                            glow.setColor(Color.web("#00ffff"));
                            glow.setWidth(20);
                            glow.setHeight(20);
                            vpsBox.setEffect(glow);
                            vpsBox.setStyle(vpsBox.getStyle() + "-fx-border-color: #ff00ff;");
                        });
                        
                        vpsBox.setOnMouseExited(e -> {
                            vpsBox.setEffect(null);
                            vpsBox.setStyle("""
                                -fx-background-color: #2d0052;
                                -fx-padding: 10px;
                                -fx-background-radius: 0px;
                                -fx-border-color: #00ffff;
                                -fx-border-width: 2px;
                                -fx-border-style: solid;
                                -fx-effect: dropshadow(gaussian, #00ffff, 3, 0, 0, 0);
                                """);
                        });
                        
                        vpsList.getChildren().add(vpsBox);
                    }
                }
                dataLoaded = true;
            }
            

            if (!dataLoaded) {
                vpsList.getChildren().clear();
                List<VPSOptimization> installedVPS = rack.getInstalledVPS();

                if (installedVPS.isEmpty()) {
                    Label emptyLabel = createCyberLabel("NO VPS INSTALLED");
                    vpsList.getChildren().add(emptyLabel);
                } else {
                    for (VPSOptimization vps : installedVPS) {
                        VBox vpsBox = new VBox(5);
                        vpsBox.setStyle("""
                        -fx-background-color: #2d0052;
                        -fx-padding: 10px;
                        -fx-background-radius: 0px;
                        -fx-border-color: #00ffff;
                        -fx-border-width: 2px;
                        -fx-border-style: solid;
                        -fx-effect: dropshadow(gaussian, #00ffff, 3, 0, 0, 0);
                        """);

                        Label nameLabel = new Label("SERVER " + vps.getVCPUs() + "vCPU");
                        nameLabel.setStyle("""
                            -fx-text-fill: #ff00ff; 
                            -fx-font-weight: bold;
                            -fx-font-family: 'Courier New';
                            """);

                        Label specsLabel = new Label(vps.getRamInGB() + "GB RAM");
                        specsLabel.setStyle("""
                            -fx-text-fill: #00ffff;
                            -fx-font-family: 'Courier New';
                            """);

                        Label sizeLabel = new Label(vps.getSize().getDisplayName());
                        sizeLabel.setStyle("""
                            -fx-text-fill: #00ffff;
                            -fx-font-family: 'Courier New';
                            """);

                        vpsBox.getChildren().addAll(nameLabel, specsLabel, sizeLabel);
                        

                        vpsBox.setOnMouseEntered(e -> {
                            DropShadow glow = new DropShadow();
                            glow.setColor(Color.web("#00ffff"));
                            glow.setWidth(20);
                            glow.setHeight(20);
                            vpsBox.setEffect(glow);
                            vpsBox.setStyle(vpsBox.getStyle() + "-fx-border-color: #ff00ff;");
                        });
                        
                        vpsBox.setOnMouseExited(e -> {
                            vpsBox.setEffect(null);
                            vpsBox.setStyle("""
                                -fx-background-color: #2d0052;
                                -fx-padding: 10px;
                                -fx-background-radius: 0px;
                                -fx-border-color: #00ffff;
                                -fx-border-width: 2px;
                                -fx-border-style: solid;
                                -fx-effect: dropshadow(gaussian, #00ffff, 3, 0, 0, 0);
                                """);
                        });
                        
                        vpsList.getChildren().add(vpsBox);
                    }
                }
            }

            int availableSlots = rack.getMaxSlotUnits() - rack.getUnlockedSlotUnits();
            int upgradeCost = calculateUpgradeCost();
            

            int discountPercent = 0;
            try {
                SkillPointsSystem skillPointsSystem = 
                    ResourceManager.getInstance().getSkillPointsSystem();
                
                if (skillPointsSystem != null) {
                    discountPercent = skillPointsSystem.getRackSlotUpgradeDiscount();
                }
            } catch (Exception e) {
                System.out.println("Error getting skill points system: " + e.getMessage());
            }
            

            if (discountPercent > 0) {
                upgradeCostLabel.setText(String.format("UPGRADE COST: $%d (-%d%% DISCOUNT)", 
                    upgradeCost, discountPercent));

                Glow discountGlow = new Glow(0.5);
                upgradeCostLabel.setEffect(discountGlow);
                upgradeCostLabel.setStyle(upgradeCostLabel.getStyle() + "; -fx-text-fill: #00ffaa;");
            } else {
                upgradeCostLabel.setText(String.format("UPGRADE COST: $%d", upgradeCost));
                upgradeCostLabel.setEffect(null);
                upgradeCostLabel.setStyle(upgradeCostLabel.getStyle() + "; -fx-text-fill: #00ffff;");
            }
            
            upgradeInfoLabel.setText(String.format("SERVER: %d\nSLOTS: %d/%d (%d available)\nUPGRADE COST: $%d",
                    rack.getRackIndex() + 1,
                    rack.getUnlockedSlotUnits(),
                    rack.getMaxSlotUnits(),
                    availableSlots,
                    upgradeCost));

            boolean canAffordUpgrade = parent.getCompany().getMoney() >= upgradeCost;
            boolean hasAvailableSlots = rack.getUnlockedSlotUnits() < rack.getMaxSlotUnits();
            

            upgradeButton.setText("UPGRADE RACK ($" + upgradeCost + ")");
            

            upgradeButton.setDisable(!hasAvailableSlots || !canAffordUpgrade);
            prevRackButton.setDisable(rack.getRackIndex() <= 0);
            nextRackButton.setDisable(rack.getRackIndex() >= rack.getMaxRacks() - 1);
        }
    }
    
    private Label createCyberLabel(String text) {
        Label label = new Label(text);
        label.setStyle("""
            -fx-text-fill: #ff00ff;
            -fx-font-family: 'Courier New';
            -fx-font-size: 14px;
            -fx-padding: 5px;
            -fx-background-color: #2d0052;
            -fx-background-radius: 0px;
            -fx-border-color: #00ffff;
            -fx-border-width: 1px;
            -fx-border-style: dashed;
            """);
        return label;
    }

    private int calculateUpgradeCost() {
        int currentUnlocked = rack.getUnlockedSlotUnits();
        int baseCost = currentUnlocked * 100;
        

        int discountPercent = 0;
        try {

            SkillPointsSystem skillPointsSystem = 
                ResourceManager.getInstance().getSkillPointsSystem();
            
            if (skillPointsSystem != null) {
                discountPercent = skillPointsSystem.getRackSlotUpgradeDiscount();
            }
        } catch (Exception e) {
            System.out.println("Error getting skill points system: " + e.getMessage());
        }
        

        if (discountPercent > 0) {
            baseCost = (int)(baseCost * (1 - discountPercent / 100.0));
        }
        
        return baseCost;
    }


    public void syncWithGameState() {
        GameState currentState = ResourceManager.getInstance().getCurrentState();
        if (currentState != null) {

            int currentIndex = rack.getRackIndex();
            

            boolean success = ResourceManager.getInstance().loadRackDataFromGameState(currentState);
            
            if (success) {

                Rack updatedRack = ResourceManager.getInstance().getRack();
                

                if (currentIndex >= 0 && currentIndex < updatedRack.getMaxRacks()) {
                    updatedRack.setRackIndex(currentIndex);
                    System.out.println("กลับไปที่ Rack #" + (currentIndex + 1) + " หลังจากซิงค์ข้อมูล");
                }
                

                updateUI();
                
                System.out.println("ซิงค์ข้อมูล RackManagementUI กับ GameState สำเร็จ");
                parent.pushNotification("Rack Management", "ข้อมูล Rack ได้รับการอัปเดตแล้ว");
            } else {
                System.out.println("ไม่สามารถซิงค์ข้อมูล RackManagementUI กับ GameState ได้");
                parent.pushNotification("Rack Management", "ไม่สามารถอัปเดตข้อมูล Rack ได้");
            }
        }
    }

    public synchronized void openRackInfo() {

        List<Node> originalRootNodes = new ArrayList<>(parent.getRootStack().getChildren());
        

        final boolean menuBarWasVisible = parent.getMenuBar().isVisible();
        final boolean marketMenuBarWasVisible = parent.getInGameMarketMenuBar().isVisible();
        final boolean moneyUIWasVisible = parent.getMoneyUI().isVisible();
        final boolean dateViewWasVisible = parent.getDateView().isVisible();
        

        this.previousUIStates = new boolean[] {
            menuBarWasVisible, marketMenuBarWasVisible, moneyUIWasVisible, dateViewWasVisible
        };
        
        BorderPane rackPane = new BorderPane();
        rackPane.setPrefSize(800, 600);
        rackPane.getStyleClass().add("rack-pane");
        

        rackPane.setStyle("""
            -fx-background-color: linear-gradient(to bottom, #12071e, #3a015c);
            -fx-background-radius: 10px;
            -fx-effect: dropshadow(gaussian, #7f00ff, 15, 0, 0, 0);
            """);


        parent.getMenuBar().setVisible(false);
        parent.getInGameMarketMenuBar().setVisible(false);
        parent.getMoneyUI().setVisible(false);
        parent.getDateView().setVisible(false);


        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(15));
        topBar.setStyle("""
            -fx-background-color: #280042;
            -fx-border-color: #ff00ff;
            -fx-border-width: 0 0 2 0;
            -fx-effect: dropshadow(gaussian, #ff00ff, 5, 0, 0, 0);
        """);

        Label titleLabel = new Label("RACK MANAGEMENT SYSTEM");
        titleLabel.setStyle("""
            -fx-font-family: 'Courier New';
            -fx-font-size: 24px;
            -fx-font-weight: bold;
            -fx-text-fill: #00ffff;
            -fx-effect: dropshadow(gaussian, #00ffff, 5, 0, 0, 0);
        """);

        Button closeButton = createPixelButton("CLOSE", "#F44336");
        closeButton.setOnAction(e -> {

            dispose();
            

            parent.returnToRoom();
        });


        Button prevRackButton = createPixelButton("◄ PREV", "#6a00ff");
        Button nextRackButton = createPixelButton("NEXT ►", "#6a00ff");


        Label rackIndexLabel = new Label("RACK_" + (parent.getRack().getRackIndex() + 1));
        rackIndexLabel.setStyle("""
            -fx-font-family: 'Courier New';
            -fx-font-size: 18px;
            -fx-text-fill: #ff00ff;
            -fx-font-weight: bold;
            -fx-padding: 5px;
            -fx-background-color: #1a0033;
            -fx-background-radius: 0px;
            -fx-border-color: #ff00ff;
            -fx-border-width: 2px;
            -fx-border-style: solid;
            -fx-effect: dropshadow(gaussian, #ff00ff, 3, 0, 0, 0);
        """);


        prevRackButton.setOnAction(e -> {
            if (parent.getRack().prevRack()) {

                rackIndexLabel.setText("RACK_" + (parent.getRack().getRackIndex() + 1));
                openRackInfo();
            } else {
                parent.pushNotification("Navigation", "You are at the first rack.");
            }
        });

        nextRackButton.setOnAction(e -> {
            if (parent.getRack().nextRack()) {

                rackIndexLabel.setText("RACK_" + (parent.getRack().getRackIndex() + 1));
                openRackInfo();
            } else {
                parent.pushNotification("Navigation", "You are at the last rack.");
            }
        });

        HBox navigationBox = new HBox(10);
        navigationBox.setAlignment(Pos.CENTER_RIGHT);
        navigationBox.getChildren().addAll(prevRackButton, rackIndexLabel, nextRackButton);

        topBar.getChildren().addAll(titleLabel, navigationBox, closeButton);


        BorderPane contentBox = new BorderPane();
        contentBox.setPadding(new Insets(10));
        contentBox.setStyle("""
            -fx-background-color: #12071e;
            -fx-padding: 10px;
            -fx-border-color: #8a2be2;
            -fx-border-width: 2px;
            -fx-border-style: solid;
            -fx-background-radius: 0px;
            -fx-effect: dropshadow(gaussian, #8a2be2, 10, 0, 0, 0);
            """);


        if (parent.getRack().getMaxSlotUnits() == 0) {
            VBox noRackMessage = new VBox(20);
            noRackMessage.setAlignment(Pos.CENTER);
            noRackMessage.setSpacing(20);

            Label messageLabel = new Label("RACK NOT DETECTED");
            messageLabel.setStyle("""
                -fx-font-family: 'Courier New';
                -fx-font-size: 24px;
                -fx-text-fill: #ff00ff;
                -fx-font-weight: bold;
                -fx-effect: dropshadow(gaussian, #ff00ff, 10, 0, 0, 0);
            """);

            Label descriptionLabel = new Label("INITIATE PROCUREMENT PROTOCOL: RACK ACQUISITION REQUIRED");
            descriptionLabel.setStyle("""
                -fx-font-family: 'Courier New';
                -fx-font-size: 16px;
                -fx-text-fill: #00ffff;
            """);

            Button marketButton = createPixelButton("ACCESS MARKET TERMINAL", "#6a00ff");
            marketButton.setOnAction(e -> {
                parent.openMarket();
            });

            noRackMessage.getChildren().addAll(messageLabel, descriptionLabel, marketButton);
            contentBox.setCenter(noRackMessage);
        } else if (parent.getVpsList().isEmpty()) {

            HBox mainLayout = new HBox(20);
            mainLayout.setAlignment(Pos.CENTER_LEFT);
            

            VBox rackCabinet = new VBox(0);
            rackCabinet.setPadding(new Insets(0));
            rackCabinet.setPrefWidth(500);
            rackCabinet.setMinHeight(500);
            rackCabinet.setMaxHeight(Double.MAX_VALUE);
            

            BorderPane rackFrame = new BorderPane();
            rackFrame.setPrefWidth(500);
            rackFrame.setMaxHeight(Double.MAX_VALUE);
            rackFrame.setStyle("""
                -fx-background-color: #191919;
                -fx-border-color: #00ffff;
                -fx-border-width: 2;
                -fx-border-style: solid;
                -fx-effect: dropshadow(gaussian, #00ffff, 5, 0, 0, 0);
                """);
            

            VBox unitMarkings = new VBox(0);
            unitMarkings.setPrefWidth(30);
            unitMarkings.setStyle("""
                -fx-background-color: #111111;
                -fx-border-color: #333333;
                -fx-border-width: 0 1 0 0;
                """);
            

            ScrollPane slotContainer = new ScrollPane();
            slotContainer.setFitToWidth(true);
            slotContainer.setFitToHeight(true);
            slotContainer.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            slotContainer.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            slotContainer.setStyle("""
                -fx-background-color: transparent;
                -fx-background: transparent;
                -fx-background-insets: 0;
                -fx-padding: 0;
                """);
            

            GridPane rackSlots = new GridPane();
            rackSlots.setVgap(2);
            rackSlots.setHgap(0);
            rackSlots.setPadding(new Insets(0));
            rackSlots.setStyle("""
                -fx-background-color: #141414;
                """);
            
            ColumnConstraints col1 = new ColumnConstraints();
            col1.setPrefWidth(450);
            col1.setHgrow(Priority.ALWAYS);
            
            ColumnConstraints col2 = new ColumnConstraints();
            col2.setPrefWidth(50);
            
            rackSlots.getColumnConstraints().addAll(col1, col2);


            int slotUnitHeight = 40;

            for (int i = 0; i < parent.getRack().getMaxSlotUnits(); i++) {
                RowConstraints row = new RowConstraints();
                row.setPrefHeight(slotUnitHeight);
                row.setMinHeight(slotUnitHeight);
                rackSlots.getRowConstraints().add(row);
                

                Label unitLabel = new Label(String.format("U%02d", i + 1));
                unitLabel.setStyle("""
                -fx-font-family: 'Courier New';
                    -fx-font-size: 10px;
                    -fx-text-fill: #666666;
                    """);
                StackPane unitLabelBox = new StackPane(unitLabel);
                unitLabelBox.setPrefHeight(slotUnitHeight);
                unitLabelBox.setAlignment(Pos.CENTER);
                rackSlots.add(unitLabelBox, 1, i);
                

                Pane slot = createEnhancedRackSlot(i, null, i < parent.getRack().getUnlockedSlotUnits(), slotUnitHeight);
                rackSlots.add(slot, 0, i);
                slotPanes.add(slot);
            }
            
            slotContainer.setContent(rackSlots);
            

            GridPane topVents = new GridPane();
            topVents.setPrefHeight(15);
            topVents.setHgap(4);
            topVents.setVgap(2);
            topVents.setAlignment(Pos.CENTER);
            topVents.setStyle("-fx-background-color: #222222;");
            
            for (int i = 0; i < 20; i++) {
                Rectangle vent = new Rectangle(15, 3);
                vent.setFill(Color.web("#111111"));
                vent.setArcWidth(1);
                vent.setArcHeight(1);
                topVents.add(vent, i % 10, i / 10);
            }
            

            HBox powerSupply = new HBox(10);
            powerSupply.setPrefHeight(20);
            powerSupply.setStyle("-fx-background-color: #222222;");
            powerSupply.setAlignment(Pos.CENTER_RIGHT);
            

            Rectangle powerButton = new Rectangle(8, 8);
            powerButton.setFill(Color.web("#00ff00"));
            

            Timeline pulseTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, 
                    new KeyValue(powerButton.opacityProperty(), 1.0)),
                new KeyFrame(Duration.seconds(1.5), 
                    new KeyValue(powerButton.opacityProperty(), 0.5)),
                new KeyFrame(Duration.seconds(3), 
                    new KeyValue(powerButton.opacityProperty(), 1.0))
            );
            pulseTimeline.setCycleCount(Timeline.INDEFINITE);
            pulseTimeline.play();
            

            Rectangle powerMeter1 = new Rectangle(4, 6);
            powerMeter1.setFill(Color.web("#ff0000"));
            Rectangle powerMeter2 = new Rectangle(4, 6);
            powerMeter2.setFill(Color.web("#ffff00"));
            Rectangle powerMeter3 = new Rectangle(4, 6);
            powerMeter3.setFill(Color.web("#00ff00"));
            
            powerSupply.getChildren().addAll(powerMeter1, powerMeter2, powerMeter3, powerButton);
            powerSupply.setPadding(new Insets(0, 20, 0, 0));
            

            rackFrame.setCenter(slotContainer);
            rackCabinet.getChildren().addAll(topVents, rackFrame, powerSupply);
            

            VBox rackContainerWithSpacer = new VBox();
            rackContainerWithSpacer.setAlignment(Pos.BOTTOM_CENTER);
            rackContainerWithSpacer.setPrefWidth(500);
            

            Region spacer = new Region();
            VBox.setVgrow(spacer, Priority.ALWAYS);
            
            rackContainerWithSpacer.getChildren().addAll(spacer, rackCabinet);
            

            VBox infoPane = new VBox(15);
            infoPane.setPrefWidth(250);
            infoPane.setMinHeight(550);
            infoPane.setPrefHeight(600);
            infoPane.setMaxHeight(Double.MAX_VALUE);
            VBox.setVgrow(infoPane, Priority.ALWAYS);
            infoPane.setStyle("""
                -fx-background-color: #280042;
                -fx-padding: 15px;
                -fx-border-color: #8a2be2;
                -fx-border-width: 2px;
                -fx-border-style: solid;
                -fx-background-radius: 0px;
                -fx-effect: dropshadow(gaussian, #8a2be2, 5, 0, 0, 0);
                """);

            Label infoTitle = new Label("RACK INFO");
            infoTitle.setMaxWidth(Double.MAX_VALUE);
            infoTitle.setAlignment(Pos.CENTER);
            infoTitle.setStyle("""
                -fx-font-family: 'Courier New';
                -fx-font-size: 18px;
                -fx-font-weight: bold;
                -fx-text-fill: #ff00ff;
                -fx-effect: dropshadow(gaussian, #ff00ff, 3, 0, 0, 0);
                -fx-padding: 5px;
                -fx-background-color: #12071e;
                -fx-background-radius: 0px;
                -fx-border-color: #ff00ff;
                -fx-border-width: 1px;
                -fx-border-style: solid;
            """);
            

            Label emptyRackMessage = new Label("NO SERVERS INSTALLED");
            emptyRackMessage.setMaxWidth(Double.MAX_VALUE);
            emptyRackMessage.setAlignment(Pos.CENTER);
            emptyRackMessage.setStyle("""
                -fx-font-family: 'Courier New';
                -fx-font-size: 16px;
                -fx-text-fill: #00ffff;
                -fx-font-weight: bold;
                -fx-effect: dropshadow(gaussian, #00ffff, 5, 0, 0, 0);
                -fx-padding: 10px;
                -fx-background-color: #1a0033;
                -fx-background-radius: 0px;
                -fx-border-color: #00ffff;
                -fx-border-width: 1px;
                -fx-border-style: solid;
                -fx-alignment: center;
            """);


            VBox statusPanel = new VBox(8);
            statusPanel.setMaxWidth(Double.MAX_VALUE);
            statusPanel.setStyle("""
                -fx-background-color: #1a0033;
                -fx-padding: 10px;
                -fx-background-radius: 0px;
                -fx-border-color: #00ffff;
                -fx-border-width: 1px;
                -fx-border-style: solid;
                """);

            GridPane statusGrid = new GridPane();
            statusGrid.setMaxWidth(Double.MAX_VALUE);
            statusGrid.setHgap(10);
            statusGrid.setVgap(8);
            
            int usedSlots = parent.getRack().getOccupiedSlotUnits();
            int availableSlots = parent.getRack().getMaxSlotUnits() - usedSlots;
            

            Label serversLabel = new Label("SERVERS:");
            serversLabel.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 14px; -fx-text-fill: #00ffff; -fx-font-weight: bold;");
            Label serversValue = new Label("0");
            serversValue.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 14px; -fx-text-fill: #00ffff;");
            
            Label slotsLabel = new Label("SLOTS:");
            slotsLabel.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 14px; -fx-text-fill: #00ffff; -fx-font-weight: bold;");
            Label slotsValue = new Label(usedSlots + "/" + parent.getRack().getMaxSlotUnits() + 
                            " (" + parent.getRack().getAvailableSlotUnits() + " AVAILABLE)");
            slotsValue.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 14px; -fx-text-fill: #00ffff;");
            
            Label networkLabel = new Label("NETWORK:");
            networkLabel.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 14px; -fx-text-fill: #00ffff; -fx-font-weight: bold;");
            

            int baseNetworkSpeed = 10;
            int networkSpeedBonus = ResourceManager.getInstance().getSkillPointsSystem().getRackNetworkSpeedBonus();
            int totalNetworkSpeed = baseNetworkSpeed + networkSpeedBonus;
            networkValueEmpty = new Label(totalNetworkSpeed + " Gbps");
            
            networkValueEmpty.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 14px; -fx-text-fill: #00ffff;");
            
            Label usersLabel = new Label("ACTIVE USERS:");
            usersLabel.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 14px; -fx-text-fill: #00ffff; -fx-font-weight: bold;");
            Label usersValue = new Label("0");
            usersValue.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 14px; -fx-text-fill: #00ffff;");
            

            statusGrid.add(serversLabel, 0, 0);
            statusGrid.add(serversValue, 1, 0);
            statusGrid.add(slotsLabel, 0, 1);
            statusGrid.add(slotsValue, 1, 1);
            statusGrid.add(networkLabel, 0, 2);
            statusGrid.add(networkValueEmpty, 1, 2);
            statusGrid.add(usersLabel, 0, 3);
            statusGrid.add(usersValue, 1, 3);
            

            ColumnConstraints column1 = new ColumnConstraints();
            column1.setHgrow(Priority.NEVER);
            ColumnConstraints column2 = new ColumnConstraints();
            column2.setHgrow(Priority.ALWAYS);
            statusGrid.getColumnConstraints().addAll(column1, column2);
            
            statusPanel.getChildren().add(statusGrid);
            

            Button inventoryButton = createPixelButton("SERVER INVENTORY", "#3498db");
            inventoryButton.setMaxWidth(Double.MAX_VALUE);
            inventoryButton.setOnAction(e -> parent.openVPSInventory());
            
            int upgradeCost = calculateUpgradeCost();
            Button upgradeButton = createPixelButton("UPGRADE RACK ($" + upgradeCost + ")", "#4CAF50");
            upgradeButton.setMaxWidth(Double.MAX_VALUE);
            upgradeButton.setOnAction(e -> {
                Rack currentRack = parent.getRack();
                int currentUnlockedSlots = currentRack.getUnlockedSlotUnits();
                int maxRackSlots = currentRack.getMaxSlotUnits();

                if (currentUnlockedSlots >= maxRackSlots) {
                    parent.pushNotification("CANNOT UPGRADE", "This rack already has maximum slots unlocked (" + maxRackSlots + " slots)");
                    return;
                }

                int currentUpgradeCost = calculateUpgradeCost();
                if (parent.getCompany().getMoney() >= currentUpgradeCost) {

                    parent.getCompany().setMoney(parent.getCompany().getMoney() - currentUpgradeCost);
                    
                    if (parent.getRack().upgrade()) {
                        Timeline pulseAnimation = new Timeline();
                        pulseAnimation.getKeyFrames().addAll(
                            new KeyFrame(Duration.ZERO, 
                                new KeyValue(upgradeButton.scaleXProperty(), 1.0),
                                new KeyValue(upgradeButton.scaleYProperty(), 1.0)
                            ),
                            new KeyFrame(Duration.millis(200), 
                                new KeyValue(upgradeButton.scaleXProperty(), 1.05),
                                new KeyValue(upgradeButton.scaleYProperty(), 1.05)
                            ),
                            new KeyFrame(Duration.millis(400), 
                                new KeyValue(upgradeButton.scaleXProperty(), 1.0),
                                new KeyValue(upgradeButton.scaleYProperty(), 1.0)
                            )
                        );
                        
                        pulseAnimation.setOnFinished(event -> {
                            parent.pushNotification("UPGRADE COMPLETE", "RACK CAPACITY INCREASED TO " + 
                                                  parent.getRack().getUnlockedSlotUnits() + " SLOTS");
                            openRackInfo();
                        });
                        
                        pulseAnimation.play();
                    }
                } else {
                    parent.pushNotification("UPGRADE FAILED", "INSUFFICIENT FUNDS");
                }
            });
            
            Button marketButton = createPixelButton("MARKET", "#6a00ff");
            marketButton.setMaxWidth(Double.MAX_VALUE);
            marketButton.setOnAction(e -> {
                parent.openMarket();
            });


            infoPane.getChildren().addAll(infoTitle, emptyRackMessage, statusPanel, inventoryButton, upgradeButton, marketButton);
            

            List<VPSOptimization> uninstalledServers = parent.getVpsInventory().getAllVPS();
            if (!uninstalledServers.isEmpty()) {
                VBox uninstalledSection = new VBox(10);
                uninstalledSection.setPadding(new Insets(10, 0, 0, 0));
                VBox.setVgrow(uninstalledSection, Priority.ALWAYS);
                
                Label uninstalledLabel = new Label("AVAILABLE SERVERS");
                uninstalledLabel.setMaxWidth(Double.MAX_VALUE);
                uninstalledLabel.setAlignment(Pos.CENTER);
                uninstalledLabel.setStyle("""
                    -fx-font-family: 'Courier New';
                    -fx-font-size: 16px;
                    -fx-font-weight: bold;
                    -fx-text-fill: #ff00ff;
                    -fx-effect: dropshadow(gaussian, #ff00ff, 3, 0, 0, 0);
                    -fx-padding: 5px;
                    -fx-background-color: #12071e;
                    -fx-background-radius: 0px;
                    -fx-border-color: #ff00ff;
                    -fx-border-width: 1px;
                    -fx-border-style: solid;
                """);
                
                ScrollPane uninstalledScrollPane = new ScrollPane();
                uninstalledScrollPane.setStyle("""
                    -fx-background-color: transparent;
                    -fx-background: transparent; 
                    -fx-background-insets: 0;
                    -fx-padding: 0;
                """);
                uninstalledScrollPane.setFitToWidth(true);
                uninstalledScrollPane.setPrefHeight(200);
                uninstalledScrollPane.setMinHeight(200);
                VBox.setVgrow(uninstalledScrollPane, Priority.ALWAYS);
                
                VBox serverList = new VBox(8);
                serverList.setPadding(new Insets(5));
                
                for (VPSOptimization server : uninstalledServers) {

                    String serverId = "";
                    for (String id : parent.getVpsInventory().getAllVPSIds()) {
                        if (parent.getVpsInventory().getVPS(id) == server) {
                            serverId = id;
                            break;
                        }
                    }
                    
                    final String finalServerId = serverId;
                    
                    HBox serverRow = new HBox(5);
                    serverRow.setAlignment(Pos.CENTER_LEFT);
                    

                    VBox serverInfo = new VBox(2);
                    serverInfo.setPrefWidth(150);
                    serverInfo.setStyle("""
                        -fx-background-color: #2d0052;
                        -fx-padding: 5px;
                        -fx-background-radius: 0px;
                        -fx-border-color: #00ffff;
                        -fx-border-width: 2px;
                        -fx-border-style: solid;
                    """);
                    
                    Label nameLabel = new Label("SERVER " + server.getVCPUs() + "vCPU");
                    nameLabel.setStyle("""
                        -fx-text-fill: #ff00ff; 
                        -fx-font-weight: bold;
                        -fx-font-family: 'Courier New';
                    """);
                    
                    Label specsLabel = new Label(server.getRamInGB() + "GB RAM | " + server.getSize().getDisplayName());
                    specsLabel.setStyle("""
                        -fx-text-fill: #00ffff;
                        -fx-font-family: 'Courier New';
                    """);
                    
                    serverInfo.getChildren().addAll(nameLabel, specsLabel);
                    

                    Button installButton = createPixelButton("INSTALL", "#4CAF50");
                    

                    installButton.setOnMousePressed(e -> {
                        installButton.setStyle(
                            "-fx-background-color: #388E3C;" +
                            "-fx-font-family: 'Courier New';" +
                            "-fx-font-size: 14px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-text-fill: white;" +
                            "-fx-padding: 6px 10px;" +
                            "-fx-background-radius: 0px;" +
                            "-fx-border-color: white;" +
                            "-fx-border-width: 2px;" +
                            "-fx-translate-y: 2px;"
                        );
                    });
                    
                    installButton.setOnMouseReleased(e -> {
                        installButton.setStyle(
                            "-fx-background-color: #4CAF50;" +
                            "-fx-font-family: 'Courier New';" +
                            "-fx-font-size: 14px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-text-fill: white;" +
                            "-fx-padding: 8px 12px;" +
                            "-fx-background-radius: 0px;" +
                            "-fx-border-color: white;" +
                            "-fx-border-width: 2px;" +
                            "-fx-translate-y: 0px;"
                        );
                    });
                    
                    installButton.setOnAction(event -> {
                        try {
                            if (parent.installVPSFromInventory(finalServerId)) {
                                parent.pushNotification("SUCCESS", "Server installed successfully");
                                installButton.setDisable(true);
                                Timeline successAnimation = new Timeline(
                                    new KeyFrame(Duration.ZERO, 
                                        new KeyValue(installButton.textFillProperty(), Color.WHITE)),
                                    new KeyFrame(Duration.millis(200), 
                                        new KeyValue(installButton.textFillProperty(), Color.GREEN))
                                );
                                successAnimation.setOnFinished(e -> openRackInfo());
                                successAnimation.play();
                            } else {
                                parent.pushNotification("ERROR", "Not enough slots available or server is incompatible");
                            }
                        } catch (Exception e) {
                            parent.pushNotification("ERROR", "An error occurred: " + e.getMessage());
                            System.err.println("Error installing VPS: " + e.getMessage());
                            e.printStackTrace();
                        }
                    });
                    
                    serverRow.getChildren().addAll(serverInfo, installButton);
                    serverList.getChildren().add(serverRow);
                }
                
                uninstalledScrollPane.setContent(serverList);
                uninstalledSection.getChildren().addAll(uninstalledLabel, uninstalledScrollPane);
                
                infoPane.getChildren().add(uninstalledSection);
            }


            Region bottomSpacer = new Region();
            VBox.setVgrow(bottomSpacer, Priority.ALWAYS);
            infoPane.getChildren().add(bottomSpacer);


            mainLayout.getChildren().addAll(rackContainerWithSpacer, infoPane);
            HBox.setHgrow(infoPane, Priority.ALWAYS);
            mainLayout.setAlignment(Pos.CENTER);
            contentBox.setCenter(mainLayout);
        } else {

            HBox mainLayout = new HBox(20);
            mainLayout.setAlignment(Pos.CENTER_LEFT);
            

            VBox rackCabinet = new VBox(0);
            rackCabinet.setPadding(new Insets(0));
            rackCabinet.setPrefWidth(500);
            rackCabinet.setMinHeight(500);
            rackCabinet.setMaxHeight(Double.MAX_VALUE);
            

            BorderPane rackFrame = new BorderPane();
            rackFrame.setPrefWidth(500);
            rackFrame.setMaxHeight(Double.MAX_VALUE);
            rackFrame.setStyle("""
                -fx-background-color: #191919;
                -fx-border-color: #00ffff;
                -fx-border-width: 2;
                -fx-border-style: solid;
                -fx-effect: dropshadow(gaussian, #00ffff, 5, 0, 0, 0);
                """);
            

            VBox unitMarkings = new VBox(0);
            unitMarkings.setPrefWidth(30);
            unitMarkings.setStyle("""
                -fx-background-color: #111111;
                -fx-border-color: #333333;
                -fx-border-width: 0 1 0 0;
                """);
            

            ScrollPane slotContainer = new ScrollPane();
            slotContainer.setFitToWidth(true);
            slotContainer.setFitToHeight(true);
            slotContainer.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            slotContainer.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            slotContainer.setStyle("""
                -fx-background-color: transparent;
                -fx-background: transparent;
                -fx-background-insets: 0;
                -fx-padding: 0;
                """);
            

            GridPane rackSlots = new GridPane();
            rackSlots.setVgap(2);
            rackSlots.setHgap(0);
            rackSlots.setPadding(new Insets(0));
            rackSlots.setStyle("""
                -fx-background-color: #141414;
                """);
            
            ColumnConstraints col1 = new ColumnConstraints();
            col1.setPrefWidth(450);
            col1.setHgrow(Priority.ALWAYS);
            
            ColumnConstraints col2 = new ColumnConstraints();
            col2.setPrefWidth(50);
            
            rackSlots.getColumnConstraints().addAll(col1, col2);


            int slotUnitHeight = 40;

            for (int i = 0; i < parent.getRack().getMaxSlotUnits(); i++) {
                RowConstraints row = new RowConstraints();
                row.setPrefHeight(slotUnitHeight);
                row.setMinHeight(slotUnitHeight);
                rackSlots.getRowConstraints().add(row);
                

                Label unitLabel = new Label(String.format("U%02d", i + 1));
                unitLabel.setStyle("""
                    -fx-font-family: 'Courier New';
                    -fx-font-size: 10px;
                    -fx-text-fill: #666666;
                    """);
                StackPane unitLabelBox = new StackPane(unitLabel);
                unitLabelBox.setPrefHeight(slotUnitHeight);
                unitLabelBox.setAlignment(Pos.CENTER);
                rackSlots.add(unitLabelBox, 1, i);
            }


            createEnhancedRackSlots(rackSlots, slotUnitHeight);
            
            slotContainer.setContent(rackSlots);
            

            GridPane topVents = new GridPane();
            topVents.setPrefHeight(15);
            topVents.setHgap(4);
            topVents.setVgap(2);
            topVents.setAlignment(Pos.CENTER);
            topVents.setStyle("-fx-background-color: #222222;");
            
            for (int i = 0; i < 20; i++) {
                Rectangle vent = new Rectangle(15, 3);
                vent.setFill(Color.web("#111111"));
                vent.setArcWidth(1);
                vent.setArcHeight(1);
                topVents.add(vent, i % 10, i / 10);
            }
            

            HBox powerSupply = new HBox(10);
            powerSupply.setPrefHeight(20);
            powerSupply.setStyle("-fx-background-color: #222222;");
            powerSupply.setAlignment(Pos.CENTER_RIGHT);
            

            Rectangle powerButton = new Rectangle(8, 8);
            powerButton.setFill(Color.web("#00ff00"));
            

            Timeline pulseTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, 
                    new KeyValue(powerButton.opacityProperty(), 1.0)),
                new KeyFrame(Duration.seconds(1.5), 
                    new KeyValue(powerButton.opacityProperty(), 0.5)),
                new KeyFrame(Duration.seconds(3), 
                    new KeyValue(powerButton.opacityProperty(), 1.0))
            );
            pulseTimeline.setCycleCount(Timeline.INDEFINITE);
            pulseTimeline.play();
            

            Rectangle powerMeter1 = new Rectangle(4, 6);
            powerMeter1.setFill(Color.web("#ff0000"));
            Rectangle powerMeter2 = new Rectangle(4, 6);
            powerMeter2.setFill(Color.web("#ffff00"));
            Rectangle powerMeter3 = new Rectangle(4, 6);
            powerMeter3.setFill(Color.web("#00ff00"));
            
            powerSupply.getChildren().addAll(powerMeter1, powerMeter2, powerMeter3, powerButton);
            powerSupply.setPadding(new Insets(0, 20, 0, 0));
            

            rackFrame.setCenter(slotContainer);
            rackCabinet.getChildren().addAll(topVents, rackFrame, powerSupply);
            

            VBox rackContainerWithSpacer = new VBox();
            rackContainerWithSpacer.setAlignment(Pos.BOTTOM_CENTER);
            rackContainerWithSpacer.setPrefWidth(500);
            

            Region spacer = new Region();
            VBox.setVgrow(spacer, Priority.ALWAYS);
            
            rackContainerWithSpacer.getChildren().addAll(spacer, rackCabinet);
            

            VBox infoPane = new VBox(15);
            infoPane.setPrefWidth(250);
            infoPane.setMinHeight(550);
            infoPane.setPrefHeight(600);
            infoPane.setMaxHeight(Double.MAX_VALUE);
            VBox.setVgrow(infoPane, Priority.ALWAYS);
            infoPane.setStyle("""
                -fx-background-color: #280042;
                -fx-padding: 15px;
                -fx-border-color: #8a2be2;
                -fx-border-width: 2px;
                -fx-border-style: solid;
                -fx-background-radius: 0px;
                -fx-effect: dropshadow(gaussian, #8a2be2, 5, 0, 0, 0);
                """);

            Label infoTitle = new Label("RACK INFO");
            infoTitle.setMaxWidth(Double.MAX_VALUE);
            infoTitle.setAlignment(Pos.CENTER);
            infoTitle.setStyle("""
                -fx-font-family: 'Courier New';
                -fx-font-size: 18px;
                -fx-font-weight: bold;
                -fx-text-fill: #ff00ff;
                -fx-effect: dropshadow(gaussian, #ff00ff, 3, 0, 0, 0);
                -fx-padding: 5px;
                -fx-background-color: #12071e;
                -fx-background-radius: 0px;
                -fx-border-color: #ff00ff;
                -fx-border-width: 1px;
                -fx-border-style: solid;
                """);

            int usedSlots = parent.getRack().getOccupiedSlotUnits();
            int availableSlots = parent.getRack().getMaxSlotUnits() - usedSlots;


            VBox statusPanel = new VBox(8);
            statusPanel.setMaxWidth(Double.MAX_VALUE);
            statusPanel.setStyle("""
                -fx-background-color: #1a0033;
                -fx-padding: 10px;
                -fx-background-radius: 0px;
                -fx-border-color: #00ffff;
                -fx-border-width: 1px;
                -fx-border-style: solid;
                """);


            GridPane statusGrid = new GridPane();
            statusGrid.setMaxWidth(Double.MAX_VALUE);
            statusGrid.setHgap(10);
            statusGrid.setVgap(8);
            

            Label serversLabel = new Label("SERVERS:");
            serversLabel.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 14px; -fx-text-fill: #00ffff; -fx-font-weight: bold;");
            Label serversValue = new Label(Integer.toString(parent.getVpsList().size()));
            serversValue.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 14px; -fx-text-fill: #00ffff;");
            

            Label slotsLabel = new Label("SLOTS:");
            slotsLabel.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 14px; -fx-text-fill: #00ffff; -fx-font-weight: bold;");
            Label slotsValue = new Label(usedSlots + "/" + parent.getRack().getMaxSlotUnits() + 
                              " (" + parent.getRack().getAvailableSlotUnits() + " AVAILABLE)");
            slotsValue.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 14px; -fx-text-fill: #00ffff;");
            

            Label networkLabel = new Label("NETWORK:");
            networkLabel.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 14px; -fx-text-fill: #00ffff; -fx-font-weight: bold;");
            

            int baseNetworkSpeed = 10;
            int networkSpeedBonus = ResourceManager.getInstance().getSkillPointsSystem().getRackNetworkSpeedBonus();
            int totalNetworkSpeed = baseNetworkSpeed + networkSpeedBonus;
            networkValuePopulated = new Label(totalNetworkSpeed + " Gbps");
            
            networkValuePopulated.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 14px; -fx-text-fill: #00ffff;");
            

            Label usersLabel = new Label("ACTIVE USERS:");
            usersLabel.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 14px; -fx-text-fill: #00ffff; -fx-font-weight: bold;");
            Label usersValue = new Label("10");
            usersValue.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 14px; -fx-text-fill: #00ffff;");
            

            statusGrid.add(serversLabel, 0, 0);
            statusGrid.add(serversValue, 1, 0);
            statusGrid.add(slotsLabel, 0, 1);
            statusGrid.add(slotsValue, 1, 1);
            statusGrid.add(networkLabel, 0, 2);
            statusGrid.add(networkValuePopulated, 1, 2);
            statusGrid.add(usersLabel, 0, 3);
            statusGrid.add(usersValue, 1, 3);
            

            ColumnConstraints column1 = new ColumnConstraints();
            column1.setHgrow(Priority.NEVER);
            ColumnConstraints column2 = new ColumnConstraints();
            column2.setHgrow(Priority.ALWAYS);
            statusGrid.getColumnConstraints().addAll(column1, column2);
            

            statusPanel.getChildren().add(statusGrid);

            Button inventoryButton = createPixelButton("SERVER INVENTORY", "#3498db");
            inventoryButton.setMaxWidth(Double.MAX_VALUE);
            inventoryButton.setOnAction(e -> parent.openVPSInventory());
            
            int upgradeCost = calculateUpgradeCost();
            Button upgradeButton = createPixelButton("UPGRADE RACK ($" + upgradeCost + ")", "#4CAF50");
            upgradeButton.setMaxWidth(Double.MAX_VALUE);
            upgradeButton.setOnAction(e -> {
                // ตรวจสอบว่า rack มี slot ว่างที่สามารถ upgrade ได้หรือไม่
                Rack currentRack = parent.getRack();
                int currentUnlockedSlots = currentRack.getUnlockedSlotUnits();
                int maxRackSlots = currentRack.getMaxSlotUnits();

                // ตรวจสอบว่าได้ upgrade ครบแล้วหรือไม่
                if (currentUnlockedSlots >= maxRackSlots) {
                    parent.pushNotification("CANNOT UPGRADE", "This rack already has maximum slots unlocked (" + maxRackSlots + " slots)");
                    return;
                }

                int currentUpgradeCost = calculateUpgradeCost();
                if (parent.getCompany().getMoney() >= currentUpgradeCost) {

                    parent.getCompany().setMoney(parent.getCompany().getMoney() - currentUpgradeCost);
                    
                    if (parent.getRack().upgrade()) {
                        Timeline pulseAnimation = new Timeline();
                        pulseAnimation.getKeyFrames().addAll(
                            new KeyFrame(Duration.ZERO, 
                                new KeyValue(upgradeButton.scaleXProperty(), 1.0),
                                new KeyValue(upgradeButton.scaleYProperty(), 1.0)
                            ),
                            new KeyFrame(Duration.millis(200), 
                                new KeyValue(upgradeButton.scaleXProperty(), 1.05),
                                new KeyValue(upgradeButton.scaleYProperty(), 1.05)
                            ),
                            new KeyFrame(Duration.millis(400), 
                                new KeyValue(upgradeButton.scaleXProperty(), 1.0),
                                new KeyValue(upgradeButton.scaleYProperty(), 1.0)
                            )
                        );
                        
                        pulseAnimation.setOnFinished(event -> {
                            parent.pushNotification("UPGRADE COMPLETE", "RACK CAPACITY INCREASED TO " + 
                                                  parent.getRack().getUnlockedSlotUnits() + " SLOTS");
                            openRackInfo();
                        });
                        
                        pulseAnimation.play();
                    }
                } else {
                    parent.pushNotification("UPGRADE FAILED", "INSUFFICIENT FUNDS");
                }
            });

            infoPane.getChildren().addAll(infoTitle, statusPanel, inventoryButton, upgradeButton);


            Button marketButton = createPixelButton("MARKET", "#6a00ff");
            marketButton.setMaxWidth(Double.MAX_VALUE);
            marketButton.setOnAction(e -> {
                parent.openMarket();
            });
            infoPane.getChildren().add(marketButton);


            List<VPSOptimization> uninstalledServers = parent.getVpsInventory().getAllVPS();
            if (!uninstalledServers.isEmpty()) {
                VBox uninstalledSection = new VBox(10);
                uninstalledSection.setPadding(new Insets(10, 0, 0, 0));
                VBox.setVgrow(uninstalledSection, Priority.ALWAYS);
                
                Label uninstalledLabel = new Label("AVAILABLE SERVERS");
                uninstalledLabel.setMaxWidth(Double.MAX_VALUE);
                uninstalledLabel.setAlignment(Pos.CENTER);
                uninstalledLabel.setStyle("""
                    -fx-font-family: 'Courier New';
                    -fx-font-size: 16px;
                    -fx-font-weight: bold;
                    -fx-text-fill: #ff00ff;
                    -fx-effect: dropshadow(gaussian, #ff00ff, 3, 0, 0, 0);
                    -fx-padding: 5px;
                    -fx-background-color: #12071e;
                    -fx-background-radius: 0px;
                    -fx-border-color: #ff00ff;
                    -fx-border-width: 1px;
                    -fx-border-style: solid;
                """);
                
                ScrollPane uninstalledScrollPane = new ScrollPane();
                uninstalledScrollPane.setStyle("""
                    -fx-background-color: transparent;
                    -fx-background: transparent;
                    -fx-background-insets: 0;
                    -fx-padding: 0;
                """);
                uninstalledScrollPane.setFitToWidth(true);
                uninstalledScrollPane.setPrefHeight(250);
                uninstalledScrollPane.setMinHeight(200);
                VBox.setVgrow(uninstalledScrollPane, Priority.ALWAYS);
                
                VBox serverList = new VBox(8);
                serverList.setPadding(new Insets(5));
                
                for (VPSOptimization server : uninstalledServers) {

                    String serverId = "";
                    for (String id : parent.getVpsInventory().getAllVPSIds()) {
                        if (parent.getVpsInventory().getVPS(id) == server) {
                            serverId = id;
                            break;
                        }
                    }
                    
                    final String finalServerId = serverId;
                    
                    HBox serverRow = new HBox(5);
                    serverRow.setAlignment(Pos.CENTER_LEFT);
                    

                    VBox serverInfo = new VBox(2);
                    serverInfo.setPrefWidth(150);
                    serverInfo.setStyle("""
                        -fx-background-color: #2d0052;
                        -fx-padding: 5px;
                        -fx-background-radius: 0px;
                        -fx-border-color: #00ffff;
                        -fx-border-width: 2px;
                        -fx-border-style: solid;
                    """);
                    
                    Label nameLabel = new Label("SERVER " + server.getVCPUs() + "vCPU");
                    nameLabel.setStyle("""
                        -fx-text-fill: #ff00ff; 
                        -fx-font-weight: bold;
                        -fx-font-family: 'Courier New';
                    """);
                    
                    Label specsLabel = new Label(server.getRamInGB() + "GB RAM | " + server.getSize().getDisplayName());
                    specsLabel.setStyle("""
                        -fx-text-fill: #00ffff;
                        -fx-font-family: 'Courier New';
                    """);
                    
                    serverInfo.getChildren().addAll(nameLabel, specsLabel);
                    

                    Button installButton = createPixelButton("INSTALL", "#4CAF50");
                    

                    installButton.setOnMousePressed(e -> {
                        installButton.setStyle(
                            "-fx-background-color: #388E3C;" +
                            "-fx-font-family: 'Courier New';" +
                            "-fx-font-size: 14px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-text-fill: white;" +
                            "-fx-padding: 6px 10px;" +
                            "-fx-background-radius: 0px;" +
                            "-fx-border-color: white;" +
                            "-fx-border-width: 2px;" +
                            "-fx-translate-y: 2px;"
                        );
                    });
                    
                    installButton.setOnMouseReleased(e -> {
                        installButton.setStyle(
                            "-fx-background-color: #4CAF50;" +
                            "-fx-font-family: 'Courier New';" +
                            "-fx-font-size: 14px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-text-fill: white;" +
                            "-fx-padding: 8px 12px;" +
                            "-fx-background-radius: 0px;" +
                            "-fx-border-color: white;" +
                            "-fx-border-width: 2px;" +
                            "-fx-translate-y: 0px;"
                        );
                    });
                    

                    installButton.setOnAction(event -> {
                        try {
                            if (parent.installVPSFromInventory(finalServerId)) {
                                parent.pushNotification("SUCCESS", "Server installed successfully");

                                installButton.setDisable(true);
                                Timeline successAnimation = new Timeline(
                                    new KeyFrame(Duration.ZERO, 
                                        new KeyValue(installButton.textFillProperty(), Color.WHITE)),
                                    new KeyFrame(Duration.millis(200), 
                                        new KeyValue(installButton.textFillProperty(), Color.GREEN))
                                );
                                successAnimation.setOnFinished(e -> openRackInfo());
                                successAnimation.play();
                            } else {
                                parent.pushNotification("ERROR", "Not enough slots available or server is incompatible");
                            }
                        } catch (Exception e) {
                            parent.pushNotification("ERROR", "An error occurred: " + e.getMessage());
                            System.err.println("Error installing VPS: " + e.getMessage());
                            e.printStackTrace();
                        }
                    });
                    
                    serverRow.getChildren().addAll(serverInfo, installButton);
                    serverList.getChildren().add(serverRow);
                }
            }


            Region bottomSpacer = new Region();
            VBox.setVgrow(bottomSpacer, Priority.ALWAYS);
            infoPane.getChildren().add(bottomSpacer);


            mainLayout.getChildren().addAll(rackContainerWithSpacer, infoPane);
            HBox.setHgrow(infoPane, Priority.ALWAYS);
            mainLayout.setAlignment(Pos.CENTER);
            contentBox.setCenter(mainLayout);
        }

        rackPane.setTop(topBar);
        rackPane.setCenter(contentBox);
        BorderPane.setMargin(contentBox, new Insets(10, 0, 10, 0));

        parent.getGameArea().getChildren().clear();
        parent.getGameArea().getChildren().add(rackPane);


        rackPane.getStylesheets().add(getClass().getResource("/css/rackinfo-pane.css").toExternalForm());
    }

    private Label createStatusLabel(String text) {
        Label label = new Label(text);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setStyle("""
            -fx-font-family: 'Courier New';
            -fx-font-size: 14px;
            -fx-text-fill: #00ffff;
            -fx-padding: 5px;
            -fx-background-color: #1a0033;
            -fx-background-radius: 0px;
            -fx-border-color: #00ffff;
            -fx-border-width: 1px;
            -fx-border-style: dotted;
            """);
        return label;
    }
    
    private Button createPixelButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-font-family: 'Courier New';" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;" +
            "-fx-padding: 8px 12px;" +
            "-fx-background-radius: 0px;" +
            "-fx-border-color: white;" +
            "-fx-border-width: 2px;" +
            "-fx-border-style: solid;" +
            "-fx-effect: dropshadow(gaussian, " + color + ", 5, 0, 0, 0);"
        );
        

        button.setOnMouseEntered(e -> {
            button.setStyle(
                "-fx-background-color: " + color + ";" +
                "-fx-font-family: 'Courier New';" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #00ffff;" +
                "-fx-padding: 8px 12px;" +
                "-fx-background-radius: 0px;" +
                "-fx-border-color: #00ffff;" +
                "-fx-border-width: 2px;" +
                "-fx-border-style: solid;" +
                "-fx-effect: dropshadow(gaussian, " + color + ", 5, 0, 0, 0);"
             );
         });
         
        button.setOnMouseExited(e -> {
            button.setStyle(
                "-fx-background-color: " + color + ";" +
                "-fx-font-family: 'Courier New';" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: white;" +
                "-fx-padding: 8px 12px;" +
                "-fx-background-radius: 0px;" +
                "-fx-border-color: white;" +
                "-fx-border-width: 2px;" +
                "-fx-border-style: solid;" +
                "-fx-effect: dropshadow(gaussian, " + color + ", 5, 0, 0, 0);"
             );
         });
         
        return button;
    }


    private void createEnhancedRackSlots(GridPane rackSlots, int slotHeight) {
        slotPanes.clear();
        int currentSlot = 0;


        for (VPSOptimization vps : parent.getVpsList()) {
            int slotsRequired = vps.getSlotsRequired();
            Pane slot = createEnhancedRackSlot(currentSlot, vps, true, slotHeight * slotsRequired);
            rackSlots.add(slot, 0, currentSlot, 1, slotsRequired);
            slotPanes.add(slot);
            currentSlot += slotsRequired;
        }


        for (int i = currentSlot; i < parent.getRack().getMaxSlotUnits(); i++) {
            Pane slot = createEnhancedRackSlot(i, null, i < parent.getRack().getUnlockedSlotUnits(), slotHeight);
            rackSlots.add(slot, 0, i);
            slotPanes.add(slot);
        }
    }


    private Pane createEnhancedRackSlot(int index, VPSOptimization vps, boolean isSlotAvailable, int slotHeight) {
        Pane slot = new Pane();
        slot.setPrefSize(450, slotHeight);
        

        Rectangle serverBg = new Rectangle(450, slotHeight);
        serverBg.setFill(Color.web("#0d0d0d"));
        

        Rectangle leftRail = new Rectangle(10, slotHeight);
        leftRail.setFill(Color.web("#181818"));
        leftRail.setStroke(Color.web("#333333"));
        leftRail.setStrokeWidth(1);
        
        Rectangle rightRail = new Rectangle(10, slotHeight);
        rightRail.setFill(Color.web("#181818"));
        rightRail.setStroke(Color.web("#333333"));
        rightRail.setStrokeWidth(1);
        rightRail.setLayoutX(440);
        

        Rectangle leftTopScrew = new Rectangle(6, 6);
        leftTopScrew.setFill(Color.web("#666666"));
        leftTopScrew.setLayoutX(2);
        leftTopScrew.setLayoutY(6);
        
        Rectangle leftBottomScrew = new Rectangle(6, 6);
        leftBottomScrew.setFill(Color.web("#666666"));
        leftBottomScrew.setLayoutX(2);
        leftBottomScrew.setLayoutY(slotHeight - 12);
        
        Rectangle rightTopScrew = new Rectangle(6, 6);
        rightTopScrew.setFill(Color.web("#666666"));
        rightTopScrew.setLayoutX(442);
        rightTopScrew.setLayoutY(6);
        
        Rectangle rightBottomScrew = new Rectangle(6, 6);
        rightBottomScrew.setFill(Color.web("#666666"));
        rightBottomScrew.setLayoutX(442);
        rightBottomScrew.setLayoutY(slotHeight - 12);


        slot.getChildren().addAll(serverBg, leftRail, rightRail, 
                                leftTopScrew, leftBottomScrew, rightTopScrew, rightBottomScrew);
        
        if (vps != null) {

            Rectangle serverFace = new Rectangle(420, slotHeight - 10);
            serverFace.setLayoutX(15);
            serverFace.setLayoutY(5);
            serverFace.setFill(Color.web("#4b0082"));
            serverFace.setStroke(Color.web("#8a2be2"));
            serverFace.setStrokeWidth(2);
            

            Rectangle leftHandle1 = new Rectangle(6, 8);
            leftHandle1.setFill(Color.web("#333333"));
            leftHandle1.setLayoutX(25);
            leftHandle1.setLayoutY(8);
            
            Rectangle leftHandle2 = new Rectangle(6, 8);
            leftHandle2.setFill(Color.web("#333333"));
            leftHandle2.setLayoutX(25);
            leftHandle2.setLayoutY(slotHeight - 16);
            

            Rectangle rightHandle1 = new Rectangle(6, 8);
            rightHandle1.setFill(Color.web("#333333"));
            rightHandle1.setLayoutX(420);
            rightHandle1.setLayoutY(8);
            
            Rectangle rightHandle2 = new Rectangle(6, 8);
            rightHandle2.setFill(Color.web("#333333"));
            rightHandle2.setLayoutX(420);
            rightHandle2.setLayoutY(slotHeight - 16);
            

            Rectangle ledPanel = new Rectangle(80, slotHeight - 20);
            ledPanel.setFill(Color.web("#2a0052"));
            ledPanel.setStroke(Color.web("#ff00ff"));
            ledPanel.setStrokeWidth(1);
            ledPanel.setLayoutX(30);
            ledPanel.setLayoutY(10);
            

            int ledY = Math.min(20, slotHeight / 4);
            Rectangle powerLed = new Rectangle(6, 6);
            powerLed.setFill(Color.web("#00ff00"));
            powerLed.setLayoutX(40);
            powerLed.setLayoutY(ledY);
            powerLed.setEffect(new Glow(0.5));
            
            Rectangle statusLed = new Rectangle(6, 6);
            statusLed.setFill(Color.web("#00ffff"));
            statusLed.setLayoutX(60);
            statusLed.setLayoutY(ledY);
            statusLed.setEffect(new Glow(0.3));
            
            Rectangle errorLed = new Rectangle(6, 6);
            errorLed.setFill(Color.web("#ff0000"));
            errorLed.setLayoutX(80);
            errorLed.setLayoutY(ledY);
            errorLed.setOpacity(0.3);
            
            Rectangle networkLed = new Rectangle(6, 6);
            networkLed.setFill(Color.web("#ffcc00"));
            networkLed.setLayoutX(100);
            networkLed.setLayoutY(ledY);
            

            Timeline blinkTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, 
                    new KeyValue(networkLed.opacityProperty(), 1.0)),
                new KeyFrame(Duration.seconds(0.1), 
                    new KeyValue(networkLed.opacityProperty(), 0.3)),
                new KeyFrame(Duration.seconds(0.3), 
                    new KeyValue(networkLed.opacityProperty(), 1.0)),
                new KeyFrame(Duration.seconds(0.5), 
                    new KeyValue(networkLed.opacityProperty(), 1.0)),
                new KeyFrame(Duration.seconds(0.6), 
                    new KeyValue(networkLed.opacityProperty(), 0.3)),
                new KeyFrame(Duration.seconds(0.8), 
                    new KeyValue(networkLed.opacityProperty(), 1.0))
            );
            blinkTimeline.setCycleCount(Timeline.INDEFINITE);
            blinkTimeline.play();
            

            Rectangle specsPanel = new Rectangle(120, slotHeight - 20);
            specsPanel.setFill(Color.web("#1f004d"));
            specsPanel.setStroke(Color.web("#8a2be2"));
            specsPanel.setStrokeWidth(1);
            specsPanel.setLayoutX(120);
            specsPanel.setLayoutY(10);
            

            double fontSize = Math.min(16, slotHeight / 3);
            double smallFontSize = Math.min(12, slotHeight / 4);
            double textY = Math.max(5, (slotHeight - fontSize * 3) / 4);
            

            Label serverTypeLabel = new Label(vps.getSize().getDisplayName());
            serverTypeLabel.setStyle("""
                -fx-font-family: 'Courier New';
                -fx-font-size: %fpx;
                -fx-font-weight: bold;
                -fx-text-fill: #ff00ff;
                -fx-effect: dropshadow(gaussian, #ff00ff, 2, 0, 0, 0);
                """.formatted(fontSize));
            serverTypeLabel.setLayoutX(130);
            serverTypeLabel.setLayoutY(textY);
            

            if (slotHeight >= 60) {

                Label cpuLabel = new Label("CPU: " + vps.getVCPUs() + " vCPUs");
                cpuLabel.setStyle("""
                    -fx-font-family: 'Courier New';
                    -fx-font-size: %fpx;
                    -fx-text-fill: #00ffff;
                    """.formatted(smallFontSize));
                cpuLabel.setLayoutX(130);
                cpuLabel.setLayoutY(textY + fontSize + 2);
                
                Label ramLabel = new Label("RAM: " + vps.getRamInGB() + " GB");
                ramLabel.setStyle("""
                    -fx-font-family: 'Courier New';
                    -fx-font-size: %fpx;
                    -fx-text-fill: #00ffff;
                    """.formatted(smallFontSize));
                ramLabel.setLayoutX(130);
                ramLabel.setLayoutY(textY + fontSize + smallFontSize + 4);
                
                slot.getChildren().addAll(cpuLabel, ramLabel);
            } else {

                serverTypeLabel.setLayoutY(slotHeight / 2.0 - fontSize / 2.0);
            }
            

            Label addressLabel = new Label("ID: 0x" + Integer.toHexString(index).toUpperCase());
            addressLabel.setStyle("""
                -fx-font-family: 'Courier New';
                -fx-font-size: 9px;
                -fx-text-fill: #666666;
                """);
            addressLabel.setLayoutX(130);

            addressLabel.setLayoutY(slotHeight - 15);
            

            Rectangle portSection = new Rectangle(140, slotHeight - 20);
            portSection.setFill(Color.web("#1f004d"));
            portSection.setStroke(Color.web("#8a2be2"));
            portSection.setStrokeWidth(1);
            portSection.setLayoutX(250);
            portSection.setLayoutY(10);
            

            for (int i = 0; i < Math.min(2, slotHeight / 20); i++) {
                Rectangle port = new Rectangle(20, 10);
                port.setFill(Color.web("#222222"));
                port.setStroke(Color.web("#555555"));
                port.setStrokeWidth(1);
                port.setLayoutX(360);
                port.setLayoutY(15 + (i * 20));
                

                Rectangle portLight = new Rectangle(4, 4);
                portLight.setFill(Color.web("#00ff00"));
                portLight.setLayoutX(364);
                portLight.setLayoutY(18 + (i * 20));
                
                slot.getChildren().addAll(port, portLight);
            }
            

            if (slotHeight >= 60) {

                for (int row = 0; row < Math.min(5, slotHeight / 10); row++) {
                    for (int col = 0; col < 6; col++) {
                        Rectangle vent = new Rectangle(4, 2);
                        vent.setFill(Color.web("#12071e"));
                        vent.setLayoutX(270 + (col * 10));
                        vent.setLayoutY(15 + (row * 8));
                        slot.getChildren().add(vent);
                    }
                }
                

                int maxGauges = Math.min(2, slotHeight / 30);
                for (int i = 0; i < maxGauges; i++) {
                    Rectangle gaugeBack = new Rectangle(60, 6);
                    gaugeBack.setFill(Color.web("#222222"));
                    gaugeBack.setLayoutX(280);
                    gaugeBack.setLayoutY(50 + (i * 12));
                    
                    double usageWidth = 10 + (Math.random() * 40);
                    Rectangle gaugeLevel = new Rectangle(usageWidth, 6);
                    gaugeLevel.setFill(Color.web("#00ffff"));
                    gaugeLevel.setLayoutX(280);
                    gaugeLevel.setLayoutY(50 + (i * 12));
                    

                    Timeline usageAnimation = new Timeline(
                        new KeyFrame(Duration.ZERO, 
                            new KeyValue(gaugeLevel.widthProperty(), usageWidth)),
                        new KeyFrame(Duration.seconds(1 + Math.random()), 
                            new KeyValue(gaugeLevel.widthProperty(), 10 + (Math.random() * 40))),
                        new KeyFrame(Duration.seconds(2 + Math.random()), 
                            new KeyValue(gaugeLevel.widthProperty(), 10 + (Math.random() * 40)))
                    );
                    usageAnimation.setCycleCount(Timeline.INDEFINITE);
                    usageAnimation.play();
                    

                    if (slotHeight > 80) {
                        Label gaugeLabel = new Label("CPU" + (i+1));
                        gaugeLabel.setStyle("""
                            -fx-font-family: 'Courier New';
                            -fx-font-size: 9px;
                            -fx-text-fill: #666666;
                            """);
                        gaugeLabel.setLayoutX(255);
                        gaugeLabel.setLayoutY(48 + (i * 12));
                        slot.getChildren().add(gaugeLabel);
                    }
                    
                    slot.getChildren().addAll(gaugeBack, gaugeLevel);
                }
            }
            

            slot.getChildren().addAll(
                serverFace, 
                leftHandle1, leftHandle2, rightHandle1, rightHandle2,
                ledPanel, powerLed, statusLed, errorLed, networkLed,
                specsPanel, portSection,
                serverTypeLabel, addressLabel
            );
            

            slot.setOnMouseEntered(e -> {
                serverFace.setFill(Color.web("#6a00ff"));
                powerLed.setEffect(new Glow(0.8));
                statusLed.setEffect(new Glow(0.8));
                
                DropShadow glow = new DropShadow();
                glow.setColor(Color.web("#ff00ff"));
                glow.setWidth(15);
                glow.setHeight(15);
                serverFace.setEffect(glow);
            });
            
            slot.setOnMouseExited(e -> {
                serverFace.setFill(Color.web("#4b0082"));
                powerLed.setEffect(new Glow(0.5));
                statusLed.setEffect(new Glow(0.3));
                serverFace.setEffect(null);
            });
            
            slot.setOnMouseClicked(e -> parent.openVPSInfoPage(vps));
            
        } else if (isSlotAvailable) {

            Rectangle emptyFace = new Rectangle(420, slotHeight - 10);
            emptyFace.setLayoutX(15);
            emptyFace.setLayoutY(5);
            emptyFace.setFill(Color.web("#1a1a1a"));
            emptyFace.setStroke(Color.web("#333333"));
            emptyFace.setStrokeWidth(1);
            

            Rectangle leftRailInner = new Rectangle(3, slotHeight - 20);
            leftRailInner.setFill(Color.web("#333333"));
            leftRailInner.setLayoutX(20);
            leftRailInner.setLayoutY(10);
            
            Rectangle rightRailInner = new Rectangle(3, slotHeight - 20);
            rightRailInner.setFill(Color.web("#333333"));
            rightRailInner.setLayoutX(427);
            rightRailInner.setLayoutY(10);
            

            Circle mountHole1 = new Circle(3);
            mountHole1.setFill(Color.web("#111111"));
            mountHole1.setCenterX(35);
            mountHole1.setCenterY(15);
            
            Circle mountHole2 = new Circle(3);
            mountHole2.setFill(Color.web("#111111"));
            mountHole2.setCenterX(35);
            mountHole2.setCenterY(slotHeight - 15);
            
            Circle mountHole3 = new Circle(3);
            mountHole3.setFill(Color.web("#111111"));
            mountHole3.setCenterX(415);
            mountHole3.setCenterY(15);
            
            Circle mountHole4 = new Circle(3);
            mountHole4.setFill(Color.web("#111111"));
            mountHole4.setCenterX(415);
            mountHole4.setCenterY(slotHeight - 15);
            

            Label emptyLabel = new Label("[ AVAILABLE ]");
            emptyLabel.setStyle("""
                -fx-font-family: 'Courier New';
                -fx-font-size: 14px;
                -fx-font-weight: bold;
                -fx-text-fill: #666666;
                """);
            emptyLabel.setLayoutX(150);
            emptyLabel.setLayoutY(slotHeight/2 - 10);
            

            Label unitSizeLabel = new Label("1U");
            unitSizeLabel.setStyle("""
                -fx-font-family: 'Courier New';
                -fx-font-size: 10px;
                -fx-text-fill: #444444;
                """);
            unitSizeLabel.setLayoutX(30);
            unitSizeLabel.setLayoutY(slotHeight/2 - 5);
            

            slot.getChildren().addAll(
                emptyFace, leftRailInner, rightRailInner,
                mountHole1, mountHole2, mountHole3, mountHole4,
                emptyLabel, unitSizeLabel
            );
            

            slot.setOnMouseEntered(e -> {
                emptyFace.setStroke(Color.web("#8a2be2"));
                emptyFace.setStrokeWidth(2);
                emptyLabel.setStyle("""
                    -fx-font-family: 'Courier New';
                    -fx-font-size: 14px;
                    -fx-font-weight: bold;
                    -fx-text-fill: #00ffff;
                    -fx-effect: dropshadow(gaussian, #00ffff, 3, 0, 0, 0);
                    """);
            });
            
            slot.setOnMouseExited(e -> {
                emptyFace.setStroke(Color.web("#333333"));
                emptyFace.setStrokeWidth(1);
                emptyLabel.setStyle("""
                    -fx-font-family: 'Courier New';
                    -fx-font-size: 14px;
                    -fx-font-weight: bold;
                    -fx-text-fill: #666666;
                    """);
            });
            
            slot.setOnMouseClicked(e -> parent.openVPSInventory());
            
        } else {

            Rectangle lockedFace = new Rectangle(420, slotHeight - 10);
            lockedFace.setLayoutX(15);
            lockedFace.setLayoutY(5);
            lockedFace.setFill(Color.web("#111111"));
            lockedFace.setStroke(Color.web("#222222"));
            lockedFace.setStrokeWidth(1);
            

            Rectangle lockBase = new Rectangle(16, 16);
            lockBase.setFill(Color.web("#ff00ff", 0.3));
            lockBase.setLayoutX(220 - 8);
            lockBase.setLayoutY(slotHeight/2);
            
            Rectangle lockTop = new Rectangle(10, 10);
            lockTop.setFill(Color.web("#ff00ff", 0.3));
            lockTop.setLayoutX(220 - 5);
            lockTop.setLayoutY(slotHeight/2 - 10);
            

            Label lockedLabel = new Label("LOCKED");
            lockedLabel.setStyle("""
                -fx-font-family: 'Courier New';
                -fx-font-size: 14px;
                -fx-font-weight: bold;
                -fx-text-fill: #333333;
                """);
            lockedLabel.setLayoutX(200);
            lockedLabel.setLayoutY(slotHeight/2 + 20);
            
            slot.getChildren().addAll(lockedFace, lockBase, lockTop, lockedLabel);
            slot.setOnMouseClicked(e -> parent.pushNotification("Locked Slot", "Upgrade rack to unlock this slot."));
        }
        
        return slot;
    }

    public int getMAX_SLOTS() {
        return parent.getRack().getMaxSlotUnits();
    }


    @Override
    public void onRackUIUpdate() {

        updateNetworkLabels();
    }


    private void updateNetworkLabels() {

        int baseNetworkSpeed = 10;
        int networkSpeedBonus = ResourceManager.getInstance().getSkillPointsSystem().getRackNetworkSpeedBonus();
        int totalNetworkSpeed = baseNetworkSpeed + networkSpeedBonus;
        

        if (networkValueEmpty != null) {
            networkValueEmpty.setText(totalNetworkSpeed + " Gbps");
        }
        
        if (networkValuePopulated != null) {
            networkValuePopulated.setText(totalNetworkSpeed + " Gbps");
        }
    }


    public void dispose() {

        System.out.println("ยกเลิกการลงทะเบียน RackUIUpdateListener");
        

        ResourceManager.getInstance().removeRackUIUpdateListener(this);
        

        if (originalRootNodes != null && !originalRootNodes.isEmpty()) {

            parent.getGameArea().getChildren().removeIf(node -> node instanceof BorderPane);
            

            StackPane rootStack = parent.getRootStack();
            

            Node gameArea = rootStack.getChildren().isEmpty() ? null : rootStack.getChildren().get(0);
            

            rootStack.getChildren().clear();
            if (gameArea != null) {
                rootStack.getChildren().add(gameArea);
            }
            

            for (Node node : originalRootNodes) {
                if (node != null && node != gameArea && !rootStack.getChildren().contains(node)) {
                    rootStack.getChildren().add(node);
                }
            }
            
            System.out.println("คืนค่า nodes ใน rootStack เรียบร้อย (" + rootStack.getChildren().size() + " nodes)");
        }
        

        if (parent.getMenuBar() != null) {
            parent.getMenuBar().setVisible(true);
        }
        
        if (parent.getInGameMarketMenuBar() != null) {
            parent.getInGameMarketMenuBar().setVisible(true);
        }
        
        if (parent.getMoneyUI() != null) {
            parent.getMoneyUI().setVisible(true);
        }
        
        if (parent.getDateView() != null) {
            parent.getDateView().setVisible(true);
        }
        
        System.out.println("ล้างการลงทะเบียน RackManagementUI และคืนค่า UI เรียบร้อย");
    }
}

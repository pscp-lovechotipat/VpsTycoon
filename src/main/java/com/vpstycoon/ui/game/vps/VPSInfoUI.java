package com.vpstycoon.ui.game.vps;

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
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class VPSInfoUI {
    private final GameplayContentPane parent;
    
    public VPSInfoUI(GameplayContentPane parent) {
        this.parent = parent;
    }

    public void openVPSInfoPage(VPSOptimization vps) {
        
        int usedVCPUs = vps.getVms().stream().mapToInt(VPSOptimization.VM::getVcpu).sum();
        int usedRamGB = vps.getVms().stream()
                .mapToInt(vm -> Integer.parseInt(vm.getRam().replaceAll("[^0-9]", ""))).sum();
        int usedDiskGB = vps.getVms().stream()
                .mapToInt(vm -> Integer.parseInt(vm.getDisk().replaceAll("[^0-9]", ""))).sum();

        
        int availableVCPUs = vps.getVCPUs() - usedVCPUs;
        int availableRamGB = vps.getRamInGB() - usedRamGB;
        int availableDiskGB = vps.getDiskInGB() - usedDiskGB;

        
        BorderPane vpsInfoPane = new BorderPane();
        vpsInfoPane.setPrefSize(800, 600);
        vpsInfoPane.setStyle("-fx-background-color: linear-gradient(to bottom, #1a0033, #000022); -fx-padding: 20px;");

        
        parent.getMenuBar().setVisible(false);
        parent.getInGameMarketMenuBar().setVisible(false);

        
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10));
        topBar.setStyle("""
                -fx-background-color: #2a0a3a;
                -fx-padding: 10px;
                -fx-background-radius: 5px;
                -fx-border-color: #8a2be2;
                -fx-border-width: 2px;
                -fx-effect: dropshadow(gaussian, rgba(110,0,220,0.4), 10, 0, 0, 5);
                """);

        
        String vpsId = vps.getVpsId();

        Label titleLabel = new Label("SERVER_" + vpsId + ".sys");
        titleLabel.setStyle("""
                -fx-text-fill: #e0b0ff;
                -fx-font-size: 24px;
                -fx-font-weight: bold;
                -fx-effect: dropshadow(gaussian, #9370db, 2, 0.3, 0, 0);
                """);

        Button backButton = UIUtils.createModernButton("Back", "#F44336");
        backButton.setOnAction(e -> parent.openRackInfo());
        topBar.getChildren().addAll(backButton, titleLabel);

        
        BorderPane serverContainer = new BorderPane();
        serverContainer.setPadding(new Insets(0, 0, 15, 0));
        
        
        HBox physicalServer = createPhysicalServerView(vps, usedVCPUs, usedRamGB, usedDiskGB);
        serverContainer.setCenter(physicalServer);
        
        
        VBox vmSection = new VBox(15);
        vmSection.setPadding(new Insets(15));
        vmSection.setStyle("""
                -fx-background-color: #1a0033;
                -fx-padding: 15px;
                -fx-background-radius: 5px;
                -fx-border-color: #8a2be2;
                -fx-border-width: 1px;
                -fx-effect: dropshadow(gaussian, rgba(110,0,220,0.3), 10, 0, 0, 5);
                """);
        
        
        HBox vmTitleBar = new HBox();
        vmTitleBar.setAlignment(Pos.CENTER_LEFT);
        vmTitleBar.setStyle("-fx-background-color: #2a0a3a; -fx-padding: 10px; -fx-background-radius: 5px;");
        
        Rectangle ledIndicator = new Rectangle(8, 8);
        ledIndicator.setFill(Color.web("#00ff00"));
        ledIndicator.setEffect(new Glow(0.5));
        
        Label vmSectionTitle = new Label(" VIRTUAL MACHINES");
        vmSectionTitle.setStyle("""
                -fx-text-fill: #00ffff;
                -fx-font-family: 'Courier New';
                -fx-font-size: 16px;
                -fx-font-weight: bold;
                -fx-effect: dropshadow(gaussian, #00ffff, 2, 0.3, 0, 0);
                """);
        
        
        Timeline blinkTimeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(ledIndicator.opacityProperty(), 1.0)),
            new KeyFrame(Duration.seconds(0.5), new KeyValue(ledIndicator.opacityProperty(), 0.3)),
            new KeyFrame(Duration.seconds(1.0), new KeyValue(ledIndicator.opacityProperty(), 1.0))
        );
        blinkTimeline.setCycleCount(Timeline.INDEFINITE);
        blinkTimeline.play();
        
        vmTitleBar.getChildren().addAll(ledIndicator, vmSectionTitle);
        
        
        ScrollPane vmScrollPane = new ScrollPane();
        vmScrollPane.setFitToWidth(true);
        vmScrollPane.setStyle("""
                -fx-background: transparent;
                -fx-background-color: transparent;
                -fx-padding: 10px 0px;
                """);
        
        VBox vmList = new VBox(10);
        vmList.setPadding(new Insets(10, 0, 10, 0));
        
        
        if (vps.getVms().isEmpty()) {
            HBox emptyBox = new HBox();
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(20));
            emptyBox.setStyle("""
                    -fx-background-color: #0d0d0d;
                    -fx-border-color: #666666;
                    -fx-border-width: 1px;
                    -fx-border-style: dashed;
                    """);
                    
            Label emptyLabel = new Label("NO VIRTUAL MACHINES DEPLOYED");
            emptyLabel.setStyle("""
                    -fx-font-family: 'Courier New';
                    -fx-font-size: 14px;
                    -fx-text-fill: #888888;
                    -fx-font-style: italic;
                    """);
            emptyBox.getChildren().add(emptyLabel);
            vmList.getChildren().add(emptyBox);
        } else {
            for (VPSOptimization.VM vm : vps.getVms()) {
                HBox vmEntry = createVMEntry(vm, vps);
                vmList.getChildren().add(vmEntry);
            }
        }
        
        vmScrollPane.setContent(vmList);
        vmSection.getChildren().addAll(vmTitleBar, vmScrollPane);
        VBox.setVgrow(vmScrollPane, Priority.ALWAYS);
        
        
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10));
        buttonBox.setStyle("""
                -fx-background-color: #0a0a0a;
                -fx-padding: 10px;
                -fx-background-radius: 5px;
                -fx-border-color: #666666;
                -fx-border-width: 1px;
                """);
        
        Button uninstallButton = UIUtils.createModernButton("Uninstall Server", "#F44336");
        uninstallButton.setOnAction(e -> uninstallVPS(vps));
        
        Button createVMButton = UIUtils.createModernButton("Create VM", "#4CAF50");
        createVMButton.setOnAction(e -> parent.openCreateVMPage(vps));
        
        buttonBox.getChildren().addAll(uninstallButton, createVMButton);
        
        
        VBox centerContent = new VBox(20);
        centerContent.getChildren().addAll(serverContainer, vmSection);
        VBox.setVgrow(vmSection, Priority.ALWAYS);
        
        vpsInfoPane.setTop(topBar);
        vpsInfoPane.setCenter(centerContent);
        vpsInfoPane.setBottom(buttonBox);

        
        parent.getGameArea().getChildren().clear();
        parent.getGameArea().getChildren().add(vpsInfoPane);
    }
    
    
    private HBox createPhysicalServerView(VPSOptimization vps, int usedVCPUs, int usedRamGB, int usedDiskGB) {
        HBox serverBox = new HBox();
        serverBox.setAlignment(Pos.CENTER);
        serverBox.setPrefHeight(220);
        serverBox.setStyle("""
                -fx-background-color: #0a0a0a;
                -fx-background-radius: 5px;
                -fx-border-color: #666666;
                -fx-border-width: 1px;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 10, 0, 0, 5);
                """);
                
        
        VBox serverChassis = new VBox(5);
        serverChassis.setPrefWidth(750);
        serverChassis.setPrefHeight(200);
        serverChassis.setAlignment(Pos.CENTER);
        serverChassis.setPadding(new Insets(10));
        serverChassis.setStyle("""
                -fx-background-color: linear-gradient(to bottom, #181818, #0a0a0a);
                -fx-background-radius: 3px;
                -fx-border-color: #333333;
                -fx-border-width: 1px;
                -fx-border-radius: 3px;
                """);
                
        
        HBox frontPanel = new HBox(10);
        frontPanel.setAlignment(Pos.CENTER_LEFT);
        frontPanel.setPrefHeight(40);
        frontPanel.setStyle("""
                -fx-background-color: #1a1a1a;
                -fx-background-radius: 2px;
                -fx-border-color: #333333;
                -fx-border-width: 1px;
                """);
        
        
        Label brandLabel = new Label("CYBER CORE " + vps.getSize().getDisplayName());
        brandLabel.setStyle("""
                -fx-font-family: 'Courier New';
                -fx-font-size: 14px;
                -fx-font-weight: bold;
                -fx-text-fill: #cccccc;
                """);
        brandLabel.setPadding(new Insets(0, 0, 0, 10));
        
        
        Circle powerButton = new Circle(8);
        powerButton.setFill(Color.web("#333333"));
        powerButton.setStroke(Color.web("#666666"));
        powerButton.setStrokeWidth(1);
        
        Circle powerLed = new Circle(4);
        powerLed.setFill(Color.web("#00ff00"));
        powerLed.setEffect(new Glow(0.8));
        
        
        HBox statusLEDs = new HBox(8);
        statusLEDs.setAlignment(Pos.CENTER);
        
        Circle hddLed = createStatusLED("#ff9900", true);  
        Circle errorLed = createStatusLED("#ff0000", false); 
        Circle networkLed = createStatusLED("#00ffff", true); 
        
        statusLEDs.getChildren().addAll(hddLed, errorLed, networkLed);
        statusLEDs.setPadding(new Insets(0, 0, 0, 20));
        
        
        HBox usbPorts = new HBox(5);
        for (int i = 0; i < 2; i++) {
            Rectangle usbPort = new Rectangle(12, 6);
            usbPort.setFill(Color.web("#0a0a0a"));
            usbPort.setStroke(Color.web("#333333"));
            usbPort.setStrokeWidth(1);
            usbPort.setArcWidth(2);
            usbPort.setArcHeight(2);
            usbPort.setEffect(new DropShadow(2, Color.BLACK));
            usbPorts.getChildren().add(usbPort);
        }
        
        usbPorts.setPadding(new Insets(0, 0, 0, 20));
        
        frontPanel.getChildren().addAll(brandLabel, powerButton, powerLed, statusLEDs, usbPorts);
        
        
        HBox serverContent = new HBox(20);
        serverContent.setPadding(new Insets(15, 10, 10, 10));
        serverContent.setPrefHeight(140);
        serverContent.setStyle("-fx-background-color: #0d0d0d;");
        
        
        VBox hardwareInfo = new VBox(10);
        hardwareInfo.setPrefWidth(350);
        hardwareInfo.setStyle("""
                -fx-background-color: #1f1f1f;
                -fx-padding: 10px;
                -fx-border-color: #444444;
                -fx-border-width: 1px;
                """);
        
        Label hwInfoTitle = new Label("HARDWARE SPECIFICATIONS");
        hwInfoTitle.setStyle("""
                -fx-font-family: 'Courier New';
                -fx-font-size: 14px;
                -fx-font-weight: bold;
                -fx-text-fill: #00ffff;
                """);
        
        
        HBox cpuInfo = createHardwareInfoRow("vCPUs", vps.getVCPUs(), usedVCPUs);
        
        
        HBox ramInfo = createHardwareInfoRow("RAM", vps.getRamInGB(), usedRamGB);
        
        
        HBox diskInfo = createHardwareInfoRow("DISK", vps.getDiskInGB(), usedDiskGB);
        
        
        Label sizeLabel = new Label(String.format("FORM FACTOR: %s (SLOTS: %d)", 
                vps.getSize().getDisplayName(), vps.getSlotsRequired()));
        sizeLabel.setStyle("""
                -fx-font-family: 'Courier New';
                -fx-font-size: 12px;
                -fx-text-fill: #aaaaaa;
                """);
        
        hardwareInfo.getChildren().addAll(hwInfoTitle, cpuInfo, ramInfo, diskInfo, sizeLabel);
        
        
        VBox systemStatus = new VBox(10);
        systemStatus.setPrefWidth(350);
        systemStatus.setStyle("""
                -fx-background-color: #1a1a1a;
                -fx-padding: 10px;
                -fx-border-color: #444444;
                -fx-border-width: 1px;
                """);
        
        Label statusTitle = new Label("SYSTEM STATUS");
        statusTitle.setStyle("""
                -fx-font-family: 'Courier New';
                -fx-font-size: 14px;
                -fx-font-weight: bold;
                -fx-text-fill: #00ffff;
                """);
        
        
        VBox statusContent = new VBox(8);
        statusContent.setPadding(new Insets(5, 0, 0, 0));
        
        
        Label networkStatusLabel = new Label("NETWORK: CONNECTED (10 Gbps)");
        networkStatusLabel.setStyle("""
                -fx-font-family: 'Courier New';
                -fx-font-size: 12px;
                -fx-text-fill: #00ff00;
                """);
        
        
        Label healthStatusLabel = new Label("SERVER HEALTH: OPTIMAL");
        healthStatusLabel.setStyle("""
                -fx-font-family: 'Courier New';
                -fx-font-size: 12px;
                -fx-text-fill: #00ff00;
                """);
        
        
        Label uptimeLabel = new Label("UPTIME: 30d:12h:45m:10s");
        uptimeLabel.setStyle("""
                -fx-font-family: 'Courier New';
                -fx-font-size: 12px;
                -fx-text-fill: #aaaaaa;
                """);
        
        
        Label vmCountLabel = new Label("VIRTUAL MACHINES: " + vps.getVms().size());
        vmCountLabel.setStyle("""
                -fx-font-family: 'Courier New';
                -fx-font-size: 12px;
                -fx-text-fill: #aaaaaa;
                """);
        
        
        Label utilizationLabel = new Label(String.format("RESOURCE UTILIZATION: %.1f%%", 
                calculateUtilization(vps, usedVCPUs, usedRamGB, usedDiskGB)));
        utilizationLabel.setStyle("""
                -fx-font-family: 'Courier New';
                -fx-font-size: 12px;
                -fx-text-fill: #aaaaaa;
                """);
        
        statusContent.getChildren().addAll(
            networkStatusLabel, healthStatusLabel, uptimeLabel, vmCountLabel, utilizationLabel
        );
        
        systemStatus.getChildren().addAll(statusTitle, statusContent);
        
        serverContent.getChildren().addAll(hardwareInfo, systemStatus);
        
        serverChassis.getChildren().addAll(frontPanel, serverContent);
        serverBox.getChildren().add(serverChassis);
        
        return serverBox;
    }
    
    
    private HBox createHardwareInfoRow(String resourceName, int total, int used) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        
        
        int available = total - used;
        double usedPercentage = (double) used / total;
        
        
        Label nameLabel = new Label(resourceName + ":");
        nameLabel.setPrefWidth(50);
        nameLabel.setStyle("""
                -fx-font-family: 'Courier New';
                -fx-font-size: 12px;
                -fx-font-weight: bold;
                -fx-text-fill: #00ffff;
                """);
        
        
        HBox progressContainer = new HBox();
        progressContainer.setPrefWidth(180);
        progressContainer.setPrefHeight(15);
        progressContainer.setStyle("""
                -fx-background-color: #0a0a0a;
                -fx-border-color: #444444;
                -fx-border-width: 1px;
                """);
        
        
        HBox usedBar = new HBox();
        usedBar.setPrefWidth(180 * usedPercentage);
        usedBar.setPrefHeight(13);
        
        
        String barColor;
        if (usedPercentage < 0.6) {
            barColor = "#00cc00"; 
        } else if (usedPercentage < 0.8) {
            barColor = "#ffcc00"; 
        } else {
            barColor = "#ff6600"; 
        }
        
        usedBar.setStyle("-fx-background-color: " + barColor + ";");
        progressContainer.getChildren().add(usedBar);
        
        
        Label countLabel = new Label(String.format("%d/%d  [%d free]", used, total, available));
        countLabel.setStyle("""
                -fx-font-family: 'Courier New';
                -fx-font-size: 11px;
                -fx-text-fill: #aaaaaa;
                """);
        
        row.getChildren().addAll(nameLabel, progressContainer, countLabel);
        return row;
    }
    
    
    private HBox createVMEntry(VPSOptimization.VM vm, VPSOptimization vps) {
        HBox entry = new HBox(15);
        entry.setPadding(new Insets(10));
        entry.setStyle("""
                -fx-background-color: #1a1a1a;
                -fx-background-radius: 3px;
                -fx-border-color: #444444;
                -fx-border-width: 1px;
                -fx-border-radius: 3px;
                """);
        
        
        Circle statusIndicator = new Circle(5);
        statusIndicator.setFill(Color.web("#00ff00"));
        statusIndicator.setEffect(new Glow(0.5));
        statusIndicator.setTranslateY(8);
        
        
        VBox vmInfo = new VBox(5);
        vmInfo.setPrefWidth(500);
        HBox.setHgrow(vmInfo, Priority.ALWAYS);
        
        
        Label vmNameLabel = new Label("VM: " + vm.getIp());
        vmNameLabel.setStyle("""
                -fx-font-family: 'Courier New';
                -fx-font-size: 14px;
                -fx-font-weight: bold;
                -fx-text-fill: #00ffff;
                """);
        
        
        Label vmSpecsLabel = new Label(String.format(
                "vCPUs: %d | RAM: %s | Disk: %s", 
                vm.getVcpu(), vm.getRam(), vm.getDisk()));
        vmSpecsLabel.setStyle("""
                -fx-font-family: 'Courier New';
                -fx-font-size: 12px;
                -fx-text-fill: #aaaaaa;
                """);
        
        vmInfo.getChildren().addAll(vmNameLabel, vmSpecsLabel);
        
        
        Button manageButton = new Button("MANAGE");
        manageButton.setStyle("""
                -fx-background-color: #3a1a4a;
                -fx-border-color: #8a2be2;
                -fx-border-width: 1px;
                -fx-text-fill: #e0b0ff;
                -fx-font-family: 'Courier New';
                -fx-font-size: 12px;
                -fx-font-weight: bold;
                -fx-padding: 5px 15px;
                """);
        
        manageButton.setOnMouseEntered(e -> manageButton.setStyle("""
                -fx-background-color: #4a2a5a;
                -fx-border-color: #b041ff;
                -fx-border-width: 1px;
                -fx-text-fill: #f0d0ff;
                -fx-font-family: 'Courier New';
                -fx-font-size: 12px;
                -fx-font-weight: bold;
                -fx-padding: 5px 15px;
                -fx-effect: dropshadow(gaussian, #b041ff, 10, 0.5, 0, 0);
                """));
        
        manageButton.setOnMouseExited(e -> manageButton.setStyle("""
                -fx-background-color: #3a1a4a;
                -fx-border-color: #8a2be2;
                -fx-border-width: 1px;
                -fx-text-fill: #e0b0ff;
                -fx-font-family: 'Courier New';
                -fx-font-size: 12px;
                -fx-font-weight: bold;
                -fx-padding: 5px 15px;
                """));
        
        manageButton.setOnAction(e -> parent.openVMInfoPage(vm, vps));
        
        entry.getChildren().addAll(statusIndicator, vmInfo, manageButton);
        return entry;
    }
    
    
    private Circle createStatusLED(String color, boolean isBlinking) {
        Circle led = new Circle(4);
        led.setFill(Color.web(color));
        led.setEffect(new Glow(0.5));
        
        if (isBlinking) {
            Timeline blinkTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(led.opacityProperty(), 1.0)),
                new KeyFrame(Duration.seconds(0.1), new KeyValue(led.opacityProperty(), 0.3)),
                new KeyFrame(Duration.seconds(0.5), new KeyValue(led.opacityProperty(), 1.0))
            );
            blinkTimeline.setCycleCount(Timeline.INDEFINITE);
            blinkTimeline.play();
        }
        
        return led;
    }
    
    
    private double calculateUtilization(VPSOptimization vps, int usedVCPUs, int usedRamGB, int usedDiskGB) {
        double cpuUtil = (double) usedVCPUs / vps.getVCPUs();
        double ramUtil = (double) usedRamGB / vps.getRamInGB();
        double diskUtil = (double) usedDiskGB / vps.getDiskInGB();
        
        
        return (cpuUtil + ramUtil + diskUtil) / 3 * 100;
    }
    
    
    private void uninstallVPS(VPSOptimization vps) {
        
        if (!vps.getVms().isEmpty()) {
            parent.pushNotification("UNINSTALL FAILED", 
                    "Cannot uninstall server with running VMs. Please delete all VMs first.");
            return;
        }
        
        
        boolean success = parent.uninstallVPSToInventory(vps);
        
        if (success) {
            parent.pushNotification("SERVER UNINSTALLED", 
                    "Successfully uninstalled server. It has been moved to your inventory.");
            
            
            parent.openRackInfo();
        } else {
            parent.pushNotification("UNINSTALL FAILED", 
                    "Failed to uninstall server.");
        }
    }
}


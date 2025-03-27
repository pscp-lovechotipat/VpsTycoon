package com.vpstycoon.ui.game.vps;

import com.vpstycoon.game.company.SkillPointsSystem;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.game.vps.VPSOptimization;
import com.vpstycoon.ui.game.GameplayContentPane;
import com.vpstycoon.ui.game.utils.UIUtils;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VMCreationUI {
    private final GameplayContentPane parent;

    public VMCreationUI(GameplayContentPane parent) {
        this.parent = parent;
    }

    /**
     * Generate the next available IP in the subnet range based on the VPS IP.
     * @param vps The VPS to base the IP on
     * @return The next available IP or null if no IPs are available
     */
    private String getNextAvailableIP(VPSOptimization vps) {
        // Get the VPS IP
        String vpsId = parent.getVpsManager().getVPSMap().keySet().stream()
                .filter(id -> parent.getVpsManager().getVPS(id) == vps)
                .findFirst()
                .orElse(null);
        if (vpsId == null) {
            // Fallback: If VPS ID can't be found, generate a default IP
            return "10.0.0.2";
        }

        // Extract the base IP (before the name suffix)
        String vpsIp;
        if (vpsId.contains("-")) {
            vpsIp = vpsId.split("-")[0];
        } else {
            vpsIp = vpsId; // No dash in the ID, use as is
        }
        
        String[] ipParts = vpsIp.split("\\.");
        if (ipParts.length != 4) {
            // Fallback: If IP format is invalid, generate a default IP
            return "10.0.0.2";
        }

        // Define the subnet range (e.g., 103.216.158.2 to 103.216.158.255)
        String baseSubnet = ipParts[0] + "." + ipParts[1] + "." + ipParts[2] + ".";
        int minRange = 2;   // Start at .2
        int maxRange = 255; // End at .255

        // Get existing VM IPs
        List<String> usedIps = vps.getVms().stream()
                .map(VPSOptimization.VM::getIp)
                .filter(ip -> ip != null && ip.startsWith(baseSubnet))
                .collect(Collectors.toList());

        // Find the next available IP
        for (int i = minRange; i <= maxRange; i++) {
            String candidateIp = baseSubnet + i;
            if (!usedIps.contains(candidateIp)) {
                return candidateIp;
            }
        }
        
        // If all IPs in the subnet are used, return a fallback IP from another subnet
        return "192.168." + (ipParts[2].equals("0") ? "1" : "0") + ".2";
    }

    public void openCreateVMPage(VPSOptimization vps) {
        // Calculate currently used resources
        int usedVCPUs = vps.getVms().stream().mapToInt(VPSOptimization.VM::getVcpu).sum();
        int usedRamGB = vps.getVms().stream()
                .mapToInt(vm -> Integer.parseInt(vm.getRam().replace(" GB", ""))).sum();
        int usedDiskGB = vps.getVms().stream()
                .mapToInt(vm -> Integer.parseInt(vm.getDisk().replace(" GB", ""))).sum();

        // Calculate available resources
        int availableVCPUs = vps.getVCPUs() - usedVCPUs;
        int availableRamGB = vps.getRamInGB() - usedRamGB;
        int availableDiskGB = vps.getDiskInGB() - usedDiskGB;

        if (availableVCPUs < 1 || availableRamGB < 1 || availableDiskGB < 10) {
            parent.pushNotification("Insufficient Resources",
                    "No available resources to create a new VM. Please delete existing VMs to free up resources.");
            parent.openVPSInfoPage(vps);
            return;
        }

        String autoIp = getNextAvailableIP(vps);
        if (autoIp == null) {
            parent.pushNotification("IP Address Error",
                    "No available IP addresses in the subnet range. Maximum VMs reached.");
            parent.openVPSInfoPage(vps);
            return;
        }

        // สร้างหน้าหลักสำหรับสร้าง VM
        BorderPane createVMPane = new BorderPane();
        createVMPane.setPrefSize(800, 600);
        createVMPane.setStyle("-fx-background-color: linear-gradient(to bottom, #2A1B3D, #1A0B2E); -fx-padding: 20px;");

        // Hide the menu bars
        parent.getMenuBar().setVisible(false);
        parent.getInGameMarketMenuBar().setVisible(false);
        
        // ส่วนหัว - Cyberpunk header
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #3A1C5A; -fx-padding: 15px; -fx-background-radius: 5px; " +
                "-fx-effect: dropshadow(gaussian, rgba(120, 0, 255, 0.4), 15, 0, 0, 7); " +
                "-fx-border-color: #8A2BE2; -fx-border-width: 1px; -fx-border-radius: 5px;");
        String vpsId = parent.getVpsManager().getVPSMap().keySet().stream()
                .filter(id -> parent.getVpsManager().getVPS(id) == vps).findFirst().orElse("Unknown");
        
        Label titleLabel = new Label(">> DEPLOY NEW INSTANCE <<");
        titleLabel.setStyle("-fx-text-fill: #E4FBFF; -fx-font-size: 24px; -fx-font-weight: bold; " +
                "-fx-font-family: 'Monospace'; -fx-effect: dropshadow(gaussian, #00F6FF, 5, 0, 0, 0);");
        
        Label subtitleLabel = new Label("Server: " + vpsId);
        subtitleLabel.setStyle("-fx-text-fill: #00F6FF; -fx-font-size: 16px; -fx-font-family: 'Monospace';");
        
        VBox titleBox = new VBox(5);
        titleBox.getChildren().addAll(titleLabel, subtitleLabel);
        
        Button backButton = createCyberButton("< BACK", "#F44336");
        backButton.setOnAction(e -> parent.openVPSInfoPage(vps));
        topBar.getChildren().addAll(backButton, titleBox);

        // Container for the form
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-padding: 10px;");
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        VBox formContainer = new VBox(20);
        formContainer.setPadding(new Insets(10));
        
        // Resource visualization
        HBox resourceVisualization = createCyberCard("AVAILABLE RESOURCES");
        resourceVisualization.setSpacing(20);
        
        // Create progress bars for resources
        int vCpuPercent = (int)(100 * (1 - (double)availableVCPUs/vps.getVCPUs()));
        int ramPercent = (int)(100 * (1 - (double)availableRamGB/vps.getRamInGB()));
        int diskPercent = (int)(100 * (1 - (double)availableDiskGB/vps.getDiskInGB()));
        
        VBox cpuVis = createResourceBar("CPU", availableVCPUs + "/" + vps.getVCPUs() + " vCPUs", vCpuPercent);
        VBox ramVis = createResourceBar("RAM", availableRamGB + "/" + vps.getRamInGB() + " GB", ramPercent);
        VBox diskVis = createResourceBar("SSD", availableDiskGB + "/" + vps.getDiskInGB() + " GB", diskPercent);
        
        resourceVisualization.getChildren().addAll(cpuVis, ramVis, diskVis);
        
        // INSTANCE DETAILS SECTION
        VBox instanceSection = createCyberSection("01 // INSTANCE CONFIG");
        
        // Instance name with cyber styling
        HBox nameBox = new HBox(15);
        nameBox.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label("HOSTNAME:");
        nameLabel.setStyle("-fx-text-fill: #00F6FF; -fx-font-weight: bold; -fx-font-family: 'Monospace';");
        TextField nameField = createCyberTextField("my-awesome-server");
        nameField.setPromptText("Enter instance name");
        nameBox.getChildren().addAll(nameLabel, nameField);
        
        // Operating System selection
        HBox osBox = new HBox(15);
        osBox.setAlignment(Pos.CENTER_LEFT);
        Label osLabel = new Label("OS IMAGE:");
        osLabel.setStyle("-fx-text-fill: #00F6FF; -fx-font-weight: bold; -fx-font-family: 'Monospace';");
        
        ComboBox<String> osComboBox = createCyberComboBox(FXCollections.observableArrayList(
                "Ubuntu 22.04 LTS", "CentOS 9 Stream", "Debian 12", 
                "Windows Server 2022", "Alpine Linux 3.18", "Arch Linux", "FreeBSD 14"));
        osComboBox.setValue("Ubuntu 22.04 LTS");
        osBox.getChildren().addAll(osLabel, osComboBox);
        
        // IP Address field
        HBox ipBox = new HBox(15);
        ipBox.setAlignment(Pos.CENTER_LEFT);
        Label ipLabel = new Label("IP ADDRESS:");
        ipLabel.setStyle("-fx-text-fill: #00F6FF; -fx-font-weight: bold; -fx-font-family: 'Monospace';");
        TextField ipField = createCyberTextField(autoIp);
        ipField.setPromptText("Auto-assigned IP");
        ipField.setDisable(true);
        ipBox.getChildren().addAll(ipLabel, ipField);
        
        instanceSection.getChildren().addAll(nameBox, osBox, ipBox);
        
        // HARDWARE CONFIGURATION SECTION
        VBox hardwareSection = createCyberSection("02 // HARDWARE SPECS");

        // Preset configurations
        HBox presetBox = new HBox(15);
        presetBox.setAlignment(Pos.CENTER_LEFT);
        Label presetLabel = new Label("PRESET:");
        presetLabel.setStyle("-fx-text-fill: #00F6FF; -fx-font-weight: bold; -fx-font-family: 'Monospace';");
        
        ComboBox<String> presetComboBox = createCyberComboBox(FXCollections.observableArrayList(
                "Basic (1 vCPU, 1GB RAM, 10GB SSD)", 
                "Standard (2 vCPU, 4GB RAM, 50GB SSD)", 
                "Performance (4 vCPU, 8GB RAM, 100GB SSD)",
                "Enterprise (8 vCPU, 16GB RAM, 200GB SSD)",
                "Custom Configuration"));
        presetComboBox.setValue("Custom Configuration");
        presetBox.getChildren().addAll(presetLabel, presetComboBox);
        
        // vCPU selection with slider
        HBox vcpuBox = new HBox(15);
        vcpuBox.setAlignment(Pos.CENTER_LEFT);
        Label vcpuLabel = new Label("vCPUs:");
        vcpuLabel.setStyle("-fx-text-fill: #00F6FF; -fx-font-weight: bold; -fx-font-family: 'Monospace';");
        
        // Create CPU options
        List<Integer> vcpuOptions = new ArrayList<>();
        for (int i = 1; i <= availableVCPUs && i <= 16; i *= 2) {
            vcpuOptions.add(i);
        }
        if (vcpuOptions.isEmpty()) vcpuOptions.add(1);
        
        ComboBox<Integer> vcpuComboBox = createCyberComboBox(FXCollections.observableArrayList(vcpuOptions));
        vcpuComboBox.setValue(vcpuOptions.getFirst());
        
        Label vcpuDetailLabel = new Label("Optimal for: web servers, small applications");
        vcpuDetailLabel.setStyle("-fx-text-fill: #00F6FF; -fx-font-style: italic; -fx-font-size: 12px;");
        
        VBox vcpuVBox = new VBox(5);
        vcpuVBox.getChildren().addAll(vcpuComboBox, vcpuDetailLabel);
        vcpuBox.getChildren().addAll(vcpuLabel, vcpuVBox);
        
        // RAM selection with modern styling
        HBox ramBox = new HBox(15);
        ramBox.setAlignment(Pos.CENTER_LEFT);
        Label ramLabel = new Label("MEMORY:");
        ramLabel.setStyle("-fx-text-fill: #00F6FF; -fx-font-weight: bold; -fx-font-family: 'Monospace';");
        
        List<String> ramOptions = new ArrayList<>();
        for (int ram : new int[]{1, 2, 4, 8, 16, 32}) {
            if (ram <= availableRamGB) {
                ramOptions.add(ram + " GB");
            }
        }
        if (ramOptions.isEmpty()) ramOptions.add("1 GB");
        
        ComboBox<String> ramComboBox = createCyberComboBox(FXCollections.observableArrayList(ramOptions));
        ramComboBox.setValue(ramOptions.getFirst());
        
        Label ramDetailLabel = new Label("High-performance DDR4 ECC memory");
        ramDetailLabel.setStyle("-fx-text-fill: #00F6FF; -fx-font-style: italic; -fx-font-size: 12px;");
        
        VBox ramVBox = new VBox(5);
        ramVBox.getChildren().addAll(ramComboBox, ramDetailLabel);
        ramBox.getChildren().addAll(ramLabel, ramVBox);
        
        // Disk selection with enhanced UI
        HBox diskBox = new HBox(15);
        diskBox.setAlignment(Pos.CENTER_LEFT);
        Label diskLabel = new Label("STORAGE:");
        diskLabel.setStyle("-fx-text-fill: #00F6FF; -fx-font-weight: bold; -fx-font-family: 'Monospace';");
        
        List<String> diskOptions = new ArrayList<>();
        for (int disk : new int[]{10, 20, 50, 100, 200, 500, 1024}) {
            if (disk <= availableDiskGB) {
                diskOptions.add(disk + " GB");
            }
        }
        if (diskOptions.isEmpty()) diskOptions.add("10 GB");
        
        ComboBox<String> diskComboBox = createCyberComboBox(FXCollections.observableArrayList(diskOptions));
        diskComboBox.setValue(diskOptions.getFirst());
        
        ComboBox<String> diskTypeComboBox = createCyberComboBox(FXCollections.observableArrayList(
                "NVMe SSD", "SSD", "HDD"));
        diskTypeComboBox.setValue("NVMe SSD");
        
        VBox diskVBox = new VBox(5);
        HBox diskSelectionBox = new HBox(10);
        diskSelectionBox.getChildren().addAll(diskComboBox, diskTypeComboBox);
        
        Label diskDetailLabel = new Label("NVMe SSD with up to 3,500 MB/s read speeds");
        diskDetailLabel.setStyle("-fx-text-fill: #00F6FF; -fx-font-style: italic; -fx-font-size: 12px;");
        
        diskVBox.getChildren().addAll(diskSelectionBox, diskDetailLabel);
        diskBox.getChildren().addAll(diskLabel, diskVBox);
        
        hardwareSection.getChildren().addAll(presetBox, vcpuBox, ramBox, diskBox);
        
        // NETWORK SECTION
        VBox networkSection = createCyberSection("03 // NETWORK CONFIG");
        
        // Network options
        HBox networkBox = new HBox(15);
        networkBox.setAlignment(Pos.CENTER_LEFT);
        Label networkLabel = new Label("NETWORK:");
        networkLabel.setStyle("-fx-text-fill: #00F6FF; -fx-font-weight: bold; -fx-font-family: 'Monospace';");
        
        ComboBox<String> networkComboBox = createCyberComboBox(FXCollections.observableArrayList(
                "Public (Internet Accessible)", 
                "Private (Internal Only)", 
                "Hybrid (Public + Private)"));
        networkComboBox.setValue("Public (Internet Accessible)");
        networkBox.getChildren().addAll(networkLabel, networkComboBox);
        
        // Bandwidth options
        HBox bandwidthBox = new HBox(15);
        bandwidthBox.setAlignment(Pos.CENTER_LEFT);
        Label bandwidthLabel = new Label("BANDWIDTH:");
        bandwidthLabel.setStyle("-fx-text-fill: #00F6FF; -fx-font-weight: bold; -fx-font-family: 'Monospace';");
        
        ComboBox<String> bandwidthComboBox = createCyberComboBox(FXCollections.observableArrayList(
                "1 Gbps (Standard)", 
                "2 Gbps (Enhanced)", 
                "5 Gbps (Premium)", 
                "10 Gbps (Enterprise)"));
        bandwidthComboBox.setValue("1 Gbps (Standard)");
        bandwidthBox.getChildren().addAll(bandwidthLabel, bandwidthComboBox);
        
        // Firewall section
        VBox firewallSection = createCyberSection("04 // SECURITY");
        
        // Check if firewall management is unlocked
        SkillPointsSystem skillPointsSystem = ResourceManager.getInstance().getSkillPointsSystem();
        boolean firewallUnlocked = skillPointsSystem != null && 
                skillPointsSystem.isFirewallManagementUnlocked();

        // Define default rules outside the if block so they're accessible everywhere
        List<String> defaultRules = new ArrayList<>();
        defaultRules.add("ALLOW TCP IN 22 (SSH) FROM ANY");
        defaultRules.add("ALLOW TCP IN 80 (HTTP) FROM ANY");
        defaultRules.add("ALLOW TCP IN 443 (HTTPS) FROM ANY");
        defaultRules.add("DENY ALL OTHER INCOMING TRAFFIC");

        if (firewallUnlocked) {
            // Security level selector
            HBox securityLevelBox = new HBox(15);
            securityLevelBox.setAlignment(Pos.CENTER_LEFT);
            Label securityLabel = new Label("SECURITY PROFILE:");
            securityLabel.setStyle("-fx-text-fill: #00F6FF; -fx-font-weight: bold; -fx-font-family: 'Monospace';");
            
            ComboBox<String> securityComboBox = createCyberComboBox(FXCollections.observableArrayList(
                    "Standard (Basic Protection)", 
                    "Enhanced (DDoS Protection)", 
                    "Fortress (Maximum Security)"));
            securityComboBox.setValue("Standard (Basic Protection)");
            securityLevelBox.getChildren().addAll(securityLabel, securityComboBox);
            
            // Ports configuration
            HBox portsBox = new HBox(15);
            portsBox.setAlignment(Pos.CENTER_LEFT);
            Label portsLabel = new Label("OPEN PORTS:");
            portsLabel.setStyle("-fx-text-fill: #00F6FF; -fx-font-weight: bold; -fx-font-family: 'Monospace';");
            TextField portsField = createCyberTextField("22, 80, 443");
            portsField.setPromptText("Comma-separated list of ports (e.g., 22, 80, 443)");
            portsBox.getChildren().addAll(portsLabel, portsField);

            // Firewall rules with modern styling
            VBox rulesBox = new VBox(10);
            rulesBox.setAlignment(Pos.CENTER_LEFT);
            Label rulesLabel = new Label("FIREWALL RULES:");
            rulesLabel.setStyle("-fx-text-fill: #00F6FF; -fx-font-weight: bold; -fx-font-family: 'Monospace';");

            ListView<String> rulesList = new ListView<>();
            rulesList.setPrefHeight(120);
            rulesList.setStyle("-fx-background-color: #2A1B3D; -fx-text-fill: white; " +
                    "-fx-control-inner-background: #2A1B3D; " +
                    "-fx-border-color: #8A2BE2; -fx-border-width: 1px;");

            rulesList.setItems(FXCollections.observableArrayList(defaultRules));

            HBox ruleActionBox = new HBox(10);
            TextField newRuleField = createCyberTextField("");
            newRuleField.setPromptText("Enter new rule");
            Button addRuleButton = createCyberButton("ADD RULE", "#2196F3");
            addRuleButton.setOnAction(e -> {
                if (!newRuleField.getText().isEmpty()) {
                    rulesList.getItems().add(newRuleField.getText());
                    newRuleField.clear();
                }
            });

            ruleActionBox.getChildren().addAll(newRuleField, addRuleButton);
            rulesBox.getChildren().addAll(rulesLabel, rulesList, ruleActionBox);

            firewallSection.getChildren().addAll(securityLevelBox, portsBox, rulesBox);
        } else {
            // Locked firewall section
            VBox lockedBox = new VBox(10);
            lockedBox.setAlignment(Pos.CENTER);
            lockedBox.setStyle("-fx-border-color: #8A2BE2; -fx-border-width: 1px; " +
                    "-fx-background-color: rgba(138, 43, 226, 0.1); -fx-padding: 20px;");
            
            Label lockIcon = new Label("🔒");
            lockIcon.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 36px;");
            
            Label lockedLabel = new Label("ADVANCED SECURITY MODULE LOCKED");
            lockedLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 18px;");
            
            Label descLabel = new Label("Upgrade Security skill to unlock firewall management");
            descLabel.setStyle("-fx-text-fill: #00F6FF; -fx-font-style: italic;");

            Button upgradeButton = createCyberButton("SKILL UPGRADE", "#3498db");
            upgradeButton.setOnAction(e -> parent.openSkillPointsWindow());

            lockedBox.getChildren().addAll(lockIcon, lockedLabel, descLabel, upgradeButton);
            firewallSection.getChildren().add(lockedBox);
        }
        
        // STARTUP OPTIONS SECTION
        VBox startupSection = createCyberSection("05 // INITIALIZATION");
        
        // Startup script
        VBox scriptBox = new VBox(10);
        scriptBox.setAlignment(Pos.CENTER_LEFT);
        Label scriptLabel = new Label("STARTUP SCRIPT:");
        scriptLabel.setStyle("-fx-text-fill: #00F6FF; -fx-font-weight: bold; -fx-font-family: 'Monospace';");
        
        TextArea scriptArea = new TextArea();
        scriptArea.setPrefHeight(100);
        scriptArea.setPromptText("#!/bin/bash\n# Enter your startup script here\n# This will run when the VM boots");
        scriptArea.setStyle("-fx-background-color: #2A1B3D; -fx-text-fill: #00F6FF; " +
                "-fx-control-inner-background: #2A1B3D; " +
                "-fx-border-color: #8A2BE2; -fx-border-width: 1px; -fx-font-family: 'Monospace';");
        
        scriptBox.getChildren().addAll(scriptLabel, scriptArea);
        
        // Backup options
        HBox backupBox = new HBox(15);
        backupBox.setAlignment(Pos.CENTER_LEFT);
        Label backupLabel = new Label("BACKUP SCHEDULE:");
        backupLabel.setStyle("-fx-text-fill: #00F6FF; -fx-font-weight: bold; -fx-font-family: 'Monospace';");
        
        ComboBox<String> backupComboBox = createCyberComboBox(FXCollections.observableArrayList(
                "None", "Daily", "Weekly", "Monthly"));
        backupComboBox.setValue("None");
        backupBox.getChildren().addAll(backupLabel, backupComboBox);
        
        startupSection.getChildren().addAll(scriptBox, backupBox);
        
        // Cost estimation with cyber styling
        HBox costEstimation = createCyberCard("ESTIMATED COST");
        
        Label costLabel = new Label("MONTHLY COST: $20.99");
        costLabel.setStyle("-fx-text-fill: #00F6FF; -fx-font-size: 18px; -fx-font-weight: bold; " +
                "-fx-font-family: 'Monospace';");
        
        Label costDetail = new Label("Includes base VM cost + additional services");
        costDetail.setStyle("-fx-text-fill: white; -fx-font-style: italic;");
        
        VBox costInfo = new VBox(5);
        costInfo.getChildren().addAll(costLabel, costDetail);
        costEstimation.getChildren().add(costInfo);
        
        // Combine all sections
        networkSection.getChildren().addAll(networkBox, bandwidthBox);
        formContainer.getChildren().addAll(
                resourceVisualization, 
                instanceSection, 
                hardwareSection, 
                networkSection, 
                firewallSection, 
                startupSection,
                costEstimation);
        
        scrollPane.setContent(formContainer);
        
        // Action buttons with cyber styling
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(20, 10, 10, 10));
        buttonBox.setStyle("-fx-background-color: rgba(58, 28, 90, 0.7); -fx-padding: 10px; " +
                "-fx-border-color: #8A2BE2; -fx-border-width: 1px 0 0 0;");
        
        Button resetButton = createCyberButton("RESET", "#F44336");
        resetButton.setOnAction(e -> {
            nameField.clear();
            nameField.setPromptText("Enter instance name");
            osComboBox.setValue("Ubuntu 22.04 LTS");
            vcpuComboBox.setValue(vcpuOptions.get(0));
            ramComboBox.setValue(ramOptions.get(0));
            diskComboBox.setValue(diskOptions.get(0));
            diskTypeComboBox.setValue("NVMe SSD");
            networkComboBox.setValue("Public (Internet Accessible)");
            bandwidthComboBox.setValue("1 Gbps (Standard)");
            if (firewallUnlocked) {
                try {
                    // First, find the ports box which is the second child
                    HBox portsBox = (HBox) firewallSection.getChildren().get(1);
                    if (portsBox != null) {
                        // Find the text field which is the second child of portsBox
                        TextField portsFieldRef = (TextField) portsBox.getChildren().get(1);
                        if (portsFieldRef != null) {
                            portsFieldRef.setText("22, 80, 443");
                        }
                    }
                    
                    // Find the rules box which is the third child
                    VBox rulesBox = (VBox) firewallSection.getChildren().get(2);
                    if (rulesBox != null) {
                        // Find the list view which is the second child of rulesBox
                        ListView<String> rulesListRef = (ListView<String>) rulesBox.getChildren().get(1);
                        if (rulesListRef != null) {
                            rulesListRef.setItems(FXCollections.observableArrayList(defaultRules));
                        }
                    }
                } catch (Exception ex) {
                    System.out.println("Warning: Could not reset some firewall fields: " + ex.getMessage());
                }
            }
            scriptArea.clear();
            backupComboBox.setValue("None");
        });
        
        Button deployButton = createCyberButton("DEPLOY", "#8A2BE2");
        deployButton.setStyle("-fx-background-color: #8A2BE2; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10px 25px; -fx-font-size: 14px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 0); " +
                "-fx-border-color: #00F6FF; -fx-border-width: 1px; " +
                "-fx-cursor: hand;");
        deployButton.setOnAction(e -> {
            if (nameField.getText().isEmpty()) {
                System.out.println("Validation Error: Name field is required.");
                showValidationError("Instance name required", "Please enter a name for your VM instance.");
            } else {
                try {
                    VPSOptimization.VM newVM = new VPSOptimization.VM(
                            nameField.getText(),
                            vcpuComboBox.getValue(),
                            Integer.parseInt(ramComboBox.getValue().replaceAll("[^0-9]", "")),
                            Integer.parseInt(diskComboBox.getValue().replaceAll("[^0-9]", ""))
                    );

                    // กำหนดค่าเพิ่มเติมหลังสร้าง VM
                    newVM.setIp(ipField.getText());
                    newVM.setStatus("Initializing");
                    
                    // Store additional configuration in the VM's description or elsewhere if needed
                    // These methods don't exist in the VM class, so we'll comment them out
                    // newVM.setOperatingSystem(osComboBox.getValue());
                    // newVM.setDiskType(diskTypeComboBox.getValue());
                    // newVM.setNetworkType(networkComboBox.getValue());
                    // newVM.setBandwidth(bandwidthComboBox.getValue());
                    
                    // Add custom VM description with additional info
                    String vmDescription = "OS: " + osComboBox.getValue() + 
                                          ", Disk: " + diskTypeComboBox.getValue() +
                                          ", Network: " + networkComboBox.getValue() +
                                          ", Bandwidth: " + bandwidthComboBox.getValue();
                    // Store this description somewhere if needed
                    
                    if (firewallUnlocked) {
                        try {
                            // First, find the rules box which is the third child
                            VBox rulesBox = (VBox) firewallSection.getChildren().get(2);
                            if (rulesBox != null) {
                                // Find the list view which is the second child of rulesBox
                                ListView<String> rulesList = (ListView<String>) rulesBox.getChildren().get(1);
                                if (rulesList != null) {
                                    List<String> rules = new ArrayList<>(rulesList.getItems());
                                    // setFirewallRules doesn't exist, save rules somewhere else if needed
                                    // newVM.setFirewallRules(rules);
                                    
                                    // อาจจะบันทึกกฎ Firewall เป็น custom data หรือใน GameState ที่อื่น
                                    System.out.println("Firewall rules: " + rules + " for VM: " + newVM.getName());
                                }
                            }
                        } catch (Exception ex) {
                            System.out.println("Warning: Could not retrieve firewall rules: " + ex.getMessage());
                        }
                    }

                    // Ensure VM is set to "Running" status so it's available for assignment
                    newVM.setStatus("Running");
                    
                    vps.addVM(newVM);
                    System.out.println("Success: VM created: " + ipField.getText() + " in VPS: " + vpsId);
                    
                    // Explicitly update the dashboard to refresh VM count
                    parent.pushNotification("VM Created", "VM " + nameField.getText() + " has been created and is now available for assignment.");
                    
                    parent.openVPSInfoPage(vps);
                } catch (Exception ex) {
                    System.out.println("Error: " + ex.getMessage());
                    showValidationError("Error creating VM", ex.getMessage());
                }
            }
        });
        buttonBox.getChildren().addAll(resetButton, deployButton);

        createVMPane.setTop(topBar);
        createVMPane.setCenter(scrollPane);
        createVMPane.setBottom(buttonBox);

        // แสดงผล
        parent.getGameArea().getChildren().clear();
        parent.getGameArea().getChildren().add(createVMPane);
    }
    
    // Helper methods for UI elements with cyber styling
    
    private Button createCyberButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 8px 15px; -fx-font-family: 'Monospace'; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 5, 0, 0, 0); " +
                "-fx-border-color: white; -fx-border-width: 1px; " +
                "-fx-cursor: hand;");
        return button;
    }
    
    private TextField createCyberTextField(String defaultText) {
        TextField textField = new TextField(defaultText);
        textField.setStyle("-fx-background-color: #2A1B3D; -fx-text-fill: #00F6FF; " +
                "-fx-border-color: #8A2BE2; -fx-border-width: 1px; -fx-border-radius: 3px; " +
                "-fx-font-family: 'Monospace';");
        return textField;
    }
    
    private VBox createCyberSection(String title) {
        VBox section = new VBox(15);
        section.setPadding(new Insets(15));
        section.setStyle("-fx-background-color: rgba(58, 28, 90, 0.4); -fx-background-radius: 5px; " +
                "-fx-border-color: #8A2BE2; -fx-border-width: 1px; -fx-border-radius: 5px; " +
                "-fx-effect: dropshadow(gaussian, rgba(120, 0, 255, 0.2), 10, 0, 0, 3);");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #00F6FF; -fx-font-size: 16px; -fx-font-weight: bold; " +
                "-fx-font-family: 'Monospace'; -fx-padding: 0 0 5 0; " + 
                "-fx-border-color: #8A2BE2; -fx-border-width: 0 0 1 0;");
        titleLabel.setPrefWidth(Double.MAX_VALUE);
                
        section.getChildren().add(titleLabel);
        return section;
    }
    
    private HBox createCyberCard(String title) {
        HBox card = new HBox(15);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: rgba(58, 28, 90, 0.4); -fx-background-radius: 5px; " +
                "-fx-border-color: #00F6FF; -fx-border-width: 1px; -fx-border-radius: 5px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0, 246, 255, 0.2), 10, 0, 0, 3);");
        
        VBox titleBox = new VBox(5);
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #00F6FF; -fx-font-size: 16px; -fx-font-weight: bold; " +
                "-fx-font-family: 'Monospace';");
        titleBox.getChildren().add(titleLabel);
        
        card.getChildren().add(titleBox);
        return card;
    }
    
    private VBox createResourceBar(String resourceName, String resourceValue, int usedPercent) {
        VBox resourceBox = new VBox(5);
        
        Label nameLabel = new Label(resourceName);
        nameLabel.setStyle("-fx-text-fill: #00F6FF; -fx-font-weight: bold; -fx-font-family: 'Monospace';");
        
        ProgressBar progressBar = new ProgressBar((double)usedPercent/100);
        progressBar.setPrefWidth(150);
        progressBar.setStyle("-fx-accent: " + (usedPercent > 80 ? "#F44336" : 
                              usedPercent > 60 ? "#FF9800" : "#4CAF50"));
        
        Label valueLabel = new Label(resourceValue + " available");
        valueLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-family: 'Monospace';");
        
        resourceBox.getChildren().addAll(nameLabel, progressBar, valueLabel);
        return resourceBox;
    }
    
    /**
     * Creates a styled ComboBox with improved visibility for the cyberpunk theme
     * @param items The items to include in the ComboBox
     * @param <T> The type of items in the ComboBox
     * @return A styled ComboBox
     */
    private <T> ComboBox<T> createCyberComboBox(javafx.collections.ObservableList<T> items) {
        ComboBox<T> comboBox = new ComboBox<>(items);
        
        // Set the base style for the ComboBox
        comboBox.setStyle(
                "-fx-background-color: #221133; " + 
                "-fx-border-color: #8A2BE2; " +
                "-fx-border-width: 1px; " +
                "-fx-border-radius: 3px; " +
                "-fx-font-family: 'Monospace';"
        );
        
        // Apply custom styling to make text more visible
        comboBox.setButtonCell(new ListCell<T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                    setStyle("-fx-text-fill: #00F6FF; -fx-font-weight: bold; -fx-background-color: transparent;");
                }
            }
        });
        
        // Style the dropdown items for better visibility
        comboBox.setCellFactory(param -> new ListCell<T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    setStyle("-fx-text-fill: #00F6FF; -fx-background-color: #221133;");
                }
            }
        });
        
        return comboBox;
    }
    
    private void showValidationError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
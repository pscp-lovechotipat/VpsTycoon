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
        if (vpsId == null) return null;

        // Extract the base IP (before the name suffix)
        String vpsIp = vpsId.split("-")[0];
        String[] ipParts = vpsIp.split("\\.");
        if (ipParts.length != 4) return null;

        // Define the subnet range (e.g., 103.216.158.2 to 103.216.158.255)
        String baseSubnet = ipParts[0] + "." + ipParts[1] + "." + ipParts[2] + ".";
        int minRange = 2;   // Start at .2
        int maxRange = 255; // End at .255

        // Get existing VM IPs
        List<String> usedIps = vps.getVms().stream()
                .map(VPSOptimization.VM::getIp)
                .filter(ip -> ip.startsWith(baseSubnet))
                .collect(Collectors.toList());

        // Find the next available IP
        for (int i = minRange; i <= maxRange; i++) {
            String candidateIp = baseSubnet + i;
            if (!usedIps.contains(candidateIp)) {
                return candidateIp;
            }
        }
        return null; // No available IPs in the range
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
        createVMPane.setStyle("-fx-background-color: linear-gradient(to bottom, #2E3B4E, #1A252F); -fx-padding: 20px;");

        // Hide the menu bars
        parent.getMenuBar().setVisible(false);
        parent.getInGameMarketMenuBar().setVisible(false);
        
        // ส่วนหัว
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #37474F; -fx-padding: 10px; -fx-background-radius: 10px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);");
        String vpsId = parent.getVpsManager().getVPSMap().keySet().stream()
                .filter(id -> parent.getVpsManager().getVPS(id) == vps).findFirst().orElse("Unknown");
        Label titleLabel = new Label("Create VM for Server: " + vpsId);
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");
        Button backButton = UIUtils.createModernButton("Back", "#F44336");
        backButton.setOnAction(e -> parent.openVPSInfoPage(vps));
        topBar.getChildren().addAll(backButton, titleLabel);

        // ฟอร์มสำหรับกรอกข้อมูล
        HBox formBox = UIUtils.createCard();
        formBox.setAlignment(Pos.CENTER);
        formBox.setPadding(new Insets(15));

        // ส่วนข้อมูลพื้นฐาน
        VBox infoSection = UIUtils.createSection("Basic Information");
        HBox nameBox = new HBox(10);
        TextField nameField = new TextField();
        nameField.setPromptText("Enter VM Name");
        nameBox.getChildren().addAll(new Label("Name:"), nameField);

        HBox ipBox = new HBox(10);
        TextField ipField = new TextField(autoIp);
        ipField.setPromptText("Auto-assigned IP");
        ipField.setDisable(true); // IP is auto-assigned and not editable
        ipBox.getChildren().addAll(new Label("IP:"), ipField);
        infoSection.getChildren().addAll(nameBox, ipBox);

        // ส่วนการตั้งค่าประสิทธิภาพ
        VBox perfSection = UIUtils.createSection("Performance Settings");
        
        // vCPU dropdown
        HBox vcpuBox = new HBox(10);
        vcpuBox.setAlignment(Pos.CENTER_LEFT);
        Label vcpuLabel = new Label("vCPUs:");
        List<Integer> vcpuOptions = new ArrayList<>();
        for (int i = 1; i <= availableVCPUs && i <= 16; i *= 2) {
            vcpuOptions.add(i);
        }
        if (vcpuOptions.isEmpty()) vcpuOptions.add(1);

        ComboBox<Integer> vcpuComboBox = new ComboBox<>(
                FXCollections.observableArrayList(vcpuOptions)
        );
        vcpuComboBox.setValue(vcpuOptions.getFirst()); // Default value
        vcpuBox.getChildren().addAll(vcpuLabel, vcpuComboBox);
        
        // RAM dropdown
        HBox ramBox = new HBox(10);
        ramBox.setAlignment(Pos.CENTER_LEFT);
        Label ramLabel = new Label("RAM:");
        List<String> ramOptions = new ArrayList<>();
        for (int ram : new int[]{1, 2, 4, 8, 16, 32}) {
            if (ram <= availableRamGB) {
                ramOptions.add(ram + " GB");
            }
        }
        if (ramOptions.isEmpty()) ramOptions.add("1 GB");

        ComboBox<String> ramComboBox = new ComboBox<>(
                FXCollections.observableArrayList(ramOptions)
        );
        ramComboBox.setValue(ramOptions.getFirst()); // Default value
        ramBox.getChildren().addAll(ramLabel, ramComboBox);
        
        // Disk dropdown
        HBox diskBox = new HBox(10);
        diskBox.setAlignment(Pos.CENTER_LEFT);
        Label diskLabel = new Label("Disk:");
        List<String> diskOptions = new ArrayList<>();
        for (int disk : new int[]{10, 20, 50, 100, 200, 500, 1024}) {
            if (disk <= availableDiskGB) {
                diskOptions.add(disk + " GB");
            }
        }
        if (diskOptions.isEmpty()) diskOptions.add("10 GB");
        ComboBox<String> diskComboBox = new ComboBox<>(
                FXCollections.observableArrayList(diskOptions)
        );
        diskComboBox.setValue(diskOptions.getFirst()); // Default value
        diskBox.getChildren().addAll(diskLabel, diskComboBox);
        
        perfSection.getChildren().addAll(vcpuBox, ramBox, diskBox);
        
        // Firewall Section
        VBox firewallSection = UIUtils.createSection("Firewall Settings");
        
        // Check if firewall management is unlocked
        SkillPointsSystem skillPointsSystem = ResourceManager.getInstance().getSkillPointsSystem();
        boolean firewallUnlocked = skillPointsSystem != null && 
                skillPointsSystem.isFirewallManagementUnlocked();

        if (firewallUnlocked) {
            HBox portsBox = new HBox(10);
            portsBox.setAlignment(Pos.CENTER_LEFT);
            Label portsLabel = new Label("Open Ports:");
            TextField portsField = new TextField("22, 80, 443");
            portsField.setPromptText("Comma-separated list of ports (e.g., 22, 80, 443)");
            portsBox.getChildren().addAll(portsLabel, portsField);

            VBox rulesBox = new VBox(10);
            rulesBox.setAlignment(Pos.CENTER_LEFT);
            Label rulesLabel = new Label("Firewall Rules:");

            ListView<String> rulesList = new ListView<>();
            rulesList.setPrefHeight(100);

            List<String> defaultRules = new ArrayList<>();
            defaultRules.add("Allow SSH (Port 22)");
            defaultRules.add("Allow HTTP (Port 80)");
            defaultRules.add("Allow HTTPS (Port 443)");
            defaultRules.add("Block all other incoming traffic");

            rulesList.setItems(FXCollections.observableArrayList(defaultRules));

            HBox ruleActionBox = new HBox(10);
            TextField newRuleField = new TextField();
            newRuleField.setPromptText("Enter new rule");
            Button addRuleButton = UIUtils.createModernButton("Add Rule", "#2196F3");
            addRuleButton.setOnAction(e -> {
                if (!newRuleField.getText().isEmpty()) {
                    rulesList.getItems().add(newRuleField.getText());
                    newRuleField.clear();
                }
            });

            ruleActionBox.getChildren().addAll(newRuleField, addRuleButton);
            rulesBox.getChildren().addAll(rulesLabel, rulesList, ruleActionBox);

            firewallSection.getChildren().addAll(portsBox, rulesBox);
        } else {
            Label lockedLabel = new Label("Firewall management is locked. Upgrade Security skill to unlock.");
            lockedLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");

            Button upgradeButton = UIUtils.createModernButton("Go to Skills", "#3498db");
            upgradeButton.setOnAction(e -> parent.openSkillPointsWindow());

            firewallSection.getChildren().addAll(lockedLabel, upgradeButton);
        }

        formBox.getChildren().addAll(infoSection, perfSection, firewallSection);

        // ปุ่มควบคุม
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10));
        Button resetButton = UIUtils.createModernButton("Reset", "#F44336");
        resetButton.setOnAction(e -> {
            nameField.clear();
            ipField.clear();
            vcpuComboBox.setValue(vcpuOptions.get(0));
            ramComboBox.setValue(ramOptions.get(0));
            diskComboBox.setValue(diskOptions.get(0));
        });
        Button createButton = UIUtils.createModernButton("Create", "#2196F3");
        createButton.setOnAction(e -> {
            if (nameField.getText().isEmpty() || ipField.getText().isEmpty()) {
                System.out.println("Validation Error: Name and IP fields are required.");
            } else {
                try {
                    VPSOptimization.VM newVM = new VPSOptimization.VM(
                            ipField.getText(), nameField.getText(),
                            vcpuComboBox.getValue(), ramComboBox.getValue(),
                            diskComboBox.getValue(), "Running"
                    );

                    if (firewallUnlocked) {
                        @SuppressWarnings("unchecked")
                        ListView<String> rulesList = (ListView<String>)
                                ((VBox) ((HBox) firewallSection.getChildren().get(1)).getChildren().get(1)).getChildren().get(1);
                        List<String> rules = new ArrayList<>(rulesList.getItems());
                        newVM.setFirewallRules(rules);
                    }

                    vps.addVM(newVM);
                    System.out.println("Success: VM created: " + ipField.getText() + " in VPS: " + vpsId);
                    parent.openVPSInfoPage(vps);
                } catch (Exception ex) {
                    System.out.println("Error: " + ex.getMessage());
                }
            }
        });
        buttonBox.getChildren().addAll(resetButton, createButton);

        createVMPane.setTop(topBar);
        createVMPane.setCenter(formBox);
        createVMPane.setBottom(buttonBox);

        // แสดงผล
        parent.getGameArea().getChildren().clear();
        parent.getGameArea().getChildren().add(createVMPane);
    }
}
package com.vpstycoon.ui.game.vps;

import com.vpstycoon.game.SkillPointsSystem;
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

public class VMCreationUI {
    private final GameplayContentPane parent;

    public VMCreationUI(GameplayContentPane parent) {
        this.parent = parent;
    }

    public void openCreateVMPage(VPSOptimization vps) {
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
        TextField ipField = new TextField();
        ipField.setPromptText("Enter IP (e.g., 192.168.1.10)");
        ipBox.getChildren().addAll(new Label("IP:"), ipField);
        infoSection.getChildren().addAll(nameBox, ipBox);

        // ส่วนการตั้งค่าประสิทธิภาพ
        VBox perfSection = UIUtils.createSection("Performance Settings");
        
        // vCPU dropdown
        HBox vcpuBox = new HBox(10);
        vcpuBox.setAlignment(Pos.CENTER_LEFT);
        Label vcpuLabel = new Label("vCPUs:");
        ComboBox<Integer> vcpuComboBox = new ComboBox<>(
                FXCollections.observableArrayList(1, 2, 4, 8, 16)
        );
        vcpuComboBox.setValue(1); // Default value
        vcpuBox.getChildren().addAll(vcpuLabel, vcpuComboBox);
        
        // RAM dropdown
        HBox ramBox = new HBox(10);
        ramBox.setAlignment(Pos.CENTER_LEFT);
        Label ramLabel = new Label("RAM:");
        ComboBox<String> ramComboBox = new ComboBox<>(
                FXCollections.observableArrayList("1 GB", "2 GB", "4 GB", "8 GB", "16 GB", "32 GB")
        );
        ramComboBox.setValue("1 GB"); // Default value
        ramBox.getChildren().addAll(ramLabel, ramComboBox);
        
        // Disk dropdown
        HBox diskBox = new HBox(10);
        diskBox.setAlignment(Pos.CENTER_LEFT);
        Label diskLabel = new Label("Disk:");
        ComboBox<String> diskComboBox = new ComboBox<>(
                FXCollections.observableArrayList("10 GB", "20 GB", "50 GB", "100 GB", "200 GB", "500 GB", "1 TB")
        );
        diskComboBox.setValue("20 GB"); // Default value
        diskBox.getChildren().addAll(diskLabel, diskComboBox);
        
        perfSection.getChildren().addAll(vcpuBox, ramBox, diskBox);
        
        // Firewall Section
        VBox firewallSection = UIUtils.createSection("Firewall Settings");
        
        // Check if firewall management is unlocked
        SkillPointsSystem skillPointsSystem = parent.getSkillPointsSystem();
        boolean firewallUnlocked = skillPointsSystem != null && 
                skillPointsSystem.isFirewallManagementUnlocked();
        
        if (firewallUnlocked) {
            // Allowed ports
            HBox portsBox = new HBox(10);
            portsBox.setAlignment(Pos.CENTER_LEFT);
            Label portsLabel = new Label("Open Ports:");
            TextField portsField = new TextField("22, 80, 443"); // Default common ports
            portsField.setPromptText("Comma-separated list of ports (e.g., 22, 80, 443)");
            portsBox.getChildren().addAll(portsLabel, portsField);
            
            // Firewall rules
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
            vcpuComboBox.setValue(1);
            ramComboBox.setValue("1 GB");
            diskComboBox.setValue("20 GB");
        });
        Button createButton = UIUtils.createModernButton("Create", "#2196F3");
        createButton.setOnAction(e -> {
            // ตรวจสอบข้อมูล
            if (nameField.getText().isEmpty() || ipField.getText().isEmpty()) {
                System.out.println("Validation Error: Name and IP fields are required.");
            } else {
                try {
                    // สร้าง VM ใหม่ด้วยค่าเริ่มต้น
                    VPSOptimization.VM newVM = new VPSOptimization.VM(
                            ipField.getText(), nameField.getText(),
                            vcpuComboBox.getValue(), ramComboBox.getValue(), 
                            diskComboBox.getValue(), "Running"
                    );
                    
                    // Add firewall rules if available
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
package com.vpstycoon.ui.game.vps;

import com.vpstycoon.game.vps.VPSOptimization;
import com.vpstycoon.ui.game.GameplayContentPane;
import com.vpstycoon.ui.game.utils.UIUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

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

        // ส่วนหัว
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #37474F; -fx-padding: 10px; -fx-background-radius: 10px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);");
        String vpsId = parent.getVpsManager().getVPSMap().keySet().stream()
                .filter(id -> parent.getVpsManager().getVPS(id) == vps).findFirst().orElse("Unknown");
        Label titleLabel = new Label("Create VM for VPS: " + vpsId);
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
        HBox perfBox = new HBox(10);
        TextField vcpuField = new TextField();
        vcpuField.setPromptText("e.g., 1");
        TextField ramField = new TextField();
        ramField.setPromptText("e.g., 2 GB");
        TextField diskField = new TextField();
        diskField.setPromptText("e.g., 20 GB");
        perfBox.getChildren().addAll(
                new Label("vCPUs:"), vcpuField,
                new Label("RAM:"), ramField,
                new Label("Disk:"), diskField
        );
        perfSection.getChildren().add(perfBox);

        formBox.getChildren().addAll(infoSection, perfSection);

        // ปุ่มควบคุม
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10));
        Button resetButton = UIUtils.createModernButton("Reset", "#F44336");
        resetButton.setOnAction(e -> {
            nameField.clear();
            ipField.clear();
            vcpuField.clear();
            ramField.clear();
            diskField.clear();
        });
        Button createButton = UIUtils.createModernButton("Create", "#2196F3");
        createButton.setOnAction(e -> {
            // ตรวจสอบข้อมูล
            if (nameField.getText().isEmpty() || ipField.getText().isEmpty() || vcpuField.getText().isEmpty() ||
                    ramField.getText().isEmpty() || diskField.getText().isEmpty()) {
                System.out.println("Validation Error: All fields are required.");
            } else {
                try {
                    // สร้าง VM ใหม่
                    VPSOptimization.VM newVM = new VPSOptimization.VM(
                            ipField.getText(), nameField.getText(),
                            Integer.parseInt(vcpuField.getText()), ramField.getText(), diskField.getText(), "Running"
                    );
                    vps.addVM(newVM);
                    System.out.println("Success: VM created: " + ipField.getText() + " in VPS: " + vpsId);
                    parent.openVPSInfoPage(vps);
                } catch (NumberFormatException ex) {
                    System.out.println("Error: Invalid numeric input for vCPUs.");
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
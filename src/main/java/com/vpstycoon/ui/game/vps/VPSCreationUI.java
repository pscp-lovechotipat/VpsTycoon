package com.vpstycoon.ui.game.vps;

import com.vpstycoon.game.vps.VPSOptimization;
import com.vpstycoon.ui.game.GameplayContentPane;
import com.vpstycoon.ui.game.utils.UIUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class VPSCreationUI {
    private final GameplayContentPane parent;

    public VPSCreationUI(GameplayContentPane parent) {
        this.parent = parent;
    }

    public void openCreateVPSPage() {
        // ตรวจสอบว่ามีสล็อตว่างหรือไม่
        if (parent.getVpsList().size() >= parent.getOccupiedSlots()) {
            System.out.println("Cannot create VPS: All slots are occupied.");
            return;
        }

        // สร้างหน้าหลักสำหรับสร้าง VPS
        BorderPane createVPSPane = new BorderPane();
        createVPSPane.setPrefSize(800, 600);
        createVPSPane.setStyle("-fx-background-color: linear-gradient(to bottom, #2E3B4E, #1A252F); -fx-padding: 20px;");

        // ส่วนหัว
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #37474F; -fx-padding: 10px; -fx-background-radius: 10px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);");
        Label titleLabel = new Label("Create Virtual Private Server");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");
        Button backButton = UIUtils.createModernButton("Back", "#F44336");
        backButton.setOnAction(e -> parent.openRackInfo());
        topBar.getChildren().addAll(backButton, titleLabel);

        // ฟอร์มสำหรับกรอกข้อมูล
        HBox formBox = UIUtils.createCard();
        formBox.setAlignment(Pos.CENTER);
        formBox.setPadding(new Insets(15));

        // ส่วนข้อมูลพื้นฐาน
        VBox infoSection = UIUtils.createSection("Basic Information");
        HBox nameBox = new HBox(10);
        TextField nameField = new TextField();
        nameField.setPromptText("Enter VPS Name");
        nameBox.getChildren().addAll(new Label("Name:"), nameField);
        HBox ipBox = new HBox(10);
        TextField ipField = new TextField();
        ipField.setPromptText("Enter IP (e.g., 103.216.158.233)");
        ipBox.getChildren().addAll(new Label("IP:"), ipField);
        infoSection.getChildren().addAll(nameBox, ipBox);

        // ส่วนการตั้งค่าประสิทธิภาพ
        VBox perfSection = UIUtils.createSection("Performance Settings");
        HBox perfBox = new HBox(10);
        TextField vcpuField = new TextField();
        vcpuField.setPromptText("e.g., 2");
        TextField ramField = new TextField();
        ramField.setPromptText("e.g., 4 GB");
        TextField diskField = new TextField();
        diskField.setPromptText("e.g., 50 GB");
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
                    // สร้าง VPS ใหม่
                    VPSOptimization newVPS = new VPSOptimization();
                    newVPS.setVCPUs(Integer.parseInt(vcpuField.getText()));
                    newVPS.setRamInGB(Integer.parseInt(ramField.getText().replace(" GB", "")));
                    newVPS.setDiskInGB(Integer.parseInt(diskField.getText().replace(" GB", "")));
                    String vpsId = ipField.getText() + "-" + nameField.getText();
                    parent.getVpsManager().createVPS(vpsId);
                    parent.getVpsManager().getVPSMap().put(vpsId, newVPS);
                    parent.getVpsList().add(newVPS);
                    System.out.println("Success: VPS created: " + vpsId);
                    parent.openRackInfo();
                } catch (NumberFormatException ex) {
                    System.out.println("Error: Invalid numeric input for vCPUs, RAM, or Disk.");
                }
            }
        });
        buttonBox.getChildren().addAll(resetButton, createButton);

        createVPSPane.setTop(topBar);
        createVPSPane.setCenter(formBox);
        createVPSPane.setBottom(buttonBox);

        // แสดงผล
        parent.getGameArea().getChildren().clear();
        parent.getGameArea().getChildren().add(createVPSPane);
    }
}
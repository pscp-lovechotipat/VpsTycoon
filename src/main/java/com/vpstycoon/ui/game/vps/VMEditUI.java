package com.vpstycoon.ui.game.vps;

import com.vpstycoon.game.vps.VPSOptimization;
import com.vpstycoon.ui.game.GameplayContentPane;
import com.vpstycoon.ui.game.utils.UIUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class VMEditUI {
    private final GameplayContentPane parent;

    public VMEditUI(GameplayContentPane parent) {
        this.parent = parent;
    }

    public void openEditVMPage(VPSOptimization.VM vm, VPSOptimization vps) {
        // สร้างหน้าหลักสำหรับแก้ไข VM
        BorderPane editVMPane = new BorderPane();
        editVMPane.setPrefSize(800, 600);
        editVMPane.setStyle("-fx-background-color: linear-gradient(to bottom, #2E3B4E, #1A252F); -fx-padding: 20px;");

        // ส่วนหัว
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #37474F; -fx-padding: 10px; -fx-background-radius: 10px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);");
        Label titleLabel = new Label("Edit VM: " + vm.getIp());
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");
        Button backButton = UIUtils.createModernButton("Back", "#F44336");
        backButton.setOnAction(e -> parent.openVMInfoPage(vm, vps));
        topBar.getChildren().addAll(backButton, titleLabel);

        // ฟอร์มสำหรับแก้ไขข้อมูล
        HBox formBox = UIUtils.createCard();
        formBox.setAlignment(Pos.CENTER);
        formBox.setPadding(new Insets(15));

        // ส่วนข้อมูลพื้นฐาน
        VBox infoSection = UIUtils.createSection("Basic Information");
        HBox nameBox = new HBox(10);
        TextField nameField = new TextField(vm.getName());
        nameField.setPromptText("Enter VM Name");
        nameBox.getChildren().addAll(new Label("Name:"), nameField);
        HBox ipBox = new HBox(10);
        TextField ipField = new TextField(vm.getIp());
        ipField.setPromptText("Enter IP (e.g., 192.168.1.10)");
        ipField.setDisable(true); // IP ไม่สามารถแก้ไขได้
        ipBox.getChildren().addAll(new Label("IP:"), ipField);
        infoSection.getChildren().addAll(nameBox, ipBox);

        // ส่วนการตั้งค่าประสิทธิภาพ
        VBox perfSection = UIUtils.createSection("Performance Settings");
        HBox perfBox = new HBox(10);
        TextField vcpuField = new TextField(String.valueOf(vm.getVCPUs()));
        vcpuField.setPromptText("e.g., 1");
        TextField ramField = new TextField(vm.getRam());
        ramField.setPromptText("e.g., 2 GB");
        TextField diskField = new TextField(vm.getDisk());
        diskField.setPromptText("e.g., 20 GB");
        perfBox.getChildren().addAll(
                new Label("vCPUs:"), vcpuField,
                new Label("RAM:"), ramField,
                new Label("Disk:"), diskField
        );
        perfSection.getChildren().add(perfBox);

        // ส่วนสถานะ
        VBox statusSection = UIUtils.createSection("Status");
        ChoiceBox<String> statusChoice = new ChoiceBox<>();
        statusChoice.getItems().addAll("Running", "Stopped", "Paused");
        statusChoice.setValue(vm.getStatus());
        statusSection.getChildren().addAll(new Label("Status:"), statusChoice);

        formBox.getChildren().addAll(infoSection, perfSection, statusSection);

        // ปุ่มควบคุม
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10));
        Button saveButton = UIUtils.createModernButton("Save", "#2196F3");
        saveButton.setOnAction(e -> {
            try {
                vm.setName(nameField.getText());
                vm.setVCPUs(Integer.parseInt(vcpuField.getText()));
                vm.setRam(ramField.getText());
                vm.setDisk(diskField.getText());
                vm.setStatus(statusChoice.getValue());
                System.out.println("VM updated: " + vm.getIp());
                parent.openVMInfoPage(vm, vps);
            } catch (NumberFormatException ex) {
                System.out.println("Error: Invalid vCPUs value.");
            }
        });
        Button cancelButton = UIUtils.createModernButton("Cancel", "#F44336");
        cancelButton.setOnAction(e -> parent.openVMInfoPage(vm, vps));
        buttonBox.getChildren().addAll(saveButton, cancelButton);

        editVMPane.setTop(topBar);
        editVMPane.setCenter(formBox);
        editVMPane.setBottom(buttonBox);

        // แสดงผล
        parent.getGameArea().getChildren().clear();
        parent.getGameArea().getChildren().add(editVMPane);
    }
}
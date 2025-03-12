package com.vpstycoon.ui.game.vps;

import com.vpstycoon.game.vps.VPSOptimization;
import com.vpstycoon.ui.game.GameplayContentPane;
import com.vpstycoon.ui.game.utils.UIUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class VMInfoUI {
    private final GameplayContentPane parent;

    public VMInfoUI(GameplayContentPane parent) {
        this.parent = parent;
    }

    public void openVMInfoPage(VPSOptimization.VM vm, VPSOptimization vps) {
        // สร้างหน้าหลักสำหรับข้อมูล VM
        BorderPane vmInfoPane = new BorderPane();
        vmInfoPane.setPrefSize(800, 600);
        vmInfoPane.setStyle("-fx-background-color: linear-gradient(to bottom, #2E3B4E, #1A252F); -fx-padding: 20px;");

        // Hide the menu bars
        parent.getMenuBar().setVisible(false);
        parent.getInGameMarketMenuBar().setVisible(false);
        
        // ส่วนหัว
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #37474F; -fx-padding: 10px; -fx-background-radius: 10px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);");
        Label titleLabel = new Label("VM Details: " + vm.getIp());
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");
        Button backButton = UIUtils.createModernButton("Back", "#F44336");
        backButton.setOnAction(e -> parent.openVPSInfoPage(vps));
        topBar.getChildren().addAll(backButton, titleLabel);

        // ข้อมูล VM
        HBox infoBox = UIUtils.createCard();
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setPadding(new Insets(15));
        VBox vmSection = UIUtils.createSection("VM Information");
        Label vmDetail = new Label("IP: " + vm.getIp() + "\nName: " + vm.getName() +
                "\nStatus: " + vm.getStatus() + "\nvCPUs: " + vm.getVcpu() +
                " | RAM: " + vm.getRam() + " | Disk: " + vm.getDisk());
        vmDetail.setStyle("-fx-text-fill: #B0BEC5; -fx-font-size: 14px;");
        vmSection.getChildren().add(vmDetail);
        infoBox.getChildren().add(vmSection);

        // ปุ่มควบคุม
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10));
        Button editButton = UIUtils.createModernButton("Edit", "#2196F3");
        editButton.setOnAction(e -> parent.openEditVMPage(vm, vps));
        Button deleteButton = UIUtils.createModernButton("Delete", "#F44336");
        deleteButton.setOnAction(e -> {
            vps.getVms().remove(vm);
            System.out.println("VM deleted: " + vm.getIp());
            parent.openVPSInfoPage(vps);
        });
        buttonBox.getChildren().addAll(editButton, deleteButton);

        vmInfoPane.setTop(topBar);
        vmInfoPane.setCenter(infoBox);
        vmInfoPane.setBottom(buttonBox);

        // แสดงผล
        parent.getGameArea().getChildren().clear();
        parent.getGameArea().getChildren().add(vmInfoPane);
    }
}
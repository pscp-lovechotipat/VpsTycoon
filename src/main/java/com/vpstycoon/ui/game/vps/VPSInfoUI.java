package com.vpstycoon.ui.game.vps;

import com.vpstycoon.game.vps.VPSOptimization;
import com.vpstycoon.ui.game.GameplayContentPane;
import com.vpstycoon.ui.game.utils.UIUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;

public class VPSInfoUI {
    private final GameplayContentPane parent;

    public VPSInfoUI(GameplayContentPane parent) {
        this.parent = parent;
    }

    public void openVPSInfoPage(VPSOptimization vps) {
        // สร้างหน้าหลักสำหรับข้อมูล VPS
        BorderPane vpsInfoPane = new BorderPane();
        vpsInfoPane.setPrefSize(800, 600);
        vpsInfoPane.setStyle("-fx-background-color: linear-gradient(to bottom, #2E3B4E, #1A252F); -fx-padding: 20px;");

        // ส่วนหัว
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #37474F; -fx-padding: 10px; -fx-background-radius: 10px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);");
        String vpsId = parent.getVpsManager().getVPSMap().keySet().stream()
                .filter(id -> parent.getVpsManager().getVPS(id) == vps).findFirst().orElse("Unknown");
        Label titleLabel = new Label("VPS Details: " + vpsId);
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");
        Button backButton = UIUtils.createModernButton("Back", "#F44336");
        backButton.setOnAction(e -> parent.openRackInfo());
        topBar.getChildren().addAll(backButton, titleLabel);

        // ส่วนเนื้อหา
        VBox centerBox = new VBox(20);
        centerBox.setAlignment(Pos.CENTER);

        // ข้อมูล VPS
        HBox infoBox = UIUtils.createCard();
        infoBox.setPadding(new Insets(15));
        VBox vpsSection = UIUtils.createSection("VPS Information");
        Label vpsDetail = new Label("vCPUs: " + vps.getVCPUs() + "\nRAM: " + vps.getRamInGB() + " GB\nDisk: " + vps.getDiskInGB() + " GB");
        vpsDetail.setStyle("-fx-text-fill: #B0BEC5; -fx-font-size: 14px;");
        vpsSection.getChildren().add(vpsDetail);
        infoBox.getChildren().add(vpsSection);

        // รายการ VM
        HBox vmListBox = UIUtils.createCard();
        vmListBox.setPadding(new Insets(15));
        Label vmLabel = new Label("Virtual Machines");
        vmLabel.setStyle("-fx-text-fill: #B0BEC5; -fx-font-size: 20px; -fx-font-weight: bold;");
        VBox vmRows = new VBox(10);
        for (VPSOptimization.VM vm : vps.getVms()) {
            HBox row = new HBox(10);
            Button vmButton = new Button(vm.getIp());
            vmButton.setPrefWidth(200);
            vmButton.setStyle("-fx-background-color: #455A64; -fx-text-fill: white; -fx-padding: 10px; -fx-background-radius: 8px;");
            vmButton.setOnMouseEntered(e -> vmButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10px; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);"));
            vmButton.setOnMouseExited(e -> vmButton.setStyle("-fx-background-color: #455A64; -fx-text-fill: white; -fx-padding: 10px; -fx-background-radius: 8px;"));
            vmButton.setOnAction(e -> parent.openVMInfoPage(vm, vps));
            row.getChildren().add(vmButton);
            vmRows.getChildren().add(row);
        }
        vmListBox.getChildren().addAll(vmLabel, new Separator(), vmRows);

        centerBox.getChildren().addAll(infoBox, vmListBox);

        // ปุ่มควบคุม
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10));
        Button createVMButton = UIUtils.createModernButton("Create VM", "#2196F3");
        createVMButton.setOnAction(e -> parent.openCreateVMPage(vps));
        Button deleteVPSButton = UIUtils.createModernButton("Delete VPS", "#F44336");
        deleteVPSButton.setOnAction(e -> {
            parent.getVpsManager().getVPSMap().remove(vpsId);
            parent.getVpsList().remove(vps);
            System.out.println("VPS deleted: " + vpsId);
            parent.openRackInfo();
        });
        buttonBox.getChildren().addAll(createVMButton, deleteVPSButton);

        vpsInfoPane.setTop(topBar);
        vpsInfoPane.setCenter(centerBox);
        vpsInfoPane.setBottom(buttonBox);

        // แสดงผล
        parent.getGameArea().getChildren().clear();
        parent.getGameArea().getChildren().add(vpsInfoPane);
    }
}
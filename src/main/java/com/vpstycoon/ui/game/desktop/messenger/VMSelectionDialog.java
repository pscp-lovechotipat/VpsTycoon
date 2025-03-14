package com.vpstycoon.ui.game.desktop.messenger;

import com.vpstycoon.game.vps.VPSOptimization;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.List;

public class VMSelectionDialog extends VBox {
    private ComboBox<VPSOptimization.VM> vmComboBox;
    private Button confirmButton;
    private VPSOptimization.VM selectedVM;
    private StackPane parentPane;
    private Runnable onConfirm; // ตัวแปรสำหรับเก็บ callback

    // Constructor ที่ปรับแล้ว (ลบ Runnable onConfirm ออก)
    public VMSelectionDialog(List<VPSOptimization.VM> availableVMs, StackPane parentPane) {
        this.parentPane = parentPane;

        setAlignment(Pos.CENTER);
        setPadding(new Insets(20));
        setPrefSize(300, 200);
        setMaxSize(300, 200);
        setStyle("-fx-background-color: #34495e; -fx-border-color: #6a00ff; -fx-border-width: 2;");

        // Label
        Label selectVMLabel = new Label("Select VM:");
        selectVMLabel.setStyle("-fx-text-fill: white; -fx-font-family: 'Monospace'; -fx-font-size: 16;");

        // ComboBox สำหรับเลือก VM
        vmComboBox = new ComboBox<>();
        vmComboBox.setMaxWidth(Double.MAX_VALUE);
        vmComboBox.setStyle("-fx-background-color: #34495e; -fx-text-fill: white;");
        vmComboBox.getItems().addAll(availableVMs);
        vmComboBox.setPromptText(availableVMs.isEmpty() ? "No available VMs" : "Select a VM");

        // ปุ่มยืนยัน
        confirmButton = new Button("Confirm");
        confirmButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-family: 'Monospace';");
        confirmButton.setOnAction(e -> {
            selectedVM = vmComboBox.getValue();
            if (selectedVM != null) {
                if (onConfirm != null) { // ตรวจสอบว่า onConfirm ไม่เป็น null
                    onConfirm.run();
                }
                closeDialog();
            }
        });

        // ปุ่มปิด
        Button closeButton = new Button("X");
        closeButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-family: 'Monospace';");
        closeButton.setOnAction(e -> closeDialog());

        // จัดตำแหน่งปุ่มปิดที่มุมขวาบน
        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.getChildren().add(closeButton);

        getChildren().addAll(topBar, selectVMLabel, vmComboBox, confirmButton);

        // เพิ่มพื้นหลังกึ่งโปร่งใสให้กับ dialog
        Rectangle overlay = new Rectangle();
        overlay.setFill(Color.rgb(0, 0, 0, 0.7));
        overlay.widthProperty().bind(parentPane.widthProperty());
        overlay.heightProperty().bind(parentPane.heightProperty());

        // เพิ่ม dialog และ overlay เข้าไปใน parentPane
        parentPane.getChildren().addAll(overlay, this);
        StackPane.setAlignment(this, Pos.CENTER);
    }

    public VPSOptimization.VM getSelectedVM() {
        return selectedVM;
    }

    private void closeDialog() {
        parentPane.getChildren().remove(this);
        parentPane.getChildren().remove(parentPane.getChildren().size() - 1); // ลบ overlay
    }

    // เมธอดใหม่สำหรับกำหนด callback
    public void setOnConfirm(Runnable onConfirm) {
        this.onConfirm = onConfirm;
    }
}
package com.vpstycoon.ui.game.desktop.messenger.views;

import com.vpstycoon.game.vps.VPSOptimization;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

import java.util.List;

public class VMSelectionDialog extends VBox {
    private ComboBox<VPSOptimization.VM> vmComboBox;
    private Button confirmButton;
    private VPSOptimization.VM selectedVM;
    private StackPane parentPane;
    private Runnable onConfirm;

    public VMSelectionDialog(List<VPSOptimization.VM> availableVMs, StackPane parentPane) {
        this.parentPane = parentPane;

        // โหลด CSS ภายนอก
        getStylesheets().add(getClass().getResource("/css/vmselect-modal.css").toExternalForm());

        // ตั้งค่าพื้นฐาน
        setAlignment(Pos.TOP_CENTER); // ปรับเป็น TOP_CENTER เพื่อให้เริ่มจากด้านบน
        setPadding(new Insets(25));
        setPrefSize(320, 440);
        setMaxSize(320, 440);
        getStyleClass().add("vm-selection-dialog");
        setEffect(new DropShadow(20, Color.valueOf("#00ffcc")));

        // Top Bar (สำหรับปุ่ม Close)
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.TOP_RIGHT); // ขวาบน
        Button closeButton = new Button("X");
        closeButton.getStyleClass().add("close-button");
        closeButton.setPrefSize(30, 30);
        closeButton.setOnAction(e -> closeDialog());
        topBar.getChildren().add(closeButton);

        // Label
        Label selectVMLabel = new Label("SELECT VM:");
        selectVMLabel.setFont(Font.font("Monospace", 18));
        selectVMLabel.getStyleClass().add("select-vm-label");

        // ComboBox
        vmComboBox = new ComboBox<>();
        vmComboBox.setMaxWidth(250);
        vmComboBox.getStyleClass().add("vm-combobox");
        vmComboBox.getItems().addAll(availableVMs);
        vmComboBox.setPromptText(availableVMs.isEmpty() ? "NO VM DETECTED" : "CHOOSE YOUR VM");

        // ปุ่ม Confirm (ใส่ใน HBox เพื่อจัดกึ่งกลางด้านล่าง)
        confirmButton = new Button("CONFIRM");
        confirmButton.setPrefWidth(120);
        confirmButton.getStyleClass().add("confirm-button");
        confirmButton.setOnAction(e -> {
            selectedVM = vmComboBox.getValue();
            if (selectedVM != null) {
                if (onConfirm != null) {
                    onConfirm.run();
                }
                closeDialog();
            }
        });

        // HBox สำหรับ Confirm เพื่อจัดกึ่งกลางด้านล่าง
        HBox confirmBox = new HBox();
        confirmBox.setAlignment(Pos.CENTER);
        confirmBox.getChildren().add(confirmButton);

        // เพิ่ม Spacer เพื่อดัน Confirm ลงล่าง
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS); // ทำให้ Spacer ขยายตัวดัน Confirm ลงล่าง

        // จัดเรียงองค์ประกอบ
        getChildren().addAll(topBar, selectVMLabel, vmComboBox, spacer, confirmBox);
        setSpacing(20);

        // Overlay
        Rectangle overlay = new Rectangle();
        overlay.setFill(Color.rgb(10, 10, 20, 0.85));
        overlay.widthProperty().bind(parentPane.widthProperty());
        overlay.heightProperty().bind(parentPane.heightProperty());

        // เพิ่มเข้า parentPane
        parentPane.getChildren().addAll(overlay, this);
        StackPane.setAlignment(this, Pos.CENTER);
    }

    public VPSOptimization.VM getSelectedVM() {
        return selectedVM;
    }

    private void closeDialog() {
        parentPane.getChildren().remove(this);
        parentPane.getChildren().remove(parentPane.getChildren().size() - 1);
    }

    public void setOnConfirm(Runnable onConfirm) {
        this.onConfirm = onConfirm;
    }
}
package com.vpstycoon.ui.game.desktop.messenger.views;

import com.vpstycoon.game.vps.VPSOptimization;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
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
        setAlignment(Pos.TOP_CENTER);
        setPadding(new Insets(25));
        setPrefSize(320, 440);
        setMaxSize(320, 440);
        getStyleClass().add("vm-selection-dialog");
        setEffect(new DropShadow(20, Color.valueOf("#00ffcc")));

        // Top Bar (สำหรับปุ่ม Close)
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.TOP_RIGHT);
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

        // ตั้งค่า CellFactory เพื่อแสดงรายละเอียด VM
        vmComboBox.setCellFactory(param -> new ListCell<VPSOptimization.VM>() {
            @Override
            protected void updateItem(VPSOptimization.VM vm, boolean empty) {
                super.updateItem(vm, empty);
                if (empty || vm == null) {
                    setText(null);
                } else {
                    setText(vm.getName() + " (" + vm.getIp() + ") - " +
                            vm.getVcpu() + " vCPUs, " + vm.getRam() + ", " + vm.getDisk());
                    setStyle("-fx-text-fill: white;");
                }
            }
        });

        vmComboBox.setButtonCell(new ListCell<VPSOptimization.VM>() {
            @Override
            protected void updateItem(VPSOptimization.VM vm, boolean empty) {
                super.updateItem(vm, empty);
                if (empty || vm == null) {
                    setText(null);
                } else {
                    setText(vm.getName() + " (" + vm.getIp() + ") - " +
                            vm.getVcpu() + " vCPUs, " + vm.getRam() + ", " + vm.getDisk());
                    setStyle("-fx-text-fill: white;");
                }
            }
        });

        // เพิ่มส่วน Rating Impact Preview (ส่วนที่ขาดหายไป)
        VBox ratingImpactBox = new VBox(5);
        ratingImpactBox.setStyle("-fx-background-color: rgba(46, 204, 113, 0.2); -fx-padding: 10px; -fx-border-radius: 5px; -fx-background-radius: 5px;");

        Label ratingImpactTitle = new Label("Rating Impact Preview:");
        ratingImpactTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #2ecc71;");

        Label ratingImpactLabel = new Label("Select a VM to see rating impact");
        ratingImpactLabel.setStyle("-fx-text-fill: white;");

        ratingImpactBox.getChildren().addAll(ratingImpactTitle, ratingImpactLabel);

        // Error Label (สำหรับกรณีที่ไม่เลือก VM)
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-opacity: 0;");

        // ปุ่ม Confirm (ใส่ใน HBox เพื่อจัดกึ่งกลางด้านล่าง)
        confirmButton = new Button("CONFIRM");
        confirmButton.setPrefWidth(120);
        confirmButton.getStyleClass().add("confirm-button");
        confirmButton.setOnAction(e -> {
            selectedVM = vmComboBox.getValue();
            if (selectedVM == null) {
                errorLabel.setText("Please select a VM to assign");
                errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-opacity: 1;");
                return;
            }
            if (onConfirm != null) {
                onConfirm.run();
                onConfirm.run();
            }
            closeDialog();
        });

        // HBox สำหรับ Confirm เพื่อจัดกึ่งกลางด้านล่าง
        HBox confirmBox = new HBox();
        confirmBox.setAlignment(Pos.CENTER);
        confirmBox.getChildren().add(confirmButton);

        // เพิ่ม Spacer เพื่อดัน Confirm ลงล่าง
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // จัดเรียงองค์ประกอบ
        getChildren().addAll(topBar, selectVMLabel, vmComboBox, new Separator(), ratingImpactBox, errorLabel, spacer, confirmBox);
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
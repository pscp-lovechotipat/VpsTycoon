package com.vpstycoon.ui.game.desktop.messenger.views;

import com.vpstycoon.game.vps.VPSOptimization;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.List;

public class VMSelectionDialog extends VBox {
    private ComboBox<VPSOptimization.VM> vmComboBox;
    private Button confirmButton;
    private VPSOptimization.VM selectedVM;
    private StackPane parentPane;
    private Runnable onConfirm;
    private Timeline glowAnimation;
    private Label vmSpecsLabel;
    private Label vmStatusLabel;
    private HBox vmInfoContainer;

    public VMSelectionDialog(List<VPSOptimization.VM> availableVMs, StackPane parentPane) {
        this.parentPane = parentPane;

        // โหลด CSS ภายนอก
        getStylesheets().add(getClass().getResource("/css/vmselect-modal.css").toExternalForm());

        // ตั้งค่าพื้นฐาน
        setAlignment(Pos.TOP_CENTER);
        setPadding(new Insets(25));
        setPrefSize(450, 500);
        setMaxSize(450, 500);
        getStyleClass().add("vm-selection-dialog");
        
        // เพิ่ม drop shadow effect
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.web("#9e33ff"));
        dropShadow.setRadius(15);
        dropShadow.setSpread(0.2);
        setEffect(dropShadow);

        // Top Bar with Cyberpunk style
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER);
        topBar.setPadding(new Insets(0, 0, 10, 0));
        topBar.setSpacing(10);
        topBar.setStyle("-fx-border-color: transparent transparent #9e33ff transparent; -fx-border-width: 0 0 1 0;");
        
        // สร้าง gradient icon แบบ cyberpunk
        Rectangle iconBg = new Rectangle(24, 24);
        iconBg.setArcWidth(5);
        iconBg.setArcHeight(5);
        
        // สร้าง Gradient สำหรับไอคอน
        Stop[] stops = new Stop[] {
            new Stop(0, Color.web("#00c3ff")),
            new Stop(1, Color.web("#9e33ff"))
        };
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
        iconBg.setFill(gradient);
        
        // เพิ่ม glow effect
        Glow glow = new Glow(0.8);
        iconBg.setEffect(glow);
        
        // เพิ่ม glow animation
        glowAnimation = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(glow.levelProperty(), 0.5)),
            new KeyFrame(Duration.seconds(1.5), new KeyValue(glow.levelProperty(), 0.8))
        );
        glowAnimation.setAutoReverse(true);
        glowAnimation.setCycleCount(Timeline.INDEFINITE);
        glowAnimation.play();
        
        // ข้อความใน icon
        Text vmText = new Text("VM");
        vmText.setFill(Color.WHITE);
        vmText.setStyle("-fx-font-weight: bold;");
        StackPane iconContainer = new StackPane(iconBg, vmText);
        
        // Title with cyberpunk style
        Label titleLabel = new Label("VM SELECTION SYSTEM");
        titleLabel.setStyle("-fx-text-fill: #00ffff; -fx-font-size: 16px; -fx-font-weight: bold; -fx-font-family: 'Monospace', 'Courier New', monospace;");
        titleLabel.setEffect(new Glow(0.4));
        
        // Close button 
        Button closeButton = new Button("×");
        closeButton.getStyleClass().add("close-button");
        closeButton.setPrefSize(30, 30);
        closeButton.setOnAction(e -> closeDialog());
                
        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.getChildren().addAll(iconContainer, titleLabel);
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        
        topBar.getChildren().addAll(titleBox, closeButton);

        // เพิ่ม cyber data header
        HBox statusBar = new HBox(10);
        statusBar.setAlignment(Pos.CENTER_RIGHT);
        
        Circle statusIndicator = new Circle(6);
        statusIndicator.setFill(Color.web("#00ff88"));
        statusIndicator.setEffect(new Glow(0.8));
        
        Label statusLabel = new Label("ONLINE | VPS COUNT: " + availableVMs.size());
        statusLabel.setStyle("-fx-text-fill: #00ff88; -fx-font-size: 10px; -fx-font-family: 'Monospace', 'Courier New', monospace;");
        
        statusBar.getChildren().addAll(statusLabel, statusIndicator);

        // VM Selection Label
        Label selectVMLabel = new Label("SELECT VM FROM INVENTORY:");
        selectVMLabel.getStyleClass().add("select-vm-label");

        // ComboBox with improved style
        vmComboBox = new ComboBox<>();
        vmComboBox.setMaxWidth(400);
        vmComboBox.getStyleClass().add("vm-combobox");
        vmComboBox.getItems().addAll(availableVMs);
        vmComboBox.setPromptText(availableVMs.isEmpty() ? "NO VM DETECTED" : "[ SELECT VM ]");

        // ตั้งค่า CellFactory เพื่อแสดงรายละเอียด VM แบบ cyberpunk
        vmComboBox.setCellFactory(param -> new ListCell<VPSOptimization.VM>() {
            @Override
            protected void updateItem(VPSOptimization.VM vm, boolean empty) {
                super.updateItem(vm, empty);
                if (empty || vm == null) {
                    setText(null);
                } else {
                    setText("VM: " + vm.getName().toUpperCase() + " | " + 
                            vm.getVcpu() + " vCPUs | " + vm.getRam() + " | " + vm.getDisk());
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
                    setText("VM: " + vm.getName().toUpperCase() + " | " + 
                            vm.getVcpu() + " vCPUs | " + vm.getRam() + " | " + vm.getDisk());
                }
            }
        });
        
        // VM ที่เลือกข้อมูลเพิ่มเติม
        vmInfoContainer = new HBox();
        vmInfoContainer.setVisible(false);
        vmInfoContainer.setAlignment(Pos.CENTER);
        vmInfoContainer.setPadding(new Insets(10));
        vmInfoContainer.getStyleClass().add("vm-info-panel");
        
        VBox vmDetailsBox = new VBox(5);
        vmDetailsBox.setAlignment(Pos.CENTER_LEFT);
        
        Label vmDetailsTitle = new Label("VM SPECIFICATIONS");
        vmDetailsTitle.getStyleClass().add("vm-info-title");
        
        vmSpecsLabel = new Label();
        vmSpecsLabel.getStyleClass().add("vm-info-value");
        
        vmStatusLabel = new Label();
        vmStatusLabel.getStyleClass().add("vm-info-value");
        vmStatusLabel.setStyle("-fx-text-fill: #00ff88;");
        
        vmDetailsBox.getChildren().addAll(vmDetailsTitle, vmSpecsLabel, vmStatusLabel);
        vmInfoContainer.getChildren().add(vmDetailsBox);
        
        // ComboBox change listener
        vmComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                vmSpecsLabel.setText(
                    "NAME: " + newVal.getName().toUpperCase() + "\n" +
                    "IP: " + newVal.getIp() + "\n" +
                    "CPU: " + newVal.getVcpu() + " vCPU(s)\n" +
                    "RAM: " + newVal.getRam() + "\n" +
                    "DISK: " + newVal.getDisk()
                );
                vmStatusLabel.setText("STATUS: READY FOR ASSIGNMENT");
                vmInfoContainer.setVisible(true);
            } else {
                vmInfoContainer.setVisible(false);
            }
        });

        // เพิ่ม digital lines สไตล์ cyberpunk
        Line leftLine = createDigitalLine();
        Line rightLine = createDigitalLine();
        
        HBox linesContainer = new HBox();
        linesContainer.setAlignment(Pos.CENTER);
        linesContainer.setSpacing(10);
        linesContainer.getChildren().addAll(leftLine, new Label("VM ASSIGNMENT"), rightLine);
        
        // Error label
        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setOpacity(0);

        // ปุ่ม Confirm ในสไตล์ cyberpunk 
        confirmButton = new Button("ASSIGN VM");
        confirmButton.setPrefWidth(180);
        confirmButton.getStyleClass().add("confirm-button");
        
        confirmButton.setOnAction(e -> {
            selectedVM = vmComboBox.getValue();
            if (selectedVM == null) {
                errorLabel.setText("[ERROR]: Please select a VM to assign");
                errorLabel.setOpacity(1);
                
                // แสดง error message แล้วเฟดออกอัตโนมัติ
                Timeline fadeOutTimeline = new Timeline(
                    new KeyFrame(Duration.seconds(3), new KeyValue(errorLabel.opacityProperty(), 0))
                );
                fadeOutTimeline.play();
                return;
            }
            if (onConfirm != null) {
                onConfirm.run();
            }
            closeDialog();
        });

        // HBox สำหรับ Confirm เพื่อจัดกึ่งกลางด้านล่าง
        HBox confirmBox = new HBox();
        confirmBox.setAlignment(Pos.CENTER);
        confirmBox.getChildren().addAll(confirmButton);

        // เพิ่ม Spacer เพื่อดัน Confirm ลงล่าง
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // จัดเรียงองค์ประกอบ
        getChildren().addAll(
            topBar, 
            statusBar,
            selectVMLabel, 
            vmComboBox, 
            vmInfoContainer,
            linesContainer,
            errorLabel, 
            spacer, 
            confirmBox
        );
        setSpacing(15);

        // Overlay พื้นหลังทึบแสงในสไตล์ cyberpunk
        Rectangle overlay = new Rectangle();
        overlay.setFill(Color.rgb(19, 26, 44, 0.9));
        overlay.widthProperty().bind(parentPane.widthProperty());
        overlay.heightProperty().bind(parentPane.heightProperty());

        // เพิ่มเข้า parentPane
        parentPane.getChildren().addAll(overlay, this);
        StackPane.setAlignment(this, Pos.CENTER);
    }
    
    // Helper method สำหรับสร้างเส้น digital line
    private Line createDigitalLine() {
        Line line = new Line(0, 0, 100, 0);
        line.setStroke(Color.web("#9e33ff"));
        line.setStrokeWidth(1);
        line.setOpacity(0.8);
        return line;
    }
    
    // Helper class สำหรับ status circle
    private static class Circle extends javafx.scene.shape.Circle {
        public Circle(double radius) {
            super(radius);
        }
    }

    public VPSOptimization.VM getSelectedVM() {
        return selectedVM;
    }

    private void closeDialog() {
        if (glowAnimation != null) {
            glowAnimation.stop();
        }
        parentPane.getChildren().remove(this);
        parentPane.getChildren().remove(parentPane.getChildren().size() - 1);
    }

    public void setOnConfirm(Runnable onConfirm) {
        this.onConfirm = onConfirm;
    }
}
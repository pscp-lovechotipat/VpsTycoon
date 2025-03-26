package com.vpstycoon.ui.game.desktop.messenger.views;

import com.vpstycoon.game.manager.CustomerRequest;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.effect.Glow;
import javafx.scene.effect.DropShadow;

import java.util.List;

public class RequestListView extends VBox {
    private ListView<CustomerRequest> requestView;

    public RequestListView() {
        setPadding(new Insets(10));
        setPrefWidth(450);
        getStyleClass().add("request-list");

        // Cyberpunk style header
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 10, 0));
        
        // ไอคอนสำหรับหัวข้อ
        Rectangle headerIcon = new Rectangle(20, 20);
        headerIcon.setArcWidth(5);
        headerIcon.setArcHeight(5);
        
        // สร้าง Gradient สำหรับไอคอน
        Stop[] stops = new Stop[] {
            new Stop(0, Color.web("#00c3ff")),
            new Stop(1, Color.web("#9e33ff"))
        };
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
        headerIcon.setFill(gradient);
        
        // เพิ่ม Glow effect
        Glow glow = new Glow(0.7);
        headerIcon.setEffect(glow);
        
        Label title = new Label("ACTIVE REQUESTS");
        title.getStyleClass().add("request-list-title");
        
        // เพิ่มข้อมูลเกี่ยวกับจำนวนคำขอ
        Label countLabel = new Label("[ 0 ]");
        countLabel.setStyle("-fx-text-fill: #00ffff; -fx-font-size: 12px; -fx-font-family: 'Monospace', 'Courier New', monospace;");
        
        headerBox.getChildren().addAll(headerIcon, title, countLabel);
        HBox.setHgrow(title, Priority.ALWAYS);

        // เพิ่ม Digital Border
        String digitalBorderStyle = "-fx-border-color: #9e33ff; -fx-border-width: 1; " +
                                    "-fx-border-radius: 5; -fx-effect: dropshadow(gaussian, #9e33ff, 5, 0.3, 0, 0);";

        // สร้าง ListView ด้วย custom cell factory
        requestView = new ListView<>();
        requestView.getStyleClass().add("request-list-view");
        requestView.setStyle(requestView.getStyle() + digitalBorderStyle);

        // Cyberpunk style cell factory
        requestView.setCellFactory(param -> new ListCell<CustomerRequest>() {
            @Override
            protected void updateItem(CustomerRequest request, boolean empty) {
                super.updateItem(request, empty);
                if (empty || request == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox cellContent = new HBox(10);
                    cellContent.setAlignment(Pos.CENTER_LEFT);
                    cellContent.setPadding(new Insets(5));

                    // เปลี่ยนจาก Circle เป็น Rectangle แบบ cyberpunk
                    Rectangle avatar = new Rectangle(30, 30);
                    avatar.setArcWidth(5);
                    avatar.setArcHeight(5);
                    
                    int nameHash = request.getName().hashCode();
                    int r = Math.abs(nameHash % 100) + 100;
                    int g = Math.abs((nameHash / 100) % 100);
                    int b = Math.abs((nameHash / 10000) % 100) + 100;
                    Color customerColor = Color.rgb(r, g, b);
                    avatar.setFill(customerColor);
                    avatar.setStroke(Color.web("#9e33ff"));
                    avatar.setStrokeWidth(1);
                    
                    // ใส่ Drop Shadow Effect
                    DropShadow dropShadow = new DropShadow();
                    dropShadow.setColor(Color.web("#9e33ff", 0.5));
                    dropShadow.setRadius(10);
                    avatar.setEffect(dropShadow);

                    VBox textContent = new VBox(3);
                    HBox nameStatusBox = new HBox(5);
                    nameStatusBox.setAlignment(Pos.CENTER_LEFT);

                    // เปลี่ยนชื่อให้เป็นตัวพิมพ์ใหญ่ทั้งหมดตามสไตล์ cyberpunk
                    Label nameLabel = new Label(request.getTitle().toUpperCase());
                    nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #E0FFFF; -fx-font-family: 'Monospace', 'Courier New', monospace;");

                    Label statusLabel = new Label();
                    statusLabel.setStyle("-fx-font-size: 10px; -fx-padding: 2 5; -fx-background-radius: 3;");
                    if (request.isActive()) {
                        statusLabel.setText("✓ ASSIGNED");
                        statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #2ecc71; -fx-text-fill: white;");
                    } else if (request.isExpired()) {
                        statusLabel.setText("⏱ EXPIRED");
                        statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #e74c3c; -fx-text-fill: white;");
                    } else {
                        statusLabel.setText("⌛ WAITING");
                        statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #3498db; -fx-text-fill: white;");
                    }

                    nameStatusBox.getChildren().addAll(nameLabel, statusLabel);

                    // เปลี่ยนรูปแบบการแสดงผลข้อมูล VM ใน cyberpunk style
                    Label previewLabel = new Label("SPECS: [ " + request.getRequiredVCPUs() + " vCPU | " +
                            request.getRequiredRam() + " RAM ]");
                    previewLabel.setStyle("-fx-text-fill: #00ffff; -fx-font-size: 11px; -fx-font-family: 'Monospace', 'Courier New', monospace;");

                    // เปลี่ยนจากการใช้ getRatePerHour() เป็นข้อความทั่วไป
                    Label rateLabel = new Label("PRIORITY: HIGH");
                    rateLabel.setStyle("-fx-text-fill: #00ff88; -fx-font-size: 11px; -fx-font-family: 'Monospace', 'Courier New', monospace;");

                    textContent.getChildren().addAll(nameStatusBox, previewLabel, rateLabel);

                    // ไอคอนแสดงสถานะ
                    Circle statusIndicator = new Circle(5);
                    
                    // เพิ่ม Glow effect ให้กับ statusIndicator
                    Glow statusGlow = new Glow(0.8);
                    statusIndicator.setEffect(statusGlow);
                    
                    if (request.isActive()) {
                        statusIndicator.setFill(Color.rgb(0, 255, 128));
                    } else if (request.isExpired()) {
                        statusIndicator.setFill(Color.rgb(255, 77, 77));
                    } else {
                        statusIndicator.setFill(Color.rgb(0, 200, 255));
                    }
                    
                    HBox.setMargin(statusIndicator, new Insets(0, 0, 0, 5));

                    cellContent.getChildren().addAll(avatar, textContent, statusIndicator);
                    HBox.setHgrow(textContent, Priority.ALWAYS);

                    setGraphic(cellContent);
                    setText(null);
                }
            }
        });

        getChildren().addAll(headerBox, requestView);
        VBox.setVgrow(requestView, Priority.ALWAYS);
    }

    public void updateRequestList(List<CustomerRequest> requests) {
        requestView.getItems().clear();
        requestView.getItems().addAll(requests);
        
        // อัปเดตจำนวนคำขอในหัวข้อ
        Label countLabel = (Label) ((HBox) getChildren().get(0)).getChildren().get(2);
        countLabel.setText("[ " + requests.size() + " ]");
    }

    public CustomerRequest getSelectedRequest() {
        return requestView.getSelectionModel().getSelectedItem();
    }

    public ListView<CustomerRequest> getRequestView() {
        return requestView;
    }
}
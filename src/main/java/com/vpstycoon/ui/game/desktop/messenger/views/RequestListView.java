package com.vpstycoon.ui.game.desktop.messenger.views;

import com.vpstycoon.game.manager.CustomerRequest;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.util.List;

public class RequestListView extends VBox {
    private ListView<CustomerRequest> requestView;

    public RequestListView() {
        setPadding(new Insets(10));
        setPrefWidth(450);
        getStyleClass().add("request-list");

        
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 10, 0));
        
        
        Rectangle headerIcon = new Rectangle(20, 20);
        headerIcon.setArcWidth(5);
        headerIcon.setArcHeight(5);
        
        
        Stop[] stops = new Stop[] {
            new Stop(0, Color.web("#00c3ff")),
            new Stop(1, Color.web("#9e33ff"))
        };
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
        headerIcon.setFill(gradient);
        
        
        Glow glow = new Glow(0.7);
        headerIcon.setEffect(glow);
        
        Label title = new Label("ACTIVE REQUESTS");
        title.getStyleClass().add("request-list-title");
        
        
        Label countLabel = new Label("[ 0 ]");
        countLabel.setStyle("-fx-text-fill: #00ffff; -fx-font-size: 12px; -fx-font-family: 'Monospace', 'Courier New', monospace;");
        
        headerBox.getChildren().addAll(headerIcon, title, countLabel);
        HBox.setHgrow(title, Priority.ALWAYS);

        
        String digitalBorderStyle = "-fx-border-color: #9e33ff; -fx-border-width: 1; " +
                                    "-fx-border-radius: 5; -fx-effect: dropshadow(gaussian, #9e33ff, 5, 0.3, 0, 0);";

        
        requestView = new ListView<>();
        requestView.getStyleClass().add("request-list-view");
        requestView.setStyle(requestView.getStyle() + digitalBorderStyle);

        
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
                    cellContent.setPadding(new Insets(8));
                    cellContent.setMinHeight(95);

                    
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
                    
                    
                    DropShadow dropShadow = new DropShadow();
                    dropShadow.setColor(Color.web("#9e33ff", 0.5));
                    dropShadow.setRadius(10);
                    avatar.setEffect(dropShadow);

                    VBox textContent = new VBox(5);
                    textContent.setPrefWidth(350);
                    HBox nameStatusBox = new HBox(5);
                    nameStatusBox.setAlignment(Pos.CENTER_LEFT);

                    
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

                    
                    Label previewLabel = new Label("SPECS: [ " + request.getRequiredVCPUs() + " vCPU | " +
                            request.getRequiredRam() + " RAM | " + request.getRequiredDisk() + " DISK ]");
                    previewLabel.setStyle("-fx-text-fill: #00ffff; -fx-font-size: 11px; -fx-font-family: 'Monospace', 'Courier New', monospace;");

                    
                    Label requestTypeLabel = new Label("TYPE: [ " + request.getRequestType() + " | " + 
                            request.getRentalPeriodType().getDisplayName() + " | " + 
                            request.getDuration() + " DAYS ]");
                    requestTypeLabel.setStyle("-fx-text-fill: #00ff88; -fx-font-size: 11px; -fx-font-family: 'Monospace', 'Courier New', monospace;");

                    
                    Label rateLabel = new Label("RATE: [ $" + String.format("%.2f", request.getMonthlyPayment()) + " / MONTH ]");
                    rateLabel.setStyle("-fx-text-fill: #ffcc00; -fx-font-size: 11px; -fx-font-family: 'Monospace', 'Courier New', monospace;");

                    textContent.getChildren().addAll(nameStatusBox, previewLabel, requestTypeLabel, rateLabel);

                    
                    Circle statusIndicator = new Circle(5);
                    
                    
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
        
        System.out.println("โหลดข้อมูล CustomerRequest จำนวน: " + (requests != null ? requests.size() : 0) + " รายการ");
        
        if (requests == null || requests.isEmpty()) {
            System.out.println("ไม่พบข้อมูล CustomerRequest");
            
            Label countLabel = (Label) ((HBox) getChildren().get(0)).getChildren().get(2);
            countLabel.setText("[ 0 ]");
            
            requestView.setPlaceholder(new Label("ไม่มีคำขอที่รอดำเนินการ"));
            return;
        }
        
        try {
            requestView.getItems().addAll(requests);
            
            int validCount = 0;
            for (CustomerRequest request : requests) {
                if (request != null && request.getTitle() != null && request.getRequiredVCPUs() > 0) {
                    validCount++;
                } else {
                    System.out.println("พบข้อมูล CustomerRequest ที่ไม่สมบูรณ์: " + 
                        (request != null && request.getTitle() != null ? request.getTitle() : "null"));
                }
            }
            System.out.println("จำนวน CustomerRequest ที่สมบูรณ์: " + validCount + "/" + requests.size());
            
            Label countLabel = (Label) ((HBox) getChildren().get(0)).getChildren().get(2);
            countLabel.setText("[ " + requests.size() + " ]");
            
            if (!requests.isEmpty()) {
                requestView.getSelectionModel().select(0);
            }
        } catch (Exception e) {
            System.err.println("เกิดข้อผิดพลาดในการอัปเดตรายการคำขอ: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public CustomerRequest getSelectedRequest() {
        return requestView.getSelectionModel().getSelectedItem();
    }

    public ListView<CustomerRequest> getRequestView() {
        return requestView;
    }
}


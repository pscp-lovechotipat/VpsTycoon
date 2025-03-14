package com.vpstycoon.ui.game.desktop.messenger;

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
import javafx.scene.shape.Circle;

import java.util.List;

public class RequestListView extends VBox {
    private ListView<CustomerRequest> requestView;

    public RequestListView() {
        setPadding(new Insets(10));
        setPrefWidth(350);
        getStyleClass().add("request-list");

        Label title = new Label("Customer Requests");
        title.getStyleClass().add("request-list-title");

        requestView = new ListView<>();
        requestView.getStyleClass().add("request-list-view");

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

                    Circle avatar = new Circle(15);
                    int nameHash = request.getName().hashCode();
                    int r = Math.abs(nameHash % 100) + 100;
                    int g = Math.abs((nameHash / 100) % 100);
                    int b = Math.abs((nameHash / 10000) % 100) + 100;
                    Color customerColor = Color.rgb(r, g, b);
                    avatar.setFill(customerColor);

                    VBox textContent = new VBox(3);
                    HBox nameStatusBox = new HBox(5);
                    nameStatusBox.setAlignment(Pos.CENTER_LEFT);

                    Label nameLabel = new Label(request.getTitle());
                    nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-family: 'Monospace', 'Courier New', monospace;");

                    Label statusLabel = new Label();
                    statusLabel.setStyle("-fx-font-size: 10px; -fx-padding: 2 5; -fx-background-radius: 3;");
                    if (request.isActive()) {
                        statusLabel.setText("✓ Assigned");
                        statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #2ecc71; -fx-text-fill: white;");
                    } else if (request.isExpired()) {
                        statusLabel.setText("⏱ Expired");
                        statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #e74c3c; -fx-text-fill: white;");
                    } else {
                        statusLabel.setText("⌛ Waiting");
                        statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #3498db; -fx-text-fill: white;");
                    }

                    nameStatusBox.getChildren().addAll(nameLabel, statusLabel);

                    Label previewLabel = new Label("Needs VM: " + request.getRequiredVCPUs() + " vCPUs, " +
                            request.getRequiredRam() + " RAM");
                    previewLabel.setStyle("-fx-text-fill: rgba(255, 255, 255, 0.7); -fx-font-size: 12px; -fx-font-family: 'Monospace', 'Courier New', monospace;");

                    textContent.getChildren().addAll(nameStatusBox, previewLabel);

                    Circle statusIndicator = new Circle(5);
                    statusIndicator.setFill(request.isActive() ? Color.rgb(0, 255, 128) : Color.rgb(0, 200, 255));
                    HBox.setMargin(statusIndicator, new Insets(0, 0, 0, 5));

                    cellContent.getChildren().addAll(avatar, textContent, statusIndicator);
                    HBox.setHgrow(textContent, Priority.ALWAYS);

                    setGraphic(cellContent);
                    setText(null);
                }
            }
        });

        getChildren().addAll(title, requestView);
        VBox.setVgrow(requestView, Priority.ALWAYS);
    }

    public void updateRequestList(List<CustomerRequest> requests) {
        requestView.getItems().clear();
        requestView.getItems().addAll(requests);
    }

    public CustomerRequest getSelectedRequest() {
        return requestView.getSelectionModel().getSelectedItem();
    }

    public ListView<CustomerRequest> getRequestView() {
        return requestView;
    }
}
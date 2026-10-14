package com.vpstycoon.ui.game.desktop.messenger.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class DashboardView extends VBox {
    private Label ratingLabel;
    private Label activeRequestsLabel;
    private Label availableVMsLabel;
    private Label totalVPSLabel;

    public DashboardView() {
        setPadding(new Insets(10));
        getStyleClass().add("dashboard");
        setMaxHeight(120);

        Label title = new Label("SYSTEM DASHBOARD");
        title.getStyleClass().add("dashboard-title");

        GridPane dashboardGrid = new GridPane();
        dashboardGrid.getStyleClass().add("dashboard-grid");
        dashboardGrid.setAlignment(Pos.CENTER);

        VBox ratingCard = createCard("RATING");
        ratingLabel = (Label) ratingCard.getChildren().get(0);

        VBox requestsCard = createCard("REQUESTS");
        activeRequestsLabel = (Label) requestsCard.getChildren().get(0);

        VBox availableVMsCard = createCard("FREE VMs");
        availableVMsLabel = (Label) availableVMsCard.getChildren().get(0);

        VBox totalVPSCard = createCard("TOTAL SERVERS");
        totalVPSLabel = (Label) totalVPSCard.getChildren().get(0);

        dashboardGrid.add(ratingCard, 0, 0);
        dashboardGrid.add(requestsCard, 1, 0);
        dashboardGrid.add(availableVMsCard, 2, 0);
        dashboardGrid.add(totalVPSCard, 3, 0);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(25);
        dashboardGrid.getColumnConstraints().addAll(col1, col1, col1, col1);

        getChildren().addAll(title, dashboardGrid);
    }

    private VBox createCard(String desc) {
        VBox card = new VBox(5);
        card.getStyleClass().add("dashboard-card");
        card.setAlignment(Pos.CENTER);

        Label valueLabel = new Label("0");
        valueLabel.getStyleClass().add("dashboard-value");

        Label descLabel = new Label(desc);
        descLabel.getStyleClass().add("dashboard-label");

        card.getChildren().addAll(valueLabel, descLabel);
        return card;
    }

    public void updateDashboard(double rating, int activeRequests, int availableVMs, int totalVPS) {
        ratingLabel.setText(String.format("%.1f", rating));
        ratingLabel.getStyleClass().removeAll("rating-high", "rating-medium", "rating-low");
        if (rating >= 4.0) {
            ratingLabel.getStyleClass().add("rating-high");
        } else if (rating >= 3.0) {
            ratingLabel.getStyleClass().add("rating-medium");
        } else {
            ratingLabel.getStyleClass().add("rating-low");
        }

        activeRequestsLabel.setText(String.valueOf(activeRequests));
        availableVMsLabel.setText(String.valueOf(availableVMs));
        totalVPSLabel.setText(String.valueOf(totalVPS));
    }
}

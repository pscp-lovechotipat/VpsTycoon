package com.vpstycoon.ui.game.desktop;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Random;

public class DashboardWindow extends VBox {
    private final double companyRating;
    private final int marketingPoints;
    private final double monthlyRevenue;
    private final Runnable onClose;

    public DashboardWindow(double companyRating, int marketingPoints, double monthlyRevenue, Runnable onClose) {
        this.companyRating = companyRating;
        this.marketingPoints = marketingPoints;
        this.monthlyRevenue = monthlyRevenue;
        this.onClose = onClose;

        setupUI();
        styleWindow();
    }

    private void setupUI() {
        setPrefSize(600, 400);

        // Title Bar
        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_RIGHT);
        titleBar.setPadding(new Insets(5, 10, 5, 10));
        titleBar.setStyle("-fx-background-color: #4CAF50;");

        Button closeButton = new Button("X");
        closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
        closeButton.setOnAction(e -> onClose.run());

        titleBar.getChildren().add(closeButton);

        // Dashboard Title
        Label titleLabel = new Label("Dashboard");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: black;");

        // Stats
        VBox statsContainer = new VBox(10);
        statsContainer.setPadding(new Insets(10));
        statsContainer.getChildren().addAll(
                createStatLabel("Company Rating: ", String.format("%.2f ★", companyRating)),
                createStatLabel("Marketing Points: ", marketingPoints + " MP"),
                createStatLabel("Monthly Revenue: ", String.format("$%.2f", monthlyRevenue))
        );

        // Add Revenue Chart
        LineChart<String, Number> revenueChart = createRevenueChart();

        VBox content = new VBox(10, titleLabel, statsContainer, revenueChart);
        content.setPadding(new Insets(10));
        VBox.setVgrow(content, Priority.ALWAYS);

        getChildren().addAll(titleBar, content);
    }

    private HBox createStatLabel(String label, String value) {
        HBox statRow = new HBox(10);
        statRow.setAlignment(Pos.CENTER_LEFT);

        Label statLabel = new Label(label);
        statLabel.setStyle("-fx-font-weight: bold;");

        Label statValue = new Label(value);
        statValue.setStyle("-fx-text-fill: blue;");

        statRow.getChildren().addAll(statLabel, statValue);
        return statRow;
    }

    private LineChart<String, Number> createRevenueChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Month");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Revenue ($)");

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Monthly Revenue Growth");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenue");

        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

        // Mock Revenue Data (You can replace this with real revenue from vpsManager)
        Random random = new Random();
        double revenue = 1000.0;
        for (String month : months) {
            revenue += random.nextDouble() * 500 - 250; // Simulate increase/decrease
            series.getData().add(new XYChart.Data<>(month, revenue));
        }

        lineChart.getData().add(series);
        return lineChart;
    }

    private void styleWindow() {
        setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0);");
    }
}

package com.vpstycoon.ui.game.desktop;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
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
        setPrefSize(700, 500); // Slightly larger for better graph visibility

        // Title Bar
        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_RIGHT);
        titleBar.setPadding(new Insets(8, 15, 8, 15));
        titleBar.setStyle("-fx-background-color: linear-gradient(to right, #2c3e50, #3498db);");

        Label titleLabel = new Label("Business Dashboard");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-text-fill: white; -fx-font-weight: bold;");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        titleLabel.setAlignment(Pos.CENTER_LEFT);

        Button closeButton = new Button("✕");
        closeButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 2 8;");
        closeButton.setOnAction(e -> onClose.run());

        titleBar.getChildren().addAll(titleLabel, closeButton);

        // Stats Section
        HBox statsContainer = new HBox(20);
        statsContainer.setPadding(new Insets(15));
        statsContainer.setStyle("-fx-background-color: #ecf0f1; -fx-border-radius: 5; -fx-background-radius: 5;");
        statsContainer.getChildren().addAll(
                createStatCard("Company Rating", String.format("%.2f ★", companyRating), "#e67e22"),
                createStatCard("Marketing Points", marketingPoints + " MP", "#8e44ad"),
                createStatCard("Monthly Revenue", String.format("$%.2f", monthlyRevenue), "#27ae60")
        );

        // Graphs
        LineChart<String, Number> revenueChart = createRevenueChart();
        LineChart<String, Number> customerChart = createCustomerAcquisitionChart();

        VBox graphsContainer = new VBox(20, revenueChart, customerChart);
        graphsContainer.setPadding(new Insets(15));
        VBox.setVgrow(graphsContainer, Priority.ALWAYS);

        getChildren().addAll(titleBar, statsContainer, graphsContainer);
    }

    private VBox createStatCard(String label, String value, String color) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: white; -fx-border-color: " + color + "; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        Label statLabel = new Label(label);
        statLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        Label statValue = new Label(value);
        statValue.setStyle("-fx-font-size: 18px; -fx-text-fill: " + color + "; -fx-font-weight: bold;");

        card.getChildren().addAll(statLabel, statValue);
        return card;
    }

    private LineChart<String, Number> createRevenueChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Month");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Revenue ($)");

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Monthly Revenue Growth");
        lineChart.setStyle("-fx-background-color: white; -fx-border-radius: 5; -fx-background-radius: 5;");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenue");

        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        Random random = new Random();
        double revenue = 1000.0;
        for (String month : months) {
            revenue += random.nextDouble() * 500 - 250;
            XYChart.Data<String, Number> data = new XYChart.Data<>(month, revenue);
            data.setNode(new Label(String.format("$%.2f", revenue))); // Tooltip-like effect
            Tooltip.install(data.getNode(), new Tooltip(String.format("$%.2f", revenue)));
            series.getData().add(data);
        }

        lineChart.getData().add(series);
        return lineChart;
    }

    private LineChart<String, Number> createCustomerAcquisitionChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Month");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("New Customers");

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Customer Acquisition Over Time");
        lineChart.setStyle("-fx-background-color: white; -fx-border-radius: 5; -fx-background-radius: 5;");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("New Customers");

        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        Random random = new Random();
        int customers = 50;
        for (String month : months) {
            customers += random.nextInt(20) - 5; // Simulate customer growth
            XYChart.Data<String, Number> data = new XYChart.Data<>(month, customers);
            data.setNode(new Label(customers + " customers"));
            Tooltip.install(data.getNode(), new Tooltip(customers + " new customers"));
            series.getData().add(data);
        }

        lineChart.getData().add(series);
        return lineChart;
    }

    private void styleWindow() {
        setStyle("-fx-background-color: #f5f6fa; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 5); -fx-border-radius: 10; -fx-background-radius: 10;");
    }
}
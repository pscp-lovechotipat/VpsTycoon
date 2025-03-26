package com.vpstycoon.ui.game.desktop;

import com.vpstycoon.game.vps.VPSOptimization;
import javafx.application.Platform;
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
import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.game.manager.VPSManager;
import com.vpstycoon.game.manager.RequestManager;

public class DashboardWindow extends VBox {
    private Company company;
    private ResourceManager resourceManager;
    private VPSManager vpsManager;
    private RequestManager requestManager;
    private final Runnable onClose;

    // UI components that need to be updated
    private Label statRatingValue;
    private Label statMarketingValue;
    private Label statRevenueValue;
    private Label statPointsValue;
    private Label statMoneyValue;

    private LineChart<String, Number> revenueChart;
    private XYChart.Series<String, Number> revenueSeries;
    private XYChart.Series<String, Number> expenseSeries;
    private XYChart.Series<String, Number> profitSeries;

    private LineChart<String, Number> performanceChart;
    private XYChart.Series<String, Number> cpuSeries;
    private XYChart.Series<String, Number> ramSeries;

    private XYChart.Series<String, Number> diskSeries;

    // Statistics variables
    private Label statVPSValue;
    private Label statVMValue;
    private Label statCustomerValue;
    private Label statAvgRatingValue;
    private Label statUptimeValue;

    public DashboardWindow(Company company, VPSManager vpsManager, RequestManager requestManager, Runnable onClose) {
        this.company = company;
        this.resourceManager = ResourceManager.getInstance();
        this.vpsManager = vpsManager;
        this.requestManager = requestManager;
        this.onClose = onClose;

        setupUI();
        styleWindow();
        startDataUpdates();
    }

    private void setupUI() {
        setPrefSize(700, 500);

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

        // Stats Section - First Row
        HBox statsContainer = new HBox(15);
        statsContainer.setPadding(new Insets(15, 15, 5, 15));
        statsContainer.setStyle("-fx-background-color: #ecf0f1; -fx-border-radius: 5; -fx-background-radius: 5;");

        VBox ratingCard = createStatCard("Company Rating", String.format("%.2f ★", company.getRating()), "#e67e22");
        statRatingValue = (Label) ratingCard.getChildren().get(1);

        VBox marketingCard = createStatCard("Marketing Points", company.getMarketingPoints() + " MP", "#8e44ad");
        statMarketingValue = (Label) marketingCard.getChildren().get(1);

        VBox revenueCard = createStatCard("Monthly Revenue", String.format("$%.2f", calculateMonthlyRevenue()), "#27ae60");
        statRevenueValue = (Label) revenueCard.getChildren().get(1);

        VBox pointsCard = createStatCard("Skill Points", company.getSkillPointsAvailable() + " SP", "#3498db");
        statPointsValue = (Label) pointsCard.getChildren().get(1);

        VBox moneyCard = createStatCard("Total Money", "$" + company.getMoney(), "#e74c3c");
        statMoneyValue = (Label) moneyCard.getChildren().get(1);

        statsContainer.getChildren().addAll(ratingCard, marketingCard, revenueCard, pointsCard, moneyCard);

        // Stats Section - Second Row
        HBox statsContainer2 = new HBox(15);
        statsContainer2.setPadding(new Insets(5, 15, 15, 15));
        statsContainer2.setStyle("-fx-background-color: #ecf0f1; -fx-border-radius: 5; -fx-background-radius: 5;");

        VBox vpsCard = createStatCard("Total Servers", String.valueOf(vpsManager.getVPSMap().size()), "#2980b9");
        statVPSValue = (Label) vpsCard.getChildren().get(1);

        VBox vmCard = createStatCard("Total VMs", String.valueOf(calculateTotalVMs()), "#16a085");
        statVMValue = (Label) vmCard.getChildren().get(1);

        VBox customerCard = createStatCard("Customers", String.valueOf(requestManager.getRequests().size()), "#f39c12");
        statCustomerValue = (Label) customerCard.getChildren().get(1);

        double avgRating = calculateAverageRating();
        VBox ratingAvgCard = createStatCard("Avg. Rating", String.format("%.1f ★", avgRating), "#c0392b");
        statAvgRatingValue = (Label) ratingAvgCard.getChildren().get(1);

        VBox uptimeCard = createStatCard("Uptime", String.format("%.2f%%", calculateUptime()), "#2c3e50");
        statUptimeValue = (Label) uptimeCard.getChildren().get(1);

        statsContainer2.getChildren().addAll(vpsCard, vmCard, customerCard, ratingAvgCard, uptimeCard);

        // Graphs Container
        VBox graphsContainer = new VBox(15);
        graphsContainer.setPadding(new Insets(15));
        VBox.setVgrow(graphsContainer, Priority.ALWAYS);

        revenueChart = createRevenueChart();
        performanceChart = createPerformanceChart();

        HBox chartsRow = new HBox(15, revenueChart, performanceChart);
        HBox.setHgrow(revenueChart, Priority.ALWAYS);
        HBox.setHgrow(performanceChart, Priority.ALWAYS);

        graphsContainer.getChildren().add(chartsRow);

        getChildren().addAll(titleBar, statsContainer, statsContainer2, graphsContainer);
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
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time");
        yAxis.setLabel("Amount (THB)");

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Financial Performance");
        chart.setAnimated(false);
        chart.setCreateSymbols(false);

        revenueSeries = new XYChart.Series<>();
        revenueSeries.setName("Revenue");

        expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Expenses");

        profitSeries = new XYChart.Series<>();
        profitSeries.setName("Profit");

        for (int i = 0; i < 10; i++) {
            revenueSeries.getData().add(new XYChart.Data<>("Day " + i, 0));
            expenseSeries.getData().add(new XYChart.Data<>("Day " + i, 0));
            profitSeries.getData().add(new XYChart.Data<>("Day " + i, 0));
        }

        chart.getData().add(revenueSeries);
        chart.getData().add(expenseSeries);
        chart.getData().add(profitSeries);

        return chart;
    }

    private LineChart<String, Number> createPerformanceChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis(0, 100, 10);
        xAxis.setLabel("Time");
        yAxis.setLabel("Usage (%)");

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("System Performance");
        chart.setAnimated(false);
        chart.setCreateSymbols(false);

        cpuSeries = new XYChart.Series<>();
        cpuSeries.setName("CPU");

        ramSeries = new XYChart.Series<>();
        ramSeries.setName("RAM");

        diskSeries = new XYChart.Series<>();
        diskSeries.setName("Disk");

        for (int i = 0; i < 10; i++) {
            cpuSeries.getData().add(new XYChart.Data<>("Day " + i, 0));
            ramSeries.getData().add(new XYChart.Data<>("Day " + i, 0));
            diskSeries.getData().add(new XYChart.Data<>("Day " + i, 0));
        }

        chart.getData().add(cpuSeries);
        chart.getData().add(ramSeries);
        chart.getData().add(diskSeries);

        return chart;
    }

    private void styleWindow() {
        setStyle("-fx-background-color: #f5f6fa; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 5); -fx-border-radius: 10; -fx-background-radius: 10;");
    }

    // Helper methods to calculate real values
    private double calculateMonthlyRevenue() {
        return requestManager.getRequests().stream()
                .filter(req -> req.isActive())
                .mapToDouble(req -> req.getRequiredVCPUs() * 10.0 + Double.parseDouble(req.getRequiredRam()) * 5.0 + Double.parseDouble(req.getRequiredDisk()) * 2.0)
                .sum();
    }

    private int calculateTotalVMs() {
        return vpsManager.getVPSMap().values().stream()
                .mapToInt(vps -> vps.getVms().size())
                .sum();
    }

    private double calculateAverageRating() {
        if (requestManager.getRequests().isEmpty()) return company.getRating();
        double totalRating = requestManager.getRequests().stream()
                .mapToDouble(req -> company.getRating())
                .sum();
        return totalRating / Math.max(1, requestManager.getRequests().size());
    }

    private double calculateUptime() {
        int totalVMs = calculateTotalVMs();
        if (totalVMs == 0) return 100.0;
        long runningVMs = vpsManager.getVPSMap().values().stream()
                .flatMap(vps -> vps.getVms().stream())
                .filter(vm -> "Running".equals(vm.getStatus()))
                .count();
        return (runningVMs * 100.0) / totalVMs;
    }

    public void updateDashboard() {
        Platform.runLater(() -> {
            // Update UI components - Main stats
            statRatingValue.setText(String.format("%.2f ★", company.getRating()));
            statMarketingValue.setText(company.getMarketingPoints() + " MP");
            double monthlyRevenue = calculateMonthlyRevenue();
            statRevenueValue.setText(String.format("$%.2f", monthlyRevenue));
            statPointsValue.setText(company.getSkillPointsAvailable() + " SP");
            statMoneyValue.setText("$" + company.getMoney());

            // Update UI components - Additional stats
            statVPSValue.setText(String.valueOf(vpsManager.getVPSMap().size()));
            statVMValue.setText(String.valueOf(calculateTotalVMs()));
            statCustomerValue.setText(String.valueOf(requestManager.getRequests().size()));
            statAvgRatingValue.setText(String.format("%.1f ★", calculateAverageRating()));
            statUptimeValue.setText(String.format("%.2f%%", calculateUptime()));

            // Update charts with new data
            updateChartData(monthlyRevenue);
        });
    }

    private void startDataUpdates() {
        Thread updateThread = new Thread(() -> {
            try {
                while (true) {
                    Thread.sleep(3000);
                    updateDashboard();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        updateThread.setDaemon(true);
        updateThread.start();
    }

    private void updateChartData(double monthlyRevenue) {
        if (revenueSeries.getData().size() >= 10) {
            revenueSeries.getData().remove(0);
            expenseSeries.getData().remove(0);
            profitSeries.getData().remove(0);
        }

        double expenses = monthlyRevenue * 0.6;
        double profit = monthlyRevenue - expenses;

        String timeStamp = "Day " + (System.currentTimeMillis() / 1000 % 1000);
        revenueSeries.getData().add(new XYChart.Data<>(timeStamp, monthlyRevenue));
        expenseSeries.getData().add(new XYChart.Data<>(timeStamp, expenses));
        profitSeries.getData().add(new XYChart.Data<>(timeStamp, profit));

        if (cpuSeries.getData().size() >= 10) {
            cpuSeries.getData().removeFirst();
            ramSeries.getData().removeFirst();
            diskSeries.getData().removeFirst();
        }

        double avgCpuUsage = vpsManager.getVPSMap().values().stream()
                .flatMap(vps -> vps.getVms().stream())
                .mapToDouble(VPSOptimization.VM::getVcpu)
                .average().orElse(50.0);

        double avgRamUsage = vpsManager.getVPSMap().values().stream()
                .flatMap(vps -> vps.getVms().stream())
                .mapToDouble(vm -> Double.parseDouble(vm.getRam().split(" ")[0]))
                .average().orElse(50.0);

        double avgDiskUsage = vpsManager.getVPSMap().values().stream()
                .flatMap(vps -> vps.getVms().stream())
                .mapToDouble(vm -> Double.parseDouble(vm.getDisk()))
                .average().orElse(50.0);

        cpuSeries.getData().add(new XYChart.Data<>(timeStamp, avgCpuUsage));
        ramSeries.getData().add(new XYChart.Data<>(timeStamp, avgRamUsage));
        diskSeries.getData().add(new XYChart.Data<>(timeStamp, avgDiskUsage));
    }
}
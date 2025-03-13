package com.vpstycoon.ui.game.desktop;

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

import java.util.Random;

public class DashboardWindow extends VBox {
    private double companyRating;
    private int marketingPoints;
    private double monthlyRevenue;
    private int skillPoints;
    private long totalMoney;
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
    
    // เพิ่มตัวแปรสำหรับเก็บข้อมูลสถิติ
    private int totalVPS;
    private int totalVMs;
    private int totalCustomers;
    private double averageRating;

    // New variables for the second row of statistics cards
    private Label statVPSValue;
    private Label statVMValue;
    private Label statCustomerValue;
    private Label statAvgRatingValue;
    private Label statUptimeValue;

    public DashboardWindow(double companyRating, int marketingPoints, double monthlyRevenue, int skillPoints, long totalMoney, Runnable onClose) {
        this.companyRating = companyRating;
        this.marketingPoints = marketingPoints;
        this.monthlyRevenue = monthlyRevenue;
        this.skillPoints = skillPoints;
        this.totalMoney = totalMoney;
        this.onClose = onClose;
        
        // สร้างข้อมูลจำลองสำหรับการแสดงผล
        this.totalVPS = new Random().nextInt(5) + 2;
        this.totalVMs = new Random().nextInt(10) + 5;
        this.totalCustomers = new Random().nextInt(8) + 3;
        this.averageRating = 3.5 + (new Random().nextDouble() * 1.5);

        setupUI();
        styleWindow();
        startDataUpdates();
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

        // Stats Section - First Row
        HBox statsContainer = new HBox(15);
        statsContainer.setPadding(new Insets(15, 15, 5, 15));
        statsContainer.setStyle("-fx-background-color: #ecf0f1; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        VBox ratingCard = createStatCard("Company Rating", String.format("%.2f ★", companyRating), "#e67e22");
        statRatingValue = (Label) ratingCard.getChildren().get(1);
        
        VBox marketingCard = createStatCard("Marketing Points", marketingPoints + " MP", "#8e44ad");
        statMarketingValue = (Label) marketingCard.getChildren().get(1);
        
        VBox revenueCard = createStatCard("Monthly Revenue", String.format("$%.2f", monthlyRevenue), "#27ae60");
        statRevenueValue = (Label) revenueCard.getChildren().get(1);
        
        VBox pointsCard = createStatCard("Skill Points", skillPoints + " SP", "#3498db");
        statPointsValue = (Label) pointsCard.getChildren().get(1);
        
        VBox moneyCard = createStatCard("Total Money", "$" + totalMoney, "#e74c3c");
        statMoneyValue = (Label) moneyCard.getChildren().get(1);
        
        statsContainer.getChildren().addAll(ratingCard, marketingCard, revenueCard, pointsCard, moneyCard);

        // Stats Section - Second Row (New Stats)
        HBox statsContainer2 = new HBox(15);
        statsContainer2.setPadding(new Insets(5, 15, 15, 15));
        statsContainer2.setStyle("-fx-background-color: #ecf0f1; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        VBox vpsCard = createStatCard("Total VPS", String.valueOf(totalVPS), "#2980b9");
        VBox vmCard = createStatCard("Total VMs", String.valueOf(totalVMs), "#16a085");
        VBox customerCard = createStatCard("Customers", String.valueOf(totalCustomers), "#f39c12");
        VBox ratingAvgCard = createStatCard("Avg. Rating", String.format("%.1f ★", averageRating), "#c0392b");
        VBox uptimeCard = createStatCard("Uptime", "99.9%", "#2c3e50");
        
        // Store references to the value labels for updating
        Label vpsValue = (Label) vpsCard.getChildren().get(1);
        Label vmValue = (Label) vmCard.getChildren().get(1);
        Label customerValue = (Label) customerCard.getChildren().get(1);
        Label ratingAvgValue = (Label) ratingAvgCard.getChildren().get(1);
        Label uptimeValue = (Label) uptimeCard.getChildren().get(1);
        
        // Add these to class variables for updating
        this.statVPSValue = vpsValue;
        this.statVMValue = vmValue;
        this.statCustomerValue = customerValue;
        this.statAvgRatingValue = ratingAvgValue;
        this.statUptimeValue = uptimeValue;
        
        statsContainer2.getChildren().addAll(vpsCard, vmCard, customerCard, ratingAvgCard, uptimeCard);

        // Graphs Container
        VBox graphsContainer = new VBox(15);
        graphsContainer.setPadding(new Insets(15));
        VBox.setVgrow(graphsContainer, Priority.ALWAYS);

        // Revenue Chart
        revenueChart = createRevenueChart();
        
        // Performance Chart
        performanceChart = createPerformanceChart();
        
        // Add charts to container
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
        
        // Create series with proper generic type parameters
        revenueSeries = new XYChart.Series<>();
        revenueSeries.setName("Revenue");
        
        expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Expenses");
        
        profitSeries = new XYChart.Series<>();
        profitSeries.setName("Profit");
        
        // Initialize with some data
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
        
        // Create series with proper generic type parameters
        cpuSeries = new XYChart.Series<>();
        cpuSeries.setName("CPU");
        
        ramSeries = new XYChart.Series<>();
        ramSeries.setName("RAM");
        
        diskSeries = new XYChart.Series<>();
        diskSeries.setName("Disk");
        
        // Initialize with some data
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
    
    /**
     * Updates the dashboard with new data in real-time
     */
    public void updateDashboard(double companyRating, int marketingPoints, double monthlyRevenue, int skillPoints, long totalMoney) {
        Platform.runLater(() -> {
            // Update instance variables
            this.companyRating = companyRating;
            this.marketingPoints = marketingPoints;
            this.monthlyRevenue = monthlyRevenue;
            this.skillPoints = skillPoints;
            this.totalMoney = totalMoney;
            
            // Simulate changes in other metrics
            this.totalVPS = Math.max(1, this.totalVPS + (new Random().nextInt(3) - 1));
            this.totalVMs = Math.max(2, this.totalVMs + (new Random().nextInt(3) - 1));
            this.totalCustomers = Math.max(1, this.totalCustomers + (new Random().nextInt(3) - 1));
            this.averageRating = Math.min(5.0, Math.max(1.0, this.averageRating + (new Random().nextDouble() * 0.4 - 0.2)));
            
            // Update UI components - Main stats
            statRatingValue.setText(String.format("%.2f ★", companyRating));
            statMarketingValue.setText(marketingPoints + " MP");
            statRevenueValue.setText(String.format("$%.2f", monthlyRevenue));
            statPointsValue.setText(skillPoints + " SP");
            statMoneyValue.setText("$" + totalMoney);
            
            // Update UI components - Additional stats
            statVPSValue.setText(String.valueOf(totalVPS));
            statVMValue.setText(String.valueOf(totalVMs));
            statCustomerValue.setText(String.valueOf(totalCustomers));
            statAvgRatingValue.setText(String.format("%.1f ★", averageRating));
            
            // Randomly vary uptime between 99.8% and 100%
            double uptime = 99.8 + (new Random().nextDouble() * 0.2);
            statUptimeValue.setText(String.format("%.2f%%", uptime));
            
            // Update charts with new data
            updateChartData();
        });
    }

    /**
     * เริ่มการอัปเดตข้อมูลแบบอัตโนมัติ
     */
    private void startDataUpdates() {
        // สร้าง Thread สำหรับอัปเดตข้อมูลทุก 3 วินาที
        Thread updateThread = new Thread(() -> {
            try {
                while (true) {
                    Thread.sleep(3000);
                    Platform.runLater(this::updateChartData);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        updateThread.setDaemon(true);
        updateThread.start();
    }
    
    /**
     * อัปเดตข้อมูลกราฟ
     */
    private void updateChartData() {
        Random random = new Random();
        
        // Update revenue chart
        if (revenueSeries.getData().size() >= 10) {
            revenueSeries.getData().remove(0);
            expenseSeries.getData().remove(0);
            profitSeries.getData().remove(0);
        }
        
        double revenue = monthlyRevenue * (0.8 + 0.4 * random.nextDouble());
        double expenses = monthlyRevenue * 0.6 * (0.8 + 0.4 * random.nextDouble());
        double profit = revenue - expenses;
        
        revenueSeries.getData().add(new XYChart.Data<>("Day " + System.currentTimeMillis() % 1000, revenue));
        expenseSeries.getData().add(new XYChart.Data<>("Day " + System.currentTimeMillis() % 1000, expenses));
        profitSeries.getData().add(new XYChart.Data<>("Day " + System.currentTimeMillis() % 1000, profit));
        
        // Update performance chart
        if (cpuSeries.getData().size() >= 10) {
            cpuSeries.getData().remove(0);
            ramSeries.getData().remove(0);
            diskSeries.getData().remove(0);
        }
        
        double cpu = 20 + 60 * random.nextDouble();
        double ram = 30 + 50 * random.nextDouble();
        double disk = 10 + 30 * random.nextDouble();
        
        cpuSeries.getData().add(new XYChart.Data<>("Day " + System.currentTimeMillis() % 1000, cpu));
        ramSeries.getData().add(new XYChart.Data<>("Day " + System.currentTimeMillis() % 1000, ram));
        diskSeries.getData().add(new XYChart.Data<>("Day " + System.currentTimeMillis() % 1000, disk));
    }
}
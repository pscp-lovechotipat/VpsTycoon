package com.vpstycoon.ui.game.desktop;

import com.vpstycoon.game.company.Company;
import com.vpstycoon.game.manager.RequestManager;
import com.vpstycoon.game.manager.VPSManager;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.game.vps.VPSOptimization;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class DashboardWindow extends VBox {
    private Company company;
    private ResourceManager resourceManager;
    private VPSManager vpsManager;
    private RequestManager requestManager;
    private final transient Runnable onClose;
    private Timeline glowAnimation;

    
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
        
        
        company.addRatingObserver(newRating -> updateDashboard());
    }

    private void setupUI() {
        setPrefSize(700, 500);

        
        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_RIGHT);
        titleBar.setPadding(new Insets(8, 15, 8, 15));
        titleBar.setStyle("-fx-background-color: #37474F; -fx-background-radius: 10 10 0 0; -fx-border-color: #9e33ff; -fx-border-width: 0 0 2 0; -fx-effect: dropshadow(gaussian, rgba(0, 255, 255, 0.3), 5, 0, 0, 3);");

        
        Rectangle iconBg = new Rectangle(24, 24);
        iconBg.setArcWidth(5);
        iconBg.setArcHeight(5);
        
        
        Stop[] stops = new Stop[] {
            new Stop(0, Color.web("#00ffff")),
            new Stop(1, Color.web("#ff00ff"))
        };
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
        iconBg.setFill(gradient);
        
        
        Glow glow = new Glow(0.8);
        iconBg.setEffect(glow);
        
        
        glowAnimation = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(glow.levelProperty(), 0.5)),
            new KeyFrame(Duration.seconds(1.5), new KeyValue(glow.levelProperty(), 0.8))
        );
        glowAnimation.setAutoReverse(true);
        glowAnimation.setCycleCount(Timeline.INDEFINITE);
        glowAnimation.play();
        
        
        Text iconText = new Text("D");
        iconText.setFill(Color.WHITE);
        iconText.setStyle("-fx-font-weight: bold;");
        HBox iconContainer = new HBox(iconBg);
        iconContainer.setAlignment(Pos.CENTER);
        iconContainer.getChildren().add(iconText);
        iconContainer.setTranslateX(-12);

        Label titleLabel = new Label("BUSINESS DASHBOARD");
        titleLabel.setStyle("-fx-font-family: 'Monospace', 'Courier New', monospace; -fx-font-size: 18px; -fx-text-fill: #E0FFFF; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, #a100ff, 5, 0.1, 0, 0);");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        titleLabel.setAlignment(Pos.CENTER_LEFT);
        
        
        Label versionLabel = new Label("v3.7");
        versionLabel.setStyle("-fx-text-fill: #00ffff; -fx-font-size: 10px; -fx-font-family: 'Monospace', 'Courier New', monospace;");
        versionLabel.setTranslateY(4);
        
        VBox titleVersionBox = new VBox(0);
        titleVersionBox.setAlignment(Pos.CENTER_LEFT);
        titleVersionBox.getChildren().addAll(titleLabel, versionLabel);

        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.getChildren().addAll(iconContainer, titleVersionBox);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Button closeButton = new Button("×");
        closeButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand; -fx-background-radius: 5px; -fx-effect: dropshadow(gaussian, #F44336, 10, 0.5, 0, 0);");
        closeButton.setOnMouseEntered(e -> closeButton.setStyle("-fx-background-color: #EF5350; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand; -fx-background-radius: 5px; -fx-effect: dropshadow(gaussian, rgba(255, 0, 128, 0.8), 10, 0, 0, 0);"));
        closeButton.setOnMouseExited(e -> closeButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand; -fx-background-radius: 5px; -fx-effect: dropshadow(gaussian, #F44336, 10, 0.5, 0, 0);"));
        closeButton.setOnAction(e -> onClose.run());

        titleBar.getChildren().addAll(titleBox, closeButton);

        
        HBox statsContainer = new HBox(15);
        statsContainer.setPadding(new Insets(15, 15, 5, 15));
        statsContainer.setStyle("-fx-background-color: #263238; -fx-background-radius: 5; -fx-border-color: #9900ff; -fx-border-width: 1; -fx-border-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 5);");

        VBox ratingCard = createStatCard("Company Rating", String.format("%.2f ★", company.getRating()), "#00ffff");
        statRatingValue = (Label) ratingCard.getChildren().get(1);

        VBox marketingCard = createStatCard("Marketing Points", company.getMarketingPoints() + " MP", "#ff00ff");
        statMarketingValue = (Label) marketingCard.getChildren().get(1);

        VBox revenueCard = createStatCard("Monthly Revenue", String.format("$%.2f", calculateMonthlyRevenue()), "#00ff88");
        statRevenueValue = (Label) revenueCard.getChildren().get(1);

        VBox pointsCard = createStatCard("Skill Points", company.getSkillPointsAvailable() + " SP", "#9e33ff");
        statPointsValue = (Label) pointsCard.getChildren().get(1);

        VBox moneyCard = createStatCard("Total Money", "$" + company.getMoney(), "#ff5555");
        statMoneyValue = (Label) moneyCard.getChildren().get(1);

        statsContainer.getChildren().addAll(ratingCard, marketingCard, revenueCard, pointsCard, moneyCard);

        
        HBox statsContainer2 = new HBox(15);
        statsContainer2.setPadding(new Insets(5, 15, 15, 15));
        statsContainer2.setStyle("-fx-background-color: #263238; -fx-background-radius: 5; -fx-border-color: #9900ff; -fx-border-width: 1; -fx-border-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 5);");

        VBox vpsCard = createStatCard("Total Servers", String.valueOf(vpsManager.getVPSMap().size()), "#00c3ff");
        statVPSValue = (Label) vpsCard.getChildren().get(1);

        VBox vmCard = createStatCard("Total VMs", String.valueOf(calculateTotalVMs()), "#9e33ff");
        statVMValue = (Label) vmCard.getChildren().get(1);

        VBox customerCard = createStatCard("Customers", String.valueOf(requestManager.getRequests().size()), "#ff00ff");
        statCustomerValue = (Label) customerCard.getChildren().get(1);

        double avgRating = calculateAverageRating();
        VBox ratingAvgCard = createStatCard("Avg. Rating", String.format("%.1f ★", avgRating), "#00ffff");
        statAvgRatingValue = (Label) ratingAvgCard.getChildren().get(1);

        VBox uptimeCard = createStatCard("Uptime", String.format("%.2f%%", calculateUptime()), "#00ff88");
        statUptimeValue = (Label) uptimeCard.getChildren().get(1);

        statsContainer2.getChildren().addAll(vpsCard, vmCard, customerCard, ratingAvgCard, uptimeCard);

        
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
        card.setStyle("-fx-background-color: #37474F; -fx-background-radius: 5; -fx-border-color: " + color + "; -fx-border-width: 1; -fx-border-radius: 5; -fx-padding: 5; -fx-alignment: center; -fx-effect: innershadow(gaussian, " + color + ", 10, 0.3, 0, 0);");
        
        
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web(color));
        glow.setRadius(10);
        
        card.setOnMouseEntered(e -> {
            card.setEffect(glow);
            card.setStyle("-fx-background-color: #3a1f5d; -fx-background-radius: 5; -fx-border-color: " + color + "; -fx-border-width: 2; -fx-border-radius: 5; -fx-padding: 5; -fx-alignment: center;");
        });
        
        card.setOnMouseExited(e -> {
            card.setEffect(null);
            card.setStyle("-fx-background-color: #37474F; -fx-background-radius: 5; -fx-border-color: " + color + "; -fx-border-width: 1; -fx-border-radius: 5; -fx-padding: 5; -fx-alignment: center; -fx-effect: innershadow(gaussian, " + color + ", 10, 0.3, 0, 0);");
        });

        Label statLabel = new Label(label);
        statLabel.setStyle("-fx-font-family: 'Monospace', 'Courier New', monospace; -fx-font-size: 10px; -fx-text-fill: #E0FFFF; -fx-padding: 2 0 0 0;");

        Label statValue = new Label(value);
        statValue.setStyle("-fx-font-family: 'Monospace', 'Courier New', monospace; -fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + color + "; -fx-effect: dropshadow(gaussian, rgba(0, 255, 255, 0.5), 2, 0, 0, 0);");

        card.getChildren().addAll(statLabel, statValue);
        return card;
    }

    private LineChart<String, Number> createRevenueChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Time");
        yAxis.setLabel("Amount (THB)");
        
        
        xAxis.setStyle("-fx-tick-label-fill: #00ffff; -fx-font-family: 'Monospace', 'Courier New', monospace;");
        yAxis.setStyle("-fx-tick-label-fill: #00ffff; -fx-font-family: 'Monospace', 'Courier New', monospace;");

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Financial Performance");
        chart.setAnimated(false);
        chart.setCreateSymbols(false);
        
        
        chart.setStyle("""
            -fx-background-color: #263238;
            -fx-background-radius: 5;
            -fx-border-color: #9900ff;
            -fx-border-width: 1;
            -fx-border-radius: 5;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 5);
            -fx-legend-side: BOTTOM;
            """);
        
        
        chart.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Platform.runLater(() -> {
                    if (chart.lookup(".chart-title") != null) {
                        chart.lookup(".chart-title").setStyle("-fx-text-fill: #00ffff; -fx-font-family: 'Monospace', 'Courier New', monospace; -fx-font-size: 14px;");
                    }
                });
            }
        });

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
        
        
        chart.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                applyCyberSeriesColors(chart);
            }
        });

        return chart;
    }
    
    private void setSeriesColors(LineChart<String, Number> chart) {
        
        
    }
    
    private void applyCyberSeriesColors(LineChart<String, Number> chart) {
        String[] colors = {"#00ffff", "#ff5555", "#00ff88"};
        
        for (int i = 0; i < chart.getData().size(); i++) {
            final int seriesIndex = i;
            final String color = i < colors.length ? colors[i] : "#ffffff";
            
            
            XYChart.Series<String, Number> series = chart.getData().get(seriesIndex);
            series.getNode().setStyle("-fx-stroke: " + color + "; -fx-stroke-width: 2px;");
            
            
            for (XYChart.Data<String, Number> data : series.getData()) {
                
                data.nodeProperty().addListener((ov, oldNode, newNode) -> {
                    if (newNode != null) {
                        newNode.setStyle("-fx-background-color: " + color + ", white;");
                    }
                });
            }
        }
    }

    private LineChart<String, Number> createPerformanceChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis(0, 100, 10);
        xAxis.setLabel("Time");
        yAxis.setLabel("Usage (%)");
        
        
        xAxis.setStyle("-fx-tick-label-fill: #00ffff; -fx-font-family: 'Monospace', 'Courier New', monospace;");
        yAxis.setStyle("-fx-tick-label-fill: #00ffff; -fx-font-family: 'Monospace', 'Courier New', monospace;");

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("System Performance");
        chart.setAnimated(false);
        chart.setCreateSymbols(false);
        
        
        chart.setStyle("""
            -fx-background-color: #263238;
            -fx-background-radius: 5;
            -fx-border-color: #9900ff;
            -fx-border-width: 1;
            -fx-border-radius: 5;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 5);
            -fx-legend-side: BOTTOM;
            """);
        
        
        chart.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Platform.runLater(() -> {
                    if (chart.lookup(".chart-title") != null) {
                        chart.lookup(".chart-title").setStyle("-fx-text-fill: #00ffff; -fx-font-family: 'Monospace', 'Courier New', monospace; -fx-font-size: 14px;");
                    }
                });
            }
        });

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
        
        
        chart.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                applyCyberPerformanceColors(chart);
            }
        });

        return chart;
    }
    
    private void applyCyberPerformanceColors(LineChart<String, Number> chart) {
        String[] colors = {"#ff00ff", "#00c3ff", "#9e33ff"};
        
        for (int i = 0; i < chart.getData().size(); i++) {
            final int seriesIndex = i;
            final String color = i < colors.length ? colors[i] : "#ffffff";
            
            
            XYChart.Series<String, Number> series = chart.getData().get(seriesIndex);
            series.getNode().setStyle("-fx-stroke: " + color + "; -fx-stroke-width: 2px;");
            
            
            for (XYChart.Data<String, Number> data : series.getData()) {
                
                data.nodeProperty().addListener((ov, oldNode, newNode) -> {
                    if (newNode != null) {
                        newNode.setStyle("-fx-background-color: " + color + ", white;");
                    }
                });
            }
        }
    }

    private void styleWindow() {
        setStyle("""
            -fx-background-color: #12071e;
            -fx-effect: dropshadow(gaussian, #7f00ff, 10, 0, 0, 0);
            -fx-border-color: #8a2be2;
            -fx-border-width: 2px;
            -fx-border-style: solid;
            -fx-border-radius: 10;
            -fx-background-radius: 10;
            """);
    }

    
    private double calculateMonthlyRevenue() {
        return requestManager.getRequests().stream()
                .filter(req -> req.isActive())
                .mapToDouble(req -> {
                    
                    String ramStr = req.getRequiredRam().split(" ")[0];
                    String diskStr = req.getRequiredDisk().split(" ")[0];
                    return req.getRequiredVCPUs() * 10.0 + Double.parseDouble(ramStr) * 5.0 + Double.parseDouble(diskStr) * 2.0;
                })
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
            
            statRatingValue.setText(String.format("%.2f ★", company.getRating()));
            statMarketingValue.setText(company.getMarketingPoints() + " MP");
            double monthlyRevenue = calculateMonthlyRevenue();
            statRevenueValue.setText(String.format("$%.2f", monthlyRevenue));
            statPointsValue.setText(company.getSkillPointsAvailable() + " SP");
            statMoneyValue.setText("$" + company.getMoney());

            
            statVPSValue.setText(String.valueOf(vpsManager.getVPSMap().size()));
            statVMValue.setText(String.valueOf(calculateTotalVMs()));
            statCustomerValue.setText(String.valueOf(requestManager.getRequests().size()));
            statAvgRatingValue.setText(String.format("%.1f ★", calculateAverageRating()));
            statUptimeValue.setText(String.format("%.2f%%", calculateUptime()));

            
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
        XYChart.Data<String, Number> revenueData = new XYChart.Data<>(timeStamp, monthlyRevenue);
        XYChart.Data<String, Number> expensesData = new XYChart.Data<>(timeStamp, expenses);
        XYChart.Data<String, Number> profitData = new XYChart.Data<>(timeStamp, profit);
        
        revenueSeries.getData().add(revenueData);
        expenseSeries.getData().add(expensesData);
        profitSeries.getData().add(profitData);
        
        
        revenueData.nodeProperty().addListener((ov, oldNode, newNode) -> {
            if (newNode != null) {
                newNode.setStyle("-fx-background-color: #00ffff, white;");
            }
        });
        
        expensesData.nodeProperty().addListener((ov, oldNode, newNode) -> {
            if (newNode != null) {
                newNode.setStyle("-fx-background-color: #ff5555, white;");
            }
        });
        
        profitData.nodeProperty().addListener((ov, oldNode, newNode) -> {
            if (newNode != null) {
                newNode.setStyle("-fx-background-color: #00ff88, white;");
            }
        });

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
                .mapToDouble(vm -> Double.parseDouble(vm.getRam().split("GB")[0]))
                .average().orElse(50.0);

        double avgDiskUsage = vpsManager.getVPSMap().values().stream()
                .flatMap(vps -> vps.getVms().stream())
                .mapToDouble(vm -> Double.parseDouble(vm.getDisk().split("GB")[0]))
                .average().orElse(50.0);
                
        XYChart.Data<String, Number> cpuData = new XYChart.Data<>(timeStamp, avgCpuUsage);
        XYChart.Data<String, Number> ramData = new XYChart.Data<>(timeStamp, avgRamUsage);
        XYChart.Data<String, Number> diskData = new XYChart.Data<>(timeStamp, avgDiskUsage);
        
        cpuSeries.getData().add(cpuData);
        ramSeries.getData().add(ramData);
        diskSeries.getData().add(diskData);
        
        
        cpuData.nodeProperty().addListener((ov, oldNode, newNode) -> {
            if (newNode != null) {
                newNode.setStyle("-fx-background-color: #ff00ff, white;");
            }
        });
        
        ramData.nodeProperty().addListener((ov, oldNode, newNode) -> {
            if (newNode != null) {
                newNode.setStyle("-fx-background-color: #00c3ff, white;");
            }
        });
        
        diskData.nodeProperty().addListener((ov, oldNode, newNode) -> {
            if (newNode != null) {
                newNode.setStyle("-fx-background-color: #9e33ff, white;");
            }
        });
    }
}


package com.vpstycoon.ui.game.vps;

import com.vpstycoon.game.vps.VPSOptimization;
import com.vpstycoon.ui.game.GameplayContentPane;
import com.vpstycoon.ui.game.utils.UIUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import java.util.Random;

public class VMInfoUI {
    private final GameplayContentPane parent;
    private final Random random = new Random();
    private Timeline blinkingEffect;

    public VMInfoUI(GameplayContentPane parent) {
        this.parent = parent;
    }

    public void openVMInfoPage(VPSOptimization.VM vm, VPSOptimization vps) {
        // Main container with cyber theme
        BorderPane vmInfoPane = new BorderPane();
        vmInfoPane.setPrefSize(800, 600);
        vmInfoPane.setStyle("-fx-background-color: linear-gradient(to bottom, #1a0033, #380066); -fx-padding: 20px; -fx-border-color: #8a2be2; -fx-border-width: 3px; -fx-border-radius: 5px;");

        // Hide the menu bars
        parent.getMenuBar().setVisible(false);
        parent.getInGameMarketMenuBar().setVisible(false);
        
        // Cyber-style top bar
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #220033; -fx-padding: 10px; -fx-background-radius: 10px; -fx-effect: dropshadow(gaussian, rgba(138,43,226,0.6), 15, 0, 0, 5); -fx-border-color: #8a2be2; -fx-border-width: 1px; -fx-border-radius: 10px;");

        Label titleLabel = new Label("VM CONSOLE: " + vm.getIp());
        titleLabel.setStyle("-fx-text-fill: #00ffff; -fx-font-size: 24px; -fx-font-weight: bold; -fx-font-family: 'Monospace'; -fx-effect: dropshadow(three-pass-box, rgba(0,255,255,0.8), 6, 0, 0, 0);");
        
        Button backButton = createCyberButton("< RETURN", "#ff00ff");
        backButton.setOnAction(e -> {
            if (blinkingEffect != null) {
                blinkingEffect.stop();
            }
            parent.openVPSInfoPage(vps);
        });
        
        // Add blinking status indicator
        Rectangle statusIndicator = new Rectangle(15, 15);
        statusIndicator.setFill(Color.GREEN);
        statusIndicator.setArcHeight(5);
        statusIndicator.setArcWidth(5);
        
        blinkingEffect = new Timeline(
            new KeyFrame(Duration.seconds(0.8), e -> statusIndicator.setFill(Color.GREEN)),
            new KeyFrame(Duration.seconds(1.6), e -> statusIndicator.setFill(Color.DARKGREEN))
        );
        blinkingEffect.setCycleCount(Timeline.INDEFINITE);
        blinkingEffect.play();
        
        Label statusLabel = new Label("ONLINE");
        statusLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-weight: bold;");
        
        HBox statusBox = new HBox(5);
        statusBox.setAlignment(Pos.CENTER);
        statusBox.getChildren().addAll(statusIndicator, statusLabel);
        
        topBar.getChildren().addAll(backButton, titleLabel, statusBox);

        // VM Dashboard - main content
        VBox contentBox = new VBox(15);
        contentBox.setStyle("-fx-background-color: rgba(25, 0, 51, 0.7); -fx-padding: 20px; -fx-background-radius: 10px; -fx-border-color: #8a2be2; -fx-border-width: 1px; -fx-border-radius: 10px;");
        
        // Stats section
        Label sectionTitle = new Label("SYSTEM METRICS");
        sectionTitle.setStyle("-fx-text-fill: #00ffff; -fx-font-size: 18px; -fx-font-weight: bold; -fx-border-color: #8a2be2; -fx-border-width: 0 0 1px 0; -fx-padding: 0 0 5px 0;");
        
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(25);
        statsGrid.setVgap(15);
        statsGrid.setPadding(new Insets(10));
        
        // CPU usage
        addResourceMonitor(statsGrid, "CPU USAGE", generateRandomValue(10, 90), 0);
        
        // RAM usage
        addResourceMonitor(statsGrid, "RAM USAGE", generateRandomValue(20, 85), 1);
        
        // Network traffic
        addResourceMonitor(statsGrid, "NETWORK I/O", generateRandomValue(5, 60), 2);
        
        // Disk I/O
        addResourceMonitor(statsGrid, "DISK I/O", generateRandomValue(15, 70), 3);
        
        // VM information section
        VBox vmInfoSection = new VBox(10);
        vmInfoSection.setStyle("-fx-background-color: rgba(40, 0, 80, 0.5); -fx-padding: 15px; -fx-background-radius: 8px; -fx-border-color: #9370db; -fx-border-width: 1px; -fx-border-radius: 8px;");
        
        Label vmInfoTitle = new Label("MACHINE SPECIFICATIONS");
        vmInfoTitle.setStyle("-fx-text-fill: #e0aaff; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        GridPane vmDetailsGrid = new GridPane();
        vmDetailsGrid.setHgap(15);
        vmDetailsGrid.setVgap(10);
        
        addInfoField(vmDetailsGrid, "IP ADDRESS:", vm.getIp(), 0);
        addInfoField(vmDetailsGrid, "VM NAME:", vm.getName(), 1);
        addInfoField(vmDetailsGrid, "STATUS:", vm.getStatus(), 2);
        addInfoField(vmDetailsGrid, "vCPU CORES:", vm.getVcpu() + " cores", 3);
        addInfoField(vmDetailsGrid, "MEMORY:", vm.getRam() + " GB", 4);
        addInfoField(vmDetailsGrid, "STORAGE:", vm.getDisk() + " GB", 5);
        addInfoField(vmDetailsGrid, "UPTIME:", generateRandomUptime(), 6);
        addInfoField(vmDetailsGrid, "OS:", "CyberLinux 2077", 7);
        
        vmInfoSection.getChildren().addAll(vmInfoTitle, vmDetailsGrid);
        
        contentBox.getChildren().addAll(sectionTitle, statsGrid, vmInfoSection);

        // Action buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));
        buttonBox.setStyle("-fx-background-color: rgba(30, 0, 60, 0.7); -fx-padding: 15px; -fx-background-radius: 8px; -fx-border-color: #8a2be2; -fx-border-width: 1px; -fx-border-radius: 8px;");
        
        Button rebootButton = createCyberButton("REBOOT", "#00bfff");
        rebootButton.setOnAction(e -> showActionMessage("Rebooting VM..."));
        
        Button consoleButton = createCyberButton("OPEN CONSOLE", "#32cd32");
        consoleButton.setOnAction(e -> showActionMessage("Opening console session..."));
        
        Button editButton = createCyberButton("CONFIGURE", "#ff9900");
        editButton.setOnAction(e -> parent.openEditVMPage(vm, vps));
        
        Button deleteButton = createCyberButton("TERMINATE", "#ff3333");
        deleteButton.setOnAction(e -> {
            vps.getVms().remove(vm);
            System.out.println("VM deleted: " + vm.getIp());
            if (blinkingEffect != null) {
                blinkingEffect.stop();
            }
            parent.openVPSInfoPage(vps);
        });
        
        buttonBox.getChildren().addAll(rebootButton, consoleButton, editButton, deleteButton);

        vmInfoPane.setTop(topBar);
        vmInfoPane.setCenter(contentBox);
        vmInfoPane.setBottom(buttonBox);

        // Display the updated UI
        parent.getGameArea().getChildren().clear();
        parent.getGameArea().getChildren().add(vmInfoPane);
    }
    
    private Button createCyberButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-border-color: white; -fx-border-width: 1px; -fx-background-radius: 5px; -fx-border-radius: 5px; " +
                "-fx-cursor: hand; -fx-font-family: 'Monospace'; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 5, 0, 0, 1);");
        
        button.setOnMouseEntered(e -> 
            button.setStyle("-fx-background-color: derive(" + color + ", 20%); -fx-text-fill: white; -fx-font-weight: bold; " +
                    "-fx-border-color: white; -fx-border-width: 2px; -fx-background-radius: 5px; -fx-border-radius: 5px; " +
                    "-fx-cursor: hand; -fx-font-family: 'Monospace'; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 8, 0, 0, 1);")
        );
        
        button.setOnMouseExited(e -> 
            button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; " +
                    "-fx-border-color: white; -fx-border-width: 1px; -fx-background-radius: 5px; -fx-border-radius: 5px; " +
                    "-fx-cursor: hand; -fx-font-family: 'Monospace'; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 5, 0, 0, 1);")
        );
        
        return button;
    }
    
    private void addResourceMonitor(GridPane grid, String label, double value, int row) {
        Label nameLabel = new Label(label);
        nameLabel.setStyle("-fx-text-fill: #e0aaff; -fx-font-weight: bold; -fx-font-family: 'Monospace';");
        
        ProgressBar progressBar = new ProgressBar(value / 100.0);
        progressBar.setPrefWidth(200);
        progressBar.setStyle(getProgressBarStyle(value));
        
        Label valueLabel = new Label(String.format("%.1f%%", value));
        valueLabel.setStyle("-fx-text-fill: #e0aaff; -fx-font-family: 'Monospace';");
        
        grid.add(nameLabel, 0, row);
        grid.add(progressBar, 1, row);
        grid.add(valueLabel, 2, row);
    }
    
    private String getProgressBarStyle(double value) {
        String barColor;
        if (value < 50) {
            barColor = "linear-gradient(to right, #00ff00, #66ff00)";
        } else if (value < 80) {
            barColor = "linear-gradient(to right, #ffcc00, #ff9900)";
        } else {
            barColor = "linear-gradient(to right, #ff6600, #ff0000)";
        }
        
        return "-fx-accent: " + barColor + "; -fx-background-color: #220033; -fx-border-color: #8a2be2; -fx-border-width: 1px;";
    }
    
    private void addInfoField(GridPane grid, String label, String value, int row) {
        Label nameLabel = new Label(label);
        nameLabel.setStyle("-fx-text-fill: #9370db; -fx-font-weight: bold; -fx-font-family: 'Monospace';");
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill: #e0aaff; -fx-font-family: 'Monospace';");
        
        grid.add(nameLabel, 0, row);
        grid.add(valueLabel, 1, row);
    }
    
    private double generateRandomValue(int min, int max) {
        return min + (max - min) * random.nextDouble();
    }
    
    private String generateRandomUptime() {
        int days = random.nextInt(30);
        int hours = random.nextInt(24);
        int minutes = random.nextInt(60);
        return days + "d " + hours + "h " + minutes + "m";
    }
    
    private void showActionMessage(String message) {
        System.out.println(message);
        // Here you could implement a futuristic notification popup if desired
    }
}
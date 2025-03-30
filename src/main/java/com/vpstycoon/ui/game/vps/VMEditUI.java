package com.vpstycoon.ui.game.vps;

import com.vpstycoon.game.vps.VPSOptimization;
import com.vpstycoon.ui.game.GameplayContentPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class VMEditUI {
    private final GameplayContentPane parent;

    public VMEditUI(GameplayContentPane parent) {
        this.parent = parent;
    }

    public void openEditVMPage(VPSOptimization.VM vm, VPSOptimization vps) {
        
        BorderPane editVMPane = new BorderPane();
        editVMPane.setPrefSize(800, 600);
        editVMPane.setStyle("-fx-background-color: linear-gradient(to bottom, #1E0033, #2D0A4E); -fx-padding: 20px;");
        
        
        GridPane cyberGrid = new GridPane();
        cyberGrid.setHgap(1);
        cyberGrid.setVgap(1);
        for (int i = 0; i < 40; i++) {
            for (int j = 0; j < 30; j++) {
                StackPane gridCell = new StackPane();
                gridCell.setPrefSize(20, 20);
                gridCell.setStyle("-fx-background-color: transparent; -fx-border-color: rgba(170, 80, 255, 0.1); -fx-border-width: 0.5;");
                cyberGrid.add(gridCell, i, j);
            }
        }
        editVMPane.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
        
        
        StackPane contentContainer = new StackPane();
        contentContainer.getChildren().addAll(cyberGrid);
        
        
        parent.getMenuBar().setVisible(false);
        parent.getInGameMarketMenuBar().setVisible(false);
        
        
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #3A0066; -fx-padding: 15px; -fx-background-radius: 5px; -fx-border-color: #8A2BE2; -fx-border-width: 2px; -fx-effect: dropshadow(gaussian, rgba(170, 80, 255, 0.7), 10, 0, 0, 5);");
        
        Label titleLabel = new Label("SYSTEM: EDIT VM_" + vm.getIp());
        titleLabel.setFont(Font.font("Monospace", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: #00FFFF; -fx-font-weight: bold; -fx-font-family: 'Courier New';");
        DropShadow glow = new DropShadow();
        glow.setColor(Color.AQUA);
        glow.setHeight(8);
        glow.setWidth(8);
        titleLabel.setEffect(glow);
        
        Button backButton = createCyberButton("< RETURN", "#440088", "#9933CC");
        backButton.setOnAction(e -> parent.openVMInfoPage(vm, vps));
        topBar.getChildren().addAll(backButton, titleLabel);
        
        
        VBox formBox = new VBox(20);
        formBox.setAlignment(Pos.CENTER);
        formBox.setPadding(new Insets(25));
        formBox.setStyle("-fx-background-color: rgba(40, 10, 60, 0.7); -fx-background-radius: 10px; -fx-border-color: #8A2BE2; -fx-border-width: 1px; -fx-border-radius: 10px; -fx-effect: dropshadow(gaussian, rgba(170, 80, 255, 0.5), 10, 0, 0, 5);");

        
        VBox infoSection = createCyberSection("BASIC CONFIG");
        
        HBox nameBox = new HBox(15);
        nameBox.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label("NAME:");
        nameLabel.setStyle("-fx-text-fill: #BB99FF; -fx-font-family: 'Courier New'; -fx-font-weight: bold;");
        TextField nameField = createCyberTextField(vm.getName());
        nameField.setPromptText("ENTER_VM_NAME");
        nameBox.getChildren().addAll(nameLabel, nameField);
        
        HBox ipBox = new HBox(15);
        ipBox.setAlignment(Pos.CENTER_LEFT);
        Label ipLabel = new Label("IP:");
        ipLabel.setStyle("-fx-text-fill: #BB99FF; -fx-font-family: 'Courier New'; -fx-font-weight: bold;");
        TextField ipField = createCyberTextField(vm.getIp());
        ipField.setPromptText("IP_ADDRESS");
        ipField.setDisable(true);
        ipField.setStyle("-fx-background-color: #220033; -fx-text-fill: #888888; -fx-font-family: 'Courier New'; -fx-border-color: #553377; -fx-border-width: 1;");
        ipBox.getChildren().addAll(ipLabel, ipField);
        infoSection.getChildren().addAll(nameBox, ipBox);

        
        VBox perfSection = createCyberSection("HARDWARE SPECS");
        
        
        HBox vcpuBox = new HBox(15);
        vcpuBox.setAlignment(Pos.CENTER_LEFT);
        Label vcpuLabel = new Label("vCPUs:");
        vcpuLabel.setStyle("-fx-text-fill: #BB99FF; -fx-font-family: 'Courier New'; -fx-font-weight: bold;");
        
        TextField vcpuField = createCyberTextField(String.valueOf(vm.getVcpu()));
        vcpuField.setPrefWidth(60);
        
        Button decreaseCpuBtn = createCyberButton("-", "#550055", "#AA33AA");
        decreaseCpuBtn.setPrefWidth(30);
        Button increaseCpuBtn = createCyberButton("+", "#550055", "#AA33AA");
        increaseCpuBtn.setPrefWidth(30);
        
        decreaseCpuBtn.setOnAction(e -> {
            try {
                int current = Integer.parseInt(vcpuField.getText());
                if (current > 1) {
                    vcpuField.setText(String.valueOf(current - 1));
                }
            } catch (NumberFormatException ex) {
                vcpuField.setText("1");
            }
        });
        
        increaseCpuBtn.setOnAction(e -> {
            try {
                int current = Integer.parseInt(vcpuField.getText());
                vcpuField.setText(String.valueOf(current + 1));
            } catch (NumberFormatException ex) {
                vcpuField.setText("1");
            }
        });
        
        
        Label ramLabel = new Label("RAM:");
        ramLabel.setStyle("-fx-text-fill: #BB99FF; -fx-font-family: 'Courier New'; -fx-font-weight: bold;");
        TextField ramField = createCyberTextField(vm.getRam());
        
        
        Label diskLabel = new Label("DISK:");
        diskLabel.setStyle("-fx-text-fill: #BB99FF; -fx-font-family: 'Courier New'; -fx-font-weight: bold;");
        TextField diskField = createCyberTextField(vm.getDisk());
        
        vcpuBox.getChildren().addAll(vcpuLabel, decreaseCpuBtn, vcpuField, increaseCpuBtn, ramLabel, ramField, diskLabel, diskField);
        
        perfSection.getChildren().add(vcpuBox);

        
        VBox statusSection = createCyberSection("OPERATIONAL STATUS");
        HBox statusBox = new HBox(15);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        Label statusLabel = new Label("STATUS:");
        statusLabel.setStyle("-fx-text-fill: #BB99FF; -fx-font-family: 'Courier New'; -fx-font-weight: bold;");
        
        ChoiceBox<String> statusChoice = new ChoiceBox<>();
        statusChoice.getItems().addAll("Running", "Stopped", "Paused");
        statusChoice.setValue(vm.getStatus());
        statusChoice.setStyle("-fx-background-color: #330055; -fx-mark-color: #AA66FF; -fx-text-fill: #DDAAFF;");
        
        
        StackPane statusLight = new StackPane();
        statusLight.setPrefSize(15, 15);
        statusLight.setStyle("-fx-background-radius: 10; -fx-background-color: " + 
                             (vm.getStatus().equals("Running") ? "#00FF66" : 
                              vm.getStatus().equals("Paused") ? "#FFAA00" : "#FF3333") + ";");
        
        statusChoice.setOnAction(e -> {
            String newStatus = statusChoice.getValue();
            statusLight.setStyle("-fx-background-radius: 10; -fx-background-color: " + 
                             (newStatus.equals("Running") ? "#00FF66" : 
                              newStatus.equals("Paused") ? "#FFAA00" : "#FF3333") + ";");
        });
        
        Glow lightGlow = new Glow(0.8);
        statusLight.setEffect(lightGlow);
        
        statusBox.getChildren().addAll(statusLabel, statusChoice, statusLight);
        statusSection.getChildren().add(statusBox);

        formBox.getChildren().addAll(infoSection, perfSection, statusSection);

        
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(15));
        buttonBox.setStyle("-fx-background-color: rgba(40, 10, 60, 0.5); -fx-background-radius: 5px;");
        
        Button saveButton = createCyberButton("EDIT", "#006644", "#00AA66");
        saveButton.setOnAction(e -> {
            try {
                vm.setName(nameField.getText());
                vm.setVcpu(Integer.parseInt(vcpuField.getText()));
                vm.setRam(ramField.getText());
                vm.setDisk(diskField.getText());
                vm.setStatus(statusChoice.getValue());
                System.out.println("VM updated: " + vm.getIp());
                parent.openVMInfoPage(vm, vps);
            } catch (NumberFormatException ex) {
                System.out.println("Error: Invalid vCPUs value.");
            }
        });
        
        Button cancelButton = createCyberButton("CANCEL", "#660022", "#AA3344");
        cancelButton.setOnAction(e -> parent.openVMInfoPage(vm, vps));
        
        buttonBox.getChildren().addAll(saveButton, cancelButton);

        editVMPane.setTop(topBar);
        editVMPane.setCenter(formBox);
        editVMPane.setBottom(buttonBox);

        
        parent.getGameArea().getChildren().clear();
        parent.getGameArea().getChildren().add(editVMPane);
    }
    
    private Button createCyberButton(String text, String baseColor, String hoverColor) {
        Button button = new Button(text);
        button.setFont(Font.font("Courier New", FontWeight.BOLD, 12));
        button.setStyle("-fx-background-color: " + baseColor + "; -fx-text-fill: #FFFFFF; " +
                        "-fx-font-family: 'Courier New'; -fx-font-weight: bold; " + 
                        "-fx-border-color: " + hoverColor + "; -fx-border-width: 1px;");
        
        button.setOnMouseEntered(e -> {
            button.setStyle("-fx-background-color: " + hoverColor + "; -fx-text-fill: #FFFFFF; " +
                           "-fx-font-family: 'Courier New'; -fx-font-weight: bold; " +
                           "-fx-border-color: #FFFFFF; -fx-border-width: 1px;");
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle("-fx-background-color: " + baseColor + "; -fx-text-fill: #FFFFFF; " +
                           "-fx-font-family: 'Courier New'; -fx-font-weight: bold; " + 
                           "-fx-border-color: " + hoverColor + "; -fx-border-width: 1px;");
        });
        
        return button;
    }
    
    private TextField createCyberTextField(String text) {
        TextField field = new TextField(text);
        field.setStyle("-fx-background-color: #220033; -fx-text-fill: #00FFFF; -fx-font-family: 'Courier New'; " +
                      "-fx-border-color: #553377; -fx-border-width: 1; -fx-highlight-fill: #AA33FF;");
        return field;
    }
    
    private VBox createCyberSection(String title) {
        VBox section = new VBox(10);
        section.setPadding(new Insets(15, 10, 15, 10));
        section.setStyle("-fx-background-color: rgba(30, 5, 50, 0.7); -fx-background-radius: 5px; " +
                        "-fx-border-color: #5522AA; -fx-border-width: 1px; -fx-border-radius: 5px;");
        
        Label titleLabel = new Label(">> " + title);
        titleLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 14));
        titleLabel.setStyle("-fx-text-fill: #AA66FF; -fx-font-family: 'Courier New';");
        
        section.getChildren().add(titleLabel);
        return section;
    }
}

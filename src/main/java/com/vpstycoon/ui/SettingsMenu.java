package com.vpstycoon.ui;

import com.vpstycoon.Config;
import com.vpstycoon.ScreenResolution;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SettingsMenu {
    private Stage primaryStage;
    private ScreenResolution selectedResolution = Config.getResolution();
    private boolean fullscreen = Config.isFullscreen();

    public SettingsMenu(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void show() {
        primaryStage.setTitle("Settings");

        // Resolution Selector
        ComboBox<ScreenResolution> resolutionComboBox = new ComboBox<>();
        resolutionComboBox.getItems().addAll(ScreenResolution.values());
        resolutionComboBox.setValue(selectedResolution);
        resolutionComboBox.setOnAction(e -> selectedResolution = resolutionComboBox.getValue());

        // Fullscreen Toggle
        CheckBox fullscreenCheckBox = new CheckBox("Enable Fullscreen");
        fullscreenCheckBox.setSelected(fullscreen);
        fullscreenCheckBox.setOnAction(e -> fullscreen = fullscreenCheckBox.isSelected());

        // Apply Button (Saves the settings)
        Button applyButton = new Button("Apply");
        applyButton.setOnAction(e -> {
            Config.setResolution(selectedResolution);
            Config.setFullscreen(fullscreen);
            primaryStage.setFullScreen(fullscreen);
            System.out.println("Applied Resolution: " + selectedResolution + " | Fullscreen: " + fullscreen);
            new MainMenu(primaryStage).show();
        });

        // Back Button
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> new MainMenu(primaryStage).show());

        VBox vbox = new VBox(20, resolutionComboBox, fullscreenCheckBox, applyButton, backButton);
        vbox.setAlignment(Pos.CENTER);

        BorderPane root = new BorderPane();
        root.setCenter(vbox);

        primaryStage.setScene(new Scene(root, Config.getResolution().getWidth(), Config.getResolution().getHeight()));
        primaryStage.setFullScreen(Config.isFullscreen());
    }
}

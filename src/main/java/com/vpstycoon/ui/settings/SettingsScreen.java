package com.vpstycoon.ui.settings;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import com.vpstycoon.screen.ScreenResolution;

import com.vpstycoon.config.GameConfig;
import com.vpstycoon.screen.ScreenManager;
import com.vpstycoon.ui.base.GameScreen;
import com.vpstycoon.ui.SceneController;
import com.vpstycoon.ui.navigation.Navigator;

public class SettingsScreen extends GameScreen {
    private final SettingsViewModel viewModel;
    private final Navigator navigator;
    private Label volumeMusicLabel;
    private Label volumeSFXLabel;
    private VBox resolutionBox;
    private ToggleGroup resolutionGroup;

    public SettingsScreen(GameConfig config, ScreenManager screenManager, Navigator navigator) {
        super(config, screenManager);
        this.viewModel = new SettingsViewModel(config);
        this.navigator = navigator;
    }

    @Override
    protected Region createContent() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #2C3E50;");

        // Enforce resolution
        enforceResolution(root);

        // Title
        Label titleLabel = new Label("Settings");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-weight: bold;");

        // Display Settings
        VBox displaySettings = createDisplaySettings();
        
        // Audio Settings
        VBox audioSettings = createAudioSettings();
        
        // Graphics Settings
        VBox graphicsSettings = createGraphicsSettings();

        // Navigation Buttons
        HBox buttonBox = createNavigationButtons();

        // Add all components to root
        root.getChildren().addAll(
            titleLabel,
            createSeparator(),
            displaySettings,
            createSeparator(),
            audioSettings,
            createSeparator(),
            graphicsSettings,
            createSeparator(),
            buttonBox
        );

        return root;
    }

    private VBox createAudioSettings() {
        VBox settings = createSettingsSection("Audio Settings");

        // Music Volume
        VBox musicBox = new VBox(5);
        HBox musicControl = new HBox(10);
        musicControl.setAlignment(Pos.CENTER);
        
        Label musicLabel = new Label("Music Volume:");
        musicLabel.setStyle("-fx-text-fill: white;");
        volumeMusicLabel = new Label("50%");
        volumeMusicLabel.setStyle("-fx-text-fill: white;");
        
        Slider musicSlider = new Slider(0, 100, 50);
        musicSlider.setShowTickLabels(true);
        musicSlider.setShowTickMarks(true);
        musicSlider.setMajorTickUnit(20);
        musicSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            volumeMusicLabel.setText(String.format("%.0f%%", newVal.doubleValue()));
            viewModel.musicVolumeProperty().set(newVal.doubleValue() / 100.0);
        });
        
        musicControl.getChildren().addAll(musicLabel, musicSlider, volumeMusicLabel);
        musicBox.getChildren().add(musicControl);

        // SFX Volume
        VBox sfxBox = new VBox(5);
        HBox sfxControl = new HBox(10);
        sfxControl.setAlignment(Pos.CENTER);
        
        Label sfxLabel = new Label("SFX Volume:");
        sfxLabel.setStyle("-fx-text-fill: white;");
        volumeSFXLabel = new Label("50%");
        volumeSFXLabel.setStyle("-fx-text-fill: white;");
        
        Slider sfxSlider = new Slider(0, 100, 50);
        sfxSlider.setShowTickLabels(true);
        sfxSlider.setShowTickMarks(true);
        sfxSlider.setMajorTickUnit(20);
        sfxSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            volumeSFXLabel.setText(String.format("%.0f%%", newVal.doubleValue()));
            viewModel.sfxVolumeProperty().set(newVal.doubleValue() / 100.0);
        });
        
        sfxControl.getChildren().addAll(sfxLabel, sfxSlider, volumeSFXLabel);
        sfxBox.getChildren().add(sfxControl);

        settings.getChildren().addAll(musicBox, sfxBox);
        return settings;
    }

    private VBox createDisplaySettings() {
        VBox settings = createSettingsSection("Display Settings");

        // Fullscreen toggle
        CheckBox fullscreenCheck = new CheckBox("Fullscreen");
        fullscreenCheck.setStyle("-fx-text-fill: white;");
        fullscreenCheck.selectedProperty().bindBidirectional(viewModel.fullscreenProperty());

        // Resolution dropdown
        HBox resolutionBox = new HBox(10);
        resolutionBox.setAlignment(Pos.CENTER_LEFT);
        Label resolutionLabel = new Label("Screen Resolution:");
        resolutionLabel.setStyle("-fx-text-fill: white;");
        
        ComboBox<ScreenResolution> resolutionComboBox = new ComboBox<>();
        resolutionComboBox.setStyle("""
            -fx-background-color: #34495E;
            -fx-text-fill: white;
            -fx-mark-color: white;
            -fx-font-size: 14px;
            """);
        
        // Add available resolutions to the combo box
        ScreenResolution maxRes = ScreenResolution.getMaxSupportedResolution();
        for (ScreenResolution res : ScreenResolution.values()) {
            if (res.getWidth() <= maxRes.getWidth() && res.getHeight() <= maxRes.getHeight()) {
                resolutionComboBox.getItems().add(res);
            }
        }
        
        // Set current resolution
        resolutionComboBox.setValue(viewModel.resolutionProperty().get());
        
        // Bind resolution selection to viewModel
        resolutionComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                viewModel.resolutionProperty().set(newVal);
            }
        });

        // Bind fullscreen to disable resolution selection
        fullscreenCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            resolutionComboBox.setDisable(newVal);
            if (newVal) {
                resolutionComboBox.setValue(maxRes);
            }
        });

        resolutionBox.getChildren().addAll(resolutionLabel, resolutionComboBox);
        settings.getChildren().addAll(fullscreenCheck, resolutionBox);
        return settings;
    }

    private VBox createGraphicsSettings() {
        VBox settings = createSettingsSection("Graphics Settings");

        // VSync toggle
        CheckBox vsyncCheck = new CheckBox("VSync");
        vsyncCheck.setStyle("-fx-text-fill: white;");
        vsyncCheck.selectedProperty().bindBidirectional(viewModel.vsyncProperty());

        settings.getChildren().add(vsyncCheck);
        return settings;
    }

    private HBox createNavigationButtons() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button applyButton = createButton("Apply");
        Button backButton = createButton("Back");

        applyButton.setOnAction(e -> {
            viewModel.saveSettings();
            SceneController.getInstance().updateResolution();
        });

        backButton.setOnAction(e -> navigator.showMainMenu());

        buttonBox.getChildren().addAll(applyButton, backButton);
        return buttonBox;
    }

    private Button createButton(String text) {
        Button button = new Button(text);
        button.setPrefWidth(120);
        button.setPrefHeight(30);
        button.setStyle("""
            -fx-background-color: #3498DB;
            -fx-text-fill: white;
            -fx-font-size: 14px;
            -fx-font-weight: bold;
            -fx-background-radius: 5;
            """);

        button.setOnMouseEntered(e -> 
            button.setStyle(button.getStyle().replace("#3498DB", "#2980B9"))
        );
        button.setOnMouseExited(e -> 
            button.setStyle(button.getStyle().replace("#2980B9", "#3498DB"))
        );

        return button;
    }

    private VBox createSettingsSection(String title) {
        VBox section = new VBox(15);
        section.setAlignment(Pos.CENTER);
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-font-weight: bold;");
        
        section.getChildren().add(titleLabel);
        return section;
    }

    private Separator createSeparator() {
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #34495E;");
        return separator;
    }
} 
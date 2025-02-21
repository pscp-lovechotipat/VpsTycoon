package com.vpstycoon.ui.settings;

import com.vpstycoon.config.GameConfig;
import com.vpstycoon.event.GameEventBus;
import com.vpstycoon.event.SettingsChangedEvent;
import com.vpstycoon.screen.ScreenManager;
import com.vpstycoon.screen.ScreenResolution;
import com.vpstycoon.ui.base.GameScreen;
import com.vpstycoon.ui.components.buttons.Menu.MenuButton;
import com.vpstycoon.ui.components.buttons.Menu.MenuButtonType;
import com.vpstycoon.ui.navigation.Navigator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class SettingsScreen extends GameScreen {
    private final Navigator navigator;
    private Slider musicVolumeSlider;
    private Slider sfxVolumeSlider;
    private ComboBox<ScreenResolution> resolutionComboBox;
    private CheckBox fullscreenCheckBox;
    private CheckBox vsyncCheckBox;

    public SettingsScreen(GameConfig config, ScreenManager screenManager, Navigator navigator) {
        super(config, screenManager);
        this.navigator = navigator;
    }

    @Override
    protected Region createContent() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: #2C3E50;");

        // Title
        Label titleLabel = createTitleLabel("Settings");
        
        // Settings container
        VBox settingsContainer = createSettingsContainer();
        
        // Buttons
        HBox buttonContainer = createButtonContainer();

        root.getChildren().addAll(titleLabel, settingsContainer, buttonContainer);
        enforceResolution(root);
        
        return root;
    }

    private Label createTitleLabel(String text) {
        Label label = new Label(text);
        label.setStyle("""
            -fx-font-size: 32px;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            """);
        return label;
    }

    private VBox createSettingsContainer() {
        VBox container = new VBox(15);
        container.setStyle("""
            -fx-background-color: rgba(255, 255, 255, 0.1);
            -fx-padding: 20;
            -fx-background-radius: 10;
            """);
        container.setMaxWidth(500);

        // Volume controls
        container.getChildren().addAll(
            createVolumeControls(),
            new Separator(),
            createDisplayControls()
        );

        return container;
    }

    private VBox createVolumeControls() {
        VBox controls = new VBox(10);
        controls.getChildren().addAll(
            createSectionLabel("Audio Settings"),
            createSliderControl("Music Volume", config.getMusicVolume(), slider -> {
                musicVolumeSlider = slider;
                slider.valueProperty().addListener((obs, old, newVal) -> 
                    config.setMusicVolume(newVal.doubleValue())
                );
            }),
            createSliderControl("SFX Volume", config.getSfxVolume(), slider -> {
                sfxVolumeSlider = slider;
                slider.valueProperty().addListener((obs, old, newVal) -> 
                    config.setSfxVolume(newVal.doubleValue())
                );
            })
        );
        return controls;
    }

    private VBox createDisplayControls() {
        VBox controls = new VBox(10);
        
        // Resolution ComboBox
        resolutionComboBox = new ComboBox<>();
        resolutionComboBox.getItems().addAll(ScreenResolution.values());
        resolutionComboBox.setValue(config.getResolution());
        resolutionComboBox.setOnAction(e -> config.setResolution(resolutionComboBox.getValue()));
        
        // Checkboxes
        fullscreenCheckBox = new CheckBox("Fullscreen");
        fullscreenCheckBox.setSelected(config.isFullscreen());
        fullscreenCheckBox.setOnAction(e -> config.setFullscreen(fullscreenCheckBox.isSelected()));
        
        vsyncCheckBox = new CheckBox("V-Sync");
        vsyncCheckBox.setSelected(config.isVsyncEnabled());
        vsyncCheckBox.setOnAction(e -> config.setVsyncEnabled(vsyncCheckBox.isSelected()));

        controls.getChildren().addAll(
            createSectionLabel("Display Settings"),
            createLabeledControl("Resolution", resolutionComboBox),
            createStyledCheckBox(fullscreenCheckBox),
            createStyledCheckBox(vsyncCheckBox)
        );
        
        return controls;
    }

    private Label createSectionLabel(String text) {
        Label label = new Label(text);
        label.setStyle("""
            -fx-font-size: 18px;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            """);
        return label;
    }

    private HBox createSliderControl(String labelText, double initialValue, SliderInitializer initializer) {
        HBox container = new HBox(10);
        container.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill: white;");
        label.setPrefWidth(100);

        Slider slider = new Slider(0, 1, initialValue);
        slider.setStyle("""
            -fx-control-inner-background: #34495E;
            -fx-accent: #3498DB;
            """);
        slider.setPrefWidth(200);

        initializer.initialize(slider);

        container.getChildren().addAll(label, slider);
        return container;
    }

    private HBox createLabeledControl(String labelText, Control control) {
        HBox container = new HBox(10);
        container.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill: white;");
        label.setPrefWidth(100);

        container.getChildren().addAll(label, control);
        return container;
    }

    private CheckBox createStyledCheckBox(CheckBox checkBox) {
        checkBox.setStyle("-fx-text-fill: white;");
        return checkBox;
    }

    private HBox createButtonContainer() {
        HBox container = new HBox(20);
        container.setAlignment(Pos.CENTER);

        MenuButton saveButton = new MenuButton(MenuButtonType.BACK);
        saveButton.setOnAction(e -> {
            config.save();
            GameEventBus.getInstance().publish(new SettingsChangedEvent(config));
            navigator.showMainMenu();
        });

        container.getChildren().add(saveButton);
        return container;
    }

    @FunctionalInterface
    private interface SliderInitializer {
        void initialize(Slider slider);
    }
} 
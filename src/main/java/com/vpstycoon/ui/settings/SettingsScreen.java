package com.vpstycoon.ui.settings;

import com.vpstycoon.FontLoader;
import com.vpstycoon.audio.AudioManager;
import com.vpstycoon.config.GameConfig;
import com.vpstycoon.event.GameEventBus;
import com.vpstycoon.event.SettingsChangedEvent;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.screen.ScreenManager;
import com.vpstycoon.screen.ScreenResolution;
import com.vpstycoon.ui.base.GameScreen;
import com.vpstycoon.ui.components.buttons.Menu.MenuButton;
import com.vpstycoon.ui.components.buttons.Menu.MenuButtonType;
import com.vpstycoon.ui.navigation.Navigator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class SettingsScreen extends GameScreen {
    public final Navigator navigator;
    private Slider musicVolumeSlider;
    private Slider sfxVolumeSlider;
    private ComboBox<ScreenResolution> resolutionComboBox;
    private CheckBox fullscreenCheckBox;
    private CheckBox vsyncCheckBox;
    private AudioManager audioManager;

    public SettingsScreen(GameConfig config, ScreenManager screenManager, Navigator navigator) {
        super(config, screenManager);
        this.navigator = navigator;
        this.audioManager = ResourceManager.getInstance().getAudioManager();
    }

    @Override
    protected Region createContent() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        
        // Updated background to dark cyberpunk gradient
        root.setStyle("""
            -fx-background-color: linear-gradient(to bottom right, #1a0933, #2e0966, #1a0933);
            """);

        // Title with cyber styling
        Label titleLabel = createTitleLabel("Settings");

        // Settings container
        VBox settingsContainer = createSettingsContainer();

        HBox buttonsRow = new HBox(20);
        buttonsRow.setAlignment(Pos.CENTER);

        buttonsRow.getChildren().addAll(this.createBackButton(), this.createApplyButton());

        root.getChildren().addAll(titleLabel, settingsContainer, buttonsRow);
        enforceResolution(root);

        return root;
    }

    private Label createTitleLabel(String text) {
        Label label = new Label(text);
        label.setFont(FontLoader.TITLE_FONT);
        label.setStyle("""
            -fx-text-fill: white;
            -fx-effect: dropshadow(gaussian, #ff00ff, 8, 0.4, 0, 0);
            """);

        return label;
    }

    private VBox createSettingsContainer() {
        VBox container = new VBox(15);
        container.setStyle("""
            -fx-background-color: rgba(30, 15, 50, 0.7);
            -fx-padding: 25;
            -fx-background-radius: 2;
            -fx-border-color: #800fd1, #3498DB;
            -fx-border-width: 2, 1;
            -fx-border-radius: 2;
            -fx-border-insets: 0, 3;
            -fx-effect: dropshadow(gaussian, #800fd1, 15, 0.3, 0, 0);
            """);
        container.setMaxWidth(500);

        // Volume controls
        container.getChildren().addAll(
            createVolumeControls(),
            new Separator() {{
                setStyle("-fx-background-color: #800fd1; -fx-opacity: 0.6;");
            }},
            createDisplayControls()
        );

        return container;
    }

    private VBox createVolumeControls() {
        VBox controls = new VBox(10);
        
        // Add a pixel-art decorative border for audio controls
        controls.setStyle("""
            -fx-border-color: #800fd1;
            -fx-border-width: 2;
            -fx-border-style: segments(2, 2, 2, 2);
            -fx-padding: 10;
            -fx-background-color: rgba(50, 20, 80, 0.3);
            """);
            
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

        // Add a pixel-art decorative border for display controls
        controls.setStyle("""
            -fx-border-color: #800fd1;
            -fx-border-width: 2;
            -fx-border-style: segments(2, 2, 2, 2);
            -fx-padding: 10;
            -fx-background-color: rgba(50, 20, 80, 0.3);
            """);

        // Resolution ComboBox
        resolutionComboBox = new ComboBox<>();
        resolutionComboBox.getItems().addAll(ScreenResolution.values());
        resolutionComboBox.setValue(config.getResolution());
        resolutionComboBox.setOnAction(e -> {
            config.setResolution(resolutionComboBox.getValue());
            GameEventBus.getInstance().publish(new SettingsChangedEvent(config));
        });

        // Checkboxes with cyber styling
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
        label.setFont(FontLoader.SECTION_FONT);
        label.setStyle("""
            -fx-text-fill: #00ffff;
            -fx-effect: dropshadow(gaussian, #00ffff, 5, 0.2, 0, 0);
            """);
        return label;
    }

    private HBox createSliderControl(String labelText, double initialValue, SliderInitializer initializer) {
        HBox container = new HBox(10);
        container.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label(labelText);
        label.setFont(FontLoader.LABEL_FONT);
        label.setStyle("""
           -fx-text-fill: white;
        """);
        label.setPrefWidth(100);

        Slider slider = new Slider(0, 1, initialValue);
        slider.setStyle("""
            -fx-control-inner-background: #1a0933;
            -fx-accent: #800fd1;
            -fx-background-radius: 0;
            -fx-padding: 8;
            -fx-border-color: #3498DB;
            -fx-border-width: 1;
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
        label.setFont(FontLoader.LABEL_FONT);
        label.setStyle("""
           -fx-text-fill: white;
        """);
        label.setPrefWidth(100);

        // Style ComboBox if that's the control type
        if (control instanceof ComboBox) {
            control.setStyle("""
                -fx-background-color: #1a0933;
                -fx-text-fill: white;
                -fx-font-family: "System";
                -fx-border-color: #3498DB;
                -fx-border-width: 1;
                -fx-background-radius: 0;
                -fx-border-radius: 0;
                """);
        }

        container.getChildren().addAll(label, control);
        return container;
    }

    private CheckBox createStyledCheckBox(CheckBox checkBox) {
        checkBox.setFont(FontLoader.LABEL_FONT);
        checkBox.setStyle("""
           -fx-text-fill: white;
           -fx-tick-label-fill: #00ffff;
        """);
        
        // Add a hover effect
        checkBox.setOnMouseEntered(e -> {
            checkBox.setStyle("""
                -fx-text-fill: #00ffff;
                -fx-cursor: hand;
            """);
        });
        
        checkBox.setOnMouseExited(e -> {
            checkBox.setStyle("""
                -fx-text-fill: white;
            """);
        });
        
        return checkBox;
    }

    protected MenuButton createBackButton() {
        MenuButton backButton = new MenuButton(MenuButtonType.BACK);
        backButton.setOnAction(e -> {
            navigator.showMainMenu();
            audioManager.playSoundEffect("click.wav");
        });
        
        // Add additional cyberpunk styling
        styleMenuButton(backButton);
        
        return backButton;
    }

    protected MenuButton createApplyButton() {
        MenuButton applyButton = new MenuButton(MenuButtonType.APPLY);
        applyButton.setOnAction(e -> {
            audioManager.playSoundEffect("click.wav");
            config.save();
            GameEventBus.getInstance().publish(new SettingsChangedEvent(config));
            
            // Force UI refresh after resolution change to prevent white borders
            if (config.getResolution() != null) {
                // Let's use a simple approach - navigate back to main menu and then to settings again
                // This ensures the screen is completely rebuilt with the new resolution
                navigator.showMainMenu();
                
                // Use runLater to ensure navigation completes before returning to settings
                javafx.application.Platform.runLater(() -> {
                    navigator.showSettings();
                });
            }
        });
        
        // Add additional cyberpunk styling
        styleMenuButton(applyButton);
        
        return applyButton;
    }
    
    // Helper method to add additional cyberpunk styling to buttons
    private void styleMenuButton(MenuButton button) {
        // Add glow effect to buttons
        javafx.scene.effect.DropShadow buttonGlow = new javafx.scene.effect.DropShadow();
        buttonGlow.setColor(javafx.scene.paint.Color.rgb(180, 50, 255, 0.7));
        buttonGlow.setRadius(15);
        button.setEffect(buttonGlow);
        
        // Make buttons stand out more
        button.setPrefWidth(200);
    }

    @FunctionalInterface
    private interface SliderInitializer {
        void initialize(Slider slider);
    }
} 
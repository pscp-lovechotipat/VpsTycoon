package com.vpstycoon.ui.settings;

import com.vpstycoon.application.FontLoader;
import com.vpstycoon.audio.AudioManager;
import com.vpstycoon.config.GameConfig;
import com.vpstycoon.event.GameEventBus;
import com.vpstycoon.event.SettingsChangedEvent;
import com.vpstycoon.game.resource.ResourceManager;
import com.vpstycoon.screen.ScreenManager;
import com.vpstycoon.screen.ScreenResolution;
import com.vpstycoon.ui.components.buttons.Menu.MenuButton;
import com.vpstycoon.ui.components.buttons.Menu.MenuButtonType;
import com.vpstycoon.ui.navigation.Navigator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class SettingsScreen extends StackPane {
    private final Navigator navigator;
    private final GameConfig config;
    private final ScreenManager screenManager;
    private final Runnable onCloseSettings;
    private Slider musicVolumeSlider;
    private Slider sfxVolumeSlider;
    private ComboBox<ScreenResolution> resolutionComboBox;
    private CheckBox fullscreenCheckBox;
    private CheckBox vsyncCheckBox;
    private AudioManager audioManager;

    public SettingsScreen(GameConfig config, ScreenManager screenManager, Navigator navigator, Runnable onCloseSettings) {
        this.navigator = navigator;
        this.config = config;
        this.screenManager = screenManager;
        this.onCloseSettings = onCloseSettings;
        this.audioManager = ResourceManager.getInstance().getAudioManager();
        
        
        setViewOrder(-1000);
        
        setupUI();
    }
    
    
    public SettingsScreen(GameConfig config, Navigator navigator, Runnable onCloseSettings) {
        this.navigator = navigator;
        this.config = config;
        this.screenManager = null; 
        this.onCloseSettings = onCloseSettings;
        this.audioManager = ResourceManager.getInstance().getAudioManager();
        
        
        setViewOrder(-1000);
        
        setupUI();
    }

    private void setupUI() {
        
        Rectangle background = new Rectangle();
        background.widthProperty().bind(widthProperty());
        background.heightProperty().bind(heightProperty());
        
        
        background.setFill(Color.rgb(30, 15, 50, 0.9));  
        
        
        background.setStroke(Color.rgb(180, 50, 255, 0.2));
        background.setStrokeWidth(1);

        
        Label titleLabel = createTitleLabel("Settings");

        
        VBox settingsContainer = createSettingsContainer();
        settingsContainer.setMaxWidth(500);
        settingsContainer.setMaxHeight(450);
        
        
        HBox buttonsRow = new HBox(20);
        buttonsRow.setAlignment(Pos.CENTER);
        buttonsRow.getChildren().addAll(createBackButton(), createApplyButton());
        
        
        VBox contentBox = new VBox(20);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(30));
        contentBox.setMaxWidth(450);
        contentBox.setMaxHeight(550);
        
        
        contentBox.setStyle(
            "-fx-background-color: rgba(30, 15, 50, 0.8);" +
            "-fx-background-radius: 2;" +
            "-fx-border-color: #ff00ff, #00ffff;" +
            "-fx-border-width: 2, 1;" +
            "-fx-border-radius: 2;" +
            "-fx-border-insets: 0, 3;" +
            "-fx-effect: dropshadow(gaussian, #ff00ff, 5, 0.2, 0, 0);"
        );
        
        contentBox.getChildren().addAll(titleLabel, settingsContainer, buttonsRow);

        
        BorderPane centerPane = new BorderPane();
        centerPane.setCenter(contentBox);

        getChildren().addAll(background, centerPane);

        
        background.setOnMouseClicked(e -> {
            if (e.getTarget() == background) {
                onCloseSettings.run();
            }
        });
    }

    private synchronized Label createTitleLabel(String text) {
        Label label = new Label(text);
        label.setFont(FontLoader.SUBTITLE_FONT);
        label.setStyle("""
            -fx-text-fill: white;
            -fx-effect: dropshadow(gaussian, #ff00ff, 4, 0.3, 0, 0);
            """);

        
        Glow glow = new Glow(0.8);
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(255, 0, 255, 0.7));
        shadow.setRadius(5);
        label.setEffect(shadow);

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
            -fx-effect: dropshadow(gaussian, #800fd1, 5, 0.3, 0, 0);
            """);

        
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

        controls.setStyle("""
            -fx-border-color: #800fd1;
            -fx-border-width: 2;
            -fx-border-style: segments(2, 2, 2, 2);
            -fx-padding: 10;
            -fx-background-color: rgba(50, 20, 80, 0.3);
            """);

        
        resolutionComboBox = new ComboBox<>();
        resolutionComboBox.getItems().addAll(ScreenResolution.values());
        resolutionComboBox.setValue(config.getResolution());
        resolutionComboBox.setOnAction(e -> {
            config.setResolution(resolutionComboBox.getValue());
        });

        
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
            -fx-effect: dropshadow(gaussian, #00ffff, 3, 0.2, 0, 0);
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

    private MenuButton createBackButton() {
        MenuButton backButton = new MenuButton(MenuButtonType.BACK);
        backButton.setOnAction(e -> {
            audioManager.playSoundEffect("click.wav");
            onCloseSettings.run();
        });
        
        styleMenuButton(backButton);
        
        return backButton;
    }

    private MenuButton createApplyButton() {
        MenuButton applyButton = new MenuButton(MenuButtonType.APPLY);
        applyButton.setOnAction(e -> {
            audioManager.playSoundEffect("click.wav");
            config.save();
            
            
            if (screenManager != null) {
                
                javafx.stage.Stage stage = (javafx.stage.Stage) getScene().getWindow();
                javafx.scene.Scene scene = getScene();
                
                
                screenManager.applySettings(stage, scene);
            }
            
            
            GameEventBus.getInstance().publish(new SettingsChangedEvent(config));
            
            
            onCloseSettings.run();
        });
        
        styleMenuButton(applyButton);
        
        return applyButton;
    }
    
    private void styleMenuButton(MenuButton button) {
        
        DropShadow buttonGlow = new DropShadow();
        buttonGlow.setColor(Color.rgb(180, 50, 255, 0.7));
        buttonGlow.setRadius(5);
        button.setEffect(buttonGlow);
        
        
        button.setPrefWidth(250);
    }

    @FunctionalInterface
    private interface SliderInitializer {
        void initialize(Slider slider);
    }

    public void show() {
        
        if (screenManager == null) {
            System.err.println("Cannot show SettingsScreen as standalone: screenManager is null");
            return;
        }
        
        
        screenManager.switchScreen(this);
    }
} 


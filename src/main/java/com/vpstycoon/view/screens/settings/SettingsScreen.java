package com.vpstycoon.view.screens.settings;

import com.vpstycoon.audio.interfaces.IAudioManager;
import com.vpstycoon.config.GameConfig;
import com.vpstycoon.event.GameEventBus;
import com.vpstycoon.event.SettingsChangedEvent;
import com.vpstycoon.navigation.interfaces.INavigator;
import com.vpstycoon.screen.ScreenManager;
import com.vpstycoon.screen.ScreenResolution;
import com.vpstycoon.service.ResourceManager;
import com.vpstycoon.view.base.GameScreen;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;


public class SettingsScreen extends GameScreen {
    
    private final GameConfig config;
    private final ScreenManager screenManager;
    private final INavigator navigator;
    private final IAudioManager audioManager;
    
    private ComboBox<ScreenResolution> resolutionComboBox;
    private Slider musicVolumeSlider;
    private Slider sfxVolumeSlider;
    
    
    public SettingsScreen(GameConfig config, ScreenManager screenManager, INavigator navigator) {
        this.config = config;
        this.screenManager = screenManager;
        this.navigator = navigator;
        this.audioManager = ResourceManager.getInstance().getAudioManager();
        
        createUI();
    }
    
    
    private void createUI() {
        BorderPane rootPane = new BorderPane();
        rootPane.setStyle("-fx-background-color: #2C3E50;");
        
        
        Label titleLabel = new Label("ตั้งค่า");
        titleLabel.setStyle("-fx-font-size: 24; -fx-text-fill: white;");
        titleLabel.setPadding(new Insets(20, 0, 20, 0));
        BorderPane.setAlignment(titleLabel, Pos.CENTER);
        rootPane.setTop(titleLabel);
        
        
        GridPane settingsGrid = new GridPane();
        settingsGrid.setHgap(10);
        settingsGrid.setVgap(15);
        settingsGrid.setPadding(new Insets(20));
        settingsGrid.setAlignment(Pos.CENTER);
        
        
        Label resolutionLabel = new Label("ความละเอียดหน้าจอ:");
        resolutionLabel.setTextFill(Color.WHITE);
        
        resolutionComboBox = new ComboBox<>();
        resolutionComboBox.getItems().addAll(ScreenResolution.getAvailableResolutions());
        resolutionComboBox.setValue(config.getResolution());
        
        
        Label musicVolumeLabel = new Label("ระดับเสียงเพลง:");
        musicVolumeLabel.setTextFill(Color.WHITE);
        
        musicVolumeSlider = new Slider(0, 1, config.getMusicVolume());
        musicVolumeSlider.setShowTickMarks(true);
        musicVolumeSlider.setShowTickLabels(true);
        musicVolumeSlider.setMajorTickUnit(0.25);
        musicVolumeSlider.setBlockIncrement(0.1);
        
        
        Label sfxVolumeLabel = new Label("ระดับเสียงเอฟเฟกต์:");
        sfxVolumeLabel.setTextFill(Color.WHITE);
        
        sfxVolumeSlider = new Slider(0, 1, config.getSfxVolume());
        sfxVolumeSlider.setShowTickMarks(true);
        sfxVolumeSlider.setShowTickLabels(true);
        sfxVolumeSlider.setMajorTickUnit(0.25);
        sfxVolumeSlider.setBlockIncrement(0.1);
        
        
        settingsGrid.add(resolutionLabel, 0, 0);
        settingsGrid.add(resolutionComboBox, 1, 0);
        settingsGrid.add(musicVolumeLabel, 0, 1);
        settingsGrid.add(musicVolumeSlider, 1, 1);
        settingsGrid.add(sfxVolumeLabel, 0, 2);
        settingsGrid.add(sfxVolumeSlider, 1, 2);
        
        rootPane.setCenter(settingsGrid);
        
        
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20));
        
        Button applyButton = new Button("นำไปใช้");
        applyButton.setOnAction(e -> {
            applySettings();
            audioManager.playSound("click.mp3");
        });
        
        Button backButton = new Button("กลับ");
        backButton.setOnAction(e -> {
            audioManager.playSound("click.mp3");
            navigator.navigateToMainMenu();
        });
        
        buttonBox.getChildren().addAll(applyButton, backButton);
        rootPane.setBottom(buttonBox);
        
        root = rootPane;
    }
    
    
    private void applySettings() {
        
        config.setResolution(resolutionComboBox.getValue());
        
        
        config.setMusicVolume(musicVolumeSlider.getValue());
        config.setSfxVolume(sfxVolumeSlider.getValue());
        
        
        config.save();
        
        
        GameEventBus.getInstance().publish(new SettingsChangedEvent(config));
        
        
        screenManager.updateScreenResolution();
    }

    @Override
    public void onShow() {
        
        audioManager.playSound("menu_open.mp3");
        
        
        resolutionComboBox.setValue(config.getResolution());
        musicVolumeSlider.setValue(config.getMusicVolume());
        sfxVolumeSlider.setValue(config.getSfxVolume());
    }

    @Override
    public void onHide() {
        
    }

    @Override
    public void onResize(double width, double height) {
        
    }
} 

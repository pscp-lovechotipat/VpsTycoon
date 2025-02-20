package com.vpstycoon.ui.settings;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.scene.control.Alert;

import com.vpstycoon.config.GameConfig;
import com.vpstycoon.event.GameEventBus;
import com.vpstycoon.event.SettingsChangedEvent;
import com.vpstycoon.screen.ScreenResolution;

public class SettingsViewModel {
    private final GameConfig config;
    private final ObjectProperty<ScreenResolution> resolution;
    private final BooleanProperty fullscreen;
    private final DoubleProperty musicVolume;
    private final DoubleProperty sfxVolume;
    private final BooleanProperty vsync;
    
    public SettingsViewModel(GameConfig config) {
        this.config = config;
        this.resolution = new SimpleObjectProperty<>(config.getResolution());
        this.fullscreen = new SimpleBooleanProperty(config.isFullscreen());
        this.musicVolume = new SimpleDoubleProperty(config.getMusicVolume());
        this.sfxVolume = new SimpleDoubleProperty(config.getSfxVolume());
        this.vsync = new SimpleBooleanProperty(config.isVsyncEnabled());
        
        setupBindings();
    }
    
    private void setupBindings() {
        resolution.addListener((obs, old, newValue) -> 
            config.setResolution(newValue));
        fullscreen.addListener((obs, old, newValue) -> 
            config.setFullscreen(newValue));
        musicVolume.addListener((obs, old, newValue) -> 
            config.setMusicVolume(newValue.doubleValue()));
        sfxVolume.addListener((obs, old, newValue) -> 
            config.setSfxVolume(newValue.doubleValue()));
        vsync.addListener((obs, old, newValue) -> 
            config.setVsyncEnabled(newValue));
    }
    
    public void saveSettings() {
        Platform.runLater(() -> {
            try {
                config.setResolution(resolution.get());
                config.setFullscreen(fullscreen.get());
                config.setMusicVolume(musicVolume.get());
                config.setSfxVolume(sfxVolume.get());
                config.setVsyncEnabled(vsync.get());
                config.save();
                
                // Publish event to notify other components
                GameEventBus.getInstance().publish(new SettingsChangedEvent(config));
            } catch (Exception e) {
                showErrorDialog("Failed to save settings: " + e.getMessage());
            }
        });
    }
    
    private void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Settings Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Property getters
    public ObjectProperty<ScreenResolution> resolutionProperty() { return resolution; }
    public BooleanProperty fullscreenProperty() { return fullscreen; }
    public DoubleProperty musicVolumeProperty() { return musicVolume; }
    public DoubleProperty sfxVolumeProperty() { return sfxVolume; }
    public BooleanProperty vsyncProperty() { return vsync; }
    
    // ... other view model methods ...
} 
package com.vpstycoon.view.screens.settings;

import com.vpstycoon.audio.AudioManager;
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

/**
 * หน้าจอตั้งค่าของเกม
 */
public class SettingsScreen extends GameScreen {
    
    private final GameConfig config;
    private final ScreenManager screenManager;
    private final INavigator navigator;
    private final AudioManager audioManager;
    
    private ComboBox<ScreenResolution> resolutionComboBox;
    private Slider musicVolumeSlider;
    private Slider sfxVolumeSlider;
    
    /**
     * สร้าง SettingsScreen
     */
    public SettingsScreen(GameConfig config, ScreenManager screenManager, INavigator navigator) {
        this.config = config;
        this.screenManager = screenManager;
        this.navigator = navigator;
        this.audioManager = ResourceManager.getInstance().getAudioManager();
        
        createUI();
    }
    
    // สร้าง UI สำหรับหน้าจอ
    private void createUI() {
        BorderPane rootPane = new BorderPane();
        rootPane.setStyle("-fx-background-color: #2C3E50;");
        
        // หัวข้อ
        Label titleLabel = new Label("ตั้งค่า");
        titleLabel.setStyle("-fx-font-size: 24; -fx-text-fill: white;");
        titleLabel.setPadding(new Insets(20, 0, 20, 0));
        BorderPane.setAlignment(titleLabel, Pos.CENTER);
        rootPane.setTop(titleLabel);
        
        // สร้าง Grid สำหรับตัวเลือกตั้งค่า
        GridPane settingsGrid = new GridPane();
        settingsGrid.setHgap(10);
        settingsGrid.setVgap(15);
        settingsGrid.setPadding(new Insets(20));
        settingsGrid.setAlignment(Pos.CENTER);
        
        // ความละเอียดหน้าจอ
        Label resolutionLabel = new Label("ความละเอียดหน้าจอ:");
        resolutionLabel.setTextFill(Color.WHITE);
        
        resolutionComboBox = new ComboBox<>();
        resolutionComboBox.getItems().addAll(ScreenResolution.getAvailableResolutions());
        resolutionComboBox.setValue(config.getResolution());
        
        // ระดับเสียงเพลง
        Label musicVolumeLabel = new Label("ระดับเสียงเพลง:");
        musicVolumeLabel.setTextFill(Color.WHITE);
        
        musicVolumeSlider = new Slider(0, 1, config.getMusicVolume());
        musicVolumeSlider.setShowTickMarks(true);
        musicVolumeSlider.setShowTickLabels(true);
        musicVolumeSlider.setMajorTickUnit(0.25);
        musicVolumeSlider.setBlockIncrement(0.1);
        
        // ระดับเสียงเอฟเฟกต์
        Label sfxVolumeLabel = new Label("ระดับเสียงเอฟเฟกต์:");
        sfxVolumeLabel.setTextFill(Color.WHITE);
        
        sfxVolumeSlider = new Slider(0, 1, config.getSfxVolume());
        sfxVolumeSlider.setShowTickMarks(true);
        sfxVolumeSlider.setShowTickLabels(true);
        sfxVolumeSlider.setMajorTickUnit(0.25);
        sfxVolumeSlider.setBlockIncrement(0.1);
        
        // เพิ่มส่วนประกอบลงใน GridPane
        settingsGrid.add(resolutionLabel, 0, 0);
        settingsGrid.add(resolutionComboBox, 1, 0);
        settingsGrid.add(musicVolumeLabel, 0, 1);
        settingsGrid.add(musicVolumeSlider, 1, 1);
        settingsGrid.add(sfxVolumeLabel, 0, 2);
        settingsGrid.add(sfxVolumeSlider, 1, 2);
        
        rootPane.setCenter(settingsGrid);
        
        // ปุ่มด้านล่าง
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20));
        
        Button applyButton = new Button("นำไปใช้");
        applyButton.setOnAction(e -> {
            applySettings();
            audioManager.playSoundEffect("click.mp3");
        });
        
        Button backButton = new Button("กลับ");
        backButton.setOnAction(e -> {
            audioManager.playSoundEffect("click.mp3");
            navigator.navigateToMainMenu();
        });
        
        buttonBox.getChildren().addAll(applyButton, backButton);
        rootPane.setBottom(buttonBox);
        
        root = rootPane;
    }
    
    // บันทึกการตั้งค่า
    private void applySettings() {
        // ตั้งค่าความละเอียด
        config.setResolution(resolutionComboBox.getValue());
        
        // ตั้งค่าระดับเสียง
        config.setMusicVolume(musicVolumeSlider.getValue());
        config.setSfxVolume(sfxVolumeSlider.getValue());
        
        // บันทึกการตั้งค่า
        config.save();
        
        // แจ้งเตือนระบบว่ามีการเปลี่ยนแปลงการตั้งค่า
        GameEventBus.getInstance().publish(new SettingsChangedEvent(config));
        
        // อัปเดตหน้าจอ
        screenManager.updateScreenResolution();
    }

    @Override
    public void onShow() {
        // อาจเล่นเสียงเมื่อเข้าหน้าตั้งค่า
        audioManager.playSoundEffect("menu_open.mp3");
        
        // รีเซ็ตค่าให้ตรงกับค่าปัจจุบัน
        resolutionComboBox.setValue(config.getResolution());
        musicVolumeSlider.setValue(config.getMusicVolume());
        sfxVolumeSlider.setValue(config.getSfxVolume());
    }

    @Override
    public void onHide() {
        // ทำความสะอาดทรัพยากร
    }

    @Override
    public void onResize(double width, double height) {
        // ปรับขนาดตามหน้าจอ
    }
} 
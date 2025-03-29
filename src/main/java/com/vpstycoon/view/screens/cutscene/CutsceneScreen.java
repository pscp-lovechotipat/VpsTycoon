package com.vpstycoon.view.screens.cutscene;

import com.vpstycoon.config.GameConfig;
import com.vpstycoon.navigation.interfaces.INavigator;
import com.vpstycoon.screen.ScreenManager;
import com.vpstycoon.view.base.GameScreen;

import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

/**
 * หน้าจอ Cutscene สำหรับแสดงเนื้อเรื่องในเกม
 */
public class CutsceneScreen extends GameScreen {
    
    private final GameConfig config;
    private final ScreenManager screenManager;
    private final INavigator navigator;
    
    /**
     * สร้าง CutsceneScreen
     */
    public CutsceneScreen(GameConfig config, ScreenManager screenManager, INavigator navigator) {
        this.config = config;
        this.screenManager = screenManager;
        this.navigator = navigator;
        
        createUI();
    }
    
    // สร้าง UI สำหรับหน้าจอ
    private void createUI() {
        StackPane rootPane = new StackPane();
        rootPane.setStyle("-fx-background-color: black;");
        
        // สร้างเนื้อหา cutscene แบบง่าย
        VBox contentBox = new VBox(20);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setMaxWidth(800);
        
        Label titleLabel = new Label("วีพีเอส ไทคูน");
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setFont(new Font("System", 48));
        
        Label subtitleLabel = new Label("เริ่มต้นการเดินทางของคุณในฐานะผู้ให้บริการเซิร์ฟเวอร์");
        subtitleLabel.setTextFill(Color.LIGHTGRAY);
        subtitleLabel.setFont(new Font("System", 24));
        subtitleLabel.setTextAlignment(TextAlignment.CENTER);
        subtitleLabel.setWrapText(true);
        
        Button skipButton = new Button("ข้าม");
        skipButton.setOnAction(e -> navigator.navigateToMainMenu());
        
        contentBox.getChildren().addAll(titleLabel, subtitleLabel, skipButton);
        
        rootPane.getChildren().add(contentBox);
        
        root = rootPane;
        
        // เพิ่ม transition
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(2), rootPane);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    @Override
    public void onShow() {
        // อาจเพิ่มคำสั่งเริ่มเพลงหรือแอนิเมชัน
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
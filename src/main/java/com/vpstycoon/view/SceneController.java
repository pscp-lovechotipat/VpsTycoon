package com.vpstycoon.view;

import com.vpstycoon.config.GameConfig;
import com.vpstycoon.screen.ScreenManager;
import com.vpstycoon.screen.ScreenResolution;
import com.vpstycoon.view.interfaces.ISceneController;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * คลาสควบคุมการแสดงผล Scene หลักของแอปพลิเคชัน
 */
public class SceneController implements ISceneController {
    private static SceneController instance;
    private final Stage stage;
    private final GameConfig config;
    private final ScreenManager screenManager;
    private final StackPane rootContainer;
    private Scene mainScene;

    private SceneController(Stage stage, GameConfig config, ScreenManager screenManager) {
        this.stage = stage;
        this.config = config;
        this.screenManager = screenManager;
        this.rootContainer = new StackPane();
        
        initializeScene();
    }

    /**
     * ดึง instance ของ SceneController
     */
    public static SceneController getInstance() {
        return instance;
    }

    /**
     * เริ่มต้น SceneController
     */
    public static void initialize(Stage stage, GameConfig config, ScreenManager screenManager) {
        if (instance == null) {
            instance = new SceneController(stage, config, screenManager);
        }
    }

    // เริ่มต้น Scene
    private void initializeScene() {
        // ตั้งค่าสีพื้นหลังเป็นสีดำ
        rootContainer.setStyle("-fx-background-color: black;");
        
        // สร้าง scene กับพื้นหลังสีดำเพื่อหลีกเลี่ยงขอบสีขาว
        mainScene = new Scene(rootContainer);
        mainScene.setFill(Color.BLACK);
        
        // ตั้งค่าขนาดของ stage ให้เท่ากับ scene
        stage.setScene(mainScene);
        
        // ใช้ความละเอียดเริ่มต้น
        updateResolution();
    }

    /**
     * ตั้งค่าเนื้อหาใน Scene
     */
    @Override
    public void setContent(Parent content) {
        rootContainer.getChildren().clear();
        rootContainer.getChildren().add(content);
    }

    /**
     * อัปเดตความละเอียดของหน้าจอ
     */
    @Override
    public void updateResolution() {
        ScreenResolution resolution = config.getResolution();
        
        // ตั้งค่าขนาดของ container
        setRegionSize(rootContainer, resolution.getWidth(), resolution.getHeight());
        
        // นำการตั้งค่าไปใช้กับ stage
        screenManager.applySettings(stage, mainScene);
        
        // แน่ใจว่าขนาดของ scene ตรงกับ stage
        mainScene.setFill(Color.BLACK);
        
        // บังคับให้มีการวาดเนื้อหาใหม่
        rootContainer.requestLayout();
        
        // ถ้ามีเนื้อหาใดๆ ให้ปรับขนาดให้เหมาะกับความละเอียดใหม่
        if (!rootContainer.getChildren().isEmpty()) {
            for (javafx.scene.Node child : rootContainer.getChildren()) {
                if (child instanceof Region) {
                    Region region = (Region) child;
                    setRegionSize(region, resolution.getWidth(), resolution.getHeight());
                    region.requestLayout();
                }
            }
        }
    }
    
    /**
     * ตั้งค่าขนาดของ Region
     */
    private void setRegionSize(Region region, double width, double height) {
        region.setPrefWidth(width);
        region.setPrefHeight(height);
        region.setMinWidth(width);
        region.setMinHeight(height);
        region.setMaxWidth(width);
        region.setMaxHeight(height);
    }
    
    /**
     * ดึง Stage หลัก
     */
    @Override
    public Stage getStage() {
        return stage;
    }
    
    /**
     * ดึง Scene หลัก
     */
    @Override
    public Scene getScene() {
        return mainScene;
    }
} 
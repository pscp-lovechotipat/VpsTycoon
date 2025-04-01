package com.vpstycoon.view.interfaces;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public interface ISceneController {
    
    
    void setContent(Parent content);
    
    
    void updateResolution();
    
    
    Stage getStage();
    
    
    Scene getScene();
} 


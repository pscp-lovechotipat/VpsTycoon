package com.vpstycoon.view.base;

import com.vpstycoon.view.interfaces.IGameScreen;
import javafx.scene.Parent;


public abstract class GameScreen implements IGameScreen {
    
    protected Parent root;
    
    
    @Override
    public Parent getRoot() {
        return root;
    }
    
    
    @Override
    public abstract void onShow();
    
    
    @Override
    public abstract void onHide();
    
    
    @Override
    public abstract void onResize(double width, double height);
} 


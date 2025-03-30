package com.vpstycoon.view.interfaces;

import javafx.scene.Parent;


public interface IGameScreen extends IView {
    
    
    Parent getRoot();
    
    
    void onShow();
    
    
    void onHide();
    
    
    void onResize(double width, double height);
} 

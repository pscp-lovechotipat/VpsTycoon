package com.vpstycoon.ui.game.desktop;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

public class DesktopIcon extends VBox {
    private final String name;
    private final Runnable onClick;
    
    public DesktopIcon(String iconCode, String name, Runnable onClick) {
        this.name = name;
        this.onClick = onClick;
        
        setAlignment(Pos.CENTER);
        setSpacing(5);
        
        
        FontIcon icon = FontIcon.of(FontAwesomeSolid.COMMENTS);
        icon.setIconSize(32);
        icon.setIconColor(javafx.scene.paint.Color.WHITE);
        
        
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-text-fill: white;");
        
        getChildren().addAll(icon, nameLabel);
        
        
        setStyle("-fx-padding: 10; -fx-cursor: hand;");
        
        
        setOnMouseClicked(e -> onClick.run());
        
        
        setOnMouseEntered(e -> setStyle("-fx-padding: 10; -fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 5;"));
        setOnMouseExited(e -> setStyle("-fx-padding: 10;"));
    }
} 


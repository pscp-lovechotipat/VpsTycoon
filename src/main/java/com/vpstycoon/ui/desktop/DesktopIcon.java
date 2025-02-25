package com.vpstycoon.ui.desktop;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

public class DesktopIcon extends VBox {
    private final String name;
    private final Runnable onClick;
    
    public DesktopIcon(String iconCode, String name, Runnable onClick) {
        this.name = name;
        this.onClick = onClick;
        
        setAlignment(Pos.CENTER);
        setSpacing(5);
        
        // Create icon using FontAwesome
        FontIcon icon = FontIcon.of(FontAwesomeSolid.COMMENTS);
        icon.setIconSize(32);
        icon.setIconColor(javafx.scene.paint.Color.WHITE);
        
        // Create label
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-text-fill: white;");
        
        getChildren().addAll(icon, nameLabel);
        
        // Style
        setStyle("-fx-padding: 10; -fx-cursor: hand;");
        
        // Click handler
        setOnMouseClicked(e -> onClick.run());
        
        // Hover effect
        setOnMouseEntered(e -> setStyle("-fx-padding: 10; -fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 5;"));
        setOnMouseExited(e -> setStyle("-fx-padding: 10;"));
    }
} 
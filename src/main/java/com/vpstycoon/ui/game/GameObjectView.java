package com.vpstycoon.ui.game;

import com.vpstycoon.game.GameObject;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class GameObjectView extends StackPane {
    private final Circle background;
    private final GameObject gameObject;

    public GameObjectView(GameObject gameObject) {
        this.gameObject = gameObject;

        
        Color buttonColor;
        switch (gameObject.getName().toLowerCase()) {
            case "marketing":
                buttonColor = Color.web("#28a745"); 
                break;
            case "security":
                buttonColor = Color.web("#6f42c1"); 
                break;
            case "network":
                buttonColor = Color.web("#007bff"); 
                break;
            case "server":
                buttonColor = Color.web("#f39c12"); 
                break;
            case "database":
                buttonColor = Color.web("#e74c3c"); 
                break;
            default:
                buttonColor = Color.LIGHTGRAY; 
        }

        
        background = new Circle(50);
        background.setFill(buttonColor);
        background.setStroke(Color.TRANSPARENT);
        background.setStrokeWidth(2);

        Label label = new Label(gameObject.getName());
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");

        getChildren().addAll(background, label);
        setTranslateX(gameObject.getX());
        setTranslateY(gameObject.getY());

        setOnMouseEntered(e -> {
            background.setStroke(Color.WHITE);
            background.setEffect(new DropShadow());
        });

        setOnMouseExited(e -> {
            background.setStroke(Color.TRANSPARENT);
            background.setEffect(null);
        });
        
        setTranslateX(gameObject.getX());
        setTranslateY(gameObject.getY() - 100); 
    }
}

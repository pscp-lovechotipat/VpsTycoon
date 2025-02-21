package com.vpstycoon.ui.game;

import com.vpstycoon.game.GameObject;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class GameObjectView extends StackPane {
    private final Rectangle background;
    private final GameObject gameObject;

    public GameObjectView(GameObject gameObject) {
        this.gameObject = gameObject;
        
        // สร้างพื้นหลัง
        background = new Rectangle(100, 100);
        background.setFill(Color.LIGHTBLUE);
        background.setStroke(Color.TRANSPARENT);
        background.setStrokeWidth(2);
        
        // สร้าง label
        Label label = new Label(gameObject.getName());
        label.setStyle("-fx-font-weight: bold;");

        getChildren().addAll(background, label);
        setTranslateX(gameObject.getX());
        setTranslateY(gameObject.getY());

        // เพิ่ม hover effect
        setOnMouseEntered(e -> {
            background.setStroke(Color.BLUE);
            background.setEffect(new DropShadow());
        });

        setOnMouseExited(e -> {
            background.setStroke(Color.TRANSPARENT);
            background.setEffect(null);
        });
    }
} 
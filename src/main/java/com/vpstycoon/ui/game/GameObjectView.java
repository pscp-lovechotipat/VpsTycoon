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

        // ✅ กำหนดสีของปุ่มตามประเภทของ GameObject
        Color buttonColor;
        switch (gameObject.getName().toLowerCase()) {
            case "marketing":
                buttonColor = Color.web("#28a745"); // 🟢 Green
                break;
            case "security":
                buttonColor = Color.web("#6f42c1"); // 🟣 Purple
                break;
            case "network":
                buttonColor = Color.web("#007bff"); // 🔵 Blue
                break;
            case "server":
                buttonColor = Color.web("#f39c12"); // 🟠 Orange (ตัวอย่าง)
                break;
            case "database":
                buttonColor = Color.web("#e74c3c"); // 🔴 Red (ตัวอย่าง)
                break;
            default:
                buttonColor = Color.LIGHTGRAY; // ค่าเริ่มต้น (เป็นสีเทา)
        }

        // เปลี่ยนพื้นหลังเป็นวงกลม และใช้สีที่กำหนด
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
        // ✅ ปรับตำแหน่งให้อยู่สูงขึ้นไป (เลื่อนขึ้น)
        setTranslateX(gameObject.getX());
        setTranslateY(gameObject.getY() - 100); // เลื่อนขึ้นไป 100 px
    }
}
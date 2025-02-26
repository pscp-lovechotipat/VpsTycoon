package com.vpstycoon.ui.utils;

import javafx.scene.control.Button;

public class ButtonUtils {
    // สร้างปุ่มแบบโมดัล (พร้อม hover effect)
    public static Button createModalButton(String text) {
        Button button = new Button(text);
        button.setStyle("""
                -fx-background-color: #3498DB;
                -fx-text-fill: white;
                -fx-font-size: 14px;
                -fx-padding: 8 20;
                -fx-background-radius: 5;
                -fx-min-width: 120;
                """);

        button.setOnMouseEntered(e ->
                button.setStyle(button.getStyle().replace("#3498DB", "#2980B9"))
        );

        button.setOnMouseExited(e ->
                button.setStyle(button.getStyle().replace("#2980B9", "#3498DB"))
        );

        return button;
    }

    // สร้างปุ่มทั่วไป
    public static Button createButton(String text) {
        Button button = new Button(text);
        button.setStyle("""
                -fx-background-color: #3498DB;
                -fx-text-fill: white;
                -fx-font-size: 14px;
                -fx-padding: 8px 15px;
                -fx-background-radius: 5;
                """);
        return button;
    }
}

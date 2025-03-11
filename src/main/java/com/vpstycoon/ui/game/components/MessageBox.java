package com.vpstycoon.ui.game.components;

import com.vpstycoon.FontLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class MessageBox extends StackPane {
    public MessageBox(String text) {
        setMinSize(250, 30); // 🔹 กำหนดขนาดกล่อง
        setMaxSize(250, 30);

        setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, new CornerRadii(3), null)));
        setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(3), new BorderWidths(1))));

        Label message = new Label(text);
        message.setFont(FontLoader.SECTION_FONT);
        message.setStyle("-fx-text-fill: black; -fx-padding: 10;");

        getChildren().add(message);
        setAlignment(Pos.CENTER);
    }
}
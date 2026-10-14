package com.vpstycoon.ui.game.messenger;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

final class CustomerAvatar {
    private CustomerAvatar() {
    }

    static Color colorFor(String customerName) {
        int nameHash = customerName.hashCode();
        int r = Math.abs(nameHash % 100) + 100;
        int g = Math.abs((nameHash / 100) % 100);
        int b = Math.abs((nameHash / 10000) % 100) + 100;
        return Color.rgb(r, g, b);
    }

    static Rectangle create(String customerName) {
        Rectangle avatar = new Rectangle(30, 30);
        avatar.setArcWidth(5);
        avatar.setArcHeight(5);
        avatar.setFill(colorFor(customerName));
        avatar.setStroke(Color.web("#9e33ff"));
        avatar.setStrokeWidth(1);
        return avatar;
    }
}

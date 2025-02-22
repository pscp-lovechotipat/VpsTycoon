package com.vpstycoon.ui.components.buttons.Menu;

import com.vpstycoon.resource.ResourceManager;
import javafx.scene.control.Button;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import java.net.URL;

public class MenuButton extends Button {
    private static final double WIDTH = 160;
    private static final double HEIGHT = 40;

    private MenuButtonType type;

    public MenuButton(MenuButtonType type) {
        this.type = type;
        this.render();
    }

    private void render() {
        this.setPrefSize(WIDTH, HEIGHT);

        // โหลด GIF สำหรับปุ่ม
        String gifPath = "/images/buttons/" + type.getValue() + ".gif";
        URL gifUrl = ResourceManager.getResource(gifPath);

        ImageView imageView = new ImageView(new Image(gifUrl.toExternalForm()));

        imageView.setFitWidth(WIDTH);
        imageView.setFitHeight(HEIGHT);
        imageView.setPreserveRatio(true);

        // ตั้งค่า graphic
        this.setGraphic(imageView);

        // สไตล์พื้นฐานของปุ่ม
        this.setStyle("""
            -fx-background-color: transparent;
            -fx-background-radius: 0;
            -fx-border-color: transparent;
            -fx-border-width: 2;
            -fx-padding: 0;
            """);

        // เพิ่ม hover effect ด้วย stroke
        this.setOnMouseEntered(e ->
                this.setEffect(neon())
        );

        this.setOnMouseExited(e ->
                this.setEffect(null)
        );
    }

    private Effect neon() {
        Glow glow = new Glow(1);
        DropShadow neonShadow = new DropShadow(20, Color.rgb(145, 0, 255, 0.6));
        ColorAdjust colorAdjust = new ColorAdjust();

        neonShadow.setSpread(.2);
        colorAdjust.setBrightness(.3);
        colorAdjust.setSaturation(.4);

        glow.setInput(colorAdjust);
        neonShadow.setInput(glow);
        return neonShadow;
    }

//    private void renderFallback() {
//
//    }

}

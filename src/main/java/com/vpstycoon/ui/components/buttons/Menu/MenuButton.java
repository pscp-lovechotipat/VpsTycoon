package com.vpstycoon.ui.components.buttons.Menu;

import com.vpstycoon.audio.AudioManager;
import com.vpstycoon.game.resource.ResourceManager;
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
    private AudioManager audioManager;

    private MenuButtonType type;

    public MenuButton(MenuButtonType type) {
        this.audioManager = AudioManager.getInstance();
        this.type = type;
        this.render();
    }

    private void render() {
        this.setPrefSize(WIDTH, HEIGHT);

        String gifPath = "/images/buttons/" + type.getValue() + ".gif";
        URL gifUrl = ResourceManager.getResource(gifPath);

        if (gifUrl != null) {
            try {
                Image image = new Image(gifUrl.toExternalForm());
                ImageView imageView = new ImageView(image);

                imageView.setFitWidth(WIDTH);
                imageView.setFitHeight(HEIGHT);
                imageView.setPreserveRatio(true);

                this.setGraphic(imageView);

                this.setStyle("""
                        -fx-background-color: transparent;
                        -fx-background-radius: 0;
                        -fx-border-color: transparent;
                        -fx-border-width: 2;
                        -fx-padding: 0;
                        """);

            } catch (Exception e) {
                this.setGraphic(null);
                this.setWidth(200);
                this.setHeight(50);
                this.setStyle("""
                        -fx-background-color: #800fd1;
                        -fx-background-radius: 15;
                        -fx-border-color: transparent;
                        -fx-border-width: 2;
                        -fx-padding: 0;
                        """);
            }
        } else {
            this.setGraphic(null);
            this.setText(type.getValue());
            this.setStyle("""
                    -fx-background-color: #800fd1;
                    -fx-background-radius: 15;
                    -fx-border-color: transparent;
                    -fx-border-width: 2;
                    -fx-color: #ffffff;
                    -fx-padding: 0;
                    """);
        }

        // เพิ่ม hover effect ด้วย stroke
        this.setOnMouseEntered(e -> {
                this.setEffect(neon());
                audioManager.playSoundEffect("hover2.wav");
        }
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

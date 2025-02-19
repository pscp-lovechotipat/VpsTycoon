package com.vpstycoon.ui;

import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.effect.Glow;
import javafx.scene.paint.Color;

public class NeonShadow {
    public Effect neon() {
        Glow glow = new Glow(1);
        DropShadow neonShadow = new DropShadow(10, Color.rgb(145, 0, 255, 0.6));
        ColorAdjust colorAdjust = new ColorAdjust();

        neonShadow.setSpread(0.2);
        colorAdjust.setBrightness(0.3);
        colorAdjust.setSaturation(0.4);

        glow.setInput(colorAdjust);
        neonShadow.setInput(glow);
        return neonShadow;
    }
}

package com.vpstycoon;

import javafx.scene.text.Font;

import java.util.Objects;

public class FontLoader {
    public static Font TITLE_FONT;
    public static Font SECTION_FONT;
    public static Font LABEL_FONT;

    static {
        TITLE_FONT = loadFont(50);
        SECTION_FONT = loadFont(30);
        LABEL_FONT = loadFont(22);
    }

    private static Font loadFont(double size) {
        return Font.loadFont(
                Objects.requireNonNull(FontLoader.class.getResource("/fonts/Px_basic_font3-Regular.otf")).toExternalForm(), size
        );
    }
}

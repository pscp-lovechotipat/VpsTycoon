package com.vpstycoon.ui.common;

import javafx.scene.text.Font;

import java.util.Objects;

public final class FontLoader {
    public static final Font TITLE_FONT = loadFont(50);
    public static final Font SUBTITLE_FONT = loadFont(42);
    public static final Font SECTION_FONT = loadFont(30);
    public static final Font LABEL_FONT = loadFont(22);

    private FontLoader() {
    }

    public static Font loadFont(double size) {
        String path = Objects.requireNonNull(
                FontLoader.class.getResource("/fonts/Px_basic_font3-Regular.ttf"), "Font not found"
        ).toExternalForm();
        Font font = Font.loadFont(path, size);
        if (font == null) {
            System.out.println("Failed to load font at: " + path);
        }
        return font;
    }
}

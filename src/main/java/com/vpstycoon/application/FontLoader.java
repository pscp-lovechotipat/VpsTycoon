package com.vpstycoon.application;

import java.util.Objects;

import javafx.scene.text.Font;

public class FontLoader {
    public static Font TITLE_FONT;
    public static Font SUBTITLE_FONT;
    public static Font SECTION_FONT;
    public static Font LABEL_FONT;

    static {
        TITLE_FONT = loadFont(50);
        SUBTITLE_FONT = loadFont(42);
        SECTION_FONT = loadFont(30);
        LABEL_FONT = loadFont(22);
    }

    
    public static Font loadFont(double size) {
        String path = Objects.requireNonNull(
                FontLoader.class.getResource("/fonts/Px_basic_font3-Regular.ttf"), "ไม่พบไฟล์ฟอนต์"
        ).toExternalForm();
        Font font = Font.loadFont(path, size);
        if (font == null) {
            System.out.println("โหลดฟอนต์ล้มเหลว: " + path);
        }
        return font;
    }
} 

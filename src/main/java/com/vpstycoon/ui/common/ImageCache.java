package com.vpstycoon.ui.common;

import javafx.scene.image.Image;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ImageCache {
    private static final Map<String, Image> CACHE = new ConcurrentHashMap<>();

    public static Image get(String path) {
        Image cached = CACHE.get(path);
        if (cached != null) {
            return cached;
        }
        URL url = ImageCache.class.getResource(path);
        if (url == null) {
            System.err.println("ไม่พบรูปภาพ: " + path);
            return null;
        }
        Image image = new Image(url.toExternalForm());
        CACHE.put(path, image);
        return image;
    }
}

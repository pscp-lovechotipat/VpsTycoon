package com.vpstycoon.ui.common;

import javafx.scene.image.Image;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ImageCache {
    private static final Map<String, Image> CACHE = new ConcurrentHashMap<>();

    private ImageCache() {
    }

    public static Image get(String path) {
        return CACHE.computeIfAbsent(path, ImageCache::load);
    }

    private static Image load(String path) {
        URL url = ImageCache.class.getResource(path);
        if (url == null) {
            System.err.println("ไม่พบรูปภาพ: " + path);
            return null;
        }
        return new Image(url.toExternalForm());
    }
}

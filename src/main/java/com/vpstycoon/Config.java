package com.vpstycoon;

import java.io.File;

public class Config {
    private static ScreenResolution selectedResolution = ScreenResolution.FULL_HD;
    private static boolean isFullscreen = false;
    private static final String SAVE_FILE = "savegame.dat";

    public static ScreenResolution getResolution() {
        if (selectedResolution == null) {
            selectedResolution = ScreenResolution.FULL_HD;
        }
        return selectedResolution;
    }

    public static void setResolution(ScreenResolution resolution) {
        selectedResolution = resolution;
    }

    public static boolean isFullscreen() {
        return isFullscreen;
    }

    public static void setFullscreen(boolean fullscreen) {
        isFullscreen = fullscreen;
    }

    // Check if a save file exists
    public static boolean hasSavedGame() {
        return new File(SAVE_FILE).exists();
    }

    // Create a save file to indicate game progress
    public static void saveGame() {
        try {
            new File(SAVE_FILE).createNewFile();
        } catch (Exception e) {
            System.err.println("Error saving game: " + e.getMessage());
        }
    }

    // Delete save file to reset progress
    public static void deleteSave() {
        new File(SAVE_FILE).delete();
    }
}

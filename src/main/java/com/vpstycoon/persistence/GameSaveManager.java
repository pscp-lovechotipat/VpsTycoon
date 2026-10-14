package com.vpstycoon.persistence;

import com.vpstycoon.model.GameState;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class GameSaveManager {
    static final String GAME_FOLDER = System.getProperty("user.home") + File.separator + "Documents" + File.separator + "VpsTycoon";
    static final String SAVE_FILE = GAME_FOLDER + File.separator + "savegame.dat";
    static final String CHAT_HISTORY_FILE = GAME_FOLDER + File.separator + "save.dat";

    public GameSaveManager() {
        ensureGameFolder();
    }

    static void ensureGameFolder() {
        try {
            File gameDir = new File(GAME_FOLDER);
            if (!gameDir.exists()) {
                boolean created = gameDir.mkdirs();
                if (created) {
                    System.out.println("สร้างโฟลเดอร์เกม: " + gameDir.getAbsolutePath());
                } else {
                    System.err.println("ไม่สามารถสร้างโฟลเดอร์เกม: " + gameDir.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            System.err.println("เกิดข้อผิดพลาดในการสร้างโฟลเดอร์: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void save(GameState state) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            oos.writeObject(state);
        }
        System.out.println("Game saved successfully to: " + SAVE_FILE);
    }

    public GameState load() throws IOException, ClassNotFoundException {
        File saveFile = new File(SAVE_FILE);
        if (!saveFile.exists() || saveFile.length() == 0) {
            return null;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(saveFile))) {
            GameState state = (GameState) ois.readObject();
            System.out.println("โหลดข้อมูลเกมสำเร็จ จาก: " + saveFile.getAbsolutePath());
            return state;
        }
    }

    public boolean saveExists() {
        return new File(SAVE_FILE).exists();
    }

    public void deleteSave() {
        deleteFile(SAVE_FILE, "ไฟล์บันทึก");
    }

    public void deleteAllSaves() {
        deleteSave();
        deleteFile(CHAT_HISTORY_FILE, "ไฟล์บันทึกประวัติแชท");
    }

    private static void deleteFile(String path, String description) {
        File file = new File(path);
        if (file.exists()) {
            if (file.delete()) {
                System.out.println("ลบ" + description + "สำเร็จ: " + file.getAbsolutePath());
            } else {
                System.err.println("ลบ" + description + "ไม่สำเร็จ: " + file.getAbsolutePath());
            }
        }
    }
}

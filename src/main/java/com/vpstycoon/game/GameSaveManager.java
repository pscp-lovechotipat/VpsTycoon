package com.vpstycoon.game;

import java.io.*;

public class GameSaveManager {
    private static final String GAME_FOLDER = System.getProperty("user.home") + File.separator + "Documents" + File.separator + "VpsTycoon";
    private static final String SAVE_FILE = GAME_FOLDER + File.separator + "savegame.dat";
    private static final String CHAT_SAVE_FILE = GAME_FOLDER + File.separator + "save.dat";
    private static final String BACKUP_DIR = GAME_FOLDER + File.separator + "backups";

    public GameSaveManager() {
        createGameDirectory();
    }

    private void createGameDirectory() {
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

            File backupDir = new File(BACKUP_DIR);
            if (!backupDir.exists()) {
                boolean created = backupDir.mkdirs();
                if (created) {
                    System.out.println("สร้างโฟลเดอร์สำรองข้อมูล: " + backupDir.getAbsolutePath());
                } else {
                    System.err.println("ไม่สามารถสร้างโฟลเดอร์สำรองข้อมูล: " + backupDir.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            System.err.println("เกิดข้อผิดพลาดในการสร้างโฟลเดอร์: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteGame() {
        File saveFile = new File(SAVE_FILE);
        if (saveFile.exists()) {
            boolean deleted = saveFile.delete();
            if (deleted) {
                System.out.println("ลบไฟล์บันทึกสำเร็จ: " + saveFile.getAbsolutePath());
            } else {
                System.err.println("ลบไฟล์บันทึกไม่สำเร็จ: " + saveFile.getAbsolutePath());
            }
        }

        File chatSaveFile = new File(CHAT_SAVE_FILE);
        if (chatSaveFile.exists()) {
            boolean deleted = chatSaveFile.delete();
            if (deleted) {
                System.out.println("ลบไฟล์บันทึกประวัติแชทสำเร็จ: " + chatSaveFile.getAbsolutePath());
            } else {
                System.err.println("ลบไฟล์บันทึกประวัติแชทไม่สำเร็จ: " + chatSaveFile.getAbsolutePath());
            }
        }
    }

    public boolean saveExists() {
        return new File(SAVE_FILE).exists();
    }
}

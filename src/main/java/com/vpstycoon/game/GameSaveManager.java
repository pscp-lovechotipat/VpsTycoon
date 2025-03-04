package com.vpstycoon.game;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GameSaveManager {
    private static final String SAVE_FILE = "savegame.dat"; // ไฟล์หลักสำหรับบันทึก
    private static final String BACKUP_DIR = "backups";     // โฟลเดอร์สำหรับเก็บไฟล์สำรอง

    public GameSaveManager() {
        createBackupDirectory();
    }

    // สร้างโฟลเดอร์ backups ถ้ายังไม่มี
    private void createBackupDirectory() {
        File backupDir = new File(BACKUP_DIR);
        if (!backupDir.exists()) {
            backupDir.mkdir();
        }
    }

    // ฟังก์ชันบันทึกเกม
    public void saveGame(GameState state) {
        try {
            File saveFile = new File(SAVE_FILE);

            // ถ้ามีไฟล์บันทึกอยู่แล้ว ให้สร้างสำเนาก่อน
            if (saveFile.exists() && saveFile.length() > 0) {
                createBackup();
            }

            // ใช้ ObjectOutputStream เพื่อบันทึกอ็อบเจ็กต์
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(saveFile))) {
                oos.writeObject(state);
            }
            System.out.println("บันทึกเกมสำเร็จ: " + saveFile.getAbsolutePath());

        } catch (IOException e) {
            System.err.println("บันทึกเกมล้มเหลว: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ฟังก์ชันลบไฟล์บันทึก
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
    }

    // สร้างไฟล์สำรองก่อนบันทึกทับ
    private void createBackup() {
        try {
            File sourceFile = new File(SAVE_FILE);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            File backupFile = new File(BACKUP_DIR + "/savegame_" + timestamp + ".bak");
            Files.copy(sourceFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ฟังก์ชันโหลดเกม
    public GameState loadGame() {
        File saveFile = new File(SAVE_FILE);

        try {
            // ถ้าไม่มีไฟล์บันทึกหรือไฟล์ว่าง ให้คืนค่า GameState ใหม่
            if (!saveFile.exists() || saveFile.length() == 0) {
                System.out.println("ไม่มีไฟล์บันทึก");
                return new GameState();
            }

            // ใช้ ObjectInputStream เพื่อโหลดอ็อบเจ็กต์
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(saveFile))) {
                GameState state = (GameState) ois.readObject();
                if (state == null) {
                    System.err.println("ข้อมูลบันทึกว่างเปล่า สร้างเกมใหม่");
                    return new GameState();
                }
                System.out.println("โหลดเกมสำเร็จ: " + saveFile.getAbsolutePath());
                return state;
            }

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("โหลดเกมล้มเหลว: " + e.getMessage());
            e.printStackTrace();
            createCorruptedFileBackup(saveFile); // สำรองไฟล์ที่เสียหาย
            return new GameState();
        }
    }

    // สำรองไฟล์ที่เสียหาย
    private void createCorruptedFileBackup(File originalFile) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            File backupFile = new File(BACKUP_DIR + "/corrupted_save_" + timestamp + ".bak");
            Files.copy(originalFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("สำรองไฟล์ที่เสียหายล้มเหลว: " + e.getMessage());
        }
    }

    // ตรวจสอบว่าไฟล์บันทึกมีอยู่หรือไม่
    public boolean saveExists() {
        return new File(SAVE_FILE).exists();
    }
}
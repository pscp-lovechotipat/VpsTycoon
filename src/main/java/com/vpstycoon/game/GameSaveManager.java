package com.vpstycoon.game;

import com.vpstycoon.ui.game.desktop.messenger.models.ChatHistoryManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    
    public void saveGame(GameState state) {
        try {
            File saveFile = new File(SAVE_FILE);

            
            if (saveFile.exists() && saveFile.length() > 0) {
                createBackup();
            }

            
            saveChatHistory(state);

            
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(saveFile))) {
                oos.writeObject(state);
            }
            System.out.println("บันทึกเกมสำเร็จ: " + saveFile.getAbsolutePath());

        } catch (IOException e) {
            System.err.println("บันทึกเกมล้มเหลว: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
    private void saveChatHistory(GameState state) {
        
        ChatHistoryManager chatManager = ChatHistoryManager.getInstance();
        if (chatManager != null) {
            
            chatManager.saveChatHistory();
            System.out.println("บันทึกประวัติแชทสำเร็จ: " + new File(CHAT_SAVE_FILE).getAbsolutePath());
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

    
    private void createBackup() {
        try {
            File sourceFile = new File(SAVE_FILE);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            File backupFile = new File(BACKUP_DIR + "/savegame_" + timestamp + ".bak");
            Files.copy(sourceFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            
            
            File chatSourceFile = new File(CHAT_SAVE_FILE);
            if (chatSourceFile.exists()) {
                File chatBackupFile = new File(BACKUP_DIR + "/save_" + timestamp + ".bak");
                Files.copy(chatSourceFile.toPath(), chatBackupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    public GameState loadGame() {
        File saveFile = new File(SAVE_FILE);

        try {
            
            if (!saveFile.exists() || saveFile.length() == 0) {
                System.out.println("ไม่มีไฟล์บันทึก");
                return new GameState();
            }

            
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(saveFile))) {
                GameState state = (GameState) ois.readObject();
                if (state == null) {
                    System.err.println("ข้อมูลบันทึกว่างเปล่า สร้างเกมใหม่");
                    return new GameState();
                }
                System.out.println("โหลดเกมสำเร็จ: " + saveFile.getAbsolutePath());
                
                
                System.out.println("กำลังโหลดประวัติแชทจาก " + CHAT_SAVE_FILE);
                ChatHistoryManager.resetInstance(); 
                ChatHistoryManager chatManager = ChatHistoryManager.getInstance();
                
                return state;
            }

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("โหลดเกมล้มเหลว: " + e.getMessage());
            e.printStackTrace();
            createCorruptedFileBackup(saveFile); 
            return new GameState();
        }
    }

    
    private void createCorruptedFileBackup(File originalFile) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            File backupFile = new File(BACKUP_DIR + "/corrupted_save_" + timestamp + ".bak");
            Files.copy(originalFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("สำรองไฟล์ที่เสียหายล้มเหลว: " + e.getMessage());
        }
    }

    
    public boolean saveExists() {
        return new File(SAVE_FILE).exists();
    }
}


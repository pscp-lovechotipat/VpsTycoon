package com.vpstycoon.game;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GameSaveManager {
    private static final String SAVE_FILE = "savegame.dat";
    private static final String BACKUP_DIR = "backups";
    private final ObjectMapper mapper;

    public GameSaveManager() {
        this.mapper = new ObjectMapper();
        createBackupDirectory();
    }

    private void createBackupDirectory() {
        File backupDir = new File(BACKUP_DIR);
        if (!backupDir.exists()) {
            backupDir.mkdir();
        }
    }

    public void saveGame(GameState state) {
        try {
            File saveFile = new File(SAVE_FILE);

            if (saveFile.exists() && saveFile.length() > 0) {
                createBackup();
            }

            mapper.writeValue(saveFile, state);
            System.out.println("Save game successfully: " + saveFile.getAbsolutePath());

        } catch (IOException e) {
            System.err.println("Save game failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteGame() {
        File saveFile = new File(SAVE_FILE);
        if (saveFile.exists()) {
            boolean deleted = saveFile.delete();
            if (deleted) {
                System.out.println("Delete game save: " + saveFile.getAbsolutePath());
            } else {
                System.err.println("Delete game save failed: " + saveFile.getAbsolutePath());
            }
        }
    }

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

    public GameState loadGame() {
        File saveFile = new File(SAVE_FILE);

        try {
            if (!saveFile.exists() || saveFile.length() == 0) {
                System.out.println("No save game file");
                return new GameState();
            }

            GameState state = mapper.readValue(saveFile, GameState.class);

            if (state == null) {
                System.err.println("Save game is null, Create a new game.");
                return new GameState();
            }

            System.out.println("Load save success: " + saveFile.getAbsolutePath());
            return state;

        } catch (IOException e) {
            System.err.println("Error to load save game: " + e.getMessage());
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
            System.err.println("Error backing up corrupted save: " + e.getMessage());
        }
    }

    public boolean saveExists() {
        return new File(SAVE_FILE).exists();
    }
}
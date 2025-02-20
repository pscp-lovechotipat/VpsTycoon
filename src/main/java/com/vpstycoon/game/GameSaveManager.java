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
            // Create backup of existing save if it exists
            File saveFile = new File(SAVE_FILE);
            if (saveFile.exists()) {
                createBackup();
            }

            // Save new game state
            mapper.writeValue(saveFile, state);
        } catch (IOException e) {
            e.printStackTrace();
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
        try {
            return mapper.readValue(new File(SAVE_FILE), GameState.class);
        } catch (IOException e) {
            return new GameState();
        }
    }

    public boolean saveExists() {
        return new File(SAVE_FILE).exists();
    }
} 
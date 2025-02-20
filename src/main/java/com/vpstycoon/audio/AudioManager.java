package com.vpstycoon.audio;

import java.util.Map;
import java.util.HashMap;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.application.Platform;
import com.vpstycoon.config.GameConfig;
import com.vpstycoon.event.GameEventBus;
import com.vpstycoon.event.EventListener;
import com.vpstycoon.event.SettingsChangedEvent;
import com.vpstycoon.resource.ResourceManager;
import java.net.URL;

public class AudioManager {
    private static final AudioManager instance = new AudioManager();
    private final Map<String, Media> soundCache = new HashMap<>();
    private MediaPlayer musicPlayer;
    private double musicVolume = 0.5;
    private double sfxVolume = 0.5;

    private AudioManager() {
        GameEventBus.getInstance().subscribe(
            SettingsChangedEvent.class, 
            this::onSettingsChanged
        );
    }

    private void onSettingsChanged(SettingsChangedEvent event) {
        setMusicVolume(event.getNewConfig().getMusicVolume());
        setSfxVolume(event.getNewConfig().getSfxVolume());
    }

    public static AudioManager getInstance() {
        return instance;
    }

    public void playMusic(String musicFile) {
        Platform.runLater(() -> {
            try {
                if (musicPlayer != null) {
                    musicPlayer.stop();
                }
                String resourcePath = ResourceManager.getMusicPath(musicFile);
                URL resourceUrl = ResourceManager.getResource(resourcePath);
                if (resourceUrl == null) {
                    throw new RuntimeException("Music file not found: " + resourcePath);
                }
                Media music = new Media(resourceUrl.toExternalForm());
                musicPlayer = new MediaPlayer(music);
                musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                musicPlayer.setVolume(musicVolume);
                musicPlayer.play();
            } catch (Exception e) {
                System.err.println("Failed to play music: " + e.getMessage());
            }
        });
    }

    public void playSoundEffect(String soundFile) {
        Platform.runLater(() -> {
            try {
                Media sound = soundCache.computeIfAbsent(soundFile, k -> {
                    try {
                        String resourcePath = ResourceManager.getSoundPath(k);
                        URL resourceUrl = ResourceManager.getResource(resourcePath);
                        if (resourceUrl == null) {
                            throw new RuntimeException("Sound file not found: " + resourcePath);
                        }
                        return new Media(resourceUrl.toExternalForm());
                    } catch (Exception e) {
                        System.err.println("Failed to load sound: " + e.getMessage());
                        return null;
                    }
                });
                if (sound != null) {
                    MediaPlayer player = new MediaPlayer(sound);
                    player.setVolume(sfxVolume);
                    player.play();
                    player.setOnEndOfMedia(player::dispose);
                }
            } catch (Exception e) {
                System.err.println("Failed to play sound effect: " + e.getMessage());
            }
        });
    }

    public void setMusicVolume(double volume) {
        this.musicVolume = volume;
        if (musicPlayer != null) {
            musicPlayer.setVolume(volume);
        }
    }

    public void setSfxVolume(double volume) {
        this.sfxVolume = volume;
    }

    public void dispose() {
        if (musicPlayer != null) {
            musicPlayer.dispose();
        }
        soundCache.clear();
    }
} 
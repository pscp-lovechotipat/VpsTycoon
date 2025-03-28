package com.vpstycoon.audio;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.vpstycoon.audio.interfaces.IAudioManager;
import com.vpstycoon.event.GameEventBus;
import com.vpstycoon.event.SettingsChangedEvent;
import com.vpstycoon.service.ResourceManager;

import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class AudioManager implements IAudioManager {
    private final Map<String, Media> soundCache = new HashMap<>();
    private final List<MediaPlayer> activeSfxPlayers = new ArrayList<>();
    private MediaPlayer musicPlayer;
    private double musicVolume = 0.5;
    private double sfxVolume = 0.5;
    private boolean isMusicPaused = false;

    public AudioManager() {
        GameEventBus.getInstance().subscribe(
                SettingsChangedEvent.class,
                this::onSettingsChanged
        );
    }

    private void onSettingsChanged(SettingsChangedEvent event) {
        setMusicVolume(event.getNewConfig().getMusicVolume());
        setSfxVolume(event.getNewConfig().getSfxVolume());
    }

    // เล่นเพลงประกอบเกม
    @Override
    public void playMusic(String musicFile) {
        Platform.runLater(() -> {
            try {
                if (musicPlayer != null) {
                    if (musicPlayer.getMedia().getSource().endsWith(musicFile)) {
                        if (isMusicPaused) {
                            resumeMusic();
                        }
                        return;
                    }
                    musicPlayer.stop();
                    musicPlayer.dispose();
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
                isMusicPaused = false;
            } catch (Exception e) {
                System.err.println("Failed to play music: " + e.getMessage());
            }
        });
    }

    // หยุดเพลงประกอบเกมชั่วคราว
    @Override
    public void pauseMusic() {
        Platform.runLater(() -> {
            if (musicPlayer != null && musicPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                musicPlayer.pause();
                isMusicPaused = true;
            }
        });
    }

    // เล่นเพลงประกอบเกมต่อจากจุดที่หยุดไว้
    @Override
    public void resumeMusic() {
        Platform.runLater(() -> {
            if (musicPlayer != null && isMusicPaused) {
                musicPlayer.play();
                isMusicPaused = false;
            }
        });
    }

    // เล่นเสียงเอฟเฟกต์
    @Override
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
                    activeSfxPlayers.add(player);
                    player.play();
                    player.setOnEndOfMedia(() -> {
                        player.dispose();
                        activeSfxPlayers.remove(player);
                    });
                }
            } catch (Exception e) {
                System.err.println("Failed to play sound effect: " + e.getMessage());
            }
        });
    }

    // หยุดเล่นเสียงเอฟเฟกต์
    @Override
    public void stopSoundEffect(String soundFile) {
        Platform.runLater(() -> {
            Iterator<MediaPlayer> iterator = activeSfxPlayers.iterator();
            while (iterator.hasNext()) {
                MediaPlayer player = iterator.next();
                if (player.getMedia().getSource().endsWith(soundFile)) {
                    player.stop();
                    player.dispose();
                    iterator.remove();
                }
            }
        });
    }

    // ตั้งค่าระดับความดังของเพลงประกอบ
    @Override
    public void setMusicVolume(double volume) {
        this.musicVolume = volume;
        if (musicPlayer != null) {
            musicPlayer.setVolume(volume);
        }
    }

    // ตั้งค่าระดับความดังของเสียงเอฟเฟกต์
    @Override
    public void setSfxVolume(double volume) {
        this.sfxVolume = volume;
    }

    // ล้างทรัพยากรเสียงทั้งหมด
    @Override
    public void dispose() {
        if (musicPlayer != null) {
            musicPlayer.dispose();
        }
        for (MediaPlayer player : activeSfxPlayers) {
            player.dispose();
        }
        activeSfxPlayers.clear();
        soundCache.clear();
    }

    // โหลดไฟล์เสียงเอฟเฟกต์ล่วงหน้าเพื่อหลีกเลี่ยงการกระตุกเมื่อเล่นครั้งแรก
    @Override
    public void preloadSoundEffect(String name) {
        if (!soundCache.containsKey(name)) {
            try {
                final String soundName = name;
                URL url = ResourceManager.getResource(ResourceManager.getSoundPath(name));
                if (url != null) {
                    final URL soundUrl = url;
                    Platform.runLater(() -> {
                        try {
                            Media sound = new Media(soundUrl.toString());
                            soundCache.put(soundName, sound);
                            System.out.println("โหลดไฟล์เสียง: " + soundName + " เรียบร้อยแล้ว");
                        } catch (Exception ex) {
                            System.err.println("เกิดข้อผิดพลาดในการโหลด Media สำหรับไฟล์เสียง " + soundName + ": " + ex.getMessage());
                        }
                    });
                } else {
                    System.err.println("ไม่พบไฟล์เสียง: " + name);
                }
            } catch (Exception e) {
                System.err.println("เกิดข้อผิดพลาดในการโหลดไฟล์เสียง " + name + ": " + e.getMessage());
            }
        }
    }
}

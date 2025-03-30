package com.vpstycoon.audio.interfaces;

public interface IAudioManager {
    void playMusic(String musicFile);
    void pauseMusic();
    void resumeMusic();
    void playSoundEffect(String soundFile);
    void stopSoundEffect(String soundFile);
    void setMusicVolume(double volume);
    void setSfxVolume(double volume);
    void dispose();
    void preloadSoundEffect(String name);
} 

package com.vpstycoon.audio.interfaces;


public interface IAudioManager {
    
    
    void playSound(String sound);
    
    
    void playSound(String sound, double volume);
    
    
    void playMusic(String music);
    
    
    void playMusic(String music, double volume);
    
    
    void stopMusic();
    
    
    void pauseMusic();
    
    
    void resumeMusic();
    
    
    void setSoundVolume(double volume);
    
    
    void setMusicVolume(double volume);
    
    
    double getSoundVolume();
    
    
    double getMusicVolume();
    
    
    void setMuted(boolean muted);
    
    
    boolean isMuted();
} 


package com.vpstycoon.ui.game.notification;

import com.vpstycoon.audio.AudioManager;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.effect.Glow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class NotificationView extends VBox {
    private AudioManager audioManager;

    public NotificationView() {
        setSpacing(10); 
        setPadding(new Insets(50)); 
        
        setMouseTransparent(true);
    }

    public void setAudioManager(AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    
    public void addNotificationPane(String title, String content) {
        Pane notificationPane = createNotificationPane(title, content);
        
        
        
        javafx.application.Platform.runLater(() -> {
            getChildren().add(notificationPane);
            
            
            TranslateTransition translateOutTransition = new TranslateTransition(Duration.seconds(0.3), notificationPane);
            translateOutTransition.setFromX(0); 
            translateOutTransition.setToX(300); 
    
            TranslateTransition translateInTransition = new TranslateTransition(Duration.seconds(0.3), notificationPane);
            translateInTransition.setFromX(300);
            translateInTransition.setToX(0);
    
            
            FadeTransition fadeOutTransition = new FadeTransition(Duration.seconds(0.3), notificationPane);
            fadeOutTransition.setFromValue(1.0); 
            fadeOutTransition.setToValue(0.0); 
    
            FadeTransition fadeInTransition = new FadeTransition(Duration.seconds(0.3), notificationPane);
            fadeInTransition.setFromValue(0.0);
            fadeInTransition.setToValue(1.0);
    
            
            ParallelTransition parallelOutTransition = new ParallelTransition(translateOutTransition, fadeOutTransition);
            ParallelTransition parallelInTransition = new ParallelTransition(translateInTransition, fadeInTransition);
    
            
            Timeline timeline = new Timeline(new KeyFrame(
                    Duration.seconds(3), 
                    event -> {
                        
                        parallelOutTransition.play();
                    }
            ));
    
            parallelInTransition.play();
    
            parallelInTransition.setOnFinished(event -> {
                timeline.setCycleCount(1); 
                timeline.play(); 
            });
    
            
            parallelOutTransition.setOnFinished(event -> getChildren().remove(notificationPane));
    
            if (audioManager != null) {
                audioManager.playSoundEffect("noti.mp3");
            }
        });
    }

    
    private Pane createNotificationPane(String title, String content) {
        VBox pane = new VBox(5); 
        pane.setPadding(new Insets(10));
        
        
        pane.setMouseTransparent(true);
        
        
        pane.setStyle("""
            -fx-background-color: rgba(40, 10, 60, 0.85); 
            -fx-border-color: #6a00ff; 
            -fx-border-width: 2px;
            -fx-border-radius: 5;
            -fx-background-radius: 5;
            -fx-effect: dropshadow(gaussian, rgba(106, 0, 255, 0.6), 10, 0, 0, 0);
        """);

        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.rgb(0, 255, 255)); 
        titleLabel.setStyle("-fx-padding: 0 0 5 0;");

        
        Label contentLabel = new Label(content);
        contentLabel.setFont(Font.font("System", 12));
        contentLabel.setTextFill(Color.WHITE);
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(380);

        pane.getChildren().addAll(titleLabel, contentLabel);
        
        
        Glow glow = new Glow(0.3);
        pane.setEffect(glow);
        
        return pane;
    }
}


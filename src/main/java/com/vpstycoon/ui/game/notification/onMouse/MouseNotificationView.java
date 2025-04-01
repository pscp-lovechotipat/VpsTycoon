package com.vpstycoon.ui.game.notification.onMouse;

import com.vpstycoon.audio.AudioManager;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class MouseNotificationView extends Pane {
    private AudioManager audioManager;
    public MouseNotificationView() {
        setMouseTransparent(true);
    }

    public void setAudioManager(AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    public void addNotificationPane(String content, double mouseX, double mouseY) {
        
        Pane notificationPane = createNotificationPane(content);
        notificationPane.setPrefWidth(200); 
        double sceneX = mouseX - getScene().getWindow().getX();
        double sceneY = mouseY - getScene().getWindow().getY();

        
        notificationPane.setLayoutX(sceneX - 50);
        notificationPane.setLayoutY(sceneY - 100);

        
        getChildren().add(notificationPane);

        
        TranslateTransition appear = new TranslateTransition(Duration.seconds(0.5), notificationPane);
        appear.setFromY(50); 
        appear.setToY(0);    
        appear.setOnFinished(event -> {
            
            PauseTransition pause = new PauseTransition(Duration.seconds(1));
            pause.setOnFinished(e -> {
                
                TranslateTransition disappear = new TranslateTransition(Duration.seconds(0.5), notificationPane);
                disappear.setFromY(0);   
                disappear.setToY(-50);   
                disappear.setOnFinished(ev -> getChildren().remove(notificationPane)); 
                disappear.play();
            });
            pause.play();
        });
        appear.play();
    }

    
    private Pane createNotificationPane(String content) {
        VBox pane = new VBox();

        pane.setStyle("-fx-padding: 10;");

        Label label = new Label(content);
        label.setTextFill(Color.web("#FFD700"));

        pane.getChildren().add(label);


        return pane;
    }
}



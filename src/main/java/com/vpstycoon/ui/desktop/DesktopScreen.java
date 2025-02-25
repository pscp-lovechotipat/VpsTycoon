package com.vpstycoon.ui.desktop;

import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Popup;

import com.vpstycoon.manager.RequestManager;
import com.vpstycoon.manager.VPSManager;
import com.vpstycoon.chat.ChatSystem;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

public class DesktopScreen extends StackPane {
    private final double companyRating;
    private final int marketingPoints;
    private final ChatSystem chatSystem;
    private final RequestManager requestManager;
    private final VPSManager vpsManager;
    private Popup chatWindow;
    
    public DesktopScreen(double companyRating, int marketingPoints, 
                        ChatSystem chatSystem, RequestManager requestManager,
                        VPSManager vpsManager) {
        this.companyRating = companyRating;
        this.marketingPoints = marketingPoints;
        this.chatSystem = chatSystem;
        this.requestManager = requestManager;
        this.vpsManager = vpsManager;
        
        setupUI();
    }
    
    private void setupUI() {
        // Set desktop background
        setStyle("-fx-background-color: #1e1e1e;");
        
        // Create desktop icons container
        FlowPane iconsContainer = new FlowPane(10, 10);
        iconsContainer.setPadding(new Insets(20));
        iconsContainer.setAlignment(Pos.TOP_LEFT);
        
        // Add Messenger icon using FontAwesome
        DesktopIcon messengerIcon = new DesktopIcon(
            FontAwesomeSolid.COMMENTS.toString(),
            "Messenger",
            this::openChatWindow
        );
        
        iconsContainer.getChildren().add(messengerIcon);
        
        getChildren().add(iconsContainer);
    }
    
    private void openChatWindow() {
        if (chatWindow == null) {
            chatWindow = new Popup();
            chatWindow.setAutoHide(true);
            
            MessengerWindow messengerContent = new MessengerWindow(
                chatSystem,
                () -> chatWindow.hide()
            );
            
            chatWindow.getContent().add(messengerContent);
        }
        
        if (!chatWindow.isShowing()) {
            // Show popup centered on the screen
            chatWindow.show(getScene().getWindow());
            chatWindow.setX(getScene().getWindow().getX() + 
                (getScene().getWindow().getWidth() - chatWindow.getWidth()) / 2);
            chatWindow.setY(getScene().getWindow().getY() + 
                (getScene().getWindow().getHeight() - chatWindow.getHeight()) / 2);
        }
    }
} 
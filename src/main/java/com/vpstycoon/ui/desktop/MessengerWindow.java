package com.vpstycoon.ui.desktop;

import com.vpstycoon.chat.ChatSystem;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class MessengerWindow extends VBox {
    private final ChatSystem chatSystem;
    private final Runnable onClose;
    
    public MessengerWindow(ChatSystem chatSystem, Runnable onClose) {
        this.chatSystem = chatSystem;
        this.onClose = onClose;
        
        setupUI();
        styleWindow();
    }
    
    private void setupUI() {
        setPrefSize(800, 600);
        
        // Title bar
        HBox titleBar = createTitleBar();
        
        // Chat content
        HBox content = new HBox();
        content.setPadding(new Insets(0));
        VBox.setVgrow(content, Priority.ALWAYS);
        
        // Contacts list
        VBox contactsList = createContactsList();
        
        // Chat area
        VBox chatArea = createChatArea();
        
        content.getChildren().addAll(contactsList, chatArea);
        
        getChildren().addAll(titleBar, content);
    }
    
    private HBox createTitleBar() {
        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_RIGHT);
        titleBar.setPadding(new Insets(5, 10, 5, 10));
        titleBar.setStyle("-fx-background-color: #0084ff;");
        
        Button closeButton = new Button("X");
        closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
        closeButton.setOnAction(e -> onClose.run());
        
        titleBar.getChildren().add(closeButton);
        return titleBar;
    }
    
    private VBox createContactsList() {
        VBox contactsList = new VBox(5);
        contactsList.setPrefWidth(250);
        contactsList.setPadding(new Insets(10));
        contactsList.setStyle("-fx-background-color: #f0f0f0;");
        
        TextField searchBox = new TextField();
        searchBox.setPromptText("Search");
        searchBox.setStyle("-fx-background-radius: 20;");
        
        ListView<String> listView = new ListView<>();
        listView.getItems().addAll(
            "Individual Customer",
            "Small Business",
            "Medium Business",
            "Large Business"
        );
        VBox.setVgrow(listView, Priority.ALWAYS);
        
        contactsList.getChildren().addAll(searchBox, listView);
        return contactsList;
    }
    
    private VBox createChatArea() {
        VBox chatArea = new VBox(10);
        chatArea.setPadding(new Insets(10));
        HBox.setHgrow(chatArea, Priority.ALWAYS);
        
        // Messages area
        ScrollPane messagesScroll = new ScrollPane();
        messagesScroll.setFitToWidth(true);
        VBox.setVgrow(messagesScroll, Priority.ALWAYS);
        
        VBox messagesBox = new VBox(10);
        messagesBox.setPadding(new Insets(10));
        
        // Sample messages
        addMessage(messagesBox, "Hello! How can I help you today?", true);
        addMessage(messagesBox, "I need a high-performance VPS server", false);
        
        messagesScroll.setContent(messagesBox);
        
        // Input area
        HBox inputArea = new HBox(10);
        inputArea.setAlignment(Pos.CENTER);
        
        TextField messageInput = new TextField();
        messageInput.setPromptText("Type a message...");
        HBox.setHgrow(messageInput, Priority.ALWAYS);
        
        Button sendButton = new Button("Send");
        
        inputArea.getChildren().addAll(messageInput, sendButton);
        
        chatArea.getChildren().addAll(messagesScroll, inputArea);
        return chatArea;
    }
    
    private void addMessage(VBox messagesBox, String text, boolean isOwn) {
        HBox messageContainer = new HBox();
        messageContainer.setAlignment(isOwn ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        
        Label message = new Label(text);
        message.setWrapText(true);
        message.setMaxWidth(400);
        message.setPadding(new Insets(8, 12, 8, 12));
        message.setStyle(String.format(
            "-fx-background-color: %s; -fx-background-radius: 15; -fx-text-fill: %s;",
            isOwn ? "#0084ff" : "#e9ecef",
            isOwn ? "white" : "black"
        ));
        
        messageContainer.getChildren().add(message);
        messagesBox.getChildren().add(messageContainer);
    }
    
    private void styleWindow() {
        setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0);");
    }
} 
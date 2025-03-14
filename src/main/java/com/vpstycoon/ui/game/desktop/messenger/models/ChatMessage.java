package com.vpstycoon.ui.game.desktop.messenger.models;

import com.vpstycoon.ui.game.desktop.messenger.MessageType;

import java.io.Serializable;

public class ChatMessage implements Serializable {
    private MessageType type;
    private String content;
    private long timestamp;

    public ChatMessage(MessageType type, String content) {
        this.type = type;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }

    public MessageType getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
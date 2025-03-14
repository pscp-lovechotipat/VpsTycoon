package com.vpstycoon.ui.game.desktop.messenger.models;

import com.vpstycoon.ui.game.desktop.messenger.MessageType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ChatMessage implements Serializable {
    private MessageType type;
    private String content;
    private long timestamp;
    private Map<String, Object> metadata;

    public ChatMessage(MessageType type, String content, Map<String, Object> metadata) {
        this.type = type;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
        this.metadata = metadata != null ? metadata : new HashMap<>();
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

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
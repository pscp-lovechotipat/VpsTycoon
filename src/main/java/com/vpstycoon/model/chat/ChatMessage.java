package com.vpstycoon.model.chat;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ChatMessage implements Serializable {
    private MessageType type;
    private String content;
    private Map<String, Object> metadata;

    public ChatMessage(MessageType type, String content, Map<String, Object> metadata) {
        this.type = type;
        this.content = content;
        this.metadata = metadata != null ? metadata : new HashMap<>();
    }

    public MessageType getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}

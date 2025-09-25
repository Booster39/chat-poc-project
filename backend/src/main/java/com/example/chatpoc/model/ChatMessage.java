package com.example.chatpoc.model;

import java.time.LocalDateTime;

public class ChatMessage {
    private String id;
    private String content;
    private String sender;
    private MessageType type;
    private String sessionId;
    private LocalDateTime timestamp;

    public enum MessageType {
        CHAT, JOIN, LEAVE, TYPING
    }

    public ChatMessage() {
        this.timestamp = LocalDateTime.now();
    }

    public ChatMessage(String content, String sender, MessageType type, String sessionId) {
        this();
        this.content = content;
        this.sender = sender;
        this.type = type;
        this.sessionId = sessionId;
    }

    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    
    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }
    
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}

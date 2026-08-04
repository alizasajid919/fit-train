package com.fitness.app.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "grocery_voice_messages")
public class GroceryVoiceMessage implements Serializable {

    @PrimaryKey
    @NonNull
    private String id;
    private String sender;
    private String text;
    private long timestamp;

    public GroceryVoiceMessage() {
    }

    public GroceryVoiceMessage(@NonNull String id, String sender, String text, long timestamp) {
        this.id = id;
        this.sender = sender;
        this.text = text;
        this.timestamp = timestamp;
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}

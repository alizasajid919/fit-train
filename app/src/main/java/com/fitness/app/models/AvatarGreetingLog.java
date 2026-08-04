package com.fitness.app.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.UUID;

@Entity(tableName = "avatar_greeting_logs")
public class AvatarGreetingLog implements Serializable {

    @PrimaryKey
    @NonNull
    private String id;
    private String greetingText;
    private long timestamp;

    public AvatarGreetingLog() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getGreetingText() { return greetingText; }
    public void setGreetingText(String greetingText) { this.greetingText = greetingText; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}

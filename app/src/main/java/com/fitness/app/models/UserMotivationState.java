package com.fitness.app.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.UUID;

@Entity(tableName = "user_motivation_states")
public class UserMotivationState implements Serializable {

    @PrimaryKey
    @NonNull
    private String id;
    private String lastMotivationText;
    private long lastUpdated;

    public UserMotivationState() {
        this.id = UUID.randomUUID().toString();
        this.lastUpdated = System.currentTimeMillis();
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getLastMotivationText() { return lastMotivationText; }
    public void setLastMotivationText(String lastMotivationText) { this.lastMotivationText = lastMotivationText; }

    public long getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }
}

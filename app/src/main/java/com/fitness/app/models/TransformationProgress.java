package com.fitness.app.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "transformation_progress")
public class TransformationProgress implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int progressId;
    
    private String userId;
    private int weekNumber;
    private int weight;
    private int bodyFat;
    private String photoUrl;
    private int workoutsCompleted;
    private int workoutsTotal;
    private String aiFeedbackText;
    private long timestamp;

    public TransformationProgress() {}

    public TransformationProgress(String userId, int weekNumber, int weight, int bodyFat, String photoUrl, int workoutsCompleted, int workoutsTotal, String aiFeedbackText, long timestamp) {
        this.userId = userId;
        this.weekNumber = weekNumber;
        this.weight = weight;
        this.bodyFat = bodyFat;
        this.photoUrl = photoUrl;
        this.workoutsCompleted = workoutsCompleted;
        this.workoutsTotal = workoutsTotal;
        this.aiFeedbackText = aiFeedbackText;
        this.timestamp = timestamp;
    }

    public int getProgressId() { return progressId; }
    public void setProgressId(int progressId) { this.progressId = progressId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getWeekNumber() { return weekNumber; }
    public void setWeekNumber(int weekNumber) { this.weekNumber = weekNumber; }

    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }

    public int getBodyFat() { return bodyFat; }
    public void setBodyFat(int bodyFat) { this.bodyFat = bodyFat; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public int getWorkoutsCompleted() { return workoutsCompleted; }
    public void setWorkoutsCompleted(int workoutsCompleted) { this.workoutsCompleted = workoutsCompleted; }

    public int getWorkoutsTotal() { return workoutsTotal; }
    public void setWorkoutsTotal(int workoutsTotal) { this.workoutsTotal = workoutsTotal; }

    public String getAiFeedbackText() { return aiFeedbackText; }
    public void setAiFeedbackText(String aiFeedbackText) { this.aiFeedbackText = aiFeedbackText; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}

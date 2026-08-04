package com.fitness.app.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "transformation_tasks")
public class TransformationTask implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int taskId;
    
    private String userId;
    private String date; // yyyy-MM-dd
    private String taskType; // WORKOUT, DIET, WATER, SLEEP
    private String title;
    private String description;
    private int targetValue; // steps target, ml target, calories target, minutes target
    private int progressValue; // actual completed
    private boolean isCompleted;

    public TransformationTask() {}

    public TransformationTask(String userId, String date, String taskType, String title, String description, int targetValue, int progressValue, boolean isCompleted) {
        this.userId = userId;
        this.date = date;
        this.taskType = taskType;
        this.title = title;
        this.description = description;
        this.targetValue = targetValue;
        this.progressValue = progressValue;
        this.isCompleted = isCompleted;
    }

    public int getTaskId() { return taskId; }
    public void setTaskId(int taskId) { this.taskId = taskId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getTargetValue() { return targetValue; }
    public void setTargetValue(int targetValue) { this.targetValue = targetValue; }

    public int getProgressValue() { return progressValue; }
    public void setProgressValue(int progressValue) { this.progressValue = progressValue; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}

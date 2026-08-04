package com.fitness.app.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.UUID;

@Entity(tableName = "fitness_challenges")
public class FitnessChallenge implements Serializable {

    @PrimaryKey
    @NonNull
    private String id;
    private String name;
    private String description;
    private String duration; // WEEKLY or MONTHLY
    private String difficulty; // EASY, MEDIUM, HARD, EXTREME
    private int xpReward;
    private String badgeName;
    private String tasksJson; // JSON representation of daily tasks
    private int progressPercent;
    private int daysRemaining;
    private long startDate;
    private long endDate;
    private String status; // NOT_STARTED, IN_PROGRESS, COMPLETED, FAILED
    
    // Dynamic Benefits & Rewards attributes
    private int caloriesEstimate;
    private int estimatedWorkoutTime; // in minutes
    private String healthBenefits; // Comma or newline separated benefits

    public FitnessChallenge() {
        this.id = UUID.randomUUID().toString();
        this.startDate = System.currentTimeMillis();
        this.endDate = System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000); // Default 7 days
        this.progressPercent = 0;
        this.status = "NOT_STARTED";
        this.caloriesEstimate = 0;
        this.estimatedWorkoutTime = 0;
        this.healthBenefits = "";
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public int getXpReward() { return xpReward; }
    public void setXpReward(int xpReward) { this.xpReward = xpReward; }

    public String getBadgeName() { return badgeName; }
    public void setBadgeName(String badgeName) { this.badgeName = badgeName; }

    public String getTasksJson() { return tasksJson; }
    public void setTasksJson(String tasksJson) { this.tasksJson = tasksJson; }

    public int getProgressPercent() { return progressPercent; }
    public void setProgressPercent(int progressPercent) { this.progressPercent = progressPercent; }

    public int getDaysRemaining() { return daysRemaining; }
    public void setDaysRemaining(int daysRemaining) { this.daysRemaining = daysRemaining; }

    public long getStartDate() { return startDate; }
    public void setStartDate(long startDate) { this.startDate = startDate; }

    public long getEndDate() { return endDate; }
    public void setEndDate(long endDate) { this.endDate = endDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getCaloriesEstimate() { return caloriesEstimate; }
    public void setCaloriesEstimate(int caloriesEstimate) { this.caloriesEstimate = caloriesEstimate; }

    public int getEstimatedWorkoutTime() { return estimatedWorkoutTime; }
    public void setEstimatedWorkoutTime(int estimatedWorkoutTime) { this.estimatedWorkoutTime = estimatedWorkoutTime; }

    public String getHealthBenefits() { return healthBenefits; }
    public void setHealthBenefits(String healthBenefits) { this.healthBenefits = healthBenefits; }
}

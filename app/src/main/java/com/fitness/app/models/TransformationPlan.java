package com.fitness.app.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "transformation_plans")
public class TransformationPlan implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int planId;
    
    private String userId;
    private int durationWeeks;
    private long startDate;
    private String currentPhotoUrl;
    private String idealPhotoUrl;
    private int startWeight;
    private int targetWeight;
    private int currentBodyFat;
    private int targetBodyFat;
    private String estimatedMuscleDev;
    private String areasToImprove;
    private String whyChosenText;
    private String status; // e.g. ACTIVE, COMPLETED
    private String milestonesJson; // Serialized list of milestones

    public TransformationPlan() {}

    public TransformationPlan(String userId, int durationWeeks, long startDate, String currentPhotoUrl, String idealPhotoUrl, int startWeight, int targetWeight, int currentBodyFat, int targetBodyFat, String estimatedMuscleDev, String areasToImprove, String whyChosenText, String status, String milestonesJson) {
        this.userId = userId;
        this.durationWeeks = durationWeeks;
        this.startDate = startDate;
        this.currentPhotoUrl = currentPhotoUrl;
        this.idealPhotoUrl = idealPhotoUrl;
        this.startWeight = startWeight;
        this.targetWeight = targetWeight;
        this.currentBodyFat = currentBodyFat;
        this.targetBodyFat = targetBodyFat;
        this.estimatedMuscleDev = estimatedMuscleDev;
        this.areasToImprove = areasToImprove;
        this.whyChosenText = whyChosenText;
        this.status = status;
        this.milestonesJson = milestonesJson;
    }

    public int getPlanId() { return planId; }
    public void setPlanId(int planId) { this.planId = planId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getDurationWeeks() { return durationWeeks; }
    public void setDurationWeeks(int durationWeeks) { this.durationWeeks = durationWeeks; }

    public long getStartDate() { return startDate; }
    public void setStartDate(long startDate) { this.startDate = startDate; }

    public String getCurrentPhotoUrl() { return currentPhotoUrl; }
    public void setCurrentPhotoUrl(String currentPhotoUrl) { this.currentPhotoUrl = currentPhotoUrl; }

    public String getIdealPhotoUrl() { return idealPhotoUrl; }
    public void setIdealPhotoUrl(String idealPhotoUrl) { this.idealPhotoUrl = idealPhotoUrl; }

    public int getStartWeight() { return startWeight; }
    public void setStartWeight(int startWeight) { this.startWeight = startWeight; }

    public int getTargetWeight() { return targetWeight; }
    public void setTargetWeight(int targetWeight) { this.targetWeight = targetWeight; }

    public int getCurrentBodyFat() { return currentBodyFat; }
    public void setCurrentBodyFat(int currentBodyFat) { this.currentBodyFat = currentBodyFat; }

    public int getTargetBodyFat() { return targetBodyFat; }
    public void setTargetBodyFat(int targetBodyFat) { this.targetBodyFat = targetBodyFat; }

    public String getEstimatedMuscleDev() { return estimatedMuscleDev; }
    public void setEstimatedMuscleDev(String estimatedMuscleDev) { this.estimatedMuscleDev = estimatedMuscleDev; }

    public String getAreasToImprove() { return areasToImprove; }
    public void setAreasToImprove(String areasToImprove) { this.areasToImprove = areasToImprove; }

    public String getWhyChosenText() { return whyChosenText; }
    public void setWhyChosenText(String whyChosenText) { this.whyChosenText = whyChosenText; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMilestonesJson() { return milestonesJson; }
    public void setMilestonesJson(String milestonesJson) { this.milestonesJson = milestonesJson; }
}

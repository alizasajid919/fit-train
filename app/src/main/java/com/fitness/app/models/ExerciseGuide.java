package com.fitness.app.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "exercise_guides")
public class ExerciseGuide {

    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String title;
    private String difficulty;
    private String description;
    private String commonErrors;
    private String postureTips;
    private String safetyInstructions;
    private String targetMuscles;
    private String recommendedRepetitions;
    private String category;
    private String videoUrl;
    private int imageRes;

    public ExerciseGuide(String title, String difficulty, String description, String commonErrors, 
                         String postureTips, String safetyInstructions, String targetMuscles, 
                         String recommendedRepetitions, String category, String videoUrl, int imageRes) {
        this.title = title;
        this.difficulty = difficulty;
        this.description = description;
        this.commonErrors = commonErrors;
        this.postureTips = postureTips;
        this.safetyInstructions = safetyInstructions;
        this.targetMuscles = targetMuscles;
        this.recommendedRepetitions = recommendedRepetitions;
        this.category = category;
        this.videoUrl = videoUrl;
        this.imageRes = imageRes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCommonErrors() {
        return commonErrors;
    }

    public void setCommonErrors(String commonErrors) {
        this.commonErrors = commonErrors;
    }

    public String getPostureTips() {
        return postureTips;
    }

    public void setPostureTips(String postureTips) {
        this.postureTips = postureTips;
    }

    public String getSafetyInstructions() {
        return safetyInstructions;
    }

    public void setSafetyInstructions(String safetyInstructions) {
        this.safetyInstructions = safetyInstructions;
    }

    public String getTargetMuscles() {
        return targetMuscles;
    }

    public void setTargetMuscles(String targetMuscles) {
        this.targetMuscles = targetMuscles;
    }

    public String getRecommendedRepetitions() {
        return recommendedRepetitions;
    }

    public void setRecommendedRepetitions(String recommendedRepetitions) {
        this.recommendedRepetitions = recommendedRepetitions;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public int getImageRes() {
        return imageRes;
    }

    public void setImageRes(int imageRes) {
        this.imageRes = imageRes;
    }
}

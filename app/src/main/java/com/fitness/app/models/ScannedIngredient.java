package com.fitness.app.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "scanned_ingredients")
public class ScannedIngredient implements Serializable {

    @PrimaryKey
    @NonNull
    private String id;
    private String name;
    private String category;
    private int confidence;
    private String estimatedQuantity;
    private int calories;
    private double protein;
    private double carbs;
    private double fat;
    private double fiber;
    private double sugar;
    private double sodium;
    private String vitamins;
    private String minerals;
    private String freshness;
    private String expiryDate;
    private long timestamp;

    public ScannedIngredient() {
    }

    public ScannedIngredient(@NonNull String id, String name, String category, int confidence, String estimatedQuantity,
                             int calories, double protein, double carbs, double fat, double fiber, double sugar, double sodium,
                             String vitamins, String minerals, String freshness, String expiryDate, long timestamp) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.confidence = confidence;
        this.estimatedQuantity = estimatedQuantity;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
        this.fiber = fiber;
        this.sugar = sugar;
        this.sodium = sodium;
        this.vitamins = vitamins;
        this.minerals = minerals;
        this.freshness = freshness;
        this.expiryDate = expiryDate;
        this.timestamp = timestamp;
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getConfidence() { return confidence; }
    public void setConfidence(int confidence) { this.confidence = confidence; }

    public String getEstimatedQuantity() { return estimatedQuantity; }
    public void setEstimatedQuantity(String estimatedQuantity) { this.estimatedQuantity = estimatedQuantity; }

    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }

    public double getProtein() { return protein; }
    public void setProtein(double protein) { this.protein = protein; }

    public double getCarbs() { return carbs; }
    public void setCarbs(double carbs) { this.carbs = carbs; }

    public double getFat() { return fat; }
    public void setFat(double fat) { this.fat = fat; }

    public double getFiber() { return fiber; }
    public void setFiber(double fiber) { this.fiber = fiber; }

    public double getSugar() { return sugar; }
    public void setSugar(double sugar) { this.sugar = sugar; }

    public double getSodium() { return sodium; }
    public void setSodium(double sodium) { this.sodium = sodium; }

    public String getVitamins() { return vitamins; }
    public void setVitamins(String vitamins) { this.vitamins = vitamins; }

    public String getMinerals() { return minerals; }
    public void setMinerals(String minerals) { this.minerals = minerals; }

    public String getFreshness() { return freshness; }
    public void setFreshness(String freshness) { this.freshness = freshness; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}

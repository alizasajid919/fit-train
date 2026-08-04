package com.fitness.app.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.UUID;

@Entity(tableName = "grocery_product_scans")
public class GroceryProductScan implements Serializable {

    @PrimaryKey
    @NonNull
    private String id;
    private String productName;
    private String brand;
    private String foodCategory;
    private String ingredients;
    private int calories;
    private double protein;
    private double carbs;
    private double fat;
    private double sugar;
    private double fiber;
    private double sodium;
    private String vitaminsMinerals;
    private boolean isHealthy;
    private boolean isHighlyProcessed;
    private boolean isOrganic;
    private boolean isSuitableWeightLoss;
    private boolean isSuitableWeightGain;
    private boolean isSuitableMuscleGain;
    private boolean isSuitableDiabetic;
    private int healthScore;
    private int nutritionScore;
    private String aiRecommendation;
    private String whyRecommended;
    private String alternativeProducts;
    private long timestamp;

    public GroceryProductScan() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getFoodCategory() { return foodCategory; }
    public void setFoodCategory(String foodCategory) { this.foodCategory = foodCategory; }

    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }

    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }

    public double getProtein() { return protein; }
    public void setProtein(double protein) { this.protein = protein; }

    public double getCarbs() { return carbs; }
    public void setCarbs(double carbs) { this.carbs = carbs; }

    public double getFat() { return fat; }
    public void setFat(double fat) { this.fat = fat; }

    public double getSugar() { return sugar; }
    public void setSugar(double sugar) { this.sugar = sugar; }

    public double getFiber() { return fiber; }
    public void setFiber(double fiber) { this.fiber = fiber; }

    public double getSodium() { return sodium; }
    public void setSodium(double sodium) { this.sodium = sodium; }

    public String getVitaminsMinerals() { return vitaminsMinerals; }
    public void setVitaminsMinerals(String vitaminsMinerals) { this.vitaminsMinerals = vitaminsMinerals; }

    public boolean isHealthy() { return isHealthy; }
    public void setHealthy(boolean healthy) { isHealthy = healthy; }

    public boolean isHighlyProcessed() { return isHighlyProcessed; }
    public void setHighlyProcessed(boolean highlyProcessed) { isHighlyProcessed = highlyProcessed; }

    public boolean isOrganic() { return isOrganic; }
    public void setOrganic(boolean organic) { isOrganic = organic; }

    public boolean isSuitableWeightLoss() { return isSuitableWeightLoss; }
    public void setSuitableWeightLoss(boolean suitableWeightLoss) { isSuitableWeightLoss = suitableWeightLoss; }

    public boolean isSuitableWeightGain() { return isSuitableWeightGain; }
    public void setSuitableWeightGain(boolean suitableWeightGain) { isSuitableWeightGain = suitableWeightGain; }

    public boolean isSuitableMuscleGain() { return isSuitableMuscleGain; }
    public void setSuitableMuscleGain(boolean suitableMuscleGain) { isSuitableMuscleGain = suitableMuscleGain; }

    public boolean isSuitableDiabetic() { return isSuitableDiabetic; }
    public void setSuitableDiabetic(boolean suitableDiabetic) { isSuitableDiabetic = suitableDiabetic; }

    public int getHealthScore() { return healthScore; }
    public void setHealthScore(int healthScore) { this.healthScore = healthScore; }

    public int getNutritionScore() { return nutritionScore; }
    public void setNutritionScore(int nutritionScore) { this.nutritionScore = nutritionScore; }

    public String getAiRecommendation() { return aiRecommendation; }
    public void setAiRecommendation(String aiRecommendation) { this.aiRecommendation = aiRecommendation; }

    public String getWhyRecommended() { return whyRecommended; }
    public void setWhyRecommended(String whyRecommended) { this.whyRecommended = whyRecommended; }

    public String getAlternativeProducts() { return alternativeProducts; }
    public void setAlternativeProducts(String alternativeProducts) { this.alternativeProducts = alternativeProducts; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}

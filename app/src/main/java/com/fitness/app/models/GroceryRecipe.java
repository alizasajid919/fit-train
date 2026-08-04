package com.fitness.app.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "grocery_recipes")
public class GroceryRecipe implements Serializable {

    @PrimaryKey
    @NonNull
    private String id;
    private String name;
    private String mealType;
    private int prepTimeMinutes;
    private int cookTimeMinutes;
    private String difficulty;
    private int calories;
    private double protein;
    private double carbs;
    private double fat;
    private String ingredientsJson;
    private String stepsJson;
    private String healthBenefits;
    private String missingIngredients;
    private boolean isSaved;
    private String servingSize;
    private String healthierReplacements;
    private String recipeImage;
    private long timestamp;

    public GroceryRecipe() {
    }

    public GroceryRecipe(@NonNull String id, String name, String mealType, int prepTimeMinutes, int cookTimeMinutes,
                         String difficulty, int calories, double protein, double carbs, double fat,
                         String ingredientsJson, String stepsJson, String healthBenefits, String missingIngredients,
                         boolean isSaved, String servingSize, String healthierReplacements, String recipeImage, long timestamp) {
        this.id = id;
        this.name = name;
        this.mealType = mealType;
        this.prepTimeMinutes = prepTimeMinutes;
        this.cookTimeMinutes = cookTimeMinutes;
        this.difficulty = difficulty;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
        this.ingredientsJson = ingredientsJson;
        this.stepsJson = stepsJson;
        this.healthBenefits = healthBenefits;
        this.missingIngredients = missingIngredients;
        this.isSaved = isSaved;
        this.servingSize = servingSize;
        this.healthierReplacements = healthierReplacements;
        this.recipeImage = recipeImage;
        this.timestamp = timestamp;
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }

    public int getPrepTimeMinutes() { return prepTimeMinutes; }
    public void setPrepTimeMinutes(int prepTimeMinutes) { this.prepTimeMinutes = prepTimeMinutes; }

    public int getCookTimeMinutes() { return cookTimeMinutes; }
    public void setCookTimeMinutes(int cookTimeMinutes) { this.cookTimeMinutes = cookTimeMinutes; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }

    public double getProtein() { return protein; }
    public void setProtein(double protein) { this.protein = protein; }

    public double getCarbs() { return carbs; }
    public void setCarbs(double carbs) { this.carbs = carbs; }

    public double getFat() { return fat; }
    public void setFat(double fat) { this.fat = fat; }

    public String getIngredientsJson() { return ingredientsJson; }
    public void setIngredientsJson(String ingredientsJson) { this.ingredientsJson = ingredientsJson; }

    public String getStepsJson() { return stepsJson; }
    public void setStepsJson(String stepsJson) { this.stepsJson = stepsJson; }

    public String getHealthBenefits() { return healthBenefits; }
    public void setHealthBenefits(String healthBenefits) { this.healthBenefits = healthBenefits; }

    public String getMissingIngredients() { return missingIngredients; }
    public void setMissingIngredients(String missingIngredients) { this.missingIngredients = missingIngredients; }

    public boolean isSaved() { return isSaved; }
    public void setSaved(boolean saved) { isSaved = saved; }

    public String getServingSize() { return servingSize; }
    public void setServingSize(String servingSize) { this.servingSize = servingSize; }

    public String getHealthierReplacements() { return healthierReplacements; }
    public void setHealthierReplacements(String healthierReplacements) { this.healthierReplacements = healthierReplacements; }

    public String getRecipeImage() { return recipeImage; }
    public void setRecipeImage(String recipeImage) { this.recipeImage = recipeImage; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}

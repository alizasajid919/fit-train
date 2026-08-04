package com.fitness.app.data.room;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "logged_meals")
public class LoggedMeal {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String mealType; // "Breakfast", "Lunch", "Dinner", "Snack"
    public String name;
    public int calories;
    public int protein;
    public int carbs;
    public int fat;
    public String date; // "yyyy-MM-dd"
    public long timestamp;
    
    // New fields for scheduling
    public String mealTime;
    public String notes;
    public boolean isChecked;
    public int iconResId;

    // Upgraded details fields
    public int fiber;
    public int sugar;
    public String servingSize;
    public String ingredients;
    public String steps;

    public LoggedMeal() {}

    public LoggedMeal(String mealType, String name, int calories, int protein, int carbs, int fat, String date, long timestamp) {
        this.mealType = mealType;
        this.name = name;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
        this.date = date;
        this.timestamp = timestamp;
        this.mealTime = "09:00 AM";
        this.isChecked = true; // default logged meals are consumed
        this.notes = "";
        this.iconResId = 0;
    }

    public LoggedMeal(String mealType, String name, int calories, int protein, int carbs, int fat, String date, long timestamp, String mealTime, String notes, boolean isChecked, int iconResId) {
        this.mealType = mealType;
        this.name = name;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
        this.date = date;
        this.timestamp = timestamp;
        this.mealTime = mealTime;
        this.notes = notes;
        this.isChecked = isChecked;
        this.iconResId = iconResId;
    }

    // Constructor with detail fields
    public LoggedMeal(String mealType, String name, int calories, int protein, int carbs, int fat, String date, long timestamp, String mealTime, String notes, boolean isChecked, int iconResId, int fiber, int sugar, String servingSize, String ingredients, String steps) {
        this.mealType = mealType;
        this.name = name;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
        this.date = date;
        this.timestamp = timestamp;
        this.mealTime = mealTime;
        this.notes = notes;
        this.isChecked = isChecked;
        this.iconResId = iconResId;
        this.fiber = fiber;
        this.sugar = sugar;
        this.servingSize = servingSize;
        this.ingredients = ingredients;
        this.steps = steps;
    }
}

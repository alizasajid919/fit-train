package com.fitness.app.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "weekly_meal_plan_days")
public class WeeklyMealPlanDay implements Serializable {

    @PrimaryKey
    @NonNull
    private String id;
    private int dayOfWeek;
    private String dayName;
    private String breakfastRecipeName;
    private int breakfastCalories;
    private String lunchRecipeName;
    private int lunchCalories;
    private String dinnerRecipeName;
    private int dinnerCalories;
    private String snackRecipeName;
    private int snackCalories;
    private int totalCalories;
    private double totalProtein;
    private int waterIntakeMl;
    private double totalCarbs;
    private double totalFat;
    private String expectedNutrition;
    private String breakfastDetailsJson;
    private String morningSnackDetailsJson;
    private String lunchDetailsJson;
    private String eveningSnackDetailsJson;
    private String dinnerDetailsJson;
    private long timestamp;

    public WeeklyMealPlanDay() {
    }

    public WeeklyMealPlanDay(@NonNull String id, int dayOfWeek, String dayName, String breakfastRecipeName, int breakfastCalories,
                             String lunchRecipeName, int lunchCalories, String dinnerRecipeName, int dinnerCalories,
                             String snackRecipeName, int snackCalories, int totalCalories, double totalProtein, int waterIntakeMl,
                             double totalCarbs, double totalFat, String expectedNutrition, String breakfastDetailsJson,
                             String morningSnackDetailsJson, String lunchDetailsJson, String eveningSnackDetailsJson,
                             String dinnerDetailsJson, long timestamp) {
        this.id = id;
        this.dayOfWeek = dayOfWeek;
        this.dayName = dayName;
        this.breakfastRecipeName = breakfastRecipeName;
        this.breakfastCalories = breakfastCalories;
        this.lunchRecipeName = lunchRecipeName;
        this.lunchCalories = lunchCalories;
        this.dinnerRecipeName = dinnerRecipeName;
        this.dinnerCalories = dinnerCalories;
        this.snackRecipeName = snackRecipeName;
        this.snackCalories = snackCalories;
        this.totalCalories = totalCalories;
        this.totalProtein = totalProtein;
        this.waterIntakeMl = waterIntakeMl;
        this.totalCarbs = totalCarbs;
        this.totalFat = totalFat;
        this.expectedNutrition = expectedNutrition;
        this.breakfastDetailsJson = breakfastDetailsJson;
        this.morningSnackDetailsJson = morningSnackDetailsJson;
        this.lunchDetailsJson = lunchDetailsJson;
        this.eveningSnackDetailsJson = eveningSnackDetailsJson;
        this.dinnerDetailsJson = dinnerDetailsJson;
        this.timestamp = timestamp;
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public int getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(int dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public String getDayName() { return dayName; }
    public void setDayName(String dayName) { this.dayName = dayName; }

    public String getBreakfastRecipeName() { return breakfastRecipeName; }
    public void setBreakfastRecipeName(String breakfastRecipeName) { this.breakfastRecipeName = breakfastRecipeName; }

    public int getBreakfastCalories() { return breakfastCalories; }
    public void setBreakfastCalories(int breakfastCalories) { this.breakfastCalories = breakfastCalories; }

    public String getLunchRecipeName() { return lunchRecipeName; }
    public void setLunchRecipeName(String lunchRecipeName) { this.lunchRecipeName = lunchRecipeName; }

    public int getLunchCalories() { return lunchCalories; }
    public void setLunchCalories(int lunchCalories) { this.lunchCalories = lunchCalories; }

    public String getDinnerRecipeName() { return dinnerRecipeName; }
    public void setDinnerRecipeName(String dinnerRecipeName) { this.dinnerRecipeName = dinnerRecipeName; }

    public int getDinnerCalories() { return dinnerCalories; }
    public void setDinnerCalories(int dinnerCalories) { this.dinnerCalories = dinnerCalories; }

    public String getSnackRecipeName() { return snackRecipeName; }
    public void setSnackRecipeName(String snackRecipeName) { this.snackRecipeName = snackRecipeName; }
    public String getMorningSnackRecipeName() { return snackRecipeName; }
    public void setMorningSnackRecipeName(String morningSnackRecipeName) { this.snackRecipeName = morningSnackRecipeName; }

    public int getSnackCalories() { return snackCalories; }
    public void setSnackCalories(int snackCalories) { this.snackCalories = snackCalories; }
    public int getMorningSnackCalories() { return snackCalories; }
    public void setMorningSnackCalories(int morningSnackCalories) { this.snackCalories = morningSnackCalories; }

    public int getTotalCalories() { return totalCalories; }
    public void setTotalCalories(int totalCalories) { this.totalCalories = totalCalories; }

    public double getTotalProtein() { return totalProtein; }
    public void setTotalProtein(double totalProtein) { this.totalProtein = totalProtein; }

    public int getWaterIntakeMl() { return waterIntakeMl; }
    public void setWaterIntakeMl(int waterIntakeMl) { this.waterIntakeMl = waterIntakeMl; }

    public double getTotalCarbs() { return totalCarbs; }
    public void setTotalCarbs(double totalCarbs) { this.totalCarbs = totalCarbs; }

    public double getTotalFat() { return totalFat; }
    public void setTotalFat(double totalFat) { this.totalFat = totalFat; }

    public String getExpectedNutrition() { return expectedNutrition; }
    public void setExpectedNutrition(String expectedNutrition) { this.expectedNutrition = expectedNutrition; }

    public String getBreakfastDetailsJson() { return breakfastDetailsJson; }
    public void setBreakfastDetailsJson(String breakfastDetailsJson) { this.breakfastDetailsJson = breakfastDetailsJson; }

    public String getMorningSnackDetailsJson() { return morningSnackDetailsJson; }
    public void setMorningSnackDetailsJson(String morningSnackDetailsJson) { this.morningSnackDetailsJson = morningSnackDetailsJson; }

    public String getLunchDetailsJson() { return lunchDetailsJson; }
    public void setLunchDetailsJson(String lunchDetailsJson) { this.lunchDetailsJson = lunchDetailsJson; }

    public String getEveningSnackDetailsJson() { return eveningSnackDetailsJson; }
    public void setEveningSnackDetailsJson(String eveningSnackDetailsJson) { this.eveningSnackDetailsJson = eveningSnackDetailsJson; }

    public String getDinnerDetailsJson() { return dinnerDetailsJson; }
    public void setDinnerDetailsJson(String dinnerDetailsJson) { this.dinnerDetailsJson = dinnerDetailsJson; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}

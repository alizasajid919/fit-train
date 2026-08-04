package com.fitness.app.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class ProgressLog implements Serializable {
    private String date; // "yyyy-MM-dd"
    private int waterConsumedMl;
    private int caloriesBurned;
    private int caloriesConsumed;
    private int stepsCount;
    private int sleepDurationMinutes;
    private double currentWeight;
    private double currentHeight;
    private double currentBmi;
    private double currentBodyFatPercentage;
    private long updatedAt;

    public ProgressLog() {}

    public ProgressLog(String date) {
        this.date = date;
        this.updatedAt = System.currentTimeMillis();
    }

    public ProgressLog(String date, int waterConsumedMl, int caloriesBurned, int caloriesConsumed, int stepsCount, int sleepDurationMinutes, double currentWeight, double currentHeight, double currentBmi, double currentBodyFatPercentage, long updatedAt) {
        this.date = date;
        this.waterConsumedMl = waterConsumedMl;
        this.caloriesBurned = caloriesBurned;
        this.caloriesConsumed = caloriesConsumed;
        this.stepsCount = stepsCount;
        this.sleepDurationMinutes = sleepDurationMinutes;
        this.currentWeight = currentWeight;
        this.currentHeight = currentHeight;
        this.currentBmi = currentBmi;
        this.currentBodyFatPercentage = currentBodyFatPercentage;
        this.updatedAt = updatedAt;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public int getWaterConsumedMl() { return waterConsumedMl; }
    public void setWaterConsumedMl(int waterConsumedMl) { this.waterConsumedMl = waterConsumedMl; }

    public int getCaloriesBurned() { return caloriesBurned; }
    public void setCaloriesBurned(int caloriesBurned) { this.caloriesBurned = caloriesBurned; }

    public int getCaloriesConsumed() { return caloriesConsumed; }
    public void setCaloriesConsumed(int caloriesConsumed) { this.caloriesConsumed = caloriesConsumed; }

    public int getStepsCount() { return stepsCount; }
    public void setStepsCount(int stepsCount) { this.stepsCount = stepsCount; }

    public int getSleepDurationMinutes() { return sleepDurationMinutes; }
    public void setSleepDurationMinutes(int sleepDurationMinutes) { this.sleepDurationMinutes = sleepDurationMinutes; }

    public double getCurrentWeight() { return currentWeight; }
    public void setCurrentWeight(double currentWeight) { this.currentWeight = currentWeight; }

    public double getCurrentHeight() { return currentHeight; }
    public void setCurrentHeight(double currentHeight) { this.currentHeight = currentHeight; }

    public double getCurrentBmi() { return currentBmi; }
    public void setCurrentBmi(double currentBmi) { this.currentBmi = currentBmi; }

    public double getCurrentBodyFatPercentage() { return currentBodyFatPercentage; }
    public void setCurrentBodyFatPercentage(double currentBodyFatPercentage) { this.currentBodyFatPercentage = currentBodyFatPercentage; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    // JSON Serialization
    public JSONObject toJsonObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("date", date);
        obj.put("waterConsumedMl", waterConsumedMl);
        obj.put("caloriesBurned", caloriesBurned);
        obj.put("caloriesConsumed", caloriesConsumed);
        obj.put("stepsCount", stepsCount);
        obj.put("sleepDurationMinutes", sleepDurationMinutes);
        obj.put("currentWeight", currentWeight);
        obj.put("currentHeight", currentHeight);
        obj.put("currentBmi", currentBmi);
        obj.put("currentBodyFatPercentage", currentBodyFatPercentage);
        obj.put("updatedAt", updatedAt);
        return obj;
    }

    // JSON Deserialization
    public static ProgressLog fromJsonObject(JSONObject obj) throws JSONException {
        return new ProgressLog(
            obj.optString("date", ""),
            obj.optInt("waterConsumedMl", 0),
            obj.optInt("caloriesBurned", 0),
            obj.optInt("caloriesConsumed", 0),
            obj.optInt("stepsCount", 0),
            obj.optInt("sleepDurationMinutes", 0),
            obj.optDouble("currentWeight", 0.0),
            obj.optDouble("currentHeight", 0.0),
            obj.optDouble("currentBmi", 0.0),
            obj.optDouble("currentBodyFatPercentage", 0.0),
            obj.optLong("updatedAt", 0)
        );
    }
}

package com.fitness.app.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class WorkoutLog implements Serializable {
    private String id;
    private String exerciseName;
    private int sets;
    private int reps;
    private double weight;
    private String notes;
    private String date; // yyyy-MM-dd
    private long timestamp;

    public WorkoutLog() {}

    public WorkoutLog(String id, String exerciseName, int sets, int reps, double weight, String notes, String date, long timestamp) {
        this.id = id;
        this.exerciseName = exerciseName;
        this.sets = sets;
        this.reps = reps;
        this.weight = weight;
        this.notes = notes;
        this.date = date;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getExerciseName() { return exerciseName; }
    public void setExerciseName(String exerciseName) { this.exerciseName = exerciseName; }

    public int getSets() { return sets; }
    public void setSets(int sets) { this.sets = sets; }

    public int getReps() { return reps; }
    public void setReps(int reps) { this.reps = reps; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    // JSON Serialization
    public JSONObject toJsonObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("exerciseName", exerciseName);
        obj.put("sets", sets);
        obj.put("reps", reps);
        obj.put("weight", weight);
        obj.put("notes", notes);
        obj.put("date", date);
        obj.put("timestamp", timestamp);
        return obj;
    }

    // JSON Deserialization
    public static WorkoutLog fromJsonObject(JSONObject obj) throws JSONException {
        return new WorkoutLog(
            obj.optString("id", ""),
            obj.optString("exerciseName", ""),
            obj.optInt("sets", 0),
            obj.optInt("reps", 0),
            obj.optDouble("weight", 0.0),
            obj.optString("notes", ""),
            obj.optString("date", ""),
            obj.optLong("timestamp", 0)
        );
    }
}

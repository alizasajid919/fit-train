package com.fitness.app.models;

import org.json.JSONException;
import org.json.JSONObject;

public class WorkoutSchedule {
    private String id;
    private String workoutName;
    private String date; // yyyy-MM-dd
    private String time; // HH:mm or hh:mm a
    private int durationMin;
    private String notes;
    private boolean completed;

    public WorkoutSchedule() {}

    public WorkoutSchedule(String id, String workoutName, String date, String time, int durationMin, String notes, boolean completed) {
        this.id = id;
        this.workoutName = workoutName;
        this.date = date;
        this.time = time;
        this.durationMin = durationMin;
        this.notes = notes;
        this.completed = completed;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getWorkoutName() { return workoutName; }
    public void setWorkoutName(String workoutName) { this.workoutName = workoutName; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public int getDurationMin() { return durationMin; }
    public void setDurationMin(int durationMin) { this.durationMin = durationMin; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public JSONObject toJsonObject() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("id", id);
            obj.put("workoutName", workoutName);
            obj.put("date", date);
            obj.put("time", time);
            obj.put("durationMin", durationMin);
            obj.put("notes", notes);
            obj.put("completed", completed);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static WorkoutSchedule fromJsonObject(JSONObject obj) {
        if (obj == null) return null;
        WorkoutSchedule s = new WorkoutSchedule();
        try {
            s.setId(obj.optString("id", ""));
            s.setWorkoutName(obj.optString("workoutName", ""));
            s.setDate(obj.optString("date", ""));
            s.setTime(obj.optString("time", ""));
            s.setDurationMin(obj.optInt("durationMin", 30));
            s.setNotes(obj.optString("notes", ""));
            s.setCompleted(obj.optBoolean("completed", false));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }
}

package com.fitness.app.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WorkoutPlan implements Serializable {
    private String id;
    private String goal;
    private String fitnessLevel;
    private List<Exercise> exercises;
    private long createdAt;

    public WorkoutPlan() {
        this.exercises = new ArrayList<>();
    }

    public WorkoutPlan(String id, String goal, String fitnessLevel, List<Exercise> exercises, long createdAt) {
        this.id = id;
        this.goal = goal;
        this.fitnessLevel = fitnessLevel;
        this.exercises = exercises;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }

    public String getFitnessLevel() { return fitnessLevel; }
    public void setFitnessLevel(String fitnessLevel) { this.fitnessLevel = fitnessLevel; }

    public List<Exercise> getExercises() { return exercises; }
    public void setExercises(List<Exercise> exercises) { this.exercises = exercises; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    // JSON Serialization
    public JSONObject toJsonObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("goal", goal);
        obj.put("fitnessLevel", fitnessLevel);
        obj.put("createdAt", createdAt);

        JSONArray arr = new JSONArray();
        for (Exercise ex : exercises) {
            arr.put(ex.toJsonObject());
        }
        obj.put("exercises", arr);
        return obj;
    }

    // JSON Deserialization
    public static WorkoutPlan fromJsonObject(JSONObject obj) throws JSONException {
        WorkoutPlan plan = new WorkoutPlan();
        plan.setId(obj.optString("id", ""));
        plan.setGoal(obj.optString("goal", ""));
        plan.setFitnessLevel(obj.optString("fitnessLevel", ""));
        plan.setCreatedAt(obj.optLong("createdAt", 0));

        JSONArray arr = obj.optJSONArray("exercises");
        List<Exercise> list = new ArrayList<>();
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                list.add(Exercise.fromJsonObject(arr.getJSONObject(i)));
            }
        }
        plan.setExercises(list);
        return plan;
    }

    public static class Exercise implements Serializable {
        private String name;
        private String description;
        private int sets;
        private int reps;
        private int durationSeconds; // 0 if rep-based

        public Exercise() {}

        public Exercise(String name, String description, int sets, int reps, int durationSeconds) {
            this.name = name;
            this.description = description;
            this.sets = sets;
            this.reps = reps;
            this.durationSeconds = durationSeconds;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public int getSets() { return sets; }
        public void setSets(int sets) { this.sets = sets; }

        public int getReps() { return reps; }
        public void setReps(int reps) { this.reps = reps; }

        public int getDurationSeconds() { return durationSeconds; }
        public void setDurationSeconds(int durationSeconds) { this.durationSeconds = durationSeconds; }

        public JSONObject toJsonObject() throws JSONException {
            JSONObject obj = new JSONObject();
            obj.put("name", name);
            obj.put("description", description);
            obj.put("sets", sets);
            obj.put("reps", reps);
            obj.put("durationSeconds", durationSeconds);
            return obj;
        }

        public static Exercise fromJsonObject(JSONObject obj) throws JSONException {
            return new Exercise(
                obj.optString("name", ""),
                obj.optString("description", ""),
                obj.optInt("sets", 0),
                obj.optInt("reps", 0),
                obj.optInt("durationSeconds", 0)
            );
        }
    }
}

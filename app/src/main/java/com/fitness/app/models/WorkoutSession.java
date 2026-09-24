package com.fitness.app.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WorkoutSession implements Serializable {

    private String sessionId;
    private String userId;
    private String workoutId;
    private String workoutTitle;
    private String workoutCategory;
    private String difficulty;

    private long startTimeMs;
    private long endTimeMs;
    private long activeDurationMs;
    private long pauseDurationMs;

    private int totalPlannedExercises;
    private int completedExercisesCount;
    private int partiallyCompletedExercisesCount;
    private int skippedExercisesCount;

    private int totalPlannedSets;
    private int completedSetsCount;

    private int totalPlannedReps;
    private int actualCompletedReps;
    private int invalidRepsCount;

    private double completionPercentage;
    private int estimatedCalories;

    private String heartRateStatus; // "Not available" or "Avg 142 BPM"
    private String stepsStatus;     // "Not applicable" or "1,240 steps"

    private String userRpe;          // "Easy", "Moderate", "Difficult", "Very Difficult"
    private boolean isStoppedEarly;

    private List<WorkoutExerciseItem> exercises;

    private String aiPerformanceAnalysis;
    private String aiRecommendations;
    private String dateStr;          // "yyyy-MM-dd"
    private long timestamp;

    public WorkoutSession() {
        this.sessionId = UUID.randomUUID().toString();
        this.userId = "";
        this.workoutId = "";
        this.workoutTitle = "Workout Session";
        this.workoutCategory = "General";
        this.difficulty = "Intermediate";
        this.startTimeMs = System.currentTimeMillis();
        this.endTimeMs = 0;
        this.activeDurationMs = 0;
        this.pauseDurationMs = 0;
        this.heartRateStatus = "Not available";
        this.stepsStatus = "Not applicable";
        this.userRpe = "Moderate";
        this.isStoppedEarly = false;
        this.exercises = new ArrayList<>();
        this.aiPerformanceAnalysis = "";
        this.aiRecommendations = "";
        this.dateStr = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
        this.timestamp = System.currentTimeMillis();
    }

    public WorkoutSession(String workoutTitle, String workoutCategory, String difficulty) {
        this();
        this.workoutTitle = workoutTitle != null ? workoutTitle : "Workout Session";
        this.workoutCategory = workoutCategory != null ? workoutCategory : "General";
        this.difficulty = difficulty != null ? difficulty : "Intermediate";
    }

    // Getters and Setters
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getWorkoutId() { return workoutId; }
    public void setWorkoutId(String workoutId) { this.workoutId = workoutId; }

    public String getWorkoutTitle() { return workoutTitle; }
    public void setWorkoutTitle(String workoutTitle) { this.workoutTitle = workoutTitle; }

    public String getWorkoutCategory() { return workoutCategory; }
    public void setWorkoutCategory(String workoutCategory) { this.workoutCategory = workoutCategory; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public long getStartTimeMs() { return startTimeMs; }
    public void setStartTimeMs(long startTimeMs) { this.startTimeMs = startTimeMs; }

    public long getEndTimeMs() { return endTimeMs; }
    public void setEndTimeMs(long endTimeMs) { this.endTimeMs = endTimeMs; }

    public long getActiveDurationMs() { return activeDurationMs; }
    public void setActiveDurationMs(long activeDurationMs) { this.activeDurationMs = activeDurationMs; }

    public long getPauseDurationMs() { return pauseDurationMs; }
    public void setPauseDurationMs(long pauseDurationMs) { this.pauseDurationMs = pauseDurationMs; }

    public int getTotalPlannedExercises() { return totalPlannedExercises; }
    public void setTotalPlannedExercises(int totalPlannedExercises) { this.totalPlannedExercises = totalPlannedExercises; }

    public int getCompletedExercisesCount() { return completedExercisesCount; }
    public void setCompletedExercisesCount(int completedExercisesCount) { this.completedExercisesCount = completedExercisesCount; }

    public int getPartiallyCompletedExercisesCount() { return partiallyCompletedExercisesCount; }
    public void setPartiallyCompletedExercisesCount(int partiallyCompletedExercisesCount) { this.partiallyCompletedExercisesCount = partiallyCompletedExercisesCount; }

    public int getSkippedExercisesCount() { return skippedExercisesCount; }
    public void setSkippedExercisesCount(int skippedExercisesCount) { this.skippedExercisesCount = skippedExercisesCount; }

    public int getTotalPlannedSets() { return totalPlannedSets; }
    public void setTotalPlannedSets(int totalPlannedSets) { this.totalPlannedSets = totalPlannedSets; }

    public int getCompletedSetsCount() { return completedSetsCount; }
    public void setCompletedSetsCount(int completedSetsCount) { this.completedSetsCount = completedSetsCount; }

    public int getTotalPlannedReps() { return totalPlannedReps; }
    public void setTotalPlannedReps(int totalPlannedReps) { this.totalPlannedReps = totalPlannedReps; }

    public int getActualCompletedReps() { return actualCompletedReps; }
    public void setActualCompletedReps(int actualCompletedReps) { this.actualCompletedReps = actualCompletedReps; }

    public int getInvalidRepsCount() { return invalidRepsCount; }
    public void setInvalidRepsCount(int invalidRepsCount) { this.invalidRepsCount = invalidRepsCount; }

    public double getCompletionPercentage() { return completionPercentage; }
    public void setCompletionPercentage(double completionPercentage) { this.completionPercentage = completionPercentage; }

    public int getEstimatedCalories() { return estimatedCalories; }
    public void setEstimatedCalories(int estimatedCalories) { this.estimatedCalories = estimatedCalories; }

    public String getHeartRateStatus() { return heartRateStatus; }
    public void setHeartRateStatus(String heartRateStatus) { this.heartRateStatus = heartRateStatus; }

    public String getStepsStatus() { return stepsStatus; }
    public void setStepsStatus(String stepsStatus) { this.stepsStatus = stepsStatus; }

    public String getUserRpe() { return userRpe; }
    public void setUserRpe(String userRpe) { this.userRpe = userRpe; }

    public boolean isStoppedEarly() { return isStoppedEarly; }
    public void setStoppedEarly(boolean stoppedEarly) { isStoppedEarly = stoppedEarly; }

    public List<WorkoutExerciseItem> getExercises() { return exercises; }
    public void setExercises(List<WorkoutExerciseItem> exercises) { this.exercises = exercises; }

    public String getAiPerformanceAnalysis() { return aiPerformanceAnalysis; }
    public void setAiPerformanceAnalysis(String aiPerformanceAnalysis) { this.aiPerformanceAnalysis = aiPerformanceAnalysis; }

    public String getAiRecommendations() { return aiRecommendations; }
    public void setAiRecommendations(String aiRecommendations) { this.aiRecommendations = aiRecommendations; }

    public String getDateStr() { return dateStr; }
    public void setDateStr(String dateStr) { this.dateStr = dateStr; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    /**
     * Recalculates all aggregated tallies (planned vs actual reps/sets, completion %, MET calories).
     */
    public void finalizeSessionMetrics(double userWeightKg) {
        if (endTimeMs <= 0) {
            endTimeMs = System.currentTimeMillis();
        }
        if (activeDurationMs <= 0) {
            activeDurationMs = Math.max(1000L, endTimeMs - startTimeMs - pauseDurationMs);
        }

        totalPlannedExercises = exercises != null ? exercises.size() : 0;
        completedExercisesCount = 0;
        partiallyCompletedExercisesCount = 0;
        skippedExercisesCount = 0;

        totalPlannedSets = 0;
        completedSetsCount = 0;
        totalPlannedReps = 0;
        actualCompletedReps = 0;
        invalidRepsCount = 0;
        double totalPlannedWorkload = 0.0;
        double totalActualWorkload = 0.0;

        if (exercises != null) {
            for (WorkoutExerciseItem item : exercises) {
                totalPlannedSets += item.getPlannedSets();
                completedSetsCount += item.getCompletedSets();

                int exPlannedReps = item.getTotalPlannedRepsForExercise();
                totalPlannedReps += exPlannedReps;
                actualCompletedReps += item.getActualCompletedReps();
                invalidRepsCount += item.getInvalidReps();

                if (exPlannedReps > 0) {
                    totalPlannedWorkload += exPlannedReps;
                    totalActualWorkload += item.getActualCompletedReps();
                } else if (item.getPlannedDurationSec() > 0) {
                    int durPlanned = (item.getPlannedSets() > 0 ? item.getPlannedSets() : 1) * item.getPlannedDurationSec();
                    totalPlannedWorkload += durPlanned;
                    totalActualWorkload += item.getActualDurationSec();
                } else {
                    totalPlannedWorkload += Math.max(1, item.getPlannedSets());
                    totalActualWorkload += item.getCompletedSets();
                }

                if (item.getStatus() == WorkoutExerciseItem.Status.COMPLETED) {
                    completedExercisesCount++;
                } else if (item.getStatus() == WorkoutExerciseItem.Status.PARTIALLY_COMPLETED) {
                    partiallyCompletedExercisesCount++;
                } else if (item.getStatus() == WorkoutExerciseItem.Status.SKIPPED) {
                    skippedExercisesCount++;
                } else {
                    // NOT_STARTED -> Mark as SKIPPED on finalization
                    item.setStatus(WorkoutExerciseItem.Status.SKIPPED);
                    skippedExercisesCount++;
                }
            }
        }

        // Calculate Completion Percentage based on total workload
        if (totalPlannedWorkload > 0) {
            completionPercentage = (totalActualWorkload / totalPlannedWorkload) * 100.0;
        } else if (totalPlannedExercises > 0) {
            completionPercentage = (completedExercisesCount / (double) totalPlannedExercises) * 100.0;
        } else {
            completionPercentage = 100.0;
        }

        // Clamp between 0.0% and 100.0%
        completionPercentage = Math.min(100.0, Math.max(0.0, completionPercentage));

        if (completionPercentage < 99.0) {
            isStoppedEarly = true;
        }

        // Calculate MET-based Calories
        double met = 5.0;
        String lowerTitle = workoutTitle.toLowerCase();
        if (lowerTitle.contains("squat") || lowerTitle.contains("leg")) met = 5.5;
        else if (lowerTitle.contains("run") || lowerTitle.contains("cardio") || lowerTitle.contains("hiit") || lowerTitle.contains("jump")) met = 8.0;
        else if (lowerTitle.contains("pushup") || lowerTitle.contains("chest") || lowerTitle.contains("arm")) met = 4.5;
        else if (lowerTitle.contains("deadlift") || lowerTitle.contains("strength") || lowerTitle.contains("press")) met = 6.0;

        double activeMins = activeDurationMs / 60000.0;
        if (activeMins < 0.25) activeMins = 0.25;

        double weight = userWeightKg > 0 ? userWeightKg : 70.0;
        estimatedCalories = (int) Math.round((met * 3.5 * weight / 200.0) * activeMins);
        if (estimatedCalories < 5) estimatedCalories = 12;
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("sessionId", sessionId);
        obj.put("userId", userId);
        obj.put("workoutId", workoutId);
        obj.put("workoutTitle", workoutTitle);
        obj.put("workoutCategory", workoutCategory);
        obj.put("difficulty", difficulty);
        obj.put("startTimeMs", startTimeMs);
        obj.put("endTimeMs", endTimeMs);
        obj.put("activeDurationMs", activeDurationMs);
        obj.put("pauseDurationMs", pauseDurationMs);
        obj.put("totalPlannedExercises", totalPlannedExercises);
        obj.put("completedExercisesCount", completedExercisesCount);
        obj.put("partiallyCompletedExercisesCount", partiallyCompletedExercisesCount);
        obj.put("skippedExercisesCount", skippedExercisesCount);
        obj.put("totalPlannedSets", totalPlannedSets);
        obj.put("completedSetsCount", completedSetsCount);
        obj.put("totalPlannedReps", totalPlannedReps);
        obj.put("actualCompletedReps", actualCompletedReps);
        obj.put("invalidRepsCount", invalidRepsCount);
        obj.put("completionPercentage", completionPercentage);
        obj.put("estimatedCalories", estimatedCalories);
        obj.put("heartRateStatus", heartRateStatus);
        obj.put("stepsStatus", stepsStatus);
        obj.put("userRpe", userRpe);
        obj.put("isStoppedEarly", isStoppedEarly);
        obj.put("aiPerformanceAnalysis", aiPerformanceAnalysis);
        obj.put("aiRecommendations", aiRecommendations);
        obj.put("dateStr", dateStr);
        obj.put("timestamp", timestamp);

        JSONArray arr = new JSONArray();
        if (exercises != null) {
            for (WorkoutExerciseItem item : exercises) {
                arr.put(item.toJsonObject());
            }
        }
        obj.put("exercises", arr);

        return obj;
    }

    public static WorkoutSession fromJsonObject(JSONObject obj) {
        if (obj == null) return null;
        WorkoutSession session = new WorkoutSession();
        session.setSessionId(obj.optString("sessionId", UUID.randomUUID().toString()));
        session.setUserId(obj.optString("userId", ""));
        session.setWorkoutId(obj.optString("workoutId", ""));
        session.setWorkoutTitle(obj.optString("workoutTitle", "Workout Session"));
        session.setWorkoutCategory(obj.optString("workoutCategory", "General"));
        session.setDifficulty(obj.optString("difficulty", "Intermediate"));
        session.setStartTimeMs(obj.optLong("startTimeMs", 0));
        session.setEndTimeMs(obj.optLong("endTimeMs", 0));
        session.setActiveDurationMs(obj.optLong("activeDurationMs", 0));
        session.setPauseDurationMs(obj.optLong("pauseDurationMs", 0));
        session.setTotalPlannedExercises(obj.optInt("totalPlannedExercises", 0));
        session.setCompletedExercisesCount(obj.optInt("completedExercisesCount", 0));
        session.setPartiallyCompletedExercisesCount(obj.optInt("partiallyCompletedExercisesCount", 0));
        session.setSkippedExercisesCount(obj.optInt("skippedExercisesCount", 0));
        session.setTotalPlannedSets(obj.optInt("totalPlannedSets", 0));
        session.setCompletedSetsCount(obj.optInt("completedSetsCount", 0));
        session.setTotalPlannedReps(obj.optInt("totalPlannedReps", 0));
        session.setActualCompletedReps(obj.optInt("actualCompletedReps", 0));
        session.setInvalidRepsCount(obj.optInt("invalidRepsCount", 0));
        session.setCompletionPercentage(obj.optDouble("completionPercentage", 0.0));
        session.setEstimatedCalories(obj.optInt("estimatedCalories", 0));
        session.setHeartRateStatus(obj.optString("heartRateStatus", "Not available"));
        session.setStepsStatus(obj.optString("stepsStatus", "Not applicable"));
        session.setUserRpe(obj.optString("userRpe", "Moderate"));
        session.setStoppedEarly(obj.optBoolean("isStoppedEarly", false));
        session.setAiPerformanceAnalysis(obj.optString("aiPerformanceAnalysis", ""));
        session.setAiRecommendations(obj.optString("aiRecommendations", ""));
        session.setDateStr(obj.optString("dateStr", ""));
        session.setTimestamp(obj.optLong("timestamp", 0));

        JSONArray arr = obj.optJSONArray("exercises");
        List<WorkoutExerciseItem> list = new ArrayList<>();
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                try {
                    WorkoutExerciseItem item = WorkoutExerciseItem.fromJsonObject(arr.getJSONObject(i));
                    if (item != null) list.add(item);
                } catch (JSONException ignored) {}
            }
        }
        session.setExercises(list);
        return session;
    }
}

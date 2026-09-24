package com.fitness.app.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class WorkoutExerciseItem implements Serializable {

    public enum Status {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETED,
        PARTIALLY_COMPLETED,
        SKIPPED
    }

    private String name;
    private String targetMuscle;
    private int plannedSets;
    private int completedSets;
    private int plannedReps;
    private int actualCompletedReps;
    private int invalidReps;
    private int plannedDurationSec;
    private int actualDurationSec;
    private Status status;
    private String formNotes;

    public WorkoutExerciseItem() {
        this.status = Status.NOT_STARTED;
        this.formNotes = "";
        this.invalidReps = 0;
    }

    public WorkoutExerciseItem(String name, String targetMuscle, int plannedSets, int plannedReps, int plannedDurationSec) {
        this.name = name;
        this.targetMuscle = targetMuscle != null ? targetMuscle : "Full Body";
        this.plannedSets = plannedSets;
        this.completedSets = 0;
        this.plannedReps = plannedReps;
        this.actualCompletedReps = 0;
        this.invalidReps = 0;
        this.plannedDurationSec = plannedDurationSec;
        this.actualDurationSec = 0;
        this.status = Status.NOT_STARTED;
        this.formNotes = "";
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTargetMuscle() { return targetMuscle; }
    public void setTargetMuscle(String targetMuscle) { this.targetMuscle = targetMuscle; }

    public int getPlannedSets() { return plannedSets; }
    public void setPlannedSets(int plannedSets) { this.plannedSets = plannedSets; }

    public int getCompletedSets() { return completedSets; }
    public void setCompletedSets(int completedSets) { this.completedSets = completedSets; }

    public int getPlannedReps() { return plannedReps; }
    public void setPlannedReps(int plannedReps) { this.plannedReps = plannedReps; }

    public int getActualCompletedReps() { return actualCompletedReps; }
    public void setActualCompletedReps(int actualCompletedReps) { this.actualCompletedReps = actualCompletedReps; }

    public int getInvalidReps() { return invalidReps; }
    public void setInvalidReps(int invalidReps) { this.invalidReps = invalidReps; }

    public int getPlannedDurationSec() { return plannedDurationSec; }
    public void setPlannedDurationSec(int plannedDurationSec) { this.plannedDurationSec = plannedDurationSec; }

    public int getActualDurationSec() { return actualDurationSec; }
    public void setActualDurationSec(int actualDurationSec) { this.actualDurationSec = actualDurationSec; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getFormNotes() { return formNotes; }
    public void setFormNotes(String formNotes) { this.formNotes = formNotes; }

    public int getTotalPlannedRepsForExercise() {
        if (plannedReps > 0 && plannedSets > 0) {
            return plannedSets * plannedReps;
        }
        return plannedReps;
    }

    // Convenience calculation
    public double getExerciseCompletionPercentage() {
        int totalPlanned = getTotalPlannedRepsForExercise();
        if (totalPlanned <= 0 && plannedDurationSec <= 0 && plannedSets <= 0) return 100.0;

        if (totalPlanned > 0) {
            return Math.min(100.0, (actualCompletedReps / (double) Math.max(1, totalPlanned)) * 100.0);
        } else if (plannedDurationSec > 0) {
            int totalPlannedDuration = (plannedSets > 0 ? plannedSets : 1) * plannedDurationSec;
            return Math.min(100.0, (actualDurationSec / (double) Math.max(1, totalPlannedDuration)) * 100.0);
        } else if (plannedSets > 0) {
            return Math.min(100.0, (completedSets / (double) Math.max(1, plannedSets)) * 100.0);
        }
        return 0.0;
    }

    public JSONObject toJsonObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("name", name);
        obj.put("targetMuscle", targetMuscle);
        obj.put("plannedSets", plannedSets);
        obj.put("completedSets", completedSets);
        obj.put("plannedReps", plannedReps);
        obj.put("actualCompletedReps", actualCompletedReps);
        obj.put("invalidReps", invalidReps);
        obj.put("plannedDurationSec", plannedDurationSec);
        obj.put("actualDurationSec", actualDurationSec);
        obj.put("status", status != null ? status.name() : Status.NOT_STARTED.name());
        obj.put("formNotes", formNotes != null ? formNotes : "");
        return obj;
    }

    public static WorkoutExerciseItem fromJsonObject(JSONObject obj) {
        if (obj == null) return null;
        WorkoutExerciseItem item = new WorkoutExerciseItem();
        item.setName(obj.optString("name", "Exercise"));
        item.setTargetMuscle(obj.optString("targetMuscle", "Full Body"));
        item.setPlannedSets(obj.optInt("plannedSets", 3));
        item.setCompletedSets(obj.optInt("completedSets", 0));
        item.setPlannedReps(obj.optInt("plannedReps", 10));
        item.setActualCompletedReps(obj.optInt("actualCompletedReps", 0));
        item.setInvalidReps(obj.optInt("invalidReps", 0));
        item.setPlannedDurationSec(obj.optInt("plannedDurationSec", 0));
        item.setActualDurationSec(obj.optInt("actualDurationSec", 0));
        item.setFormNotes(obj.optString("formNotes", ""));

        String statusStr = obj.optString("status", Status.NOT_STARTED.name());
        try {
            item.setStatus(Status.valueOf(statusStr));
        } catch (Exception e) {
            item.setStatus(Status.NOT_STARTED);
        }
        return item;
    }
}

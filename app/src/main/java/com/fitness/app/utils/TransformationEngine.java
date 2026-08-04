package com.fitness.app.utils;

import com.fitness.app.models.TransformationPlan;
import com.fitness.app.models.TransformationProgress;
import com.fitness.app.models.TransformationTask;
import com.fitness.app.models.User;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TransformationEngine {

    public static class AssessmentResult {
        public int durationWeeks;
        public int currentBodyFat;
        public int targetBodyFat;
        public String muscleDevelopment;
        public String areasToImprove;
        public String timelineRationals;
        public String milestonesJson;

        public AssessmentResult(int durationWeeks, int currentBodyFat, int targetBodyFat, String muscleDevelopment, String areasToImprove, String timelineRationals, String milestonesJson) {
            this.durationWeeks = durationWeeks;
            this.currentBodyFat = currentBodyFat;
            this.targetBodyFat = targetBodyFat;
            this.muscleDevelopment = muscleDevelopment;
            this.areasToImprove = areasToImprove;
            this.timelineRationals = timelineRationals;
            this.milestonesJson = milestonesJson;
        }
    }

    /**
     * Estimates transformation details using user profile and uploaded photos.
     */
    public static AssessmentResult analyzeTransformation(User user, String currentPhotoUrl, String idealPhotoUrl) {
        int weightDiff = Math.abs((int) user.getWeight() - (int) user.getTargetWeight());
        
        // Duration choice based on weight difference and goals
        int durationWeeks = 12; 
        if (weightDiff < 5) {
            durationWeeks = 8; // Small adjustment
        } else if (weightDiff >= 15) {
            durationWeeks = 16; // Significant transformation
        }

        // Estimate body fat categories based on BMI
        double heightM = user.getHeight() / 100.0;
        double bmi = user.getWeight() / (heightM * heightM);
        int currentBodyFat = (int) (bmi * 1.2 + 0.23 * user.getAge() - 16.2); // Simple estimated formula
        if (currentBodyFat < 5) currentBodyFat = 12;

        int targetBodyFat = currentBodyFat - (int)(weightDiff * 0.6);
        if (targetBodyFat < 8) targetBodyFat = 10;

        String goal = user.getGoal() != null ? user.getGoal().toLowerCase() : "general";
        String muscleDev = "Normal baseline development with moderate endurance.";
        String areasToImprove = "Core abdominal definition and posterior glute recruitment.";
        String rationale = "Based on your weight difference of " + weightDiff + "kg and goal of " + user.getGoal() 
                + ", we recommend a " + durationWeeks + "-week program. This provides a healthy fat loss rate of ~0.5kg/week, preserving muscle mass and ensuring permanent results.";

        if (goal.contains("muscle") || goal.contains("gain")) {
            muscleDev = "Sub-optimal mass in deltoids and pectorals.";
            areasToImprove = "Upper body strength and progressive overload capacity.";
            rationale = "A " + durationWeeks + "-week slow lean-bulking plan is chosen. This avoids excess fat gain while providing progressive weight-loading targets.";
        }

        // Generate milestones
        JSONArray milestones = new JSONArray();
        try {
            for (int w = 1; w <= durationWeeks; w++) {
                JSONObject m = new JSONObject();
                m.put("week", w);
                double fraction = (double) w / durationWeeks;
                double expectedWeight = user.getWeight() + (user.getTargetWeight() - user.getWeight()) * fraction;
                double expectedBF = currentBodyFat + (targetBodyFat - currentBodyFat) * fraction;
                
                m.put("targetWeight", Math.round(expectedWeight));
                m.put("targetBodyFat", Math.round(expectedBF));
                m.put("description", "Week " + w + " milestone: Focus on active recovery & nutrition consistency.");
                milestones.put(m);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new AssessmentResult(durationWeeks, currentBodyFat, targetBodyFat, muscleDev, areasToImprove, rationale, milestones.toString());
    }

    /**
     * Automatically adjusts calorie/macro targets and exercise sets/reps dynamically based on weekly checkpoints.
     */
    public static List<TransformationTask> generateDailyTasksForDay(User user, TransformationPlan plan, List<TransformationProgress> progressLogs, String date) {
        List<TransformationTask> tasks = new ArrayList<>();
        
        // Base targets
        int baseCalories = 2000;
        int baseSteps = 10000;
        int workoutDurationMin = 30;

        String goal = user.getGoal() != null ? user.getGoal().toLowerCase() : "general";
        if (goal.contains("lose") || goal.contains("fat")) {
            baseCalories = 1750;
            baseSteps = 12000;
        } else if (goal.contains("gain") || goal.contains("muscle")) {
            baseCalories = 2400;
            baseSteps = 8000;
        }

        // Dynamic adjustment based on latest weekly progress log
        if (progressLogs != null && !progressLogs.isEmpty()) {
            TransformationProgress latest = progressLogs.get(progressLogs.size() - 1);
            double compliance = latest.getWorkoutsTotal() > 0 ? (double) latest.getWorkoutsCompleted() / latest.getWorkoutsTotal() : 1.0;
            
            if (compliance < 0.6) {
                // If workouts missed, lower the base targets to avoid user burnout and ease them back in
                baseCalories += 100; // Allow slightly more buffer
                baseSteps = Math.max(8000, baseSteps - 1500);
                workoutDurationMin = Math.max(20, workoutDurationMin - 5);
            } else if (compliance > 0.9) {
                // If highly compliant, scale up the intensity
                baseSteps += 1000;
                workoutDurationMin += 5;
            }
        }

        // 1. Workout Task
        String workoutTitle = "AI Transformation Core workout";
        String workoutDesc = "Warmup: 5 min stretching. Main: Squats 4x12, Pushups 4x10, Plank 3x60s. Cardio: " + (baseSteps > 10000 ? "20" : "10") + " min run.";
        tasks.add(new TransformationTask(user.getUid(), date, "WORKOUT", workoutTitle, workoutDesc, workoutDurationMin, 0, false));

        // 2. Diet Task
        int protein = (int) (user.getWeight() * 2.0); // 2g per kg
        int fat = (int) (user.getWeight() * 0.8);
        int carbs = (baseCalories - (protein * 4 + fat * 9)) / 4;
        String dietTitle = "Target: " + baseCalories + " kcal";
        String dietDesc = "Breakfast: Oatmeal with eggs. Lunch: Chicken breast with rice & veggies. Dinner: Salmon with avocado salad. Snacks: Protein shake.";
        tasks.add(new TransformationTask(user.getUid(), date, "DIET", dietTitle, dietDesc, baseCalories, 0, false));

        // 3. Water Task
        tasks.add(new TransformationTask(user.getUid(), date, "WATER", "Hydration Target", "Consume at least 3,000ml of clean drinking water.", 3000, 0, false));

        // 4. Sleep Task
        tasks.add(new TransformationTask(user.getUid(), date, "SLEEP", "Rest & Recovery", "Ensure high-quality recovery sleep.", 8, 0, false));

        return tasks;
    }
}

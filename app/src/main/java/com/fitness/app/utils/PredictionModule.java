package com.fitness.app.utils;

import com.fitness.app.models.ProgressLog;
import com.fitness.app.models.User;

import java.util.List;
import java.util.Map;

public class PredictionModule {

    public static class PredictionResult {
        public double estimatedWeightIn4Weeks;
        public int daysToReachGoal;
        public double estimatedCaloriesBurnedNextWeek;
        public String confidenceLevel;
        public String predictionMessage;

        public PredictionResult(double estimatedWeightIn4Weeks, int daysToReachGoal, double estimatedCaloriesBurnedNextWeek, String confidenceLevel, String predictionMessage) {
            this.estimatedWeightIn4Weeks = estimatedWeightIn4Weeks;
            this.daysToReachGoal = daysToReachGoal;
            this.estimatedCaloriesBurnedNextWeek = estimatedCaloriesBurnedNextWeek;
            this.confidenceLevel = confidenceLevel;
            this.predictionMessage = predictionMessage;
        }
    }

    public static PredictionResult getPredictions(User user, Map<String, ProgressLog> logs) {
        double currentWeight = user.getWeight();
        double currentHeight = user.getHeight();
        String goal = user.getGoal(); // "Improve Shape", "Lean & Tone", "Lose Fat", "Gain Muscle", "Improve Fitness"

        // Default local calculations
        double calorieDeficitPerDay = 500.0; // Default estimate
        double estimatedWeightIn4Weeks = currentWeight;
        int daysToGoal = 30;
        double estimatedCaloriesBurned = 2200;
        String confidence = "Moderate";
        String message = "Keep tracking your daily metrics to improve prediction accuracy.";

        // Calculate average metrics if logs exist
        if (logs != null && !logs.isEmpty()) {
            double totalWeight = 0;
            double totalBurned = 0;
            double totalConsumed = 0;
            int count = 0;
            for (ProgressLog log : logs.values()) {
                if (log.getCurrentWeight() > 0) {
                    totalWeight += log.getCurrentWeight();
                    count++;
                }
                totalBurned += log.getCaloriesBurned() > 0 ? log.getCaloriesBurned() : 2000;
                totalConsumed += log.getCaloriesConsumed() > 0 ? log.getCaloriesConsumed() : 2000;
            }
            if (count > 0) {
                currentWeight = totalWeight / count;
            }
            double avgBurned = totalBurned / logs.size();
            double avgConsumed = totalConsumed / logs.size();
            calorieDeficitPerDay = avgBurned - avgConsumed;
            estimatedCaloriesBurned = avgBurned;
        }

        // Calibrate based on goal
        if ("Lose Fat".equalsIgnoreCase(goal)) {
            // 7700 kcal deficit = 1kg fat loss
            double weightLossPerDayKg = calorieDeficitPerDay > 0 ? (calorieDeficitPerDay / 7700.0) : 0.1; // Default min loss
            if (weightLossPerDayKg <= 0) weightLossPerDayKg = 0.05; // Fallback if consuming > burning
            
            estimatedWeightIn4Weeks = currentWeight - (weightLossPerDayKg * 28);
            double targetWeight = currentWeight - 5.0; // Arbitrary target weight (5kg less)
            daysToGoal = (int) Math.max(7, Math.round((currentWeight - targetWeight) / weightLossPerDayKg));
            
            message = String.format("Based on your calorie deficit, you are on track to lose %.1f kg in the next 4 weeks.", currentWeight - estimatedWeightIn4Weeks);
            confidence = logs != null && logs.size() >= 5 ? "High" : "Moderate";
        } else if ("Gain Muscle".equalsIgnoreCase(goal)) {
            double weightGainPerDayKg = calorieDeficitPerDay < 0 ? (Math.abs(calorieDeficitPerDay) / 6000.0) : 0.05;
            if (weightGainPerDayKg <= 0) weightGainPerDayKg = 0.03;

            estimatedWeightIn4Weeks = currentWeight + (weightGainPerDayKg * 28);
            double targetWeight = currentWeight + 4.0; // Arbitrary target weight
            daysToGoal = (int) Math.max(7, Math.round((targetWeight - currentWeight) / weightGainPerDayKg));

            message = String.format("With your current caloric surplus, you are projected to gain %.1f kg of lean mass in 4 weeks.", estimatedWeightIn4Weeks - currentWeight);
            confidence = logs != null && logs.size() >= 5 ? "High" : "Moderate";
        } else {
            // General maintenance
            estimatedWeightIn4Weeks = currentWeight;
            daysToGoal = 0;
            message = "Your weight is projected to remain stable. Keep up the active lifestyle!";
        }

        // Prevent invalid negative results
        if (estimatedWeightIn4Weeks < 30) estimatedWeightIn4Weeks = 30;

        return new PredictionResult(
            estimatedWeightIn4Weeks,
            daysToGoal,
            estimatedCaloriesBurned,
            confidence,
            message
        );
    }
}

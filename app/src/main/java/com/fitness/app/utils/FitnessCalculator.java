package com.fitness.app.utils;

import java.util.Locale;

public class FitnessCalculator {

    public static double calculateBmi(double weightKg, double heightCm) {
        if (weightKg <= 0 || heightCm <= 0) return 0.0;
        double heightM = heightCm / 100.0;
        return weightKg / (heightM * heightM);
    }

    public static String getBmiCategory(double bmi) {
        if (bmi <= 0) return "Unknown";
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25.0) return "Normal weight";
        if (bmi < 30.0) return "Overweight";
        return "Obese";
    }

    public static double calculateBmr(double weightKg, double heightCm, int age, String gender) {
        if (weightKg <= 0 || heightCm <= 0) return 0.0;
        int ageValue = age > 0 ? age : 25; // Default age if not provided
        boolean isMale = gender == null || !gender.trim().equalsIgnoreCase("female");

        // Mifflin-St Jeor Formula
        if (isMale) {
            return (10 * weightKg) + (6.25 * heightCm) - (5 * ageValue) + 5;
        } else {
            return (10 * weightKg) + (6.25 * heightCm) - (5 * ageValue) - 161;
        }
    }

    public static double calculateTdee(double bmr, String activityLevel) {
        if (bmr <= 0) return 0.0;
        if (activityLevel == null) activityLevel = "Moderate";
        String act = activityLevel.toLowerCase(Locale.getDefault());

        double multiplier = 1.375; // Default Moderate
        if (act.contains("sedentary") || act.contains("low") || act.contains("light")) {
            multiplier = 1.2;
        } else if (act.contains("high") || act.contains("heavy") || act.contains("athlete") || act.contains("very active")) {
            multiplier = 1.725;
        } else if (act.contains("active") || act.contains("moderate")) {
            multiplier = 1.55;
        }
        return bmr * multiplier;
    }

    public static int calculateDailyCalorieTarget(double tdee, String goal) {
        if (tdee <= 0) return 2000;
        if (goal == null) goal = "maintain";
        String g = goal.toLowerCase(Locale.getDefault());

        if (g.contains("lose") || g.contains("fat") || g.contains("cut")) {
            return (int) Math.max(1200, tdee - 500); // 500 kcal deficit
        } else if (g.contains("gain") || g.contains("build") || g.contains("muscle") || g.contains("bulk")) {
            return (int) (tdee + 400); // 400 kcal surplus
        }
        return (int) tdee;
    }

    public static int calculateProteinTarget(double weightKg, String goal) {
        if (weightKg <= 0) weightKg = 70.0;
        if (goal == null) goal = "maintain";
        String g = goal.toLowerCase(Locale.getDefault());

        double multiplier = 1.6; // g per kg
        if (g.contains("build") || g.contains("muscle") || g.contains("gain") || g.contains("bulk")) {
            multiplier = 2.0;
        } else if (g.contains("lose") || g.contains("fat") || g.contains("cut")) {
            multiplier = 1.8;
        }
        return (int) Math.round(weightKg * multiplier);
    }

    public static int calculateDailyWaterGoal(double weightKg) {
        if (weightKg <= 0) weightKg = 70.0;
        return (int) Math.round(weightKg * 35.0); // 35 ml per kg
    }

    public static double calculateWeightProgressPercent(double startWeight, double currentWeight, double targetWeight) {
        if (startWeight <= 0 || currentWeight <= 0 || targetWeight <= 0) return 0.0;
        if (Double.compare(startWeight, targetWeight) == 0) return 100.0;

        double totalToChange = Math.abs(startWeight - targetWeight);
        double changeDone = Math.abs(startWeight - currentWeight);

        double percent = (changeDone / totalToChange) * 100.0;
        return Math.min(100.0, Math.max(0.0, percent));
    }
}

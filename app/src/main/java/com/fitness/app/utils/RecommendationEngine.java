package com.fitness.app.utils;

import com.fitness.app.models.ProgressLog;
import com.fitness.app.models.User;
import com.fitness.app.models.WorkoutPlan;
import com.fitness.app.models.DietPlan;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecommendationEngine {

    public static List<String> getRecommendations(User user, Map<String, ProgressLog> logs) {
        List<String> list = new ArrayList<>();

        // Default Recommendations based on Goal
        String goal = user.getGoal();
        if ("Lose Fat".equalsIgnoreCase(goal)) {
            list.add("Focus on calorie-deficit meals. High fiber and low glycemic foods will help keep you full.");
            list.add("Integrate 30-40 minutes of moderate intensity cardio (HIIT or brisk walking) 3-4 times a week.");
        } else if ("Gain Muscle".equalsIgnoreCase(goal)) {
            list.add("Increase protein intake to 1.6g - 2.2g per kg of body weight to support muscle protein synthesis.");
            list.add("Focus on progressive overload: increase the weight or reps slightly in each workout session.");
        } else {
            list.add("Keep a consistent schedule. Balance strength training with active recovery days.");
            list.add("Prioritize nutrient-dense foods: colorful vegetables, whole grains, and healthy fats.");
        }

        // Analyze recent logs
        if (logs != null && !logs.isEmpty()) {
            double avgWater = 0;
            double avgSteps = 0;
            double avgSleep = 0;
            for (ProgressLog log : logs.values()) {
                avgWater += log.getWaterConsumedMl();
                avgSteps += log.getStepsCount();
                avgSleep += log.getSleepDurationMinutes();
            }
            avgWater /= logs.size();
            avgSteps /= logs.size();
            avgSleep /= logs.size();

            // Water intake check
            if (avgWater < 2000) {
                list.add("Your average water intake is below 2L. Try carrying a water bottle to reach your daily hydration target.");
            } else {
                list.add("Great job keeping hydrated! Hydration supports metabolism and workout recovery.");
            }

            // Steps check
            if (avgSteps < 6000) {
                list.add("Your daily step count is low. Aim for a short 15-minute walk after meals to increase active calorie burn.");
            } else if (avgSteps > 10000) {
                list.add("Fantastic daily activity level! Keep maintaining over 10,000 steps daily.");
            }

            // Sleep check
            if (avgSleep > 0 && avgSleep < 420) { // 7 hours
                list.add("Sleep is critical for muscle recovery. Try to secure 7-8 hours of restful sleep every night.");
            }
        }

        // General wellness advice
        list.add("Listen to your body. If you feel excessive fatigue, schedule an active recovery day with gentle stretching.");

        return list;
    }

    public static String getMotivationalMessage() {
        String[] motivations = {
            "Consistency is key! Every small step counts towards your larger goal.",
            "You are stronger than your excuses. Let's make today a great workout day!",
            "Progress is progress, no matter how small. Keep moving forward!",
            "Believe you can and you're halfway there. Keep pushing!",
            "Your body can stand almost anything. It's your mind that you have to convince.",
            "Don't stop when you are tired. Stop when you are done!"
        };
        int index = (int) (Math.random() * motivations.length);
        return motivations[index];
    }

    public static WorkoutPlan generateDailyWorkoutPlan(User user, String difficultyFeedback, boolean completedYesterday) {
        String level = user.getFitnessExperience();
        if (level == null || level.isEmpty()) level = "Beginner";

        int repsMultiplier = 0;
        if ("Easy".equalsIgnoreCase(difficultyFeedback)) {
            repsMultiplier = 2;
        } else if ("Hard".equalsIgnoreCase(difficultyFeedback) || !completedYesterday) {
            repsMultiplier = -2;
        }

        String goal = user.getGoal();
        String equip = user.getAvailableEquipment();
        if (equip == null || equip.isEmpty()) equip = "Bodyweight";

        List<WorkoutPlan.Exercise> exercises = new ArrayList<>();
        
        if ("Lose Fat".equalsIgnoreCase(goal) || "Lose Weight".equalsIgnoreCase(goal)) {
            if ("Bodyweight".equalsIgnoreCase(equip)) {
                exercises.add(new WorkoutPlan.Exercise("Jumping Jacks", "High-intensity cardio warm-up", 3, Math.max(10, 20 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Bodyweight Squats", "Lower body strength builder", 4, Math.max(8, 15 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Mountain Climbers", "Core and stamina push", 3, Math.max(15, 30 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Plank Hold", "Static abdominal hold", 3, 0, Math.max(30, 45 + repsMultiplier * 5)));
            } else if ("Dumbbells".equalsIgnoreCase(equip)) {
                exercises.add(new WorkoutPlan.Exercise("Dumbbell Goblet Squats", "Weighted squat targeting quads & glutes", 4, Math.max(8, 12 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Dumbbell Shoulder Press", "Upper body press", 3, Math.max(8, 10 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Dumbbell Renegade Rows", "Core stability and back strength", 3, Math.max(8, 10 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Burpees", "Full body cardio burst", 3, Math.max(5, 10 + repsMultiplier), 0));
            } else {
                exercises.add(new WorkoutPlan.Exercise("Barbell Back Squats", "Core lower body compound movement", 4, Math.max(6, 10 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Bench Press", "Chest builder", 4, Math.max(6, 10 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Lat Pulldown", "Back width builder", 3, Math.max(8, 12 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Treadmill Sprint", "Interval cardio sprint", 1, 0, 300));
            }
        } else if ("Gain Muscle".equalsIgnoreCase(goal) || "Build Muscle".equalsIgnoreCase(goal)) {
            if ("Bodyweight".equalsIgnoreCase(equip)) {
                exercises.add(new WorkoutPlan.Exercise("Push-ups", "Chest, shoulders, and triceps builder", 4, Math.max(8, 15 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Pike Push-ups", "Shoulder strength focus", 3, Math.max(6, 10 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Bulgarian Split Squats", "Single-leg hypertrophy builder", 3, Math.max(8, 12 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Pull-ups", "Back and biceps strength builder", 4, Math.max(4, 8 + repsMultiplier), 0));
            } else if ("Dumbbells".equalsIgnoreCase(equip)) {
                exercises.add(new WorkoutPlan.Exercise("Dumbbell Chest Press", "Weighted chest builder", 4, Math.max(8, 12 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Dumbbell Romanian Deadlifts", "Hamstrings and glutes builder", 4, Math.max(8, 10 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Dumbbell Bicep Curls", "Isolated arm hypertrophy", 3, Math.max(10, 15 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Dumbbell Lateral Raises", "Shoulder width builder", 3, Math.max(10, 12 + repsMultiplier), 0));
            } else {
                exercises.add(new WorkoutPlan.Exercise("Deadlifts", "Heavy full body posterior builder", 4, Math.max(4, 6 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Incline Dumbbell Press", "Upper chest builder", 4, Math.max(8, 12 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Barbell Rows", "Upper back strength", 4, Math.max(8, 10 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Tricep Cable Pushdowns", "Arm isolated extension", 3, Math.max(10, 15 + repsMultiplier), 0));
            }
        } else {
            exercises.add(new WorkoutPlan.Exercise("Burpees", "Full body explosive movement", 3, Math.max(5, 8 + repsMultiplier), 0));
            exercises.add(new WorkoutPlan.Exercise("Air Squats", "Lower body endurance builder", 4, Math.max(10, 15 + repsMultiplier), 0));
            exercises.add(new WorkoutPlan.Exercise("Superman Hold", "Lower back and posterior chain builder", 3, 0, Math.max(20, 30 + repsMultiplier * 3)));
            exercises.add(new WorkoutPlan.Exercise("Jumping Rope", "Steady cardio push", 3, 0, 120));
        }

        if ("Hard".equalsIgnoreCase(difficultyFeedback) && !completedYesterday) {
            exercises.clear();
            exercises.add(new WorkoutPlan.Exercise("Gentle Yoga & Stretching", "Active rest recovery stretches", 1, 0, 900));
        }

        String planId = "workout_" + System.currentTimeMillis();
        return new WorkoutPlan(planId, goal, level, exercises, System.currentTimeMillis());
    }

    public static DietPlan generateDailyDietPlan(User user, String dietFeedback, boolean completedYesterday) {
        return generateDailyDietPlan(user, dietFeedback, completedYesterday, null);
    }

    public static DietPlan generateDailyDietPlan(User user, String dietFeedback, boolean completedYesterday, String dateStr) {
        return generateDailyDietPlan(user, dietFeedback, completedYesterday, false, false, dateStr);
    }

    public static DietPlan generateDailyDietPlan(User user, String dietFeedback, boolean completedYesterday, boolean cheatMealEnabled, boolean hasWorkoutToday) {
        return generateDailyDietPlan(user, dietFeedback, completedYesterday, cheatMealEnabled, hasWorkoutToday, null);
    }

    public static DietPlan generateDailyDietPlan(User user, String dietFeedback, boolean completedYesterday, boolean cheatMealEnabled, boolean hasWorkoutToday, String dateStr) {
        double weight = user.getWeight() > 0 ? user.getWeight() : 70.0;
        double height = user.getHeight() > 0 ? user.getHeight() : 170.0;
        int age = user.getAge() > 0 ? user.getAge() : 25;
        String gender = user.getGender() != null ? user.getGender() : "Female";
        String goal = user.getGoal() != null ? user.getGoal() : "Maintain Weight";
        String dietPref = user.getDietaryPreference() != null ? user.getDietaryPreference() : "Balanced";
        String allergies = user.getAllergies() != null ? user.getAllergies() : "";
        String medicalConditions = user.getMedicalConditions() != null ? user.getMedicalConditions() : "";

        // 1. Calculate base daily calories (BMR + Activity Multiplier)
        int targetCalories = user.getDailyCaloriesGoal();
        if (targetCalories <= 0) {
            double bmr;
            if ("Male".equalsIgnoreCase(gender)) {
                bmr = 10 * weight + 6.25 * height - 5 * age + 5;
            } else {
                bmr = 10 * weight + 6.25 * height - 5 * age - 161;
            }

            double multiplier = 1.375; // Light activity default
            String act = user.getActivityLevel();
            if (act != null) {
                if (act.contains("Sedentary")) multiplier = 1.2;
                else if (act.contains("Light")) multiplier = 1.375;
                else if (act.contains("Moderat") || act.contains("Active")) multiplier = 1.55;
                else if (act.contains("Very") || act.contains("Athlete")) multiplier = 1.725;
            }

            double tdee = bmr * multiplier;
            if (goal.contains("Loss") || goal.contains("Fat")) {
                targetCalories = (int) (tdee - 500);
            } else if (goal.contains("Gain") || goal.contains("Weight Gain") || goal.contains("Muscle")) {
                targetCalories = (int) (tdee + 350);
            } else if (goal.contains("Recomposition")) {
                targetCalories = (int) (tdee - 150);
            } else {
                targetCalories = (int) tdee;
            }

            int minCal = "Male".equalsIgnoreCase(gender) ? 1500 : 1200;
            if (targetCalories < minCal) targetCalories = minCal;
        }

        if ("No".equalsIgnoreCase(dietFeedback)) {
            targetCalories = (int) (targetCalories * 0.95);
        }

        // 2. Macronutrient percentages
        int proteinPercent = 30;
        int carbsPercent = 40;
        int fatPercent = 30;

        if ("Keto".equalsIgnoreCase(dietPref)) {
            proteinPercent = 20;
            carbsPercent = 5;
            fatPercent = 75;
        } else if ("Vegan".equalsIgnoreCase(dietPref) || "Vegetarian".equalsIgnoreCase(dietPref)) {
            proteinPercent = 20;
            carbsPercent = 55;
            fatPercent = 25;
        } else if (goal.contains("Gain") || goal.contains("Muscle") || goal.contains("Performance")) {
            proteinPercent = 35;
            carbsPercent = 45;
            fatPercent = 20;
        } else if (goal.contains("Loss") || goal.contains("Fat")) {
            proteinPercent = 40;
            carbsPercent = 30;
            fatPercent = 30;
        }

        int targetProtein = (int) ((targetCalories * (proteinPercent / 100.0)) / 4.0);
        int targetCarbs = (int) ((targetCalories * (carbsPercent / 100.0)) / 4.0);
        int targetFat = (int) ((targetCalories * (fatPercent / 100.0)) / 9.0);
        int targetWater = user.getDailyWaterGoal() > 0 ? user.getDailyWaterGoal() : (int) (weight * 35);
        if (targetWater < 2000) targetWater = 2000;

        int targetFiber = (int) (targetCalories * 0.014); // 14g per 1000 kcal
        if (targetFiber < 25) targetFiber = 25;
        int targetSugar = (int) ((targetCalories * 0.08) / 4.0); // 8% of energy in sugar
        
        int targetSodium = 2000;
        if (medicalConditions.toLowerCase().contains("hypertension") || medicalConditions.toLowerCase().contains("blood pressure")) {
            targetSodium = 1500;
        }

        // Determine specific day of the week based on dateStr to ensure weekly variety
        int day = 1;
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                java.util.Date date = sdf.parse(dateStr);
                if (date != null) {
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.setTime(date);
                    day = cal.get(java.util.Calendar.DAY_OF_WEEK);
                }
            } catch (Exception e) {
                java.util.Calendar calendar = java.util.Calendar.getInstance();
                day = calendar.get(java.util.Calendar.DAY_OF_WEEK);
            }
        } else {
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            day = calendar.get(java.util.Calendar.DAY_OF_WEEK);
        }

        // 3. Generate meals with calorie splitting
        List<DietPlan.Meal> meals = new ArrayList<>();
        double pBreakfast = 0.20;
        double pMorningDrink = 0.05;
        double pMorningSnack = 0.10;
        double pLunch = 0.30;
        double pAfternoonDrink = 0.05;
        double pAfternoonSnack = 0.10;
        double pDinner = 0.20;
        
        double pCheatMeal = 0.0;
        if (cheatMealEnabled) {
            pCheatMeal = 0.20;
            pDinner = 0.10;
            pAfternoonSnack = 0.05;
        }
        
        int breakfastCal = (int) (targetCalories * pBreakfast);
        int morningDrinkCal = (int) (targetCalories * pMorningDrink);
        int morningSnackCal = (int) (targetCalories * pMorningSnack);
        int lunchCal = (int) (targetCalories * pLunch);
        int afternoonDrinkCal = (int) (targetCalories * pAfternoonDrink);
        int afternoonSnackCal = (int) (targetCalories * pAfternoonSnack);
        int dinnerCal = (int) (targetCalories * pDinner);
        int cheatMealCal = (int) (targetCalories * pCheatMeal);
        
        int totalUsed = breakfastCal + morningDrinkCal + morningSnackCal + lunchCal + afternoonDrinkCal + afternoonSnackCal + dinnerCal + cheatMealCal;
        int diff = targetCalories - totalUsed;
        dinnerCal += diff;

        meals.add(generateMealForType("Breakfast", breakfastCal, dietPref, goal, allergies, medicalConditions, proteinPercent, carbsPercent, fatPercent, day));
        meals.add(generateMealForType("Morning Drink", morningDrinkCal, dietPref, goal, allergies, medicalConditions, proteinPercent, carbsPercent, fatPercent, day));
        meals.add(generateMealForType("Morning Snack", morningSnackCal, dietPref, goal, allergies, medicalConditions, proteinPercent, carbsPercent, fatPercent, day));
        meals.add(generateMealForType("Lunch", lunchCal, dietPref, goal, allergies, medicalConditions, proteinPercent, carbsPercent, fatPercent, day));
        meals.add(generateMealForType("Afternoon Drink", afternoonDrinkCal, dietPref, goal, allergies, medicalConditions, proteinPercent, carbsPercent, fatPercent, day));
        meals.add(generateMealForType("Afternoon Snack", afternoonSnackCal, dietPref, goal, allergies, medicalConditions, proteinPercent, carbsPercent, fatPercent, day));
        if (cheatMealCal > 0) {
            meals.add(generateMealForType("Cheat Meal", cheatMealCal, dietPref, goal, allergies, medicalConditions, proteinPercent, carbsPercent, fatPercent, day));
        } else {
            meals.add(generateMealForType("Dinner", dinnerCal, dietPref, goal, allergies, medicalConditions, proteinPercent, carbsPercent, fatPercent, day));
        }

        String planId = "diet_" + System.currentTimeMillis();
        return new DietPlan(planId, targetCalories, targetProtein, targetCarbs, targetFat, targetWater, dietPref, meals, System.currentTimeMillis(), targetFiber, targetSugar, targetSodium);
    }

    private static DietPlan.Meal generateMealForType(String type, int targetCals, String dietPref, String goal, String allergies, String medicalConditions, int pPct, int cPct, int fPct, int day) {
        List<com.fitness.app.data.local.PakistaniMealDatabase.PakistaniMealRecord> candidates =
                com.fitness.app.data.local.PakistaniMealDatabase.filterMeals(type, dietPref, goal, medicalConditions);

        if (candidates == null || candidates.isEmpty()) {
            candidates = com.fitness.app.data.local.PakistaniMealDatabase.getAllMeals();
        }

        int index = Math.abs((type.hashCode() + day * 31) % candidates.size());
        com.fitness.app.data.local.PakistaniMealDatabase.PakistaniMealRecord chosen = candidates.get(index);

        DietPlan.Meal meal = chosen.toDietPlanMeal();

        if (targetCals > 0 && chosen.calories > 0) {
            double ratio = (double) targetCals / chosen.calories;
            meal.setCalories(targetCals);
            meal.setProtein(Math.round(chosen.protein * ratio * 10.0) / 10.0);
            meal.setCarbs(Math.round(chosen.carbs * ratio * 10.0) / 10.0);
            meal.setFat(Math.round(chosen.fat * ratio * 10.0) / 10.0);
            meal.setFiber(Math.round(chosen.fiber * ratio * 10.0) / 10.0);
        }

        return meal;
    }
}

package com.fitness.app.utils;

import com.fitness.app.models.ProgressLog;
import com.fitness.app.models.User;
import com.fitness.app.models.WorkoutPlan;
import com.fitness.app.models.DietPlan;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RecommendationEngine {

    public static List<String> getRecommendations(User user, Map<String, ProgressLog> logs) {
        List<String> list = new ArrayList<>();

        if (user == null) {
            list.add("Maintain a balanced schedule with regular activity and hydration.");
            return list;
        }

        String goal = user.getGoal() != null ? user.getGoal() : "Improve Fitness";
        String cleanGoal = goal.toLowerCase(Locale.getDefault());
        String medical = user.getMedicalConditions() != null ? user.getMedicalConditions().toLowerCase(Locale.getDefault()) : "";
        int age = user.getAge() > 0 ? user.getAge() : 25;
        double weight = user.getWeight() > 0 ? user.getWeight() : 70.0;
        double targetWeight = user.getTargetWeight() > 0 ? user.getTargetWeight() : weight;

        // 1. Goal-Specific Recommendations
        if (cleanGoal.contains("loss") || cleanGoal.contains("fat")) {
            list.add("Focus on a sustained caloric deficit with high-protein and high-fiber foods to support fat loss while preserving muscle.");
            list.add("Incorporate 30-45 minutes of brisk walking or moderate cardio 4-5 days a week for steady energy burn.");
            if (targetWeight < weight && weight > 0) {
                double remaining = weight - targetWeight;
                list.add(String.format(Locale.getDefault(), "Target Progress: You have %.1f kg to reach your weight loss goal of %.1f kg.", remaining, targetWeight));
            }
        } else if (cleanGoal.contains("gain") || cleanGoal.contains("muscle") || cleanGoal.contains("bulk")) {
            list.add("Maintain a slight caloric surplus (~300-500 kcal) with 1.6g to 2.2g of protein per kg of body weight.");
            list.add("Focus on progressive strength overload: add 1 rep or slight resistance every week for key compound exercises.");
            if (targetWeight > weight && weight > 0) {
                double remaining = targetWeight - weight;
                list.add(String.format(Locale.getDefault(), "Target Progress: You have %.1f kg to gain to hit your target of %.1f kg.", remaining, targetWeight));
            }
        } else if (cleanGoal.contains("tone") || cleanGoal.contains("shape") || cleanGoal.contains("recomposition")) {
            list.add("Prioritize body recomposition: keep calories near maintenance while maintaining high protein for lean muscle definition.");
            list.add("Combine moderate resistance training 3-4 days a week with light active recovery days.");
        } else if (cleanGoal.contains("endurance") || cleanGoal.contains("cardio") || cleanGoal.contains("energy")) {
            list.add("Focus on aerobic threshold building and complex carbohydrate intake to sustain daily stamina.");
            list.add("Build steady workout sessions increasing total active time by 5-10% weekly.");
        } else {
            list.add("Keep a consistent routine balancing resistance training, aerobic activity, and rest days.");
            list.add("Prioritize nutrient-dense whole foods: colorful vegetables, healthy fats, and lean protein sources.");
        }

        // 2. Health Condition Guardrails
        if (medical.contains("joint") || medical.contains("arthritis")) {
            list.add("Joint Protection: Perform low-impact, joint-friendly movements (Chair Squats, Wall Sits, Step-ups). Avoid high-impact jumping.");
        }
        if (medical.contains("hypertension") || medical.contains("blood pressure")) {
            list.add("Cardiovascular Safety: Keep sodium intake below 1500mg daily and practice controlled breathing during workouts.");
        }
        if (medical.contains("diabetes")) {
            list.add("Metabolic Guidance: Choose complex, low-GI carbohydrates and limit added sugars to support stable blood glucose.");
        }
        if (medical.contains("asthma") || medical.contains("respiratory")) {
            list.add("Respiratory Safety: Warm up thoroughly and take 60-90 second rest breaks between exercises.");
        }

        // 3. Age-Specific Adaptations
        if (age >= 50) {
            list.add("Recovery Focus: Prioritize joint flexibility, core stability, and 7-8 hours of sleep for optimal recovery.");
        } else if (age < 25) {
            list.add("Performance Focus: Your recovery rate is fast—strive for progressive consistency in strength and activity targets.");
        }

        // 4. Log Analysis & Progress Checking
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

            int targetWater = user.getDailyWaterGoal() > 0 ? user.getDailyWaterGoal() : (int) (weight * 35);
            if (avgWater < targetWater) {
                list.add(String.format(Locale.getDefault(), "Hydration Note: Your average water intake is ~%.0f ml. Aim for your daily target of %d ml.", avgWater, targetWater));
            } else {
                list.add("Hydration Star: Excellent work keeping your hydration above target level!");
            }

            int targetSteps = user.getDailyStepGoal() > 0 ? user.getDailyStepGoal() : 8000;
            if (avgSteps < targetSteps) {
                list.add(String.format(Locale.getDefault(), "Activity Level: You average ~%.0f steps/day. A short 15-minute walk will help hit your target of %d steps.", avgSteps, targetSteps));
            }

            if (avgSleep > 0 && avgSleep < 420) {
                list.add("Sleep Optimization: Sleep averaged under 7 hours. Adequate rest is essential for muscle repair and fat loss.");
            }
        }

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

        int age = user.getAge() > 0 ? user.getAge() : 25;
        String goal = user.getGoal() != null ? user.getGoal() : "Improve Fitness";
        String cleanGoal = goal.toLowerCase(Locale.getDefault());
        String equip = user.getAvailableEquipment() != null ? user.getAvailableEquipment().toLowerCase(Locale.getDefault()) : "bodyweight";
        String medical = user.getMedicalConditions() != null ? user.getMedicalConditions().toLowerCase(Locale.getDefault()) : "";

        boolean hasJointIssue = medical.contains("joint") || medical.contains("arthritis");
        boolean isSenior = age >= 50;

        List<WorkoutPlan.Exercise> exercises = new ArrayList<>();

        if (cleanGoal.contains("loss") || cleanGoal.contains("fat")) {
            // WEIGHT LOSS WORKOUT PLAN
            if (hasJointIssue || isSenior) {
                exercises.add(new WorkoutPlan.Exercise("Chair Squats", "Low-impact quad & glute strengthening", 3, Math.max(8, 12 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Wall Push-ups", "Safe upper body push without joint strain", 3, Math.max(8, 12 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Seated Knee Tucks", "Core stability exercise", 3, Math.max(10, 15 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Brisk Stationary Marching", "Low-impact aerobic burn", 3, 0, Math.max(60, 90 + repsMultiplier * 10)));
            } else if (equip.contains("dumbbell")) {
                exercises.add(new WorkoutPlan.Exercise("Dumbbell Goblet Squats", "Weighted squat for quad & core calorie burn", 4, Math.max(8, 12 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Dumbbell Shoulder Press", "Overhead press for upper body strength", 3, Math.max(8, 10 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Dumbbell Renegade Rows", "Core stability & back strength", 3, Math.max(8, 10 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Dumbbell Thrusters", "Full-body compound cardio push", 3, Math.max(6, 10 + repsMultiplier), 0));
            } else if (equip.contains("resistance band") || equip.contains("band")) {
                exercises.add(new WorkoutPlan.Exercise("Banded Monster Walks", "Glute & hip strength warm-up", 3, Math.max(12, 15 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Banded Chest Press", "Controlled resistance chest builder", 4, Math.max(10, 15 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Banded Rows", "Back strength & posture corrector", 3, Math.max(12, 15 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Banded Squat Pulses", "High-rep lower body burn", 3, Math.max(12, 20 + repsMultiplier), 0));
            } else if (equip.contains("chair") || equip.contains("bottle") || equip.contains("backpack")) {
                exercises.add(new WorkoutPlan.Exercise("Backpack Weighted Squats", "Household equipment squat", 4, Math.max(8, 12 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Chair Dips", "Tricep & chest push", 3, Math.max(8, 12 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Water Bottle Bicep Curls", "Isolated arm conditioning", 3, Math.max(12, 15 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Step-ups on Chair/Stairs", "Lower body cardio burn", 3, Math.max(10, 15 + repsMultiplier), 0));
            } else {
                exercises.add(new WorkoutPlan.Exercise("Jumping Jacks", "High-intensity cardio warm-up", 3, Math.max(15, 25 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Bodyweight Squats", "Lower body strength builder", 4, Math.max(10, 15 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Mountain Climbers", "Core and stamina push", 3, Math.max(15, 30 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Plank Hold", "Static core strength hold", 3, 0, Math.max(30, 45 + repsMultiplier * 5)));
            }
        } else if (cleanGoal.contains("gain") || cleanGoal.contains("muscle") || cleanGoal.contains("bulk")) {
            // WEIGHT GAIN / MUSCLE GAIN PLAN
            if (hasJointIssue || isSenior) {
                exercises.add(new WorkoutPlan.Exercise("Glute Bridges", "Joint-safe posterior chain builder", 4, Math.max(10, 15 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Incline Bench Push-ups", "Controlled chest builder", 3, Math.max(8, 12 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Supported Single-Leg Rows", "Upper back strength", 3, Math.max(8, 12 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Bird-Dog Holds", "Core & lower back stability", 3, 0, 30));
            } else if (equip.contains("dumbbell")) {
                exercises.add(new WorkoutPlan.Exercise("Dumbbell Chest Press", "Chest & tricep hypertrophy", 4, Math.max(8, 12 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Dumbbell Romanian Deadlifts", "Hamstring & glute strength", 4, Math.max(8, 10 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Dumbbell Bicep Curls", "Isolated arm builder", 3, Math.max(10, 12 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Dumbbell Lateral Raises", "Shoulder hypertrophy", 3, Math.max(10, 12 + repsMultiplier), 0));
            } else if (equip.contains("barbell")) {
                exercises.add(new WorkoutPlan.Exercise("Barbell Deadlifts", "Compound full-body builder", 4, Math.max(4, 8 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Barbell Bench Press", "Chest mass builder", 4, Math.max(6, 10 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Barbell Bent-Over Rows", "Back thickness builder", 4, Math.max(6, 10 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Barbell Overhead Press", "Shoulder strength builder", 3, Math.max(6, 8 + repsMultiplier), 0));
            } else if (equip.contains("backpack") || equip.contains("resistance band")) {
                exercises.add(new WorkoutPlan.Exercise("Weighted Backpack Squats", "Loaded progressive squats", 4, Math.max(8, 12 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Banded Push-ups", "Resisted chest push", 4, Math.max(8, 12 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Backpack Rows", "Loaded back builder", 4, Math.max(10, 12 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Banded Bicep Curls", "Isolated arm resistance", 3, Math.max(12, 15 + repsMultiplier), 0));
            } else {
                exercises.add(new WorkoutPlan.Exercise("Push-ups", "Chest, shoulders, and triceps builder", 4, Math.max(8, 15 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Pike Push-ups", "Shoulder hypertrophy focus", 3, Math.max(6, 10 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Bulgarian Split Squats", "Single-leg hypertrophy builder", 3, Math.max(8, 12 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Pull-ups", "Back and biceps strength builder", 4, Math.max(4, 8 + repsMultiplier), 0));
            }
        } else if (cleanGoal.contains("tone") || cleanGoal.contains("shape") || cleanGoal.contains("recomposition")) {
            // TONE BODY / LEAN & TONE PLAN
            if (hasJointIssue || isSenior) {
                exercises.add(new WorkoutPlan.Exercise("Wall Sit", "Isometric leg toning", 3, 0, Math.max(30, 45 + repsMultiplier * 5)));
                exercises.add(new WorkoutPlan.Exercise("Standing Side Leg Raises", "Hip & outer thigh toning", 3, Math.max(12, 15 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Knee Push-ups", "Upper body toning", 3, Math.max(8, 12 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Cat-Cow Stretches", "Spinal mobility & core posture", 3, 0, 45));
            } else {
                exercises.add(new WorkoutPlan.Exercise("Lunges", "Lower body sculpting & balance", 3, Math.max(10, 12 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Push-up to Side Plank", "Upper body & core toning", 3, Math.max(6, 10 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Glute Bridge Pulses", "Posterior sculpting", 3, Math.max(12, 20 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Bicycle Crunches", "Abdominal definition", 3, Math.max(15, 20 + repsMultiplier), 0));
            }
        } else if (cleanGoal.contains("endurance") || cleanGoal.contains("cardio") || cleanGoal.contains("energy")) {
            // ENDURANCE & CARDIO PLAN
            exercises.add(new WorkoutPlan.Exercise("High Knees", "Cardiovascular stamina push", 4, Math.max(20, 30 + repsMultiplier), 0));
            exercises.add(new WorkoutPlan.Exercise("Jump Squats", "Explosive leg endurance", 3, Math.max(10, 15 + repsMultiplier), 0));
            exercises.add(new WorkoutPlan.Exercise("Speed Skaters", "Lateral movement & agility", 3, Math.max(15, 20 + repsMultiplier), 0));
            exercises.add(new WorkoutPlan.Exercise("Burpees", "Full-body aerobic interval", 3, Math.max(6, 10 + repsMultiplier), 0));
        } else {
            // GENERAL FITNESS & BALANCED MAINTAIN PLAN
            exercises.add(new WorkoutPlan.Exercise("Air Squats", "Lower body functional movement", 3, Math.max(10, 15 + repsMultiplier), 0));
            exercises.add(new WorkoutPlan.Exercise("Standard Push-ups", "Upper body push", 3, Math.max(8, 12 + repsMultiplier), 0));
            exercises.add(new WorkoutPlan.Exercise("Superman Hold", "Lower back & posture hold", 3, 0, Math.max(20, 30 + repsMultiplier * 3)));
            exercises.add(new WorkoutPlan.Exercise("Jumping Rope / Shadow Skipping", "Cardio coordination", 3, 0, 120));
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

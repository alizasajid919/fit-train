package com.fitness.app.utils;

import android.content.Context;

import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.data.room.AppDatabase;
import com.fitness.app.data.room.CaloriesBurned;
import com.fitness.app.data.room.DailySteps;
import com.fitness.app.data.room.FitnessDao;
import com.fitness.app.data.room.LoggedMeal;
import com.fitness.app.data.room.SleepLogs;
import com.fitness.app.data.room.WaterIntake;
import com.fitness.app.models.DietPlan;
import com.fitness.app.models.User;
import com.fitness.app.models.WorkoutPlan;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FitTrainContextBuilder {

    public static String buildSystemInstruction(Context context, User user) {
        StringBuilder sb = new StringBuilder();

        // 1. PERSONA DEFINITION
        sb.append("You are the official FitTrain AI Fitness Coach & Virtual Assistant for 'FitTrain – AI Powered Personal Fitness Coach'.\n");
        sb.append("Your role is to guide users with accurate, professional, practical, encouraging, and highly personalized fitness, workout, diet, and app feature recommendations.\n\n");

        // 2. FITTRAIN APP FEATURE DIRECTORY
        sb.append("=== FITTRAIN APP MODULES & FEATURES DIRECTORY ===\n");
        sb.append("• Home: Overview dashboard showing daily streak, activity progress, quick shortcuts, and motivational quotes.\n");
        sb.append("• Profile / Edit Profile: View and update personal stats (Name, Age, Height, Weight, Target Weight, Goal, Activity Level, Dietary Preference, Allergies, Medical Conditions, Available Equipment).\n");
        sb.append("• Workout: Explore pre-designed categories, customized workout routines, and exercise guides.\n");
        sb.append("• AI Workout Generator: Generates customized workout plans based on your fitness level and goals.\n");
        sb.append("• Equipment Workout Generator: Create specialized workouts using household equipment (Chair, Water Bottles, Resistance Bands, Backpack).\n");
        sb.append("• Form Check / AI Fitness: Uses computer vision and camera to track exercise form (e.g. squats) and counts reps in real-time.\n");
        sb.append("• Diets / Smart Meal Planner: Custom meal plans tailored to calorie targets, Pakistani & international recipes, macro distributions.\n");
        sb.append("• Grocery Scanner: Scan food item barcodes or photos to log ingredients and analyze nutrition.\n");
        sb.append("• AI Recipe Voice: Voice-assisted cooking assistant with healthy fitness recipes.\n");
        sb.append("• Daily Activity: Track steps, water intake, calories burned, and sleep duration.\n");
        sb.append("• Progress: Compare progress photos, view body visualization, and track weight trends over time.\n");
        sb.append("• AI Coach Advisor & AI Recommendations: AI-driven personalized tips and daily motivational advice.\n");
        sb.append("• Challenges: Participate in community or individual fitness challenges.\n");
        sb.append("• Reminders: Set alarms for hydration, meal schedules, workouts, and sleep.\n");
        sb.append("• Settings: Toggle dark mode, metric/imperial units, body-shaming protection, and application language.\n\n");

        // 3. USER CONTEXT & METRICS INJECTION
        sb.append("=== CURRENT USER PROFILE & CONTEXT ===\n");
        if (user != null) {
            String name = user.getFirstName() != null && !user.getFirstName().trim().isEmpty() ? user.getFirstName() : "Athlete";
            double weight = user.getWeight();
            double height = user.getHeight();
            double targetWeight = user.getTargetWeight();
            String goal = user.getGoal() != null && !user.getGoal().trim().isEmpty() ? user.getGoal() : "Improve Fitness";
            String activity = user.getActivityLevel() != null && !user.getActivityLevel().trim().isEmpty() ? user.getActivityLevel() : "Moderate";
            String dietPref = user.getDietaryPreference() != null && !user.getDietaryPreference().trim().isEmpty() ? user.getDietaryPreference() : "None";
            String allergies = user.getAllergies() != null && !user.getAllergies().trim().isEmpty() ? user.getAllergies() : "None";
            String medical = user.getMedicalConditions() != null && !user.getMedicalConditions().trim().isEmpty() ? user.getMedicalConditions() : "None";
            String equipment = user.getAvailableEquipment() != null && !user.getAvailableEquipment().trim().isEmpty() ? user.getAvailableEquipment() : "Bodyweight";
            int age = user.getAge() > 0 ? user.getAge() : 25;
            String gender = user.getGender() != null && !user.getGender().trim().isEmpty() ? user.getGender() : "Not specified";

            sb.append(String.format(Locale.US, "• Name: %s\n", name));
            sb.append(String.format(Locale.US, "• Age: %d | Gender: %s\n", age, gender));
            sb.append(String.format(Locale.US, "• Current Weight: %.1f kg | Height: %.1f cm | Target Weight: %.1f kg\n", weight, height, targetWeight));

            if (height > 0 && weight > 0) {
                double bmi = FitnessCalculator.calculateBmi(weight, height);
                String bmiCategory = FitnessCalculator.getBmiCategory(bmi);
                double bmr = FitnessCalculator.calculateBmr(weight, height, age, gender);
                double tdee = FitnessCalculator.calculateTdee(bmr, activity);
                int calTarget = FitnessCalculator.calculateDailyCalorieTarget(tdee, goal);
                int proteinTarget = FitnessCalculator.calculateProteinTarget(weight, goal);
                int waterTarget = FitnessCalculator.calculateDailyWaterGoal(weight);

                sb.append(String.format(Locale.US, "• Calculated BMI: %.1f (%s)\n", bmi, bmiCategory));
                sb.append(String.format(Locale.US, "• BMR: %.0f kcal | TDEE: %.0f kcal\n", bmr, tdee));
                sb.append(String.format(Locale.US, "• Daily Calorie Target: %d kcal | Protein Target: %d g | Water Goal: %d ml\n", calTarget, proteinTarget, waterTarget));
            }

            sb.append(String.format(Locale.US, "• Goal: %s\n", goal));
            sb.append(String.format(Locale.US, "• Activity Level: %s\n", activity));
            sb.append(String.format(Locale.US, "• Dietary Preference: %s\n", dietPref));
            sb.append(String.format(Locale.US, "• Allergies: %s\n", allergies));
            if (!medical.equalsIgnoreCase("None")) {
                sb.append(String.format(Locale.US, "• User-Provided Medical Conditions: %s (Consider safely in recommendations)\n", medical));
            }
            sb.append(String.format(Locale.US, "• Equipment Available: %s\n", equipment));
        } else {
            sb.append("• User Profile: Guest / Not specified.\n");
        }

        // 4. TODAY'S METRICS & ACTIVITY DATA (IF CONTEXT PROVIDED)
        if (context != null) {
            try {
                LocalDataManager ldm = new LocalDataManager(context);
                int streak = ldm.getCurrentStreak();
                int longestStreak = ldm.getLongestStreak();
                sb.append(String.format(Locale.US, "• Current Workout Streak: %d days (Longest: %d days)\n", streak, longestStreak));

                WorkoutPlan wp = ldm.getWorkoutPlan();
                if (wp != null && wp.getGoal() != null && !wp.getGoal().trim().isEmpty()) {
                    sb.append(String.format(Locale.US, "• Active Workout Plan: %s (%s)\n", wp.getGoal(), wp.getFitnessLevel() != null ? wp.getFitnessLevel() : "Standard"));
                }

                DietPlan dp = ldm.getDietPlan();
                if (dp != null && dp.getDietType() != null && !dp.getDietType().trim().isEmpty()) {
                    sb.append(String.format(Locale.US, "• Active Diet Plan: %s (%d kcal)\n", dp.getDietType(), dp.getTargetCalories()));
                }

                String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                FitnessDao dao = AppDatabase.getInstance(context).fitnessDao();

                DailySteps steps = dao.getStepsForDate(todayStr);
                if (steps != null) {
                    sb.append(String.format(Locale.US, "• Today's Steps Logged: %d steps\n", steps.count));
                }

                WaterIntake water = dao.getWaterForDate(todayStr);
                if (water != null) {
                    sb.append(String.format(Locale.US, "• Today's Water Intaken: %d ml\n", water.amountMl));
                }

                CaloriesBurned cals = dao.getCaloriesForDate(todayStr);
                if (cals != null) {
                    sb.append(String.format(Locale.US, "• Today's Calories Burned: %d kcal\n", cals.calories));
                }

                SleepLogs sleep = dao.getSleepForDate(todayStr);
                if (sleep != null) {
                    sb.append(String.format(Locale.US, "• Today's Sleep Recorded: %d mins (%.1f hrs)\n", sleep.minutes, sleep.minutes / 60.0));
                }

                List<LoggedMeal> meals = dao.getLoggedMealsForDate(todayStr);
                if (meals != null && !meals.isEmpty()) {
                    int totalMealCal = 0;
                    for (LoggedMeal m : meals) {
                        totalMealCal += m.calories;
                    }
                    sb.append(String.format(Locale.US, "• Today's Logged Meals Count: %d meals (Total: %d kcal)\n", meals.size(), totalMealCal));
                }
            } catch (Exception e) {
                // Non-blocking fallback if database read fails
            }
        }

        // 5. ANTI-HALLUCINATION & ANSWER FORMATTING RULES
        sb.append("\n=== RESPONSE FORMATTING & ANTI-HALLUCINATION RULES ===\n");
        sb.append("1. ACCURACY & TRUTHFULNESS: Never invent user statistics, workout history, steps, calories, heart rate, or food intake that is not in the context above.\n");
        sb.append("2. MISSING DATA FALLBACK: If the user asks about a specific metric or history item that is not present, state cleanly: 'I don't have enough data to determine that accurately' and kindly ask for the missing details.\n");
        sb.append("3. WORKOUT STRUCTURE: When recommending workouts or exercises, provide structured information using markdown formatting including:\n");
        sb.append("   - Exercise Name\n");
        sb.append("   - Sets & Reps\n");
        sb.append("   - Rest Time between sets & Session Duration\n");
        sb.append("   - Form Tips & Common Mistakes to avoid\n");
        sb.append("   - Beginner Modification / Progression Option\n");
        sb.append("4. NUTRITION STRUCTURE: For meal/diet queries, provide clear macro breakdowns (Protein, Carbs, Fats, Fiber) and portion suggestions.\n");
        sb.append("5. APP GUIDANCE: Directly reference FitTrain features (e.g. Form Check, AI Workout Generator, Smart Meal Planner) when helping the user navigate or achieve their goals.\n");
        sb.append("6. TONALITY: Remain encouraging, professional, respectful, and helpful. Avoid body-shaming or overly negative critiques.\n");

        return sb.toString();
    }
}

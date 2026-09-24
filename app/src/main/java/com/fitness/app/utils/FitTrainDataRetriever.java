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
import com.fitness.app.models.WorkoutLog;
import com.fitness.app.models.WorkoutPlan;
import com.fitness.app.models.WorkoutSession;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FitTrainDataRetriever {

    public static String getUserProfile(Context context, User user) {
        if (user == null && context != null) {
            user = new LocalDataManager(context).getUser();
        }
        if (user == null) {
            return "User Profile: Not logged in / Guest user.";
        }

        StringBuilder sb = new StringBuilder();
        String name = user.getFirstName() != null && !user.getFirstName().trim().isEmpty() ? user.getFirstName() : "Athlete";
        sb.append(String.format(Locale.US, "Name: %s | Age: %d | Gender: %s\n", name, user.getAge() > 0 ? user.getAge() : 25, user.getGender() != null ? user.getGender() : "Not specified"));
        sb.append(String.format(Locale.US, "Weight: %.1f kg | Height: %.1f cm | Target Weight: %.1f kg\n", user.getWeight(), user.getHeight(), user.getTargetWeight()));
        
        double bmi = FitnessCalculator.calculateBmi(user.getWeight(), user.getHeight());
        String bmiCategory = FitnessCalculator.getBmiCategory(bmi);
        sb.append(String.format(Locale.US, "BMI: %.1f (%s)\n", bmi, bmiCategory));
        sb.append(String.format(Locale.US, "Goal: %s | Activity Level: %s\n", user.getGoal(), user.getActivityLevel()));
        sb.append(String.format(Locale.US, "Diet Preference: %s | Allergies: %s\n", user.getDietaryPreference(), user.getAllergies()));
        if (user.getMedicalConditions() != null && !user.getMedicalConditions().trim().equalsIgnoreCase("none")) {
            sb.append(String.format(Locale.US, "Medical Conditions: %s\n", user.getMedicalConditions()));
        }
        sb.append(String.format(Locale.US, "Available Equipment: %s", user.getAvailableEquipment()));

        return sb.toString();
    }

    public static String getTodayWorkout(Context context, User user) {
        if (context == null) return "Context unavailable to query today's workout.";

        LocalDataManager ldm = new LocalDataManager(context);
        String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Check if a workout session was logged today
        List<WorkoutSession> sessions = ldm.getAllWorkoutSessions();
        for (WorkoutSession s : sessions) {
            if (todayStr.equals(s.getDateStr())) {
                return String.format(Locale.US, "Logged Session Today: '%s' (%s, %d mins, %d kcal burned, %d%% completed)",
                        s.getWorkoutTitle(), s.getDifficulty(), s.getActiveDurationMs() / 60000, s.getEstimatedCalories(), (int) s.getCompletionPercentage());
            }
        }

        // Check active assigned workout plan
        WorkoutPlan plan = ldm.getWorkoutPlan();
        if (plan != null && plan.getExercises() != null && !plan.getExercises().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Assigned Daily Workout Plan (%s Level):\n", plan.getFitnessLevel() != null ? plan.getFitnessLevel() : "Standard"));
            for (WorkoutPlan.Exercise ex : plan.getExercises()) {
                sb.append(String.format(Locale.US, "• %s: %d sets x %d reps (%s)\n",
                        ex.getName(), ex.getSets(), ex.getReps(), ex.getDescription() != null ? ex.getDescription() : "Perform with good form"));
            }
            return sb.toString().trim();
        }

        return "No specific workout has been logged or assigned for today yet. You can generate a custom workout routine in the AI Workout Generator tab!";
    }

    public static String getWorkoutHistory(Context context) {
        if (context == null) return "No workout history available.";

        LocalDataManager ldm = new LocalDataManager(context);
        List<WorkoutSession> sessions = ldm.getAllWorkoutSessions();
        if (!sessions.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Recent Workout Sessions:\n");
            int limit = Math.min(5, sessions.size());
            for (int i = 0; i < limit; i++) {
                WorkoutSession s = sessions.get(sessions.size() - 1 - i);
                sb.append(String.format(Locale.US, "• [%s] %s - %d mins, %d kcal\n",
                        s.getDateStr(), s.getWorkoutTitle(), s.getActiveDurationMs() / 60000, s.getEstimatedCalories()));
            }
            return sb.toString().trim();
        }

        List<WorkoutLog> logs = ldm.getAllWorkoutLogs();
        if (!logs.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Recent Exercise Logs:\n");
            int limit = Math.min(5, logs.size());
            for (int i = 0; i < limit; i++) {
                WorkoutLog log = logs.get(logs.size() - 1 - i);
                sb.append(String.format(Locale.US, "• [%s] %s: %d sets x %d reps (%.1f kg)\n",
                        log.getDate(), log.getExerciseName(), log.getSets(), log.getReps(), log.getWeight()));
            }
            return sb.toString().trim();
        }

        return "No past workout sessions or logs recorded yet.";
    }

    public static String getTodayDiet(Context context) {
        if (context == null) return "Diet information unavailable.";

        String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        FitnessDao dao = AppDatabase.getInstance(context).fitnessDao();
        List<LoggedMeal> loggedMeals = dao.getLoggedMealsForDate(todayStr);

        if (loggedMeals != null && !loggedMeals.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Logged Meals Today:\n");
            int totalCal = 0;
            int totalProtein = 0;
            int totalCarbs = 0;
            int totalFat = 0;

            for (LoggedMeal m : loggedMeals) {
                sb.append(String.format(Locale.US, "• [%s] %s - %d kcal (P: %.1fg, C: %.1fg, F: %.1fg)\n",
                        m.mealType != null ? m.mealType : "Meal", m.name, m.calories, m.protein, m.carbs, m.fat));
                totalCal += m.calories;
                totalProtein += (int) m.protein;
                totalCarbs += (int) m.carbs;
                totalFat += (int) m.fat;
            }
            sb.append(String.format(Locale.US, "Today's Totals: %d kcal | Protein: %dg | Carbs: %dg | Fat: %dg",
                    totalCal, totalProtein, totalCarbs, totalFat));
            return sb.toString();
        }

        LocalDataManager ldm = new LocalDataManager(context);
        DietPlan plan = ldm.getDietPlan();
        if (plan != null && plan.getMeals() != null && !plan.getMeals().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format(Locale.US, "Assigned Daily Diet Plan (%s - Target: %d kcal):\n",
                    plan.getDietType() != null ? plan.getDietType() : "Standard", plan.getTargetCalories()));
            for (DietPlan.Meal m : plan.getMeals()) {
                sb.append(String.format(Locale.US, "• %s (%s): %s - %d kcal\n",
                        m.getName(), m.getType(), m.getDescription(), m.getCalories()));
            }
            return sb.toString().trim();
        }

        return "You haven't logged any meals for today yet, and no diet plan is active. You can log meals or generate a diet plan in the Diets tab!";
    }

    public static String getDailyActivity(Context context) {
        if (context == null) return "Daily activity data unavailable.";

        String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        FitnessDao dao = AppDatabase.getInstance(context).fitnessDao();

        DailySteps steps = dao.getStepsForDate(todayStr);
        WaterIntake water = dao.getWaterForDate(todayStr);
        CaloriesBurned cals = dao.getCaloriesForDate(todayStr);
        SleepLogs sleep = dao.getSleepForDate(todayStr);

        StringBuilder sb = new StringBuilder();
        sb.append("Today's Recorded Activity Metrics:\n");
        sb.append(String.format(Locale.US, "• Steps: %s\n", steps != null ? steps.count + " steps" : "0 steps logged"));
        sb.append(String.format(Locale.US, "• Water Intake: %s\n", water != null ? water.amountMl + " ml" : "0 ml logged"));
        sb.append(String.format(Locale.US, "• Burnt Calories: %s\n", cals != null ? cals.calories + " kcal" : "0 kcal logged"));
        sb.append(String.format(Locale.US, "• Sleep Duration: %s", sleep != null ? sleep.minutes + " mins (" + String.format(Locale.US, "%.1f", sleep.minutes / 60.0) + " hrs)" : "0 mins logged"));

        return sb.toString();
    }

    public static String getStreakAndProgress(Context context, User user) {
        if (context == null) return "Streak data unavailable.";

        LocalDataManager ldm = new LocalDataManager(context);
        int currentStreak = ldm.getCurrentStreak();
        int longestStreak = ldm.getLongestStreak();

        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.US, "Current Workout Streak: %d Days 🔥 | Longest Streak: %d Days\n", currentStreak, longestStreak));

        if (user != null) {
            double currentWeight = user.getWeight();
            double targetWeight = user.getTargetWeight();
            if (targetWeight > 0) {
                double diff = Math.abs(currentWeight - targetWeight);
                if (currentWeight > targetWeight) {
                    sb.append(String.format(Locale.US, "Weight Delta: %.1f kg to lose to reach target of %.1f kg.", diff, targetWeight));
                } else if (currentWeight < targetWeight) {
                    sb.append(String.format(Locale.US, "Weight Delta: %.1f kg to gain to reach target of %.1f kg.", diff, targetWeight));
                } else {
                    sb.append("Target Weight: Reached exact target weight!");
                }
            }
        }

        return sb.toString();
    }
}

package com.fitness.app.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import com.fitness.app.models.ChatMessage;
import com.fitness.app.models.DietPlan;
import com.fitness.app.models.ProgressLog;
import com.fitness.app.models.Reminder;
import com.fitness.app.models.User;
import com.fitness.app.models.WorkoutPlan;
import com.fitness.app.models.WorkoutLog;
import com.fitness.app.models.WorkoutSchedule;
import com.fitness.app.models.WorkoutSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class LocalDataManager {
    private static final String PREF_NAME = "fittrain_local_prefs";
    private static final String KEY_USER = "key_user";
    private static final String KEY_WORKOUT_PLAN = "key_workout_plan";
    private static final String KEY_DIET_PLAN = "key_diet_plan";
    private static final String KEY_PROGRESS_LOGS = "key_progress_logs";
    private static final String KEY_CHAT_MESSAGES = "key_chat_messages";
    private static final String KEY_REMINDERS = "key_reminders";
    private static final String KEY_DARK_THEME = "key_dark_theme";
    private static final String KEY_UNITS_METRIC = "key_units_metric";
    private static final String KEY_ONBOARDING_SEEN = "key_onboarding_seen";
    private static final String KEY_WORKOUT_LOGS = "key_workout_logs";
    private static final String KEY_BODY_SHAMING_PROTECTION = "key_body_shaming_protection";
    private static final String KEY_WORKOUT_SCHEDULE = "key_workout_schedule";
    private static final String KEY_WORKOUT_SESSIONS = "key_workout_sessions";

    public final SharedPreferences sharedPreferences;
    public final SharedPreferences.Editor editor;
    private Context context;

    public LocalDataManager(Context context) {
        this.context = context.getApplicationContext();
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public Context getContext() {
        return context;
    }

    // Onboarding Status
    public void setOnboardingSeen(boolean seen) {
        editor.putBoolean(KEY_ONBOARDING_SEEN, seen).apply();
    }

    public boolean isOnboardingSeen() {
        return sharedPreferences.getBoolean(KEY_ONBOARDING_SEEN, false);
    }

    // User Profile
    public void saveUser(User user) {
        if (user == null) {
            editor.remove(KEY_USER).apply();
            return;
        }
        try {
            User existingUser = getUser();
            if (existingUser != null) {
                String oldGoal = existingUser.getGoal() != null ? existingUser.getGoal() : "";
                String newGoal = user.getGoal() != null ? user.getGoal() : "";
                double oldWeight = existingUser.getWeight();
                double newWeight = user.getWeight();
                int oldCal = existingUser.getDailyCaloriesGoal();
                int newCal = user.getDailyCaloriesGoal();
                String oldDiet = existingUser.getDietaryPreference() != null ? existingUser.getDietaryPreference() : "";
                String newDiet = user.getDietaryPreference() != null ? user.getDietaryPreference() : "";
                String oldAllergies = existingUser.getAllergies() != null ? existingUser.getAllergies() : "";
                String newAllergies = user.getAllergies() != null ? user.getAllergies() : "";
                String oldMedical = existingUser.getMedicalConditions() != null ? existingUser.getMedicalConditions() : "";
                String newMedical = user.getMedicalConditions() != null ? user.getMedicalConditions() : "";
                String oldActivity = existingUser.getActivityLevel() != null ? existingUser.getActivityLevel() : "";
                String newActivity = user.getActivityLevel() != null ? user.getActivityLevel() : "";
                
                if (!oldGoal.equalsIgnoreCase(newGoal) ||
                    Double.compare(oldWeight, newWeight) != 0 ||
                    oldCal != newCal ||
                    !oldDiet.equalsIgnoreCase(newDiet) ||
                    !oldAllergies.equalsIgnoreCase(newAllergies) ||
                    !oldMedical.equalsIgnoreCase(newMedical) ||
                    !oldActivity.equalsIgnoreCase(newActivity)) {
                    
                    editor.remove(KEY_DIET_PLAN);
                    editor.remove(KEY_WORKOUT_PLAN);
                    editor.remove("last_plan_gen_date");
                    editor.putBoolean("plan_needs_regeneration", true);
                }
            } else {
                editor.putBoolean("plan_needs_regeneration", true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            JSONObject obj = new JSONObject();
            obj.put("uid", user.getUid());
            obj.put("firstName", user.getFirstName());
            obj.put("lastName", user.getLastName());
            obj.put("email", user.getEmail());
            obj.put("gender", user.getGender());
            obj.put("dob", user.getDob());
            obj.put("height", user.getHeight());
            obj.put("weight", user.getWeight());
            obj.put("goal", user.getGoal());
            obj.put("activityLevel", user.getActivityLevel());
            obj.put("profileCompleted", user.isProfileCompleted());
            obj.put("profileImageUrl", user.getProfileImageUrl());
            obj.put("createdAt", user.getCreatedAt());
            obj.put("updatedAt", user.getUpdatedAt());

            obj.put("mobileNumber", user.getMobileNumber());
            obj.put("age", user.getAge());
            obj.put("bloodGroup", user.getBloodGroup());
            obj.put("country", user.getCountry());
            obj.put("city", user.getCity());
            obj.put("fitnessExperience", user.getFitnessExperience());
            obj.put("targetWeight", user.getTargetWeight());
            obj.put("dailyWaterGoal", user.getDailyWaterGoal());
            obj.put("dailyCaloriesGoal", user.getDailyCaloriesGoal());
            obj.put("dailyStepGoal", user.getDailyStepGoal());
            obj.put("dailySleepGoal", user.getDailySleepGoal());
            obj.put("medicalConditions", user.getMedicalConditions());
            obj.put("injuries", user.getInjuries());
            obj.put("allergies", user.getAllergies());
            obj.put("dietaryPreference", user.getDietaryPreference());
            obj.put("emergencyContact", user.getEmergencyContact());
            obj.put("availableEquipment", user.getAvailableEquipment());
            obj.put("workoutLocation", user.getWorkoutLocation());
            obj.put("workoutDuration", user.getWorkoutDuration());
            obj.put("workoutDaysPerWeek", user.getWorkoutDaysPerWeek());
            obj.put("dislikedFoods", user.getDislikedFoods());
            obj.put("mealsPerDay", user.getMealsPerDay());
            obj.put("targetPace", user.getTargetPace());
            obj.put("preferredWorkoutTime", user.getPreferredWorkoutTime());

            editor.putString(KEY_USER, obj.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public User getUser() {
        String json = sharedPreferences.getString(KEY_USER, null);
        if (json == null) return null;
        try {
            JSONObject obj = new JSONObject(json);
            User user = new User(
                obj.optString("uid", ""),
                obj.optString("firstName", ""),
                obj.optString("lastName", ""),
                obj.optString("email", ""),
                obj.optLong("createdAt", 0)
            );
            user.setGender(obj.optString("gender", ""));
            user.setDob(obj.optString("dob", ""));
            user.setHeight(obj.optDouble("height", 0.0));
            user.setWeight(obj.optDouble("weight", 0.0));
            user.setGoal(obj.optString("goal", ""));
            user.setActivityLevel(obj.optString("activityLevel", ""));
            user.setProfileCompleted(obj.optBoolean("profileCompleted", false));
            user.setProfileImageUrl(obj.optString("profileImageUrl", ""));
            user.setUpdatedAt(obj.optLong("updatedAt", 0));

            user.setMobileNumber(obj.optString("mobileNumber", ""));
            user.setAge(obj.optInt("age", 0));
            user.setBloodGroup(obj.optString("bloodGroup", ""));
            user.setCountry(obj.optString("country", ""));
            user.setCity(obj.optString("city", ""));
            user.setFitnessExperience(obj.optString("fitnessExperience", ""));
            user.setTargetWeight(obj.optDouble("targetWeight", 0.0));
            user.setDailyWaterGoal(obj.optInt("dailyWaterGoal", 0));
            user.setDailyCaloriesGoal(obj.optInt("dailyCaloriesGoal", 0));
            user.setDailyStepGoal(obj.optInt("dailyStepGoal", 0));
            user.setDailySleepGoal(obj.optInt("dailySleepGoal", 0));
            user.setMedicalConditions(obj.optString("medicalConditions", ""));
            user.setInjuries(obj.optString("injuries", ""));
            user.setAllergies(obj.optString("allergies", ""));
            user.setDietaryPreference(obj.optString("dietaryPreference", ""));
            user.setEmergencyContact(obj.optString("emergencyContact", ""));
            user.setAvailableEquipment(obj.optString("availableEquipment", ""));

            user.setWorkoutLocation(obj.optString("workoutLocation", "Home"));
            user.setWorkoutDuration(obj.optInt("workoutDuration", 30));
            user.setWorkoutDaysPerWeek(obj.optInt("workoutDaysPerWeek", 4));
            user.setDislikedFoods(obj.optString("dislikedFoods", ""));
            user.setMealsPerDay(obj.optInt("mealsPerDay", 3));
            user.setTargetPace(obj.optString("targetPace", "Balanced"));
            user.setPreferredWorkoutTime(obj.optString("preferredWorkoutTime", "Morning"));

            return user;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Workout Plan
    public void saveWorkoutPlan(WorkoutPlan plan) {
        if (plan == null) {
            editor.remove(KEY_WORKOUT_PLAN).apply();
            return;
        }
        try {
            editor.putString(KEY_WORKOUT_PLAN, plan.toJsonObject().toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public WorkoutPlan getWorkoutPlan() {
        String json = sharedPreferences.getString(KEY_WORKOUT_PLAN, null);
        if (json == null) return null;
        try {
            return WorkoutPlan.fromJsonObject(new JSONObject(json));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Diet Plan
    public void saveDietPlan(DietPlan plan) {
        if (plan == null) {
            editor.remove(KEY_DIET_PLAN).apply();
            return;
        }
        try {
            editor.putString(KEY_DIET_PLAN, plan.toJsonObject().toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public DietPlan getDietPlan() {
        String json = sharedPreferences.getString(KEY_DIET_PLAN, null);
        if (json == null) return null;
        try {
            return DietPlan.fromJsonObject(new JSONObject(json));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Progress Logs (Mapped by Date)
    public void saveProgressLog(ProgressLog log) {
        if (log == null || log.getDate() == null) return;
        Map<String, ProgressLog> allLogs = getAllProgressLogs();
        allLogs.put(log.getDate(), log);
        saveAllProgressLogs(allLogs);
    }

    public ProgressLog getProgressLog(String date) {
        Map<String, ProgressLog> allLogs = getAllProgressLogs();
        ProgressLog log = allLogs.get(date);
        if (log == null) {
            log = new ProgressLog(date);
            // set defaults based on current user height/weight/BMI
            User user = getUser();
            if (user != null) {
                log.setCurrentHeight(user.getHeight());
                log.setCurrentWeight(user.getWeight());
                if (user.getHeight() > 0) {
                    double bmi = user.getWeight() / ((user.getHeight() / 100.0) * (user.getHeight() / 100.0));
                    log.setCurrentBmi(bmi);
                }
            }
        }
        return log;
    }

    public Map<String, ProgressLog> getAllProgressLogs() {
        Map<String, ProgressLog> result = new HashMap<>();
        String json = sharedPreferences.getString(KEY_PROGRESS_LOGS, null);
        if (json == null) return result;
        try {
            JSONObject mainObj = new JSONObject(json);
            Iterator<String> keys = mainObj.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject logObj = mainObj.getJSONObject(key);
                result.put(key, ProgressLog.fromJsonObject(logObj));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void saveAllProgressLogs(Map<String, ProgressLog> logs) {
        JSONObject mainObj = new JSONObject();
        try {
            for (Map.Entry<String, ProgressLog> entry : logs.entrySet()) {
                mainObj.put(entry.getKey(), entry.getValue().toJsonObject());
            }
            editor.putString(KEY_PROGRESS_LOGS, mainObj.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Chat Messages
    public void addChatMessage(ChatMessage msg) {
        if (msg == null) return;
        List<ChatMessage> list = getChatMessages();
        list.add(msg);
        saveChatMessages(list);
    }

    public List<ChatMessage> getChatMessages() {
        List<ChatMessage> list = new ArrayList<>();
        String json = sharedPreferences.getString(KEY_CHAT_MESSAGES, null);
        if (json == null) return list;
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                list.add(ChatMessage.fromJsonObject(arr.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void saveChatMessages(List<ChatMessage> list) {
        JSONArray arr = new JSONArray();
        try {
            for (ChatMessage msg : list) {
                arr.put(msg.toJsonObject());
            }
            editor.putString(KEY_CHAT_MESSAGES, arr.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Reminders
    public void saveReminders(List<Reminder> reminders) {
        JSONArray arr = new JSONArray();
        try {
            for (Reminder r : reminders) {
                arr.put(r.toJsonObject());
            }
            editor.putString(KEY_REMINDERS, arr.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public List<Reminder> getReminders() {
        List<Reminder> list = new ArrayList<>();
        String json = sharedPreferences.getString(KEY_REMINDERS, null);
        if (json == null) {
            // Load default reminders
            list.add(new Reminder("1", "WATER", 9, 0, true, "Time to drink some water and stay hydrated!"));
            list.add(new Reminder("2", "WATER", 14, 0, true, "Hydrate yourself! Drink a glass of water."));
            list.add(new Reminder("3", "MEAL", 8, 30, true, "Breakfast time! Fuel your body."));
            list.add(new Reminder("4", "MEAL", 13, 0, true, "Lunch time! Fuel your day."));
            list.add(new Reminder("5", "WORKOUT", 18, 0, true, "Time for your daily workout! Let's get fit."));
            list.add(new Reminder("6", "SLEEP", 22, 30, true, "Wind down. Time to prepare for sleep."));
            list.add(new Reminder("7", "MEAL", 19, 30, true, "Dinner time! Savor a healthy end to your day."));
            list.add(new Reminder("8", "MEAL", 16, 0, true, "Snack time! Keep your metabolism active."));
            saveReminders(list);
            return list;
        }
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                list.add(Reminder.fromJsonObject(arr.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Dark Theme Toggle
    public void setDarkThemeEnabled(boolean enabled) {
        editor.putBoolean(KEY_DARK_THEME, enabled).apply();
    }

    public boolean isDarkThemeEnabled() {
        return sharedPreferences.getBoolean(KEY_DARK_THEME, false);
    }

    // Metric Units (Weight in kg, Height in cm) vs Imperial (lbs, feet/inches)
    public void setMetricUnitsEnabled(boolean enabled) {
        editor.putBoolean(KEY_UNITS_METRIC, enabled).apply();
    }

    public boolean isMetricUnitsEnabled() {
        return sharedPreferences.getBoolean(KEY_UNITS_METRIC, true);
    }

    // No Body-Shaming & Support Mode
    public void setBodyShamingProtectionEnabled(boolean enabled) {
        editor.putBoolean(KEY_BODY_SHAMING_PROTECTION, enabled).apply();
    }

    public boolean isBodyShamingProtectionEnabled() {
        return sharedPreferences.getBoolean(KEY_BODY_SHAMING_PROTECTION, false);
    }

    // Workout Logs
    public void saveWorkoutLog(WorkoutLog log) {
        if (log == null || log.getId() == null) return;
        List<WorkoutLog> logs = getAllWorkoutLogs();
        boolean updated = false;
        for (int i = 0; i < logs.size(); i++) {
            if (logs.get(i).getId().equals(log.getId())) {
                logs.set(i, log);
                updated = true;
                break;
            }
        }
        if (!updated) {
            logs.add(log);
        }
        saveAllWorkoutLogs(logs);
    }

    public List<WorkoutLog> getAllWorkoutLogs() {
        List<WorkoutLog> list = new ArrayList<>();
        String json = sharedPreferences.getString(KEY_WORKOUT_LOGS, null);
        if (json == null) return list;
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                list.add(WorkoutLog.fromJsonObject(arr.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void saveAllWorkoutLogs(List<WorkoutLog> logs) {
        JSONArray arr = new JSONArray();
        try {
            for (WorkoutLog log : logs) {
                arr.put(log.toJsonObject());
            }
            editor.putString(KEY_WORKOUT_LOGS, arr.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public List<WorkoutLog> getWorkoutLogsForExercise(String exerciseName) {
        List<WorkoutLog> filtered = new ArrayList<>();
        if (exerciseName == null || exerciseName.trim().isEmpty()) return filtered;
        String cleanName = exerciseName.trim().toLowerCase();
        List<WorkoutLog> allLogs = getAllWorkoutLogs();
        for (WorkoutLog log : allLogs) {
            if (log.getExerciseName() != null && log.getExerciseName().trim().toLowerCase().equals(cleanName)) {
                filtered.add(log);
            }
        }
        // Sort by timestamp descending
        filtered.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        return filtered;
    }

    // Unified Workout Session Persistence (Single Source of Truth)
    public void saveWorkoutSession(WorkoutSession session) {
        if (session == null || session.getSessionId() == null) return;
        List<WorkoutSession> sessions = getAllWorkoutSessions();
        boolean updated = false;
        for (int i = 0; i < sessions.size(); i++) {
            if (sessions.get(i).getSessionId().equals(session.getSessionId())) {
                sessions.set(i, session);
                updated = true;
                break;
            }
        }
        if (!updated) {
            sessions.add(session);
        }
        saveAllWorkoutSessions(sessions);
    }

    public List<WorkoutSession> getAllWorkoutSessions() {
        List<WorkoutSession> list = new ArrayList<>();
        String json = sharedPreferences.getString(KEY_WORKOUT_SESSIONS, null);
        if (json == null) return list;
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                WorkoutSession s = WorkoutSession.fromJsonObject(arr.getJSONObject(i));
                if (s != null) list.add(s);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public WorkoutSession getWorkoutSession(String sessionId) {
        if (sessionId == null) return null;
        List<WorkoutSession> all = getAllWorkoutSessions();
        for (WorkoutSession s : all) {
            if (sessionId.equals(s.getSessionId())) {
                return s;
            }
        }
        return null;
    }

    public WorkoutSession getPreviousWorkoutSession(String title, String currentSessionId) {
        if (title == null) return null;
        String cleanTitle = title.trim().toLowerCase();
        List<WorkoutSession> all = getAllWorkoutSessions();
        all.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        for (WorkoutSession s : all) {
            if (!s.getSessionId().equals(currentSessionId) && s.getWorkoutTitle() != null && s.getWorkoutTitle().trim().toLowerCase().contains(cleanTitle)) {
                return s;
            }
        }
        return null;
    }

    public void saveAllWorkoutSessions(List<WorkoutSession> sessions) {
        JSONArray arr = new JSONArray();
        try {
            for (WorkoutSession s : sessions) {
                arr.put(s.toJsonObject());
            }
            editor.putString(KEY_WORKOUT_SESSIONS, arr.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Workout Schedules
    public void saveWorkoutSchedule(WorkoutSchedule schedule) {
        if (schedule == null || schedule.getId() == null) return;
        List<WorkoutSchedule> list = getAllWorkoutSchedules();
        boolean updated = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(schedule.getId())) {
                list.set(i, schedule);
                updated = true;
                break;
            }
        }
        if (!updated) {
            list.add(schedule);
        }
        saveAllWorkoutSchedules(list);
    }

    public List<WorkoutSchedule> getAllWorkoutSchedules() {
        List<WorkoutSchedule> list = new ArrayList<>();
        String json = sharedPreferences.getString(KEY_WORKOUT_SCHEDULE, null);
        if (json == null) return list;
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                list.add(WorkoutSchedule.fromJsonObject(arr.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void saveAllWorkoutSchedules(List<WorkoutSchedule> list) {
        JSONArray arr = new JSONArray();
        for (WorkoutSchedule s : list) {
            arr.put(s.toJsonObject());
        }
        editor.putString(KEY_WORKOUT_SCHEDULE, arr.toString()).apply();
    }

    // Streak Management Helpers
    private static final String KEY_CURRENT_STREAK = "user_workout_streak";
    private static final String KEY_LONGEST_STREAK = "user_longest_streak";
    private static final String KEY_LAST_ACTIVITY_DATE = "last_activity_date";

    public int getCurrentStreak() {
        checkAndResetStreak();
        return sharedPreferences.getInt(KEY_CURRENT_STREAK, 0);
    }

    public int getLongestStreak() {
        return sharedPreferences.getInt(KEY_LONGEST_STREAK, 0);
    }

    public void incrementStreak() {
        String todayStr = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
        String lastDate = sharedPreferences.getString(KEY_LAST_ACTIVITY_DATE, "");

        if (todayStr.equals(lastDate)) {
            return;
        }

        int current = sharedPreferences.getInt(KEY_CURRENT_STREAK, 0);
        int longest = sharedPreferences.getInt(KEY_LONGEST_STREAK, 0);

        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_YEAR, -1);
        String yesterdayStr = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(cal.getTime());

        if (yesterdayStr.equals(lastDate)) {
            current++;
        } else {
            current = 1;
        }

        if (current > longest) {
            longest = current;
        }

        editor.putInt(KEY_CURRENT_STREAK, current);
        editor.putInt(KEY_LONGEST_STREAK, longest);
        editor.putString(KEY_LAST_ACTIVITY_DATE, todayStr);
        editor.apply();
    }

    public void checkAndResetStreak() {
        String lastDate = sharedPreferences.getString(KEY_LAST_ACTIVITY_DATE, "");
        if (lastDate.isEmpty()) {
            return;
        }

        String todayStr = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
        if (todayStr.equals(lastDate)) {
            return;
        }

        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_YEAR, -1);
        String yesterdayStr = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(cal.getTime());

        if (yesterdayStr.equals(lastDate)) {
            return;
        }

        editor.putInt(KEY_CURRENT_STREAK, 0);
        editor.apply();
    }

    // Clear All cache (when logging out or deletion)
    public void clearAll() {
        editor.remove(KEY_USER);
        editor.remove(KEY_WORKOUT_PLAN);
        editor.remove(KEY_DIET_PLAN);
        editor.remove(KEY_PROGRESS_LOGS);
        editor.remove(KEY_CHAT_MESSAGES);
        editor.remove(KEY_WORKOUT_LOGS);
        editor.remove(KEY_WORKOUT_SCHEDULE);
        editor.remove(KEY_CURRENT_STREAK);
        editor.remove(KEY_LONGEST_STREAK);
        editor.remove(KEY_LAST_ACTIVITY_DATE);
        editor.apply();
    }
}

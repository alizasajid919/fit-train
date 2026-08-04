package com.fitness.app.data.room;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FitnessRepository {

    private final FitnessDao dao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public FitnessRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        dao = db.fitnessDao();
    }

    // Steps
    public void saveSteps(String date, int count) {
        executor.execute(() -> {
            DailySteps record = dao.getStepsForDate(date);
            if (record == null) {
                record = new DailySteps();
                record.date = date;
            }
            record.count = count;
            dao.insertSteps(record);
        });
    }

    public LiveData<DailySteps> getStepsLive(String date) {
        return dao.getStepsLiveForDate(date);
    }

    // Calories
    public void saveCalories(String date, int calories) {
        executor.execute(() -> {
            CaloriesBurned record = dao.getCaloriesForDate(date);
            if (record == null) {
                record = new CaloriesBurned();
                record.date = date;
            }
            record.calories = calories;
            dao.insertCalories(record);
        });
    }

    public LiveData<CaloriesBurned> getCaloriesLive(String date) {
        return dao.getCaloriesLiveForDate(date);
    }

    // Water
    public void saveWater(String date, int amountMl) {
        executor.execute(() -> {
            WaterIntake record = dao.getWaterForDate(date);
            if (record == null) {
                record = new WaterIntake();
                record.date = date;
            }
            record.amountMl = amountMl;
            dao.insertWater(record);
        });
    }

    public LiveData<WaterIntake> getWaterLive(String date) {
        return dao.getWaterLiveForDate(date);
    }

    // Sleep
    public void saveSleep(String date, int minutes) {
        executor.execute(() -> {
            SleepLogs record = dao.getSleepForDate(date);
            if (record == null) {
                record = new SleepLogs();
                record.date = date;
            }
            record.minutes = minutes;
            dao.insertSleep(record);
        });
    }

    public LiveData<SleepLogs> getSleepLive(String date) {
        return dao.getSleepLiveForDate(date);
    }

    // Logged Meals
    public void saveLoggedMeal(LoggedMeal meal) {
        executor.execute(() -> {
            dao.insertLoggedMeal(meal);
        });
    }

    public void deleteLoggedMeal(LoggedMeal meal) {
        executor.execute(() -> {
            dao.deleteLoggedMeal(meal);
        });
    }

    public LiveData<java.util.List<LoggedMeal>> getLoggedMealsLive(String date) {
        return dao.getLoggedMealsLiveForDate(date);
    }

    // Exercise Guides Repository methods
    public void saveExerciseGuide(com.fitness.app.models.ExerciseGuide guide) {
        executor.execute(() -> dao.insertExerciseGuide(guide));
    }

    public void deleteExerciseGuide(com.fitness.app.models.ExerciseGuide guide) {
        executor.execute(() -> dao.deleteExerciseGuide(guide));
    }

    public LiveData<java.util.List<com.fitness.app.models.ExerciseGuide>> getAllExerciseGuidesLive() {
        return dao.getAllExerciseGuidesLive();
    }

    public java.util.List<com.fitness.app.models.ExerciseGuide> getAllExerciseGuidesSync() {
        return dao.getAllExerciseGuides();
    }

    public void clearAllExerciseGuides() {
        executor.execute(() -> dao.clearAllExerciseGuides());
    }
}

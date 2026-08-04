package com.fitness.app.data.room;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class FitnessViewModel extends AndroidViewModel {

    private final FitnessRepository repository;

    public FitnessViewModel(@NonNull Application application) {
        super(application);
        repository = new FitnessRepository(application);
    }

    public void saveSteps(String date, int count) {
        repository.saveSteps(date, count);
    }

    public LiveData<DailySteps> getStepsLive(String date) {
        return repository.getStepsLive(date);
    }

    public void saveCalories(String date, int calories) {
        repository.saveCalories(date, calories);
    }

    public LiveData<CaloriesBurned> getCaloriesLive(String date) {
        return repository.getCaloriesLive(date);
    }

    public void saveWater(String date, int amountMl) {
        repository.saveWater(date, amountMl);
    }

    public LiveData<WaterIntake> getWaterLive(String date) {
        return repository.getWaterLive(date);
    }

    public void saveSleep(String date, int minutes) {
        repository.saveSleep(date, minutes);
    }

    public LiveData<SleepLogs> getSleepLive(String date) {
        return repository.getSleepLive(date);
    }

    // Logged Meals
    public void saveLoggedMeal(LoggedMeal meal) {
        repository.saveLoggedMeal(meal);
    }

    public void deleteLoggedMeal(LoggedMeal meal) {
        repository.deleteLoggedMeal(meal);
    }

    public LiveData<java.util.List<LoggedMeal>> getLoggedMealsLive(String date) {
        return repository.getLoggedMealsLive(date);
    }

    // Exercise Guides View Model methods
    public void saveExerciseGuide(com.fitness.app.models.ExerciseGuide guide) {
        repository.saveExerciseGuide(guide);
    }

    public void deleteExerciseGuide(com.fitness.app.models.ExerciseGuide guide) {
        repository.deleteExerciseGuide(guide);
    }

    public LiveData<java.util.List<com.fitness.app.models.ExerciseGuide>> getAllExerciseGuidesLive() {
        return repository.getAllExerciseGuidesLive();
    }

    public java.util.List<com.fitness.app.models.ExerciseGuide> getAllExerciseGuidesSync() {
        return repository.getAllExerciseGuidesSync();
    }

    public void clearAllExerciseGuides() {
        repository.clearAllExerciseGuides();
    }
}

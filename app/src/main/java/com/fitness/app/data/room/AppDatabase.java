package com.fitness.app.data.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {
        DailySteps.class, CaloriesBurned.class, WaterIntake.class, SleepLogs.class, LoggedMeal.class,
        com.fitness.app.models.ChatMessage.class, com.fitness.app.models.ExerciseGuide.class,
        com.fitness.app.models.TransformationPlan.class, com.fitness.app.models.TransformationProgress.class,
        com.fitness.app.models.TransformationTask.class,
        com.fitness.app.models.ScannedIngredient.class, com.fitness.app.models.GroceryRecipe.class,
        com.fitness.app.models.ShoppingListItem.class, com.fitness.app.models.WeeklyMealPlanDay.class,
        com.fitness.app.models.GroceryVoiceMessage.class, com.fitness.app.models.GroceryProductScan.class,
        com.fitness.app.models.UserMotivationState.class, com.fitness.app.models.AvatarGreetingLog.class,
        com.fitness.app.models.EquipmentWorkoutPlan.class, com.fitness.app.models.FitnessChallenge.class,
        com.fitness.app.models.UserChallengeStats.class
}, version = 16, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract FitnessDao fitnessDao();
    public abstract ChatDao chatDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "fitness_room_database")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }
}

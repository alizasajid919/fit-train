package com.fitness.app.data.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface FitnessDao {

    // Steps
    @Query("SELECT * FROM daily_steps WHERE date = :date LIMIT 1")
    DailySteps getStepsForDate(String date);

    @Query("SELECT * FROM daily_steps WHERE date = :date LIMIT 1")
    LiveData<DailySteps> getStepsLiveForDate(String date);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSteps(DailySteps steps);

    // Calories
    @Query("SELECT * FROM calories_burned WHERE date = :date LIMIT 1")
    CaloriesBurned getCaloriesForDate(String date);

    @Query("SELECT * FROM calories_burned WHERE date = :date LIMIT 1")
    LiveData<CaloriesBurned> getCaloriesLiveForDate(String date);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCalories(CaloriesBurned calories);

    // Water
    @Query("SELECT * FROM water_intake WHERE date = :date LIMIT 1")
    WaterIntake getWaterForDate(String date);

    @Query("SELECT * FROM water_intake WHERE date = :date LIMIT 1")
    LiveData<WaterIntake> getWaterLiveForDate(String date);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWater(WaterIntake water);

    // Sleep
    @Query("SELECT * FROM sleep_logs WHERE date = :date LIMIT 1")
    SleepLogs getSleepForDate(String date);

    @Query("SELECT * FROM sleep_logs WHERE date = :date LIMIT 1")
    LiveData<SleepLogs> getSleepLiveForDate(String date);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSleep(SleepLogs sleep);

    // Logged Meals
    @Query("SELECT * FROM logged_meals ORDER BY timestamp ASC")
    java.util.List<LoggedMeal> getAllLoggedMeals();

    @Query("SELECT * FROM logged_meals WHERE date = :date ORDER BY timestamp ASC")
    java.util.List<LoggedMeal> getLoggedMealsForDate(String date);

    @Query("SELECT * FROM logged_meals WHERE date = :date ORDER BY timestamp ASC")
    LiveData<java.util.List<LoggedMeal>> getLoggedMealsLiveForDate(String date);

    @Query("SELECT * FROM logged_meals WHERE id = :id LIMIT 1")
    LoggedMeal getLoggedMealById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertLoggedMeal(LoggedMeal meal);

    @androidx.room.Delete
    void deleteLoggedMeal(LoggedMeal meal);

    @Query("DELETE FROM logged_meals WHERE date = :date")
    void deleteLoggedMealsForDate(String date);

    @Query("DELETE FROM logged_meals WHERE date = :date AND isChecked = 0")
    void deleteUncompletedLoggedMealsForDate(String date);

    @Query("DELETE FROM logged_meals WHERE isChecked = 0")
    void deleteAllUncompletedLoggedMeals();

    // Exercise Guides CRUD
    @Query("SELECT * FROM exercise_guides ORDER BY id ASC")
    java.util.List<com.fitness.app.models.ExerciseGuide> getAllExerciseGuides();

    @Query("SELECT * FROM exercise_guides ORDER BY id ASC")
    LiveData<java.util.List<com.fitness.app.models.ExerciseGuide>> getAllExerciseGuidesLive();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertExerciseGuide(com.fitness.app.models.ExerciseGuide guide);

    @androidx.room.Delete
    void deleteExerciseGuide(com.fitness.app.models.ExerciseGuide guide);

    @Query("DELETE FROM exercise_guides")
    void clearAllExerciseGuides();

    // Transformation Plan
    @Query("SELECT * FROM transformation_plans WHERE userId = :userId AND status = 'ACTIVE' LIMIT 1")
    com.fitness.app.models.TransformationPlan getActiveTransformationPlan(String userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertTransformationPlan(com.fitness.app.models.TransformationPlan plan);

    @Query("DELETE FROM transformation_plans WHERE userId = :userId")
    void deleteTransformationPlansForUser(String userId);

    // Transformation Progress
    @Query("SELECT * FROM transformation_progress WHERE userId = :userId ORDER BY weekNumber ASC")
    java.util.List<com.fitness.app.models.TransformationProgress> getTransformationProgressForUser(String userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTransformationProgress(com.fitness.app.models.TransformationProgress progress);

    // Transformation Tasks
    @Query("SELECT * FROM transformation_tasks WHERE userId = :userId AND date = :date")
    java.util.List<com.fitness.app.models.TransformationTask> getTransformationTasksForDate(String userId, String date);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTransformationTask(com.fitness.app.models.TransformationTask task);

    @Query("UPDATE transformation_tasks SET progressValue = :progress, isCompleted = :completed WHERE taskId = :taskId")
    void updateTransformationTaskProgress(int taskId, int progress, boolean completed);

    // Scanned Ingredients
    @Query("SELECT * FROM scanned_ingredients ORDER BY timestamp DESC")
    java.util.List<com.fitness.app.models.ScannedIngredient> getAllScannedIngredients();

    @Query("SELECT * FROM scanned_ingredients ORDER BY timestamp DESC")
    LiveData<java.util.List<com.fitness.app.models.ScannedIngredient>> getAllScannedIngredientsLive();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertScannedIngredient(com.fitness.app.models.ScannedIngredient ingredient);

    @androidx.room.Delete
    void deleteScannedIngredient(com.fitness.app.models.ScannedIngredient ingredient);

    @Query("DELETE FROM scanned_ingredients")
    void clearScannedIngredients();

    // Grocery Recipes
    @Query("SELECT * FROM grocery_recipes ORDER BY timestamp DESC")
    java.util.List<com.fitness.app.models.GroceryRecipe> getAllGroceryRecipes();

    @Query("SELECT * FROM grocery_recipes ORDER BY timestamp DESC")
    LiveData<java.util.List<com.fitness.app.models.GroceryRecipe>> getAllGroceryRecipesLive();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertGroceryRecipe(com.fitness.app.models.GroceryRecipe recipe);

    @androidx.room.Delete
    void deleteGroceryRecipe(com.fitness.app.models.GroceryRecipe recipe);

    @Query("DELETE FROM grocery_recipes")
    void clearGroceryRecipes();

    // Shopping List
    @Query("SELECT * FROM shopping_list_items ORDER BY category ASC, isPurchased ASC")
    java.util.List<com.fitness.app.models.ShoppingListItem> getAllShoppingListItems();

    @Query("SELECT * FROM shopping_list_items ORDER BY category ASC, isPurchased ASC")
    LiveData<java.util.List<com.fitness.app.models.ShoppingListItem>> getAllShoppingListItemsLive();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertShoppingListItem(com.fitness.app.models.ShoppingListItem item);

    @androidx.room.Delete
    void deleteShoppingListItem(com.fitness.app.models.ShoppingListItem item);

    @Query("DELETE FROM shopping_list_items")
    void clearShoppingList();

    // Weekly Meal Plan Days
    @Query("SELECT * FROM weekly_meal_plan_days ORDER BY dayOfWeek ASC")
    java.util.List<com.fitness.app.models.WeeklyMealPlanDay> getWeeklyMealPlan();

    @Query("SELECT * FROM weekly_meal_plan_days ORDER BY dayOfWeek ASC")
    LiveData<java.util.List<com.fitness.app.models.WeeklyMealPlanDay>> getWeeklyMealPlanLive();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWeeklyMealPlanDay(com.fitness.app.models.WeeklyMealPlanDay day);

    @Query("DELETE FROM weekly_meal_plan_days")
    void clearWeeklyMealPlan();

    // Grocery Voice Messages
    @Query("SELECT * FROM grocery_voice_messages ORDER BY timestamp ASC")
    java.util.List<com.fitness.app.models.GroceryVoiceMessage> getAllVoiceMessages();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertVoiceMessage(com.fitness.app.models.GroceryVoiceMessage message);

    @Query("DELETE FROM grocery_voice_messages")
    void clearVoiceMessages();

    // Grocery Product Scans (Scan History)
    @Query("SELECT * FROM grocery_product_scans ORDER BY timestamp DESC")
    java.util.List<com.fitness.app.models.GroceryProductScan> getAllProductScans();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProductScan(com.fitness.app.models.GroceryProductScan scan);

    @Query("DELETE FROM grocery_product_scans")
    void clearProductScans();

    // Motivation & Greetings History
    @Query("SELECT * FROM user_motivation_states LIMIT 1")
    com.fitness.app.models.UserMotivationState getMotivationState();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMotivationState(com.fitness.app.models.UserMotivationState state);

    @Query("SELECT * FROM avatar_greeting_logs ORDER BY timestamp DESC")
    java.util.List<com.fitness.app.models.AvatarGreetingLog> getAllGreetingLogs();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertGreetingLog(com.fitness.app.models.AvatarGreetingLog log);

    @Query("DELETE FROM avatar_greeting_logs")
    void clearGreetingLogs();

    // Equipment Workout Plans
    @Query("SELECT * FROM equipment_workout_plans ORDER BY dateCreated DESC")
    java.util.List<com.fitness.app.models.EquipmentWorkoutPlan> getAllEquipmentWorkoutPlans();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertEquipmentWorkoutPlan(com.fitness.app.models.EquipmentWorkoutPlan plan);

    @Query("DELETE FROM equipment_workout_plans WHERE id = :planId")
    void deleteEquipmentWorkoutPlan(String planId);

    // Fitness Challenges
    @Query("SELECT * FROM fitness_challenges ORDER BY startDate DESC")
    java.util.List<com.fitness.app.models.FitnessChallenge> getAllFitnessChallenges();

    @Query("SELECT * FROM fitness_challenges WHERE status = 'IN_PROGRESS' LIMIT 1")
    com.fitness.app.models.FitnessChallenge getActiveChallenge();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFitnessChallenge(com.fitness.app.models.FitnessChallenge challenge);

    @Query("DELETE FROM fitness_challenges")
    void clearAllFitnessChallenges();

    // User Challenge Stats
    @Query("SELECT * FROM user_challenge_stats LIMIT 1")
    com.fitness.app.models.UserChallengeStats getUserChallengeStats();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUserChallengeStats(com.fitness.app.models.UserChallengeStats stats);
}

package com.fitness.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.fitness.app.R;
import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.models.DietPlan;
import com.fitness.app.models.User;
import com.fitness.app.models.WorkoutPlan;
import com.fitness.app.utils.RecommendationEngine;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AIRecommendationsActivity extends AppCompatActivity {

    private TextView tvWorkoutGoal, tvWorkoutLevel, tvWorkoutTitle;
    private TextView tvDietCalories, tvDietWater, tvProteinVal, tvCarbsVal, tvFatVal;
    private TextView tvFiberVal, tvSugarVal, tvSodiumVal;
    private ProgressBar pbProtein, pbCarbs, pbFats, pbFiber, pbSugar, pbSodium;
    private TextView tvStepTarget, tvSleepTarget, tvRecoveryAdvice;
    private LinearLayout llExercisesList, llMealsList;

    private LocalDataManager localDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_recommendations);

        localDb = new LocalDataManager(this);

        // Bind Views
        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());

        tvWorkoutGoal = findViewById(R.id.tvWorkoutGoal);
        tvWorkoutLevel = findViewById(R.id.tvWorkoutLevel);
        tvWorkoutTitle = findViewById(R.id.tvWorkoutTitle);
        llExercisesList = findViewById(R.id.llExercisesList);

        tvDietCalories = findViewById(R.id.tvDietCalories);
        tvDietWater = findViewById(R.id.tvDietWater);
        tvProteinVal = findViewById(R.id.tvProteinVal);
        tvCarbsVal = findViewById(R.id.tvCarbsVal);
        tvFatVal = findViewById(R.id.tvFatVal);
        tvFiberVal = findViewById(R.id.tvFiberVal);
        tvSugarVal = findViewById(R.id.tvSugarVal);
        tvSodiumVal = findViewById(R.id.tvSodiumVal);
        
        pbProtein = findViewById(R.id.pbProtein);
        pbCarbs = findViewById(R.id.pbCarbs);
        pbFats = findViewById(R.id.pbFats);
        pbFiber = findViewById(R.id.pbFiber);
        pbSugar = findViewById(R.id.pbSugar);
        pbSodium = findViewById(R.id.pbSodium);
        
        llMealsList = findViewById(R.id.llMealsList);

        tvStepTarget = findViewById(R.id.tvStepTarget);
        tvSleepTarget = findViewById(R.id.tvSleepTarget);
        tvRecoveryAdvice = findViewById(R.id.tvRecoveryAdvice);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecommendations();
    }

    private void loadRecommendations() {
        try {
            User user = localDb.getUser();
            if (user == null) {
                android.widget.Toast.makeText(this, "Please complete your profile to view recommendations.", android.widget.Toast.LENGTH_LONG).show();
                return;
            }

            // Load or generate today's plans
            WorkoutPlan workout = localDb.getWorkoutPlan();
            DietPlan diet = localDb.getDietPlan();
            String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            String lastGenDate = localDb.sharedPreferences.getString("last_plan_gen_date", "");

            // Detect real-time profile changes (e.g. goal, weight, or preference update)
            DietPlan tempDiet = RecommendationEngine.generateDailyDietPlan(user, "Yes", true);
            boolean profileChanged = diet != null && (
                (user.getDietaryPreference() != null && !user.getDietaryPreference().equalsIgnoreCase(diet.getDietType())) ||
                Math.abs(tempDiet.getTargetCalories() - diet.getTargetCalories()) > 50
            );

            if (workout == null || diet == null || !todayStr.equals(lastGenDate) || profileChanged) {
                String diffFeedback = localDb.sharedPreferences.getString("last_workout_difficulty", "Medium");
                boolean completedYesterday = localDb.sharedPreferences.getBoolean("yesterday_workout_completed", true);
                
                workout = RecommendationEngine.generateDailyWorkoutPlan(user, diffFeedback, completedYesterday);
                diet = tempDiet;
                
                localDb.saveWorkoutPlan(workout);
                localDb.saveDietPlan(diet);
                localDb.sharedPreferences.edit().putString("last_plan_gen_date", todayStr).apply();
            }

            // 1. Bind Workout Recommendation
            tvWorkoutGoal.setText("Goal: " + (user.getGoal() != null ? user.getGoal() : "Improve Shape"));
            tvWorkoutLevel.setText(user.getFitnessExperience() != null ? user.getFitnessExperience() : "Beginner");
            if (workout != null) {
                tvWorkoutTitle.setText(workout.getGoal() + " Specific Program");

                llExercisesList.removeAllViews();
                for (WorkoutPlan.Exercise ex : workout.getExercises()) {
                    View itemView = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, null);
                    TextView text1 = itemView.findViewById(android.R.id.text1);
                    TextView text2 = itemView.findViewById(android.R.id.text2);

                    text1.setText("🏃 " + ex.getName());
                    text1.setTextColor(getResources().getColor(android.R.color.black));
                    text1.setTextSize(14);

                    String repsStr = ex.getDurationSeconds() > 0 
                            ? (ex.getDurationSeconds() + " seconds hold") 
                            : (ex.getSets() + " sets x " + ex.getReps() + " reps");
                    text2.setText(repsStr + " | Target: " + ex.getDescription());
                    text2.setTextColor(getResources().getColor(android.R.color.darker_gray));
                    text2.setTextSize(12);

                    llExercisesList.addView(itemView);
                }

                final WorkoutPlan finalWorkout = workout;
                findViewById(R.id.btnStartWorkout).setOnClickListener(v -> {
                    Intent intent = new Intent(AIRecommendationsActivity.this, LogWorkoutActivity.class);
                    intent.putExtra("exercise_name", finalWorkout.getGoal());
                    intent.putExtra("duration_min", "25");
                    intent.putExtra("calories_kcal", "240");
                    startActivity(intent);
                });
            }

            // 2. Bind Diet Recommendations
            if (diet != null) {
                tvDietCalories.setText(String.format(Locale.US, "%,d kcal", diet.getTargetCalories()));
                tvDietWater.setText(String.format(Locale.US, "%,d ml", diet.getTargetWaterMl()));

                tvProteinVal.setText(diet.getTargetProteinGrams() + "g");
                tvCarbsVal.setText(diet.getTargetCarbsGrams() + "g");
                tvFatVal.setText(diet.getTargetFatGrams() + "g");
                tvFiberVal.setText(diet.getTargetFiberGrams() + "g");
                tvSugarVal.setText(diet.getTargetSugarGrams() + "g");
                tvSodiumVal.setText(diet.getTargetSodiumMg() + "mg");

                pbProtein.setMax(diet.getTargetProteinGrams() * 2);
                pbProtein.setProgress(diet.getTargetProteinGrams());

                pbCarbs.setMax(diet.getTargetCarbsGrams() * 2);
                pbCarbs.setProgress(diet.getTargetCarbsGrams());

                pbFats.setMax(diet.getTargetFatGrams() * 2);
                pbFats.setProgress(diet.getTargetFatGrams());

                pbFiber.setMax(diet.getTargetFiberGrams() * 2);
                pbFiber.setProgress(diet.getTargetFiberGrams());

                pbSugar.setMax(diet.getTargetSugarGrams() * 2);
                pbSugar.setProgress(diet.getTargetSugarGrams());

                pbSodium.setMax(diet.getTargetSodiumMg() * 2);
                pbSodium.setProgress(diet.getTargetSodiumMg());

                llMealsList.removeAllViews();
                for (DietPlan.Meal meal : diet.getMeals()) {
                    View itemView = getLayoutInflater().inflate(R.layout.item_recommendation_meal, null);
                    TextView tvMealEmoji = itemView.findViewById(R.id.tvMealEmoji);
                    TextView tvMealType = itemView.findViewById(R.id.tvMealType);
                    TextView tvMealCalories = itemView.findViewById(R.id.tvMealCalories);
                    TextView tvMealName = itemView.findViewById(R.id.tvMealName);
                    TextView tvMealProtein = itemView.findViewById(R.id.tvMealProtein);
                    TextView tvMealCarbs = itemView.findViewById(R.id.tvMealCarbs);
                    TextView tvMealFat = itemView.findViewById(R.id.tvMealFat);
                    TextView tvMealFiber = itemView.findViewById(R.id.tvMealFiber);
                    TextView tvMealPortions = itemView.findViewById(R.id.tvMealPortions);
                    TextView tvMealIngredients = itemView.findViewById(R.id.tvMealIngredients);
                    TextView tvMealDescription = itemView.findViewById(R.id.tvMealDescription);

                    // Map colorful meal emojis
                    String emoji = "🍽️";
                    String type = meal.getType();
                    if ("Breakfast".equalsIgnoreCase(type)) emoji = "🍳";
                    else if ("Morning Snack".equalsIgnoreCase(type)) emoji = "🍎";
                    else if ("Lunch".equalsIgnoreCase(type)) emoji = "🍗";
                    else if ("Evening Snack".equalsIgnoreCase(type)) emoji = "🥜";
                    else if ("Dinner".equalsIgnoreCase(type)) emoji = "🍽️";

                    tvMealEmoji.setText(emoji);
                    tvMealType.setText(type);
                    tvMealCalories.setText(meal.getCalories() + " kcal");
                    tvMealName.setText(meal.getName());
                    
                    tvMealProtein.setText(String.format(Locale.US, "P: %.0fg", meal.getProtein()));
                    tvMealCarbs.setText(String.format(Locale.US, "C: %.0fg", meal.getCarbs()));
                    tvMealFat.setText(String.format(Locale.US, "F: %.0fg", meal.getFat()));
                    tvMealFiber.setText(String.format(Locale.US, "Fb: %.0fg", meal.getFiber()));
                    
                    tvMealPortions.setText("Portion: " + meal.getPortionSize() + " | Serving: " + meal.getServingSize());
                    tvMealIngredients.setText("Ingredients: " + meal.getIngredients());
                    tvMealDescription.setText(meal.getDescription());

                    llMealsList.addView(itemView);
                }
            }

            // 3. Calculate and bind dynamically adjusted targets based on age, weight, goal & activity
            double weightVal = user.getWeight() > 0 ? user.getWeight() : 70.0;
            int ageVal = user.getAge() > 0 ? user.getAge() : 25;
            String goalStr = user.getGoal() != null ? user.getGoal() : "Maintain Weight";
            String actStr = user.getActivityLevel() != null ? user.getActivityLevel() : "Sedentary";

            int stepsGoal = user.getDailyStepGoal();
            if (stepsGoal <= 0) {
                stepsGoal = 8000;
                if (actStr.contains("Active") || actStr.contains("Moderat")) stepsGoal = 10000;
                if (actStr.contains("Very") || actStr.contains("Athlete")) stepsGoal = 12000;
                if (goalStr.contains("Loss") || goalStr.contains("Fat")) stepsGoal += 2000; // Boost NEAT
            }

            int sleepGoal = user.getDailySleepGoal();
            if (sleepGoal <= 0) {
                sleepGoal = 8;
                if (ageVal < 18) sleepGoal = 9;
                String diff = localDb.sharedPreferences.getString("last_workout_difficulty", "Medium");
                if ("Hard".equalsIgnoreCase(diff)) sleepGoal += 1;
            }

            tvStepTarget.setText(String.format(Locale.getDefault(), "%,d steps", stepsGoal));
            tvSleepTarget.setText(sleepGoal + " hours");

            // Specific recovery tip based on yesterday's difficulty level
            String diff = localDb.sharedPreferences.getString("last_workout_difficulty", "Medium");
            if ("Hard".equalsIgnoreCase(diff)) {
                tvRecoveryAdvice.setText("Yesterday's session was intense. Focus on complete recovery: 15 mins gentle hamstring stretching, 3L water hydration, and sleep at least 8.5 hours.");
            } else if ("Easy".equalsIgnoreCase(diff)) {
                tvRecoveryAdvice.setText("Yesterday felt light! Today we have increased sets/intensity. Keep training with explosive compound motions and recover with dynamic foam rolling.");
            } else {
                tvRecoveryAdvice.setText("Stay consistent. Perform 10 minutes of active stretching post-workout. Focus on sleep quality and clean lean proteins.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            android.widget.Toast.makeText(this, "Error generating AI recommendations: " + e.getLocalizedMessage(), android.widget.Toast.LENGTH_LONG).show();
        }
    }
}

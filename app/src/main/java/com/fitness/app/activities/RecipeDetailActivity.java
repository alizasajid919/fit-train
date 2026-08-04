package com.fitness.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fitness.app.R;
import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.models.DietPlan;
import com.fitness.app.models.User;
import com.fitness.app.data.room.LoggedMeal;

public class RecipeDetailActivity extends AppCompatActivity {

    private String mealName;
    private String mealType;
    private int caloriesVal;
    private String ingredientsVal;
    private String instructionsVal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_recipe_detail);

        // Retrieve intent extras
        mealName = getIntent().getStringExtra("meal_name");
        mealType = getIntent().getStringExtra("meal_type");
        caloriesVal = getIntent().getIntExtra("meal_calories", 350);
        ingredientsVal = getIntent().getStringExtra("meal_ingredients");
        instructionsVal = getIntent().getStringExtra("meal_instructions");

        if (mealName == null) mealName = "Blueberry Pancakes";
        if (mealType == null) mealType = "BREAKFAST";
        if (ingredientsVal == null) {
            ingredientsVal = "1 cup Oats, 1 Whole Banana, 2 tbsp Peanut Butter, 1 cup Milk, Berries";
        }
        if (instructionsVal == null) {
            instructionsVal = "Blend all ingredients until smooth or cook on a lightly greased hot griddle until bubbles form. Flip and cook until golden.";
        }

        // Bind UI Views
        TextView tvMealTitle = findViewById(R.id.tvMealTitle);
        TextView tvAuthor = findViewById(R.id.tvAuthor);
        TextView tvCalories = findViewById(R.id.tvCalories);
        TextView tvProtein = findViewById(R.id.tvProtein);
        TextView tvCarbs = findViewById(R.id.tvCarbs);
        TextView tvFat = findViewById(R.id.tvFat);
        TextView tvDescription = findViewById(R.id.tvDescription);
        TextView tvIngredientsList = findViewById(R.id.tvIngredientsList);
        TextView tvStepsList = findViewById(R.id.tvStepsList);
        TextView tvMealEmoji = findViewById(R.id.tvMealEmoji);
        Button btnAddSchedule = findViewById(R.id.btnAddSchedule);

        // Bind Favorite button overlay
        android.widget.ImageButton btnFavorite = findViewById(R.id.btnFavorite);
        if (btnFavorite != null) {
            java.util.Set<String> favs = getSharedPreferences("diet_prefs", MODE_PRIVATE)
                    .getStringSet("favorite_meals", new java.util.HashSet<>());
            boolean isFavorite = favs.contains(mealName);
            btnFavorite.setImageResource(isFavorite ? R.drawable.ic_heart_fav_filled : R.drawable.ic_heart_fav);

            btnFavorite.setOnClickListener(v -> {
                android.content.SharedPreferences prefs = getSharedPreferences("diet_prefs", MODE_PRIVATE);
                java.util.Set<String> nextFavs = new java.util.HashSet<>(prefs.getStringSet("favorite_meals", new java.util.HashSet<>()));
                boolean nextState = !nextFavs.contains(mealName);
                if (nextState) {
                    nextFavs.add(mealName);
                    btnFavorite.setImageResource(R.drawable.ic_heart_fav_filled);
                    Toast.makeText(RecipeDetailActivity.this, "Meal saved to favorites!", Toast.LENGTH_SHORT).show();
                } else {
                    nextFavs.remove(mealName);
                    btnFavorite.setImageResource(R.drawable.ic_heart_fav);
                    Toast.makeText(RecipeDetailActivity.this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                }
                prefs.edit().putStringSet("favorite_meals", nextFavs).apply();
            });
        }

        // Set Data
        tvMealTitle.setText(mealName);
        tvAuthor.setText("by james oliver");
        tvCalories.setText(caloriesVal + " kcal");
        
        // Calculate estimated macros
        int protein = (int) (caloriesVal * 0.15 / 4);
        int carbs = (int) (caloriesVal * 0.55 / 4);
        int fat = (int) (caloriesVal * 0.30 / 9);
        tvProtein.setText(protein + "g\nProtein");
        tvCarbs.setText(carbs + "g\nCarbs");
        tvFat.setText(fat + "g\nFat");

        tvDescription.setText(String.format("Pancakes are the perfect way to start your day. Made with natural, organic ingredients to ensure high energy, metabolic lift, and pure deliciousness. Ideal for your %s routine.", mealType.toLowerCase()));

        // Format Ingredients list
        StringBuilder ingBuilder = new StringBuilder();
        String[] ings = ingredientsVal.split(",");
        for (String ing : ings) {
            ingBuilder.append("• ").append(ing.trim()).append("\n");
        }
        tvIngredientsList.setText(ingBuilder.toString().trim());

        // Format Step-by-Step Instructions
        StringBuilder instBuilder = new StringBuilder();
        String[] steps;
        if (instructionsVal.contains("\n")) {
            steps = instructionsVal.split("\n");
        } else {
            steps = instructionsVal.split("(?<=\\.)\\s+");
        }
        int stepNum = 1;
        for (String step : steps) {
            String trimmedStep = step.trim();
            trimmedStep = trimmedStep.replaceAll("^(?i)(?:step\\s*)?\\d+\\s*[:\\.]?\\s*", "").trim();
            if (trimmedStep.endsWith(".")) {
                trimmedStep = trimmedStep.substring(0, trimmedStep.length() - 1).trim();
            }
            if (!trimmedStep.isEmpty()) {
                instBuilder.append(stepNum).append(". ").append(trimmedStep).append(".\n\n");
                stepNum++;
            }
        }
        tvStepsList.setText(instBuilder.toString().trim());

        // Emoji selection
        tvMealEmoji.setText(MealScheduleActivity.getMealEmoji(-1, mealName));

        // Back action
        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());

        // Add to schedule action
        btnAddSchedule.setOnClickListener(v -> {
            LocalDataManager localDb = new LocalDataManager(RecipeDetailActivity.this);
            com.fitness.app.data.room.FitnessDao fitnessDao = com.fitness.app.data.room.AppDatabase.getInstance(RecipeDetailActivity.this).fitnessDao();
            
            String todayStr = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
            
            new Thread(() -> {
                try {
                    int finalIconId = 0;
                    String name = mealName;
                    for (int i = 0; i < MealScheduleActivity.emojiNames.length; i++) {
                        if (name.toLowerCase(java.util.Locale.getDefault()).contains(MealScheduleActivity.emojiNames[i].toLowerCase(java.util.Locale.getDefault()))) {
                            finalIconId = i;
                            break;
                        }
                    }
                    
                    int targetProt = (int) (caloriesVal * 0.15 / 4);
                    int targetCarbs = (int) (caloriesVal * 0.55 / 4);
                    int targetFat = (int) (caloriesVal * 0.30 / 9);
                    
                    LoggedMeal logged = new LoggedMeal(
                            mealType != null ? mealType : "Snacks",
                            mealName,
                            caloriesVal,
                            targetProt,
                            targetCarbs,
                            targetFat,
                            todayStr,
                            System.currentTimeMillis(),
                            "12:00 PM",
                            "Added from recipe details.",
                            false,
                            finalIconId,
                            (int) (caloriesVal * 0.014),
                            0,
                            "1 serving",
                            ingredientsVal,
                            instructionsVal
                    );
                    
                    fitnessDao.insertLoggedMeal(logged);
                    
                    java.util.List<LoggedMeal> dayMeals = fitnessDao.getLoggedMealsForDate(todayStr);
                    int totalConsumed = 0;
                    for (LoggedMeal m : dayMeals) {
                        if (m.isChecked) {
                            totalConsumed += m.calories;
                        }
                    }
                    com.fitness.app.models.ProgressLog progressLog = localDb.getProgressLog(todayStr);
                    progressLog.setCaloriesConsumed(totalConsumed);
                    progressLog.setUpdatedAt(System.currentTimeMillis());
                    localDb.saveProgressLog(progressLog);
                    
                    try {
                        new com.fitness.app.repositories.UserRepository().syncLocalDataToFirestore(localDb);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    
                    runOnUiThread(() -> {
                        Toast.makeText(RecipeDetailActivity.this, mealName + " added to today's Meal Schedule! 🍽️", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        Toast.makeText(RecipeDetailActivity.this, "Failed to add meal to schedule.", Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}

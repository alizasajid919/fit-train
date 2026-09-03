package com.fitness.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fitness.app.R;
import com.fitness.app.data.room.AppDatabase;
import com.fitness.app.data.room.FitnessDao;
import com.fitness.app.data.room.LoggedMeal;
import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.models.ProgressLog;
import com.fitness.app.models.User;

import java.util.Locale;

public class MealDetailActivity extends AppCompatActivity {

    private int mealId = -1;
    private LoggedMeal meal;
    private FitnessDao fitnessDao;

    private TextView tvMealEmoji;
    private TextView tvMealTitle, tvCategoryTime;
    private TextView tvCalories, tvProtein, tvCarbs, tvFat, tvFiber, tvSugar, tvServing;
    private TextView tvNotes, tvIngredientsList, tvStepsList;
    private Button btnDelete, btnEdit, btnCompleteToggle;
    private ImageButton btnBack, btnEditTop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_meal_detail);

        fitnessDao = AppDatabase.getInstance(this).fitnessDao();
        mealId = getIntent().getIntExtra("meal_id", -1);

        // Bind Views
        tvMealEmoji = findViewById(R.id.tvMealEmoji);
        tvMealTitle = findViewById(R.id.tvMealTitle);
        tvCategoryTime = findViewById(R.id.tvCategoryTime);

        tvCalories = findViewById(R.id.tvCalories);
        tvProtein = findViewById(R.id.tvProtein);
        tvCarbs = findViewById(R.id.tvCarbs);
        tvFat = findViewById(R.id.tvFat);
        tvFiber = findViewById(R.id.tvFiber);
        tvSugar = findViewById(R.id.tvSugar);
        tvServing = findViewById(R.id.tvServing);

        tvNotes = findViewById(R.id.tvNotes);
        tvIngredientsList = findViewById(R.id.tvIngredientsList);
        tvStepsList = findViewById(R.id.tvStepsList);

        btnDelete = findViewById(R.id.btnDelete);
        btnEdit = findViewById(R.id.btnEdit);
        btnCompleteToggle = findViewById(R.id.btnCompleteToggle);
        btnBack = findViewById(R.id.btnBack);
        btnEditTop = findViewById(R.id.btnEditTop);

        // Back action
        btnBack.setOnClickListener(v -> onBackPressed());

        // Delete action
        btnDelete.setOnClickListener(v -> deleteMeal());

        // Edit actions
        btnEdit.setOnClickListener(v -> launchEditActivity());
        btnEditTop.setOnClickListener(v -> launchEditActivity());

        // Complete toggle action
        btnCompleteToggle.setOnClickListener(v -> toggleCompletedState());

        loadMealDetails();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMealDetails();
    }

    private void loadMealDetails() {
        if (mealId == -1) {
            Toast.makeText(this, "Error loading meal details", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Query meal from Room DB
        try {
            meal = null;
            // Room allowMainThreadQueries is active in AppDatabase builder
            // Query by iterating or direct DAO query (we will get all meals for date or query by ID if supported)
            // Let's query by checking all date logs or load directly. To make it extremely robust:
            java.util.List<LoggedMeal> allMeals = fitnessDao.getLoggedMealsForDate(getIntent().getStringExtra("selected_date"));
            if (allMeals == null || allMeals.isEmpty()) {
                // query database or use general query
                allMeals = fitnessDao.getLoggedMealsForDate(null); // returns all if date null or empty? Let's check FitnessDao
            }
            // If the query above didn't return, let's query a general dates fallback
            if (allMeals != null) {
                for (LoggedMeal m : allMeals) {
                    if (m.id == mealId) {
                        meal = m;
                        break;
                    }
                }
            }

            // Fallback: If not found in current date list, search all items by date iterator or DAO lookup
            if (meal == null) {
                // direct DAO call
                meal = fitnessDao.getLoggedMealById(mealId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (meal == null) {
            Toast.makeText(this, "Meal not found in database", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        LocalDataManager localDb = new LocalDataManager(this);
        User user = localDb.getUser();

        // Set Fields
        tvMealTitle.setText(meal.name);
        tvCategoryTime.setText(meal.mealType + "  •  " + (meal.mealTime != null ? meal.mealTime : "09:00 AM"));

        tvCalories.setText(meal.calories + " kcal");
        tvProtein.setText(meal.protein + "g");
        tvCarbs.setText(meal.carbs + "g");
        tvFat.setText(meal.fat + "g");
        tvFiber.setText(meal.fiber + "g");
        tvSugar.setText(meal.sugar + "g");
        tvServing.setText(meal.servingSize != null ? meal.servingSize : "1 Serving");

        // Notes
        String userGoal = user != null ? user.getGoal() : "Maintain Weight";
        String notesText = (meal.notes != null && !meal.notes.trim().isEmpty() 
                            && !meal.notes.toLowerCase().contains("no custom notes")
                            && !meal.notes.toLowerCase().contains("scanned groceries context"))
                ? meal.notes
                : generateDynamicNotesAndTips(meal.name, meal.mealType, meal.calories, meal.protein, meal.carbs, meal.fat, userGoal);
        tvNotes.setText(notesText);

        // Ingredients
        String ingredientsStr = (meal.ingredients != null && !meal.ingredients.trim().isEmpty() && !meal.ingredients.toLowerCase().contains("pancake mix"))
                ? meal.ingredients
                : generateDynamicIngredients(meal.name, meal.mealType, meal.calories, user != null ? user.getDietaryPreference() : "Balanced", user != null ? user.getAllergies() : "");
        tvIngredientsList.setText(formatIngredientsWithCalories(ingredientsStr, meal.calories));

        // Preparation steps
        String prepAndCook = generatePrepAndCookDetails(meal.name, meal.mealType);
        String stepsStr = (meal.steps != null && !meal.steps.trim().isEmpty() && !meal.steps.toLowerCase().contains("mix pancake"))
                ? meal.steps
                : generateDynamicSteps(meal.name, meal.mealType);
                
        StringBuilder stepBuilder = new StringBuilder();
        stepBuilder.append(prepAndCook).append("\n\nInstructions:\n");
        String[] steps;
        if (stepsStr.contains("\n")) {
            steps = stepsStr.split("\n");
        } else {
            steps = stepsStr.split("(?<=\\.)\\s+");
        }
        int num = 1;
        for (String step : steps) {
            String trimmed = step.trim();
            trimmed = trimmed.replaceAll("^(?i)(?:step\\s*)?\\d+\\s*[:\\.]?\\s*", "").trim();
            if (trimmed.endsWith(".")) {
                trimmed = trimmed.substring(0, trimmed.length() - 1).trim();
            }
            if (!trimmed.isEmpty()) {
                stepBuilder.append(num).append(". ").append(trimmed).append(".\n\n");
                num++;
            }
        }
        tvStepsList.setText(stepBuilder.toString().trim());

        // Emoji Binding
        tvMealEmoji.setText(MealScheduleActivity.getMealEmoji(meal.iconResId, meal.name));

        // Complete Button state
        if (meal.isChecked) {
            btnCompleteToggle.setText("Completed ✅");
            btnCompleteToggle.setBackgroundColor(0xFF2563EB); // Solid Primary Blue
        } else {
            btnCompleteToggle.setText("Mark as Completed");
            btnCompleteToggle.setBackgroundColor(0xFF3B82F6); // Slate/Blue Brand color
        }
    }

    private void deleteMeal() {
        if (meal == null) return;
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Meal")
                .setMessage("Are you sure you want to remove this meal from your schedule?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    new Thread(() -> {
                        try {
                            fitnessDao.deleteLoggedMeal(meal);
                            updateDailyProgressLog();
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Meal deleted from schedule!", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void launchEditActivity() {
        Intent intent = new Intent(this, AddMealActivity.class);
        intent.putExtra("selected_date", meal.date);
        intent.putExtra("meal_id", meal.id);
        startActivityForResult(intent, 100);
    }

    private void toggleCompletedState() {
        if (meal != null) {
            meal.isChecked = !meal.isChecked;
            new Thread(() -> {
                try {
                    fitnessDao.insertLoggedMeal(meal); // Room REPLACE update
                    updateDailyProgressLog();
                    
                    if (meal.isChecked) {
                        String todayStr = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
                        if (todayStr.equals(meal.date)) {
                            LocalDataManager localDb = new LocalDataManager(MealDetailActivity.this);
                            localDb.incrementStreak();
                        }
                    }
                    
                    runOnUiThread(() -> {
                        Toast.makeText(this, meal.isChecked ? "Meal marked as completed! 🍽️" : "Meal unchecked", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        loadMealDetails(); // Refresh view
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void updateDailyProgressLog() {
        if (meal == null) return;
        LocalDataManager localDb = new LocalDataManager(this);
        String date = meal.date;
        try {
            java.util.List<LoggedMeal> dayMeals = fitnessDao.getLoggedMealsForDate(date);
            int totalConsumed = 0;
            for (LoggedMeal m : dayMeals) {
                if (m.isChecked) {
                    totalConsumed += m.calories;
                }
            }
            
            ProgressLog progressLog = localDb.getProgressLog(date);
            progressLog.setCaloriesConsumed(totalConsumed);
            progressLog.setUpdatedAt(System.currentTimeMillis());
            localDb.saveProgressLog(progressLog);
            
            try {
                new com.fitness.app.repositories.UserRepository().syncLocalDataToFirestore(localDb);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            loadMealDetails();
            setResult(RESULT_OK);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private String formatIngredientsWithCalories(String ingredientsStr, int totalCalories) {
        if (ingredientsStr == null || ingredientsStr.trim().isEmpty()) {
            return "No ingredients details available.";
        }
        
        String[] items = ingredientsStr.split(",");
        StringBuilder sb = new StringBuilder();
        
        double[] relativeWeights = new double[items.length];
        double totalRelativeWeight = 0;
        
        for (int i = 0; i < items.length; i++) {
            String item = items[i].toLowerCase(Locale.getDefault());
            double factor = 1.0;
            if (item.contains("oil") || item.contains("butter") || item.contains("fat")) {
                factor = 4.0;
            } else if (item.contains("chicken") || item.contains("steak") || item.contains("beef") || item.contains("salmon") || item.contains("protein") || item.contains("egg")) {
                factor = 2.5;
            } else if (item.contains("rice") || item.contains("oats") || item.contains("honey") || item.contains("bread") || item.contains("pie") || item.contains("granola")) {
                factor = 2.0;
            } else if (item.contains("milk") || item.contains("yogurt") || item.contains("cheese")) {
                factor = 1.5;
            } else if (item.contains("apple") || item.contains("banana") || item.contains("berry") || item.contains("orange") || item.contains("avocado")) {
                factor = 1.0;
            } else {
                factor = 0.3;
            }
            relativeWeights[i] = factor;
            totalRelativeWeight += factor;
        }
        
        for (int i = 0; i < items.length; i++) {
            String rawItem = items[i].trim();
            if (rawItem.isEmpty()) continue;
            
            String qty = "";
            String name = rawItem;
            
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("^([\\d\\./\\s]+(?:g|ml|tbsp|tsp|clove|cloves|slice|slices|cup|cups|scoop|scoops|pinch|pinches)?)\\s+(.*)$", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(rawItem);
            if (m.find()) {
                qty = m.group(1).trim();
                name = m.group(2).trim();
            } else {
                java.util.regex.Matcher m2 = java.util.regex.Pattern.compile("^([\\d\\./\\s]+)(.*)$").matcher(rawItem);
                if (m2.find()) {
                    qty = m2.group(1).trim();
                    name = m2.group(2).trim();
                }
            }
            
            String lowerName = name.toLowerCase(Locale.getDefault());
            String category = "Other";
            String emoji = "🍽️";
            String substitute = "alternative seasoning";
            
            if (lowerName.contains("salmon") || lowerName.contains("fish") || lowerName.contains("trout")) {
                category = "Fish";
                emoji = "🐟";
                substitute = "Mackerel or baked organic tofu";
            } else if (lowerName.contains("turkey") || lowerName.contains("chicken")) {
                category = "Lean Meat";
                emoji = lowerName.contains("turkey") ? "🦃" : "🍗";
                substitute = "Turkey breast or lean seitan";
            } else if (lowerName.contains("beef") || lowerName.contains("steak") || lowerName.contains("meat") || lowerName.contains("patty")) {
                category = "Meat";
                emoji = "🥩";
                substitute = "Lean pork loin or bison filet";
            } else if (lowerName.contains("bell pepper") || lowerName.contains("peppers") || lowerName.contains("broccoli") || lowerName.contains("spinach") || lowerName.contains("greens") || lowerName.contains("cucumber") || lowerName.contains("celery") || lowerName.contains("carrot") || lowerName.contains("beans") || lowerName.contains("lettuce") || lowerName.contains("tomato")) {
                category = "Vegetable";
                emoji = lowerName.contains("pepper") ? "🫑" : "🥦";
                substitute = "Asparagus or fresh zucchini slices";
            } else if (lowerName.contains("apple") || lowerName.contains("banana") || lowerName.contains("berry") || lowerName.contains("blueberries") || lowerName.contains("strawberries") || lowerName.contains("raspberries") || lowerName.contains("lemon") || lowerName.contains("lime") || lowerName.contains("fruit")) {
                category = "Fruit";
                emoji = "🍎";
                substitute = "Any fresh seasonal organic fruit";
            } else if (lowerName.contains("rice") || lowerName.contains("oats") || lowerName.contains("oat") || lowerName.contains("bread") || lowerName.contains("wheat") || lowerName.contains("flour") || lowerName.contains("granola") || lowerName.contains("grain") || lowerName.contains("quinoa")) {
                category = "Whole Grain";
                emoji = "🌾";
                substitute = "Quinoa, wild black rice, or amaranth";
            } else if (lowerName.contains("milk") || lowerName.contains("yogurt") || lowerName.contains("cheese") || lowerName.contains("cheddar") || lowerName.contains("dairy")) {
                category = "Dairy";
                emoji = "🥛";
                substitute = "Plant-based unsweetened almond or soy dairy";
            } else if (lowerName.contains("butter") || lowerName.contains("oil") || lowerName.contains("fat")) {
                category = "Healthy Fat";
                emoji = "🫒";
                substitute = "Extra virgin olive oil or avocado oil";
            } else if (lowerName.contains("almond") || lowerName.contains("pecan") || lowerName.contains("nut") || lowerName.contains("almonds") || lowerName.contains("pecans")) {
                category = "Nut";
                emoji = "🥜";
                substitute = "Raw sunflower seeds or pumpkin seeds";
            } else if (lowerName.contains("seed") || lowerName.contains("seeds") || lowerName.contains("chia")) {
                category = "Seed";
                emoji = "🌻";
                substitute = "Flax seeds, hemp hearts, or chia seeds";
            } else if (lowerName.contains("honey") || lowerName.contains("sugar") || lowerName.contains("sweetener")) {
                category = "Sweetener";
                emoji = "🍯";
                substitute = "Erythritol or stevia drops";
            } else if (lowerName.contains("egg") || lowerName.contains("eggs")) {
                category = "Protein";
                emoji = "🥚";
                substitute = "Egg whites or scrambled silken tofu";
            } else if (lowerName.contains("salt") || lowerName.contains("cinnamon") || lowerName.contains("oregano") || lowerName.contains("dill") || lowerName.contains("rosemary") || lowerName.contains("pepper") || lowerName.contains("spices") || lowerName.contains("garlic")) {
                category = "Spice / Herb";
                emoji = "🧂";
                substitute = "Lemon zest, garlic powder, or potassium-salt";
            }
            
            int kcal = 0;
            if (totalRelativeWeight > 0) {
                kcal = (int) Math.round((relativeWeights[i] / totalRelativeWeight) * totalCalories);
            }
            
            String formattedName = name;
            if (formattedName.length() > 1) {
                formattedName = formattedName.substring(0, 1).toUpperCase(Locale.getDefault()) + formattedName.substring(1);
            }
            
            sb.append(emoji).append(" ").append(formattedName)
              .append(" (").append(category).append(")");
            
            if (!qty.isEmpty()) {
                sb.append(" — ").append(qty);
            } else {
                sb.append(" — 1 serving");
            }
            
            sb.append(" (").append(kcal).append(" kcal)")
              .append("  [Substitute: ").append(substitute).append("]\n\n");
        }
        
        return sb.toString().trim();
    }

    private String generateDynamicIngredients(String name, String type, int calories, String dietPref, String allergies) {
        String lower = name.toLowerCase(Locale.getDefault());
        boolean hasNuts = allergies.toLowerCase().contains("nut");
        boolean hasDairy = allergies.toLowerCase().contains("dairy") || allergies.toLowerCase().contains("milk");
        boolean hasGluten = allergies.toLowerCase().contains("gluten") || allergies.toLowerCase().contains("wheat");
        
        if (lower.contains("pancake")) {
            if ("keto".equalsIgnoreCase(dietPref)) {
                return "60g almond flour, 2 organic eggs, 30ml unsweetened almond milk, 15g erythritol sweetener, 10g unsalted butter";
            } else if ("vegan".equalsIgnoreCase(dietPref) || "vegetarian".equalsIgnoreCase(dietPref)) {
                return "80g rolled oats (ground), 120ml organic soy milk, 1 tbsp organic maple syrup, 1/2 mashed ripe banana, 1 tsp coconut oil";
            } else {
                return "80g whole wheat flour, 100ml low-fat milk, 1 tbsp organic raw honey, 1 organic egg, 10g unsalted butter";
            }
        }
        if (lower.contains("coffee")) {
            String milkType = hasDairy ? "unsweetened oat milk" : "low-fat milk";
            return "15g premium organic coffee beans, 250ml filtered water, 30ml " + milkType + ", 1 tsp raw sugar";
        }
        if (lower.contains("steak") || lower.contains("beef") || lower.contains("ribeye")) {
            return "200g grass-fed ribeye steak, 15ml extra virgin olive oil, 1 sprig fresh rosemary, 1 clove crushed garlic, sea salt, coarse black pepper";
        }
        if (lower.contains("chicken") || lower.contains("turkey") || lower.contains("breast")) {
            return "180g lean chicken breast filet, 10ml avocado oil, 1/2 fresh lemon, 1/2 tsp garlic powder, dried oregano, sea salt, black pepper";
        }
        if (lower.contains("salmon") || lower.contains("fish") || lower.contains("trout")) {
            return "150g wild-caught Atlantic salmon fillet, 10g grass-fed butter, 1 clove minced garlic, fresh dill sprigs, 1 organic lemon slice";
        }
        if (lower.contains("salad")) {
            return "100g organic mixed baby greens, 1/2 crisp cucumber, 50g cherry tomatoes, 1 tbsp extra virgin olive oil, 1 tsp organic apple cider vinegar";
        }
        if (lower.contains("oatmeal") || lower.contains("oats")) {
            String milkType = hasDairy ? "unsweetened almond milk" : "low-fat milk";
            String nutChoice = hasNuts ? "1 tbsp chia seeds" : "15g crushed raw almonds";
            return "50g organic rolled oats, 200ml " + milkType + ", 1 tbsp organic maple syrup, 1/2 tsp ground cinnamon, " + nutChoice;
        }
        if (lower.contains("yogurt") || lower.contains("parfait")) {
            String base = hasDairy ? "coconut milk yogurt" : "non-fat Greek yogurt";
            String granola = hasGluten ? "gluten-free granola" : "organic oat granola";
            return "150g " + base + ", 30g " + granola + ", 50g mixed fresh blueberries and raspberries, 1 tsp raw honey";
        }
        if (lower.contains("toast") || lower.contains("bread") || lower.contains("sandwich")) {
            String breadType = hasGluten ? "gluten-free seeded bread" : "whole wheat bread";
            if (lower.contains("avocado")) {
                return "2 slices of " + breadType + ", 1/2 ripe avocado, 1/2 tsp red pepper flakes, sea salt, fresh lemon juice";
            }
            return "2 slices of " + breadType + ", 100g lean sliced turkey breast, 1 slice cheddar cheese, 1 leaf organic romaine lettuce, 1 slice fresh tomato";
        }
        if (lower.contains("shake") || lower.contains("smoothie")) {
            String base = hasDairy ? "unsweetened soy milk" : "low-fat milk";
            return "1 scoop whey protein isolate, 250ml " + base + ", 50g frozen mixed berries, 1/2 frozen banana";
        }
        if (lower.contains("fruit") || lower.contains("orange") || lower.contains("apple") || lower.contains("banana") || lower.contains("berry")) {
            return "1 medium seasonal organic fruit (apple, orange, or banana), 10g mixed raw pumpkin and sunflower seeds";
        }
        if (lower.contains("tofu")) {
            return "150g extra-firm organic tofu, 15ml toasted sesame oil, 100g mixed stir-fry vegetables (broccoli, bell peppers), 1 tbsp low-sodium soy sauce";
        }
        if (lower.contains("egg") || lower.contains("scramble") || lower.contains("omelet")) {
            return "3 whole organic eggs, 1 tsp grass-fed butter, 30g chopped spinach, 20g diced cherry tomatoes, sea salt, black pepper";
        }
        
        return "150g cooked principal ingredient (" + name + "), 10ml olive oil, assorted fresh herbs, sea salt, pepper";
    }

    private String generatePrepAndCookDetails(String name, String type) {
        String lower = name.toLowerCase(Locale.getDefault());
        int prep = 10;
        int cook = 15;
        String difficulty = "Easy";
        
        if (lower.contains("pancake") || lower.contains("oatmeal") || lower.contains("eggs") || lower.contains("omelet") || lower.contains("scramble")) {
            prep = 5; cook = 10; difficulty = "Easy";
        } else if (lower.contains("coffee") || lower.contains("fruit") || lower.contains("shake") || lower.contains("smoothie") || lower.contains("yogurt")) {
            prep = 3; cook = 0; difficulty = "Easy";
        } else if (lower.contains("steak") || lower.contains("salmon") || lower.contains("chicken") || lower.contains("tofu") || lower.contains("stir-fry")) {
            prep = 10; cook = 15; difficulty = "Medium";
        } else if (lower.contains("salad")) {
            prep = 8; cook = 0; difficulty = "Easy";
        } else if (lower.contains("bake") || lower.contains("roast")) {
            prep = 15; cook = 30; difficulty = "Medium";
        }
        
        return String.format(Locale.getDefault(), "⏱️ Prep Time: %d mins  |  🍳 Cook Time: %d mins  |  ⚡ Difficulty: %s", prep, cook, difficulty);
    }

    private String generateDynamicSteps(String name, String type) {
        String lower = name.toLowerCase(Locale.getDefault());
        if (lower.contains("pancake")) {
            return "1. Whisk flour, milk, egg, and sweetener in a bowl until smooth.\n\n2. Heat a non-stick pan over medium heat and melt a thin slice of butter.\n\n3. Pour batter onto the pan and cook until bubbles form. Flip and cook until golden brown.\n\n4. Plate and serve warm, drizzled with organic honey or maple syrup.";
        }
        if (lower.contains("coffee")) {
            return "1. Heat water to approximately 200°F (93°C).\n\n2. Grind coffee beans and place them in a French press or drip filter.\n\n3. Pour hot water over the coffee grounds, let it brew for 4 minutes, and press or pour.\n\n4. Stir in warm milk and sweetener of choice.";
        }
        if (lower.contains("steak") || lower.contains("beef") || lower.contains("ribeye")) {
            return "1. Pat steak dry with paper towels. Generously season both sides with sea salt and black pepper.\n\n2. Heat olive oil in a heavy skillet or cast-iron pan over high heat until smoking.\n\n3. Sear steak for 3-4 minutes per side. Add butter, garlic, and rosemary to the pan in the final minute, basting the steak.\n\n4. Remove from heat, let it rest for 5 minutes, then slice against the grain.";
        }
        if (lower.contains("chicken") || lower.contains("turkey") || lower.contains("breast")) {
            return "1. Season chicken breast evenly with garlic powder, oregano, sea salt, and black pepper.\n\n2. Heat avocado oil in a grill pan or skillet over medium-high heat.\n\n3. Cook chicken for 6-8 minutes on each side, or until the internal temperature reaches 165°F (74°C).\n\n4. Squeeze fresh lemon juice over the chicken, cover with foil to rest for 5 minutes, then serve.";
        }
        if (lower.contains("salmon") || lower.contains("fish")) {
            return "1. Preheat oven to 400°F (200°C). Season salmon fillet with sea salt, pepper, and minced garlic.\n\n2. Melt butter and pour over salmon. Top with fresh dill and a lemon slice.\n\n3. Place salmon on a lined baking tray and bake for 12-15 minutes until flaky.\n\n4. Serve alongside freshly sautéed green beans or asparagus.";
        }
        if (lower.contains("salad")) {
            return "1. Wash, dry, and chop mixed baby greens, cucumber, and cherry tomatoes.\n\n2. Toss vegetables in a large salad bowl.\n\n3. Drizzle with extra virgin olive oil and apple cider vinegar.\n\n4. Season with a pinch of sea salt and freshly ground pepper, and serve immediately.";
        }
        if (lower.contains("oatmeal") || lower.contains("oats")) {
            return "1. Combine rolled oats and milk in a small saucepan over medium heat.\n\n2. Bring to a gentle boil, then lower heat and simmer for 5-7 minutes, stirring occasionally.\n\n3. Remove from heat and stir in cinnamon, protein powder (if using), and maple syrup.\n\n4. Transfer to a bowl and garnish with crushed almonds and chia seeds.";
        }
        if (lower.contains("yogurt") || lower.contains("parfait")) {
            return "1. Spoon half of the Greek or coconut yogurt into the bottom of a serving bowl.\n\n2. Sprinkle a layer of granola and half of the mixed berries.\n\n3. Add the remaining yogurt, followed by the rest of the granola and berries.\n\n4. Drizzle with raw organic honey or sugar-free syrup.";
        }
        if (lower.contains("toast") || lower.contains("bread") || lower.contains("sandwich")) {
            return "1. Toast bread slices to desired crispness.\n\n2. Spread mashed avocado on the toast (for avocado toast) or layer sliced turkey, cheese, lettuce, and tomato (for turkey sandwich).\n\n3. Season with red pepper flakes, sea salt, or pepper.\n\n4. Slice diagonally and enjoy fresh.";
        }
        if (lower.contains("shake") || lower.contains("smoothie")) {
            return "1. Add milk and a scoop of protein powder to a high-speed blender.\n\n2. Add frozen berries, banana, or other ingredients.\n\n3. Blend on high for 30-45 seconds until smooth and creamy.\n\n4. Pour into a glass and serve chilled.";
        }
        if (lower.contains("fruit") || lower.contains("orange") || lower.contains("apple") || lower.contains("banana") || lower.contains("berry")) {
            return "1. Rinse fruit under cold running water.\n\n2. Core and slice (for apples/pears) or peel (for oranges/bananas).\n\n3. Arrange slices on a small plate.\n\n4. Serve alongside a handful of raw seeds or nuts.";
        }
        if (lower.contains("tofu")) {
            return "1. Press tofu with a paper towel to remove excess water, then cut into 1-inch cubes.\n\n2. Heat sesame oil in a wok or large skillet over medium-high heat. Add tofu cubes and sear until golden.\n\n3. Add mixed vegetables (broccoli, bell peppers) and stir-fry for 5 minutes.\n\n4. Drizzle low-sodium soy sauce and fresh ginger, toss well, and serve hot.";
        }
        if (lower.contains("egg") || lower.contains("scramble") || lower.contains("omelet")) {
            return "1. Crack organic eggs into a bowl, season with salt and pepper, and whisk until well beaten.\n\n2. Melt butter in a non-stick pan over medium-low heat. Add spinach and tomatoes and sauté for 2 minutes.\n\n3. Pour in beaten eggs. Stir gently with a spatula, folding the eggs over until cooked but still moist.\n\n4. Transfer to a plate and serve immediately.";
        }
        
        return "1. Prepare and wash all ingredients.\n\n2. Sauté principal ingredients in a light coating of olive oil over medium heat.\n\n3. Add preferred seasonings (sea salt, pepper, herbs) to enhance flavor profile.\n\n4. Plate nicely and serve warm.";
    }

    private String generateDynamicNotesAndTips(String name, String type, int calories, double protein, double carbs, double fat, String goal) {
        String lower = name.toLowerCase(Locale.getDefault());
        String goalClean = (goal != null) ? goal.toLowerCase() : "maintenance";
        
        String selectionReason = "";
        String goalSupport = "";
        String nutritionBenefits = "";
        String timing = "";
        String hydration = "";
        String portion = "1 standard serving";
        String alternative = "Sun-dried tomatoes or baked tofu";
        
        if (goalClean.contains("loss") || goalClean.contains("fat")) {
            selectionReason = "This meal has been selected to support your weight management by providing nutrient density with a low glycemic load.";
            goalSupport = "It helps you stay in a caloric deficit while maintaining muscle mass and boosting metabolic efficiency.";
        } else if (goalClean.contains("gain") || goalClean.contains("muscle")) {
            selectionReason = "This meal has been selected to optimize muscle protein synthesis and replenish glycogen stores.";
            goalSupport = "The balance of high-quality amino acids and complex carbs directly supports muscle repair and hypertrophic adaptations.";
        } else {
            selectionReason = "This meal has been selected to maintain steady energy levels and support baseline metabolic processes.";
            goalSupport = "It offers a balanced macronutrient ratio to sustain physical performance and general wellness.";
        }

        if (lower.contains("pancake")) {
            nutritionBenefits = "Provides a healthy mix of clean carbs and protein. Oats/almond flour supply sustained energy and dietary fiber.";
            alternative = "Whole-wheat waffles or seed-based pancakes";
        } else if (lower.contains("coffee")) {
            nutritionBenefits = "Contains natural antioxidants and caffeine to enhance focus and fat oxidation rates.";
            alternative = "Organic green tea or matcha latte";
        } else if (lower.contains("steak") || lower.contains("beef") || lower.contains("ribeye")) {
            nutritionBenefits = "Rich in iron, zinc, creatine, and high-biological-value protein for muscle tissue repair.";
            alternative = "Grilled bison steaks or ostrich medallions";
        } else if (lower.contains("chicken") || lower.contains("turkey") || lower.contains("breast")) {
            nutritionBenefits = "Lean, low-fat source of high-quality protein containing essential amino acids for fast recovery.";
            alternative = "Extra-firm baked tofu or skinless pheasant breast";
        } else if (lower.contains("salmon") || lower.contains("fish")) {
            nutritionBenefits = "Rich in Omega-3 fatty acids (EPA/DHA) which help reduce inflammation and support cardiovascular health.";
            alternative = "Atlantic mackerel or fresh sea bass";
        } else if (lower.contains("salad")) {
            nutritionBenefits = "High in vitamins A, C, and K, fiber, and essential minerals to optimize gut microbiome health.";
            alternative = "Sautéed baby kale or roasted vegetable medley";
        } else if (lower.contains("oatmeal") || lower.contains("oats")) {
            nutritionBenefits = "Rich in beta-glucan soluble fiber, which stabilizes blood glucose levels and keeps you satiated.";
            alternative = "Organic quinoa flakes or amaranth porridge";
        } else if (lower.contains("yogurt") || lower.contains("parfait")) {
            nutritionBenefits = "Contains calcium and live probiotics that support gut flora, digestion, and bone density.";
            alternative = "Fermented coconut kefir or cashew milk yogurt";
        } else if (lower.contains("toast") || lower.contains("bread") || lower.contains("sandwich")) {
            nutritionBenefits = "Complex carbohydrates from whole grains provide slow-release muscle glycogen to power workouts.";
            alternative = "Gluten-free sweet potato toasts or organic seed crackers";
        } else if (lower.contains("shake") || lower.contains("smoothie")) {
            nutritionBenefits = "Rapidly digestible protein isolate for immediate post-training muscle protein synthesis.";
            alternative = "Organic pea and hemp protein shake";
        } else if (lower.contains("tofu")) {
            nutritionBenefits = "Excellent source of plant-based protein containing all nine essential amino acids and iron.";
            alternative = "Tempeh cubes or organic edamame pods";
        } else if (lower.contains("egg") || lower.contains("scramble") || lower.contains("omelet")) {
            nutritionBenefits = "Contains choline, Vitamin D, and the highest reference quality protein score of any whole food.";
            alternative = "Scrambled organic silken tofu with nutritional yeast";
        } else {
            nutritionBenefits = "Packed with micronutrients and dietary fiber to aid metabolism and overall cellular function.";
        }

        if (type.toLowerCase().contains("breakfast") || type.toLowerCase().contains("morning")) {
            timing = "Ideal for morning consumption to break your overnight fast and sustain brain function throughout the day.";
        } else if (type.toLowerCase().contains("pre workout")) {
            timing = "Best consumed 60–90 minutes before your workout session to ensure maximum muscle glycogen availability and prevent cramping.";
        } else if (type.toLowerCase().contains("post workout")) {
            timing = "Savor this within 45 minutes of training to take advantage of the anabolic window and speed up muscle tissue repair.";
        } else if (type.toLowerCase().contains("dinner") || type.toLowerCase().contains("evening")) {
            timing = "Perfect for late afternoon or evening to promote overnight recovery and cellular repair without disrupting sleep.";
        } else {
            timing = "Best consumed as a balanced snack or midday fuel to maintain steady metabolic activity.";
        }

        hydration = "Sip 350–500 ml of filtered water alongside this meal to support optimal digestion and nutrient transport.";
        portion = String.format(Locale.getDefault(), "A portion size of approximately %d calories (%d g protein, %d g carbs, %d g fat) is targeted for your goal.", calories, (int)Math.round(protein), (int)Math.round(carbs), (int)Math.round(fat));

        return "🎯 Selection Reason:\n" + selectionReason + "\n\n" +
               "💪 Goal Support:\n" + goalSupport + "\n\n" +
               "🌟 Nutritional Benefits:\n" + nutritionBenefits + "\n\n" +
               "⏱️ Best Time to Consume:\n" + timing + "\n\n" +
               "💧 Hydration Recommendation:\n" + hydration + "\n\n" +
               "🍽️ Portion Guide:\n" + portion + "\n\n" +
               "🌿 Healthy Alternative:\n" + alternative;
    }
}

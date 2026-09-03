package com.fitness.app.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fitness.app.R;
import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.data.room.FitnessViewModel;
import com.fitness.app.data.room.LoggedMeal;
import com.fitness.app.models.DietPlan;
import com.fitness.app.models.User;
import com.fitness.app.models.ProgressLog;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class DietPlannerActivity extends AppCompatActivity {

    private TextView tvTargetCalories, tvCaloriesVal, tvProteinVal, tvCarbsVal, tvFatVal;
    private ProgressBar pbCircleProgress, pbProtein, pbCarbs, pbFat;
    private Chip chipAllMeals, chipLoggedMeals, chipFavorites;
    private RecyclerView rvMeals;
    private View llNoFavorites;
    private FloatingActionButton fabAddMeal;

    private LocalDataManager localDb;
    private FitnessViewModel viewModel;
    private DietPlan currentPlan;

    private List<DietPlan.Meal> originalMeals = new ArrayList<>();
    private List<LoggedMeal> todayLoggedMeals = new ArrayList<>();
    private MealAdapter adapter;

    // 0 = Meal Schedule, 1 = Logged Diary, 2 = Favorites
    private int showingFilterMode = 0;
    private String todayDateString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_diet_planner);

        localDb = new LocalDataManager(this);
        viewModel = new ViewModelProvider(this).get(FitnessViewModel.class);
        todayDateString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Bind Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        // Bind Views
        tvTargetCalories = findViewById(R.id.tvTargetCalories);
        tvCaloriesVal = findViewById(R.id.tvCaloriesVal);
        tvProteinVal = findViewById(R.id.tvProteinVal);
        tvCarbsVal = findViewById(R.id.tvCarbsVal);
        tvFatVal = findViewById(R.id.tvFatVal);

        pbCircleProgress = findViewById(R.id.pbCircleProgress);
        pbProtein = findViewById(R.id.pbProtein);
        pbCarbs = findViewById(R.id.pbCarbs);
        pbFat = findViewById(R.id.pbFat);

        chipAllMeals = findViewById(R.id.chipAllMeals);
        chipLoggedMeals = findViewById(R.id.chipLoggedMeals);
        chipFavorites = findViewById(R.id.chipFavorites);

        rvMeals = findViewById(R.id.rvMeals);
        llNoFavorites = findViewById(R.id.llNoFavorites);
        fabAddMeal = findViewById(R.id.fabAddMeal);

        setupData();

        adapter = new MealAdapter();
        rvMeals.setLayoutManager(new LinearLayoutManager(this));
        rvMeals.setAdapter(adapter);

        setupListeners();
        setupDatabaseObservers();
        applyFilters();

        findViewById(R.id.btnRegeneratePlan).setOnClickListener(v -> {
            generateNewDietPlan();
            setupData();
            applyFilters();
            Toast.makeText(this, "New AI Diet Plan Generated!", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupData() {
        currentPlan = localDb.getDietPlan();
        if (currentPlan == null) {
            generateNewDietPlan();
            return;
        }

        originalMeals.clear();
        originalMeals.addAll(currentPlan.getMeals());

        tvTargetCalories.setText("/ " + currentPlan.getTargetCalories() + " kcal");
        pbCircleProgress.setMax(currentPlan.getTargetCalories());

        pbProtein.setMax(currentPlan.getTargetProteinGrams());
        pbCarbs.setMax(currentPlan.getTargetCarbsGrams());
        pbFat.setMax(currentPlan.getTargetFatGrams());
    }

    private void setupDatabaseObservers() {
        viewModel.getLoggedMealsLive(todayDateString).observe(this, meals -> {
            if (meals != null) {
                todayLoggedMeals.clear();
                todayLoggedMeals.addAll(meals);

                int totalCal = 0;
                int totalProtein = 0;
                int totalCarbs = 0;
                int totalFat = 0;

                for (LoggedMeal m : todayLoggedMeals) {
                    totalCal += m.calories;
                    totalProtein += m.protein;
                    totalCarbs += m.carbs;
                    totalFat += m.fat;
                }

                // Sync with daily ProgressLog for centralized stats calculation
                ProgressLog todayLog = localDb.getProgressLog(todayDateString);
                if (todayLog == null) {
                    todayLog = new ProgressLog(todayDateString);
                }
                todayLog.setCaloriesConsumed(totalCal);
                localDb.saveProgressLog(todayLog);

                tvCaloriesVal.setText(String.valueOf(totalCal));
                pbCircleProgress.setProgress(Math.min(totalCal, currentPlan.getTargetCalories()));

                pbProtein.setProgress(Math.min(totalProtein, currentPlan.getTargetProteinGrams()));
                tvProteinVal.setText(totalProtein + "g / " + currentPlan.getTargetProteinGrams() + "g");

                pbCarbs.setProgress(Math.min(totalCarbs, currentPlan.getTargetCarbsGrams()));
                tvCarbsVal.setText(totalCarbs + "g / " + currentPlan.getTargetCarbsGrams() + "g");

                pbFat.setProgress(Math.min(totalFat, currentPlan.getTargetFatGrams()));
                tvFatVal.setText(totalFat + "g / " + currentPlan.getTargetFatGrams() + "g");

                if (showingFilterMode == 1) {
                    applyFilters();
                }
            }
        });
    }

    private void generateNewDietPlan() {
        User user = localDb.getUser();
        if (user == null) {
            user = new User();
            user.setUid("guest");
            user.setFirstName("Guest");
            user.setLastName("User");
            user.setWeight(70.0);
            user.setHeight(175.0);
            user.setAge(28);
            user.setGender("Male");
            user.setGoal("Maintain Weight");
            user.setDietaryPreference("Balanced");
            user.setDailyCaloriesGoal(2000);
            localDb.saveUser(user);
        }

        boolean cheatMealEnabled = localDb.sharedPreferences.getBoolean("cheat_meal_enabled", false);
        boolean hasWorkoutToday = false;
        try {
            java.util.List<com.fitness.app.models.WorkoutLog> workoutLogs = localDb.getAllWorkoutLogs();
            if (workoutLogs != null) {
                for (com.fitness.app.models.WorkoutLog wl : workoutLogs) {
                    if (todayDateString.equals(wl.getDate())) {
                        hasWorkoutToday = true;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        currentPlan = com.fitness.app.utils.RecommendationEngine.generateDailyDietPlan(user, "Yes", true, cheatMealEnabled, hasWorkoutToday);
        localDb.saveDietPlan(currentPlan);
        setupData();
    }

    private void setupListeners() {
        chipAllMeals.setOnClickListener(v -> {
            showingFilterMode = 0;
            chipAllMeals.setChecked(true);
            chipLoggedMeals.setChecked(false);
            chipFavorites.setChecked(false);
            applyFilters();
        });

        chipLoggedMeals.setOnClickListener(v -> {
            showingFilterMode = 1;
            chipAllMeals.setChecked(false);
            chipLoggedMeals.setChecked(true);
            chipFavorites.setChecked(false);
            applyFilters();
        });

        chipFavorites.setOnClickListener(v -> {
            showingFilterMode = 2;
            chipAllMeals.setChecked(false);
            chipLoggedMeals.setChecked(false);
            chipFavorites.setChecked(true);
            applyFilters();
        });

        fabAddMeal.setOnClickListener(v -> openCustomMealDialog());
    }

    private void openCustomMealDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_log_meal, null);
        builder.setView(dialogView);

        Spinner spMealType = dialogView.findViewById(R.id.spMealType);
        EditText etMealName = dialogView.findViewById(R.id.etMealName);
        EditText etMealCalories = dialogView.findViewById(R.id.etMealCalories);
        EditText etMealProtein = dialogView.findViewById(R.id.etMealProtein);
        EditText etMealCarbs = dialogView.findViewById(R.id.etMealCarbs);
        EditText etMealFat = dialogView.findViewById(R.id.etMealFat);

        String[] mealCategories = new String[]{"Breakfast", "Lunch", "Dinner", "Snack"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, mealCategories);
        spMealType.setAdapter(adapter);

        builder.setPositiveButton("Log Food", (dialog, which) -> {
            String name = etMealName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter food name", Toast.LENGTH_SHORT).show();
                return;
            }

            int cal = etMealCalories.getText().toString().isEmpty() ? 0 : Integer.parseInt(etMealCalories.getText().toString());
            int protein = etMealProtein.getText().toString().isEmpty() ? 0 : Integer.parseInt(etMealProtein.getText().toString());
            int carbs = etMealCarbs.getText().toString().isEmpty() ? 0 : Integer.parseInt(etMealCarbs.getText().toString());
            int fat = etMealFat.getText().toString().isEmpty() ? 0 : Integer.parseInt(etMealFat.getText().toString());

            String type = spMealType.getSelectedItem().toString();

            LoggedMeal meal = new LoggedMeal(type, name, cal, protein, carbs, fat, todayDateString, System.currentTimeMillis());
            viewModel.saveLoggedMeal(meal);
            Toast.makeText(this, name + " logged successfully!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void applyFilters() {
        llNoFavorites.setVisibility(View.GONE);
        rvMeals.setVisibility(View.VISIBLE);
        adapter.notifyDataSetChanged();

        int itemCount = adapter.getItemCount();
        if (itemCount == 0) {
            llNoFavorites.setVisibility(View.VISIBLE);
            rvMeals.setVisibility(View.GONE);
            TextView title = llNoFavorites.findViewById(R.id.tvNoFavoritesTitle);
            TextView desc = llNoFavorites.findViewById(R.id.tvNoFavoritesDesc);

            if (showingFilterMode == 0) {
                title.setText("No Meals Scheduled");
                desc.setText("Tap Regenerate to generate AI Meal plans.");
            } else if (showingFilterMode == 1) {
                title.setText("No Logged Foods");
                desc.setText("Log food diary entries today using the + button.");
            } else {
                title.setText("No Saved Favorites");
                desc.setText("Star AI meal options to save them here.");
            }
        }
    }

    private Set<String> getFavoriteMeals() {
        return getSharedPreferences("diet_prefs", Context.MODE_PRIVATE)
                .getStringSet("favorite_meals", new HashSet<>());
    }

    private void saveFavoriteMeal(String mealName, boolean isFav) {
        SharedPreferences prefs = getSharedPreferences("diet_prefs", Context.MODE_PRIVATE);
        Set<String> favs = new HashSet<>(prefs.getStringSet("favorite_meals", new HashSet<>()));
        if (isFav) {
            favs.add(mealName);
        } else {
            favs.remove(mealName);
        }
        prefs.edit().putStringSet("favorite_meals", favs).apply();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private class MealAdapter extends RecyclerView.Adapter<MealAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meal, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (showingFilterMode == 1) {
                // Logged Diary Mode
                LoggedMeal meal = todayLoggedMeals.get(position);
                holder.tvMealType.setText(meal.mealType.toUpperCase());
                holder.tvMealCalories.setText(meal.calories + " kcal");
                holder.tvMealName.setText(meal.name);
                holder.tvMealIngredients.setText(String.format(Locale.getDefault(), "Protein: %dg | Carbs: %dg | Fat: %dg", meal.protein, meal.carbs, meal.fat));
                holder.tvMealDesc.setText("Logged at " + new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(meal.timestamp)));

                holder.ivFavorite.setVisibility(View.GONE);
                holder.ivDeleteMeal.setVisibility(View.VISIBLE);
                holder.ivDeleteMeal.setOnClickListener(v -> {
                    viewModel.deleteLoggedMeal(meal);
                    Toast.makeText(DietPlannerActivity.this, "Logged food removed", Toast.LENGTH_SHORT).show();
                });

                // Stripe Color based on Meal Type
                setStripeColor(holder.vMealAccentStripe, meal.mealType);

            } else {
                // AI Suggestions or Favorites Mode
                List<DietPlan.Meal> suggestions = getFilteredSuggestions();
                DietPlan.Meal meal = suggestions.get(position);

                holder.tvMealType.setText(meal.getType().toUpperCase());
                holder.tvMealCalories.setText(meal.getCalories() + " kcal");
                holder.tvMealName.setText(meal.getName());
                holder.tvMealIngredients.setText("Ingredients: " + meal.getIngredients());
                holder.tvMealDesc.setText(meal.getDescription());

                holder.ivFavorite.setVisibility(View.VISIBLE);
                holder.ivDeleteMeal.setVisibility(View.GONE);

                Set<String> favs = getFavoriteMeals();
                boolean isFavorite = favs.contains(meal.getName());
                holder.ivFavorite.setImageResource(isFavorite ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);

                holder.ivFavorite.setOnClickListener(v -> {
                    boolean nextState = !favs.contains(meal.getName());
                    saveFavoriteMeal(meal.getName(), nextState);
                    holder.ivFavorite.setImageResource(nextState ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
                    Toast.makeText(DietPlannerActivity.this, nextState ? "Meal saved to favorites!" : "Removed from favorites", Toast.LENGTH_SHORT).show();
                    applyFilters();
                });

                // Open Recipe details on click
                holder.itemView.setOnClickListener(v -> {
                    android.content.Intent intent = new android.content.Intent(DietPlannerActivity.this, RecipeDetailActivity.class);
                    intent.putExtra("meal_name", meal.getName());
                    intent.putExtra("meal_type", meal.getType());
                    intent.putExtra("meal_calories", meal.getCalories());
                    intent.putExtra("meal_ingredients", meal.getIngredients());
                    intent.putExtra("meal_instructions", meal.getDescription());
                    startActivity(intent);
                });

                setStripeColor(holder.vMealAccentStripe, meal.getType());
            }
        }

        private void setStripeColor(View v, String category) {
            if (category == null) return;
            switch (category.toLowerCase()) {
                case "breakfast":
                    v.setBackgroundColor(0xFF4CC9F0); // Secondary
                    break;
                case "lunch":
                    v.setBackgroundColor(0xFF6C63FF); // Primary
                    break;
                case "dinner":
                    v.setBackgroundColor(0xFF00C2FF); // Accent
                    break;
                default:
                    v.setBackgroundColor(0xFFFFB703); // Snack/Yellow
                    break;
            }
        }

        private List<DietPlan.Meal> getFilteredSuggestions() {
            List<DietPlan.Meal> list = new ArrayList<>();
            Set<String> favs = getFavoriteMeals();
            for (DietPlan.Meal m : originalMeals) {
                if (showingFilterMode == 0 || (showingFilterMode == 2 && favs.contains(m.getName()))) {
                    list.add(m);
                }
            }
            return list;
        }

        @Override
        public int getItemCount() {
            if (showingFilterMode == 1) {
                return todayLoggedMeals.size();
            } else {
                return getFilteredSuggestions().size();
            }
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvMealType, tvMealCalories, tvMealName, tvMealIngredients, tvMealDesc;
            ImageView ivFavorite, ivDeleteMeal;
            View vMealAccentStripe;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvMealType = itemView.findViewById(R.id.tvMealType);
                tvMealCalories = itemView.findViewById(R.id.tvMealCalories);
                tvMealName = itemView.findViewById(R.id.tvMealName);
                tvMealIngredients = itemView.findViewById(R.id.tvMealIngredients);
                tvMealDesc = itemView.findViewById(R.id.tvMealDesc);
                ivFavorite = itemView.findViewById(R.id.ivMealFavorite);
                ivDeleteMeal = itemView.findViewById(R.id.ivDeleteMeal);
                vMealAccentStripe = itemView.findViewById(R.id.vMealAccentStripe);
            }
        }
    }
}

package com.fitness.app.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fitness.app.R;
import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.models.DietPlan;
import com.fitness.app.models.User;
import com.fitness.app.utils.GroceryAiEngine;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class DietFragment extends Fragment {

    private TextView tvTargetCalories, tvProteinVal, tvCarbsVal, tvFatVal;
    private Chip chipAllMeals, chipFavorites;
    private RecyclerView rvDietMeals;
    private View llNoFavorites;
    private ProgressBar pbDietLoading;

    private LocalDataManager localDb;
    private final List<DietPlan.Meal> originalMeals = new ArrayList<>();
    private final List<DietPlan.Meal> displayedMeals = new ArrayList<>();
    private MealAdapter adapter;
    private boolean showingFavoritesOnly = false;
    private String lastUserGoal = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diet, container, false);

        localDb = new LocalDataManager(requireContext());

        tvTargetCalories = view.findViewById(R.id.tvTargetCalories);
        tvProteinVal = view.findViewById(R.id.tvProteinVal);
        tvCarbsVal = view.findViewById(R.id.tvCarbsVal);
        tvFatVal = view.findViewById(R.id.tvFatVal);

        chipAllMeals = view.findViewById(R.id.chipAllMeals);
        chipFavorites = view.findViewById(R.id.chipFavorites);
        rvDietMeals = view.findViewById(R.id.rvDietMeals);
        llNoFavorites = view.findViewById(R.id.llNoFavorites);
        pbDietLoading = view.findViewById(R.id.pbDietLoading);

        // Bind new UX elements
        View cardDietTip = view.findViewById(R.id.cardDietTip);
        View ivDismissDietTip = view.findViewById(R.id.ivDismissDietTip);
        View btnShowSuggestions = view.findViewById(R.id.btnShowSuggestions);

        // Tip banner logic
        boolean showDietTip = localDb.sharedPreferences.getBoolean("show_diet_tip", true);
        if (showDietTip) {
            cardDietTip.setVisibility(View.VISIBLE);
        } else {
            cardDietTip.setVisibility(View.GONE);
        }

        ivDismissDietTip.setOnClickListener(v -> {
            localDb.sharedPreferences.edit().putBoolean("show_diet_tip", false).apply();
            cardDietTip.setVisibility(View.GONE);
        });

        // Show suggestions action inside empty state
        btnShowSuggestions.setOnClickListener(v -> {
            showingFavoritesOnly = false;
            chipAllMeals.setChecked(true);
            chipFavorites.setChecked(false);
            applyFilters();
        });

        adapter = new MealAdapter(displayedMeals);
        rvDietMeals.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvDietMeals.setAdapter(adapter);

        setupListeners();
        
        // Initial data fetch
        fetchPersonalizedDietData();

        // Check Schedule Banner Action
        view.findViewById(R.id.btnCheckSchedule).setOnClickListener(v -> {
            startActivity(new android.content.Intent(getActivity(), com.fitness.app.activities.MealScheduleActivity.class));
        });

        // Open Smart Meal Planner / Grocery Scanner
        view.findViewById(R.id.btnOpenMealPlanner).setOnClickListener(v -> {
            startActivity(new android.content.Intent(getActivity(), com.fitness.app.activities.GroceryScannerActivity.class));
        });

        // More toolbar action -> Settings activity
        view.findViewById(R.id.ivDietMore).setOnClickListener(v -> {
            startActivity(new android.content.Intent(getActivity(), com.fitness.app.activities.SettingsActivity.class));
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Automatically check if user goals have updated and refresh lists
        User user = localDb.getUser();
        if (user != null && !user.getGoal().equalsIgnoreCase(lastUserGoal)) {
            fetchPersonalizedDietData();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            User user = localDb.getUser();
            if (user != null && !user.getGoal().equalsIgnoreCase(lastUserGoal)) {
                fetchPersonalizedDietData();
            }
        }
    }

    private void fetchPersonalizedDietData() {
        User user = localDb.getUser();
        if (user == null) return;
        
        lastUserGoal = user.getGoal();
        pbDietLoading.setVisibility(View.VISIBLE);
        rvDietMeals.setVisibility(View.GONE);
        llNoFavorites.setVisibility(View.GONE);

        GroceryAiEngine.generatePersonalizedMeals(requireContext(), user, new GroceryAiEngine.PersonalizedMealsCallback() {
            @Override
            public void onSuccess(List<DietPlan.Meal> meals, int targetCal, int targetProt, int targetCarbs, int targetFat) {
                if (!isAdded()) return;
                pbDietLoading.setVisibility(View.GONE);
                
                originalMeals.clear();
                originalMeals.addAll(meals);
                
                // Save diet plan targets to local shared preferences database
                DietPlan plan = new DietPlan(
                    java.util.UUID.randomUUID().toString(),
                    targetCal,
                    targetProt,
                    targetCarbs,
                    targetFat,
                    user.getDailyWaterGoal() > 0 ? user.getDailyWaterGoal() : 2500,
                    "AI Personalized",
                    meals,
                    System.currentTimeMillis()
                );
                localDb.saveDietPlan(plan);
                
                tvTargetCalories.setText(String.format(Locale.getDefault(), "Daily Target: %d kcal", targetCal));
                tvProteinVal.setText(targetProt + "g");
                tvCarbsVal.setText(targetCarbs + "g");
                tvFatVal.setText(targetFat + "g");
                
                applyFilters();
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                pbDietLoading.setVisibility(View.GONE);
                Toast.makeText(getActivity(), "Error loading meals: " + error, Toast.LENGTH_SHORT).show();
                applyFilters();
            }
        });
    }

    private void setupListeners() {
        chipAllMeals.setOnClickListener(v -> {
            showingFavoritesOnly = false;
            chipAllMeals.setChecked(true);
            chipFavorites.setChecked(false);
            applyFilters();
        });

        chipFavorites.setOnClickListener(v -> {
            showingFavoritesOnly = true;
            chipAllMeals.setChecked(false);
            chipFavorites.setChecked(true);
            applyFilters();
        });
    }

    private void applyFilters() {
        displayedMeals.clear();
        Set<String> favs = getFavoriteMeals();

        for (DietPlan.Meal meal : originalMeals) {
            if (!showingFavoritesOnly || favs.contains(meal.getName())) {
                displayedMeals.add(meal);
            }
        }

        if (displayedMeals.isEmpty()) {
            llNoFavorites.setVisibility(View.VISIBLE);
            rvDietMeals.setVisibility(View.GONE);
            if (showingFavoritesOnly) {
                ((TextView) llNoFavorites.findViewById(R.id.tvNoFavoritesTitle)).setText("No Saved Favorites");
            } else {
                ((TextView) llNoFavorites.findViewById(R.id.tvNoFavoritesTitle)).setText("No Meal Suggestions");
            }
        } else {
            llNoFavorites.setVisibility(View.GONE);
            rvDietMeals.setVisibility(View.VISIBLE);
        }

        adapter.notifyDataSetChanged();
    }

    private Set<String> getFavoriteMeals() {
        return requireContext().getSharedPreferences("diet_prefs", Context.MODE_PRIVATE)
                .getStringSet("favorite_meals", new HashSet<>());
    }

    private void saveFavoriteMeal(String mealName, boolean isFav) {
        SharedPreferences prefs = requireContext().getSharedPreferences("diet_prefs", Context.MODE_PRIVATE);
        Set<String> favs = new HashSet<>(prefs.getStringSet("favorite_meals", new HashSet<>()));
        if (isFav) {
            favs.add(mealName);
        } else {
            favs.remove(mealName);
        }
        prefs.edit().putStringSet("favorite_meals", favs).apply();
    }

    private class MealAdapter extends RecyclerView.Adapter<MealAdapter.ViewHolder> {
        private final List<DietPlan.Meal> list;

        public MealAdapter(List<DietPlan.Meal> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meal, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DietPlan.Meal meal = list.get(position);
            holder.tvMealType.setText(meal.getType().toUpperCase());
            holder.tvMealCalories.setText(meal.getCalories() + " kcal");
            holder.tvMealName.setText(meal.getName());
            holder.tvMealIngredients.setText("Ingredients: " + meal.getIngredients());
            holder.tvMealDesc.setText(meal.getDescription());

            // Compute and bind dynamic macros split
            int cal = meal.getCalories();
            int prot = (int) (cal * 0.25 / 4);
            int carbs = (int) (cal * 0.50 / 4);
            int fat = (int) (cal * 0.25 / 9);

            holder.tvMealProt.setText("P: " + prot + "g");
            holder.tvMealCarbs.setText("C: " + carbs + "g");
            holder.tvMealFat.setText("F: " + fat + "g");

            // Set dynamic AI recommendation badge
            User user = localDb.getUser();
            String goal = user != null ? user.getGoal() : "Fitness";
            if ("Lose Fat".equalsIgnoreCase(goal)) {
                holder.tvMealAiRec.setText("★ Fat Burner");
            } else if ("Gain Muscle".equalsIgnoreCase(goal)) {
                holder.tvMealAiRec.setText("★ High Protein");
            } else {
                holder.tvMealAiRec.setText("★ Lean & Fit");
            }

            // Bind illustrations
            String type = meal.getType().toLowerCase(Locale.getDefault());
            if (type.contains("break")) {
                holder.ivMealImage.setImageResource(R.drawable.onboarding_1);
            } else if (type.contains("lunch")) {
                holder.ivMealImage.setImageResource(R.drawable.onboarding_2);
            } else if (type.contains("snack")) {
                holder.ivMealImage.setImageResource(R.drawable.onboarding_3);
            } else {
                holder.ivMealImage.setImageResource(R.drawable.onboarding_4);
            }

            // Action triggers (item click AND View Full Recipe button click)
            View.OnClickListener clickAction = v -> {
                android.content.Intent intent = new android.content.Intent(getActivity(), com.fitness.app.activities.RecipeDetailActivity.class);
                intent.putExtra("meal_name", meal.getName());
                intent.putExtra("meal_type", meal.getType());
                intent.putExtra("meal_calories", meal.getCalories());
                intent.putExtra("meal_ingredients", meal.getIngredients());
                intent.putExtra("meal_instructions", meal.getDescription());
                startActivity(intent);
            };

            holder.itemView.setOnClickListener(clickAction);
            holder.btnViewFullRecipe.setOnClickListener(clickAction);

            Set<String> favs = getFavoriteMeals();
            boolean isFavorite = favs.contains(meal.getName());
            holder.ivFavorite.setImageResource(isFavorite ? R.drawable.ic_heart_fav_filled : R.drawable.ic_heart_fav);

            holder.ivFavorite.setOnClickListener(v -> {
                boolean nextState = !favs.contains(meal.getName());
                saveFavoriteMeal(meal.getName(), nextState);
                holder.ivFavorite.setImageResource(nextState ? R.drawable.ic_heart_fav_filled : R.drawable.ic_heart_fav);
                Toast.makeText(getActivity(), nextState ? "Meal saved to favorites!" : "Removed from favorites", Toast.LENGTH_SHORT).show();
                if (showingFavoritesOnly) {
                    applyFilters();
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvMealType, tvMealCalories, tvMealName, tvMealIngredients, tvMealDesc;
            TextView tvMealProt, tvMealCarbs, tvMealFat, tvMealAiRec, btnViewFullRecipe;
            ImageView ivFavorite, ivMealImage;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvMealType = itemView.findViewById(R.id.tvMealType);
                tvMealCalories = itemView.findViewById(R.id.tvMealCalories);
                tvMealName = itemView.findViewById(R.id.tvMealName);
                tvMealIngredients = itemView.findViewById(R.id.tvMealIngredients);
                tvMealDesc = itemView.findViewById(R.id.tvMealDesc);
                ivFavorite = itemView.findViewById(R.id.ivMealFavorite);
                ivMealImage = itemView.findViewById(R.id.ivMealImage);

                tvMealProt = itemView.findViewById(R.id.tvMealProt);
                tvMealCarbs = itemView.findViewById(R.id.tvMealCarbs);
                tvMealFat = itemView.findViewById(R.id.tvMealFat);
                tvMealAiRec = itemView.findViewById(R.id.tvMealAiRec);
                btnViewFullRecipe = itemView.findViewById(R.id.btnViewFullRecipe);
            }
        }
    }
}

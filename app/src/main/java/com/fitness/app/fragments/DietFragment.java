package com.fitness.app.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
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
import com.fitness.app.data.room.AppDatabase;
import com.fitness.app.data.room.FitnessDao;
import com.fitness.app.data.room.LoggedMeal;
import com.fitness.app.models.DietPlan;
import com.fitness.app.models.User;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

public class DietFragment extends Fragment {

    private TextView tvTargetCalories, tvProteinVal, tvCarbsVal, tvFatVal;

    private LocalDataManager localDb;

    // Daily Meal Schedule Main Views
    private View llExpandedSchedule;
    private TextView tvCalendarMonth;
    private ImageButton btnScheduleMore;
    private com.google.android.material.button.MaterialButton btnAddMealSlot;
    private RecyclerView rvScheduleList;

    // Category Filter Chips
    private Chip chipFilterAll, chipFilterBreakfast, chipFilterLunch, chipFilterDinner, chipFilterSnacks;
    private String selectedCategoryFilter = "ALL";

    // Calendar Days Views
    private final View[] dayContainers = new View[7];
    private final TextView[] tvDays = new TextView[7];
    private final TextView[] tvDates = new TextView[7];

    // Today Meal Nutritions views
    private ProgressBar pbKcalProgress;
    private TextView tvConsumedVal, tvProteinsVal, tvFatsVal, tvCarbsValSummary;

    // Database fields
    private FitnessDao fitnessDao;
    private String selectedDate; // yyyy-MM-dd
    private final List<ScheduledMealItem> scheduleList = new ArrayList<>();
    private List<LoggedMeal> dbMeals = new ArrayList<>();
    private ScheduleAdapter scheduleAdapter;
    private DietPlan dietPlan;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diet, container, false);

        localDb = new LocalDataManager(requireContext());

        tvTargetCalories = view.findViewById(R.id.tvTargetCalories);
        tvProteinVal = view.findViewById(R.id.tvProteinVal);
        tvCarbsVal = view.findViewById(R.id.tvCarbsVal);
        tvFatVal = view.findViewById(R.id.tvFatVal);

        // Initialize schedule database and date
        fitnessDao = AppDatabase.getInstance(requireContext()).fitnessDao();
        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        dietPlan = localDb.getDietPlan();

        // Bind expanded schedule views
        llExpandedSchedule = view.findViewById(R.id.llExpandedSchedule);
        tvCalendarMonth = view.findViewById(R.id.tvCalendarMonth);
        btnScheduleMore = view.findViewById(R.id.btnScheduleMore);
        btnAddMealSlot = view.findViewById(R.id.btnAddMealSlot);
        rvScheduleList = view.findViewById(R.id.rvScheduleList);

        if (llExpandedSchedule != null) {
            llExpandedSchedule.setVisibility(View.VISIBLE);
        }

        // Bind category filter chips
        chipFilterAll = view.findViewById(R.id.chipFilterAll);
        chipFilterBreakfast = view.findViewById(R.id.chipFilterBreakfast);
        chipFilterLunch = view.findViewById(R.id.chipFilterLunch);
        chipFilterDinner = view.findViewById(R.id.chipFilterDinner);
        chipFilterSnacks = view.findViewById(R.id.chipFilterSnacks);

        setupFilterChips();

        // Bind calendar day containers
        for (int i = 0; i < 7; i++) {
            int containerId = getResources().getIdentifier("dayContainer" + (i + 1), "id", requireContext().getPackageName());
            int dayId = getResources().getIdentifier("tvDay" + (i + 1), "id", requireContext().getPackageName());
            int dateId = getResources().getIdentifier("tvDate" + (i + 1), "id", requireContext().getPackageName());

            dayContainers[i] = view.findViewById(containerId);
            tvDays[i] = view.findViewById(dayId);
            tvDates[i] = view.findViewById(dateId);
        }

        // Bind progress bars and text views
        pbKcalProgress = view.findViewById(R.id.pbKcalProgress);
        tvConsumedVal = view.findViewById(R.id.tvConsumedVal);
        tvProteinsVal = view.findViewById(R.id.tvProteinsVal);
        tvFatsVal = view.findViewById(R.id.tvFatsVal);
        tvCarbsValSummary = view.findViewById(R.id.tvCarbsValSummary);

        // Setup schedule list recycler view
        if (rvScheduleList != null) {
            rvScheduleList.setLayoutManager(new LinearLayoutManager(requireContext()));
            scheduleAdapter = new ScheduleAdapter();
            rvScheduleList.setAdapter(scheduleAdapter);
        }

        // Listeners for schedule actions
        if (btnScheduleMore != null) {
            btnScheduleMore.setOnClickListener(this::showMoreMenu);
        }

        if (btnAddMealSlot != null) {
            btnAddMealSlot.setOnClickListener(v -> {
                v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(80).withEndAction(() -> {
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(80).start();
                    Intent intent = new Intent(getActivity(), com.fitness.app.activities.AddMealActivity.class);
                    intent.putExtra("selected_date", selectedDate);
                    startActivityForResult(intent, 100);
                }).start();
            });
        }

        // More toolbar action -> Settings activity
        View ivDietMore = view.findViewById(R.id.ivDietMore);
        if (ivDietMore != null) {
            ivDietMore.setOnClickListener(this::showMoreMenu);
        }

        loadMealsForDate();
        setupCalendarDays();
        updateKcalProgress();

        return view;
    }

    private void setupFilterChips() {
        if (chipFilterAll == null) return;

        View.OnClickListener chipListener = v -> {
            chipFilterAll.setChecked(v.getId() == R.id.chipFilterAll);
            if (chipFilterBreakfast != null) chipFilterBreakfast.setChecked(v.getId() == R.id.chipFilterBreakfast);
            if (chipFilterLunch != null) chipFilterLunch.setChecked(v.getId() == R.id.chipFilterLunch);
            if (chipFilterDinner != null) chipFilterDinner.setChecked(v.getId() == R.id.chipFilterDinner);
            if (chipFilterSnacks != null) chipFilterSnacks.setChecked(v.getId() == R.id.chipFilterSnacks);

            if (v.getId() == R.id.chipFilterBreakfast) selectedCategoryFilter = "Breakfast";
            else if (v.getId() == R.id.chipFilterLunch) selectedCategoryFilter = "Lunch";
            else if (v.getId() == R.id.chipFilterDinner) selectedCategoryFilter = "Dinner";
            else if (v.getId() == R.id.chipFilterSnacks) selectedCategoryFilter = "Snacks";
            else selectedCategoryFilter = "ALL";

            loadMealsForDate();
            if (scheduleAdapter != null) {
                scheduleAdapter.notifyDataSetChanged();
            }
        };

        chipFilterAll.setOnClickListener(chipListener);
        if (chipFilterBreakfast != null) chipFilterBreakfast.setOnClickListener(chipListener);
        if (chipFilterLunch != null) chipFilterLunch.setOnClickListener(chipListener);
        if (chipFilterDinner != null) chipFilterDinner.setOnClickListener(chipListener);
        if (chipFilterSnacks != null) chipFilterSnacks.setOnClickListener(chipListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean needsRegen = localDb.sharedPreferences.getBoolean("plan_needs_regeneration", false);
        if (needsRegen) {
            localDb.sharedPreferences.edit().putBoolean("plan_needs_regeneration", false).apply();
            showLoading("Updating meal plan...");
            new Thread(() -> {
                try {
                    fitnessDao.deleteUncompletedLoggedMealsForDate(selectedDate);
                    prepopulatePresetMeals();
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            hideLoading();
                            loadMealsForDate();
                            if (scheduleAdapter != null) {
                                scheduleAdapter.notifyDataSetChanged();
                            }
                            updateKcalProgress();
                            setupCalendarDays();
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(this::hideLoading);
                    }
                }
            }).start();
        } else {
            loadMealsForDate();
            if (scheduleAdapter != null) {
                scheduleAdapter.notifyDataSetChanged();
            }
            updateKcalProgress();
            setupCalendarDays();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == android.app.Activity.RESULT_OK) {
            loadMealsForDate();
            if (scheduleAdapter != null) {
                scheduleAdapter.notifyDataSetChanged();
            }
            updateKcalProgress();
            setupCalendarDays();
        }
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

    public static class ScheduledMealItem {
        public int id;
        public boolean isHeader;
        public String category;
        public String title;
        public String time;
        public int calories;
        public boolean checked;
        public String countLabel;
        public int iconResId;
        public int protein;
        public int carbs;
        public int fat;
        public String ingredients;
        public String description;

        public ScheduledMealItem(int id, String category, String title, String time, int calories, boolean checked, int iconResId, int protein, int carbs, int fat, String ingredients, String description) {
            this.id = id;
            this.isHeader = false;
            this.category = category;
            this.title = title;
            this.time = time;
            this.calories = calories;
            this.checked = checked;
            this.iconResId = iconResId;
            this.protein = protein;
            this.carbs = carbs;
            this.fat = fat;
            this.ingredients = ingredients;
            this.description = description;
        }

        public ScheduledMealItem(String category, String countLabel) {
            this.id = -1;
            this.isHeader = true;
            this.category = category;
            this.countLabel = countLabel;
        }
    }

    private void setupCalendarDays() {
        if (!isAdded()) return;
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat ymdFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date d = ymdFmt.parse(selectedDate);
            if (d != null) {
                cal.setTime(d);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int daysToSubtract = (dayOfWeek == Calendar.SUNDAY) ? -6 : (Calendar.MONDAY - dayOfWeek);
        cal.add(Calendar.DAY_OF_YEAR, daysToSubtract);

        SimpleDateFormat dayNameFmt = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat dateNumFmt = new SimpleDateFormat("d", Locale.getDefault());
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());

        for (int i = 0; i < 7; i++) {
            if (tvDays[i] != null) tvDays[i].setText(dayNameFmt.format(cal.getTime()).substring(0, 1).toUpperCase());
            if (tvDates[i] != null) tvDates[i].setText(dateNumFmt.format(cal.getTime()));

            final String clickedDate = ymdFmt.format(cal.getTime());

            if (dayContainers[i] != null) {
                if (clickedDate.equals(selectedDate)) {
                    android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
                    shape.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
                    shape.setCornerRadius(24f);
                    shape.setColor(0xFF2563EB); // Vibrant Primary Blue
                    dayContainers[i].setBackground(shape);
                    if (tvDays[i] != null) tvDays[i].setTextColor(0xFFFFFFFF);
                    if (tvDates[i] != null) tvDates[i].setTextColor(0xFFFFFFFF);
                    if (tvCalendarMonth != null) tvCalendarMonth.setText(monthFormat.format(cal.getTime()));
                } else {
                    android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
                    shape.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
                    shape.setCornerRadius(24f);
                    shape.setColor(0x00FFFFFF);
                    shape.setStroke(2, 0xFFBFDBFE);
                    dayContainers[i].setBackground(shape);
                    if (tvDays[i] != null) tvDays[i].setTextColor(0xFF64748B);
                    if (tvDates[i] != null) tvDates[i].setTextColor(0xFF1E293B);
                }

                dayContainers[i].setOnClickListener(v -> {
                    selectedDate = clickedDate;
                    setupCalendarDays();
                    loadMealsForDate();
                    if (scheduleAdapter != null) {
                        scheduleAdapter.notifyDataSetChanged();
                    }
                    updateKcalProgress();
                });
            }

            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    private void loadMealsForDate() {
        dbMeals.clear();
        try {
            dbMeals = fitnessDao.getLoggedMealsForDate(selectedDate);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (dbMeals == null || dbMeals.isEmpty()) {
            prepopulatePresetMeals();
            try {
                dbMeals = fitnessDao.getLoggedMealsForDate(selectedDate);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        scheduleList.clear();

        if (dbMeals != null && !dbMeals.isEmpty()) {
            java.util.Collections.sort(dbMeals, (m1, m2) -> {
                int t1 = parseTimeToMinutes(m1.mealTime);
                int t2 = parseTimeToMinutes(m2.mealTime);
                return Integer.compare(t1, t2);
            });

            String lastCategory = null;
            List<LoggedMeal> currentCategoryMeals = new ArrayList<>();

            for (LoggedMeal m : dbMeals) {
                String category = getDynamicCategoryForMeal(m);

                // Apply quick filter check
                if (!matchesCategoryFilter(category)) {
                    continue;
                }

                if (lastCategory == null) {
                    lastCategory = category;
                    currentCategoryMeals.add(m);
                } else if (category.equalsIgnoreCase(lastCategory)) {
                    currentCategoryMeals.add(m);
                } else {
                    emitCategoryGroup(lastCategory, currentCategoryMeals);
                    currentCategoryMeals = new ArrayList<>();
                    lastCategory = category;
                    currentCategoryMeals.add(m);
                }
            }
            if (lastCategory != null && !currentCategoryMeals.isEmpty()) {
                emitCategoryGroup(lastCategory, currentCategoryMeals);
            }
        }
    }

    private boolean matchesCategoryFilter(String category) {
        if ("ALL".equalsIgnoreCase(selectedCategoryFilter)) return true;
        if (category == null) return false;

        String catLower = category.toLowerCase(Locale.getDefault());
        String filterLower = selectedCategoryFilter.toLowerCase(Locale.getDefault());

        if (filterLower.equals("breakfast")) return catLower.contains("break");
        if (filterLower.equals("lunch")) return catLower.contains("lunch");
        if (filterLower.equals("dinner")) return catLower.contains("dinner");
        if (filterLower.equals("snacks")) return catLower.contains("snack") || catLower.contains("drink");

        return catLower.contains(filterLower);
    }

    private void prepopulatePresetMeals() {
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
                    if (selectedDate.equals(wl.getDate())) {
                        hasWorkoutToday = true;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        DietPlan generatedPlan = com.fitness.app.utils.RecommendationEngine.generateDailyDietPlan(user, "Yes", true, cheatMealEnabled, hasWorkoutToday, selectedDate);
        if (generatedPlan != null) {
            localDb.saveDietPlan(generatedPlan);

            java.util.List<LoggedMeal> existing = fitnessDao.getLoggedMealsForDate(selectedDate);
            java.util.Set<String> completedTypes = new java.util.HashSet<>();
            if (existing != null) {
                for (LoggedMeal em : existing) {
                    if (em.isChecked && em.mealType != null) {
                        completedTypes.add(em.mealType.toLowerCase());
                    }
                }
            }

            List<DietPlan.Meal> planMeals = generatedPlan.getMeals();
            int index = 0;
            for (DietPlan.Meal m : planMeals) {
                if (m.getType() != null && completedTypes.contains(m.getType().toLowerCase())) {
                    continue;
                }
                String mealType = m.getType();

                String time = "08:00 AM";
                if ("Breakfast".equalsIgnoreCase(m.getType())) time = "07:00 AM";
                else if ("Morning Drink".equalsIgnoreCase(m.getType())) time = "08:00 AM";
                else if ("Morning Snack".equalsIgnoreCase(m.getType())) time = "10:30 AM";
                else if ("Lunch".equalsIgnoreCase(m.getType())) time = "01:00 PM";
                else if ("Afternoon Drink".equalsIgnoreCase(m.getType())) time = "03:30 PM";
                else if ("Afternoon Snack".equalsIgnoreCase(m.getType())) time = "04:30 PM";
                else if ("Dinner".equalsIgnoreCase(m.getType()) || "Cheat Meal".equalsIgnoreCase(m.getType())) time = "07:30 PM";
                else if ("Pre Workout Meal".equalsIgnoreCase(m.getType())) time = "11:30 AM";
                else if ("Post Workout Meal".equalsIgnoreCase(m.getType())) time = "03:00 PM";
                else if ("Evening Snack".equalsIgnoreCase(m.getType())) time = "04:30 PM";

                int iconId = findEmojiIndexByName(m.getName());
                if (iconId == -1) iconId = 0;

                String notes = com.fitness.app.activities.MealScheduleActivity.generateIntelligentNotes(m.getType(), user.getGoal(), m.getCalories(), m.getProtein(), m.getCarbs(), m.getFat());

                LoggedMeal logged = new LoggedMeal(
                        mealType,
                        m.getName(),
                        m.getCalories(),
                        (int) Math.round(m.getProtein()),
                        (int) Math.round(m.getCarbs()),
                        (int) Math.round(m.getFat()),
                        selectedDate,
                        System.currentTimeMillis() + (index * 60000),
                        time,
                        notes,
                        false,
                        iconId,
                        (int) Math.round(m.getFiber()),
                        0,
                        m.getServingSize() != null && !m.getServingSize().isEmpty() ? m.getServingSize() : "1 serving",
                        m.getIngredients(),
                        m.getDescription()
                );

                fitnessDao.insertLoggedMeal(logged);
                index++;
            }
        }
    }

    private void updateKcalProgress() {
        if (!isAdded()) return;
        int consumed = 0;
        int target = 2000;
        int targetProtein = 140;
        int targetCarbs = 220;
        int targetFat = 65;

        User user = localDb.getUser();
        if (user != null) {
            target = user.getDailyCaloriesGoal() > 0 ? user.getDailyCaloriesGoal() : 2000;
        }

        int totalProtein = 0;
        int totalCarbs = 0;
        int totalFat = 0;

        if (dbMeals != null) {
            for (LoggedMeal m : dbMeals) {
                if (m.isChecked) {
                    consumed += m.calories;
                    totalProtein += m.protein;
                    totalCarbs += m.carbs;
                    totalFat += m.fat;
                }
            }
        }

        if (tvTargetCalories != null) {
            tvTargetCalories.setText(String.format(Locale.getDefault(), "Daily Target: %,d kcal", target));
        }

        if (tvConsumedVal != null) {
            tvConsumedVal.setText(String.format(Locale.getDefault(), "%,d / %,d kcal", consumed, target));
        }

        if (pbKcalProgress != null) {
            pbKcalProgress.setMax(target);
            pbKcalProgress.setProgress(Math.min(consumed, target));
        }

        if (tvProteinVal != null) tvProteinVal.setText(targetProtein + "g");
        if (tvProteinsVal != null) tvProteinsVal.setText(totalProtein + " / " + targetProtein + "g");

        if (tvCarbsVal != null) tvCarbsVal.setText(targetCarbs + "g");
        if (tvCarbsValSummary != null) tvCarbsValSummary.setText(totalCarbs + " / " + targetCarbs + "g");

        if (tvFatVal != null) tvFatVal.setText(targetFat + "g");
        if (tvFatsVal != null) tvFatsVal.setText(totalFat + " / " + targetFat + "g");
    }

    private void showMoreMenu(View anchor) {
        if (!isAdded()) return;
        androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(requireContext(), anchor);
        popup.getMenu().add(0, 1, 0, "Add Meal");
        popup.getMenu().add(0, 2, 1, "Edit Schedule Goals");
        popup.getMenu().add(0, 3, 2, "Regenerate Meal Plan");
        popup.getMenu().add(0, 4, 3, "Copy Schedule to Another Day");
        popup.getMenu().add(0, 5, 4, "View Nutrition Summary");
        popup.getMenu().add(0, 6, 5, "Export Meal Plan");
        popup.getMenu().add(0, 7, 6, "Refresh Data");
        popup.getMenu().add(0, 8, 7, "Settings");

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    Intent addIntent = new Intent(getActivity(), com.fitness.app.activities.AddMealActivity.class);
                    addIntent.putExtra("selected_date", selectedDate);
                    startActivityForResult(addIntent, 100);
                    return true;
                case 2:
                    showEditScheduleDialog();
                    return true;
                case 3:
                    showRegenerateMealPlanConfirmDialog();
                    return true;
                case 4:
                    showCopyScheduleDatePicker();
                    return true;
                case 5:
                    Intent summaryIntent = new Intent(getActivity(), com.fitness.app.activities.NutritionSummaryActivity.class);
                    summaryIntent.putExtra("selected_date", selectedDate);
                    startActivity(summaryIntent);
                    return true;
                case 6:
                    exportMealPlan();
                    return true;
                case 7:
                    refreshData();
                    return true;
                case 8:
                    startActivity(new Intent(getActivity(), com.fitness.app.activities.SettingsActivity.class));
                    return true;
            }
            return false;
        });
        popup.show();
    }

    private void showEditScheduleDialog() {
        int currentKcal = dietPlan != null ? dietPlan.getTargetCalories() : 2000;

        android.widget.LinearLayout layout = new android.widget.LinearLayout(requireContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 40);

        final android.widget.EditText etKcal = new android.widget.EditText(requireContext());
        etKcal.setHint("Calorie Goal (kcal)");
        etKcal.setText(String.valueOf(currentKcal));
        etKcal.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(etKcal);

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Edit Calorie Goal")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String kcalStr = etKcal.getText().toString().trim();
                    if (!kcalStr.isEmpty()) {
                        int k = Integer.parseInt(kcalStr);
                        User user = localDb.getUser();
                        if (user != null) {
                            user.setDailyCaloriesGoal(k);
                            localDb.saveUser(user);
                        }
                        updateKcalProgress();
                        Toast.makeText(requireContext(), "Target updated!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showRegenerateMealPlanConfirmDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Regenerate Meal Plan")
                .setMessage("Are you sure you want to regenerate your meal plan for today?")
                .setPositiveButton("Regenerate", (dialog, which) -> regenerateMealPlan())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void regenerateMealPlan() {
        showLoading("Generating personalized meals...");
        new Thread(() -> {
            try {
                fitnessDao.deleteLoggedMealsForDate(selectedDate);
                prepopulatePresetMeals();
                dbMeals = fitnessDao.getLoggedMealsForDate(selectedDate);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        hideLoading();
                        loadMealsForDate();
                        if (scheduleAdapter != null) {
                            scheduleAdapter.notifyDataSetChanged();
                        }
                        updateKcalProgress();
                        Toast.makeText(requireContext(), "New meal plan generated! 🍽️", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        hideLoading();
                        Toast.makeText(requireContext(), "Failed to regenerate meal plan.", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        }).start();
    }

    private void showCopyScheduleDatePicker() {
        com.google.android.material.datepicker.MaterialDatePicker<Long> datePicker =
                com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Select Date to Copy To")
                        .setSelection(com.google.android.material.datepicker.MaterialDatePicker.todayInUtcMilliseconds())
                        .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            String targetDate = sdf.format(new Date(selection));

            if (targetDate.equals(selectedDate)) {
                Toast.makeText(requireContext(), "Cannot copy schedule to the same day!", Toast.LENGTH_SHORT).show();
                return;
            }

            copyScheduleToDate(targetDate);
        });

        datePicker.show(getParentFragmentManager(), "COPY_DATE_PICKER");
    }

    private void copyScheduleToDate(String targetDate) {
        showLoading("Copying meal schedule...");
        new Thread(() -> {
            try {
                List<LoggedMeal> currentMeals = fitnessDao.getLoggedMealsForDate(selectedDate);
                if (currentMeals != null && !currentMeals.isEmpty()) {
                    fitnessDao.deleteLoggedMealsForDate(targetDate);
                    for (LoggedMeal m : currentMeals) {
                        LoggedMeal copy = new LoggedMeal(
                                m.mealType,
                                m.name,
                                m.calories,
                                m.protein,
                                m.carbs,
                                m.fat,
                                targetDate,
                                System.currentTimeMillis(),
                                m.mealTime,
                                m.notes,
                                false,
                                m.iconResId,
                                m.fiber,
                                m.sugar,
                                m.servingSize,
                                m.ingredients,
                                m.steps
                        );
                        fitnessDao.insertLoggedMeal(copy);
                    }
                }
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        hideLoading();
                        Toast.makeText(requireContext(), "Schedule copied!", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(this::hideLoading);
                }
            }
        }).start();
    }

    private void exportMealPlan() {
        if (dbMeals == null || dbMeals.isEmpty()) {
            Toast.makeText(requireContext(), "No meals to export!", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Meal Plan for ").append(selectedDate).append("\n\n");
        for (LoggedMeal m : dbMeals) {
            sb.append("[").append(m.mealType).append("] ").append(m.name).append(" - ").append(m.mealTime).append("\n");
            sb.append("Calories: ").append(m.calories).append(" kcal | P: ").append(m.protein).append("g, C: ").append(m.carbs).append("g, F: ").append(m.fat).append("g\n\n");
        }

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Export Meal Plan"));
    }

    private void refreshData() {
        loadMealsForDate();
        if (scheduleAdapter != null) scheduleAdapter.notifyDataSetChanged();
        updateKcalProgress();
        Toast.makeText(requireContext(), "Data refreshed 🔄", Toast.LENGTH_SHORT).show();
    }

    public static int parseTimeToMinutes(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) return 720;
        try {
            timeStr = timeStr.trim().toUpperCase(Locale.getDefault());
            int hour = 0, minute = 0;
            boolean pm = false, am = false;
            if (timeStr.contains("PM")) { pm = true; timeStr = timeStr.replace("PM", "").trim(); }
            else if (timeStr.contains("AM")) { am = true; timeStr = timeStr.replace("AM", "").trim(); }

            String[] parts = timeStr.split(":");
            if (parts.length >= 1) hour = Integer.parseInt(parts[0].trim());
            if (parts.length >= 2) minute = Integer.parseInt(parts[1].trim());

            if (pm && hour < 12) hour += 12;
            else if (am && hour == 12) hour = 0;
            return hour * 60 + minute;
        } catch (Exception e) {
            return 720;
        }
    }

    public static String getDynamicCategoryForMeal(LoggedMeal meal) {
        String type = meal.mealType;
        if (type == null) type = "Meal";

        if (type.equalsIgnoreCase("Breakfast") ||
                type.equalsIgnoreCase("Lunch") ||
                type.equalsIgnoreCase("Dinner") ||
                type.equalsIgnoreCase("Pre Workout Meal") ||
                type.equalsIgnoreCase("Post Workout Meal") ||
                type.equalsIgnoreCase("Morning Drink") ||
                type.equalsIgnoreCase("Afternoon Drink") ||
                type.equalsIgnoreCase("Cheat Meal")) {
            return type;
        }

        int minutes = parseTimeToMinutes(meal.mealTime);
        if (minutes < 510) return "Early Morning Snack";
        else if (minutes < 720) return "Morning Snack";
        else if (minutes < 840) return "Midday Snack";
        else if (minutes < 1110) return "Afternoon Snack";
        else if (minutes < 1230) return "Evening Snack";
        else return "Late Night Snack";
    }

    private void emitCategoryGroup(String category, List<LoggedMeal> meals) {
        int totalKcal = 0;
        for (LoggedMeal m : meals) {
            totalKcal += m.calories;
        }
        scheduleList.add(new ScheduledMealItem(category, meals.size() + " meal" + (meals.size() > 1 ? "s" : "") + " | " + totalKcal + " calories"));
        for (LoggedMeal m : meals) {
            int icon = m.iconResId != 0 ? m.iconResId : R.drawable.ic_meal_breakfast;
            scheduleList.add(new ScheduledMealItem(
                    m.id,
                    m.mealType,
                    m.name,
                    m.mealTime != null ? m.mealTime : "08:00 AM",
                    m.calories,
                    m.isChecked,
                    icon,
                    m.protein,
                    m.carbs,
                    m.fat,
                    m.ingredients,
                    m.steps
            ));
        }
    }

    private int findEmojiIndexByName(String name) {
        if (name == null) return -1;
        String lower = name.toLowerCase(Locale.getDefault());
        for (int i = 0; i < com.fitness.app.activities.MealScheduleActivity.emojiNames.length; i++) {
            if (lower.contains(com.fitness.app.activities.MealScheduleActivity.emojiNames[i].toLowerCase(Locale.getDefault()))) {
                return i;
            }
        }
        return -1;
    }

    private androidx.appcompat.app.AlertDialog loadingDialog;

    private void showLoading(String message) {
        if (!isAdded()) return;
        if (loadingDialog != null && loadingDialog.isShowing()) loadingDialog.dismiss();

        android.widget.LinearLayout layout = new android.widget.LinearLayout(requireContext());
        layout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        layout.setPadding(50, 40, 50, 40);
        layout.setGravity(android.view.Gravity.CENTER_VERTICAL);

        com.google.android.material.progressindicator.CircularProgressIndicator progress =
                new com.google.android.material.progressindicator.CircularProgressIndicator(requireContext());
        progress.setIndeterminate(true);

        TextView textView = new TextView(requireContext());
        textView.setText(message);
        textView.setTextSize(16);
        textView.setPadding(40, 0, 0, 0);

        layout.addView(progress);
        layout.addView(textView);

        loadingDialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setView(layout)
                .setCancelable(false)
                .create();
        loadingDialog.show();
    }

    private void hideLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) loadingDialog.dismiss();
    }

    private class ScheduleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public int getItemViewType(int position) {
            return scheduleList.get(position).isHeader ? 0 : 1;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == 0) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meal_schedule_header, parent, false);
                return new HeaderViewHolder(view);
            } else {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meal_schedule_slot, parent, false);
                return new SlotViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ScheduledMealItem item = scheduleList.get(position);
            if (item.isHeader) {
                HeaderViewHolder hHolder = (HeaderViewHolder) holder;
                hHolder.tvHeaderCategory.setText(item.category);
                hHolder.tvHeaderCount.setText(item.countLabel);
            } else {
                SlotViewHolder sHolder = (SlotViewHolder) holder;

                sHolder.tvTitle.setText(item.title);
                if (sHolder.tvCategoryTag != null) {
                    sHolder.tvCategoryTag.setText(item.category != null ? item.category.toUpperCase() : "MEAL");
                }
                sHolder.tvTime.setText(item.time);
                sHolder.tvCalories.setText(item.calories + " kcal");

                if (sHolder.tvSlotProt != null) sHolder.tvSlotProt.setText("P: " + item.protein + "g");
                if (sHolder.tvSlotCarbs != null) sHolder.tvSlotCarbs.setText("C: " + item.carbs + "g");
                if (sHolder.tvSlotFat != null) sHolder.tvSlotFat.setText("F: " + item.fat + "g");

                if (sHolder.tvSlotDesc != null) {
                    if (item.ingredients != null && !item.ingredients.trim().isEmpty()) {
                        sHolder.tvSlotDesc.setText(item.ingredients);
                    } else if (item.description != null && !item.description.trim().isEmpty()) {
                        sHolder.tvSlotDesc.setText(item.description);
                    } else {
                        sHolder.tvSlotDesc.setText("Nutritious balanced meal for fitness goals");
                    }
                }

                if (sHolder.tvTitleCheckmark != null) {
                    sHolder.tvTitleCheckmark.setVisibility(item.checked ? View.VISIBLE : View.GONE);
                }

                // Checkbox mark eaten
                if (sHolder.cbMarkEaten != null) {
                    sHolder.cbMarkEaten.setOnCheckedChangeListener(null);
                    sHolder.cbMarkEaten.setChecked(item.checked);
                    sHolder.cbMarkEaten.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        item.checked = isChecked;
                        if (sHolder.tvTitleCheckmark != null) {
                            sHolder.tvTitleCheckmark.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                        }
                        new Thread(() -> {
                            try {
                                LoggedMeal logged = fitnessDao.getLoggedMealById(item.id);
                                if (logged != null) {
                                    logged.isChecked = isChecked;
                                    fitnessDao.insertLoggedMeal(logged);
                                }
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(DietFragment.this::updateKcalProgress);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }).start();
                    });
                }

                // Food emoji thumbnail (Replaces pictures)
                if (sHolder.ivMealImageSlot != null) {
                    sHolder.ivMealImageSlot.setVisibility(View.GONE);
                }
                if (sHolder.tvMealEmoji != null) {
                    String emojiStr = com.fitness.app.activities.MealScheduleActivity.getMealEmoji(item.iconResId, item.title);
                    sHolder.tvMealEmoji.setText(emojiStr);
                    sHolder.tvMealEmoji.setVisibility(View.VISIBLE);
                }

                // Favorite heart button
                Set<String> favs = getFavoriteMeals();
                boolean isFav = favs.contains(item.title);
                if (sHolder.ivFavoriteSlot != null) {
                    sHolder.ivFavoriteSlot.setColorFilter(isFav ? 0xFFEF4444 : 0xFF94A3B8);
                    sHolder.ivFavoriteSlot.setOnClickListener(v -> {
                        boolean newFav = !getFavoriteMeals().contains(item.title);
                        saveFavoriteMeal(item.title, newFav);
                        sHolder.ivFavoriteSlot.setColorFilter(newFav ? 0xFFEF4444 : 0xFF94A3B8);
                        Toast.makeText(requireContext(), newFav ? "Saved to favorites ❤️" : "Removed from favorites", Toast.LENGTH_SHORT).show();
                    });
                }

                // View Recipe CTA
                View.OnClickListener launchDetail = v -> {
                    Intent intent = new Intent(getActivity(), com.fitness.app.activities.MealDetailActivity.class);
                    intent.putExtra("selected_date", selectedDate);
                    intent.putExtra("meal_id", item.id);
                    startActivityForResult(intent, 100);
                };

                if (sHolder.btnViewFullRecipe != null) {
                    sHolder.btnViewFullRecipe.setOnClickListener(launchDetail);
                }
                sHolder.itemView.setOnClickListener(launchDetail);

                setIconContainerStyle(sHolder.cardIconContainer, item.category);
            }
        }

        private int getMealDrawableRes(String category, String title) {
            if (title != null) {
                String lower = title.toLowerCase(Locale.getDefault());
                if (lower.contains("pancake") || lower.contains("oatmeal") || lower.contains("egg") || lower.contains("toast") || lower.contains("smoothie")) {
                    return R.drawable.onboarding_1;
                } else if (lower.contains("chicken") || lower.contains("steak") || lower.contains("salmon") || lower.contains("rice") || lower.contains("salad")) {
                    return R.drawable.onboarding_2;
                } else if (lower.contains("shake") || lower.contains("snack") || lower.contains("almond") || lower.contains("bar") || lower.contains("fruit")) {
                    return R.drawable.onboarding_3;
                }
            }
            if (category != null) {
                String lowerCat = category.toLowerCase(Locale.getDefault());
                if (lowerCat.contains("breakfast")) return R.drawable.onboarding_1;
                if (lowerCat.contains("lunch")) return R.drawable.onboarding_2;
                if (lowerCat.contains("dinner")) return R.drawable.onboarding_4;
                if (lowerCat.contains("snack") || lowerCat.contains("drink")) return R.drawable.onboarding_3;
            }
            return R.drawable.ic_healthy_fitness_ai;
        }

        private void setIconContainerStyle(com.google.android.material.card.MaterialCardView card, String category) {
            if (card == null || category == null) return;
            switch (category.toLowerCase()) {
                case "breakfast":
                case "morning snack":
                    card.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(0xFFE0F2FE));
                    break;
                case "pre workout meal":
                    card.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(0xFFFEF08A));
                    break;
                case "lunch":
                    card.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(0xFFF3E8FF));
                    break;
                case "post workout meal":
                    card.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(0xFFFED7AA));
                    break;
                case "evening snack":
                case "snacks":
                    card.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(0xFFFFE4E6));
                    break;
                case "dinner":
                    card.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(0xFFDBEAFE));
                    break;
                case "cheat meal":
                    card.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(0xFFFCA5A5));
                    break;
                default:
                    card.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(0xFFF1F5F9));
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return scheduleList.size();
        }

        class HeaderViewHolder extends RecyclerView.ViewHolder {
            TextView tvHeaderCategory, tvHeaderCount;

            public HeaderViewHolder(@NonNull View itemView) {
                super(itemView);
                tvHeaderCategory = itemView.findViewById(R.id.tvHeaderCategory);
                tvHeaderCount = itemView.findViewById(R.id.tvHeaderCount);
            }
        }

        class SlotViewHolder extends RecyclerView.ViewHolder {
            com.google.android.material.card.MaterialCardView cardIconContainer;
            TextView tvCategoryTag, tvTitle, tvTime, tvCalories, tvMealEmoji, tvTitleCheckmark, tvSlotDesc, tvSlotProt, tvSlotCarbs, tvSlotFat;
            ImageView ivMealImageSlot, ivFavoriteSlot;
            CheckBox cbMarkEaten;
            TextView btnViewFullRecipe;

            public SlotViewHolder(@NonNull View itemView) {
                super(itemView);
                cardIconContainer = itemView.findViewById(R.id.cardIconContainer);
                tvCategoryTag = itemView.findViewById(R.id.tvCategoryTag);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvTitleCheckmark = itemView.findViewById(R.id.tvTitleCheckmark);
                tvTime = itemView.findViewById(R.id.tvTime);
                tvCalories = itemView.findViewById(R.id.tvCalories);
                tvMealEmoji = itemView.findViewById(R.id.tvMealEmoji);
                tvSlotDesc = itemView.findViewById(R.id.tvSlotDesc);
                tvSlotProt = itemView.findViewById(R.id.tvSlotProt);
                tvSlotCarbs = itemView.findViewById(R.id.tvSlotCarbs);
                tvSlotFat = itemView.findViewById(R.id.tvSlotFat);
                ivMealImageSlot = itemView.findViewById(R.id.ivMealImageSlot);
                ivFavoriteSlot = itemView.findViewById(R.id.ivFavoriteSlot);
                cbMarkEaten = itemView.findViewById(R.id.cbMarkEaten);
                btnViewFullRecipe = itemView.findViewById(R.id.btnViewFullRecipe);
            }
        }
    }
}

package com.fitness.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fitness.app.R;
import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.data.room.AppDatabase;
import com.fitness.app.data.room.FitnessDao;
import com.fitness.app.data.room.LoggedMeal;
import com.fitness.app.models.DietPlan;
import com.fitness.app.models.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MealScheduleActivity extends AppCompatActivity {

    private RecyclerView rvScheduleList;
    private TextView tvCalendarMonth;
    
    private ProgressBar pbKcalProgress, pbProteinsProgress, pbFatsProgress, pbCarbsProgress, pbRemainingProgress, pbCompletedProgress;
    private TextView tvConsumedVal, tvProteinsVal, tvFatsVal, tvCarbsVal, tvRemainingVal, tvCompletedVal;
    private TextView tvKcalPercent, tvProteinsPercent, tvFatsPercent, tvCarbsPercent, tvRemainingPercent, tvCompletedPercent;

    private LocalDataManager localDb;
    private FitnessDao fitnessDao;
    private DietPlan dietPlan;
    private ScheduleAdapter adapter;
    private List<ScheduledMealItem> scheduleList = new ArrayList<>();
    private List<LoggedMeal> dbMeals = new ArrayList<>();
    private String selectedDate; // yyyy-MM-dd

    public static class ScheduledMealItem {
        public int id; // database primary key ID
        public boolean isHeader;
        public String category;
        public String title;
        public String time;
        public int calories;
        public boolean checked;
        public String countLabel; // e.g. "2 meals | 230 calories"
        public int iconResId; // specific icon resource

        // Constructor for child item
        public ScheduledMealItem(int id, String category, String title, String time, int calories, boolean checked, int iconResId) {
            this.id = id;
            this.isHeader = false;
            this.category = category;
            this.title = title;
            this.time = time;
            this.calories = calories;
            this.checked = checked;
            this.iconResId = iconResId;
        }

        // Constructor for header item
        public ScheduledMealItem(String category, String countLabel) {
            this.id = -1;
            this.isHeader = true;
            this.category = category;
            this.countLabel = countLabel;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_meal_schedule);

        localDb = new LocalDataManager(this);
        fitnessDao = AppDatabase.getInstance(this).fitnessDao();
        dietPlan = localDb.getDietPlan();

        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Bind Views
        rvScheduleList = findViewById(R.id.rvScheduleList);
        tvCalendarMonth = findViewById(R.id.tvCalendarMonth);
        pbKcalProgress = findViewById(R.id.pbKcalProgress);
        pbProteinsProgress = findViewById(R.id.pbProteinsProgress);
        pbFatsProgress = findViewById(R.id.pbFatsProgress);
        pbCarbsProgress = findViewById(R.id.pbCarbsProgress);
        pbRemainingProgress = findViewById(R.id.pbRemainingProgress);
        pbCompletedProgress = findViewById(R.id.pbCompletedProgress);

        tvConsumedVal = findViewById(R.id.tvConsumedVal);
        tvProteinsVal = findViewById(R.id.tvProteinsVal);
        tvFatsVal = findViewById(R.id.tvFatsVal);
        tvCarbsVal = findViewById(R.id.tvCarbsVal);
        tvRemainingVal = findViewById(R.id.tvRemainingVal);
        tvCompletedVal = findViewById(R.id.tvCompletedVal);

        tvKcalPercent = findViewById(R.id.tvKcalPercent);
        tvProteinsPercent = findViewById(R.id.tvProteinsPercent);
        tvFatsPercent = findViewById(R.id.tvFatsPercent);
        tvCarbsPercent = findViewById(R.id.tvCarbsPercent);
        tvRemainingPercent = findViewById(R.id.tvRemainingPercent);
        tvCompletedPercent = findViewById(R.id.tvCompletedPercent);

        // Setup Month Title
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        tvCalendarMonth.setText(monthFormat.format(new Date()));

        // Back action
        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());

        // Floating Action Button with smooth micro-animation to launch AddMealActivity
        findViewById(R.id.fabAddMealSlot).setOnClickListener(v -> {
            v.animate().scaleX(0.85f).scaleY(0.85f).setDuration(80).withEndAction(() -> {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(80).start();
                Intent intent = new Intent(this, AddMealActivity.class);
                intent.putExtra("selected_date", selectedDate);
                startActivityForResult(intent, 100);
            }).start();
        });

        findViewById(R.id.btnMore).setOnClickListener(v -> showMoreMenu(v));
        setupCalendarDays();

        rvScheduleList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ScheduleAdapter();
        rvScheduleList.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean needsRegen = localDb.sharedPreferences.getBoolean("plan_needs_regeneration", false);
        if (needsRegen) {
            localDb.sharedPreferences.edit().putBoolean("plan_needs_regeneration", false).apply();
            showLoading("Updating meal plan based on new settings...");
            new Thread(() -> {
                try {
                    fitnessDao.deleteUncompletedLoggedMealsForDate(selectedDate);
                    
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                    for (int i = 0; i < 7; i++) {
                        cal.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY + i);
                        String dateStr = sdf.format(cal.getTime());
                        if (!dateStr.equals(selectedDate)) {
                            fitnessDao.deleteUncompletedLoggedMealsForDate(dateStr);
                        }
                    }
                    
                    prepopulatePresetMeals();
                    
                    runOnUiThread(() -> {
                        hideLoading();
                        loadMealsForDate();
                        adapter.notifyDataSetChanged();
                        updateKcalProgress();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> hideLoading());
                }
            }).start();
        } else {
            loadMealsForDate();
            adapter.notifyDataSetChanged();
            updateKcalProgress();
        }
    }

    private void setupCalendarDays() {
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
        int daysToSubtract;
        if (dayOfWeek == Calendar.SUNDAY) {
            daysToSubtract = -6;
        } else {
            daysToSubtract = Calendar.MONDAY - dayOfWeek;
        }
        cal.add(Calendar.DAY_OF_YEAR, daysToSubtract);
        
        TextView[] tvDays = {
            findViewById(R.id.tvDay1), findViewById(R.id.tvDay2),
            findViewById(R.id.tvDay3), findViewById(R.id.tvDay4),
            findViewById(R.id.tvDay5), findViewById(R.id.tvDay6),
            findViewById(R.id.tvDay7)
        };
        TextView[] tvDates = {
            findViewById(R.id.tvDate1), findViewById(R.id.tvDate2),
            findViewById(R.id.tvDate3), findViewById(R.id.tvDate4),
            findViewById(R.id.tvDate5), findViewById(R.id.tvDate6),
            findViewById(R.id.tvDate7)
        };
        View[] dayContainers = {
            findViewById(R.id.dayContainer1), findViewById(R.id.dayContainer2),
            findViewById(R.id.dayContainer3), findViewById(R.id.dayContainer4),
            findViewById(R.id.dayContainer5), findViewById(R.id.dayContainer6),
            findViewById(R.id.dayContainer7)
        };

        SimpleDateFormat dayNameFmt = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat dateNumFmt = new SimpleDateFormat("d", Locale.getDefault());
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());

        for (int i = 0; i < 7; i++) {
            tvDays[i].setText(dayNameFmt.format(cal.getTime()).substring(0, 1).toUpperCase());
            tvDates[i].setText(dateNumFmt.format(cal.getTime()));
            
            final String clickedDate = ymdFmt.format(cal.getTime());
            final int index = i;

            if (clickedDate.equals(selectedDate)) {
                android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
                shape.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
                shape.setCornerRadius(24f);
                shape.setColor(0xFF1D4ED8); // Darker Primary Blue (blue-700)
                dayContainers[i].setBackground(shape);
                tvDays[i].setTextColor(0xFFFFFFFF);
                tvDates[i].setTextColor(0xFFFFFFFF);
                tvCalendarMonth.setText(monthFormat.format(cal.getTime()));
            } else {
                android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
                shape.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
                shape.setCornerRadius(24f);
                shape.setColor(0x00FFFFFF); // Transparent
                shape.setStroke(2, 0xFFCBD5E1); // Light border (slate-300)
                dayContainers[i].setBackground(shape);
                tvDays[i].setTextColor(0xFF64748B);
                tvDates[i].setTextColor(0xFF1E293B);
            }

            dayContainers[i].setOnClickListener(v -> {
                selectedDate = clickedDate;
                setupCalendarDays();
                loadMealsForDate();
                adapter.notifyDataSetChanged();
                updateKcalProgress();
            });

            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    public static String generateIntelligentNotes(String mealType, String goal, int calories, double protein, double carbs, double fat) {
        String notes = "";
        String goalClean = (goal != null) ? goal.toLowerCase() : "maintenance";
        if (mealType == null) mealType = "meal";
        
        if (mealType.equalsIgnoreCase("Breakfast")) {
            if (goalClean.contains("loss") || goalClean.contains("fat")) {
                notes = "This light breakfast is packed with fiber and lean nutrients to jumpstart your metabolism and support fat burning throughout the morning.";
            } else if (goalClean.contains("gain") || goalClean.contains("muscle")) {
                notes = "A high-protein, calorie-dense breakfast designed to fuel muscle recovery and provide sustained energy for heavy lifts.";
            } else {
                notes = "Balanced start to the day. High-quality protein and complex carbs to keep your blood sugar stable and maintain steady energy levels.";
            }
        } else if (mealType.equalsIgnoreCase("Lunch")) {
            if (goalClean.contains("loss") || goalClean.contains("fat")) {
                notes = "A clean, low-calorie lunch high in protein to keep you satiated and maintain lean muscle mass during your fat-loss phase.";
            } else if (goalClean.contains("gain") || goalClean.contains("muscle")) {
                notes = "Power lunch with ample carbohydrates and protein to fuel muscle glycogen synthesis and accelerate post-workout recovery.";
            } else {
                notes = "A nutrient-rich midday meal providing key macronutrients to sustain focus, energy, and physical performance for the rest of the day.";
            }
        } else if (mealType.equalsIgnoreCase("Dinner")) {
            if (goalClean.contains("loss") || goalClean.contains("fat")) {
                notes = "A light, digestible evening meal emphasizing lean protein and greens to support nighttime recovery without spiking insulin before bed.";
            } else if (goalClean.contains("gain") || goalClean.contains("muscle")) {
                notes = "A hearty dinner rich in slow-digesting protein and healthy fats to support muscle protein synthesis and recovery during sleep.";
            } else {
                notes = "A wholesomely balanced dinner to satisfy hunger, replenish nutrients, and promote restful sleep and next-day recovery.";
            }
        } else {
            if (goalClean.contains("loss") || goalClean.contains("fat")) {
                notes = "A healthy snack designed to curb cravings and sustain energy levels between main meals without exceeding calorie targets.";
            } else if (goalClean.contains("gain") || goalClean.contains("muscle")) {
                notes = "An anabolic snack supplying quick-acting nutrients to boost muscle growth and keep you in a positive calorie balance.";
            } else {
                notes = "A quick, nourishing snack to stabilize blood sugar and prevent midday fatigue while providing essential micro-nutrients.";
            }
        }
        return notes;
    }

    private void loadMealsForDate() {
        dbMeals.clear();
        try {
            dbMeals = fitnessDao.getLoggedMealsForDate(selectedDate);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // If database is empty for selected date, pre-populate presets into Room
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

    public static int parseTimeToMinutes(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return 720;
        }
        try {
            timeStr = timeStr.trim().toUpperCase(Locale.getDefault());
            int hour = 0;
            int minute = 0;
            boolean pm = false;
            boolean am = false;
            
            if (timeStr.contains("PM")) {
                pm = true;
                timeStr = timeStr.replace("PM", "").trim();
            } else if (timeStr.contains("AM")) {
                am = true;
                timeStr = timeStr.replace("AM", "").trim();
            }
            
            String[] parts = timeStr.split(":");
            if (parts.length >= 1) {
                hour = Integer.parseInt(parts[0].trim());
            }
            if (parts.length >= 2) {
                minute = Integer.parseInt(parts[1].trim());
            }
            
            if (pm && hour < 12) {
                hour += 12;
            } else if (am && hour == 12) {
                hour = 0;
            }
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
        if (minutes < 510) {
            return "Early Morning Snack";
        } else if (minutes < 720) {
            return "Morning Snack";
        } else if (minutes < 840) {
            return "Midday Snack";
        } else if (minutes < 1110) {
            return "Afternoon Snack";
        } else if (minutes < 1230) {
            return "Evening Snack";
        } else {
            return "Late Night Snack";
        }
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
                    icon
            ));
        }
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
                 if (iconId == -1) {
                     if ("Breakfast".equalsIgnoreCase(m.getType())) iconId = 0; // Pancake
                     else if ("Morning Drink".equalsIgnoreCase(m.getType())) iconId = 1; // Coffee
                     else if ("Lunch".equalsIgnoreCase(m.getType())) iconId = 2; // Steak
                     else if ("Afternoon Drink".equalsIgnoreCase(m.getType())) iconId = 19; // Shake
                     else if ("Dinner".equalsIgnoreCase(m.getType()) || "Cheat Meal".equalsIgnoreCase(m.getType())) iconId = 3; // Salad
                     else if ("Pre Workout Meal".equalsIgnoreCase(m.getType())) iconId = 4; // Oatmeal
                     else if ("Post Workout Meal".equalsIgnoreCase(m.getType())) iconId = 19; // Shake
                     else iconId = 4; // Oatmeal
                 }

                String notes = generateIntelligentNotes(m.getType(), user.getGoal(), m.getCalories(), m.getProtein(), m.getCarbs(), m.getFat());

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

    private int findEmojiIndexByName(String name) {
        if (name == null) return -1;
        String lower = name.toLowerCase(Locale.getDefault());
        for (int i = 0; i < emojiNames.length; i++) {
            if (lower.contains(emojiNames[i].toLowerCase(Locale.getDefault()))) {
                return i;
            }
        }
        return -1;
    }

    private void updateKcalProgress() {
        int consumed = 0;
        int target = 2000;
        int targetProtein = 150;
        int targetCarbs = 200;
        int targetFat = 65;

        User user = localDb.getUser();
        if (user != null) {
            target = user.getDailyCaloriesGoal();
            if (target <= 0) {
                double weight = user.getWeight() > 0 ? user.getWeight() : 70.0;
                double height = user.getHeight() > 0 ? user.getHeight() : 170.0;
                int age = user.getAge() > 0 ? user.getAge() : 25;
                String gender = user.getGender() != null ? user.getGender() : "Female";
                String goal = user.getGoal() != null ? user.getGoal() : "Maintain Weight";
                
                double bmr;
                if ("Male".equalsIgnoreCase(gender)) {
                    bmr = 10 * weight + 6.25 * height - 5 * age + 5;
                } else {
                    bmr = 10 * weight + 6.25 * height - 5 * age - 161;
                }

                double multiplier = 1.375;
                String act = user.getActivityLevel();
                if (act != null) {
                    if (act.contains("Sedentary")) multiplier = 1.2;
                    else if (act.contains("Light")) multiplier = 1.375;
                    else if (act.contains("Moderat") || act.contains("Active")) multiplier = 1.55;
                    else if (act.contains("Very") || act.contains("Athlete")) multiplier = 1.725;
                }

                double tdee = bmr * multiplier;
                if (goal.contains("Loss") || goal.contains("Fat")) {
                    target = (int) (tdee - 500);
                } else if (goal.contains("Gain") || goal.contains("Weight Gain") || goal.contains("Muscle")) {
                    target = (int) (tdee + 350);
                } else if (goal.contains("Recomposition")) {
                    target = (int) (tdee - 150);
                } else {
                    target = (int) tdee;
                }

                int minCal = "Male".equalsIgnoreCase(gender) ? 1500 : 1200;
                if (target < minCal) target = minCal;
            }
            
            String dietPref = user.getDietaryPreference() != null ? user.getDietaryPreference() : "Balanced";
            String goal = user.getGoal() != null ? user.getGoal() : "Maintain Weight";
            int proteinPercent = 30;
            int carbsPercent = 40;
            int fatPercent = 30;

            if ("Keto".equalsIgnoreCase(dietPref)) {
                proteinPercent = 20;
                carbsPercent = 5;
                fatPercent = 75;
            } else if ("Vegan".equalsIgnoreCase(dietPref) || "Vegetarian".equalsIgnoreCase(dietPref)) {
                proteinPercent = 20;
                carbsPercent = 55;
                fatPercent = 25;
            } else if (goal.contains("Gain") || goal.contains("Muscle") || goal.contains("Performance")) {
                proteinPercent = 35;
                carbsPercent = 45;
                fatPercent = 20;
            } else if (goal.contains("Loss") || goal.contains("Fat")) {
                proteinPercent = 40;
                carbsPercent = 30;
                fatPercent = 30;
            }

            targetProtein = (int) ((target * (proteinPercent / 100.0)) / 4.0);
            targetCarbs = (int) ((target * (carbsPercent / 100.0)) / 4.0);
            targetFat = (int) ((target * (fatPercent / 100.0)) / 9.0);
        }
        
        int totalProtein = 0;
        int totalCarbs = 0;
        int totalFat = 0;

        int completedCount = 0;
        int totalCount = 0;

        for (LoggedMeal m : dbMeals) {
            totalCount++;
            if (m.isChecked) {
                completedCount++;
                consumed += m.calories;
                totalProtein += m.protein;
                totalCarbs += m.carbs;
                totalFat += m.fat;
            }
        }

        int caloriesBurned = 0;
        try {
            com.fitness.app.models.ProgressLog progress = localDb.getProgressLog(selectedDate);
            if (progress != null) {
                caloriesBurned = progress.getCaloriesBurned();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        int kcalPct = target > 0 ? (int) Math.round((double) consumed / target * 100) : 0;
        int protPct = targetProtein > 0 ? (int) Math.round((double) totalProtein / targetProtein * 100) : 0;
        int fatPct = targetFat > 0 ? (int) Math.round((double) totalFat / targetFat * 100) : 0;
        int carbsPct = targetCarbs > 0 ? (int) Math.round((double) totalCarbs / targetCarbs * 100) : 0;
        
        int remainingKcal = target - consumed + caloriesBurned;
        if (remainingKcal < 0) remainingKcal = 0;
        
        int remPct = target > 0 ? (int) Math.round((double) remainingKcal / target * 100) : 0;
        int compPct = totalCount > 0 ? (int) Math.round((double) completedCount / totalCount * 100) : 0;

        if (tvConsumedVal != null) {
            tvConsumedVal.setText(consumed + " / " + target);
        }
        if (tvKcalPercent != null) {
            tvKcalPercent.setText(kcalPct + "%");
        }
        if (pbKcalProgress != null) {
            pbKcalProgress.setMax(target);
            pbKcalProgress.setProgress(Math.min(consumed, target));
        }

        // Proteins
        if (tvProteinsVal != null) {
            tvProteinsVal.setText(totalProtein + " / " + targetProtein + "g");
        }
        if (tvProteinsPercent != null) {
            tvProteinsPercent.setText(protPct + "%");
        }
        if (pbProteinsProgress != null) {
            pbProteinsProgress.setMax(targetProtein);
            pbProteinsProgress.setProgress(Math.min(totalProtein, targetProtein));
        }

        // Fats
        if (tvFatsVal != null) {
            tvFatsVal.setText(totalFat + " / " + targetFat + "g");
        }
        if (tvFatsPercent != null) {
            tvFatsPercent.setText(fatPct + "%");
        }
        if (pbFatsProgress != null) {
            pbFatsProgress.setMax(targetFat);
            pbFatsProgress.setProgress(Math.min(totalFat, targetFat));
        }

        // Carbs
        if (tvCarbsVal != null) {
            tvCarbsVal.setText(totalCarbs + " / " + targetCarbs + "g");
        }
        if (tvCarbsPercent != null) {
            tvCarbsPercent.setText(carbsPct + "%");
        }
        if (pbCarbsProgress != null) {
            pbCarbsProgress.setMax(targetCarbs);
            pbCarbsProgress.setProgress(Math.min(totalCarbs, targetCarbs));
        }

        // Remaining
        if (tvRemainingVal != null) {
            tvRemainingVal.setText(remainingKcal + " kcal");
        }
        if (tvRemainingPercent != null) {
            tvRemainingPercent.setText(remPct + "%");
        }
        if (pbRemainingProgress != null) {
            pbRemainingProgress.setMax(target);
            pbRemainingProgress.setProgress(Math.min(remainingKcal, target));
        }

        // Completed
        if (tvCompletedVal != null) {
            tvCompletedVal.setText(completedCount + " / " + totalCount);
        }
        if (tvCompletedPercent != null) {
            tvCompletedPercent.setText(compPct + "%");
        }
        if (pbCompletedProgress != null) {
            pbCompletedProgress.setMax(totalCount > 0 ? totalCount : 1);
            pbCompletedProgress.setProgress(completedCount);
        }

        // Save today's progress to local cache and sync to cloud
        com.fitness.app.models.ProgressLog progressLog = localDb.getProgressLog(selectedDate);
        if (progressLog != null) {
            progressLog.setCaloriesConsumed(consumed);
            progressLog.setUpdatedAt(System.currentTimeMillis());
            localDb.saveProgressLog(progressLog);
            
            // Sync to firestore
            new Thread(() -> {
                try {
                    new com.fitness.app.repositories.UserRepository().syncLocalDataToFirestore(localDb);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            loadMealsForDate();
            adapter.notifyDataSetChanged();
            updateKcalProgress();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    // RecyclerView Adapter supporting Header vs Slot
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
                if (item.checked) {
                    sHolder.tvTitleCheckmark.setVisibility(View.VISIBLE);
                } else {
                    sHolder.tvTitleCheckmark.setVisibility(View.GONE);
                }
                sHolder.tvTime.setText(item.time);
                sHolder.tvCalories.setText(item.calories + " kcal");

                // Set dynamic emoji based on index or name
                sHolder.tvMealEmoji.setText(getMealEmoji(item.iconResId, item.title));

                // Click chevron arrow to launch details activity
                sHolder.btnNavigateDetail.setOnClickListener(v -> {
                    Intent intent = new Intent(MealScheduleActivity.this, MealDetailActivity.class);
                    intent.putExtra("selected_date", selectedDate);
                    intent.putExtra("meal_id", item.id);
                    startActivityForResult(intent, 100);
                });

                // Clicking the entire card also launches the details view
                sHolder.itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(MealScheduleActivity.this, MealDetailActivity.class);
                    intent.putExtra("selected_date", selectedDate);
                    intent.putExtra("meal_id", item.id);
                    startActivityForResult(intent, 100);
                });

                // Icon Container Color Tint representing category
                setIconContainerStyle(sHolder.cardIconContainer, item.category);
            }
        }

        private void setIconContainerStyle(com.google.android.material.card.MaterialCardView card, String category) {
            if (category == null) return;
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
                    card.setCardBackgroundColor(android.content.res.ColorStateList.valueOf(0xFFDCFCE7));
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
            TextView tvTitle, tvTime, tvCalories, tvMealEmoji, tvTitleCheckmark;
            android.widget.ImageView btnNavigateDetail;

            public SlotViewHolder(@NonNull View itemView) {
                super(itemView);
                cardIconContainer = itemView.findViewById(R.id.cardIconContainer);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvTitleCheckmark = itemView.findViewById(R.id.tvTitleCheckmark);
                tvTime = itemView.findViewById(R.id.tvTime);
                tvCalories = itemView.findViewById(R.id.tvCalories);
                tvMealEmoji = itemView.findViewById(R.id.tvMealEmoji);
                btnNavigateDetail = itemView.findViewById(R.id.btnNavigateDetail);
            }
        }
    }

    public static final String[] emojiNames = {
            "Pancake", "Coffee", "Steak", "Salad", "Oatmeal", "Apple Pie", "Pizza", "Burger", "Sandwich", "Chicken", "Rice", "Pasta", "Noodles", "Soup", "Eggs", "Apple", "Banana", "Watermelon", "Strawberry", "Juice", "Milk", "Water"
    };

    public static final String[] emojiChars = {
            "🥞", "☕", "🥩", "🥗", "🥣", "🥧", "🍕", "🍔", "🥪", "🍗", "🍚", "🍝", "🍜", "🍲", "🥚", "🍎", "🍌", "🍉", "🍓", "🧃", "🥛", "💧"
    };

    public static String getMealEmoji(int iconResId, String name) {
        if (iconResId >= 0 && iconResId < emojiChars.length) {
            return emojiChars[iconResId];
        }

        if (name == null) return "🍽️";
        String lower = name.toLowerCase(java.util.Locale.getDefault());

        for (int i = 0; i < emojiNames.length; i++) {
            if (lower.contains(emojiNames[i].toLowerCase(java.util.Locale.getDefault()))) {
                return emojiChars[i];
            }
        }

        if (lower.contains("pancake")) return "🥞";
        if (lower.contains("coffee")) return "☕";
        if (lower.contains("salad")) return "🥗";
        if (lower.contains("oatmeal")) return "🥣";
        if (lower.contains("pie")) return "🥧";
        if (lower.contains("pizza")) return "🍕";
        if (lower.contains("burger")) return "🍔";
        if (lower.contains("taco")) return "🌮";
        if (lower.contains("wrap")) return "🌯";
        if (lower.contains("sandwich")) return "🥪";
        if (lower.contains("chicken")) return "🍗";
        if (lower.contains("steak")) return "🥩";
        if (lower.contains("bbq")) return "🍖";
        if (lower.contains("fries")) return "🍟";
        if (lower.contains("rice")) return "🍚";
        if (lower.contains("pasta")) return "🍝";
        if (lower.contains("noodle")) return "🍜";
        if (lower.contains("soup")) return "🍲";
        if (lower.contains("sushi")) return "🍣";
        if (lower.contains("curry")) return "🍛";
        if (lower.contains("fish")) return "🐟";
        if (lower.contains("egg")) return "🥚";
        if (lower.contains("cheese")) return "🧀";
        if (lower.contains("milk")) return "🥛";
        if (lower.contains("apple")) return "🍎";
        if (lower.contains("banana")) return "🍌";
        if (lower.contains("grape")) return "🍇";
        if (lower.contains("strawberry")) return "🍓";
        if (lower.contains("watermelon")) return "🍉";
        if (lower.contains("pineapple")) return "🍍";
        if (lower.contains("avocado")) return "🥑";
        if (lower.contains("carrot")) return "🥕";
        if (lower.contains("corn")) return "🌽";
        if (lower.contains("cucumber")) return "🥒";
        if (lower.contains("tomato")) return "🍅";
        if (lower.contains("blueberry")) return "🫐";
        if (lower.contains("mango")) return "🥭";
        if (lower.contains("peach")) return "🍑";
        if (lower.contains("lemon")) return "🍋";
        if (lower.contains("orange")) return "🍊";
        if (lower.contains("bread")) return "🍞";
        if (lower.contains("cupcake")) return "🧁";
        if (lower.contains("cake")) return "🍰";
        if (lower.contains("cookie")) return "🍪";
        if (lower.contains("chocolate")) return "🍫";
        if (lower.contains("popcorn")) return "🍿";
        if (lower.contains("nut")) return "🥜";
        if (lower.contains("juice")) return "🧃";
        if (lower.contains("soda") || lower.contains("cola") || lower.contains("soft drink")) return "🥤";
        if (lower.contains("water")) return "💧";

        return "🍽️";
    }

    private androidx.appcompat.app.AlertDialog loadingDialog;

    private void showLoading(String message) {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
        
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        layout.setPadding(50, 40, 50, 40);
        layout.setGravity(android.view.Gravity.CENTER_VERTICAL);
        
        com.google.android.material.progressindicator.CircularProgressIndicator progress = 
                new com.google.android.material.progressindicator.CircularProgressIndicator(this);
        progress.setIndeterminate(true);
        progress.setIndicatorSize(80);
        progress.setTrackCornerRadius(4);
        
        android.widget.TextView textView = new android.widget.TextView(this);
        textView.setText(message);
        textView.setTextSize(16);
        textView.setTextColor(0xFF1E293B);
        textView.setPadding(40, 0, 0, 0);
        
        layout.addView(progress);
        layout.addView(textView);
        
        loadingDialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(layout)
                .setCancelable(false)
                .create();
        loadingDialog.show();
    }

    private void hideLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void showMoreMenu(View anchor) {
        androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, "Add Meal");
        popup.getMenu().add(0, 2, 1, "Edit Schedule");
        popup.getMenu().add(0, 3, 2, "Regenerate Meal Plan");
        popup.getMenu().add(0, 4, 3, "Copy Schedule to Another Day");
        popup.getMenu().add(0, 5, 4, "View Nutrition Summary");
        popup.getMenu().add(0, 6, 5, "Export Meal Plan");
        popup.getMenu().add(0, 7, 6, "Refresh Data");
        popup.getMenu().add(0, 8, 7, "Settings");

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    Intent intent = new Intent(this, AddMealActivity.class);
                    intent.putExtra("selected_date", selectedDate);
                    startActivityForResult(intent, 100);
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
                    Intent summaryIntent = new Intent(this, NutritionSummaryActivity.class);
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
                    startActivity(new Intent(this, SettingsActivity.class));
                    return true;
            }
            return false;
        });
        popup.show();
    }

    private void showEditScheduleDialog() {
        int currentKcal = dietPlan != null ? dietPlan.getTargetCalories() : 2000;
        int currentProtein = dietPlan != null ? dietPlan.getTargetProteinGrams() : 150;
        int currentCarbs = dietPlan != null ? dietPlan.getTargetCarbsGrams() : 200;
        int currentFat = dietPlan != null ? dietPlan.getTargetFatGrams() : 65;

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 40);

        final android.widget.EditText etKcal = new android.widget.EditText(this);
        etKcal.setHint("Calorie Goal (kcal)");
        etKcal.setText(String.valueOf(currentKcal));
        etKcal.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(etKcal);

        final android.widget.EditText etProtein = new android.widget.EditText(this);
        etProtein.setHint("Protein Goal (g)");
        etProtein.setText(String.valueOf(currentProtein));
        etProtein.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(etProtein);

        final android.widget.EditText etCarbs = new android.widget.EditText(this);
        etCarbs.setHint("Carbs Goal (g)");
        etCarbs.setText(String.valueOf(currentCarbs));
        etCarbs.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(etCarbs);

        final android.widget.EditText etFat = new android.widget.EditText(this);
        etFat.setHint("Fat Goal (g)");
        etFat.setText(String.valueOf(currentFat));
        etFat.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(etFat);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Edit Nutrition Targets")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String kcalStr = etKcal.getText().toString().trim();
                    String protStr = etProtein.getText().toString().trim();
                    String carbStr = etCarbs.getText().toString().trim();
                    String fatStr = etFat.getText().toString().trim();

                    if (!kcalStr.isEmpty() && !protStr.isEmpty() && !carbStr.isEmpty() && !fatStr.isEmpty()) {
                        int k = Integer.parseInt(kcalStr);
                        int p = Integer.parseInt(protStr);
                        int c = Integer.parseInt(carbStr);
                        int f = Integer.parseInt(fatStr);

                        if (dietPlan == null) {
                            dietPlan = new DietPlan();
                        }
                        dietPlan.setTargetCalories(k);
                        dietPlan.setTargetProteinGrams(p);
                        dietPlan.setTargetCarbsGrams(c);
                        dietPlan.setTargetFatGrams(f);
                        localDb.saveDietPlan(dietPlan);

                        User user = localDb.getUser();
                        if (user != null) {
                            user.setDailyCaloriesGoal(k);
                            localDb.saveUser(user);
                        }

                        new Thread(() -> {
                            try {
                                new com.fitness.app.repositories.UserRepository().syncLocalDataToFirestore(localDb);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }).start();

                        updateKcalProgress();
                        Toast.makeText(this, "Targets updated successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Goals cannot be empty!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showRegenerateMealPlanConfirmDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Regenerate Meal Plan")
                .setMessage("Are you sure you want to regenerate your meal plan for today? This will override your current meals.")
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
                runOnUiThread(() -> {
                    hideLoading();
                    loadMealsForDate();
                    adapter.notifyDataSetChanged();
                    updateKcalProgress();
                    Toast.makeText(this, "New meal plan generated successfully! 🍽️", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    hideLoading();
                    Toast.makeText(this, "Failed to regenerate meal plan.", Toast.LENGTH_SHORT).show();
                });
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
                Toast.makeText(this, "Cannot copy schedule to the same day!", Toast.LENGTH_SHORT).show();
                return;
            }

            copyScheduleToDate(targetDate);
        });

        datePicker.show(getSupportFragmentManager(), "COPY_DATE_PICKER");
    }

    private void copyScheduleToDate(String targetDate) {
        showLoading("Copying meal schedule to " + targetDate + "...");
        new Thread(() -> {
            try {
                List<LoggedMeal> currentMeals = fitnessDao.getLoggedMealsForDate(selectedDate);
                if (currentMeals == null || currentMeals.isEmpty()) {
                    runOnUiThread(() -> {
                        hideLoading();
                        Toast.makeText(this, "No meals found on this day to copy!", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

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

                int totalConsumed = 0;
                java.util.List<LoggedMeal> targetMeals = fitnessDao.getLoggedMealsForDate(targetDate);
                for (LoggedMeal m : targetMeals) {
                    if (m.isChecked) {
                        totalConsumed += m.calories;
                    }
                }
                com.fitness.app.models.ProgressLog targetLog = localDb.getProgressLog(targetDate);
                targetLog.setCaloriesConsumed(totalConsumed);
                targetLog.setUpdatedAt(System.currentTimeMillis());
                localDb.saveProgressLog(targetLog);

                new com.fitness.app.repositories.UserRepository().syncLocalDataToFirestore(localDb);

                runOnUiThread(() -> {
                    hideLoading();
                    Toast.makeText(this, "Schedule copied to " + targetDate + " successfully!", Toast.LENGTH_SHORT).show();
                    if (targetDate.equals(selectedDate)) {
                        loadMealsForDate();
                        adapter.notifyDataSetChanged();
                        updateKcalProgress();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    hideLoading();
                    Toast.makeText(this, "Error copying schedule.", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }



    private void exportMealPlan() {
        if (dbMeals == null || dbMeals.isEmpty()) {
            Toast.makeText(this, "No meals to export for this day!", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = localDb.getUser();
        String goal = user != null ? user.getGoal() : "Maintenance";

        StringBuilder sb = new StringBuilder();
        sb.append("My Meal Plan for ").append(selectedDate).append("\n");
        sb.append("Fitness Goal: ").append(goal).append("\n\n");

        for (LoggedMeal m : dbMeals) {
            sb.append("[").append(m.mealType).append("] ").append(m.name).append(" - ").append(m.mealTime).append("\n");
            sb.append("Calories: ").append(m.calories).append(" kcal | P: ").append(m.protein).append("g, C: ").append(m.carbs).append("g, F: ").append(m.fat).append("g\n");
            if (m.ingredients != null && !m.ingredients.trim().isEmpty()) {
                sb.append("Ingredients: ").append(m.ingredients).append("\n");
            }
            if (m.notes != null && !m.notes.trim().isEmpty()) {
                sb.append("Note: ").append(m.notes).append("\n");
            }
            sb.append("\n");
        }

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, "Export Meal Plan");
        startActivity(shareIntent);
    }

    private void refreshData() {
        showLoading("Syncing with cloud...");
        new Thread(() -> {
            try {
                Thread.sleep(800);
                new com.fitness.app.repositories.UserRepository().syncLocalDataToFirestore(localDb);
                dbMeals = fitnessDao.getLoggedMealsForDate(selectedDate);
                runOnUiThread(() -> {
                    hideLoading();
                    loadMealsForDate();
                    adapter.notifyDataSetChanged();
                    updateKcalProgress();
                    Toast.makeText(this, "Data refreshed and synced with cloud! 🔄", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    hideLoading();
                    Toast.makeText(this, "Sync failed, displaying local cache.", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
}

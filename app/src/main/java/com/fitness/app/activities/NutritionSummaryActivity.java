package com.fitness.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fitness.app.R;
import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.data.room.AppDatabase;
import com.fitness.app.data.room.FitnessDao;
import com.fitness.app.data.room.LoggedMeal;
import com.fitness.app.models.ProgressLog;
import com.fitness.app.models.User;
import com.fitness.app.models.WorkoutLog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class NutritionSummaryActivity extends AppCompatActivity {

    private String selectedDate;
    private FitnessDao fitnessDao;
    private LocalDataManager localDb;

    // Summary Card Views
    private TextView tvSummaryDate, tvCaloriePercent, tvCaloriesConsumedVal, tvCalorieGoalVal;
    private com.google.android.material.progressindicator.LinearProgressIndicator pbCalorieProgress;
    private TextView tvCalorieRemainingText, tvCalorieBurnedText;

    // Core Macros
    private TextView tvProteinVal, tvProteinRemaining;
    private com.google.android.material.progressindicator.LinearProgressIndicator pbProteinProgress;
    private TextView tvCarbsVal, tvCarbsRemaining;
    private com.google.android.material.progressindicator.LinearProgressIndicator pbCarbsProgress;
    private TextView tvFatVal, tvFatRemaining;
    private com.google.android.material.progressindicator.LinearProgressIndicator pbFatProgress;

    // Other Nutrients
    private TextView tvFiberSummary, tvSugarSummary, tvSodiumSummary, tvWaterSummary;
    private com.google.android.material.progressindicator.LinearProgressIndicator pbFiberProgress, pbWaterProgress;

    // AI Score & Coach
    private TextView tvFoodScore, tvFoodScoreReason, tvAiCoachMessage;

    // Daily Goals Status Checklists
    private ImageView ivCalorieGoalCheck, ivProteinGoalCheck, ivCarbsGoalCheck, ivFatGoalCheck, ivWaterGoalCheck;
    private TextView tvCalorieGoalStatus, tvProteinGoalStatus, tvCarbsGoalStatus, tvFatGoalStatus, tvWaterGoalStatus;

    // Lists & Sources
    private LinearLayout llMealsListContainer;
    private TextView tvProteinSources, tvCarbSources, tvFatSources, tvFiberSources;

    // Weekly Chart representers
    private TextView tvYesterdayCalValue, tvTodayCalValue, tvAvgCalValue, tvWeeklyTrendDesc;
    private View vBarYesterday, vBarToday, vBarAvg;

    // Performance Metrics
    private TextView tvMealsPlanned, tvMealsCompleted, tvMealsMissed, tvCompletionRate, tvLoggingStreak, tvWorkoutBurnedMetric, tvNetCalories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_nutrition_summary);

        localDb = new LocalDataManager(this);
        fitnessDao = AppDatabase.getInstance(this).fitnessDao();

        selectedDate = getIntent().getStringExtra("selected_date");
        if (selectedDate == null) {
            selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        }

        // Bind layout views
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        tvSummaryDate = findViewById(R.id.tvSummaryDate);
        tvCaloriePercent = findViewById(R.id.tvCaloriePercent);
        tvCaloriesConsumedVal = findViewById(R.id.tvCaloriesConsumedVal);
        tvCalorieGoalVal = findViewById(R.id.tvCalorieGoalVal);
        pbCalorieProgress = findViewById(R.id.pbCalorieProgress);
        tvCalorieRemainingText = findViewById(R.id.tvCalorieRemainingText);
        tvCalorieBurnedText = findViewById(R.id.tvCalorieBurnedText);

        tvProteinVal = findViewById(R.id.tvProteinVal);
        tvProteinRemaining = findViewById(R.id.tvProteinRemaining);
        pbProteinProgress = findViewById(R.id.pbProteinProgress);

        tvCarbsVal = findViewById(R.id.tvCarbsVal);
        tvCarbsRemaining = findViewById(R.id.tvCarbsRemaining);
        pbCarbsProgress = findViewById(R.id.pbCarbsProgress);

        tvFatVal = findViewById(R.id.tvFatVal);
        tvFatRemaining = findViewById(R.id.tvFatRemaining);
        pbFatProgress = findViewById(R.id.pbFatProgress);

        tvFiberSummary = findViewById(R.id.tvFiberSummary);
        pbFiberProgress = findViewById(R.id.pbFiberProgress);
        tvSugarSummary = findViewById(R.id.tvSugarSummary);
        tvSodiumSummary = findViewById(R.id.tvSodiumSummary);
        tvWaterSummary = findViewById(R.id.tvWaterSummary);
        pbWaterProgress = findViewById(R.id.pbWaterProgress);

        tvFoodScore = findViewById(R.id.tvFoodScore);
        tvFoodScoreReason = findViewById(R.id.tvFoodScoreReason);
        tvAiCoachMessage = findViewById(R.id.tvAiCoachMessage);

        ivCalorieGoalCheck = findViewById(R.id.ivCalorieGoalCheck);
        ivProteinGoalCheck = findViewById(R.id.ivProteinGoalCheck);
        ivCarbsGoalCheck = findViewById(R.id.ivCarbsGoalCheck);
        ivFatGoalCheck = findViewById(R.id.ivFatGoalCheck);
        ivWaterGoalCheck = findViewById(R.id.ivWaterGoalCheck);

        tvCalorieGoalStatus = findViewById(R.id.tvCalorieGoalStatus);
        tvProteinGoalStatus = findViewById(R.id.tvProteinGoalStatus);
        tvCarbsGoalStatus = findViewById(R.id.tvCarbsGoalStatus);
        tvFatGoalStatus = findViewById(R.id.tvFatGoalStatus);
        tvWaterGoalStatus = findViewById(R.id.tvWaterGoalStatus);

        llMealsListContainer = findViewById(R.id.llMealsListContainer);

        tvProteinSources = findViewById(R.id.tvProteinSources);
        tvCarbSources = findViewById(R.id.tvCarbSources);
        tvFatSources = findViewById(R.id.tvFatSources);
        tvFiberSources = findViewById(R.id.tvFiberSources);

        tvYesterdayCalValue = findViewById(R.id.tvYesterdayCalValue);
        tvTodayCalValue = findViewById(R.id.tvTodayCalValue);
        tvAvgCalValue = findViewById(R.id.tvAvgCalValue);
        tvWeeklyTrendDesc = findViewById(R.id.tvWeeklyTrendDesc);
        vBarYesterday = findViewById(R.id.vBarYesterday);
        vBarToday = findViewById(R.id.vBarToday);
        vBarAvg = findViewById(R.id.vBarAvg);

        tvMealsPlanned = findViewById(R.id.tvMealsPlanned);
        tvMealsCompleted = findViewById(R.id.tvMealsCompleted);
        tvMealsMissed = findViewById(R.id.tvMealsMissed);
        tvCompletionRate = findViewById(R.id.tvCompletionRate);
        tvLoggingStreak = findViewById(R.id.tvLoggingStreak);
        tvWorkoutBurnedMetric = findViewById(R.id.tvWorkoutBurnedMetric);
        tvNetCalories = findViewById(R.id.tvNetCalories);

        // Load & Compute data
        loadAndPopulateSummary();
    }

    private void loadAndPopulateSummary() {
        // Set Friendly Date header text
        try {
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date d = parser.parse(selectedDate);
            if (d != null) {
                SimpleDateFormat formatter = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
                tvSummaryDate.setText(formatter.format(d));
            } else {
                tvSummaryDate.setText(selectedDate);
            }
        } catch (Exception e) {
            tvSummaryDate.setText(selectedDate);
        }

        // Fetch user information
        User user = localDb.getUser();
        if (user == null) {
            Toast.makeText(this, "Profile not initialized", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Mifflin-St Jeor daily targets
        int targetCalories = user.getDailyCaloriesGoal();
        if (targetCalories <= 0) {
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
                targetCalories = (int) (tdee - 500);
            } else if (goal.contains("Gain") || goal.contains("Muscle")) {
                targetCalories = (int) (tdee + 350);
            } else {
                targetCalories = (int) tdee;
            }

            int minCal = "Male".equalsIgnoreCase(gender) ? 1500 : 1200;
            if (targetCalories < minCal) targetCalories = minCal;
        }

        // Split macros targets
        String dietPref = user.getDietaryPreference() != null ? user.getDietaryPreference() : "Balanced";
        String userGoal = user.getGoal() != null ? user.getGoal() : "Maintain Weight";
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
        } else if (userGoal.contains("Gain") || userGoal.contains("Muscle") || userGoal.contains("Performance")) {
            proteinPercent = 35;
            carbsPercent = 45;
            fatPercent = 20;
        } else if (userGoal.contains("Loss") || userGoal.contains("Fat")) {
            proteinPercent = 40;
            carbsPercent = 30;
            fatPercent = 30;
        }

        int targetProtein = (int) ((targetCalories * (proteinPercent / 100.0)) / 4.0);
        int targetCarbs = (int) ((targetCalories * (carbsPercent / 100.0)) / 4.0);
        int targetFat = (int) ((targetCalories * (fatPercent / 100.0)) / 9.0);
        int targetFiber = (int) (targetCalories * 0.014); // 14g per 1000 kcal

        // Fetch logged items from DAO
        List<LoggedMeal> dayMeals = new ArrayList<>();
        try {
            dayMeals = fitnessDao.getLoggedMealsForDate(selectedDate);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int totalCount = dayMeals != null ? dayMeals.size() : 0;
        int completedCount = 0;

        int consumedCalories = 0;
        int consumedProtein = 0;
        int consumedCarbs = 0;
        int consumedFat = 0;
        int consumedFiber = 0;
        int consumedSugar = 0;
        int consumedSodium = 0;

        // Trace food source names
        List<String> proteinsFound = new ArrayList<>();
        List<String> carbsFound = new ArrayList<>();
        List<String> fatsFound = new ArrayList<>();
        List<String> fibersFound = new ArrayList<>();

        if (dayMeals != null && !dayMeals.isEmpty()) {
            llMealsListContainer.removeAllViews();
            LayoutInflater inflater = LayoutInflater.from(this);

            for (LoggedMeal m : dayMeals) {
                if (m.isChecked) {
                    completedCount++;
                    consumedCalories += m.calories;
                    consumedProtein += m.protein;
                    consumedCarbs += m.carbs;
                    consumedFat += m.fat;
                    consumedFiber += m.fiber;
                    consumedSugar += m.sugar;
                    // Sodium placeholder sum or default to 280mg per meal
                    consumedSodium += (m.calories * 0.7); 

                    // Extract food sources
                    parseAndMapSources(m.name, m.ingredients, proteinsFound, carbsFound, fatsFound, fibersFound);

                    // Add custom list view item
                    View mealView = inflater.inflate(R.layout.item_meal_schedule_slot, llMealsListContainer, false);
                    TextView tvTitle = mealView.findViewById(R.id.tvTitle);
                    TextView tvTime = mealView.findViewById(R.id.tvTime);
                    TextView tvCalories = mealView.findViewById(R.id.tvCalories);
                    TextView tvMealEmoji = mealView.findViewById(R.id.tvMealEmoji);
                    TextView tvTitleCheckmark = mealView.findViewById(R.id.tvTitleCheckmark);

                    tvTitle.setText(m.name);
                    tvTime.setText(m.mealTime != null ? m.mealTime : "12:00 PM");
                    tvCalories.setText(m.calories + " kcal");
                    tvMealEmoji.setText(MealScheduleActivity.getMealEmoji(m.iconResId, m.name));
                    tvTitleCheckmark.setVisibility(View.VISIBLE);

                    // Prevent click navigation inside summary report details list to avoid stack
                    mealView.findViewById(R.id.btnNavigateDetail).setVisibility(View.GONE);

                    llMealsListContainer.addView(mealView);
                }
            }
        }

        if (completedCount == 0) {
            TextView emptyText = new TextView(this);
            emptyText.setText("No completed meals logged for this date.");
            emptyText.setTextColor(0xFF64748B);
            emptyText.setPadding(16, 20, 16, 20);
            llMealsListContainer.addView(emptyText);
        }

        // Workout calories burned calculation
        int caloriesBurned = 0;
        try {
            ProgressLog progress = localDb.getProgressLog(selectedDate);
            if (progress != null) {
                caloriesBurned = progress.getCaloriesBurned();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Remaining calculations
        int remainingCalories = targetCalories - consumedCalories + caloriesBurned;
        if (remainingCalories < 0) remainingCalories = 0;

        int kcalPercentVal = targetCalories > 0 ? (int) Math.round((double) consumedCalories / targetCalories * 100) : 0;

        // Set Calorie Summary details
        tvCaloriePercent.setText(kcalPercentVal + "%");
        tvCaloriesConsumedVal.setText(String.format(Locale.getDefault(), "%,d", consumedCalories));
        tvCalorieGoalVal.setText(" / " + String.format(Locale.getDefault(), "%,d kcal", targetCalories));
        pbCalorieProgress.setMax(targetCalories);
        pbCalorieProgress.setProgress(Math.min(consumedCalories, targetCalories));
        tvCalorieRemainingText.setText("Remaining: " + String.format(Locale.getDefault(), "%,d kcal", remainingCalories));
        tvCalorieBurnedText.setText("Workout Burned: +" + caloriesBurned + " kcal");

        // Set Macro fields
        tvProteinVal.setText(consumedProtein + " / " + targetProtein + "g");
        pbProteinProgress.setMax(targetProtein > 0 ? targetProtein : 1);
        pbProteinProgress.setProgress(Math.min(consumedProtein, targetProtein));
        tvProteinRemaining.setText("Rem: " + Math.max(0, targetProtein - consumedProtein) + "g");

        tvCarbsVal.setText(consumedCarbs + " / " + targetCarbs + "g");
        pbCarbsProgress.setMax(targetCarbs > 0 ? targetCarbs : 1);
        pbCarbsProgress.setProgress(Math.min(consumedCarbs, targetCarbs));
        tvCarbsRemaining.setText("Rem: " + Math.max(0, targetCarbs - consumedCarbs) + "g");

        tvFatVal.setText(consumedFat + " / " + targetFat + "g");
        pbFatProgress.setMax(targetFat > 0 ? targetFat : 1);
        pbFatProgress.setProgress(Math.min(consumedFat, targetFat));
        tvFatRemaining.setText("Rem: " + Math.max(0, targetFat - consumedFat) + "g");

        // Set Other nutrients Summary values
        tvFiberSummary.setText(consumedFiber + "g / " + targetFiber + "g (" + (targetFiber > 0 ? (consumedFiber * 100 / targetFiber) : 0) + "%)");
        pbFiberProgress.setMax(targetFiber > 0 ? targetFiber : 1);
        pbFiberProgress.setProgress(Math.min(consumedFiber, targetFiber));

        tvSugarSummary.setText(consumedSugar + "g / 50g max");
        tvSodiumSummary.setText(consumedSodium + "mg / 2,300mg");

        // Water summary lookup
        int waterIntake = 0;
        int waterGoal = user.getDailyWaterGoal() > 0 ? user.getDailyWaterGoal() : 2500;
        try {
            ProgressLog progressLog = localDb.getProgressLog(selectedDate);
            if (progressLog != null) {
                waterIntake = progressLog.getWaterConsumedMl();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        int waterPercentVal = waterGoal > 0 ? (int) Math.round((double) waterIntake / waterGoal * 100) : 0;
        tvWaterSummary.setText(waterIntake + "ml / " + waterGoal + "ml (" + waterPercentVal + "%)");
        pbWaterProgress.setMax(waterGoal > 0 ? waterGoal : 1);
        pbWaterProgress.setProgress(Math.min(waterIntake, waterGoal));

        // Daily Checklist checkboxes setup
        ivCalorieGoalCheck.setImageResource(consumedCalories >= targetCalories && targetCalories > 0 ? R.drawable.ic_check_circle : R.drawable.ic_circle_empty);
        tvCalorieGoalStatus.setText("Calories Target: " + consumedCalories + " / " + targetCalories + " kcal (" + kcalPercentVal + "% completed)");

        ivProteinGoalCheck.setImageResource(consumedProtein >= targetProtein && targetProtein > 0 ? R.drawable.ic_check_circle : R.drawable.ic_circle_empty);
        tvProteinGoalStatus.setText("Protein Target: " + consumedProtein + " / " + targetProtein + "g (" + (targetProtein > 0 ? (consumedProtein * 100 / targetProtein) : 0) + "% completed)");

        ivCarbsGoalCheck.setImageResource(consumedCarbs >= targetCarbs && targetCarbs > 0 ? R.drawable.ic_check_circle : R.drawable.ic_circle_empty);
        tvCarbsGoalStatus.setText("Carbs Target: " + consumedCarbs + " / " + targetCarbs + "g (" + (targetCarbs > 0 ? (consumedCarbs * 100 / targetCarbs) : 0) + "% completed)");

        ivFatGoalCheck.setImageResource(consumedFat >= targetFat && targetFat > 0 ? R.drawable.ic_check_circle : R.drawable.ic_circle_empty);
        tvFatGoalStatus.setText("Fats Target: " + consumedFat + " / " + targetFat + "g (" + (targetFat > 0 ? (consumedFat * 100 / targetFat) : 0) + "% completed)");

        ivWaterGoalCheck.setImageResource(waterIntake >= waterGoal && waterGoal > 0 ? R.drawable.ic_check_circle : R.drawable.ic_circle_empty);
        tvWaterGoalStatus.setText("Water Goal: " + waterIntake + " / " + waterGoal + " ml (" + waterPercentVal + "% completed)");

        // Set Macro Food Sources values (Fallback if empty list found)
        tvProteinSources.setText(proteinsFound.isEmpty() ? "Egg whites, Greek Yogurt, Whey Protein" : joinStringList(proteinsFound));
        tvCarbSources.setText(carbsFound.isEmpty() ? "Oats, Blueberries, Honey, Brown Rice" : joinStringList(carbsFound));
        tvFatSources.setText(fatsFound.isEmpty() ? "Chia Seeds, Almonds, Avocado" : joinStringList(fatsFound));
        tvFiberSources.setText(fibersFound.isEmpty() ? "Chia Seeds, Mixed Berries, Vegetables" : joinStringList(fibersFound));

        // Food Quality Score calculation
        int finalScore = 100;
        List<String> reasons = new ArrayList<>();
        if (consumedProtein >= targetProtein * 0.9) {
            reasons.add("Excellent protein intake.");
        } else {
            finalScore -= 10;
            reasons.add("Protein intake slightly deficient.");
        }
        if (consumedFiber >= targetFiber * 0.8) {
            reasons.add("High fiber source diversity.");
        } else {
            finalScore -= 12;
            reasons.add("Fiber slightly low.");
        }
        if (consumedSugar > 60) {
            finalScore -= 15;
            reasons.add("High added sugars.");
        } else {
            reasons.add("Good carbohydrate balance.");
        }
        if (consumedCalories > targetCalories + 300) {
            finalScore -= 8;
            reasons.add("Exceeded daily calorie goal limit.");
        }
        if (finalScore < 30) finalScore = 30;

        tvFoodScore.setText(String.valueOf(finalScore));
        tvFoodScoreReason.setText(reasons.isEmpty() ? "Perfect calorie and macronutrient balance." : joinReasons(reasons));

        // Detailed AI Nutrition Coach Analysis
        StringBuilder advice = new StringBuilder();
        advice.append("Excellent effort logging your food choices for ")
                .append(tvSummaryDate.getText().toString())
                .append(". You completed ")
                .append(kcalPercentVal)
                .append("% of your calorie limit and ")
                .append(targetProtein > 0 ? (consumedProtein * 100 / targetProtein) : 0)
                .append("% of your protein target. ");

        if (consumedProtein < targetProtein * 0.7) {
            advice.append("To support your ")
                    .append(userGoal.toLowerCase())
                    .append(" goals, consider incorporating extra protein elements like egg whites, tofu, or lean fish. ");
        } else {
            advice.append("Your protein intake is optimal for recovery and lean mass preservation. ");
        }

        if (consumedFiber < targetFiber * 0.7) {
            advice.append("Your dietary fiber remains low. Try incorporating seeds (chia/flax), kale, or quinoa. ");
        }

        if (waterIntake < waterGoal * 0.7) {
            advice.append("Drinking an additional 500–700 ml of filtered water is recommended to aid macro digestion.");
        } else {
            advice.append("Your hydration levels are great today, which aids metabolism.");
        }
        tvAiCoachMessage.setText(advice.toString());

        // Weekly comparison setup
        int yesterdayCalories = 0;
        int averageCalories = 1580;
        try {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -1);
            String yesterdayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
            ProgressLog yestLog = localDb.getProgressLog(yesterdayStr);
            if (yestLog != null) {
                yesterdayCalories = yestLog.getCaloriesConsumed();
            }
            if (yesterdayCalories == 0) yesterdayCalories = (int) (targetCalories * 0.82); // logical fallback
        } catch (Exception e) {
            yesterdayCalories = (int) (targetCalories * 0.82);
        }

        tvYesterdayCalValue.setText(String.valueOf(yesterdayCalories));
        tvTodayCalValue.setText(String.valueOf(consumedCalories));
        averageCalories = (yesterdayCalories + consumedCalories + (int)(targetCalories * 0.88)) / 3; // rolling mock average
        tvAvgCalValue.setText(String.valueOf(averageCalories));

        // Adjust vertical height representation
        int maxCalRef = Math.max(targetCalories, Math.max(yesterdayCalories, consumedCalories));
        if (maxCalRef <= 0) maxCalRef = 2000;
        
        ViewGroup.LayoutParams lpYes = vBarYesterday.getLayoutParams();
        lpYes.height = (int) (100.0 * yesterdayCalories / maxCalRef * getResources().getDisplayMetrics().density);
        vBarYesterday.setLayoutParams(lpYes);

        ViewGroup.LayoutParams lpTod = vBarToday.getLayoutParams();
        lpTod.height = (int) (100.0 * consumedCalories / maxCalRef * getResources().getDisplayMetrics().density);
        vBarToday.setLayoutParams(lpTod);

        ViewGroup.LayoutParams lpAvg = vBarAvg.getLayoutParams();
        lpAvg.height = (int) (100.0 * averageCalories / maxCalRef * getResources().getDisplayMetrics().density);
        vBarAvg.setLayoutParams(lpAvg);

        tvWeeklyTrendDesc.setText(String.format(Locale.getDefault(), "Weekly Trend: Balanced. Your average consumption is %s kcal (%d%% of daily target goal).", String.format(Locale.getDefault(), "%,d", averageCalories), (int)(averageCalories * 100.0 / targetCalories)));

        // Performance Summary metrics
        tvMealsPlanned.setText(String.valueOf(totalCount));
        tvMealsCompleted.setText(String.valueOf(completedCount));
        tvMealsMissed.setText(String.valueOf(Math.max(0, totalCount - completedCount)));
        tvCompletionRate.setText(totalCount > 0 ? (completedCount * 100 / totalCount) + "%" : "0%");

        int streakValue = localDb.sharedPreferences.getInt("current_streak", 1);
        tvLoggingStreak.setText(streakValue + " day" + (streakValue > 1 ? "s" : ""));
        tvWorkoutBurnedMetric.setText(caloriesBurned + " kcal");
        
        int netCals = consumedCalories - caloriesBurned;
        tvNetCalories.setText(netCals + " kcal");
    }

    private void parseAndMapSources(String name, String ingredients, List<String> p, List<String> c, List<String> f, List<String> fb) {
        String input = ((name != null ? name : "") + ", " + (ingredients != null ? ingredients : "")).toLowerCase(Locale.getDefault());
        
        if (input.contains("egg") || input.contains("omelet") || input.contains("chicken") || input.contains("turkey") || input.contains("salmon") || input.contains("steak") || input.contains("beef") || input.contains("yogurt") || input.contains("tofu") || input.contains("protein")) {
            if (input.contains("egg") && !p.contains("Eggs")) p.add("Eggs");
            if (input.contains("chicken") && !p.contains("Chicken")) p.add("Chicken");
            if (input.contains("turkey") && !p.contains("Turkey")) p.add("Turkey");
            if (input.contains("salmon") && !p.contains("Salmon")) p.add("Salmon");
            if (input.contains("steak") && !p.contains("Beef")) p.add("Beef");
            if (input.contains("yogurt") && !p.contains("Greek Yogurt")) p.add("Greek Yogurt");
            if (input.contains("tofu") && !p.contains("Tofu")) p.add("Tofu");
        }
        
        if (input.contains("oat") || input.contains("rice") || input.contains("bread") || input.contains("honey") || input.contains("fruit") || input.contains("berry") || input.contains("banana") || input.contains("apple")) {
            if (input.contains("oat") && !c.contains("Rolled Oats")) c.add("Rolled Oats");
            if (input.contains("rice") && !c.contains("Brown Rice")) c.add("Brown Rice");
            if (input.contains("honey") && !c.contains("Honey")) c.add("Honey");
            if (input.contains("berry") && !c.contains("Mixed Berries")) c.add("Mixed Berries");
            if (input.contains("banana") && !c.contains("Banana")) c.add("Banana");
        }
        
        if (input.contains("avocado") || input.contains("oil") || input.contains("butter") || input.contains("nut") || input.contains("almond") || input.contains("chia") || input.contains("seed")) {
            if (input.contains("avocado") && !f.contains("Avocado")) f.add("Avocado");
            if (input.contains("oil") && !f.contains("Olive Oil")) f.add("Olive Oil");
            if (input.contains("almond") && !f.contains("Almonds")) f.add("Almonds");
            if (input.contains("chia") && !f.contains("Chia Seeds")) f.add("Chia Seeds");
        }
        
        if (input.contains("spinach") || input.contains("broccoli") || input.contains("vegetable") || input.contains("chia") || input.contains("berry") || input.contains("greens")) {
            if (input.contains("chia") && !fb.contains("Chia Seeds")) fb.add("Chia Seeds");
            if (input.contains("berry") && !fb.contains("Mixed Berries")) fb.add("Mixed Berries");
            if (input.contains("broccoli") && !fb.contains("Broccoli")) fb.add("Broccoli");
            if (input.contains("spinach") && !fb.contains("Spinach")) fb.add("Spinach");
        }
    }

    private String joinStringList(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    private String joinReasons(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }
}

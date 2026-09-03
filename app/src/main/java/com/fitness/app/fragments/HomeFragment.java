package com.fitness.app.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.fitness.app.R;
import com.fitness.app.activities.AICoachActivity;
import com.fitness.app.activities.DetailTrackerActivity;
import com.fitness.app.activities.LogWorkoutActivity;
import com.fitness.app.activities.ProfileActivity;
import com.fitness.app.activities.ProgressTrackerActivity;
import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.models.ProgressLog;
import com.fitness.app.models.User;
import com.fitness.app.models.WorkoutLog;
import com.fitness.app.models.WorkoutPlan;
import com.fitness.app.models.DietPlan;
import com.fitness.app.repositories.UserRepository;
import com.fitness.app.utils.RecommendationEngine;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    private TextView tvGreeting, tvGoalText;
    private TextView tvStepsVal, tvCaloriesVal, tvWaterVal, tvSleepVal;
    private TextView tvStepsPercent, tvCaloriesPercent, tvWaterPercent, tvSleepPercent;
    private ProgressBar pbSteps, pbCalories, pbWater, pbSleep;
    private TextView tvBmiVal, tvBmiTip, tvWeeklyAdvice, tvCoachGreeting, tvCoachMotivation;
    private ImageView ivProfilePic;

    private TextView tvDailyTaskWorkout, tvDailyTaskDiet, tvDailyTaskStatus;
    private android.widget.CheckBox cbTaskSteps, cbTaskWater, cbTaskSleep;
    private com.google.android.material.button.MaterialButton btnCompleteDailyTask;

    // Redesign Beginner Guide & Quick Log views
    private View cardBeginnerGuide;
    private ImageView ivDismissBeginnerGuide;
    private View btnGuideLearnForm, btnGuideGotIt;
    private View btnHomeMealPlanner;
    private View btnHomeQuickAddWater;

    // New Hero Banner views
    private TextView tvHeroGreeting, tvHeroQuote, tvHeroProgressText;
    private ProgressBar pbHeroProgress;
    private com.google.android.material.button.MaterialButton btnStartTodayWorkout;

    private LocalDataManager localDb;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        localDb = new LocalDataManager(requireContext());

        // Bind views
        tvGreeting = view.findViewById(R.id.tvGreeting);
        tvGoalText = view.findViewById(R.id.tvGoalText);
        ivProfilePic = view.findViewById(R.id.ivProfilePic);

        tvStepsVal = view.findViewById(R.id.tvStepsVal);
        tvCaloriesVal = view.findViewById(R.id.tvCaloriesVal);
        tvWaterVal = view.findViewById(R.id.tvWaterVal);
        tvSleepVal = view.findViewById(R.id.tvSleepVal);

        tvStepsPercent = view.findViewById(R.id.tvStepsPercent);
        tvCaloriesPercent = view.findViewById(R.id.tvCaloriesPercent);
        tvWaterPercent = view.findViewById(R.id.tvWaterPercent);
        tvSleepPercent = view.findViewById(R.id.tvSleepPercent);

        pbSteps = view.findViewById(R.id.pbSteps);
        pbCalories = view.findViewById(R.id.pbCalories);
        pbWater = view.findViewById(R.id.pbWater);
        pbSleep = view.findViewById(R.id.pbSleep);

        tvBmiVal = view.findViewById(R.id.tvBmiVal);
        tvBmiTip = view.findViewById(R.id.tvBmiTip);
        tvWeeklyAdvice = view.findViewById(R.id.tvWeeklyAdvice);
        tvCoachGreeting = view.findViewById(R.id.tvCoachGreeting);
        tvCoachMotivation = view.findViewById(R.id.tvCoachMotivation);

        // Bind Daily Task elements
        tvDailyTaskWorkout = view.findViewById(R.id.tvDailyTaskWorkout);
        tvDailyTaskDiet = view.findViewById(R.id.tvDailyTaskDiet);
        tvDailyTaskStatus = view.findViewById(R.id.tvDailyTaskStatus);
        cbTaskSteps = view.findViewById(R.id.cbTaskSteps);
        cbTaskWater = view.findViewById(R.id.cbTaskWater);
        cbTaskSleep = view.findViewById(R.id.cbTaskSleep);
        btnCompleteDailyTask = view.findViewById(R.id.btnCompleteDailyTask);

        btnCompleteDailyTask.setOnClickListener(v -> showAiFeedbackDialog());

        // Bind Hero Banner elements
        tvHeroGreeting = view.findViewById(R.id.tvHeroGreeting);
        tvHeroQuote = view.findViewById(R.id.tvHeroQuote);
        tvHeroProgressText = view.findViewById(R.id.tvHeroProgressText);
        pbHeroProgress = view.findViewById(R.id.pbHeroProgress);
        btnStartTodayWorkout = view.findViewById(R.id.btnStartTodayWorkout);

        btnStartTodayWorkout.setOnClickListener(v -> {
            WorkoutPlan currentWorkout = localDb.getWorkoutPlan();
            String exerciseName = (currentWorkout != null && currentWorkout.getExercises().size() > 0) 
                    ? currentWorkout.getGoal() 
                    : "Full Body Shred";
            Intent intent = new Intent(getActivity(), LogWorkoutActivity.class);
            intent.putExtra("exercise_name", exerciseName);
            intent.putExtra("duration_min", "25");
            intent.putExtra("calories_kcal", "240");
            startActivity(intent);
        });

        // Bind New UX elements
        cardBeginnerGuide = view.findViewById(R.id.cardBeginnerGuide);
        ivDismissBeginnerGuide = view.findViewById(R.id.ivDismissBeginnerGuide);
        btnGuideLearnForm = view.findViewById(R.id.btnGuideLearnForm);
        btnGuideGotIt = view.findViewById(R.id.btnGuideGotIt);
        btnHomeMealPlanner = view.findViewById(R.id.btnHomeMealPlanner);
        btnHomeQuickAddWater = view.findViewById(R.id.btnHomeQuickAddWater);

        // Beginner Guide Logic
        boolean showGuide = localDb.sharedPreferences.getBoolean("show_beginner_guide", true);
        if (showGuide) {
            cardBeginnerGuide.setVisibility(View.VISIBLE);
        } else {
            cardBeginnerGuide.setVisibility(View.GONE);
        }

        ivDismissBeginnerGuide.setOnClickListener(v -> {
            localDb.sharedPreferences.edit().putBoolean("show_beginner_guide", false).apply();
            cardBeginnerGuide.setVisibility(View.GONE);
            android.widget.Toast.makeText(getContext(), "Guide dismissed. Tap 'Form Guides' in Workouts anytime!", android.widget.Toast.LENGTH_SHORT).show();
        });

        btnGuideGotIt.setOnClickListener(v -> {
            localDb.sharedPreferences.edit().putBoolean("show_beginner_guide", false).apply();
            cardBeginnerGuide.setVisibility(View.GONE);
        });

        btnGuideLearnForm.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), com.fitness.app.activities.LearningTutorialActivity.class));
        });

        // Fast Water Log Button
        btnHomeQuickAddWater.setOnClickListener(v -> {
            String todayStr = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
            ProgressLog todayLog = localDb.getProgressLog(todayStr);
            int newWater = todayLog.getWaterConsumedMl() + 250;
            todayLog.setWaterConsumedMl(newWater);
            localDb.saveProgressLog(todayLog);
            android.widget.Toast.makeText(getContext(), "Water added: +250 ml! 💧", android.widget.Toast.LENGTH_SHORT).show();
            loadDashboardData();
        });

        // Smart Meal Planner Scanner Shortcut
        btnHomeMealPlanner.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), com.fitness.app.activities.GroceryScannerActivity.class));
        });

        // Header click -> Switch tab to Profile for consistency
        view.findViewById(R.id.profileCard).setOnClickListener(v -> {
            if (getActivity() instanceof com.fitness.app.activities.MainActivity) {
                com.google.android.material.bottomnavigation.BottomNavigationView nav = getActivity().findViewById(R.id.bottomNavigation);
                if (nav != null) {
                    nav.setSelectedItemId(R.id.navigation_profile);
                }
            } else {
                startActivity(new Intent(getActivity(), ProfileActivity.class));
            }
        });

        // Today's Activity Cards
        view.findViewById(R.id.cardSteps).setOnClickListener(v -> startDetailTracker("steps"));
        view.findViewById(R.id.cardCalories).setOnClickListener(v -> startDetailTracker("calories"));
        view.findViewById(R.id.cardWater).setOnClickListener(v -> startDetailTracker("water"));
        view.findViewById(R.id.cardSleep).setOnClickListener(v -> startActivity(new Intent(getActivity(), com.fitness.app.activities.SleepTrackerActivity.class)));
        view.findViewById(R.id.cardBmi).setOnClickListener(v -> startDetailTracker("bmi"));

        // Daily Recommended Workout Button
        view.findViewById(R.id.btnStartWorkout).setOnClickListener(v -> startFeaturedWorkout("Full Body Shred"));

        // Safe setup for hidden elements (to avoid breaking constraints / compilation)
        View btnAICoach = view.findViewById(R.id.btnAICoach);
        if (btnAICoach != null) {
            btnAICoach.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), AICoachActivity.class);
                intent.putExtra("select_tab", 1);
                startActivity(intent);
            });
        }
        view.findViewById(R.id.btnAICoachChat).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AICoachActivity.class);
            intent.putExtra("select_tab", 1);
            startActivity(intent);
        });
        view.findViewById(R.id.btnOpenWorkoutGenerator).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), com.fitness.app.activities.EquipmentSelectionActivity.class));
        });
        view.findViewById(R.id.btnOpenFitnessChallenges).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), com.fitness.app.activities.ChallengesHubActivity.class));
        });

        view.findViewById(R.id.cardAiFormCheck).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), com.fitness.app.activities.RealTimeFeedbackActivity.class);
            intent.putExtra("exercise_name", "Standard Squats");
            startActivity(intent);
        });
        view.findViewById(R.id.btnStartAiFormCheck).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), com.fitness.app.activities.RealTimeFeedbackActivity.class);
            intent.putExtra("exercise_name", "Standard Squats");
            startActivity(intent);
        });
        view.findViewById(R.id.btnViewSquatHistory).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), com.fitness.app.activities.SquatHistoryActivity.class);
            startActivity(intent);
        });

        View btnAIRecommendations = view.findViewById(R.id.btnAIRecommendations);
        if (btnAIRecommendations != null) {
            btnAIRecommendations.setOnClickListener(v -> {
                startActivity(new Intent(getActivity(), com.fitness.app.activities.AIRecommendationsActivity.class));
            });
        }
        view.findViewById(R.id.btnTransformation).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), com.fitness.app.activities.TransformationIntroActivity.class));
        });
        view.findViewById(R.id.btnLogWorkout).setOnClickListener(v -> startActivity(new Intent(getActivity(), LogWorkoutActivity.class)));
        view.findViewById(R.id.btnLogMetrics).setOnClickListener(v -> startActivity(new Intent(getActivity(), ProgressTrackerActivity.class)));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboardData();
    }

    private void loadDashboardData() {
        User user = localDb.getUser();
        if (user == null) return;

        // Greeting
        String name = user.getFirstName() != null ? user.getFirstName() : "Guest";
        tvGreeting.setText(getString(R.string.home_hello, name));
        
        new Thread(() -> {
            try {
                com.fitness.app.data.room.AppDatabase db = com.fitness.app.data.room.AppDatabase.getInstance(requireContext());
                com.fitness.app.models.UserChallengeStats stats = db.fitnessDao().getUserChallengeStats();
                final int totalXp = stats != null ? stats.getTotalXp() : 0;
                final int level = (totalXp / 300) + 1;
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        String goalText = user.getGoal() != null ? user.getGoal() : "Improve Fitness";
                        tvGoalText.setText(getString(R.string.home_goal, goalText, level, totalXp));
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // Profile pic
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().trim().isEmpty()) {
            Glide.with(this)
                    .load(user.getProfileImageUrl())
                    .placeholder(R.drawable.ic_user)
                    .error(R.drawable.ic_user)
                    .circleCrop()
                    .into(ivProfilePic);
        } else {
            ivProfilePic.setImageResource(R.drawable.ic_user);
        }

        WorkoutPlan currentWorkout = localDb.getWorkoutPlan();
        DietPlan currentDiet = localDb.getDietPlan();
        String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String lastGenDate = localDb.sharedPreferences.getString("last_plan_gen_date", "");

        if (currentWorkout == null || currentDiet == null || !todayStr.equals(lastGenDate)) {
            String diffFeedback = localDb.sharedPreferences.edit().putString("last_plan_gen_date", todayStr).commit() ? "Medium" : "Medium";
            boolean completedYesterday = localDb.sharedPreferences.getBoolean("yesterday_workout_completed", true);
            
            currentWorkout = RecommendationEngine.generateDailyWorkoutPlan(user, diffFeedback, completedYesterday);
            
            boolean cheatMealEnabled = localDb.sharedPreferences.getBoolean("cheat_meal_enabled", false);
            boolean hasWorkoutToday = currentWorkout != null && currentWorkout.getExercises().size() > 0;
            
            currentDiet = RecommendationEngine.generateDailyDietPlan(user, "Yes", completedYesterday, cheatMealEnabled, hasWorkoutToday, todayStr);
            
            localDb.saveWorkoutPlan(currentWorkout);
            localDb.saveDietPlan(currentDiet);
            
            new Thread(() -> {
                try {
                    new UserRepository().syncLocalDataToFirestore(localDb);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }

        if (currentWorkout != null && currentWorkout.getExercises().size() > 0) {
            tvDailyTaskWorkout.setText(getString(R.string.home_workout_task, currentWorkout.getGoal() + " (" + currentWorkout.getExercises().get(0).getName() + " + " + (currentWorkout.getExercises().size() - 1) + " more)"));
        } else {
            tvDailyTaskWorkout.setText(getString(R.string.home_workout_rest));
        }
        if (currentDiet != null) {
            tvDailyTaskDiet.setText(getString(R.string.home_diet_task, currentDiet.getDietType(), currentDiet.getTargetCalories()));
        }

        boolean dailyTaskCompleted = localDb.sharedPreferences.getBoolean("daily_task_completed_" + todayStr, false);
        if (dailyTaskCompleted) {
            tvDailyTaskStatus.setText(getString(R.string.home_task_completed));
            btnCompleteDailyTask.setEnabled(false);
            btnCompleteDailyTask.setText(getString(R.string.home_btn_completed_today));
        } else {
            tvDailyTaskStatus.setText(getString(R.string.home_task_in_progress));
            btnCompleteDailyTask.setEnabled(true);
            btnCompleteDailyTask.setText(getString(R.string.home_btn_complete_tasks));
        }

        // Today's metrics
        String today = todayStr;
        ProgressLog todayLog = localDb.getProgressLog(today);

        // Steps Card
        int steps = todayLog.getStepsCount();
        tvStepsVal.setText(getString(R.string.home_steps_val, String.valueOf(steps)));
        pbSteps.setProgress(steps);
        int stepsPercent = (int) ((steps / 10000.0) * 100);
        if (stepsPercent > 100) stepsPercent = 100;
        tvStepsPercent.setText(stepsPercent >= 100 ? getString(R.string.home_steps_goal) : getString(R.string.home_steps_percent, stepsPercent));

        // Calories Card
        int calories = todayLog.getCaloriesBurned();
        tvCaloriesVal.setText(getString(R.string.home_calories_val, String.valueOf(calories)));
        pbCalories.setProgress(calories);
        int calPercent = (int) ((calories / 2000.0) * 100);
        if (calPercent > 100) calPercent = 100;
        tvCaloriesPercent.setText(getString(R.string.home_calories_percent, calPercent));

        // Water Card
        int water = todayLog.getWaterConsumedMl();
        tvWaterVal.setText(getString(R.string.home_water_val, String.valueOf(water)));
        pbWater.setProgress(water);
        int waterPercent = (int) ((water / 2500.0) * 100);
        if (waterPercent > 100) waterPercent = 100;
        tvWaterPercent.setText(waterPercent >= 100 ? getString(R.string.home_water_goal) : getString(R.string.home_water_percent, waterPercent));

        // Sleep Card
        int sleepMin = todayLog.getSleepDurationMinutes();
        int hours = sleepMin / 60;
        int minutes = sleepMin % 60;
        tvSleepVal.setText(getString(R.string.home_sleep_val, hours, minutes));
        pbSleep.setProgress(sleepMin);
        int sleepPercent = (int) ((sleepMin / 480.0) * 100);
        if (sleepPercent > 100) sleepPercent = 100;
        tvSleepPercent.setText(sleepPercent >= 100 ? getString(R.string.home_sleep_goal) : getString(R.string.home_sleep_percent, sleepPercent));

        // CheckBox goals state
        cbTaskSteps.setChecked(steps >= 10000);
        cbTaskWater.setChecked(water >= 2500);
        cbTaskSleep.setChecked(sleepMin >= 480);

        cbTaskSteps.setEnabled(steps < 10000);
        cbTaskWater.setEnabled(water < 2500);
        cbTaskSleep.setEnabled(sleepMin < 480);

        // BMI Card
        double weight = user.getWeight() > 0 ? user.getWeight() : todayLog.getCurrentWeight();
        double height = user.getHeight() > 0 ? user.getHeight() : todayLog.getCurrentHeight();
        if (weight > 0 && height > 0) {
            double heightM = height / 100.0;
            double bmi = weight / (heightM * heightM);
            String bmiClass = getBmiCategory(bmi);
            tvBmiVal.setText(getString(R.string.home_bmi_val, String.format(Locale.getDefault(), "%.1f (%s)", bmi, bmiClass)));
            tvBmiTip.setText(getBmiTip(bmiClass));
            tvBmiTip.setTextColor(getBmiColor(bmiClass));
        } else {
            tvBmiVal.setText(getString(R.string.home_bmi_val, "N/A"));
            tvBmiTip.setText(getString(R.string.home_bmi_prompt));
            tvBmiTip.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }

        // AI Coach Insights Greetings & Advice
        tvCoachGreeting.setText(String.format("Hi %s,", name));

        String[] motivations = {
            "“Success starts with self-discipline.”",
            "“Your only limit is you.”",
            "“Don't stop when you're tired. Stop when you're done.”",
            "“Believe you can and you're halfway there.”",
            "“Action is the foundational key to all success.”",
            "“Quality sleep is the driver of performance.”"
        };
        int dayIndex = Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % motivations.length;
        tvCoachMotivation.setText(motivations[dayIndex]);

        if (localDb.isBodyShamingProtectionEnabled()) {
            tvWeeklyAdvice.setText(getEmotionalSupportMessage(user.getGender()));
        } else {
            // Dynamic personalized recommendation logic based on logs
            String specificAdvice = null;
            if (water < 1500) {
                specificAdvice = "You're behind on hydration. Drink another 500 ml of water today.";
            } else if (sleepMin > 0 && sleepMin < 360) {
                specificAdvice = "You slept only " + (sleepMin / 60) + " hours. Aim for at least 7–8 hours to improve recovery.";
            } else if (steps > 0 && steps < 5000) {
                specificAdvice = "You're at " + steps + " steps today. Take a quick walk to keep active!";
            } else {
                List<WorkoutLog> workoutLogs = localDb.getAllWorkoutLogs();
                if (workoutLogs.isEmpty()) {
                    specificAdvice = "You missed yesterday's workout. Let's complete a quick session today.";
                }
            }

            if (specificAdvice != null) {
                tvWeeklyAdvice.setText(specificAdvice);
            } else {
                Map<String, ProgressLog> logs = localDb.getAllProgressLogs();
                List<String> advices = RecommendationEngine.getRecommendations(user, logs);
                if (!advices.isEmpty()) {
                    tvWeeklyAdvice.setText(advices.get(0));
                } else {
                    tvWeeklyAdvice.setText("Great work! Stay consistent and log your activity daily.");
                }
            }
        }

        // --- NEW: Calculate and display Hero Banner data ---
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greetingPrefix;
        if (currentHour < 12) {
            greetingPrefix = "Good Morning";
        } else if (currentHour < 17) {
            greetingPrefix = "Good Afternoon";
        } else {
            greetingPrefix = "Good Evening";
        }
        tvHeroGreeting.setText(String.format("%s, %s!", greetingPrefix, name));
        tvHeroQuote.setText(motivations[dayIndex]);

        int totalGoals = 5;
        int completedGoals = 0;

        if (steps >= 10000) completedGoals++;
        if (water >= 2500) completedGoals++;
        if (sleepMin >= 480) completedGoals++;

        if (dailyTaskCompleted) completedGoals++;

        List<WorkoutLog> todayWorkoutLogs = localDb.getAllWorkoutLogs();
        boolean loggedWorkoutToday = false;
        for (WorkoutLog wl : todayWorkoutLogs) {
            if (todayStr.equals(wl.getDate())) {
                loggedWorkoutToday = true;
                break;
            }
        }
        if (loggedWorkoutToday) completedGoals++;
        int progressPercent = (completedGoals * 100) / totalGoals;
        pbHeroProgress.setProgress(progressPercent);
        tvHeroProgressText.setText(String.format("Today's Progress: %d%% (%d/%d completed)", progressPercent, completedGoals, totalGoals));
    }

    private void startDetailTracker(String type) {
        Intent intent = new Intent(getActivity(), DetailTrackerActivity.class);
        intent.putExtra("tracker_type", type);
        startActivity(intent);
    }

    private void startFeaturedWorkout(String name) {
        Intent intent = new Intent(getActivity(), LogWorkoutActivity.class);
        intent.putExtra("is_featured", true);
        intent.putExtra("exercise_name", name);

        if ("Full Body Shred".equalsIgnoreCase(name)) {
            intent.putExtra("featured_stats", "25 Mins • Intermediate • 240 kcal");
            intent.putExtra("featured_desc", "A high-intensity, metabolic conditioning workout targeting major muscle groups to maximize calorie burn.");
            intent.putExtra("illustration_res", R.drawable.onboarding_2);
            intent.putExtra("calories_kcal", 240);
            intent.putExtra("duration_min", 25);
            intent.putExtra("difficulty", "Intermediate");
        } else if ("Upper Body Builder".equalsIgnoreCase(name)) {
            intent.putExtra("featured_stats", "30 Mins • Beginner • 210 kcal");
            intent.putExtra("featured_desc", "Focus on building strength and definition in your chest, back, shoulders, and arms using controlled dumbbell movements.");
            intent.putExtra("illustration_res", R.drawable.onboarding_1);
            intent.putExtra("calories_kcal", 210);
            intent.putExtra("duration_min", 30);
            intent.putExtra("difficulty", "Beginner");
        } else {
            intent.putExtra("featured_stats", "20 Mins • Advanced • 250 kcal");
            intent.putExtra("featured_desc", "Explosive abdominal and aerobic movements designed to sculpt your core while boosting cardiovascular endurance.");
            intent.putExtra("illustration_res", R.drawable.onboarding_3);
            intent.putExtra("calories_kcal", 250);
            intent.putExtra("duration_min", 20);
            intent.putExtra("difficulty", "Advanced");
        }
        startActivity(intent);
    }

    private String getBmiCategory(double bmi) {
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25) return "Normal";
        if (bmi < 30) return "Overweight";
        return "Obese";
    }

    private String getBmiTip(String category) {
        switch (category) {
            case "Underweight":
                return "Increase your healthy calorie intake.";
            case "Normal":
                return "Healthy range! Keep it up.";
            case "Overweight":
                return "Focus on regular exercise and balanced nutrition.";
            default:
                return "Consistent calorie deficit & active recovery advised.";
        }
    }

    private int getBmiColor(String category) {
        switch (category) {
            case "Normal":
                return 0xFF2563EB; // Blue Success Color
            case "Underweight":
                return 0xFFFFB703; // Accent Yellow Color
            default:
                return 0xFFEF4444; // Error Red Color
        }
    }

    private String getEmotionalSupportMessage(String gender) {
        String[] general = {
            "Your worth is not defined by a scale. You are valuable, strong, and capable of amazing things.",
            "Treat your body with kindness and respect today. Every positive step you take is a win.",
            "Focus on how you feel—your energy, your strength, and your peace of mind. That is true fitness.",
            "Be patient and gentle with yourself. You are building healthy habits for a lifetime."
        };
        String[] female = {
            "You are strong, confident, and beautiful exactly as you are. FitTrain stands with you to celebrate your inner strength.",
            "Embrace your unique journey. Your self-respect and peace of mind are the most important goals.",
            "Nourish your body and soul. Health is about self-love, strength, and vitality, never comparison."
        };

        if ("Female".equalsIgnoreCase(gender)) {
            return female[(int) (Math.random() * female.length)];
        } else {
            return general[(int) (Math.random() * general.length)];
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            loadDashboardData();
        }
    }

    private void showAiFeedbackDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_ai_feedback, null);
        builder.setView(view);

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        com.google.android.material.button.MaterialButton btnEasy = view.findViewById(R.id.btnFeedbackEasy);
        com.google.android.material.button.MaterialButton btnMedium = view.findViewById(R.id.btnFeedbackMedium);
        com.google.android.material.button.MaterialButton btnHard = view.findViewById(R.id.btnFeedbackHard);
        com.google.android.material.switchmaterial.SwitchMaterial switchDiet = view.findViewById(R.id.switchFeedbackDiet);
        com.google.android.material.switchmaterial.SwitchMaterial switchWater = view.findViewById(R.id.switchFeedbackWater);
        com.google.android.material.slider.Slider sliderFeeling = view.findViewById(R.id.sliderFeedbackFeeling);
        com.google.android.material.button.MaterialButton btnCancel = view.findViewById(R.id.btnFeedbackCancel);
        com.google.android.material.button.MaterialButton btnSubmit = view.findViewById(R.id.btnFeedbackSubmit);

        final String[] selectedDifficulty = {"Medium"};

        java.lang.Runnable updateDifficultySelection = () -> {
            if ("Easy".equals(selectedDifficulty[0])) {
                btnEasy.setBackgroundColor(getResources().getColor(R.color.primary));
                btnEasy.setTextColor(getResources().getColor(android.R.color.white));
                btnMedium.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                btnMedium.setTextColor(getResources().getColor(R.color.primary));
                btnHard.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                btnHard.setTextColor(getResources().getColor(R.color.primary));
            } else if ("Medium".equals(selectedDifficulty[0])) {
                btnEasy.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                btnEasy.setTextColor(getResources().getColor(R.color.primary));
                btnMedium.setBackgroundColor(getResources().getColor(R.color.primary));
                btnMedium.setTextColor(getResources().getColor(android.R.color.white));
                btnHard.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                btnHard.setTextColor(getResources().getColor(R.color.primary));
            } else {
                btnEasy.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                btnEasy.setTextColor(getResources().getColor(R.color.primary));
                btnMedium.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                btnMedium.setTextColor(getResources().getColor(R.color.primary));
                btnHard.setBackgroundColor(getResources().getColor(R.color.primary));
                btnHard.setTextColor(getResources().getColor(android.R.color.white));
            }
        };

        btnEasy.setOnClickListener(v -> {
            selectedDifficulty[0] = "Easy";
            updateDifficultySelection.run();
        });
        btnMedium.setOnClickListener(v -> {
            selectedDifficulty[0] = "Medium";
            updateDifficultySelection.run();
        });
        btnHard.setOnClickListener(v -> {
            selectedDifficulty[0] = "Hard";
            updateDifficultySelection.run();
        });

        updateDifficultySelection.run();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSubmit.setOnClickListener(v -> {
            String diff = selectedDifficulty[0];
            boolean dietDone = switchDiet.isChecked();
            boolean waterDone = switchWater.isChecked();
            float energy = sliderFeeling.getValue();

            SharedPreferences.Editor editor = localDb.sharedPreferences.edit();
            editor.putString("last_workout_difficulty", diff);
            editor.putBoolean("yesterday_workout_completed", true);
            editor.putBoolean("yesterday_diet_completed", dietDone);
            editor.putBoolean("yesterday_water_completed", waterDone);
            editor.putFloat("last_energy_level", energy);
            editor.putBoolean("daily_task_completed_" + new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()), true);
            editor.apply();

            int currentStreak = localDb.sharedPreferences.getInt("user_workout_streak", 0);
            localDb.sharedPreferences.edit().putInt("user_workout_streak", currentStreak + 1).apply();

            dialog.dismiss();
            
            android.widget.Toast.makeText(getContext(), "AI Coach Feedback Submitted! Streak Incremented! 🎉", android.widget.Toast.LENGTH_SHORT).show();
            loadDashboardData();
        });

        dialog.show();
    }
}

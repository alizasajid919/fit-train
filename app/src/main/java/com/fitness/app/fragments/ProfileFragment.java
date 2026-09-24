package com.fitness.app.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import com.bumptech.glide.Glide;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.fitness.app.R;
import com.fitness.app.activities.EditProfileActivity;
import com.fitness.app.activities.OnboardingActivity;
import com.fitness.app.activities.SettingsActivity;
import com.fitness.app.activities.HelpActivity;
import com.fitness.app.activities.PrivacyActivity;
import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.models.ProgressLog;
import com.fitness.app.models.User;
import com.fitness.app.models.WorkoutLog;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private TextView tvProfileName, tvProfileEmail, tvProfileGoal, tvProfileStreak;
    private TextView tvSummaryWorkouts, tvSummaryCalories, tvSummarySteps, tvSummaryBmi;
    private TextView tvProfileAiTip, tvProfileMotivation;
    private TextView tvProfileGoalTitle, tvProfileGoalPercent;
    private ProgressBar pbProfileGoal;
    private ImageView ivProfilePic;
    private TextView badgePioneer, badgeConsistent, badgeHydrated;

    private View pChartBarMon, pChartBarTue, pChartBarWed, pChartBarThu, pChartBarFri, pChartBarSat, pChartBarSun;

    private LocalDataManager localDb;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        localDb = new LocalDataManager(requireContext());

        // Bind views
        ivProfilePic = view.findViewById(R.id.ivProfilePic);
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        tvProfileGoal = view.findViewById(R.id.tvProfileGoal);
        tvProfileStreak = view.findViewById(R.id.tvProfileStreak);

        tvProfileAiTip = view.findViewById(R.id.tvProfileAiTip);
        tvProfileMotivation = view.findViewById(R.id.tvProfileMotivation);
        tvProfileGoalTitle = view.findViewById(R.id.tvProfileGoalTitle);
        tvProfileGoalPercent = view.findViewById(R.id.tvProfileGoalPercent);
        pbProfileGoal = view.findViewById(R.id.pbProfileGoal);

        badgePioneer = view.findViewById(R.id.badgePioneer);
        badgeConsistent = view.findViewById(R.id.badgeConsistent);
        badgeHydrated = view.findViewById(R.id.badgeHydrated);

        tvSummaryWorkouts = view.findViewById(R.id.tvSummaryWorkouts);
        tvSummaryCalories = view.findViewById(R.id.tvSummaryCalories);
        tvSummarySteps = view.findViewById(R.id.tvSummarySteps);
        tvSummaryBmi = view.findViewById(R.id.tvSummaryBmi);

        pChartBarMon = view.findViewById(R.id.pChartBarMon);
        pChartBarTue = view.findViewById(R.id.pChartBarTue);
        pChartBarWed = view.findViewById(R.id.pChartBarWed);
        pChartBarThu = view.findViewById(R.id.pChartBarThu);
        pChartBarFri = view.findViewById(R.id.pChartBarFri);
        pChartBarSat = view.findViewById(R.id.pChartBarSat);
        pChartBarSun = view.findViewById(R.id.pChartBarSun);

        view.findViewById(R.id.fabEditProfile).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), EditProfileActivity.class));
        });

        view.findViewById(R.id.layoutActionEditProfileDetails).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), EditProfileActivity.class));
        });

        view.findViewById(R.id.layoutActionEditProfile).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), SettingsActivity.class));
        });

        view.findViewById(R.id.layoutActionHelp).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), HelpActivity.class));
        });
        view.findViewById(R.id.layoutActionPrivacy).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), PrivacyActivity.class));
        });
        view.findViewById(R.id.layoutActionLogout).setOnClickListener(v -> performLogout());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setupProfile();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            setupProfile();
        }
    }

    private void setupProfile() {
        User user = localDb.getUser();
        if (user == null) return;

        tvProfileName.setText(user.getFirstName() + " " + user.getLastName());
        if (tvProfileEmail != null) {
            tvProfileEmail.setVisibility(View.GONE);
        }

        String goal = user.getGoal() != null && !user.getGoal().isEmpty() ? user.getGoal() : "Improve Fitness";
        tvProfileGoalTitle.setText(goal);

        new Thread(() -> {
            try {
                com.fitness.app.data.room.AppDatabase db = com.fitness.app.data.room.AppDatabase.getInstance(requireContext());
                com.fitness.app.models.UserChallengeStats stats = db.fitnessDao().getUserChallengeStats();
                final int totalXp = stats != null ? stats.getTotalXp() : 0;
                final int level = (totalXp / 300) + 1;
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        tvProfileGoal.setText("Goal: " + goal + " • Lvl " + level + " (" + totalXp + " XP)");
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // Profile Image
        String imgUri = user.getProfileImageUrl();
        if (imgUri != null && !imgUri.trim().isEmpty()) {
            try {
                Glide.with(this)
                        .load(imgUri)
                        .placeholder(R.drawable.onboarding_1)
                        .error(R.drawable.onboarding_1)
                        .circleCrop()
                        .into(ivProfilePic);
            } catch (Exception e) {
                ivProfilePic.setImageResource(R.drawable.onboarding_1);
            }
        } else {
            ivProfilePic.setImageResource(R.drawable.onboarding_1);
        }

        // Fetch logs for statistics
        List<WorkoutLog> workoutLogs = localDb.getAllWorkoutLogs();
        Map<String, ProgressLog> progressLogs = localDb.getAllProgressLogs();

        int totalWorkouts = workoutLogs.size();
        long totalSteps = 0;
        long totalCalories = 0;

        for (ProgressLog log : progressLogs.values()) {
            totalSteps += log.getStepsCount();
            totalCalories += log.getCaloriesBurned();
        }

        tvSummaryWorkouts.setText(totalWorkouts + " Sessions");
        tvSummaryCalories.setText(String.format("%,d kcal", totalCalories));
        tvSummarySteps.setText(String.format("%,d", totalSteps));

        // Streak & BMI
        int currentStreak = localDb.getCurrentStreak();
        int longestStreak = localDb.getLongestStreak();
        tvProfileStreak.setText("🔥 " + currentStreak + " Day Streak (Best: " + longestStreak + ")");

        // Dynamic Badge Milestones Unlock
        if (totalWorkouts > 0) {
            badgePioneer.setAlpha(1.0f);
        } else {
            badgePioneer.setAlpha(0.3f);
        }

        if (currentStreak >= 3 || longestStreak >= 3) {
            badgeConsistent.setAlpha(1.0f);
        } else {
            badgeConsistent.setAlpha(0.3f);
        }

        String todayStr = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new java.util.Date());
        ProgressLog todayLog = localDb.getProgressLog(todayStr);
        if (todayLog != null && todayLog.getWaterConsumedMl() >= 2500) {
            badgeHydrated.setAlpha(1.0f);
        } else {
            badgeHydrated.setAlpha(0.3f);
        }

        double weight = user.getWeight();
        double height = user.getHeight();
        if (weight > 0 && height > 0) {
            double heightM = height / 100.0;
            double bmi = weight / (heightM * heightM);
            String category = getBmiCategory(bmi);
            tvSummaryBmi.setText(String.format(Locale.getDefault(), "%.1f (%s)", bmi, category));
        } else {
            tvSummaryBmi.setText("N/A");
        }

        // Goal progress
        int percent = calculateGoalProgress(user, progressLogs);
        tvProfileGoalPercent.setText(percent + "%");
        pbProfileGoal.setProgress(percent);

        // AI Tip
        tvProfileAiTip.setText(getCoachTipForGoal(goal));
        tvProfileMotivation.setText(String.format("🔥 You have registered %d workouts! Showing amazing consistency on your health journey.", totalWorkouts));

        // Setup Charts
        setAnalyticsCharts();
    }

    private String getBmiCategory(double bmi) {
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25) return "Normal";
        if (bmi < 30) return "Overweight";
        return "Obese";
    }

    private String getCoachTipForGoal(String goal) {
        switch (goal.toLowerCase()) {
            case "lose weight":
                return "Focus on keeping a slight caloric deficit and steady-state walking routines. Your consistency will lead to amazing results!";
            case "gain muscle":
                return "Ensure your protein intake is high and lift weights progressively heavier. Target at least 8 hours of deep sleep to maximize muscle repair.";
            default:
                return "A balanced diet and mixing cardio with resistance training will yield the best long-term metabolic flexibility and cardiovascular health.";
        }
    }

    private void setAnalyticsCharts() {
        // Set dynamic height representation of Monday-Sunday steps logs
        Map<String, ProgressLog> logs = localDb.getAllProgressLogs();
        int[] stepsForWeek = new int[7]; // Mon-Sun
        
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        int currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (currentDayOfWeek == Calendar.SUNDAY) {
            cal.add(Calendar.DAY_OF_YEAR, -6);
        } else {
            cal.add(Calendar.DAY_OF_YEAR, Calendar.MONDAY - currentDayOfWeek);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        for (int i = 0; i < 7; i++) {
            String dateStr = sdf.format(cal.getTime());
            ProgressLog log = logs.get(dateStr);
            if (log != null) {
                stepsForWeek[i] = log.getStepsCount();
            } else {
                stepsForWeek[i] = 0;
            }
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Bind heights relative to steps goal
        User user = localDb.getUser();
        int targetGoal = (user != null && user.getDailyStepGoal() > 0) ? user.getDailyStepGoal() : 10000;
        updateBarHeight(pChartBarMon, stepsForWeek[0], targetGoal);
        updateBarHeight(pChartBarTue, stepsForWeek[1], targetGoal);
        updateBarHeight(pChartBarWed, stepsForWeek[2], targetGoal);
        updateBarHeight(pChartBarThu, stepsForWeek[3], targetGoal);
        updateBarHeight(pChartBarFri, stepsForWeek[4], targetGoal);
        updateBarHeight(pChartBarSat, stepsForWeek[5], targetGoal);
        updateBarHeight(pChartBarSun, stepsForWeek[6], targetGoal);
    }

    private int calculateGoalProgress(User user, Map<String, ProgressLog> progressLogs) {
        if (user == null) return 0;
        
        String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new java.util.Date());
        ProgressLog todayLog = progressLogs.get(todayStr);
        
        int stepGoal = user.getDailyStepGoal() > 0 ? user.getDailyStepGoal() : 10000;
        int waterGoal = user.getDailyWaterGoal() > 0 ? user.getDailyWaterGoal() : 2500;
        int calGoal = user.getDailyCaloriesGoal() > 0 ? user.getDailyCaloriesGoal() : 2000;
        int sleepGoal = user.getDailySleepGoal() > 0 ? user.getDailySleepGoal() : 480;
        
        if (todayLog == null) {
            if (progressLogs.isEmpty()) return 0;
            double totalProgress = 0;
            int count = 0;
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            for (int i = 0; i < 7; i++) {
                String dateStr = sdf.format(cal.getTime());
                ProgressLog log = progressLogs.get(dateStr);
                if (log != null) {
                    double dayProgress = getDayProgressPercentage(log, stepGoal, waterGoal, calGoal, sleepGoal);
                    totalProgress += dayProgress;
                    count++;
                }
                cal.add(Calendar.DAY_OF_YEAR, -1);
            }
            if (count == 0) return 0;
            return (int) Math.min(100, Math.max(0, totalProgress / count));
        } else {
            return (int) getDayProgressPercentage(todayLog, stepGoal, waterGoal, calGoal, sleepGoal);
        }
    }

    private double getDayProgressPercentage(ProgressLog log, int stepGoal, int waterGoal, int calGoal, int sleepGoal) {
        double stepProgress = (double) log.getStepsCount() / stepGoal;
        double waterProgress = (double) log.getWaterConsumedMl() / waterGoal;
        double calProgress = (double) log.getCaloriesBurned() / calGoal;
        double sleepProgress = (double) log.getSleepDurationMinutes() / sleepGoal;
        
        stepProgress = Math.min(1.0, stepProgress);
        waterProgress = Math.min(1.0, waterProgress);
        calProgress = Math.min(1.0, calProgress);
        sleepProgress = Math.min(1.0, sleepProgress);
        
        return ((stepProgress + waterProgress + calProgress + sleepProgress) / 4.0) * 100.0;
    }

    private void updateBarHeight(View bar, int steps, int target) {
        if (bar == null) return;
        int maxPixelHeight = 120; // 120dp representation
        float ratio = (float) steps / target;
        int finalHeightDp = Math.max(15, (int) (ratio * maxPixelHeight));
        
        // Convert DP to pixels
        int finalHeightPx = (int) (finalHeightDp * getResources().getDisplayMetrics().density);
        
        ViewGroup.LayoutParams params = bar.getLayoutParams();
        params.height = finalHeightPx;
        bar.setLayoutParams(params);
    }

    private void performLogout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to log out of your FitTrain account?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    localDb.clearAll();
                    localDb.setOnboardingSeen(false);
                    Intent intent = new Intent(getActivity(), OnboardingActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}

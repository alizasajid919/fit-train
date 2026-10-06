package com.fitness.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import com.bumptech.glide.Glide;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.fitness.app.R;
import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.models.ProgressLog;
import com.fitness.app.models.User;
import com.fitness.app.models.WorkoutLog;
import com.fitness.app.viewmodels.AuthViewModel;
import com.fitness.app.viewmodels.ProfileViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileName, tvProfileGoal, tvProfileStreak;
    private TextView tvSummaryWorkouts, tvSummaryCalories, tvSummarySteps, tvSummaryBmi;
    private TextView tvProfileAiTip, tvProfileMotivation;
    private TextView tvProfileGoalTitle, tvProfileGoalPercent;
    private ProgressBar pbProfileGoal;
    private ImageView ivProfilePic;
    private View layoutActionEditProfileDetails, layoutActionEditProfile, layoutActionHelp, layoutActionPrivacy, layoutActionLogout;

    private TextView tvProfileMobile, tvProfileDob, tvProfileGender, tvProfileHeightWeight, tvProfileLevel, tvProfileCalorieGoal;
    private TextView tvProfileBloodGroup, tvProfileTargetWeight, tvProfileWorkoutLocation, tvProfileWorkoutSchedule, tvProfileDietStyle, tvProfileMedicalConditions;

    private LocalDataManager localDb;
    private AuthViewModel authViewModel;
    private ProfileViewModel profileViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        localDb = new LocalDataManager(this);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // Bind Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        // Bind Views
        ivProfilePic = findViewById(R.id.ivProfilePic);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileGoal = findViewById(R.id.tvProfileGoal);
        tvProfileStreak = findViewById(R.id.tvProfileStreak);

        tvProfileAiTip = findViewById(R.id.tvProfileAiTip);
        tvProfileMotivation = findViewById(R.id.tvProfileMotivation);
        tvProfileGoalTitle = findViewById(R.id.tvProfileGoalTitle);
        tvProfileGoalPercent = findViewById(R.id.tvProfileGoalPercent);
        pbProfileGoal = findViewById(R.id.pbProfileGoal);

        tvProfileMobile = findViewById(R.id.tvProfileMobile);
        tvProfileDob = findViewById(R.id.tvProfileDob);
        tvProfileGender = findViewById(R.id.tvProfileGender);
        tvProfileHeightWeight = findViewById(R.id.tvProfileHeightWeight);
        tvProfileLevel = findViewById(R.id.tvProfileLevel);
        tvProfileCalorieGoal = findViewById(R.id.tvProfileCalorieGoal);
        tvProfileBloodGroup = findViewById(R.id.tvProfileBloodGroup);
        tvProfileTargetWeight = findViewById(R.id.tvProfileTargetWeight);
        tvProfileWorkoutLocation = findViewById(R.id.tvProfileWorkoutLocation);
        tvProfileWorkoutSchedule = findViewById(R.id.tvProfileWorkoutSchedule);
        tvProfileDietStyle = findViewById(R.id.tvProfileDietStyle);
        tvProfileMedicalConditions = findViewById(R.id.tvProfileMedicalConditions);

        tvSummaryWorkouts = findViewById(R.id.tvSummaryWorkouts);
        tvSummaryCalories = findViewById(R.id.tvSummaryCalories);
        tvSummarySteps = findViewById(R.id.tvSummarySteps);
        tvSummaryBmi = findViewById(R.id.tvSummaryBmi);

        layoutActionEditProfileDetails = findViewById(R.id.layoutActionEditProfileDetails);
        layoutActionEditProfile = findViewById(R.id.layoutActionEditProfile);
        layoutActionHelp = findViewById(R.id.layoutActionHelp);
        layoutActionPrivacy = findViewById(R.id.layoutActionPrivacy);
        layoutActionLogout = findViewById(R.id.layoutActionLogout);

        if (layoutActionEditProfileDetails != null) {
            layoutActionEditProfileDetails.setOnClickListener(v -> {
                startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
            });
        }

        findViewById(R.id.fabEditProfile).setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
        });

        setupProfile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupProfile();
    }

    private void setupProfile() {
        User user = localDb.getUser();
        if (user == null) {
            user = new User("guest_uid", "Guest", "User", "guest@fittrain.com", System.currentTimeMillis());
            user.setGender("Male");
            user.setHeight(175);
            user.setWeight(70);
            user.setGoal("Improve Shape");
            user.setProfileCompleted(true);
            localDb.saveUser(user);
        }

        tvProfileName.setText(user.getFirstName() + " " + user.getLastName());

        String goal = user.getGoal() != null && !user.getGoal().isEmpty() ? user.getGoal() : "Improve Shape";
        tvProfileGoal.setText("Goal: " + goal + " • Lvl 2 (370 XP)");
        tvProfileGoalTitle.setText(goal);

        tvProfileMobile.setText(user.getMobileNumber() != null && !user.getMobileNumber().isEmpty() ? user.getMobileNumber() : "N/A");
        tvProfileDob.setText((user.getDob() != null && !user.getDob().isEmpty() ? user.getDob() : "N/A") + (user.getAge() > 0 ? " (" + user.getAge() + " yrs)" : ""));
        tvProfileGender.setText(user.getGender() != null && !user.getGender().isEmpty() ? user.getGender() : "N/A");
        tvProfileHeightWeight.setText((int) user.getHeight() + " cm / " + (int) user.getWeight() + " kg");
        if (tvProfileBloodGroup != null) tvProfileBloodGroup.setText(user.getBloodGroup() != null && !user.getBloodGroup().isEmpty() ? user.getBloodGroup() : "N/A");
        
        String exp = user.getFitnessExperience() != null && !user.getFitnessExperience().isEmpty() ? user.getFitnessExperience() : "Beginner";
        String act = user.getActivityLevel() != null && !user.getActivityLevel().isEmpty() ? user.getActivityLevel() : "Moderate";
        tvProfileLevel.setText(exp + " • " + act);

        if (tvProfileTargetWeight != null) {
            String pace = user.getTargetPace() != null && !user.getTargetPace().isEmpty() ? user.getTargetPace() : "Balanced";
            tvProfileTargetWeight.setText((int) user.getTargetWeight() + " kg (" + pace + " Pace)");
        }

        if (tvProfileWorkoutLocation != null) {
            String loc = user.getWorkoutLocation() != null && !user.getWorkoutLocation().isEmpty() ? user.getWorkoutLocation() : "At Home";
            String equip = user.getAvailableEquipment() != null && !user.getAvailableEquipment().isEmpty() ? user.getAvailableEquipment() : "Bodyweight";
            tvProfileWorkoutLocation.setText(loc + " (" + equip + ")");
        }

        if (tvProfileWorkoutSchedule != null) {
            int dur = user.getWorkoutDuration() > 0 ? user.getWorkoutDuration() : 30;
            int days = user.getWorkoutDaysPerWeek() > 0 ? user.getWorkoutDaysPerWeek() : 4;
            String time = user.getPreferredWorkoutTime() != null && !user.getPreferredWorkoutTime().isEmpty() ? user.getPreferredWorkoutTime() : "Morning";
            tvProfileWorkoutSchedule.setText(dur + " min • " + days + " days/wk • " + time);
        }

        if (tvProfileDietStyle != null) {
            String diet = user.getDietaryPreference() != null && !user.getDietaryPreference().isEmpty() ? user.getDietaryPreference() : "Balanced";
            int meals = user.getMealsPerDay() > 0 ? user.getMealsPerDay() : 3;
            String env = user.getEatingEnvironment() != null && !user.getEatingEnvironment().isEmpty() ? user.getEatingEnvironment() : "Home";
            tvProfileDietStyle.setText(diet + " • " + meals + " meals/day (" + env + ")");
        }

        if (tvProfileMedicalConditions != null) {
            tvProfileMedicalConditions.setText(user.getMedicalConditions() != null && !user.getMedicalConditions().isEmpty() ? user.getMedicalConditions() : "None");
        }

        int targetCal = 2000;
        if (goal.toLowerCase().contains("lose")) {
            targetCal = 1600;
        } else if (goal.toLowerCase().contains("gain") || goal.toLowerCase().contains("build")) {
            targetCal = 2600;
        }
        tvProfileCalorieGoal.setText(targetCal + " kcal");

        // Profile Image URI
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

        int streak = progressLogs.isEmpty() ? 0 : Math.max(0, progressLogs.size());
        tvProfileStreak.setText("🔥 " + streak + " Day Streak\n(Best: 1)");

        double weight = user.getWeight();
        double height = user.getHeight();
        if (weight > 0 && height > 0) {
            double heightM = height / 100.0;
            double bmi = weight / (heightM * heightM);
            String category = getBmiCategory(bmi);
            tvSummaryBmi.setText(String.format("%.1f (%s)", bmi, category));
        } else {
            tvSummaryBmi.setText("N/A");
        }

        // Goal progress completion percentage (simulated based on workouts / 16 sessions)
        int percent = Math.min(100, Math.max(15, (totalWorkouts * 100) / 16));
        tvProfileGoalPercent.setText(percent + "%");
        pbProfileGoal.setProgress(percent);

        // AI Tip & Motivation messages
        tvProfileAiTip.setText(getCoachTipForGoal(goal));
        tvProfileMotivation.setText(String.format("🔥 You have registered %d workouts! Showing amazing consistency on your health journey.", totalWorkouts));

        // Setup Charts
        setAnalyticsCharts();

        // Actions
        layoutActionEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, SettingsActivity.class));
        });

        layoutActionHelp.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, HelpActivity.class));
        });

        layoutActionPrivacy.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, PrivacyActivity.class));
        });
        layoutActionLogout.setOnClickListener(v -> performLogout());
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
                return "Increase protein intake and prioritize compound lifts like squats and chest presses. Recovery is when muscles grow!";
            case "improve shape":
                return "A balanced diet and mixing cardio with resistance training will yield the best long-term metabolic flexibility and cardiovascular health.";
            case "increase strength":
                return "Focus on progressive overload—gradually adding weights in your routines. Always practice safe lifting form.";
            case "improve endurance":
                return "Steady tempo cardio and staying hydrated are key. Try keeping a consistent heart rate range during run sessions.";
            default:
                return "Focus on building simple daily habits. Drinking water, walking, and getting 8 hours of sleep are the foundation of fitness.";
        }
    }

    private void showHelpDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Help & Support")
                .setMessage("Welcome to FitTrain Support!\n\nEmail: support@fittrain.com\nHours: 9 AM - 6 PM Mon-Fri\n\nIf you have any questions about workout routines, diets, or technical issues, reach out and our coaches will assist you.")
                .setPositiveButton("Close", null)
                .show();
    }

    private void showPrivacyDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Privacy Policy")
                .setMessage("FitTrain respects your privacy and personal metrics.\n\n1. All biometric calculations (Weight, Height, BMI) are stored locally in private shared preferences.\n2. No Body-Shaming Policy: Your weight and fitness progress are completely private. Comparison against other users is strictly disabled.\n3. Data protection: All sync services utilize industry-standard TLS encryption protocols.")
                .setPositiveButton("Close", null)
                .show();
    }

    private void performLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to log out of your FitTrain account?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
                    localDb.clearAll();
                    localDb.setOnboardingSeen(false);
                    Toast.makeText(this, "Logged out successfully.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ProfileActivity.this, OnboardingActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finishAffinity();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setAnalyticsCharts() {
        BarChart barChart = findViewById(R.id.profileWeeklyChart);
        if (barChart == null) return;

        boolean isDark = localDb.isDarkThemeEnabled();
        int textColor = isDark ? 0xFF94A3B8 : 0xFF64748B;
        int valueColor = isDark ? 0xFFF8FAFC : 0xFF1E293B;
        int gridColor = isDark ? 0xFF334155 : 0xFFE2E8F0;

        barChart.getDescription().setEnabled(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setMaxVisibleValueCount(10);
        barChart.setPinchZoom(false);
        barChart.setDrawGridBackground(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(textColor);
        xAxis.setTextSize(11f);
        
        final String[] days = new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        xAxis.setValueFormatter(new IndexAxisValueFormatter(days));

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(gridColor);
        leftAxis.setTextColor(textColor);
        leftAxis.setAxisMinimum(0f);

        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(false);

        List<BarEntry> entries = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        
        for (int i = 0; i < 7; i++) {
            String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
            ProgressLog log = localDb.getProgressLog(dateStr);
            float stepsVal = log != null ? log.getStepsCount() : 0f;
            
            if (stepsVal <= 0) {
                stepsVal = (float) (3000 + Math.random() * 6000);
            }
            
            entries.add(new BarEntry(i, stepsVal));
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        BarDataSet dataSet = new BarDataSet(entries, "Steps History");
        dataSet.setColor(0xFF6C63FF); // Brand Primary Color
        dataSet.setValueTextColor(valueColor);
        dataSet.setValueTextSize(10f);
        
        BarData data = new BarData(dataSet);
        barChart.setData(data);
        barChart.animateY(800);
        barChart.invalidate();
    }
}

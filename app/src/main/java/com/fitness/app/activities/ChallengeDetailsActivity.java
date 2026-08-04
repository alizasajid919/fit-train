package com.fitness.app.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.fitness.app.R;
import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.data.room.AppDatabase;
import com.fitness.app.data.room.FitnessDao;
import com.fitness.app.models.FitnessChallenge;
import com.fitness.app.models.User;
import com.fitness.app.models.UserChallengeStats;
import com.fitness.app.utils.ReminderScheduler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class ChallengeDetailsActivity extends AppCompatActivity {

    private TextView tvToolbarTitle;
    private TextView tvDetailName, tvDetailDifficulty, tvDetailDuration, tvDetailReward, tvDetailBadge;
    private TextView tvDetailProgressLabel;
    private ProgressBar pbDetailProgress;
    private TextView tvDetailStreak, tvDetailMissed, tvDetailDaysLeft;
    private TextView tvDetailDesc, tvDetailRules, tvDetailBenefits;
    private LinearLayout layoutDetailTasksContainer;
    private androidx.appcompat.widget
            .AppCompatButton btnActionChallenge;

    private FitnessDao fitnessDao;
    private LocalDataManager localDb;
    private FitnessChallenge challenge;
    private UserChallengeStats userStats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_challenge_details);

        fitnessDao = AppDatabase.getInstance(this).fitnessDao();
        localDb = new LocalDataManager(this);

        // Bind Views
        tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        tvDetailName = findViewById(R.id.tvDetailName);
        tvDetailDifficulty = findViewById(R.id.tvDetailDifficulty);
        tvDetailDuration = findViewById(R.id.tvDetailDuration);
        tvDetailReward = findViewById(R.id.tvDetailReward);
        tvDetailBadge = findViewById(R.id.tvDetailBadge);
        
        tvDetailProgressLabel = findViewById(R.id.tvDetailProgressLabel);
        pbDetailProgress = findViewById(R.id.pbDetailProgress);
        
        tvDetailStreak = findViewById(R.id.tvDetailStreak);
        tvDetailMissed = findViewById(R.id.tvDetailMissed);
        tvDetailDaysLeft = findViewById(R.id.tvDetailDaysLeft);
        
        tvDetailDesc = findViewById(R.id.tvDetailDesc);
        tvDetailRules = findViewById(R.id.tvDetailRules);
        tvDetailBenefits = findViewById(R.id.tvDetailBenefits);
        
        layoutDetailTasksContainer = findViewById(R.id.layoutDetailTasksContainer);
        btnActionChallenge = findViewById(R.id.btnActionChallenge);

        challenge = (FitnessChallenge) getIntent().getSerializableExtra("challenge_item");
        if (challenge == null) {
            Toast.makeText(this, "Failed to load challenge details", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());

        loadStatsAndRender();
    }

    private void loadStatsAndRender() {
        new Thread(() -> {
            userStats = fitnessDao.getUserChallengeStats();
            if (userStats == null) {
                userStats = new UserChallengeStats();
                fitnessDao.insertUserChallengeStats(userStats);
            }

            runOnUiThread(() -> {
                bindChallengeData();
                renderTasksChecklist();
                setupActionButton();
            });
        }).start();
    }

    private void bindChallengeData() {
        tvToolbarTitle.setText(challenge.getName());
        tvDetailName.setText(challenge.getName());
        tvDetailDifficulty.setText(challenge.getDifficulty());
        tvDetailDuration.setText(challenge.getDuration());
        tvDetailReward.setText("+" + challenge.getXpReward() + " XP");
        tvDetailBadge.setText(challenge.getBadgeName() + " Badge");

        tvDetailDesc.setText(challenge.getDescription());

        // Format estimated end date
        String estEndStr = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(new Date(challenge.getEndDate()));
        tvDetailDaysLeft.setText("⏰ Left: " + Math.max(0, challenge.getDaysRemaining()) + " Days\n(Est. End: " + estEndStr + ")");

        // Dynamic Rules & Benefits depending on difficulty/duration
        if ("WEEKLY".equalsIgnoreCase(challenge.getDuration())) {
            tvDetailRules.setText("• Complete all daily tasks consistently.\n• Complete each task within its specified day.\n• Finish all tasks to unlock the " + challenge.getBadgeName() + " Badge.");
        } else {
            tvDetailRules.setText("• Complete monthly milestone targets.\n• Log in daily to ensure progress logs register.\n• Unlock premium " + challenge.getBadgeName() + " rewards.");
        }

        // Dynamically build benefits & rewards metrics
        StringBuilder benefitsBuilder = new StringBuilder();
        benefitsBuilder.append("• Available Rewards: ").append(challenge.getXpReward()).append(" XP + ").append(challenge.getBadgeName()).append(" Badge\n");
        benefitsBuilder.append("• Est. Calories Burned: ").append(challenge.getCaloriesEstimate()).append(" kcal/day\n");
        benefitsBuilder.append("• Daily Activity Time: ").append(challenge.getEstimatedWorkoutTime()).append(" minutes\n\n");
        benefitsBuilder.append("Health Benefits:\n").append(challenge.getHealthBenefits());
        tvDetailBenefits.setText(benefitsBuilder.toString());

        updateProgressUi();
        updateStreakUi();
    }

    private void updateProgressUi() {
        int progress = challenge.getProgressPercent();
        tvDetailProgressLabel.setText("Current Progress: " + progress + "%");
        pbDetailProgress.setProgress(progress);
    }

    private void updateStreakUi() {
        tvDetailStreak.setText("🔥 Streak: " + userStats.getCurrentStreak() + " Days");
        tvDetailMissed.setText("⚠️ Missed: " + userStats.getMissedDays() + " Days");
    }

    private void renderTasksChecklist() {
        layoutDetailTasksContainer.removeAllViews();
        boolean isActive = "IN_PROGRESS".equalsIgnoreCase(challenge.getStatus());
        boolean isCompleted = "COMPLETED".equalsIgnoreCase(challenge.getStatus());

        try {
            JSONArray arr = new JSONArray(challenge.getTasksJson());
            for (int i = 0; i < arr.length(); i++) {
                final int index = i;
                JSONObject obj = arr.getJSONObject(i);
                View taskView = LayoutInflater.from(this).inflate(R.layout.item_challenge_task, layoutDetailTasksContainer, false);

                CheckBox cb = taskView.findViewById(R.id.cbTaskCompleted);
                TextView tvDay = taskView.findViewById(R.id.tvTaskDay);
                TextView tvName = taskView.findViewById(R.id.tvTaskName);

                tvDay.setText(obj.getString("day"));
                tvName.setText(obj.getString("name"));

                boolean isDone = obj.optBoolean("completed", false);
                cb.setChecked(isDone);

                // Enable/disable checklist based on status
                if (!isActive) {
                    cb.setEnabled(false);
                } else {
                    cb.setEnabled(true);
                }

                cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    updateTaskState(index, isChecked);
                });

                layoutDetailTasksContainer.addView(taskView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateTaskState(int index, boolean isChecked) {
        new Thread(() -> {
            try {
                JSONArray arr = new JSONArray(challenge.getTasksJson());
                JSONObject obj = arr.getJSONObject(index);
                boolean oldChecked = obj.optBoolean("completed", false);
                
                if (oldChecked == isChecked) return; // No change

                obj.put("completed", isChecked);
                challenge.setTasksJson(arr.toString());

                // 1. Award / Deduct XP instantly (+10 XP per task checked)
                int xpChange = isChecked ? 10 : -10;
                userStats.setTotalXp(Math.max(0, userStats.getTotalXp() + xpChange));

                // 2. Track Streaks & Daily Logs
                String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                JSONArray logs = new JSONArray(userStats.getDailyLogsJson());
                Set<String> logsSet = new HashSet<>();
                for (int k = 0; k < logs.length(); k++) {
                    logsSet.add(logs.getString(k));
                }

                if (isChecked) {
                    if (!logsSet.contains(todayStr)) {
                        logsSet.add(todayStr);
                        logs.put(todayStr);
                        userStats.setDailyLogsJson(logs.toString());
                        
                        // Recalculate streak
                        int streak = calculateCurrentStreak(logsSet);
                        userStats.setCurrentStreak(streak);
                        if (streak > userStats.getMaxStreak()) {
                            userStats.setMaxStreak(streak);
                        }
                    }
                } else {
                    // Check if yesterday had completed task, if not streak is 0
                    // Keep it simple, unchecking today does not aggressively wipe past streaks
                }

                // 3. Compute progress percentage
                int completedCount = 0;
                for (int i = 0; i < arr.length(); i++) {
                    if (arr.getJSONObject(i).optBoolean("completed", false)) {
                        completedCount++;
                    }
                }
                int newProgress = (int) (((double) completedCount / arr.length()) * 100);
                challenge.setProgressPercent(newProgress);

                // 4. Calculate missed days based on elapsed days since start
                long elapsedDays = (System.currentTimeMillis() - challenge.getStartDate()) / (24L * 60 * 60 * 1000);
                int missed = (int) Math.max(0, elapsedDays - completedCount);
                userStats.setMissedDays(missed);

                // 5. Completion detection
                boolean justFinished = false;
                if (newProgress == 100 && !"COMPLETED".equals(challenge.getStatus())) {
                    challenge.setStatus("COMPLETED");
                    justFinished = true;

                    // Award final completion reward
                    userStats.setTotalXp(userStats.getTotalXp() + challenge.getXpReward());
                    userStats.setChallengesCompleted(userStats.getChallengesCompleted() + 1);

                    // Unlock Badge
                    JSONArray badgeArr = new JSONArray(userStats.getUnlockedBadgesJson());
                    boolean alreadyUnlocked = false;
                    for (int j = 0; j < badgeArr.length(); j++) {
                        if (badgeArr.getString(j).equalsIgnoreCase(challenge.getBadgeName())) {
                            alreadyUnlocked = true;
                            break;
                        }
                    }
                    if (!alreadyUnlocked) {
                        badgeArr.put(challenge.getBadgeName());
                    }
                    userStats.setUnlockedBadgesJson(badgeArr.toString());
                } else if (newProgress < 100) {
                    challenge.setStatus("IN_PROGRESS");
                }

                // Update database
                fitnessDao.insertFitnessChallenge(challenge);
                fitnessDao.insertUserChallengeStats(userStats);

                final boolean showMilestone = justFinished;
                runOnUiThread(() -> {
                    updateProgressUi();
                    updateStreakUi();
                    if (showMilestone) {
                        showCelebrationDialog(challenge.getBadgeName(), challenge.getXpReward());
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private int calculateCurrentStreak(Set<String> logsSet) {
        int streak = 0;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        
        while (true) {
            String dateStr = sdf.format(calendar.getTime());
            if (logsSet.contains(dateStr)) {
                streak++;
                calendar.add(Calendar.DAY_OF_YEAR, -1); // Go back one day
            } else {
                break;
            }
        }
        return streak;
    }

    private void setupActionButton() {
        String status = challenge.getStatus();
        if ("NOT_STARTED".equalsIgnoreCase(status) || "UPCOMING".equalsIgnoreCase(status)) {
            btnActionChallenge.setText("Start Challenge 🚀");
            btnActionChallenge.setEnabled(true);
            btnActionChallenge.setOnClickListener(v -> startChallenge());
        } else if ("IN_PROGRESS".equalsIgnoreCase(status)) {
            btnActionChallenge.setText("Continue Challenge 💪");
            btnActionChallenge.setEnabled(false); // Task checkboxes are checkable instead
            btnActionChallenge.setAlpha(0.6f);
        } else if ("COMPLETED".equalsIgnoreCase(status)) {
            btnActionChallenge.setText("Challenge Completed! 🎉");
            btnActionChallenge.setEnabled(false);
            btnActionChallenge.setAlpha(0.6f);
        }
    }

    private void startChallenge() {
        // Enforce only ONE active challenge at a time!
        new Thread(() -> {
            FitnessChallenge active = fitnessDao.getActiveChallenge();
            if (active != null) {
                runOnUiThread(() -> Toast.makeText(this, "You already have an active challenge! Complete it first.", Toast.LENGTH_LONG).show());
                return;
            }

            // Start this challenge
            challenge.setStatus("IN_PROGRESS");
            challenge.setStartDate(System.currentTimeMillis());
            
            int totalDays = 7;
            try {
                org.json.JSONArray tasks = new org.json.JSONArray(challenge.getTasksJson());
                totalDays = tasks.length();
            } catch (Exception e) {
                e.printStackTrace();
            }

            long dayMs = 24L * 60 * 60 * 1000;
            challenge.setEndDate(System.currentTimeMillis() + (totalDays * dayMs));
            challenge.setDaysRemaining(totalDays);

            fitnessDao.insertFitnessChallenge(challenge);

            // Schedule Alarms/Reminders
            runOnUiThread(() -> {
                Toast.makeText(this, "Challenge Started! Complete daily tasks.", Toast.LENGTH_SHORT).show();
                setupActionButton();
                renderTasksChecklist();
                bindChallengeData();
                ReminderScheduler.rescheduleChallengeReminders(this);
            });
        }).start();
    }

    private void showCelebrationDialog(String badgeName, int xpReward) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_milestone_celebration, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvBadgeName = dialogView.findViewById(R.id.tvCelebrationBadgeName);
        TextView tvDescription = dialogView.findViewById(R.id.tvCelebrationDesc);
        
        tvBadgeName.setText(badgeName + " Badge Unlocked!");
        tvDescription.setText("Fantastic work completing all daily targets!\nYou earned +" + xpReward + " XP reward.");

        dialogView.findViewById(R.id.btnCelebrationDone).setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        dialog.show();
    }
}

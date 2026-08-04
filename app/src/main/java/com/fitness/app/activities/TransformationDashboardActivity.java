package com.fitness.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.fitness.app.R;
import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.data.room.AppDatabase;
import com.fitness.app.models.TransformationPlan;
import com.fitness.app.models.TransformationProgress;
import com.fitness.app.models.User;
import com.google.firebase.storage.FirebaseStorage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class TransformationDashboardActivity extends AppCompatActivity {

    private ProgressBar pbTransformationProgress;
    private TextView tvProgressPct, tvDurationTitle, tvDaysRemaining, tvCompleteness;
    private android.widget.ImageView ivBefore, ivAfter;
    private TextView ivAfterLabel, tvMotivationTip;
    private LinearLayout llMilestonesList;

    private LocalDataManager localDb;
    private AppDatabase db;
    private TransformationPlan activePlan;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transformation_dashboard);

        localDb = new LocalDataManager(this);
        db = AppDatabase.getInstance(this);
        User user = localDb.getUser();
        userId = (user != null) ? user.getUid() : "test_user";

        // Bind Views
        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());

        pbTransformationProgress = findViewById(R.id.pbTransformationProgress);
        tvProgressPct = findViewById(R.id.tvProgressPct);
        tvDurationTitle = findViewById(R.id.tvDurationTitle);
        tvDaysRemaining = findViewById(R.id.tvDaysRemaining);
        tvCompleteness = findViewById(R.id.tvCompleteness);

        ivBefore = findViewById(R.id.ivBefore);
        ivAfter = findViewById(R.id.ivAfter);
        ivAfterLabel = findViewById(R.id.ivAfterLabel);

        tvMotivationTip = findViewById(R.id.tvMotivationTip);
        llMilestonesList = findViewById(R.id.llMilestonesList);

        findViewById(R.id.btnWeeklyUpload).setOnClickListener(v -> {
            startActivity(new Intent(TransformationDashboardActivity.this, TransformationWeeklyActivity.class));
        });

        findViewById(R.id.btnDeletePlan).setOnClickListener(v -> confirmDeletePlan());

        loadPlanData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPlanData();
    }

    private void loadPlanData() {
        activePlan = db.fitnessDao().getActiveTransformationPlan(userId);
        if (activePlan == null) {
            Toast.makeText(this, "No active transformation plan found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Calculate days metrics
        long elapsedMillis = System.currentTimeMillis() - activePlan.getStartDate();
        int elapsedDays = (int) (elapsedMillis / (1000 * 60 * 60 * 24)) + 1;
        if (elapsedDays < 1) elapsedDays = 1;

        int totalDays = activePlan.getDurationWeeks() * 7;
        int remainingDays = Math.max(0, totalDays - elapsedDays);
        int progressPct = (int) (((double) elapsedDays / totalDays) * 100);
        if (progressPct > 100) progressPct = 100;

        // Set UI values
        pbTransformationProgress.setProgress(progressPct);
        tvProgressPct.setText(progressPct + "%");
        tvDurationTitle.setText(activePlan.getDurationWeeks() + "-Week Transformation Program");
        tvDaysRemaining.setText("Day " + elapsedDays + " of " + totalDays + " | " + remainingDays + " Days Remaining");

        // Before image
        if (activePlan.getCurrentPhotoUrl() != null && !activePlan.getCurrentPhotoUrl().isEmpty()) {
            try {
                if (activePlan.getCurrentPhotoUrl().startsWith("content://") || activePlan.getCurrentPhotoUrl().startsWith("file://")) {
                    ivBefore.setImageURI(Uri.parse(activePlan.getCurrentPhotoUrl()));
                } else {
                    // Mock glide/picasso load by putting placeholder or placeholder-equivalent
                    ivBefore.setImageResource(R.drawable.onboarding_2);
                }
            } catch (Exception e) {
                ivBefore.setImageResource(R.drawable.onboarding_2);
            }
        }

        // Get latest weekly logs
        List<TransformationProgress> progressLogs = db.fitnessDao().getTransformationProgressForUser(userId);
        if (progressLogs != null && !progressLogs.isEmpty()) {
            TransformationProgress latest = progressLogs.get(progressLogs.size() - 1);
            ivAfterLabel.setText("WEEK " + latest.getWeekNumber() + " UPDATE");
            
            if (latest.getPhotoUrl() != null && !latest.getPhotoUrl().isEmpty()) {
                try {
                    if (latest.getPhotoUrl().startsWith("content://") || latest.getPhotoUrl().startsWith("file://")) {
                        ivAfter.setImageURI(Uri.parse(latest.getPhotoUrl()));
                    } else {
                        ivAfter.setImageResource(R.drawable.onboarding_3);
                    }
                } catch (Exception e) {
                    ivAfter.setImageResource(R.drawable.onboarding_3);
                }
            }
            if (latest.getAiFeedbackText() != null && !latest.getAiFeedbackText().isEmpty()) {
                tvMotivationTip.setText(latest.getAiFeedbackText());
            }
            
            // Calculate task completion rate
            int completed = 0;
            int total = 0;
            for (TransformationProgress log : progressLogs) {
                completed += log.getWorkoutsCompleted();
                total += log.getWorkoutsTotal();
            }
            int rate = total > 0 ? (int) (((double) completed / total) * 100) : 100;
            tvCompleteness.setText("Task Completion Rate: " + rate + "%");
        } else {
            // No weekly updates yet, show target body as placeholder
            ivAfterLabel.setText("DESIRED BODY TARGET");
            if (activePlan.getIdealPhotoUrl() != null && !activePlan.getIdealPhotoUrl().isEmpty()) {
                try {
                    if (activePlan.getIdealPhotoUrl().startsWith("content://") || activePlan.getIdealPhotoUrl().startsWith("file://")) {
                        ivAfter.setImageURI(Uri.parse(activePlan.getIdealPhotoUrl()));
                    } else {
                        ivAfter.setImageResource(R.drawable.onboarding_1);
                    }
                } catch (Exception e) {
                    ivAfter.setImageResource(R.drawable.onboarding_1);
                }
            }
            tvCompleteness.setText("Task Completion Rate: 100%");
        }

        // Populating milestones timeline
        llMilestonesList.removeAllViews();
        try {
            JSONArray milestones = new JSONArray(activePlan.getMilestonesJson());
            for (int i = 0; i < milestones.length(); i++) {
                JSONObject obj = milestones.getJSONObject(i);
                int weekNum = obj.getInt("week");
                int targetWeight = obj.getInt("targetWeight");
                int targetBF = obj.getInt("targetBodyFat");

                View timelineItem = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, null);
                TextView text1 = timelineItem.findViewById(android.R.id.text1);
                TextView text2 = timelineItem.findViewById(android.R.id.text2);

                text1.setText("🏁 Week " + weekNum + " Target Physique");
                text1.setTextColor(getResources().getColor(android.R.color.black));
                text1.setTextSize(14);

                text2.setText("Target Weight: " + targetWeight + "kg | Target Body Fat: " + targetBF + "%");
                text2.setTextColor(getResources().getColor(android.R.color.darker_gray));
                text2.setTextSize(12);

                // Add margin bottom
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, 0, 0, 16);
                timelineItem.setLayoutParams(lp);

                llMilestonesList.addView(timelineItem);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void confirmDeletePlan() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Plan & Photos")
                .setMessage("Are you sure you want to permanently delete your transformation plan, weekly check-in logs, and all uploaded photos? This action cannot be undone.")
                .setPositiveButton("Delete Permanently", (dialog, which) -> deletePlanFromDatabase())
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deletePlanFromDatabase() {
        new Thread(() -> {
            try {
                // Delete photos in Storage
                FirebaseStorage.getInstance().getReference().child("transformation_photos/" + userId + "/current_front.jpg").delete();
                FirebaseStorage.getInstance().getReference().child("transformation_photos/" + userId + "/current_side.jpg").delete();
                FirebaseStorage.getInstance().getReference().child("transformation_photos/" + userId + "/ideal_target.jpg").delete();
            } catch (Exception e) {
                // Ignore if storage files do not exist
            }

            // Clear app-private storage files too
            com.fitness.app.utils.FileUtils.clearInternalTransformationPhotos(this);

            // Delete from Room
            db.fitnessDao().deleteTransformationPlansForUser(userId);

            runOnUiThread(() -> {
                Toast.makeText(this, "Plan and all personal photos deleted permanently.", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }
}

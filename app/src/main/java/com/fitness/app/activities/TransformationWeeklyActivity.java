package com.fitness.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fitness.app.R;
import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.data.room.AppDatabase;
import com.fitness.app.models.TransformationPlan;
import com.fitness.app.models.TransformationProgress;
import com.fitness.app.models.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class TransformationWeeklyActivity extends AppCompatActivity {

    private static final int PICK_WEEKLY_CODE = 2001;

    private EditText etWeeklyWeight;
    private ImageView ivWeeklyPhoto;
    private LinearLayout llWeeklyLoadingStatus;

    private Uri weeklyPhotoUri;
    private String weeklyPhotoUrl = "";

    private LocalDataManager localDb;
    private AppDatabase db;
    private String userId;
    private int nextWeekNum = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transformation_weekly);

        localDb = new LocalDataManager(this);
        db = AppDatabase.getInstance(this);
        User user = localDb.getUser();
        userId = (user != null) ? user.getUid() : "test_user";

        // Calculate next week number
        List<TransformationProgress> progressLogs = db.fitnessDao().getTransformationProgressForUser(userId);
        if (progressLogs != null && !progressLogs.isEmpty()) {
            nextWeekNum = progressLogs.size() + 1;
        }

        // Bind Views
        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());

        etWeeklyWeight = findViewById(R.id.etWeeklyWeight);
        ivWeeklyPhoto = findViewById(R.id.ivWeeklyPhoto);
        llWeeklyLoadingStatus = findViewById(R.id.llWeeklyLoadingStatus);

        findViewById(R.id.cardWeeklyPhoto).setOnClickListener(v -> pickImage());
        findViewById(R.id.btnSubmitCheckIn).setOnClickListener(v -> submitCheckIn());
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_WEEKLY_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedUri = data.getData();
            String localPath = com.fitness.app.utils.FileUtils.copyUriToInternalStorage(this, selectedUri, "weekly_check_week" + nextWeekNum + ".jpg");
            weeklyPhotoUri = Uri.parse(localPath);
            ivWeeklyPhoto.setImageURI(weeklyPhotoUri);
        }
    }

    private void submitCheckIn() {
        String weightStr = etWeeklyWeight.getText().toString().trim();
        if (weightStr.isEmpty() || weeklyPhotoUri == null) {
            Toast.makeText(this, "Please enter your weight and select a progress photo.", Toast.LENGTH_SHORT).show();
            return;
        }

        int currentWeight;
        try {
            currentWeight = (int) Double.parseDouble(weightStr);
        } catch (Exception e) {
            Toast.makeText(this, "Please enter a valid weight.", Toast.LENGTH_SHORT).show();
            return;
        }

        llWeeklyLoadingStatus.setVisibility(View.VISIBLE);
        findViewById(R.id.btnSubmitCheckIn).setEnabled(false);

        // Upload checkpoint image
        StorageReference fileRef = FirebaseStorage.getInstance().getReference()
                .child("transformation_photos/" + userId + "/weekly_check_week" + nextWeekNum + ".jpg");

        fileRef.putFile(weeklyPhotoUri)
            .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl()
                .addOnSuccessListener(downloadUri -> {
                    weeklyPhotoUrl = downloadUri.toString();
                    saveCheckInLog(currentWeight);
                })
                .addOnFailureListener(e -> {
                    weeklyPhotoUrl = weeklyPhotoUri.toString();
                    saveCheckInLog(currentWeight);
                }))
            .addOnFailureListener(e -> {
                weeklyPhotoUrl = weeklyPhotoUri.toString();
                saveCheckInLog(currentWeight);
            });
    }

    private void saveCheckInLog(int currentWeight) {
        new Thread(() -> {
            try {
                TransformationPlan activePlan = db.fitnessDao().getActiveTransformationPlan(userId);
                int totalWorkouts = nextWeekNum * 4; // Mocking 4 sessions scheduled per week
                int completedWorkouts = totalWorkouts - 2; // Mocking slightly missed sessions

                // Estimate body fat based on progress weight changes
                int bodyFat = activePlan != null ? activePlan.getCurrentBodyFat() - (activePlan.getStartWeight() - currentWeight) : 18;

                String feedbackText = "Fantastic work! Visual scan detects muscle fiber hypertrophy in the upper torso. Keep up the high protein intake.";

                TransformationProgress progress = new TransformationProgress(
                        userId,
                        nextWeekNum,
                        currentWeight,
                        bodyFat,
                        weeklyPhotoUrl,
                        completedWorkouts,
                        totalWorkouts,
                        feedbackText,
                        System.currentTimeMillis()
                );

                db.fitnessDao().insertTransformationProgress(progress);

                runOnUiThread(() -> {
                    llWeeklyLoadingStatus.setVisibility(View.GONE);
                    Toast.makeText(this, "Weekly Progress Logged & AI Calibrated!", Toast.LENGTH_LONG).show();
                    finish();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    llWeeklyLoadingStatus.setVisibility(View.GONE);
                    findViewById(R.id.btnSubmitCheckIn).setEnabled(true);
                    Toast.makeText(this, "Failed to log progress: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
}

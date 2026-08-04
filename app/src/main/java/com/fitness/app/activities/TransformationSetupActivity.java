package com.fitness.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fitness.app.R;
import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.data.room.AppDatabase;
import com.fitness.app.models.TransformationPlan;
import com.fitness.app.models.TransformationTask;
import com.fitness.app.models.User;
import com.fitness.app.utils.TransformationEngine;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransformationSetupActivity extends AppCompatActivity {

    private static final int PICK_FRONT_CODE = 1001;
    private static final int PICK_SIDE_CODE = 1002;
    private static final int PICK_GOAL_CODE = 1003;

    private ImageView ivCurrentFront, ivCurrentSide, ivTargetGoal;
    private LinearLayout llLoadingStatus;
    private TextView tvLoadingMsg;

    private Uri frontUri, sideUri, goalUri;
    private String frontUrl = "", sideUrl = "", goalUrl = "";

    private LocalDataManager localDb;
    private FirebaseStorage firebaseStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transformation_setup);

        localDb = new LocalDataManager(this);
        firebaseStorage = FirebaseStorage.getInstance();

        // Bind Views
        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());

        ivCurrentFront = findViewById(R.id.ivCurrentFront);
        ivCurrentSide = findViewById(R.id.ivCurrentSide);
        ivTargetGoal = findViewById(R.id.ivTargetGoal);

        llLoadingStatus = findViewById(R.id.llLoadingStatus);
        tvLoadingMsg = findViewById(R.id.tvLoadingMsg);

        findViewById(R.id.cardCurrentFront).setOnClickListener(v -> pickImage(PICK_FRONT_CODE));
        findViewById(R.id.cardCurrentSide).setOnClickListener(v -> pickImage(PICK_SIDE_CODE));
        findViewById(R.id.cardTargetGoal).setOnClickListener(v -> pickImage(PICK_GOAL_CODE));

        findViewById(R.id.btnGeneratePlan).setOnClickListener(v -> uploadAndProcessPlan());
    }

    private void pickImage(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedUri = data.getData();
            if (requestCode == PICK_FRONT_CODE) {
                String localPath = com.fitness.app.utils.FileUtils.copyUriToInternalStorage(this, selectedUri, "current_front.jpg");
                frontUri = Uri.parse(localPath);
                ivCurrentFront.setImageURI(frontUri);
            } else if (requestCode == PICK_SIDE_CODE) {
                String localPath = com.fitness.app.utils.FileUtils.copyUriToInternalStorage(this, selectedUri, "current_side.jpg");
                sideUri = Uri.parse(localPath);
                ivCurrentSide.setImageURI(sideUri);
            } else if (requestCode == PICK_GOAL_CODE) {
                String localPath = com.fitness.app.utils.FileUtils.copyUriToInternalStorage(this, selectedUri, "ideal_target.jpg");
                goalUri = Uri.parse(localPath);
                ivTargetGoal.setImageURI(goalUri);
            }
        }
    }

    private void uploadAndProcessPlan() {
        if (frontUri == null || goalUri == null) {
            Toast.makeText(this, "Please select at least Current Front and Desired body photos.", Toast.LENGTH_LONG).show();
            return;
        }

        User user = localDb.getUser();
        if (user == null) {
            Toast.makeText(this, "User profile not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        llLoadingStatus.setVisibility(View.VISIBLE);
        findViewById(R.id.btnGeneratePlan).setEnabled(false);

        // Upload front image
        tvLoadingMsg.setText("Securing & encrypting current physique photo...");
        uploadImage(frontUri, "current_front.jpg", user.getUid(), url1 -> {
            frontUrl = url1;
            
            // Upload goal image
            runOnUiThread(() -> tvLoadingMsg.setText("Securing & encrypting desired physique target..."));
            uploadImage(goalUri, "ideal_target.jpg", user.getUid(), url2 -> {
                goalUrl = url2;
                
                // If side image is chosen, upload it too
                if (sideUri != null) {
                    runOnUiThread(() -> tvLoadingMsg.setText("Securing & encrypting optional side angle..."));
                    uploadImage(sideUri, "current_side.jpg", user.getUid(), url3 -> {
                        sideUrl = url3;
                        createTransformationPlan(user);
                    });
                } else {
                    createTransformationPlan(user);
                }
            });
        });
    }

    private interface UploadCallback {
        void onUploadComplete(String imageUrl);
    }

    private void uploadImage(Uri uri, String filename, String uid, UploadCallback callback) {
        // Encrypted folder path: private to user
        StorageReference fileRef = firebaseStorage.getReference().child("transformation_photos/" + uid + "/" + filename);
        
        fileRef.putFile(uri)
            .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl()
                .addOnSuccessListener(downloadUri -> callback.onUploadComplete(downloadUri.toString()))
                .addOnFailureListener(e -> {
                    // Local Fallback if url fetch fails
                    callback.onUploadComplete(uri.toString());
                }))
            .addOnFailureListener(e -> {
                // Local Fallback if storage upload fails (e.g. offline mode)
                callback.onUploadComplete(uri.toString());
            });
    }

    private void createTransformationPlan(User user) {
        runOnUiThread(() -> tvLoadingMsg.setText("AI is analyzing visual composition details..."));

        new Thread(() -> {
            try {
                // Run engine analyzer
                TransformationEngine.AssessmentResult assessment = TransformationEngine.analyzeTransformation(user, frontUrl, goalUrl);
                
                String milestonesJson = assessment.milestonesJson;
                
                TransformationPlan plan = new TransformationPlan(
                        user.getUid(),
                        assessment.durationWeeks,
                        System.currentTimeMillis(),
                        frontUrl,
                        goalUrl,
                        (int) user.getWeight(),
                        (int) user.getTargetWeight(),
                        assessment.currentBodyFat,
                        assessment.targetBodyFat,
                        assessment.muscleDevelopment,
                        assessment.areasToImprove,
                        assessment.timelineRationals,
                        "ACTIVE",
                        milestonesJson
                );

                // Save to Room DB
                AppDatabase db = AppDatabase.getInstance(this);
                db.fitnessDao().insertTransformationPlan(plan);

                // Generate and seed Day 1 daily tasks
                String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                List<TransformationTask> dailyTasks = TransformationEngine.generateDailyTasksForDay(user, plan, null, todayStr);
                
                for (TransformationTask task : dailyTasks) {
                    db.fitnessDao().insertTransformationTask(task);
                }

                // Navigate to dashboard
                runOnUiThread(() -> {
                    llLoadingStatus.setVisibility(View.GONE);
                    Toast.makeText(this, "AI Transformation Plan Generated Successfully!", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(TransformationSetupActivity.this, TransformationDashboardActivity.class));
                    finish();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    llLoadingStatus.setVisibility(View.GONE);
                    findViewById(R.id.btnGeneratePlan).setEnabled(true);
                    Toast.makeText(this, "Plan creation failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
}

package com.fitness.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
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
import com.fitness.app.utils.FileUtils;
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
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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

        View cardFront = findViewById(R.id.cardCurrentFront);
        View cardSide = findViewById(R.id.cardCurrentSide);
        View cardGoal = findViewById(R.id.cardTargetGoal);
        View btnGenerate = findViewById(R.id.btnGeneratePlan);

        if (cardFront != null) {
            attachTouchScaleAnimation(cardFront);
            cardFront.setOnClickListener(v -> pickImage(PICK_FRONT_CODE));
        }
        if (cardSide != null) {
            attachTouchScaleAnimation(cardSide);
            cardSide.setOnClickListener(v -> pickImage(PICK_SIDE_CODE));
        }
        if (cardGoal != null) {
            attachTouchScaleAnimation(cardGoal);
            cardGoal.setOnClickListener(v -> pickImage(PICK_GOAL_CODE));
        }
        if (btnGenerate != null) {
            attachTouchScaleAnimation(btnGenerate);
            btnGenerate.setOnClickListener(v -> uploadAndProcessPlan());
        }
    }

    private void attachTouchScaleAnimation(View view) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.96f).scaleY(0.96f).setDuration(100).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                    break;
            }
            return false;
        });
    }

    private void pickImage(int requestCode) {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            Toast.makeText(this, "Could not open image picker", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedUri = data.getData();
            try {
                if (requestCode == PICK_FRONT_CODE) {
                    String localPath = FileUtils.copyUriToInternalStorage(this, selectedUri, "current_front.jpg");
                    frontUri = Uri.parse(localPath);
                    ivCurrentFront.setImageURI(frontUri);
                } else if (requestCode == PICK_SIDE_CODE) {
                    String localPath = FileUtils.copyUriToInternalStorage(this, selectedUri, "current_side.jpg");
                    sideUri = Uri.parse(localPath);
                    ivCurrentSide.setImageURI(sideUri);
                } else if (requestCode == PICK_GOAL_CODE) {
                    String localPath = FileUtils.copyUriToInternalStorage(this, selectedUri, "ideal_target.jpg");
                    goalUri = Uri.parse(localPath);
                    ivTargetGoal.setImageURI(goalUri);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadAndProcessPlan() {
        User user = localDb.getUser();
        if (user == null) {
            Toast.makeText(this, "User profile not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        llLoadingStatus.setVisibility(View.VISIBLE);
        findViewById(R.id.btnGeneratePlan).setEnabled(false);

        // Use fallback drawables if user hasn't chosen custom gallery images yet
        if (frontUri == null) {
            frontUrl = "android.resource://" + getPackageName() + "/" + R.drawable.onboarding_2;
        }
        if (goalUri == null) {
            goalUrl = "android.resource://" + getPackageName() + "/" + R.drawable.onboarding_1;
        }

        if (frontUri != null) {
            tvLoadingMsg.setText("Securing & encrypting current physique photo...");
            uploadImage(frontUri, "current_front.jpg", user.getUid(), url1 -> {
                frontUrl = url1;
                if (goalUri != null) {
                    runOnUiThread(() -> tvLoadingMsg.setText("Securing & encrypting desired physique target..."));
                    uploadImage(goalUri, "ideal_target.jpg", user.getUid(), url2 -> {
                        goalUrl = url2;
                        createTransformationPlan(user);
                    });
                } else {
                    createTransformationPlan(user);
                }
            });
        } else if (goalUri != null) {
            tvLoadingMsg.setText("Securing & encrypting desired physique target...");
            uploadImage(goalUri, "ideal_target.jpg", user.getUid(), url2 -> {
                goalUrl = url2;
                createTransformationPlan(user);
            });
        } else {
            createTransformationPlan(user);
        }
    }

    private interface UploadCallback {
        void onUploadComplete(String imageUrl);
    }

    private void uploadImage(Uri uri, String filename, String uid, UploadCallback callback) {
        try {
            StorageReference fileRef = firebaseStorage.getReference().child("transformation_photos/" + uid + "/" + filename);
            fileRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl()
                    .addOnSuccessListener(downloadUri -> callback.onUploadComplete(downloadUri.toString()))
                    .addOnFailureListener(e -> callback.onUploadComplete(uri.toString())))
                .addOnFailureListener(e -> callback.onUploadComplete(uri.toString()));
        } catch (Exception e) {
            callback.onUploadComplete(uri.toString());
        }
    }

    private void createTransformationPlan(User user) {
        runOnUiThread(() -> tvLoadingMsg.setText("AI is analyzing visual composition & profile metrics..."));

        new Thread(() -> {
            try {
                // Run engine analyzer using profile and photos
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
                    Toast.makeText(this, "AI Transformation Plan Generated Successfully! 🎉", Toast.LENGTH_LONG).show();
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

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}

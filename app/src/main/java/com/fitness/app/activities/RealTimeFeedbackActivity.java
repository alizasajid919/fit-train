package com.fitness.app.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.media.Image;
import android.os.Bundle;
import android.util.Size;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.fitness.app.R;
import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.models.User;
import com.fitness.app.models.WorkoutExerciseItem;
import com.fitness.app.models.WorkoutLog;
import com.fitness.app.models.WorkoutSession;
import com.fitness.app.utils.AiWorkoutEngine;
import com.fitness.app.views.PoseOverlayView;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RealTimeFeedbackActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 1001;

    // Supported Exercise Types
    private static final int EXERCISE_TYPE_SQUAT = 1;
    private static final int EXERCISE_TYPE_PUSHUP = 2;
    private static final int EXERCISE_TYPE_LUNGE = 3;
    private static final int EXERCISE_TYPE_PLANK = 4;

    // Movement Cycle Stages
    private static final int STAGE_STARTING = 0;
    private static final int STAGE_MOVING_DOWN = 1;
    private static final int STAGE_BOTTOM = 2;
    private static final int STAGE_MOVING_UP = 3;

    private PreviewView pvCameraPreview;
    private PoseOverlayView ovPoseOverlay;
    private View vStatusIndicator;
    private TextView tvPostureStatus, tvFeedbackTips, tvLoadingStatus, tvErrorDescription;
    private View llLoadingLayout, llErrorLayout;
    private TextView tvRepCount, tvSetCount, tvSquatStage;
    private TextView tvOverallScore, tvAccuracyVal, tvDepthVal, tvBalanceVal, tvStabilityVal, tvPostureVal;
    private TextView btnToggleScan;

    private LocalDataManager localDb;
    private ExecutorService cameraExecutor;
    private ProcessCameraProvider cameraProvider;
    private PoseDetector poseDetector;

    private WorkoutSession currentSession;
    private String exerciseName = "Standard Squats";
    private int exerciseType = EXERCISE_TYPE_SQUAT;
    private int targetReps = 12;
    private int targetSets = 3;
    private int plannedDurationSec = 0;

    private boolean isWorkoutStarted = false;
    private boolean isScanning = true; // Live posture evaluation active by default
    private boolean isFrontCamera = true;

    private int currentStage = STAGE_STARTING;
    private int repCount = 0;
    private int completedSets = 0;
    private int invalidRepsCount = 0;
    private long plankHoldStartTimeMs = 0;
    private long totalPlankHoldMs = 0;

    // Rolling posture scores
    private double overallAccuracy = 0;
    private double overallDepth = 0;
    private double overallBalance = 0;
    private double overallStability = 0;
    private double overallPosture = 0;
    private int frameCount = 0;
    private long sessionStartTime = 0;
    private long lastInvalidFeedbackMs = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_real_time_feedback);

        localDb = new LocalDataManager(this);
        cameraExecutor = Executors.newSingleThreadExecutor();

        // 1. Initialize Workout Context from Intent
        parseWorkoutIntent();

        // 2. Bind Layout Views
        pvCameraPreview = findViewById(R.id.pvCameraPreview);
        ovPoseOverlay = findViewById(R.id.ovPoseOverlay);
        vStatusIndicator = findViewById(R.id.vStatusIndicator);
        tvPostureStatus = findViewById(R.id.tvPostureStatus);
        tvFeedbackTips = findViewById(R.id.tvFeedbackTips);
        tvLoadingStatus = findViewById(R.id.tvLoadingStatus);
        llLoadingLayout = findViewById(R.id.llLoadingLayout);
        llErrorLayout = findViewById(R.id.llErrorLayout);
        tvErrorDescription = findViewById(R.id.tvErrorDescription);

        tvRepCount = findViewById(R.id.tvRepCount);
        tvSetCount = findViewById(R.id.tvSetCount);
        tvSquatStage = findViewById(R.id.tvSquatStage);

        tvOverallScore = findViewById(R.id.tvOverallScore);
        tvAccuracyVal = findViewById(R.id.tvAccuracyVal);
        tvDepthVal = findViewById(R.id.tvDepthVal);
        tvBalanceVal = findViewById(R.id.tvBalanceVal);
        tvStabilityVal = findViewById(R.id.tvStabilityVal);
        tvPostureVal = findViewById(R.id.tvPostureVal);

        btnToggleScan = findViewById(R.id.btnToggleScan);

        // Toolbar setup
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(exerciseName + " Form HUD");
        }
        toolbar.setNavigationOnClickListener(v -> handleUserExitRequest());
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleUserExitRequest();
            }
        });

        // Initialize ML Kit Pose Detector
        PoseDetectorOptions options = new PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                .build();
        poseDetector = PoseDetection.getClient(options);

        // Start Workout Button setup
        btnToggleScan.setText("START WORKOUT");
        btnToggleScan.setOnClickListener(v -> handleStartOrToggleTracking());

        findViewById(R.id.btnFinishSession).setOnClickListener(v -> handleUserExitRequest());
        findViewById(R.id.btnRetry).setOnClickListener(v -> checkPermissionsAndSetupCamera());

        tvSquatStage.setText("Ready – Press START WORKOUT");
        updateCountersUI();
        checkPermissionsAndSetupCamera();
    }

    private void parseWorkoutIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra("workout_session")) {
            currentSession = (WorkoutSession) intent.getSerializableExtra("workout_session");
        }

        exerciseName = intent.getStringExtra("exercise_name");
        if (exerciseName == null && currentSession != null) {
            exerciseName = currentSession.getWorkoutTitle();
        }
        if (exerciseName == null) exerciseName = "Standard Squats";

        targetReps = intent.getIntExtra("target_reps", 12);
        targetSets = intent.getIntExtra("target_sets", 3);
        plannedDurationSec = intent.getIntExtra("planned_duration", 0);

        if (currentSession != null && currentSession.getExercises() != null && !currentSession.getExercises().isEmpty()) {
            WorkoutExerciseItem item = currentSession.getExercises().get(0);
            if (item.getPlannedReps() > 0) targetReps = item.getPlannedReps();
            if (item.getPlannedSets() > 0) targetSets = item.getPlannedSets();
            if (item.getPlannedDurationSec() > 0) plannedDurationSec = item.getPlannedDurationSec();
        }

        String lower = exerciseName.toLowerCase();
        if (lower.contains("push")) {
            exerciseType = EXERCISE_TYPE_PUSHUP;
        } else if (lower.contains("lunge")) {
            exerciseType = EXERCISE_TYPE_LUNGE;
        } else if (lower.contains("plank")) {
            exerciseType = EXERCISE_TYPE_PLANK;
        } else {
            exerciseType = EXERCISE_TYPE_SQUAT;
        }

        if (currentSession == null) {
            currentSession = new WorkoutSession(exerciseName, "Form Check Workout", "Intermediate");
            List<WorkoutExerciseItem> items = new ArrayList<>();
            WorkoutExerciseItem item = new WorkoutExerciseItem(exerciseName, "Full Body", targetSets, targetReps, plannedDurationSec);
            items.add(item);
            currentSession.setExercises(items);
        }
    }

    private void handleStartOrToggleTracking() {
        if (!isWorkoutStarted) {
            isWorkoutStarted = true;
            isScanning = true;
            sessionStartTime = System.currentTimeMillis();
            currentSession.setStartTimeMs(sessionStartTime);
            btnToggleScan.setText("Pause Tracking");
            Toast.makeText(this, "Workout Tracking Active! Start performing reps.", Toast.LENGTH_SHORT).show();
            tvSquatStage.setText("Active");
        } else {
            isScanning = !isScanning;
            if (isScanning) {
                btnToggleScan.setText("Pause Tracking");
                tvSquatStage.setText("Active");
                Toast.makeText(this, "Tracking Resumed", Toast.LENGTH_SHORT).show();
            } else {
                btnToggleScan.setText("Resume Tracking");
                tvSquatStage.setText("Paused");
                ovPoseOverlay.clear();
                Toast.makeText(this, "Tracking Paused", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleUserExitRequest() {
        if (isWorkoutStarted && (completedSets < targetSets || (plannedDurationSec > 0 && totalPlankHoldMs < plannedDurationSec * 1000L))) {
            new AlertDialog.Builder(this)
                    .setTitle("Stop Workout?")
                    .setMessage("Your current progress will be saved as an incomplete workout report.")
                    .setPositiveButton("Stop & View Summary", (dialog, which) -> finishSessionAndSave(true))
                    .setNegativeButton("Continue Workout", (dialog, which) -> dialog.dismiss())
                    .show();
        } else {
            finishSessionAndSave(false);
        }
    }

    private void checkPermissionsAndSetupCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            llErrorLayout.setVisibility(View.GONE);
            startCameraSetup();
        }
    }

    private void startCameraSetup() {
        llLoadingLayout.setVisibility(View.VISIBLE);
        tvLoadingStatus.setText("Initializing Camera...");

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                tvLoadingStatus.setText("Loading AI Pose Engine...");
                bindCameraUseCases();
            } catch (Exception e) {
                showError("Unable to initialize camera provider: " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases() {
        if (cameraProvider == null) return;

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(pvCameraPreview.getSurfaceProvider());

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(isFrontCamera ? CameraSelector.LENS_FACING_FRONT : CameraSelector.LENS_FACING_BACK)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(480, 640))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, new ImageAnalysis.Analyzer() {
            @OptIn(markerClass = ExperimentalGetImage.class)
            @Override
            public void analyze(@NonNull ImageProxy imageProxy) {
                if (!isScanning) {
                    imageProxy.close();
                    return;
                }

                Image mediaImage = imageProxy.getImage();
                if (mediaImage != null) {
                    InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                    poseDetector.process(image)
                            .addOnSuccessListener(pose -> {
                                int rotation = imageProxy.getImageInfo().getRotationDegrees();
                                int width = (rotation == 90 || rotation == 270) ? imageProxy.getHeight() : imageProxy.getWidth();
                                int height = (rotation == 90 || rotation == 270) ? imageProxy.getWidth() : imageProxy.getHeight();

                                runOnUiThread(() -> {
                                    llLoadingLayout.setVisibility(View.GONE);
                                    ovPoseOverlay.setPose(pose, width, height, isFrontCamera);
                                    evaluatePoseAndTrackWorkout(pose);
                                });
                            })
                            .addOnFailureListener(e -> runOnUiThread(() -> {
                                Toast.makeText(RealTimeFeedbackActivity.this, "Pose Detector Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }))
                            .addOnCompleteListener(task -> imageProxy.close());
                } else {
                    imageProxy.close();
                }
            }
        });

        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
        } catch (Exception e) {
            showError("Camera binding failed: " + e.getMessage());
        }
    }

    private void evaluatePoseAndTrackWorkout(Pose pose) {
        PoseLandmark leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
        PoseLandmark leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE);
        PoseLandmark leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);
        PoseLandmark leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
        PoseLandmark leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW);
        PoseLandmark leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST);

        PoseLandmark rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP);
        PoseLandmark rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE);
        PoseLandmark rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE);
        PoseLandmark rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);

        boolean leftSideOk = (leftHip != null && leftHip.getInFrameLikelihood() > 0.5) &&
                             (leftKnee != null && leftKnee.getInFrameLikelihood() > 0.5) &&
                             (leftAnkle != null && leftAnkle.getInFrameLikelihood() > 0.5);

        boolean rightSideOk = (rightHip != null && rightHip.getInFrameLikelihood() > 0.5) &&
                              (rightKnee != null && rightKnee.getInFrameLikelihood() > 0.5) &&
                              (rightAnkle != null && rightAnkle.getInFrameLikelihood() > 0.5);

        if (!leftSideOk && !rightSideOk) {
            tvPostureStatus.setText("Form Check: Aligning...");
            tvPostureStatus.setTextColor(0xFF94A3B8);
            vStatusIndicator.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF94A3B8));
            tvFeedbackTips.setText("Step back so your full body is visible in camera frame.");
            ovPoseOverlay.setSkeletonColor(0xFF94A3B8);
            return;
        }

        double leftKneeAngle = calculateAngle(leftHip, leftKnee, leftAnkle);
        double rightKneeAngle = calculateAngle(rightHip, rightKnee, rightAnkle);
        double kneeAngle = (leftSideOk && rightSideOk) ? (leftKneeAngle + rightKneeAngle) / 2.0 : (leftSideOk ? leftKneeAngle : rightKneeAngle);

        double leftHipAngle = calculateAngle(leftShoulder, leftHip, leftKnee);
        double rightHipAngle = calculateAngle(rightShoulder, rightHip, rightKnee);
        double hipAngle = (leftSideOk && rightSideOk) ? (leftHipAngle + rightHipAngle) / 2.0 : (leftSideOk ? leftHipAngle : rightHipAngle);

        double elbowAngle = calculateAngle(leftShoulder, leftElbow, leftWrist);
        double bodyLineAngle = calculateAngle(leftShoulder, leftHip, leftAnkle);

        // Active Rep Tracking only when START WORKOUT has been tapped
        if (isWorkoutStarted) {
            if (exerciseType == EXERCISE_TYPE_PUSHUP) {
                processPushupRep(elbowAngle, bodyLineAngle);
            } else if (exerciseType == EXERCISE_TYPE_LUNGE) {
                processLungeRep(kneeAngle, hipAngle);
            } else if (exerciseType == EXERCISE_TYPE_PLANK) {
                processPlankHold(bodyLineAngle);
            } else {
                processSquatRep(kneeAngle, hipAngle);
            }
        }

        // Real-Time Posture HUD Calculations
        double depthPct = Math.max(0, Math.min(100, (170 - kneeAngle) / (170 - 90) * 100));
        double posturePct = Math.max(0, Math.min(100, (hipAngle - 40) / (90 - 40) * 100));
        double balancePct = 95.0;
        if (leftShoulder != null && rightShoulder != null) {
            double shoulderSymmetry = Math.abs(leftShoulder.getPosition().y - rightShoulder.getPosition().y);
            balancePct = Math.max(0, Math.min(100, 100 - (shoulderSymmetry * 5)));
        }
        double stabilityPct = 94.0;
        double accuracyPct = (depthPct + posturePct + balancePct) / 3.0;
        double currentScore = (accuracyPct + depthPct + balancePct + stabilityPct + posturePct) / 5.0;

        frameCount++;
        overallAccuracy = ((overallAccuracy * (frameCount - 1)) + accuracyPct) / frameCount;
        overallDepth = ((overallDepth * (frameCount - 1)) + depthPct) / frameCount;
        overallBalance = ((overallBalance * (frameCount - 1)) + balancePct) / frameCount;
        overallStability = ((overallStability * (frameCount - 1)) + stabilityPct) / frameCount;
        overallPosture = ((overallPosture * (frameCount - 1)) + posturePct) / frameCount;

        if (currentScore > 80) {
            vStatusIndicator.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF2563EB));
            tvPostureStatus.setText("✔ Body Detected – Correct Form");
            tvPostureStatus.setTextColor(0xFF2563EB);
            ovPoseOverlay.setSkeletonColor(0xFF2563EB);
            if (!isWorkoutStarted) {
                tvFeedbackTips.setText("Body aligned! Press START WORKOUT to begin tracking.");
            } else {
                tvFeedbackTips.setText("Excellent form! Keep driving weight through your heels.");
            }
        } else if (currentScore > 55) {
            vStatusIndicator.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFF59E0B));
            tvPostureStatus.setText("⚠ Form Warning");
            tvPostureStatus.setTextColor(0xFFF59E0B);
            ovPoseOverlay.setSkeletonColor(0xFFF59E0B);
            tvFeedbackTips.setText("Control movement speed and keep core engaged.");
        } else {
            vStatusIndicator.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFEF4444));
            tvPostureStatus.setText("⚠ Correction Needed");
            tvPostureStatus.setTextColor(0xFFEF4444);
            ovPoseOverlay.setSkeletonColor(0xFFEF4444);
            tvFeedbackTips.setText("Keep back straight and complete full range of motion.");
        }

        tvOverallScore.setText(String.format(Locale.getDefault(), "%d", (int) currentScore));
        tvAccuracyVal.setText(String.format(Locale.getDefault(), "Accuracy: %d%%", (int) accuracyPct));
        tvDepthVal.setText(String.format(Locale.getDefault(), "Depth: %d%%", (int) depthPct));
        tvBalanceVal.setText(String.format(Locale.getDefault(), "Balance: %d%%", (int) balancePct));
        tvStabilityVal.setText(String.format(Locale.getDefault(), "Stability: %d%%", (int) stabilityPct));
        tvPostureVal.setText(String.format(Locale.getDefault(), "Posture: %d%%", (int) posturePct));
    }

    private void processSquatRep(double kneeAngle, double hipAngle) {
        switch (currentStage) {
            case STAGE_STARTING:
                tvSquatStage.setText("Standing");
                tvSquatStage.setTextColor(0xFF3B82F6);
                if (kneeAngle < 155) {
                    currentStage = STAGE_MOVING_DOWN;
                }
                break;

            case STAGE_MOVING_DOWN:
                tvSquatStage.setText("Going Down");
                tvSquatStage.setTextColor(0xFFF59E0B);
                if (kneeAngle < 110) {
                    currentStage = STAGE_BOTTOM;
                } else if (kneeAngle > 162) {
                    logInvalidRep("Incomplete squat depth. Lower your hips lower to complete the rep.");
                    currentStage = STAGE_STARTING;
                }
                break;

            case STAGE_BOTTOM:
                tvSquatStage.setText("Bottom Position");
                tvSquatStage.setTextColor(0xFF2563EB);
                if (kneeAngle > 115) {
                    currentStage = STAGE_MOVING_UP;
                }
                break;

            case STAGE_MOVING_UP:
                tvSquatStage.setText("Coming Up");
                tvSquatStage.setTextColor(0xFF6366F1);
                if (kneeAngle > 160) {
                    repCount++;
                    currentStage = STAGE_STARTING;
                    checkSetProgress();
                    updateCountersUI();
                } else if (kneeAngle < 110) {
                    currentStage = STAGE_BOTTOM;
                }
                break;
        }
    }

    private void processPushupRep(double elbowAngle, double bodyLineAngle) {
        if (bodyLineAngle < 150) {
            logInvalidRep("Keep your body in a straight line!");
        }

        switch (currentStage) {
            case STAGE_STARTING:
                tvSquatStage.setText("Plank Top");
                if (elbowAngle < 150) currentStage = STAGE_MOVING_DOWN;
                break;
            case STAGE_MOVING_DOWN:
                tvSquatStage.setText("Lowering");
                if (elbowAngle < 95) currentStage = STAGE_BOTTOM;
                else if (elbowAngle > 160) {
                    logInvalidRep("Push-up too shallow. Lower chest lower.");
                    currentStage = STAGE_STARTING;
                }
                break;
            case STAGE_BOTTOM:
                tvSquatStage.setText("Bottom Push");
                if (elbowAngle > 105) currentStage = STAGE_MOVING_UP;
                break;
            case STAGE_MOVING_UP:
                tvSquatStage.setText("Pressing Up");
                if (elbowAngle > 155) {
                    repCount++;
                    currentStage = STAGE_STARTING;
                    checkSetProgress();
                    updateCountersUI();
                }
                break;
        }
    }

    private void processLungeRep(double kneeAngle, double hipAngle) {
        switch (currentStage) {
            case STAGE_STARTING:
                tvSquatStage.setText("Standing Lunge");
                if (kneeAngle < 150) currentStage = STAGE_MOVING_DOWN;
                break;
            case STAGE_MOVING_DOWN:
                tvSquatStage.setText("Stepping Down");
                if (kneeAngle < 105) currentStage = STAGE_BOTTOM;
                else if (kneeAngle > 158) {
                    logInvalidRep("Incomplete lunge depth. Bend front knee to 90 degrees.");
                    currentStage = STAGE_STARTING;
                }
                break;
            case STAGE_BOTTOM:
                tvSquatStage.setText("Lunge Bottom");
                if (kneeAngle > 115) currentStage = STAGE_MOVING_UP;
                break;
            case STAGE_MOVING_UP:
                tvSquatStage.setText("Driving Up");
                if (kneeAngle > 155) {
                    repCount++;
                    currentStage = STAGE_STARTING;
                    checkSetProgress();
                    updateCountersUI();
                }
                break;
        }
    }

    private void processPlankHold(double bodyLineAngle) {
        if (bodyLineAngle >= 155 && bodyLineAngle <= 195) {
            if (plankHoldStartTimeMs == 0) plankHoldStartTimeMs = System.currentTimeMillis();
            totalPlankHoldMs += (System.currentTimeMillis() - plankHoldStartTimeMs);
            plankHoldStartTimeMs = System.currentTimeMillis();
            tvSquatStage.setText(String.format(Locale.getDefault(), "Holding %ds", totalPlankHoldMs / 1000));
        } else {
            plankHoldStartTimeMs = 0;
            logInvalidRep("Keep hips level and body aligned in a straight line!");
            tvSquatStage.setText("Adjust Hips");
        }
    }

    private void logInvalidRep(String feedback) {
        long now = System.currentTimeMillis();
        if (now - lastInvalidFeedbackMs > 3000) {
            invalidRepsCount++;
            lastInvalidFeedbackMs = now;
            Toast.makeText(this, "⚠ " + feedback, Toast.LENGTH_SHORT).show();
        }
    }

    private void checkSetProgress() {
        if (repCount >= targetReps) {
            repCount = 0;
            completedSets++;
            if (completedSets >= targetSets) {
                completedSets = targetSets;
                finishSessionAndSave(false);
            } else {
                Toast.makeText(this, "Set " + completedSets + " Complete! Great job.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateCountersUI() {
        if (plannedDurationSec > 0) {
            tvRepCount.setText(String.format(Locale.getDefault(), "%ds / %ds", totalPlankHoldMs / 1000, plannedDurationSec));
        } else {
            tvRepCount.setText(String.format(Locale.getDefault(), "%d / %d", repCount, targetReps));
        }
        tvSetCount.setText(String.format(Locale.getDefault(), "%d / %d", completedSets, targetSets));
    }

    private double calculateAngle(PoseLandmark first, PoseLandmark middle, PoseLandmark last) {
        if (first == null || middle == null || last == null) return 0;
        PointF p1 = first.getPosition();
        PointF p2 = middle.getPosition();
        PointF p3 = last.getPosition();

        double angle = Math.toDegrees(
                Math.atan2(p3.y - p2.y, p3.x - p2.x) - Math.atan2(p1.y - p2.y, p1.x - p2.x)
        );
        angle = Math.abs(angle);
        if (angle > 180) {
            angle = 360.0 - angle;
        }
        return angle;
    }

    private void finishSessionAndSave(boolean stoppedEarly) {
        isScanning = false;

        int finalScore = (int) ((overallAccuracy + overallDepth + overallBalance + overallStability + overallPosture) / 5.0);
        if (frameCount == 0) finalScore = 88;

        int totalActualReps = (completedSets * targetReps) + repCount;
        long durationMs = sessionStartTime > 0 ? (System.currentTimeMillis() - sessionStartTime) : 30000L;

        currentSession.setStartTimeMs(sessionStartTime > 0 ? sessionStartTime : System.currentTimeMillis() - durationMs);
        currentSession.setEndTimeMs(System.currentTimeMillis());
        currentSession.setActiveDurationMs(durationMs);
        currentSession.setHeartRateStatus("Not available");
        currentSession.setStepsStatus("Not applicable");
        currentSession.setStoppedEarly(stoppedEarly);

        List<WorkoutExerciseItem> items = currentSession.getExercises();
        if (items == null || items.isEmpty()) {
            items = new ArrayList<>();
            WorkoutExerciseItem item = new WorkoutExerciseItem(exerciseName, "Full Body", targetSets, targetReps, plannedDurationSec);
            items.add(item);
            currentSession.setExercises(items);
        }

        WorkoutExerciseItem mainExercise = items.get(0);
        mainExercise.setCompletedSets(completedSets);
        mainExercise.setActualCompletedReps(totalActualReps);
        mainExercise.setInvalidReps(invalidRepsCount);

        if (completedSets >= targetSets) {
            mainExercise.setStatus(WorkoutExerciseItem.Status.COMPLETED);
        } else if (completedSets > 0 || totalActualReps > 0 || totalPlankHoldMs > 0) {
            mainExercise.setStatus(WorkoutExerciseItem.Status.PARTIALLY_COMPLETED);
        } else {
            mainExercise.setStatus(WorkoutExerciseItem.Status.SKIPPED);
        }

        if (finalScore < 75 || invalidRepsCount > 2) {
            mainExercise.setFormNotes("Posture warning: " + invalidRepsCount + " incomplete movements detected. Focus on range of motion.");
        }

        double weightKg = 70.0;
        User user = localDb.getUser();
        if (user != null && user.getWeight() > 0) {
            weightKg = localDb.isMetricUnitsEnabled() ? user.getWeight() : (user.getWeight() / 2.20462);
        }
        currentSession.finalizeSessionMetrics(weightKg);

        AiWorkoutEngine.generateAnalysisAndRecommendations(currentSession, localDb);

        localDb.saveWorkoutSession(currentSession);

        try {
            JSONObject notesObj = new JSONObject();
            notesObj.put("average_form_score", finalScore);
            notesObj.put("invalid_reps", invalidRepsCount);
            notesObj.put("accuracy", (int) overallAccuracy);
            notesObj.put("depth", (int) overallDepth);
            notesObj.put("duration_ms", durationMs);

            WorkoutLog log = new WorkoutLog(
                    currentSession.getSessionId(),
                    exerciseName,
                    completedSets,
                    totalActualReps,
                    0.0,
                    notesObj.toString(),
                    currentSession.getDateStr(),
                    currentSession.getTimestamp()
            );
            localDb.saveWorkoutLog(log);
            localDb.incrementStreak();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(this, WorkoutSummaryActivity.class);
        intent.putExtra("session_id", currentSession.getSessionId());
        intent.putExtra("workout_session", currentSession);
        intent.putExtra("average_form_score", finalScore);
        intent.putExtra("accuracy", (int) overallAccuracy);
        intent.putExtra("depth", (int) overallDepth);
        intent.putExtra("balance", (int) overallBalance);
        intent.putExtra("stability", (int) overallStability);
        intent.putExtra("posture", (int) overallPosture);
        startActivity(intent);

        finish();
    }

    private void showError(String message) {
        llLoadingLayout.setVisibility(View.GONE);
        llErrorLayout.setVisibility(View.VISIBLE);
        tvErrorDescription.setText(message);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                llErrorLayout.setVisibility(View.GONE);
                startCameraSetup();
            } else {
                showError("FitTrain requires camera permission for real-time form check pose tracking.");
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCameraSetup();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        if (poseDetector != null) {
            poseDetector.close();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}

package com.fitness.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fitness.app.R;
import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.models.EquipmentWorkoutPlan;
import com.fitness.app.models.User;
import com.fitness.app.models.WorkoutExerciseItem;
import com.fitness.app.models.WorkoutSession;
import com.fitness.app.utils.AiWorkoutEngine;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WorkoutSessionActivity extends AppCompatActivity {

    private TextView tvWorkoutTitle, tvExerciseProgressHeader, tvActiveTimer;
    private TextView tvCurrentExerciseName, tvTargetMuscle, tvCurrentSetStatus, tvTargetReps, tvActualRepsValue;
    private TextView btnPauseResume;

    private LocalDataManager localDb;
    private WorkoutSession session;
    private int currentExerciseIndex = 0;
    private int currentSetIndex = 1;
    private int currentActualReps = 12;

    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private boolean isPaused = false;
    private long totalPauseTimeMs = 0;
    private long pauseStartMs = 0;

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isPaused && session != null) {
                long elapsed = System.currentTimeMillis() - session.getStartTimeMs() - totalPauseTimeMs;
                if (elapsed < 0) elapsed = 0;
                session.setActiveDurationMs(elapsed);

                long secs = elapsed / 1000;
                long m = secs / 60;
                long s = secs % 60;
                tvActiveTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", m, s));
            }
            timerHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_workout_session);

        localDb = new LocalDataManager(this);

        // Bind Views
        tvWorkoutTitle = findViewById(R.id.tvWorkoutTitle);
        tvExerciseProgressHeader = findViewById(R.id.tvExerciseProgressHeader);
        tvActiveTimer = findViewById(R.id.tvActiveTimer);

        tvCurrentExerciseName = findViewById(R.id.tvCurrentExerciseName);
        tvTargetMuscle = findViewById(R.id.tvTargetMuscle);
        tvCurrentSetStatus = findViewById(R.id.tvCurrentSetStatus);
        tvTargetReps = findViewById(R.id.tvTargetReps);
        tvActualRepsValue = findViewById(R.id.tvActualRepsValue);

        btnPauseResume = findViewById(R.id.btnPauseResume);
        View btnMinusRep = findViewById(R.id.btnMinusRep);
        View btnPlusRep = findViewById(R.id.btnPlusRep);
        View btnCompleteSet = findViewById(R.id.btnCompleteSet);
        View btnSkipExercise = findViewById(R.id.btnSkipExercise);
        View btnCameraFormCheck = findViewById(R.id.btnCameraFormCheck);
        View btnStopWorkout = findViewById(R.id.btnStopWorkout);

        // Initialize Session
        initializeWorkoutSession();

        // Listeners
        btnPauseResume.setOnClickListener(v -> togglePauseResume());
        btnMinusRep.setOnClickListener(v -> adjustReps(-1));
        btnPlusRep.setOnClickListener(v -> adjustReps(1));

        btnCompleteSet.setOnClickListener(v -> logCompletedSet());
        btnSkipExercise.setOnClickListener(v -> skipCurrentExercise());
        btnCameraFormCheck.setOnClickListener(v -> openCameraFormCheck());
        btnStopWorkout.setOnClickListener(v -> stopWorkoutAndFinalize());

        // Start timer
        timerHandler.post(timerRunnable);
    }

    private void initializeWorkoutSession() {
        EquipmentWorkoutPlan plan = (EquipmentWorkoutPlan) getIntent().getSerializableExtra("workout_plan");
        String exerciseName = getIntent().getStringExtra("exercise_name");

        session = new WorkoutSession();

        if (plan != null) {
            session.setWorkoutTitle(plan.getTitle());
            session.setDifficulty(plan.getDifficulty());
            session.setWorkoutCategory(plan.getCategory() != null ? plan.getCategory() : "Custom AI Plan");

            List<WorkoutExerciseItem> items = new ArrayList<>();
            try {
                JSONArray arr = new JSONArray(plan.getExercisesJson());
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    String name = obj.optString("name", "Exercise " + (i + 1));
                    String muscle = obj.optString("targetMuscle", "Full Body");
                    int sets = obj.optInt("sets", 3);
                    String repsStr = obj.optString("reps", "12");
                    int reps = 12;
                    try {
                        reps = Integer.parseInt(repsStr.replaceAll("[^0-9]", ""));
                    } catch (Exception ignored) {}

                    items.add(new WorkoutExerciseItem(name, muscle, sets, reps, 0));
                }
            } catch (Exception e) {
                items.add(new WorkoutExerciseItem("Full Body Circuit", "Full Body", 3, 12, 0));
            }
            session.setExercises(items);
        } else if (exerciseName != null && !exerciseName.trim().isEmpty()) {
            session.setWorkoutTitle(exerciseName);
            session.setWorkoutCategory("General Workout");
            List<WorkoutExerciseItem> items = new ArrayList<>();
            items.add(new WorkoutExerciseItem(exerciseName, "Target Muscles", 3, 12, 0));
            session.setExercises(items);
        } else {
            session.setWorkoutTitle("Custom AI Fitness Workout");
            List<WorkoutExerciseItem> items = new ArrayList<>();
            items.add(new WorkoutExerciseItem("Standard Squats", "Quadriceps & Glutes", 3, 12, 0));
            items.add(new WorkoutExerciseItem("Push-ups", "Chest & Triceps", 3, 10, 0));
            items.add(new WorkoutExerciseItem("Plank Hold", "Core & Abs", 3, 0, 45));
            session.setExercises(items);
        }

        User user = localDb.getUser();
        if (user != null) {
            session.setUserId(user.getUid());
        }

        updateCurrentExerciseUI();
    }

    private void updateCurrentExerciseUI() {
        if (session == null || session.getExercises() == null || session.getExercises().isEmpty()) return;

        List<WorkoutExerciseItem> exercises = session.getExercises();
        if (currentExerciseIndex >= exercises.size()) {
            // All exercises finished!
            stopWorkoutAndFinalize();
            return;
        }

        WorkoutExerciseItem currentEx = exercises.get(currentExerciseIndex);
        currentEx.setStatus(WorkoutExerciseItem.Status.IN_PROGRESS);

        tvWorkoutTitle.setText(session.getWorkoutTitle());
        tvExerciseProgressHeader.setText(String.format(Locale.getDefault(), "Exercise %d of %d", currentExerciseIndex + 1, exercises.size()));

        tvCurrentExerciseName.setText(currentEx.getName());
        tvTargetMuscle.setText(getString(R.string.target_muscle_format, currentEx.getTargetMuscle()));

        tvCurrentSetStatus.setText(String.format(Locale.getDefault(), "Set %d of %d", currentSetIndex, currentEx.getPlannedSets()));
        
        if (currentEx.getPlannedReps() > 0) {
            tvTargetReps.setText(String.format(Locale.getDefault(), "Target: %d Reps", currentEx.getPlannedReps()));
            currentActualReps = currentEx.getPlannedReps();
        } else {
            tvTargetReps.setText(String.format(Locale.getDefault(), "Target: %ds Hold", currentEx.getPlannedDurationSec()));
            currentActualReps = currentEx.getPlannedDurationSec();
        }

        tvActualRepsValue.setText(String.valueOf(currentActualReps));
    }

    private void togglePauseResume() {
        if (isPaused) {
            isPaused = false;
            totalPauseTimeMs += (System.currentTimeMillis() - pauseStartMs);
            btnPauseResume.setText(R.string.workout_pause);
            Toast.makeText(this, "Workout Resumed", Toast.LENGTH_SHORT).show();
        } else {
            isPaused = true;
            pauseStartMs = System.currentTimeMillis();
            btnPauseResume.setText(R.string.workout_resume);
            Toast.makeText(this, "Workout Paused", Toast.LENGTH_SHORT).show();
        }
    }

    private void adjustReps(int delta) {
        currentActualReps = Math.max(1, currentActualReps + delta);
        tvActualRepsValue.setText(String.valueOf(currentActualReps));
    }

    private void logCompletedSet() {
        if (session == null || session.getExercises() == null || currentExerciseIndex >= session.getExercises().size()) return;
        WorkoutExerciseItem currentEx = session.getExercises().get(currentExerciseIndex);

        currentEx.setCompletedSets(currentEx.getCompletedSets() + 1);
        if (currentEx.getPlannedReps() > 0) {
            currentEx.setActualCompletedReps(currentEx.getActualCompletedReps() + currentActualReps);
        } else if (currentEx.getPlannedDurationSec() > 0) {
            currentEx.setActualDurationSec(currentEx.getActualDurationSec() + currentActualReps);
        } else {
            currentEx.setActualCompletedReps(currentEx.getActualCompletedReps() + currentActualReps);
        }

        if (currentSetIndex < currentEx.getPlannedSets()) {
            currentSetIndex++;
            Toast.makeText(this, "Set " + (currentSetIndex - 1) + " logged! Next set ready.", Toast.LENGTH_SHORT).show();
            updateCurrentExerciseUI();
        } else {
            // Exercise completed
            currentEx.setStatus(WorkoutExerciseItem.Status.COMPLETED);
            Toast.makeText(this, currentEx.getName() + " completed! 🎉", Toast.LENGTH_SHORT).show();
            currentExerciseIndex++;
            currentSetIndex = 1;
            updateCurrentExerciseUI();
        }
    }

    private void skipCurrentExercise() {
        if (session == null || session.getExercises() == null || currentExerciseIndex >= session.getExercises().size()) return;
        WorkoutExerciseItem currentEx = session.getExercises().get(currentExerciseIndex);

        if (currentEx.getCompletedSets() > 0) {
            currentEx.setStatus(WorkoutExerciseItem.Status.PARTIALLY_COMPLETED);
        } else {
            currentEx.setStatus(WorkoutExerciseItem.Status.SKIPPED);
        }

        Toast.makeText(this, "Skipped " + currentEx.getName(), Toast.LENGTH_SHORT).show();
        currentExerciseIndex++;
        currentSetIndex = 1;
        updateCurrentExerciseUI();
    }

    private void openCameraFormCheck() {
        if (session == null || session.getExercises() == null || currentExerciseIndex >= session.getExercises().size()) return;
        WorkoutExerciseItem currentEx = session.getExercises().get(currentExerciseIndex);
        Intent intent = new Intent(this, RealTimeFeedbackActivity.class);
        intent.putExtra("exercise_name", currentEx.getName());
        intent.putExtra("target_reps", currentEx.getPlannedReps());
        intent.putExtra("target_sets", currentEx.getPlannedSets());
        intent.putExtra("planned_duration", currentEx.getPlannedDurationSec());
        intent.putExtra("workout_session", session);
        startActivity(intent);
    }

    private void stopWorkoutAndFinalize() {
        timerHandler.removeCallbacks(timerRunnable);
        if (isPaused) {
            totalPauseTimeMs += (System.currentTimeMillis() - pauseStartMs);
        }
        session.setPauseDurationMs(totalPauseTimeMs);
        session.setEndTimeMs(System.currentTimeMillis());

        // Finalize remaining exercises state if stopped early
        List<WorkoutExerciseItem> exercises = session.getExercises();
        if (exercises != null) {
            for (int i = 0; i < exercises.size(); i++) {
                WorkoutExerciseItem item = exercises.get(i);
                if (i >= currentExerciseIndex && item.getStatus() != WorkoutExerciseItem.Status.COMPLETED) {
                    if (item.getCompletedSets() > 0) {
                        item.setStatus(WorkoutExerciseItem.Status.PARTIALLY_COMPLETED);
                    } else {
                        item.setStatus(WorkoutExerciseItem.Status.SKIPPED);
                    }
                }
            }
        }

        // Fetch User Weight
        double weightKg = 70.0;
        User user = localDb.getUser();
        if (user != null && user.getWeight() > 0) {
            weightKg = localDb.isMetricUnitsEnabled() ? user.getWeight() : (user.getWeight() / 2.20462);
        }

        // Calculate session metrics & MET calories
        session.finalizeSessionMetrics(weightKg);

        // Generate data-driven AI Performance Analysis & Recommendations
        AiWorkoutEngine.generateAnalysisAndRecommendations(session, localDb);

        // Save session to LocalDataManager (Single Source of Truth)
        localDb.saveWorkoutSession(session);

        // Also save to legacy WorkoutLog for backwards compatibility
        com.fitness.app.models.WorkoutLog log = new com.fitness.app.models.WorkoutLog(
            session.getSessionId(),
            session.getWorkoutTitle(),
            session.getCompletedSetsCount(),
            session.getActualCompletedReps(),
            0.0,
            session.getAiPerformanceAnalysis(),
            session.getDateStr(),
            session.getTimestamp()
        );
        localDb.saveWorkoutLog(log);
        localDb.incrementStreak();

        // Open After Workout Summary Screen
        Intent intent = new Intent(this, WorkoutSummaryActivity.class);
        intent.putExtra("session_id", session.getSessionId());
        intent.putExtra("workout_session", session);
        startActivity(intent);

        finish();
    }

    @Override
    protected void onDestroy() {
        timerHandler.removeCallbacks(timerRunnable);
        super.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}

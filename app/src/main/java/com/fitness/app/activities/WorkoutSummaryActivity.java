package com.fitness.app.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fitness.app.R;
import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.models.ProgressLog;
import com.fitness.app.models.User;
import com.fitness.app.models.WorkoutExerciseItem;
import com.fitness.app.models.WorkoutSession;
import com.fitness.app.repositories.UserRepository;
import com.fitness.app.utils.AiWorkoutEngine;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WorkoutSummaryActivity extends AppCompatActivity {

    private LocalDataManager localDb;
    private WorkoutSession session;

    private TextView tvExerciseName, tvFormScore, tvFormFeedback;
    private ProgressBar pbAccuracy, pbDepth, pbBalance, pbStability, pbPosture;
    private TextView tvAccuracy, tvDepth, tvBalance, tvStability, tvPosture;

    private TextView tvTotalReps, tvTotalSets, tvDuration, tvCalories, tvCompletionRate, tvHeartRate, tvStepsDistance;
    private RecyclerView rvExerciseBreakdown;
    private ChipGroup cgRpe;
    private TextView tvAiAnalysisBody, tvAiRecommendationBody;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_workout_summary);

        localDb = new LocalDataManager(this);

        // Bind Views
        tvExerciseName = findViewById(R.id.tvExerciseName);
        tvFormScore = findViewById(R.id.tvFormScore);
        tvFormFeedback = findViewById(R.id.tvFormFeedback);
        
        pbAccuracy = findViewById(R.id.pbAccuracy);
        pbDepth = findViewById(R.id.pbDepth);
        pbBalance = findViewById(R.id.pbBalance);
        pbStability = findViewById(R.id.pbStability);
        pbPosture = findViewById(R.id.pbPosture);

        tvAccuracy = findViewById(R.id.tvAccuracy);
        tvDepth = findViewById(R.id.tvDepth);
        tvBalance = findViewById(R.id.tvBalance);
        tvStability = findViewById(R.id.tvStability);
        tvPosture = findViewById(R.id.tvPosture);

        tvTotalReps = findViewById(R.id.tvTotalReps);
        tvTotalSets = findViewById(R.id.tvTotalSets);
        tvDuration = findViewById(R.id.tvDuration);
        tvCalories = findViewById(R.id.tvCalories);
        tvCompletionRate = findViewById(R.id.tvCompletionRate);
        tvHeartRate = findViewById(R.id.tvHeartRate);
        tvStepsDistance = findViewById(R.id.tvStepsDistance);

        rvExerciseBreakdown = findViewById(R.id.rvExerciseBreakdown);
        cgRpe = findViewById(R.id.cgRpe);
        tvAiAnalysisBody = findViewById(R.id.tvAiAnalysisBody);
        tvAiRecommendationBody = findViewById(R.id.tvAiRecommendationBody);

        // Retrieve Single Source of Truth WorkoutSession
        loadWorkoutSession();

        // Bind Data & UI
        displaySessionReport();

        // Setup RPE chip selection listener
        cgRpe.setOnCheckedChangeListener((group, checkedId) -> {
            if (session == null) return;
            if (checkedId == R.id.chipEasy) {
                session.setUserRpe("Easy");
            } else if (checkedId == R.id.chipModerate) {
                session.setUserRpe("Moderate");
            } else if (checkedId == R.id.chipDifficult) {
                session.setUserRpe("Difficult");
            } else if (checkedId == R.id.chipVeryDifficult) {
                session.setUserRpe("Very Difficult");
            }
            // Re-evaluate AI Analysis & Recommendations with user RPE feedback
            AiWorkoutEngine.generateAnalysisAndRecommendations(session, localDb);
            localDb.saveWorkoutSession(session);
            tvAiAnalysisBody.setText(session.getAiPerformanceAnalysis());
            tvAiRecommendationBody.setText(session.getAiRecommendations());
        });

        // Navigation actions
        findViewById(R.id.btnDone).setOnClickListener(v -> {
            Intent intent = new Intent(WorkoutSummaryActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.btnViewHistory).setOnClickListener(v -> {
            Intent intent = new Intent(WorkoutSummaryActivity.this, SquatHistoryActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void loadWorkoutSession() {
        // 1. Try to get serialized WorkoutSession from Intent
        session = (WorkoutSession) getIntent().getSerializableExtra("workout_session");

        // 2. Try to load from session_id
        if (session == null) {
            String sessionId = getIntent().getStringExtra("session_id");
            if (sessionId != null) {
                session = localDb.getWorkoutSession(sessionId);
            }
        }

        // 3. Fallback: Create session from individual Intent Extras
        if (session == null) {
            String exercise = getIntent().getStringExtra("exercise_name");
            if (exercise == null || exercise.trim().isEmpty()) exercise = "Standard Squats";

            session = new WorkoutSession(exercise, "General", "Intermediate");

            int reps = getIntent().getIntExtra("total_reps", 12);
            int completedSets = getIntent().getIntExtra("completed_sets", 3);
            long durationMs = getIntent().getLongExtra("duration_ms", 60000L);
            String hrStr = getIntent().getStringExtra("heart_rate_str");
            String stepsStr = getIntent().getStringExtra("steps_distance_str");

            session.setActiveDurationMs(durationMs);
            if (hrStr != null) session.setHeartRateStatus(hrStr);
            if (stepsStr != null) session.setStepsStatus(stepsStr);

            List<WorkoutExerciseItem> items = new ArrayList<>();
            WorkoutExerciseItem item = new WorkoutExerciseItem(exercise, "Full Body", completedSets, reps, 0);
            item.setCompletedSets(completedSets);
            item.setActualCompletedReps(reps);
            item.setStatus(WorkoutExerciseItem.Status.COMPLETED);
            items.add(item);

            session.setExercises(items);

            double weightKg = 70.0;
            User user = localDb.getUser();
            if (user != null && user.getWeight() > 0) {
                weightKg = localDb.isMetricUnitsEnabled() ? user.getWeight() : (user.getWeight() / 2.20462);
            }
            session.finalizeSessionMetrics(weightKg);
            AiWorkoutEngine.generateAnalysisAndRecommendations(session, localDb);
            localDb.saveWorkoutSession(session);
        }
    }

    private void displaySessionReport() {
        if (session == null) return;

        // Header Title
        tvExerciseName.setText(session.getWorkoutTitle() + " Session");

        // Posture / Form metrics
        int score = getIntent().getIntExtra("average_form_score", 90);
        int acc = getIntent().getIntExtra("accuracy", 85);
        int dep = getIntent().getIntExtra("depth", 90);
        int bal = getIntent().getIntExtra("balance", 95);
        int stab = getIntent().getIntExtra("stability", 94);
        int post = getIntent().getIntExtra("posture", 80);

        tvFormScore.setText(String.format(Locale.getDefault(), "%d%%", score));

        if (score >= 80) {
            tvFormScore.setTextColor(0xFF2563EB); // Success Blue
            tvFormFeedback.setText("Excellent posture control! You maintained balanced alignment throughout the session.");
        } else if (score >= 60) {
            tvFormScore.setTextColor(0xFFF59E0B); // Warning Yellow
            tvFormFeedback.setText("Good session. Watch for alignment shift alerts. Focus on controlled eccentric phase.");
        } else {
            tvFormScore.setTextColor(0xFFEF4444); // Error Red
            tvFormFeedback.setText("Form needs attention. Keep chest high, back straight, and lower deeper for complete movement.");
        }

        pbAccuracy.setProgress(acc);
        pbDepth.setProgress(dep);
        pbBalance.setProgress(bal);
        pbStability.setProgress(stab);
        pbPosture.setProgress(post);

        tvAccuracy.setText(String.format(Locale.getDefault(), "%d%%", acc));
        tvDepth.setText(String.format(Locale.getDefault(), "%d%%", dep));
        tvBalance.setText(String.format(Locale.getDefault(), "%d%%", bal));
        tvStability.setText(String.format(Locale.getDefault(), "%d%%", stab));
        tvPosture.setText(String.format(Locale.getDefault(), "%d%%", post));

        // Display Session Summary Metrics
        if (session.getTotalPlannedReps() > 0) {
            tvTotalReps.setText(String.format(Locale.getDefault(), "%d / %d Reps", session.getActualCompletedReps(), session.getTotalPlannedReps()));
        } else {
            tvTotalReps.setText(String.format(Locale.getDefault(), "%d Reps", session.getActualCompletedReps()));
        }

        if (session.getTotalPlannedSets() > 0) {
            tvTotalSets.setText(String.format(Locale.getDefault(), "%d / %d Sets", session.getCompletedSetsCount(), session.getTotalPlannedSets()));
        } else {
            tvTotalSets.setText(String.format(Locale.getDefault(), "%d Sets", session.getCompletedSetsCount()));
        }

        long secs = session.getActiveDurationMs() / 1000;
        long m = secs / 60;
        long s = secs % 60;
        if (m > 0) {
            tvDuration.setText(String.format(Locale.getDefault(), "%dm %ds", m, s));
        } else {
            tvDuration.setText(String.format(Locale.getDefault(), "%ds", s));
        }

        tvCalories.setText(String.format(Locale.getDefault(), "%d kcal", session.getEstimatedCalories()));
        tvCompletionRate.setText(String.format(Locale.getDefault(), "%.1f%% (%d/%d)", session.getCompletionPercentage(), session.getCompletedExercisesCount(), session.getTotalPlannedExercises()));

        tvHeartRate.setText(session.getHeartRateStatus());
        tvStepsDistance.setText(session.getStepsStatus());

        // Setup Exercise-by-Exercise Breakdown RecyclerView
        if (session.getExercises() != null && !session.getExercises().isEmpty()) {
            rvExerciseBreakdown.setLayoutManager(new LinearLayoutManager(this));
            rvExerciseBreakdown.setAdapter(new ExerciseBreakdownAdapter(session.getExercises()));
        }

        // Display AI Analysis & Recommendations
        if (session.getAiPerformanceAnalysis() != null && !session.getAiPerformanceAnalysis().isEmpty()) {
            tvAiAnalysisBody.setText(session.getAiPerformanceAnalysis());
        } else {
            AiWorkoutEngine.generateAnalysisAndRecommendations(session, localDb);
            tvAiAnalysisBody.setText(session.getAiPerformanceAnalysis());
        }

        if (session.getAiRecommendations() != null && !session.getAiRecommendations().isEmpty()) {
            tvAiRecommendationBody.setText(session.getAiRecommendations());
        } else {
            tvAiRecommendationBody.setText(session.getAiRecommendations());
        }

        // Update Daily ProgressLog & Firestore Sync
        try {
            String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            ProgressLog progressLog = localDb.getProgressLog(todayDate);
            if (progressLog == null) {
                progressLog = new ProgressLog(todayDate);
            }
            progressLog.setCaloriesBurned(progressLog.getCaloriesBurned() + session.getEstimatedCalories());
            localDb.saveProgressLog(progressLog);

            new Thread(() -> {
                try {
                    new UserRepository().syncLocalDataToFirestore(localDb);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class ExerciseBreakdownAdapter extends RecyclerView.Adapter<ExerciseBreakdownAdapter.ViewHolder> {

        private final List<WorkoutExerciseItem> list;

        ExerciseBreakdownAdapter(List<WorkoutExerciseItem> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workout_report_exercise, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            WorkoutExerciseItem item = list.get(position);
            holder.tvExerciseName.setText(item.getName());

            int totalPlannedExReps = item.getTotalPlannedRepsForExercise();
            String invalidStr = item.getInvalidReps() > 0 ? String.format(Locale.getDefault(), " (%d Form Corrections)", item.getInvalidReps()) : "";
            if (totalPlannedExReps > 0) {
                holder.tvExerciseStats.setText(String.format(Locale.getDefault(),
                    "%d / %d Sets • %d / %d Reps%s", item.getCompletedSets(), item.getPlannedSets(), item.getActualCompletedReps(), totalPlannedExReps, invalidStr));
            } else if (item.getPlannedDurationSec() > 0) {
                int totalPlannedDur = (item.getPlannedSets() > 0 ? item.getPlannedSets() : 1) * item.getPlannedDurationSec();
                holder.tvExerciseStats.setText(String.format(Locale.getDefault(),
                    "%d / %d Sets • %ds / %ds Hold%s", item.getCompletedSets(), item.getPlannedSets(), item.getActualDurationSec(), totalPlannedDur, invalidStr));
            } else {
                holder.tvExerciseStats.setText(String.format(Locale.getDefault(),
                    "%d / %d Sets%s", item.getCompletedSets(), item.getPlannedSets(), invalidStr));
            }

            if (item.getStatus() == WorkoutExerciseItem.Status.COMPLETED) {
                holder.tvStatusBadge.setText("Completed");
                holder.tvStatusBadge.setTextColor(Color.parseColor("#10B981"));
                holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#064E3B"));
            } else if (item.getStatus() == WorkoutExerciseItem.Status.PARTIALLY_COMPLETED) {
                holder.tvStatusBadge.setText("Partial");
                holder.tvStatusBadge.setTextColor(Color.parseColor("#F59E0B"));
                holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#78350F"));
            } else {
                holder.tvStatusBadge.setText("Skipped");
                holder.tvStatusBadge.setTextColor(Color.parseColor("#EF4444"));
                holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#7F1D1D"));
            }
        }

        @Override
        public int getItemCount() {
            return list != null ? list.size() : 0;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvExerciseName, tvExerciseStats, tvStatusBadge;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvExerciseName = itemView.findViewById(R.id.tvExerciseName);
                tvExerciseStats = itemView.findViewById(R.id.tvExerciseStats);
                tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}

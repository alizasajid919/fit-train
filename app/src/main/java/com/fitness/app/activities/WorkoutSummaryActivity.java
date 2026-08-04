package com.fitness.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.fitness.app.R;

import java.util.Locale;

public class WorkoutSummaryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_workout_summary);

        // Bind Views
        TextView tvExerciseName = findViewById(R.id.tvExerciseName);
        TextView tvFormScore = findViewById(R.id.tvFormScore);
        TextView tvFormFeedback = findViewById(R.id.tvFormFeedback);
        
        ProgressBar pbAccuracy = findViewById(R.id.pbAccuracy);
        ProgressBar pbDepth = findViewById(R.id.pbDepth);
        ProgressBar pbBalance = findViewById(R.id.pbBalance);
        ProgressBar pbStability = findViewById(R.id.pbStability);
        ProgressBar pbPosture = findViewById(R.id.pbPosture);

        TextView tvAccuracy = findViewById(R.id.tvAccuracy);
        TextView tvDepth = findViewById(R.id.tvDepth);
        TextView tvBalance = findViewById(R.id.tvBalance);
        TextView tvStability = findViewById(R.id.tvStability);
        TextView tvPosture = findViewById(R.id.tvPosture);

        TextView tvTotalReps = findViewById(R.id.tvTotalReps);
        TextView tvTotalSets = findViewById(R.id.tvTotalSets);
        TextView tvDuration = findViewById(R.id.tvDuration);
        TextView tvCalories = findViewById(R.id.tvCalories);

        // Extract Bundle extras
        String exercise = getIntent().getStringExtra("exercise_name");
        int reps = getIntent().getIntExtra("total_reps", 0);
        int sets = getIntent().getIntExtra("completed_sets", 0);
        long durationMs = getIntent().getLongExtra("duration_ms", 0);
        int score = getIntent().getIntExtra("average_form_score", 90);

        int acc = getIntent().getIntExtra("accuracy", 85);
        int dep = getIntent().getIntExtra("depth", 90);
        int bal = getIntent().getIntExtra("balance", 95);
        int stab = getIntent().getIntExtra("stability", 94);
        int post = getIntent().getIntExtra("posture", 80);

        // Set static properties
        if (exercise != null) {
            tvExerciseName.setText(exercise + " Session");
        }
        tvFormScore.setText(String.format(Locale.getDefault(), "%d%%", score));

        if (score >= 80) {
            tvFormScore.setTextColor(0xFF22C55E); // Success Green
            tvFormFeedback.setText("Excellent posture control! You maintained balanced alignment throughout the entire session.");
        } else if (score >= 60) {
            tvFormScore.setTextColor(0xFFF59E0B); // Warning Yellow
            tvFormFeedback.setText("Good session. Watch out for knee forward overhang alerts. Try driving your hips backward.");
        } else {
            tvFormScore.setTextColor(0xFFEF4444); // Error Red
            tvFormFeedback.setText("Lower form score. Keep chest high, back straight, and lower deeper for complete squats.");
        }

        // Set Progress Bars
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

        // Format stats
        tvTotalReps.setText(String.format(Locale.getDefault(), "%d Reps", reps));
        tvTotalSets.setText(String.format(Locale.getDefault(), "%d Sets", sets));

        long totalSecs = durationMs / 1000;
        long mins = totalSecs / 60;
        long secs = totalSecs % 60;
        if (mins > 0) {
            tvDuration.setText(String.format(Locale.getDefault(), "%dm %ds", mins, secs));
        } else {
            tvDuration.setText(String.format(Locale.getDefault(), "%ds", secs));
        }

        // Est calories: 1.1 kcal per rep
        double cals = reps * 1.1;
        tvCalories.setText(String.format(Locale.getDefault(), "%.1f kcal", cals));

        // Button Click navigation
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

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}

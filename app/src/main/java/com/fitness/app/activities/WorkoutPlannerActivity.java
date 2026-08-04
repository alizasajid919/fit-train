package com.fitness.app.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fitness.app.R;
import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.models.User;
import com.fitness.app.models.WorkoutPlan;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WorkoutPlannerActivity extends AppCompatActivity {

    private TextView tvWorkoutGoal, tvWorkoutDifficulty;
    private RecyclerView rvWorkouts;
    private LocalDataManager localDb;
    private ExerciseAdapter adapter;
    private WorkoutPlan currentPlan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_planner);

        localDb = new LocalDataManager(this);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        tvWorkoutGoal = findViewById(R.id.tvWorkoutGoal);
        tvWorkoutDifficulty = findViewById(R.id.tvWorkoutDifficulty);
        rvWorkouts = findViewById(R.id.rvWorkouts);

        loadWorkoutPlan();

        findViewById(R.id.btnRegenerate).setOnClickListener(v -> {
            generateNewWorkoutPlan();
            Toast.makeText(this, "New AI Workout Plan Generated!", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadWorkoutPlan() {
        currentPlan = localDb.getWorkoutPlan();
        if (currentPlan == null) {
            generateNewWorkoutPlan();
        } else {
            bindWorkoutPlan(currentPlan);
        }
    }

    private void generateNewWorkoutPlan() {
        User user = localDb.getUser();
        String goal = user != null ? user.getGoal() : "Improve Fitness";
        String level = user != null ? user.getActivityLevel() : "Active";

        List<WorkoutPlan.Exercise> list = new ArrayList<>();
        if ("Lose Fat".equalsIgnoreCase(goal)) {
            list.add(new WorkoutPlan.Exercise("Jumping Jacks", "Great full-body cardio to burn calories.", 3, 0, 30));
            list.add(new WorkoutPlan.Exercise("Bodyweight Squats", "Strengthens thighs, hips, and glutes.", 4, 15, 0));
            list.add(new WorkoutPlan.Exercise("Mountain Climbers", "Engages core and builds cardio endurance.", 3, 20, 0));
            list.add(new WorkoutPlan.Exercise("Push-ups", "Builds chest, shoulder, and core strength.", 3, 12, 0));
            list.add(new WorkoutPlan.Exercise("Plank Hold", "Strengthens entire abdominal core area.", 3, 0, 45));
        } else if ("Gain Muscle".equalsIgnoreCase(goal)) {
            list.add(new WorkoutPlan.Exercise("Push-ups (Weighted)", "Build chest and triceps muscles.", 4, 12, 0));
            list.add(new WorkoutPlan.Exercise("Bodyweight Squats", "Builds quad and hamstring mass.", 4, 20, 0));
            list.add(new WorkoutPlan.Exercise("Bench Dips", "Focuses on building triceps and shoulder strength.", 3, 12, 0));
            list.add(new WorkoutPlan.Exercise("Lunges", "Unilateral leg exercise to build quad mass.", 3, 12, 0));
            list.add(new WorkoutPlan.Exercise("Plank", "Core stability under load.", 3, 0, 60));
        } else {
            list.add(new WorkoutPlan.Exercise("Burpees", "Explosive conditioning movement.", 3, 10, 0));
            list.add(new WorkoutPlan.Exercise("Squats", "Standard lower body builder.", 3, 15, 0));
            list.add(new WorkoutPlan.Exercise("Push-ups", "Chest and arm strength builder.", 3, 12, 0));
            list.add(new WorkoutPlan.Exercise("Glute Bridges", "Excellent posterior chain activation.", 3, 15, 0));
            list.add(new WorkoutPlan.Exercise("Plank", "General core stability.", 3, 0, 60));
        }

        currentPlan = new WorkoutPlan(UUID.randomUUID().toString(), goal, level, list, System.currentTimeMillis());
        localDb.saveWorkoutPlan(currentPlan);
        bindWorkoutPlan(currentPlan);

        // Upload in background thread to Firestore if logged in
        new Thread(() -> {
            try {
                com.fitness.app.repositories.UserRepository repo = new com.fitness.app.repositories.UserRepository();
                repo.syncLocalDataToFirestore(localDb);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void bindWorkoutPlan(WorkoutPlan plan) {
        tvWorkoutGoal.setText(String.format("Goal: %s", plan.getGoal()));
        tvWorkoutDifficulty.setText(String.format("Level: %s", plan.getFitnessLevel()));

        adapter = new ExerciseAdapter(plan.getExercises());
        rvWorkouts.setLayoutManager(new LinearLayoutManager(this));
        rvWorkouts.setAdapter(adapter);
    }

    // RecyclerView Adapter
    private static class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {
        private final List<WorkoutPlan.Exercise> exercises;

        public ExerciseAdapter(List<WorkoutPlan.Exercise> exercises) {
            this.exercises = exercises;
        }

        @NonNull
        @Override
        public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exercise, parent, false);
            return new ExerciseViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
            WorkoutPlan.Exercise ex = exercises.get(position);
            holder.tvExerciseName.setText(ex.getName());
            holder.tvExerciseDesc.setText(ex.getDescription());

            if (ex.getDurationSeconds() > 0) {
                holder.tvExerciseStats.setText(String.format(java.util.Locale.getDefault(), "%d Sets x %d seconds", ex.getSets(), ex.getDurationSeconds()));
            } else {
                holder.tvExerciseStats.setText(String.format(java.util.Locale.getDefault(), "%d Sets x %d Reps", ex.getSets(), ex.getReps()));
            }
        }

        @Override
        public int getItemCount() {
            return exercises.size();
        }

        static class ExerciseViewHolder extends RecyclerView.ViewHolder {
            TextView tvExerciseName, tvExerciseStats, tvExerciseDesc;

            public ExerciseViewHolder(@NonNull View itemView) {
                super(itemView);
                tvExerciseName = itemView.findViewById(R.id.tvExerciseName);
                tvExerciseStats = itemView.findViewById(R.id.tvExerciseStats);
                tvExerciseDesc = itemView.findViewById(R.id.tvExerciseDesc);
            }
        }
    }
}

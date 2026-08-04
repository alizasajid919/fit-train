package com.fitness.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.fitness.app.R;

import java.util.ArrayList;
import java.util.List;

public class CategoryWorkoutsActivity extends AppCompatActivity {

    private ImageView ivCategoryHeaderImage;
    private TextView tvCategoryHeaderTitle, tvCategoryHeaderDesc;
    private RecyclerView rvCategoryWorkouts;
    private WorkoutAdapter adapter;
    private String categoryName;

    public static class WorkoutItem {
        public String title;
        public String description;
        public int imageResId;
        public String duration;
        public String difficulty;
        public int calories;

        public WorkoutItem(String title, String description, int imageResId, String duration, String difficulty, int calories) {
            this.title = title;
            this.description = description;
            this.imageResId = imageResId;
            this.duration = duration;
            this.difficulty = difficulty;
            this.calories = calories;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_workouts);

        // Retrieve extra
        categoryName = getIntent().getStringExtra("category_name");
        if (categoryName == null) categoryName = "Strength";

        // Bind Views
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
        getSupportActionBar().setTitle(categoryName);

        ivCategoryHeaderImage = findViewById(R.id.ivCategoryHeaderImage);
        tvCategoryHeaderTitle = findViewById(R.id.tvCategoryHeaderTitle);
        tvCategoryHeaderDesc = findViewById(R.id.tvCategoryHeaderDesc);
        rvCategoryWorkouts = findViewById(R.id.rvCategoryWorkouts);

        setupHeaderAndData();
    }

    private void setupHeaderAndData() {
        List<WorkoutItem> workouts = new ArrayList<>();

        if ("Cardio".equalsIgnoreCase(categoryName)) {
            tvCategoryHeaderTitle.setText("Cardio Conditioning");
            tvCategoryHeaderDesc.setText("Burn fat, improve endurance, and boost heart health.");
            ivCategoryHeaderImage.setImageResource(R.drawable.onboarding_2);

            workouts.add(new WorkoutItem(
                    "HIIT Cardio Blast",
                    "High-intensity interval training loops to maximize calorie burn and aerobic power.",
                    R.drawable.onboarding_2,
                    "20 min",
                    "Intermediate",
                    250
            ));
            workouts.add(new WorkoutItem(
                    "Brisk Walk & Run",
                    "Alternate periods of brisk walking and steady jogging to build basic stamina.",
                    R.drawable.onboarding_1,
                    "30 min",
                    "Beginner",
                    180
            ));
            workouts.add(new WorkoutItem(
                    "Jumping Rope Interval",
                    "Explosive jumping rope patterns designed to build calf strength and burning power.",
                    R.drawable.onboarding_2,
                    "15 min",
                    "Advanced",
                    200
            ));
        } else if ("Mindfulness".equalsIgnoreCase(categoryName)) {
            tvCategoryHeaderTitle.setText("Mindfulness & Yoga");
            tvCategoryHeaderDesc.setText("Find your focus, relieve anxiety, and stretch your body.");
            ivCategoryHeaderImage.setImageResource(R.drawable.onboarding_4);

            workouts.add(new WorkoutItem(
                    "Restorative Yoga Flow",
                    "Gentle yoga stretches to improve alignment, release tightness, and calm the system.",
                    R.drawable.onboarding_4,
                    "25 min",
                    "Beginner",
                    90
            ));
            workouts.add(new WorkoutItem(
                    "Deep Breathing Session",
                    "Visual breathing synchronization exercises to clear the mind and lower stress levels.",
                    R.drawable.onboarding_4,
                    "5 min",
                    "Beginner",
                    10
            ));
            workouts.add(new WorkoutItem(
                    "Body Scan Meditation",
                    "A mindful check-in to relax muscles and restore energy after a busy day.",
                    R.drawable.onboarding_4,
                    "15 min",
                    "Beginner",
                    15
            ));
        } else {
            // Strength (default)
            tvCategoryHeaderTitle.setText("Strength Training");
            tvCategoryHeaderDesc.setText("Build lean muscle mass and increase bone density.");
            ivCategoryHeaderImage.setImageResource(R.drawable.onboarding_1);

            workouts.add(new WorkoutItem(
                    "Dumbbell Upper Body",
                    "Target chest, back, shoulders, and arms with classic dumbbell movements.",
                    R.drawable.onboarding_1,
                    "30 min",
                    "Intermediate",
                    220
            ));
            workouts.add(new WorkoutItem(
                    "Barbell Lower Body",
                    "Heavy squats, deadlifts, and loaded lunges to develop power and leg strength.",
                    R.drawable.onboarding_2,
                    "45 min",
                    "Advanced",
                    350
            ));
            workouts.add(new WorkoutItem(
                    "Core Crusher",
                    "Sculpt your abs and strengthen obliques with this high-efficiency core routine.",
                    R.drawable.onboarding_1,
                    "15 min",
                    "Beginner",
                    100
            ));
        }

        adapter = new WorkoutAdapter(workouts);
        rvCategoryWorkouts.setAdapter(adapter);
    }

    private class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.ViewHolder> {
        private final List<WorkoutItem> list;

        public WorkoutAdapter(List<WorkoutItem> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_workout, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            WorkoutItem item = list.get(position);
            holder.tvTitle.setText(item.title);
            holder.tvDesc.setText(item.description);
            holder.ivImage.setImageResource(item.imageResId);
            holder.tvDuration.setText(item.duration);
            holder.tvDifficulty.setText(item.difficulty);
            holder.tvCalories.setText(item.calories + " kcal");

            holder.btnStart.setOnClickListener(v -> {
                Intent intent = new Intent(CategoryWorkoutsActivity.this, LogWorkoutActivity.class);
                intent.putExtra("exercise_name", item.title);
                intent.putExtra("duration_min", item.duration.replace(" min", ""));
                intent.putExtra("calories_kcal", String.valueOf(item.calories));
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivImage;
            TextView tvTitle, tvDesc, tvDuration, tvDifficulty, tvCalories;
            Button btnStart;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivImage = itemView.findViewById(R.id.ivWorkoutImage);
                tvTitle = itemView.findViewById(R.id.tvWorkoutTitle);
                tvDesc = itemView.findViewById(R.id.tvWorkoutDesc);
                tvDuration = itemView.findViewById(R.id.tvWorkoutDuration);
                tvDifficulty = itemView.findViewById(R.id.tvWorkoutDifficulty);
                tvCalories = itemView.findViewById(R.id.tvWorkoutCalories);
                btnStart = itemView.findViewById(R.id.btnStartWorkout);
            }
        }
    }
}

package com.fitness.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fitness.app.R;
import com.fitness.app.data.room.FitnessViewModel;
import com.fitness.app.models.ExerciseGuide;
import com.fitness.app.models.User;

import java.util.ArrayList;
import java.util.List;

public class LearningTutorialActivity extends AppCompatActivity {

    private RecyclerView rvTutorials;
    private ProgressBar pbLoading;
    private TextView tvEmptyState;
    private View layoutError;
    private TextView tvErrorMsg;

    private List<ExerciseGuide> tutorialList = new ArrayList<>();
    private TutorialAdapter adapter;
    private FitnessViewModel fitnessViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_learning_tutorial);

        rvTutorials = findViewById(R.id.rvTutorials);
        pbLoading = findViewById(R.id.pbLoading);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        layoutError = findViewById(R.id.layoutError);
        tvErrorMsg = findViewById(R.id.tvErrorMsg);

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());
        findViewById(R.id.btnRetry).setOnClickListener(v -> loadTutorialsFromDb());

        fitnessViewModel = new ViewModelProvider(this).get(FitnessViewModel.class);

        rvTutorials.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TutorialAdapter();
        rvTutorials.setAdapter(adapter);

        loadTutorialsFromDb();
    }

    private void loadTutorialsFromDb() {
        showLoading();
        fitnessViewModel.getAllExerciseGuidesLive().observe(this, guides -> {
            if (guides == null || guides.isEmpty()) {
                seedDefaultGuides();
            } else {
                tutorialList.clear();
                
                User user = new com.fitness.app.data.local.LocalDataManager(this).getUser();
                if (user != null) {
                    String userLevel = user.getFitnessExperience();
                    String userGoal = user.getGoal();
                    
                    List<com.fitness.app.models.ExerciseGuide> matchingGuides = new ArrayList<>();
                    List<com.fitness.app.models.ExerciseGuide> otherGuides = new ArrayList<>();
                    
                    for (com.fitness.app.models.ExerciseGuide guide : guides) {
                        boolean levelMatch = (userLevel != null && userLevel.equalsIgnoreCase(guide.getDifficulty()));
                        boolean goalMatch = false;
                        if (userGoal != null) {
                            if ((userGoal.contains("Muscle") || userGoal.contains("Gain")) && "STRENGTH".equalsIgnoreCase(guide.getCategory())) {
                                goalMatch = true;
                            } else if ((userGoal.contains("Fat") || userGoal.contains("Lose")) && "CARDIO".equalsIgnoreCase(guide.getCategory())) {
                                goalMatch = true;
                            }
                        }
                        
                        if (levelMatch || goalMatch) {
                            matchingGuides.add(guide);
                        } else {
                            otherGuides.add(guide);
                        }
                    }
                    
                    tutorialList.addAll(matchingGuides);
                    tutorialList.addAll(otherGuides);
                } else {
                    tutorialList.addAll(guides);
                }
                
                adapter.notifyDataSetChanged();
                showContent();
            }
        });
    }

    private void seedDefaultGuides() {
        new Thread(() -> {
            try {
                fitnessViewModel.clearAllExerciseGuides();

                // 1. Squat Form Guide
                fitnessViewModel.saveExerciseGuide(new ExerciseGuide(
                        "Standard Squat Form", "Beginner",
                        "Squats build quads, glutes and hamstrings. Keep feet shoulder-width apart, bend knees and push hips back like sitting in a chair.",
                        "Knees caving inward; heels lifting off the floor; chest dropping too forward; round back.",
                        "Keep heels flat, look straight ahead, keep core tight, and drive through your heels.",
                        "Do not let knees push past toes. Stop if you feel sharp knee pain.",
                        "Quads, Glutes, Hamstrings",
                        "3 Sets x 12-15 Reps",
                        "STRENGTH",
                        "https://www.youtube.com/watch?v=aclHkVaku9U",
                        R.drawable.onboarding_2
                ));

                // 2. Plank Hold Form Guide
                fitnessViewModel.saveExerciseGuide(new ExerciseGuide(
                        "Full Body Plank Hold", "Intermediate",
                        "Planks build isometric core strength. Maintain a straight forearm-plank position from head to heels, squeezing your glutes and abs.",
                        "Sagging hips; arching lower back; neck looking upward causing stress; holding breath.",
                        "Engage core completely, squeeze glutes, keep neck neutral, and push elbows into the floor.",
                        "Stop immediately if you experience sharp lower back pain. Keep spine straight.",
                        "Abs, Core, Shoulders",
                        "3 Sets x 45-60 Secs",
                        "CORE",
                        "https://www.youtube.com/watch?v=pSHjTRCQxIw",
                        R.drawable.onboarding_4
                ));

                // 3. Push-ups Form Guide
                fitnessViewModel.saveExerciseGuide(new ExerciseGuide(
                        "Push-ups Strength Check", "Intermediate",
                        "Pushups build chest, anterior deltoids and triceps. Lower chest to floor and push up with core and glutes fully tight.",
                        "Flaring elbows out to 90 degrees; neck reaching forward; hips dropping; half reps.",
                        "Keep elbows tucked at 45 degrees, engage core, keep neck neutral, and lock out at the top.",
                        "Keep wrists aligned beneath your shoulders. Transition to knee push-ups if form breaks.",
                        "Chest, Triceps, Shoulders",
                        "3 Sets x 10-15 Reps",
                        "STRENGTH",
                        "https://www.youtube.com/watch?v=IODxDxX7oi4",
                        R.drawable.onboarding_1
                ));

                // 4. Alternating Lunges Form Guide
                fitnessViewModel.saveExerciseGuide(new ExerciseGuide(
                        "Alternating Lunges", "Beginner",
                        "Lunges target unilateral thigh strength. Step forward with one leg and lower hips until both knees are bent at a 90-degree angle.",
                        "Front knee pushing past toe; stepping too narrow causing loss of balance; trunk leaning forward.",
                        "Step wide, lower hips straight down, keep chest upright, and push off the front heel.",
                        "Maintain balance and step straight. Ensure surface is stable and non-slippery.",
                        "Quads, Glutes, Hamstrings",
                        "3 Sets x 10 Reps / Leg",
                        "STRENGTH",
                        "https://www.youtube.com/watch?v=QOVaHWMGRyI",
                        R.drawable.onboarding_3
                ));

                // 5. Burpee Cardio Burn Form Guide
                fitnessViewModel.saveExerciseGuide(new ExerciseGuide(
                        "Burpee Cardio Burn", "Advanced",
                        "Burpees are a full body aerobic exercise that builds endurance and burns high fat. Drop into squat, kick feet back, do push-up, jump back, and explode vertically.",
                        "Lower back sagging during push-up; landing heavily on flat feet; neck arching during drop.",
                        "Keep core tight during push-up; land softly on the balls of your feet; synchronize breath.",
                        "Ensure soft surface/mat; avoid if you have wrist or severe lower back injury.",
                        "Full Body, Cardiorespiratory System",
                        "4 Sets x 10-12 Reps",
                        "CARDIO",
                        "https://www.youtube.com/watch?v=qLBImHhCX8o",
                        R.drawable.onboarding_2
                ));
            } catch (Exception e) {
                runOnUiThread(() -> showError("Failed to seed database: " + e.getMessage()));
            }
        }).start();
    }

    private void showLoading() {
        pbLoading.setVisibility(View.VISIBLE);
        rvTutorials.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
    }

    private void showContent() {
        pbLoading.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
        if (tutorialList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvTutorials.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvTutorials.setVisibility(View.VISIBLE);
        }
    }

    private void showError(String message) {
        pbLoading.setVisibility(View.GONE);
        rvTutorials.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.GONE);
        layoutError.setVisibility(View.VISIBLE);
        tvErrorMsg.setText(message);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    // Adapter Class
    private class TutorialAdapter extends RecyclerView.Adapter<TutorialAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tutorial_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ExerciseGuide item = tutorialList.get(position);
            holder.tvTitle.setText(item.getTitle());
            holder.tvLevel.setText(item.getDifficulty());
            holder.tvDesc.setText(item.getDescription());
            holder.tvTargetMuscles.setText(item.getTargetMuscles());
            holder.tvRecommended.setText(item.getRecommendedRepetitions());
            holder.tvCategory.setText(item.getCategory().toUpperCase());
            holder.tvPostureTips.setText(item.getPostureTips());
            holder.tvSafety.setText(item.getSafetyInstructions());
            holder.tvErrors.setText(item.getCommonErrors());
            holder.ivThumb.setImageResource(item.getImageRes());

            // Handle different difficulty badge backgrounds
            if ("Beginner".equalsIgnoreCase(item.getDifficulty())) {
                holder.tvLevel.setBackgroundResource(R.drawable.bg_badge_beginner);
                holder.tvLevel.setTextColor(0xFF3B82F6); // Blue
            } else if ("Intermediate".equalsIgnoreCase(item.getDifficulty())) {
                holder.tvLevel.setBackgroundResource(R.drawable.bg_badge_intermediate);
                holder.tvLevel.setTextColor(0xFF8B5CF6); // Purple/indigo
            } else {
                holder.tvLevel.setBackgroundResource(R.drawable.bg_badge_advanced);
                holder.tvLevel.setTextColor(0xFFEF4444); // Red
            }

            // Connect to Real-time coach camera preview
            holder.btnLaunchCoach.setOnClickListener(v -> {
                Intent intent = new Intent(LearningTutorialActivity.this, RealTimeFeedbackActivity.class);
                intent.putExtra("exercise_name", item.getTitle());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return tutorialList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivThumb;
            TextView tvTitle, tvLevel, tvDesc, tvCategory;
            TextView tvTargetMuscles, tvRecommended, tvPostureTips, tvSafety, tvErrors;
            View btnLaunchCoach;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivThumb = itemView.findViewById(R.id.ivThumb);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvLevel = itemView.findViewById(R.id.tvLevel);
                tvDesc = itemView.findViewById(R.id.tvDesc);
                tvCategory = itemView.findViewById(R.id.tvCategory);
                tvTargetMuscles = itemView.findViewById(R.id.tvTargetMuscles);
                tvRecommended = itemView.findViewById(R.id.tvRecommended);
                tvPostureTips = itemView.findViewById(R.id.tvPostureTips);
                tvSafety = itemView.findViewById(R.id.tvSafety);
                tvErrors = itemView.findViewById(R.id.tvErrors);
                btnLaunchCoach = itemView.findViewById(R.id.btnLaunchCoach);
            }
        }
    }
}

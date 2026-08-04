package com.fitness.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.fitness.app.R;
import com.fitness.app.data.room.AppDatabase;
import com.fitness.app.data.room.FitnessDao;
import com.fitness.app.models.EquipmentWorkoutPlan;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Locale;

public class WorkoutDetailsActivity extends AppCompatActivity {

    private EquipmentWorkoutPlan plan;
    private FitnessDao fitnessDao;
    private TextToSpeech textToSpeech;
    private boolean isTtsInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_details);

        fitnessDao = AppDatabase.getInstance(this).fitnessDao();

        plan = (EquipmentWorkoutPlan) getIntent().getSerializableExtra("workout_plan");
        if (plan == null) {
            Toast.makeText(this, "Failed to load workout details", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Bind main summary
        TextView tvWorkoutTitle = findViewById(R.id.tvWorkoutTitle);
        TextView tvDifficulty = findViewById(R.id.tvDifficulty);
        TextView tvDuration = findViewById(R.id.tvDuration);
        TextView tvCalories = findViewById(R.id.tvCalories);

        tvWorkoutTitle.setText(plan.getTitle());
        tvDifficulty.setText(plan.getDifficulty());
        tvDuration.setText(plan.getDuration() + " mins");
        tvCalories.setText(plan.getCalories() + " kcal");

        // Dynamically inflate exercises
        LinearLayout layoutExercisesContainer = findViewById(R.id.layoutExercisesContainer);
        try {
            JSONArray arr = new JSONArray(plan.getExercisesJson());
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                View exerciseView = LayoutInflater.from(this).inflate(R.layout.item_workout_exercise, layoutExercisesContainer, false);
                
                TextView tvExerciseName = exerciseView.findViewById(R.id.tvExerciseName);
                TextView tvTargetMuscle = exerciseView.findViewById(R.id.tvTargetMuscle);
                TextView tvSetsReps = exerciseView.findViewById(R.id.tvSetsReps);
                TextView tvRest = exerciseView.findViewById(R.id.tvRest);
                TextView tvInstructions = exerciseView.findViewById(R.id.tvInstructions);
                TextView tvMistakes = exerciseView.findViewById(R.id.tvMistakes);
                TextView tvSafety = exerciseView.findViewById(R.id.tvSafety);

                tvExerciseName.setText(obj.optString("name", "Exercise"));
                tvTargetMuscle.setText(obj.optString("targetMuscle", "All Body"));
                
                int sets = obj.optInt("sets", 3);
                String reps = obj.optString("reps", "12");
                tvSetsReps.setText("Sets: " + sets + " | Reps: " + reps);
                
                tvRest.setText("Rest: " + obj.optString("restTime", "60s"));
                tvInstructions.setText(obj.optString("instructions", ""));
                tvMistakes.setText(obj.optString("commonMistakes", "None"));
                tvSafety.setText(obj.optString("safetyTips", "Focus on form"));

                layoutExercisesContainer.addView(exerciseView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initialize TextToSpeech for Voice Coaching
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.US);
                isTtsInitialized = true;
            }
        });

        // Click actions
        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());

        findViewById(R.id.btnShare).setOnClickListener(v -> shareWorkoutContent());
        findViewById(R.id.btnSaveWorkout).setOnClickListener(v -> saveWorkoutToDb());
        findViewById(R.id.btnPdfDownload).setOnClickListener(v -> downloadWorkoutAsPdfText());
        findViewById(R.id.btnDeleteWorkout).setOnClickListener(v -> deleteWorkoutFromDb());

        // Voice Coach triggers TTS to read workout steps
        findViewById(R.id.btnVoiceCoach).setOnClickListener(v -> speakWorkoutSteps());
    }

    private void saveWorkoutToDb() {
        new Thread(() -> {
            fitnessDao.insertEquipmentWorkoutPlan(plan);
            runOnUiThread(() -> Toast.makeText(WorkoutDetailsActivity.this, "Workout saved successfully! 🎉", Toast.LENGTH_SHORT).show());
        }).start();
    }

    private void shareWorkoutContent() {
        String shareBody = buildWorkoutTextFormat();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, plan.getTitle());
        intent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(intent, "Share Workout Plan"));
    }

    private void deleteWorkoutFromDb() {
        new Thread(() -> {
            fitnessDao.deleteEquipmentWorkoutPlan(plan.getId());
            runOnUiThread(() -> {
                Toast.makeText(WorkoutDetailsActivity.this, "Workout deleted successfully", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }

    private void downloadWorkoutAsPdfText() {
        try {
            // Write to local external storage cache and share as text file provider
            File cacheDir = getExternalCacheDir();
            File txtFile = new File(cacheDir, plan.getTitle().replaceAll("\\s+", "_") + "_Plan.txt");
            
            FileOutputStream fos = new FileOutputStream(txtFile);
            fos.write(buildWorkoutTextFormat().getBytes());
            fos.close();

            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", txtFile);
            
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "text/plain");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);

            Toast.makeText(this, "Plan exported successfully to: " + txtFile.getName(), Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String buildWorkoutTextFormat() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- ").append(plan.getTitle().toUpperCase()).append(" ---\n");
        sb.append("Difficulty: ").append(plan.getDifficulty()).append("\n");
        sb.append("Duration: ").append(plan.getDuration()).append(" mins\n");
        sb.append("Calories: ").append(plan.getCalories()).append(" kcal\n\n");
        sb.append("EXERCISES:\n");

        try {
            JSONArray arr = new JSONArray(plan.getExercisesJson());
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                sb.append(i + 1).append(". ").append(obj.getString("name")).append("\n");
                sb.append("  Muscle: ").append(obj.getString("targetMuscle")).append("\n");
                sb.append("  Sets/Reps: ").append(obj.getInt("sets")).append("x").append(obj.getString("reps")).append("\n");
                sb.append("  Rest: ").append(obj.getString("restTime")).append("\n");
                sb.append("  Instructions: ").append(obj.getString("instructions")).append("\n\n");
            }
        } catch (Exception ignored) {}

        return sb.toString();
    }

    private void speakWorkoutSteps() {
        if (!isTtsInitialized) {
            Toast.makeText(this, "Voice Coach is initializing. Please tap again in a moment.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            textToSpeech.speak("Starting voice coaching for " + plan.getTitle(), TextToSpeech.QUEUE_FLUSH, null, "COACH_START");
            
            JSONArray arr = new JSONArray(plan.getExercisesJson());
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String name = obj.getString("name");
                int sets = obj.getInt("sets");
                String reps = obj.getString("reps");
                String instructions = obj.getString("instructions");
                
                String speechText = String.format("Next exercise is %s. Perform %d sets of %s reps. Guidelines: %s.", name, sets, reps, instructions);
                textToSpeech.speak(speechText, TextToSpeech.QUEUE_ADD, null, "COACH_STEP_" + i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}

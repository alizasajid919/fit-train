package com.fitness.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fitness.app.R;
import com.fitness.app.activities.LogWorkoutActivity;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WorkoutsFragment extends Fragment {

    private EditText etSearchWorkouts;
    private Chip chipAll, chipStrength, chipCardio, chipMindfulness;
    private Chip chipBeginner, chipIntermediate, chipAdvanced;
    private RecyclerView rvWorkoutsList;
    private View llNoResults;

    private View cardOnboarding;
    private View cardBeginnerTips;
    private TextView tvTipTitle, tvTipText;
    private com.fitness.app.data.local.LocalDataManager localDb;

    private List<WorkoutItem> originalList = new ArrayList<>();
    private List<WorkoutItem> filteredList = new ArrayList<>();
    private WorkoutListAdapter adapter;

    private String selectedCategory = "All";
    private String selectedDifficulty = "All";
    private String searchQuery = "";

    private final java.util.Set<String> activeFilters = new java.util.HashSet<>();
    private final android.os.Handler searchHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable searchRunnable;

    public static class WorkoutItem {
        public String title;
        public String description;
        public String category;
        public String duration;
        public String difficulty;
        public int calories;
        public int imageRes;

        public WorkoutItem(String title, String description, String category, String duration, String difficulty, int calories, int imageRes) {
            this.title = title;
            this.description = description;
            this.category = category;
            this.duration = duration;
            this.difficulty = difficulty;
            this.calories = calories;
            this.imageRes = imageRes;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workouts, container, false);

        etSearchWorkouts = view.findViewById(R.id.etSearchWorkouts);
        chipAll = view.findViewById(R.id.chipAll);
        chipStrength = view.findViewById(R.id.chipStrength);
        chipCardio = view.findViewById(R.id.chipCardio);
        chipMindfulness = view.findViewById(R.id.chipMindfulness);

        chipBeginner = view.findViewById(R.id.chipBeginner);
        chipIntermediate = view.findViewById(R.id.chipIntermediate);
        chipAdvanced = view.findViewById(R.id.chipAdvanced);

        rvWorkoutsList = view.findViewById(R.id.rvWorkoutsList);
        llNoResults = view.findViewById(R.id.llNoResults);

        localDb = new com.fitness.app.data.local.LocalDataManager(getActivity());
        localDb.sharedPreferences.edit().putBoolean("onboarding_card_dismissed", false).apply();
        if (!localDb.sharedPreferences.contains("saved_workout_log_count")) {
            localDb.sharedPreferences.edit().putInt("saved_workout_log_count", localDb.getAllWorkoutLogs().size()).apply();
        }
        loadActiveFilters();

        cardOnboarding = view.findViewById(R.id.cardOnboarding);
        cardBeginnerTips = view.findViewById(R.id.cardBeginnerTips);
        tvTipTitle = view.findViewById(R.id.tvTipTitle);
        tvTipText = view.findViewById(R.id.tvTipText);

        // Click calendar schedule
        view.findViewById(R.id.btnWorkoutSchedule).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), com.fitness.app.activities.WorkoutScheduleActivity.class));
        });

        // Click form guides button
        view.findViewById(R.id.btnFormGuides).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), com.fitness.app.activities.LearningTutorialActivity.class));
        });

        // Onboarding Card Dismiss
        view.findViewById(R.id.btnDismissOnboarding).setOnClickListener(v -> {
            localDb.sharedPreferences.edit().putBoolean("onboarding_card_dismissed", true).apply();
            cardOnboarding.setVisibility(View.GONE);
        });

        // Onboarding Learn Forms Action
        view.findViewById(R.id.btnLearnForms).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), com.fitness.app.activities.LearningTutorialActivity.class));
        });

        // Tip Dismiss
        view.findViewById(R.id.btnDismissTip).setOnClickListener(v -> {
            localDb.sharedPreferences.edit().putBoolean("workouts_tip_dismissed", true).apply();
            cardBeginnerTips.setVisibility(View.GONE);
        });

        // Bottom sheet filter button click listener
        view.findViewById(R.id.btnFilterOptions).setOnClickListener(v -> {
            showFilterDialog();
        });

        setupData();

        adapter = new WorkoutListAdapter(filteredList);
        rvWorkoutsList.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvWorkoutsList.setAdapter(adapter);

        setupListeners();
        applyFilters();
        updateFitnessTip(localDb);

        return view;
    }

    private void saveActiveFilters() {
        if (localDb != null) {
            localDb.sharedPreferences.edit()
                    .putStringSet("workout_active_filters", activeFilters)
                    .apply();
        }
    }

    private void loadActiveFilters() {
        if (localDb != null) {
            java.util.Set<String> saved = localDb.sharedPreferences.getStringSet("workout_active_filters", null);
            if (saved != null) {
                activeFilters.clear();
                activeFilters.addAll(saved);
            }
        }
    }

    private void setupData() {
        originalList.clear();
        
        // Strength
        originalList.add(new WorkoutItem("Dumbbell Upper Body", "Target chest, back, shoulders, and arms with dumbbells.", "Strength", "30 min", "Intermediate", 220, R.drawable.onboarding_1));
        originalList.add(new WorkoutItem("Barbell Lower Body", "Heavy squats, deadlifts, and lunges to build power.", "Strength", "45 min", "Advanced", 350, R.drawable.onboarding_2));
        originalList.add(new WorkoutItem("Core Crusher", "Sculpt your abdominal abs and obliques with high efficiency.", "Strength", "15 min", "Beginner", 100, R.drawable.onboarding_1));
        originalList.add(new WorkoutItem("Full Body Shred", "A high energy full-body weight routine to tone muscles.", "Strength", "25 min", "Intermediate", 240, R.drawable.onboarding_2));
        originalList.add(new WorkoutItem("Upper Body Builder", "Gain chest, bicep, and back thickness.", "Strength", "30 min", "Beginner", 210, R.drawable.onboarding_1));

        // Cardio
        originalList.add(new WorkoutItem("HIIT Cardio Blast", "High intensity intervals designed to burn fat fast.", "Cardio", "20 min", "Intermediate", 250, R.drawable.onboarding_2));
        originalList.add(new WorkoutItem("Brisk Walk & Run", "Alternate jogging and walking periods to build endurance.", "Cardio", "30 min", "Beginner", 180, R.drawable.onboarding_1));
        originalList.add(new WorkoutItem("Jumping Rope Interval", "Explosive jumping rope loops to build calves and lungs.", "Cardio", "15 min", "Advanced", 200, R.drawable.onboarding_2));
        originalList.add(new WorkoutItem("Cardio Core Burner", "Focus on sweating and core stability simultaneously.", "Cardio", "15 min", "Advanced", 190, R.drawable.onboarding_2));

        // Mindfulness
        originalList.add(new WorkoutItem("Restorative Yoga Flow", "Gentle yoga stretches to improve alignment and relax.", "Mindfulness", "25 min", "Beginner", 90, R.drawable.onboarding_4));
        originalList.add(new WorkoutItem("Deep Breathing Session", "Visual breathing synchronization to lower heart rate.", "Mindfulness", "5 min", "Beginner", 10, R.drawable.onboarding_4));
        originalList.add(new WorkoutItem("Body Scan Meditation", "A mindful check-in to relax muscles and restore energy.", "Mindfulness", "15 min", "Beginner", 15, R.drawable.onboarding_4));
    }

    private void setupListeners() {
        // Category Chips Toggle
        chipAll.setOnClickListener(v -> {
            activeFilters.remove("Strength");
            activeFilters.remove("Cardio");
            activeFilters.remove("Mindfulness");
            saveActiveFilters();
            applyFilters();
        });
        chipStrength.setOnClickListener(v -> {
            activeFilters.add("Strength");
            activeFilters.remove("Cardio");
            activeFilters.remove("Mindfulness");
            saveActiveFilters();
            applyFilters();
        });
        chipCardio.setOnClickListener(v -> {
            activeFilters.add("Cardio");
            activeFilters.remove("Strength");
            activeFilters.remove("Mindfulness");
            saveActiveFilters();
            applyFilters();
        });
        chipMindfulness.setOnClickListener(v -> {
            activeFilters.add("Mindfulness");
            activeFilters.remove("Strength");
            activeFilters.remove("Cardio");
            saveActiveFilters();
            applyFilters();
        });

        // Difficulty Chips Toggle
        chipBeginner.setOnClickListener(v -> toggleMainDifficulty("Beginner"));
        chipIntermediate.setOnClickListener(v -> toggleMainDifficulty("Intermediate"));
        chipAdvanced.setOnClickListener(v -> toggleMainDifficulty("Advanced"));

        // Search Input with 150ms debounce
        etSearchWorkouts.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim();
                searchHandler.removeCallbacks(searchRunnable);
                searchRunnable = () -> applyFilters();
                searchHandler.postDelayed(searchRunnable, 150);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void toggleMainDifficulty(String difficulty) {
        if (activeFilters.contains(difficulty)) {
            activeFilters.remove(difficulty);
        } else {
            activeFilters.add(difficulty);
        }
        saveActiveFilters();
        applyFilters();
    }

    private void applyFilters() {
        filteredList.clear();

        // Update the main tab's category chips selection state
        boolean hasCategoryFilter = activeFilters.contains("Strength") || activeFilters.contains("Cardio") || activeFilters.contains("Mindfulness");
        chipAll.setChecked(!hasCategoryFilter);
        chipStrength.setChecked(activeFilters.contains("Strength"));
        chipCardio.setChecked(activeFilters.contains("Cardio"));
        chipMindfulness.setChecked(activeFilters.contains("Mindfulness"));

        // Update the main tab's difficulty chips selection state
        chipBeginner.setChecked(activeFilters.contains("Beginner"));
        chipIntermediate.setChecked(activeFilters.contains("Intermediate"));
        chipAdvanced.setChecked(activeFilters.contains("Advanced"));

        for (WorkoutItem item : originalList) {
            // 1. Category Filter Match
            boolean categoryMatch = true;
            if (activeFilters.contains("Strength") || activeFilters.contains("Cardio") || activeFilters.contains("Mindfulness")) {
                categoryMatch = activeFilters.contains(item.category);
            } else if (activeFilters.contains("Fat Loss") || activeFilters.contains("Muscle Gain")) {
                boolean matchFatLoss = activeFilters.contains("Fat Loss") && (item.description.toLowerCase().contains("fat") || item.description.toLowerCase().contains("shred") || item.description.toLowerCase().contains("burn"));
                boolean matchMuscleGain = activeFilters.contains("Muscle Gain") && (item.description.toLowerCase().contains("muscle") || item.description.toLowerCase().contains("power") || item.description.toLowerCase().contains("builder") || item.title.toLowerCase().contains("builder"));
                categoryMatch = matchFatLoss || matchMuscleGain;
            }

            // 2. Difficulty Filter Match
            boolean difficultyMatch = true;
            if (activeFilters.contains("Beginner") || activeFilters.contains("Intermediate") || activeFilters.contains("Advanced")) {
                difficultyMatch = activeFilters.contains(item.difficulty);
            }

            // 3. Muscle Group Filter Match
            boolean muscleMatch = true;
            boolean hasMuscleFilter = activeFilters.contains("Full Body") || activeFilters.contains("Upper Body") || activeFilters.contains("Lower Body") || activeFilters.contains("Core");
            if (hasMuscleFilter) {
                muscleMatch = false;
                String lowerTitle = item.title.toLowerCase();
                String lowerDesc = item.description.toLowerCase();
                if (activeFilters.contains("Full Body") && (lowerTitle.contains("full body") || lowerDesc.contains("full body"))) {
                    muscleMatch = true;
                }
                if (activeFilters.contains("Upper Body") && (lowerTitle.contains("upper body") || lowerTitle.contains("chest") || lowerTitle.contains("builder") || lowerDesc.contains("chest") || lowerDesc.contains("arms") || lowerDesc.contains("shoulders"))) {
                    muscleMatch = true;
                }
                if (activeFilters.contains("Lower Body") && (lowerTitle.contains("lower body") || lowerTitle.contains("squats") || lowerTitle.contains("lunges") || lowerDesc.contains("legs") || lowerDesc.contains("calves"))) {
                    muscleMatch = true;
                }
                if (activeFilters.contains("Core") && (lowerTitle.contains("core") || lowerTitle.contains("crusher") || lowerDesc.contains("abdominal") || lowerDesc.contains("abs") || lowerDesc.contains("obliques"))) {
                    muscleMatch = true;
                }
            }

            // 4. Equipment Filter Match
            boolean equipmentMatch = true;
            boolean hasEquipmentFilter = activeFilters.contains("Home Workout") || activeFilters.contains("Gym Workout") || activeFilters.contains("Dumbbell") || activeFilters.contains("Barbell");
            if (hasEquipmentFilter) {
                equipmentMatch = false;
                boolean isGym = item.title.toLowerCase().contains("dumbbell") || item.title.toLowerCase().contains("barbell");
                if (activeFilters.contains("Home Workout") && !isGym) {
                    equipmentMatch = true;
                }
                if (activeFilters.contains("Gym Workout") && isGym) {
                    equipmentMatch = true;
                }
                if (activeFilters.contains("Dumbbell") && item.title.toLowerCase().contains("dumbbell")) {
                    equipmentMatch = true;
                }
                if (activeFilters.contains("Barbell") && item.title.toLowerCase().contains("barbell")) {
                    equipmentMatch = true;
                }
            }

            // 5. Duration Filter Match
            boolean durationMatch = true;
            boolean hasDurationFilter = activeFilters.contains("10 min") || activeFilters.contains("20 min") || activeFilters.contains("30 min") || activeFilters.contains("45 min") || activeFilters.contains("60+ min");
            if (hasDurationFilter) {
                durationMatch = false;
                int durationVal = Integer.parseInt(item.duration.replace(" min", "").trim());
                if (activeFilters.contains("10 min") && durationVal <= 15) durationMatch = true;
                if (activeFilters.contains("20 min") && durationVal > 15 && durationVal <= 22) durationMatch = true;
                if (activeFilters.contains("30 min") && durationVal > 22 && durationVal <= 35) durationMatch = true;
                if (activeFilters.contains("45 min") && durationVal > 35 && durationVal <= 50) durationMatch = true;
                if (activeFilters.contains("60+ min") && durationVal > 50) durationMatch = true;
            }

            // 6. Calories Filter Match
            boolean caloriesMatch = true;
            boolean hasCaloriesFilter = activeFilters.contains("< 150 kcal") || activeFilters.contains("150 - 300 kcal") || activeFilters.contains("> 300 kcal");
            if (hasCaloriesFilter) {
                caloriesMatch = false;
                if (activeFilters.contains("< 150 kcal") && item.calories < 150) caloriesMatch = true;
                if (activeFilters.contains("150 - 300 kcal") && item.calories >= 150 && item.calories <= 300) caloriesMatch = true;
                if (activeFilters.contains("> 300 kcal") && item.calories > 300) caloriesMatch = true;
            }

            // 7. Search Match
            boolean searchMatch = fuzzyMatch(searchQuery, item);

            if (categoryMatch && difficultyMatch && muscleMatch && equipmentMatch && durationMatch && caloriesMatch && searchMatch) {
                filteredList.add(item);
            }
        }

        if (filteredList.isEmpty()) {
            llNoResults.setVisibility(View.VISIBLE);
            rvWorkoutsList.setVisibility(View.GONE);
        } else {
            llNoResults.setVisibility(View.GONE);
            rvWorkoutsList.setVisibility(View.VISIBLE);
        }

        adapter.notifyDataSetChanged();
        updateFitnessTip(localDb);
    }

    private boolean fuzzyMatch(String query, WorkoutItem item) {
        if (query == null || query.isEmpty()) return true;

        String cleanQuery = query.toLowerCase(Locale.getDefault()).trim();
        String[] tokens = cleanQuery.split("\\s+");

        String title = item.title.toLowerCase(Locale.getDefault());
        String desc = item.description.toLowerCase(Locale.getDefault());
        String cat = item.category.toLowerCase(Locale.getDefault());
        String diff = item.difficulty.toLowerCase(Locale.getDefault());

        StringBuilder metaBuilder = new StringBuilder();
        metaBuilder.append(title).append(" ").append(desc).append(" ").append(cat).append(" ").append(diff);

        if (title.contains("dumbbell")) metaBuilder.append(" dumbbell db equipment weight gym");
        if (title.contains("barbell")) metaBuilder.append(" barbell bb equipment weight gym");
        if (title.contains("rope")) metaBuilder.append(" rope jump calves jump-rope");

        if (title.contains("upper") || desc.contains("chest") || desc.contains("arms") || desc.contains("shoulders")) {
            metaBuilder.append(" upper chest shoulders arms back bicep tricep");
        }
        if (title.contains("lower") || desc.contains("squats") || desc.contains("lunges") || desc.contains("legs")) {
            metaBuilder.append(" lower legs squats lunges calves glutes hamstrings");
        }
        if (title.contains("core") || desc.contains("abs") || desc.contains("abdominal") || desc.contains("obliques")) {
            metaBuilder.append(" core abs obliques abdominals stomach crusher flat");
        }
        if (title.contains("full") || desc.contains("full-body")) {
            metaBuilder.append(" full body general complete routine tone");
        }

        String metadata = metaBuilder.toString();

        for (String token : tokens) {
            if (metadata.contains(token)) continue;

            if (token.length() > 3) {
                boolean foundFuzzy = false;
                String[] words = metadata.split("\\s+");
                for (String word : words) {
                    if (word.length() >= token.length() - 1 && word.length() <= token.length() + 1) {
                        if (getLevenshteinDistance(token, word) <= 1) {
                            foundFuzzy = true;
                            break;
                        }
                    }
                }
                if (foundFuzzy) continue;
            }
            return false;
        }
        return true;
    }

    private int getLevenshteinDistance(String s, String t) {
        if (s == null || t == null) return 0;
        int n = s.length();
        int m = t.length();
        if (n == 0) return m;
        if (m == 0) return n;
        int[] p = new int[n + 1];
        int[] d = new int[n + 1];
        int[] _d;
        int i, j;
        char t_j;
        int cost;
        for (i = 0; i <= n; i++) p[i] = i;
        for (j = 1; j <= m; j++) {
            t_j = t.charAt(j - 1);
            d[0] = j;
            for (i = 1; i <= n; i++) {
                cost = s.charAt(i - 1) == t_j ? 0 : 1;
                d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + cost);
            }
            _d = p;
            p = d;
            d = _d;
        }
        return p[n];
    }

    private void showFilterDialog() {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = 
                new com.google.android.material.bottomsheet.BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_workout_filters, null);
        dialog.setContentView(view);

        com.google.android.material.chip.Chip fChipBeginner = view.findViewById(R.id.fChipBeginner);
        com.google.android.material.chip.Chip fChipIntermediate = view.findViewById(R.id.fChipIntermediate);
        com.google.android.material.chip.Chip fChipAdvanced = view.findViewById(R.id.fChipAdvanced);

        com.google.android.material.chip.Chip fChipStrength = view.findViewById(R.id.fChipStrength);
        com.google.android.material.chip.Chip fChipCardio = view.findViewById(R.id.fChipCardio);
        com.google.android.material.chip.Chip fChipMindfulness = view.findViewById(R.id.fChipMindfulness);
        com.google.android.material.chip.Chip fChipFatLoss = view.findViewById(R.id.fChipFatLoss);
        com.google.android.material.chip.Chip fChipMuscleGain = view.findViewById(R.id.fChipMuscleGain);

        com.google.android.material.chip.Chip fChipFullBody = view.findViewById(R.id.fChipFullBody);
        com.google.android.material.chip.Chip fChipUpperBody = view.findViewById(R.id.fChipUpperBody);
        com.google.android.material.chip.Chip fChipLowerBody = view.findViewById(R.id.fChipLowerBody);
        com.google.android.material.chip.Chip fChipCore = view.findViewById(R.id.fChipCore);

        com.google.android.material.chip.Chip fChipHomeWorkout = view.findViewById(R.id.fChipHomeWorkout);
        com.google.android.material.chip.Chip fChipGymWorkout = view.findViewById(R.id.fChipGymWorkout);
        com.google.android.material.chip.Chip fChipDumbbell = view.findViewById(R.id.fChipDumbbell);
        com.google.android.material.chip.Chip fChipBarbell = view.findViewById(R.id.fChipBarbell);

        com.google.android.material.chip.Chip fChipDur10 = view.findViewById(R.id.fChipDur10);
        com.google.android.material.chip.Chip fChipDur20 = view.findViewById(R.id.fChipDur20);
        com.google.android.material.chip.Chip fChipDur30 = view.findViewById(R.id.fChipDur30);
        com.google.android.material.chip.Chip fChipDur45 = view.findViewById(R.id.fChipDur45);
        com.google.android.material.chip.Chip fChipDur60 = view.findViewById(R.id.fChipDur60);

        com.google.android.material.chip.Chip fChipCalLow = view.findViewById(R.id.fChipCalLow);
        com.google.android.material.chip.Chip fChipCalMid = view.findViewById(R.id.fChipCalMid);
        com.google.android.material.chip.Chip fChipCalHigh = view.findViewById(R.id.fChipCalHigh);

        fChipBeginner.setChecked(activeFilters.contains("Beginner"));
        fChipIntermediate.setChecked(activeFilters.contains("Intermediate"));
        fChipAdvanced.setChecked(activeFilters.contains("Advanced"));

        fChipStrength.setChecked(activeFilters.contains("Strength"));
        fChipCardio.setChecked(activeFilters.contains("Cardio"));
        fChipMindfulness.setChecked(activeFilters.contains("Mindfulness"));
        fChipFatLoss.setChecked(activeFilters.contains("Fat Loss"));
        fChipMuscleGain.setChecked(activeFilters.contains("Muscle Gain"));

        fChipFullBody.setChecked(activeFilters.contains("Full Body"));
        fChipUpperBody.setChecked(activeFilters.contains("Upper Body"));
        fChipLowerBody.setChecked(activeFilters.contains("Lower Body"));
        fChipCore.setChecked(activeFilters.contains("Core"));

        fChipHomeWorkout.setChecked(activeFilters.contains("Home Workout"));
        fChipGymWorkout.setChecked(activeFilters.contains("Gym Workout"));
        fChipDumbbell.setChecked(activeFilters.contains("Dumbbell"));
        fChipBarbell.setChecked(activeFilters.contains("Barbell"));

        fChipDur10.setChecked(activeFilters.contains("10 min"));
        fChipDur20.setChecked(activeFilters.contains("20 min"));
        fChipDur30.setChecked(activeFilters.contains("30 min"));
        fChipDur45.setChecked(activeFilters.contains("45 min"));
        fChipDur60.setChecked(activeFilters.contains("60+ min"));

        fChipCalLow.setChecked(activeFilters.contains("< 150 kcal"));
        fChipCalMid.setChecked(activeFilters.contains("150 - 300 kcal"));
        fChipCalHigh.setChecked(activeFilters.contains("> 300 kcal"));

        view.findViewById(R.id.btnResetFilters).setOnClickListener(v -> {
            fChipBeginner.setChecked(false);
            fChipIntermediate.setChecked(false);
            fChipAdvanced.setChecked(false);
            fChipStrength.setChecked(false);
            fChipCardio.setChecked(false);
            fChipMindfulness.setChecked(false);
            fChipFatLoss.setChecked(false);
            fChipMuscleGain.setChecked(false);
            fChipFullBody.setChecked(false);
            fChipUpperBody.setChecked(false);
            fChipLowerBody.setChecked(false);
            fChipCore.setChecked(false);
            fChipHomeWorkout.setChecked(false);
            fChipGymWorkout.setChecked(false);
            fChipDumbbell.setChecked(false);
            fChipBarbell.setChecked(false);
            fChipDur10.setChecked(false);
            fChipDur20.setChecked(false);
            fChipDur30.setChecked(false);
            fChipDur45.setChecked(false);
            fChipDur60.setChecked(false);
            fChipCalLow.setChecked(false);
            fChipCalMid.setChecked(false);
            fChipCalHigh.setChecked(false);
        });

        view.findViewById(R.id.btnApplyFilters).setOnClickListener(v -> {
            activeFilters.clear();
            if (fChipBeginner.isChecked()) activeFilters.add("Beginner");
            if (fChipIntermediate.isChecked()) activeFilters.add("Intermediate");
            if (fChipAdvanced.isChecked()) activeFilters.add("Advanced");

            if (fChipStrength.isChecked()) activeFilters.add("Strength");
            if (fChipCardio.isChecked()) activeFilters.add("Cardio");
            if (fChipMindfulness.isChecked()) activeFilters.add("Mindfulness");
            if (fChipFatLoss.isChecked()) activeFilters.add("Fat Loss");
            if (fChipMuscleGain.isChecked()) activeFilters.add("Muscle Gain");

            if (fChipFullBody.isChecked()) activeFilters.add("Full Body");
            if (fChipUpperBody.isChecked()) activeFilters.add("Upper Body");
            if (fChipLowerBody.isChecked()) activeFilters.add("Lower Body");
            if (fChipCore.isChecked()) activeFilters.add("Core");

            if (fChipHomeWorkout.isChecked()) activeFilters.add("Home Workout");
            if (fChipGymWorkout.isChecked()) activeFilters.add("Gym Workout");
            if (fChipDumbbell.isChecked()) activeFilters.add("Dumbbell");
            if (fChipBarbell.isChecked()) activeFilters.add("Barbell");

            if (fChipDur10.isChecked()) activeFilters.add("10 min");
            if (fChipDur20.isChecked()) activeFilters.add("20 min");
            if (fChipDur30.isChecked()) activeFilters.add("30 min");
            if (fChipDur45.isChecked()) activeFilters.add("45 min");
            if (fChipDur60.isChecked()) activeFilters.add("60+ min");

            if (fChipCalLow.isChecked()) activeFilters.add("< 150 kcal");
            if (fChipCalMid.isChecked()) activeFilters.add("150 - 300 kcal");
            if (fChipCalHigh.isChecked()) activeFilters.add("> 300 kcal");

            saveActiveFilters();
            applyFilters();
            dialog.dismiss();
        });

        dialog.show();
    }

    private class WorkoutListAdapter extends RecyclerView.Adapter<WorkoutListAdapter.ViewHolder> {
        private final List<WorkoutItem> workouts;

        public WorkoutListAdapter(List<WorkoutItem> workouts) {
            this.workouts = workouts;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_workout, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            WorkoutItem item = workouts.get(position);
            holder.tvTitle.setText(item.title);
            holder.tvDesc.setText(item.description);
            holder.tvDuration.setText(item.duration);
            holder.tvDifficulty.setText(item.difficulty);
            holder.tvCalories.setText(item.calories + " kcal");
            holder.ivImage.setImageResource(item.imageRes);

            // Style difficulty badges dynamically
            String diff = item.difficulty.toLowerCase(java.util.Locale.getDefault());
            if (diff.contains("begin")) {
                holder.tvDifficulty.setTextColor(android.graphics.Color.parseColor("#15803D"));
                holder.tvDifficulty.setBackgroundResource(R.drawable.bg_badge_beginner);
            } else if (diff.contains("intermed")) {
                holder.tvDifficulty.setTextColor(android.graphics.Color.parseColor("#C2410C"));
                holder.tvDifficulty.setBackgroundResource(R.drawable.bg_badge_intermediate);
            } else {
                holder.tvDifficulty.setTextColor(android.graphics.Color.parseColor("#B91C1C"));
                holder.tvDifficulty.setBackgroundResource(R.drawable.bg_badge_advanced);
            }

            holder.btnStart.setOnClickListener(v -> {
                holder.btnStart.setEnabled(false);
                holder.btnStart.setText("Starting...");
                if (holder.pbStart != null) {
                    holder.pbStart.setVisibility(View.VISIBLE);
                }
                holder.btnStart.postDelayed(() -> {
                    holder.btnStart.setEnabled(true);
                    holder.btnStart.setText("Start Workout");
                    if (holder.pbStart != null) {
                        holder.pbStart.setVisibility(View.GONE);
                    }
                    Intent intent = new Intent(getActivity(), LogWorkoutActivity.class);
                    intent.putExtra("exercise_name", item.title);
                    intent.putExtra("duration_min", item.duration.replace(" min", ""));
                    intent.putExtra("calories_kcal", String.valueOf(item.calories));
                    startActivity(intent);
                }, 600);
            });
        }

        @Override
        public int getItemCount() {
            return workouts.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            android.widget.ImageView ivImage;
            TextView tvTitle, tvDesc, tvDuration, tvDifficulty, tvCalories;
            android.widget.Button btnStart;
            android.widget.ProgressBar pbStart;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivImage = itemView.findViewById(R.id.ivWorkoutImage);
                tvTitle = itemView.findViewById(R.id.tvWorkoutTitle);
                tvDesc = itemView.findViewById(R.id.tvWorkoutDesc);
                tvDuration = itemView.findViewById(R.id.tvWorkoutDuration);
                tvDifficulty = itemView.findViewById(R.id.tvWorkoutDifficulty);
                tvCalories = itemView.findViewById(R.id.tvWorkoutCalories);
                btnStart = itemView.findViewById(R.id.btnStartWorkout);
                pbStart = itemView.findViewById(R.id.pbStartWorkout);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (localDb != null) {
            // 1. Refresh onboarding card visibility
            boolean onboardingCardDismissed = localDb.sharedPreferences.getBoolean("onboarding_card_dismissed", false);
            if (cardOnboarding != null) {
                cardOnboarding.setVisibility(onboardingCardDismissed ? View.GONE : View.VISIBLE);
            }

            // 2. Intelligent tips refresh after workouts
            int savedLogCount = localDb.sharedPreferences.getInt("saved_workout_log_count", 0);
            int currentLogCount = localDb.getAllWorkoutLogs().size();
            if (currentLogCount > savedLogCount) {
                // A new workout was logged! Reset tip dismissal so a new tip shows up
                localDb.sharedPreferences.edit()
                        .putBoolean("workouts_tip_dismissed", false)
                        .putInt("saved_workout_log_count", currentLogCount)
                        .apply();
                
                if (cardBeginnerTips != null) {
                    cardBeginnerTips.setVisibility(View.VISIBLE);
                    updateFitnessTip(localDb);
                }
            } else {
                // Just update tip based on current state
                boolean tipDismissed = localDb.sharedPreferences.getBoolean("workouts_tip_dismissed", false);
                if (cardBeginnerTips != null) {
                    cardBeginnerTips.setVisibility(tipDismissed ? View.GONE : View.VISIBLE);
                    if (!tipDismissed) {
                        updateFitnessTip(localDb);
                    }
                }
            }
        }
    }

    private void updateFitnessTip(com.fitness.app.data.local.LocalDataManager localDb) {
        if (tvTipText == null || tvTipTitle == null || localDb == null) return;

        com.fitness.app.models.User user = localDb.getUser();
        String fitnessLevel = (user != null && user.getFitnessExperience() != null) ? user.getFitnessExperience() : "Beginner";
        
        String category = selectedCategory;
        String tip = "";

        if ("Strength".equalsIgnoreCase(category)) {
            if ("Beginner".equalsIgnoreCase(fitnessLevel)) {
                tip = "Form is everything. Focus on slow, controlled movements and master the basics before increasing weight.";
            } else if ("Intermediate".equalsIgnoreCase(fitnessLevel)) {
                tip = "Progressive overload is key. Try adding 2-5% more weight or doing 1 extra rep compared to last session.";
            } else {
                tip = "Incorporate tempo training (e.g., 3-second negatives) to maximize muscle fiber recruitment and strength.";
            }
        } else if ("Cardio".equalsIgnoreCase(category)) {
            if ("Beginner".equalsIgnoreCase(fitnessLevel)) {
                tip = "Stay in your aerobic zone. You should be able to maintain a conversation while walking or slow jogging.";
            } else if ("Intermediate".equalsIgnoreCase(fitnessLevel)) {
                tip = "Try interval training (HIIT): 30 seconds of high intensity followed by 60 seconds of active recovery.";
            } else {
                tip = "Track your resting heart rate and recovery rate to monitor cardiovascular efficiency and fatigue.";
            }
        } else if ("Mindfulness".equalsIgnoreCase(category)) {
            if ("Beginner".equalsIgnoreCase(fitnessLevel)) {
                tip = "Focus on the physical sensation of your breath. If your mind wanders, gently bring it back without judgment.";
            } else if ("Intermediate".equalsIgnoreCase(fitnessLevel)) {
                tip = "Try nasal breathing exclusively during yoga to keep your parasympathetic nervous system fully activated.";
            } else {
                tip = "Pair mindfulness with post-workout stretching to speed up nervous system recovery and lower cortisol levels.";
            }
        } else { // "All" or other
            if ("Beginner".equalsIgnoreCase(fitnessLevel)) {
                tip = "Consistency over intensity. Establishing a habit of moving 3-4 times a week is your most powerful tool.";
            } else if ("Intermediate".equalsIgnoreCase(fitnessLevel)) {
                tip = "Vary your workout routines every 4-6 weeks to prevent plateaus and keep muscle adaptation active.";
            } else {
                tip = "Maximize muscle recovery with 7-9 hours of deep sleep and clean hydration of at least 3-4 liters daily.";
            }
        }

        if ("All".equalsIgnoreCase(category)) {
            tvTipTitle.setText("COACH TIP");
        } else {
            tvTipTitle.setText(category.toUpperCase() + " TIP");
        }
        tvTipText.setText(tip);
    }
}

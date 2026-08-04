package com.fitness.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fitness.app.R;
import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.data.room.AppDatabase;
import com.fitness.app.data.room.FitnessDao;
import com.fitness.app.models.EquipmentWorkoutPlan;
import com.fitness.app.models.User;
import com.fitness.app.utils.GroceryAiEngine;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EquipmentSelectionActivity extends AppCompatActivity {

    private ChipGroup cgGoals;
    private CheckBox cbNoEquipment, cbChair, cbWaterBottle, cbResistanceBand, cbBackpack;
    private CheckBox cbYogaMat, cbDumbbells, cbBarbell, cbPullUpBar, cbBench;
    private CheckBox cbKettlebell, cbMedicineBall, cbJumpRope, cbStairs, cbSofa, cbWall, cbTowel, cbTable;
    
    private View layoutLoading;
    private View layoutEmptyHistory;
    private RecyclerView rvWorkoutHistory;
    
    private LocalDataManager localDb;
    private FitnessDao fitnessDao;
    private final List<EquipmentWorkoutPlan> historyPlans = new ArrayList<>();
    private HistoryAdapter historyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_equipment_selection);

        localDb = new LocalDataManager(this);
        fitnessDao = AppDatabase.getInstance(this).fitnessDao();

        // Bind Views
        cgGoals = findViewById(R.id.cgGoals);
        
        cbNoEquipment = findViewById(R.id.cbNoEquipment);
        cbChair = findViewById(R.id.cbChair);
        cbWaterBottle = findViewById(R.id.cbWaterBottle);
        cbResistanceBand = findViewById(R.id.cbResistanceBand);
        cbBackpack = findViewById(R.id.cbBackpack);
        cbYogaMat = findViewById(R.id.cbYogaMat);
        cbDumbbells = findViewById(R.id.cbDumbbells);
        cbBarbell = findViewById(R.id.cbBarbell);
        cbPullUpBar = findViewById(R.id.cbPullUpBar);
        cbBench = findViewById(R.id.cbBench);
        cbKettlebell = findViewById(R.id.cbKettlebell);
        cbMedicineBall = findViewById(R.id.cbMedicineBall);
        cbJumpRope = findViewById(R.id.cbJumpRope);
        cbStairs = findViewById(R.id.cbStairs);
        cbSofa = findViewById(R.id.cbSofa);
        cbWall = findViewById(R.id.cbWall);
        cbTowel = findViewById(R.id.cbTowel);
        cbTable = findViewById(R.id.cbTable);

        layoutLoading = findViewById(R.id.layoutLoading);
        layoutEmptyHistory = findViewById(R.id.layoutEmptyHistory);
        rvWorkoutHistory = findViewById(R.id.rvWorkoutHistory);

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());
        findViewById(R.id.btnSubmitGeneration).setOnClickListener(v -> generateWorkoutPlan());

        // Setup Mutually Exclusive Selection for No Equipment
        setupExclusionRules();

        // Load Pre-selections from User settings
        loadProfilePreselections();

        // Setup History list
        setupHistoryRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistoryPlans();
    }

    private void setupExclusionRules() {
        CheckBox[] otherEquipments = new CheckBox[]{
            cbChair, cbWaterBottle, cbResistanceBand, cbBackpack, cbYogaMat, 
            cbDumbbells, cbBarbell, cbPullUpBar, cbBench, cbKettlebell, 
            cbMedicineBall, cbJumpRope, cbStairs, cbSofa, cbWall, cbTowel, cbTable
        };

        cbNoEquipment.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                for (CheckBox cb : otherEquipments) {
                    if (cb.isChecked()) cb.setChecked(false);
                }
            }
        });

        for (CheckBox cb : otherEquipments) {
            cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (cbNoEquipment.isChecked()) cbNoEquipment.setChecked(false);
                }
            });
        }
    }

    private void loadProfilePreselections() {
        User user = localDb.getUser();
        if (user == null) return;

        // Pre-check Active Workout Goal Chip
        String activeGoal = user.getGoal();
        if (activeGoal != null && !activeGoal.trim().isEmpty()) {
            String lowerGoal = activeGoal.toLowerCase();
            if (lowerGoal.contains("weight") && lowerGoal.contains("loss")) {
                cgGoals.check(R.id.chipWeightLoss);
            } else if (lowerGoal.contains("muscle") && lowerGoal.contains("gain")) {
                cgGoals.check(R.id.chipMuscleGain);
            } else if (lowerGoal.contains("fat") && lowerGoal.contains("loss")) {
                cgGoals.check(R.id.chipFatLoss);
            } else if (lowerGoal.contains("strength")) {
                cgGoals.check(R.id.chipStrength);
            } else if (lowerGoal.contains("endurance")) {
                cgGoals.check(R.id.chipEndurance);
            } else if (lowerGoal.contains("cardio")) {
                cgGoals.check(R.id.chipCardio);
            }
        }

        // Pre-check Available Equipment
        String savedEquip = user.getAvailableEquipment();
        if (savedEquip != null && !savedEquip.trim().isEmpty()) {
            String[] items = savedEquip.split(",\\s*");
            for (String item : items) {
                if ("No Equipment".equalsIgnoreCase(item)) cbNoEquipment.setChecked(true);
                else if ("Chair".equalsIgnoreCase(item)) cbChair.setChecked(true);
                else if ("Water Bottle".equalsIgnoreCase(item)) cbWaterBottle.setChecked(true);
                else if ("Resistance Band".equalsIgnoreCase(item)) cbResistanceBand.setChecked(true);
                else if ("Backpack".equalsIgnoreCase(item)) cbBackpack.setChecked(true);
                else if ("Yoga Mat".equalsIgnoreCase(item)) cbYogaMat.setChecked(true);
                else if ("Dumbbells".equalsIgnoreCase(item)) cbDumbbells.setChecked(true);
                else if ("Barbell".equalsIgnoreCase(item)) cbBarbell.setChecked(true);
                else if ("Pull-up Bar".equalsIgnoreCase(item)) cbPullUpBar.setChecked(true);
                else if ("Bench".equalsIgnoreCase(item)) cbBench.setChecked(true);
                else if ("Kettlebell".equalsIgnoreCase(item)) cbKettlebell.setChecked(true);
                else if ("Medicine Ball".equalsIgnoreCase(item)) cbMedicineBall.setChecked(true);
                else if ("Jump Rope".equalsIgnoreCase(item)) cbJumpRope.setChecked(true);
                else if ("Stairs".equalsIgnoreCase(item)) cbStairs.setChecked(true);
                else if ("Sofa".equalsIgnoreCase(item)) cbSofa.setChecked(true);
                else if ("Wall".equalsIgnoreCase(item)) cbWall.setChecked(true);
                else if ("Towel".equalsIgnoreCase(item)) cbTowel.setChecked(true);
                else if ("Table".equalsIgnoreCase(item)) cbTable.setChecked(true);
            }
        }
    }

    private void setupHistoryRecyclerView() {
        rvWorkoutHistory.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new HistoryAdapter(historyPlans, plan -> {
            Intent intent = new Intent(EquipmentSelectionActivity.this, WorkoutDetailsActivity.class);
            intent.putExtra("workout_plan", plan);
            startActivity(intent);
        });
        rvWorkoutHistory.setAdapter(historyAdapter);
    }

    private void loadHistoryPlans() {
        new Thread(() -> {
            List<EquipmentWorkoutPlan> list = fitnessDao.getAllEquipmentWorkoutPlans();
            runOnUiThread(() -> {
                historyPlans.clear();
                if (list != null && !list.isEmpty()) {
                    historyPlans.addAll(list);
                    layoutEmptyHistory.setVisibility(View.GONE);
                    rvWorkoutHistory.setVisibility(View.VISIBLE);
                } else {
                    layoutEmptyHistory.setVisibility(View.VISIBLE);
                    rvWorkoutHistory.setVisibility(View.GONE);
                }
                historyAdapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void generateWorkoutPlan() {
        // Collect goal
        int checkedChipId = cgGoals.getCheckedChipId();
        if (checkedChipId == View.NO_ID) {
            Toast.makeText(this, "Please select a workout goal", Toast.LENGTH_SHORT).show();
            return;
        }
        Chip chip = findViewById(checkedChipId);
        String goal = chip.getText().toString();

        // Collect equipment
        List<String> selectedEquipment = new ArrayList<>();
        if (cbNoEquipment.isChecked()) selectedEquipment.add("No Equipment");
        if (cbChair.isChecked()) selectedEquipment.add("Chair");
        if (cbWaterBottle.isChecked()) selectedEquipment.add("Water Bottle");
        if (cbResistanceBand.isChecked()) selectedEquipment.add("Resistance Band");
        if (cbBackpack.isChecked()) selectedEquipment.add("Backpack");
        if (cbYogaMat.isChecked()) selectedEquipment.add("Yoga Mat");
        if (cbDumbbells.isChecked()) selectedEquipment.add("Dumbbells");
        if (cbBarbell.isChecked()) selectedEquipment.add("Barbell");
        if (cbPullUpBar.isChecked()) selectedEquipment.add("Pull-up Bar");
        if (cbBench.isChecked()) selectedEquipment.add("Bench");
        if (cbKettlebell.isChecked()) selectedEquipment.add("Kettlebell");
        if (cbMedicineBall.isChecked()) selectedEquipment.add("Medicine Ball");
        if (cbJumpRope.isChecked()) selectedEquipment.add("Jump Rope");
        if (cbStairs.isChecked()) selectedEquipment.add("Stairs");
        if (cbSofa.isChecked()) selectedEquipment.add("Sofa");
        if (cbWall.isChecked()) selectedEquipment.add("Wall");
        if (cbTowel.isChecked()) selectedEquipment.add("Towel");
        if (cbTable.isChecked()) selectedEquipment.add("Table");

        // Smart Validation
        if (selectedEquipment.isEmpty()) {
            Toast.makeText(this, "Please select at least one equipment or choose 'No Equipment'", Toast.LENGTH_LONG).show();
            return;
        }

        // Save selected equipment list in user profile
        User user = localDb.getUser();
        if (user != null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < selectedEquipment.size(); i++) {
                sb.append(selectedEquipment.get(i));
                if (i < selectedEquipment.size() - 1) sb.append(", ");
            }
            user.setAvailableEquipment(sb.toString());
            localDb.saveUser(user);
        }

        layoutLoading.setVisibility(View.VISIBLE);

        GroceryAiEngine.generateEquipmentWorkout(this, user, goal, selectedEquipment, new GroceryAiEngine.EquipmentWorkoutCallback() {
            @Override
            public void onSuccess(EquipmentWorkoutPlan plan) {
                // Save to Room DB and navigate
                new Thread(() -> {
                    fitnessDao.insertEquipmentWorkoutPlan(plan);
                    runOnUiThread(() -> {
                        layoutLoading.setVisibility(View.GONE);
                        Intent intent = new Intent(EquipmentSelectionActivity.this, WorkoutDetailsActivity.class);
                        intent.putExtra("workout_plan", plan);
                        startActivity(intent);
                    });
                }).start();
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    layoutLoading.setVisibility(View.GONE);
                    Toast.makeText(EquipmentSelectionActivity.this, "AI generation failed: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private interface OnItemClickListener {
        void onItemClick(EquipmentWorkoutPlan plan);
    }

    private static class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

        private final List<EquipmentWorkoutPlan> list;
        private final OnItemClickListener listener;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

        HistoryAdapter(List<EquipmentWorkoutPlan> list, OnItemClickListener listener) {
            this.list = list;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_generated_workout_row, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            EquipmentWorkoutPlan plan = list.get(position);
            holder.tvPlanTitle.setText(plan.getTitle());
            holder.tvPlanSubtitle.setText(String.format(Locale.getDefault(), "%s • %d mins • %d kcal", plan.getDifficulty(), plan.getDuration(), plan.getCalories()));
            holder.tvPlanDate.setText(dateFormat.format(new Date(plan.getDateCreated())));

            holder.itemView.setOnClickListener(v -> listener.onItemClick(plan));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvPlanTitle, tvPlanSubtitle, tvPlanDate;

            ViewHolder(View itemView) {
                super(itemView);
                tvPlanTitle = itemView.findViewById(R.id.tvPlanTitle);
                tvPlanSubtitle = itemView.findViewById(R.id.tvPlanSubtitle);
                tvPlanDate = itemView.findViewById(R.id.tvPlanDate);
            }
        }
    }
}

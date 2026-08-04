package com.fitness.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fitness.app.R;
import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.models.WorkoutLog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SquatHistoryActivity extends AppCompatActivity {

    private LocalDataManager localDb;
    private RecyclerView rvHistoryList;
    private View llEmptyState;
    private ChipGroup cgFilters;
    private HistoryListAdapter adapter;

    private final List<WorkoutLog> allLogs = new ArrayList<>();
    private final List<WorkoutLog> displayList = new ArrayList<>();

    private String currentFilter = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_squat_history);

        localDb = new LocalDataManager(this);

        // Bind Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        // Bind Views
        rvHistoryList = findViewById(R.id.rvHistoryList);
        llEmptyState = findViewById(R.id.llEmptyState);
        cgFilters = findViewById(R.id.cgFilters);

        // Bind Start Squat Check Button
        findViewById(R.id.btnStartSquatCheck).setOnClickListener(v -> {
            Intent intent = new Intent(this, RealTimeFeedbackActivity.class);
            intent.putExtra("exercise_name", "Standard Squats");
            startActivity(intent);
        });

        // Setup RecyclerView
        rvHistoryList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryListAdapter(displayList, this::openSessionReport);
        rvHistoryList.setAdapter(adapter);

        // Set up filters
        cgFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chipAll) {
                currentFilter = "All";
            } else if (id == R.id.chipCorrect) {
                currentFilter = "Correct";
            } else if (id == R.id.chipNeedsWork) {
                currentFilter = "NeedsWork";
            }
            applyFilters();
        });

        // Load logs
        loadHistoryLogs();
    }

    private void loadHistoryLogs() {
        allLogs.clear();
        
        // Retrieve logs for both "Standard Squat" and "Standard Squats"
        List<WorkoutLog> logs1 = localDb.getWorkoutLogsForExercise("Standard Squat");
        List<WorkoutLog> logs2 = localDb.getWorkoutLogsForExercise("Standard Squats");

        // Use a list to hold combined logs unique by ID
        List<WorkoutLog> combined = new ArrayList<>();
        List<String> ids = new ArrayList<>();

        if (logs1 != null) {
            for (WorkoutLog l : logs1) {
                if (!ids.contains(l.getId())) {
                    combined.add(l);
                    ids.add(l.getId());
                }
            }
        }
        if (logs2 != null) {
            for (WorkoutLog l : logs2) {
                if (!ids.contains(l.getId())) {
                    combined.add(l);
                    ids.add(l.getId());
                }
            }
        }

        // Sort descending by timestamp
        combined.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        allLogs.addAll(combined);

        applyFilters();
    }

    private void applyFilters() {
        displayList.clear();

        for (WorkoutLog log : allLogs) {
            int score = parseFormScoreFromLog(log);
            if ("Correct".equals(currentFilter)) {
                if (score >= 80) displayList.add(log);
            } else if ("NeedsWork".equals(currentFilter)) {
                if (score < 80) displayList.add(log);
            } else {
                displayList.add(log); // All
            }
        }

        adapter.notifyDataSetChanged();

        if (displayList.isEmpty()) {
            llEmptyState.setVisibility(View.VISIBLE);
            rvHistoryList.setVisibility(View.GONE);
        } else {
            llEmptyState.setVisibility(View.GONE);
            rvHistoryList.setVisibility(View.VISIBLE);
        }
    }

    private int parseFormScoreFromLog(WorkoutLog log) {
        int score = 85; // default fallback
        String notes = log.getNotes();
        if (notes != null && !notes.trim().isEmpty()) {
            if (notes.startsWith("{")) {
                try {
                    JSONObject obj = new JSONObject(notes);
                    score = obj.optInt("average_form_score", score);
                } catch (Exception e) {
                    // Try to parse using raw string formats if not JSON
                }
            } else if (notes.contains("Score: ")) {
                try {
                    int idx = notes.indexOf("Score: ") + 7;
                    int endIdx = notes.indexOf("%", idx);
                    if (endIdx > idx) {
                        score = Integer.parseInt(notes.substring(idx, endIdx));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return score;
    }

    private void openSessionReport(WorkoutLog log) {
        int score = 85;
        int acc = 85;
        int dep = 90;
        int bal = 95;
        int stab = 94;
        int post = 80;
        long durationMs = 120000; // 2 minutes default

        String notes = log.getNotes();
        if (notes != null && notes.startsWith("{")) {
            try {
                JSONObject obj = new JSONObject(notes);
                score = obj.optInt("average_form_score", score);
                acc = obj.optInt("accuracy", acc);
                dep = obj.optInt("depth", dep);
                bal = obj.optInt("balance", bal);
                stab = obj.optInt("stability", stab);
                post = obj.optInt("posture", post);
                durationMs = obj.optLong("duration_ms", durationMs);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (notes != null && notes.contains("Score: ")) {
            score = parseFormScoreFromLog(log);
            // Parse other metrics if present
            if (notes.contains("Balance: ")) {
                try {
                    int bIdx = notes.indexOf("Balance: ") + 9;
                    int bEnd = notes.indexOf("%", bIdx);
                    bal = Integer.parseInt(notes.substring(bIdx, bEnd));
                } catch (Exception e) { e.printStackTrace(); }
            }
            if (notes.contains("Depth: ")) {
                try {
                    int dIdx = notes.indexOf("Depth: ") + 7;
                    int dEnd = notes.indexOf("%", dIdx);
                    dep = Integer.parseInt(notes.substring(dIdx, dEnd));
                } catch (Exception e) { e.printStackTrace(); }
            }
        }

        Intent intent = new Intent(this, WorkoutSummaryActivity.class);
        intent.putExtra("exercise_name", log.getExerciseName());
        intent.putExtra("total_reps", log.getReps());
        intent.putExtra("completed_sets", log.getSets());
        intent.putExtra("duration_ms", durationMs);
        intent.putExtra("average_form_score", score);
        intent.putExtra("accuracy", acc);
        intent.putExtra("depth", dep);
        intent.putExtra("balance", bal);
        intent.putExtra("stability", stab);
        intent.putExtra("posture", post);
        startActivity(intent);
    }

    private interface OnItemClickListener {
        void onItemClick(WorkoutLog log);
    }

    private static class HistoryListAdapter extends RecyclerView.Adapter<HistoryListAdapter.ViewHolder> {

        private final List<WorkoutLog> list;
        private final OnItemClickListener clickListener;
        private final SimpleDateFormat displayFormat = new SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault());

        HistoryListAdapter(List<WorkoutLog> list, OnItemClickListener clickListener) {
            this.list = list;
            this.clickListener = clickListener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_squat_history_row, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            WorkoutLog log = list.get(position);
            
            // Format Date
            String formattedDate = log.getDate();
            try {
                Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(log.getDate());
                if (date != null) {
                    // Try to display simple time formatting
                    long now = System.currentTimeMillis();
                    long diff = now - log.getTimestamp();
                    if (diff > 0 && diff < 86400000) {
                        formattedDate = "Today, " + new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date(log.getTimestamp()));
                    } else if (diff > 0 && diff < 172800000) {
                        formattedDate = "Yesterday, " + new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date(log.getTimestamp()));
                    } else {
                        formattedDate = displayFormat.format(new Date(log.getTimestamp()));
                    }
                }
            } catch (Exception e) {
                // Keep default
            }

            holder.tvHistoryDate.setText(formattedDate);
            holder.tvHistorySummary.setText(String.format(Locale.getDefault(), "%d Reps • %d Sets", log.getReps(), log.getSets()));

            // Parse Form Score
            int score = 85;
            String notes = log.getNotes();
            if (notes != null && notes.startsWith("{")) {
                try {
                    JSONObject obj = new JSONObject(notes);
                    score = obj.optInt("average_form_score", score);
                } catch (Exception e) { e.printStackTrace(); }
            } else if (notes != null && notes.contains("Score: ")) {
                try {
                    int idx = notes.indexOf("Score: ") + 7;
                    int endIdx = notes.indexOf("%", idx);
                    score = Integer.parseInt(notes.substring(idx, endIdx));
                } catch (Exception e) { e.printStackTrace(); }
            }

            holder.tvHistoryScore.setText(String.valueOf(score));

            // Dynamic styling based on score
            if (score >= 80) {
                holder.tvHistoryStatus.setText("CORRECT FORM");
                holder.tvHistoryStatus.setTextColor(0xFF22C55E); // Green
                holder.tvHistoryScore.setTextColor(0xFF22C55E);
                holder.flScoreIndicator.setBackgroundResource(R.drawable.bg_ring_green);
            } else if (score >= 60) {
                holder.tvHistoryStatus.setText("NEEDS WORK");
                holder.tvHistoryStatus.setTextColor(0xFFF59E0B); // Yellow
                holder.tvHistoryScore.setTextColor(0xFFF59E0B);
                holder.flScoreIndicator.setBackgroundResource(R.drawable.bg_ring_yellow);
            } else {
                holder.tvHistoryStatus.setText("NEEDS WORK");
                holder.tvHistoryStatus.setTextColor(0xFFEF4444); // Red
                holder.tvHistoryScore.setTextColor(0xFFEF4444);
                holder.flScoreIndicator.setBackgroundResource(R.drawable.bg_ring_red);
            }

            holder.itemView.setOnClickListener(v -> clickListener.onItemClick(log));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvHistoryDate, tvHistoryStatus, tvHistorySummary, tvHistoryScore;
            FrameLayout flScoreIndicator;

            ViewHolder(View itemView) {
                super(itemView);
                tvHistoryDate = itemView.findViewById(R.id.tvHistoryDate);
                tvHistoryStatus = itemView.findViewById(R.id.tvHistoryStatus);
                tvHistorySummary = itemView.findViewById(R.id.tvHistorySummary);
                tvHistoryScore = itemView.findViewById(R.id.tvHistoryScore);
                flScoreIndicator = itemView.findViewById(R.id.flScoreIndicator);
            }
        }
    }
}

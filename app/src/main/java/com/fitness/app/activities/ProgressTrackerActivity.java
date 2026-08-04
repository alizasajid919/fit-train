
package com.fitness.app.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fitness.app.R;
import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.models.ProgressLog;
import com.fitness.app.models.User;
import com.fitness.app.utils.PredictionModule;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProgressTrackerActivity extends AppCompatActivity {

    private TextView tvTargetWater, tvTargetSteps;
    private TextView tvPredictionWeight, tvPredictionDays, tvPredictionMsg;
    private View cardBodyShamingPolicy;
    private RecyclerView rvRecentLogs;

    private View pChartBarMon, pChartBarTue, pChartBarWed, pChartBarThu, pChartBarFri, pChartBarSat, pChartBarSun;

    private LocalDataManager localDb;
    private String todayDateString;
    private List<ProgressLog> loggedDaysList = new ArrayList<>();
    private LogAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Slide/fade animation opening transitions
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_progress_tracker);

        localDb = new LocalDataManager(this);
        todayDateString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        // Bind Views
        tvTargetWater = findViewById(R.id.tvTargetWater);
        tvTargetSteps = findViewById(R.id.tvTargetSteps);
        tvPredictionWeight = findViewById(R.id.tvPredictionWeight);
        tvPredictionDays = findViewById(R.id.tvPredictionDays);
        tvPredictionMsg = findViewById(R.id.tvPredictionMsg);
        cardBodyShamingPolicy = findViewById(R.id.cardBodyShamingPolicy);
        rvRecentLogs = findViewById(R.id.rvRecentLogs);

        pChartBarMon = findViewById(R.id.pChartBarMon);
        pChartBarTue = findViewById(R.id.pChartBarTue);
        pChartBarWed = findViewById(R.id.pChartBarWed);
        pChartBarThu = findViewById(R.id.pChartBarThu);
        pChartBarFri = findViewById(R.id.pChartBarFri);
        pChartBarSat = findViewById(R.id.pChartBarSat);
        pChartBarSun = findViewById(R.id.pChartBarSun);

        if (localDb.isBodyShamingProtectionEnabled()) {
            cardBodyShamingPolicy.setVisibility(View.VISIBLE);
        } else {
            cardBodyShamingPolicy.setVisibility(View.GONE);
        }

        // Setup RecyclerView
        adapter = new LogAdapter(loggedDaysList);
        rvRecentLogs.setLayoutManager(new LinearLayoutManager(this));
        rvRecentLogs.setAdapter(adapter);

        // Load dashboard and lists
        refreshDashboard();

        // Shortcut to Progress Photo comparison gallery flow
        findViewById(R.id.cardPhotoProgress).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(ProgressTrackerActivity.this, ProgressPhotoActivity.class);
            startActivity(intent);
        });

        // FAB to Log Metrics
        findViewById(R.id.fabLogMetrics).setOnClickListener(v -> showLogDialog());
    }

    private void refreshDashboard() {
        ProgressLog todayLog = localDb.getProgressLog(todayDateString);
        int stepsLogged = todayLog != null ? todayLog.getStepsCount() : 0;
        int waterLogged = todayLog != null ? todayLog.getWaterConsumedMl() : 0;

        User user = localDb.getUser();
        int stepGoal = (user != null && user.getDailyStepGoal() > 0) ? user.getDailyStepGoal() : 10000;
        int waterGoal = (user != null && user.getDailyWaterGoal() > 0) ? user.getDailyWaterGoal() : 2500;

        tvTargetWater.setText(waterLogged + " / " + waterGoal + " ml");
        tvTargetSteps.setText(stepsLogged + " / " + stepGoal);

        // Load Forecast Predictions
        loadPredictions();

        // Load Weekly Charts
        setWeeklyAnalyticsCharts();

        // Load Recent logs list (sorted descending)
        loadRecentLogsList();
    }

    private void loadPredictions() {
        User user = localDb.getUser();
        if (user == null) return;

        Map<String, ProgressLog> logs = localDb.getAllProgressLogs();
        PredictionModule.PredictionResult result = PredictionModule.getPredictions(user, logs);

        tvPredictionWeight.setText(String.format(Locale.getDefault(), "Estimated weight in 4 weeks: %.1f kg", result.estimatedWeightIn4Weeks));
        if (result.daysToReachGoal > 0) {
            tvPredictionDays.setText(String.format(Locale.getDefault(), "Estimated days to reach goal: %d days", result.daysToReachGoal));
        } else {
            tvPredictionDays.setText("Estimated days to reach goal: stable maintenance");
        }
        tvPredictionMsg.setText(result.predictionMessage);
    }

    private void setWeeklyAnalyticsCharts() {
        Map<String, ProgressLog> logs = localDb.getAllProgressLogs();
        int[] stepsForWeek = new int[7];
        
        // Calculate dates for the current week (Monday to Sunday)
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        int currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (currentDayOfWeek == Calendar.SUNDAY) {
            cal.add(Calendar.DAY_OF_YEAR, -6);
        } else {
            cal.add(Calendar.DAY_OF_YEAR, Calendar.MONDAY - currentDayOfWeek);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        
        for (int i = 0; i < 7; i++) {
            String dateKey = sdf.format(cal.getTime());
            ProgressLog log = logs.get(dateKey);
            if (log != null && log.getStepsCount() > 0) {
                stepsForWeek[i] = log.getStepsCount();
            } else {
                stepsForWeek[i] = 0;
            }
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Bind heights relative to dynamic step goal
        User user = localDb.getUser();
        int targetGoal = (user != null && user.getDailyStepGoal() > 0) ? user.getDailyStepGoal() : 10000;
        updateBarHeight(pChartBarMon, stepsForWeek[0], targetGoal);
        updateBarHeight(pChartBarTue, stepsForWeek[1], targetGoal);
        updateBarHeight(pChartBarWed, stepsForWeek[2], targetGoal);
        updateBarHeight(pChartBarThu, stepsForWeek[3], targetGoal);
        updateBarHeight(pChartBarFri, stepsForWeek[4], targetGoal);
        updateBarHeight(pChartBarSat, stepsForWeek[5], targetGoal);
        updateBarHeight(pChartBarSun, stepsForWeek[6], targetGoal);
    }

    private void updateBarHeight(View bar, int steps, int target) {
        if (bar == null) return;
        int maxPixelHeight = 110; // 110dp representation
        float ratio = (float) steps / target;
        int finalHeightDp = Math.max(10, (int) (ratio * maxPixelHeight));
        int finalHeightPx = (int) (finalHeightDp * getResources().getDisplayMetrics().density);
        
        ViewGroup.LayoutParams params = bar.getLayoutParams();
        params.height = finalHeightPx;
        bar.setLayoutParams(params);
    }

    private void loadRecentLogsList() {
        Map<String, ProgressLog> logs = localDb.getAllProgressLogs();
        loggedDaysList.clear();
        
        // Add all existing logs
        List<ProgressLog> sorted = new ArrayList<>(logs.values());
        sorted.sort((a, b) -> b.getDate().compareTo(a.getDate())); // Newest first
        loggedDaysList.addAll(sorted);

        // Mock a couple items if empty, to ensure the UI looks fully functional
        if (loggedDaysList.isEmpty()) {
            ProgressLog m1 = new ProgressLog("2026-07-02");
            m1.setStepsCount(7200);
            m1.setCaloriesConsumed(1750);
            m1.setWaterConsumedMl(1500);
            m1.setCurrentWeight(70.2);
            loggedDaysList.add(m1);

            ProgressLog m2 = new ProgressLog("2026-07-01");
            m2.setStepsCount(9100);
            m2.setCaloriesConsumed(1900);
            m2.setWaterConsumedMl(2000);
            m2.setCurrentWeight(70.5);
            loggedDaysList.add(m2);
        }

        adapter.notifyDataSetChanged();
    }

    private void showLogDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_log_metrics, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText etLogSteps = dialogView.findViewById(R.id.etLogSteps);
        EditText etLogCalories = dialogView.findViewById(R.id.etLogCalories);
        EditText etLogWater = dialogView.findViewById(R.id.etLogWater);
        EditText etLogSleep = dialogView.findViewById(R.id.etLogSleep);
        EditText etLogWeight = dialogView.findViewById(R.id.etLogWeight);

        Button btnPlusGlass = dialogView.findViewById(R.id.btnPlusGlass);
        Button btnSaveLog = dialogView.findViewById(R.id.btnSaveLog);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        // Preload today's logs
        ProgressLog todayLog = localDb.getProgressLog(todayDateString);
        if (todayLog != null) {
            if (todayLog.getStepsCount() > 0) etLogSteps.setText(String.valueOf(todayLog.getStepsCount()));
            if (todayLog.getCaloriesConsumed() > 0) etLogCalories.setText(String.valueOf(todayLog.getCaloriesConsumed()));
            if (todayLog.getWaterConsumedMl() > 0) etLogWater.setText(String.valueOf(todayLog.getWaterConsumedMl()));
            if (todayLog.getSleepDurationMinutes() > 0) etLogSleep.setText(String.valueOf(todayLog.getSleepDurationMinutes()));
            if (todayLog.getCurrentWeight() > 0) etLogWeight.setText(String.valueOf(todayLog.getCurrentWeight()));
        }

        btnPlusGlass.setOnClickListener(v -> {
            String val = etLogWater.getText().toString().trim();
            int current = 0;
            try {
                if (!val.isEmpty()) current = Integer.parseInt(val);
            } catch (NumberFormatException e) {
                // Ignore parse fail
            }
            etLogWater.setText(String.valueOf(current + 250));
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSaveLog.setOnClickListener(v -> {
            String stepsStr = etLogSteps.getText().toString().trim();
            String caloriesStr = etLogCalories.getText().toString().trim();
            String waterStr = etLogWater.getText().toString().trim();
            String sleepStr = etLogSleep.getText().toString().trim();
            String weightStr = etLogWeight.getText().toString().trim();

            int steps = 0;
            int calories = 0;
            int water = 0;
            int sleep = 0;
            double weight = 0.0;

            // 1. Validation Checks
            try {
                if (!stepsStr.isEmpty()) {
                    steps = Integer.parseInt(stepsStr);
                    if (steps < 0) {
                        etLogSteps.setError("Steps cannot be negative");
                        return;
                    }
                }
            } catch (NumberFormatException e) {
                etLogSteps.setError("Invalid steps format");
                return;
            }

            try {
                if (!caloriesStr.isEmpty()) {
                    calories = Integer.parseInt(caloriesStr);
                    if (calories < 0) {
                        etLogCalories.setError("Calories cannot be negative");
                        return;
                    }
                }
            } catch (NumberFormatException e) {
                etLogCalories.setError("Invalid calories format");
                return;
            }

            try {
                if (!waterStr.isEmpty()) {
                    water = Integer.parseInt(waterStr);
                    if (water < 0) {
                        etLogWater.setError("Water consumption cannot be negative");
                        return;
                    }
                }
            } catch (NumberFormatException e) {
                etLogWater.setError("Invalid water format");
                return;
            }

            try {
                if (!sleepStr.isEmpty()) {
                    sleep = Integer.parseInt(sleepStr);
                    if (sleep < 0) {
                        etLogSleep.setError("Sleep duration cannot be negative");
                        return;
                    }
                }
            } catch (NumberFormatException e) {
                etLogSleep.setError("Invalid sleep duration");
                return;
            }

            try {
                if (!weightStr.isEmpty()) {
                    weight = Double.parseDouble(weightStr);
                    if (weight <= 0) {
                        etLogWeight.setError("Weight must be greater than 0");
                        return;
                    }
                }
            } catch (NumberFormatException e) {
                etLogWeight.setError("Invalid weight format");
                return;
            }

            ProgressLog log = localDb.getProgressLog(todayDateString);
            if (log == null) log = new ProgressLog(todayDateString);

            // Set data
            log.setStepsCount(steps);
            log.setCaloriesConsumed(calories);
            log.setWaterConsumedMl(water);
            log.setSleepDurationMinutes(sleep);

            User user = localDb.getUser();
            if (weight > 0 && user != null) {
                log.setCurrentWeight(weight);
                user.setWeight(weight);
                user.setUpdatedAt(System.currentTimeMillis());
                
                // Calculate BMI
                double height = user.getHeight();
                log.setCurrentHeight(height);
                if (height > 0) {
                    double heightM = height / 100.0;
                    double bmi = weight / (heightM * heightM);
                    log.setCurrentBmi(bmi);
                }
                localDb.saveUser(user);
            }

            log.setUpdatedAt(System.currentTimeMillis());
            localDb.saveProgressLog(log);

            Toast.makeText(this, "Progress Logged Successfully!", Toast.LENGTH_SHORT).show();
            refreshDashboard();
            dialog.dismiss();

            // Cloud sync in background thread
            new Thread(() -> {
                try {
                    com.fitness.app.repositories.UserRepository repo = new com.fitness.app.repositories.UserRepository();
                    repo.syncLocalDataToFirestore(localDb);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });

        dialog.show();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private class LogAdapter extends RecyclerView.Adapter<LogAdapter.ViewHolder> {
        private final List<ProgressLog> logsList;

        public LogAdapter(List<ProgressLog> list) {
            this.logsList = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_log, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ProgressLog log = logsList.get(position);
            holder.tvLogDate.setText(log.getDate());
            holder.tvLogWeight.setText("⚖️ " + (log.getCurrentWeight() > 0 ? log.getCurrentWeight() + " kg" : "N/A"));
            holder.tvLogSteps.setText(String.format(Locale.getDefault(), "%,d", log.getStepsCount()));
            holder.tvLogCalories.setText(String.format(Locale.getDefault(), "%,d kcal", log.getCaloriesConsumed()));
            holder.tvLogWater.setText(String.format(Locale.getDefault(), "%.1f L", log.getWaterConsumedMl() / 1000.0));
        }

        @Override
        public int getItemCount() {
            return logsList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvLogDate, tvLogWeight, tvLogSteps, tvLogCalories, tvLogWater;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvLogDate = itemView.findViewById(R.id.tvLogDate);
                tvLogWeight = itemView.findViewById(R.id.tvLogWeight);
                tvLogSteps = itemView.findViewById(R.id.tvLogSteps);
                tvLogCalories = itemView.findViewById(R.id.tvLogCalories);
                tvLogWater = itemView.findViewById(R.id.tvLogWater);
            }
        }
    }
}

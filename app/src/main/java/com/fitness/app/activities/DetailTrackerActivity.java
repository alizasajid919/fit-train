package com.fitness.app.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.fitness.app.R;
import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.data.room.FitnessViewModel;
import com.fitness.app.models.ProgressLog;
import com.fitness.app.models.User;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DetailTrackerActivity extends AppCompatActivity {

    private String trackerType;
    private LocalDataManager localDb;
    private FitnessViewModel viewModel;
    private ProgressLog todayLog;
    private User user;

    private TextView tvProgressTitle, tvLargeStat, tvStatSubtitle, tvSuggestionsText, tvActionTitle;
    private View cardProgressCircle, cardQuickAction, cardBmiForm, layoutWaterActions, layoutDirectLog;
    private EditText etLogVal, etBmiHeight, etBmiWeight;
    private Button btnQuickAddWater, btnQuickAddWaterLarge, btnDirectLogSave, btnBmiCalculate;
    private ProgressBar pbCircleProgress;
    private ImageView ivCoachIllustration, ivBmiIllustration;
    private com.google.android.material.textfield.TextInputLayout tilLogVal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_tracker);

        trackerType = getIntent().getStringExtra("tracker_type");
        if (trackerType == null) trackerType = "steps";

        localDb = new LocalDataManager(this);
        viewModel = new ViewModelProvider(this).get(FitnessViewModel.class);
        user = localDb.getUser();
        if (user == null) {
            user = new User("guest_uid", "Guest", "User", "guest@fittrain.com", System.currentTimeMillis());
            user.setGender("Male");
            user.setHeight(175);
            user.setWeight(70);
            user.setGoal("Improve Shape");
            user.setProfileCompleted(true);
            localDb.saveUser(user);
        }

        loadTodayProgressLog();

        // Bind Views
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        tvProgressTitle = findViewById(R.id.tvProgressTitle);
        tvLargeStat = findViewById(R.id.tvLargeStat);
        tvStatSubtitle = findViewById(R.id.tvStatSubtitle);
        tvSuggestionsText = findViewById(R.id.tvSuggestionsText);
        tvActionTitle = findViewById(R.id.tvActionTitle);

        cardProgressCircle = findViewById(R.id.cardProgressCircle);
        cardQuickAction = findViewById(R.id.cardQuickAction);
        cardBmiForm = findViewById(R.id.cardBmiForm);
        layoutWaterActions = findViewById(R.id.layoutWaterActions);
        layoutDirectLog = findViewById(R.id.layoutDirectLog);

        etLogVal = findViewById(R.id.etLogVal);
        etBmiHeight = findViewById(R.id.etBmiHeight);
        etBmiWeight = findViewById(R.id.etBmiWeight);
        tilLogVal = findViewById(R.id.tilLogVal);

        btnQuickAddWater = findViewById(R.id.btnQuickAddWater);
        btnQuickAddWaterLarge = findViewById(R.id.btnQuickAddWaterLarge);
        btnDirectLogSave = findViewById(R.id.btnDirectLogSave);
        btnBmiCalculate = findViewById(R.id.btnBmiCalculate);

        pbCircleProgress = findViewById(R.id.pbCircleProgress);
        ivCoachIllustration = findViewById(R.id.ivCoachIllustration);
        ivBmiIllustration = findViewById(R.id.ivBmiIllustration);

        setupTrackerView();
        setupBarChart();
    }

    private void loadTodayProgressLog() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        todayLog = localDb.getProgressLog(today);
        if (todayLog == null) {
            todayLog = new ProgressLog(today);
            todayLog.setCurrentWeight(user.getWeight());
            todayLog.setCurrentHeight(user.getHeight());
            localDb.saveProgressLog(todayLog);
        }
    }

    private void setupTrackerView() {
        layoutWaterActions.setVisibility(View.GONE);
        layoutDirectLog.setVisibility(View.GONE);
        cardBmiForm.setVisibility(View.GONE);
        ivBmiIllustration.setVisibility(View.GONE);

        int stepGoal = user.getDailyStepGoal() > 0 ? user.getDailyStepGoal() : 10000;
        int waterGoal = user.getDailyWaterGoal() > 0 ? user.getDailyWaterGoal() : 2500;
        int calGoal = user.getDailyCaloriesGoal() > 0 ? user.getDailyCaloriesGoal() : 2000;
        int sleepGoal = user.getDailySleepGoal() > 0 ? user.getDailySleepGoal() : 480;

        switch (trackerType.toLowerCase()) {
            case "steps":
                if (getSupportActionBar() != null) getSupportActionBar().setTitle("Steps Tracker");
                tvProgressTitle.setText("Today's Steps");
                tvLargeStat.setText(String.format(Locale.getDefault(), "%,d", todayLog.getStepsCount()));

                int stepPercent = (int) (((double) todayLog.getStepsCount() / stepGoal) * 100);
                pbCircleProgress.setProgress(Math.min(stepPercent, 100));
                tvStatSubtitle.setText(String.format(Locale.getDefault(), "Goal: %,d steps (%d%% Completed)", stepGoal, stepPercent));
                tvStatSubtitle.setTextColor(Color.parseColor("#64748B"));

                if (stepPercent >= 85) {
                    tvSuggestionsText.setText("You've completed " + stepPercent + "% of today's goal. Keep walking! You are almost there!");
                } else {
                    int remaining = stepGoal - todayLog.getStepsCount();
                    tvSuggestionsText.setText("Excellent! Only " + String.format(Locale.getDefault(), "%,d", Math.max(0, remaining)) + " steps left to achieve today's goal. Brisk walking improves circulation.");
                }

                tvActionTitle.setText("Log Steps Manually");
                layoutDirectLog.setVisibility(View.VISIBLE);
                if (tilLogVal != null) {
                    tilLogVal.setHint("Enter Steps");
                    tilLogVal.setPlaceholderText("e.g. 5000");
                }
                ivCoachIllustration.setImageResource(R.drawable.onboarding_2);

                btnDirectLogSave.setOnClickListener(v -> {
                    String input = etLogVal.getText().toString().trim();
                    if (!input.isEmpty()) {
                        try {
                            int steps = Integer.parseInt(input);
                            if (steps < 0) {
                                Toast.makeText(this, "Steps count cannot be negative", Toast.LENGTH_SHORT).show();
                                  return;
                            }
                            todayLog.setStepsCount(steps);
                            localDb.saveProgressLog(todayLog);
                            viewModel.saveSteps(todayLog.getDate(), steps);

                            tvLargeStat.setText(String.format(Locale.getDefault(), "%,d", steps));
                            int newPercent = (int) (((double) steps / stepGoal) * 100);
                            pbCircleProgress.setProgress(Math.min(newPercent, 100));
                            tvStatSubtitle.setText(String.format(Locale.getDefault(), "Goal: %,d steps (%d%% Completed)", stepGoal, newPercent));

                            if (newPercent >= 85) {
                                tvSuggestionsText.setText("You've completed " + newPercent + "% of today's goal. Keep walking!");
                            } else {
                                int rem = stepGoal - steps;
                                tvSuggestionsText.setText("Excellent! Only " + String.format(Locale.getDefault(), "%,d", Math.max(0, rem)) + " steps left to achieve today's goal.");
                            }

                            etLogVal.setText("");
                            setupBarChart();
                            Toast.makeText(this, "Steps updated successfully!", Toast.LENGTH_SHORT).show();
                        } catch (NumberFormatException e) {
                            Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;

            case "calories":
                if (getSupportActionBar() != null) getSupportActionBar().setTitle("Calories Tracker");
                tvProgressTitle.setText("Calories Burned");
                tvLargeStat.setText(todayLog.getCaloriesBurned() + "\nkcal");

                int calPercent = (int) (((double) todayLog.getCaloriesBurned() / calGoal) * 100);
                pbCircleProgress.setProgress(Math.min(calPercent, 100));
                tvStatSubtitle.setText(String.format(Locale.getDefault(), "Goal: %,d kcal (%d%% Completed)", calGoal, calPercent));
                tvStatSubtitle.setTextColor(Color.parseColor("#64748B"));
                tvSuggestionsText.setText("Great job burning calories today! Keep focus on nutrition and stay hydrated to assist metabolic recovery.");

                tvActionTitle.setText("Log Calories Burned Manually");
                layoutDirectLog.setVisibility(View.VISIBLE);
                if (tilLogVal != null) {
                    tilLogVal.setHint("Enter Calories (kcal)");
                    tilLogVal.setPlaceholderText("e.g. 2000");
                }
                ivCoachIllustration.setImageResource(R.drawable.onboarding_1);

                btnDirectLogSave.setOnClickListener(v -> {
                    String input = etLogVal.getText().toString().trim();
                    if (!input.isEmpty()) {
                        try {
                            int calories = Integer.parseInt(input);
                            if (calories < 0) {
                                Toast.makeText(this, "Calories burned cannot be negative", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            todayLog.setCaloriesBurned(calories);
                            localDb.saveProgressLog(todayLog);
                            viewModel.saveCalories(todayLog.getDate(), calories);

                            tvLargeStat.setText(calories + "\nkcal");
                            int newPercent = (int) (((double) calories / calGoal) * 100);
                            pbCircleProgress.setProgress(Math.min(newPercent, 100));
                            tvStatSubtitle.setText(String.format(Locale.getDefault(), "Goal: %,d kcal (%d%% Completed)", calGoal, newPercent));

                            etLogVal.setText("");
                            setupBarChart();
                            Toast.makeText(this, "Calories updated successfully!", Toast.LENGTH_SHORT).show();
                        } catch (NumberFormatException e) {
                            Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;

            case "water":
                if (getSupportActionBar() != null) getSupportActionBar().setTitle("Water Tracker");
                tvProgressTitle.setText("Daily Hydration");
                tvLargeStat.setText(todayLog.getWaterConsumedMl() + "\nml");

                int waterPercent = (int) (((double) todayLog.getWaterConsumedMl() / waterGoal) * 100);
                pbCircleProgress.setProgress(Math.min(waterPercent, 100));
                tvStatSubtitle.setText(String.format(Locale.getDefault(), "Goal: %,d ml (%d%% Completed)", waterGoal, waterPercent));
                tvStatSubtitle.setTextColor(Color.parseColor("#64748B"));

                if (waterPercent >= 100) {
                    tvSuggestionsText.setText("Water goal achieved! Outstanding job staying hydrated today.");
                } else {
                    int remaining = waterGoal - todayLog.getWaterConsumedMl();
                    tvSuggestionsText.setText("You need another " + remaining + " ml of water to reach today's goal. Hydration supports performance.");
                }

                tvActionTitle.setText("Log Hydration");
                layoutWaterActions.setVisibility(View.VISIBLE);
                layoutDirectLog.setVisibility(View.VISIBLE);
                if (tilLogVal != null) {
                    tilLogVal.setHint("Enter Water Amount (ml)");
                    tilLogVal.setPlaceholderText("e.g. 250");
                }
                ivCoachIllustration.setImageResource(R.drawable.success_illustration);

                btnQuickAddWater.setOnClickListener(v -> addWater(250));
                btnQuickAddWaterLarge.setOnClickListener(v -> addWater(500));

                btnDirectLogSave.setOnClickListener(v -> {
                    String input = etLogVal.getText().toString().trim();
                    if (!input.isEmpty()) {
                        try {
                            int amount = Integer.parseInt(input);
                            if (amount < 0) {
                                Toast.makeText(this, "Water amount cannot be negative", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            addWater(amount);
                            etLogVal.setText("");
                        } catch (NumberFormatException e) {
                            Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;

            case "sleep":
                if (getSupportActionBar() != null) getSupportActionBar().setTitle("Sleep Tracker");
                tvProgressTitle.setText("Sleep Duration");
                int min = todayLog.getSleepDurationMinutes();
                tvLargeStat.setText(String.format(Locale.getDefault(), "%dh %dm", min / 60, min % 60));

                int sleepPercent = (int) (((double) min / sleepGoal) * 100);
                pbCircleProgress.setProgress(Math.min(sleepPercent, 100));
                tvStatSubtitle.setText(String.format(Locale.getDefault(), "Goal: %.1f hours (%d%% Completed)", sleepGoal / 60.0, sleepPercent));
                tvStatSubtitle.setTextColor(Color.parseColor("#64748B"));

                if (min >= sleepGoal) {
                    tvSuggestionsText.setText("Sleep goal achieved! You gave your body the recovery it deserves.");
                } else {
                    tvSuggestionsText.setText("You slept only " + (min / 60) + " hours. Aim for at least " + (sleepGoal / 60) + " hours tonight to assist protein synthesis.");
                }

                tvActionTitle.setText("Log Sleep Duration (Minutes)");
                layoutDirectLog.setVisibility(View.VISIBLE);
                etLogVal.setHint("Enter sleep minutes (e.g. 480)");
                ivCoachIllustration.setImageResource(R.drawable.onboarding_4);

                btnDirectLogSave.setOnClickListener(v -> {
                    String input = etLogVal.getText().toString().trim();
                    if (!input.isEmpty()) {
                        try {
                            int minutes = Integer.parseInt(input);
                            if (minutes < 0) {
                                Toast.makeText(this, "Sleep duration cannot be negative", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            todayLog.setSleepDurationMinutes(minutes);
                            localDb.saveProgressLog(todayLog);
                            viewModel.saveSleep(todayLog.getDate(), minutes);

                            tvLargeStat.setText(String.format(Locale.getDefault(), "%dh %dm", minutes / 60, minutes % 60));
                            int newPercent = (int) (((double) minutes / sleepGoal) * 100);
                            pbCircleProgress.setProgress(Math.min(newPercent, 100));
                            tvStatSubtitle.setText(String.format(Locale.getDefault(), "Goal: %.1f hours (%d%% Completed)", sleepGoal / 60.0, newPercent));

                            if (minutes >= sleepGoal) {
                                tvSuggestionsText.setText("Sleep goal achieved!");
                            } else {
                                tvSuggestionsText.setText("You slept only " + (minutes / 60) + " hours. Aim for at least " + (sleepGoal / 60) + " hours tonight.");
                            }

                            etLogVal.setText("");
                            setupBarChart();
                            Toast.makeText(this, "Sleep updated successfully!", Toast.LENGTH_SHORT).show();
                        } catch (NumberFormatException e) {
                            Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;

            case "bmi":
                if (getSupportActionBar() != null) getSupportActionBar().setTitle("BMI Details");
                tvProgressTitle.setText("Body Mass Index");
                cardQuickAction.setVisibility(View.GONE);
                cardBmiForm.setVisibility(View.VISIBLE);
                pbCircleProgress.setVisibility(View.GONE);
                ivBmiIllustration.setVisibility(View.VISIBLE);

                double weight = user.getWeight() > 0 ? user.getWeight() : todayLog.getCurrentWeight();
                double height = user.getHeight() > 0 ? user.getHeight() : todayLog.getCurrentHeight();

                TextView tvBmiHeightLabel = findViewById(R.id.tvBmiHeightLabel);
                TextView tvBmiWeightLabel = findViewById(R.id.tvBmiWeightLabel);
                boolean isMetric = localDb.isMetricUnitsEnabled();
                if (tvBmiHeightLabel != null) {
                    tvBmiHeightLabel.setText(isMetric ? "Height (cm)" : "Height (in)");
                }
                if (tvBmiWeightLabel != null) {
                    tvBmiWeightLabel.setText(isMetric ? "Weight (kg)" : "Weight (lbs)");
                }

                if (isMetric) {
                    etBmiHeight.setText(String.valueOf(height));
                    etBmiWeight.setText(String.valueOf(weight));
                } else {
                    double inches = height / 2.54;
                    double lbs = weight * 2.20462;
                    etBmiHeight.setText(String.format(Locale.US, "%.1f", inches));
                    etBmiWeight.setText(String.format(Locale.US, "%.1f", lbs));
                }

                updateBmiDisplay(weight, height);

                btnBmiCalculate.setOnClickListener(v -> {
                    String hInput = etBmiHeight.getText().toString().trim();
                    String wInput = etBmiWeight.getText().toString().trim();
                    if (!hInput.isEmpty() && !wInput.isEmpty()) {
                        try {
                            double newHeight = Double.parseDouble(hInput);
                            double newWeight = Double.parseDouble(wInput);
                            
                            boolean metric = localDb.isMetricUnitsEnabled();
                            double heightInCm = metric ? newHeight : newHeight * 2.54;
                            double weightInKg = metric ? newWeight : newWeight / 2.20462;

                            // Validate Input Ranges
                            if (heightInCm < 50 || heightInCm > 300) {
                                Toast.makeText(this, metric ? "Please enter a valid height between 50 and 300 cm" : "Please enter a valid height between 20 and 118 inches", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (weightInKg < 10 || weightInKg > 500) {
                                Toast.makeText(this, metric ? "Please enter a valid weight between 10 and 500 kg" : "Please enter a valid weight between 22 and 1100 lbs", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            btnBmiCalculate.setEnabled(false);
                            btnBmiCalculate.setText("Saving & Syncing...");
                            Toast.makeText(this, "Syncing with backend API...", Toast.LENGTH_SHORT).show();

                            // Simulate API sync latency
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                user.setHeight((int) heightInCm);
                                user.setWeight(weightInKg);
                                localDb.saveUser(user);

                                double hM = heightInCm / 100.0;
                                double bmi = weightInKg / (hM * hM);

                                todayLog.setCurrentHeight(heightInCm);
                                todayLog.setCurrentWeight(weightInKg);
                                todayLog.setCurrentBmi(bmi);
                                localDb.saveProgressLog(todayLog);

                                updateBmiDisplay(weightInKg, heightInCm);

                                btnBmiCalculate.setEnabled(true);
                                btnBmiCalculate.setText("Save & Recalculate");

                                setupBarChart();
                                Toast.makeText(DetailTrackerActivity.this, "BMI Successfully Synced!", Toast.LENGTH_SHORT).show();
                            }, 600);

                        } catch (NumberFormatException e) {
                            Toast.makeText(this, "Please enter valid values", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
        }
    }

    private void addWater(int amountMl) {
        int current = todayLog.getWaterConsumedMl();
        int newTotal = current + amountMl;
        todayLog.setWaterConsumedMl(newTotal);
        localDb.saveProgressLog(todayLog);
        viewModel.saveWater(todayLog.getDate(), newTotal);

        tvLargeStat.setText(newTotal + "\nml");
        int waterGoal = user.getDailyWaterGoal() > 0 ? user.getDailyWaterGoal() : 2500;
        int percent = (int) (((double) newTotal / waterGoal) * 100);
        pbCircleProgress.setProgress(Math.min(percent, 100));
        tvStatSubtitle.setText(String.format(Locale.getDefault(), "Goal: %,d ml (%d%% Completed)", waterGoal, percent));

        if (percent >= 100) {
            tvSuggestionsText.setText("Water goal achieved!");
        } else {
            tvSuggestionsText.setText("You need another " + (waterGoal - newTotal) + " ml of water to reach today's goal.");
        }

        setupBarChart();
        Toast.makeText(this, "+" + amountMl + " ml logged!", Toast.LENGTH_SHORT).show();
    }

    private void updateBmiDisplay(double weight, double height) {
        if (weight > 0 && height > 0) {
            double heightM = height / 100.0;
            double bmi = weight / (heightM * heightM);
            String category = getBmiCategory(bmi);

            tvLargeStat.setText(String.format(Locale.getDefault(), "%.1f", bmi));
            tvStatSubtitle.setText("Category: " + category);
            tvSuggestionsText.setText(getBmiSuggestions(category));

            // Set Category color dynamically
            if (category.equals("Underweight")) {
                tvStatSubtitle.setTextColor(Color.parseColor("#0284C7"));
            } else if (category.equals("Normal Weight")) {
                tvStatSubtitle.setTextColor(Color.parseColor("#22C55E"));
            } else if (category.equals("Overweight")) {
                tvStatSubtitle.setTextColor(Color.parseColor("#EA580C"));
            } else {
                tvStatSubtitle.setTextColor(Color.parseColor("#DC2626"));
            }
        } else {
            tvLargeStat.setText("N/A");
            tvStatSubtitle.setText("Please enter values below");
            tvStatSubtitle.setTextColor(Color.parseColor("#64748B"));
            tvSuggestionsText.setText("Enter your height and weight to calculate your Body Mass Index (BMI).");
        }
    }

    private String getBmiCategory(double bmi) {
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25) return "Normal Weight";
        if (bmi < 30) return "Overweight";
        return "Obese";
    }

    private String getBmiSuggestions(String category) {
        switch (category) {
            case "Underweight":
                return "Focus on a nutrient-dense, high-protein surplus diet and compound strength training to build healthy muscle mass safely.";
            case "Normal Weight":
                return "Excellent! You are in a healthy BMI range. Focus on maintaining muscle mass, hydration, and progressive overload in training.";
            case "Overweight":
                return "Incorporate a mild caloric deficit alongside compound resistance lifts and steady-state cardiovascular training to optimize weight loss.";
            default:
                return "Aim for consistent cardiovascular exercises, hydration, and consult a nutritionist to formulate a structured deficit diet plan.";
        }
    }

    private void setupBarChart() {
        BarChart barChart = findViewById(R.id.weeklyBarChart);
        if (barChart == null) return;

        barChart.getDescription().setEnabled(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setMaxVisibleValueCount(10);
        barChart.setPinchZoom(false);
        barChart.setDrawGridBackground(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(0xFF64748B);
        xAxis.setTextSize(11f);

        final String[] days = new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        xAxis.setValueFormatter(new IndexAxisValueFormatter(days));

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(0xFFE2E8F0);
        leftAxis.setTextColor(0xFF64748B);
        leftAxis.setAxisMinimum(0f);

        // Customize YAxis Maximum for BMI
        if (trackerType.equalsIgnoreCase("bmi")) {
            leftAxis.setAxisMaximum(40f);
        } else {
            leftAxis.resetAxisMaximum();
        }

        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(false);

        List<BarEntry> entries = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        for (int i = 0; i < 7; i++) {
            String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
            ProgressLog log = localDb.getProgressLog(dateStr);
            float val = 0f;
            if (trackerType.equalsIgnoreCase("steps")) {
                val = log.getStepsCount();
            } else if (trackerType.equalsIgnoreCase("calories")) {
                val = log.getCaloriesBurned();
            } else if (trackerType.equalsIgnoreCase("water")) {
                val = log.getWaterConsumedMl();
            } else if (trackerType.equalsIgnoreCase("sleep")) {
                val = log.getSleepDurationMinutes() / 60.0f; // hours
            } else if (trackerType.equalsIgnoreCase("bmi")) {
                val = (float) log.getCurrentBmi();
                if (val <= 0) {
                    double w = log.getCurrentWeight();
                    double h = log.getCurrentHeight();
                    if (w > 0 && h > 0) {
                        double hM = h / 100.0;
                        val = (float) (w / (hM * hM));
                    }
                }
            }

            if (val <= 0) {
                if (trackerType.equalsIgnoreCase("bmi")) {
                    double w = user.getWeight();
                    double h = user.getHeight();
                    if (w > 0 && h > 0) {
                        double hM = h / 100.0;
                        val = (float) (w / (hM * hM));
                    }
                } else {
                    val = 0f;
                }
            }

            entries.add(new BarEntry(i, val));
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        BarDataSet dataSet = new BarDataSet(entries, trackerType);
        dataSet.setColor(0xFF6C63FF); // Brand Primary Color
        dataSet.setValueTextColor(0xFF1E293B);
        dataSet.setValueTextSize(10f);

        // Custom formatting for chart value tags
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getBarLabel(BarEntry barEntry) {
                if (trackerType.equalsIgnoreCase("bmi")) {
                    return String.format(Locale.getDefault(), "%.1f", barEntry.getY());
                } else if (trackerType.equalsIgnoreCase("sleep")) {
                    return String.format(Locale.getDefault(), "%.1f", barEntry.getY());
                } else {
                    return String.format(Locale.getDefault(), "%.0f", barEntry.getY());
                }
            }
        });

        BarData data = new BarData(dataSet);
        barChart.setData(data);
        barChart.animateY(800);
        barChart.invalidate();
    }
}

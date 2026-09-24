package com.fitness.app.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CalendarContract;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fitness.app.R;
import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.models.ProgressLog;
import com.fitness.app.models.Reminder;
import com.fitness.app.models.WorkoutLog;
import com.fitness.app.repositories.UserRepository;
import com.fitness.app.views.SuccessCheckmarkView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;

import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class LogWorkoutActivity extends AppCompatActivity {

    private NestedScrollView scrollView;
    private TextInputLayout tilExerciseName, tilSets, tilReps, tilWeight;
    private EditText etExerciseName, etSets, etReps, etWeight, etNotes;
    private TextView tvNotesCharCount;

    // Collapsible History Section
    private View btnToggleHistory;
    private ImageView ivHistoryArrow;
    private LinearLayout layoutHistoryBody, layoutEmptyHistory;
    private RecyclerView rvWorkoutHistory;
    private HistoryAdapter historyAdapter;
    private boolean isHistoryExpanded = false;

    // Calendar Selection
    private SwitchMaterial switchCalendar;
    private LinearLayout layoutDateSelection;
    private View btnSelectDate;
    private TextView tvSelectedDate;
    private String selectedWorkoutDate; // yyyy-MM-dd

    // Save Button & Loading State
    private MaterialButton btnSaveWorkout;
    private ProgressBar pbSavingProgress;
    private SuccessCheckmarkView successCheckmark;

    private LocalDataManager localDb;
    private final List<WorkoutLog> historyList = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());

    private int selectedHour = 9;
    private int selectedMinute = 0;
    private int selectedYear, selectedMonth, selectedDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Slide/fade animation opening transitions
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_log_workout);

        localDb = new LocalDataManager(this);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        }

        // Bind Views
        scrollView = findViewById(R.id.scrollView);
        tilExerciseName = findViewById(R.id.tilExerciseName);
        tilSets = findViewById(R.id.tilSets);
        tilReps = findViewById(R.id.tilReps);
        tilWeight = findViewById(R.id.tilWeight);
        
        tilWeight.setHint(localDb.isMetricUnitsEnabled() ? "Weight (kg)" : "Weight (lbs)");

        etExerciseName = findViewById(R.id.etExerciseName);
        etSets = findViewById(R.id.etSets);
        etReps = findViewById(R.id.etReps);
        etWeight = findViewById(R.id.etWeight);
        etNotes = findViewById(R.id.etNotes);
        tvNotesCharCount = findViewById(R.id.tvNotesCharCount);

        // Collapsible History
        btnToggleHistory = findViewById(R.id.btnToggleHistory);
        ivHistoryArrow = findViewById(R.id.ivHistoryArrow);
        layoutHistoryBody = findViewById(R.id.layoutHistoryBody);
        layoutEmptyHistory = findViewById(R.id.layoutEmptyHistory);
        rvWorkoutHistory = findViewById(R.id.rvWorkoutHistory);

        // Calendar
        switchCalendar = findViewById(R.id.switchCalendar);
        layoutDateSelection = findViewById(R.id.layoutDateSelection);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);

        // Actions
        btnSaveWorkout = findViewById(R.id.btnSaveWorkout);
        pbSavingProgress = findViewById(R.id.pbSavingProgress);
        successCheckmark = findViewById(R.id.successCheckmark);

        // Featured Header view bindings
        View cvFeaturedHeader = findViewById(R.id.cvFeaturedHeader);
        TextView tvFeaturedName = findViewById(R.id.tvFeaturedName);
        TextView tvFeaturedStats = findViewById(R.id.tvFeaturedStats);
        TextView tvFeaturedDesc = findViewById(R.id.tvFeaturedDesc);
        ImageView ivFeaturedIllustration = findViewById(R.id.ivFeaturedIllustration);
        TextView tvFormTitle = findViewById(R.id.tvFormTitle);

        // Initial Values & Configuration
        selectedWorkoutDate = dateFormat.format(new Date());
        tvSelectedDate.setText(displayDateFormat.format(new Date()));
        etNotes.setFilters(new InputFilter[]{new InputFilter.LengthFilter(200)});

        // Calendar variables default setup
        Calendar now = Calendar.getInstance();
        selectedYear = now.get(Calendar.YEAR);
        selectedMonth = now.get(Calendar.MONTH);
        selectedDay = now.get(Calendar.DAY_OF_MONTH);

        setupHistoryRecyclerView();
        setupListeners();

        // Handle prefilled featured workout config
        boolean isFeatured = getIntent().getBooleanExtra("is_featured", false);
        if (isFeatured) {
            cvFeaturedHeader.setVisibility(View.VISIBLE);

            String featuredName = getIntent().getStringExtra("exercise_name");
            String featuredStats = getIntent().getStringExtra("featured_stats");
            String featuredDesc = getIntent().getStringExtra("featured_desc");
            int illustrationRes = getIntent().getIntExtra("illustration_res", R.drawable.onboarding_1);

            if (featuredName != null) tvFeaturedName.setText(featuredName);
            if (featuredStats != null) tvFeaturedStats.setText(featuredStats);
            if (featuredDesc != null) tvFeaturedDesc.setText(featuredDesc);
            ivFeaturedIllustration.setImageResource(illustrationRes);

            tvFormTitle.setText(R.string.log_reps_weights_title);

            // Pre-fill fields to valid starting baselines for premium user experience
            if (featuredName != null) {
                etExerciseName.setText(featuredName);
                etExerciseName.setEnabled(false);
                tilExerciseName.setEnabled(false);
            }
            etSets.setText("3");
            etReps.setText("12");
            etWeight.setText(localDb.isMetricUnitsEnabled() ? "20" : "45");
        } else {
            String prefilledExercise = getIntent().getStringExtra("exercise_name");
            if (prefilledExercise != null && !prefilledExercise.trim().isEmpty()) {
                etExerciseName.setText(prefilledExercise);
            }
        }

        checkFormValidity();
    }

    private void setupHistoryRecyclerView() {
        historyAdapter = new HistoryAdapter(historyList, log -> {
            etExerciseName.setText(log.getExerciseName());
            etSets.setText(String.valueOf(log.getSets()));
            etReps.setText(String.valueOf(log.getReps()));
            
            double displayedWeight = localDb.isMetricUnitsEnabled() ? log.getWeight() : (log.getWeight() * 2.20462);
            etWeight.setText(String.format(Locale.US, "%.1f", displayedWeight));
            if (log.getNotes() != null && !log.getNotes().isEmpty()) {
                etNotes.setText(log.getNotes());
            } else {
                etNotes.setText("");
            }
            
            clearValidationErrors();
            hideKeyboard();
            Toast.makeText(this, "Auto-filled with previous workout data!", Toast.LENGTH_SHORT).show();
            scrollView.smoothScrollTo(0, 0);
        });
        
        rvWorkoutHistory.setLayoutManager(new LinearLayoutManager(this));
        rvWorkoutHistory.setAdapter(historyAdapter);
    }

    private void setupListeners() {
        etExerciseName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateHistoryList(s.toString());
                checkFormValidity();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etExerciseName.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && etExerciseName.getText().length() > 0 && !isHistoryExpanded) {
                toggleHistory(true);
            }
        });

        TextWatcher requiredFieldsWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkFormValidity();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };
        etSets.addTextChangedListener(requiredFieldsWatcher);
        etReps.addTextChangedListener(requiredFieldsWatcher);

        etNotes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = s.length();
                tvNotesCharCount.setText(String.format(Locale.getDefault(), "%d / 200", length));
                if (length >= 200) {
                    tvNotesCharCount.setTextColor(ContextCompat.getColor(LogWorkoutActivity.this, R.color.workout_accent));
                } else {
                    tvNotesCharCount.setTextColor(ContextCompat.getColor(LogWorkoutActivity.this, R.color.workout_text_secondary));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnToggleHistory.setOnClickListener(v -> toggleHistory(!isHistoryExpanded));

        switchCalendar.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                layoutDateSelection.setVisibility(View.VISIBLE);
                showDatePicker(); // Trigger scheduling flow immediately on toggle
            } else {
                layoutDateSelection.setVisibility(View.GONE);
                selectedWorkoutDate = dateFormat.format(new Date());
                tvSelectedDate.setText(displayDateFormat.format(new Date()));
            }
        });

        btnSelectDate.setOnClickListener(v -> showDatePicker());

        btnSaveWorkout.setOnClickListener(v -> saveWorkout());
    }

    private void toggleHistory(boolean expand) {
        isHistoryExpanded = expand;
        if (expand) {
            layoutHistoryBody.setVisibility(View.VISIBLE);
            ivHistoryArrow.setRotation(90f);
        } else {
            layoutHistoryBody.setVisibility(View.GONE);
            ivHistoryArrow.setRotation(0f);
        }
    }

    private void updateHistoryList(String exerciseName) {
        historyList.clear();
        if (exerciseName != null && !exerciseName.trim().isEmpty()) {
            List<WorkoutLog> matches = localDb.getWorkoutLogsForExercise(exerciseName);
            historyList.addAll(matches);
        }

        historyAdapter.notifyDataSetChanged();

        if (historyList.isEmpty()) {
            layoutEmptyHistory.setVisibility(View.VISIBLE);
            rvWorkoutHistory.setVisibility(View.GONE);
        } else {
            layoutEmptyHistory.setVisibility(View.GONE);
            rvWorkoutHistory.setVisibility(View.VISIBLE);
        }
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog picker = new DatePickerDialog(this,
            (view, year, month, dayOfMonth) -> {
                selectedYear = year;
                selectedMonth = month;
                selectedDay = dayOfMonth;

                Calendar selectedCal = Calendar.getInstance();
                selectedCal.set(Calendar.YEAR, year);
                selectedCal.set(Calendar.MONTH, month);
                selectedCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                selectedWorkoutDate = dateFormat.format(selectedCal.getTime());
                tvSelectedDate.setText(displayDateFormat.format(selectedCal.getTime()));

                // Open Time Picker next
                showTimePicker();
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        );
        picker.show();
    }

    private void showTimePicker() {
        Calendar cal = Calendar.getInstance();
        TimePickerDialog picker = new TimePickerDialog(this,
            (view, hourOfDay, minute) -> {
                selectedHour = hourOfDay;
                selectedMinute = minute;

                Calendar selectedCal = Calendar.getInstance();
                selectedCal.set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute);
                
                SimpleDateFormat displayWithTime = new SimpleDateFormat("MMMM d, yyyy 'at' hh:mm a", Locale.getDefault());
                tvSelectedDate.setText(displayWithTime.format(selectedCal.getTime()));

                Toast.makeText(this, "Schedule saved for " + displayWithTime.format(selectedCal.getTime()), Toast.LENGTH_LONG).show();

                // Save offline Reminder entry
                String title = etExerciseName.getText().toString().trim();
                if (title.isEmpty()) title = "Workout Session";
                Reminder reminder = new Reminder(
                    UUID.randomUUID().toString(),
                    "WORKOUT",
                    selectedHour,
                    selectedMinute,
                    true,
                    "Time for your scheduled " + title + "!"
                );
                List<Reminder> reminders = localDb.getReminders();
                reminders.add(reminder);
                localDb.saveReminders(reminders);

                // Launch calendar insertion intent
                insertSystemCalendarEvent(title, selectedCal);
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            false
        );
        picker.show();
    }

    private void insertSystemCalendarEvent(String title, Calendar time) {
        try {
            Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, time.getTimeInMillis())
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, time.getTimeInMillis() + 30 * 60 * 1000)
                .putExtra(CalendarContract.Events.TITLE, "FitTrain: " + title)
                .putExtra(CalendarContract.Events.DESCRIPTION, "Get active and stay consistent with FitTrain!")
                .putExtra(CalendarContract.Events.EVENT_LOCATION, "FitTrain App")
                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "System Calendar App unavailable. Scheduled locally.", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkFormValidity() {
        String name = etExerciseName.getText().toString().trim();
        String setsStr = etSets.getText().toString().trim();
        String repsStr = etReps.getText().toString().trim();

        boolean isValid = !name.isEmpty() && !setsStr.isEmpty() && !repsStr.isEmpty();

        try {
            if (isValid) {
                int sets = Integer.parseInt(setsStr);
                int reps = Integer.parseInt(repsStr);
                isValid = sets > 0 && reps > 0;
            }
        } catch (NumberFormatException e) {
            isValid = false;
        }

        if (isValid) {
            btnSaveWorkout.setEnabled(true);
            btnSaveWorkout.setClickable(true);
            btnSaveWorkout.setAlpha(1.0f);
        } else {
            btnSaveWorkout.setEnabled(false);
            btnSaveWorkout.setClickable(false);
            btnSaveWorkout.setAlpha(0.6f);
        }
    }

    private void clearValidationErrors() {
        tilExerciseName.setError(null);
        tilSets.setError(null);
        tilReps.setError(null);
    }

    private void saveWorkout() {
        hideKeyboard();

        String name = etExerciseName.getText().toString().trim();
        String setsStr = etSets.getText().toString().trim();
        String repsStr = etReps.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        TextInputLayout errorLayout = null;
        View errorView = null;

        if (name.isEmpty()) {
            tilExerciseName.setError("Exercise Name is required");
            errorLayout = tilExerciseName;
            errorView = etExerciseName;
        } else {
            tilExerciseName.setError(null);
        }

        if (setsStr.isEmpty()) {
            tilSets.setError("Sets is required");
            if (errorLayout == null) {
                errorLayout = tilSets;
                errorView = etSets;
            }
        } else {
            try {
                int sets = Integer.parseInt(setsStr);
                if (sets <= 0) {
                    tilSets.setError("Sets must be greater than 0");
                    if (errorLayout == null) {
                        errorLayout = tilSets;
                        errorView = etSets;
                    }
                } else {
                    tilSets.setError(null);
                }
            } catch (NumberFormatException e) {
                tilSets.setError("Invalid number");
                if (errorLayout == null) {
                    errorLayout = tilSets;
                    errorView = etSets;
                }
            }
        }

        if (repsStr.isEmpty()) {
            tilReps.setError("Reps is required");
            if (errorLayout == null) {
                errorLayout = tilReps;
                errorView = etReps;
            }
        } else {
            try {
                int reps = Integer.parseInt(repsStr);
                if (reps <= 0) {
                    tilReps.setError("Reps must be greater than 0");
                    if (errorLayout == null) {
                        errorLayout = tilReps;
                        errorView = etReps;
                    }
                } else {
                    tilReps.setError(null);
                }
            } catch (NumberFormatException e) {
                tilReps.setError("Invalid number");
                if (errorLayout == null) {
                    errorLayout = tilReps;
                    errorView = etReps;
                }
            }
        }

        if (errorLayout != null) {
            final View viewToScroll = errorLayout;
            scrollView.post(() -> {
                Rect rect = new Rect();
                viewToScroll.getHitRect(rect);
                scrollView.smoothScrollTo(0, rect.top - 50);
            });
            errorView.requestFocus();
            return;
        }

        btnSaveWorkout.setText("");
        pbSavingProgress.setVisibility(View.VISIBLE);
        btnSaveWorkout.setEnabled(false);

        double weight = 0.0;
        if (!weightStr.isEmpty()) {
            try {
                double inputWeight = Double.parseDouble(weightStr);
                weight = localDb.isMetricUnitsEnabled() ? inputWeight : (inputWeight / 2.20462);
            } catch (NumberFormatException e) {
                // Keep 0.0
            }
        }

        int sets = Integer.parseInt(setsStr);
        int reps = Integer.parseInt(repsStr);

        final WorkoutLog log = new WorkoutLog(
            UUID.randomUUID().toString(),
            name,
            sets,
            reps,
            weight,
            notes,
            selectedWorkoutDate,
            System.currentTimeMillis()
        );

        // Save local log
        localDb.saveWorkoutLog(log);

        // Save Single Source of Truth WorkoutSession
        com.fitness.app.models.WorkoutSession session = new com.fitness.app.models.WorkoutSession(name, "Manual Log", "Intermediate");
        session.setSessionId(log.getId());
        session.setDateStr(selectedWorkoutDate);

        java.util.List<com.fitness.app.models.WorkoutExerciseItem> items = new java.util.ArrayList<>();
        com.fitness.app.models.WorkoutExerciseItem item = new com.fitness.app.models.WorkoutExerciseItem(name, "Target Muscles", sets, reps, 0);
        item.setCompletedSets(sets);
        item.setActualCompletedReps(reps);
        item.setStatus(com.fitness.app.models.WorkoutExerciseItem.Status.COMPLETED);
        items.add(item);
        session.setExercises(items);

        double weightKg = 70.0;
        com.fitness.app.models.User user = localDb.getUser();
        if (user != null && user.getWeight() > 0) {
            weightKg = localDb.isMetricUnitsEnabled() ? user.getWeight() : (user.getWeight() / 2.20462);
        }
        session.finalizeSessionMetrics(weightKg);
        com.fitness.app.utils.AiWorkoutEngine.generateAnalysisAndRecommendations(session, localDb);
        localDb.saveWorkoutSession(session);

        localDb.incrementStreak();

        // Automatically update Daily Progress & Weekly Calories Burned inside ProgressLog
        try {
            ProgressLog progressLog = localDb.getProgressLog(selectedWorkoutDate);
            if (progressLog == null) {
                progressLog = new ProgressLog(selectedWorkoutDate);
            }
            int calBurned = getIntent().getIntExtra("calories_kcal", 0);
            if (calBurned <= 0) {
                double met = 5.0;
                String lowerName = name.toLowerCase();
                if (lowerName.contains("squat") || lowerName.contains("leg")) met = 5.5;
                else if (lowerName.contains("run") || lowerName.contains("cardio") || lowerName.contains("hiit") || lowerName.contains("jump")) met = 8.0;
                else if (lowerName.contains("pushup") || lowerName.contains("chest") || lowerName.contains("arm")) met = 4.5;
                else if (lowerName.contains("deadlift") || lowerName.contains("strength") || lowerName.contains("press")) met = 6.0;

                double durationMins = (sets * reps * 4.0) / 60.0;
                if (durationMins < 1.0) durationMins = 1.0;
                calBurned = (int) Math.round((met * 3.5 * weightKg / 200.0) * durationMins);
                if (calBurned < 10) calBurned = 15;
            }
            progressLog.setCaloriesBurned(progressLog.getCaloriesBurned() + calBurned);
            localDb.saveProgressLog(progressLog);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Firestore sync in background thread
        new Thread(() -> {
            try {
                UserRepository repo = new UserRepository();
                repo.syncLocalDataToFirestore(localDb);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        new Handler().postDelayed(() -> {
            pbSavingProgress.setVisibility(View.GONE);
            successCheckmark.setVisibility(View.VISIBLE);

            successCheckmark.startAnimation(() -> {
                Snackbar.make(scrollView, "Workout Saved Successfully", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(ContextCompat.getColor(LogWorkoutActivity.this, R.color.workout_success))
                    .setTextColor(ContextCompat.getColor(LogWorkoutActivity.this, R.color.workout_white))
                    .show();

                clearForm();

                new Handler().postDelayed(this::finish, 1000);
            });
        }, 800);
    }

    private void clearForm() {
        etExerciseName.setText("");
        etSets.setText("");
        etReps.setText("");
        etWeight.setText("");
        etNotes.setText("");
        tvNotesCharCount.setText("0 / 200");
        clearValidationErrors();
        switchCalendar.setChecked(false);
        checkFormValidity();
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    hideKeyboard();
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private static class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

        public interface OnItemClickListener {
            void onItemClick(WorkoutLog log);
        }

        private final List<WorkoutLog> logs;
        private final OnItemClickListener listener;
        private final SimpleDateFormat listDateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

        public HistoryAdapter(List<WorkoutLog> logs, OnItemClickListener listener) {
            this.logs = logs;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workout_history, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            WorkoutLog log = logs.get(position);
            
            String dateStr = log.getDate();
            try {
                Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(log.getDate());
                if (d != null) {
                    dateStr = listDateFormat.format(d);
                }
            } catch (Exception e) {
                // Keep default
            }

            holder.tvHistoryDate.setText(dateStr);
            holder.tvHistoryStats.setText(log.getSets() + " Sets x " + log.getReps() + " Reps");
            boolean isMetric = new com.fitness.app.data.local.LocalDataManager(holder.itemView.getContext()).isMetricUnitsEnabled();
            if (isMetric) {
                holder.tvHistoryWeight.setText("• " + (int) log.getWeight() + " kg");
            } else {
                holder.tvHistoryWeight.setText("• " + (int) Math.round(log.getWeight() * 2.20462) + " lbs");
            }

            holder.itemView.setOnClickListener(v -> listener.onItemClick(log));
        }

        @Override
        public int getItemCount() {
            return logs.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvHistoryDate, tvHistoryStats, tvHistoryWeight;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvHistoryDate = itemView.findViewById(R.id.tvHistoryDate);
                tvHistoryStats = itemView.findViewById(R.id.tvHistoryStats);
                tvHistoryWeight = itemView.findViewById(R.id.tvHistoryWeight);
            }
        }
    }
}

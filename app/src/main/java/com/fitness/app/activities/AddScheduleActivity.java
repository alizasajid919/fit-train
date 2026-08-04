package com.fitness.app.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fitness.app.R;
import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.models.WorkoutSchedule;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

public class AddScheduleActivity extends AppCompatActivity {

    private AutoCompleteTextView actvWorkoutSelect, actvTimeSelect;
    private TextInputEditText etCustomWorkoutName, etScheduleDate, etScheduleDuration, etScheduleNotes;

    private LocalDataManager localDb;
    private String selectedDate; // yyyy-MM-dd

    private final String[] workoutPresets = {
            "HIIT Cardio Blast",
            "Full Body Shred",
            "Core Crusher",
            "Yoga Flow & Stretch",
            "Lower Body Power",
            "Upper Body Pump",
            "Custom (Enter Below)"
    };

    private final String[] timeSlots = {
            "06:00 AM", "07:00 AM", "08:00 AM", "09:00 AM", "10:00 AM", "11:00 AM",
            "12:00 PM", "01:00 PM", "02:00 PM", "03:00 PM", "04:00 PM", "05:00 PM",
            "06:00 PM", "07:00 PM", "08:00 PM", "09:00 PM", "10:00 PM"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Slide/fade animation opening transitions
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_add_schedule);

        localDb = new LocalDataManager(this);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        // Bind Views
        actvWorkoutSelect = findViewById(R.id.actvWorkoutSelect);
        actvTimeSelect = findViewById(R.id.actvTimeSelect);
        etCustomWorkoutName = findViewById(R.id.etCustomWorkoutName);
        etScheduleDate = findViewById(R.id.etScheduleDate);
        etScheduleDuration = findViewById(R.id.etScheduleDuration);
        etScheduleNotes = findViewById(R.id.etScheduleNotes);

        // Prepopulate presets
        ArrayAdapter<String> workoutAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, workoutPresets);
        actvWorkoutSelect.setAdapter(workoutAdapter);

        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, timeSlots);
        actvTimeSelect.setAdapter(timeAdapter);

        // Initial setup from Intent extras
        String presetDate = getIntent().getStringExtra("selected_date");
        selectedDate = presetDate != null ? presetDate : new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        etScheduleDate.setText(selectedDate);

        String presetTime = getIntent().getStringExtra("selected_time");
        if (presetTime != null) {
            actvTimeSelect.setText(presetTime, false);
        } else {
            actvTimeSelect.setText("09:00 AM", false);
        }

        // Setup Date Click Picker
        etScheduleDate.setOnClickListener(v -> showDatePicker());

        // Buttons
        findViewById(R.id.btnCancel).setOnClickListener(v -> onBackPressed());
        findViewById(R.id.btnSaveSchedule).setOnClickListener(v -> saveSchedule());
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Schedule Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            selectedDate = sdf.format(new Date(selection));
            etScheduleDate.setText(selectedDate);
        });

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private void saveSchedule() {
        String selectedChoice = actvWorkoutSelect.getText().toString().trim();
        String customName = etCustomWorkoutName.getText().toString().trim();
        String timeChoice = actvTimeSelect.getText().toString().trim();
        String durationStr = etScheduleDuration.getText().toString().trim();
        String notesStr = etScheduleNotes.getText().toString().trim();

        String finalWorkoutName = selectedChoice;
        if (selectedChoice.isEmpty() || "Custom (Enter Below)".equalsIgnoreCase(selectedChoice)) {
            finalWorkoutName = customName;
        }

        if (finalWorkoutName.isEmpty()) {
            Toast.makeText(this, "Please choose or enter a workout name", Toast.LENGTH_SHORT).show();
            return;
        }

        int duration = 30;
        if (!durationStr.isEmpty()) {
            try {
                duration = Integer.parseInt(durationStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid duration", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Create schedule item
        WorkoutSchedule s = new WorkoutSchedule(
                UUID.randomUUID().toString(),
                finalWorkoutName,
                selectedDate,
                timeChoice,
                duration,
                notesStr,
                false
        );

        localDb.saveWorkoutSchedule(s);
        Toast.makeText(this, "Workout Scheduled Successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}

package com.fitness.app.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fitness.app.databinding.ActivityProgressSettingsBinding;
import com.fitness.app.utils.ReminderScheduler;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ProgressSettingsActivity extends AppCompatActivity {

    private ActivityProgressSettingsBinding binding;
    private SharedPreferences sharedPrefs;
    private final String[] frequencies = {"Daily", "Weekly", "Every 2 Weeks", "Monthly", "Custom Interval"};

    private int reminderHour = 9;
    private int reminderMinute = 0;
    private int reminderDay = 1;
    private int reminderMonth = 0; // Calendar.JANUARY is 0
    private int reminderYear = 2026;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProgressSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPrefs = getSharedPreferences("ProgressTrackerPrefs", MODE_PRIVATE);

        // Setup Toolbar back
        binding.btnBack.setOnClickListener(v -> onBackPressed());

        // Setup Spinner Adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, frequencies);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerFrequency.setAdapter(adapter);

        binding.spinnerFrequency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 4) {
                    binding.layoutCustomInterval.setVisibility(View.VISIBLE);
                } else {
                    binding.layoutCustomInterval.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Time picker click
        binding.tvReminderTime.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                reminderHour = hourOfDay;
                reminderMinute = minute;
                updateTimeDisplay();
            }, reminderHour, reminderMinute, false);
            timePickerDialog.show();
        });

        // Date picker click
        binding.tvReminderDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                reminderYear = year;
                reminderMonth = month;
                reminderDay = dayOfMonth;
                updateDateDisplay();
            }, reminderYear, reminderMonth, reminderDay);
            datePickerDialog.show();
        });

        // Load Cached Settings
        loadSettings();

        // Save Click
        binding.btnSave.setOnClickListener(v -> {
            saveSettings();
            Toast.makeText(this, "Settings saved successfully", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void updateTimeDisplay() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, reminderHour);
        cal.set(Calendar.MINUTE, reminderMinute);
        SimpleDateFormat fmt = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        binding.tvReminderTime.setText(fmt.format(cal.getTime()));
    }

    private void updateDateDisplay() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, reminderYear);
        cal.set(Calendar.MONTH, reminderMonth);
        cal.set(Calendar.DAY_OF_MONTH, reminderDay);
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        binding.tvReminderDate.setText(fmt.format(cal.getTime()));
    }

    private void loadSettings() {
        boolean remindersEnabled = sharedPrefs.getBoolean("reminder_enabled", true);
        int frequencyIndex = sharedPrefs.getInt("reminder_frequency_index", 3); // Default: Monthly (index 3 now)
        boolean saveToGallery = sharedPrefs.getBoolean("save_to_gallery", false);
        int qualityId = sharedPrefs.getInt("photo_quality_id", 0);
        boolean privacyLock = sharedPrefs.getBoolean("privacy_lock", false);

        reminderHour = sharedPrefs.getInt("reminder_time_hour", 9);
        reminderMinute = sharedPrefs.getInt("reminder_time_minute", 0);
        
        Calendar now = Calendar.getInstance();
        reminderDay = sharedPrefs.getInt("reminder_date_day", now.get(Calendar.DAY_OF_MONTH));
        reminderMonth = sharedPrefs.getInt("reminder_date_month", now.get(Calendar.MONTH));
        reminderYear = sharedPrefs.getInt("reminder_date_year", now.get(Calendar.YEAR));

        int customInterval = sharedPrefs.getInt("reminder_custom_interval_days", 30);

        binding.switchReminders.setChecked(remindersEnabled);
        binding.spinnerFrequency.setSelection(frequencyIndex);
        binding.switchSaveToGallery.setChecked(saveToGallery);
        binding.etCustomInterval.setText(String.valueOf(customInterval));

        if (qualityId == 0) {
            binding.rbHighQuality.setChecked(true);
        } else {
            binding.rbCompressed.setChecked(true);
        }

        binding.switchPrivacyLock.setChecked(privacyLock);

        updateTimeDisplay();
        updateDateDisplay();
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        
        boolean enabled = binding.switchReminders.isChecked();
        int frequencyIndex = binding.spinnerFrequency.getSelectedItemPosition();
        
        editor.putBoolean("reminder_enabled", enabled);
        editor.putInt("reminder_frequency_index", frequencyIndex);
        editor.putBoolean("save_to_gallery", binding.switchSaveToGallery.isChecked());
        
        int qualityId = binding.rbHighQuality.isChecked() ? 0 : 1;
        editor.putInt("photo_quality_id", qualityId);
        
        editor.putBoolean("privacy_lock", binding.switchPrivacyLock.isChecked());
        
        editor.putInt("reminder_time_hour", reminderHour);
        editor.putInt("reminder_time_minute", reminderMinute);
        editor.putInt("reminder_date_day", reminderDay);
        editor.putInt("reminder_date_month", reminderMonth);
        editor.putInt("reminder_date_year", reminderYear);

        int customInterval = 30;
        try {
            customInterval = Integer.parseInt(binding.etCustomInterval.getText().toString().trim());
        } catch (Exception e) {
            // ignore
        }
        editor.putInt("reminder_custom_interval_days", customInterval);
        
        if (enabled) {
            editor.putBoolean("photo_reminder_dismissed", false);
        }
        
        editor.apply();

        // Schedule notification alarm via AlarmManager
        ReminderScheduler.schedulePhotoReminder(this, enabled, frequencyIndex, reminderHour, reminderMinute, customInterval);
    }
}

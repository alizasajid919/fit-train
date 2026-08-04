package com.fitness.app.activities;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fitness.app.R;
import com.fitness.app.data.local.LocalDataManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class AddSleepAlarmActivity extends AppCompatActivity {

    private EditText etAlarmTitle;
    private TextView tvBedtimeValue, tvWaketimeValue;
    private Button btnSaveAlarm;
    private ImageButton btnDeleteAlarm;
    private TextView tvHeaderTitle;

    private int bedHour = 22, bedMinute = 30;
    private int wakeHour = 6, wakeMinute = 30;

    private boolean[] repeatDays = new boolean[7]; // Mon=0, Tue=1, Wed=2, Thu=3, Fri=4, Sat=5, Sun=6
    private TextView[] dayViews = new TextView[7];
    private int[] dayViewIds = {R.id.tvDayMon, R.id.tvDayTue, R.id.tvDayWed, R.id.tvDayThu, R.id.tvDayFri, R.id.tvDaySat, R.id.tvDaySun};

    private LocalDataManager localDb;
    private String alarmId = null;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_add_sleep_alarm);

        localDb = new LocalDataManager(this);

        etAlarmTitle = findViewById(R.id.etAlarmTitle);
        tvBedtimeValue = findViewById(R.id.tvBedtimeValue);
        tvWaketimeValue = findViewById(R.id.tvWaketimeValue);
        btnSaveAlarm = findViewById(R.id.btnSaveAlarm);
        btnDeleteAlarm = findViewById(R.id.btnDeleteAlarm);
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());

        // Setup Day Views
        for (int i = 0; i < 7; i++) {
            dayViews[i] = findViewById(dayViewIds[i]);
            final int index = i;
            dayViews[i].setOnClickListener(v -> {
                repeatDays[index] = !repeatDays[index];
                updateDayViewStyle(index);
            });
            updateDayViewStyle(i);
        }

        // Bedtime Click
        findViewById(R.id.layoutBedtimePicker).setOnClickListener(v -> {
            TimePickerDialog picker = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    bedHour = hourOfDay;
                    bedMinute = minute;
                    tvBedtimeValue.setText(formatTime(bedHour, bedMinute));
                }, bedHour, bedMinute, false);
            picker.show();
        });

        // Wake Time Click
        findViewById(R.id.layoutWaketimePicker).setOnClickListener(v -> {
            TimePickerDialog picker = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    wakeHour = hourOfDay;
                    wakeMinute = minute;
                    tvWaketimeValue.setText(formatTime(wakeHour, wakeMinute));
                }, wakeHour, wakeMinute, false);
            picker.show();
        });

        // Check if editing
        alarmId = getIntent().getStringExtra("alarm_id");
        if (alarmId != null) {
            isEditMode = true;
            tvHeaderTitle.setText("Edit Schedule / Alarm");
            btnSaveAlarm.setText("Save Changes");
            btnDeleteAlarm.setVisibility(View.VISIBLE);

            etAlarmTitle.setText(getIntent().getStringExtra("alarm_title"));
            String bedStr = getIntent().getStringExtra("alarm_bedtime");
            String wakeStr = getIntent().getStringExtra("alarm_waketime");
            tvBedtimeValue.setText(bedStr);
            tvWaketimeValue.setText(wakeStr);

            parseTime(bedStr, true);
            parseTime(wakeStr, false);

            String existingDays = getIntent().getStringExtra("alarm_repeat");
            if (existingDays != null) {
                if (existingDays.equals("Everyday")) {
                    java.util.Arrays.fill(repeatDays, true);
                } else if (existingDays.equals("Weekdays")) {
                    for (int i = 0; i < 5; i++) repeatDays[i] = true;
                } else if (existingDays.equals("Weekends")) {
                    repeatDays[5] = true;
                    repeatDays[6] = true;
                } else if (!existingDays.equals("Once")) {
                    String[] parts = existingDays.split(", ");
                    String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
                    for (String p : parts) {
                        for (int i = 0; i < 7; i++) {
                            if (dayNames[i].equals(p)) {
                                repeatDays[i] = true;
                            }
                        }
                    }
                }
                for (int i = 0; i < 7; i++) {
                    updateDayViewStyle(i);
                }
            }
        }

        btnSaveAlarm.setOnClickListener(v -> saveAlarm());
        btnDeleteAlarm.setOnClickListener(v -> deleteAlarm());
    }

    private void updateDayViewStyle(int index) {
        if (repeatDays[index]) {
            dayViews[index].setBackgroundResource(R.drawable.bg_day_circle_selected);
            dayViews[index].setTextColor(0xFFFFFFFF);
        } else {
            dayViews[index].setBackgroundResource(R.drawable.bg_day_circle_unselected);
            dayViews[index].setTextColor(0xFF64748B);
        }
    }

    private String getRepeatDaysString() {
        int count = 0;
        for (boolean b : repeatDays) {
            if (b) count++;
        }
        if (count == 7) return "Everyday";
        if (count == 0) return "Once";
        if (count == 5 && repeatDays[0] && repeatDays[1] && repeatDays[2] && repeatDays[3] && repeatDays[4]) return "Weekdays";
        if (count == 2 && repeatDays[5] && repeatDays[6]) return "Weekends";

        StringBuilder sb = new StringBuilder();
        String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (int i = 0; i < 7; i++) {
            if (repeatDays[i]) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(dayNames[i]);
            }
        }
        return sb.toString();
    }

    private void parseTime(String timeStr, boolean isBedtime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            java.util.Date date = sdf.parse(timeStr);
            if (date != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                if (isBedtime) {
                    bedHour = cal.get(Calendar.HOUR_OF_DAY);
                    bedMinute = cal.get(Calendar.MINUTE);
                } else {
                    wakeHour = cal.get(Calendar.HOUR_OF_DAY);
                    wakeMinute = cal.get(Calendar.MINUTE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String formatTime(int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.getTime());
    }

    private void saveAlarm() {
        String title = etAlarmTitle.getText().toString().trim();
        if (title.isEmpty()) {
            etAlarmTitle.setError("Schedule Title is required");
            etAlarmTitle.requestFocus();
            return;
        }

        if (bedHour == wakeHour && bedMinute == wakeMinute) {
            Toast.makeText(this, "Warning: Bedtime and Wake-up time cannot be the same!", Toast.LENGTH_LONG).show();
            return;
        }

        final String finalTitle = title;
        final String bedtimeStr = formatTime(bedHour, bedMinute);
        final String wakeTimeStr = formatTime(wakeHour, wakeMinute);
        final String repeatStr = getRepeatDaysString();

        Toast.makeText(this, "Syncing with backend API...", Toast.LENGTH_SHORT).show();

        // Simulate network API Latency
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            String key = "sleep_alarms_list";
            String saved = localDb.sharedPreferences.getString(key, "[]");
            try {
                org.json.JSONArray arr = new org.json.JSONArray(saved);
                String nowStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                if (isEditMode) {
                    // Update existing
                    for (int i = 0; i < arr.length(); i++) {
                        org.json.JSONObject obj = arr.getJSONObject(i);
                        if (obj.getString("id").equals(alarmId)) {
                            obj.put("title", finalTitle);
                            obj.put("bedtime", bedtimeStr);
                            obj.put("wakeTime", wakeTimeStr);
                            obj.put("repeat", repeatStr);
                            obj.put("lastUpdatedDate", nowStr);
                            break;
                        }
                    }
                } else {
                    // Create new
                    org.json.JSONObject obj = new org.json.JSONObject();
                    alarmId = UUID.randomUUID().toString();
                    obj.put("id", alarmId);
                    obj.put("title", finalTitle);
                    obj.put("bedtime", bedtimeStr);
                    obj.put("wakeTime", wakeTimeStr);
                    obj.put("repeat", repeatStr);
                    obj.put("enabled", true);
                    obj.put("createdDate", nowStr);
                    obj.put("lastUpdatedDate", nowStr);
                    arr.put(obj);
                }

                localDb.editor.putString(key, arr.toString()).apply();

                // Schedule local notifications/reminders
                scheduleNotifications(alarmId, finalTitle, bedHour, bedMinute, wakeHour, wakeMinute);

                Toast.makeText(AddSleepAlarmActivity.this, "Sleep Schedule Synced!", Toast.LENGTH_SHORT).show();
                finish();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(AddSleepAlarmActivity.this, "Failed to sync schedule", Toast.LENGTH_SHORT).show();
            }
        }, 600);
    }

    private void deleteAlarm() {
        Toast.makeText(this, "Deleting alarm on backend...", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            String key = "sleep_alarms_list";
            String saved = localDb.sharedPreferences.getString(key, "[]");
            try {
                org.json.JSONArray arr = new org.json.JSONArray(saved);
                org.json.JSONArray newArr = new org.json.JSONArray();
                for (int i = 0; i < arr.length(); i++) {
                    org.json.JSONObject obj = arr.getJSONObject(i);
                    if (!obj.getString("id").equals(alarmId)) {
                        newArr.put(obj);
                    }
                }

                localDb.editor.putString(key, newArr.toString()).apply();

                // Cancel scheduled alarms
                cancelNotifications(alarmId);

                Toast.makeText(AddSleepAlarmActivity.this, "Alarm Deleted!", Toast.LENGTH_SHORT).show();
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 500);
    }

    private void scheduleNotifications(String alarmId, String title, int bedH, int bedM, int wakeH, int wakeM) {
        // Cancel first to prevent duplicates
        cancelNotifications(alarmId);

        // 1. Bedtime schedule
        String bedtimeStr = formatTime(bedH, bedM);
        com.fitness.app.utils.SleepAlarmScheduler.scheduleSleepAlarm(this, alarmId, title, bedtimeStr, "SLEEP", repeatDays, true);

        // 2. Wake-up schedule
        String waketimeStr = formatTime(wakeH, wakeM);
        com.fitness.app.utils.SleepAlarmScheduler.scheduleSleepAlarm(this, alarmId, title, waketimeStr, "WAKEUP", repeatDays, true);
    }

    private void cancelNotifications(String alarmId) {
        com.fitness.app.utils.SleepAlarmScheduler.cancelSleepAlarm(this, alarmId, "SLEEP");
        com.fitness.app.utils.SleepAlarmScheduler.cancelSleepAlarm(this, alarmId, "WAKEUP");
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}

package com.fitness.app.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fitness.app.R;
import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.data.room.AppDatabase;
import com.fitness.app.data.room.SleepLogs;
import com.fitness.app.models.ProgressLog;
import com.fitness.app.models.User;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.switchmaterial.SwitchMaterial;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import androidx.appcompat.app.AlertDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SleepTrackerActivity extends AppCompatActivity {

    private LineChart sleepLineChart;
    private TextView tvSleepQuality, tvSleepDurationHours;
    private ProgressBar pbSleepDuration;
    private RecyclerView rvSleepSchedules;

    private LocalDataManager localDb;
    private AppDatabase roomDb;
    private List<SleepScheduleItem> scheduleList = new ArrayList<>();
    private ScheduleAdapter adapter;

    public static class SleepScheduleItem {
        public String id;
        public String title;
        public String bedtime;
        public String wakeTime;
        public boolean enabled;
        public String repeat;

        public SleepScheduleItem(String id, String title, String bedtime, String wakeTime, boolean enabled, String repeat) {
            this.id = id;
            this.title = title;
            this.bedtime = bedtime;
            this.wakeTime = wakeTime;
            this.enabled = enabled;
            this.repeat = repeat;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_sleep_tracker);

        localDb = new LocalDataManager(this);
        roomDb = AppDatabase.getInstance(this);

        // Bind Views
        sleepLineChart = findViewById(R.id.sleepLineChart);
        tvSleepQuality = findViewById(R.id.tvSleepQuality);
        tvSleepDurationHours = findViewById(R.id.tvSleepDurationHours);
        pbSleepDuration = findViewById(R.id.pbSleepDuration);
        rvSleepSchedules = findViewById(R.id.rvSleepSchedules);

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());
        findViewById(R.id.btnAddAlarm).setOnClickListener(v -> {
            startActivity(new Intent(SleepTrackerActivity.this, AddSleepAlarmActivity.class));
        });

        setupSchedulesList();
        checkPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSleepData();
        setupSleepChart();
        loadSchedules();
    }

    private void loadSleepData() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        
        // 1. Fetch from Room Database
        SleepLogs roomLog = roomDb.fitnessDao().getSleepForDate(today);
        int sleepMin = 0;
        
        if (roomLog != null && roomLog.minutes > 0) {
            sleepMin = roomLog.minutes;
        } else {
            // Check SharedPreferences cache
            ProgressLog todayLog = localDb.getProgressLog(today);
            if (todayLog != null && todayLog.getSleepDurationMinutes() > 0) {
                sleepMin = todayLog.getSleepDurationMinutes();
            }
        }

        User user = localDb.getUser();
        int sleepGoal = (user != null && user.getDailySleepGoal() > 0) ? user.getDailySleepGoal() : 480;

        int hours = sleepMin / 60;
        int minutes = sleepMin % 60;

        tvSleepDurationHours.setText(String.format(Locale.getDefault(), "%dh %dm", hours, minutes));
        pbSleepDuration.setMax(sleepGoal);
        pbSleepDuration.setProgress(Math.min(sleepMin, sleepGoal));

        // Sleep score and quality calculation
        int score = sleepGoal > 0 ? (sleepMin * 100) / sleepGoal : 0;
        if (score > 100) score = 100;

        if (score >= 90) {
            tvSleepQuality.setText(String.format(Locale.getDefault(), "Excellent Sleep (%d%%)", score));
        } else if (score >= 75) {
            tvSleepQuality.setText(String.format(Locale.getDefault(), "Good Sleep (%d%%)", score));
        } else if (score >= 60) {
            tvSleepQuality.setText(String.format(Locale.getDefault(), "Fair Sleep (%d%%)", score));
        } else {
            tvSleepQuality.setText(String.format(Locale.getDefault(), "Insufficient Sleep (%d%%)", score));
        }
    }

    private void setupSleepChart() {
        sleepLineChart.getDescription().setEnabled(false);
        sleepLineChart.setDrawGridBackground(false);
        sleepLineChart.setPinchZoom(false);
        sleepLineChart.getLegend().setEnabled(false);

        XAxis xAxis = sleepLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(0xFF64748B);

        final String[] days = new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        xAxis.setValueFormatter(new IndexAxisValueFormatter(days));

        YAxis leftAxis = sleepLineChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(0xFFE2E8F0);
        leftAxis.setTextColor(0xFF64748B);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(12f); // max 12 hours

        sleepLineChart.getAxisRight().setEnabled(false);

        // Fetch last 7 days of sleep statistics
        List<Entry> entries = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -6); // Go back 6 days

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (int i = 0; i < 7; i++) {
            String dateStr = sdf.format(cal.getTime());
            SleepLogs log = roomDb.fitnessDao().getSleepForDate(dateStr);
            float hoursValue = 0f;
            if (log != null && log.minutes > 0) {
                hoursValue = log.minutes / 60.0f;
            } else {
                ProgressLog progressLog = localDb.getProgressLog(dateStr);
                if (progressLog != null && progressLog.getSleepDurationMinutes() > 0) {
                    hoursValue = progressLog.getSleepDurationMinutes() / 60.0f;
                }
            }
            entries.add(new Entry(i, hoursValue));
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        LineDataSet dataSet = new LineDataSet(entries, "Sleep duration");
        dataSet.setColor(0xFF6C63FF);
        dataSet.setLineWidth(3f);
        dataSet.setCircleColor(0xFF6C63FF);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawCircleHole(false);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);
        sleepLineChart.setData(lineData);
        sleepLineChart.animateX(800);
        sleepLineChart.invalidate();
    }

    private void setupSchedulesList() {
        rvSleepSchedules.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ScheduleAdapter();
        rvSleepSchedules.setAdapter(adapter);
    }

    private void loadSchedules() {
        scheduleList.clear();

        String key = "sleep_alarms_list";
        String saved = localDb.sharedPreferences.getString(key, null);
        if (saved == null) {
            // Default active schedules
            scheduleList.add(new SleepScheduleItem("1", "Daily Rest", "10:30 PM", "06:30 AM", true, "Everyday"));
            scheduleList.add(new SleepScheduleItem("2", "Weekend Alarm", "11:30 PM", "08:30 AM", false, "Weekends"));
            saveSchedulesToPrefs();
        } else {
            try {
                org.json.JSONArray arr = new org.json.JSONArray(saved);
                for (int i = 0; i < arr.length(); i++) {
                    org.json.JSONObject obj = arr.getJSONObject(i);
                    scheduleList.add(new SleepScheduleItem(
                        obj.getString("id"),
                        obj.getString("title"),
                        obj.getString("bedtime"),
                        obj.getString("wakeTime"),
                        obj.getBoolean("enabled"),
                        obj.optString("repeat", "Once")
                    ));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void saveSchedulesToPrefs() {
        try {
            org.json.JSONArray arr = new org.json.JSONArray();
            for (SleepScheduleItem item : scheduleList) {
                org.json.JSONObject obj = new org.json.JSONObject();
                obj.put("id", item.id);
                obj.put("title", item.title);
                obj.put("bedtime", item.bedtime);
                obj.put("wakeTime", item.wakeTime);
                obj.put("enabled", item.enabled);
                obj.put("repeat", item.repeat);
                arr.put(obj);
            }
            localDb.editor.putString("sleep_alarms_list", arr.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void scheduleNotificationsFor(SleepScheduleItem item) {
        boolean[] repeatDays = com.fitness.app.utils.SleepAlarmScheduler.parseRepeatDays(item.repeat);
        com.fitness.app.utils.SleepAlarmScheduler.scheduleSleepAlarm(this, item.id, item.title, item.bedtime, "SLEEP", repeatDays, true);
        com.fitness.app.utils.SleepAlarmScheduler.scheduleSleepAlarm(this, item.id, item.title, item.wakeTime, "WAKEUP", repeatDays, true);
    }

    private void cancelNotificationsFor(String alarmId) {
        com.fitness.app.utils.SleepAlarmScheduler.cancelSleepAlarm(this, alarmId, "SLEEP");
        com.fitness.app.utils.SleepAlarmScheduler.cancelSleepAlarm(this, alarmId, "WAKEUP");
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    // Schedule Adapter
    private class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sleep_schedule, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            SleepScheduleItem item = scheduleList.get(position);
            holder.tvTitle.setText(item.title);
            holder.tvTimes.setText(String.format("Bedtime: %s • Wake up: %s\nRepeat: %s", item.bedtime, item.wakeTime, item.repeat));
            
            // Unbind listener before setting value to prevent recursive triggers
            holder.swEnable.setOnCheckedChangeListener(null);
            holder.swEnable.setChecked(item.enabled);

            holder.swEnable.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.enabled = isChecked;
                saveSchedulesToPrefs();
                if (isChecked) {
                    scheduleNotificationsFor(item);
                    Toast.makeText(SleepTrackerActivity.this, "Alarm enabled", Toast.LENGTH_SHORT).show();
                } else {
                    cancelNotificationsFor(item.id);
                    Toast.makeText(SleepTrackerActivity.this, "Alarm disabled", Toast.LENGTH_SHORT).show();
                }
            });

            // Handle edit click
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(SleepTrackerActivity.this, AddSleepAlarmActivity.class);
                intent.putExtra("alarm_id", item.id);
                intent.putExtra("alarm_title", item.title);
                intent.putExtra("alarm_bedtime", item.bedtime);
                intent.putExtra("alarm_waketime", item.wakeTime);
                intent.putExtra("alarm_repeat", item.repeat);
                intent.putExtra("alarm_enabled", item.enabled);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return scheduleList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvTimes;
            SwitchMaterial swEnable;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvTimes = itemView.findViewById(R.id.tvTimes);
                swEnable = itemView.findViewById(R.id.swEnable);
            }
        }
    }

    private void checkPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            android.app.AlarmManager alarmManager = (android.app.AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                new AlertDialog.Builder(this)
                    .setTitle("Exact Alarm Permission Required")
                    .setMessage("FitTrain requires the Exact Alarm permission to ring your sleep alarms precisely on time. Please enable it in the system settings.")
                    .setPositiveButton("Open Settings", (dialog, which) -> {
                        Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            }
        }
    }
}


package com.fitness.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fitness.app.R;
import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.models.WorkoutSchedule;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class

WorkoutScheduleActivity extends AppCompatActivity {

    private RecyclerView rvScheduleSlots;
    private View llNoSchedules;

    private LocalDataManager localDb;
    private String selectedDate; // yyyy-MM-dd
    private final String[] datesOfWeek = new String[7];
    private final TextView[] tvDayVals = new TextView[7];
    private final View[] layoutDays = new View[7];

    private final List<String> hoursList = new ArrayList<>();
    private List<WorkoutSchedule> todaySchedulesList = new ArrayList<>();
    private ScheduleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Slide/fade animation opening transitions
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_workout_schedule);

        localDb = new LocalDataManager(this);
        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        // Bind Hourly Slots
        rvScheduleSlots = findViewById(R.id.rvScheduleSlots);
        llNoSchedules = findViewById(R.id.llNoSchedules);

        setupHoursList();
        setupCalendarDays();

        // Setup RecyclerView slots
        adapter = new ScheduleAdapter();
        rvScheduleSlots.setLayoutManager(new LinearLayoutManager(this));
        rvScheduleSlots.setAdapter(adapter);

        refreshSchedules();

        // FAB to Add Schedule
        findViewById(R.id.fabAddSchedule).setOnClickListener(v -> {
            Intent intent = new Intent(WorkoutScheduleActivity.this, AddScheduleActivity.class);
            intent.putExtra("selected_date", selectedDate);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshSchedules();
    }

    private void setupHoursList() {
        hoursList.clear();
        hoursList.add("06:00 AM");
        hoursList.add("07:00 AM");
        hoursList.add("08:00 AM");
        hoursList.add("09:00 AM");
        hoursList.add("10:00 AM");
        hoursList.add("11:00 AM");
        hoursList.add("12:00 PM");
        hoursList.add("01:00 PM");
        hoursList.add("02:00 PM");
        hoursList.add("03:00 PM");
        hoursList.add("04:00 PM");
        hoursList.add("05:00 PM");
        hoursList.add("06:00 PM");
        hoursList.add("07:00 PM");
        hoursList.add("08:00 PM");
        hoursList.add("09:00 PM");
        hoursList.add("10:00 PM");
    }

    private void setupCalendarDays() {
        // Find View Bindings
        layoutDays[0] = findViewById(R.id.daySun);
        layoutDays[1] = findViewById(R.id.dayMon);
        layoutDays[2] = findViewById(R.id.dayTue);
        layoutDays[3] = findViewById(R.id.dayWed);
        layoutDays[4] = findViewById(R.id.dayThu);
        layoutDays[5] = findViewById(R.id.dayFri);
        layoutDays[6] = findViewById(R.id.daySat);

        tvDayVals[0] = findViewById(R.id.tvDayValSun);
        tvDayVals[1] = findViewById(R.id.tvDayValMon);
        tvDayVals[2] = findViewById(R.id.tvDayValTue);
        tvDayVals[3] = findViewById(R.id.tvDayValWed);
        tvDayVals[4] = findViewById(R.id.tvDayValThu);
        tvDayVals[5] = findViewById(R.id.tvDayValFri);
        tvDayVals[6] = findViewById(R.id.tvDayValSat);

        // Calculate dynamic dates for the current calendar week
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        SimpleDateFormat sdfDayVal = new SimpleDateFormat("dd", Locale.getDefault());
        SimpleDateFormat sdfFullDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (int i = 0; i < 7; i++) {
            datesOfWeek[i] = sdfFullDate.format(cal.getTime());
            tvDayVals[i].setText(sdfDayVal.format(cal.getTime()));
            
            final int index = i;
            layoutDays[i].setOnClickListener(v -> {
                selectedDate = datesOfWeek[index];
                highlightSelectedDay(index);
                refreshSchedules();
            });

            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Highlight today
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        int defaultSelect = 0;
        for (int i = 0; i < 7; i++) {
            if (datesOfWeek[i].equals(today)) {
                defaultSelect = i;
                selectedDate = today;
                break;
            }
        }
        highlightSelectedDay(defaultSelect);
    }

    private void highlightSelectedDay(int index) {
        for (int i = 0; i < 7; i++) {
            if (i == index) {
                // Highlight color background
                tvDayVals[i].setBackgroundResource(R.drawable.bg_badge_goal); // Circular purple highlight
                tvDayVals[i].setTextColor(getResources().getColor(R.color.white));
            } else {
                tvDayVals[i].setBackground(null);
                tvDayVals[i].setTextColor(getResources().getColor(R.color.black));
            }
        }
    }

    private void refreshSchedules() {
        List<WorkoutSchedule> all = localDb.getAllWorkoutSchedules();
        todaySchedulesList.clear();

        for (WorkoutSchedule s : all) {
            if (s.getDate().equals(selectedDate)) {
                todaySchedulesList.add(s);
            }
        }

        // Always show the hours recycler slots list
        adapter.notifyDataSetChanged();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private String getPhotoHour(String path) {
        try {
            int index = path.indexOf("Progress_");
            if (index != -1) {
                String sub = path.substring(index + 9, index + 24); // yyyyMMdd_HHmmss
                Date date = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).parse(sub);
                if (date != null) {
                    return new SimpleDateFormat("hh:00 a", Locale.getDefault()).format(date);
                }
            }
        } catch (Exception e) {}
        return null;
    }

    private int parseSlotHourTo24h(String hourStr) {
        try {
            Date date = new SimpleDateFormat("hh:00 a", Locale.getDefault()).parse(hourStr);
            Calendar cal = Calendar.getInstance();
            if (date != null) {
                cal.setTime(date);
                return cal.get(Calendar.HOUR_OF_DAY);
            }
        } catch (Exception e) {}
        return -1;
    }

    private void editReminderTime(com.fitness.app.models.Reminder reminder) {
        android.app.TimePickerDialog picker = new android.app.TimePickerDialog(this, (view, hourOfDay, minute) -> {
            reminder.setHour(hourOfDay);
            reminder.setMinute(minute);
            
            // Reschedule
            List<com.fitness.app.models.Reminder> all = localDb.getReminders();
            for (int i = 0; i < all.size(); i++) {
                if (all.get(i).getId().equals(reminder.getId())) {
                    all.set(i, reminder);
                    break;
                }
            }
            localDb.saveReminders(all);
            com.fitness.app.utils.ReminderScheduler.scheduleReminder(this, reminder);
            
            Toast.makeText(this, "Reminder updated successfully!", Toast.LENGTH_SHORT).show();
            refreshSchedules();
        }, reminder.getHour(), reminder.getMinute(), false);
        picker.show();
    }

    private void deleteReminder(com.fitness.app.models.Reminder reminder) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Reminder")
                .setMessage("Are you sure you want to delete this reminder from your calendar?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    com.fitness.app.utils.ReminderScheduler.cancelReminder(this, reminder);
                    List<com.fitness.app.models.Reminder> all = localDb.getReminders();
                    for (int i = 0; i < all.size(); i++) {
                        if (all.get(i).getId().equals(reminder.getId())) {
                            all.remove(i);
                            break;
                        }
                    }
                    localDb.saveReminders(all);
                    Toast.makeText(this, "Reminder deleted", Toast.LENGTH_SHORT).show();
                    refreshSchedules();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule_slot, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String hour = hoursList.get(position);
            holder.tvSlotHour.setText(hour);

            // 1. Check for matching workouts
            WorkoutSchedule matchingSchedule = null;
            for (WorkoutSchedule s : todaySchedulesList) {
                if (s.getTime().equalsIgnoreCase(hour)) {
                    matchingSchedule = s;
                    break;
                }
            }

            // 2. Check for matching progress photos
            String matchingPhotoPath = null;
            String matchingPhotoDir = null;
            String photoDateLabel = "";
            try {
                Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDate);
                if (d != null) {
                    photoDateLabel = new SimpleDateFormat("d MMMM", Locale.getDefault()).format(d);
                }
            } catch (Exception e) {}
            
            android.content.SharedPreferences prefs = getSharedPreferences("ProgressTrackerPrefs", MODE_PRIVATE);
            java.util.Set<String> captured = prefs.getStringSet("captured_photos_set", null);
            if (captured != null && !photoDateLabel.isEmpty()) {
                for (String record : captured) {
                    String[] parts = record.split("\\|");
                    if (parts.length >= 3) {
                        String path = parts[0];
                        String date = parts[1];
                        String dir = parts[2];
                        if (date.equalsIgnoreCase(photoDateLabel)) {
                            String photoHr = getPhotoHour(path);
                            if (hour.equalsIgnoreCase(photoHr)) {
                                matchingPhotoPath = path;
                                matchingPhotoDir = dir;
                                break;
                            }
                        }
                    }
                }
            }

            // 3. Check for matching alarms/reminders
            com.fitness.app.models.Reminder matchingReminder = null;
            List<com.fitness.app.models.Reminder> reminders = localDb.getReminders();
            int slot24Hour = parseSlotHourTo24h(hour);
            for (com.fitness.app.models.Reminder r : reminders) {
                if (r.getHour() == slot24Hour) {
                    matchingReminder = r;
                    break;
                }
            }

            if (matchingSchedule != null) {
                WorkoutSchedule finalSchedule = matchingSchedule;
                holder.tvScheduleName.setText("🏋️ " + finalSchedule.getWorkoutName());
                holder.tvScheduleDuration.setText(finalSchedule.getDurationMin() + " mins" + 
                        (finalSchedule.getNotes() != null && !finalSchedule.getNotes().isEmpty() ? " | " + finalSchedule.getNotes() : ""));
                
                holder.ivScheduleCheck.setVisibility(View.VISIBLE);
                holder.ivScheduleCheck.setImageResource(finalSchedule.isCompleted() ? 
                        android.R.drawable.checkbox_on_background : android.R.drawable.checkbox_off_background);

                holder.ivScheduleCheck.setOnClickListener(v -> {
                    finalSchedule.setCompleted(!finalSchedule.isCompleted());
                    localDb.saveWorkoutSchedule(finalSchedule);
                    holder.ivScheduleCheck.setImageResource(finalSchedule.isCompleted() ? 
                            android.R.drawable.checkbox_on_background : android.R.drawable.checkbox_off_background);
                    Toast.makeText(WorkoutScheduleActivity.this, finalSchedule.isCompleted() ? 
                            "Workout marked complete! Keep it up!" : "Workout incomplete", Toast.LENGTH_SHORT).show();
                });

                holder.cvScheduleCard.setCardBackgroundColor(getResources().getColor(R.color.white));
                holder.cvScheduleCard.setOnClickListener(null);
            } 
            else if (matchingPhotoPath != null) {
                final String path = matchingPhotoPath;
                holder.tvScheduleName.setText("📸 Progress Photo Logged");
                holder.tvScheduleDuration.setText(matchingPhotoDir + " | Milestone captured!");
                holder.ivScheduleCheck.setVisibility(View.VISIBLE);
                holder.ivScheduleCheck.setImageResource(android.R.drawable.ic_menu_camera);

                holder.cvScheduleCard.setCardBackgroundColor(0xFFE0F2FE); // Light blue card
                holder.cvScheduleCard.setOnClickListener(v -> {
                    Intent intent = new Intent(WorkoutScheduleActivity.this, ProgressPhotoActivity.class);
                    startActivity(intent);
                });
            } 
            else if (matchingReminder != null) {
                com.fitness.app.models.Reminder r = matchingReminder;
                holder.tvScheduleName.setText("⏰ Reminder: " + r.getType());
                holder.tvScheduleDuration.setText(r.getMessage() + " (" + String.format(Locale.getDefault(), "%02d:%02d", r.getHour(), r.getMinute()) + ")");
                
                holder.ivScheduleCheck.setVisibility(View.VISIBLE);
                holder.ivScheduleCheck.setImageResource(r.isEnabled() ? 
                        android.R.drawable.checkbox_on_background : android.R.drawable.checkbox_off_background);

                holder.ivScheduleCheck.setOnClickListener(v -> {
                    r.setEnabled(!r.isEnabled());
                    
                    // Save reminder list
                    List<com.fitness.app.models.Reminder> all = localDb.getReminders();
                    for (int i = 0; i < all.size(); i++) {
                        if (all.get(i).getId().equals(r.getId())) {
                            all.set(i, r);
                            break;
                        }
                    }
                    localDb.saveReminders(all);
                    
                    if (r.isEnabled()) {
                        com.fitness.app.utils.ReminderScheduler.scheduleReminder(WorkoutScheduleActivity.this, r);
                    } else {
                        com.fitness.app.utils.ReminderScheduler.cancelReminder(WorkoutScheduleActivity.this, r);
                    }
                    
                    holder.ivScheduleCheck.setImageResource(r.isEnabled() ? 
                            android.R.drawable.checkbox_on_background : android.R.drawable.checkbox_off_background);
                    Toast.makeText(WorkoutScheduleActivity.this, r.isEnabled() ? "Reminder enabled" : "Reminder disabled", Toast.LENGTH_SHORT).show();
                });

                holder.cvScheduleCard.setCardBackgroundColor(0xFFF3E8FF); // Light purple card
                holder.cvScheduleCard.setOnClickListener(v -> {
                    new androidx.appcompat.app.AlertDialog.Builder(WorkoutScheduleActivity.this)
                            .setTitle("Reminder Options")
                            .setItems(new String[]{"Edit Time", "Delete Reminder", "Cancel"}, (dialog, which) -> {
                                if (which == 0) {
                                    editReminderTime(r);
                                } else if (which == 1) {
                                    deleteReminder(r);
                                }
                            })
                            .show();
                });
            } 
            else {
                holder.tvScheduleName.setText("No workout scheduled");
                holder.tvScheduleDuration.setText("Tap to add a session");
                holder.ivScheduleCheck.setVisibility(View.GONE);
                holder.cvScheduleCard.setCardBackgroundColor(0xFFF1F5F9); // Gray background for empty slot

                holder.cvScheduleCard.setOnClickListener(v -> {
                    Intent intent = new Intent(WorkoutScheduleActivity.this, AddScheduleActivity.class);
                    intent.putExtra("selected_date", selectedDate);
                    intent.putExtra("selected_time", hour);
                    startActivity(intent);
                });
            }
        }

        @Override
        public int getItemCount() {
            return hoursList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvSlotHour, tvScheduleName, tvScheduleDuration;
            ImageView ivScheduleCheck;
            CardView cvScheduleCard;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvSlotHour = itemView.findViewById(R.id.tvSlotHour);
                tvScheduleName = itemView.findViewById(R.id.tvScheduleName);
                tvScheduleDuration = itemView.findViewById(R.id.tvScheduleDuration);
                ivScheduleCheck = itemView.findViewById(R.id.ivScheduleCheck);
                cvScheduleCard = itemView.findViewById(R.id.cvScheduleCard);
            }
        }
    }
}

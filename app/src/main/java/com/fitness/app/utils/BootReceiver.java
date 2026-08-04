package com.fitness.app.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.models.Reminder;

import java.util.List;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // 1. Reschedule standard fitness goals reminders
            LocalDataManager localDb = new LocalDataManager(context);
            List<Reminder> reminders = localDb.getReminders();
            for (Reminder r : reminders) {
                if (r.isEnabled()) {
                    ReminderScheduler.scheduleReminder(context, r);
                }
            }

            // 2. Reschedule progress photo reminders
            SharedPreferences sharedPrefs = context.getSharedPreferences("ProgressTrackerPrefs", Context.MODE_PRIVATE);
            boolean enabled = sharedPrefs.getBoolean("reminder_enabled", true);
            if (enabled) {
                int frequencyIndex = sharedPrefs.getInt("reminder_frequency_index", 3);
                int hour = sharedPrefs.getInt("reminder_time_hour", 9);
                int minute = sharedPrefs.getInt("reminder_time_minute", 0);
                int customInterval = sharedPrefs.getInt("reminder_custom_interval_days", 30);
                
                ReminderScheduler.schedulePhotoReminder(context, true, frequencyIndex, hour, minute, customInterval);
            }

            // 3. Reschedule sleep/wake alarms
            SharedPreferences sleepPrefs = context.getSharedPreferences("fittrain_local_prefs", Context.MODE_PRIVATE);
            String savedAlarms = sleepPrefs.getString("sleep_alarms_list", null);
            if (savedAlarms != null) {
                try {
                    org.json.JSONArray arr = new org.json.JSONArray(savedAlarms);
                    for (int i = 0; i < arr.length(); i++) {
                        org.json.JSONObject obj = arr.getJSONObject(i);
                        String id = obj.getString("id");
                        String title = obj.getString("title");
                        String bedtime = obj.getString("bedtime");
                        String wakeTime = obj.getString("wakeTime");
                        boolean isEnabled = obj.getBoolean("enabled");
                        String repeat = obj.optString("repeat", "Once");

                        if (isEnabled) {
                            boolean[] repeatDays = SleepAlarmScheduler.parseRepeatDays(repeat);
                            SleepAlarmScheduler.scheduleSleepAlarm(context, id, title, bedtime, "SLEEP", repeatDays, true);
                            SleepAlarmScheduler.scheduleSleepAlarm(context, id, title, wakeTime, "WAKEUP", repeatDays, true);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // 4. Reschedule fitness challenge alarms
            ReminderScheduler.rescheduleChallengeReminders(context);
        }
    }
}

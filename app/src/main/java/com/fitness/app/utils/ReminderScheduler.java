package com.fitness.app.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.fitness.app.models.Reminder;

import java.util.Calendar;

public class ReminderScheduler {

    public static void scheduleReminder(Context context, Reminder reminder) {
        if (!reminder.isEnabled()) {
            cancelReminder(context, reminder);
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("type", reminder.getType());
        intent.putExtra("message", reminder.getMessage());

        // Unique request code per reminder ID
        int requestCode = Integer.parseInt(reminder.getId());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, reminder.getHour());
        calendar.set(Calendar.MINUTE, reminder.getMinute());
        calendar.set(Calendar.SECOND, 0);

        // If time has already passed today, schedule for tomorrow
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Set repeating alarm daily
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );
    }

    public static void schedulePhotoReminder(Context context, boolean enabled, int frequencyIndex, int hour, int minute, int customIntervalDays) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("type", "PHOTO");
        intent.putExtra("message", "Time to log your progress photo and check your fit milestone! 📸");

        int requestCode = 9999; // Unique request code for progress photo reminders

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (!enabled) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
            return;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        long intervalMillis;
        if (frequencyIndex == 0) {
            intervalMillis = AlarmManager.INTERVAL_DAY; // Daily
        } else if (frequencyIndex == 1) {
            intervalMillis = AlarmManager.INTERVAL_DAY * 7; // Weekly
        } else if (frequencyIndex == 2) {
            intervalMillis = AlarmManager.INTERVAL_DAY * 14; // Every 2 weeks
        } else if (frequencyIndex == 3) {
            intervalMillis = AlarmManager.INTERVAL_DAY * 30; // Monthly
        } else {
            intervalMillis = AlarmManager.INTERVAL_DAY * (customIntervalDays > 0 ? customIntervalDays : 30); // Custom
        }

        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                intervalMillis,
                pendingIntent
        );
    }

    public static void cancelReminder(Context context, Reminder reminder) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, ReminderReceiver.class);
        int requestCode = Integer.parseInt(reminder.getId());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    public static void scheduleChallengeDailyReminder(Context context, long triggerTime) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("type", "CHALLENGE_DAILY");
        intent.putExtra("message", "Don't forget to complete your daily challenge task! Let's build that streak! 🏆");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                9001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );
    }

    public static void scheduleChallengeDeadlineReminder(Context context, long deadlineTime) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("type", "CHALLENGE_DEADLINE");
        intent.putExtra("message", "Your active challenge ends in 24 hours! Squeeze in your final tasks! ⏰");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                9002,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long trigger = deadlineTime - (24L * 60 * 60 * 1000);
        if (trigger > System.currentTimeMillis()) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, trigger, pendingIntent);
        }
    }

    public static void scheduleChallengeMissedReminder(Context context, long triggerTime) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("type", "CHALLENGE_MISSED");
        intent.putExtra("message", "You missed yesterday's task! Log in today to keep your streak! ⚡");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                9003,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
    }

    public static void scheduleChallengeWeeklyReminder(Context context, long triggerTime) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("type", "CHALLENGE_WEEKLY");
        intent.putExtra("message", "Check out your weekly fitness challenge progress report! 📈");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                9004,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                AlarmManager.INTERVAL_DAY * 7,
                pendingIntent
        );
    }

    public static void rescheduleChallengeReminders(Context context) {
        new Thread(() -> {
            try {
                com.fitness.app.data.room.AppDatabase db = com.fitness.app.data.room.AppDatabase.getInstance(context);
                com.fitness.app.models.FitnessChallenge active = db.fitnessDao().getActiveChallenge();
                if (active != null && "IN_PROGRESS".equals(active.getStatus())) {
                    // Daily at 9:00 AM
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, 9);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                        calendar.add(Calendar.DAY_OF_YEAR, 1);
                    }
                    scheduleChallengeDailyReminder(context, calendar.getTimeInMillis());
                    
                    // Deadline
                    scheduleChallengeDeadlineReminder(context, active.getEndDate());

                    // Weekly
                    Calendar weeklyCalendar = Calendar.getInstance();
                    weeklyCalendar.add(Calendar.DAY_OF_YEAR, 7);
                    scheduleChallengeWeeklyReminder(context, weeklyCalendar.getTimeInMillis());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}

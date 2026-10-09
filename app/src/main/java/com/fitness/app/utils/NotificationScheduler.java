package com.fitness.app.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.models.WorkoutLog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationScheduler {

    public static final int REQ_MORNING = 4001;
    public static final int REQ_WORKOUT = 4002;
    public static final int REQ_MEAL_BREAKFAST = 4003;
    public static final int REQ_MEAL_LUNCH = 4004;
    public static final int REQ_MEAL_DINNER = 4005;
    public static final int REQ_HYDRATION = 4006;
    public static final int REQ_EVENING = 4007;
    public static final int REQ_NIGHT = 4008;

    public static void scheduleAll(Context context) {
        LocalDataManager db = new LocalDataManager(context);
        if (!db.isNotificationMasterEnabled()) {
            cancelAll(context);
            return;
        }

        // 1. Morning Motivation
        if (db.isMorningMotivationEnabled()) {
            String timeStr = db.getMorningMotivationTime();
            int[] hm = parseTime(timeStr, 8, 0);
            scheduleDailyAlarm(context, REQ_MORNING, "MORNING", hm[0], hm[1]);
        } else {
            cancelAlarm(context, REQ_MORNING);
        }

        // 2. Workout Reminder
        if (db.isWorkoutReminderEnabled() && !isWorkoutCompletedToday(context)) {
            String timeStr = db.getWorkoutReminderTime();
            int[] hm = parseTime(timeStr, 17, 0);
            scheduleDailyAlarm(context, REQ_WORKOUT, "WORKOUT", hm[0], hm[1]);
        } else {
            cancelAlarm(context, REQ_WORKOUT);
        }

        // 3. Meal Reminders
        if (db.isMealReminderEnabled()) {
            scheduleDailyAlarm(context, REQ_MEAL_BREAKFAST, "MEAL_BREAKFAST", 8, 30);
            scheduleDailyAlarm(context, REQ_MEAL_LUNCH, "MEAL_LUNCH", 13, 0);
            scheduleDailyAlarm(context, REQ_MEAL_DINNER, "MEAL_DINNER", 19, 30);
        } else {
            cancelAlarm(context, REQ_MEAL_BREAKFAST);
            cancelAlarm(context, REQ_MEAL_LUNCH);
            cancelAlarm(context, REQ_MEAL_DINNER);
        }

        // 4. Hydration Reminders
        if (db.isHydrationReminderEnabled()) {
            int intervalHours = db.getHydrationIntervalHours();
            if (intervalHours <= 0) intervalHours = 2;
            scheduleHydrationAlarms(context, intervalHours);
        } else {
            cancelAlarm(context, REQ_HYDRATION);
        }

        // 5. Evening Progress
        if (db.isEveningProgressEnabled()) {
            String timeStr = db.getEveningProgressTime();
            int[] hm = parseTime(timeStr, 19, 0);
            scheduleDailyAlarm(context, REQ_EVENING, "EVENING", hm[0], hm[1]);
        } else {
            cancelAlarm(context, REQ_EVENING);
        }

        // 6. Good Night
        if (db.isGoodNightEnabled()) {
            String timeStr = db.getGoodNightTime();
            int[] hm = parseTime(timeStr, 21, 30);
            scheduleDailyAlarm(context, REQ_NIGHT, "NIGHT", hm[0], hm[1]);
        } else {
            cancelAlarm(context, REQ_NIGHT);
        }
    }

    public static void scheduleDailyAlarm(Context context, int requestCode, String notificationType, int hour, int minute) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("type", notificationType);
        intent.putExtra("request_code", requestCode);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    private static void scheduleHydrationAlarms(Context context, int intervalHours) {
        // Schedule next daytime hydration alert between 9:00 AM and 9:00 PM
        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);

        int currentHour = now.get(Calendar.HOUR_OF_DAY);
        int nextHour = currentHour + intervalHours;

        if (nextHour < 9) {
            target.set(Calendar.HOUR_OF_DAY, 9);
            target.set(Calendar.MINUTE, 0);
        } else if (nextHour <= 21) {
            target.set(Calendar.HOUR_OF_DAY, nextHour);
            target.set(Calendar.MINUTE, 0);
        } else {
            // Tomorrow 9:00 AM
            target.add(Calendar.DAY_OF_YEAR, 1);
            target.set(Calendar.HOUR_OF_DAY, 9);
            target.set(Calendar.MINUTE, 0);
        }

        scheduleDailyAlarm(context, REQ_HYDRATION, "HYDRATION", target.get(Calendar.HOUR_OF_DAY), target.get(Calendar.MINUTE));
    }

    public static void cancelAlarm(Context context, int requestCode) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, ReminderReceiver.class);
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

    public static void cancelAll(Context context) {
        int[] codes = {REQ_MORNING, REQ_WORKOUT, REQ_MEAL_BREAKFAST, REQ_MEAL_LUNCH, REQ_MEAL_DINNER, REQ_HYDRATION, REQ_EVENING, REQ_NIGHT};
        for (int code : codes) {
            cancelAlarm(context, code);
        }
    }

    public static boolean isWorkoutCompletedToday(Context context) {
        try {
            LocalDataManager db = new LocalDataManager(context);
            String todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            boolean taskCompleted = db.sharedPreferences.getBoolean("daily_task_completed_" + todayStr, false);
            if (taskCompleted) return true;

            List<WorkoutLog> logs = db.getAllWorkoutLogs();
            for (WorkoutLog log : logs) {
                if (todayStr.equals(log.getDate())) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static int[] parseTime(String timeStr, int defaultHour, int defaultMin) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return new int[]{defaultHour, defaultMin};
        }
        try {
            String[] parts = timeStr.trim().split(":");
            if (parts.length >= 2) {
                int h = Integer.parseInt(parts[0].trim());
                int m = Integer.parseInt(parts[1].trim());
                return new int[]{h, m};
            }
        } catch (Exception ignored) {}
        return new int[]{defaultHour, defaultMin};
    }
}

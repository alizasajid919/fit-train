package com.fitness.app.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SleepAlarmScheduler {

    public static void scheduleSleepAlarm(Context context, String id, String title, String timeStr, String type, boolean[] repeatDays, boolean enabled) {
        Log.d("FitTrainAlarm", "scheduleSleepAlarm: id=" + id + ", title=" + title + ", time=" + timeStr + ", type=" + type + ", enabled=" + enabled);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction("com.fitness.app.TRIGGER_ALARM");
        intent.putExtra("id", id);
        intent.putExtra("title", title);
        intent.putExtra("time", timeStr);
        intent.putExtra("type", type);
        intent.putExtra("repeatDays", repeatDays);

        int requestCode = (id + type).hashCode();
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

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            Date date = sdf.parse(timeStr);
            if (date != null) {
                Calendar calendar = Calendar.getInstance();
                Calendar timeCal = Calendar.getInstance();
                timeCal.setTime(date);

                calendar.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
                calendar.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                long triggerTime = getNextTriggerTime(calendar, repeatDays);
                Log.d("FitTrainAlarm", "Computed alarm target time: " + calendar.getTime().toString() + " (triggerTime=" + triggerTime + ", current=" + System.currentTimeMillis() + ")");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                }
            }
        } catch (Exception e) {
            Log.e("FitTrainAlarm", "Exception while scheduling sleep alarm", e);
        }
    }

    public static void cancelSleepAlarm(Context context, String id, String type) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction("com.fitness.app.TRIGGER_ALARM");
        int requestCode = (id + type).hashCode();

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

    private static long getNextTriggerTime(Calendar calendar, boolean[] repeatDays) {
        long now = System.currentTimeMillis();
        boolean hasRepeat = false;
        if (repeatDays != null) {
            for (boolean b : repeatDays) {
                if (b) {
                    hasRepeat = true;
                    break;
                }
            }
        }

        if (!hasRepeat) {
            if (calendar.getTimeInMillis() <= now) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
            return calendar.getTimeInMillis();
        }

        for (int i = 0; i < 7; i++) {
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            int repeatIndex = (dayOfWeek == Calendar.SUNDAY) ? 6 : (dayOfWeek - 2);

            if (repeatDays[repeatIndex] && calendar.getTimeInMillis() > now) {
                return calendar.getTimeInMillis();
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        
        return calendar.getTimeInMillis();
    }

    public static boolean[] parseRepeatDays(String repeatStr) {
        boolean[] repeatDays = new boolean[7];
        if (repeatStr == null || repeatStr.isEmpty() || repeatStr.equals("Once")) {
            return repeatDays;
        }
        if (repeatStr.equals("Everyday")) {
            java.util.Arrays.fill(repeatDays, true);
            return repeatDays;
        }
        if (repeatStr.equals("Weekdays")) {
            for (int i = 0; i < 5; i++) repeatDays[i] = true;
            return repeatDays;
        }
        if (repeatStr.equals("Weekends")) {
            repeatDays[5] = true;
            repeatDays[6] = true;
            return repeatDays;
        }
        String[] parts = repeatStr.split(", ");
        String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (String p : parts) {
            for (int i = 0; i < 7; i++) {
                if (dayNames[i].equalsIgnoreCase(p.trim())) {
                    repeatDays[i] = true;
                }
            }
        }
        return repeatDays;
    }
}

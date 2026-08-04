package com.fitness.app.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.fitness.app.R;
import com.fitness.app.activities.MainActivity;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "fittrain_reminders_channel";
    private static final String CHANNEL_NAME = "FitTrain Reminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if ("ACTION_SNOOZE".equals(action)) {
            int notifId = intent.getIntExtra("notification_id", 0);
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null && notifId != 0) {
                manager.cancel(notifId);
            }
            
            android.app.AlarmManager alarmManager = (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                Intent reschedIntent = new Intent(context, ReminderReceiver.class);
                reschedIntent.putExtra("type", intent.getStringExtra("type"));
                reschedIntent.putExtra("message", intent.getStringExtra("message"));
                
                PendingIntent reschedPendingIntent = PendingIntent.getBroadcast(
                        context,
                        (int) System.currentTimeMillis(),
                        reschedIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );
                
                long snoozeTime = System.currentTimeMillis() + (15L * 60 * 1000); // Snooze for 15 minutes
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, snoozeTime, reschedPendingIntent);
                } else {
                    alarmManager.set(android.app.AlarmManager.RTC_WAKEUP, snoozeTime, reschedPendingIntent);
                }
            }
            android.widget.Toast.makeText(context, "Reminder snoozed for 15 minutes", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        String type = intent.getStringExtra("type");
        String message = intent.getStringExtra("message");
        if (message == null) message = "Time to stay on track with your fitness goal!";

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        // Create Channel for Oreo and higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            manager.createNotificationChannel(channel);
        }

        // Tap action
        Intent tapIntent;
        if ("WATER".equalsIgnoreCase(type)) {
            tapIntent = new Intent(context, com.fitness.app.activities.DetailTrackerActivity.class);
            tapIntent.putExtra("tracker_type", "water");
        } else if ("MEAL".equalsIgnoreCase(type)) {
            tapIntent = new Intent(context, MainActivity.class);
            tapIntent.putExtra("navigate_to", "diets");
        } else if ("WORKOUT".equalsIgnoreCase(type)) {
            tapIntent = new Intent(context, MainActivity.class);
            tapIntent.putExtra("navigate_to", "workouts");
        } else if ("SLEEP".equalsIgnoreCase(type)) {
            tapIntent = new Intent(context, com.fitness.app.activities.SleepTrackerActivity.class);
        } else if ("PHOTO".equalsIgnoreCase(type)) {
            tapIntent = new Intent(context, com.fitness.app.activities.ProgressPhotoActivity.class);
        } else if (type != null && type.toUpperCase().startsWith("CHALLENGE")) {
            tapIntent = new Intent(context, com.fitness.app.activities.ChallengesHubActivity.class);
        } else {
            tapIntent = new Intent(context, MainActivity.class);
        }
        tapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        int notificationId = (int) System.currentTimeMillis();

        Intent snoozeIntent = new Intent(context, ReminderReceiver.class);
        snoozeIntent.setAction("ACTION_SNOOZE");
        snoozeIntent.putExtra("type", type);
        snoozeIntent.putExtra("message", message);
        snoozeIntent.putExtra("notification_id", notificationId);
        
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId + 1,
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_user) // fallback to user icon
                .setContentTitle(getNotificationTitle(type))
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setVibrate(new long[]{1000, 1000}) // Vibrate if enabled by device
                .setAutoCancel(true)
                .addAction(android.R.drawable.ic_lock_idle_alarm, "Snooze (15m)", snoozePendingIntent);

        manager.notify(notificationId, builder.build());
    }

    private String getNotificationTitle(String type) {
        if (type == null) return "FitTrain Reminder";
        switch (type.toUpperCase()) {
            case "WATER":
                return "Hydration Alert 💧";
            case "MEAL":
                return "Mealtime Reminder 🍎";
            case "WORKOUT":
                return "Time to Train! 🏋️";
            case "SLEEP":
                return "Bedtime Routine 🌙";
            case "PHOTO":
                return "Progress Photo Check-in 📸";
            case "CHALLENGE_DAILY":
                return "Daily Challenge Target 🏆";
            case "CHALLENGE_DEADLINE":
                return "Challenge Ending Soon! ⏰";
            case "CHALLENGE_MISSED":
                return "Streak at Risk! ⚡";
            case "CHALLENGE_WEEKLY":
                return "Weekly Progress Report 📈";
            default:
                return "FitTrain Daily Goal";
        }
    }
}

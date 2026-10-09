package com.fitness.app.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.fitness.app.data.local.LocalDataManager;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();

        if ("ACTION_SNOOZE".equals(action)) {
            int notifId = intent.getIntExtra("notification_id", 0);
            android.app.NotificationManager manager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null && notifId != 0) {
                manager.cancel(notifId);
            }
            android.widget.Toast.makeText(context, "Reminder snoozed for 15 minutes ⏱️", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        LocalDataManager db = new LocalDataManager(context);
        if (!db.isNotificationMasterEnabled()) {
            return;
        }

        String type = intent.getStringExtra("type");
        if (type == null) type = "MORNING";

        // Check category switches before showing
        if ("MORNING".equalsIgnoreCase(type) && !db.isMorningMotivationEnabled()) return;
        if ("WORKOUT".equalsIgnoreCase(type) && (!db.isWorkoutReminderEnabled() || NotificationScheduler.isWorkoutCompletedToday(context))) return;
        if (type.startsWith("MEAL") && !db.isMealReminderEnabled()) return;
        if ("HYDRATION".equalsIgnoreCase(type) && !db.isHydrationReminderEnabled()) return;
        if ("EVENING".equalsIgnoreCase(type) && !db.isEveningProgressEnabled()) return;
        if ("NIGHT".equalsIgnoreCase(type) && !db.isGoodNightEnabled()) return;

        // Show the goal-personalized notification
        NotificationHelper.showNotification(context, type);

        // Reschedule next alarm for daily continuity
        NotificationScheduler.scheduleAll(context);
    }
}

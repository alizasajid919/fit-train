package com.fitness.app.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.content.ContextCompat;

import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String id = intent.getStringExtra("id");
        String title = intent.getStringExtra("title");
        String time = intent.getStringExtra("time");
        String type = intent.getStringExtra("type");
        boolean[] repeatDays = intent.getBooleanArrayExtra("repeatDays");

        Log.d("FitTrainAlarm", "AlarmReceiver onReceive: id=" + id + ", title=" + title + ", type=" + type + ", action=" + intent.getAction());

        if (id == null) {
            Log.e("FitTrainAlarm", "AlarmReceiver: id is null, ignoring alarm broadcast");
            return;
        }

        // Start Foreground Alarm Sound/Vibration Service
        Intent serviceIntent = new Intent(context, SleepAlarmService.class);
        serviceIntent.putExtra("id", id);
        serviceIntent.putExtra("title", title);
        serviceIntent.putExtra("type", type);
        ContextCompat.startForegroundService(context, serviceIntent);

        // If repeating, reschedule for the next weekday
        if (repeatDays != null) {
            SleepAlarmScheduler.scheduleSleepAlarm(context, id, title, time, type, repeatDays, true);
        }
    }
}

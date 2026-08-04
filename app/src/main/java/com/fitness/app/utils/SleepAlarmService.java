package com.fitness.app.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.fitness.app.R;
import com.fitness.app.activities.SleepTrackerActivity;

public class SleepAlarmService extends Service {

    private static final String CHANNEL_ID = "fittrain_sleep_alarm_channel";
    private static final String CHANNEL_NAME = "FitTrain Sleep Alarms";
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;

    @Override
    public void onCreate() {
        super.onCreate();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            Log.e("FitTrainAlarm", "SleepAlarmService onStartCommand: intent is null");
            return START_NOT_STICKY;
        }

        String action = intent.getAction();
        String id = intent.getStringExtra("id");
        String title = intent.getStringExtra("title");
        String type = intent.getStringExtra("type");

        Log.d("FitTrainAlarm", "SleepAlarmService onStartCommand: action=" + action + ", id=" + id + ", title=" + title + ", type=" + type);

        if ("ACTION_DISMISS".equals(action)) {
            Log.d("FitTrainAlarm", "SleepAlarmService: Dismissing alarm id=" + id);
            stopSelf();
            return START_NOT_STICKY;
        }

        if ("ACTION_SNOOZE".equals(action)) {
            Log.d("FitTrainAlarm", "SleepAlarmService: Snoozing alarm id=" + id);
            snoozeAlarm(id, title, type);
            stopSelf();
            return START_NOT_STICKY;
        }

        // Play loud looping alarm ringtone
        startAlarmSound();

        // Start long repeating vibration
        if (vibrator != null && vibrator.hasVibrator()) {
            long[] pattern = {0, 1000, 1000};
            vibrator.vibrate(pattern, 0); // Loop vibration
        }

        // Build notification channel
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.enableVibration(true);
            manager.createNotificationChannel(channel);
        }

        // Tap action opens SleepTrackerActivity
        Intent tapIntent = new Intent(this, SleepTrackerActivity.class);
        PendingIntent tapPending = PendingIntent.getActivity(
                this,
                0,
                tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Dismiss action
        Intent dismissIntent = new Intent(this, SleepAlarmService.class);
        dismissIntent.setAction("ACTION_DISMISS");
        PendingIntent dismissPending = PendingIntent.getService(
                this,
                (id + "dismiss").hashCode(),
                dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Snooze action
        Intent snoozeIntent = new Intent(this, SleepAlarmService.class);
        snoozeIntent.setAction("ACTION_SNOOZE");
        snoozeIntent.putExtra("id", id);
        snoozeIntent.putExtra("title", title);
        snoozeIntent.putExtra("type", type);
        PendingIntent snoozePending = PendingIntent.getService(
                this,
                (id + "snooze").hashCode(),
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String notificationTitle = "WAKEUP".equalsIgnoreCase(type) ? "Rise and Shine! ⏰" : "Time for Bedtime! 🛏️";
        String notificationText = "WAKEUP".equalsIgnoreCase(type) ? "Wake up schedule: " + title : "Bedtime routine starts now (" + title + ")";

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_sleep)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setContentIntent(tapPending)
                .setFullScreenIntent(tapPending, true) // Show full screen overlay on lockscreen
                .addAction(R.drawable.ic_sleep, "Snooze", snoozePending)
                .addAction(R.drawable.ic_sleep, "Dismiss", dismissPending)
                .setOngoing(true)
                .setAutoCancel(false)
                .build();

        startForeground(9998, notification);

        return START_STICKY;
    }

    private void startAlarmSound() {
        Log.d("FitTrainAlarm", "SleepAlarmService: starting alarm sound playback");
        try {
            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(this, alarmUri);
            } catch (Exception ex) {
                Log.e("FitTrainAlarm", "Primary Uri setDataSource failed, trying notification sound", ex);
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                mediaPlayer.setDataSource(this, alarmUri);
            }

            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build());
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
            Log.d("FitTrainAlarm", "SleepAlarmService: Alarm sound playback started successfully");
        } catch (Exception e) {
            Log.e("FitTrainAlarm", "SleepAlarmService: Failed to play alarm sound", e);
        }
    }

    private void snoozeAlarm(String id, String title, String type) {
        Log.d("FitTrainAlarm", "Snoozing alarm: id=" + id + ", title=" + title + ", type=" + type);
        android.app.AlarmManager alarmManager = (android.app.AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setAction("com.fitness.app.TRIGGER_ALARM");
        intent.putExtra("id", id);
        intent.putExtra("title", title + " (Snoozed)");
        intent.putExtra("type", type);
        intent.putExtra("repeatDays", (boolean[]) null); // Snooze is one-time alarm

        int requestCode = (id + type + "snooze").hashCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long triggerAt = System.currentTimeMillis() + (5 * 60 * 1000); // 5 minutes snooze

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
        } else {
            alarmManager.setExact(android.app.AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
        }
        Log.d("FitTrainAlarm", "Snoozed alarm scheduled for +5 minutes (" + triggerAt + ")");
    }

    @Override
    public void onDestroy() {
        Log.d("FitTrainAlarm", "SleepAlarmService: onDestroy called, stopping sound and vibration");
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

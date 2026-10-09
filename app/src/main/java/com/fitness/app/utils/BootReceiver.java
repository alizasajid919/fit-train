package com.fitness.app.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Restore all scheduled notifications and reminders after device reboot
            NotificationScheduler.scheduleAll(context);
        }
    }
}

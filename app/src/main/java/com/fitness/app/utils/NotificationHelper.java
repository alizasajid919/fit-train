package com.fitness.app.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.fitness.app.R;
import com.fitness.app.activities.DetailTrackerActivity;
import com.fitness.app.activities.MainActivity;
import com.fitness.app.activities.ProgressTrackerActivity;
import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.models.User;

public class NotificationHelper {

    public static final String CHANNEL_MOTIVATION = "fittrain_channel_motivation";
    public static final String CHANNEL_WORKOUT = "fittrain_channel_workout";
    public static final String CHANNEL_MEAL = "fittrain_channel_meal";
    public static final String CHANNEL_HYDRATION = "fittrain_channel_hydration";
    public static final String CHANNEL_EVENING = "fittrain_channel_evening";
    public static final String CHANNEL_NIGHT = "fittrain_channel_night";

    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager == null) return;

            NotificationChannel motivation = new NotificationChannel(
                    CHANNEL_MOTIVATION,
                    "Morning Motivation",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            motivation.setDescription("Daily morning fitness inspiration and goal reminders.");

            NotificationChannel workout = new NotificationChannel(
                    CHANNEL_WORKOUT,
                    "Workout Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            workout.setDescription("Reminders to complete your scheduled daily workout routine.");

            NotificationChannel meal = new NotificationChannel(
                    CHANNEL_MEAL,
                    "Meal Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            meal.setDescription("Reminders for planned breakfast, lunch, snacks, and dinner.");

            NotificationChannel hydration = new NotificationChannel(
                    CHANNEL_HYDRATION,
                    "Hydration Alerts",
                    NotificationManager.IMPORTANCE_LOW
            );
            hydration.setDescription("Regular reminders to drink water during waking hours.");

            NotificationChannel evening = new NotificationChannel(
                    CHANNEL_EVENING,
                    "Evening Progress Review",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            evening.setDescription("Encouraging evening check-ins to review completed activities.");

            NotificationChannel night = new NotificationChannel(
                    CHANNEL_NIGHT,
                    "Good Night Rest",
                    NotificationManager.IMPORTANCE_LOW
            );
            night.setDescription("Bedtime reminders to prepare for quality sleep and recovery.");

            manager.createNotificationChannel(motivation);
            manager.createNotificationChannel(workout);
            manager.createNotificationChannel(meal);
            manager.createNotificationChannel(hydration);
            manager.createNotificationChannel(evening);
            manager.createNotificationChannel(night);
        }
    }

    public static void showNotification(Context context, String type) {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        createNotificationChannels(context);
        LocalDataManager db = new LocalDataManager(context);
        User user = db.getUser();
        String userName = (user != null && user.getFirstName() != null && !user.getFirstName().trim().isEmpty())
                ? user.getFirstName() : "Champion";
        String goal = (user != null && user.getGoal() != null) ? user.getGoal().toLowerCase() : "";

        String channelId;
        String title;
        String message;
        Intent intent;
        int notifId;

        if ("MORNING".equalsIgnoreCase(type)) {
            channelId = CHANNEL_MOTIVATION;
            notifId = 3001;
            title = "Good morning, " + userName + "! 🌞";
            if (goal.contains("loss") || goal.contains("lose")) {
                message = "Your fitness journey continues today! Stay active and keep your calorie deficit on target.";
            } else if (goal.contains("muscle") || goal.contains("gain")) {
                message = "Fuel up and get ready for a strong day! Let's build solid muscle and reach new peaks.";
            } else {
                message = "Your fitness journey continues today! Let's make consistent progress together.";
            }
            intent = new Intent(context, MainActivity.class);
            intent.putExtra("navigate_to", "home");

        } else if ("WORKOUT".equalsIgnoreCase(type)) {
            channelId = CHANNEL_WORKOUT;
            notifId = 3002;
            title = "Time to move! 💪";
            if (goal.contains("loss") || goal.contains("lose")) {
                message = "Your planned workout is waiting. Every session burns calories and brings you closer to your target weight!";
            } else if (goal.contains("muscle") || goal.contains("gain")) {
                message = "Time for your strength session! Push your limits and stimulate muscle growth today.";
            } else {
                message = "Your workout is waiting! Every small step counts toward a healthier, stronger you.";
            }
            intent = new Intent(context, MainActivity.class);
            intent.putExtra("navigate_to", "workouts");

        } else if ("MEAL_BREAKFAST".equalsIgnoreCase(type) || "MEAL_LUNCH".equalsIgnoreCase(type)
                || "MEAL_DINNER".equalsIgnoreCase(type) || "MEAL".equalsIgnoreCase(type)) {
            channelId = CHANNEL_MEAL;
            notifId = 3003;
            if ("MEAL_BREAKFAST".equalsIgnoreCase(type)) {
                title = "Breakfast Time! 🍳";
                message = "Kickstart your metabolism with a healthy, balanced morning meal.";
            } else if ("MEAL_LUNCH".equalsIgnoreCase(type)) {
                title = "Lunch Time! 🥗";
                message = "Time to refuel your body with a nutritious afternoon meal.";
            } else if ("MEAL_DINNER".equalsIgnoreCase(type)) {
                title = "Dinner Time! 🍲";
                message = "Savor a healthy dinner and nourish your muscle recovery for tonight.";
            } else {
                title = "Stay on track! 🥗";
                message = "It's time for your planned meal. Keep your nutrition aligned with your fitness goals.";
            }
            intent = new Intent(context, MainActivity.class);
            intent.putExtra("navigate_to", "diets");

        } else if ("HYDRATION".equalsIgnoreCase(type)) {
            channelId = CHANNEL_HYDRATION;
            notifId = 3004;
            title = "Time to hydrate! 💧";
            message = "Take a moment to drink some water. Staying hydrated supports metabolism and peak physical performance.";
            intent = new Intent(context, DetailTrackerActivity.class);
            intent.putExtra("tracker_type", "water");

        } else if ("EVENING".equalsIgnoreCase(type)) {
            channelId = CHANNEL_EVENING;
            notifId = 3005;
            title = "You've made progress today! ⭐";
            message = "Check your completed workouts, water intake, and daily step count. Celebrate every step of your journey.";
            intent = new Intent(context, ProgressTrackerActivity.class);

        } else if ("NIGHT".equalsIgnoreCase(type)) {
            channelId = CHANNEL_NIGHT;
            notifId = 3006;
            title = "Good night, " + userName + "! 🌙";
            message = "Rest well and get ready for another healthy day tomorrow. Quality sleep is key to optimal fitness recovery.";
            intent = new Intent(context, MainActivity.class);
            intent.putExtra("navigate_to", "home");

        } else {
            channelId = CHANNEL_MOTIVATION;
            notifId = 3000;
            title = "FitTrain Goal Update ⚡";
            message = "Stay consistent with your daily health and fitness routine today!";
            intent = new Intent(context, MainActivity.class);
            intent.putExtra("navigate_to", "home");
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notifId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.notify(notifId, builder.build());
        } catch (SecurityException ignored) {}
    }
}

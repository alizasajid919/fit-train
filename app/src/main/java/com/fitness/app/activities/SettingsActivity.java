package com.fitness.app.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.fitness.app.R;
import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.models.User;
import com.fitness.app.utils.NotificationHelper;
import com.fitness.app.utils.NotificationScheduler;
import com.fitness.app.viewmodels.AuthViewModel;
import com.fitness.app.viewmodels.ProfileViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private static final int PERM_NOTIF_CODE = 202;

    private SwitchCompat switchDarkTheme, switchMetricUnits, switchBodyShaming;
    private SwitchCompat switchNotifMaster, switchNotifMorning, switchNotifWorkout, switchNotifMeal, switchNotifHydration, switchNotifEvening, switchNotifNight;
    private MaterialButton btnTimeMorning, btnTimeWorkout, btnIntervalHydration, btnTimeEvening, btnTimeNight, btnTestNotification;

    private TextView tvCloudStatus;
    private View layoutGuestActions, layoutUserActions;
    private android.widget.ProgressBar pbSync;
    private android.widget.Button btnSyncNow;

    private LocalDataManager localDb;
    private AuthViewModel authViewModel;
    private ProfileViewModel profileViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        localDb = new LocalDataManager(this);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // Bind Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        // Bind Preference Switches
        switchDarkTheme = findViewById(R.id.switchDarkTheme);
        switchMetricUnits = findViewById(R.id.switchMetricUnits);
        switchBodyShaming = findViewById(R.id.switchBodyShaming);

        // Bind Notification Switches & Buttons
        switchNotifMaster = findViewById(R.id.switchNotifMaster);
        switchNotifMorning = findViewById(R.id.switchNotifMorning);
        switchNotifWorkout = findViewById(R.id.switchNotifWorkout);
        switchNotifMeal = findViewById(R.id.switchNotifMeal);
        switchNotifHydration = findViewById(R.id.switchNotifHydration);
        switchNotifEvening = findViewById(R.id.switchNotifEvening);
        switchNotifNight = findViewById(R.id.switchNotifNight);

        btnTimeMorning = findViewById(R.id.btnTimeMorning);
        btnTimeWorkout = findViewById(R.id.btnTimeWorkout);
        btnIntervalHydration = findViewById(R.id.btnIntervalHydration);
        btnTimeEvening = findViewById(R.id.btnTimeEvening);
        btnTimeNight = findViewById(R.id.btnTimeNight);
        btnTestNotification = findViewById(R.id.btnTestNotification);

        tvCloudStatus = findViewById(R.id.tvCloudStatus);
        layoutGuestActions = findViewById(R.id.layoutGuestActions);
        layoutUserActions = findViewById(R.id.layoutUserActions);
        pbSync = findViewById(R.id.pbSync);
        btnSyncNow = findViewById(R.id.btnSyncNow);

        loadSettingsAndProfile();

        // Listeners for account rows
        findViewById(R.id.layoutSettingsEditProfile).setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, EditProfileActivity.class));
        });

        findViewById(R.id.layoutSettingsLanguage).setOnClickListener(v -> showLanguageDialog());
        findViewById(R.id.layoutSettingsTerms).setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, TermsActivity.class));
        });
        findViewById(R.id.layoutSettingsAbout).setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, AboutActivity.class));
        });

        // App Preference Switches Listeners
        switchDarkTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            localDb.setDarkThemeEnabled(isChecked);
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        switchMetricUnits.setOnCheckedChangeListener((buttonView, isChecked) -> {
            localDb.setMetricUnitsEnabled(isChecked);
            Toast.makeText(this, "Units preference updated", Toast.LENGTH_SHORT).show();
        });

        switchBodyShaming.setOnCheckedChangeListener((buttonView, isChecked) -> {
            localDb.setBodyShamingProtectionEnabled(isChecked);
            Toast.makeText(this, "Body-Shaming Protection & Support Mode updated", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.layoutSettingsResetOnboarding).setOnClickListener(v -> {
            localDb.setOnboardingSeen(false);
            localDb.editor.putBoolean("onboarding_card_dismissed", false).apply();
            Toast.makeText(this, "Onboarding card reset! It will appear again on the Workouts tab.", Toast.LENGTH_LONG).show();
        });

        // Notification Switch Listeners
        switchNotifMaster.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) checkAndRequestNotificationPermission();
            localDb.setNotificationMasterEnabled(isChecked);
            NotificationScheduler.scheduleAll(this);
            Toast.makeText(this, isChecked ? "All Notifications Enabled 🔔" : "All Notifications Disabled 🔕", Toast.LENGTH_SHORT).show();
        });

        switchNotifMorning.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) checkAndRequestNotificationPermission();
            localDb.setMorningMotivationEnabled(isChecked);
            NotificationScheduler.scheduleAll(this);
        });

        switchNotifWorkout.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) checkAndRequestNotificationPermission();
            localDb.setWorkoutReminderEnabled(isChecked);
            NotificationScheduler.scheduleAll(this);
        });

        switchNotifMeal.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) checkAndRequestNotificationPermission();
            localDb.setMealReminderEnabled(isChecked);
            NotificationScheduler.scheduleAll(this);
        });

        switchNotifHydration.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) checkAndRequestNotificationPermission();
            localDb.setHydrationReminderEnabled(isChecked);
            NotificationScheduler.scheduleAll(this);
        });

        switchNotifEvening.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) checkAndRequestNotificationPermission();
            localDb.setEveningProgressEnabled(isChecked);
            NotificationScheduler.scheduleAll(this);
        });

        switchNotifNight.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) checkAndRequestNotificationPermission();
            localDb.setGoodNightEnabled(isChecked);
            NotificationScheduler.scheduleAll(this);
        });

        // Notification Time Buttons Listeners
        btnTimeMorning.setOnClickListener(v -> showTimePickerDialog(localDb.getMorningMotivationTime(), time -> {
            localDb.setMorningMotivationTime(time);
            btnTimeMorning.setText(formatTimeDisplay(time));
        }));

        btnTimeWorkout.setOnClickListener(v -> showTimePickerDialog(localDb.getWorkoutReminderTime(), time -> {
            localDb.setWorkoutReminderTime(time);
            btnTimeWorkout.setText(formatTimeDisplay(time));
        }));

        btnIntervalHydration.setOnClickListener(v -> showHydrationIntervalDialog());

        btnTimeEvening.setOnClickListener(v -> showTimePickerDialog(localDb.getEveningProgressTime(), time -> {
            localDb.setEveningProgressTime(time);
            btnTimeEvening.setText(formatTimeDisplay(time));
        }));

        btnTimeNight.setOnClickListener(v -> showTimePickerDialog(localDb.getGoodNightTime(), time -> {
            localDb.setGoodNightTime(time);
            btnTimeNight.setText(formatTimeDisplay(time));
        }));

        // Send Test Notification Button
        btnTestNotification.setOnClickListener(v -> {
            checkAndRequestNotificationPermission();
            NotificationHelper.showNotification(this, "MORNING");
            Toast.makeText(this, "Instant test notification sent! Check your notification shade 🔔", Toast.LENGTH_SHORT).show();
        });

        // Cloud Actions
        findViewById(R.id.btnSyncNow).setOnClickListener(v -> syncCloudData());
        findViewById(R.id.btnLogout).setOnClickListener(v -> performLogout());
        findViewById(R.id.btnDeleteAccount).setOnClickListener(v -> {
            FirebaseUser user = authViewModel.getCurrentUser();
            if (user != null) {
                new AlertDialog.Builder(this)
                        .setTitle("Delete Account")
                        .setMessage("Are you sure you want to permanently delete your cloud account?")
                        .setPositiveButton("Delete", (d, w) -> {
                            user.delete().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    localDb.clearAll();
                                    localDb.setOnboardingSeen(false);
                                    Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(SettingsActivity.this, OnboardingActivity.class));
                                    finishAffinity();
                                } else {
                                    Toast.makeText(this, "Failed to delete: " + (task.getException() != null ? task.getException().getMessage() : ""), Toast.LENGTH_LONG).show();
                                }
                            });
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCloudSyncStatus();
    }

    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, PERM_NOTIF_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERM_NOTIF_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted! 🔔", Toast.LENGTH_SHORT).show();
                NotificationScheduler.scheduleAll(this);
            } else {
                Toast.makeText(this, "Notification permission denied. You can enable notifications anytime in Settings.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void loadSettingsAndProfile() {
        switchDarkTheme.setChecked(localDb.isDarkThemeEnabled());
        switchMetricUnits.setChecked(localDb.isMetricUnitsEnabled());
        switchBodyShaming.setChecked(localDb.isBodyShamingProtectionEnabled());
        
        switchNotifMaster.setChecked(localDb.isNotificationMasterEnabled());
        switchNotifMorning.setChecked(localDb.isMorningMotivationEnabled());
        switchNotifWorkout.setChecked(localDb.isWorkoutReminderEnabled());
        switchNotifMeal.setChecked(localDb.isMealReminderEnabled());
        switchNotifHydration.setChecked(localDb.isHydrationReminderEnabled());
        switchNotifEvening.setChecked(localDb.isEveningProgressEnabled());
        switchNotifNight.setChecked(localDb.isGoodNightEnabled());

        btnTimeMorning.setText(formatTimeDisplay(localDb.getMorningMotivationTime()));
        btnTimeWorkout.setText(formatTimeDisplay(localDb.getWorkoutReminderTime()));
        btnIntervalHydration.setText("Every " + localDb.getHydrationIntervalHours() + "h");
        btnTimeEvening.setText(formatTimeDisplay(localDb.getEveningProgressTime()));
        btnTimeNight.setText(formatTimeDisplay(localDb.getGoodNightTime()));

        String currentLang = localDb.sharedPreferences.getString("app_language_code", "en");
        TextView tvLang = findViewById(R.id.tvSettingsLanguageVal);
        if (tvLang != null) {
            if ("ur".equals(currentLang)) tvLang.setText("اردو");
            else if ("es".equals(currentLang)) tvLang.setText("Español");
            else if ("de".equals(currentLang)) tvLang.setText("Deutsch");
            else tvLang.setText("English");
        }
        updateCloudSyncStatus();
    }

    private String formatTimeDisplay(String time24) {
        int[] hm = NotificationScheduler.parseTime(time24, 8, 0);
        int hour = hm[0];
        int min = hm[1];
        String amPm = hour >= 12 ? "PM" : "AM";
        int displayHour = hour % 12;
        if (displayHour == 0) displayHour = 12;
        return String.format(Locale.getDefault(), "%02d:%02d %s", displayHour, min, amPm);
    }

    private void showTimePickerDialog(String currentTime24, OnTimeSetListener listener) {
        int[] hm = NotificationScheduler.parseTime(currentTime24, 8, 0);
        android.app.TimePickerDialog dialog = new android.app.TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    String formatted = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    listener.onTimeSet(formatted);
                    NotificationScheduler.scheduleAll(this);
                },
                hm[0],
                hm[1],
                false
        );
        dialog.show();
    }

    private interface OnTimeSetListener {
        void onTimeSet(String time24);
    }

    private void showHydrationIntervalDialog() {
        String[] options = {"Every 1 hour", "Every 2 hours", "Every 3 hours", "Every 4 hours"};
        int[] hours = {1, 2, 3, 4};
        new AlertDialog.Builder(this)
                .setTitle("Hydration Reminder Interval")
                .setItems(options, (dialog, which) -> {
                    int selected = hours[which];
                    localDb.setHydrationIntervalHours(selected);
                    btnIntervalHydration.setText("Every " + selected + "h");
                    NotificationScheduler.scheduleAll(this);
                    Toast.makeText(this, "Hydration interval updated", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private String getDataSize() {
        long size = 0;
        try {
            java.io.File dbFile = getDatabasePath("fitness_db");
            if (dbFile != null && dbFile.exists()) {
                size += dbFile.length();
            }
            java.io.File shFile = new java.io.File(getApplicationInfo().dataDir + "/shared_prefs/fittrain_local_prefs.xml");
            if (shFile.exists()) {
                size += shFile.length();
            }
        } catch (Exception ignored) {}
        if (size <= 0) return "14.2 KB";
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format(Locale.US, "%.1f KB", size / 1024.0);
        return String.format(Locale.US, "%.1f MB", size / (1024.0 * 1024.0));
    }

    private void updateCloudSyncStatus() {
        FirebaseUser user = authViewModel.getCurrentUser();
        long lastSync = localDb.sharedPreferences.getLong("last_sync_time", 0);
        String lastSyncStr = lastSync == 0 ? "Never" : new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()).format(new java.util.Date(lastSync));
        String backupStatus = lastSync == 0 ? "No Backup" : "Backup Completed ✓";
        String dataSize = getDataSize();

        if (user == null || user.isAnonymous()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Sync Status: Local Only (Guest User)\n");
            sb.append("Backup Status: ").append(backupStatus).append("\n");
            sb.append("Last Sync Time: ").append(lastSyncStr).append("\n");
            sb.append("Local Data Size: ").append(dataSize);
            tvCloudStatus.setText(sb.toString());
            
            layoutGuestActions.setVisibility(View.GONE);
            layoutUserActions.setVisibility(View.GONE);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Sync Status: Cloud Sync Enabled\n");
            sb.append("Account Email: ").append(user.getEmail()).append("\n");
            sb.append("Backup Status: ").append(backupStatus).append("\n");
            sb.append("Last Sync Time: ").append(lastSyncStr).append("\n");
            sb.append("Cloud Data Size: ").append(dataSize);
            tvCloudStatus.setText(sb.toString());

            layoutGuestActions.setVisibility(View.GONE);
            layoutUserActions.setVisibility(View.VISIBLE);
        }
    }

    private void showLanguageDialog() {
        String[] languages = {"English", "Spanish", "German", "اردو"};
        String[] langCodes = {"en", "es", "de", "ur"};
        new AlertDialog.Builder(this)
                .setTitle("Select App Language")
                .setItems(languages, (dialog, which) -> {
                    String selectedLang = langCodes[which];
                    localDb.sharedPreferences.edit().putString("app_language_code", selectedLang).apply();
                    
                    androidx.core.os.LocaleListCompat locales = androidx.core.os.LocaleListCompat.forLanguageTags(selectedLang);
                    AppCompatDelegate.setApplicationLocales(locales);
                    
                    Toast.makeText(this, "Language set to " + languages[which], Toast.LENGTH_SHORT).show();
                    recreate();
                })
                .show();
    }

    private void performLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Logout")
                .setMessage("Are you sure you want to log out of your FitTrain account?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    localDb.clearAll();
                    localDb.setOnboardingSeen(false);
                    new Thread(() -> {
                        try {
                            com.fitness.app.data.room.AppDatabase.getInstance(this).clearAllTables();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                    profileViewModel.logout();
                    Toast.makeText(this, "Logged out and local cache cleared.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SettingsActivity.this, OnboardingActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finishAffinity();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void syncCloudData() {
        if (btnSyncNow != null) btnSyncNow.setEnabled(false);
        if (pbSync != null) pbSync.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                com.fitness.app.repositories.UserRepository repo = new com.fitness.app.repositories.UserRepository();
                repo.syncLocalDataToFirestore(localDb);
                localDb.sharedPreferences.edit().putLong("last_sync_time", System.currentTimeMillis()).apply();
                runOnUiThread(() -> {
                    if (pbSync != null) pbSync.setVisibility(View.GONE);
                    updateCloudSyncStatus();
                    if (btnSyncNow != null) {
                        btnSyncNow.setEnabled(true);
                        btnSyncNow.setText("Sync Complete! ✓");
                        btnSyncNow.postDelayed(() -> btnSyncNow.setText("Sync Data to Cloud Now"), 3000);
                    }
                    Toast.makeText(this, "Manual cloud sync complete!", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    if (pbSync != null) pbSync.setVisibility(View.GONE);
                    if (btnSyncNow != null) btnSyncNow.setEnabled(true);
                    Toast.makeText(this, "Sync failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}

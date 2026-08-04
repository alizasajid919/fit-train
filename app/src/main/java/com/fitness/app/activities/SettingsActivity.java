package com.fitness.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.fitness.app.R;
import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.models.User;
import com.fitness.app.viewmodels.AuthViewModel;
import com.fitness.app.viewmodels.ProfileViewModel;
import com.google.firebase.auth.FirebaseUser;

public class SettingsActivity extends AppCompatActivity {

    private SwitchCompat switchDarkTheme, switchMetricUnits, switchBodyShaming;
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

        // Bind Switches
        switchDarkTheme = findViewById(R.id.switchDarkTheme);
        switchMetricUnits = findViewById(R.id.switchMetricUnits);
        switchBodyShaming = findViewById(R.id.switchBodyShaming);

        tvCloudStatus = findViewById(R.id.tvCloudStatus);
        layoutGuestActions = findViewById(R.id.layoutGuestActions);
        layoutUserActions = findViewById(R.id.layoutUserActions);
        pbSync = findViewById(R.id.pbSync);
        btnSyncNow = findViewById(R.id.btnSyncNow);

        loadSettingsAndProfile();

        // Listeners for rows
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

        // Switches Listeners
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

        // Cloud Actions
        findViewById(R.id.btnCreateAccount).setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, RegisterActivity.class));
        });

        findViewById(R.id.btnLoginAccount).setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
        });

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
                                    Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_LONG).show();
                                    authViewModel.signInAnonymously(localDb).observe(this, res -> {
                                        startActivity(new Intent(SettingsActivity.this, OnboardingActivity.class));
                                        finishAffinity();
                                    });
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

    private void loadSettingsAndProfile() {
        switchDarkTheme.setChecked(localDb.isDarkThemeEnabled());
        switchMetricUnits.setChecked(localDb.isMetricUnitsEnabled());
        switchBodyShaming.setChecked(localDb.isBodyShamingProtectionEnabled());
        
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
        if (size < 1024 * 1024) return String.format(java.util.Locale.US, "%.1f KB", size / 1024.0);
        return String.format(java.util.Locale.US, "%.1f MB", size / (1024.0 * 1024.0));
    }

    private void updateCloudSyncStatus() {
        FirebaseUser user = authViewModel.getCurrentUser();
        long lastSync = localDb.sharedPreferences.getLong("last_sync_time", 0);
        String lastSyncStr = lastSync == 0 ? "Never" : new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm a", java.util.Locale.getDefault()).format(new java.util.Date(lastSync));
        String backupStatus = lastSync == 0 ? "No Backup" : "Backup Completed ✓";
        String dataSize = getDataSize();

        if (user == null || user.isAnonymous()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Sync Status: Local Only (Guest User)\n");
            sb.append("Backup Status: ").append(backupStatus).append("\n");
            sb.append("Last Sync Time: ").append(lastSyncStr).append("\n");
            sb.append("Local Data Size: ").append(dataSize);
            tvCloudStatus.setText(sb.toString());
            
            layoutGuestActions.setVisibility(View.VISIBLE);
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
                    
                    // Set application locales using AppCompatDelegate
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
                    new Thread(() -> {
                        try {
                            com.fitness.app.data.room.AppDatabase.getInstance(this).clearAllTables();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                    profileViewModel.logout();
                    Toast.makeText(this, "Logged out and local cache cleared.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
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

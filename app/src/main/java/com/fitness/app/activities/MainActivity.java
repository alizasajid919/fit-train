package com.fitness.app.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.fitness.app.R;
import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.fragments.DietFragment;
import com.fitness.app.fragments.HomeFragment;
import com.fitness.app.fragments.ProfileFragment;
import com.fitness.app.fragments.WorkoutsFragment;
import com.fitness.app.models.User;
import com.fitness.app.viewmodels.AuthViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private LocalDataManager localDb;
    private AuthViewModel authViewModel;
    private com.fitness.app.utils.SharedPreferencesManager prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        localDb = new LocalDataManager(this);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        prefs = new com.fitness.app.utils.SharedPreferencesManager(this);

        // Verify profile loaded, otherwise route to onboarding
        User user = localDb.getUser();
        if (user == null) {
            if (localDb.isOnboardingSeen()) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            } else {
                startActivity(new Intent(MainActivity.this, OnboardingActivity.class));
            }
            finish();
            return;
        }

        // Reschedule challenge notifications
        com.fitness.app.utils.ReminderScheduler.rescheduleChallengeReminders(this);

        // Bottom Navigation Bar Setup
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setItemActiveIndicatorColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT));

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            selectTab(itemId);
            return true;
        });

        // Setup default fragment load & selected tab restore
        if (savedInstanceState == null) {
            int restoredTabId = prefs.getSelectedTab(R.id.navigation_home);
            bottomNavigation.setSelectedItemId(restoredTabId);
        } else {
            int selectedItemId = bottomNavigation.getSelectedItemId();
            selectTab(selectedItemId);
        }

        // Handle deep link notification navigation
        handleIntentNavigation(getIntent(), bottomNavigation);

        // Silently attempt sync if connected
        trySyncLocalData();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        if (bottomNavigation != null) {
            handleIntentNavigation(intent, bottomNavigation);
        }
    }

    private void handleIntentNavigation(Intent intent, BottomNavigationView bottomNavigation) {
        if (intent == null) return;
        String navigateTo = intent.getStringExtra("navigate_to");
        if (navigateTo != null) {
            if ("diets".equalsIgnoreCase(navigateTo)) {
                bottomNavigation.setSelectedItemId(R.id.navigation_diets);
            } else if ("workouts".equalsIgnoreCase(navigateTo)) {
                bottomNavigation.setSelectedItemId(R.id.navigation_workouts);
            }
        }
    }

    private void selectTab(int itemId) {
        prefs.setSelectedTab(itemId);
        androidx.fragment.app.FragmentManager fm = getSupportFragmentManager();
        androidx.fragment.app.FragmentTransaction transaction = fm.beginTransaction();

        String targetTag = getFragmentTag(itemId);

        // Hide all added fragments
        for (String tag : new String[]{"HOME_FRAGMENT", "WORKOUTS_FRAGMENT", "DIETS_FRAGMENT", "PROFILE_FRAGMENT"}) {
            Fragment f = fm.findFragmentByTag(tag);
            if (f != null) {
                if (tag.equals(targetTag)) {
                    transaction.show(f);
                } else {
                    transaction.hide(f);
                }
            }
        }

        // Show or add the selected fragment
        Fragment targetFragment = fm.findFragmentByTag(targetTag);
        if (targetFragment == null) {
            targetFragment = createFragmentForId(itemId);
            transaction.add(R.id.fragment_container, targetFragment, targetTag);
        }

        transaction.commit();
    }

    private String getFragmentTag(int itemId) {
        if (itemId == R.id.navigation_workouts) return "WORKOUTS_FRAGMENT";
        if (itemId == R.id.navigation_diets) return "DIETS_FRAGMENT";
        if (itemId == R.id.navigation_profile) return "PROFILE_FRAGMENT";
        return "HOME_FRAGMENT";
    }

    private Fragment createFragmentForId(int itemId) {
        if (itemId == R.id.navigation_workouts) return new WorkoutsFragment();
        if (itemId == R.id.navigation_diets) return new DietFragment();
        if (itemId == R.id.navigation_profile) return new ProfileFragment();
        return new HomeFragment();
    }

    private void trySyncLocalData() {
        if (authViewModel.getCurrentUser() != null) {
            new Thread(() -> {
                try {
                    // Sync silently in background thread
                    com.fitness.app.repositories.UserRepository repo = new com.fitness.app.repositories.UserRepository();
                    repo.syncLocalDataToFirestore(localDb);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}

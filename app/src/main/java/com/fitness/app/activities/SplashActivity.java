package com.fitness.app.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.appcompat.app.AppCompatActivity;

import com.fitness.app.R;
import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private LocalDataManager localDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        localDb = new LocalDataManager(this);

        // Restore saved theme preference
        if (localDb.isDarkThemeEnabled()) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        }

        // Premium Logo Animation
        View iconView = findViewById(R.id.ivLogoIcon);
        View titleView = findViewById(R.id.tvLogoTitle);
        View sloganView = findViewById(R.id.tvLogoSlogan);

        iconView.setAlpha(0f);
        iconView.setScaleX(0.5f);
        iconView.setScaleY(0.5f);
        iconView.setRotation(-45f);

        titleView.setAlpha(0f);
        titleView.setScaleX(0.8f);
        titleView.setScaleY(0.8f);

        sloganView.setAlpha(0f);
        sloganView.setTranslationY(30f);

        iconView.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .rotation(0f)
                .setDuration(1000)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        titleView.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(1000)
                .setStartDelay(200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        sloganView.animate()
                .alpha(0.8f)
                .translationY(0f)
                .setDuration(1000)
                .setStartDelay(500)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        new Handler(Looper.getMainLooper()).postDelayed(this::checkUserSession, 2200);
    }

    private void checkUserSession() {
        if (localDb.isOnboardingSeen()) {
            navigateTo(MainActivity.class);
        } else {
            navigateTo(OnboardingActivity.class);
        }
    }

    private void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(SplashActivity.this, targetActivity);
        startActivity(intent);
        finish();
    }
}

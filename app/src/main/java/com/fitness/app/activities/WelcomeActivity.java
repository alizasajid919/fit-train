package com.fitness.app.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.fitness.app.R;
import com.fitness.app.utils.SharedPreferencesManager;

public class WelcomeActivity extends AppCompatActivity {

    private SharedPreferencesManager prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        prefs = new SharedPreferencesManager(this);

        findViewById(R.id.btnGetStarted).setOnClickListener(v -> {
            if (prefs.isOnboardingSeen()) {
                startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
            } else {
                startActivity(new Intent(WelcomeActivity.this, OnboardingActivity.class));
            }
            finish();
        });
    }
}

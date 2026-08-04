package com.fitness.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.fitness.app.R;
import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.data.room.AppDatabase;
import com.fitness.app.models.TransformationPlan;
import com.fitness.app.models.User;

public class TransformationIntroActivity extends AppCompatActivity {

    private LocalDataManager localDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        localDb = new LocalDataManager(this);
        User user = localDb.getUser();
        String userId = (user != null) ? user.getUid() : "test_user";

        // Check if user already has an active transformation plan
        TransformationPlan activePlan = AppDatabase.getInstance(this).fitnessDao().getActiveTransformationPlan(userId);
        if (activePlan != null) {
            startActivity(new Intent(this, TransformationDashboardActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_transformation_intro);

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());

        Button btnGetStarted = findViewById(R.id.btnGetStarted);
        btnGetStarted.setOnClickListener(v -> {
            startActivity(new Intent(TransformationIntroActivity.this, TransformationSetupActivity.class));
            finish();
        });
    }
}

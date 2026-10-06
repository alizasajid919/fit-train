package com.fitness.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

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
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

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

        View btnGetStarted = findViewById(R.id.btnGetStarted);
        if (btnGetStarted != null) {
            attachTouchScaleAnimation(btnGetStarted);
            btnGetStarted.setOnClickListener(v -> startSetupFlow());
        }

        View card1 = findViewById(R.id.cardFeature1);
        View card2 = findViewById(R.id.cardFeature2);
        View card3 = findViewById(R.id.cardFeature3);

        if (card1 != null) {
            attachTouchScaleAnimation(card1);
            card1.setOnClickListener(v -> startSetupFlow());
        }
        if (card2 != null) {
            attachTouchScaleAnimation(card2);
            card2.setOnClickListener(v -> startSetupFlow());
        }
        if (card3 != null) {
            attachTouchScaleAnimation(card3);
            card3.setOnClickListener(v -> startSetupFlow());
        }
    }

    private void startSetupFlow() {
        startActivity(new Intent(TransformationIntroActivity.this, TransformationSetupActivity.class));
        finish();
    }

    private void attachTouchScaleAnimation(View view) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(100).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                    break;
            }
            return false;
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}

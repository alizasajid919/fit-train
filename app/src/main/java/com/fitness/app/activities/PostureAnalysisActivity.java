package com.fitness.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fitness.app.R;
import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.models.User;

public class PostureAnalysisActivity extends AppCompatActivity {

    private LocalDataManager localDb;
    private TextView tvPostureScoreTitle, tvPostureScoreSub;
    private TextView tvHeadStatus, tvShoulderStatus, tvSpineStatus, tvDeskStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_posture_analysis);

        localDb = new LocalDataManager(this);

        // Bind Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        // Bind Views
        tvPostureScoreTitle = findViewById(R.id.tvPostureScoreTitle);
        tvPostureScoreSub = findViewById(R.id.tvPostureScoreSub);
        tvHeadStatus = findViewById(R.id.tvHeadStatus);
        tvShoulderStatus = findViewById(R.id.tvShoulderStatus);
        tvSpineStatus = findViewById(R.id.tvSpineStatus);
        tvDeskStatus = findViewById(R.id.tvDeskStatus);

        findViewById(R.id.btnStartPostureCheck).setOnClickListener(v -> {
            // Run posture alignment scan
            Toast.makeText(this, "Posture alignment check initiated! Stand upright facing camera.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, RealTimeFeedbackActivity.class);
            intent.putExtra("exercise_name", "Standing Posture Check");
            startActivity(intent);
        });

        loadPostureData();
    }

    private void loadPostureData() {
        User user = localDb.getUser();
        String medical = (user != null && user.getMedicalConditions() != null) ? user.getMedicalConditions().toLowerCase() : "";

        if (medical.contains("joint") || medical.contains("arthritis") || medical.contains("hypertension")) {
            tvPostureScoreTitle.setText("Overall Posture Score: 85 / 100");
            tvPostureScoreSub.setText("Medical condition detected: Gentle joint-friendly alignment posture recommended.");
            tvDeskStatus.setText("Take 10m walking stretch break");
        } else {
            tvPostureScoreTitle.setText("Overall Posture Score: 92 / 100");
            tvPostureScoreSub.setText("Excellent upright posture & neutral shoulder alignment.");
            tvDeskStatus.setText("Optimal ergonomic position");
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}

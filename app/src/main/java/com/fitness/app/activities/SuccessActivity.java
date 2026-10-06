package com.fitness.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.fitness.app.R;

public class SuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        TextView tvWelcomeName = findViewById(R.id.tvWelcomeName);
        String firstName = getIntent().getStringExtra("first_name");
        
        if (firstName != null && !firstName.isEmpty()) {
            tvWelcomeName.setText(getString(R.string.welcome_success, firstName));
        } else {
            tvWelcomeName.setText("Welcome!");
        }

        View btnGoToHome = findViewById(R.id.btnGoToHome);
        if (btnGoToHome != null) {
            btnGoToHome.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.animate().scaleX(0.96f).scaleY(0.96f).setDuration(100).start();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                        break;
                }
                return false;
            });

            btnGoToHome.setOnClickListener(v -> {
                startActivity(new Intent(SuccessActivity.this, MainActivity.class));
                finish();
            });
        }
    }
}

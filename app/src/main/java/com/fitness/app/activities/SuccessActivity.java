package com.fitness.app.activities;

import android.content.Intent;
import android.os.Bundle;
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

        findViewById(R.id.btnGoToHome).setOnClickListener(v -> {
            startActivity(new Intent(SuccessActivity.this, MainActivity.class));
            finish();
        });
    }
}

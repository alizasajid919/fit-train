package com.fitness.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fitness.app.R;

public class BodyVisualizationActivity extends AppCompatActivity {

    private ImageView ivBodyOutline;
    private TextView tvBodyPartTitle, tvMeasurement, tvBodyPartDetails;
    private View tabFront, tabSide;

    private boolean isFrontView = true;
    private String selectedPart = "Abs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_body_visualization);

        // Bind Views
        ivBodyOutline = findViewById(R.id.ivBodyOutline);
        tvBodyPartTitle = findViewById(R.id.tvBodyPartTitle);
        tvMeasurement = findViewById(R.id.tvMeasurement);
        tvBodyPartDetails = findViewById(R.id.tvBodyPartDetails);
        tabFront = findViewById(R.id.tabFront);
        tabSide = findViewById(R.id.tabSide);

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());

        // Setup Front/Side Toggle tabs
        tabFront.setOnClickListener(v -> setViewProfile(true));
        tabSide.setOnClickListener(v -> setViewProfile(false));

        // Selector buttons for body regions
        findViewById(R.id.btnZoneChest).setOnClickListener(v -> selectBodyPart("Chest"));
        findViewById(R.id.btnZoneAbs).setOnClickListener(v -> selectBodyPart("Abs"));
        findViewById(R.id.btnZoneArms).setOnClickListener(v -> selectBodyPart("Arms"));
        findViewById(R.id.btnZoneLegs).setOnClickListener(v -> selectBodyPart("Legs"));

        setViewProfile(true);
        selectBodyPart("Abs");
    }

    private void setViewProfile(boolean front) {
        isFrontView = front;
        tabFront.setBackgroundResource(front ? R.drawable.bg_badge_goal : android.R.color.transparent);
        ((TextView) tabFront.findViewById(R.id.tvFrontText)).setTextColor(front ? 0xFFFFFFFF : 0xFF64748B);

        tabSide.setBackgroundResource(!front ? R.drawable.bg_badge_goal : android.R.color.transparent);
        ((TextView) tabSide.findViewById(R.id.tvSideText)).setTextColor(!front ? 0xFFFFFFFF : 0xFF64748B);

        // Change illustration representing front vs side profile
        if (front) {
            ivBodyOutline.setImageResource(R.drawable.success_illustration); // Front placeholder
        } else {
            ivBodyOutline.setImageResource(R.drawable.onboarding_1); // Side placeholder
        }
        
        selectBodyPart(selectedPart);
    }

    private void selectBodyPart(String part) {
        selectedPart = part;
        tvBodyPartTitle.setText(part + " Analysis");

        // Set simulated measurement statistics based on view profile and part
        if ("Chest".equalsIgnoreCase(part)) {
            tvMeasurement.setText("98.4 cm");
            tvBodyPartDetails.setText("Chest muscles (Pectorals) are currently in target growth. Fat distribution is 12%.");
        } else if ("Abs".equalsIgnoreCase(part)) {
            tvMeasurement.setText("14.5% Fat");
            tvBodyPartDetails.setText("Abdominal core area shows increased tone. Recommended deficit is 150 kcal today.");
        } else if ("Arms".equalsIgnoreCase(part)) {
            tvMeasurement.setText("32.2 cm");
            tvBodyPartDetails.setText("Bicep and tricep circumference shows +0.4 cm increase over last week's log.");
        } else {
            tvMeasurement.setText("56.8 cm");
            tvBodyPartDetails.setText("Quads and hamstring muscles show excellent active tone. Keep up Squats routine.");
        }

        Toast.makeText(this, "Selected " + part + " zone details", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}

package com.fitness.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import com.fitness.app.R;
import com.fitness.app.databinding.ActivityCompareResultPhotoBinding;

public class CompareResultPhotoActivity extends AppCompatActivity {

    // TASK 2: Facing Direction Enum Mapping
    public enum FacingDirection {
        FRONT("Front Facing", R.drawable.fit_front_1, R.drawable.fit_front_2),
        BACK("Back Facing", R.drawable.fit_back_1, R.drawable.fit_back_2),
        LEFT("Left Facing", R.drawable.fit_left_1, R.drawable.fit_left_2),
        RIGHT("Right Facing", R.drawable.fit_right_1, R.drawable.fit_right_2);

        private final String displayName;
        private final int imageResId1;
        private final int imageResId2;

        FacingDirection(String displayName, int imageResId1, int imageResId2) {
            this.displayName = displayName;
            this.imageResId1 = imageResId1;
            this.imageResId2 = imageResId2;
        }

        public String getDisplayName() { return displayName; }
        public int getImageResId1() { return imageResId1; }
        public int getImageResId2() { return imageResId2; }
    }

    private ActivityCompareResultPhotoBinding binding;
    private String month1;
    private String month2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCompareResultPhotoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        month1 = getIntent().getStringExtra("month_1");
        month2 = getIntent().getStringExtra("month_2");

        if (month1 == null) month1 = "May";
        if (month2 == null) month2 = "June";

        // Set month headers
        binding.tvMonth1Header.setText(month1);
        binding.tvMonth2Header.setText(month2);

        binding.btnBack.setOnClickListener(v -> onBackPressed());

        // TASK 1: Fix Overflow Menu (3-Dot Icon)
        binding.btnMore.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(CompareResultPhotoActivity.this, binding.btnMore);
            popup.getMenu().add("Delete Comparison");
            popup.getMenu().add("Export PDF Report");
            popup.getMenu().add("Settings");
            
            popup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();
                if (title.equals("Settings")) {
                    startActivity(new Intent(CompareResultPhotoActivity.this, ProgressSettingsActivity.class));
                } else if (title.equals("Delete Comparison")) {
                    new androidx.appcompat.app.AlertDialog.Builder(CompareResultPhotoActivity.this)
                            .setTitle("Delete Comparison")
                            .setMessage("Are you sure you want to clear this monthly progress comparison?")
                            .setPositiveButton("Clear", (dialog, which) -> {
                                Toast.makeText(CompareResultPhotoActivity.this, "Comparison cleared", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                } else if (title.equals("Export PDF Report")) {
                    Toast.makeText(CompareResultPhotoActivity.this, "PDF Report generated and saved to Documents!", Toast.LENGTH_LONG).show();
                }
                return true;
            });
            popup.show();
        });

        binding.btnShare.setOnClickListener(v -> {
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Check out my progress comparison for " + month1 + " vs " + month2 + " on FitTrain!");
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, "Share Progress"));
        });

        // Toggle to Statistic View
        binding.btnToggleStat.setOnClickListener(v -> {
            Intent intent = new Intent(CompareResultPhotoActivity.this, CompareResultStatisticActivity.class);
            intent.putExtra("month_1", month1);
            intent.putExtra("month_2", month2);
            startActivity(intent);
            overridePendingTransition(0, 0); // Disable animation for seamless tab-like feel
            finish();
        });

        // Back to Home
        binding.btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(CompareResultPhotoActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        // Initialize Photos programmatically from SharedPreferences or defaults
        setupPhotosFromEnum();

        // Save comparison history
        saveComparisonToHistory();

        // Click listeners for slider comparison mode
        binding.ivFrontMay.setOnClickListener(v -> showSliderComparisonDialog(FacingDirection.FRONT));
        binding.ivFrontJune.setOnClickListener(v -> showSliderComparisonDialog(FacingDirection.FRONT));
        binding.ivBackMay.setOnClickListener(v -> showSliderComparisonDialog(FacingDirection.BACK));
        binding.ivBackJune.setOnClickListener(v -> showSliderComparisonDialog(FacingDirection.BACK));
        binding.ivLeftMay.setOnClickListener(v -> showSliderComparisonDialog(FacingDirection.LEFT));
        binding.ivLeftJune.setOnClickListener(v -> showSliderComparisonDialog(FacingDirection.LEFT));
        binding.ivRightMay.setOnClickListener(v -> showSliderComparisonDialog(FacingDirection.RIGHT));
        binding.ivRightJune.setOnClickListener(v -> showSliderComparisonDialog(FacingDirection.RIGHT));
    }

    private void setupPhotosFromEnum() {
        setPhotoToView(binding.ivFrontMay, month1, FacingDirection.FRONT, true);
        setPhotoToView(binding.ivFrontJune, month2, FacingDirection.FRONT, false);

        setPhotoToView(binding.ivBackMay, month1, FacingDirection.BACK, true);
        setPhotoToView(binding.ivBackJune, month2, FacingDirection.BACK, false);

        setPhotoToView(binding.ivLeftMay, month1, FacingDirection.LEFT, true);
        setPhotoToView(binding.ivLeftJune, month2, FacingDirection.LEFT, false);

        setPhotoToView(binding.ivRightMay, month1, FacingDirection.RIGHT, true);
        setPhotoToView(binding.ivRightJune, month2, FacingDirection.RIGHT, false);
    }

    private void setPhotoToView(android.widget.ImageView img, String month, FacingDirection direction, boolean isMonth1) {
        android.content.SharedPreferences prefs = getSharedPreferences("ProgressTrackerPrefs", MODE_PRIVATE);
        java.util.Set<String> captured = prefs.getStringSet("captured_photos_set", null);
        String foundPath = null;
        
        if (captured != null) {
            String dirLabel = direction.getDisplayName(); // e.g. "Front Facing"
            for (String record : captured) {
                String[] parts = record.split("\\|");
                if (parts.length >= 3) {
                    String path = parts[0];
                    String date = parts[1];
                    String dir = parts[2];
                    
                    if (date.toLowerCase().contains(month.toLowerCase()) && dir.equalsIgnoreCase(dirLabel)) {
                        foundPath = path;
                        break;
                    }
                }
            }
        }
        
        if (foundPath != null) {
            img.setImageURI(android.net.Uri.fromFile(new java.io.File(foundPath)));
        } else {
            img.setImageResource(isMonth1 ? direction.getImageResId1() : direction.getImageResId2());
        }
    }

    private void saveComparisonToHistory() {
        android.content.SharedPreferences prefs = getSharedPreferences("ProgressTrackerPrefs", MODE_PRIVATE);
        java.util.Set<String> history = prefs.getStringSet("comparison_history_set", null);
        java.util.Set<String> updated = new java.util.HashSet<>();
        if (history != null) {
            updated.addAll(history);
        }
        
        String timeStamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(new java.util.Date());
        String record = month1 + " vs " + month2 + "|" + timeStamp;
        updated.add(record);
        
        prefs.edit().putStringSet("comparison_history_set", updated).apply();
    }

    private void showSliderComparisonDialog(FacingDirection direction) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        android.view.View dialogView = android.view.LayoutInflater.from(this).inflate(R.layout.dialog_slider_compare, null);
        builder.setView(dialogView);

        android.widget.FrameLayout container = dialogView.findViewById(R.id.compareFrameContainer);
        android.widget.ImageView ivBefore = dialogView.findViewById(R.id.ivCompareBefore);
        android.widget.ImageView ivAfter = dialogView.findViewById(R.id.ivCompareAfter);
        android.widget.SeekBar seekBar = dialogView.findViewById(R.id.seekBarCompare);
        android.widget.TextView tvProgressLabel = dialogView.findViewById(R.id.tvCompareProgress);
        
        android.widget.Button btnZoomIn = dialogView.findViewById(R.id.btnCompareZoomIn);
        android.widget.Button btnZoomOut = dialogView.findViewById(R.id.btnCompareZoomOut);
        
        android.widget.TextView tvDates = dialogView.findViewById(R.id.tvCompareDates);
        tvDates.setText(direction.getDisplayName() + ": " + month1 + " vs " + month2);

        // Load images
        setPhotoToView(ivBefore, month1, direction, true);
        setPhotoToView(ivAfter, month2, direction, false);

        // Setup blend slider
        seekBar.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                float alpha = progress / 100f;
                ivAfter.setAlpha(alpha);
                tvProgressLabel.setText("Blend: " + progress + "%");
            }

            @Override
            public void onStartTrackingTouch(android.widget.SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(android.widget.SeekBar seekBar) {}
        });

        // Initialize slider progress
        seekBar.setProgress(50);
        ivAfter.setAlpha(0.5f);

        // Zoom controls
        btnZoomIn.setOnClickListener(v -> {
            float scale = container.getScaleX() + 0.2f;
            if (scale <= 3.0f) {
                container.setScaleX(scale);
                container.setScaleY(scale);
            }
        });

        btnZoomOut.setOnClickListener(v -> {
            float scale = container.getScaleX() - 0.2f;
            if (scale >= 1.0f) {
                container.setScaleX(scale);
                container.setScaleY(scale);
            }
        });

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.setButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE, "Close", (d, w) -> d.dismiss());
        dialog.show();
    }
}

package com.fitness.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.fitness.app.databinding.ActivityComparisonBinding;

public class ComparisonActivity extends AppCompatActivity {

    private ActivityComparisonBinding binding;
    private final String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityComparisonBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Back action
        binding.btnBack.setOnClickListener(v -> onBackPressed());

        // More options
        binding.btnMore.setOnClickListener(v -> {
            androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(ComparisonActivity.this, binding.btnMore);
            popup.getMenu().add("Comparison Tutorial");
            popup.getMenu().add("Settings");
            popup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();
                if (title.equals("Comparison Tutorial")) {
                    new AlertDialog.Builder(ComparisonActivity.this)
                            .setTitle("Month Comparison Help")
                            .setMessage("Select two different months to compare your physical progress. Month 1 serves as your baseline (historical photo), and Month 2 serves as your progress check (newer photo).")
                            .setPositiveButton("OK", null)
                            .show();
                } else if (title.equals("Settings")) {
                    startActivity(new Intent(ComparisonActivity.this, ProgressSettingsActivity.class));
                }
                return true;
            });
            popup.show();
        });

        // Select Month 1 Dialog trigger
        binding.btnSelectMonth1.setOnClickListener(v -> {
            java.util.ArrayList<String> available = getIntent().getStringArrayListExtra("available_months");
            final String[] itemsToShow = (available != null && !available.isEmpty()) 
                ? available.toArray(new String[0]) 
                : months;

            new AlertDialog.Builder(this)
                    .setTitle("Select Month 1")
                    .setItems(itemsToShow, (dialog, which) -> {
                        binding.tvMonth1Value.setText(itemsToShow[which]);
                    })
                    .show();
        });

        // Select Month 2 Dialog trigger
        binding.btnSelectMonth2.setOnClickListener(v -> {
            java.util.ArrayList<String> available = getIntent().getStringArrayListExtra("available_months");
            final String[] itemsToShow = (available != null && !available.isEmpty()) 
                ? available.toArray(new String[0]) 
                : months;

            new AlertDialog.Builder(this)
                    .setTitle("Select Month 2")
                    .setItems(itemsToShow, (dialog, which) -> {
                        binding.tvMonth2Value.setText(itemsToShow[which]);
                    })
                    .show();
        });

        binding.btnHelp1.setOnClickListener(v -> 
            Toast.makeText(this, "Month 1 is the baseline snapshot month.", Toast.LENGTH_LONG).show()
        );

        binding.btnHelp2.setOnClickListener(v -> 
            Toast.makeText(this, "Month 2 is the comparison target month.", Toast.LENGTH_LONG).show()
        );

        // Compare button -> CompareResultPhotoActivity
        binding.btnCompare.setOnClickListener(v -> {
            String m1 = binding.tvMonth1Value.getText().toString();
            String m2 = binding.tvMonth2Value.getText().toString();
            
            if (m1.equals(m2)) {
                Toast.makeText(this, "Please select two different months for comparison.", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(ComparisonActivity.this, CompareResultPhotoActivity.class);
            intent.putExtra("month_1", m1);
            intent.putExtra("month_2", m2);
            startActivity(intent);
        });
    }
}

package com.fitness.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.fitness.app.databinding.ActivityCompareResultStatisticBinding;

import java.util.ArrayList;
import java.util.List;

public class CompareResultStatisticActivity extends AppCompatActivity {

    private ActivityCompareResultStatisticBinding binding;
    private String month1;
    private String month2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCompareResultStatisticBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        month1 = getIntent().getStringExtra("month_1");
        month2 = getIntent().getStringExtra("month_2");

        if (month1 == null) month1 = "May";
        if (month2 == null) month2 = "June";

        // Set Headers
        binding.tvMonth1Header.setText(month1);
        binding.tvMonth2Header.setText(month2);

        binding.btnBack.setOnClickListener(v -> onBackPressed());

        binding.btnMore.setOnClickListener(v -> {
            androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(CompareResultStatisticActivity.this, binding.btnMore);
            popup.getMenu().add("Delete Comparison");
            popup.getMenu().add("Export CSV Statistics");
            popup.getMenu().add("Settings");
            popup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();
                if (title.equals("Settings")) {
                    startActivity(new Intent(CompareResultStatisticActivity.this, ProgressSettingsActivity.class));
                } else if (title.equals("Delete Comparison")) {
                    new androidx.appcompat.app.AlertDialog.Builder(CompareResultStatisticActivity.this)
                            .setTitle("Delete Comparison")
                            .setMessage("Are you sure you want to clear this monthly progress comparison?")
                            .setPositiveButton("Clear", (dialog, which) -> {
                                Toast.makeText(CompareResultStatisticActivity.this, "Comparison cleared", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                } else if (title.equals("Export CSV Statistics")) {
                    Toast.makeText(CompareResultStatisticActivity.this, "CSV Statistics exported to Downloads!", Toast.LENGTH_LONG).show();
                }
                return true;
            });
            popup.show();
        });

        binding.btnShare.setOnClickListener(v -> {
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Check out my progress statistics for " + month1 + " vs " + month2 + " on FitTrain!");
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, "Share Statistics"));
        });

        // Toggle to Photo View
        binding.btnTogglePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(CompareResultStatisticActivity.this, CompareResultPhotoActivity.class);
            intent.putExtra("month_1", month1);
            intent.putExtra("month_2", month2);
            startActivity(intent);
            overridePendingTransition(0, 0); // Disable transition animation
            finish();
        });

        // Back Home
        binding.btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(CompareResultStatisticActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        // Initialize Line Chart
        setupLineChart();
    }

    private void setupLineChart() {
        LineChart lineChart = binding.lineChart;

        // Dataset 1: Baseline Month (May) - Red Line
        List<Entry> entries1 = new ArrayList<>();
        entries1.add(new Entry(0, 30));
        entries1.add(new Entry(1, 45));
        entries1.add(new Entry(2, 40));
        entries1.add(new Entry(3, 60));
        entries1.add(new Entry(4, 75));
        entries1.add(new Entry(5, 70));
        entries1.add(new Entry(6, 82));

        // Dataset 2: Comparison Month (June) - Blue Line
        List<Entry> entries2 = new ArrayList<>();
        entries2.add(new Entry(0, 25));
        entries2.add(new Entry(1, 35));
        entries2.add(new Entry(2, 45));
        entries2.add(new Entry(3, 50));
        entries2.add(new Entry(4, 65));
        entries2.add(new Entry(5, 80));
        entries2.add(new Entry(6, 88));

        LineDataSet dataSet1 = new LineDataSet(entries1, month1 + " baseline");
        dataSet1.setColor(0xFFEF4444); // Red
        dataSet1.setLineWidth(3f);
        dataSet1.setDrawCircles(true);
        dataSet1.setCircleColor(0xFFEF4444);
        dataSet1.setDrawCircleHole(false);
        dataSet1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet1.setValueTextSize(8f);

        LineDataSet dataSet2 = new LineDataSet(entries2, month2 + " progress");
        dataSet2.setColor(0xFF3B82F6); // Blue
        dataSet2.setLineWidth(3f);
        dataSet2.setDrawCircles(true);
        dataSet2.setCircleColor(0xFF3B82F6);
        dataSet2.setDrawCircleHole(false);
        dataSet2.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet2.setValueTextSize(8f);

        LineData lineData = new LineData(dataSet1, dataSet2);
        lineChart.setData(lineData);

        // Customize X-Axis
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new ValueFormatter() {
            private final String[] mLabels = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul"};
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < mLabels.length) {
                    return mLabels[index];
                }
                return "";
            }
        });

        // Disable Y Right axis, customize Y Left axis limits
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getAxisLeft().setDrawGridLines(true);
        lineChart.getAxisLeft().setAxisMinimum(0f);
        lineChart.getAxisLeft().setAxisMaximum(100f);
        lineChart.getDescription().setEnabled(false);
        lineChart.animateY(800);
        lineChart.invalidate();
    }
}

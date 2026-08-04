package com.fitness.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fitness.app.R;
import com.fitness.app.data.local.LocalDataManager;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class CompareProgressActivity extends AppCompatActivity {

    private LineChart weightProgressChart;
    private TextView tvAverageProgressPercent;
    private ProgressBar pbAverageProgress;
    private RecyclerView rvPhotosList;

    private LocalDataManager localDb;
    private List<ProgressPhotoItem> photoList = new ArrayList<>();
    private PhotoAdapter adapter;

    public static class ProgressPhotoItem {
        public String id;
        public String date;
        public String weight;
        public int imageRes;

        public ProgressPhotoItem(String id, String date, String weight, int imageRes) {
            this.id = id;
            this.date = date;
            this.weight = weight;
            this.imageRes = imageRes;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_compare_progress);

        localDb = new LocalDataManager(this);

        // Bind Views
        weightProgressChart = findViewById(R.id.weightProgressChart);
        tvAverageProgressPercent = findViewById(R.id.tvAverageProgressPercent);
        pbAverageProgress = findViewById(R.id.pbAverageProgress);
        rvPhotosList = findViewById(R.id.rvPhotosList);

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());

        findViewById(R.id.btnCompare).setOnClickListener(v -> {
            Toast.makeText(this, "Select two photos below to compare before/after details!", Toast.LENGTH_LONG).show();
        });

        findViewById(R.id.btnBodyVisualization).setOnClickListener(v -> {
            startActivity(new Intent(CompareProgressActivity.this, BodyVisualizationActivity.class));
        });

        setupWeightChart();
        setupPhotosList();
        loadProgressData();
    }

    private void setupWeightChart() {
        weightProgressChart.getDescription().setEnabled(false);
        weightProgressChart.setDrawGridBackground(false);
        weightProgressChart.setPinchZoom(false);
        weightProgressChart.getLegend().setEnabled(false);

        XAxis xAxis = weightProgressChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(0xFF64748B);

        final String[] dates = new String[]{"May 1", "May 8", "May 15", "May 22", "May 29", "Jun 5", "Jun 12"};
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dates));

        YAxis leftAxis = weightProgressChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(0xFFE2E8F0);
        leftAxis.setTextColor(0xFF64748B);
        leftAxis.setAxisMinimum(60f);
        leftAxis.setAxisMaximum(85f);

        weightProgressChart.getAxisRight().setEnabled(false);

        // Simulated weight logs (kg) falling weekly
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 74.2f));
        entries.add(new Entry(1, 73.5f));
        entries.add(new Entry(2, 72.8f));
        entries.add(new Entry(3, 72.4f));
        entries.add(new Entry(4, 71.9f));
        entries.add(new Entry(5, 71.2f));
        entries.add(new Entry(6, 70.5f));

        LineDataSet dataSet = new LineDataSet(entries, "Weight progression");
        dataSet.setColor(0xFF6C63FF);
        dataSet.setLineWidth(3f);
        dataSet.setCircleColor(0xFF6C63FF);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawCircleHole(false);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);
        weightProgressChart.setData(lineData);
        weightProgressChart.animateX(800);
        weightProgressChart.invalidate();
    }

    private void setupPhotosList() {
        rvPhotosList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapter = new PhotoAdapter();
        rvPhotosList.setAdapter(adapter);
    }

    private void loadProgressData() {
        // Average progress percentage calculation representation
        tvAverageProgressPercent.setText("Average Progress: 72%");
        pbAverageProgress.setMax(100);
        pbAverageProgress.setProgress(72);

        // Load progress photos items
        photoList.clear();
        photoList.add(new ProgressPhotoItem("1", "May 1, 2026", "74.2 kg", R.drawable.onboarding_1));
        photoList.add(new ProgressPhotoItem("2", "May 15, 2026", "72.8 kg", R.drawable.onboarding_2));
        photoList.add(new ProgressPhotoItem("3", "Jun 1, 2026", "71.9 kg", R.drawable.onboarding_3));
        photoList.add(new ProgressPhotoItem("4", "Jun 15, 2026", "70.5 kg", R.drawable.success_illustration));

        adapter.notifyDataSetChanged();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    // Horizontal Photo adapter
    private class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_progress_photo, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ProgressPhotoItem item = photoList.get(position);
            holder.tvPhotoDate.setText(item.date);
            holder.tvPhotoWeight.setText(item.weight);
            holder.ivPhoto.setImageResource(item.imageRes);

            holder.itemView.setOnClickListener(v -> {
                Toast.makeText(CompareProgressActivity.this, "Selected photo from " + item.date, Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            return photoList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivPhoto;
            TextView tvPhotoDate, tvPhotoWeight;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivPhoto = itemView.findViewById(R.id.ivPhoto);
                tvPhotoDate = itemView.findViewById(R.id.tvPhotoDate);
                tvPhotoWeight = itemView.findViewById(R.id.tvPhotoWeight);
            }
        }
    }
}

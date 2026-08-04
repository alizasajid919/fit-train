package com.fitness.app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.gridlayout.widget.GridLayout;

import com.fitness.app.R;
import com.fitness.app.databinding.ActivityProgressPhotoBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProgressPhotoActivity extends AppCompatActivity {

    public static class PhotoItem {
        public final String date;
        public final int imageResId;
        public final String imagePath;
        public boolean isSelected = false;

        public PhotoItem(String date, int imageResId) {
            this(date, imageResId, null);
        }

        public PhotoItem(String date, int imageResId, String imagePath) {
            this.date = date;
            this.imageResId = imageResId;
            this.imagePath = imagePath;
        }
    }

    private ActivityProgressPhotoBinding binding;
    private SharedPreferences sharedPrefs;
    private final List<PhotoItem> photoList = new ArrayList<>();
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProgressPhotoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPrefs = getSharedPreferences("ProgressTrackerPrefs", MODE_PRIVATE);
        sharedPrefs.edit()
                .putBoolean("photo_reminder_dismissed", false)
                .putBoolean("show_promo_card", true)
                .apply();

        // Populate baseline sample pictures matching user's layout
        initializeSamplePhotos();

        // Back Arrow
        binding.btnBack.setOnClickListener(v -> onBackPressed());

        // TASK 1: Overflow Menu (3-Dot Icon)
        binding.btnMore.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(ProgressPhotoActivity.this, binding.btnMore);
            popup.getMenu().add("Upload from Gallery");
            popup.getMenu().add("Delete Photo");
            popup.getMenu().add("Share Gallery");
            popup.getMenu().add("Edit Gallery");
            popup.getMenu().add("Settings");
            
            popup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();
                if (title.equals("Upload from Gallery")) {
                    triggerGalleryPicker();
                } else if (title.equals("Delete Photo")) {
                    triggerDeletePhotoAction();
                } else if (title.equals("Share Gallery")) {
                    triggerShareGalleryAction();
                } else if (title.equals("Edit Gallery")) {
                    enableEditMode(true);
                } else if (title.equals("Settings")) {
                    startActivity(new Intent(ProgressPhotoActivity.this, ProgressSettingsActivity.class));
                }
                return true;
            });
            popup.show();
        });

        // TASK 3: Reminder Banner Backend Logic
        setupReminderBanner();

        // TASK 5: promo card learn more
        setupPromoCard();

        // Gallery See More
        binding.tvSeeMore.setOnClickListener(v -> 
            Toast.makeText(this, "Gallery view full size", Toast.LENGTH_SHORT).show()
        );

        // Compare button -> launches Comparison Screen
        binding.btnCompare.setOnClickListener(v -> {
            Intent intent = new Intent(ProgressPhotoActivity.this, ComparisonActivity.class);
            ArrayList<String> availableMonths = new ArrayList<>();
            for (PhotoItem item : photoList) {
                String[] parts = item.date.split(" ");
                if (parts.length > 1) {
                    String month = parts[1];
                    if (!availableMonths.contains(month)) {
                        availableMonths.add(month);
                    }
                }
            }
            intent.putStringArrayListExtra("available_months", availableMonths);
            startActivity(intent);
        });

        // TASK 4: FAB Camera -> launches Take Photo Screen
        binding.fabCamera.setOnClickListener(v -> {
            Intent intent = new Intent(ProgressPhotoActivity.this, TakePhotoActivity.class);
            startActivity(intent);
        });

        // Edit actions buttons
        binding.btnCancelEdit.setOnClickListener(v -> enableEditMode(false));
        binding.btnDeleteSelected.setOnClickListener(v -> triggerBulkDeleteAction());

        // Bottom Nav bar mock navigations
        binding.navHome.setOnClickListener(v -> {
            Intent intent = new Intent(ProgressPhotoActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        binding.navCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(ProgressPhotoActivity.this, WorkoutScheduleActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.navCenterCamera).setOnClickListener(v -> 
            Toast.makeText(this, "Already on Progress Photo Screen", Toast.LENGTH_SHORT).show()
        );

        binding.navGallery.setOnClickListener(v -> {
            Intent intent = new Intent(ProgressPhotoActivity.this, ProgressTrackerActivity.class);
            startActivity(intent);
        });

        binding.navProfile.setOnClickListener(v -> {
            startActivity(new Intent(ProgressPhotoActivity.this, ProfileActivity.class));
        });

        // Initial populate
        refreshGallery();
    }

    private void initializeSamplePhotos() {
        photoList.clear();

        // 1. Read dynamically captured photos
        java.util.Set<String> captured = sharedPrefs.getStringSet("captured_photos_set", null);
        if (captured != null) {
            for (String record : captured) {
                String[] parts = record.split("\\|");
                if (parts.length >= 3) {
                    String path = parts[0];
                    String date = parts[1];
                    // parts[2] is facing direction, we map it dynamically
                    photoList.add(new PhotoItem(date, 0, path));
                }
            }
        }

        // 2. Add baseline sample pictures matching user's layout
        // 2 June
        photoList.add(new PhotoItem("2 June", R.drawable.fit_front_1));
        photoList.add(new PhotoItem("2 June", R.drawable.fit_back_1));
        photoList.add(new PhotoItem("2 June", R.drawable.fit_left_1));

        // 5 May
        photoList.add(new PhotoItem("5 May", R.drawable.fit_front_2));
        photoList.add(new PhotoItem("5 May", R.drawable.fit_back_2));
        photoList.add(new PhotoItem("5 May", R.drawable.fit_right_2));
    }

    private void refreshGallery() {
        binding.layoutGalleryContainer.removeAllViews();

        if (photoList.isEmpty()) {
            TextView emptyTv = new TextView(this);
            emptyTv.setText("No photos logged yet.");
            emptyTv.setPadding(16, 32, 16, 32);
            emptyTv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            binding.layoutGalleryContainer.addView(emptyTv);
            return;
        }

        // Group items by date using LinkedHashMap to preserve insertion order
        Map<String, List<PhotoItem>> grouped = new LinkedHashMap<>();
        for (PhotoItem item : photoList) {
            if (!grouped.containsKey(item.date)) {
                grouped.put(item.date, new ArrayList<>());
            }
            grouped.get(item.date).add(item);
        }

        for (Map.Entry<String, List<PhotoItem>> entry : grouped.entrySet()) {
            String date = entry.getKey();
            List<PhotoItem> items = entry.getValue();

            // 1. Group Date Label
            TextView dateTitle = new TextView(this);
            dateTitle.setText(date);
            dateTitle.setTextColor(Color.parseColor("#64748B"));
            dateTitle.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 12);
            dateTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            
            LinearLayout.LayoutParams lpDate = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lpDate.setMargins(0, 0, 0, 8 * (int)getResources().getDisplayMetrics().density);
            dateTitle.setLayoutParams(lpDate);
            binding.layoutGalleryContainer.addView(dateTitle);

            // 2. GridLayout for images
            GridLayout grid = new GridLayout(this);
            grid.setColumnCount(3);
            grid.setUseDefaultMargins(true);
            
            LinearLayout.LayoutParams lpGrid = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lpGrid.setMargins(0, 0, 0, 16 * (int)getResources().getDisplayMetrics().density);
            grid.setLayoutParams(lpGrid);

            // Populate images into grid
            for (PhotoItem item : items) {
                View itemView = LayoutInflater.from(this).inflate(R.layout.item_gallery_photo, grid, false);
                CardView card = (CardView) itemView;
                
                ImageView img = itemView.findViewById(R.id.ivProgressImage);
                View highlight = itemView.findViewById(R.id.viewHighlight);
                CheckBox cb = itemView.findViewById(R.id.cbSelect);

                if (item.imagePath != null) {
                    img.setImageURI(android.net.Uri.fromFile(new java.io.File(item.imagePath)));
                } else {
                    img.setImageResource(item.imageResId);
                }

                // Set selection states based on edit mode
                if (isEditMode) {
                    cb.setVisibility(View.VISIBLE);
                    cb.setChecked(item.isSelected);
                    highlight.setVisibility(item.isSelected ? View.VISIBLE : View.GONE);
                } else {
                    cb.setVisibility(View.GONE);
                    highlight.setVisibility(View.GONE);
                }

                // Click listener
                card.setOnClickListener(v -> {
                    if (isEditMode) {
                        item.isSelected = !item.isSelected;
                        cb.setChecked(item.isSelected);
                        highlight.setVisibility(item.isSelected ? View.VISIBLE : View.GONE);
                    } else {
                        // View mode zoom preview dialog
                        showPhotoZoomDialog(item);
                    }
                });

                // Configure equal 3-column weighting for GridLayout child CardViews
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f); // Weight = 1f
                params.width = 0;
                params.height = (int) (100 * getResources().getDisplayMetrics().density);
                int margin = (int) (4 * getResources().getDisplayMetrics().density);
                params.setMargins(margin, margin, margin, margin);
                card.setLayoutParams(params);

                grid.addView(card);
            }

            binding.layoutGalleryContainer.addView(grid);
        }
    }


    private static final int REQUEST_CODE_GALLERY = 2001;

    private void triggerGalleryPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK && data != null) {
            android.net.Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                String compressedPath = compressAndSaveImage(selectedImageUri);
                if (compressedPath != null) {
                    saveCapturedPhotoMetadata(compressedPath);
                } else {
                    Toast.makeText(this, "Failed to compress image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private String compressAndSaveImage(android.net.Uri imageUri) {
        try {
            android.graphics.Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            java.io.File picturesDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
            if (picturesDir != null && !picturesDir.exists()) {
                picturesDir.mkdirs();
            }
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            java.io.File compressedFile = new java.io.File(picturesDir, "Compressed_" + timeStamp + ".jpg");
            
            java.io.FileOutputStream fos = new java.io.FileOutputStream(compressedFile);
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, fos);
            fos.close();
            return compressedFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveCapturedPhotoMetadata(String absolutePath) {
        java.util.Set<String> existing = sharedPrefs.getStringSet("captured_photos_set", null);
        java.util.Set<String> updated = new java.util.HashSet<>();
        if (existing != null) {
            updated.addAll(existing);
        }

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dayFmt = new SimpleDateFormat("d", Locale.getDefault());
        SimpleDateFormat monthFmt = new SimpleDateFormat("MMMM", Locale.getDefault());
        String dateString = dayFmt.format(cal.getTime()) + " " + monthFmt.format(cal.getTime()); // e.g. "17 July"

        final String[] options = {"Front Facing", "Back Facing", "Left Facing", "Right Facing"};
        new AlertDialog.Builder(this)
                .setTitle("Select Photo Direction")
                .setItems(options, (dialog, which) -> {
                    String direction = options[which];
                    String record = absolutePath + "|" + dateString + "|" + direction;
                    updated.add(record);
                    sharedPrefs.edit().putStringSet("captured_photos_set", updated).apply();
                    initializeSamplePhotos();
                    refreshGallery();
                    Toast.makeText(this, "Photo added to gallery successfully!", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showPhotoZoomDialog(PhotoItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        ImageView zoomImg = new ImageView(this);
        if (item.imagePath != null) {
            zoomImg.setImageURI(android.net.Uri.fromFile(new java.io.File(item.imagePath)));
        } else {
            zoomImg.setImageResource(item.imageResId);
        }
        zoomImg.setAdjustViewBounds(true);
        builder.setView(zoomImg);
        builder.setTitle("Photo captured on " + item.date);
        
        builder.setPositiveButton("Close", null);
        if (item.imagePath != null) {
            builder.setNeutralButton("Edit Details", (dialog, which) -> editPhotoDetails(item));
        }
        builder.show();
    }

    private void editPhotoDetails(PhotoItem item) {
        if (item.imagePath == null) {
            Toast.makeText(this, "Default sample photos cannot be edited.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Photo Details");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_photo_details, null);
        builder.setView(dialogView);

        android.widget.EditText etDate = dialogView.findViewById(R.id.etPhotoDate);
        android.widget.Spinner spinnerDir = dialogView.findViewById(R.id.spinnerPhotoDir);

        etDate.setText(item.date);

        String[] dirs = {"Front Facing", "Back Facing", "Left Facing", "Right Facing"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dirs);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDir.setAdapter(adapter);

        java.util.Set<String> captured = sharedPrefs.getStringSet("captured_photos_set", null);
        String currentDir = "Front Facing";
        if (captured != null) {
            for (String record : captured) {
                if (record.startsWith(item.imagePath + "|")) {
                    String[] parts = record.split("\\|");
                    if (parts.length >= 3) {
                        currentDir = parts[2];
                    }
                    break;
                }
            }
        }
        for (int i = 0; i < dirs.length; i++) {
            if (dirs[i].equalsIgnoreCase(currentDir)) {
                spinnerDir.setSelection(i);
                break;
            }
        }

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newDate = etDate.getText().toString().trim();
            String newDir = spinnerDir.getSelectedItem().toString();

            if (newDate.isEmpty()) {
                Toast.makeText(this, "Date cannot be empty.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (captured != null) {
                java.util.Set<String> updated = new java.util.HashSet<>(captured);
                for (String record : captured) {
                    if (record.startsWith(item.imagePath + "|")) {
                        updated.remove(record);
                        updated.add(item.imagePath + "|" + newDate + "|" + newDir);
                        break;
                    }
                }
                sharedPrefs.edit().putStringSet("captured_photos_set", updated).apply();
                initializeSamplePhotos();
                refreshGallery();
                Toast.makeText(this, "Photo details updated!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void triggerDeletePhotoAction() {
        if (photoList.isEmpty()) {
            Toast.makeText(this, "No photos to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Last Photo")
                .setMessage("Are you sure you want to delete the most recent progress photo?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    PhotoItem removed = photoList.remove(0); // Remove the first/latest item
                    deleteCapturedPhotoFromPrefs(removed);
                    refreshGallery();
                    Toast.makeText(this, "Latest photo deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteCapturedPhotoFromPrefs(PhotoItem item) {
        if (item.imagePath != null) {
            try {
                new java.io.File(item.imagePath).delete();
            } catch (Exception e) {
                // Ignore cleanup fail
            }
            java.util.Set<String> captured = sharedPrefs.getStringSet("captured_photos_set", null);
            if (captured != null) {
                java.util.Set<String> updated = new java.util.HashSet<>(captured);
                for (String record : captured) {
                    if (record.startsWith(item.imagePath + "|")) {
                        updated.remove(record);
                        break;
                    }
                }
                sharedPrefs.edit().putStringSet("captured_photos_set", updated).apply();
            }
        }
    }

    private void triggerShareGalleryAction() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Hey! I am tracking my body recomposition and fitness milestones on FitTrain!");
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Share Gallery"));
    }

    private void enableEditMode(boolean enable) {
        isEditMode = enable;
        
        // Reset selections
        for (PhotoItem item : photoList) {
            item.isSelected = false;
        }

        if (enable) {
            binding.layoutEditActions.setVisibility(View.VISIBLE);
            binding.fabCamera.setVisibility(View.GONE);
        } else {
            binding.layoutEditActions.setVisibility(View.GONE);
            binding.fabCamera.setVisibility(View.VISIBLE);
        }

        refreshGallery();
    }

    private void triggerBulkDeleteAction() {
        List<PhotoItem> toDelete = new ArrayList<>();
        for (PhotoItem item : photoList) {
            if (item.isSelected) {
                toDelete.add(item);
            }
        }

        if (toDelete.isEmpty()) {
            Toast.makeText(this, "Please select at least one photo to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Bulk Delete")
                .setMessage("Are you sure you want to delete the " + toDelete.size() + " selected photo(s)?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    for (PhotoItem item : toDelete) {
                        deleteCapturedPhotoFromPrefs(item);
                    }
                    photoList.removeAll(toDelete);
                    enableEditMode(false);
                    Toast.makeText(this, "Selected photos deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupReminderBanner() {
        boolean remindersEnabled = sharedPrefs.getBoolean("reminder_enabled", true);
        if (!remindersEnabled) {
            binding.cardReminder.setVisibility(View.GONE);
            return;
        }

        // Calculate next reminder date dynamically
        Calendar nextReminderCal = calculateNextReminderDate();
        SimpleDateFormat fmt = new SimpleDateFormat("MMMM dd", Locale.getDefault());
        String nextReminderStr = fmt.format(nextReminderCal.getTime());
        binding.tvReminderText.setText("Next Photos Fall On " + nextReminderStr);

        // Check if current date is at or after next reminder date
        Calendar now = Calendar.getInstance();
        boolean dismissed = sharedPrefs.getBoolean("photo_reminder_dismissed", false);

        if (now.after(nextReminderCal)) {
            // Re-enable if date reached!
            dismissed = false;
            sharedPrefs.edit().putBoolean("photo_reminder_dismissed", false).apply();
        }

        if (dismissed) {
            binding.cardReminder.setVisibility(View.GONE);
        } else {
            binding.cardReminder.setVisibility(View.VISIBLE);

            binding.btnDismissReminder.setOnClickListener(v -> {
                sharedPrefs.edit().putBoolean("photo_reminder_dismissed", true).apply();
                binding.cardReminder.setVisibility(View.GONE);
                Toast.makeText(this, "Reminder dismissed until " + nextReminderStr, Toast.LENGTH_SHORT).show();
            });
        }
    }

    private Calendar calculateNextReminderDate() {
        Calendar baseCal = Calendar.getInstance();

        // Base reminder on latest photo logged
        if (!photoList.isEmpty()) {
            PhotoItem latest = photoList.get(0); // e.g. "2 June"
            try {
                String[] parts = latest.date.split(" ");
                int day = Integer.parseInt(parts[0]);
                String monthName = parts[1].toLowerCase();

                int month = Calendar.JUNE;
                if (monthName.contains("jan")) month = Calendar.JANUARY;
                else if (monthName.contains("feb")) month = Calendar.FEBRUARY;
                else if (monthName.contains("mar")) month = Calendar.MARCH;
                else if (monthName.contains("apr")) month = Calendar.APRIL;
                else if (monthName.contains("may")) month = Calendar.MAY;
                else if (monthName.contains("jun")) month = Calendar.JUNE;
                else if (monthName.contains("jul")) month = Calendar.JULY;
                else if (monthName.contains("aug")) month = Calendar.AUGUST;
                else if (monthName.contains("sep")) month = Calendar.SEPTEMBER;
                else if (monthName.contains("oct")) month = Calendar.OCTOBER;
                else if (monthName.contains("nov")) month = Calendar.NOVEMBER;
                else if (monthName.contains("dec")) month = Calendar.DECEMBER;

                baseCal.set(Calendar.DAY_OF_MONTH, day);
                baseCal.set(Calendar.MONTH, month);
            } catch (Exception e) {
                // Fallback to current date
            }
        }

        // Get configured frequency interval
        int frequencyIndex = sharedPrefs.getInt("reminder_frequency_index", 1); // 0 = Weekly, 1 = Monthly, 2 = Bi-monthly
        if (frequencyIndex == 0) {
            baseCal.add(Calendar.WEEK_OF_YEAR, 1);
        } else if (frequencyIndex == 2) {
            baseCal.add(Calendar.MONTH, 2);
        } else {
            baseCal.add(Calendar.MONTH, 1);
        }

        return baseCal;
    }

    private void setupPromoCard() {
        boolean showPromo = sharedPrefs.getBoolean("show_promo_card", true);
        if (!showPromo) {
            binding.cardPromo.setVisibility(View.GONE);
        } else {
            binding.cardPromo.setVisibility(View.VISIBLE);
            
            // Stylize title so "Photo" is colored in blue/purple accent
            TextView tvPromoTitle = binding.cardPromo.findViewById(R.id.tvPromoTitle);
            if (tvPromoTitle != null) {
                String text = "Track Your Progress Each Month With Photo";
                android.text.SpannableString spannable = new android.text.SpannableString(text);
                int startIndex = text.indexOf("Photo");
                if (startIndex != -1) {
                    spannable.setSpan(
                        new android.text.style.ForegroundColorSpan(Color.parseColor("#7B8FF0")), // Gradient blue/purple accent color
                        startIndex, 
                        startIndex + "Photo".length(), 
                        android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    );
                }
                tvPromoTitle.setText(spannable);
            }

            binding.btnPromoLearnMore.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Monthly Photo Tracking")
                        .setMessage("Monthly progress photos let you track physical changes (muscle gain, fat loss, and posture improvements) that scales don't show.\n\nTips:\n• Take photos under consistent lighting.\n• Wear similar clothing each time.\n• Stand relaxed in front, back, and side positions.")
                        .setPositiveButton("Got It", (dialog, which) -> {
                            SharedPreferences.Editor editor = sharedPrefs.edit();
                            editor.putBoolean("show_promo_card", false);
                            editor.apply();
                            binding.cardPromo.setVisibility(View.GONE);
                        })
                        .setNegativeButton("Keep Showing", null)
                        .show();
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload photos from preferences in case user just captured a photo!
        initializeSamplePhotos();
        refreshGallery();
        // Check if reminder was re-enabled in settings, if so, re-display
        setupReminderBanner();
    }
}

package com.fitness.app.activities;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fitness.app.R;
import com.fitness.app.data.room.AppDatabase;
import com.fitness.app.data.room.FitnessDao;
import com.fitness.app.data.room.LoggedMeal;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddMealActivity extends AppCompatActivity {

    private Spinner spMealCategory;
    private EditText etMealName, etMealTime, etMealCalories, etMealProtein, etMealCarbs, etMealFat, etMealNotes;
    private EditText etMealFiber, etMealSugar, etMealServing, etMealIngredients, etMealSteps;
    private LinearLayout layoutIconGrid;

    private FitnessDao fitnessDao;
    private int mealId = -1;
    private LoggedMeal editingMeal;
    private String selectedDate;
    private int selectedIconResId = 0; // Pancake default index

    private final String[] mealCategories = {"Breakfast", "Morning Snack", "Pre Workout Meal", "Lunch", "Post Workout Meal", "Evening Snack", "Snacks", "Dinner", "Cheat Meal"};

    private final String[] emojiNames = MealScheduleActivity.emojiNames;
    private final String[] emojiChars = MealScheduleActivity.emojiChars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_add_meal);

        fitnessDao = AppDatabase.getInstance(this).fitnessDao();

        // Bind Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        // Bind Views
        spMealCategory = findViewById(R.id.spMealCategory);
        etMealName = findViewById(R.id.etMealName);
        etMealTime = findViewById(R.id.etMealTime);
        etMealCalories = findViewById(R.id.etMealCalories);
        etMealProtein = findViewById(R.id.etMealProtein);
        etMealCarbs = findViewById(R.id.etMealCarbs);
        etMealFat = findViewById(R.id.etMealFat);
        etMealFiber = findViewById(R.id.etMealFiber);
        etMealSugar = findViewById(R.id.etMealSugar);
        etMealServing = findViewById(R.id.etMealServing);
        etMealIngredients = findViewById(R.id.etMealIngredients);
        etMealSteps = findViewById(R.id.etMealSteps);
        etMealNotes = findViewById(R.id.etMealNotes);
        layoutIconGrid = findViewById(R.id.layoutIconGrid);

        // Spinner Setup
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner_category, mealCategories);
        adapter.setDropDownViewResource(R.layout.item_spinner_category);
        spMealCategory.setAdapter(adapter);

        // Intent Extras
        selectedDate = getIntent().getStringExtra("selected_date");
        if (selectedDate == null) {
            selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        }

        mealId = getIntent().getIntExtra("meal_id", -1);
        if (mealId != -1) {
            loadMealForEditing();
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Edit Scheduled Meal");
            }
        } else {
            // Default time to current time
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            etMealTime.setText(sdf.format(new Date()));
        }

        // Time Picker setup
        etMealTime.setOnClickListener(v -> showTimePicker());

        // Buttons
        findViewById(R.id.btnCancel).setOnClickListener(v -> onBackPressed());
        findViewById(R.id.btnSaveMeal).setOnClickListener(v -> saveMeal());

        // Populate Food Icon Grid
        populateIconGrid();
    }

    private void loadMealForEditing() {
        // Query meal in background or main thread allowed
        editingMeal = null;
        try {
            java.util.List<LoggedMeal> meals = fitnessDao.getLoggedMealsForDate(selectedDate);
            for (LoggedMeal m : meals) {
                if (m.id == mealId) {
                    editingMeal = m;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (editingMeal != null) {
            etMealName.setText(editingMeal.name);
            etMealTime.setText(editingMeal.mealTime != null ? editingMeal.mealTime : "09:00 AM");
            etMealCalories.setText(String.valueOf(editingMeal.calories));
            etMealProtein.setText(String.valueOf(editingMeal.protein));
            etMealCarbs.setText(String.valueOf(editingMeal.carbs));
            etMealFat.setText(String.valueOf(editingMeal.fat));
            etMealFiber.setText(String.valueOf(editingMeal.fiber));
            etMealSugar.setText(String.valueOf(editingMeal.sugar));
            etMealServing.setText(editingMeal.servingSize != null ? editingMeal.servingSize : "");
            etMealIngredients.setText(editingMeal.ingredients != null ? editingMeal.ingredients : "");
            etMealSteps.setText(editingMeal.steps != null ? editingMeal.steps : "");
            etMealNotes.setText(editingMeal.notes != null ? editingMeal.notes : "");
            selectedIconResId = editingMeal.iconResId;
            if (selectedIconResId < 0 || selectedIconResId >= emojiChars.length) {
                selectedIconResId = findEmojiIndexByName(editingMeal.name);
                if (selectedIconResId == -1) {
                    selectedIconResId = 0; // fallback to Pancake
                }
            }

            // Set Category Spinner
            for (int i = 0; i < mealCategories.length; i++) {
                if (mealCategories[i].equalsIgnoreCase(editingMeal.mealType)) {
                    spMealCategory.setSelection(i);
                    break;
                }
            }
        }
    }

    private int findEmojiIndexByName(String name) {
        if (name == null) return -1;
        String lower = name.toLowerCase(Locale.getDefault());
        for (int i = 0; i < emojiNames.length; i++) {
            if (lower.contains(emojiNames[i].toLowerCase(Locale.getDefault()))) {
                return i;
            }
        }
        return -1;
    }

    private void showTimePicker() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minuteOfHour) -> {
            String format;
            int displayHour = hourOfDay;
            if (hourOfDay == 0) {
                displayHour = 12;
                format = "AM";
            } else if (hourOfDay == 12) {
                format = "PM";
            } else if (hourOfDay > 12) {
                displayHour = hourOfDay - 12;
                format = "PM";
            } else {
                format = "AM";
            }
            String timeString = String.format(Locale.getDefault(), "%02d:%02d %s", displayHour, minuteOfHour, format);
            etMealTime.setText(timeString);
        }, hour, minute, false);
        timePickerDialog.show();
    }

    private void populateIconGrid() {
        layoutIconGrid.removeAllViews();
        for (int i = 0; i < emojiNames.length; i++) {
            final int index = i;
            View view = LayoutInflater.from(this).inflate(R.layout.item_food_icon_selector, layoutIconGrid, false);
            MaterialCardView card = view.findViewById(R.id.cardContainer);
            TextView tvEmoji = view.findViewById(R.id.tvIconEmoji);
            TextView tv = view.findViewById(R.id.tvLabel);

            tvEmoji.setText(emojiChars[i]);
            tv.setText(emojiNames[i]);

            // Highlight selected icon
            if (i == selectedIconResId) {
                card.setStrokeColor(0xFF3B82F6);
                card.setStrokeWidth(4);
                card.setCardBackgroundColor(0xFFEFF6FF);
            } else {
                card.setStrokeColor(0xFFE2E8F0);
                card.setStrokeWidth(2);
                card.setCardBackgroundColor(0xFFFFFFFF);
            }

            card.setOnClickListener(v -> {
                selectedIconResId = index;
                if (etMealName.getText().toString().trim().isEmpty()) {
                    etMealName.setText(emojiNames[index]);
                }
                populateIconGrid(); // Redraw selection indicators
            });

            layoutIconGrid.addView(view);
        }
    }

    private void saveMeal() {
        String name = etMealName.getText().toString().trim();
        String time = etMealTime.getText().toString().trim();
        String kcalStr = etMealCalories.getText().toString().trim();
        String proteinStr = etMealProtein.getText().toString().trim();
        String carbsStr = etMealCarbs.getText().toString().trim();
        String fatStr = etMealFat.getText().toString().trim();
        String fiberStr = etMealFiber.getText().toString().trim();
        String sugarStr = etMealSugar.getText().toString().trim();
        String serving = etMealServing.getText().toString().trim();
        String ingredients = etMealIngredients.getText().toString().trim();
        String steps = etMealSteps.getText().toString().trim();
        String notes = etMealNotes.getText().toString().trim();
        String category = spMealCategory.getSelectedItem().toString();

        if (name.isEmpty()) {
            etMealName.setError("Meal Name is required!");
            return;
        }

        int kcal = kcalStr.isEmpty() ? 0 : Integer.parseInt(kcalStr);
        int protein = proteinStr.isEmpty() ? 0 : Integer.parseInt(proteinStr);
        int carbs = carbsStr.isEmpty() ? 0 : Integer.parseInt(carbsStr);
        int fat = fatStr.isEmpty() ? 0 : Integer.parseInt(fatStr);
        int fiber = fiberStr.isEmpty() ? 0 : Integer.parseInt(fiberStr);
        int sugar = sugarStr.isEmpty() ? 0 : Integer.parseInt(sugarStr);

        int targetIconId = selectedIconResId;
        if (editingMeal != null) {
            if (!editingMeal.name.equalsIgnoreCase(name)) {
                int newIndex = findEmojiIndexByName(name);
                if (newIndex != -1) {
                    targetIconId = newIndex;
                }
            }
        } else {
            if (selectedIconResId == 0) {
                int indexByName = findEmojiIndexByName(name);
                if (indexByName != -1) {
                    targetIconId = indexByName;
                }
            }
        }

        final int finalIconId = targetIconId;
        new Thread(() -> {
            try {
                if (editingMeal != null) {
                    editingMeal.name = name;
                    editingMeal.mealTime = time;
                    editingMeal.calories = kcal;
                    editingMeal.protein = protein;
                    editingMeal.carbs = carbs;
                    editingMeal.fat = fat;
                    editingMeal.fiber = fiber;
                    editingMeal.sugar = sugar;
                    editingMeal.servingSize = serving;
                    editingMeal.ingredients = ingredients;
                    editingMeal.steps = steps;
                    editingMeal.notes = notes;
                    editingMeal.mealType = category;
                    editingMeal.iconResId = finalIconId;
                    
                    fitnessDao.insertLoggedMeal(editingMeal);
                } else {
                    LoggedMeal newMeal = new LoggedMeal(
                            category,
                            name,
                            kcal,
                            protein,
                            carbs,
                            fat,
                            selectedDate,
                            System.currentTimeMillis(),
                            time,
                            notes,
                            false,
                            finalIconId,
                            fiber,
                            sugar,
                            serving,
                            ingredients,
                            steps
                    );
                    fitnessDao.insertLoggedMeal(newMeal);
                }
                
                updateDailyProgressLog();
                
                runOnUiThread(() -> {
                    Toast.makeText(this, editingMeal != null ? "Meal updated successfully! 🍽️" : "Meal added to schedule! 🍽️", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error saving meal", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void deleteMealWithConfirmation() {
        if (editingMeal == null) return;
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Meal")
                .setMessage("Are you sure you want to remove this meal from your schedule?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    new Thread(() -> {
                        try {
                            fitnessDao.deleteLoggedMeal(editingMeal);
                            updateDailyProgressLog();
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Meal deleted from schedule!", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateDailyProgressLog() {
        com.fitness.app.data.local.LocalDataManager localDb = new com.fitness.app.data.local.LocalDataManager(this);
        try {
            java.util.List<LoggedMeal> dayMeals = fitnessDao.getLoggedMealsForDate(selectedDate);
            int totalConsumed = 0;
            for (LoggedMeal m : dayMeals) {
                if (m.isChecked) {
                    totalConsumed += m.calories;
                }
            }
            
            com.fitness.app.models.ProgressLog progressLog = localDb.getProgressLog(selectedDate);
            progressLog.setCaloriesConsumed(totalConsumed);
            progressLog.setUpdatedAt(System.currentTimeMillis());
            localDb.saveProgressLog(progressLog);
            
            try {
                new com.fitness.app.repositories.UserRepository().syncLocalDataToFirestore(localDb);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mealId != -1) {
            getMenuInflater().inflate(R.menu.menu_add_meal, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_delete) {
            deleteMealWithConfirmation();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}

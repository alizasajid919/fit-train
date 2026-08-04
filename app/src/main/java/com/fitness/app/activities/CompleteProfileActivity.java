package com.fitness.app.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.fitness.app.R;
import com.fitness.app.models.User;
import com.fitness.app.utils.ValidationUtils;
import com.fitness.app.viewmodels.AuthViewModel;
import com.fitness.app.viewmodels.ProfileViewModel;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;
import java.util.Locale;

public class CompleteProfileActivity extends AppCompatActivity {

    private ImageView ivProfileImage;
    private Spinner spGender, spActivityLevel;
    private EditText etDob, etWeight, etHeight;
    private View progressOverlay;

    private AuthViewModel authViewModel;
    private ProfileViewModel profileViewModel;
    
    private Uri selectedImageUri;
    private User currentUserModel;

    // Image Picker Launcher
    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivProfileImage.setImageURI(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_profile);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        ivProfileImage = findViewById(R.id.ivProfileImage);
        spGender = findViewById(R.id.spGender);
        spActivityLevel = findViewById(R.id.spActivityLevel);
        etDob = findViewById(R.id.etDob);
        etWeight = findViewById(R.id.etWeight);
        etHeight = findViewById(R.id.etHeight);
        progressOverlay = findViewById(R.id.progressOverlay);

        setupSpinners();
        setupDatePicker();

        findViewById(R.id.fabAddPhoto).setOnClickListener(v -> selectImage());
        findViewById(R.id.btnNext).setOnClickListener(v -> saveProfileDetails());

        loadUserProfile();
    }

    private void setupSpinners() {
        // Gender Spinner
        String[] genders = {"Choose Gender", "Male", "Female"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genders);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGender.setAdapter(genderAdapter);

        // Activity Level Spinner
        String[] activityLevels = {"Choose Activity Level", "Beginner", "Intermediate", "Active", "Athlete"};
        ArrayAdapter<String> activityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, activityLevels);
        activityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spActivityLevel.setAdapter(activityAdapter);
    }

    private void setupDatePicker() {
        etDob.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    CompleteProfileActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        etDob.setText(date);
                    },
                    year - 20, month, day // Default age estimation 20 years ago
            );
            datePickerDialog.show();
        });
    }

    private void selectImage() {
        imagePickerLauncher.launch("image/*");
    }

    private void loadUserProfile() {
        if (authViewModel.getCurrentUser() != null) {
            String uid = authViewModel.getCurrentUser().getUid();
            profileViewModel.getUserProfile(uid).observe(this, resource -> {
                if (resource != null) {
                    switch (resource.status) {
                        case SUCCESS:
                            currentUserModel = resource.data;
                            if (currentUserModel != null) {
                                populateFields(currentUserModel);
                            }
                            break;
                        case ERROR:
                            // Do not block, allow user to enter manually
                            break;
                        case LOADING:
                            break;
                    }
                }
            });
        }
    }

    private void populateFields(User user) {
        if (user.getGender() != null && !user.getGender().isEmpty()) {
            if ("Male".equalsIgnoreCase(user.getGender())) spGender.setSelection(1);
            else if ("Female".equalsIgnoreCase(user.getGender())) spGender.setSelection(2);
        }
        if (user.getDob() != null && !user.getDob().isEmpty()) {
            etDob.setText(user.getDob());
        }
        if (user.getWeight() > 0) {
            etWeight.setText(String.valueOf(user.getWeight()));
        }
        if (user.getHeight() > 0) {
            etHeight.setText(String.valueOf(user.getHeight()));
        }
        if (user.getActivityLevel() != null && !user.getActivityLevel().isEmpty()) {
            switch (user.getActivityLevel()) {
                case "Beginner": spActivityLevel.setSelection(1); break;
                case "Intermediate": spActivityLevel.setSelection(2); break;
                case "Active": spActivityLevel.setSelection(3); break;
                case "Athlete": spActivityLevel.setSelection(4); break;
            }
        }
    }

    private void saveProfileDetails() {
        if (currentUserModel == null) {
            FirebaseUser firebaseUser = authViewModel.getCurrentUser();
            if (firebaseUser != null) {
                currentUserModel = new User(firebaseUser.getUid(), "", "", firebaseUser.getEmail(), System.currentTimeMillis());
            } else {
                Toast.makeText(this, "Profile data not synchronized. Please try again.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String gender = spGender.getSelectedItem().toString();
        String dob = etDob.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();
        String heightStr = etHeight.getText().toString().trim();
        String activityLevel = spActivityLevel.getSelectedItem().toString();

        if (spGender.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please choose a gender", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ValidationUtils.isEmpty(dob)) {
            Toast.makeText(this, "Please select your date of birth", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ValidationUtils.isEmpty(weightStr)) {
            etWeight.setError(getString(R.string.err_empty_field));
            etWeight.requestFocus();
            return;
        }

        if (ValidationUtils.isEmpty(heightStr)) {
            etHeight.setError(getString(R.string.err_empty_field));
            etHeight.requestFocus();
            return;
        }

        if (spActivityLevel.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select your activity level", Toast.LENGTH_SHORT).show();
            return;
        }

        // Perform strict height validation
        double parsedHeight = ValidationUtils.parseHeight(heightStr);
        if (parsedHeight == -1 || !ValidationUtils.isValidHeight(parsedHeight)) {
            etHeight.setError("Please enter a valid height (e.g., 170 cm or 5'7\")");
            etHeight.requestFocus();
            return;
        }

        // Perform weight validation
        double parsedWeight;
        try {
            parsedWeight = Double.parseDouble(weightStr);
        } catch (NumberFormatException e) {
            parsedWeight = -1;
        }
        if (!ValidationUtils.isValidWeight(parsedWeight)) {
            etWeight.setError("Please enter a valid weight (20 - 300 kg)");
            etWeight.requestFocus();
            return;
        }

        // Perform age validation
        int age = ValidationUtils.calculateAge(dob);
        if (age == -1 || !ValidationUtils.isValidAge(age)) {
            Toast.makeText(this, "Please enter a valid age (10 - 100 years)", Toast.LENGTH_LONG).show();
            return;
        }

        // Update User model instance
        currentUserModel.setGender(gender);
        currentUserModel.setDob(dob);
        currentUserModel.setWeight(parsedWeight);
        currentUserModel.setHeight(parsedHeight);
        currentUserModel.setActivityLevel(activityLevel);

        // If user picked a profile image, upload it first
        if (selectedImageUri != null) {
            profileViewModel.uploadProfileImage(currentUserModel.getUid(), selectedImageUri).observe(this, resource -> {
                if (resource != null) {
                    switch (resource.status) {
                        case LOADING:
                            progressOverlay.setVisibility(View.VISIBLE);
                            break;
                        case SUCCESS:
                            progressOverlay.setVisibility(View.GONE);
                            currentUserModel.setProfileImageUrl(resource.data);
                            navigateToGoalSelection();
                            break;
                        case ERROR:
                            progressOverlay.setVisibility(View.GONE);
                            Toast.makeText(this, "Image Upload Failed: " + resource.message + ". Proceeding anyway...", Toast.LENGTH_LONG).show();
                            navigateToGoalSelection();
                            break;
                    }
                }
            });
        } else {
            navigateToGoalSelection();
        }
    }

    private void navigateToGoalSelection() {
        Intent intent = new Intent(CompleteProfileActivity.this, GoalSelectionActivity.class);
        intent.putExtra("user_profile", currentUserModel);
        startActivity(intent);
    }
}

package com.fitness.app.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.fitness.app.R;
import com.fitness.app.models.User;
import com.fitness.app.utils.LocationUtils;
import com.fitness.app.utils.ValidationUtils;
import com.fitness.app.viewmodels.AuthViewModel;
import com.fitness.app.viewmodels.ProfileViewModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CompleteProfileActivity extends AppCompatActivity {

    private ImageView ivProfileImage;
    private Spinner spGender, spActivityLevel, spCountry;
    private TextView tvCity, tvMedicalCondition;
    private ImageView ivCityArrow, ivMedicalConditionArrow;
    private EditText etDob, etWeight, etHeight, etFullName, etEmail;
    private View progressOverlay;
    private ScrollView scrollView;
    private Button btnRegisterProfile;

    // Selected state variables
    private String selectedCityStr = "";
    private String selectedMedicalConditionStr = "";

    // View Containers & Inline Error Labels
    private View containerGender, containerWeight, containerHeight, containerActivityLevel, containerCountry, containerCity, containerMedicalCondition, containerFullName, containerEmail;
    private TextView tvErrorGender, tvErrorDob, tvErrorWeight, tvErrorHeight, tvErrorActivityLevel, tvErrorCountry, tvErrorCity, tvErrorMedicalCondition, tvErrorFullName, tvErrorEmail;

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

        scrollView = findViewById(R.id.scrollView);

        ivProfileImage = findViewById(R.id.ivProfileImage);
        spGender = findViewById(R.id.spGender);
        spActivityLevel = findViewById(R.id.spActivityLevel);
        spCountry = findViewById(R.id.spCountry);
        tvCity = findViewById(R.id.tvCity);
        tvMedicalCondition = findViewById(R.id.tvMedicalCondition);
        ivCityArrow = findViewById(R.id.ivCityArrow);
        ivMedicalConditionArrow = findViewById(R.id.ivMedicalConditionArrow);

        etDob = findViewById(R.id.etDob);
        etWeight = findViewById(R.id.etWeight);
        etHeight = findViewById(R.id.etHeight);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        progressOverlay = findViewById(R.id.progressOverlay);
        btnRegisterProfile = findViewById(R.id.btnNext);

        // Bind Containers & Inline Error Views
        containerFullName = findViewById(R.id.containerFullName);
        containerEmail = findViewById(R.id.containerEmail);
        containerGender = findViewById(R.id.containerGender);
        containerWeight = findViewById(R.id.containerWeight);
        containerHeight = findViewById(R.id.containerHeight);
        containerActivityLevel = findViewById(R.id.containerActivityLevel);
        containerCountry = findViewById(R.id.containerCountry);
        containerCity = findViewById(R.id.containerCity);
        containerMedicalCondition = findViewById(R.id.containerMedicalCondition);

        tvErrorFullName = findViewById(R.id.tvErrorFullName);
        tvErrorEmail = findViewById(R.id.tvErrorEmail);
        tvErrorGender = findViewById(R.id.tvErrorGender);
        tvErrorDob = findViewById(R.id.tvErrorDob);
        tvErrorWeight = findViewById(R.id.tvErrorWeight);
        tvErrorHeight = findViewById(R.id.tvErrorHeight);
        tvErrorActivityLevel = findViewById(R.id.tvErrorActivityLevel);
        tvErrorCountry = findViewById(R.id.tvErrorCountry);
        tvErrorCity = findViewById(R.id.tvErrorCity);
        tvErrorMedicalCondition = findViewById(R.id.tvErrorMedicalCondition);

        if (btnRegisterProfile != null) {
            btnRegisterProfile.setText("Register Profile");
        }

        setupSpinners();
        setupInteractiveDropdowns();
        setupDatePicker();

        findViewById(R.id.fabAddPhoto).setOnClickListener(v -> selectImage());
        if (btnRegisterProfile != null) {
            btnRegisterProfile.setOnClickListener(v -> saveProfileDetails());
        }

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

        // Country Spinner (Including Pakistan)
        List<String> countries = new ArrayList<>();
        countries.add("Choose Country");
        countries.addAll(LocationUtils.getCountries());
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, countries);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCountry.setAdapter(countryAdapter);

        spCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // When Country changes, reset selected City and require picking a city for the new country
                selectedCityStr = "";
                if (tvCity != null) {
                    tvCity.setText("Choose City");
                }
                if (tvErrorCountry != null) tvErrorCountry.setVisibility(View.GONE);
                if (tvErrorCity != null) tvErrorCity.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupInteractiveDropdowns() {
        // City Dropdown Tap Listeners
        View.OnClickListener cityClickListener = v -> {
            String selectedCountry = spCountry.getSelectedItem() != null ? spCountry.getSelectedItem().toString() : "";
            if (spCountry.getSelectedItemPosition() <= 0 || !ValidationUtils.isValidCountry(selectedCountry)) {
                showFieldError(containerCountry, tvErrorCountry, "Please select a country first");
                return;
            }
            showCitySearchDialog(selectedCountry);
        };

        if (containerCity != null) containerCity.setOnClickListener(cityClickListener);
        if (tvCity != null) tvCity.setOnClickListener(cityClickListener);
        if (ivCityArrow != null) ivCityArrow.setOnClickListener(cityClickListener);

        // Medical Condition Dropdown Tap Listeners
        View.OnClickListener medConditionClickListener = v -> showMedicalConditionDialog();
        View.OnTouchListener medTouchListener = (v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                showMedicalConditionDialog();
            }
            return false;
        };

        if (containerMedicalCondition != null) {
            containerMedicalCondition.setOnClickListener(medConditionClickListener);
            containerMedicalCondition.setOnTouchListener(medTouchListener);
        }
        if (tvMedicalCondition != null) {
            tvMedicalCondition.setOnClickListener(medConditionClickListener);
            tvMedicalCondition.setOnTouchListener(medTouchListener);
        }
        if (ivMedicalConditionArrow != null) {
            ivMedicalConditionArrow.setOnClickListener(medConditionClickListener);
            ivMedicalConditionArrow.setOnTouchListener(medTouchListener);
        }
    }

    private void showCitySearchDialog(String country) {
        List<String> cityList = LocationUtils.getCitiesForCountry(country);
        if (cityList == null || cityList.isEmpty()) {
            Toast.makeText(this, "No cities available for " + country, Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select City for " + country);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 24, 32, 16);

        EditText etSearch = new EditText(this);
        etSearch.setHint("🔍 Search city...");
        etSearch.setPadding(24, 20, 24, 20);
        etSearch.setBackgroundResource(R.drawable.bg_edittext);
        layout.addView(etSearch);

        ListView listView = new ListView(this);
        listView.setPadding(0, 16, 0, 0);
        layout.addView(listView);

        builder.setView(layout);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>(cityList));
        listView.setAdapter(adapter);

        AlertDialog dialog = builder.create();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCity = adapter.getItem(position);
            if (selectedCity != null) {
                selectedCityStr = selectedCity;
                tvCity.setText(selectedCity);
                if (tvErrorCity != null) tvErrorCity.setVisibility(View.GONE);
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showMedicalConditionDialog() {
        String[] options = {
            "None", "Hypertension (High BP)", "Diabetes (Type 1 / Type 2)",
            "Asthma / Respiratory", "Heart Condition", "Joint / Arthritis",
            "Thyroid Disorder", "Other"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Medical Condition");
        builder.setItems(options, (dialog, which) -> {
            selectedMedicalConditionStr = options[which];
            tvMedicalCondition.setText(selectedMedicalConditionStr);
            if (tvErrorMedicalCondition != null) tvErrorMedicalCondition.setVisibility(View.GONE);
            dialog.dismiss();
        });

        builder.show();
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
                        etDob.setError(null);
                        if (tvErrorDob != null) tvErrorDob.setVisibility(View.GONE);
                    },
                    year - 20, month, day
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
                            break;
                        case LOADING:
                            break;
                    }
                }
            });
        }
    }

    private void populateFields(User user) {
        if (etFullName != null) {
            String name = (user.getFirstName() != null ? user.getFirstName() : "") + 
                    (user.getLastName() != null && !user.getLastName().isEmpty() ? " " + user.getLastName() : "");
            if (!name.trim().isEmpty()) {
                etFullName.setText(name.trim());
            }
        }
        if (etEmail != null && user.getEmail() != null && !user.getEmail().isEmpty()) {
            etEmail.setText(user.getEmail());
        }
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
        if (user.getCountry() != null && !user.getCountry().isEmpty()) {
            ArrayAdapter adapter = (ArrayAdapter) spCountry.getAdapter();
            if (adapter != null) {
                int pos = adapter.getPosition(user.getCountry());
                if (pos >= 0) spCountry.setSelection(pos);
            }
        }
        if (user.getCity() != null && !user.getCity().isEmpty()) {
            selectedCityStr = user.getCity();
            if (tvCity != null) tvCity.setText(selectedCityStr);
        }
        if (user.getMedicalConditions() != null && !user.getMedicalConditions().isEmpty()) {
            selectedMedicalConditionStr = user.getMedicalConditions();
            if (tvMedicalCondition != null) tvMedicalCondition.setText(selectedMedicalConditionStr);
        }
    }

    private void clearAllErrors() {
        if (tvErrorFullName != null) tvErrorFullName.setVisibility(View.GONE);
        if (tvErrorEmail != null) tvErrorEmail.setVisibility(View.GONE);
        if (tvErrorGender != null) tvErrorGender.setVisibility(View.GONE);
        if (tvErrorDob != null) tvErrorDob.setVisibility(View.GONE);
        if (tvErrorWeight != null) tvErrorWeight.setVisibility(View.GONE);
        if (tvErrorHeight != null) tvErrorHeight.setVisibility(View.GONE);
        if (tvErrorActivityLevel != null) tvErrorActivityLevel.setVisibility(View.GONE);
        if (tvErrorCountry != null) tvErrorCountry.setVisibility(View.GONE);
        if (tvErrorCity != null) tvErrorCity.setVisibility(View.GONE);
        if (tvErrorMedicalCondition != null) tvErrorMedicalCondition.setVisibility(View.GONE);
    }

    private void showFieldError(View containerView, TextView errorTextView, String errorMessage) {
        if (errorTextView != null) {
            errorTextView.setText("⚠ " + errorMessage);
            errorTextView.setVisibility(View.VISIBLE);
        }
        if (containerView != null) {
            containerView.requestFocus();
            if (scrollView != null) {
                scrollView.post(() -> scrollView.smoothScrollTo(0, Math.max(0, containerView.getTop() - 80)));
            }
        }
    }

    private void saveProfileDetails() {
        clearAllErrors();

        if (currentUserModel == null) {
            FirebaseUser firebaseUser = authViewModel.getCurrentUser();
            if (firebaseUser != null) {
                currentUserModel = new User(firebaseUser.getUid(), "", "", firebaseUser.getEmail(), System.currentTimeMillis());
            } else {
                Toast.makeText(this, "Profile data not synchronized. Please try again.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String fullName = etFullName != null ? etFullName.getText().toString().trim() : "";
        String email = etEmail != null ? etEmail.getText().toString().trim() : "";

        // 0. Full Name Validation
        if (ValidationUtils.isEmpty(fullName)) {
            showFieldError(containerFullName, tvErrorFullName, "Please enter your full name");
            return;
        }

        // 0.5. Email Format Validation
        if (!ValidationUtils.isValidEmail(email)) {
            showFieldError(containerEmail, tvErrorEmail, "Please enter a valid email address.");
            if (etEmail != null) etEmail.requestFocus();
            return;
        }

        String gender = spGender.getSelectedItem() != null ? spGender.getSelectedItem().toString() : "";
        String dob = etDob.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();
        String heightStr = etHeight.getText().toString().trim();
        String activityLevel = spActivityLevel.getSelectedItem() != null ? spActivityLevel.getSelectedItem().toString() : "";
        String country = spCountry.getSelectedItem() != null ? spCountry.getSelectedItem().toString() : "";
        String city = selectedCityStr.trim();
        String medCondition = selectedMedicalConditionStr.trim();

        // 1. Gender Validation
        if (spGender.getSelectedItemPosition() <= 0) {
            showFieldError(containerGender, tvErrorGender, "Please select your gender");
            return;
        }

        // 2. DOB / Age Validation
        if (ValidationUtils.isEmpty(dob)) {
            showFieldError(etDob, tvErrorDob, "Please select your date of birth");
            return;
        }
        int age = ValidationUtils.calculateAge(dob);
        if (age == -1 || !ValidationUtils.isValidAge(age)) {
            showFieldError(etDob, tvErrorDob, "Please enter a valid age (10 - 100 years)");
            return;
        }

        // 3. Weight Validation
        double parsedWeight = ValidationUtils.parseWeight(weightStr);
        if (parsedWeight == -1 || !ValidationUtils.isValidWeight(parsedWeight)) {
            showFieldError(containerWeight, tvErrorWeight, "Please enter a valid weight (20 - 300 kg)");
            return;
        }

        // 4. Height Validation
        double parsedHeight = ValidationUtils.parseHeight(heightStr);
        if (parsedHeight == -1 || !ValidationUtils.isValidHeight(parsedHeight)) {
            showFieldError(containerHeight, tvErrorHeight, "Please enter a valid height (50 - 250 cm)");
            return;
        }

        // 5. Activity Level Validation
        if (spActivityLevel.getSelectedItemPosition() <= 0) {
            showFieldError(containerActivityLevel, tvErrorActivityLevel, "Please select an activity level option");
            return;
        }

        // 6. Country Validation
        if (spCountry.getSelectedItemPosition() <= 0 || !ValidationUtils.isValidCountry(country)) {
            showFieldError(containerCountry, tvErrorCountry, "Please select your country");
            return;
        }

        // 7. City Validation (Checks exact country-city relationship, rejects "John", "hello", "abc123")
        if (ValidationUtils.isEmpty(city) || !ValidationUtils.isValidCity(country, city)) {
            showFieldError(containerCity, tvErrorCity, "Please select a valid city for " + country);
            return;
        }

        // 8. Medical Condition Validation (Enforces allowed options)
        if (ValidationUtils.isEmpty(medCondition) || !ValidationUtils.isValidMedicalCondition(medCondition)) {
            showFieldError(containerMedicalCondition, tvErrorMedicalCondition, "Please select a valid medical condition option");
            return;
        }

        // Update User model instance
        String[] nameParts = fullName.split("\\s+", 2);
        currentUserModel.setFirstName(nameParts[0]);
        currentUserModel.setLastName(nameParts.length > 1 ? nameParts[1] : "");
        currentUserModel.setEmail(email);
        currentUserModel.setGender(gender);
        currentUserModel.setDob(dob);
        currentUserModel.setWeight(parsedWeight);
        currentUserModel.setHeight(parsedHeight);
        currentUserModel.setActivityLevel(activityLevel);
        currentUserModel.setCountry(country);
        currentUserModel.setCity(city);
        currentUserModel.setMedicalConditions(medCondition);

        // Set Loading State on CTA Button & Disable to prevent duplicate requests
        if (btnRegisterProfile != null) {
            btnRegisterProfile.setEnabled(false);
            btnRegisterProfile.setText("Registering...");
        }

        // Upload image if selected, then submit profile payload to backend repository
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
                            submitProfileToBackend();
                            break;
                        case ERROR:
                            progressOverlay.setVisibility(View.GONE);
                            Toast.makeText(this, "Image Upload Warning: " + resource.message + ". Proceeding...", Toast.LENGTH_SHORT).show();
                            submitProfileToBackend();
                            break;
                    }
                }
            });
        } else {
            submitProfileToBackend();
        }
    }

    private void submitProfileToBackend() {
        FirebaseUser firebaseUser = authViewModel.getCurrentUser();
        if (firebaseUser != null && !firebaseUser.isAnonymous()) {
            authViewModel.reloadAndCheckEmailVerification().observe(this, resource -> {
                if (resource != null) {
                    switch (resource.status) {
                        case SUCCESS:
                            if (Boolean.TRUE.equals(resource.data)) {
                                currentUserModel.setProfileCompleted(true);
                                executeProfileBackendSubmit();
                            } else {
                                if (btnRegisterProfile != null) {
                                    btnRegisterProfile.setEnabled(true);
                                    btnRegisterProfile.setText("Register Profile");
                                }
                                showUnverifiedEmailDialog();
                            }
                            break;
                        case ERROR:
                            if (btnRegisterProfile != null) {
                                btnRegisterProfile.setEnabled(true);
                                btnRegisterProfile.setText("Register Profile");
                            }
                            Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show();
                            break;
                        case LOADING:
                            break;
                    }
                }
            });
        } else {
            currentUserModel.setProfileCompleted(true);
            executeProfileBackendSubmit();
        }
    }

    private void showUnverifiedEmailDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Email Verification Required");
        builder.setMessage("A verification email has been sent to your email address. Please verify your email before completing your profile.");
        builder.setPositiveButton("Check Verification", (dialog, which) -> {
            submitProfileToBackend();
        });
        builder.setNeutralButton("Resend Email", (dialog, which) -> {
            authViewModel.resendEmailVerification().observe(CompleteProfileActivity.this, res -> {
                if (res != null && res.status == com.fitness.app.repositories.UserRepository.Resource.Status.SUCCESS) {
                    Toast.makeText(CompleteProfileActivity.this, res.data, Toast.LENGTH_SHORT).show();
                } else if (res != null && res.status == com.fitness.app.repositories.UserRepository.Resource.Status.ERROR) {
                    Toast.makeText(CompleteProfileActivity.this, res.message, Toast.LENGTH_SHORT).show();
                }
            });
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void executeProfileBackendSubmit() {
        profileViewModel.updateUserProfile(currentUserModel).observe(this, resource -> {
            if (resource != null) {
                switch (resource.status) {
                    case LOADING:
                        progressOverlay.setVisibility(View.VISIBLE);
                        break;
                    case SUCCESS:
                        progressOverlay.setVisibility(View.GONE);
                        // Save profile locally in Room DB
                        new com.fitness.app.data.local.LocalDataManager(CompleteProfileActivity.this).saveUser(currentUserModel);
                        
                        // Show Success State Notification
                        Snackbar.make(findViewById(android.R.id.content), "Your personal data has been successfully saved.", Snackbar.LENGTH_LONG).show();
                        
                        if (btnRegisterProfile != null) {
                            btnRegisterProfile.postDelayed(this::navigateToGoalSelection, 2000);
                        } else {
                            navigateToGoalSelection();
                        }
                        break;
                    case ERROR:
                        progressOverlay.setVisibility(View.GONE);
                        if (btnRegisterProfile != null) {
                            btnRegisterProfile.setEnabled(true);
                            btnRegisterProfile.setText("Register Profile");
                        }
                        Toast.makeText(this, "Registration failed: " + resource.message, Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });
    }

    private void navigateToGoalSelection() {
        Intent intent = new Intent(CompleteProfileActivity.this, GoalSelectionActivity.class);
        intent.putExtra("user_profile", currentUserModel);
        startActivity(intent);
        finish();
    }
}

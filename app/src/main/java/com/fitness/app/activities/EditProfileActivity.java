package com.fitness.app.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.fitness.app.R;
import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.models.ProgressLog;
import com.fitness.app.models.User;
import com.fitness.app.viewmodels.ProfileViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class EditProfileActivity extends AppCompatActivity {

    private ProfileViewModel profileViewModel;
    private android.app.ProgressDialog progressDialog;

    private static final int PICK_IMAGE_GALLERY = 100;
    private static final int CAPTURE_IMAGE_CAMERA = 101;
    private static final int CROP_IMAGE = 102;
    private static final int CAMERA_PERMISSION_REQUEST = 104;

    private EditText etEditFirstName, etEditLastName, etEditEmail, etEditDob;
    private AutoCompleteTextView etEditCity, etEditGender, etEditCountry, etEditBloodGroup, etEditActivityLevel, etEditGoal, etEditDietary, etEditConditions;
    private EditText etEditHeight, etEditWeight, etEditTargetWeight;

    private TextView tvEditProfileName, tvEditProfileEmail;
    private ImageView ivEditProfilePic;
    private Button btnRemovePic;
    private com.google.android.material.button.MaterialButton btnCancel, btnSaveProfile;
    private View btnChangePic;
    private com.google.android.material.textfield.TextInputLayout tilEditConditions;
    private ScrollView scrollView;

    private LocalDataManager localDb;
    private User user;
    private String selectedImageUriString = null;
    private Calendar dobCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Fade animation transition
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_edit_profile);

        scrollView = findViewById(R.id.scrollView);
        if (scrollView == null && findViewById(android.R.id.content) != null) {
            View root = findViewById(android.R.id.content);
            if (root instanceof ScrollView) scrollView = (ScrollView) root;
        }

        localDb = new LocalDataManager(this);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setMessage("Saving profile...");
        progressDialog.setCancelable(false);

        user = localDb.getUser();
        if (user == null) {
            user = new User("guest_uid", "Guest", "User", "guest@fittrain.com", System.currentTimeMillis());
            localDb.saveUser(user);
        }

        // Bind Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        // Bind Headers
        tvEditProfileName = findViewById(R.id.tvEditProfileName);
        tvEditProfileEmail = findViewById(R.id.tvEditProfileEmail);

        // Bind Form Inputs
        etEditFirstName = findViewById(R.id.etEditFirstName);
        etEditLastName = findViewById(R.id.etEditLastName);
        etEditEmail = findViewById(R.id.etEditEmail);
        etEditDob = findViewById(R.id.etEditDob);
        etEditCity = findViewById(R.id.etEditCity);
        
        // AutoComplete Dropdowns
        etEditGender = findViewById(R.id.etEditGender);
        etEditCountry = findViewById(R.id.etEditCountry);
        etEditBloodGroup = findViewById(R.id.etEditBloodGroup);
        etEditActivityLevel = findViewById(R.id.etEditActivityLevel);
        etEditGoal = findViewById(R.id.etEditGoal);
        etEditDietary = findViewById(R.id.etEditDietary);
        etEditConditions = findViewById(R.id.etEditConditions);

        etEditHeight = findViewById(R.id.etEditHeight);
        etEditWeight = findViewById(R.id.etEditWeight);
        etEditTargetWeight = findViewById(R.id.etEditTargetWeight);

        ivEditProfilePic = findViewById(R.id.ivEditProfilePic);
        btnChangePic = findViewById(R.id.btnChangePic);
        btnRemovePic = findViewById(R.id.btnRemovePic);
        btnCancel = findViewById(R.id.btnCancel);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        setupDropdowns();
        loadUserData();

        // Listeners
        etEditDob.setOnClickListener(v -> showDatePicker());
        btnChangePic.setOnClickListener(v -> showImagePickerDialog());
        btnRemovePic.setOnClickListener(v -> removeProfilePhoto());
        btnCancel.setOnClickListener(v -> finish());
        btnSaveProfile.setOnClickListener(v -> saveProfileData());
    }

    private void setupDropdowns() {
        String[] genders = {"Male", "Female", "Other"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genders);
        etEditGender.setAdapter(genderAdapter);

        String[] bloodGroups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        ArrayAdapter<String> bloodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, bloodGroups);
        etEditBloodGroup.setAdapter(bloodAdapter);

        String[] activityLevels = {"Sedentary", "Lightly Active", "Moderately Active", "Very Active"};
        ArrayAdapter<String> activityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, activityLevels);
        etEditActivityLevel.setAdapter(activityAdapter);

        String[] goals = {"Build Muscle", "Lose Weight", "Improve Endurance", "General Fitness"};
        ArrayAdapter<String> goalAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, goals);
        etEditGoal.setAdapter(goalAdapter);

        String[] diets = {"Vegetarian", "Vegan", "Keto", "Balanced", "High-Protein"};
        ArrayAdapter<String> dietAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, diets);
        etEditDietary.setAdapter(dietAdapter);

        List<String> countries = com.fitness.app.utils.LocationUtils.getCountries();
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, countries);
        etEditCountry.setAdapter(countryAdapter);

        etEditCountry.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCountry = etEditCountry.getText().toString();
            etEditCity.setText("");
            updateCityDropdown(selectedCountry);
        });

        etEditCity.setOnClickListener(v -> {
            String country = etEditCountry.getText().toString().trim();
            if (country.isEmpty() || !com.fitness.app.utils.ValidationUtils.isValidCountry(country)) {
                etEditCountry.setError("Please select a valid country first");
                scrollToView(etEditCountry);
                return;
            }
            showCitySearchDialog(country);
        });

        tilEditConditions = findViewById(R.id.tilEditConditions);

        String[] conditions = {"None", "Hypertension (High BP)", "Diabetes (Type 1 / Type 2)", "Asthma / Respiratory", "Heart Condition", "Joint / Arthritis", "Thyroid Disorder", "Other"};
        ArrayAdapter<String> medAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, conditions);
        etEditConditions.setAdapter(medAdapter);

        View.OnClickListener medClickListener = v -> showMedicalConditionDialogEdit();
        etEditConditions.setOnClickListener(medClickListener);
        etEditConditions.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                showMedicalConditionDialogEdit();
            }
            return false;
        });
        if (tilEditConditions != null) {
            tilEditConditions.setEndIconOnClickListener(medClickListener);
        }
    }

    private void showMedicalConditionDialogEdit() {
        String[] options = {
            "None", "Hypertension (High BP)", "Diabetes (Type 1 / Type 2)",
            "Asthma / Respiratory", "Heart Condition", "Joint / Arthritis",
            "Thyroid Disorder", "Other"
        };

        new android.app.AlertDialog.Builder(this)
            .setTitle("Select Medical Condition")
            .setItems(options, (dialog, which) -> {
                etEditConditions.setText(options[which], false);
                etEditConditions.setError(null);
                dialog.dismiss();
            })
            .show();
    }

    private void updateCityDropdown(String country) {
        List<String> cities = com.fitness.app.utils.LocationUtils.getCitiesForCountry(country);
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, cities);
        etEditCity.setAdapter(cityAdapter);
    }

    private void showCitySearchDialog(String country) {
        List<String> cityList = com.fitness.app.utils.LocationUtils.getCitiesForCountry(country);
        if (cityList == null || cityList.isEmpty()) {
            Toast.makeText(this, "No cities available for " + country, Toast.LENGTH_SHORT).show();
            return;
        }

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Select City for " + country);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(32, 24, 32, 16);

        EditText etSearch = new EditText(this);
        etSearch.setHint("🔍 Search city...");
        etSearch.setPadding(24, 20, 24, 20);
        etSearch.setBackgroundResource(R.drawable.bg_edittext);
        layout.addView(etSearch);

        android.widget.ListView listView = new android.widget.ListView(this);
        listView.setPadding(0, 16, 0, 0);
        layout.addView(listView);

        builder.setView(layout);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new 8ArrayList<>(cityList));
        listView.setAdapter(adapter);

        android.app.AlertDialog dialog = builder.create();

        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCity = adapter.getItem(position);
            if (selectedCity != null) {
                etEditCity.setText(selectedCity);
                etEditCity.setError(null);
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private void loadUserData() {
        tvEditProfileName.setText(user.getFirstName() + " " + user.getLastName());
        tvEditProfileEmail.setText(user.getEmail());

        etEditFirstName.setText(user.getFirstName());
        etEditLastName.setText(user.getLastName());
        etEditEmail.setText(user.getEmail());
        etEditDob.setText(user.getDob() != null ? user.getDob() : "");
        etEditGender.setText(user.getGender() != null ? user.getGender() : "", false);
        etEditCountry.setText(user.getCountry() != null ? user.getCountry() : "", false);
        etEditCity.setText(user.getCity() != null ? user.getCity() : "");
        etEditBloodGroup.setText(user.getBloodGroup() != null ? user.getBloodGroup() : "", false);

        boolean isMetric = localDb.isMetricUnitsEnabled();
        if (isMetric) {
            etEditHeight.setText(String.valueOf(user.getHeight()));
            etEditWeight.setText(String.valueOf(user.getWeight()));
            etEditTargetWeight.setText(user.getTargetWeight() > 0 ? String.valueOf(user.getTargetWeight()) : "");
        } else {
            double inches = user.getHeight() / 2.54;
            double lbs = user.getWeight() * 2.20462;
            double targetLbs = user.getTargetWeight() * 2.20462;
            etEditHeight.setText(String.format(Locale.US, "%.1f", inches));
            etEditWeight.setText(String.format(Locale.US, "%.1f", lbs));
            etEditTargetWeight.setText(user.getTargetWeight() > 0 ? String.format(Locale.US, "%.1f", targetLbs) : "");
        }

        etEditGoal.setText(user.getGoal() != null ? user.getGoal() : "", false);
        etEditActivityLevel.setText(user.getActivityLevel() != null ? user.getActivityLevel() : "", false);
        etEditDietary.setText(user.getDietaryPreference() != null ? user.getDietaryPreference() : "", false);

        etEditConditions.setText(user.getMedicalConditions() != null ? user.getMedicalConditions() : "");

        selectedImageUriString = user.getProfileImageUrl();
        refreshProfileImage();
        updateBadges();
    }

    private void updateBadges() {
        TextView tvGoalBadge = findViewById(R.id.tvEditProfileGoalBadge);
        if (user.getGoal() != null && !user.getGoal().isEmpty()) {
            tvGoalBadge.setText("Goal: " + user.getGoal());
        } else {
            tvGoalBadge.setText("Goal: Improve Shape");
        }
    }

    private void refreshProfileImage() {
        if (selectedImageUriString != null && !selectedImageUriString.trim().isEmpty()) {
            try {
                Glide.with(this)
                        .load(selectedImageUriString)
                        .placeholder(R.drawable.onboarding_1)
                        .error(R.drawable.onboarding_1)
                        .circleCrop()
                        .into(ivEditProfilePic);
            } catch (Exception e) {
                ivEditProfilePic.setImageResource(R.drawable.onboarding_1);
            }
        } else {
            ivEditProfilePic.setImageResource(R.drawable.onboarding_1);
        }
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date of Birth")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(selection);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            etEditDob.setText(sdf.format(calendar.getTime()));
        });
        datePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
    }

    private void showImagePickerDialog() {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_image_picker, null);

        view.findViewById(R.id.btnBottomSheetCamera).setOnClickListener(v -> {
            bottomSheet.dismiss();
            checkCameraPermissionAndLaunch();
        });
        view.findViewById(R.id.btnBottomSheetGallery).setOnClickListener(v -> {
            bottomSheet.dismiss();
            launchGallery();
        });
        view.findViewById(R.id.btnBottomSheetRemove).setOnClickListener(v -> {
            bottomSheet.dismiss();
            removeProfilePhoto();
        });
        view.findViewById(R.id.btnBottomSheetCancel).setOnClickListener(v -> {
            bottomSheet.dismiss();
        });

        bottomSheet.setContentView(view);
        bottomSheet.show();
    }

    private void checkCameraPermissionAndLaunch() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            androidx.core.app.ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
        } else {
            launchCamera();
        }
    }

    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, CAPTURE_IMAGE_CAMERA);
        } else {
            // Mock capture support for emulate runs
            selectedImageUriString = "mock_camera_image";
            ivEditProfilePic.setImageResource(R.drawable.profile_complete);
            Toast.makeText(this, "Camera Simulation Loaded", Toast.LENGTH_SHORT).show();
        }
    }

    private void launchGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_GALLERY);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to capture photos.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Uri saveBitmapToCache(Bitmap bitmap, String filename) {
        try {
            java.io.File cacheFile = new java.io.File(getCacheDir(), filename);
            java.io.FileOutputStream out = new java.io.FileOutputStream(cacheFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            return Uri.fromFile(cacheFile);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_GALLERY && data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    Intent cropIntent = new Intent(this, CropActivity.class);
                    cropIntent.putExtra("image_uri", uri.toString());
                    startActivityForResult(cropIntent, CROP_IMAGE);
                }
            } else if (requestCode == CAPTURE_IMAGE_CAMERA && data != null) {
                Bundle extras = data.getExtras();
                if (extras != null && extras.containsKey("data")) {
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    Uri cacheUri = saveBitmapToCache(imageBitmap, "captured_profile.jpg");
                    if (cacheUri != null) {
                        Intent cropIntent = new Intent(this, CropActivity.class);
                        cropIntent.putExtra("image_uri", cacheUri.toString());
                        startActivityForResult(cropIntent, CROP_IMAGE);
                    }
                }
            } else if (requestCode == CROP_IMAGE && data != null) {
                String croppedUriStr = data.getStringExtra("cropped_uri");
                if (croppedUriStr != null) {
                    selectedImageUriString = croppedUriStr;
                    refreshProfileImage();
                    Toast.makeText(this, "Profile picture cropped and updated!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void removeProfilePhoto() {
        selectedImageUriString = null;
        refreshProfileImage();
        Toast.makeText(this, "Profile image removed", Toast.LENGTH_SHORT).show();
    }

    private void scrollToView(View view) {
        if (view == null) return;
        view.requestFocus();
        if (scrollView != null) {
            scrollView.post(() -> scrollView.smoothScrollTo(0, view.getTop() - 100));
        }
    }

    private void saveProfileData() {
        String firstName = etEditFirstName.getText().toString().trim();
        String lastName = etEditLastName.getText().toString().trim();
        String email = etEditEmail.getText().toString().trim();
        String dob = etEditDob.getText().toString().trim();
        String country = etEditCountry.getText().toString().trim();
        String city = etEditCity.getText().toString().trim();
        String conditions = etEditConditions.getText().toString().trim();

        if (!com.fitness.app.utils.ValidationUtils.isValidName(firstName)) {
            etEditFirstName.setError("Please enter a valid first name (letters only)");
            scrollToView(etEditFirstName);
            return;
        }
        if (!com.fitness.app.utils.ValidationUtils.isValidName(lastName)) {
            etEditLastName.setError("Please enter a valid last name (letters only)");
            scrollToView(etEditLastName);
            return;
        }
        if (!com.fitness.app.utils.ValidationUtils.isValidEmail(email)) {
            etEditEmail.setError("Please enter a valid email address");
            scrollToView(etEditEmail);
            return;
        }
        if (dob.isEmpty()) {
            etEditDob.setError("Date of Birth is required");
            scrollToView(etEditDob);
            return;
        }
        int age = com.fitness.app.utils.ValidationUtils.calculateAge(dob);
        if (age == -1 || !com.fitness.app.utils.ValidationUtils.isValidAge(age)) {
            etEditDob.setError("Please enter a valid age (10 - 100 years)");
            scrollToView(etEditDob);
            return;
        }

        double heightVal;
        boolean metricEnabled = localDb.isMetricUnitsEnabled();
        String ht = etEditHeight.getText().toString().trim();
        double parsedHt = com.fitness.app.utils.ValidationUtils.parseHeight(ht);
        if (parsedHt == -1) {
            etEditHeight.setError("Please enter a valid height");
            scrollToView(etEditHeight);
            return;
        }
        heightVal = metricEnabled ? parsedHt : parsedHt * 2.54;
        if (!com.fitness.app.utils.ValidationUtils.isValidHeight(heightVal)) {
            etEditHeight.setError("Please enter a valid height (50 - 250 cm)");
            scrollToView(etEditHeight);
            return;
        }

        double weightVal;
        String wt = etEditWeight.getText().toString().trim();
        double parsedWt = com.fitness.app.utils.ValidationUtils.parseWeight(wt);
        if (parsedWt == -1) {
            etEditWeight.setError("Please enter a valid weight");
            scrollToView(etEditWeight);
            return;
        }
        weightVal = metricEnabled ? parsedWt : parsedWt / 2.20462;
        if (!com.fitness.app.utils.ValidationUtils.isValidWeight(weightVal)) {
            etEditWeight.setError("Please enter a valid weight (20 - 300 kg)");
            scrollToView(etEditWeight);
            return;
        }

        if (!com.fitness.app.utils.ValidationUtils.isValidCountry(country)) {
            etEditCountry.setError("Please select a valid country");
            scrollToView(etEditCountry);
            return;
        }

        if (!com.fitness.app.utils.LocationUtils.isValidCityForCountry(country, city)) {
            etEditCity.setError("Please enter or select a valid city for " + country);
            scrollToView(etEditCity);
            return;
        }

        if (!com.fitness.app.utils.ValidationUtils.isValidMedicalCondition(conditions)) {
            etEditConditions.setError("Please select a valid medical condition option");
            scrollToView(etEditConditions);
            return;
        }

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setDob(dob);
        user.setGender(etEditGender.getText().toString().trim());
        user.setCountry(country);
        user.setCity(city);
        user.setBloodGroup(etEditBloodGroup.getText().toString().trim());
        user.setHeight(heightVal);
        user.setWeight(weightVal);

        try {
            String tw = etEditTargetWeight.getText().toString().trim();
            if (tw.isEmpty()) {
                user.setTargetWeight(0);
            } else {
                double inputTw = Double.parseDouble(tw);
                user.setTargetWeight(metricEnabled ? inputTw : inputTw / 2.20462);
            }
        } catch (NumberFormatException e) {
            user.setTargetWeight(0);
        }

        user.setGoal(etEditGoal.getText().toString().trim());
        user.setActivityLevel(etEditActivityLevel.getText().toString().trim());
        user.setDietaryPreference(etEditDietary.getText().toString().trim());
        user.setMedicalConditions(conditions);

        user.setProfileImageUrl(selectedImageUriString);
        user.setProfileCompleted(true);
        user.setUpdatedAt(System.currentTimeMillis());

        com.google.firebase.auth.FirebaseUser firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            user.setUid(firebaseUser.getUid());
        }

        // Auto calculate age
        if (!user.getDob().isEmpty()) {
            try {
                String[] parts = user.getDob().split("-");
                int birthYear = Integer.parseInt(parts[0]);
                int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                user.setAge(Math.max(0, currentYear - birthYear));
            } catch (Exception ignored) {
            }
        }

        boolean isLocalImage = selectedImageUriString != null &&
                !selectedImageUriString.startsWith("http://") &&
                !selectedImageUriString.startsWith("https://");

        btnSaveProfile.setEnabled(false);

        if (firebaseUser == null) {
            // Guest mode: save locally directly
            saveUserToLocalAndFirestore(user);
            return;
        }

        if (isLocalImage) {
            progressDialog.setMessage("Uploading profile picture...");
            progressDialog.show();
            profileViewModel.uploadProfileImage(user.getUid(), Uri.parse(selectedImageUriString)).observe(this, resource -> {
                if (resource != null) {
                    switch (resource.status) {
                        case SUCCESS:
                            String remoteUrl = resource.data;
                            user.setProfileImageUrl(remoteUrl);
                            progressDialog.setMessage("Saving profile data...");
                            saveUserToLocalAndFirestore(user);
                            break;
                        case ERROR:
                            if (progressDialog.isShowing()) progressDialog.dismiss();
                            btnSaveProfile.setEnabled(true);
                            Toast.makeText(this, "Profile picture saved locally.", Toast.LENGTH_SHORT).show();
                            saveUserToLocalAndFirestore(user);
                            break;
                        case LOADING:
                            break;
                    }
                }
            });
        } else {
            progressDialog.setMessage("Saving profile data...");
            progressDialog.show();
            saveUserToLocalAndFirestore(user);
        }
    }

    private void saveUserToLocalAndFirestore(User user) {
        // Save locally
        localDb.saveUser(user);

        // Update Today's ProgressLog
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        ProgressLog todayLog = localDb.getProgressLog(today);
        if (todayLog != null) {
            todayLog.setCurrentWeight(user.getWeight());
            todayLog.setCurrentHeight(user.getHeight());
            localDb.saveProgressLog(todayLog);
        }

        com.google.firebase.auth.FirebaseUser firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            if (progressDialog.isShowing()) progressDialog.dismiss();
            btnSaveProfile.setEnabled(true);
            showSuccessNotificationAndFinish();
            return;
        }

        // Save to Firestore
        profileViewModel.updateUserProfile(user).observe(this, resource -> {
            if (resource != null) {
                switch (resource.status) {
                    case SUCCESS:
                    case ERROR:
                        if (progressDialog.isShowing()) progressDialog.dismiss();
                        btnSaveProfile.setEnabled(true);
                        showSuccessNotificationAndFinish();
                        break;
                    case LOADING:
                        break;
                }
            }
        });
    }

    private void showSuccessNotificationAndFinish() {
        Toast.makeText(this, "Your personal data has been successfully saved.", Toast.LENGTH_LONG).show();
        btnSaveProfile.postDelayed(this::finish, 2000);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}

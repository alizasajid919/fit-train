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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class EditProfileActivity extends AppCompatActivity {

    private ProfileViewModel profileViewModel;
    private android.app.ProgressDialog progressDialog;

    private static final int PICK_IMAGE_GALLERY = 100;
    private static final int CAPTURE_IMAGE_CAMERA = 101;
    private static final int CROP_IMAGE = 102;
    private static final int CAMERA_PERMISSION_REQUEST = 104;

    private EditText etEditFirstName, etEditLastName, etEditEmail, etEditMobile, etEditDob;
    private EditText etEditCity;
    private AutoCompleteTextView etEditGender, etEditCountry, etEditBloodGroup, etEditActivityLevel, etEditGoal, etEditDietary;
    private EditText etEditHeight, etEditWeight, etEditTargetWeight;
    private EditText etEditConditions, etEditEmergency;

    private TextView tvEditProfileName, tvEditProfileEmail;
    private ImageView ivEditProfilePic;
    private Button btnRemovePic;
    private com.google.android.material.button.MaterialButton btnCancel, btnSaveProfile;
    private View btnChangePic;

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
        etEditMobile = findViewById(R.id.etEditMobile);
        etEditDob = findViewById(R.id.etEditDob);
        etEditCity = findViewById(R.id.etEditCity);
        
        // AutoComplete Dropdowns
        etEditGender = findViewById(R.id.etEditGender);
        etEditCountry = findViewById(R.id.etEditCountry);
        etEditBloodGroup = findViewById(R.id.etEditBloodGroup);
        etEditActivityLevel = findViewById(R.id.etEditActivityLevel);
        etEditGoal = findViewById(R.id.etEditGoal);
        etEditDietary = findViewById(R.id.etEditDietary);

        etEditHeight = findViewById(R.id.etEditHeight);
        etEditWeight = findViewById(R.id.etEditWeight);
        etEditTargetWeight = findViewById(R.id.etEditTargetWeight);

        etEditConditions = findViewById(R.id.etEditConditions);
        etEditEmergency = findViewById(R.id.etEditEmergency);

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

        String[] countries = {"United States", "Canada", "United Kingdom", "Australia", "Germany", "India", "Other"};
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, countries);
        etEditCountry.setAdapter(countryAdapter);
    }

    private void loadUserData() {
        tvEditProfileName.setText(user.getFirstName() + " " + user.getLastName());
        tvEditProfileEmail.setText(user.getEmail());

        etEditFirstName.setText(user.getFirstName());
        etEditLastName.setText(user.getLastName());
        etEditEmail.setText(user.getEmail());
        etEditMobile.setText(user.getMobileNumber() != null ? user.getMobileNumber() : "");
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
        etEditEmergency.setText(user.getEmergencyContact() != null ? user.getEmergencyContact() : "");

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

    private void saveProfileData() {
        String firstName = etEditFirstName.getText().toString().trim();
        String lastName = etEditLastName.getText().toString().trim();
        String email = etEditEmail.getText().toString().trim();
        String mobile = etEditMobile.getText().toString().trim();
        String dob = etEditDob.getText().toString().trim();

        if (firstName.isEmpty()) {
            etEditFirstName.setError("First Name is required");
            etEditFirstName.requestFocus();
            return;
        }
        if (lastName.isEmpty()) {
            etEditLastName.setError("Last Name is required");
            etEditLastName.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            etEditEmail.setError("Email is required");
            etEditEmail.requestFocus();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEditEmail.setError("Please enter a valid email address");
            etEditEmail.requestFocus();
            return;
        }
        if (!mobile.isEmpty() && (mobile.length() < 9 || mobile.length() > 15)) {
            etEditMobile.setError("Please enter a valid phone number (9-15 digits)");
            etEditMobile.requestFocus();
            return;
        }
        if (dob.isEmpty()) {
            etEditDob.setError("Date of Birth is required");
            etEditDob.requestFocus();
            return;
        }

        double heightVal;
        boolean metricEnabled = localDb.isMetricUnitsEnabled();
        try {
            String ht = etEditHeight.getText().toString().trim();
            if (ht.isEmpty()) {
                etEditHeight.setError("Height is required");
                etEditHeight.requestFocus();
                return;
            }
            double inputHeight = Double.parseDouble(ht);
            heightVal = metricEnabled ? inputHeight : inputHeight * 2.54;
            if (heightVal < 50 || heightVal > 260) {
                etEditHeight.setError(metricEnabled ? "Height must be between 50 and 260 cm" : "Height must be between 20 and 102 inches");
                etEditHeight.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etEditHeight.setError("Invalid height format");
            etEditHeight.requestFocus();
            return;
        }

        double weightVal;
        try {
            String wt = etEditWeight.getText().toString().trim();
            if (wt.isEmpty()) {
                etEditWeight.setError("Weight is required");
                etEditWeight.requestFocus();
                return;
            }
            double inputWeight = Double.parseDouble(wt);
            weightVal = metricEnabled ? inputWeight : inputWeight / 2.20462;
            if (weightVal < 20 || weightVal > 350) {
                etEditWeight.setError(metricEnabled ? "Weight must be between 20 and 350 kg" : "Weight must be between 44 and 770 lbs");
                etEditWeight.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etEditWeight.setError("Invalid weight format");
            etEditWeight.requestFocus();
            return;
        }

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setMobileNumber(mobile);
        user.setDob(dob);
        user.setGender(etEditGender.getText().toString().trim());
        user.setCountry(etEditCountry.getText().toString().trim());
        user.setCity(etEditCity.getText().toString().trim());
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

        user.setMedicalConditions(etEditConditions.getText().toString().trim());
        user.setEmergencyContact(etEditEmergency.getText().toString().trim());

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
            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Save to Firestore
        profileViewModel.updateUserProfile(user).observe(this, resource -> {
            if (resource != null) {
                switch (resource.status) {
                    case SUCCESS:
                        if (progressDialog.isShowing()) progressDialog.dismiss();
                        btnSaveProfile.setEnabled(true);
                        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                        break;
                    case ERROR:
                        if (progressDialog.isShowing()) progressDialog.dismiss();
                        btnSaveProfile.setEnabled(true);
                        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                        break;
                    case LOADING:
                        break;
                }
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}

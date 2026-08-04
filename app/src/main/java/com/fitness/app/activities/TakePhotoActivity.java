package com.fitness.app.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.fitness.app.R;
import com.fitness.app.databinding.ActivityTakePhotoBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class TakePhotoActivity extends AppCompatActivity {

    private static final String TAG = "TakePhotoActivity";
    private static final int CAMERA_PERMISSION_CODE = 1001;

    private ActivityTakePhotoBinding binding;
    private SharedPreferences sharedPrefs;

    // CameraX variables
    private ProcessCameraProvider cameraProvider;
    private ImageCapture imageCapture;
    private Camera camera;
    
    private int currentFlashMode = ImageCapture.FLASH_MODE_OFF; // ImageCapture.FLASH_MODE_OFF / ON / AUTO
    private boolean isCameraBack = true;
    private int currentSelectedPose = 1; // 1 = Front, 2 = Back, 3 = Left, 4 = Right

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTakePhotoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPrefs = getSharedPreferences("ProgressTrackerPrefs", MODE_PRIVATE);

        // TASK 1: Top App Bar Navigation
        binding.btnBack.setOnClickListener(v -> onBackPressed());

        binding.btnMore.setOnClickListener(v -> {
            androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(TakePhotoActivity.this, binding.btnMore);
            popup.getMenu().add("Toggle Grid Lines");
            popup.getMenu().add("Camera Guidelines");
            popup.getMenu().add("Settings");
            popup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();
                if (title.equals("Camera Guidelines")) {
                    new AlertDialog.Builder(TakePhotoActivity.this)
                            .setTitle("Photo Guidelines")
                            .setMessage("For best results:\n1. Stand relaxed in front of a mirror or have someone take the photo.\n2. Keep your camera at chest level.\n3. Align your body with the silhouette overlay guideline.")
                            .setPositiveButton("Got It", null)
                            .show();
                } else if (title.equals("Toggle Grid Lines")) {
                    Toast.makeText(this, "Grid Lines toggled", Toast.LENGTH_SHORT).show();
                } else if (title.equals("Settings")) {
                    startActivity(new Intent(TakePhotoActivity.this, ProgressSettingsActivity.class));
                }
                return true;
            });
            popup.show();
        });

        // TASK 4: Flash Control Mode toggler
        binding.btnFlash.setOnClickListener(v -> toggleFlashMode());

        // TASK 4: Flip camera selector
        binding.btnFlip.setOnClickListener(v -> flipCameraSelector());

        // TASK 4: Shutter click -> Capture photo and save to gallery
        binding.btnShutter.setOnClickListener(v -> capturePhoto());

        // TASK 5: Select Pose Guide rows click listeners
        binding.btnPoseFront.setOnClickListener(v -> selectPoseGuide(1));
        binding.btnPoseBack.setOnClickListener(v -> selectPoseGuide(2));
        binding.btnPoseLeft.setOnClickListener(v -> selectPoseGuide(3));
        binding.btnPoseRight.setOnClickListener(v -> selectPoseGuide(4));

        // Initial pose selection setup
        selectPoseGuide(1);

        // TASK 2: Camera permissions request and preview setup
        checkCameraPermission();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCameraPreview();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                new AlertDialog.Builder(this)
                        .setTitle("Camera Permission Needed")
                        .setMessage("Camera access is required to take monthly progress photos and display the silhouette pose overlay.")
                        .setPositiveButton("Grant", (dialog, which) -> 
                            ActivityCompat.requestPermissions(TakePhotoActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE)
                        )
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            Toast.makeText(TakePhotoActivity.this, "Camera permission denied. Cannot capture photo.", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCameraPreview();
            } else {
                Toast.makeText(this, "Camera permission denied. Cannot capture progress photos.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void startCameraPreview() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting CameraX provider", e);
                Toast.makeText(this, "Failed to start camera preview", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases() {
        if (cameraProvider == null) return;

        // Unbind any active use cases first
        cameraProvider.unbindAll();

        // 1. Preview UseCase
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

        // 2. ImageCapture UseCase
        imageCapture = new ImageCapture.Builder()
                .setFlashMode(currentFlashMode)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        // 3. CameraSelector
        CameraSelector cameraSelector = isCameraBack 
                ? CameraSelector.DEFAULT_BACK_CAMERA 
                : CameraSelector.DEFAULT_FRONT_CAMERA;

        try {
            // Bind lifecycle to camera provider
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
        } catch (Exception e) {
            Log.e(TAG, "Use case binding failed", e);
            Toast.makeText(this, "Failed to initialize camera use cases", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleFlashMode() {
        if (imageCapture == null) {
            Toast.makeText(this, "Camera not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentFlashMode == ImageCapture.FLASH_MODE_OFF) {
            currentFlashMode = ImageCapture.FLASH_MODE_ON;
            binding.btnFlash.setColorFilter(Color.YELLOW);
            Toast.makeText(this, "Flash mode: ON", Toast.LENGTH_SHORT).show();
        } else if (currentFlashMode == ImageCapture.FLASH_MODE_ON) {
            currentFlashMode = ImageCapture.FLASH_MODE_AUTO;
            binding.btnFlash.setColorFilter(Color.CYAN);
            Toast.makeText(this, "Flash mode: AUTO", Toast.LENGTH_SHORT).show();
        } else {
            currentFlashMode = ImageCapture.FLASH_MODE_OFF;
            binding.btnFlash.setColorFilter(Color.BLACK);
            Toast.makeText(this, "Flash mode: OFF", Toast.LENGTH_SHORT).show();
        }

        imageCapture.setFlashMode(currentFlashMode);
    }

    private void flipCameraSelector() {
        isCameraBack = !isCameraBack;
        startCameraPreview();
        Toast.makeText(this, isCameraBack ? "Switched to back camera" : "Switched to front selfie camera", Toast.LENGTH_SHORT).show();
    }

    private void selectPoseGuide(int poseId) {
        currentSelectedPose = poseId;

        // Reset backgrounds on thumbnails
        binding.btnPoseFront.setCardBackgroundColor(Color.parseColor("#22FFFFFF"));
        binding.btnPoseBack.setCardBackgroundColor(Color.parseColor("#22FFFFFF"));
        binding.btnPoseLeft.setCardBackgroundColor(Color.parseColor("#22FFFFFF"));
        binding.btnPoseRight.setCardBackgroundColor(Color.parseColor("#22FFFFFF"));

        // TASK 3 & 5: Update silhouette and select border indicator
        if (poseId == 1) {
            binding.btnPoseFront.setCardBackgroundColor(Color.parseColor("#66FFFFFF"));
            binding.ivPoseGuideOverlay.setImageResource(R.drawable.pose_guide_silhouette);
            binding.ivPoseGuideOverlay.setAlpha(0.45f);
        } else if (poseId == 2) {
            binding.btnPoseBack.setCardBackgroundColor(Color.parseColor("#66FFFFFF"));
            binding.ivPoseGuideOverlay.setImageResource(R.drawable.pose_guide_silhouette);
            binding.ivPoseGuideOverlay.setAlpha(0.35f);
        } else if (poseId == 3) {
            binding.btnPoseLeft.setCardBackgroundColor(Color.parseColor("#66FFFFFF"));
            binding.ivPoseGuideOverlay.setImageResource(R.drawable.pose_guide_silhouette);
            binding.ivPoseGuideOverlay.setAlpha(0.25f);
        } else if (poseId == 4) {
            binding.btnPoseRight.setCardBackgroundColor(Color.parseColor("#66FFFFFF"));
            binding.ivPoseGuideOverlay.setImageResource(R.drawable.pose_guide_silhouette);
            binding.ivPoseGuideOverlay.setAlpha(0.15f);
        }
    }

    private void capturePhoto() {
        if (imageCapture == null) {
            Toast.makeText(this, "Camera not ready for capture", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create directory for photos
        File picturesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (picturesDir != null && !picturesDir.exists()) {
            picturesDir.mkdirs();
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File photoFile = new File(picturesDir, "Progress_" + timeStamp + ".jpg");

        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        binding.btnShutter.setEnabled(false);
        Toast.makeText(this, "Capturing milestone...", Toast.LENGTH_SHORT).show();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                binding.btnShutter.setEnabled(true);
                
                // Save new capture data persistently to SharedPreferences
                saveCapturedPhotoMetadata(photoFile.getAbsolutePath());

                // Post-capture Flow confirmation
                Toast.makeText(TakePhotoActivity.this, "Photo captured & saved to milestone records!", Toast.LENGTH_LONG).show();

                // Finish and return back to updating the Gallery
                finish();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                binding.btnShutter.setEnabled(true);
                Log.e(TAG, "Photo capture failed: " + exception.getMessage(), exception);
                Toast.makeText(TakePhotoActivity.this, "Capture failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveCapturedPhotoMetadata(String absolutePath) {
        // Read existing set of captured photos
        Set<String> existing = sharedPrefs.getStringSet("captured_photos_set", null);
        Set<String> updated = new HashSet<>();
        if (existing != null) {
            updated.addAll(existing);
        }

        // Format metadata record: path|dateLabel|directionIndex
        // For date label: construct "5 July" format
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dayFmt = new SimpleDateFormat("d", Locale.getDefault());
        SimpleDateFormat monthFmt = new SimpleDateFormat("MMMM", Locale.getDefault());
        String dateString = dayFmt.format(cal.getTime()) + " " + monthFmt.format(cal.getTime()); // e.g. "5 July"

        // Direction mapping based on selected pose guide
        String direction = "Front Facing";
        if (currentSelectedPose == 2) direction = "Back Facing";
        else if (currentSelectedPose == 3) direction = "Left Facing";
        else if (currentSelectedPose == 4) direction = "Right Facing";

        String record = absolutePath + "|" + dateString + "|" + direction;
        updated.add(record);

        // Persist back
        sharedPrefs.edit().putStringSet("captured_photos_set", updated).apply();
    }
}

package com.fitness.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.fitness.app.R;
import com.fitness.app.utils.SharedPreferencesManager;
import com.fitness.app.utils.ValidationUtils;
import com.fitness.app.viewmodels.AuthViewModel;
import com.fitness.app.viewmodels.ProfileViewModel;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private CheckBox cbRememberMe;
    private View progressOverlay;
    
    private AuthViewModel authViewModel;
    private ProfileViewModel profileViewModel;
    private SharedPreferencesManager prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        prefs = new SharedPreferencesManager(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        progressOverlay = findViewById(R.id.progressOverlay);

        // Remember Me auto-fill
        if (prefs.isRememberMeEnabled()) {
            etEmail.setText(prefs.getSavedEmail());
            etPassword.setText(prefs.getSavedPassword());
            cbRememberMe.setChecked(true);
        }

        findViewById(R.id.btnLogin).setOnClickListener(v -> performLogin());
        
        findViewById(R.id.tvForgotPassword).setOnClickListener(v -> showForgotPasswordDialog());
        
        findViewById(R.id.tvDontHaveAccount).setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!ValidationUtils.isValidEmail(email)) {
            etEmail.setError("Please enter a valid email address.");
            etEmail.requestFocus();
            return;
        }

        if (!ValidationUtils.isValidPassword(password)) {
            etPassword.setError(getString(R.string.err_short_password));
            etPassword.requestFocus();
            return;
        }

        authViewModel.login(email, password, new com.fitness.app.data.local.LocalDataManager(this)).observe(this, resource -> {
            if (resource != null) {
                switch (resource.status) {
                    case LOADING:
                        progressOverlay.setVisibility(View.VISIBLE);
                        break;
                    case SUCCESS:
                        progressOverlay.setVisibility(View.GONE);
                        FirebaseUser user = resource.data;
                        if (user != null) {
                            // Check Remember Me
                            prefs.setRememberMe(cbRememberMe.isChecked(), email, password);

                            // Check if Email Verification is complete
                            if (!user.isEmailVerified()) {
                                Toast.makeText(this, "Please verify your email. Verification link was sent to " + email, Toast.LENGTH_LONG).show();
                            }
                            
                            checkProfileCompletionAndNavigate(user.getUid());
                        }
                        break;
                    case ERROR:
                        progressOverlay.setVisibility(View.GONE);
                        Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });
    }

    private void checkProfileCompletionAndNavigate(String uid) {
        profileViewModel.getUserProfile(uid).observe(this, resource -> {
            if (resource != null) {
                switch (resource.status) {
                    case SUCCESS:
                        if (resource.data != null && resource.data.isProfileCompleted()) {
                            // Already completed profile, go to Home Screen
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        } else {
                            // Not completed, go to Complete Profile
                            startActivity(new Intent(LoginActivity.this, CompleteProfileActivity.class));
                        }
                        finish();
                        break;
                    case ERROR:
                        // No profile found or load failed, proceed to complete profile
                        startActivity(new Intent(LoginActivity.this, CompleteProfileActivity.class));
                        finish();
                        break;
                    case LOADING:
                        progressOverlay.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Password");
        
        final EditText input = new EditText(this);
        input.setHint("Enter your email address");
        input.setPadding(32, 32, 32, 32);
        builder.setView(input);

        builder.setPositiveButton("Send", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (ValidationUtils.isValidEmail(email)) {
                authViewModel.resetPassword(email).observe(this, resource -> {
                    if (resource != null) {
                        switch (resource.status) {
                            case SUCCESS:
                                Toast.makeText(LoginActivity.this, "Password reset email sent!", Toast.LENGTH_SHORT).show();
                                break;
                            case ERROR:
                                Toast.makeText(LoginActivity.this, "Error: " + resource.message, Toast.LENGTH_LONG).show();
                                break;
                            case LOADING:
                                break;
                        }
                    }
                });
            } else {
                Toast.makeText(LoginActivity.this, "Invalid email address", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}

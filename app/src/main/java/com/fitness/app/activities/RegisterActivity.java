package com.fitness.app.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.fitness.app.R;
import com.fitness.app.utils.ValidationUtils;
import com.fitness.app.viewmodels.AuthViewModel;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etEmail, etPassword;
    private CheckBox cbTerms;
    private View progressOverlay;
    private Button btnRegister;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        cbTerms = findViewById(R.id.cbTerms);
        progressOverlay = findViewById(R.id.progressOverlay);
        btnRegister = findViewById(R.id.btnRegister);

        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> performRegistration());
        }

        findViewById(R.id.tvAlreadyHaveAccount).setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void performRegistration() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Field Validations
        if (!ValidationUtils.isValidName(firstName)) {
            etFirstName.setError("Please enter a valid first name (letters only)");
            etFirstName.requestFocus();
            return;
        }

        if (!ValidationUtils.isValidName(lastName)) {
            etLastName.setError("Please enter a valid last name (letters only)");
            etLastName.requestFocus();
            return;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            etEmail.setError("Please enter a valid email address.");
            etEmail.requestFocus();
            return;
        }

        if (!ValidationUtils.isValidPassword(password)) {
            etPassword.setError("Password must be at least 8 characters");
            etPassword.requestFocus();
            return;
        }

        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Please accept the Terms of Service to register", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state and disable button to prevent duplicate submissions
        if (btnRegister != null) {
            btnRegister.setEnabled(false);
            btnRegister.setText("Creating account...");
        }

        authViewModel.register(firstName, lastName, email, password, new com.fitness.app.data.local.LocalDataManager(this)).observe(this, resource -> {
            if (resource != null) {
                switch (resource.status) {
                    case LOADING:
                        if (progressOverlay != null) progressOverlay.setVisibility(View.VISIBLE);
                        break;
                    case SUCCESS:
                        if (progressOverlay != null) progressOverlay.setVisibility(View.GONE);
                        if (btnRegister != null) {
                            btnRegister.setEnabled(true);
                            btnRegister.setText(getString(R.string.register));
                        }
                        showEmailVerificationDialog();
                        break;
                    case ERROR:
                        if (progressOverlay != null) progressOverlay.setVisibility(View.GONE);
                        if (btnRegister != null) {
                            btnRegister.setEnabled(true);
                            btnRegister.setText(getString(R.string.register));
                        }
                        Toast.makeText(this, resource.message, Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });
    }

    private void showEmailVerificationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Verify Your Email");
        builder.setMessage("A verification email has been sent to your email address. Please verify your email before completing your profile.");
        builder.setCancelable(false);

        builder.setPositiveButton("Check Verification", (dialog, which) -> {
            checkEmailVerificationStatus(dialog);
        });

        builder.setNeutralButton("Resend Email", (dialog, which) -> {
            authViewModel.resendEmailVerification().observe(RegisterActivity.this, res -> {
                if (res != null && res.status == com.fitness.app.repositories.UserRepository.Resource.Status.SUCCESS) {
                    Toast.makeText(RegisterActivity.this, res.data, Toast.LENGTH_SHORT).show();
                } else if (res != null && res.status == com.fitness.app.repositories.UserRepository.Resource.Status.ERROR) {
                    Toast.makeText(RegisterActivity.this, res.message, Toast.LENGTH_SHORT).show();
                }
            });
            showEmailVerificationDialog();
        });

        builder.setNegativeButton("Sign In Later", (dialog, which) -> {
            dialog.dismiss();
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        builder.show();
    }

    private void checkEmailVerificationStatus(android.content.DialogInterface dialog) {
        if (progressOverlay != null) progressOverlay.setVisibility(View.VISIBLE);

        authViewModel.reloadAndCheckEmailVerification().observe(this, resource -> {
            if (resource != null) {
                switch (resource.status) {
                    case SUCCESS:
                        if (progressOverlay != null) progressOverlay.setVisibility(View.GONE);
                        Boolean isVerified = resource.data;
                        if (Boolean.TRUE.equals(isVerified)) {
                            if (dialog != null) dialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Email verified successfully! Please complete your profile.", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(RegisterActivity.this, CompleteProfileActivity.class));
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Email is not verified yet. Please check your inbox and click the verification link.", Toast.LENGTH_LONG).show();
                            showEmailVerificationDialog();
                        }
                        break;
                    case ERROR:
                        if (progressOverlay != null) progressOverlay.setVisibility(View.GONE);
                        Toast.makeText(RegisterActivity.this, resource.message, Toast.LENGTH_LONG).show();
                        showEmailVerificationDialog();
                        break;
                    case LOADING:
                        break;
                }
            }
        });
    }
}

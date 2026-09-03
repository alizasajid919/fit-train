package com.fitness.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

        findViewById(R.id.btnRegister).setOnClickListener(v -> performRegistration());
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
            etEmail.setError("Please enter a valid email address");
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

        authViewModel.register(firstName, lastName, email, password, new com.fitness.app.data.local.LocalDataManager(this)).observe(this, resource -> {
            if (resource != null) {
                switch (resource.status) {
                    case LOADING:
                        progressOverlay.setVisibility(View.VISIBLE);
                        break;
                    case SUCCESS:
                        progressOverlay.setVisibility(View.GONE);
                        Toast.makeText(this, "Registration successful! Verification email sent.", Toast.LENGTH_LONG).show();
                        // Proceed to complete profile setup
                        startActivity(new Intent(RegisterActivity.this, CompleteProfileActivity.class));
                        finish();
                        break;
                    case ERROR:
                        progressOverlay.setVisibility(View.GONE);
                        Toast.makeText(this, "Error: " + resource.message, Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });
    }
}

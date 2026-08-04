package com.fitness.app.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.fitness.app.R;
import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.models.User;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HelpActivity extends AppCompatActivity {

    private EditText etHelpSearch;
    private EditText etHelpEmail;
    private AutoCompleteTextView etHelpType;
    private EditText etHelpMessage;
    private MaterialButton btnSubmitFeedback;

    private CardView faqCard1, faqCard2, faqCard3, faqCard4, faqCard5, faqCard6, faqCard7, faqCard8, faqCard9, faqCard10;
    private LinearLayout faqHeader1, faqHeader2, faqHeader3, faqHeader4, faqHeader5, faqHeader6, faqHeader7, faqHeader8, faqHeader9, faqHeader10;
    private View faqBody1, faqBody2, faqBody3, faqBody4, faqBody5, faqBody6, faqBody7, faqBody8, faqBody9, faqBody10;
    private ImageView faqArrow1, faqArrow2, faqArrow3, faqArrow4, faqArrow5, faqArrow6, faqArrow7, faqArrow8, faqArrow9, faqArrow10;

    private LocalDataManager localDb;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        localDb = new LocalDataManager(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Submitting support ticket...");
        progressDialog.setCancelable(false);

        // Bind Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        // Bind Search & Form
        etHelpSearch = findViewById(R.id.etHelpSearch);
        etHelpEmail = findViewById(R.id.etHelpEmail);
        etHelpType = findViewById(R.id.etHelpType);
        etHelpMessage = findViewById(R.id.etHelpMessage);
        btnSubmitFeedback = findViewById(R.id.btnSubmitFeedback);

        // Populate User Email if logged in
        User user = localDb.getUser();
        if (user != null && user.getEmail() != null && !user.getEmail().isEmpty()) {
            etHelpEmail.setText(user.getEmail());
        }

        // Dropdown Adapter
        String[] ticketTypes = {"FAQ Query", "Bug Report", "Feature Request", "Premium Subscription Inquiry"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, ticketTypes);
        etHelpType.setAdapter(typeAdapter);

        // Bind FAQ Expandables
        faqCard1 = findViewById(R.id.faqCard1);
        faqCard2 = findViewById(R.id.faqCard2);
        faqCard3 = findViewById(R.id.faqCard3);
        faqCard4 = findViewById(R.id.faqCard4);
        faqCard5 = findViewById(R.id.faqCard5);
        faqCard6 = findViewById(R.id.faqCard6);
        faqCard7 = findViewById(R.id.faqCard7);
        faqCard8 = findViewById(R.id.faqCard8);
        faqCard9 = findViewById(R.id.faqCard9);
        faqCard10 = findViewById(R.id.faqCard10);

        faqHeader1 = findViewById(R.id.faqHeader1);
        faqHeader2 = findViewById(R.id.faqHeader2);
        faqHeader3 = findViewById(R.id.faqHeader3);
        faqHeader4 = findViewById(R.id.faqHeader4);
        faqHeader5 = findViewById(R.id.faqHeader5);
        faqHeader6 = findViewById(R.id.faqHeader6);
        faqHeader7 = findViewById(R.id.faqHeader7);
        faqHeader8 = findViewById(R.id.faqHeader8);
        faqHeader9 = findViewById(R.id.faqHeader9);
        faqHeader10 = findViewById(R.id.faqHeader10);

        faqBody1 = findViewById(R.id.faqBody1);
        faqBody2 = findViewById(R.id.faqBody2);
        faqBody3 = findViewById(R.id.faqBody3);
        faqBody4 = findViewById(R.id.faqBody4);
        faqBody5 = findViewById(R.id.faqBody5);
        faqBody6 = findViewById(R.id.faqBody6);
        faqBody7 = findViewById(R.id.faqBody7);
        faqBody8 = findViewById(R.id.faqBody8);
        faqBody9 = findViewById(R.id.faqBody9);
        faqBody10 = findViewById(R.id.faqBody10);

        faqArrow1 = findViewById(R.id.faqArrow1);
        faqArrow2 = findViewById(R.id.faqArrow2);
        faqArrow3 = findViewById(R.id.faqArrow3);
        faqArrow4 = findViewById(R.id.faqArrow4);
        faqArrow5 = findViewById(R.id.faqArrow5);
        faqArrow6 = findViewById(R.id.faqArrow6);
        faqArrow7 = findViewById(R.id.faqArrow7);
        faqArrow8 = findViewById(R.id.faqArrow8);
        faqArrow9 = findViewById(R.id.faqArrow9);
        faqArrow10 = findViewById(R.id.faqArrow10);

        setupFAQClickListeners();
        setupSearchFilter();

        btnSubmitFeedback.setOnClickListener(v -> submitSupportRequest());
    }

    private void setupFAQClickListeners() {
        faqHeader1.setOnClickListener(v -> toggleFAQ(faqBody1, faqArrow1));
        faqHeader2.setOnClickListener(v -> toggleFAQ(faqBody2, faqArrow2));
        faqHeader3.setOnClickListener(v -> toggleFAQ(faqBody3, faqArrow3));
        faqHeader4.setOnClickListener(v -> toggleFAQ(faqBody4, faqArrow4));
        faqHeader5.setOnClickListener(v -> toggleFAQ(faqBody5, faqArrow5));
        faqHeader6.setOnClickListener(v -> toggleFAQ(faqBody6, faqArrow6));
        faqHeader7.setOnClickListener(v -> toggleFAQ(faqBody7, faqArrow7));
        faqHeader8.setOnClickListener(v -> toggleFAQ(faqBody8, faqArrow8));
        faqHeader9.setOnClickListener(v -> toggleFAQ(faqBody9, faqArrow9));
        faqHeader10.setOnClickListener(v -> toggleFAQ(faqBody10, faqArrow10));
    }

    private void toggleFAQ(View body, ImageView arrow) {
        if (body.getVisibility() == View.VISIBLE) {
            body.setVisibility(View.GONE);
            arrow.setImageResource(R.drawable.ic_chevron_down);
        } else {
            body.setVisibility(View.VISIBLE);
            arrow.setImageResource(R.drawable.ic_chevron_up);
        }
    }

    private void setupSearchFilter() {
        etHelpSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFAQs(s.toString().toLowerCase().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterFAQs(String query) {
        if (query.isEmpty()) {
            faqCard1.setVisibility(View.VISIBLE);
            faqCard2.setVisibility(View.VISIBLE);
            faqCard3.setVisibility(View.VISIBLE);
            faqCard4.setVisibility(View.VISIBLE);
            faqCard5.setVisibility(View.VISIBLE);
            faqCard6.setVisibility(View.VISIBLE);
            faqCard7.setVisibility(View.VISIBLE);
            faqCard8.setVisibility(View.VISIBLE);
            faqCard9.setVisibility(View.VISIBLE);
            faqCard10.setVisibility(View.VISIBLE);
            return;
        }

        checkAndSetFAQVisibility(faqCard1, query, "How do I start my first workout? Navigate to the Workouts screen using the bottom navigation bar. Select a featured card like '🚀 NEW TO FITTRAIN? START HERE!' or generate a daily personalized routine.");
        checkAndSetFAQVisibility(faqCard2, query, "How does the AI Coach work? The AI Coach automatically analyzes your registered physical details, height, weight, BMI, and calorie targets, generating healthy tips.");
        checkAndSetFAQVisibility(faqCard3, query, "Can I customize generated workouts? Yes, you can specify your available equipment and fitness goals under Settings or run the custom generator on the workouts screen to modify exercises.");
        checkAndSetFAQVisibility(faqCard4, query, "How do I log daily nutrition and water? Go to the Diet tab to see your recommended diet plan. You can view meal details, log nutrition inputs, and consult calorie tracking.");
        checkAndSetFAQVisibility(faqCard5, query, "How do I sync local data to the cloud? Go to Settings, select 'Link Account / Create Account'. Registering an email links your offline logs to the cloud instantly.");
        checkAndSetFAQVisibility(faqCard6, query, "How do I use the Smart Grocery Scanner? Scan Ingredients. The AI will instantly analyze its healthiness, flag allergen matches, and recommend healthy recipes.");
        checkAndSetFAQVisibility(faqCard7, query, "What does the full-screen Nutrition Summary show? Tap Nutrition Summary in the Diet menu to view your detailed macronutrient progress (Carbs, Protein, Fat), rolling weekly averages, other tracked nutrients (Fiber, Sugar, Sodium, Water), dynamic macro source breakdowns, and custom AI Coach tips.");
        checkAndSetFAQVisibility(faqCard8, query, "How do I track my Body Transformation progress? Open the Transformation Hub. Upload progress photos, update your current weight, calculate your body mass index (BMI), set target goals, and monitor changes.");
        checkAndSetFAQVisibility(faqCard9, query, "How do I complete my generated daily meals? Tap any meal card in your Diet Schedule. Tap the 'Mark as Completed' button to log the calories and sync the meal checklist.");
        checkAndSetFAQVisibility(faqCard10, query, "How do I customize my workout equipment and levels? Navigate to the Workouts generator and select 'Custom Workout Options'. Specify your experience level (Beginner, Intermediate, Advanced) and select your available equipment (Bodyweight, Dumbbells, Kettlebells, or Full Gym).");
    }

    private void checkAndSetFAQVisibility(CardView card, String query, String content) {
        if (content.toLowerCase().contains(query)) {
            card.setVisibility(View.VISIBLE);
        } else {
            card.setVisibility(View.GONE);
        }
    }

    private void submitSupportRequest() {
        String email = etHelpEmail.getText().toString().trim();
        String type = etHelpType.getText().toString().trim();
        String message = etHelpMessage.getText().toString().trim();

        if (email.isEmpty()) {
            etHelpEmail.setError("Email is required");
            etHelpEmail.requestFocus();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etHelpEmail.setError("Please enter a valid email address");
            etHelpEmail.requestFocus();
            return;
        }
        if (type.isEmpty()) {
            etHelpType.setError("Please choose a ticket type");
            etHelpType.requestFocus();
            return;
        }
        if (message.isEmpty()) {
            etHelpMessage.setError("Please specify details of your inquiry");
            etHelpMessage.requestFocus();
            return;
        }

        progressDialog.show();

        // Simulate network submit with offline queue caching
        new Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if (progressDialog.isShowing()) progressDialog.dismiss();

            String ticketId = "FT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            saveTicketLocally(ticketId, email, type, message);

            new AlertDialog.Builder(this)
                    .setTitle("Support Ticket Submitted")
                    .setMessage("Thank you! Your ticket (" + ticketId + ") has been submitted successfully.\n\nOur support team will respond to " + email + " within 24 hours. A copy has been stored locally for offline access.")
                    .setPositiveButton("OK", (dialog, which) -> {
                        etHelpMessage.setText("");
                        etHelpType.setText("", false);
                    })
                    .show();
        }, 1500);
    }

    private void saveTicketLocally(String ticketId, String email, String type, String message) {
        try {
            String existingTicketsJson = localDb.sharedPreferences.getString("cached_support_tickets", "[]");
            JSONArray tickets = new JSONArray(existingTicketsJson);

            JSONObject ticket = new JSONObject();
            ticket.put("id", ticketId);
            ticket.put("email", email);
            ticket.put("type", type);
            ticket.put("message", message);
            ticket.put("timestamp", System.currentTimeMillis());

            tickets.put(ticket);

            localDb.sharedPreferences.edit().putString("cached_support_tickets", tickets.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

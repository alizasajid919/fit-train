package com.fitness.app.activities;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.fitness.app.R;
import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.models.ChatMessage;
import com.fitness.app.models.User;
import com.fitness.app.utils.ChatManager;
import com.fitness.app.utils.FitnessCalculator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AICoachActivity extends AppCompatActivity {

    private LocalDataManager localDb;
    private ChatManager chatManager;

    // Layout views
    private TabLayout tabLayout;
    private View loadingLayout;
    private View errorLayout;
    private View dashboardLayout;
    private View chatLayout;

    // Dashboard elements
    private TextView tvDashboardMotivation;
    private TextView tvDashboardBmi;
    private TextView tvDashboardBmiRange;
    private TextView tvDashboardBmiAdvice;
    private TextView tvDashboardCalTarget;
    private TextView tvDashboardWaterTarget;
    private TextView tvDashboardWeightAdvice;
    private TextView tvBeginnerWorkout;
    private TextView tvIntermediateWorkout;
    private TextView tvAdvancedWorkout;
    private TextView tvDashboardRestAdvice;
    private TextView tvDashboardHealthTip;

    // Chat elements
    private RecyclerView rvChat;
    private EditText etMessage;
    private View chatEmptyState;
    private View searchContainer;
    private EditText etSearchQuery;
    private ImageButton btnSearchToggle;
    private ImageButton btnClearChat;

    // Chat Input Attachment Panel
    private View attachmentPreviewContainer;
    private ImageView ivAttachmentPreview;
    private TextView tvAttachmentName;
    private ImageButton btnRemoveAttachment;

    private ChatAdapter adapter;
    private final List<ChatMessage> allMessages = new ArrayList<>();
    private final List<ChatMessage> filteredMessages = new ArrayList<>();
    private boolean shouldScrollToBottom = true;
    private boolean userWaitingForResponse = false;
    private boolean isUserScrollingUp = false;
    private String lastHandledAiMsgId = null;
    private User currentUser;
    private int currentLimit = 40;

    // Attachment State
    private String selectedAttachmentUri = null;
    private String selectedAttachmentType = null;

    // Result Launchers
    private ActivityResultLauncher<String> recordAudioPermissionLauncher;
    private ActivityResultLauncher<Intent> speechRecognizerLauncher;
    private ActivityResultLauncher<Intent> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_coach);

        localDb = new LocalDataManager(this);
        chatManager = new ChatManager(this);
        currentUser = localDb.getUser();

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        // Bind layouts
        tabLayout = findViewById(R.id.tabLayout);
        loadingLayout = findViewById(R.id.loadingLayout);
        errorLayout = findViewById(R.id.errorLayout);
        dashboardLayout = findViewById(R.id.dashboardLayout);
        chatLayout = findViewById(R.id.chatLayout);

        // Bind dashboard views
        tvDashboardMotivation = findViewById(R.id.tvDashboardMotivation);
        tvDashboardBmi = findViewById(R.id.tvDashboardBmi);
        tvDashboardBmiRange = findViewById(R.id.tvDashboardBmiRange);
        tvDashboardBmiAdvice = findViewById(R.id.tvDashboardBmiAdvice);
        tvDashboardCalTarget = findViewById(R.id.tvDashboardCalTarget);
        tvDashboardWaterTarget = findViewById(R.id.tvDashboardWaterTarget);
        tvDashboardWeightAdvice = findViewById(R.id.tvDashboardWeightAdvice);
        tvBeginnerWorkout = findViewById(R.id.tvBeginnerWorkout);
        tvIntermediateWorkout = findViewById(R.id.tvIntermediateWorkout);
        tvAdvancedWorkout = findViewById(R.id.tvAdvancedWorkout);
        tvDashboardRestAdvice = findViewById(R.id.tvDashboardRestAdvice);
        tvDashboardHealthTip = findViewById(R.id.tvDashboardHealthTip);

        // Bind chat views
        rvChat = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etMessage);
        chatEmptyState = findViewById(R.id.chatEmptyState);
        searchContainer = findViewById(R.id.searchContainer);
        etSearchQuery = findViewById(R.id.etSearchQuery);
        btnSearchToggle = findViewById(R.id.btnSearchToggle);
        btnClearChat = findViewById(R.id.btnClearChat);

        // Bind attachment layout views
        attachmentPreviewContainer = findViewById(R.id.attachmentPreviewContainer);
        ivAttachmentPreview = findViewById(R.id.ivAttachmentPreview);
        tvAttachmentName = findViewById(R.id.tvAttachmentName);
        btnRemoveAttachment = findViewById(R.id.btnRemoveAttachment);

        // Setup launchers
        setupActivityLaunchers();

        // Setup RecyclerView
        adapter = new ChatAdapter(filteredMessages, this::onMessageActionClick);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(adapter);
        rvChat.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (lm != null) {
                        int lastVisible = lm.findLastCompletelyVisibleItemPosition();
                        int totalItems = adapter.getItemCount();
                        if (lastVisible < totalItems - 2) {
                            isUserScrollingUp = true;
                        }
                    }
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    if (layoutManager.findFirstVisibleItemPosition() == 0) {
                        if (currentLimit < allMessages.size()) {
                            currentLimit += 40;
                            shouldScrollToBottom = false;
                            filterAndDisplayMessages();
                        }
                    }
                    int lastVisible = layoutManager.findLastCompletelyVisibleItemPosition();
                    int totalItems = adapter.getItemCount();
                    if (totalItems > 0 && lastVisible >= totalItems - 2) {
                        isUserScrollingUp = false;
                    }
                }
            }
        });

        // Click Listeners
        findViewById(R.id.btnSend).setOnClickListener(v -> sendMessage());
        findViewById(R.id.btnUpdateProfile).setOnClickListener(v -> {
            startActivity(new Intent(AICoachActivity.this, EditProfileActivity.class));
            finish();
        });

        // Search Bar Toggle
        btnSearchToggle.setOnClickListener(v -> toggleSearchBar());
        findViewById(R.id.btnCancelSearch).setOnClickListener(v -> closeSearchBar());
        
        etSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterAndDisplayMessages();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Clear Chat
        btnClearChat.setOnClickListener(v -> showClearChatDialog());

        // Suggestions buttons
        setupSuggestionButtons();

        // Attachment triggers
        findViewById(R.id.btnAttach).setOnClickListener(v -> showAttachmentTypeDialog());
        btnRemoveAttachment.setOnClickListener(v -> clearSelectedAttachment());

        // Voice trigger
        findViewById(R.id.btnVoice).setOnClickListener(v -> checkVoicePermissionAndStart());

        // Setup TabLayout switching
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switchTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Observe Room Chat Messages
        chatManager.getMessagesLive().observe(this, messages -> {
            allMessages.clear();
            allMessages.addAll(messages);
            filterAndDisplayMessages();
        });

        // Observe AI generation loading state (and toggle send button clickability)
        chatManager.getIsLoadingLive().observe(this, isLoading -> {
            findViewById(R.id.btnSend).setEnabled(!isLoading);
            filterAndDisplayMessages();
        });

        // Observe error messages
        chatManager.getErrorLive().observe(this, errorMsg -> {
            if (errorMsg != null) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
            }
        });

        // Recover state on screen rotation if available
        if (savedInstanceState != null) {
            recoverSavedInstance(savedInstanceState);
        }

        // Select tab based on intent extra if provided
        int targetTab = getIntent().getIntExtra("select_tab", 0);
        if (targetTab >= 0 && targetTab < tabLayout.getTabCount()) {
            TabLayout.Tab tab = tabLayout.getTabAt(targetTab);
            if (tab != null) {
                tab.select();
            }
        }

        // Start 1.5 seconds loading state simulation
        startLoadingSimulation();
    }

    private void setupActivityLaunchers() {
        // Microphone permission
        recordAudioPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        startVoiceRecognition();
                    } else {
                        if (!shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                            showSettingsRedirectionDialog();
                        } else {
                            Toast.makeText(this, "Microphone permission is required for voice typing.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // Speech Recognizer
        speechRecognizerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        ArrayList<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        if (matches != null && !matches.isEmpty()) {
                            String spokenText = matches.get(0);
                            String currentText = etMessage.getText().toString();
                            if (!currentText.isEmpty()) {
                                etMessage.setText(String.format("%s %s", currentText, spokenText));
                            } else {
                                etMessage.setText(spokenText);
                            }
                            etMessage.setSelection(etMessage.getText().length());
                        }
                    }
                }
        );

        // Document Picker
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        android.net.Uri uri = result.getData().getData();
                        if (uri != null) {
                            try {
                                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            } catch (SecurityException e) {
                                // Silent fallback if persistable grant is unsupported
                            }
                            handleSelectedAttachment(uri);
                        }
                    }
                }
        );
    }

    private void checkVoicePermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startVoiceRecognition();
        } else {
            boolean hasRequestedBefore = getPreferences(MODE_PRIVATE).getBoolean("has_requested_mic", false);
            if (hasRequestedBefore && !shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                showSettingsRedirectionDialog();
            } else {
                getPreferences(MODE_PRIVATE).edit().putBoolean("has_requested_mic", true).apply();
                recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
            }
        }
    }

    private void showSettingsRedirectionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Microphone Permission Required")
                .setMessage("You have permanently disabled microphone access. Please enable it in the App Settings to use voice typing.")
                .setPositiveButton("Go to Settings", (dialog, which) -> {
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    android.net.Uri uri = android.net.Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to your AI coach...");
        try {
            speechRecognizerLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Speech recognition is not supported on this device.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAttachmentTypeDialog() {
        String[] options = {"Image", "PDF Document", "Text File"};
        new AlertDialog.Builder(this)
                .setTitle("Select attachment type")
                .setItems(options, (dialog, which) -> {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    if (which == 0) {
                        intent.setType("image/*");
                    } else if (which == 1) {
                        intent.setType("application/pdf");
                    } else {
                        intent.setType("text/plain");
                    }
                    filePickerLauncher.launch(intent);
                })
                .show();
    }

    private void handleSelectedAttachment(android.net.Uri uri) {
        String name = "Attachment";
        long size = 0;
        String type = getContentResolver().getType(uri);

        try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                int sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE);
                if (nameIndex != -1) name = cursor.getString(nameIndex);
                if (sizeIndex != -1) size = cursor.getLong(sizeIndex);
            }
        } catch (Exception e) {
            // Use defaults if query fails
        }

        // File size validation (5MB max)
        if (size > 5 * 1024 * 1024) {
            Toast.makeText(this, "Attachment size exceeds the 5MB limit.", Toast.LENGTH_LONG).show();
            return;
        }

        selectedAttachmentUri = uri.toString();

        if (type != null && type.startsWith("image/")) {
            selectedAttachmentType = "IMAGE";
            ivAttachmentPreview.setImageURI(uri);
            ivAttachmentPreview.setColorFilter(null);
        } else if (type != null && type.equals("application/pdf")) {
            selectedAttachmentType = "PDF";
            ivAttachmentPreview.setImageResource(android.R.drawable.ic_menu_save);
            ivAttachmentPreview.setColorFilter(0xFFEF4444);
        } else {
            selectedAttachmentType = "TEXT";
            ivAttachmentPreview.setImageResource(android.R.drawable.ic_menu_agenda);
            ivAttachmentPreview.setColorFilter(0xFF3B82F6);
        }

        tvAttachmentName.setText(name);
        attachmentPreviewContainer.setVisibility(View.VISIBLE);
    }

    private void clearSelectedAttachment() {
        selectedAttachmentUri = null;
        selectedAttachmentType = null;
        attachmentPreviewContainer.setVisibility(View.GONE);
        ivAttachmentPreview.setColorFilter(null);
    }

    private void setupSuggestionButtons() {
        Button btnSuggest1 = findViewById(R.id.btnSuggest1);
        Button btnSuggest2 = findViewById(R.id.btnSuggest2);
        Button btnSuggest3 = findViewById(R.id.btnSuggest3);

        btnSuggest1.setOnClickListener(v -> sendAutoPrompt(btnSuggest1.getText().toString()));
        btnSuggest2.setOnClickListener(v -> sendAutoPrompt(btnSuggest2.getText().toString()));
        btnSuggest3.setOnClickListener(v -> sendAutoPrompt(btnSuggest3.getText().toString()));
    }

    private void sendAutoPrompt(String text) {
        shouldScrollToBottom = true;
        userWaitingForResponse = true;
        isUserScrollingUp = false;
        chatManager.sendMessage(text, currentUser);
    }

    private void toggleSearchBar() {
        if (searchContainer.getVisibility() == View.VISIBLE) {
            closeSearchBar();
        } else {
            searchContainer.setVisibility(View.VISIBLE);
            etSearchQuery.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etSearchQuery, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    private void closeSearchBar() {
        searchContainer.setVisibility(View.GONE);
        etSearchQuery.setText("");
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
        filterAndDisplayMessages();
    }

    private void showClearChatDialog() {
        if (allMessages.isEmpty()) {
            Toast.makeText(this, "Conversation is already empty.", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Clear Chat history")
                .setMessage("Are you sure you want to delete all messages? This action cannot be undone.")
                .setPositiveButton("Clear", (dialog, which) -> {
                    chatManager.clearConversation();
                    Toast.makeText(this, "Chat history cleared.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void filterAndDisplayMessages() {
        String query = etSearchQuery.getText().toString().trim().toLowerCase();
        filteredMessages.clear();

        int start = Math.max(0, allMessages.size() - currentLimit);
        for (int i = start; i < allMessages.size(); i++) {
            ChatMessage msg = allMessages.get(i);
            if (query.isEmpty() || msg.getText().toLowerCase().contains(query)) {
                filteredMessages.add(msg);
            }
        }

        // Add typing indicator bubble
        Boolean isLoading = chatManager.getIsLoadingLive().getValue();
        boolean isTypingVisible = (isLoading != null && isLoading && query.isEmpty());
        if (isTypingVisible) {
            filteredMessages.add(new ChatMessage("TYPING_ID", "AI_TYPING", "AI Coach is typing...", System.currentTimeMillis()));
        }

        adapter.notifyDataSetChanged();

        // Toggle empty state
        if (filteredMessages.isEmpty()) {
            if (query.isEmpty()) {
                chatEmptyState.setVisibility(View.VISIBLE);
                rvChat.setVisibility(View.GONE);
            } else {
                chatEmptyState.setVisibility(View.GONE);
                rvChat.setVisibility(View.VISIBLE);
            }
        } else {
            chatEmptyState.setVisibility(View.GONE);
            rvChat.setVisibility(View.VISIBLE);

            ChatMessage lastMsg = filteredMessages.get(filteredMessages.size() - 1);

            if (shouldScrollToBottom) {
                shouldScrollToBottom = false;
                rvChat.post(() -> {
                    if (adapter.getItemCount() > 0) {
                        rvChat.scrollToPosition(adapter.getItemCount() - 1);
                    }
                });
            } else if (userWaitingForResponse && !isUserScrollingUp) {
                if (isTypingVisible) {
                    rvChat.post(() -> {
                        if (adapter.getItemCount() > 0) {
                            rvChat.smoothScrollToPosition(adapter.getItemCount() - 1);
                        }
                    });
                } else if ("AI".equals(lastMsg.getSender()) && !lastMsg.getId().equals(lastHandledAiMsgId)) {
                    lastHandledAiMsgId = lastMsg.getId();
                    userWaitingForResponse = false;
                    int aiMsgPosition = filteredMessages.size() - 1;

                    rvChat.post(() -> {
                        LinearLayoutManager lm = (LinearLayoutManager) rvChat.getLayoutManager();
                        if (lm != null && aiMsgPosition >= 0 && aiMsgPosition < adapter.getItemCount()) {
                            lm.scrollToPositionWithOffset(aiMsgPosition, 0);
                        }
                    });
                }
            }
        }
    }

    private void onMessageActionClick(ChatMessage message) {
        List<String> options = new ArrayList<>();
        options.add("Copy message text");
        options.add("Share message");

        if ("USER".equals(message.getSender()) && "FAILED".equals(message.getStatus())) {
            options.add("Retry sending");
        }

        options.add("Delete message");

        if (allMessages.size() > 0) {
            options.add("Regenerate last response");
        }

        String[] optionsArray = options.toArray(new String[0]);

        new AlertDialog.Builder(this)
                .setTitle("Message Actions")
                .setItems(optionsArray, (dialog, which) -> {
                    String selected = optionsArray[which];
                    if ("Copy message text".equals(selected)) {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Copied Text", message.getText());
                        if (clipboard != null) {
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(this, "Message copied to clipboard.", Toast.LENGTH_SHORT).show();
                        }
                    } else if ("Share message".equals(selected)) {
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_TEXT, message.getText());
                        startActivity(Intent.createChooser(shareIntent, "Share via"));
                    } else if ("Retry sending".equals(selected)) {
                        chatManager.retryMessage(message, currentUser);
                    } else if ("Delete message".equals(selected)) {
                        if ("TYPING_ID".equals(message.getId())) return;
                        chatManager.deleteMessage(message);
                        Toast.makeText(this, "Message deleted.", Toast.LENGTH_SHORT).show();
                    } else if ("Regenerate last response".equals(selected)) {
                        chatManager.regenerateResponse(currentUser);
                    }
                })
                .show();
    }

    private void startLoadingSimulation() {
        dashboardLayout.setVisibility(View.GONE);
        chatLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.VISIBLE);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            loadingLayout.setVisibility(View.GONE);
            validateAndLoadContent();
        }, 1500);
    }

    private void validateAndLoadContent() {
        if (currentUser != null && currentUser.getHeight() > 0 && currentUser.getWeight() > 0) {
            loadDashboardData(currentUser);
        }
        switchTab(tabLayout.getSelectedTabPosition());
    }

    private void switchTab(int position) {
        boolean hasProfileError = (currentUser == null || currentUser.getHeight() <= 0 || currentUser.getWeight() <= 0);

        if (position == 0) {
            chatLayout.setVisibility(View.GONE);
            if (hasProfileError) {
                errorLayout.setVisibility(View.VISIBLE);
                dashboardLayout.setVisibility(View.GONE);
            } else {
                errorLayout.setVisibility(View.GONE);
                dashboardLayout.setVisibility(View.VISIBLE);
            }
        } else {
            dashboardLayout.setVisibility(View.GONE);
            errorLayout.setVisibility(View.GONE);
            chatLayout.setVisibility(View.VISIBLE);
            if (shouldScrollToBottom && !filteredMessages.isEmpty()) {
                rvChat.post(() -> {
                    if (adapter.getItemCount() > 0) {
                        rvChat.scrollToPosition(adapter.getItemCount() - 1);
                    }
                });
                shouldScrollToBottom = false;
            }
        }
    }

    private void loadDashboardData(User user) {
        String goal = user.getGoal() != null && !user.getGoal().trim().isEmpty() ? user.getGoal() : "Improve Shape";
        double height = user.getHeight();
        double weight = user.getWeight();

        String name = user.getFirstName() != null && !user.getFirstName().trim().isEmpty() ? user.getFirstName() : "Athlete";
        tvDashboardMotivation.setText(String.format("“Keep pushing, %s! Focus on consistency. Every step counts toward your goal to %s.”", name, goal.toLowerCase()));

        double bmi = FitnessCalculator.calculateBmi(weight, height);
        String bmiCategory = FitnessCalculator.getBmiCategory(bmi);
        int bmiColor;
        String bmiAdvice;

        if (bmi < 18.5) {
            bmiColor = 0xFFFFB703;
            bmiAdvice = "Focus on healthy calorie surplus diet. Increase complex carbs and clean proteins.";
        } else if (bmi < 25.0) {
            bmiColor = 0xFF2563EB;
            bmiAdvice = "Healthy range! Maintain your active training and consistent meal schedule.";
        } else if (bmi < 30.0) {
            bmiColor = 0xFFEF4444;
            bmiAdvice = "Aim for regular caloric deficit. Combine HIIT workouts with cardio exercises.";
        } else {
            bmiColor = 0xFFEF4444;
            bmiAdvice = "Consult a health professional. Gentle active walking and portion control advised.";
        }

        tvDashboardBmi.setText(String.format(Locale.getDefault(), "%.1f (%s)", bmi, bmiCategory));
        tvDashboardBmi.setTextColor(bmiColor);
        tvDashboardBmiRange.setText(String.format(Locale.getDefault(), "Weight: %.1f kg | Height: %.1f cm", weight, height));
        tvDashboardBmiAdvice.setText(bmiAdvice);

        double bmr = FitnessCalculator.calculateBmr(weight, height, user.getAge(), user.getGender());
        double tdee = FitnessCalculator.calculateTdee(bmr, user.getActivityLevel());
        int calTarget = FitnessCalculator.calculateDailyCalorieTarget(tdee, goal);
        int waterTarget = user.getDailyWaterGoal() > 0 ? user.getDailyWaterGoal() : FitnessCalculator.calculateDailyWaterGoal(weight);
        tvDashboardCalTarget.setText(String.format(Locale.getDefault(), "%d kcal", calTarget));
        tvDashboardWaterTarget.setText(String.format(Locale.getDefault(), "%d ml", waterTarget));

        double targetWeight = user.getTargetWeight();
        if (targetWeight > 0) {
            double weightDiff = Math.abs(weight - targetWeight);
            if (weight > targetWeight) {
                tvDashboardWeightAdvice.setText(String.format(Locale.getDefault(), "You are %.1f kg away from your target weight of %.1f kg. Deficit recommended.", weightDiff, targetWeight));
            } else if (weight < targetWeight) {
                tvDashboardWeightAdvice.setText(String.format(Locale.getDefault(), "You are %.1f kg away from your target weight of %.1f kg. Surplus recommended.", weightDiff, targetWeight));
            } else {
                tvDashboardWeightAdvice.setText("Congratulations! You have reached your exact target weight. Maintain and build lean muscle.");
            }
        } else {
            tvDashboardWeightAdvice.setText("Set a target weight in Edit Profile to unlock detailed weight tracking insights.");
        }

        if (goal.toLowerCase().contains("gain") || goal.toLowerCase().contains("build") || goal.toLowerCase().contains("muscle")) {
            tvBeginnerWorkout.setText("15-20 mins light compound dumbbell raises and stretches");
            tvIntermediateWorkout.setText("25 mins Full Body Shred strength intervals");
            tvAdvancedWorkout.setText("40 mins Heavy progressive overload squat and bench press");
        } else if (goal.toLowerCase().contains("lose") || goal.toLowerCase().contains("fat")) {
            tvBeginnerWorkout.setText("20 mins brisk cardio walk at 5% incline");
            tvIntermediateWorkout.setText("25 mins High Intensity Cardio Core Burner");
            tvAdvancedWorkout.setText("35 mins Weighted Circuit HIIT training");
        } else {
            tvBeginnerWorkout.setText("15 mins active joint rotation and bodyweight squats");
            tvIntermediateWorkout.setText("25 mins Full Body Shred workout");
            tvAdvancedWorkout.setText("35 mins Cardio Core and Strength hybrid session");
        }

        tvDashboardRestAdvice.setText("Aim for 1-2 rest days per week. Adequate sleep (7-8 hours) is critical. Use active recovery such as gentle walking on rest days.");
        tvDashboardHealthTip.setText("Smart Tip: Eat a protein-rich snack within 45 minutes after your workout to optimize muscle synthesis and speed up recovery.");
    }

    private void sendMessage() {
        Boolean isLoading = chatManager.getIsLoadingLive().getValue();
        if (isLoading != null && isLoading) {
            return; // Ignore if already processing to prevent duplicate messages from multiple fast clicks
        }

        String text = etMessage.getText().toString().trim();
        if (text.isEmpty() && selectedAttachmentUri == null) return;

        shouldScrollToBottom = true;
        userWaitingForResponse = true;
        isUserScrollingUp = false;

        // Prevent double sends
        findViewById(R.id.btnSend).setEnabled(false);

        chatManager.sendMessage(text, selectedAttachmentUri, selectedAttachmentType, currentUser);
        
        etMessage.setText("");
        clearSelectedAttachment();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("message_draft", etMessage.getText().toString());
        outState.putString("attachment_uri", selectedAttachmentUri);
        outState.putString("attachment_type", selectedAttachmentType);
        outState.putString("attachment_name", tvAttachmentName.getText().toString());
    }

    private void recoverSavedInstance(Bundle savedInstanceState) {
        String draft = savedInstanceState.getString("message_draft", "");
        etMessage.setText(draft);
        etMessage.setSelection(draft.length());

        selectedAttachmentUri = savedInstanceState.getString("attachment_uri", null);
        selectedAttachmentType = savedInstanceState.getString("attachment_type", null);
        String attachmentName = savedInstanceState.getString("attachment_name", "");

        if (selectedAttachmentUri != null) {
            android.net.Uri uri = android.net.Uri.parse(selectedAttachmentUri);
            if ("IMAGE".equals(selectedAttachmentType)) {
                ivAttachmentPreview.setImageURI(uri);
            } else if ("PDF".equals(selectedAttachmentType)) {
                ivAttachmentPreview.setImageResource(android.R.drawable.ic_menu_save);
                ivAttachmentPreview.setColorFilter(0xFFEF4444);
            } else {
                ivAttachmentPreview.setImageResource(android.R.drawable.ic_menu_agenda);
                ivAttachmentPreview.setColorFilter(0xFF3B82F6);
            }
            tvAttachmentName.setText(attachmentName);
            attachmentPreviewContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                // If user clicks a button or interactive view, don't clear focus
                View btnSend = findViewById(R.id.btnSend);
                View btnAttach = findViewById(R.id.btnAttach);
                View btnVoice = findViewById(R.id.btnVoice);
                View attachmentContainer = findViewById(R.id.attachmentPreviewContainer);

                android.graphics.Rect sendRect = new android.graphics.Rect();
                android.graphics.Rect attachRect = new android.graphics.Rect();
                android.graphics.Rect voiceRect = new android.graphics.Rect();
                android.graphics.Rect previewRect = new android.graphics.Rect();

                if (btnSend != null) btnSend.getGlobalVisibleRect(sendRect);
                if (btnAttach != null) btnAttach.getGlobalVisibleRect(attachRect);
                if (btnVoice != null) btnVoice.getGlobalVisibleRect(voiceRect);
                if (attachmentContainer != null) attachmentContainer.getGlobalVisibleRect(previewRect);

                int x = (int) event.getRawX();
                int y = (int) event.getRawY();

                if (sendRect.contains(x, y) || attachRect.contains(x, y) || voiceRect.contains(x, y) || previewRect.contains(x, y)) {
                    // Let the click pass to the button, do not clear focus or hide keyboard
                    return super.dispatchTouchEvent(event);
                }

                android.graphics.Rect outRect = new android.graphics.Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains(x, y)) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    public interface OnMessageActionListener {
        void onMessageLongClick(ChatMessage message);
    }

    private static class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
        private final List<ChatMessage> list;
        private final OnMessageActionListener listener;
        private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        public ChatAdapter(List<ChatMessage> list, OnMessageActionListener listener) {
            this.list = list;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
            return new ChatViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
            ChatMessage msg = list.get(position);
            String formattedTime = timeFormat.format(new Date(msg.getTimestamp()));

            if ("USER".equals(msg.getSender())) {
                holder.layoutUser.setVisibility(View.VISIBLE);
                holder.layoutAI.setVisibility(View.GONE);
                holder.tvUserMsg.setText(msg.getText());
                holder.tvUserTime.setText(formattedTime);

                // Bind WhatsApp status indicators
                if ("FAILED".equals(msg.getStatus())) {
                    holder.ivMessageStatus.setImageResource(android.R.drawable.stat_notify_error);
                    holder.ivMessageStatus.setColorFilter(0xFFEF4444);
                } else {
                    holder.ivMessageStatus.setColorFilter(null);
                    if (msg.getStatus() == null || "SENT".equals(msg.getStatus())) {
                        holder.ivMessageStatus.setImageResource(R.drawable.ic_single_tick);
                    } else if ("DELIVERED".equals(msg.getStatus())) {
                        holder.ivMessageStatus.setImageResource(R.drawable.ic_double_tick);
                    } else if ("READ".equals(msg.getStatus())) {
                        holder.ivMessageStatus.setImageResource(R.drawable.ic_double_tick_blue);
                    }
                }

                // Bind User Attachment display inside bubble
                if (msg.getAttachmentUri() != null && !msg.getAttachmentUri().trim().isEmpty()) {
                    holder.layoutUserAttachment.setVisibility(View.VISIBLE);
                    String name = getFileNameFromUri(msg.getAttachmentUri());
                    holder.tvUserAttachmentName.setText(name);

                    if ("IMAGE".equalsIgnoreCase(msg.getAttachmentType())) {
                        Glide.with(holder.itemView.getContext())
                                .load(msg.getAttachmentUri())
                                .placeholder(R.drawable.ic_attach)
                                .error(R.drawable.ic_attach)
                                .centerCrop()
                                .into(holder.ivUserAttachmentIcon);
                    } else if ("PDF".equalsIgnoreCase(msg.getAttachmentType())) {
                        holder.ivUserAttachmentIcon.setImageResource(android.R.drawable.ic_menu_save);
                        holder.ivUserAttachmentIcon.setColorFilter(0xFFEF4444);
                    } else {
                        holder.ivUserAttachmentIcon.setImageResource(android.R.drawable.ic_menu_agenda);
                        holder.ivUserAttachmentIcon.setColorFilter(0xFF3B82F6);
                    }
                } else {
                    holder.layoutUserAttachment.setVisibility(View.GONE);
                }
            } else if ("AI_TYPING".equals(msg.getSender())) {
                holder.layoutUser.setVisibility(View.GONE);
                holder.layoutAI.setVisibility(View.VISIBLE);
                holder.tvAIMsg.setText("AI Coach is compiling recommendations...");
                holder.tvAITime.setText("Typing");
                holder.layoutUserAttachment.setVisibility(View.GONE);
            } else {
                holder.layoutUser.setVisibility(View.GONE);
                holder.layoutAI.setVisibility(View.VISIBLE);
                holder.tvAIMsg.setText(msg.getText());
                holder.tvAITime.setText(formattedTime);
                holder.layoutUserAttachment.setVisibility(View.GONE);
            }

            holder.itemView.setOnLongClickListener(v -> {
                listener.onMessageLongClick(msg);
                return true;
            });
        }

        private static String getFileNameFromUri(String uriString) {
            if (uriString == null) return "file";
            int lastSlash = uriString.lastIndexOf('/');
            if (lastSlash != -1 && lastSlash < uriString.length() - 1) {
                return uriString.substring(lastSlash + 1);
            }
            return "attachment";
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ChatViewHolder extends RecyclerView.ViewHolder {
            View layoutUser, layoutAI;
            TextView tvUserMsg, tvAIMsg, tvUserTime, tvAITime;

            // Attachment view components
            View layoutUserAttachment;
            ImageView ivUserAttachmentIcon;
            TextView tvUserAttachmentName;

            // WhatsApp status indicator
            ImageView ivMessageStatus;

            public ChatViewHolder(@NonNull View itemView) {
                super(itemView);
                layoutUser = itemView.findViewById(R.id.layoutUser);
                layoutAI = itemView.findViewById(R.id.layoutAI);
                tvUserMsg = itemView.findViewById(R.id.tvUserMsg);
                tvAIMsg = itemView.findViewById(R.id.tvAIMsg);
                tvUserTime = itemView.findViewById(R.id.tvUserTime);
                tvAITime = itemView.findViewById(R.id.tvAITime);

                layoutUserAttachment = itemView.findViewById(R.id.layoutUserAttachment);
                ivUserAttachmentIcon = itemView.findViewById(R.id.ivUserAttachmentIcon);
                tvUserAttachmentName = itemView.findViewById(R.id.tvUserAttachmentName);

                ivMessageStatus = itemView.findViewById(R.id.ivMessageStatus);
            }
        }
    }
}

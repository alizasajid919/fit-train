package com.fitness.app.activities;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.fitness.app.R;
import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.models.DietPlan;
import com.fitness.app.models.User;
import com.fitness.app.models.WorkoutPlan;
import com.fitness.app.utils.FitnessCalculator;
import com.fitness.app.utils.RecommendationEngine;
import com.google.android.material.button.MaterialButton;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class OnboardingActivity extends AppCompatActivity {

    private LocalDataManager localDb;

    // Layout Containers
    private ImageButton btnBackStep;
    private TextView tvStepIndicator, tvStepError;
    private ProgressBar pbOnboardingProgress;
    private ViewGroup stepContainer;
    private View bottomBar;
    private MaterialButton btnNextStep;

    // Total Flow Steps:
    // 0..2: Intro Slides (Trophy, Comparison, Satiety)
    // 3: NEW ONBOARDING INTRO SCREEN ("Let's start with some questions..." - AFTER ALL INTROS, BEFORE NAME)
    // 4..21: Question & Input Screens (18 questions)
    // 22: Profile Analysis & Calculation
    // 23: Welcome & Dashboard Launch
    private int currentStep = 0;
    private static final int TOTAL_QUESTION_STEPS = 18; // Steps 4 to 21

    // User Data State (CRITICAL: ALL SELECTIONS START 100% UNSELECTED & EMPTY FOR NEW USERS)
    private String name = "";
    private String dobString = ""; // yyyy-MM-dd
    private int birthYear = 0;
    private int birthMonth = 0;
    private int birthDay = 0;
    private int age = 0;

    private String gender = ""; // UNSELECTED
    private boolean isMetric = true;
    private String heightInputStr = "";
    private double heightCm = 0.0; // EMPTY
    private String weightInputStr = "";
    private double weightKg = 0.0; // EMPTY
    private String bloodGroup = ""; // UNSELECTED
    private String goal = ""; // UNSELECTED
    private String targetWeightInputStr = "";
    private double targetWeightKg = 0.0; // EMPTY
    private String targetPace = ""; // UNSELECTED

    private String eatingEnvironment = ""; // UNSELECTED
    private String eatingOutFrequency = ""; // UNSELECTED
    private String activityLevel = ""; // UNSELECTED
    private String fitnessExperience = ""; // UNSELECTED
    private String workoutLocation = ""; // UNSELECTED
    private final Set<String> selectedEquipment = new HashSet<>();
    private int workoutDuration = 0; // UNSELECTED
    private int workoutDaysPerWeek = 0; // UNSELECTED
    private String preferredWorkoutTime = ""; // UNSELECTED

    private String dietaryPreference = ""; // UNSELECTED
    private final Set<String> selectedAllergies = new HashSet<>();
    private final Set<String> selectedHealthConcerns = new HashSet<>();
    private String otherHealthConcernText = ""; // Custom "Others" text
    private int mealsPerDay = 0; // UNSELECTED
    private String medicalConditions = ""; // UNSELECTED
    private String injuries = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        localDb = new LocalDataManager(this);

        // Bind Views
        btnBackStep = findViewById(R.id.btnBackStep);
        tvStepIndicator = findViewById(R.id.tvStepIndicator);
        tvStepError = findViewById(R.id.tvStepError);
        pbOnboardingProgress = findViewById(R.id.pbOnboardingProgress);
        stepContainer = findViewById(R.id.stepContainer);
        bottomBar = findViewById(R.id.bottomBar);
        btnNextStep = findViewById(R.id.btnNextStep);

        btnBackStep.setOnClickListener(v -> navigatePrevious());
        btnNextStep.setOnClickListener(v -> processNextStep());

        renderStep(currentStep);
    }

    private void renderStep(int step) {
        hideError();
        stepContainer.removeAllViews();

        if (step <= 2) {
            // Intro Slides (Trophy, Comparison, Satiety)
            btnBackStep.setVisibility(step > 0 ? View.VISIBLE : View.GONE);
            tvStepIndicator.setText(String.format(Locale.getDefault(), "FitTrain • Intro %d of 3", step + 1));
            pbOnboardingProgress.setVisibility(View.GONE);
            btnNextStep.setText("Continue");
            bottomBar.setVisibility(View.VISIBLE);
        } else if (step == 3) {
            // NEW ONBOARDING INTRO SCREEN: "Let's start with some questions..." (AFTER ALL INTROS, BEFORE NAME)
            btnBackStep.setVisibility(View.VISIBLE);
            tvStepIndicator.setText("Personalized Setup");
            pbOnboardingProgress.setVisibility(View.GONE);
            btnNextStep.setText("Let's Start 🚀");
            bottomBar.setVisibility(View.VISIBLE);
        } else if (step >= 4 && step <= 21) {
            // Question Screens
            int qNum = step - 3;
            btnBackStep.setVisibility(View.VISIBLE);
            tvStepIndicator.setText(String.format(Locale.getDefault(), "Question %d of %d", qNum, TOTAL_QUESTION_STEPS));
            pbOnboardingProgress.setVisibility(View.VISIBLE);
            pbOnboardingProgress.setProgress((qNum * 100) / TOTAL_QUESTION_STEPS);
            btnNextStep.setText("Continue");
            bottomBar.setVisibility(View.VISIBLE);
        } else if (step == 22) {
            // Calculation
            btnBackStep.setVisibility(View.GONE);
            tvStepIndicator.setText("Analyzing Profile");
            pbOnboardingProgress.setVisibility(View.VISIBLE);
            pbOnboardingProgress.setProgress(100);
            bottomBar.setVisibility(View.GONE);
        } else if (step == 23) {
            // Summary Welcome
            btnBackStep.setVisibility(View.GONE);
            tvStepIndicator.setText("Your Plan Is Ready!");
            pbOnboardingProgress.setVisibility(View.GONE);
            bottomBar.setVisibility(View.VISIBLE);
            btnNextStep.setText("START MY FITTRAIN PLAN 🎉");
        }

        switch (step) {
            // Intro Sequence (Steps 0-2)
            case 0: buildIntroTrophySlide(); break;
            case 1: buildIntroComparisonSlide(); break;
            case 2: buildIntroSatietySlide(); break;

            // NEW INTRO START SCREEN (Step 3 - AFTER ALL INTROS, BEFORE NAME)
            case 3: buildNewIntroStartSlide(); break;

            // Questions (Steps 4-21 - STRICTLY UNSELECTED FOR NEW USERS)
            case 4: buildNameStep(); break;
            case 5: buildDateOfBirthStep(); break;
            case 6: buildGenderStep(); break;
            case 7: buildHeightStep(); break;
            case 8: buildWeightStep(); break;
            case 9: buildBloodGroupStep(); break; // BLOOD GROUP SCREEN
            case 10: buildGoalStep(); break;
            case 11: buildTargetWeightStep(); break;
            case 12: buildMilestoneProjectionSlide(); break;
            case 13: buildDietSectionIntroSlide(); break;
            case 14: buildEatingEnvironmentsStep(); break;
            case 15: buildMealsPerDayStep(); break;
            case 16: buildNutritionReportSlide(); break;
            case 17: buildDietStyleStep(); break;
            case 18: buildHealthConcernsStep(); break; // HEALTH CONCERNS WITH OTHERS
            case 19: buildActivityStep(); break;
            case 20: buildExperienceStep(); break;
            case 21: buildWorkoutPreferencesStep(); break;

            // Plan Generation & Launch (Steps 22-23)
            case 22: buildPlanGenerationStep(); break;
            case 23: buildPersonalizedWelcomeStep(); break;
        }

        // Animated Screen Entrance (Fade + Subtle Slide Up)
        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(240);
        TranslateAnimation slideUp = new TranslateAnimation(0, 0, 30, 0);
        slideUp.setDuration(240);

        stepContainer.startAnimation(fadeIn);
        stepContainer.startAnimation(slideUp);
    }

    private void navigatePrevious() {
        if (currentStep > 0 && currentStep <= 21) {
            currentStep--;
            renderStep(currentStep);
        }
    }

    private void processNextStep() {
        if (validateStep(currentStep)) {
            if (currentStep < 21) {
                currentStep++;
                renderStep(currentStep);
            } else if (currentStep == 21) {
                currentStep = 22;
                renderStep(22);
            } else if (currentStep == 23) {
                finishOnboardingAndLaunchHome();
            }
        }
    }

    private boolean validateStep(int step) {
        hideError();
        switch (step) {
            case 4: // Name
                if (name.trim().isEmpty()) {
                    showError("Please enter your name to continue.");
                    return false;
                }
                break;
            case 5: // DOB
                if (dobString.isEmpty() || age <= 0) {
                    showError("Please select your date of birth.");
                    return false;
                }
                break;
            case 6: // Gender
                if (gender.isEmpty()) {
                    showError("Please select your gender.");
                    return false;
                }
                break;
            case 7: // Height
                if (heightCm <= 0) {
                    showError("Please enter your height.");
                    return false;
                }
                break;
            case 8: // Weight
                if (weightKg <= 0) {
                    showError("Please enter your current weight.");
                    return false;
                }
                break;
            case 9: // Blood Group
                if (bloodGroup.isEmpty()) {
                    showError("Please select your blood group.");
                    return false;
                }
                break;
            case 10: // Goal
                if (goal.isEmpty()) {
                    showError("Please select your main goal.");
                    return false;
                }
                break;
            case 11: // Target Weight
                if (targetWeightKg <= 0) {
                    showError("Please enter your target weight.");
                    return false;
                }
                break;
            case 14: // Environment
                if (eatingEnvironment.isEmpty()) {
                    showError("Please select an eating environment.");
                    return false;
                }
                break;
            case 15: // Meals
                if (mealsPerDay <= 0) {
                    showError("Please select your daily meal frequency.");
                    return false;
                }
                break;
            case 17: // Diet Style
                if (dietaryPreference.isEmpty()) {
                    showError("Please select your dietary style.");
                    return false;
                }
                break;
            case 18: // Health Concerns
                if (selectedHealthConcerns.isEmpty()) {
                    showError("Please select any health concerns or tap 'None'.");
                    return false;
                }
                if (selectedHealthConcerns.contains("Others") && otherHealthConcernText.trim().isEmpty()) {
                    showError("Please specify your health concern in the text box below.");
                    return false;
                }
                break;
            case 19: // Activity
                if (activityLevel.isEmpty()) {
                    showError("Please select your activity level.");
                    return false;
                }
                break;
            case 20: // Experience
                if (fitnessExperience.isEmpty()) {
                    showError("Please select your fitness level.");
                    return false;
                }
                break;
            case 21: // Location
                if (workoutLocation.isEmpty()) {
                    showError("Please select your workout location.");
                    return false;
                }
                break;
        }
        return true;
    }

    private void showError(String msg) {
        tvStepError.setText(msg);
        tvStepError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        tvStepError.setVisibility(View.GONE);
    }

    // ====================================================================
    // EDUCATIONAL TOOLTIP CARD: "BEHIND THE QUESTION"
    // ====================================================================

    private void addBehindTheQuestionCard(LinearLayout container, String summaryText, String detailText) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_option_card_unselected);
        card.setPadding(20, 16, 20, 16);

        LinearLayout headerRow = new LinearLayout(this);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);

        ImageView ivIcon = new ImageView(this);
        ivIcon.setImageResource(R.drawable.ic_behind_question_owl);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(36, 36);
        iconParams.setMargins(0, 0, 14, 0);
        ivIcon.setLayoutParams(iconParams);

        LinearLayout textCol = new LinearLayout(this);
        textCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams colParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        textCol.setLayoutParams(colParams);

        TextView tvHeader = new TextView(this);
        tvHeader.setText("Behind the question");
        tvHeader.setTextSize(14);
        tvHeader.setTypeface(null, Typeface.BOLD);
        tvHeader.setTextColor(0xFF0F172A);

        TextView tvSummary = new TextView(this);
        tvSummary.setText(summaryText);
        tvSummary.setTextSize(12);
        tvSummary.setTextColor(0xFF64748B);

        textCol.addView(tvHeader);
        textCol.addView(tvSummary);

        TextView btnMore = new TextView(this);
        btnMore.setText("More");
        btnMore.setTextSize(13);
        btnMore.setTypeface(null, Typeface.BOLD);
        btnMore.setTextColor(0xFFF97316);
        btnMore.setPadding(12, 6, 12, 6);

        headerRow.addView(ivIcon);
        headerRow.addView(textCol);
        headerRow.addView(btnMore);
        card.addView(headerRow);

        TextView tvDetail = new TextView(this);
        tvDetail.setText(detailText);
        tvDetail.setTextSize(12);
        tvDetail.setTextColor(0xFF475569);
        tvDetail.setPadding(0, 12, 0, 4);
        tvDetail.setVisibility(View.GONE);
        card.addView(tvDetail);

        btnMore.setOnClickListener(v -> {
            if (tvDetail.getVisibility() == View.GONE) {
                tvDetail.setVisibility(View.VISIBLE);
                btnMore.setText("Less");
            } else {
                tvDetail.setVisibility(View.GONE);
                btnMore.setText("More");
            }
        });

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 20);
        card.setLayoutParams(params);
        container.addView(card);
    }

    // ====================================================================
    // INTRO SLIDES (0 to 3)
    // ====================================================================

    private void buildIntroTrophySlide() {
        LinearLayout layout = createVerticalContainer();
        layout.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView tvTitle = createHeaderTitle("Goal Weight Will Be Reached In FitTrain");
        tvTitle.setGravity(Gravity.CENTER);
        tvTitle.setPadding(0, 16, 0, 8);

        TextView tvSub = createSubTitle("Let's start with a few quick questions to generate your personalized fitness & nutrition plan!");
        tvSub.setGravity(Gravity.CENTER);

        layout.addView(tvTitle);
        layout.addView(tvSub);

        addLargeAnimatedIllustration(layout, R.drawable.ic_intro_trophy, 200, 200);

        layout.addView(createFeatureBadge("🎯 Goal-Driven Calorie Deficit Engine"));
        layout.addView(createFeatureBadge("🏋️ Personalized Workouts for Home or Gym"));
        layout.addView(createFeatureBadge("🥗 Science-Backed Meal Schedules & Nutrition"));
        layout.addView(createFeatureBadge("🤖 24/7 AI Personal Fitness Coach Assistant"));

        stepContainer.addView(layout);
    }

    private void buildIntroComparisonSlide() {
        LinearLayout layout = createVerticalContainer();

        TextView tvTitle = createHeaderTitle("FitTrain simplifies weight loss and is built for real life");
        TextView tvSub = createSubTitle("Forget rigid meal prep! FitTrain adapts to your daily lifestyle effortlessly.");
        layout.addView(tvTitle);
        layout.addView(tvSub);

        addLargeAnimatedIllustration(layout, R.drawable.ic_ai_meal_scan, 200, 200);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout cardLeft = new LinearLayout(this);
        cardLeft.setOrientation(LinearLayout.VERTICAL);
        cardLeft.setBackgroundResource(R.drawable.bg_option_card_unselected);
        cardLeft.setPadding(20, 20, 20, 20);

        TextView tvLeftTitle = new TextView(this);
        tvLeftTitle.setText("Traditional\nApproach");
        tvLeftTitle.setTextSize(16);
        tvLeftTitle.setTypeface(null, Typeface.BOLD);
        tvLeftTitle.setTextColor(0xFF0F172A);
        tvLeftTitle.setPadding(0, 0, 0, 12);
        cardLeft.addView(tvLeftTitle);

        cardLeft.addView(createBulletItem("✖ Manually weigh every single ingredient"));
        cardLeft.addView(createBulletItem("✖ Spend hours preparing meals"));
        cardLeft.addView(createBulletItem("✖ Easily lose motivation"));

        LinearLayout cardRight = new LinearLayout(this);
        cardRight.setOrientation(LinearLayout.VERTICAL);
        cardRight.setBackgroundResource(R.drawable.bg_option_card_selected);
        cardRight.setPadding(20, 20, 20, 20);

        TextView tvRightTitle = new TextView(this);
        tvRightTitle.setText("With FitTrain\nTracker");
        tvRightTitle.setTextSize(16);
        tvRightTitle.setTypeface(null, Typeface.BOLD);
        tvRightTitle.setTextColor(0xFF6366F1);
        tvRightTitle.setPadding(0, 0, 0, 12);
        cardRight.addView(tvRightTitle);

        cardRight.addView(createCheckItem("✔ Enjoy meals anywhere, anytime"));
        cardRight.addView(createCheckItem("✔ Simply scan before eating"));
        cardRight.addView(createCheckItem("✔ Achieve results effortlessly"));

        LinearLayout.LayoutParams paramHalf = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        paramHalf.setMargins(6, 0, 6, 0);
        cardLeft.setLayoutParams(paramHalf);
        cardRight.setLayoutParams(paramHalf);

        row.addView(cardLeft);
        row.addView(cardRight);
        layout.addView(row);

        stepContainer.addView(layout);
    }

    private void buildIntroSatietySlide() {
        LinearLayout layout = createVerticalContainer();

        TextView tvTitle = createHeaderTitle("Learn to make better food choices — Same Calories Better Results");
        TextView tvSub = createSubTitle("High-satiety nutrient meals keep you full longer on the exact same caloric budget.");
        layout.addView(tvTitle);
        layout.addView(tvSub);

        addLargeAnimatedIllustration(layout, R.drawable.ic_intro_satiety, 200, 200);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout cardDrink = new LinearLayout(this);
        cardDrink.setOrientation(LinearLayout.VERTICAL);
        cardDrink.setBackgroundResource(R.drawable.bg_option_card_unselected);
        cardDrink.setPadding(16, 16, 16, 16);

        TextView tvCalDrink = new TextView(this);
        tvCalDrink.setText("590 cal");
        tvCalDrink.setTextSize(14);
        tvCalDrink.setTypeface(null, Typeface.BOLD);
        tvCalDrink.setTextColor(0xFFEA580C);

        TextView tvDrinkTitle = new TextView(this);
        tvDrinkTitle.setText("That one drink 🥤");
        tvDrinkTitle.setTextSize(15);
        tvDrinkTitle.setTypeface(null, Typeface.BOLD);
        tvDrinkTitle.setTextColor(0xFF0F172A);
        tvDrinkTitle.setPadding(0, 8, 0, 8);

        cardDrink.addView(tvCalDrink);
        cardDrink.addView(tvDrinkTitle);
        cardDrink.addView(createBulletItem("✖ Quick hunger"));
        cardDrink.addView(createBulletItem("✖ Low nutrients"));
        cardDrink.addView(createBulletItem("✖ Crashes energy"));

        LinearLayout cardMeal = new LinearLayout(this);
        cardMeal.setOrientation(LinearLayout.VERTICAL);
        cardMeal.setBackgroundResource(R.drawable.bg_option_card_selected);
        cardMeal.setPadding(16, 16, 16, 16);

        TextView tvCalMeal = new TextView(this);
        tvCalMeal.setText("590 cal");
        tvCalMeal.setTextSize(14);
        tvCalMeal.setTypeface(null, Typeface.BOLD);
        tvCalMeal.setTextColor(0xFF10B981);

        TextView tvMealTitle = new TextView(this);
        tvMealTitle.setText("An entire meal 🥗");
        tvMealTitle.setTextSize(15);
        tvMealTitle.setTypeface(null, Typeface.BOLD);
        tvMealTitle.setTextColor(0xFF6366F1);
        tvMealTitle.setPadding(0, 8, 0, 8);

        cardMeal.addView(tvCalMeal);
        cardMeal.addView(tvMealTitle);
        cardMeal.addView(createCheckItem("✔ High satiety"));
        cardMeal.addView(createCheckItem("✔ Rich in nutrients"));
        cardMeal.addView(createCheckItem("✔ Boosts energy"));

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        p.setMargins(6, 0, 6, 0);
        cardDrink.setLayoutParams(p);
        cardMeal.setLayoutParams(p);

        row.addView(cardDrink);
        row.addView(cardMeal);
        layout.addView(row);

        stepContainer.addView(layout);
    }

    // NEW ONBOARDING INTRO SCREEN (Step 3: AFTER ALL INTROS, BEFORE NAME)
    private void buildNewIntroStartSlide() {
        LinearLayout layout = createVerticalContainer();
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setPadding(20, 20, 20, 20);

        // Large Start AI Illustration (180x180 dp with smooth Entrance Animation)
        ImageView ivStart = new ImageView(this);
        ivStart.setImageResource(R.drawable.ic_onboarding_start);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(180, 180);
        imgParams.setMargins(0, 12, 0, 20);
        ivStart.setLayoutParams(imgParams);

        // Animated Image Entrance (Slide Up + Scale Pulse)
        TranslateAnimation animStartImg = new TranslateAnimation(0, 0, 60, 0);
        animStartImg.setDuration(400);
        ScaleAnimation animStartScale = new ScaleAnimation(0.85f, 1.0f, 0.85f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animStartScale.setDuration(400);
        ivStart.startAnimation(animStartImg);
        ivStart.startAnimation(animStartScale);

        layout.addView(ivStart);

        TextView tvTitle = createHeaderTitle("Let's start with some questions to get your custom plan");
        tvTitle.setGravity(Gravity.CENTER);

        TextView tvSub = createSubTitle("Answer a few simple questions so FitTrain can personalize your daily workout, diet, and calorie targets.");
        tvSub.setGravity(Gravity.CENTER);

        layout.addView(tvTitle);
        layout.addView(tvSub);

        // Login Row: "Already have an account? Log In"
        LinearLayout loginRow = new LinearLayout(this);
        loginRow.setOrientation(LinearLayout.HORIZONTAL);
        loginRow.setGravity(Gravity.CENTER);
        loginRow.setPadding(0, 20, 0, 0);

        TextView tvAlready = new TextView(this);
        tvAlready.setText("Already have an account? ");
        tvAlready.setTextSize(14);
        tvAlready.setTextColor(0xFF64748B);

        TextView tvLoginBtn = new TextView(this);
        tvLoginBtn.setText("Log In");
        tvLoginBtn.setTextSize(14);
        tvLoginBtn.setTypeface(null, Typeface.BOLD);
        tvLoginBtn.setTextColor(0xFF2563EB);
        tvLoginBtn.setPadding(6, 6, 6, 6);

        tvLoginBtn.setOnClickListener(v -> {
            Intent intent = new Intent(OnboardingActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        loginRow.addView(tvAlready);
        loginRow.addView(tvLoginBtn);
        layout.addView(loginRow);

        stepContainer.addView(layout);
    }

    // ====================================================================
    // QUESTION STEPS (4 to 21 - UNSELECTED INITIAL STATES & VISUALS)
    // ====================================================================

    private void buildNameStep() {
        LinearLayout layout = createVerticalContainer();
        layout.addView(createHeaderTitle("👤 What's your name?"));
        layout.addView(createSubTitle("We'll use this to personalize your FitTrain experience."));

        // STRICT: Empty input field
        EditText etName = createEditText("Enter your name");
        etName.setText(name);
        etName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { name = s.toString(); }
            @Override public void afterTextChanged(Editable s) {}
        });
        layout.addView(etName);

        addLargeAnimatedIllustration(layout, R.drawable.ic_welcome_person, 240, 240);
        stepContainer.addView(layout);
    }

    private void buildDateOfBirthStep() {
        LinearLayout layout = createVerticalContainer();
        addBehindTheQuestionCard(layout, "1. Knowing your birth date helps us calculate BMR accurately...", "Basal Metabolic Rate (BMR) declines with age. Selecting your birth date allows FitTrain to accurately calculate your metabolic rate and caloric needs.");

        layout.addView(createHeaderTitle("📅 What's your date of birth?"));
        layout.addView(createSubTitle("Select your birth date to calculate your age accurately."));

        MaterialButton btnPicker = new MaterialButton(this);
        btnPicker.setText(dobString.isEmpty() ? "📅 Select your date of birth" : "📅 Date of Birth: " + dobString + " (" + age + " years)");
        btnPicker.setBackgroundTintList(android.content.res.ColorStateList.valueOf(dobString.isEmpty() ? 0xFFE2E8F0 : 0xFF2563EB));
        btnPicker.setTextColor(dobString.isEmpty() ? 0xFF64748B : 0xFFFFFFFF);
        btnPicker.setPadding(24, 20, 24, 20);

        btnPicker.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            int y = birthYear > 0 ? birthYear : 2001;
            int m = birthMonth > 0 ? birthMonth - 1 : 7;
            int d = birthDay > 0 ? birthDay : 15;

            DatePickerDialog dpd = new DatePickerDialog(OnboardingActivity.this, (view, year, monthOfYear, dayOfMonth) -> {
                birthYear = year;
                birthMonth = monthOfYear + 1;
                birthDay = dayOfMonth;

                int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                age = currentYear - birthYear;
                dobString = String.format(Locale.getDefault(), "%04d-%02d-%02d", birthYear, birthMonth, birthDay);

                btnPicker.setText("📅 Date of Birth: " + dobString + " (" + age + " years)");
                btnPicker.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF2563EB));
                btnPicker.setTextColor(0xFFFFFFFF);
            }, y, m, d);
            dpd.show();
        });
        layout.addView(btnPicker);

        addLargeAnimatedIllustration(layout, R.drawable.ic_calendar_age, 240, 240);

        stepContainer.addView(layout);
    }

    private void buildGenderStep() {
        LinearLayout layout = createVerticalContainer();
        addBehindTheQuestionCard(layout, "1. Tailored Fitness Plans: Gender...", "Biological gender affects muscle distribution and basal metabolic formulas (Mifflin-St Jeor formula).");

        layout.addView(createHeaderTitle("What is your gender?"));

        String[] genders = {"Male", "Female", "Non-binary"};
        String[] icons = {"👦 Male", "👧 Female", "🧑 Non-binary"};

        for (int i = 0; i < genders.length; i++) {
            final String g = genders[i];
            View card = createOptionCard(icons[i], g.equalsIgnoreCase(gender), 0xFF6366F1);
            card.setOnClickListener(v -> {
                gender = g;
                animateSelection(card);
                renderStep(6);
            });
            layout.addView(card);
        }
        addLargeAnimatedIllustration(layout, R.drawable.ic_gender_avatars, 180, 180);
        stepContainer.addView(layout);
    }

    private void buildHeightStep() {
        LinearLayout layout = createVerticalContainer();
        addBehindTheQuestionCard(layout, "1. Body Surface Metric...", "Height is critical to compute Body Mass Index (BMI) and total energy expenditure.");

        layout.addView(createHeaderTitle("Your Height"));

        LinearLayout unitSwitcher = new LinearLayout(this);
        unitSwitcher.setOrientation(LinearLayout.HORIZONTAL);
        unitSwitcher.setPadding(0, 0, 0, 16);

        MaterialButton btnCm = new MaterialButton(this);
        btnCm.setText("cm");
        btnCm.setBackgroundTintList(android.content.res.ColorStateList.valueOf(isMetric ? 0xFFF97316 : 0xFFE2E8F0));
        btnCm.setTextColor(isMetric ? 0xFFFFFFFF : 0xFF64748B);

        MaterialButton btnFt = new MaterialButton(this);
        btnFt.setText("ft");
        btnFt.setBackgroundTintList(android.content.res.ColorStateList.valueOf(!isMetric ? 0xFFF97316 : 0xFFE2E8F0));
        btnFt.setTextColor(!isMetric ? 0xFFFFFFFF : 0xFF64748B);

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        p.setMargins(4, 0, 4, 0);
        btnCm.setLayoutParams(p);
        btnFt.setLayoutParams(p);

        btnCm.setOnClickListener(v -> { isMetric = true; renderStep(7); });
        btnFt.setOnClickListener(v -> { isMetric = false; renderStep(7); });

        unitSwitcher.addView(btnFt);
        unitSwitcher.addView(btnCm);
        layout.addView(unitSwitcher);

        TextView tvVal = new TextView(this);
        if (heightCm == 0) heightCm = 165.0;
        tvVal.setText(isMetric ? String.format(Locale.getDefault(), "%.1f cm", heightCm) : String.format(Locale.getDefault(), "%d ft %d in", (int)(heightCm/30.48), (int)((heightCm%30.48)/2.54)));
        tvVal.setTextSize(28);
        tvVal.setTypeface(null, Typeface.BOLD);
        tvVal.setTextColor(0xFF0F172A);
        tvVal.setGravity(Gravity.CENTER);
        tvVal.setPadding(0, 16, 0, 16);
        layout.addView(tvVal);

        SeekBar sbHeight = new SeekBar(this);
        sbHeight.setMax(130);
        sbHeight.setProgress((int) (heightCm - 120));
        sbHeight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                heightCm = 120 + progress;
                tvVal.setText(isMetric ? String.format(Locale.getDefault(), "%.1f cm", heightCm) : String.format(Locale.getDefault(), "%d ft %d in", (int)(heightCm/30.48), (int)((heightCm%30.48)/2.54)));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        layout.addView(sbHeight);

        addLargeAnimatedIllustration(layout, R.drawable.ic_height_ruler_full, 260, 260);

        stepContainer.addView(layout);
    }

    private void buildWeightStep() {
        LinearLayout layout = createVerticalContainer();
        addBehindTheQuestionCard(layout, "1. Weight & BMR Index...", "Current weight establishes baseline caloric maintenance and hydration goals.");

        layout.addView(createHeaderTitle("What's your current weight?"));

        LinearLayout unitSwitcher = new LinearLayout(this);
        unitSwitcher.setOrientation(LinearLayout.HORIZONTAL);

        MaterialButton btnKg = new MaterialButton(this);
        btnKg.setText("kg");
        btnKg.setBackgroundTintList(android.content.res.ColorStateList.valueOf(isMetric ? 0xFFF97316 : 0xFFE2E8F0));
        btnKg.setTextColor(isMetric ? 0xFFFFFFFF : 0xFF64748B);

        MaterialButton btnLbs = new MaterialButton(this);
        btnLbs.setText("lbs");
        btnLbs.setBackgroundTintList(android.content.res.ColorStateList.valueOf(!isMetric ? 0xFFF97316 : 0xFFE2E8F0));
        btnLbs.setTextColor(!isMetric ? 0xFFFFFFFF : 0xFF64748B);

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        p.setMargins(4, 0, 4, 0);
        btnKg.setLayoutParams(p);
        btnLbs.setLayoutParams(p);

        btnKg.setOnClickListener(v -> { isMetric = true; renderStep(8); });
        btnLbs.setOnClickListener(v -> { isMetric = false; renderStep(8); });

        unitSwitcher.addView(btnLbs);
        unitSwitcher.addView(btnKg);
        layout.addView(unitSwitcher);

        TextView tvWeightVal = new TextView(this);
        if (weightKg == 0) weightKg = 70.0;
        tvWeightVal.setText(isMetric ? String.format(Locale.getDefault(), "%.1f kg", weightKg) : String.format(Locale.getDefault(), "%.1f lbs", weightKg * 2.20462));
        tvWeightVal.setTextSize(34);
        tvWeightVal.setTypeface(null, Typeface.BOLD);
        tvWeightVal.setTextColor(0xFF0F172A);
        tvWeightVal.setGravity(Gravity.CENTER);
        tvWeightVal.setPadding(0, 16, 0, 8);
        layout.addView(tvWeightVal);

        SeekBar sbWeight = new SeekBar(this);
        sbWeight.setMax(170);
        sbWeight.setProgress((int) (weightKg - 30));

        LinearLayout bmiBox = new LinearLayout(this);
        bmiBox.setOrientation(LinearLayout.VERTICAL);
        bmiBox.setBackgroundResource(R.drawable.bg_option_card_unselected);
        bmiBox.setPadding(20, 16, 20, 16);

        TextView tvBmiTitle = new TextView(this);
        double heightM = (heightCm > 0 ? heightCm : 165.0) / 100.0;
        double bmi = weightKg / (heightM * heightM);

        String category;
        int categoryColor;
        if (bmi < 18.5) { category = "Underweight"; categoryColor = 0xFF3B82F6; }
        else if (bmi < 25.0) { category = "Normal"; categoryColor = 0xFF10B981; }
        else if (bmi < 30.0) { category = "Overweight"; categoryColor = 0xFFF97316; }
        else { category = "Obesity"; categoryColor = 0xFFEF4444; }

        tvBmiTitle.setText(String.format(Locale.getDefault(), "Your BMI: %.1f  [%s]", bmi, category));
        tvBmiTitle.setTextSize(15);
        tvBmiTitle.setTypeface(null, Typeface.BOLD);
        tvBmiTitle.setTextColor(categoryColor);

        TextView tvBmiDesc = new TextView(this);
        tvBmiDesc.setText("We will use your index to tailor a personal deficit plan for you.");
        tvBmiDesc.setTextSize(12);
        tvBmiDesc.setTextColor(0xFF64748B);
        tvBmiDesc.setPadding(0, 4, 0, 0);

        bmiBox.addView(tvBmiTitle);
        bmiBox.addView(tvBmiDesc);

        sbWeight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                weightKg = 30 + progress;
                tvWeightVal.setText(isMetric ? String.format(Locale.getDefault(), "%.1f kg", weightKg) : String.format(Locale.getDefault(), "%.1f lbs", weightKg * 2.20462));

                double hM = (heightCm > 0 ? heightCm : 165.0) / 100.0;
                double b = weightKg / (hM * hM);
                String cat;
                int cColor;
                if (b < 18.5) { cat = "Underweight"; cColor = 0xFF3B82F6; }
                else if (b < 25.0) { cat = "Normal"; cColor = 0xFF10B981; }
                else if (b < 30.0) { cat = "Overweight"; cColor = 0xFFF97316; }
                else { cat = "Obesity"; cColor = 0xFFEF4444; }

                tvBmiTitle.setText(String.format(Locale.getDefault(), "Your BMI: %.1f  [%s]", b, cat));
                tvBmiTitle.setTextColor(cColor);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        layout.addView(sbWeight);
        layout.addView(bmiBox);

        addLargeAnimatedIllustration(layout, R.drawable.ic_weight_scale_full, 260, 260);

        stepContainer.addView(layout);
    }

    // BLOOD GROUP SCREEN (Step 9)
    private void buildBloodGroupStep() {
        LinearLayout layout = createVerticalContainer();
        layout.addView(createHeaderTitle("🩸 What's your blood group?"));
        layout.addView(createSubTitle("Select your blood group for emergency and medical profile context."));

        String[] groups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "Don't know"};

        for (String g : groups) {
            boolean isSel = g.equalsIgnoreCase(bloodGroup);
            View card = createOptionCard(isSel ? "🩸 " + g : "🩸 " + g, isSel, 0xFFEF4444);
            card.setOnClickListener(v -> {
                bloodGroup = g;
                animateSelection(card);
                renderStep(9);
            });
            layout.addView(card);
        }

        addLargeAnimatedIllustration(layout, R.drawable.ic_blood_drop, 220, 220);

        stepContainer.addView(layout);
    }

    private void buildGoalStep() {
        LinearLayout layout = createVerticalContainer();
        layout.addView(createHeaderTitle("🎯 What do you want to achieve?"));
        layout.addView(createSubTitle("Tap an option to select your goal."));

        String[] goals = {"Lose Weight", "Build Muscle", "Maintain Weight", "Improve Fitness", "Build Strength", "Improve Endurance", "Improve Flexibility"};
        String[] descs = {
            "🔥 Burn fat and achieve a leaner body shape",
            "💪 Gain strength, muscle mass & definition",
            "⚖️ Keep current weight & balance health",
            "⚡ Increase endurance & overall energy",
            "🏋️ Heavy strength training & power",
            "🏃 Long-distance cardio & stamina",
            "🧘 Stretching, mobility & active recovery"
        };
        String[] icons = {"🔥", "💪", "⚖️", "⚡", "🏋️", "🏃", "🧘"};
        int[] colors = {0xFFF97316, 0xFF6366F1, 0xFF14B8A6, 0xFF8B5CF6, 0xFF2563EB, 0xFFF59E0B, 0xFFEC4899};

        for (int i = 0; i < goals.length; i++) {
            final String g = goals[i];
            View card = createDetailedOptionCard(icons[i] + " " + g, descs[i], g.equalsIgnoreCase(goal), colors[i]);
            card.setOnClickListener(v -> {
                goal = g;
                animateSelection(card);
                renderStep(10);
            });
            layout.addView(card);
        }
        addLargeAnimatedIllustration(layout, R.drawable.ic_target_goal, 240, 240);
        stepContainer.addView(layout);
    }

    private void buildTargetWeightStep() {
        LinearLayout layout = createVerticalContainer();
        layout.addView(createHeaderTitle("🎯 What is your target weight?"));
        layout.addView(createSubTitle("Enter your target goal weight."));

        TextView tvTargetVal = new TextView(this);
        if (targetWeightKg == 0) targetWeightKg = weightKg > 0 ? weightKg - 5.0 : 65.0;
        tvTargetVal.setText(isMetric ? String.format(Locale.getDefault(), "%.1f kg", targetWeightKg) : String.format(Locale.getDefault(), "%.1f lbs", targetWeightKg * 2.20462));
        tvTargetVal.setTextSize(34);
        tvTargetVal.setTypeface(null, Typeface.BOLD);
        tvTargetVal.setTextColor(0xFF2563EB);
        tvTargetVal.setGravity(Gravity.CENTER);
        tvTargetVal.setPadding(0, 16, 0, 16);
        layout.addView(tvTargetVal);

        SeekBar sbTarget = new SeekBar(this);
        sbTarget.setMax(170);
        sbTarget.setProgress((int) (targetWeightKg - 30));
        sbTarget.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                targetWeightKg = 30 + progress;
                tvTargetVal.setText(isMetric ? String.format(Locale.getDefault(), "%.1f kg", targetWeightKg) : String.format(Locale.getDefault(), "%.1f lbs", targetWeightKg * 2.20462));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        layout.addView(sbTarget);

        addLargeAnimatedIllustration(layout, R.drawable.ic_target_pace, 240, 240);

        stepContainer.addView(layout);
    }

    private void buildMilestoneProjectionSlide() {
        LinearLayout layout = createVerticalContainer();
        layout.addView(createHeaderTitle("With FitTrain, every milestone is within reach"));
        layout.addView(createSubTitle("Step-by-step guidance to help you reach your goals safely and effectively."));

        addLargeAnimatedIllustration(layout, R.drawable.ic_milestone_graph, 240, 240);

        LinearLayout cardMilestones = new LinearLayout(this);
        cardMilestones.setOrientation(LinearLayout.HORIZONTAL);
        cardMilestones.setBackgroundResource(R.drawable.bg_option_card_selected);
        cardMilestones.setPadding(20, 20, 20, 20);

        double target = targetWeightKg > 0 ? targetWeightKg : 65.0;
        double current = weightKg > 0 ? weightKg : 70.0;
        double mid = (current + target) / 2.0;

        cardMilestones.addView(createMilestonePill(String.format(Locale.getDefault(), "%.0f kg", current), 0xFFF87171));
        cardMilestones.addView(createMilestonePill(String.format(Locale.getDefault(), "%.0f kg", mid), 0xFFFB923C));
        cardMilestones.addView(createMilestonePill(String.format(Locale.getDefault(), "%.0f kg", target), 0xFF4ADE80));

        layout.addView(cardMilestones);
        stepContainer.addView(layout);
    }

    private View createMilestonePill(String weightText, int bgColor) {
        TextView tv = new TextView(this);
        tv.setText(weightText);
        tv.setTextSize(14);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextColor(0xFFFFFFFF);
        tv.setGravity(Gravity.CENTER);
        tv.setBackgroundColor(bgColor);
        tv.setPadding(16, 12, 16, 12);

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        p.setMargins(4, 0, 4, 0);
        tv.setLayoutParams(p);
        return tv;
    }

    private void buildDietSectionIntroSlide() {
        LinearLayout layout = createVerticalContainer();
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setPadding(20, 20, 20, 20);

        addLargeAnimatedIllustration(layout, R.drawable.ic_ai_meal_scan, 260, 260);

        TextView tvTitle = createHeaderTitle("Let's learn more about your eating habits");
        tvTitle.setGravity(Gravity.CENTER);

        TextView tvSub = createSubTitle("Making smart food decisions is a big part of weight loss and body transformation.");
        tvSub.setGravity(Gravity.CENTER);

        layout.addView(tvTitle);
        layout.addView(tvSub);

        stepContainer.addView(layout);
    }

    private void buildEatingEnvironmentsStep() {
        LinearLayout layout = createVerticalContainer();
        addBehindTheQuestionCard(layout, "1. Nutritional Awareness...", "Where you eat influences your caloric intake and meal choices.");

        layout.addView(createHeaderTitle("In what environments do you usually eat?"));

        String[] envs = {"At home", "At restaurants", "At the office or school", "Others"};
        String[] icons = {"🏠 At home", "🥘 At restaurants", "🏫 At the office or school", "🏕️ Others"};

        for (int i = 0; i < envs.length; i++) {
            final String e = envs[i];
            View card = createOptionCard(icons[i], e.equalsIgnoreCase(eatingEnvironment), 0xFFF97316);
            card.setOnClickListener(v -> {
                eatingEnvironment = e;
                animateSelection(card);
                renderStep(14);
            });
            layout.addView(card);
        }
        addLargeAnimatedIllustration(layout, R.drawable.ic_fork_knife, 220, 220);
        stepContainer.addView(layout);
    }

    private void buildMealsPerDayStep() {
        LinearLayout layout = createVerticalContainer();
        addBehindTheQuestionCard(layout, "1. Energy Stability...", "Meal frequency helps us format your ideal protein and caloric distribution.");

        layout.addView(createHeaderTitle("How many meals do you have per day?"));

        int[] meals = {5, 4, 3, 2};
        String[] titles = {"5 meals per day (Meals + 2 Snacks)", "4 meals per day (Meals + 1 Snack)", "3 meals per day (Breakfast, Lunch, Dinner)", "2 meals per day (Breakfast & Lunch)"};
        String[] icons = {"🍳🍝🥘🍟🥨", "🍳🍝🥘🍟", "🍳🍝🥘", "🍳🍝"};

        for (int i = 0; i < meals.length; i++) {
            final int m = meals[i];
            View card = createDetailedOptionCard(icons[i] + "  " + m + " Meals", titles[i], mealsPerDay == m, 0xFFF97316);
            card.setOnClickListener(v -> {
                mealsPerDay = m;
                animateSelection(card);
                renderStep(15);
            });
            layout.addView(card);
        }
        addLargeAnimatedIllustration(layout, R.drawable.ic_diet_plate, 220, 220);
        stepContainer.addView(layout);
    }

    private void buildNutritionReportSlide() {
        LinearLayout layout = createVerticalContainer();
        layout.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView tvTitle = createHeaderTitle("Nutrition report based on your answers");
        tvTitle.setGravity(Gravity.CENTER);
        layout.addView(tvTitle);

        LinearLayout cardBmr = new LinearLayout(this);
        cardBmr.setOrientation(LinearLayout.VERTICAL);
        cardBmr.setBackgroundResource(R.drawable.bg_option_card_selected);
        cardBmr.setPadding(24, 20, 24, 20);
        cardBmr.setGravity(Gravity.CENTER);

        TextView tvBmrLabel = new TextView(this);
        tvBmrLabel.setText("BMR (Basal Metabolic Rate)");
        tvBmrLabel.setTextSize(13);
        tvBmrLabel.setTextColor(0xFF64748B);
        cardBmr.addView(tvBmrLabel);

        TextView tvBmrVal = new TextView(this);
        tvBmrVal.setText("1450 Cal per day");
        tvBmrVal.setTextSize(24);
        tvBmrVal.setTypeface(null, Typeface.BOLD);
        tvBmrVal.setTextColor(0xFFF97316);
        cardBmr.addView(tvBmrVal);

        DonutMacroChart chart = new DonutMacroChart(this);
        LinearLayout.LayoutParams cParams = new LinearLayout.LayoutParams(180, 180);
        cParams.setMargins(0, 16, 0, 16);
        chart.setLayoutParams(cParams);
        cardBmr.addView(chart);

        TextView tvLegend = new TextView(this);
        tvLegend.setText("🟠 Carbs (50%)   🟢 Protein (30%)   🟡 Fat (20%)");
        tvLegend.setTextSize(12);
        tvLegend.setTypeface(null, Typeface.BOLD);
        tvLegend.setTextColor(0xFF0F172A);
        cardBmr.addView(tvLegend);

        layout.addView(cardBmr);
        stepContainer.addView(layout);
    }

    private void buildDietStyleStep() {
        LinearLayout layout = createVerticalContainer();
        layout.addView(createHeaderTitle("🥗 What is your dietary style?"));

        String[] diets = {"Balanced", "Non-Vegetarian", "Vegetarian", "Vegan", "Halal", "Low Carb"};
        String[] icons = {"🍽️ Balanced", "🥩 Non-Vegetarian", "🥗 Vegetarian", "🥬 Vegan", "🕌 Halal", "🥑 Low Carb"};

        for (int i = 0; i < diets.length; i++) {
            final String d = diets[i];
            View card = createOptionCard(icons[i], d.equalsIgnoreCase(dietaryPreference), 0xFFF97316);
            card.setOnClickListener(v -> {
                dietaryPreference = d;
                animateSelection(card);
                renderStep(17);
            });
            layout.addView(card);
        }
        addLargeAnimatedIllustration(layout, R.drawable.ic_diet_plate, 180, 180);
        stepContainer.addView(layout);
    }

    // HEALTH CONCERNS SCREEN WITH "OTHERS" EXPANDABLE CUSTOM INPUT FIELD (Step 18)
    private void buildHealthConcernsStep() {
        LinearLayout layout = createVerticalContainer();
        addBehindTheQuestionCard(layout, "1. Safe Exercise Recommendation...", "Informing us of health concerns ensures we generate exercise & diet plans safely.");

        layout.addView(createHeaderTitle("Do you have any health concerns?"));

        String[] concerns = {"None", "Diabetes", "High blood pressure", "Asthma", "Joint/Knee issues", "Back problems", "Heart-related concerns", "Thyroid", "Others"};
        String[] icons = {"🧘 None", "💉 Diabetes", "🩸 High blood pressure", "🫁 Asthma", "🦵 Joint/Knee issues", "🦴 Back problems", "🫀 Heart-related concerns", "🦋 Thyroid", "➕ Others"};

        for (int i = 0; i < concerns.length; i++) {
            final String c = concerns[i];
            boolean isSel = selectedHealthConcerns.contains(c);
            View card = createOptionCard(icons[i], isSel, 0xFFEF4444);
            card.setOnClickListener(v -> {
                if ("None".equalsIgnoreCase(c)) {
                    selectedHealthConcerns.clear();
                    selectedHealthConcerns.add("None");
                } else {
                    selectedHealthConcerns.remove("None");
                    if (selectedHealthConcerns.contains(c)) selectedHealthConcerns.remove(c);
                    else selectedHealthConcerns.add(c);
                }
                animateSelection(card);
                renderStep(18);
            });
            layout.addView(card);
        }

        // Expandable Custom Input Area when "Others" is selected
        if (selectedHealthConcerns.contains("Others")) {
            LinearLayout boxOther = new LinearLayout(this);
            boxOther.setOrientation(LinearLayout.VERTICAL);
            boxOther.setBackgroundResource(R.drawable.bg_option_card_selected);
            boxOther.setPadding(20, 16, 20, 16);

            TextView tvPrompt = new TextView(this);
            tvPrompt.setText("Please tell us about your health concern:");
            tvPrompt.setTextSize(14);
            tvPrompt.setTypeface(null, Typeface.BOLD);
            tvPrompt.setTextColor(0xFF0F172A);
            boxOther.addView(tvPrompt);

            EditText etOther = createEditText("Enter your specific health concern...");
            etOther.setText(otherHealthConcernText);
            etOther.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    otherHealthConcernText = s.toString();
                }
                @Override public void afterTextChanged(Editable s) {}
            });
            boxOther.addView(etOther);

            LinearLayout.LayoutParams oParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            oParams.setMargins(0, 12, 0, 0);
            boxOther.setLayoutParams(oParams);
            layout.addView(boxOther);
        }

        addLargeAnimatedIllustration(layout, R.drawable.ic_health_shield, 240, 240);

        stepContainer.addView(layout);
    }

    private void buildActivityStep() {
        LinearLayout layout = createVerticalContainer();
        addBehindTheQuestionCard(layout, "1. Calorie Needs Calculation...", "Activity level determines Total Daily Energy Expenditure (TDEE).");

        layout.addView(createHeaderTitle("What is your activity level?"));

        String[] acts = {"Sedentary", "Light active", "Moderately active", "Very active"};
        String[] actDescs = {
            "📶 I spend most of my day sitting",
            "📶 I have made doing exercises a lasting habit",
            "📶 I work on my feet and move around throughout the day",
            "📶 I spend most of my day doing physical activities"
        };

        for (int i = 0; i < acts.length; i++) {
            final String a = acts[i];
            View card = createDetailedOptionCard(a, actDescs[i], a.equalsIgnoreCase(activityLevel), 0xFF6366F1);
            card.setOnClickListener(v -> {
                activityLevel = a;
                animateSelection(card);
                renderStep(19);
            });
            layout.addView(card);
        }

        addLargeAnimatedIllustration(layout, R.drawable.ic_activity_runner, 240, 240);

        stepContainer.addView(layout);
    }

    private void buildExperienceStep() {
        LinearLayout layout = createVerticalContainer();
        layout.addView(createHeaderTitle("🌱 What's your fitness level?"));

        String[] exps = {"Beginner", "Intermediate", "Advanced"};
        String[] expDescs = {"🌱 New to workout routines", "🌿 Active 6+ months", "🌳 Experienced in strength training"};

        for (int i = 0; i < exps.length; i++) {
            final String e = exps[i];
            View card = createDetailedOptionCard(e, expDescs[i], e.equalsIgnoreCase(fitnessExperience), 0xFF6366F1);
            card.setOnClickListener(v -> {
                fitnessExperience = e;
                animateSelection(card);
                renderStep(20);
            });
            layout.addView(card);
        }

        addLargeAnimatedIllustration(layout, R.drawable.ic_fitness_level, 240, 240);

        stepContainer.addView(layout);
    }

    private void buildWorkoutPreferencesStep() {
        LinearLayout layout = createVerticalContainer();
        layout.addView(createHeaderTitle("🏠 Where do you work out & equipment?"));

        String[] locs = {"Home", "Gym", "Both"};
        String[] icons = {"🏠 At Home", "🏋️ At the Gym", "🔄 Both Home & Gym"};

        for (int i = 0; i < locs.length; i++) {
            final String l = locs[i];
            View card = createOptionCard(icons[i], l.equalsIgnoreCase(workoutLocation), 0xFF6366F1);
            card.setOnClickListener(v -> {
                workoutLocation = l;
                animateSelection(card);
                renderStep(21);
            });
            layout.addView(card);
        }

        addLargeAnimatedIllustration(layout, R.drawable.ic_workout_gear, 240, 240);

        stepContainer.addView(layout);
    }

    // ====================================================================
    // PLAN GENERATION & SUMMARY
    // ====================================================================

    private void buildPlanGenerationStep() {
        LinearLayout layout = createVerticalContainer();
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setPadding(20, 60, 20, 40);

        ProgressBar pbSpinner = new ProgressBar(this);
        pbSpinner.setIndeterminate(true);
        layout.addView(pbSpinner);

        TextView tvGenTitle = createHeaderTitle("Generating Your FitTrain Plan...");
        tvGenTitle.setGravity(Gravity.CENTER);
        tvGenTitle.setPadding(0, 24, 0, 12);
        layout.addView(tvGenTitle);

        TextView tvStatus = new TextView(this);
        tvStatus.setTextSize(15);
        tvStatus.setTextColor(0xFF2563EB);
        tvStatus.setTypeface(null, Typeface.BOLD);
        tvStatus.setGravity(Gravity.CENTER);
        layout.addView(tvStatus);

        stepContainer.addView(layout);

        String[] statusSteps = {
            "Analyzing body metrics & BMR...",
            "Calculating daily calorie deficit & hydration targets...",
            "Filtering exercise plan for your equipment...",
            "Generating personalized nutrition schedule...",
            "Personalizing AI Fitness Coach context..."
        };

        Handler handler = new Handler(Looper.getMainLooper());
        for (int i = 0; i < statusSteps.length; i++) {
            final int index = i;
            handler.postDelayed(() -> tvStatus.setText(statusSteps[index]), index * 500L);
        }

        handler.postDelayed(() -> {
            generateAndSaveUserProfile();
            currentStep = 23;
            renderStep(23);
        }, 2600L);
    }

    private void generateAndSaveUserProfile() {
        User user = new User("user_" + System.currentTimeMillis(), name, "", "", System.currentTimeMillis());
        user.setProfileCompleted(true);
        user.setDob(dobString);
        user.setBloodGroup(bloodGroup.isEmpty() ? "O+" : bloodGroup);
        user.setAge(age > 0 ? age : 25);
        user.setGender(gender.isEmpty() ? "Female" : gender);
        user.setHeight(heightCm > 0 ? heightCm : 165.0);
        user.setWeight(weightKg > 0 ? weightKg : 70.0);
        user.setGoal(goal.isEmpty() ? "Lose Weight" : goal);
        user.setTargetWeight(targetWeightKg > 0 ? targetWeightKg : 65.0);
        user.setTargetPace(targetPace.isEmpty() ? "Balanced" : targetPace);
        user.setActivityLevel(activityLevel.isEmpty() ? "Lightly Active" : activityLevel);
        user.setFitnessExperience(fitnessExperience.isEmpty() ? "Beginner" : fitnessExperience);

        user.setWorkoutLocation(workoutLocation.isEmpty() ? "Home" : workoutLocation);
        user.setAvailableEquipment(selectedEquipment.isEmpty() ? "No Equipment" : joinSet(selectedEquipment, ", "));
        user.setWorkoutDuration(workoutDuration > 0 ? workoutDuration : 30);
        user.setWorkoutDaysPerWeek(workoutDaysPerWeek > 0 ? workoutDaysPerWeek : 4);
        user.setPreferredWorkoutTime(preferredWorkoutTime.isEmpty() ? "Morning" : preferredWorkoutTime);

        user.setDietaryPreference(dietaryPreference.isEmpty() ? "Balanced" : dietaryPreference);
        user.setAllergies(selectedAllergies.isEmpty() ? "None" : joinSet(selectedAllergies, ", "));
        user.setMealsPerDay(mealsPerDay > 0 ? mealsPerDay : 3);
        user.setEatingEnvironment(eatingEnvironment.isEmpty() ? "At home" : eatingEnvironment);
        user.setEatingOutFrequency(eatingOutFrequency.isEmpty() ? "1-2 times per week" : eatingOutFrequency);

        String finalHealthConcerns = selectedHealthConcerns.isEmpty() ? "None" : joinSet(selectedHealthConcerns, ", ");
        if (selectedHealthConcerns.contains("Others") && !otherHealthConcernText.isEmpty()) {
            finalHealthConcerns += " (" + otherHealthConcernText.trim() + ")";
        }
        user.setMedicalConditions(finalHealthConcerns);
        user.setInjuries(injuries.isEmpty() ? "None" : injuries);

        double w = user.getWeight();
        double h = user.getHeight();
        int a = user.getAge();

        double bmr;
        if ("Male".equalsIgnoreCase(user.getGender())) {
            bmr = 10 * w + 6.25 * h - 5 * a + 5;
        } else {
            bmr = 10 * w + 6.25 * h - 5 * a - 161;
        }

        double multiplier = 1.375;
        if (user.getActivityLevel().contains("Sedentary")) multiplier = 1.2;
        else if (user.getActivityLevel().contains("Light")) multiplier = 1.375;
        else if (user.getActivityLevel().contains("Moderat")) multiplier = 1.55;
        else if (user.getActivityLevel().contains("Very")) multiplier = 1.725;

        double tdee = bmr * multiplier;
        int targetCalories;
        if ("Lose Weight".equalsIgnoreCase(user.getGoal())) {
            targetCalories = (int) (tdee - 500);
        } else if ("Build Muscle".equalsIgnoreCase(user.getGoal()) || "Gain Weight".equalsIgnoreCase(user.getGoal())) {
            targetCalories = (int) (tdee + 350);
        } else {
            targetCalories = (int) tdee;
        }

        user.setDailyCaloriesGoal(Math.max(1200, targetCalories));
        user.setDailyWaterGoal(FitnessCalculator.calculateDailyWaterGoal(w));
        user.setDailyStepGoal(user.getActivityLevel().contains("Sedentary") ? 6000 : 8500);
        user.setDailySleepGoal(480);

        localDb.setOnboardingSeen(true);
        localDb.saveUser(user);

        WorkoutPlan workoutPlan = RecommendationEngine.generateDailyWorkoutPlan(user, "Normal", true);
        localDb.saveWorkoutPlan(workoutPlan);

        DietPlan dietPlan = RecommendationEngine.generateDailyDietPlan(user, "Normal", true);
        localDb.saveDietPlan(dietPlan);
    }

    private void buildPersonalizedWelcomeStep() {
        LinearLayout layout = createVerticalContainer();

        User u = localDb.getUser();
        String displayUser = (u != null && u.getFirstName() != null && !u.getFirstName().isEmpty()) ? u.getFirstName() : name;

        TextView tvTitle = createHeaderTitle("Welcome to FitTrain, " + displayUser + "! 🎉");
        TextView tvSub = createSubTitle("Your custom " + (u != null ? u.getGoal() : goal) + " plan is ready. Here is your personalized targets overview:");
        layout.addView(tvTitle);
        layout.addView(tvSub);

        LinearLayout summaryBox = new LinearLayout(this);
        summaryBox.setOrientation(LinearLayout.VERTICAL);
        summaryBox.setBackgroundResource(R.drawable.bg_option_card_selected);
        summaryBox.setPadding(24, 20, 24, 20);

        summaryBox.addView(createSummaryRow("🎯 Primary Goal", (u != null ? u.getGoal() : goal) + " (Target: " + String.format(Locale.getDefault(), "%.1f kg", (u != null ? u.getTargetWeight() : targetWeightKg)) + ")"));
        summaryBox.addView(createSummaryRow("🩸 Blood Group", (u != null ? u.getBloodGroup() : bloodGroup)));
        summaryBox.addView(createSummaryRow("🔥 Daily Calories", (u != null ? u.getDailyCaloriesGoal() : 1800) + " kcal target"));
        summaryBox.addView(createSummaryRow("💧 Daily Water", (u != null ? u.getDailyWaterGoal() : 2500) + " ml hydration"));
        summaryBox.addView(createSummaryRow("👣 Daily Step Target", (u != null ? u.getDailyStepGoal() : 8000) + " steps"));
        summaryBox.addView(createSummaryRow("🏋️ Workout Session", (u != null ? u.getWorkoutDuration() : 30) + " min (" + (u != null ? u.getWorkoutLocation() : "Home") + ")"));
        summaryBox.addView(createSummaryRow("🥗 Nutrition Style", (u != null ? u.getDietaryPreference() : "Balanced") + " diet"));

        layout.addView(summaryBox);
        stepContainer.addView(layout);
    }

    private View createSummaryRow(String title, String val) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 10, 0, 10);

        TextView tvLabel = new TextView(this);
        tvLabel.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        tvLabel.setText(title);
        tvLabel.setTextSize(14);
        tvLabel.setTextColor(0xFF0F172A);

        TextView tvVal = new TextView(this);
        tvVal.setText(val);
        tvVal.setTextSize(14);
        tvVal.setTypeface(null, Typeface.BOLD);
        tvVal.setTextColor(0xFF2563EB);

        row.addView(tvLabel);
        row.addView(tvVal);
        return row;
    }

    private void finishOnboardingAndLaunchHome() {
        Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private String joinSet(Set<String> set, String delimiter) {
        if (set == null || set.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String s : set) {
            if (!first) sb.append(delimiter);
            sb.append(s);
            first = false;
        }
        return sb.toString();
    }

    // ==========================================
    // SELECTION ANIMATION & CARD BUILDERS
    // ==========================================

    private void addLargeAnimatedIllustration(LinearLayout container, int drawableResId, int widthDp, int heightDp) {
        float density = getResources().getDisplayMetrics().density;

        // Eato-style Large Visual Card Container
        LinearLayout cardBox = new LinearLayout(this);
        cardBox.setOrientation(LinearLayout.VERTICAL);
        cardBox.setGravity(Gravity.CENTER);
        cardBox.setBackgroundResource(R.drawable.bg_option_card_unselected);
        cardBox.setPadding((int) (16 * density), (int) (16 * density), (int) (16 * density), (int) (16 * density));

        ImageView iv = new ImageView(this);
        iv.setImageResource(drawableResId);
        int w = (int) (widthDp * density);
        int h = (int) (heightDp * density);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(w, h);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        iv.setLayoutParams(params);

        cardBox.addView(iv);

        LinearLayout.LayoutParams boxParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        boxParams.setMargins(0, (int) (18 * density), 0, (int) (12 * density));
        cardBox.setLayoutParams(boxParams);

        // Entrance Micro-Animations: Slide Up + Scale Pulse + Fade In
        TranslateAnimation animImg = new TranslateAnimation(0, 0, 75 * density, 0);
        animImg.setDuration(450);
        ScaleAnimation animScale = new ScaleAnimation(0.82f, 1.0f, 0.82f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animScale.setDuration(450);
        AlphaAnimation animAlpha = new AlphaAnimation(0.0f, 1.0f);
        animAlpha.setDuration(450);

        cardBox.startAnimation(animImg);
        cardBox.startAnimation(animScale);
        cardBox.startAnimation(animAlpha);

        container.addView(cardBox);
    }

    private void animateSelection(View v) {
        ScaleAnimation scale = new ScaleAnimation(0.97f, 1.0f, 0.97f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(150);
        v.startAnimation(scale);
    }

    private LinearLayout createVerticalContainer() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return layout;
    }

    private TextView createHeaderTitle(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(22);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextColor(0xFF0F172A);
        tv.setPadding(0, 0, 0, 8);
        return tv;
    }

    private TextView createSubTitle(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(14);
        tv.setTextColor(0xFF64748B);
        tv.setPadding(0, 0, 0, 20);
        return tv;
    }

    private EditText createEditText(String hint) {
        EditText et = new EditText(this);
        et.setHint(hint);
        et.setTextSize(16);
        et.setPadding(24, 20, 24, 20);
        et.setBackgroundResource(R.drawable.bg_edittext);
        et.setSingleLine(true);
        return et;
    }

    // UNSELECTED CARD BUILDER (Default is UNSELECTED)
    private View createOptionCard(String title, boolean isSelected, int accentColor) {
        TextView tv = new TextView(this);
        tv.setText(isSelected ? title + "  ✓" : title);
        tv.setTextSize(15);
        tv.setTypeface(null, isSelected ? Typeface.BOLD : Typeface.NORMAL);
        tv.setTextColor(isSelected ? accentColor : 0xFF0F172A);
        tv.setBackgroundResource(isSelected ? R.drawable.bg_option_card_selected : R.drawable.bg_option_card_unselected);
        tv.setPadding(24, 20, 24, 20);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 6, 0, 6);
        tv.setLayoutParams(params);
        return tv;
    }

    private View createDetailedOptionCard(String title, String desc, boolean isSelected, int accentColor) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundResource(isSelected ? R.drawable.bg_option_card_selected : R.drawable.bg_option_card_unselected);
        layout.setPadding(24, 18, 24, 18);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(isSelected ? title + "  ✓" : title);
        tvTitle.setTextSize(16);
        tvTitle.setTypeface(null, isSelected ? Typeface.BOLD : Typeface.NORMAL);
        tvTitle.setTextColor(isSelected ? accentColor : 0xFF0F172A);

        TextView tvDesc = new TextView(this);
        tvDesc.setText(desc);
        tvDesc.setTextSize(13);
        tvDesc.setTextColor(0xFF64748B);
        tvDesc.setPadding(0, 4, 0, 0);

        layout.addView(tvTitle);
        layout.addView(tvDesc);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 8, 0, 8);
        layout.setLayoutParams(params);
        return layout;
    }

    private View createBulletItem(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(13);
        tv.setTextColor(0xFF64748B);
        tv.setPadding(0, 4, 0, 4);
        return tv;
    }

    private View createCheckItem(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(13);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextColor(0xFF10B981);
        tv.setPadding(0, 4, 0, 4);
        return tv;
    }

    private View createFeatureBadge(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(14);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextColor(0xFF0F172A);
        tv.setBackgroundResource(R.drawable.bg_option_card_unselected);
        tv.setPadding(20, 16, 20, 16);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 6, 0, 6);
        tv.setLayoutParams(params);
        return tv;
    }

    // Donut Macro Ring Custom View
    private static class DonutMacroChart extends View {
        public DonutMacroChart(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int width = getWidth();
            int height = getHeight();
            int minDim = Math.min(width, height);
            if (minDim <= 0) return;

            float strokeWidth = minDim * 0.18f;
            float radius = (minDim - strokeWidth) / 2f;
            float cx = width / 2f;
            float cy = height / 2f;

            RectF rect = new RectF(cx - radius, cy - radius, cx + radius, cy + radius);

            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(strokeWidth);

            float startAngle = -90f;
            // Carbs 50% (Orange)
            paint.setColor(0xFFF97316);
            canvas.drawArc(rect, startAngle, 180f, false, paint);
            startAngle += 180f;

            // Protein 30% (Green)
            paint.setColor(0xFF84CC16);
            canvas.drawArc(rect, startAngle, 108f, false, paint);
            startAngle += 108f;

            // Fat 20% (Yellow)
            paint.setColor(0xFFFBBF24);
            canvas.drawArc(rect, startAngle, 72f, false, paint);
        }
    }
}

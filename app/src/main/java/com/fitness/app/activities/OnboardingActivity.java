package com.fitness.app.activities;

import android.animation.ObjectAnimator;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.fitness.app.R;
import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.models.User;
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

    // Total Flow Steps: 0 to 23
    // Step 0: Intro 1, Step 1: Intro 2, Step 2: Intro 3
    // Steps 3 to 20: Questions 1 to 18 (TOTAL 18 QUESTIONS)
    // Step 21: Dedicated Profile Setup Screen (Photo Upload & Skip)
    // Step 22: Plan Generation, Step 23: Welcome & Finish
    private int currentStep = 0;
    private static final int TOTAL_QUESTION_STEPS = 18; // Steps 3 to 20

    // User Profile & Question State - Starts EMPTY/UNSELECTED
    private String name = "";
    private String dobString = ""; // yyyy-MM-dd
    private int birthYear = 0;
    private int birthMonth = 0;
    private int birthDay = 0;
    private int age = 0;

    private String gender = ""; // UNSELECTED
    private boolean isMetric = true;
    private double heightCm = 170.0;
    private double weightKg = 70.0;
    private String bloodGroup = ""; // UNSELECTED
    private String goal = ""; // UNSELECTED
    private double targetWeightKg = 65.0;
    private String targetPace = ""; // UNSELECTED

    private String eatingEnvironment = ""; // UNSELECTED
    private int mealsPerDay = 0; // UNSELECTED
    private String dietaryPreference = ""; // UNSELECTED
    private final Set<String> selectedHealthConcerns = new HashSet<>();

    private String activityLevel = ""; // UNSELECTED
    private String fitnessExperience = ""; // UNSELECTED
    private String workoutLocation = ""; // UNSELECTED
    private final Set<String> selectedEquipment = new HashSet<>();
    private int workoutDuration = 0; // UNSELECTED
    private int workoutDaysPerWeek = 0; // UNSELECTED
    private String preferredWorkoutTime = ""; // UNSELECTED

    private String selectedProfileImageUri = "";

    // Profile Photo Crop Launcher (Pinch-to-zoom, reposition, crop)
    private final ActivityResultLauncher<Intent> cropLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String croppedUriStr = result.getData().getStringExtra("cropped_uri");
                    if (croppedUriStr != null && !croppedUriStr.isEmpty()) {
                        selectedProfileImageUri = croppedUriStr;
                        if (currentStep == 21) {
                            renderStep(21);
                        }
                    }
                }
            }
    );

    // Gallery Picker Launcher -> Passes image to CropActivity
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (Exception ignored) {}
                    Intent cropIntent = new Intent(OnboardingActivity.this, CropActivity.class);
                    cropIntent.putExtra("image_uri", imageUri.toString());
                    cropLauncher.launch(cropIntent);
                }
            }
    );

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
        pbOnboardingProgress.setMax(100);
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
            btnBackStep.setVisibility(step > 0 ? View.VISIBLE : View.GONE);
            tvStepIndicator.setText(String.format(Locale.getDefault(), "0%d / 03", step + 1));
            pbOnboardingProgress.setVisibility(View.GONE);
            bottomBar.setVisibility(View.GONE);
        } else if (step >= 3 && step <= 20) {
            int qNum = step - 2; // Question 1 to 18
            btnBackStep.setVisibility(View.VISIBLE);
            tvStepIndicator.setText(String.format(Locale.getDefault(), "Question %d of %d", qNum, TOTAL_QUESTION_STEPS));
            pbOnboardingProgress.setVisibility(View.VISIBLE);
            
            int targetProgress = (qNum * 100) / TOTAL_QUESTION_STEPS;
            ObjectAnimator.ofInt(pbOnboardingProgress, "progress", pbOnboardingProgress.getProgress(), targetProgress)
                    .setDuration(300)
                    .start();

            btnNextStep.setText("Continue");
            bottomBar.setVisibility(View.VISIBLE);
        } else if (step == 21) {
            btnBackStep.setVisibility(View.VISIBLE);
            tvStepIndicator.setText("Profile Setup");
            pbOnboardingProgress.setVisibility(View.GONE);
            btnNextStep.setText("Continue");
            bottomBar.setVisibility(View.VISIBLE);
        } else if (step == 22) {
            btnBackStep.setVisibility(View.GONE);
            tvStepIndicator.setText("Analyzing Profile");
            pbOnboardingProgress.setVisibility(View.VISIBLE);
            pbOnboardingProgress.setProgress(100);
            bottomBar.setVisibility(View.GONE);
        } else if (step == 23) {
            btnBackStep.setVisibility(View.GONE);
            tvStepIndicator.setText("Your Plan Is Ready!");
            pbOnboardingProgress.setVisibility(View.GONE);
            bottomBar.setVisibility(View.VISIBLE);
            btnNextStep.setText("START MY FITTRAIN PLAN 🎉");
        }

        switch (step) {
            case 0: buildIntroTrophySlide(); break;
            case 1: buildIntroComparisonSlide(); break;
            case 2: buildIntroSatietySlide(); break;

            case 3: buildNameStep(); break;
            case 4: buildDateOfBirthStep(); break;
            case 5: buildGenderStep(); break;
            case 6: buildHeightStep(); break;
            case 7: buildWeightStep(); break;
            case 8: buildBloodGroupStep(); break;
            case 9: buildGoalStep(); break;
            case 10: buildTargetWeightStep(); break;
            case 11: buildTargetPaceStep(); break;
            case 12: buildEatingEnvironmentsStep(); break;
            case 13: buildMealsPerDayStep(); break;
            case 14: buildDietStyleStep(); break;
            case 15: buildHealthConcernsStep(); break;
            case 16: buildActivityStep(); break;
            case 17: buildExperienceStep(); break;
            case 18: buildWorkoutPreferencesStep(); break;
            case 19: buildWorkoutScheduleStep(); break;
            case 20: buildWorkoutTimeStep(); break;

            case 21: buildProfileSetupStep(); break;
            case 22: buildPlanGenerationStep(); break;
            case 23: buildPersonalizedWelcomeStep(); break;
        }

        updateNextButtonState();

        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(300);
        TranslateAnimation slideUp = new TranslateAnimation(0, 0, 40, 0);
        slideUp.setDuration(300);

        stepContainer.startAnimation(fadeIn);
        stepContainer.startAnimation(slideUp);
    }

    private void updateNextButtonState() {
        if (currentStep >= 3 && currentStep <= 20) {
            boolean valid = isStepValid(currentStep);
            btnNextStep.setEnabled(valid);
            btnNextStep.setAlpha(valid ? 1.0f : 0.4f);
        } else {
            btnNextStep.setEnabled(true);
            btnNextStep.setAlpha(1.0f);
        }
    }

    private boolean isStepValid(int step) {
        switch (step) {
            case 3: return !name.trim().isEmpty();
            case 4: return !dobString.isEmpty() && age > 0;
            case 5: return !gender.isEmpty();
            case 6: return heightCm >= 50 && heightCm <= 250;
            case 7: return weightKg >= 20 && weightKg <= 300;
            case 8: return !bloodGroup.isEmpty();
            case 9: return !goal.isEmpty();
            case 10: return targetWeightKg >= 20 && targetWeightKg <= 300;
            case 11: return !targetPace.isEmpty();
            case 12: return !eatingEnvironment.isEmpty();
            case 13: return mealsPerDay > 0;
            case 14: return !dietaryPreference.isEmpty();
            case 15: return !selectedHealthConcerns.isEmpty();
            case 16: return !activityLevel.isEmpty();
            case 17: return !fitnessExperience.isEmpty();
            case 18: return !workoutLocation.isEmpty();
            case 19: return workoutDuration > 0 && workoutDaysPerWeek > 0;
            case 20: return !preferredWorkoutTime.isEmpty();
            default: return true;
        }
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
            case 3: // Name
                if (name.trim().isEmpty()) {
                    showError("Please enter your name to continue.");
                    return false;
                }
                break;
            case 4: // DOB
                if (dobString.isEmpty() || age <= 0) {
                    showError("Please select your date of birth.");
                    return false;
                }
                break;
            case 5: // Gender
                if (gender.isEmpty()) {
                    showError("Please select your gender.");
                    return false;
                }
                break;
            case 6: // Height
                if (heightCm < 50 || heightCm > 250) {
                    showError("Please specify a valid height.");
                    return false;
                }
                break;
            case 7: // Weight
                if (weightKg < 20 || weightKg > 300) {
                    showError("Please specify a valid weight.");
                    return false;
                }
                break;
            case 8: // Blood Group
                if (bloodGroup.isEmpty()) {
                    showError("Please select your blood group.");
                    return false;
                }
                break;
            case 9: // Goal
                if (goal.isEmpty()) {
                    showError("Please select your primary fitness goal.");
                    return false;
                }
                break;
            case 10: // Target Weight
                if (targetWeightKg < 20 || targetWeightKg > 300) {
                    showError("Please specify a target weight.");
                    return false;
                }
                break;
            case 11: // Target Pace
                if (targetPace.isEmpty()) {
                    showError("Please select a target pace.");
                    return false;
                }
                break;
            case 12: // Eating Environment
                if (eatingEnvironment.isEmpty()) {
                    showError("Please select your primary eating environment.");
                    return false;
                }
                break;
            case 13: // Meals Per Day
                if (mealsPerDay <= 0) {
                    showError("Please select your daily meal frequency.");
                    return false;
                }
                break;
            case 14: // Diet Style
                if (dietaryPreference.isEmpty()) {
                    showError("Please select a dietary preference.");
                    return false;
                }
                break;
            case 15: // Health Concerns
                if (selectedHealthConcerns.isEmpty()) {
                    showError("Please select at least one option or 'None'.");
                    return false;
                }
                break;
            case 16: // Activity Level
                if (activityLevel.isEmpty()) {
                    showError("Please select your daily activity level.");
                    return false;
                }
                break;
            case 17: // Experience
                if (fitnessExperience.isEmpty()) {
                    showError("Please select your fitness level.");
                    return false;
                }
                break;
            case 18: // Workout Location & Equipment
                if (workoutLocation.isEmpty()) {
                    showError("Please select your preferred workout location.");
                    return false;
                }
                break;
            case 19: // Workout Schedule
                if (workoutDuration <= 0 || workoutDaysPerWeek <= 0) {
                    showError("Please select your workout schedule.");
                    return false;
                }
                break;
            case 20: // Preferred Workout Time
                if (preferredWorkoutTime.isEmpty()) {
                    showError("Please select your preferred workout time.");
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

    // Helper Container Builders
    private LinearLayout createVerticalContainer() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        l.setPadding(20, 16, 20, 24);
        return l;
    }

    private TextView createHeaderTitle(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(22);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextColor(Color.parseColor("#0F172A")); // Deep Navy
        tv.setPadding(0, 8, 0, 4);
        return tv;
    }

    private TextView createSubTitle(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(14);
        tv.setTextColor(Color.parseColor("#475569"));
        tv.setPadding(0, 0, 0, 16);
        return tv;
    }

    private View createOptionCard(String title, int iconRes, boolean selected, int activeColor) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(20, 16, 20, 16);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 6, 0, 6);
        card.setLayoutParams(params);

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(16 * getResources().getDisplayMetrics().density);
        if (selected) {
            bg.setColor(Color.parseColor("#F0F9FF")); // Soft Ice Blue tint
            bg.setStroke((int)(2 * getResources().getDisplayMetrics().density), Color.parseColor("#0284C7"));
        } else {
            bg.setColor(Color.WHITE);
            bg.setStroke((int)(1 * getResources().getDisplayMetrics().density), Color.parseColor("#E2E8F0"));
        }
        card.setBackground(bg);

        ImageView iv = new ImageView(this);
        iv.setImageResource(iconRes);
        LinearLayout.LayoutParams ivParams = new LinearLayout.LayoutParams(32, 32);
        ivParams.setMargins(0, 0, 16, 0);
        iv.setLayoutParams(ivParams);

        TextView tv = new TextView(this);
        tv.setText(title);
        tv.setTextSize(16);
        tv.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
        tv.setTextColor(selected ? Color.parseColor("#0284C7") : Color.parseColor("#0F172A"));

        card.addView(iv);
        card.addView(tv);
        return card;
    }

    private void animateSelection(View view) {
        ScaleAnimation anim = new ScaleAnimation(0.96f, 1.0f, 0.96f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(160);
        view.startAnimation(anim);
    }

    private void addLargeAnimatedIllustration(ViewGroup parent, int drawableRes, int widthDp, int heightDp) {
        ImageView iv = new ImageView(this);
        iv.setImageResource(drawableRes);
        float density = getResources().getDisplayMetrics().density;
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams((int)(widthDp * density), (int)(heightDp * density));
        p.gravity = Gravity.CENTER_HORIZONTAL;
        p.setMargins(0, 12, 0, 16);
        iv.setLayoutParams(p);

        ScaleAnimation scale = new ScaleAnimation(0.94f, 1.0f, 0.94f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(400);
        iv.startAnimation(scale);

        parent.addView(iv);
    }

    private void addBehindTheQuestionCard(ViewGroup parent, String title, String explanation) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(16, 12, 16, 12);
        
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#F8FAFC"));
        bg.setCornerRadius(12 * getResources().getDisplayMetrics().density);
        bg.setStroke((int)(1 * getResources().getDisplayMetrics().density), Color.parseColor("#E2E8F0"));
        card.setBackground(bg);

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        p.setMargins(0, 0, 0, 12);
        card.setLayoutParams(p);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("💡 " + title);
        tvTitle.setTextSize(13);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setTextColor(Color.parseColor("#0F172A"));

        TextView tvExp = new TextView(this);
        tvExp.setText(explanation);
        tvExp.setTextSize(12);
        tvExp.setTextColor(Color.parseColor("#64748B"));
        tvExp.setPadding(0, 4, 0, 0);

        card.addView(tvTitle);
        card.addView(tvExp);
        parent.addView(card);
    }

    // --- ELEGANT INTRO BOTTOM NAVIGATION BAR (Skip | ● ○ ○ | Next → / Get Started →) ---
    private View createIntroBottomNav(int activeIndex, String buttonText, View.OnClickListener onNextClick) {
        float density = getResources().getDisplayMetrics().density;

        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(0, (int)(20 * density), 0, (int)(12 * density));
        bar.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        // 1. Skip Button (Left) -> Routes directly to Question 1 (Step 3)
        TextView tvSkip = new TextView(this);
        tvSkip.setText("Skip");
        tvSkip.setTextSize(15);
        tvSkip.setTypeface(null, Typeface.BOLD);
        tvSkip.setTextColor(Color.parseColor("#94A3B8"));
        tvSkip.setPadding((int)(8 * density), (int)(8 * density), (int)(8 * density), (int)(8 * density));
        tvSkip.setOnClickListener(v -> {
            currentStep = 3;
            renderStep(3);
        });

        LinearLayout.LayoutParams skipParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        tvSkip.setLayoutParams(skipParams);

        // 2. Pagination Dots (Center: ● ○ ○)
        LinearLayout dotsLayout = new LinearLayout(this);
        dotsLayout.setOrientation(LinearLayout.HORIZONTAL);
        dotsLayout.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams dotsParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        dotsLayout.setLayoutParams(dotsParams);

        for (int i = 0; i < 3; i++) {
            View dot = new View(this);
            int sizeDp = (i == activeIndex) ? 10 : 8;
            LinearLayout.LayoutParams dotP = new LinearLayout.LayoutParams(
                    (int)(sizeDp * density),
                    (int)(sizeDp * density)
            );
            dotP.setMargins((int)(4 * density), 0, (int)(4 * density), 0);
            dot.setLayoutParams(dotP);

            GradientDrawable dotBg = new GradientDrawable();
            dotBg.setShape(GradientDrawable.OVAL);
            dotBg.setColor(i == activeIndex ? Color.parseColor("#0284C7") : Color.parseColor("#CBD5E1"));
            dot.setBackground(dotBg);
            dotsLayout.addView(dot);
        }

        // 3. Next / Get Started Pill Button (Right)
        MaterialButton btnNext = new MaterialButton(this);
        btnNext.setText(buttonText);
        btnNext.setTextSize(15);
        btnNext.setTypeface(null, Typeface.BOLD);
        btnNext.setTextColor(Color.WHITE);
        btnNext.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#0284C7")));
        btnNext.setCornerRadius((int)(24 * density));
        btnNext.setPadding((int)(18 * density), (int)(10 * density), (int)(18 * density), (int)(10 * density));
        btnNext.setOnClickListener(v -> {
            animateSelection(btnNext);
            onNextClick.onClick(v);
        });

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.3f);
        btnNext.setLayoutParams(btnParams);

        bar.addView(tvSkip);
        bar.addView(dotsLayout);
        bar.addView(btnNext);

        return bar;
    }

    // --- STEP BUILDERS ---

    // ====================================================================
    // RESTORED INTRO SCREEN 01 — MOTIVATION
    // ====================================================================
    // ====================================================================
    // RESTORED INTRO SCREEN 01 — TROPHY & MOTIVATION (Eato Reference)
    // ====================================================================
    private void buildIntroTrophySlide() {
        float density = getResources().getDisplayMetrics().density;

        LinearLayout layout = createVerticalContainer();
        layout.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView tvInd = new TextView(this);
        tvInd.setText("01 / 03");
        tvInd.setTextSize(13);
        tvInd.setTypeface(null, Typeface.BOLD);
        tvInd.setTextColor(Color.parseColor("#94A3B8"));
        tvInd.setGravity(Gravity.CENTER_HORIZONTAL);
        tvInd.setPadding(0, (int)(4 * density), 0, (int)(8 * density));
        layout.addView(tvInd);

        addLargeAnimatedIllustration(layout, R.drawable.ic_intro_trophy, 260, 260);

        TextView tvTitle = createHeaderTitle("Your Stronger Self Starts Here");
        tvTitle.setTextSize(26);
        tvTitle.setGravity(Gravity.CENTER);
        tvTitle.setPadding(0, (int)(12 * density), 0, (int)(8 * density));

        TextView tvSub = createSubTitle("Let's build healthy habits, move with confidence, and become the best version of you.");
        tvSub.setGravity(Gravity.CENTER);
        tvSub.setTextSize(14);
        tvSub.setTextColor(Color.parseColor("#475569"));
        tvSub.setPadding(0, 0, 0, (int)(16 * density));

        layout.addView(tvTitle);
        layout.addView(tvSub);

        View navBar = createIntroBottomNav(0, "Next →", v -> {
            currentStep = 1;
            renderStep(1);
        });
        layout.addView(navBar);

        stepContainer.addView(layout);
    }

    // ====================================================================
    // RESTORED INTRO SCREEN 02 — PERSONALIZATION (Eato Reference)
    // ====================================================================
    private void buildIntroComparisonSlide() {
        float density = getResources().getDisplayMetrics().density;

        LinearLayout layout = createVerticalContainer();
        layout.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView tvInd = new TextView(this);
        tvInd.setText("02 / 03");
        tvInd.setTextSize(13);
        tvInd.setTypeface(null, Typeface.BOLD);
        tvInd.setTextColor(Color.parseColor("#94A3B8"));
        tvInd.setGravity(Gravity.CENTER_HORIZONTAL);
        tvInd.setPadding(0, (int)(4 * density), 0, (int)(8 * density));
        layout.addView(tvInd);

        addLargeAnimatedIllustration(layout, R.drawable.ic_intro_personalization, 260, 260);

        TextView tvTitle = createHeaderTitle("Workouts Made For You");
        tvTitle.setTextSize(26);
        tvTitle.setGravity(Gravity.CENTER);
        tvTitle.setPadding(0, (int)(12 * density), 0, (int)(8 * density));

        TextView tvSub = createSubTitle("Get personalized workouts designed around your goals, fitness level, and daily routine.");
        tvSub.setGravity(Gravity.CENTER);
        tvSub.setTextSize(14);
        tvSub.setTextColor(Color.parseColor("#475569"));
        tvSub.setPadding(0, 0, 0, (int)(16 * density));

        layout.addView(tvTitle);
        layout.addView(tvSub);

        View navBar = createIntroBottomNav(1, "Next →", v -> {
            currentStep = 2;
            renderStep(2);
        });
        layout.addView(navBar);

        stepContainer.addView(layout);
    }

    // ====================================================================
    // RESTORED INTRO SCREEN 03 — SATIETY & PROGRESS (Eato Reference)
    // ====================================================================
    // ====================================================================
    // INTRO SCREEN 03 — EXISTING ISSUES VS FITTRAIN SOLUTIONS
    // ====================================================================
    private void buildIntroSatietySlide() {
        float density = getResources().getDisplayMetrics().density;

        LinearLayout layout = createVerticalContainer();
        layout.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView tvInd = new TextView(this);
        tvInd.setText("03 / 03");
        tvInd.setTextSize(13);
        tvInd.setTypeface(null, Typeface.BOLD);
        tvInd.setTextColor(Color.parseColor("#94A3B8"));
        tvInd.setGravity(Gravity.CENTER_HORIZONTAL);
        tvInd.setPadding(0, (int)(4 * density), 0, (int)(6 * density));
        layout.addView(tvInd);

        TextView tvTitle = createHeaderTitle("Existing Issues vs FitTrain Solutions");
        tvTitle.setTextSize(23);
        tvTitle.setGravity(Gravity.CENTER);
        tvTitle.setPadding(0, (int)(4 * density), 0, (int)(4 * density));

        TextView tvSub = createSubTitle("Discover why FitTrain is the smarter, more effective way to reach your goals.");
        tvSub.setGravity(Gravity.CENTER);
        tvSub.setTextSize(13);
        tvSub.setTextColor(Color.parseColor("#475569"));
        tvSub.setPadding(0, 0, 0, (int)(12 * density));

        layout.addView(tvTitle);
        layout.addView(tvSub);

        // Comparison Table Container Card
        LinearLayout tableCard = new LinearLayout(this);
        tableCard.setOrientation(LinearLayout.VERTICAL);
        tableCard.setPadding((int)(12 * density), (int)(12 * density), (int)(12 * density), (int)(12 * density));
        
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Color.WHITE);
        cardBg.setCornerRadius(16 * density);
        cardBg.setStroke((int)(1.5f * density), Color.parseColor("#BAE6FD"));
        tableCard.setBackground(cardBg);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, (int)(12 * density));
        tableCard.setLayoutParams(cardParams);

        // Column Headers (LEFT: Existing Issues | RIGHT: FitTrain Solutions)
        LinearLayout headerRow = new LinearLayout(this);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setPadding(0, 0, 0, (int)(8 * density));

        TextView tvColLeft = new TextView(this);
        tvColLeft.setText("❌ Existing Issues");
        tvColLeft.setTextSize(13);
        tvColLeft.setTypeface(null, Typeface.BOLD);
        tvColLeft.setTextColor(Color.parseColor("#EF4444")); // Crimson Red
        tvColLeft.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvColRight = new TextView(this);
        tvColRight.setText("⚡ FitTrain Solution");
        tvColRight.setTextSize(13);
        tvColRight.setTypeface(null, Typeface.BOLD);
        tvColRight.setTextColor(Color.parseColor("#0284C7")); // Ocean Blue
        tvColRight.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        headerRow.addView(tvColLeft);
        headerRow.addView(tvColRight);
        tableCard.addView(headerRow);

        // Divider
        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)(1 * density)));
        divider.setBackgroundColor(Color.parseColor("#E2E8F0"));
        tableCard.addView(divider);

        // Comparison Rows
        String[][] comparisons = {
                {"Generic one-size workout routines", "100% Tailored AI training plans"},
                {"Calorie & macro guesswork", "Automated smart meal tracking"},
                {"Risk of bad form & injury", "Real-time AI posture feedback"},
                {"Loss of motivation & burnout", "Dynamic streaks & progress rewards"}
        };

        for (String[] rowData : comparisons) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, (int)(8 * density), 0, (int)(8 * density));
            row.setGravity(Gravity.CENTER_VERTICAL);

            // Left Side Issue Box
            LinearLayout leftBox = new LinearLayout(this);
            leftBox.setOrientation(LinearLayout.HORIZONTAL);
            leftBox.setPadding((int)(8 * density), (int)(6 * density), (int)(8 * density), (int)(6 * density));
            GradientDrawable leftBg = new GradientDrawable();
            leftBg.setColor(Color.parseColor("#FEF2F2")); // Soft Light Red tint
            leftBg.setCornerRadius(8 * density);
            leftBox.setBackground(leftBg);
            LinearLayout.LayoutParams leftP = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            leftP.setMargins(0, 0, (int)(4 * density), 0);
            leftBox.setLayoutParams(leftP);

            TextView tvIssue = new TextView(this);
            tvIssue.setText(rowData[0]);
            tvIssue.setTextSize(11);
            tvIssue.setTextColor(Color.parseColor("#991B1B"));
            leftBox.addView(tvIssue);

            // Right Side Solution Box
            LinearLayout rightBox = new LinearLayout(this);
            rightBox.setOrientation(LinearLayout.HORIZONTAL);
            rightBox.setPadding((int)(8 * density), (int)(6 * density), (int)(8 * density), (int)(6 * density));
            GradientDrawable rightBg = new GradientDrawable();
            rightBg.setColor(Color.parseColor("#F0F9FF")); // Soft Ice Blue tint
            rightBg.setCornerRadius(8 * density);
            rightBox.setBackground(rightBg);
            LinearLayout.LayoutParams rightP = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            rightP.setMargins((int)(4 * density), 0, 0, 0);
            rightBox.setLayoutParams(rightP);

            TextView tvSol = new TextView(this);
            tvSol.setText(rowData[1]);
            tvSol.setTextSize(11);
            tvSol.setTypeface(null, Typeface.BOLD);
            tvSol.setTextColor(Color.parseColor("#0369A1"));
            rightBox.addView(tvSol);

            row.addView(leftBox);
            row.addView(rightBox);
            tableCard.addView(row);
        }

        layout.addView(tableCard);

        View navBar = createIntroBottomNav(2, "Get Started →", v -> {
            currentStep = 3; // Question 1
            renderStep(3);
        });
        layout.addView(navBar);

        stepContainer.addView(layout);
    }

    // ====================================================================
    // QUESTION 1 of 18 — NAME
    // ====================================================================
    private void buildNameStep() {
        float density = getResources().getDisplayMetrics().density;

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        layout.setPadding((int)(24 * density), (int)(12 * density), (int)(24 * density), (int)(24 * density));

        TextView tvGreeting = new TextView(this);
        tvGreeting.setText("👋 Hello!");
        tvGreeting.setTextSize(16);
        tvGreeting.setTypeface(null, Typeface.BOLD);
        tvGreeting.setTextColor(Color.parseColor("#0284C7"));
        tvGreeting.setPadding(0, 0, 0, (int)(4 * density));
        layout.addView(tvGreeting);

        TextView tvHeadline = new TextView(this);
        SpannableStringBuilder spanHeadline = new SpannableStringBuilder("What's your name?");
        spanHeadline.setSpan(new ForegroundColorSpan(Color.parseColor("#0F172A")), 0, 12, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spanHeadline.setSpan(new ForegroundColorSpan(Color.parseColor("#0284C7")), 12, 17, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spanHeadline.setSpan(new StyleSpan(Typeface.BOLD), 0, 17, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvHeadline.setText(spanHeadline);
        tvHeadline.setTextSize(26);
        tvHeadline.setPadding(0, 0, 0, (int)(6 * density));
        layout.addView(tvHeadline);

        TextView tvSub = new TextView(this);
        tvSub.setText("Let's get to know you better. Please enter your name to get started.");
        tvSub.setTextSize(14);
        tvSub.setTextColor(Color.parseColor("#64748B"));
        tvSub.setPadding(0, 0, 0, (int)(20 * density));
        layout.addView(tvSub);

        LinearLayout inputCard = new LinearLayout(this);
        inputCard.setOrientation(LinearLayout.HORIZONTAL);
        inputCard.setGravity(Gravity.CENTER_VERTICAL);
        inputCard.setPadding((int)(18 * density), (int)(14 * density), (int)(18 * density), (int)(14 * density));

        GradientDrawable inputBg = new GradientDrawable();
        inputBg.setCornerRadius(24 * density);
        inputBg.setColor(Color.parseColor("#F0F9FF"));
        inputBg.setStroke((int)(1.5f * density), Color.parseColor("#BAE6FD"));
        inputCard.setBackground(inputBg);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, (int)(20 * density));
        inputCard.setLayoutParams(cardParams);

        TextView tvIcon = new TextView(this);
        tvIcon.setText("👤");
        tvIcon.setTextSize(20);
        tvIcon.setPadding(0, 0, (int)(12 * density), 0);
        inputCard.addView(tvIcon);

        EditText etName = new EditText(this);
        etName.setHint("Enter your name");
        etName.setHintTextColor(Color.parseColor("#94A3B8"));
        etName.setText(name);
        etName.setTextSize(16);
        etName.setTextColor(Color.parseColor("#0F172A"));
        etName.setTypeface(null, Typeface.BOLD);
        etName.setBackground(null);
        etName.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        etName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                name = s.toString();
                updateNextButtonState();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        inputCard.addView(etName);
        layout.addView(inputCard);

        addLargeAnimatedIllustration(layout, R.drawable.ic_welcome_person, 220, 220);

        stepContainer.addView(layout);
    }

    // ====================================================================
    // QUESTION 2 of 18 — DATE OF BIRTH
    // ====================================================================
    private void buildDateOfBirthStep() {
        LinearLayout layout = createVerticalContainer();
        addBehindTheQuestionCard(layout, "BMR & Age Calculation", "Basal Metabolic Rate (BMR) changes with age. Selecting your date of birth helps FitTrain accurately calculate your daily caloric expenditure.");

        layout.addView(createHeaderTitle("What's your date of birth? 🎂"));
        layout.addView(createSubTitle("Select your birth date to calculate your age and metabolic baseline. 📅"));

        MaterialButton btnPicker = new MaterialButton(this);
        btnPicker.setText(dobString.isEmpty() ? "📅 Select your date of birth" : "📅 Date of Birth: " + dobString + " (" + age + " years)");
        btnPicker.setBackgroundTintList(android.content.res.ColorStateList.valueOf(dobString.isEmpty() ? 0xFFE2E8F0 : 0xFF0284C7));
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
                btnPicker.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF0284C7));
                btnPicker.setTextColor(0xFFFFFFFF);
                updateNextButtonState();
            }, y, m, d);
            dpd.show();
        });
        layout.addView(btnPicker);

        addLargeAnimatedIllustration(layout, R.drawable.ic_calendar_age, 220, 220);

        stepContainer.addView(layout);
    }

    // ====================================================================
    // QUESTION 3 of 18 — GENDER
    // ====================================================================
    private void buildGenderStep() {
        LinearLayout layout = createVerticalContainer();
        addBehindTheQuestionCard(layout, "Metabolic Formula", "Biological gender affects muscle distribution and basal metabolic formulas (Mifflin-St Jeor equation).");

        layout.addView(createHeaderTitle("What is your gender? 👤"));

        String[] displayGenders = {"Male 👦", "Female 👧", "Non-binary 🧑"};
        String[] rawGenders = {"Male", "Female", "Non-binary"};
        int[] icons = {R.drawable.ic_user, R.drawable.ic_gender, R.drawable.ic_name_badge};
        int[] colors = {0xFF0284C7, 0xFFEC4899, 0xFFF59E0B};

        for (int i = 0; i < displayGenders.length; i++) {
            final String g = rawGenders[i];
            final String display = displayGenders[i];
            View card = createOptionCard(display, icons[i], g.equalsIgnoreCase(gender), colors[i]);
            card.setOnClickListener(v -> {
                gender = g;
                animateSelection(card);
                renderStep(5);
            });
            layout.addView(card);
        }
        addLargeAnimatedIllustration(layout, R.drawable.ic_gender_avatars, 180, 180);
        stepContainer.addView(layout);
    }

    // ====================================================================
    // QUESTION 4 of 18 — HEIGHT
    // ====================================================================
    private void buildHeightStep() {
        LinearLayout layout = createVerticalContainer();
        addBehindTheQuestionCard(layout, "Body Metrics", "Height is required to compute Body Mass Index (BMI) and total energy expenditure.");

        layout.addView(createHeaderTitle("Your Height 📏"));

        LinearLayout unitSwitcher = new LinearLayout(this);
        unitSwitcher.setOrientation(LinearLayout.HORIZONTAL);
        unitSwitcher.setPadding(0, 0, 0, 16);

        MaterialButton btnCm = new MaterialButton(this);
        btnCm.setText("cm");
        btnCm.setBackgroundTintList(android.content.res.ColorStateList.valueOf(isMetric ? 0xFF0284C7 : 0xFFE2E8F0));
        btnCm.setTextColor(isMetric ? 0xFFFFFFFF : 0xFF64748B);

        MaterialButton btnFt = new MaterialButton(this);
        btnFt.setText("ft");
        btnFt.setBackgroundTintList(android.content.res.ColorStateList.valueOf(!isMetric ? 0xFF0284C7 : 0xFFE2E8F0));
        btnFt.setTextColor(!isMetric ? 0xFFFFFFFF : 0xFF64748B);

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        p.setMargins(4, 0, 4, 0);
        btnCm.setLayoutParams(p);
        btnFt.setLayoutParams(p);

        btnCm.setOnClickListener(v -> { isMetric = true; renderStep(6); });
        btnFt.setOnClickListener(v -> { isMetric = false; renderStep(6); });

        unitSwitcher.addView(btnFt);
        unitSwitcher.addView(btnCm);
        layout.addView(unitSwitcher);

        TextView tvVal = new TextView(this);
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

        addLargeAnimatedIllustration(layout, R.drawable.ic_height_ruler_full, 220, 220);

        stepContainer.addView(layout);
    }

    // ====================================================================
    // QUESTION 5 of 18 — WEIGHT
    // ====================================================================
    private void buildWeightStep() {
        LinearLayout layout = createVerticalContainer();
        addBehindTheQuestionCard(layout, "Weight Baseline", "Current weight establishes baseline caloric maintenance and hydration goals.");

        layout.addView(createHeaderTitle("What's your current weight? ⚖️"));

        LinearLayout unitSwitcher = new LinearLayout(this);
        unitSwitcher.setOrientation(LinearLayout.HORIZONTAL);

        MaterialButton btnKg = new MaterialButton(this);
        btnKg.setText("kg");
        btnKg.setBackgroundTintList(android.content.res.ColorStateList.valueOf(isMetric ? 0xFF0284C7 : 0xFFE2E8F0));
        btnKg.setTextColor(isMetric ? 0xFFFFFFFF : 0xFF64748B);

        MaterialButton btnLbs = new MaterialButton(this);
        btnLbs.setText("lbs");
        btnLbs.setBackgroundTintList(android.content.res.ColorStateList.valueOf(!isMetric ? 0xFF0284C7 : 0xFFE2E8F0));
        btnLbs.setTextColor(!isMetric ? 0xFFFFFFFF : 0xFF64748B);

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        p.setMargins(4, 0, 4, 0);
        btnKg.setLayoutParams(p);
        btnLbs.setLayoutParams(p);

        btnKg.setOnClickListener(v -> { isMetric = true; renderStep(7); });
        btnLbs.setOnClickListener(v -> { isMetric = false; renderStep(7); });

        unitSwitcher.addView(btnKg);
        unitSwitcher.addView(btnLbs);
        layout.addView(unitSwitcher);

        TextView tvVal = new TextView(this);
        tvVal.setText(isMetric ? String.format(Locale.getDefault(), "%.1f kg", weightKg) : String.format(Locale.getDefault(), "%.1f lbs", weightKg * 2.20462));
        tvVal.setTextSize(28);
        tvVal.setTypeface(null, Typeface.BOLD);
        tvVal.setTextColor(0xFF0F172A);
        tvVal.setGravity(Gravity.CENTER);
        tvVal.setPadding(0, 16, 0, 16);
        layout.addView(tvVal);

        SeekBar sbWeight = new SeekBar(this);
        sbWeight.setMax(150);
        sbWeight.setProgress((int) (weightKg - 30));
        sbWeight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                weightKg = 30 + progress;
                tvVal.setText(isMetric ? String.format(Locale.getDefault(), "%.1f kg", weightKg) : String.format(Locale.getDefault(), "%.1f lbs", weightKg * 2.20462));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        layout.addView(sbWeight);

        addLargeAnimatedIllustration(layout, R.drawable.ic_weight_scale_full, 220, 220);

        stepContainer.addView(layout);
    }

    // ====================================================================
    // QUESTION 6 of 18 — BLOOD GROUP
    // ====================================================================
    private void buildBloodGroupStep() {
        LinearLayout layout = createVerticalContainer();
        addBehindTheQuestionCard(layout, "Health Insights", "Blood group information helps FitTrain provide relevant nutritional recommendations and emergency profile data.");

        layout.addView(createHeaderTitle("Select your Blood Group 🩸"));

        String[] displayGroups = {"A+ 🩸", "A- 🩸", "B+ 🩸", "B- 🩸", "O+ 🩸", "O- 🩸", "AB+ 🩸", "AB- 🩸"};
        String[] rawGroups = {"A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"};

        for (int i = 0; i < displayGroups.length; i++) {
            final String raw = rawGroups[i];
            final String display = displayGroups[i];
            View card = createOptionCard(display, R.drawable.ic_blood_drop, raw.equalsIgnoreCase(bloodGroup), 0xFFEF4444);
            card.setOnClickListener(v -> {
                bloodGroup = raw;
                animateSelection(card);
                renderStep(8);
            });
            layout.addView(card);
        }

        addLargeAnimatedIllustration(layout, R.drawable.ic_blood_compatibility, 200, 200);

        stepContainer.addView(layout);
    }

    // ====================================================================
    // QUESTION 7 of 18 — PRIMARY GOAL
    // ====================================================================
    private void buildGoalStep() {
        LinearLayout layout = createVerticalContainer();
        addBehindTheQuestionCard(layout, "Caloric Strategy", "Your primary goal determines whether FitTrain sets a calorie deficit, maintenance, or surplus target.");

        layout.addView(createHeaderTitle("What is your primary fitness goal? 🎯"));

        String[] displayGoals = {"Lose Weight 🔥", "Build Muscle 💪", "Maintain Weight ⚖️", "Increase Energy ⚡"};
        String[] rawGoals = {"Lose Weight", "Build Muscle", "Maintain Weight", "Increase Energy"};
        int[] icons = {R.drawable.ic_flame, R.drawable.ic_dumbbell, R.drawable.ic_weight_scale_full, R.drawable.ic_flash};
        int[] colors = {0xFFEF4444, 0xFF0284C7, 0xFF10B981, 0xFFF59E0B};

        for (int i = 0; i < displayGoals.length; i++) {
            final String raw = rawGoals[i];
            final String display = displayGoals[i];
            View card = createOptionCard(display, icons[i], raw.equalsIgnoreCase(goal), colors[i]);
            card.setOnClickListener(v -> {
                goal = raw;
                animateSelection(card);
                renderStep(9);
            });
            layout.addView(card);
        }

        addLargeAnimatedIllustration(layout, R.drawable.ic_flame, 180, 180);

        stepContainer.addView(layout);
    }

    // ====================================================================
    // QUESTION 8 of 18 — TARGET WEIGHT
    // ====================================================================
    private void buildTargetWeightStep() {
        LinearLayout layout = createVerticalContainer();
        addBehindTheQuestionCard(layout, "Target Milestone", "Setting a target weight enables FitTrain to estimate your project timeline and caloric budget.");

        layout.addView(createHeaderTitle("What's your target weight? 🎯"));

        TextView tvVal = new TextView(this);
        tvVal.setText(isMetric ? String.format(Locale.getDefault(), "%.1f kg", targetWeightKg) : String.format(Locale.getDefault(), "%.1f lbs", targetWeightKg * 2.20462));
        tvVal.setTextSize(28);
        tvVal.setTypeface(null, Typeface.BOLD);
        tvVal.setTextColor(0xFF0F172A);
        tvVal.setGravity(Gravity.CENTER);
        tvVal.setPadding(0, 16, 0, 16);
        layout.addView(tvVal);

        SeekBar sbTarget = new SeekBar(this);
        sbTarget.setMax(150);
        sbTarget.setProgress((int) (targetWeightKg - 30));
        sbTarget.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                targetWeightKg = 30 + progress;
                tvVal.setText(isMetric ? String.format(Locale.getDefault(), "%.1f kg", targetWeightKg) : String.format(Locale.getDefault(), "%.1f lbs", targetWeightKg * 2.20462));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        layout.addView(sbTarget);

        addLargeAnimatedIllustration(layout, R.drawable.ic_target_pace, 220, 220);

        stepContainer.addView(layout);
    }

    // ====================================================================
    // QUESTION 9 of 18 — TARGET PACE
    // ====================================================================
    private void buildTargetPaceStep() {
        LinearLayout layout = createVerticalContainer();
        addBehindTheQuestionCard(layout, "Weight Change Speed", "Choosing a sustainable pace ensures safe progression without burnout or loss of muscle mass.");

        layout.addView(createHeaderTitle("How fast do you want to reach your goal? ⏱️"));

        String[] displayPaces = {
                "Steady Pace (0.25 kg/wk) 🐢",
                "Balanced Pace (0.5 kg/wk - Recommended) ⚖️",
                "Fast Pace (0.75 kg/wk) 🚀",
                "Intensive Pace (1.0 kg/wk) 🔥"
        };
        String[] rawPaces = {"Steady", "Balanced", "Fast", "Intensive"};
        int[] icons = {R.drawable.ic_track_progress, R.drawable.ic_weight_scale_full, R.drawable.ic_flash, R.drawable.ic_flame};

        for (int i = 0; i < displayPaces.length; i++) {
            final String raw = rawPaces[i];
            final String display = displayPaces[i];
            View card = createOptionCard(display, icons[i], raw.equalsIgnoreCase(targetPace), 0xFF0284C7);
            card.setOnClickListener(v -> {
                targetPace = raw;
                animateSelection(card);
                renderStep(11);
            });
            layout.addView(card);
        }

        stepContainer.addView(layout);
    }

    // ====================================================================
    // QUESTION 10 of 18 — EATING ENVIRONMENT
    // ====================================================================
    private void buildEatingEnvironmentsStep() {
        LinearLayout layout = createVerticalContainer();
        addBehindTheQuestionCard(layout, "Meal Customization", "Your eating environment guides FitTrain's recipe complexity and meal suggestions.");

        layout.addView(createHeaderTitle("Where do you usually eat? 🍽️"));

        String[] displayEnv = {"Home Cooked 🏠", "Restaurants 🍽️", "Meal Prep 📦", "On the Go 🚗"};
        String[] rawEnv = {"Home Cooked", "Restaurants", "Meal Prep", "On the Go"};
        int[] icons = {R.drawable.ic_home, R.drawable.ic_fork_knife, R.drawable.ic_diet_plate, R.drawable.ic_activity_runner};

        for (int i = 0; i < displayEnv.length; i++) {
            final String raw = rawEnv[i];
            final String display = displayEnv[i];
            View card = createOptionCard(display, icons[i], raw.equalsIgnoreCase(eatingEnvironment), 0xFF0284C7);
            card.setOnClickListener(v -> {
                eatingEnvironment = raw;
                animateSelection(card);
                renderStep(12);
            });
            layout.addView(card);
        }

        stepContainer.addView(layout);
    }

    // ====================================================================
    // QUESTION 11 of 18 — MEALS PER DAY
    // ====================================================================
    private void buildMealsPerDayStep() {
        LinearLayout layout = createVerticalContainer();
        addBehindTheQuestionCard(layout, "Macro Distribution", "FitTrain splits your total daily calories and macronutrients based on your meal frequency.");

        layout.addView(createHeaderTitle("How many meals do you eat per day? 🥩"));

        String[] displayMeals = {"2 Meals 🥩", "3 Meals 🥗", "4+ Meals 🥬", "Intermittent Fasting ⏱️"};
        int[] mealVals = {2, 3, 4, 1};

        for (int i = 0; i < displayMeals.length; i++) {
            final int val = mealVals[i];
            final String display = displayMeals[i];
            View card = createOptionCard(display, R.drawable.ic_meal_breakfast, mealsPerDay == val, 0xFF0284C7);
            card.setOnClickListener(v -> {
                mealsPerDay = val;
                animateSelection(card);
                renderStep(13);
            });
            layout.addView(card);
        }

        stepContainer.addView(layout);
    }

    // ====================================================================
    // QUESTION 12 of 18 — DIETARY PREFERENCE
    // ====================================================================
    private void buildDietStyleStep() {
        LinearLayout layout = createVerticalContainer();
        addBehindTheQuestionCard(layout, "Dietary Alignment", "Selected preferences automatically filter recipe recommendations across your daily diet plan.");

        layout.addView(createHeaderTitle("Choose your Dietary Preference 🥗"));

        String[] displayStyle = {"Anything / Balanced 🍳", "Vegetarian 🥬", "Vegan 🥑", "Keto / Low Carb 🥩", "High Protein 🍗"};
        String[] rawStyle = {"Anything", "Vegetarian", "Vegan", "Keto", "High Protein"};

        for (int i = 0; i < displayStyle.length; i++) {
            final String raw = rawStyle[i];
            final String display = displayStyle[i];
            View card = createOptionCard(display, R.drawable.ic_diet, raw.equalsIgnoreCase(dietaryPreference), 0xFF10B981);
            card.setOnClickListener(v -> {
                dietaryPreference = raw;
                animateSelection(card);
                renderStep(14);
            });
            layout.addView(card);
        }

        stepContainer.addView(layout);
    }

    // ====================================================================
    // QUESTION 13 of 18 — HEALTH CONCERNS
    // ====================================================================
    private void buildHealthConcernsStep() {
        LinearLayout layout = createVerticalContainer();
        addBehindTheQuestionCard(layout, "Safety First", "FitTrain adapts workout intensity and exercise types around known health conditions.");

        layout.addView(createHeaderTitle("Any Health Concerns or Conditions? 🩸"));

        String[] displayConcerns = {"None 👍", "Diabetes 💉", "Hypertension 🩸", "Joint Pain 🦴", "Heart Health ❤️"};
        String[] rawConcerns = {"None", "Diabetes", "Hypertension", "Joint Pain", "Heart Health"};

        for (int i = 0; i < displayConcerns.length; i++) {
            final String raw = rawConcerns[i];
            final String display = displayConcerns[i];
            boolean selected = selectedHealthConcerns.contains(raw);
            View card = createOptionCard(display, R.drawable.ic_health_shield, selected, 0xFFEF4444);
            card.setOnClickListener(v -> {
                if (raw.equals("None")) {
                    selectedHealthConcerns.clear();
                    selectedHealthConcerns.add("None");
                } else {
                    selectedHealthConcerns.remove("None");
                    if (selected) {
                        selectedHealthConcerns.remove(raw);
                    } else {
                        selectedHealthConcerns.add(raw);
                    }
                }
                animateSelection(card);
                renderStep(15);
            });
            layout.addView(card);
        }

        stepContainer.addView(layout);
    }

    // ====================================================================
    // QUESTION 14 of 18 — ACTIVITY LEVEL
    // ====================================================================
    private void buildActivityStep() {
        LinearLayout layout = createVerticalContainer();
        addBehindTheQuestionCard(layout, "TDEE Multiplier", "Daily activity outside workouts determines Total Daily Energy Expenditure (TDEE).");

        layout.addView(createHeaderTitle("What's your daily activity level? 🏃"));

        String[] displayLevels = {"Sedentary (Desk Job) 🪑", "Lightly Active 🚶", "Moderately Active 🏃", "Very Active 🏋️"};
        String[] rawLevels = {"Sedentary", "Lightly Active", "Moderately Active", "Very Active"};

        for (int i = 0; i < displayLevels.length; i++) {
            final String raw = rawLevels[i];
            final String display = displayLevels[i];
            View card = createOptionCard(display, R.drawable.ic_activity_runner, raw.equalsIgnoreCase(activityLevel), 0xFFF59E0B);
            card.setOnClickListener(v -> {
                activityLevel = raw;
                animateSelection(card);
                renderStep(16);
            });
            layout.addView(card);
        }

        stepContainer.addView(layout);
    }

    // ====================================================================
    // QUESTION 15 of 18 — FITNESS EXPERIENCE
    // ====================================================================
    private void buildExperienceStep() {
        LinearLayout layout = createVerticalContainer();
        addBehindTheQuestionCard(layout, "Workout Complexity", "Experience level scales exercise complexity, set volume, and rest periods.");

        layout.addView(createHeaderTitle("What's your fitness experience? 💪"));

        String[] displayExp = {"Beginner 🌱", "Intermediate ⚡", "Advanced 🏆"};
        String[] rawExp = {"Beginner", "Intermediate", "Advanced"};

        for (int i = 0; i < displayExp.length; i++) {
            final String raw = rawExp[i];
            final String display = displayExp[i];
            View card = createOptionCard(display, R.drawable.ic_dumbbell, raw.equalsIgnoreCase(fitnessExperience), 0xFF0284C7);
            card.setOnClickListener(v -> {
                fitnessExperience = raw;
                animateSelection(card);
                renderStep(17);
            });
            layout.addView(card);
        }

        stepContainer.addView(layout);
    }

    // ====================================================================
    // QUESTION 16 of 18 — WORKOUT LOCATION & EQUIPMENT
    // ====================================================================
    private void buildWorkoutPreferencesStep() {
        LinearLayout layout = createVerticalContainer();
        addBehindTheQuestionCard(layout, "Equipment Matching", "Workouts are generated using only equipment you actually have available.");

        layout.addView(createHeaderTitle("Where do you prefer to workout? 🏋️"));

        String[] displayLoc = {"At Home 🏠", "At the Gym 🏋️", "Outdoor / Running 🏃"};
        String[] rawLoc = {"At Home", "At the Gym", "Outdoor"};

        for (int i = 0; i < displayLoc.length; i++) {
            final String raw = rawLoc[i];
            final String display = displayLoc[i];
            View card = createOptionCard(display, R.drawable.ic_home, raw.equalsIgnoreCase(workoutLocation), 0xFF0284C7);
            card.setOnClickListener(v -> {
                workoutLocation = raw;
                animateSelection(card);
                renderStep(18);
            });
            layout.addView(card);
        }

        stepContainer.addView(layout);
    }

    // ====================================================================
    // QUESTION 17 of 18 — WORKOUT SCHEDULE (Duration & Days per week)
    // ====================================================================
    private void buildWorkoutScheduleStep() {
        LinearLayout layout = createVerticalContainer();
        addBehindTheQuestionCard(layout, "Schedule Optimization", "FitTrain structures your weekly workout split according to your time availability.");

        layout.addView(createHeaderTitle("How often do you plan to workout? 📅"));

        layout.addView(createSubTitle("Preferred session duration:"));
        int[] durations = {15, 30, 45, 60};
        String[] displayDurations = {"15 min ⏱️", "30 min ⚡", "45 min 💪", "60 min 🏋️"};

        LinearLayout durContainer = new LinearLayout(this);
        durContainer.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < durations.length; i++) {
            final int d = durations[i];
            MaterialButton btn = new MaterialButton(this);
            btn.setText(displayDurations[i]);
            btn.setTextSize(12);
            btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(workoutDuration == d ? 0xFF0284C7 : 0xFFE2E8F0));
            btn.setTextColor(workoutDuration == d ? 0xFFFFFFFF : 0xFF475569);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            p.setMargins(4, 0, 4, 12);
            btn.setLayoutParams(p);
            btn.setOnClickListener(v -> {
                workoutDuration = d;
                renderStep(19);
            });
            durContainer.addView(btn);
        }
        layout.addView(durContainer);

        layout.addView(createSubTitle("Days per week:"));
        int[] daysList = {2, 3, 4, 5, 6};
        for (int days : daysList) {
            String title = days + " Days / Week 📆";
            View card = createOptionCard(title, R.drawable.ic_calendar_age, workoutDaysPerWeek == days, 0xFF0284C7);
            card.setOnClickListener(v -> {
                workoutDaysPerWeek = days;
                animateSelection(card);
                renderStep(19);
            });
            layout.addView(card);
        }

        stepContainer.addView(layout);
    }

    // ====================================================================
    // QUESTION 18 of 18 — PREFERRED WORKOUT TIME
    // ====================================================================
    private void buildWorkoutTimeStep() {
        LinearLayout layout = createVerticalContainer();
        addBehindTheQuestionCard(layout, "Smart Reminders", "Setting your workout time allows FitTrain to send timely workout notifications.");

        layout.addView(createHeaderTitle("What's your preferred workout time? ⏰"));

        String[] displayTimes = {"Early Morning 🌅", "Morning ☀️", "Afternoon 🌤️", "Evening 🌇", "Night 🌙"};
        String[] rawTimes = {"Early Morning", "Morning", "Afternoon", "Evening", "Night"};

        for (int i = 0; i < displayTimes.length; i++) {
            final String raw = rawTimes[i];
            final String display = displayTimes[i];
            View card = createOptionCard(display, R.drawable.ic_track_progress, raw.equalsIgnoreCase(preferredWorkoutTime), 0xFF0284C7);
            card.setOnClickListener(v -> {
                preferredWorkoutTime = raw;
                animateSelection(card);
                renderStep(20);
            });
            layout.addView(card);
        }

        stepContainer.addView(layout);
    }

    // ====================================================================
    // DEDICATED PROFILE SETUP STEP (Photo Upload & Non-blocking Skip)
    // ====================================================================
    private void buildProfileSetupStep() {
        float density = getResources().getDisplayMetrics().density;

        LinearLayout layout = createVerticalContainer();
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setPadding((int)(20 * density), (int)(16 * density), (int)(20 * density), (int)(24 * density));

        TextView tvTitle = createHeaderTitle("Set Up Your Profile Photo 📸");
        tvTitle.setGravity(Gravity.CENTER);
        tvTitle.setTextSize(24);

        TextView tvSub = createSubTitle("Add a profile photo to personalize your fitness dashboard. You can also skip this step.");
        tvSub.setGravity(Gravity.CENTER);
        tvSub.setPadding(0, 0, 0, (int)(20 * density));

        layout.addView(tvTitle);
        layout.addView(tvSub);

        // Circular Avatar Container
        ImageView ivAvatar = new ImageView(this);
        int avatarSize = (int)(140 * density);
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(avatarSize, avatarSize);
        avatarParams.gravity = Gravity.CENTER_HORIZONTAL;
        avatarParams.setMargins(0, (int)(12 * density), 0, (int)(20 * density));
        ivAvatar.setLayoutParams(avatarParams);

        GradientDrawable avatarBg = new GradientDrawable();
        avatarBg.setShape(GradientDrawable.OVAL);
        avatarBg.setColor(Color.parseColor("#F0F9FF"));
        avatarBg.setStroke((int)(3 * density), Color.parseColor("#0284C7"));
        ivAvatar.setBackground(avatarBg);
        ivAvatar.setPadding((int)(4 * density), (int)(4 * density), (int)(4 * density), (int)(4 * density));

        if (!selectedProfileImageUri.isEmpty()) {
            Glide.with(this)
                    .load(Uri.parse(selectedProfileImageUri))
                    .circleCrop()
                    .placeholder(R.drawable.ic_user)
                    .into(ivAvatar);
        } else {
            ivAvatar.setImageResource(R.drawable.ic_user);
        }

        layout.addView(ivAvatar);

        // "Add Profile Photo" / "Edit Photo" Button
        MaterialButton btnPhotoAction = new MaterialButton(this);
        btnPhotoAction.setText(selectedProfileImageUri.isEmpty() ? "📷 Add Profile Photo" : "✏️ Edit Photo");
        btnPhotoAction.setTextSize(15);
        btnPhotoAction.setTypeface(null, Typeface.BOLD);
        btnPhotoAction.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#0284C7")));
        btnPhotoAction.setTextColor(Color.WHITE);
        btnPhotoAction.setCornerRadius((int)(20 * density));
        btnPhotoAction.setPadding((int)(24 * density), (int)(12 * density), (int)(24 * density), (int)(12 * density));

        btnPhotoAction.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        layout.addView(btnPhotoAction);

        // Prominent, Non-blocking Skip Action Button
        MaterialButton btnSkipPhoto = new MaterialButton(this);
        btnSkipPhoto.setText("Skip for Now");
        btnSkipPhoto.setTextSize(14);
        btnSkipPhoto.setTypeface(null, Typeface.NORMAL);
        btnSkipPhoto.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.TRANSPARENT));
        btnSkipPhoto.setTextColor(Color.parseColor("#64748B"));
        btnSkipPhoto.setRippleColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#E2E8F0")));
        
        LinearLayout.LayoutParams skipP = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        skipP.setMargins(0, (int)(12 * density), 0, 0);
        btnSkipPhoto.setLayoutParams(skipP);

        btnSkipPhoto.setOnClickListener(v -> {
            selectedProfileImageUri = "";
            currentStep = 22;
            renderStep(22);
        });

        layout.addView(btnSkipPhoto);

        stepContainer.addView(layout);
    }

    private void buildPlanGenerationStep() {
        LinearLayout layout = createVerticalContainer();
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setPadding(20, 40, 20, 40);

        TextView tvTitle = createHeaderTitle("Building Your Custom FitTrain Plan... 🤖");
        tvTitle.setGravity(Gravity.CENTER);
        layout.addView(tvTitle);

        ProgressBar pb = new ProgressBar(this);
        pb.setIndeterminate(true);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(80, 80);
        p.setMargins(0, 32, 0, 32);
        pb.setLayoutParams(p);
        layout.addView(pb);

        TextView tvStatus = new TextView(this);
        tvStatus.setText("Calculating BMR, TDEE & Custom Macros...");
        tvStatus.setTextSize(14);
        tvStatus.setTextColor(Color.parseColor("#0284C7"));
        tvStatus.setGravity(Gravity.CENTER);
        layout.addView(tvStatus);

        stepContainer.addView(layout);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            currentStep = 23;
            renderStep(23);
        }, 1800);
    }

    private void buildPersonalizedWelcomeStep() {
        LinearLayout layout = createVerticalContainer();
        layout.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView tvTitle = createHeaderTitle("🎉 You're All Set, " + (name.isEmpty() ? "Champion" : name) + "!");
        tvTitle.setGravity(Gravity.CENTER);

        TextView tvSub = createSubTitle("Your personalized workout routine and custom meal plan have been created.");
        tvSub.setGravity(Gravity.CENTER);

        layout.addView(tvTitle);
        layout.addView(tvSub);

        addLargeAnimatedIllustration(layout, R.drawable.ic_intro_motivation, 220, 220);

        stepContainer.addView(layout);
    }

    private void finishOnboardingAndLaunchHome() {
        // Build & Save User Data to Local DB & Firestore
        User user = new User();
        user.setName(name != null && !name.trim().isEmpty() ? name : "Athlete");
        user.setAge(age > 0 ? age : (dobString != null && !dobString.isEmpty() ? com.fitness.app.utils.ValidationUtils.calculateAge(dobString) : 25));
        user.setDob(dobString != null ? dobString : "");
        user.setGender(gender != null && !gender.isEmpty() ? gender : "Male");
        user.setHeight(heightCm > 0 ? heightCm : 170.0);
        user.setWeight(weightKg > 0 ? weightKg : 70.0);
        user.setBloodGroup(bloodGroup != null ? bloodGroup : "");
        user.setGoal(goal != null && !goal.isEmpty() ? goal : "Lose Weight");
        user.setTargetWeight(targetWeightKg > 0 ? targetWeightKg : (weightKg > 0 ? weightKg - 5 : 65.0));
        user.setTargetPace(targetPace != null ? targetPace : "Balanced");
        user.setEatingEnvironment(eatingEnvironment != null ? eatingEnvironment : "Home Cooked");
        user.setMealsPerDay(mealsPerDay > 0 ? mealsPerDay : 3);
        user.setDietaryPreference(dietaryPreference != null && !dietaryPreference.isEmpty() ? dietaryPreference : "Balanced");
        user.setActivityLevel(activityLevel != null && !activityLevel.isEmpty() ? activityLevel : "Moderately Active");
        user.setFitnessExperience(fitnessExperience != null && !fitnessExperience.isEmpty() ? fitnessExperience : "Beginner");
        user.setWorkoutLocation(workoutLocation != null && !workoutLocation.isEmpty() ? workoutLocation : "At Home");
        user.setWorkoutDuration(workoutDuration > 0 ? workoutDuration : 30);
        user.setWorkoutDaysPerWeek(workoutDaysPerWeek > 0 ? workoutDaysPerWeek : 4);
        user.setPreferredWorkoutTime(preferredWorkoutTime != null && !preferredWorkoutTime.isEmpty() ? preferredWorkoutTime : "Morning");

        if (selectedProfileImageUri != null && !selectedProfileImageUri.isEmpty()) {
            user.setProfileImageUrl(selectedProfileImageUri);
        }

        StringBuilder medBuilder = new StringBuilder();
        if (selectedHealthConcerns != null && !selectedHealthConcerns.isEmpty()) {
            for (String concern : selectedHealthConcerns) {
                if (medBuilder.length() > 0) medBuilder.append(", ");
                medBuilder.append(concern);
            }
        }
        user.setMedicalConditions(medBuilder.toString().isEmpty() ? "None" : medBuilder.toString());

        if (selectedEquipment != null && !selectedEquipment.isEmpty()) {
            StringBuilder eqBuilder = new StringBuilder();
            for (String eq : selectedEquipment) {
                if (eqBuilder.length() > 0) eqBuilder.append(", ");
                eqBuilder.append(eq);
            }
            user.setAvailableEquipment(eqBuilder.toString());
        } else {
            user.setAvailableEquipment("Bodyweight");
        }

        user.setProfileCompleted(true);

        localDb.saveUser(user);
        localDb.setOnboardingCompleted(true);

        Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

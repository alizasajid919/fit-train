package com.fitness.app.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.fitness.app.R;
import com.fitness.app.data.local.LocalDataManager;

public class MindfulnessActivity extends AppCompatActivity {

    private View viewBreathingCircle;
    private TextView tvBreathingState, tvBreathingDesc, tvAffirmationQuote;
    private Button btnStartBreathing;

    private boolean isBreathingActive = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable breathingRunnable;
    private ValueAnimator circleAnimator;

    private enum BreathingState {
        INHALE, HOLD_IN, EXHALE, HOLD_OUT
    }
    private BreathingState currentState = BreathingState.HOLD_OUT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mindfulness);

        // Bind views
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        viewBreathingCircle = findViewById(R.id.viewBreathingCircle);
        tvBreathingState = findViewById(R.id.tvBreathingState);
        tvBreathingDesc = findViewById(R.id.tvBreathingDesc);
        tvAffirmationQuote = findViewById(R.id.tvAffirmationQuote);
        btnStartBreathing = findViewById(R.id.btnStartBreathing);

        // Load dynamic self-care affirmation quote
        LocalDataManager localDb = new LocalDataManager(this);
        String gender = localDb.getUser() != null ? localDb.getUser().getGender() : "Other";
        tvAffirmationQuote.setText(getRandomQuote(gender));

        btnStartBreathing.setOnClickListener(v -> {
            if (isBreathingActive) {
                stopBreathingSession();
            } else {
                startBreathingSession();
            }
        });
    }

    private void startBreathingSession() {
        isBreathingActive = true;
        btnStartBreathing.setText("Stop Session");
        btnStartBreathing.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFF44336)); // Red
        tvBreathingDesc.setText("Synchronize your breath with the visualizer circle");

        currentState = BreathingState.HOLD_OUT;
        runBreathingCycle();
    }

    private void stopBreathingSession() {
        isBreathingActive = false;
        btnStartBreathing.setText("Start Session");
        btnStartBreathing.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.primary)));
        tvBreathingDesc.setText("Click Start and synchronize your breath with the circle");
        tvBreathingState.setText("Relax");

        // Cancel animations and callbacks
        if (handler != null && breathingRunnable != null) {
            handler.removeCallbacks(breathingRunnable);
        }
        if (circleAnimator != null) {
            circleAnimator.cancel();
        }

        // Reset circle scale
        viewBreathingCircle.animate().scaleX(1.0f).scaleY(1.0f).setDuration(400).start();
    }

    private void runBreathingCycle() {
        if (!isBreathingActive) return;

        // Determine next state
        switch (currentState) {
            case HOLD_OUT:
                currentState = BreathingState.INHALE;
                tvBreathingState.setText("Inhale");
                animateCircleScale(1.0f, 1.8f, 4000); // 4 seconds inhale
                scheduleNextCycleState(4000);
                break;
            case INHALE:
                currentState = BreathingState.HOLD_IN;
                tvBreathingState.setText("Hold");
                scheduleNextCycleState(4000); // 4 seconds hold
                break;
            case HOLD_IN:
                currentState = BreathingState.EXHALE;
                tvBreathingState.setText("Exhale");
                animateCircleScale(1.8f, 1.0f, 4000); // 4 seconds exhale
                scheduleNextCycleState(4000);
                break;
            case EXHALE:
                currentState = BreathingState.HOLD_OUT;
                tvBreathingState.setText("Hold");
                scheduleNextCycleState(4000); // 4 seconds hold
                break;
        }
    }

    private void animateCircleScale(float from, float to, long duration) {
        if (circleAnimator != null) {
            circleAnimator.cancel();
        }

        circleAnimator = ValueAnimator.ofFloat(from, to);
        circleAnimator.setDuration(duration);
        circleAnimator.setInterpolator(new DecelerateInterpolator());
        circleAnimator.addUpdateListener(animation -> {
            float scale = (float) animation.getAnimatedValue();
            viewBreathingCircle.setScaleX(scale);
            viewBreathingCircle.setScaleY(scale);
        });
        circleAnimator.start();
    }

    private void scheduleNextCycleState(long delay) {
        breathingRunnable = this::runBreathingCycle;
        handler.postDelayed(breathingRunnable, delay);
    }

    private String getRandomQuote(String gender) {
        String[] generalQuotes = {
            "“Breathe in strength, breathe out stress. You are doing beautiful work, be gentle with yourself today.”",
            "“Your worth is not defined by comparison. You are unique, valuable, and strong.”",
            "“Peace is a daily practice. Start with this breath, and let go of external pressure.”",
            "“Your body is your home. Fill it with kindness, strength, and deep calming breaths.”",
            "“Taking care of your mind is just as important as training your body. Rest is progress.”"
        };

        String[] femaleQuotes = {
            "“You are strong, confident, and beautiful exactly as you are. Be proud of your unique path.”",
            "“Focus on self-love, energy, and peace of mind today. You deserve kindness from yourself.”",
            "“Speak gently to your body today. It carries your strength, your spirit, and your dreams.”",
            "“You are enough. No scale, no comparison, and no standard can define your incredible value.”",
            "“Let go of standard expectations. Breathe in peace and celebrate the powerful person you are becoming.”"
        };

        if ("Female".equalsIgnoreCase(gender)) {
            int index = (int) (Math.random() * femaleQuotes.length);
            return femaleQuotes[index];
        } else {
            int index = (int) (Math.random() * generalQuotes.length);
            return generalQuotes[index];
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isBreathingActive) {
            stopBreathingSession();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && breathingRunnable != null) {
            handler.removeCallbacks(breathingRunnable);
        }
    }
}

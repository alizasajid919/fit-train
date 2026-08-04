package com.fitness.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.fitness.app.R;
import com.fitness.app.adapters.OnboardingAdapter;
import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.models.User;
import com.fitness.app.viewmodels.AuthViewModel;
import com.fitness.app.utils.SharedPreferencesManager;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private LinearLayout layoutDots;
    private LocalDataManager localDb;
    private com.fitness.app.viewmodels.AuthViewModel authViewModel;
    private int dotsCount;
    private ImageView[] dots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        localDb = new LocalDataManager(this);
        authViewModel = new androidx.lifecycle.ViewModelProvider(this).get(com.fitness.app.viewmodels.AuthViewModel.class);
        viewPager = findViewById(R.id.viewPagerOnboarding);
        layoutDots = findViewById(R.id.layoutDots);

        // Setup Onboarding Items
        List<OnboardingAdapter.OnboardingItem> items = new ArrayList<>();
        items.add(new OnboardingAdapter.OnboardingItem(
                R.drawable.onboarding_1,
                getString(R.string.onboarding_title_1),
                getString(R.string.onboarding_desc_1)
        ));
        items.add(new OnboardingAdapter.OnboardingItem(
                R.drawable.onboarding_2,
                getString(R.string.onboarding_title_2),
                getString(R.string.onboarding_desc_2)
        ));
        items.add(new OnboardingAdapter.OnboardingItem(
                R.drawable.onboarding_3,
                getString(R.string.onboarding_title_3),
                getString(R.string.onboarding_desc_3)
        ));
        items.add(new OnboardingAdapter.OnboardingItem(
                R.drawable.onboarding_4,
                getString(R.string.onboarding_title_4),
                getString(R.string.onboarding_desc_4)
        ));

        OnboardingAdapter adapter = new OnboardingAdapter(items);
        viewPager.setAdapter(adapter);

        android.view.View tvSkip = findViewById(R.id.tvSkip);
        android.view.View fabNext = findViewById(R.id.fabNext);
        android.view.View btnGetStarted = findViewById(R.id.btnGetStarted);

        setupDotsIndicator(items.size());
        setCurrentDot(0);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                setCurrentDot(position);
                if (position == items.size() - 1) {
                    // Last screen
                    tvSkip.setVisibility(android.view.View.GONE);
                    fabNext.animate().alpha(0f).setDuration(200).withEndAction(() -> fabNext.setVisibility(android.view.View.GONE)).start();
                    btnGetStarted.setVisibility(android.view.View.VISIBLE);
                    btnGetStarted.setAlpha(0f);
                    btnGetStarted.animate().alpha(1f).setDuration(200).start();
                } else {
                    // Other screens
                    tvSkip.setVisibility(android.view.View.VISIBLE);
                    btnGetStarted.setVisibility(android.view.View.GONE);
                    fabNext.setVisibility(android.view.View.VISIBLE);
                    fabNext.setAlpha(1f);
                }
            }
        });

        fabNext.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();
            if (current < items.size() - 1) {
                viewPager.setCurrentItem(current + 1);
            } else {
                finishOnboarding();
            }
        });

        btnGetStarted.setOnClickListener(v -> finishOnboarding());

        tvSkip.setOnClickListener(v -> finishOnboarding());
    }

    private void setupDotsIndicator(int count) {
        dotsCount = count;
        dots = new ImageView[dotsCount];

        for (int i = 0; i < dotsCount; i++) {
            dots[i] = new ImageView(this);
            dots[i].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.dot_inactive));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            layoutDots.addView(dots[i], params);
        }
    }

    private void setCurrentDot(int position) {
        for (int i = 0; i < dotsCount; i++) {
            if (i == position) {
                dots[i].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.dot_active));
            } else {
                dots[i].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.dot_inactive));
            }
        }
    }

    private void finishOnboarding() {
        localDb.setOnboardingSeen(true);

        // Pre-create Guest Profile locally
        User guest = new User("guest_temp_id", "Guest", "User", "", System.currentTimeMillis());
        guest.setProfileCompleted(true); // Treat as completed for guest dashboard
        guest.setGender("Male");
        guest.setDob("2000-01-01");
        guest.setHeight(175.0);
        guest.setWeight(70.0);
        guest.setGoal("Improve Shape");
        guest.setActivityLevel("Active");
        localDb.saveUser(guest);

        // Silent Firebase Anonymous Login
        authViewModel.signInAnonymously(localDb).observe(this, resource -> {
            // Note: We navigate regardless of connection state so that offline users are never blocked!
            if (resource != null && resource.status != com.fitness.app.repositories.UserRepository.Resource.Status.LOADING) {
                startActivity(new Intent(OnboardingActivity.this, MainActivity.class));
                finish();
            }
        });
    }
}

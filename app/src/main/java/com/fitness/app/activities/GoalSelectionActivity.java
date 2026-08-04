package com.fitness.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.fitness.app.R;
import com.fitness.app.adapters.GoalAdapter;
import com.fitness.app.models.Goal;
import com.fitness.app.models.User;
import com.fitness.app.viewmodels.ProfileViewModel;

import java.util.ArrayList;
import java.util.List;

public class GoalSelectionActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private View progressOverlay;
    private ProfileViewModel profileViewModel;
    private User userProfile;
    private List<Goal> goalList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_selection);

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // Get User profile passed from CompleteProfileActivity
        userProfile = (User) getIntent().getSerializableExtra("user_profile");
        if (userProfile == null) {
            Toast.makeText(this, "Profile data lost. Navigating back.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        viewPager = findViewById(R.id.viewPagerGoals);
        progressOverlay = findViewById(R.id.progressOverlay);

        setupGoalsList();
        setupViewPager();

        findViewById(R.id.btnConfirm).setOnClickListener(v -> confirmGoalSelection());
    }

    private void setupGoalsList() {
        goalList = new ArrayList<>();
        goalList.add(new Goal(
                "Improve Shape",
                "I have a low amount of body fat and need / want to build more muscle",
                R.drawable.onboarding_1
        ));
        goalList.add(new Goal(
                "Lean & Tone",
                "I'm 'skinny fat', look thin but have no shape. I want to add lean muscle in the right way",
                R.drawable.onboarding_2
        ));
        goalList.add(new Goal(
                "Lose Fat",
                "I have over 20 lbs to lose. I want to drop all this fat and gain muscle mass",
                R.drawable.onboarding_3
        ));
        goalList.add(new Goal(
                "Gain Muscle",
                "I am skinny and want to bulk up, gain weight, and build solid dense muscle",
                R.drawable.onboarding_4
        ));
        goalList.add(new Goal(
                "Improve Fitness",
                "I want to increase my cardiovascular endurance, physical fitness, and general health",
                R.drawable.profile_complete
        ));
    }

    private void setupViewPager() {
        GoalAdapter adapter = new GoalAdapter(goalList);
        viewPager.setAdapter(adapter);

        // Styling ViewPager2 to show partial previews of left/right cards (Page Transformer)
        viewPager.setOffscreenPageLimit(3);
        
        float pageMarginPx = getResources().getDimensionPixelOffset(R.dimen.page_margin);
        float offsetPx = getResources().getDimensionPixelOffset(R.dimen.offset);
        
        viewPager.setPageTransformer((page, position) -> {
            float offset = position * -(2 * offsetPx + pageMarginPx);
            page.setTranslationX(offset);
            
            // Subtle scaling animation for the selected card
            float scale = 1 - (Math.abs(position) * 0.15f);
            page.setScaleY(scale);
            page.setScaleX(scale);
        });
    }

    private void confirmGoalSelection() {
        int selectedPosition = viewPager.getCurrentItem();
        Goal selectedGoal = goalList.get(selectedPosition);

        // Update profile
        userProfile.setGoal(selectedGoal.getTitle());
        userProfile.setProfileCompleted(true);

        // Write user profile to Firestore
        profileViewModel.updateUserProfile(userProfile).observe(this, resource -> {
            if (resource != null) {
                switch (resource.status) {
                    case LOADING:
                        progressOverlay.setVisibility(View.VISIBLE);
                        break;
                    case SUCCESS:
                        progressOverlay.setVisibility(View.GONE);
                        // Save profile locally
                        new com.fitness.app.data.local.LocalDataManager(GoalSelectionActivity.this).saveUser(userProfile);
                        // Redirect to Success Activity
                        Intent intent = new Intent(GoalSelectionActivity.this, SuccessActivity.class);
                        intent.putExtra("first_name", userProfile.getFirstName());
                        startActivity(intent);
                        finish();
                        break;
                    case ERROR:
                        progressOverlay.setVisibility(View.GONE);
                        Toast.makeText(this, "Profile sync failed: " + resource.message, Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });
    }
}

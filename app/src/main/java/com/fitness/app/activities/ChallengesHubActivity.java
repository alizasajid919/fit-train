package com.fitness.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fitness.app.R;
import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.data.room.AppDatabase;
import com.fitness.app.data.room.FitnessDao;
import com.fitness.app.models.FitnessChallenge;
import com.fitness.app.models.User;
import com.fitness.app.models.UserChallengeStats;
import com.google.android.material.chip.Chip;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChallengesHubActivity extends AppCompatActivity {

    private TextView tvUserLevel, tvUserXpLabel;
    private ProgressBar pbXpProgress;
    private TextView tvChallengeCoachAdvice;

    private Chip chipAll, chipActive, chipWeekly, chipMonthly, chipCompleted, chipUpcoming;
    private RecyclerView rvChallenges;
    private View layoutEmptyChallenges;

    private RecyclerView rvBadges;
    private BadgeAdapter badgeAdapter;

    private FitnessDao fitnessDao;
    private LocalDataManager localDb;
    private UserChallengeStats userStats;
    private final List<FitnessChallenge> allChallenges = new ArrayList<>();
    private final List<FitnessChallenge> filteredChallenges = new ArrayList<>();
    private ChallengeAdapter challengeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_challenges_hub);

        fitnessDao = AppDatabase.getInstance(this).fitnessDao();
        localDb = new LocalDataManager(this);

        // Bind Views
        tvUserLevel = findViewById(R.id.tvUserLevel);
        tvUserXpLabel = findViewById(R.id.tvUserXpLabel);
        pbXpProgress = findViewById(R.id.pbXpProgress);
        tvChallengeCoachAdvice = findViewById(R.id.tvChallengeCoachAdvice);

        chipAll = findViewById(R.id.chipAllChallenges);
        chipActive = findViewById(R.id.chipActiveChallenges);
        chipWeekly = findViewById(R.id.chipWeeklyChallenges);
        chipMonthly = findViewById(R.id.chipMonthlyChallenges);
        chipCompleted = findViewById(R.id.chipCompletedChallenges);
        chipUpcoming = findViewById(R.id.chipUpcomingChallenges);

        rvChallenges = findViewById(R.id.rvChallenges);
        layoutEmptyChallenges = findViewById(R.id.layoutEmptyChallenges);
        rvBadges = findViewById(R.id.rvBadges);

        rvChallenges.setLayoutManager(new LinearLayoutManager(this));
        rvBadges.setLayoutManager(new GridLayoutManager(this, 3));

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());

        // Bind Show All Challenges Button
        findViewById(R.id.btnShowAllChallenges).setOnClickListener(v -> {
            chipAll.setChecked(true);
            chipActive.setChecked(false);
            chipWeekly.setChecked(false);
            chipMonthly.setChecked(false);
            chipCompleted.setChecked(false);
            chipUpcoming.setChecked(false);
            applyFilters();
        });

        // Setup filter chip listeners
        setupFilterListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStatsAndChallenges();
    }

    private void setupFilterListeners() {
        View.OnClickListener clickListener = v -> {
            int id = v.getId();
            chipAll.setChecked(id == R.id.chipAllChallenges);
            chipActive.setChecked(id == R.id.chipActiveChallenges);
            chipWeekly.setChecked(id == R.id.chipWeeklyChallenges);
            chipMonthly.setChecked(id == R.id.chipMonthlyChallenges);
            chipCompleted.setChecked(id == R.id.chipCompletedChallenges);
            chipUpcoming.setChecked(id == R.id.chipUpcomingChallenges);
            applyFilters();
        };

        chipAll.setOnClickListener(clickListener);
        chipActive.setOnClickListener(clickListener);
        chipWeekly.setOnClickListener(clickListener);
        chipMonthly.setOnClickListener(clickListener);
        chipCompleted.setOnClickListener(clickListener);
        chipUpcoming.setOnClickListener(clickListener);
    }

    private void loadStatsAndChallenges() {
        new Thread(() -> {
            // Load stats
            userStats = fitnessDao.getUserChallengeStats();
            if (userStats == null) {
                userStats = new UserChallengeStats();
                fitnessDao.insertUserChallengeStats(userStats);
            }

            // Load challenges list
            List<FitnessChallenge> list = fitnessDao.getAllFitnessChallenges();
            if (list == null || list.isEmpty()) {
                seedDefaultChallenges();
                list = fitnessDao.getAllFitnessChallenges();
            }

            allChallenges.clear();
            if (list != null) {
                allChallenges.addAll(list);
            }

            runOnUiThread(() -> {
                updateStatsViews();
                applyFilters();
            });
        }).start();
    }

    private void seedDefaultChallenges() {
        long dayMs = 24L * 60 * 60 * 1000;
        long now = System.currentTimeMillis();

        // 1. 7-Day Active Health Starter (In Progress)
        FitnessChallenge active = new FitnessChallenge();
        active.setName("7-Day Active Health Starter");
        active.setDescription("Kickstart your fitness journey with simple, consistency-building habits designed for beginners.");
        active.setDifficulty("EASY");
        active.setDuration("WEEKLY");
        active.setXpReward(100);
        active.setBadgeName("Beginner");
        active.setStartDate(now - (2L * dayMs)); // started 2 days ago
        active.setEndDate(now + (5L * dayMs));
        active.setStatus("IN_PROGRESS");
        active.setProgressPercent(28);
        active.setDaysRemaining(5);
        active.setCaloriesEstimate(180);
        active.setEstimatedWorkoutTime(15);
        active.setHealthBenefits("• Improves daily metabolic rate.\n• Builds regular movement habits.\n• Increases oxygen flow and circulation.");
        active.setTasksJson("[\n" +
                "  {\"day\": \"Day 1\", \"name\": \"Walk 6,000 steps\", \"completed\": true},\n" +
                "  {\"day\": \"Day 2\", \"name\": \"Drink 3 liters of water\", \"completed\": true},\n" +
                "  {\"day\": \"Day 3\", \"name\": \"Perform 20 Squats\", \"completed\": false},\n" +
                "  {\"day\": \"Day 4\", \"name\": \"Do 15 Push-ups\", \"completed\": false},\n" +
                "  {\"day\": \"Day 5\", \"name\": \"Log 20-minute walk\", \"completed\": false},\n" +
                "  {\"day\": \"Day 6\", \"name\": \"Do 15-minute body stretch\", \"completed\": false},\n" +
                "  {\"day\": \"Day 7\", \"name\": \"Recovery walk and check weight\", \"completed\": false}\n" +
                "]");
        fitnessDao.insertFitnessChallenge(active);

        // 2. 7-Day Core Sculpt Challenge
        FitnessChallenge core = new FitnessChallenge();
        core.setName("7-Day Core Sculpt Challenge");
        core.setDescription("Strengthen your abs, lower back, and obliques with targeted daily core exercises.");
        core.setDifficulty("MEDIUM");
        core.setDuration("WEEKLY");
        core.setXpReward(150);
        core.setBadgeName("Consistent");
        core.setStartDate(now);
        core.setEndDate(now + (7L * dayMs));
        core.setStatus("NOT_STARTED");
        core.setProgressPercent(0);
        core.setDaysRemaining(7);
        core.setCaloriesEstimate(120);
        core.setEstimatedWorkoutTime(12);
        core.setHealthBenefits("• Builds core abdominal stability.\n• Lowers lower back pain risks.\n• Tones upper and lower abdominal muscles.");
        core.setTasksJson("[\n" +
                "  {\"day\": \"Day 1\", \"name\": \"45-second plank hold\", \"completed\": false},\n" +
                "  {\"day\": \"Day 2\", \"name\": \"20 Russian twists\", \"completed\": false},\n" +
                "  {\"day\": \"Day 3\", \"name\": \"15 leg raises\", \"completed\": false},\n" +
                "  {\"day\": \"Day 4\", \"name\": \"30-second side plank (each side)\", \"completed\": false},\n" +
                "  {\"day\": \"Day 5\", \"name\": \"20 bicycle crunches\", \"completed\": false},\n" +
                "  {\"day\": \"Day 6\", \"name\": \"1-minute plank hold\", \"completed\": false},\n" +
                "  {\"day\": \"Day 7\", \"name\": \"30 flutter kicks\", \"completed\": false}\n" +
                "]");
        fitnessDao.insertFitnessChallenge(core);

        // 3. 14-Day Full Body Blast (The key 14-Day challenge)
        FitnessChallenge bodyBlast = new FitnessChallenge();
        bodyBlast.setName("14-Day Full Body Blast");
        bodyBlast.setDescription("Intermediate full-body circuit routines designed to increase raw power and agility.");
        bodyBlast.setDifficulty("MEDIUM");
        bodyBlast.setDuration("WEEKLY");
        bodyBlast.setXpReward(250);
        bodyBlast.setBadgeName("Elite");
        bodyBlast.setStartDate(now);
        bodyBlast.setEndDate(now + (14L * dayMs));
        bodyBlast.setStatus("NOT_STARTED");
        bodyBlast.setProgressPercent(0);
        bodyBlast.setDaysRemaining(14);
        bodyBlast.setCaloriesEstimate(320);
        bodyBlast.setEstimatedWorkoutTime(20);
        bodyBlast.setHealthBenefits("• Promotes cardiovascular hypertrophy.\n• Targets major muscle groups in quick succession.\n• Enhances stamina, explosiveness, and mobility.");
        bodyBlast.setTasksJson("[\n" +
                "  {\"day\": \"Day 1\", \"name\": \"30 mountain climbers\", \"completed\": false},\n" +
                "  {\"day\": \"Day 2\", \"name\": \"25 jump squats\", \"completed\": false},\n" +
                "  {\"day\": \"Day 3\", \"name\": \"20 push-ups\", \"completed\": false},\n" +
                "  {\"day\": \"Day 4\", \"name\": \"1-minute wall sit\", \"completed\": false},\n" +
                "  {\"day\": \"Day 5\", \"name\": \"40 jumping jacks\", \"completed\": false},\n" +
                "  {\"day\": \"Day 6\", \"name\": \"15 burpees\", \"completed\": false},\n" +
                "  {\"day\": \"Day 7\", \"name\": \"2-minute plank\", \"completed\": false},\n" +
                "  {\"day\": \"Day 8\", \"name\": \"30 squat pulses\", \"completed\": false},\n" +
                "  {\"day\": \"Day 9\", \"name\": \"25 commandos\", \"completed\": false},\n" +
                "  {\"day\": \"Day 10\", \"name\": \"20 decline push-ups\", \"completed\": false},\n" +
                "  {\"day\": \"Day 11\", \"name\": \"1.5-minute wall sit\", \"completed\": false},\n" +
                "  {\"day\": \"Day 12\", \"name\": \"50 high knees\", \"completed\": false},\n" +
                "  {\"day\": \"Day 13\", \"name\": \"20 burpees\", \"completed\": false},\n" +
                "  {\"day\": \"Day 14\", \"name\": \"3-minute plank max hold\", \"completed\": false}\n" +
                "]");
        fitnessDao.insertFitnessChallenge(bodyBlast);

        // 4. 21-Day Yoga & Flexibility Challenge
        FitnessChallenge yoga = new FitnessChallenge();
        yoga.setName("21-Day Yoga & Flexibility");
        yoga.setDescription("Improve core flexibility, increase joints range of motion, and promote daily mindfulness.");
        yoga.setDifficulty("EASY");
        yoga.setDuration("WEEKLY");
        yoga.setXpReward(350);
        yoga.setBadgeName("Consistent");
        yoga.setStartDate(now);
        yoga.setEndDate(now + (21L * dayMs));
        yoga.setStatus("NOT_STARTED");
        yoga.setProgressPercent(0);
        yoga.setDaysRemaining(21);
        yoga.setCaloriesEstimate(90);
        yoga.setEstimatedWorkoutTime(20);
        yoga.setHealthBenefits("• Releases muscle tension.\n• Improves stability and joints range of motion.\n• Incorporates breathing exercises for mental calm.");
        yoga.setTasksJson("[\n" +
                "  {\"day\": \"Week 1 Day 1\", \"name\": \"15-minute morning stretches\", \"completed\": false},\n" +
                "  {\"day\": \"Week 1 Day 2\", \"name\": \"Deep breathing & Sun Salutation\", \"completed\": false},\n" +
                "  {\"day\": \"Week 1 Day 3\", \"name\": \"Balance poses practice\", \"completed\": false},\n" +
                "  {\"day\": \"Week 1 Day 4\", \"name\": \"Lower back relief flow\", \"completed\": false},\n" +
                "  {\"day\": \"Week 1 Day 5\", \"name\": \"Hamstring dynamic stretches\", \"completed\": false},\n" +
                "  {\"day\": \"Week 1 Day 6\", \"name\": \"Vinyasa flow sequence\", \"completed\": false},\n" +
                "  {\"day\": \"Week 1 Day 7\", \"name\": \"Full recovery breathing\", \"completed\": false}\n" +
                "]");
        fitnessDao.insertFitnessChallenge(yoga);

        // 5. 30-Day Cardio Shred (Fat Loss Challenge)
        FitnessChallenge cardio = new FitnessChallenge();
        cardio.setName("30-Day Cardio Shred");
        cardio.setDescription("High-intensity cardio challenge to maximize fat burn, increase stamina, and boost heart health.");
        cardio.setDifficulty("HARD");
        cardio.setDuration("MONTHLY");
        cardio.setXpReward(500);
        cardio.setBadgeName("Elite");
        cardio.setStartDate(now);
        cardio.setEndDate(now + (30L * dayMs));
        cardio.setStatus("NOT_STARTED");
        cardio.setProgressPercent(0);
        cardio.setDaysRemaining(30);
        cardio.setCaloriesEstimate(380);
        cardio.setEstimatedWorkoutTime(25);
        cardio.setHealthBenefits("• Promotes high caloric expenditure.\n• Elevates resting metabolic rate.\n• Accelerates abdominal and body fat loss.");
        cardio.setTasksJson("[\n" +
                "  {\"day\": \"Day 1\", \"name\": \"15 mins of jump rope\", \"completed\": false},\n" +
                "  {\"day\": \"Day 2\", \"name\": \"20-minute HIIT run\", \"completed\": false},\n" +
                "  {\"day\": \"Day 3\", \"name\": \"50 jumping jacks & 10 burpees\", \"completed\": false},\n" +
                "  {\"day\": \"Day 4\", \"name\": \"30 minutes brisk walking\", \"completed\": false},\n" +
                "  {\"day\": \"Day 5\", \"name\": \"25-minute HIIT bodyweight burn\", \"completed\": false},\n" +
                "  {\"day\": \"Day 6\", \"name\": \"15-minute stair climbing\", \"completed\": false},\n" +
                "  {\"day\": \"Day 7\", \"name\": \"Active recovery stretching\", \"completed\": false}\n" +
                "]");
        fitnessDao.insertFitnessChallenge(cardio);

        // 6. 30-Day Muscle Gain Sculpt
        FitnessChallenge muscle = new FitnessChallenge();
        muscle.setName("30-Day Muscle Gain Sculpt");
        muscle.setDescription("Advanced resistance training challenge targeting full-body hypertrophy and strength gain.");
        muscle.setDifficulty("EXTREME");
        muscle.setDuration("MONTHLY");
        muscle.setXpReward(600);
        muscle.setBadgeName("Champion");
        muscle.setStartDate(now);
        muscle.setEndDate(now + (30L * dayMs));
        muscle.setStatus("NOT_STARTED");
        muscle.setProgressPercent(0);
        muscle.setDaysRemaining(30);
        muscle.setCaloriesEstimate(320);
        muscle.setEstimatedWorkoutTime(35);
        muscle.setHealthBenefits("• Stimulates protein synthesis and muscle hypertrophy.\n• Strengthens major joints and connective tissue.\n• Tones upper/lower body shapes.");
        muscle.setTasksJson("[\n" +
                "  {\"day\": \"Day 1\", \"name\": \"4 sets of 15 bodyweight squats\", \"completed\": false},\n" +
                "  {\"day\": \"Day 2\", \"name\": \"3 sets of 12 push-ups\", \"completed\": false},\n" +
                "  {\"day\": \"Day 3\", \"name\": \"3 sets of 8 pullups or inverted rows\", \"completed\": false},\n" +
                "  {\"day\": \"Day 4\", \"name\": \"4 sets of 12 lunges (each leg)\", \"completed\": false},\n" +
                "  {\"day\": \"Day 5\", \"name\": \"3 sets of 12 bench dips\", \"completed\": false},\n" +
                "  {\"day\": \"Day 6\", \"name\": \"15-minute core strength builder\", \"completed\": false},\n" +
                "  {\"day\": \"Day 7\", \"name\": \"Stretching & muscle recovery\", \"completed\": false}\n" +
                "]");
        fitnessDao.insertFitnessChallenge(muscle);

        // 7. 14-Day Fat Loss Cardio
        FitnessChallenge fatLoss = new FitnessChallenge();
        fatLoss.setName("14-Day Fat Loss Cardio");
        fatLoss.setDescription("High intensity interval workout segments tailored to shred fat quickly and build tone.");
        fatLoss.setDifficulty("HARD");
        fatLoss.setDuration("WEEKLY");
        fatLoss.setXpReward(300);
        fatLoss.setBadgeName("Elite");
        fatLoss.setStartDate(now);
        fatLoss.setEndDate(now + (14L * dayMs));
        fatLoss.setStatus("NOT_STARTED");
        fatLoss.setProgressPercent(0);
        fatLoss.setDaysRemaining(14);
        fatLoss.setCaloriesEstimate(420);
        fatLoss.setEstimatedWorkoutTime(30);
        fatLoss.setHealthBenefits("• Drives continuous post-workout fat oxidation.\n• Tones core, leg, and cardiovascular complexes.\n• Enhances heart capacity.");
        fatLoss.setTasksJson("[\n" +
                "  {\"day\": \"Day 1\", \"name\": \"30 mountain climbers & 20 squats\", \"completed\": false},\n" +
                "  {\"day\": \"Day 2\", \"name\": \"40 jumping jacks & 10 burpees\", \"completed\": false},\n" +
                "  {\"day\": \"Day 3\", \"name\": \"25-minute brisk jog\", \"completed\": false},\n" +
                "  {\"day\": \"Day 4\", \"name\": \"15 squat jumps & 20 plank jacks\", \"completed\": false},\n" +
                "  {\"day\": \"Day 5\", \"name\": \"30-minute steady-state cardio\", \"completed\": false},\n" +
                "  {\"day\": \"Day 6\", \"name\": \"20 skater jumps & 10 burpees\", \"completed\": false},\n" +
                "  {\"day\": \"Day 7\", \"name\": \"Deep stretching & warm bath\", \"completed\": false}\n" +
                "]");
        fitnessDao.insertFitnessChallenge(fatLoss);

        // 8. 7-Day Walking Challenge
        FitnessChallenge walking = new FitnessChallenge();
        walking.setName("7-Day Walking Challenge");
        walking.setDescription("Consistent walking goals to build endurance, burn clean energy, and lower daily stress levels.");
        walking.setDifficulty("EASY");
        walking.setDuration("WEEKLY");
        walking.setXpReward(100);
        walking.setBadgeName("Beginner");
        walking.setStartDate(now);
        walking.setEndDate(now + (7L * dayMs));
        walking.setStatus("NOT_STARTED");
        walking.setProgressPercent(0);
        walking.setDaysRemaining(7);
        walking.setCaloriesEstimate(150);
        walking.setEstimatedWorkoutTime(20);
        walking.setHealthBenefits("• Clean metabolic activity.\n• Low-impact on knees and joints.\n• Burns calories while strengthening lower body.");
        walking.setTasksJson("[\n" +
                "  {\"day\": \"Day 1\", \"name\": \"Walk 5,000 steps today\", \"completed\": false},\n" +
                "  {\"day\": \"Day 2\", \"name\": \"Walk 6,000 steps today\", \"completed\": false},\n" +
                "  {\"day\": \"Day 3\", \"name\": \"Walk 7,000 steps today\", \"completed\": false},\n" +
                "  {\"day\": \"Day 4\", \"name\": \"Walk 8,000 steps today\", \"completed\": false},\n" +
                "  {\"day\": \"Day 5\", \"name\": \"Walk 6,500 steps today\", \"completed\": false},\n" +
                "  {\"day\": \"Day 6\", \"name\": \"Walk 9,000 steps today\", \"completed\": false},\n" +
                "  {\"day\": \"Day 7\", \"name\": \"Walk 10,000 steps goal!\", \"completed\": false}\n" +
                "]");
        fitnessDao.insertFitnessChallenge(walking);

        // 9. 7-Day Water Hydration Hero
        FitnessChallenge water = new FitnessChallenge();
        water.setName("7-Day Water Hydration Hero");
        water.setDescription("Recharge your body systems, improve focus, and clean out toxins with daily water targets.");
        water.setDifficulty("EASY");
        water.setDuration("WEEKLY");
        water.setXpReward(80);
        water.setBadgeName("Beginner");
        water.setStartDate(now);
        water.setEndDate(now + (7L * dayMs));
        water.setStatus("NOT_STARTED");
        water.setProgressPercent(0);
        water.setDaysRemaining(7);
        water.setCaloriesEstimate(0);
        water.setEstimatedWorkoutTime(0);
        water.setHealthBenefits("• Clarifies skin complexity.\n• Hydrates muscle cells to prevent cramping.\n• Enhances mental focus and kidney health.");
        water.setTasksJson("[\n" +
                "  {\"day\": \"Day 1\", \"name\": \"Consume 2.0 Liters of water\", \"completed\": false},\n" +
                "  {\"day\": \"Day 2\", \"name\": \"Consume 2.5 Liters of water\", \"completed\": false},\n" +
                "  {\"day\": \"Day 3\", \"name\": \"Consume 2.5 Liters of water\", \"completed\": false},\n" +
                "  {\"day\": \"Day 4\", \"name\": \"Consume 3.0 Liters of water\", \"completed\": false},\n" +
                "  {\"day\": \"Day 5\", \"name\": \"Consume 3.0 Liters of water\", \"completed\": false},\n" +
                "  {\"day\": \"Day 6\", \"name\": \"Consume 3.2 Liters of water\", \"completed\": false},\n" +
                "  {\"day\": \"Day 7\", \"name\": \"Consume 3.5 Liters water challenge!\", \"completed\": false}\n" +
                "]");
        fitnessDao.insertFitnessChallenge(water);

        // 10. 14-Day Push-up Master
        FitnessChallenge pushup = new FitnessChallenge();
        pushup.setName("14-Day Push-up Master");
        pushup.setDescription("Gradually increase chest, shoulder, and tricep strength with daily incremental push-up targets.");
        pushup.setDifficulty("MEDIUM");
        pushup.setDuration("WEEKLY");
        pushup.setXpReward(200);
        pushup.setBadgeName("Consistent");
        pushup.setStartDate(now);
        pushup.setEndDate(now + (14L * dayMs));
        pushup.setStatus("NOT_STARTED");
        pushup.setProgressPercent(0);
        pushup.setDaysRemaining(14);
        pushup.setCaloriesEstimate(110);
        pushup.setEstimatedWorkoutTime(10);
        pushup.setHealthBenefits("• Sculpts pectorals and tricep definitions.\n• Enhances upper-body push stamina.\n• Boosts core brace control.");
        pushup.setTasksJson("[\n" +
                "  {\"day\": \"Day 1\", \"name\": \"Perform 10 standard push-ups\", \"completed\": false},\n" +
                "  {\"day\": \"Day 2\", \"name\": \"Perform 12 standard push-ups\", \"completed\": false},\n" +
                "  {\"day\": \"Day 3\", \"name\": \"Perform 15 standard push-ups\", \"completed\": false},\n" +
                "  {\"day\": \"Day 4\", \"name\": \"Perform 15 diamond push-ups\", \"completed\": false},\n" +
                "  {\"day\": \"Day 5\", \"name\": \"Perform 18 standard push-ups\", \"completed\": false},\n" +
                "  {\"day\": \"Day 6\", \"name\": \"Perform 20 standard push-ups\", \"completed\": false},\n" +
                "  {\"day\": \"Day 7\", \"name\": \"Recovery day (upper body stretches)\", \"completed\": false},\n" +
                "  {\"day\": \"Day 8\", \"name\": \"Perform 20 standard push-ups\", \"completed\": false},\n" +
                "  {\"day\": \"Day 9\", \"name\": \"Perform 22 standard push-ups\", \"completed\": false},\n" +
                "  {\"day\": \"Day 10\", \"name\": \"Perform 25 standard push-ups\", \"completed\": false},\n" +
                "  {\"day\": \"Day 11\", \"name\": \"Perform 25 incline push-ups\", \"completed\": false},\n" +
                "  {\"day\": \"Day 12\", \"name\": \"Perform 30 standard push-ups\", \"completed\": false},\n" +
                "  {\"day\": \"Day 13\", \"name\": \"Perform 35 standard push-ups\", \"completed\": false},\n" +
                "  {\"day\": \"Day 14\", \"name\": \"Perform 40 standard push-ups goal!\", \"completed\": false}\n" +
                "]");
        fitnessDao.insertFitnessChallenge(pushup);

        // 11. 14-Day Plank Core Hold
        FitnessChallenge plank = new FitnessChallenge();
        plank.setName("14-Day Plank Core Hold");
        plank.setDescription("Build structural core, back, and pelvic floor strength with progressive daily plank holds.");
        plank.setDifficulty("MEDIUM");
        plank.setDuration("WEEKLY");
        plank.setXpReward(180);
        plank.setBadgeName("Consistent");
        plank.setStartDate(now);
        plank.setEndDate(now + (14L * dayMs));
        plank.setStatus("NOT_STARTED");
        plank.setProgressPercent(0);
        plank.setDaysRemaining(14);
        plank.setCaloriesEstimate(70);
        plank.setEstimatedWorkoutTime(8);
        plank.setHealthBenefits("• Develops abdominal isometric brace hold capability.\n• Reduces risks of lower back compression.\n• Improves spinal posture.");
        plank.setTasksJson("[\n" +
                "  {\"day\": \"Day 1\", \"name\": \"Hold standard plank for 30s\", \"completed\": false},\n" +
                "  {\"day\": \"Day 2\", \"name\": \"Hold standard plank for 35s\", \"completed\": false},\n" +
                "  {\"day\": \"Day 3\", \"name\": \"Hold standard plank for 45s\", \"completed\": false},\n" +
                "  {\"day\": \"Day 4\", \"name\": \"Hold side plank (each side) for 20s\", \"completed\": false},\n" +
                "  {\"day\": \"Day 5\", \"name\": \"Hold standard plank for 50s\", \"completed\": false},\n" +
                "  {\"day\": \"Day 6\", \"name\": \"Hold standard plank for 60s\", \"completed\": false},\n" +
                "  {\"day\": \"Day 7\", \"name\": \"Active recovery core movements\", \"completed\": false},\n" +
                "  {\"day\": \"Day 8\", \"name\": \"Hold standard plank for 60s\", \"completed\": false},\n" +
                "  {\"day\": \"Day 9\", \"name\": \"Hold standard plank for 70s\", \"completed\": false},\n" +
                "  {\"day\": \"Day 10\", \"name\": \"Hold standard plank for 80s\", \"completed\": false},\n" +
                "  {\"day\": \"Day 11\", \"name\": \"Hold side plank (each side) for 35s\", \"completed\": false},\n" +
                "  {\"day\": \"Day 12\", \"name\": \"Hold standard plank for 90s\", \"completed\": false},\n" +
                "  {\"day\": \"Day 13\", \"name\": \"Hold standard plank for 100s\", \"completed\": false},\n" +
                "  {\"day\": \"Day 14\", \"name\": \"Hold standard plank for 120s goal!\", \"completed\": false}\n" +
                "]");
        fitnessDao.insertFitnessChallenge(plank);

        // 12. 3-Day Warm-up Intro (Completed)
        FitnessChallenge completed = new FitnessChallenge();
        completed.setName("3-Day Warm-up Intro");
        completed.setDescription("A quick, 3-day warm-up challenge to introduce you to daily movement habits.");
        completed.setDifficulty("EASY");
        completed.setDuration("WEEKLY");
        completed.setXpReward(50);
        completed.setBadgeName("Beginner");
        completed.setStartDate(now - (10L * dayMs));
        completed.setEndDate(now - (7L * dayMs));
        completed.setStatus("COMPLETED");
        completed.setProgressPercent(100);
        completed.setDaysRemaining(0);
        completed.setCaloriesEstimate(80);
        completed.setEstimatedWorkoutTime(10);
        completed.setHealthBenefits("• Gently opens up muscle tissue.\n• Lowers standard load-bearing strain.\n• Introduces joints motion dynamics.");
        completed.setTasksJson("[\n" +
                "  {\"day\": \"Day 1\", \"name\": \"Walk 3,000 steps\", \"completed\": true},\n" +
                "  {\"day\": \"Day 2\", \"name\": \"Drink 2 liters of water\", \"completed\": true},\n" +
                "  {\"day\": \"Day 3\", \"name\": \"Do 10 squats\", \"completed\": true}\n" +
                "]");
        fitnessDao.insertFitnessChallenge(completed);
    }

    private void updateStatsViews() {
        int totalXp = userStats.getTotalXp();
        int currentLevel = (totalXp / 300) + 1;
        int levelXp = totalXp % 300;

        tvUserLevel.setText("Level " + currentLevel + ": FitTrain Champion");
        tvUserXpLabel.setText("Total XP: " + levelXp + " / 300 (Total Accumulation: " + totalXp + " XP)");
        pbXpProgress.setProgress(levelXp);

        // Load unlocked badges list
        List<String> badgeList = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(userStats.getUnlockedBadgesJson());
            for (int i = 0; i < arr.length(); i++) {
                badgeList.add(arr.getString(i));
            }
        } catch (Exception ignored) {}

        // Add default seed badge if completed challenges exists
        if (userStats.getChallengesCompleted() > 0 && badgeList.isEmpty()) {
            badgeList.add("Consistent");
        }

        badgeAdapter = new BadgeAdapter(badgeList);
        rvBadges.setAdapter(badgeAdapter);
    }

    private void applyFilters() {
        filteredChallenges.clear();

        boolean filterAll = chipAll.isChecked();
        boolean filterActive = chipActive.isChecked();
        boolean filterWeekly = chipWeekly.isChecked();
        boolean filterMonthly = chipMonthly.isChecked();
        boolean filterCompleted = chipCompleted.isChecked();
        boolean filterUpcoming = chipUpcoming.isChecked();

        for (FitnessChallenge challenge : allChallenges) {
            if (filterAll) {
                filteredChallenges.add(challenge);
            } else if (filterActive && "IN_PROGRESS".equalsIgnoreCase(challenge.getStatus())) {
                filteredChallenges.add(challenge);
            } else if (filterWeekly && "WEEKLY".equalsIgnoreCase(challenge.getDuration()) && !"COMPLETED".equalsIgnoreCase(challenge.getStatus()) && !"UPCOMING".equalsIgnoreCase(challenge.getStatus())) {
                filteredChallenges.add(challenge);
            } else if (filterMonthly && "MONTHLY".equalsIgnoreCase(challenge.getDuration()) && !"COMPLETED".equalsIgnoreCase(challenge.getStatus()) && !"UPCOMING".equalsIgnoreCase(challenge.getStatus())) {
                filteredChallenges.add(challenge);
            } else if (filterCompleted && "COMPLETED".equalsIgnoreCase(challenge.getStatus())) {
                filteredChallenges.add(challenge);
            } else if (filterUpcoming && "UPCOMING".equalsIgnoreCase(challenge.getStatus())) {
                filteredChallenges.add(challenge);
            }
        }

        if (filteredChallenges.isEmpty()) {
            layoutEmptyChallenges.setVisibility(View.VISIBLE);
            rvChallenges.setVisibility(View.GONE);
        } else {
            layoutEmptyChallenges.setVisibility(View.GONE);
            rvChallenges.setVisibility(View.VISIBLE);
        }

        // Setup AI Coach Motivation Text
        if (filterActive) {
            tvChallengeCoachAdvice.setText("Tap continue on your active challenge. Keep the streak fire burning! 🔥");
        } else if (filterCompleted) {
            tvChallengeCoachAdvice.setText("Brilliant! Look at those trophy accomplishments. Ready for another round? 🏆");
        } else {
            tvChallengeCoachAdvice.setText("Choose a challenge matching your workout experience level and start earning XP! 💪");
        }

        challengeAdapter = new ChallengeAdapter(filteredChallenges, challenge -> {
            Intent intent = new Intent(ChallengesHubActivity.this, ChallengeDetailsActivity.class);
            intent.putExtra("challenge_item", challenge);
            startActivity(intent);
        });
        rvChallenges.setAdapter(challengeAdapter);
    }

    // Challenge list adapter
    private static class ChallengeAdapter extends RecyclerView.Adapter<ChallengeAdapter.ViewHolder> {
        private final List<FitnessChallenge> list;
        private final OnItemClickListener listener;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

        interface OnItemClickListener {
            void onItemClick(FitnessChallenge challenge);
        }

        ChallengeAdapter(List<FitnessChallenge> list, OnItemClickListener listener) {
            this.list = list;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_challenge_row, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            FitnessChallenge item = list.get(position);
            holder.tvTitle.setText(item.getName());
            holder.tvDesc.setText(item.getDescription());
            holder.tvDifficulty.setText(item.getDifficulty());
            holder.tvDuration.setText(item.getDuration());
            holder.tvReward.setText("+" + item.getXpReward() + " XP");
            holder.tvBadge.setText(item.getBadgeName() + " Badge");

            String startStr = dateFormat.format(new Date(item.getStartDate()));
            String endStr = dateFormat.format(new Date(item.getEndDate()));
            holder.tvDateRange.setText("Start: " + startStr + " • End: " + endStr);

            String status = item.getStatus();
            if ("IN_PROGRESS".equalsIgnoreCase(status)) {
                holder.tvStatus.setText("In Progress");
                holder.tvStatus.setTextColor(0xFF3B82F6);
                holder.tvStatus.setBackgroundResource(R.color.white); // standard tint or transparent background
                holder.layoutProgress.setVisibility(View.VISIBLE);
                holder.tvProgressPercent.setText("Progress: " + item.getProgressPercent() + "%");
                holder.pbProgress.setProgress(item.getProgressPercent());
            } else if ("COMPLETED".equalsIgnoreCase(status)) {
                holder.tvStatus.setText("Completed");
                holder.tvStatus.setTextColor(0xFF059669);
                holder.layoutProgress.setVisibility(View.VISIBLE);
                holder.tvProgressPercent.setText("Progress: 100%");
                holder.pbProgress.setProgress(100);
            } else if ("UPCOMING".equalsIgnoreCase(status)) {
                holder.tvStatus.setText("Upcoming");
                holder.tvStatus.setTextColor(0xFF7C3AED);
                holder.layoutProgress.setVisibility(View.GONE);
            } else {
                holder.tvStatus.setText("Not Started");
                holder.tvStatus.setTextColor(0xFF64748B);
                holder.layoutProgress.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvDesc, tvDifficulty, tvDuration, tvReward, tvBadge, tvStatus, tvProgressPercent, tvDateRange;
            ProgressBar pbProgress;
            View layoutProgress;

            ViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvRowTitle);
                tvDesc = itemView.findViewById(R.id.tvRowDesc);
                tvDifficulty = itemView.findViewById(R.id.tvRowDifficulty);
                tvDuration = itemView.findViewById(R.id.tvRowDuration);
                tvReward = itemView.findViewById(R.id.tvRowReward);
                tvBadge = itemView.findViewById(R.id.tvRowBadge);
                tvStatus = itemView.findViewById(R.id.tvRowStatus);
                tvProgressPercent = itemView.findViewById(R.id.tvRowProgressPercent);
                tvDateRange = itemView.findViewById(R.id.tvRowDateRange);
                pbProgress = itemView.findViewById(R.id.pbRowProgress);
                layoutProgress = itemView.findViewById(R.id.layoutRowProgress);
            }
        }
    }

    // Recycler Adapter class for Badge items
    private static class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder> {
        private final List<String> badges;

        public BadgeAdapter(List<String> badges) {
            this.badges = badges;
        }

        @NonNull
        @Override
        public BadgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_badge, parent, false);
            return new BadgeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BadgeViewHolder holder, int position) {
            String name = badges.get(position);
            holder.tvBadgeName.setText(name);

            if (name.equalsIgnoreCase("Beginner")) {
                holder.tvBadgeIcon.setText("🥉");
            } else if (name.equalsIgnoreCase("Consistent")) {
                holder.tvBadgeIcon.setText("🥈");
            } else if (name.equalsIgnoreCase("Champion")) {
                holder.tvBadgeIcon.setText("🥇");
            } else if (name.equalsIgnoreCase("Elite")) {
                holder.tvBadgeIcon.setText("💎");
            } else if (name.equalsIgnoreCase("7-Day Streak")) {
                holder.tvBadgeIcon.setText("🔥");
            } else if (name.equalsIgnoreCase("30-Day Streak")) {
                holder.tvBadgeIcon.setText("⚡");
            } else {
                holder.tvBadgeIcon.setText("🏆");
            }
        }

        @Override
        public int getItemCount() {
            return badges.size();
        }

        static class BadgeViewHolder extends RecyclerView.ViewHolder {
            TextView tvBadgeIcon, tvBadgeName;

            public BadgeViewHolder(@NonNull View itemView) {
                super(itemView);
                tvBadgeIcon = itemView.findViewById(R.id.tvBadgeIcon);
                tvBadgeName = itemView.findViewById(R.id.tvBadgeName);
            }
        }
    }
}

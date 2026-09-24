package com.fitness.app.utils;

import android.content.Context;

import com.fitness.app.models.ChatMessage;
import com.fitness.app.models.User;
import com.fitness.app.data.local.LocalDataManager;

import java.util.List;
import java.util.Locale;

public class FitTrainAiEngine {

    public static String processQuery(String query, User user, List<ChatMessage> history, Context context) {
        if (query == null || query.trim().isEmpty()) {
            return "Hello! How can I assist your workout or diet routine today?";
        }

        // 1. Detect Intent
        FitTrainIntentRouter.Intent intent = FitTrainIntentRouter.detectIntent(query);

        // 2. Extract User Stats & Deterministic Calculations
        if (user == null && context != null) {
            user = new LocalDataManager(context).getUser();
        }

        String name = "Athlete";
        String goal = "Improve Fitness";
        double weight = 70.0;
        double height = 170.0;
        double targetWeight = 0.0;
        int age = 25;
        String gender = "Male";
        String activityLevel = "Moderate";
        String dietaryPref = "None";
        String equipment = "Bodyweight";

        if (user != null) {
            if (user.getFirstName() != null && !user.getFirstName().trim().isEmpty()) name = user.getFirstName();
            if (user.getGoal() != null && !user.getGoal().trim().isEmpty()) goal = user.getGoal();
            if (user.getWeight() > 0) weight = user.getWeight();
            if (user.getHeight() > 0) height = user.getHeight();
            if (user.getTargetWeight() > 0) targetWeight = user.getTargetWeight();
            if (user.getAge() > 0) age = user.getAge();
            if (user.getGender() != null && !user.getGender().trim().isEmpty()) gender = user.getGender();
            if (user.getActivityLevel() != null && !user.getActivityLevel().trim().isEmpty()) activityLevel = user.getActivityLevel();
            if (user.getDietaryPreference() != null && !user.getDietaryPreference().trim().isEmpty()) dietaryPref = user.getDietaryPreference();
            if (user.getAvailableEquipment() != null && !user.getAvailableEquipment().trim().isEmpty()) equipment = user.getAvailableEquipment();
        }

        double bmi = FitnessCalculator.calculateBmi(weight, height);
        String bmiCategory = FitnessCalculator.getBmiCategory(bmi);
        double bmr = FitnessCalculator.calculateBmr(weight, height, age, gender);
        double tdee = FitnessCalculator.calculateTdee(bmr, activityLevel);
        int calTarget = FitnessCalculator.calculateDailyCalorieTarget(tdee, goal);
        int proteinTarget = FitnessCalculator.calculateProteinTarget(weight, goal);
        int waterTarget = FitnessCalculator.calculateDailyWaterGoal(weight);

        String lower = query.toLowerCase(Locale.getDefault()).trim();
        boolean isRomanUrdu = lower.contains("kya") || lower.contains("hai") || lower.contains("tum") || lower.contains("mujhe")
                || lower.contains("kaise") || lower.contains("karna") || lower.contains("batao") || lower.contains("shukriya") || lower.contains("kaisy");

        // 3. INTENT-BASED DYNAMIC RESPONSE SYNTHESIS

        switch (intent) {
            case BMI_EXPLANATION:
                if (isRomanUrdu) {
                    return String.format(Locale.US, "BMI (Body Mass Index) ek numeric value hai jo aapki height aur weight se calculate hoti hai taa ke bataya ja sake ke aapka weight healthy range mein hai ya nahi.\n\n"
                            + "Formula: Weight (kg) / (Height in meters)^2\n\n"
                            + "Aapka Calculated BMI: **%.1f** (%s Category)\n"
                            + "• Underweight: < 18.5\n• Normal: 18.5 - 24.9\n• Overweight: 25.0 - 29.9\n• Obese: >= 30.0", bmi, bmiCategory);
                }
                return String.format(Locale.US, "**Body Mass Index (BMI)** is a standard measure that uses your height and weight to work out if your weight is healthy.\n\n"
                        + "• Calculation Formula: `Weight (kg) / [Height (m)]²`\n"
                        + "• Your Calculated BMI: **%.1f** (%s)\n\n"
                        + "BMI Ranges:\n"
                        + "• Underweight: Under 18.5\n"
                        + "• Normal weight: 18.5 to 24.9\n"
                        + "• Overweight: 25.0 to 29.9\n"
                        + "• Obese: 30.0 or higher\n\n"
                        + "You can track your weight and BMI changes regularly in the **Progress** tab!", bmi, bmiCategory);

            case FAT_LOSS_GUIDE:
                if (isRomanUrdu) {
                    return String.format(Locale.US, "Fat loss ke liye sab se zaroori rules ye hain:\n\n"
                            + "1. **Calorie Deficit**: Apne TDEE (%.0f kcal) se lagbhag 500 kcal kam intake karein (Target: **%d kcal/day**).\n"
                            + "2. **High Protein**: Minimum **%d g protein** daily khayein taa ke muscle mass protect rahe.\n"
                            + "3. **Strength + HIIT Cardio**: Week mein 3 din compound strength exercises aur 2-3 HIIT cardio sessions karein.\n"
                            + "4. **Hydration**: Daily %d ml paani piyein.", tdee, calTarget, proteinTarget, waterTarget);
                }
                return String.format(Locale.US, "**Scientific Fat Loss Strategy**:\n\n"
                        + "1. **Caloric Deficit**: Create a daily deficit of ~500 kcal relative to your TDEE (%.0f kcal). Your targeted intake is **%d kcal/day**.\n"
                        + "2. **Protein Preservation**: Aim for **%d g of protein** daily to preserve lean muscle while burning fat.\n"
                        + "3. **Resistance Training & HIIT**: Combine 3 full-body resistance sessions with 20-minute HIIT cardio circuits.\n"
                        + "4. **Hydration & Sleep**: Drink **%d ml water** daily and aim for 7-8 hours of quality sleep.\n\n"
                        + "Track your daily meals in **Smart Meal Planner** to ensure you stay within your calorie limit!", tdee, calTarget, proteinTarget, waterTarget);

            case MUSCLE_BUILDING_GUIDE:
                return String.format(Locale.US, "**Muscle Hypertrophy & Growth Strategy**:\n\n"
                        + "1. **Progressive Overload**: Gradually increase weight or reps over time.\n"
                        + "2. **Calorie Surplus**: Consume a clean surplus of +300 to +500 kcal (Target: **%d kcal/day**).\n"
                        + "3. **High Protein**: Consume **%d g of protein** daily (approx 1.8-2.0g per kg bodyweight).\n"
                        + "4. **Rest & Recovery**: Allow 48 hours of rest between training the same muscle group.\n\n"
                        + "Use the **AI Workout Generator** to create progressive overload split routines!", calTarget > 2000 ? calTarget : (int)(tdee + 400), proteinTarget);

            case EXERCISE_FORM_SQUATS:
                if (isRomanUrdu) {
                    return "**Correct Squat Execution & Form**:\n\n"
                            + "1. Stance: Paon shoulder-width apart rakhein, toes slightly outward point karein.\n"
                            + "2. Motion: Hips ko peechay bhejein jaisay chair par beth rahay hon.\n"
                            + "3. Depth: Thighs ground ke parallel aane tak neechay jayein.\n"
                            + "4. Knee Line: Knees ko paon ke toes ke sath align rakhein, ander na girnay dein.\n"
                            + "5. Chest & Back: Chest upright rakhein aur lower back ko flat rakhein.\n\n"
                            + "Smart Tip: Real-time live camera correction ke liye FitTrain **Form Check** mode use karein!";
                }
                return "**Proper Bodyweight/Weighted Squat Form**:\n\n"
                        + "• **Starting Stance**: Feet shoulder-width apart, toes turned slightly outwards (15-30 degrees).\n"
                        + "• **Descent Phase**: Inhale, hinge hips back first, then bend knees while keeping chest elevated.\n"
                        + "• **Depth**: Lower until hips descend slightly below knee parallel.\n"
                        + "• **Ascent Phase**: Drive firmly through mid-foot and heels, exhaling as you stand.\n"
                        + "• **Common Error**: Letting knees cave inwards (valgus knee collapse) or rounding spine.\n\n"
                        + "Pro Tip: Use the **Form Check** tab in FitTrain for live camera pose analysis and rep counting!";

            case CHAIR_WORKOUT:
                return "**FitTrain Household Chair Routine**:\n\n"
                        + "1. **Chair Dips**: 3 sets x 12 reps (Targets Triceps & Chest)\n"
                        + "2. **Chair Step-Ups**: 3 sets x 10 reps per leg (Targets Quads & Glutes)\n"
                        + "3. **Incline Chair Push-Ups**: 3 sets x 12 reps (Upper Body)\n"
                        + "4. **Seated Knee Tucks**: 3 sets x 15 reps (Core & Abs)\n\n"
                        + "Rest 45 seconds between sets. Open **Equipment Workout Generator** to create more home equipment workouts!";

            case BREAKFAST_NUTRITION:
                return String.format(Locale.US, "**High-Protein Healthy Breakfast Options**:\n\n"
                        + "• **Option 1 (Egg & Toast)**: 3 egg whites + 1 whole egg scrambled with spinach, served with 1 slice whole-wheat toast (~320 kcal, 24g Protein).\n"
                        + "• **Option 2 (Protein Oats)**: 50g rolled oats cooked in milk with 1 scoop whey protein or 1 tbsp peanut butter + banana (~400 kcal, 28g Protein).\n"
                        + "• **Option 3 (Pakistani Healthy)**: 2 boiled eggs + 1 small whole-wheat roti with cucumber slices (~300 kcal, 18g Protein).\n\n"
                        + "Log your breakfast in the **Diets** tab to update today's macro intake!", calTarget);

            case WORKOUT_MODIFICATION_EASIER:
                return "**Workout Modification (Beginner / Easier Version)**:\n\n"
                        + "I've modified the intensity for you:\n"
                        + "• Replace regular Push-ups with **Knee Push-ups** or **Wall Push-ups** (3 sets x 8 reps).\n"
                        + "• Replace Jump Squats with **Supported Chair Squats** (3 sets x 10 reps).\n"
                        + "• Reduce work interval from 45 seconds to **25 seconds** with **45 seconds rest**.\n\n"
                        + "Focus on clean form over speed. You've got this!";

            case WORKOUT_MODIFICATION_NO_EQUIPMENT:
                return "**No Equipment / Bodyweight Substitute Routine**:\n\n"
                        + "No dumbbells needed! Here are bodyweight & household swaps:\n"
                        + "• **Dumbbell Rows** → Swap with **Towel Door Rows** or **Backpack Rows** (fill backpack with books).\n"
                        + "• **Dumbbell Squats** → Swap with **Explosive Bodyweight Squats** or **Backpack Goblet Squats**.\n"
                        + "• **Dumbbell Overhead Press** → Swap with **Pike Push-ups** (targets shoulders).\n\n"
                        + "Check out **Equipment Workout Generator** in FitTrain for more household item substitutes!";

            case REPS_SETS_GUIDE:
                return "**Exercise Reps & Sets Guidelines**:\n\n"
                        + "• **For Muscle Growth (Hypertrophy)**: 3 - 4 sets of 8 - 12 reps (Rest 60-90s).\n"
                        + "• **For Endurance & Fat Loss**: 3 sets of 15 - 20 reps (Rest 30-45s).\n"
                        + "• **For Pure Strength**: 4 - 5 sets of 3 - 6 heavy reps (Rest 2-3 mins).\n\n"
                        + "Aim for the 8-12 rep range for balanced fitness and lean muscle tone!";

            case WARMUP_STAMINA_GUIDE:
                return "**Warm-Up & Stamina Building Guide**:\n\n"
                        + "• **Dynamic Warm-Up (5-8 Mins)**: Arm Circles (30s), Torso Twists (30s), High Knees (45s), Leg Swings (10/leg), Bodyweight Squats (10 reps).\n"
                        + "• **Stamina Building**: Gradually build cardiovascular endurance by incorporating progressive interval cardio (e.g. 1 min brisk run, 1 min walk x 10 rounds).\n\n"
                        + "Always warm up before high-intensity exercises to prevent injury!";

            case WORKOUT_TODAY:
            case WORKOUT_PLAN:
                String todayWorkoutData = FitTrainDataRetriever.getTodayWorkout(context, user);
                if (todayWorkoutData.contains("Assigned Daily Workout Plan") || todayWorkoutData.contains("Logged Session Today")) {
                    return String.format(Locale.US, "Here is your assigned FitTrain workout details for today, %s:\n\n%s", name, todayWorkoutData);
                }
                return generateStructuredWorkout(name, goal, calTarget, proteinTarget, equipment);

            case WORKOUT_HISTORY:
                return FitTrainDataRetriever.getWorkoutHistory(context);

            case DIET_TODAY:
            case MEAL_SCHEDULE:
                String dietData = FitTrainDataRetriever.getTodayDiet(context);
                if (dietData.contains("Logged Meals Today") || dietData.contains("Assigned Daily Diet Plan")) {
                    return String.format(Locale.US, "Here is your diet breakdown for today, %s:\n\n%s", name, dietData);
                }
                return String.format(Locale.US, "**FitTrain Nutrition Guidance** (%s):\n\n"
                        + "• **Target Calories**: %d kcal/day\n"
                        + "• **Protein Goal**: %d g\n"
                        + "• **Hydration Goal**: %d ml\n\n"
                        + "Recommended Meal Schedule:\n"
                        + "1. **Breakfast** (8:30 AM): 3 Egg white omelette with spinach + 1 slice whole-wheat toast.\n"
                        + "2. **Lunch** (1:30 PM): Grilled chicken or Daal chawal with cucumber salad.\n"
                        + "3. **Snack** (4:30 PM): Handful of almonds or roasted chickpeas (chana).\n"
                        + "4. **Dinner** (7:30 PM): Baked fish or Chicken tikka breast with steamed vegetables.", goal, calTarget, proteinTarget, waterTarget);

            case NUTRITION_MACROS:
                return String.format(Locale.US, "**Your Caloric & Macro Distribution**:\n\n"
                        + "• **BMR**: %.0f kcal/day\n"
                        + "• **TDEE**: %.0f kcal/day (%s activity)\n"
                        + "• **Target Daily Calories**: **%d kcal/day** (Goal: %s)\n"
                        + "• **Target Daily Protein**: **%d g/day**\n"
                        + "• **Target Daily Hydration**: **%d ml/day**", bmr, tdee, activityLevel, calTarget, goal, proteinTarget, waterTarget);

            case CALORIES_BURNED:
            case STEPS_ACTIVITY:
            case WATER_HYDRATION:
            case SLEEP_RECOVERY:
                return FitTrainDataRetriever.getDailyActivity(context);

            case BMI_METRICS:
                return String.format(Locale.US, "Your calculated **BMI is %.1f**, which falls in the **'%s'** category (Height: %.1f cm | Weight: %.1f kg).\n\n"
                        + "• Underweight: < 18.5\n"
                        + "• Normal: 18.5 - 24.9\n"
                        + "• Overweight: 25.0 - 29.9\n"
                        + "• Obese: >= 30.0", bmi, bmiCategory, height, weight);

            case PROGRESS_STREAK:
                return FitTrainDataRetriever.getStreakAndProgress(context, user);

            case EQUIPMENT_WORKOUT:
                return "**Equipment Workout Generator**:\n\n"
                        + "FitTrain helps you train at home using household items:\n"
                        + "• **Chair**: Chair Dips (3 sets x 12 reps), Step-ups (3 sets x 10 reps/leg), Incline Push-ups.\n"
                        + "• **Water Bottles**: Bicep Curls (3 sets x 15 reps), Lateral Raises, Overhead Tricep Extensions.\n"
                        + "• **Backpack (weighted with books)**: Backpack Goblet Squats (4 sets x 12 reps), Backpack Bent-over Rows.\n"
                        + "• **Resistance Bands**: Banded Monster Walks, Band Pull-aparts.\n\n"
                        + "Go to the **Equipment Workout Generator** screen in FitTrain to craft a custom routine with your available gear!";

            case FORM_CHECK_HELP:
                return "**FitTrain Form Check & AI Fitness**:\n\n"
                        + "1. Open the **Form Check** tab from the app drawer.\n"
                        + "2. Position your phone camera to capture your full body.\n"
                        + "3. Perform squats or bodyweight movements while real-time pose tracking corrects your posture and counts reps automatically!";

            case SMART_MEAL_PLANNER_HELP:
                return String.format(Locale.US, "**Smart Meal Planner**:\n\n"
                        + "FitTrain automatically calculates your nutrition target (**%d kcal**, **%d g Protein**) based on your goal to %s. Visit the **Diets** tab to view your custom daily recipes!", calTarget, proteinTarget, goal);

            case GROCERY_SCANNER_HELP:
                return "**Grocery Scanner**:\n\n"
                        + "Point your camera at food item barcodes or labels to extract macro values, detect additives, and auto-log items directly into your FitTrain food log!";

            case AI_RECIPE_VOICE_HELP:
                return "**AI Recipe Voice Assistant**:\n\n"
                        + "Hands-free voice assistant for cooking! Open **AI Recipe Voice** and say commands like 'Next step' or 'Read ingredients' while preparing meals.";

            case APP_NAVIGATION:
                return "**FitTrain App Feature Directory & Navigation**:\n\n"
                        + "• **Home**: Daily streak & shortcuts.\n"
                        + "• **Workout / AI Workout Generator**: Explore routines & auto-generate workouts.\n"
                        + "• **Diets / Smart Meal Planner**: Custom meal plans & nutrition logging.\n"
                        + "• **Daily Activity**: Log steps, water, burnt calories, & sleep.\n"
                        + "• **Progress**: Photo comparisons & 3D body visualization.\n"
                        + "• **Form Check**: Camera pose tracking for squat posture & rep counts.\n"
                        + "• **Settings**: Language options, Dark Mode, Metric/Imperial units.";

            case GENERAL_FITNESS:
            default:
                // MULTI-TOPIC DYNAMIC SOLVERS FOR GENERAL FITNESS QUERIES
                if (lower.contains("chest") || lower.contains("bench press")) {
                    return "**Chest & Bench Press Optimization Guide**:\n\n"
                            + "1. **Barbell/Dumbbell Bench Press**: Retract scapula, maintain arch, arch bar down to lower sternum (3-4 sets x 8-10 reps).\n"
                            + "2. **Incline Press**: Targets upper chest clavicular head (3 sets x 10 reps).\n"
                            + "3. **Chest Dips / Push-ups**: Lean forward 30 degrees to maximize pectoral recruitment.";
                } else if (lower.contains("back") || lower.contains("lat") || lower.contains("row")) {
                    return "**Back & Lat Muscle Hypertrophy**:\n\n"
                            + "1. **Lat Pulldowns / Pull-ups**: Drive elbows straight down towards ribs (3-4 sets x 8-12 reps).\n"
                            + "2. **Bent-over Barbell Rows**: Hinge hips to 45 degrees, pull bar to navel.\n"
                            + "3. **Single-Arm Dumbbell Rows**: Focus on full stretch at bottom and peak contraction at hip.";
                } else if (lower.contains("arm") || lower.contains("bicep") || lower.contains("tricep")) {
                    return "**Arm Development (Biceps & Triceps)**:\n\n"
                            + "• **Biceps**: Dumbbell Incline Curls (3 sets x 12 reps), Hammer Curls (brachialis build).\n"
                            + "• **Triceps**: Tricep Rope Pushdowns (3 sets x 15 reps), Overhead Extension (long head stretch).\n"
                            + "Remember triceps account for ~60% of total arm volume!";
                } else if (lower.contains("pre workout") || lower.contains("before workout") || lower.contains("pre-workout")) {
                    return "**Pre-Workout Nutrition Strategy**:\n\n"
                            + "Eat 60-90 minutes prior to training:\n"
                            + "• **Carbs**: Complex carbs for sustained glycogen (Oats, Banana, Whole-wheat toast).\n"
                            + "• **Protein**: 20-30g clean protein (Egg whites or Whey isolate).\n"
                            + "• **Hydration**: 500 ml water 30 minutes before starting.";
                } else if (lower.contains("post workout") || lower.contains("after workout") || lower.contains("post-workout")) {
                    return String.format(Locale.US, "**Post-Workout Recovery Fuel**:\n\n"
                            + "Consume within 45 minutes after training:\n"
                            + "• **Protein**: Target 25-%d g fast-absorbing protein (Whey, Grilled chicken, Fish) for muscle protein synthesis.\n"
                            + "• **Carbs**: Fast carbs (Rice, Fruit, Potato) to replenish depleted glycogen.\n"
                            + "• **Hydration**: Rehydrate with 500-750 ml water.", proteinTarget);
                } else if (lower.contains("creatine") || lower.contains("supplement") || lower.contains("whey")) {
                    return "**Fitness Supplements Guide**:\n\n"
                            + "• **Creatine Monohydrate**: 3-5g daily. Increases ATP cellular energy, muscle volume, and strength output.\n"
                            + "• **Whey Protein Isolate**: Convenient post-workout protein source to hit your daily target (**%d g**).\n"
                            + "• **Safety**: Supplements assist but do not replace whole food nutrition!";
                } else if (lower.contains("sore") || lower.contains("doms") || lower.contains("pain")) {
                    return "**DOMS & Muscle Soreness Recovery**:\n\n"
                            + "1. Active Recovery: Gentle 15-20 min walking or light mobility stretches.\n"
                            + "2. Hydration: Maintain your daily **%d ml water goal** to flush metabolic waste.\n"
                            + "3. Sleep: Aim for 7-8 hours of deep sleep where growth hormone is released.\n"
                            + "If experiencing acute sharp joint pain (rather than muscle soreness), consult a medical professional.";
                } else if (lower.contains("progressive overload") || lower.contains("increase weight")) {
                    return "**Progressive Overload Principles**:\n\n"
                            + "To build strength and muscle over time:\n"
                            + "1. **Weight**: Increase resistance load by 2.5 - 5% when you hit top rep range.\n"
                            + "2. **Reps**: Increase repetitions per set while maintaining clean form.\n"
                            + "3. **Density**: Reduce rest times slightly between working sets.";
                } else if (isRomanUrdu) {
                    return String.format("Aapka question '%s' receive ho gaya hai. Aap ke profile (Weight: %.1f kg, Target Goal: %s) ke mutabiq main workouts, diet plans, BMI, aur Form Check mein assist kar sakta hoon. Aap specific exercise ya meal plan ke baare mein pooch sakte hain!", query, weight, goal);
                } else {
                    return String.format("Regarding '%s': Based on your FitTrain profile (Weight: %.1f kg, Goal: %s, Daily Target: %d kcal), consistent training paired with targeted nutrition will yield optimum performance. Feel free to ask for specific exercise execution, meal plans, or app guides!", query, weight, goal, calTarget);
                }
        }
    }

    private static String generateStructuredWorkout(String name, String goal, int calTarget, int proteinTarget, String equipment) {
        if (goal.toLowerCase().contains("lose") || goal.toLowerCase().contains("fat")) {
            return String.format(Locale.US, "Hello %s! Here is your structured **Fat Loss & HIIT Routine** for today:\n\n"
                    + "1. **Bodyweight Squats**\n"
                    + "   - Sets & Reps: 4 sets x 15 reps\n"
                    + "   - Rest: 45 seconds between sets\n"
                    + "   - Form Tips: Keep chest upright, push knees outwards over toes.\n"
                    + "   - Common Mistakes: Letting knees cave inward.\n"
                    + "   - Modification: Chair squat touch.\n\n"
                    + "2. **Push-ups**\n"
                    + "   - Sets & Reps: 3 sets x 10-12 reps\n"
                    + "   - Rest: 60 seconds\n"
                    + "   - Form Tips: Maintain rigid core line, elbows at 45 degree angle.\n"
                    + "   - Modification: Knee push-ups or Wall push-ups.\n\n"
                    + "3. **Mountain Climbers**\n"
                    + "   - Sets & Reps: 3 sets x 40 seconds work\n"
                    + "   - Rest: 30 seconds\n"
                    + "   - Form Tips: Keep hips level, drive knees towards chest fast.\n\n"
                    + "• Session Duration: 25-30 mins | Target Caloric Deficit: %d kcal | Equipment: %s", name, calTarget, equipment);
        } else {
            return String.format(Locale.US, "Hello %s! Here is your structured **Hypertrophy & Strength Workout** for today:\n\n"
                    + "1. **Goblet Squats** (with %s)\n"
                    + "   - Sets & Reps: 4 sets x 10 reps\n"
                    + "   - Rest: 90 seconds\n"
                    + "   - Form Tips: Drive through heels, maintain flat lumbar spine.\n"
                    + "   - Common Mistakes: Rounding upper back.\n"
                    + "   - Progression: Increase weight load.\n\n"
                    + "2. **Single-Arm Rows**\n"
                    + "   - Sets & Reps: 4 sets x 12 reps per side\n"
                    + "   - Rest: 60 seconds\n"
                    + "   - Form Tips: Pull weight towards hip pocket, squeeze shoulder blade.\n\n"
                    + "3. **Plank Hold**\n"
                    + "   - Sets & Reps: 3 sets x 60 seconds hold\n"
                    + "   - Rest: 45 seconds\n\n"
                    + "• Session Duration: 35 mins | Protein Target: %d g post-workout | Equipment: %s", name, equipment, proteinTarget, equipment);
        }
    }
}

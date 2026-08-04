package com.fitness.app.utils;

import com.fitness.app.models.ProgressLog;
import com.fitness.app.models.User;
import com.fitness.app.models.WorkoutPlan;
import com.fitness.app.models.DietPlan;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecommendationEngine {

    public static List<String> getRecommendations(User user, Map<String, ProgressLog> logs) {
        List<String> list = new ArrayList<>();

        // Default Recommendations based on Goal
        String goal = user.getGoal();
        if ("Lose Fat".equalsIgnoreCase(goal)) {
            list.add("Focus on calorie-deficit meals. High fiber and low glycemic foods will help keep you full.");
            list.add("Integrate 30-40 minutes of moderate intensity cardio (HIIT or brisk walking) 3-4 times a week.");
        } else if ("Gain Muscle".equalsIgnoreCase(goal)) {
            list.add("Increase protein intake to 1.6g - 2.2g per kg of body weight to support muscle protein synthesis.");
            list.add("Focus on progressive overload: increase the weight or reps slightly in each workout session.");
        } else {
            list.add("Keep a consistent schedule. Balance strength training with active recovery days.");
            list.add("Prioritize nutrient-dense foods: colorful vegetables, whole grains, and healthy fats.");
        }

        // Analyze recent logs
        if (logs != null && !logs.isEmpty()) {
            double avgWater = 0;
            double avgSteps = 0;
            double avgSleep = 0;
            for (ProgressLog log : logs.values()) {
                avgWater += log.getWaterConsumedMl();
                avgSteps += log.getStepsCount();
                avgSleep += log.getSleepDurationMinutes();
            }
            avgWater /= logs.size();
            avgSteps /= logs.size();
            avgSleep /= logs.size();

            // Water intake check
            if (avgWater < 2000) {
                list.add("Your average water intake is below 2L. Try carrying a water bottle to reach your daily hydration target.");
            } else {
                list.add("Great job keeping hydrated! Hydration supports metabolism and workout recovery.");
            }

            // Steps check
            if (avgSteps < 6000) {
                list.add("Your daily step count is low. Aim for a short 15-minute walk after meals to increase active calorie burn.");
            } else if (avgSteps > 10000) {
                list.add("Fantastic daily activity level! Keep maintaining over 10,000 steps daily.");
            }

            // Sleep check
            if (avgSleep > 0 && avgSleep < 420) { // 7 hours
                list.add("Sleep is critical for muscle recovery. Try to secure 7-8 hours of restful sleep every night.");
            }
        }

        // General wellness advice
        list.add("Listen to your body. If you feel excessive fatigue, schedule an active recovery day with gentle stretching.");

        return list;
    }

    public static String getMotivationalMessage() {
        String[] motivations = {
            "Consistency is key! Every small step counts towards your larger goal.",
            "You are stronger than your excuses. Let's make today a great workout day!",
            "Progress is progress, no matter how small. Keep moving forward!",
            "Believe you can and you're halfway there. Keep pushing!",
            "Your body can stand almost anything. It's your mind that you have to convince.",
            "Don't stop when you are tired. Stop when you are done!"
        };
        int index = (int) (Math.random() * motivations.length);
        return motivations[index];
    }

    public static WorkoutPlan generateDailyWorkoutPlan(User user, String difficultyFeedback, boolean completedYesterday) {
        String level = user.getFitnessExperience();
        if (level == null || level.isEmpty()) level = "Beginner";

        int repsMultiplier = 0;
        if ("Easy".equalsIgnoreCase(difficultyFeedback)) {
            repsMultiplier = 2;
        } else if ("Hard".equalsIgnoreCase(difficultyFeedback) || !completedYesterday) {
            repsMultiplier = -2;
        }

        String goal = user.getGoal();
        String equip = user.getAvailableEquipment();
        if (equip == null || equip.isEmpty()) equip = "Bodyweight";

        List<WorkoutPlan.Exercise> exercises = new ArrayList<>();
        
        if ("Lose Fat".equalsIgnoreCase(goal) || "Lose Weight".equalsIgnoreCase(goal)) {
            if ("Bodyweight".equalsIgnoreCase(equip)) {
                exercises.add(new WorkoutPlan.Exercise("Jumping Jacks", "High-intensity cardio warm-up", 3, Math.max(10, 20 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Bodyweight Squats", "Lower body strength builder", 4, Math.max(8, 15 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Mountain Climbers", "Core and stamina push", 3, Math.max(15, 30 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Plank Hold", "Static abdominal hold", 3, 0, Math.max(30, 45 + repsMultiplier * 5)));
            } else if ("Dumbbells".equalsIgnoreCase(equip)) {
                exercises.add(new WorkoutPlan.Exercise("Dumbbell Goblet Squats", "Weighted squat targeting quads & glutes", 4, Math.max(8, 12 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Dumbbell Shoulder Press", "Upper body press", 3, Math.max(8, 10 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Dumbbell Renegade Rows", "Core stability and back strength", 3, Math.max(8, 10 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Burpees", "Full body cardio burst", 3, Math.max(5, 10 + repsMultiplier), 0));
            } else {
                exercises.add(new WorkoutPlan.Exercise("Barbell Back Squats", "Core lower body compound movement", 4, Math.max(6, 10 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Bench Press", "Chest builder", 4, Math.max(6, 10 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Lat Pulldown", "Back width builder", 3, Math.max(8, 12 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Treadmill Sprint", "Interval cardio sprint", 1, 0, 300));
            }
        } else if ("Gain Muscle".equalsIgnoreCase(goal) || "Build Muscle".equalsIgnoreCase(goal)) {
            if ("Bodyweight".equalsIgnoreCase(equip)) {
                exercises.add(new WorkoutPlan.Exercise("Push-ups", "Chest, shoulders, and triceps builder", 4, Math.max(8, 15 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Pike Push-ups", "Shoulder strength focus", 3, Math.max(6, 10 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Bulgarian Split Squats", "Single-leg hypertrophy builder", 3, Math.max(8, 12 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Pull-ups", "Back and biceps strength builder", 4, Math.max(4, 8 + repsMultiplier), 0));
            } else if ("Dumbbells".equalsIgnoreCase(equip)) {
                exercises.add(new WorkoutPlan.Exercise("Dumbbell Chest Press", "Weighted chest builder", 4, Math.max(8, 12 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Dumbbell Romanian Deadlifts", "Hamstrings and glutes builder", 4, Math.max(8, 10 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Dumbbell Bicep Curls", "Isolated arm hypertrophy", 3, Math.max(10, 15 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Dumbbell Lateral Raises", "Shoulder width builder", 3, Math.max(10, 12 + repsMultiplier), 0));
            } else {
                exercises.add(new WorkoutPlan.Exercise("Deadlifts", "Heavy full body posterior builder", 4, Math.max(4, 6 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Incline Dumbbell Press", "Upper chest builder", 4, Math.max(8, 12 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Barbell Rows", "Upper back strength", 4, Math.max(8, 10 + repsMultiplier), 0));
                exercises.add(new WorkoutPlan.Exercise("Tricep Cable Pushdowns", "Arm isolated extension", 3, Math.max(10, 15 + repsMultiplier), 0));
            }
        } else {
            exercises.add(new WorkoutPlan.Exercise("Burpees", "Full body explosive movement", 3, Math.max(5, 8 + repsMultiplier), 0));
            exercises.add(new WorkoutPlan.Exercise("Air Squats", "Lower body endurance builder", 4, Math.max(10, 15 + repsMultiplier), 0));
            exercises.add(new WorkoutPlan.Exercise("Superman Hold", "Lower back and posterior chain builder", 3, 0, Math.max(20, 30 + repsMultiplier * 3)));
            exercises.add(new WorkoutPlan.Exercise("Jumping Rope", "Steady cardio push", 3, 0, 120));
        }

        if ("Hard".equalsIgnoreCase(difficultyFeedback) && !completedYesterday) {
            exercises.clear();
            exercises.add(new WorkoutPlan.Exercise("Gentle Yoga & Stretching", "Active rest recovery stretches", 1, 0, 900));
        }

        String planId = "workout_" + System.currentTimeMillis();
        return new WorkoutPlan(planId, goal, level, exercises, System.currentTimeMillis());
    }

    public static DietPlan generateDailyDietPlan(User user, String dietFeedback, boolean completedYesterday) {
        return generateDailyDietPlan(user, dietFeedback, completedYesterday, null);
    }

    public static DietPlan generateDailyDietPlan(User user, String dietFeedback, boolean completedYesterday, String dateStr) {
        return generateDailyDietPlan(user, dietFeedback, completedYesterday, false, false, dateStr);
    }

    public static DietPlan generateDailyDietPlan(User user, String dietFeedback, boolean completedYesterday, boolean cheatMealEnabled, boolean hasWorkoutToday) {
        return generateDailyDietPlan(user, dietFeedback, completedYesterday, cheatMealEnabled, hasWorkoutToday, null);
    }

    public static DietPlan generateDailyDietPlan(User user, String dietFeedback, boolean completedYesterday, boolean cheatMealEnabled, boolean hasWorkoutToday, String dateStr) {
        double weight = user.getWeight() > 0 ? user.getWeight() : 70.0;
        double height = user.getHeight() > 0 ? user.getHeight() : 170.0;
        int age = user.getAge() > 0 ? user.getAge() : 25;
        String gender = user.getGender() != null ? user.getGender() : "Female";
        String goal = user.getGoal() != null ? user.getGoal() : "Maintain Weight";
        String dietPref = user.getDietaryPreference() != null ? user.getDietaryPreference() : "Balanced";
        String allergies = user.getAllergies() != null ? user.getAllergies() : "";
        String medicalConditions = user.getMedicalConditions() != null ? user.getMedicalConditions() : "";

        // 1. Calculate base daily calories (BMR + Activity Multiplier)
        int targetCalories = user.getDailyCaloriesGoal();
        if (targetCalories <= 0) {
            double bmr;
            if ("Male".equalsIgnoreCase(gender)) {
                bmr = 10 * weight + 6.25 * height - 5 * age + 5;
            } else {
                bmr = 10 * weight + 6.25 * height - 5 * age - 161;
            }

            double multiplier = 1.375; // Light activity default
            String act = user.getActivityLevel();
            if (act != null) {
                if (act.contains("Sedentary")) multiplier = 1.2;
                else if (act.contains("Light")) multiplier = 1.375;
                else if (act.contains("Moderat") || act.contains("Active")) multiplier = 1.55;
                else if (act.contains("Very") || act.contains("Athlete")) multiplier = 1.725;
            }

            double tdee = bmr * multiplier;
            if (goal.contains("Loss") || goal.contains("Fat")) {
                targetCalories = (int) (tdee - 500);
            } else if (goal.contains("Gain") || goal.contains("Weight Gain") || goal.contains("Muscle")) {
                targetCalories = (int) (tdee + 350);
            } else if (goal.contains("Recomposition")) {
                targetCalories = (int) (tdee - 150);
            } else {
                targetCalories = (int) tdee;
            }

            int minCal = "Male".equalsIgnoreCase(gender) ? 1500 : 1200;
            if (targetCalories < minCal) targetCalories = minCal;
        }

        if ("No".equalsIgnoreCase(dietFeedback)) {
            targetCalories = (int) (targetCalories * 0.95);
        }

        // 2. Macronutrient percentages
        int proteinPercent = 30;
        int carbsPercent = 40;
        int fatPercent = 30;

        if ("Keto".equalsIgnoreCase(dietPref)) {
            proteinPercent = 20;
            carbsPercent = 5;
            fatPercent = 75;
        } else if ("Vegan".equalsIgnoreCase(dietPref) || "Vegetarian".equalsIgnoreCase(dietPref)) {
            proteinPercent = 20;
            carbsPercent = 55;
            fatPercent = 25;
        } else if (goal.contains("Gain") || goal.contains("Muscle") || goal.contains("Performance")) {
            proteinPercent = 35;
            carbsPercent = 45;
            fatPercent = 20;
        } else if (goal.contains("Loss") || goal.contains("Fat")) {
            proteinPercent = 40;
            carbsPercent = 30;
            fatPercent = 30;
        }

        int targetProtein = (int) ((targetCalories * (proteinPercent / 100.0)) / 4.0);
        int targetCarbs = (int) ((targetCalories * (carbsPercent / 100.0)) / 4.0);
        int targetFat = (int) ((targetCalories * (fatPercent / 100.0)) / 9.0);
        int targetWater = user.getDailyWaterGoal() > 0 ? user.getDailyWaterGoal() : (int) (weight * 35);
        if (targetWater < 2000) targetWater = 2000;

        int targetFiber = (int) (targetCalories * 0.014); // 14g per 1000 kcal
        if (targetFiber < 25) targetFiber = 25;
        int targetSugar = (int) ((targetCalories * 0.08) / 4.0); // 8% of energy in sugar
        
        int targetSodium = 2000;
        if (medicalConditions.toLowerCase().contains("hypertension") || medicalConditions.toLowerCase().contains("blood pressure")) {
            targetSodium = 1500;
        }

        // Determine specific day of the week based on dateStr to ensure weekly variety
        int day = 1;
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                java.util.Date date = sdf.parse(dateStr);
                if (date != null) {
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.setTime(date);
                    day = cal.get(java.util.Calendar.DAY_OF_WEEK);
                }
            } catch (Exception e) {
                java.util.Calendar calendar = java.util.Calendar.getInstance();
                day = calendar.get(java.util.Calendar.DAY_OF_WEEK);
            }
        } else {
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            day = calendar.get(java.util.Calendar.DAY_OF_WEEK);
        }

        // 3. Generate meals with calorie splitting
        List<DietPlan.Meal> meals = new ArrayList<>();
        double pBreakfast = 0.20;
        double pMorningDrink = 0.05;
        double pMorningSnack = 0.10;
        double pLunch = 0.30;
        double pAfternoonDrink = 0.05;
        double pAfternoonSnack = 0.10;
        double pDinner = 0.20;
        
        double pCheatMeal = 0.0;
        if (cheatMealEnabled) {
            pCheatMeal = 0.20;
            pDinner = 0.10;
            pAfternoonSnack = 0.05;
        }
        
        int breakfastCal = (int) (targetCalories * pBreakfast);
        int morningDrinkCal = (int) (targetCalories * pMorningDrink);
        int morningSnackCal = (int) (targetCalories * pMorningSnack);
        int lunchCal = (int) (targetCalories * pLunch);
        int afternoonDrinkCal = (int) (targetCalories * pAfternoonDrink);
        int afternoonSnackCal = (int) (targetCalories * pAfternoonSnack);
        int dinnerCal = (int) (targetCalories * pDinner);
        int cheatMealCal = (int) (targetCalories * pCheatMeal);
        
        int totalUsed = breakfastCal + morningDrinkCal + morningSnackCal + lunchCal + afternoonDrinkCal + afternoonSnackCal + dinnerCal + cheatMealCal;
        int diff = targetCalories - totalUsed;
        dinnerCal += diff;

        meals.add(generateMealForType("Breakfast", breakfastCal, dietPref, allergies, medicalConditions, proteinPercent, carbsPercent, fatPercent, day));
        meals.add(generateMealForType("Morning Drink", morningDrinkCal, dietPref, allergies, medicalConditions, proteinPercent, carbsPercent, fatPercent, day));
        meals.add(generateMealForType("Morning Snack", morningSnackCal, dietPref, allergies, medicalConditions, proteinPercent, carbsPercent, fatPercent, day));
        meals.add(generateMealForType("Lunch", lunchCal, dietPref, allergies, medicalConditions, proteinPercent, carbsPercent, fatPercent, day));
        meals.add(generateMealForType("Afternoon Drink", afternoonDrinkCal, dietPref, allergies, medicalConditions, proteinPercent, carbsPercent, fatPercent, day));
        meals.add(generateMealForType("Afternoon Snack", afternoonSnackCal, dietPref, allergies, medicalConditions, proteinPercent, carbsPercent, fatPercent, day));
        if (cheatMealCal > 0) {
            meals.add(generateMealForType("Cheat Meal", cheatMealCal, dietPref, allergies, medicalConditions, proteinPercent, carbsPercent, fatPercent, day));
        } else {
            meals.add(generateMealForType("Dinner", dinnerCal, dietPref, allergies, medicalConditions, proteinPercent, carbsPercent, fatPercent, day));
        }

        String planId = "diet_" + System.currentTimeMillis();
        return new DietPlan(planId, targetCalories, targetProtein, targetCarbs, targetFat, targetWater, dietPref, meals, System.currentTimeMillis(), targetFiber, targetSugar, targetSodium);
    }

    private static DietPlan.Meal generateMealForType(String type, int targetCals, String dietPref, String allergies, String medicalConditions, int pPct, int cPct, int fPct, int day) {
        String name = "";
        String ingredients = "";
        String description = "";
        double protein = (targetCals * (pPct / 100.0)) / 4.0;
        double carbs = (targetCals * (cPct / 100.0)) / 4.0;
        double fat = (targetCals * (fPct / 100.0)) / 9.0;
        double fiber = targetCals * 0.014 / 4.0; // Scaled fiber target
        String servingSize = "1 Serving";
        String portionSize = "1 serving";

        boolean hasNuts = allergies.toLowerCase().contains("nut");
        boolean hasGluten = allergies.toLowerCase().contains("gluten") || allergies.toLowerCase().contains("wheat");
        boolean hasDairy = allergies.toLowerCase().contains("dairy") || allergies.toLowerCase().contains("milk");
        boolean hasSeafood = allergies.toLowerCase().contains("seafood") || allergies.toLowerCase().contains("fish");

        // Sunday = 1, Monday = 2, Tuesday = 3, Wednesday = 4, Thursday = 5, Friday = 6, Saturday = 7
        if ("Breakfast".equalsIgnoreCase(type)) {
            if ("Keto".equalsIgnoreCase(dietPref)) {
                switch (day) {
                    case 1: // Sunday
                        name = "Keto Avocado & Bacon Scramble";
                        ingredients = "3 organic eggs, 45g bacon slices, 50g fresh avocado, 1 tsp grass-fed butter, sea salt, pepper";
                        description = "Scramble eggs in butter, serve with crispy bacon and sliced fresh avocado.";
                        break;
                    case 2: // Monday
                        name = "Smoked Salmon & Spinach Omelet";
                        ingredients = "3 organic eggs, 80g smoked salmon, 30g baby spinach, 15g goat cheese, 1 tsp heavy cream, salt, pepper";
                        description = "Whisk eggs with heavy cream. Cook in skillet with spinach, fold in salmon and goat cheese.";
                        break;
                    case 3: // Tuesday
                        name = "Keto Steak & Fried Eggs";
                        ingredients = "150g grass-fed sirloin, 2 organic eggs, 1 tsp coconut oil, sea salt, black pepper";
                        description = "Pan-sear sirloin to medium-rare. Fry eggs in pan drippings. Garnish with cracked pepper.";
                        break;
                    case 4: // Wednesday
                        name = "Goat Cheese & Mushroom Frittata";
                        ingredients = "3 organic eggs, 50g sliced button mushrooms, 20g goat cheese, 10g spinach, 1 tsp olive oil";
                        description = "Sauté mushrooms and spinach in skillet. Pour in whisked eggs, crumble goat cheese on top, bake until set.";
                        break;
                    case 5: // Thursday
                        name = "Ham & Cheddar Crustless Quiche";
                        ingredients = "3 organic eggs, 50g diced lean ham, 25g shredded cheddar, 15ml heavy cream, parsley";
                        description = "Whisk eggs, cream, and ham. Pour into baking dish, top with cheddar, and bake at 375°F until golden.";
                        break;
                    case 6: // Friday
                        name = "Asparagus & Parmesan Egg Scramble";
                        ingredients = "3 organic eggs, 4 asparagus spears chopped, 15g shaved parmesan, 1 tsp butter, chives";
                        description = "Sauté asparagus in butter. Scramble with eggs and fold in parmesan and chives before serving.";
                        break;
                    default: // Saturday (7)
                        name = "Keto Flaxseed Pancakes with Butter";
                        ingredients = "30g flaxseed meal, 1 scoop vanilla protein, 1 egg, 50ml almond milk, 10g grass-fed butter";
                        description = "Whisk flaxseed, protein, egg, and milk. Cook on griddle, serve topped with melted butter.";
                        break;
                }
            } else if ("Vegan".equalsIgnoreCase(dietPref) || "Vegetarian".equalsIgnoreCase(dietPref)) {
                switch (day) {
                    case 1:
                        name = "Tofu & Turmeric Breakfast Scramble";
                        ingredients = "150g firm organic tofu, 40g spinach, 5 cherry tomatoes, 1 tsp olive oil, 1/2 tsp turmeric, sea salt";
                        description = "Crumble tofu into skillet with olive oil, sauté with vegetables, turmeric, and seasoning.";
                        break;
                    case 2:
                        name = "Berry Chia Protein Oats";
                        ingredients = "60g rolled oats, 250ml unsweetened almond milk, 1 scoop plant protein powder, 1 tbsp chia seeds, 50g fresh blueberries";
                        description = "Simmer rolled oats in almond milk, stir in protein powder and chia seeds. Top with blueberries.";
                        break;
                    case 3:
                        name = "Peanut Butter & Banana Oats";
                        ingredients = "60g rolled oats, 200ml almond milk, 1 tbsp natural peanut butter, 1/2 banana sliced, cinnamon";
                        description = "Cook oats in almond milk. Stir in cinnamon and peanut butter, top with sliced banana.";
                        break;
                    case 4:
                        name = "Avocado & Tomato Toast on Seeded Bread";
                        ingredients = "2 slices seeded bread, 1/2 ripe avocado, 1 sliced roma tomato, red pepper flakes, lemon juice";
                        description = "Toast seeded bread. Mash avocado with lemon juice, spread on toast, top with tomatoes and pepper flakes.";
                        break;
                    case 5:
                        name = "Green Garden Smoothie Bowl";
                        ingredients = "1 cup spinach, 1/2 cucumber, 1 scoop plant protein, 200ml coconut water, 50g berries, 1 tbsp hemp seeds";
                        description = "Blend spinach, cucumber, protein, and coconut water. Pour into bowl and top with berries and hemp seeds.";
                        break;
                    case 6:
                        name = "Vegan Almond Butter Oatmeal";
                        ingredients = "60g rolled oats, 200ml oat milk, 1 tbsp almond butter, 10g pumpkin seeds, 1 tsp maple syrup";
                        description = "Simmer oats in oat milk. Drizzle with almond butter and maple syrup, garnish with pumpkin seeds.";
                        break;
                    default: // Saturday (7)
                        name = "Chia Seed Pudding with Berries";
                        ingredients = "3 tbsp chia seeds, 200ml coconut milk, 1 tsp maple syrup, 50g fresh strawberries, vanilla extract";
                        description = "Whisk chia seeds, coconut milk, maple syrup, and vanilla. Let set overnight. Top with strawberries.";
                        break;
                }
            } else { // Balanced / High Protein
                switch (day) {
                    case 1:
                        name = "Greek Yogurt Parfait with Honey";
                        ingredients = "200g non-fat Greek yogurt, 30g honey oat granola, 1 tsp organic honey, 50g mixed fresh berries, 10g walnuts";
                        description = "Layer non-fat yogurt with granola and mixed berries in a glass. Drizzle with raw organic honey.";
                        break;
                    case 2:
                        name = "High Protein Banana Pancakes";
                        ingredients = "50g oat flour, 1 scoop vanilla whey protein, 100ml egg whites, 1/2 mashed banana, 1 tbsp organic maple syrup, cinnamon";
                        description = "Blend ingredients to form batter. Cook on non-stick griddle. Drizzle with maple syrup.";
                        break;
                    case 3:
                        name = "Whole Wheat Avocado & Egg Toast";
                        ingredients = "2 slices whole wheat toast, 1/2 avocado, 2 poached organic eggs, sea salt, red pepper flakes";
                        description = "Toast bread, mash avocado on top. Place poached eggs over avocado, sprinkle with salt and red pepper flakes.";
                        break;
                    case 4:
                        name = "Egg White Scramble with Spinach";
                        ingredients = "200ml egg whites, 50g baby spinach, 5 cherry tomatoes, 1 slice whole wheat toast, 1 tsp olive oil";
                        description = "Sauté spinach and tomatoes in olive oil, scramble with egg whites. Serve with dry whole wheat toast.";
                        break;
                    case 5:
                        name = "Turkey Bacon & Swiss Cheese Melt";
                        ingredients = "2 slices turkey bacon, 1 slice Swiss cheese, 1 english muffin, 1 organic egg, spinach";
                        description = "Toast english muffin. Cook egg and bacon. Assemble muffin with spinach, bacon, egg, and Swiss cheese.";
                        break;
                    case 6:
                        name = "Cottage Cheese & Peach Bowl";
                        ingredients = "200g low-fat cottage cheese, 1 sliced fresh peach, 10g sliced almonds, 1 tsp organic honey";
                        description = "Spoon cottage cheese into a bowl. Top with peach slices and almonds, drizzle with honey.";
                        break;
                    default: // Saturday (7)
                        name = "High Protein Oatmeal with Walnuts";
                        ingredients = "60g rolled oats, 250ml skim milk, 1 scoop whey protein, 15g chopped walnuts, cinnamon";
                        description = "Cook oats in skim milk. Stir in whey protein and cinnamon, top with chopped walnuts.";
                        break;
                }
            }
        } else if ("Morning Drink".equalsIgnoreCase(type)) {
            if ("Keto".equalsIgnoreCase(dietPref)) {
                switch (day) {
                    case 1:
                        name = "Bulletproof Keto Coffee";
                        ingredients = "1 cup black coffee, 1 tbsp MCT oil, 1 tbsp grass-fed unsalted butter";
                        description = "Blend freshly brewed black coffee with MCT oil and butter until frothy.";
                        break;
                    case 2:
                        name = "Unsweetened Matcha Green Tea Latte";
                        ingredients = "1 tsp ceremonial matcha powder, 50ml hot water, 200ml unsweetened almond milk, 1 drop liquid stevia";
                        description = "Whisk matcha in hot water, heat almond milk and froth, combine and sweeten with stevia.";
                        break;
                    case 3:
                        name = "Keto Iced Coffee with Heavy Cream";
                        ingredients = "200ml cold brew coffee, 30ml heavy whipping cream, ice, sugar-free vanilla syrup";
                        description = "Pour cold brew over ice, stir in heavy whipping cream and sugar-free syrup.";
                        break;
                    case 4:
                        name = "Organic Hot Black Coffee";
                        ingredients = "1 cup brewed organic coffee beans, hot filtered water";
                        description = "Brew premium coffee beans in a drip filter or French press, serve piping hot.";
                        break;
                    case 5:
                        name = "Bulletproof Cocoa Drink";
                        ingredients = "1 tbsp unsweetened cocoa, 250ml hot water, 1 tbsp coconut oil, 1 tbsp grass-fed butter, stevia";
                        description = "Blend cocoa powder, hot water, coconut oil, butter, and stevia until frothy and smooth.";
                        break;
                    case 6:
                        name = "Iced Matcha Green Tea";
                        ingredients = "1 tsp matcha powder, 250ml cold water, ice, lemon slice";
                        description = "Whisk matcha in cold water, pour over ice, garnish with a fresh lemon slice.";
                        break;
                    default: // Saturday (7)
                        name = "Keto Collagen Coffee";
                        ingredients = "1 cup black coffee, 1 scoop collagen peptides, 15ml heavy cream";
                        description = "Stir collagen peptides and heavy cream into hot black coffee until fully dissolved.";
                        break;
                }
            } else if ("Vegan".equalsIgnoreCase(dietPref) || "Vegetarian".equalsIgnoreCase(dietPref)) {
                switch (day) {
                    case 1:
                        name = "Superfood Green Tea";
                        ingredients = "1 green tea bag, 250ml hot water, 1 slice organic lemon, 3 fresh mint leaves";
                        description = "Steep green tea bag in hot water for 3 minutes, discard bag, stir in lemon and mint.";
                        break;
                    case 2:
                        name = "Almond Milk Chai Latte";
                        ingredients = "1 chai tea bag, 100ml hot water, 150ml unsweetened almond milk, 1 tsp maple syrup, cinnamon";
                        description = "Steep tea bag in hot water. Warm and froth almond milk, combine, sweeten, and dust with cinnamon.";
                        break;
                    case 3:
                        name = "Matcha Tea Drink";
                        ingredients = "1 tsp matcha powder, 250ml hot water, 1 tsp maple syrup";
                        description = "Whisk ceremonial matcha in hot water until frothy, stir in maple syrup.";
                        break;
                    case 4:
                        name = "Organic Black Coffee";
                        ingredients = "1 cup brewed organic coffee, hot filtered water";
                        description = "Brew premium coffee beans, serve hot without sugar or milk.";
                        break;
                    case 5:
                        name = "Golden Milk Turmeric Tea";
                        ingredients = "250ml unsweetened almond milk, 1/2 tsp turmeric, 1/4 tsp ginger, pinch of black pepper, 1 tsp maple syrup";
                        description = "Whisk almond milk and spices in a saucepan over medium heat. Simmer for 5 minutes, sweeten, and serve.";
                        break;
                    case 6:
                        name = "Lemon Ginger Infusion";
                        ingredients = "250ml hot water, 1 tbsp fresh lemon juice, 1/2 inch sliced ginger root, 1 tsp maple syrup";
                        description = "Steep ginger slices in hot water for 5 minutes. Stir in lemon juice and maple syrup.";
                        break;
                    default: // Saturday (7)
                        name = "Almond Butter Protein Shake";
                        ingredients = "250ml almond milk, 1 scoop pea protein, 1 tbsp almond butter, ice";
                        description = "Blend protein powder, almond milk, and almond butter with ice until smooth.";
                        break;
                }
            } else { // Balanced / High Protein
                switch (day) {
                    case 1:
                        name = "Organic French Press Black Coffee";
                        ingredients = "2 tbsp organic coffee grounds, 200ml hot filtered water";
                        description = "Steep coffee grounds in French press with hot water for 4 minutes, press plunger down and serve.";
                        break;
                    case 2:
                        name = "High Protein Iced Latte";
                        ingredients = "1 shot organic espresso, 200ml skim milk, 1/2 scoop vanilla whey protein, ice";
                        description = "Dissolve whey protein in milk, add espresso shot and pour over ice.";
                        break;
                    case 3:
                        name = "Organic Green Tea";
                        ingredients = "1 green tea bag, 250ml hot filtered water";
                        description = "Steep green tea bag in hot water for 2-3 minutes, discard bag and drink warm.";
                        break;
                    case 4:
                        name = "Double Espresso Macchiato";
                        ingredients = "2 shots organic espresso, 30ml frothed skim milk";
                        description = "Pull two shots of espresso, top with a small dollop of warm frothed skim milk.";
                        break;
                    case 5:
                        name = "Vanilla Whey Protein Shake";
                        ingredients = "1 scoop vanilla whey protein, 250ml cold water, ice";
                        description = "Shake protein powder and water with ice until fully blended.";
                        break;
                    case 6:
                        name = "Fresh Orange Juice";
                        ingredients = "3 medium organic oranges squeezed";
                        description = "Squeeze fresh oranges, serve chilled over ice immediately.";
                        break;
                    default: // Saturday (7)
                        name = "Ginger Matcha Green Tea";
                        ingredients = "1 tsp matcha powder, 1/2 tsp ginger powder, 250ml hot water";
                        description = "Whisk matcha and ginger powder in hot water until frothy, serve warm.";
                        break;
                }
            }
        } else if ("Morning Snack".equalsIgnoreCase(type)) {
            if ("Keto".equalsIgnoreCase(dietPref)) {
                switch (day) {
                    case 1:
                        name = "Keto Deviled Eggs";
                        ingredients = "2 large organic eggs, 1 tbsp avocado oil mayonnaise, 1/2 tsp yellow mustard, pinch of paprika, chives";
                        description = "Halve hard-boiled eggs, mash yolks with mayo and mustard. Pipe back into egg whites, garnish with paprika.";
                        break;
                    case 2:
                        name = "Herb & Garlic Cheese Bites";
                        ingredients = "40g premium cheddar cheese cubes, 5 black olives, pinch of dried oregano, sea salt";
                        description = "Cut block cheese into cubes, toss with olives, pinch of oregano, and sea salt.";
                        break;
                    case 3:
                        name = "Salted Pecan Halves";
                        ingredients = "30g pecan halves, 1/4 tsp sea salt, 2g coconut oil";
                        description = "Toss pecan halves in warm coconut oil, sprinkle with sea salt.";
                        break;
                    case 4:
                        name = "Avocado Slices with Olive Oil";
                        ingredients = "1/2 fresh avocado, 1 tsp extra virgin olive oil, sea salt, black pepper";
                        description = "Slice avocado, drizzle with olive oil, sprinkle with sea salt and black pepper.";
                        break;
                    case 5:
                        name = "Cucumber Slices with Cream Cheese";
                        ingredients = "100g fresh cucumber slices, 30g organic cream cheese, pinch of dill";
                        description = "Spread cream cheese onto cucumber slices, garnish with dill.";
                        break;
                    case 6:
                        name = "Macadamia Nuts Handful";
                        ingredients = "30g raw macadamia nuts";
                        description = "Enjoy raw unsalted macadamia nuts as a quick high-fat keto snack.";
                        break;
                    default: // Saturday (7)
                        name = "Bacon Wrapped Celery Sticks";
                        ingredients = "2 celery sticks, 2 slices bacon cooked crispy";
                        description = "Wrap celery sticks in crispy bacon slices and serve.";
                        break;
                }
            } else if ("Vegan".equalsIgnoreCase(dietPref) || "Vegetarian".equalsIgnoreCase(dietPref)) {
                switch (day) {
                    case 1:
                        name = "Mixed Seasonal Berries Bowl";
                        ingredients = "50g strawberries, 50g blueberries, 50g raspberries";
                        description = "Wash and mix berries together in a bowl for a clean, antioxidant-rich fruit snack.";
                        break;
                    case 2:
                        name = "Apple Slices with Seed Butter";
                        ingredients = "1 medium organic apple, 20g unsweetened sunflower seed butter";
                        description = "Slice the apple, serve alongside seed butter for a high-fiber dipping snack.";
                        break;
                    case 3:
                        name = "Raw Pumpkin Seeds Handful";
                        ingredients = "30g raw unsalted pumpkin seeds";
                        description = "Enjoy raw pumpkin seeds for a rich source of zinc and healthy plant fats.";
                        break;
                    case 4:
                        name = "Sliced Pears & Walnuts";
                        ingredients = "1 organic pear sliced, 15g walnut halves";
                        description = "Serve fresh sliced pear alongside raw walnut halves.";
                        break;
                    case 5:
                        name = "Baby Carrots & Hummus";
                        ingredients = "100g baby carrots, 50g organic garlic hummus";
                        description = "Dip fresh baby carrots in organic hummus.";
                        break;
                    case 6:
                        name = "Chilled Watermelon Cubes";
                        ingredients = "150g fresh watermelon cubes";
                        description = "Slice and chill fresh watermelon, serve cold.";
                        break;
                    default: // Saturday (7)
                        name = "Dry Roasted Chickpeas";
                        ingredients = "50g dry roasted chickpeas, pinch of sea salt, cumin";
                        description = "Toss chickpeas with cumin and salt, dry roast in oven until crispy.";
                        break;
                }
            } else { // Balanced / High Protein
                switch (day) {
                    case 1:
                        name = "Cottage Cheese & Pineapple Cup";
                        ingredients = "150g low-fat cottage cheese, 50g fresh pineapple chunks";
                        description = "Top low-fat cottage cheese with sweet pineapple chunks for a high-protein mid-morning snack.";
                        break;
                    case 2:
                        name = "Mixed Fruits & Nuts Blend";
                        ingredients = "1 organic pear, 15g unsalted raw almonds, 10g walnuts";
                        description = "Enjoy raw seasonal fruit alongside fresh unsalted nuts.";
                        break;
                    case 3:
                        name = "Organic Pear & Raw Almonds";
                        ingredients = "1 organic pear, 20g raw almonds";
                        description = "Enjoy a fresh pear alongside a handful of raw almonds.";
                        break;
                    case 4:
                        name = "Hard-Boiled Egg & Cucumber";
                        ingredients = "1 hard-boiled egg, 100g cucumber slices, sea salt";
                        description = "Slice egg and cucumber, sprinkle with sea salt, serve fresh.";
                        break;
                    case 5:
                        name = "Rice Cake with Sunflower Butter";
                        ingredients = "2 brown rice cakes, 1 tbsp sunflower seed butter";
                        description = "Spread sunflower seed butter evenly on top of crispy rice cakes.";
                        break;
                    case 6:
                        name = "Low-fat Yogurt Parfait";
                        ingredients = "150g low-fat yogurt, 30g berries, 10g sliced almonds";
                        description = "Layer low-fat yogurt with fresh berries and almonds in a cup.";
                        break;
                    default: // Saturday (7)
                        name = "Banana with Peanut Butter";
                        ingredients = "1 organic banana, 1 tbsp natural peanut butter";
                        description = "Peel banana, dip in or spread with natural peanut butter.";
                        break;
                }
            }
        } else if ("Lunch".equalsIgnoreCase(type)) {
            if ("Keto".equalsIgnoreCase(dietPref)) {
                switch (day) {
                    case 1:
                        name = "Grilled Salmon Caesar Salad";
                        ingredients = "150g fresh Atlantic salmon, 150g romaine lettuce, 15g parmesan flakes, 1 tbsp Caesar dressing, 1 tbsp olive oil";
                        description = "Sear salmon in olive oil. Chop romaine lettuce, toss with dressing and parmesan, top with warm salmon.";
                        break;
                    case 2:
                        name = "Pan-Seared Ribeye Steak & Asparagus";
                        ingredients = "180g grass-fed ribeye steak, 100g fresh asparagus, 15g unsalted butter, 1 garlic clove, sea salt, pepper";
                        description = "Sear steak in butter and garlic in a cast-iron skillet. Sauté asparagus in pan drippings.";
                        break;
                    case 3:
                        name = "Keto Garlic Butter Sea Bass";
                        ingredients = "150g sea bass fillet, 100g spinach, 20g butter, 1 tsp garlic, lemon juice";
                        description = "Sear sea bass in garlic butter. Serve over sautéed spinach with a splash of lemon.";
                        break;
                    case 4:
                        name = "Bacon-Wrapped Chicken Breast";
                        ingredients = "150g chicken breast, 2 bacon slices, 50g broccoli, 1 tbsp olive oil";
                        description = "Wrap chicken in bacon, bake at 400°F for 25 minutes. Serve with roasted broccoli.";
                        break;
                    case 5:
                        name = "Ribeye Steak with Garlic Butter";
                        ingredients = "180g ribeye steak, 15g butter, 1 garlic clove, 100g asparagus, sea salt";
                        description = "Pan-sear ribeye steak in garlic butter. Serve with sautéed asparagus.";
                        break;
                    case 6:
                        name = "Smoked Salmon & Cucumber Wrap";
                        ingredients = "100g smoked salmon, 1 large cucumber sliced thin, 30g cream cheese, dill";
                        description = "Spread cream cheese on cucumber slices, wrap with smoked salmon and garnish with dill.";
                        break;
                    default: // Saturday (7)
                        name = "Keto Avocado Pork Chops";
                        ingredients = "150g pork chop, 1/2 avocado, 1 tbsp butter, sea salt, garlic";
                        description = "Sear pork chop in butter and garlic. Serve topped with fresh sliced avocado.";
                        break;
                }
            } else if ("Vegan".equalsIgnoreCase(dietPref) || "Vegetarian".equalsIgnoreCase(dietPref)) {
                switch (day) {
                    case 1:
                        name = "Lentil & Vegetable Soup with Avocado";
                        ingredients = "1 cup cooked brown lentils, 100g mixed soup vegetables (carrots, celery, spinach), 1 tsp olive oil, 1/2 avocado sliced";
                        description = "Simmer lentils and vegetables in vegetable broth with olive oil. Serve hot topped with avocado slices.";
                        break;
                    case 2:
                        name = "Mediterranean Chickpea Quinoa Salad";
                        ingredients = "80g cooked quinoa, 100g cooked chickpeas, 1/2 cucumber, 5 cherry tomatoes, 1 tbsp olive oil, lemon juice";
                        description = "Combine quinoa, chickpeas, cucumber, and tomatoes. Toss with olive oil and fresh lemon juice.";
                        break;
                    case 3:
                        name = "Black Bean & Avocado Salad Bowl";
                        ingredients = "100g black beans, 1/2 avocado, 50g corn, 100g mixed greens, 1 tbsp olive oil, lime dressing";
                        description = "Toss black beans, avocado, corn, and greens with olive oil and fresh lime dressing.";
                        break;
                    case 4:
                        name = "Tofu stir-fry with Brown Rice";
                        ingredients = "150g tofu, 100g cooked brown rice, 100g mixed vegetables, 1 tbsp soy sauce, 1 tsp sesame oil";
                        description = "Cube tofu, stir-fry with vegetables in sesame oil and soy sauce. Serve over brown rice.";
                        break;
                    case 5:
                        name = "Roasted Cauliflower & Lentil Bowl";
                        ingredients = "1 cup cooked lentils, 100g roasted cauliflower, 50g spinach, 1 tbsp tahini, lemon juice";
                        description = "Roast cauliflower. Place over lentils and spinach, drizzle with tahini and lemon juice.";
                        break;
                    case 6:
                        name = "Quinoa salad with Edamame & Peppers";
                        ingredients = "80g cooked quinoa, 50g edamame, 50g chopped bell peppers, cilantro, lime juice, 1 tsp olive oil";
                        description = "Mix quinoa, edamame, and bell peppers. Toss with olive oil, lime juice, and cilantro.";
                        break;
                    default: // Saturday (7)
                        name = "Sweet Potato Quinoa Chili";
                        ingredients = "80g cooked quinoa, 80g sweet potato chunks, 80g black beans, 100g diced tomatoes, spices";
                        description = "Simmer sweet potatoes, black beans, and tomatoes. Stir in quinoa, season and serve.";
                        break;
                }
            } else { // Balanced / High Protein
                switch (day) {
                    case 1:
                        name = "Turkey & Hummus Wrap";
                        ingredients = "1 whole wheat wrap, 100g sliced turkey breast, 2 tbsp organic hummus, 50g cucumber slices, spinach leaves";
                        description = "Spread hummus on wrap, layer with turkey, cucumber, and spinach, roll tightly.";
                        break;
                    case 2:
                        name = "Grilled Lemon-Herb Chicken Breast";
                        ingredients = "150g chicken breast, 100g cooked brown basmati rice, 80g steamed broccoli florets, 1 tsp olive oil, lemon juice";
                        description = "Grill chicken breast seasoned with lemon and herbs. Serve with basmati rice and broccoli.";
                        break;
                    case 3:
                        name = "Baked Salmon & Brown Rice";
                        ingredients = "150g salmon fillet, 100g brown rice, 100g asparagus, 1 tsp olive oil, dill";
                        description = "Bake salmon with olive oil and dill. Serve with brown rice and roasted asparagus.";
                        break;
                    case 4:
                        name = "Lean Beef Stir-fry with Quinoa";
                        ingredients = "150g lean beef strips, 80g cooked quinoa, 100g broccoli, 1 tsp sesame oil, soy sauce";
                        description = "Sauté beef and broccoli in sesame oil and low-sodium soy sauce. Serve over quinoa.";
                        break;
                    case 5:
                        name = "Grilled Chicken & Sweet Potato";
                        ingredients = "150g chicken breast, 120g baked sweet potato, 80g green beans, 1 tsp olive oil";
                        description = "Grill chicken breast. Serve alongside baked sweet potato chunks and green beans.";
                        break;
                    case 6:
                        name = "Tuna Salad Whole Wheat Sandwich";
                        ingredients = "1 can tuna in water, 1 tbsp light mayo, 2 slices whole wheat bread, lettuce, tomato";
                        description = "Mix tuna with mayo. Layer on toasted whole wheat bread with lettuce and tomato.";
                        break;
                    default: // Saturday (7)
                        name = "Turkey Meatballs & Whole Wheat Pasta";
                        ingredients = "150g turkey meatballs, 80g whole wheat spaghetti, 100g tomato marinara sauce, parsley";
                        description = "Cook spaghetti, toss with marinara sauce. Serve topped with turkey meatballs and parsley.";
                        break;
                }
            }
        } else if ("Afternoon Drink".equalsIgnoreCase(type)) {
            if ("Keto".equalsIgnoreCase(dietPref)) {
                switch (day) {
                    case 1:
                        name = "Matcha Collagen Shake";
                        ingredients = "1 scoop collagen peptides, 1 tsp matcha powder, 250ml unsweetened coconut milk, ice";
                        description = "Blend collagen peptides, matcha, and coconut milk with ice until smooth.";
                        break;
                    case 2:
                        name = "Vanilla Avocado Keto Shake";
                        ingredients = "1 scoop zero-carb vanilla protein, 1/4 fresh avocado, 250ml unsweetened coconut milk, stevia";
                        description = "Blend zero-carb protein powder, avocado, and coconut milk until thick and creamy.";
                        break;
                    case 3:
                        name = "Chocolate Peanut Butter Keto Shake";
                        ingredients = "1 scoop chocolate whey, 1 tbsp peanut butter, 250ml almond milk, ice, stevia";
                        description = "Blend chocolate protein, peanut butter, and almond milk with ice until smooth.";
                        break;
                    case 4:
                        name = "Unsweetened Coconut Milk Latte";
                        ingredients = "1 shot espresso, 200ml unsweetened coconut milk, ice";
                        description = "Pour espresso shot over frothed coconut milk and ice.";
                        break;
                    case 5:
                        name = "Keto Protein Drink";
                        ingredients = "1 scoop isolate protein, 250ml water, 10ml heavy cream, ice";
                        description = "Shake protein powder, heavy cream, and water over ice.";
                        break;
                    case 6:
                        name = "Matcha Avocado Smoothie";
                        ingredients = "1 tsp matcha, 1/4 avocado, 200ml almond milk, stevia";
                        description = "Blend matcha, avocado, and almond milk until thick and creamy.";
                        break;
                    default: // Saturday (7)
                        name = "Keto Strawberry Shake";
                        ingredients = "1 scoop vanilla whey, 40g fresh strawberries, 200ml unsweetened coconut milk, ice";
                        description = "Blend vanilla protein, strawberries, coconut milk, and ice.";
                        break;
                }
            } else if ("Vegan".equalsIgnoreCase(dietPref) || "Vegetarian".equalsIgnoreCase(dietPref)) {
                switch (day) {
                    case 1:
                        name = "Unsweetened Soy Milk";
                        ingredients = "250ml organic unsweetened soy milk";
                        description = "Pour chilled unsweetened organic soy milk into a glass. Great source of plant-based protein.";
                        break;
                    case 2:
                        name = "Berry Blast Plant Smoothie";
                        ingredients = "1 scoop vanilla plant protein, 200ml water, 50g frozen mixed berries, ice";
                        description = "Blend plant protein powder with water, mixed berries, and ice until smooth.";
                        break;
                    case 3:
                        name = "Unsweetened Oat Milk";
                        ingredients = "250ml organic unsweetened oat milk";
                        description = "Pour chilled unsweetened organic oat milk into a glass, enjoy warm or cold.";
                        break;
                    case 4:
                        name = "Green Spinach & Banana Smoothie";
                        ingredients = "1 scoop plant protein, 200ml almond milk, 50g spinach, 1/2 banana, ice";
                        description = "Blend plant protein, almond milk, spinach, banana, and ice.";
                        break;
                    case 5:
                        name = "Almond Butter Protein Shake";
                        ingredients = "1 scoop plant protein, 250ml water, 1 tbsp almond butter, ice";
                        description = "Blend plant protein, water, and almond butter with ice until smooth.";
                        break;
                    case 6:
                        name = "Chilled Hemp Seed Drink";
                        ingredients = "250ml organic hemp milk, dash of cinnamon";
                        description = "Pour chilled organic hemp milk into a glass, dust with cinnamon.";
                        break;
                    default: // Saturday (7)
                        name = "Mixed Berry Chia Smoothie";
                        ingredients = "250ml coconut water, 1 tbsp chia seeds, 50g mixed berries, ice";
                        description = "Blend coconut water, chia seeds, and mixed berries with ice until smooth.";
                        break;
                }
            } else { // Balanced / High Protein
                switch (day) {
                    case 1:
                        name = "Chocolate Whey Protein Shake";
                        ingredients = "1 scoop chocolate whey protein powder, 250ml water, dash of cinnamon, ice";
                        description = "Shake protein powder with water, ice, and a dash of cinnamon until fully dissolved.";
                        break;
                    case 2:
                        name = "Post-Workout Recovery Shake & Berries";
                        ingredients = "1.5 scoops whey protein powder, 250ml skim milk, 50g fresh organic blueberries";
                        description = "Shake protein powder with milk until smooth, enjoy with fresh organic blueberries.";
                        break;
                    case 3:
                        name = "Banana Strawberry Whey Smoothie";
                        ingredients = "1 scoop vanilla whey, 1/2 banana, 50g strawberries, 200ml skim milk, ice";
                        description = "Blend whey protein, banana, strawberries, and milk with ice until smooth.";
                        break;
                    case 4:
                        name = "Vanilla Whey Shake with Almond Milk";
                        ingredients = "1 scoop vanilla whey, 250ml almond milk, dash of cinnamon, ice";
                        description = "Shake vanilla whey with almond milk, cinnamon, and ice.";
                        break;
                    case 5:
                        name = "Chilled Skim Milk Glass";
                        ingredients = "250ml pasteurized organic skim milk";
                        description = "Pour chilled skim milk into a glass and serve.";
                        break;
                    case 6:
                        name = "Fresh Watermelon Smoothie";
                        ingredients = "150g watermelon chunks, 1 scoop vanilla whey, ice";
                        description = "Blend watermelon chunks and vanilla whey with ice until smooth.";
                        break;
                    default: // Saturday (7)
                        name = "Protein Berry Blast Shake";
                        ingredients = "1.5 scoops whey protein, 200ml water, 50g mixed berries, ice";
                        description = "Blend whey protein and mixed berries with water and ice.";
                        break;
                }
            }
        } else if ("Afternoon Snack".equalsIgnoreCase(type)) {
            if ("Keto".equalsIgnoreCase(dietPref)) {
                switch (day) {
                    case 1:
                        name = "Cucumber & Cream Cheese Slices";
                        ingredients = "100g fresh cucumber slices, 40g organic cream cheese, pinch of fresh dill, sea salt";
                        description = "Spread cream cheese onto thick cucumber slices, garnish with dill and sea salt.";
                        break;
                    case 2:
                        name = "Keto Salted Almonds";
                        ingredients = "30g raw almonds, 1/2 tsp sea salt, 2g coconut oil";
                        description = "Toss almonds in warm coconut oil, sprinkle with sea salt, and serve.";
                        break;
                    case 3:
                        name = "Cheddar Cheese Block Cubes";
                        ingredients = "40g premium cheddar cheese block cubes";
                        description = "Cut cheese block into cubes and enjoy as a high-fat keto snack.";
                        break;
                    case 4:
                        name = "Pumpkin Seeds Sautéed in Butter";
                        ingredients = "30g pumpkin seeds, 10g butter, sea salt";
                        description = "Sauté pumpkin seeds in butter until golden, sprinkle with sea salt.";
                        break;
                    case 5:
                        name = "Hard-Boiled Eggs with Mayo";
                        ingredients = "2 hard-boiled eggs, 1 tbsp avocado oil mayonnaise, salt";
                        description = "Slice eggs, serve topped with mayonnaise and a pinch of salt.";
                        break;
                    case 6:
                        name = "Celery Sticks with Almond Butter";
                        ingredients = "3 celery sticks, 20g natural unsweetened almond butter";
                        description = "Spread almond butter on celery sticks and serve.";
                        break;
                    default: // Saturday (7)
                        name = "Beef Jerky & Olives";
                        ingredients = "30g sugar-free beef jerky, 5 black olives";
                        description = "Enjoy sugar-free beef jerky alongside black olives.";
                        break;
                }
            } else if ("Vegan".equalsIgnoreCase(dietPref) || "Vegetarian".equalsIgnoreCase(dietPref)) {
                switch (day) {
                    case 1:
                        name = "Roasted Spicy Chickpeas";
                        ingredients = "50g boiled chickpeas, 1 tsp olive oil, cayenne pepper, pinch of sea salt";
                        description = "Toss chickpeas in olive oil and cayenne pepper, roast at 400°F (200°C) until crispy.";
                        break;
                    case 2:
                        name = "Vegetable Sticks with Hummus";
                        ingredients = "50g organic garlic hummus, 80g fresh celery sticks, 80g carrot sticks";
                        description = "Serve chilled organic garlic hummus alongside fresh celery and carrot sticks.";
                        break;
                    case 3:
                        name = "Edamame Bowls with Sea Salt";
                        ingredients = "100g fresh edamame in pods, sea salt";
                        description = "Steam edamame pods, sprinkle with coarse sea salt, and serve warm.";
                        break;
                    case 4:
                        name = "Apple Slices with Almond Butter";
                        ingredients = "1 organic apple, 20g natural almond butter";
                        description = "Slice apple, serve alongside natural almond butter for dipping.";
                        break;
                    case 5:
                        name = "Spiced Beetroot Chips";
                        ingredients = "50g baked beetroot chips, pinch of sea salt, rosemary";
                        description = "Bake thinly sliced beetroot with rosemary and salt until crispy.";
                        break;
                    case 6:
                        name = "Sunflower Seeds Handful";
                        ingredients = "30g unsalted raw sunflower seeds";
                        description = "Enjoy raw sunflower seeds as a minerals-rich snacking seed.";
                        break;
                    default: // Saturday (7)
                        name = "Guacamole & Carrot Sticks";
                        ingredients = "50g fresh guacamole, 100g baby carrots";
                        description = "Serve fresh guacamole alongside baby carrot sticks.";
                        break;
                }
            } else { // Balanced / High Protein
                switch (day) {
                    case 1:
                        name = "Turkey Jerky & Celery";
                        ingredients = "30g lean turkey jerky, 3 celery sticks";
                        description = "Enjoy lean high-protein turkey jerky alongside fresh crunchy celery sticks.";
                        break;
                    case 2:
                        name = "Rice Cakes with Peanut Butter";
                        ingredients = "2 brown rice cakes, 1 tbsp natural peanut butter";
                        description = "Spread natural unsweetened peanut butter evenly on top of crispy rice cakes.";
                        break;
                    case 3:
                        name = "Low-fat Cottage Cheese Cup";
                        ingredients = "150g low-fat cottage cheese, pinch of black pepper, chives";
                        description = "Spoon cottage cheese into a cup, garnish with chives and pepper.";
                        break;
                    case 4:
                        name = "Turkey Breast & Cucumber Slices";
                        ingredients = "60g lean turkey breast slices, 100g cucumber slices";
                        description = "Enjoy turkey slices wrapped around crisp cucumber slices.";
                        break;
                    case 5:
                        name = "Mixed Berries Parfait";
                        ingredients = "100g Greek yogurt, 50g mixed berries, 10g honey";
                        description = "Layer Greek yogurt with fresh mixed berries and honey.";
                        break;
                    case 6:
                        name = "Hard-Boiled Egg & Crackers";
                        ingredients = "1 hard-boiled egg, 4 whole wheat crackers, sea salt";
                        description = "Enjoy a sliced hard-boiled egg alongside whole wheat crackers.";
                        break;
                    default: // Saturday (7)
                        name = "Apple Slices & Cheddar Cheese";
                        ingredients = "1 organic apple sliced, 30g cheddar cheese slices";
                        description = "Enjoy fresh apple slices alongside cheddar cheese slices.";
                        break;
                }
            }
        } else if ("Cheat Meal".equalsIgnoreCase(type)) {
            name = "Gourmet Cheat Cheeseburger & Fries";
            ingredients = "150g grass-fed beef patty, 1 toasted brioche bun, 1 slice aged cheddar, lettuce, tomato, 100g oven-baked potato wedges, 1 tbsp ketchup";
            description = "Grill beef patty, melt cheese, assemble bun. Serve hot alongside seasoned oven-baked potato wedges.";
        } else {
            // Dinner
            if ("Keto".equalsIgnoreCase(dietPref)) {
                switch (day) {
                    case 1:
                        name = "Keto Chicken Bacon Alfredo Bake";
                        ingredients = "150g chicken breast cubes, 30g bacon bits, 50ml heavy cream, 10g parmesan, 80g broccoli florets, garlic, butter";
                        description = "Sauté chicken, broccoli, and bacon in butter, stir in heavy cream and parmesan. Bake until bubbly.";
                        break;
                    case 2:
                        name = "Baked Garlic Butter Salmon Fillet";
                        ingredients = "150g wild-caught salmon fillet, 120g fresh green beans, 15g unsalted butter, 1 tsp minced garlic, dill, lemon";
                        description = "Bake salmon in garlic butter and dill at 400°F (200°C) for 12 minutes. Serve with sautéed green beans.";
                        break;
                    case 3:
                        name = "Grilled Beef Ribeye & Garlic Spinach";
                        ingredients = "180g ribeye steak, 100g spinach, 15g butter, 1 garlic clove, sea salt";
                        description = "Sear ribeye steak in butter. Sauté spinach in garlic and butter, serve together.";
                        break;
                    case 4:
                        name = "Lemon Garlic Chicken & Cauli-Mash";
                        ingredients = "150g chicken breast, 100g cauliflower florets, 15g butter, 1 tsp garlic, lemon juice";
                        description = "Bake chicken breast. Mash boiled cauliflower with butter and garlic. Serve together.";
                        break;
                    case 5:
                        name = "Baked Sea Bass & Green Beans";
                        ingredients = "150g sea bass fillet, 120g green beans, 10g butter, 1 garlic clove";
                        description = "Bake sea bass with garlic butter, serve alongside sautéed green beans.";
                        break;
                    case 6:
                        name = "Keto Pork Chops & Broccoli Bake";
                        ingredients = "150g pork chop, 80g broccoli, 30g shredded cheddar cheese, 10g butter";
                        description = "Sear pork chop. Sauté broccoli in butter, top with cheddar cheese, and bake until melted.";
                        break;
                    default: // Saturday (7)
                        name = "Keto Crustless Meat Lovers Pizza";
                        ingredients = "100g ground beef, 50g pepperoni slices, 50g marinara sauce, 50g mozzarella cheese, oregano";
                        description = "Layer cooked beef, marinara, mozzarella, and pepperoni in a dish, bake until bubbly.";
                        break;
                }
            } else if ("Vegan".equalsIgnoreCase(dietPref) || "Vegetarian".equalsIgnoreCase(dietPref)) {
                switch (day) {
                    case 1:
                        name = "Sweet Potato & Black Bean Quinoa Chili";
                        ingredients = "80g cooked quinoa, 80g sweet potato chunks, 80g black beans, 100g diced tomatoes, chili powder, cilantro";
                        description = "Simmer sweet potatoes, black beans, and diced tomatoes with spices. Stir in quinoa and top with cilantro.";
                        break;
                    case 2:
                        name = "Tofu Stir-Fry with Brown Rice";
                        ingredients = "150g extra-firm tofu cubed, 100g cooked brown basmati rice, 1 tsp sesame oil, 80g mixed stir-fry vegetables, 1 tbsp soy sauce";
                        description = "Stir-fry tofu cubes and mixed vegetables in sesame oil and low-sodium soy sauce. Serve over brown rice.";
                        break;
                    case 3:
                        name = "Lentil Shepherd's Pie with Sweet Potato Mash";
                        ingredients = "1 cup cooked lentils, 100g mixed veggies, 120g mashed sweet potatoes, 1 tsp olive oil";
                        description = "Sauté lentils and veggies, place in dish. Top with mashed sweet potatoes and bake until golden.";
                        break;
                    case 4:
                        name = "Spinach & Chickpea Curry with Quinoa";
                        ingredients = "100g chickpeas, 80g cooked quinoa, 100g spinach, 100g tomato puree, curry spices, coconut oil";
                        description = "Simmer chickpeas and spinach in tomato curry sauce with coconut oil. Serve over quinoa.";
                        break;
                    case 5:
                        name = "Tempeh Stir-fry with Veggies";
                        ingredients = "120g tempeh cubed, 100g mixed stir-fry veggies, 1 tsp sesame oil, 1 tbsp soy sauce, ginger";
                        description = "Sauté cubed tempeh and vegetables in sesame oil and soy sauce with fresh ginger.";
                        break;
                    case 6:
                        name = "Grilled Eggplant & Tofu Skewers";
                        ingredients = "100g eggplant chunks, 100g firm tofu cubes, 1 tbsp olive oil, oregano, sea salt";
                        description = "Skewer eggplant and tofu, brush with olive oil and spices, grill until tender.";
                        break;
                    default: // Saturday (7)
                        name = "Mediterranean Chickpea Quinoa Bowl";
                        ingredients = "80g cooked quinoa, 100g chickpeas, 50g cucumber, 5 cherry tomatoes, 1 tbsp olive oil, tahini dressing";
                        description = "Toss quinoa and chickpeas in olive oil, arrange with cucumbers and tomatoes, drizzle with tahini.";
                        break;
                }
            } else { // Balanced / High Protein
                switch (day) {
                    case 1:
                        name = "Grilled Sea Bass with Quinoa";
                        ingredients = "150g sea bass fillet, 80g cooked quinoa, 100g fresh asparagus, 1 tsp olive oil, fresh lemon";
                        description = "Sear sea bass in olive oil, squeeze lemon juice. Serve over quinoa alongside grilled asparagus.";
                        break;
                    case 2:
                        name = "Herb-Roasted Turkey Breast & Sweet Potato";
                        ingredients = "150g lean turkey breast, 100g baked sweet potato chunks, 1 tsp olive oil, 80g roasted green beans, rosemary";
                        description = "Roast lean turkey breast and sweet potato chunks seasoned with rosemary and olive oil. Serve with green beans.";
                        break;
                    case 3:
                        name = "Baked Chicken Breast & Quinoa Salad";
                        ingredients = "150g chicken breast, 80g cooked quinoa, 100g mixed greens, 1 tbsp olive oil, vinaigrette";
                        description = "Bake chicken breast. Toss quinoa with greens and olive oil vinaigrette. Serve together.";
                        break;
                    case 4:
                        name = "Grilled Sirloin Steak & Brown Rice";
                        ingredients = "180g sirloin steak, 100g brown rice, 100g spinach, 15g butter, garlic";
                        description = "Sear sirloin steak to medium. Serve with brown rice and garlic butter sautéed spinach.";
                        break;
                    case 5:
                        name = "Atlantic Salmon Fillet & Steamed Broccoli";
                        ingredients = "150g salmon fillet, 100g steamed broccoli florets, 1 tsp olive oil, lemon juice, dill";
                        description = "Sear salmon fillet in olive oil. Serve alongside warm steamed broccoli with fresh lemon juice.";
                        break;
                    case 6:
                        name = "Lean Pork Tenderloin & Baked Asparagus";
                        ingredients = "150g pork tenderloin, 100g asparagus, 1 tsp olive oil, garlic powder";
                        description = "Roast pork tenderloin. Bake asparagus with olive oil and garlic. Serve together.";
                        break;
                    default: // Saturday (7)
                        name = "Baked Lemon Cod & Wild Rice Bowl";
                        ingredients = "150g fresh cod fillet, 100g cooked wild rice, 80g steamed green beans, lemon juice, dill";
                        description = "Bake cod fillet with lemon juice and dill. Serve over wild rice alongside steamed green beans.";
                        break;
                }
            }
        }

        return new DietPlan.Meal(type, name, targetCals, ingredients, description, protein, carbs, fat, fiber, servingSize, portionSize);
    }
}

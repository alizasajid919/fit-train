package com.fitness.app.utils;

import java.util.Locale;

public class FitTrainIntentRouter {

    public enum Intent {
        WORKOUT_TODAY,
        WORKOUT_HISTORY,
        WORKOUT_PLAN,
        DIET_TODAY,
        MEAL_SCHEDULE,
        NUTRITION_MACROS,
        CALORIES_BURNED,
        STEPS_ACTIVITY,
        WATER_HYDRATION,
        SLEEP_RECOVERY,
        BMI_METRICS,
        BMI_EXPLANATION,
        FAT_LOSS_GUIDE,
        MUSCLE_BUILDING_GUIDE,
        EXERCISE_FORM_SQUATS,
        CHAIR_WORKOUT,
        BREAKFAST_NUTRITION,
        WORKOUT_MODIFICATION_EASIER,
        WORKOUT_MODIFICATION_NO_EQUIPMENT,
        REPS_SETS_GUIDE,
        WARMUP_STAMINA_GUIDE,
        PROGRESS_STREAK,
        EQUIPMENT_WORKOUT,
        FORM_CHECK_HELP,
        SMART_MEAL_PLANNER_HELP,
        GROCERY_SCANNER_HELP,
        AI_RECIPE_VOICE_HELP,
        APP_NAVIGATION,
        GENERAL_FITNESS
    }

    public static Intent detectIntent(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Intent.GENERAL_FITNESS;
        }

        String lower = query.toLowerCase(Locale.getDefault()).trim();

        // 1. BMI EXPLANATION ("What is BMI?", "Explain BMI")
        if (lower.contains("what is bmi") || lower.contains("explain bmi") || lower.contains("bmi kya hai")) {
            return Intent.BMI_EXPLANATION;
        }

        // 2. CHAIR WORKOUT ("Give me a chair workout", "chair workout")
        if (lower.contains("chair workout") || lower.contains("chair exercise")) {
            return Intent.CHAIR_WORKOUT;
        }

        // 3. FAT LOSS GUIDE ("How do I lose fat?", "how to lose belly fat", "mujhy fat loss karna hai")
        if (lower.contains("lose fat") || lower.contains("belly fat") || lower.contains("lose weight") || lower.contains("fat loss") || lower.contains("vazn kam")) {
            return Intent.FAT_LOSS_GUIDE;
        }

        // 4. MUSCLE BUILDING GUIDE ("How do I build muscle?", "gain muscle", "hypertrophy")
        if (lower.contains("build muscle") || lower.contains("muscle gain") || lower.contains("hypertrophy") || lower.contains("muscle kaise banaye")) {
            return Intent.MUSCLE_BUILDING_GUIDE;
        }

        // 5. EXERCISE FORM SQUATS ("Explain squats", "squat form", "squat kaisy krty hain")
        if (lower.contains("squat") && (lower.contains("explain") || lower.contains("form") || lower.contains("how") || lower.contains("kaise") || lower.contains("kaisy"))) {
            return Intent.EXERCISE_FORM_SQUATS;
        }

        // 6. BREAKFAST NUTRITION ("What should I eat for breakfast?", "healthy breakfast")
        if (lower.contains("breakfast") || lower.contains("nashta")) {
            return Intent.BREAKFAST_NUTRITION;
        }

        // 7. WORKOUT MODIFICATION - EASIER ("Make my workout easier", "make it easier")
        if (lower.contains("make it easier") || lower.contains("make workout easier") || lower.contains("too hard") || lower.contains("easier version")) {
            return Intent.WORKOUT_MODIFICATION_EASIER;
        }

        // 8. WORKOUT MODIFICATION - NO EQUIPMENT / NO DUMBBELLS ("I don't have dumbbells", "no dumbbells")
        if (lower.contains("no dumbbells") || lower.contains("don't have dumbbells") || lower.contains("dont have dumbbells") || lower.contains("without weights")) {
            return Intent.WORKOUT_MODIFICATION_NO_EQUIPMENT;
        }

        // 9. REPS & SETS GUIDE ("How many reps", "how many sets")
        if (lower.contains("how many reps") || lower.contains("how many sets") || lower.contains("reps count")) {
            return Intent.REPS_SETS_GUIDE;
        }

        // 10. WARMUP & STAMINA ("How should I warm up?", "improve stamina")
        if (lower.contains("warm up") || lower.contains("warmup") || lower.contains("stamina")) {
            return Intent.WARMUP_STAMINA_GUIDE;
        }

        // 11. WORKOUT TODAY (English, Roman Urdu, Urdu)
        if (lower.contains("aj mera workout") || lower.contains("aaj mera workout") || lower.contains("today workout")
                || lower.contains("workout today") || lower.contains("what should i train today")
                || lower.contains("what am i supposed to train today") || lower.contains("today's exercise")
                || lower.contains("todays exercise") || lower.contains("mujhe aaj kya karna hai")
                || lower.contains("what to do at the gym today") || lower.contains("today exercise")
                || lower.contains("aj kya workout karna hai") || lower.contains("aaj ki exercise")) {
            return Intent.WORKOUT_TODAY;
        }

        // 12. WORKOUT HISTORY & PREVIOUS SESSIONS
        if (lower.contains("workout history") || lower.contains("previous workout") || lower.contains("last workout")
                || lower.contains("what did i train yesterday") || lower.contains("completed workouts")
                || lower.contains("past workouts") || lower.contains("kal kya workout kiya")) {
            return Intent.WORKOUT_HISTORY;
        }

        // 13. WORKOUT PLAN / SPLIT
        if (lower.contains("workout plan") || lower.contains("my plan") || lower.contains("weekly split")
                || lower.contains("routine plan") || lower.contains("exercise schedule")) {
            return Intent.WORKOUT_PLAN;
        }

        // 14. DIET TODAY & TODAY'S MEALS
        if (lower.contains("aj mera diet") || lower.contains("aaj mera diet") || lower.contains("today diet")
                || lower.contains("diet today") || lower.contains("what should i eat today")
                || lower.contains("what did i eat today") || lower.contains("aj kya khana hai")
                || lower.contains("aaj kya khana hai") || lower.contains("show today's meals")
                || lower.contains("todays meals") || lower.contains("today's diet")
                || lower.contains("mera diet plan dikhao") || lower.contains("khana kya hai aaj")) {
            return Intent.DIET_TODAY;
        }

        // 15. MEAL SCHEDULE
        if (lower.contains("meal schedule") || lower.contains("when to eat") || lower.contains("breakfast time")
                || lower.contains("lunch time") || lower.contains("dinner time") || lower.contains("meal timing")) {
            return Intent.MEAL_SCHEDULE;
        }

        // 16. NUTRITION, MACROS & PROTEIN TARGETS
        if (lower.contains("protein target") || lower.contains("how much protein") || lower.contains("my macros")
                || lower.contains("macro breakdown") || lower.contains("calorie target") || lower.contains("daily calories target")
                || lower.contains("carbs") || lower.contains("fats target")) {
            return Intent.NUTRITION_MACROS;
        }

        // 17. CALORIES BURNED
        if (lower.contains("calories burned") || lower.contains("calories i burned") || lower.contains("how many calories did i burn")
                || lower.contains("burnt calories") || lower.contains("calories burnt")) {
            return Intent.CALORIES_BURNED;
        }

        // 18. STEPS & WALKING ACTIVITY
        if (lower.contains("my steps") || lower.contains("how many steps") || lower.contains("steps today")
                || lower.contains("walked today") || lower.contains("daily steps") || lower.contains("mery steps")) {
            return Intent.STEPS_ACTIVITY;
        }

        // 19. WATER & HYDRATION
        if (lower.contains("my water") || lower.contains("water intake") || lower.contains("how much water")
                || lower.contains("water target") || lower.contains("paani") || lower.contains("hydration")) {
            return Intent.WATER_HYDRATION;
        }

        // 20. SLEEP & RECOVERY
        if (lower.contains("my sleep") || lower.contains("sleep hours") || lower.contains("how much sleep")
                || lower.contains("sleep log") || lower.contains("rest day") || lower.contains("recovery")) {
            return Intent.SLEEP_RECOVERY;
        }

        // 21. BMI METRICS
        if (lower.contains("my bmi") || lower.contains("body mass index") || lower.contains("am i overweight")
                || lower.contains("bmi category") || lower.contains("calculate bmi")) {
            return Intent.BMI_METRICS;
        }

        // 22. PROGRESS & STREAK
        if (lower.contains("my streak") || lower.contains("workout streak") || lower.contains("weight progress")
                || lower.contains("target weight") || lower.contains("how far from target") || lower.contains("progress tracker") || lower.contains("my progress")) {
            return Intent.PROGRESS_STREAK;
        }

        // 23. EQUIPMENT WORKOUT (Bottle, Backpack, Band)
        if (lower.contains("bottle workout") || lower.contains("backpack workout")
                || lower.contains("resistance band") || lower.contains("household equipment") || lower.contains("home equipment")) {
            return Intent.EQUIPMENT_WORKOUT;
        }

        // 24. FITTRAIN MODULE FEATURE HELP
        if (lower.contains("form check") || lower.contains("squat count") || lower.contains("pose detection")) {
            return Intent.FORM_CHECK_HELP;
        }

        if (lower.contains("smart meal planner") || lower.contains("generate meal plan")) {
            return Intent.SMART_MEAL_PLANNER_HELP;
        }

        if (lower.contains("grocery scanner") || lower.contains("barcode scanner") || lower.contains("scan food")) {
            return Intent.GROCERY_SCANNER_HELP;
        }

        if (lower.contains("recipe voice") || lower.contains("ai recipe")) {
            return Intent.AI_RECIPE_VOICE_HELP;
        }

        if (lower.contains("where is settings") || lower.contains("how to change unit") || lower.contains("edit profile")
                || lower.contains("app navigation") || lower.contains("how to use fittrain")) {
            return Intent.APP_NAVIGATION;
        }

        // Default intent
        return Intent.GENERAL_FITNESS;
    }
}

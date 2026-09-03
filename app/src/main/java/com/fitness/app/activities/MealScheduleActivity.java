package com.fitness.app.activities;

import java.util.Locale;

public class MealScheduleActivity {

    public static final String[] emojiNames = {
            "Pancake", "Coffee", "Steak", "Salad", "Oatmeal", "Apple Pie", "Pizza", "Burger", "Sandwich", "Chicken", "Rice", "Pasta", "Noodles", "Soup", "Eggs", "Apple", "Banana", "Watermelon", "Strawberry", "Juice", "Milk", "Water"
    };

    public static final String[] emojiChars = {
            "🥞", "☕", "🥩", "🥗", "🥣", "🥧", "🍕", "🍔", "🥪", "🍗", "🍚", "🍝", "🍜", "🍲", "🥚", "🍎", "🍌", "🍉", "🍓", "🧃", "🥛", "💧"
    };

    public static String getMealEmoji(int iconResId, String name) {
        if (iconResId >= 0 && iconResId < emojiChars.length) {
            return emojiChars[iconResId];
        }

        if (name == null) return "🍽️";
        String lower = name.toLowerCase(Locale.getDefault());

        for (int i = 0; i < emojiNames.length; i++) {
            if (lower.contains(emojiNames[i].toLowerCase(Locale.getDefault()))) {
                return emojiChars[i];
            }
        }

        if (lower.contains("pancake")) return "🥞";
        if (lower.contains("coffee")) return "☕";
        if (lower.contains("salad")) return "🥗";
        if (lower.contains("oatmeal")) return "🥣";
        if (lower.contains("pie")) return "🥧";
        if (lower.contains("pizza")) return "🍕";
        if (lower.contains("burger")) return "🍔";
        if (lower.contains("taco")) return "🌮";
        if (lower.contains("wrap")) return "🌯";
        if (lower.contains("sandwich")) return "🥪";
        if (lower.contains("chicken")) return "🍗";
        if (lower.contains("steak")) return "🥩";
        if (lower.contains("bbq")) return "🍖";
        if (lower.contains("fries")) return "🍟";
        if (lower.contains("rice")) return "🍚";
        if (lower.contains("pasta")) return "🍝";
        if (lower.contains("noodle")) return "🍜";
        if (lower.contains("soup")) return "🍲";
        if (lower.contains("sushi")) return "🍣";
        if (lower.contains("curry")) return "🍛";
        if (lower.contains("fish")) return "🐟";
        if (lower.contains("egg")) return "🥚";
        if (lower.contains("cheese")) return "🧀";
        if (lower.contains("milk")) return "🥛";
        if (lower.contains("apple")) return "🍎";
        if (lower.contains("banana")) return "🍌";
        if (lower.contains("grape")) return "🍇";
        if (lower.contains("strawberry")) return "🍓";
        if (lower.contains("watermelon")) return "🍉";
        if (lower.contains("pineapple")) return "🍍";
        if (lower.contains("avocado")) return "🥑";
        if (lower.contains("carrot")) return "🥕";
        if (lower.contains("corn")) return "🌽";
        if (lower.contains("cucumber")) return "🥒";
        if (lower.contains("tomato")) return "🍅";
        if (lower.contains("blueberry")) return "🫐";
        if (lower.contains("mango")) return "🥭";
        if (lower.contains("peach")) return "🍑";
        if (lower.contains("lemon")) return "🍋";
        if (lower.contains("orange")) return "🍊";
        if (lower.contains("bread")) return "🍞";
        if (lower.contains("cupcake")) return "🧁";
        if (lower.contains("cake")) return "🍰";
        if (lower.contains("cookie")) return "🍪";
        if (lower.contains("chocolate")) return "🍫";
        if (lower.contains("popcorn")) return "🍿";
        if (lower.contains("nut")) return "🥜";
        if (lower.contains("juice")) return "🧃";
        if (lower.contains("soda") || lower.contains("cola") || lower.contains("soft drink")) return "🥤";
        if (lower.contains("water")) return "💧";

        return "🍽️";
    }

    public static String generateIntelligentNotes(String mealType, String goal, int calories, double protein, double carbs, double fat) {
        String notes = "";
        String goalClean = (goal != null) ? goal.toLowerCase() : "maintenance";
        if (mealType == null) mealType = "meal";

        if (mealType.equalsIgnoreCase("Breakfast")) {
            if (goalClean.contains("loss") || goalClean.contains("fat")) {
                notes = "This light breakfast is packed with fiber and lean nutrients to jumpstart your metabolism and support fat burning throughout the morning.";
            } else if (goalClean.contains("gain") || goalClean.contains("muscle")) {
                notes = "A high-protein, calorie-dense breakfast designed to fuel muscle recovery and provide sustained energy for heavy lifts.";
            } else {
                notes = "Balanced start to the day. High-quality protein and complex carbs to keep your blood sugar stable and maintain steady energy levels.";
            }
        } else if (mealType.equalsIgnoreCase("Lunch")) {
            if (goalClean.contains("loss") || goalClean.contains("fat")) {
                notes = "A clean, low-calorie lunch high in protein to keep you satiated and maintain lean muscle mass during your fat-loss phase.";
            } else if (goalClean.contains("gain") || goalClean.contains("muscle")) {
                notes = "Power lunch with ample carbohydrates and protein to fuel muscle glycogen synthesis and accelerate post-workout recovery.";
            } else {
                notes = "A nutrient-rich midday meal providing key macronutrients to sustain focus, energy, and physical performance for the rest of the day.";
            }
        } else if (mealType.equalsIgnoreCase("Dinner")) {
            if (goalClean.contains("loss") || goalClean.contains("fat")) {
                notes = "A light, digestible evening meal emphasizing lean protein and greens to support nighttime recovery without spiking insulin before bed.";
            } else if (goalClean.contains("gain") || goalClean.contains("muscle")) {
                notes = "A hearty dinner rich in slow-digesting protein and healthy fats to support muscle protein synthesis and recovery during sleep.";
            } else {
                notes = "A wholesomely balanced dinner to satisfy hunger, replenish nutrients, and promote restful sleep and next-day recovery.";
            }
        } else {
            if (goalClean.contains("loss") || goalClean.contains("fat")) {
                notes = "A healthy snack designed to curb cravings and sustain energy levels between main meals without exceeding calorie targets.";
            } else if (goalClean.contains("gain") || goalClean.contains("muscle")) {
                notes = "An anabolic snack supplying quick-acting nutrients to boost muscle growth and keep you in a positive calorie balance.";
            } else {
                notes = "A quick, nourishing snack to stabilize blood sugar and prevent midday fatigue while providing essential micro-nutrients.";
            }
        }
        return notes;
    }
}

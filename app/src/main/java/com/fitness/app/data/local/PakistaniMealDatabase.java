package com.fitness.app.data.local;

import com.fitness.app.models.DietPlan;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PakistaniMealDatabase {

    public static class PakistaniMealRecord {
        public String id;
        public String type; // Breakfast, Morning Drink, Morning Snack, Lunch, Afternoon Drink, Evening Snack, Dinner
        public String name;
        public String emoji;
        public int calories;
        public double protein;
        public double carbs;
        public double fat;
        public double fiber;
        public String servingSize;
        public String ingredients;
        public String instructions;
        public String prepTime;
        public String cookTime;
        public boolean isVegetarian;
        public boolean isKeto;
        public boolean suitableForWeightLoss;
        public boolean suitableForMuscleGain;
        public boolean suitableForMaintenance;

        public PakistaniMealRecord(String id, String type, String name, String emoji, int calories, double protein, double carbs, double fat, double fiber, String servingSize, String ingredients, String instructions, String prepTime, String cookTime, boolean isVegetarian, boolean isKeto, boolean suitableForWeightLoss, boolean suitableForMuscleGain, boolean suitableForMaintenance) {
            this.id = id;
            this.type = type;
            this.name = name;
            this.emoji = emoji;
            this.calories = calories;
            this.protein = protein;
            this.carbs = carbs;
            this.fat = fat;
            this.fiber = fiber;
            this.servingSize = servingSize;
            this.ingredients = ingredients;
            this.instructions = instructions;
            this.prepTime = prepTime;
            this.cookTime = cookTime;
            this.isVegetarian = isVegetarian;
            this.isKeto = isKeto;
            this.suitableForWeightLoss = suitableForWeightLoss;
            this.suitableForMuscleGain = suitableForMuscleGain;
            this.suitableForMaintenance = suitableForMaintenance;
        }

        public DietPlan.Meal toDietPlanMeal() {
            return new DietPlan.Meal(
                    type,
                    name,
                    calories,
                    ingredients,
                    "Authentic Pakistani recipe tailored for your fitness profile.",
                    protein,
                    carbs,
                    fat,
                    fiber,
                    servingSize,
                    servingSize,
                    instructions,
                    prepTime,
                    cookTime,
                    emoji,
                    "Pakistani"
            );
        }
    }

    private static final List<PakistaniMealRecord> MEAL_RECORDS = new ArrayList<>();

    static {
        // BREAKFAST MEALS
        MEAL_RECORDS.add(new PakistaniMealRecord(
                "pk_bf_anda_bhurji", "Breakfast", "Anda Bhurji with Whole Wheat Roti", "🍳",
                380, 22.0, 36.0, 14.0, 5.0, "2 Eggs + 1 Roti (50g)",
                "2 Organic Eggs, 1/2 Whole Wheat Roti (50g), 1 Small Tomato (50g), 1/2 Onion (30g), 1 Green Chili, 1 tsp Mustard Oil, Turmeric, Salt, Coriander",
                "1. Heat 1 tsp oil in a pan on medium heat.\n2. Add chopped onions, green chilies, and tomatoes. Sauté for 3 minutes.\n3. Crack eggs into the pan, add pinch of turmeric and salt.\n4. Scramble until fully cooked.\n5. Serve hot alongside 1 warm whole wheat roti.",
                "10 mins", "10 mins", false, false, true, true, true
        ));

        MEAL_RECORDS.add(new PakistaniMealRecord(
                "pk_bf_anda_paratha", "Breakfast", "Anda Paratha with Dahi (Yogurt)", "🍳",
                520, 26.0, 48.0, 22.0, 4.0, "1 Egg Paratha + 100g Dahi",
                "2 Eggs, 1 Whole Wheat Paratha (70g, light oil), 100g Plain Low-Fat Dahi, Black Pepper, Salt",
                "1. Roll wheat dough into a flat circle.\n2. Beat eggs with salt, pepper, and green chilies.\n3. Cook paratha on tawa with 1 tsp oil, pour egg mixture on top, and flip.\n4. Serve piping hot with 100g fresh plain yogurt.",
                "10 mins", "15 mins", false, false, false, true, true
        ));

        MEAL_RECORDS.add(new PakistaniMealRecord(
                "pk_bf_chana_roti", "Breakfast", "Chana Masala with Whole Wheat Roti", "🫘",
                420, 18.0, 62.0, 9.0, 11.0, "1 Cup Chana + 1 Roti",
                "1 Cup Boiled Chickpeas (150g), 1 Whole Wheat Roti (50g), 1/2 Onion, 1 Tomato, 1 tsp Cumin, Garam Masala, 1 tsp Oil",
                "1. Sauté onions and tomatoes in 1 tsp oil with cumin and garam masala.\n2. Add boiled chickpeas and 1/2 cup water. Simmer for 10 minutes.\n3. Mash a few chickpeas to thicken gravy.\n4. Serve hot with 1 fresh whole wheat roti.",
                "10 mins", "15 mins", true, false, true, true, true
        ));

        MEAL_RECORDS.add(new PakistaniMealRecord(
                "pk_bf_besan_chilla", "Breakfast", "Besan Chilla with Mint Chutney", "🥞",
                320, 15.0, 42.0, 8.0, 7.0, "2 Chillas (100g) + 2 tbsp Chutney",
                "80g Besan (Gram Flour), 30g Chopped Spinach, 1/2 Onion, 1 Green Chili, 2 tbsp Mint Chutney, Salt, Carom Seeds",
                "1. Mix besan, water, chopped spinach, onion, and spices into a smooth batter.\n2. Heat a non-stick tawa, pour 1 ladle batter, and spread thinly.\n3. Cook with minimal oil until golden on both sides.\n4. Serve with fresh mint-coriander chutney.",
                "10 mins", "10 mins", true, false, true, false, true
        ));

        // MORNING DRINKS
        MEAL_RECORDS.add(new PakistaniMealRecord(
                "pk_md_plain_lassi", "Morning Drink", "Fresh Plain Lassi (Unsweetened)", "🥛",
                140, 7.0, 12.0, 6.0, 0.0, "1 Glass (250ml)",
                "150g Plain Dahi (Yogurt), 100ml Cold Water, Pinch of Roasted Cumin Powder, Pinch of Black Salt",
                "1. Combine plain yogurt and cold water in a blender.\n2. Blend for 20 seconds until frothy.\n3. Pour into a tall glass and sprinkle roasted cumin powder and black salt.",
                "5 mins", "0 mins", true, false, true, true, true
        ));

        MEAL_RECORDS.add(new PakistaniMealRecord(
                "pk_md_mint_chaas", "Morning Drink", "Refreshing Mint Chaas (Buttermilk)", "🥛",
                90, 5.0, 8.0, 3.0, 1.0, "1 Glass (300ml)",
                "100g Plain Dahi, 200ml Water, 6 Mint Leaves, 1/4 tsp Roasted Cumin, Black Salt",
                "1. Blend yogurt, mint leaves, water, and spices together until light and smooth.\n2. Serve chilled over ice.",
                "5 mins", "0 mins", true, false, true, true, true
        ));

        MEAL_RECORDS.add(new PakistaniMealRecord(
                "pk_md_green_tea", "Morning Drink", "Pakistani Elaichi Green Tea (Kahwa)", "🍵",
                25, 0.5, 4.0, 0.2, 0.5, "1 Cup (200ml)",
                "1 Green Tea Bag / Kahwa Leaves, 2 Green Cardamoms (Elaichi), 1 tsp Honey (Optional), Lemon Wedge",
                "1. Boil water with crushed cardamom pods.\n2. Steep green tea for 3 minutes.\n3. Strain into a cup, add lemon juice and 1/2 tsp honey if desired.",
                "3 mins", "5 mins", true, true, true, true, true
        ));

        // MORNING SNACKS
        MEAL_RECORDS.add(new PakistaniMealRecord(
                "pk_sn_roasted_chana", "Morning Snack", "Crispy Roasted Chana (Chickpeas)", "🫘",
                160, 9.0, 24.0, 3.5, 6.0, "1 Small Bowl (40g)",
                "40g Roasted Black/Yellow Chickpeas (Bhuna Chana), Chaat Masala, Lemon Juice",
                "1. Toss roasted chana in a small bowl.\n2. Sprinkle 1/4 tsp chaat masala and fresh lemon juice before eating.",
                "2 mins", "0 mins", true, false, true, true, true
        ));

        MEAL_RECORDS.add(new PakistaniMealRecord(
                "pk_sn_fruit_guava_apple", "Morning Snack", "Fresh Guava & Apple Bowl", "🍎",
                110, 2.0, 26.0, 0.5, 7.0, "1 Medium Guava + 1 Small Apple",
                "1 Fresh Guava (Amrood), 1 Small Red Apple, Pinch of Chaat Masala",
                "1. Slice fresh guava and apple into bite-sized pieces.\n2. Sprinkle lightly with chaat masala.",
                "5 mins", "0 mins", true, false, true, true, true
        ));

        MEAL_RECORDS.add(new PakistaniMealRecord(
                "pk_sn_mixed_nuts", "Morning Snack", "Almonds & Walnut Mix", "🥜",
                190, 6.0, 6.0, 16.0, 3.0, "30g Handful",
                "10 Raw Almonds (Badaam), 4 Walnut Halves (Akhrot)",
                "1. Measure 30g raw nuts.\n2. Enjoy as a nutrient-dense healthy fat snack.",
                "1 min", "0 mins", true, true, true, true, true
        ));

        // LUNCH MEALS
        MEAL_RECORDS.add(new PakistaniMealRecord(
                "pk_ln_chicken_karahi", "Lunch", "Chicken Karahi with Whole Wheat Roti", "🍗",
                520, 42.0, 38.0, 18.0, 5.0, "150g Chicken Karahi + 1 Roti",
                "150g Lean Chicken Breast/Thigh, 1 Whole Wheat Roti (50g), 150g Fresh Tomatoes, 1 tsp Ginger-Garlic Paste, 1 tsp Mustard Oil, Green Chilies, Coriander",
                "1. Heat 1 tsp oil in a karahi or pan.\n2. Add ginger-garlic paste and chopped tomatoes; cook until soft.\n3. Add chicken pieces, red chili, cumin, coriander powder, and salt.\n4. Cover and simmer for 15 minutes until chicken is tender.\n5. Garnish with ginger juliennes and serve with 1 warm roti.",
                "15 mins", "20 mins", false, false, true, true, true
        ));

        MEAL_RECORDS.add(new PakistaniMealRecord(
                "pk_ln_daal_chawal", "Lunch", "Daal Chawal with Fresh Cucumber Salad", "🍛",
                460, 19.0, 78.0, 8.0, 9.0, "1 Bowl Daal + 1 Cup Basmati Rice",
                "1 Cup Yellow Moong/Masoor Daal (150g cooked), 1 Cup Boiled Basmati Rice (130g), 1/2 Cucumber, 1 Tomato, Cumin Tarka (1 tsp Ghee/Oil)",
                "1. Pressure cook moong/masoor daal with turmeric, salt, and water.\n2. Prepare tarka with 1 tsp ghee, cumin seeds, and garlic.\n3. Pour tarka over cooked daal.\n4. Serve alongside boiled basmati rice and sliced cucumber salad.",
                "10 mins", "20 mins", true, false, true, true, true
        ));

        MEAL_RECORDS.add(new PakistaniMealRecord(
                "pk_ln_chicken_curry", "Lunch", "Home-style Chicken Curry with Roti", "🍛",
                490, 38.0, 42.0, 16.0, 6.0, "1 Bowl Chicken Curry + 1 Roti",
                "150g Chicken, 1 Whole Wheat Roti (50g), 1/2 Onion, 1 Tomato, 1 tsp Garam Masala, 1 tsp Oil",
                "1. Sauté onions in 1 tsp oil until golden brown.\n2. Add ginger-garlic paste, tomatoes, and spices.\n3. Add chicken and 1 cup water. Simmer for 20 minutes.\n4. Serve with whole wheat roti.",
                "15 mins", "25 mins", false, false, true, true, true
        ));

        MEAL_RECORDS.add(new PakistaniMealRecord(
                "pk_ln_palak_paneer", "Lunch", "Palak Sabzi with Whole Wheat Roti", "🥬",
                390, 16.0, 44.0, 14.0, 8.0, "1 Bowl Palak Sabzi + 1 Roti",
                "200g Fresh Spinach (Palak), 50g Paneer/Cottage Cheese, 1 Whole Wheat Roti, 1/2 Onion, 1 tsp Oil, Spices",
                "1. Blanch spinach and puree.\n2. Sauté onion, garlic, and spices in 1 tsp oil.\n3. Add spinach puree and cubed paneer. Cook for 8 minutes.\n4. Serve warm with roti.",
                "15 mins", "15 mins", true, false, true, true, true
        ));

        MEAL_RECORDS.add(new PakistaniMealRecord(
                "pk_ln_chicken_biryani", "Lunch", "Light Chicken Biryani (Controlled Portion)", "🍚",
                580, 36.0, 68.0, 16.0, 4.0, "1 Medium Plate (250g)",
                "120g Chicken, 150g Basmati Rice, Biryani Masala, 1 tbsp Yogurt, 1 tsp Oil, Mint, Onions",
                "1. Marinate chicken in yogurt and biryani spices.\n2. Parboil basmati rice with whole spices.\n3. Layer chicken masala and rice in a pot. Steam (dum) for 15 minutes.\n4. Serve warm with kachumber salad.",
                "20 mins", "30 mins", false, false, false, true, true
        ));

        // AFTERNOON DRINKS & EVENING SNACKS
        MEAL_RECORDS.add(new PakistaniMealRecord(
                "pk_es_doodh_patti", "Afternoon Drink", "Light Doodh Patti Chai", "☕",
                80, 3.5, 8.0, 3.0, 0.0, "1 Cup (150ml)",
                "100ml Low-Fat Milk, 50ml Water, 1 tsp Black Tea Leaves, 1 Cardamom, 1/2 tsp Jaggery/Sugar",
                "1. Boil milk, water, cardamom, and tea leaves for 4 minutes until rich and aromatic.\n2. Strain into a tea cup.",
                "2 mins", "5 mins", true, false, true, true, true
        ));

        MEAL_RECORDS.add(new PakistaniMealRecord(
                "pk_es_fruit_chaat", "Evening Snack", "Spiced Pakistani Fruit Chaat", "🍎",
                140, 2.5, 32.0, 0.5, 6.0, "1 Medium Bowl (150g)",
                "1/2 Apple, 1/2 Banana, 1/2 Guava, 1/4 Orange, Chaat Masala, Fresh Lemon Juice",
                "1. Chop fruits into uniform cubes.\n2. Toss with lemon juice and chaat masala in a salad bowl.",
                "5 mins", "0 mins", true, false, true, true, true
        ));

        MEAL_RECORDS.add(new PakistaniMealRecord(
                "pk_es_chana_chaat", "Evening Snack", "Light Chana Chaat with Salad", "🫘",
                210, 11.0, 34.0, 3.0, 8.0, "1 Small Bowl (120g)",
                "80g Boiled Chickpeas, 30g Diced Cucumber, 20g Tomato, 1 tbsp Tamarind Water, Chaat Masala",
                "1. Mix chickpeas with diced cucumber, tomato, and onion.\n2. Add tamarind water and chaat masala. Mix well.",
                "5 mins", "5 mins", true, false, true, true, true
        ));

        // DINNER MEALS
        MEAL_RECORDS.add(new PakistaniMealRecord(
                "pk_dn_chicken_tikka", "Dinner", "Chicken Tikka Boti with Kachumber Salad", "🍗",
                440, 46.0, 24.0, 12.0, 4.0, "180g Tikka Boti + Salad + 1/2 Roti",
                "180g Boneless Chicken Breast, 1 tbsp Yogurt, Tikka Masala, Lemon, 1/2 Whole Wheat Roti, Mixed Cucumber-Tomato Salad",
                "1. Marinate chicken cubes in yogurt, lemon juice, and tikka spices for 30 minutes.\n2. Grill on skewers or pan-sear with 1 tsp oil until charred and cooked.\n3. Serve with fresh kachumber salad and 1/2 whole wheat roti.",
                "30 mins", "15 mins", false, false, true, true, true
        ));

        MEAL_RECORDS.add(new PakistaniMealRecord(
                "pk_dn_chicken_keema", "Dinner", "Chicken Keema Matar with Whole Wheat Roti", "🫓",
                470, 40.0, 36.0, 15.0, 6.0, "150g Keema + 1 Roti",
                "150g Minced Chicken Breast, 30g Green Peas (Matar), 1 Whole Wheat Roti, 1/2 Onion, 1 Tomato, Garam Masala, 1 tsp Oil",
                "1. Sauté onions, ginger, garlic, and spices in 1 tsp oil.\n2. Add minced chicken and peas; cook on medium heat for 15 minutes.\n3. Garnish with cilantro and serve with roti.",
                "10 mins", "20 mins", false, false, true, true, true
        ));

        MEAL_RECORDS.add(new PakistaniMealRecord(
                "pk_dn_daal_roti", "Dinner", "Mixed Daal Tadka with Whole Wheat Roti", "🫘",
                380, 17.0, 56.0, 8.0, 10.0, "1 Bowl Daal + 1 Roti",
                "1 Bowl Mixed Yellow/Red Daal (150g), 1 Whole Wheat Roti (50g), Cumin Tarka, Garlic, 1 tsp Ghee",
                "1. Cook mixed lentils with turmeric and salt.\n2. Temper with cumin seeds and garlic in 1 tsp ghee.\n3. Serve hot with warm roti and salad.",
                "10 mins", "20 mins", true, false, true, true, true
        ));

        MEAL_RECORDS.add(new PakistaniMealRecord(
                "pk_dn_fish_tikka", "Dinner", "Pan-Seared Pakistani Fish Tikka", "🐟",
                410, 44.0, 18.0, 12.0, 3.0, "180g Fish Fillet + Salad",
                "180g Fish Fillet (Raham/Sole/Finger Fish), Ajwain (Carom seeds), Lemon Juice, Fish Masala, 1 tsp Oil, Fresh Salad",
                "1. Marinate fish fillet with ajwain, lemon juice, garlic, and spices.\n2. Pan-sear in 1 tsp oil for 4 minutes per side until golden.\n3. Serve with fresh lemon wedges and green salad.",
                "15 mins", "10 mins", false, true, true, true, true
        ));
    }

    public static List<PakistaniMealRecord> getAllMeals() {
        return MEAL_RECORDS;
    }

    public static List<PakistaniMealRecord> filterMeals(String type, String dietPref, String goal, String medicalCondition) {
        List<PakistaniMealRecord> filtered = new ArrayList<>();
        boolean isVeg = "Vegetarian".equalsIgnoreCase(dietPref) || "Vegan".equalsIgnoreCase(dietPref);

        for (PakistaniMealRecord m : MEAL_RECORDS) {
            if (type != null && !type.equalsIgnoreCase(m.type)) {
                continue;
            }

            // Diet preference rule
            if (isVeg && !m.isVegetarian) {
                continue;
            }

            // Goal suitability rule
            if (goal != null) {
                String cleanGoal = goal.toLowerCase(Locale.getDefault());
                if ((cleanGoal.contains("loss") || cleanGoal.contains("fat")) && !m.suitableForWeightLoss) {
                    continue;
                }
                if ((cleanGoal.contains("gain") || cleanGoal.contains("muscle")) && !m.suitableForMuscleGain) {
                    continue;
                }
            }

            filtered.add(m);
        }

        // Fallback if filtering is too strict
        if (filtered.isEmpty()) {
            for (PakistaniMealRecord m : MEAL_RECORDS) {
                if (type != null && type.equalsIgnoreCase(m.type)) {
                    if (!isVeg || m.isVegetarian) {
                        filtered.add(m);
                    }
                }
            }
        }

        return filtered;
    }
}

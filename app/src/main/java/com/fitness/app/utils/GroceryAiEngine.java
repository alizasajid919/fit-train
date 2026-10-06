package com.fitness.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import com.fitness.app.models.GroceryRecipe;
import com.fitness.app.models.ScannedIngredient;
import com.fitness.app.models.ShoppingListItem;
import com.fitness.app.models.User;
import com.fitness.app.models.WeeklyMealPlanDay;
import com.fitness.app.models.GroceryProductScan;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class GroceryAiEngine {

    public interface VisionCallback {
        void onSuccess(List<ScannedIngredient> ingredients, List<GroceryRecipe> suggestedRecipes);
        void onError(String errorMsg);
    }

    public interface ProductScanCallback {
        void onSuccess(GroceryProductScan report);
        void onError(String errorMsg);
    }

    public interface RecipesCallback {
        void onSuccess(List<GroceryRecipe> recipes);
        void onError(String errorMsg);
    }

    public interface MealPlanCallback {
        void onSuccess(List<WeeklyMealPlanDay> mealPlan);
        void onError(String errorMsg);
    }

    public interface EquipmentWorkoutCallback {
        void onSuccess(com.fitness.app.models.EquipmentWorkoutPlan plan);
        void onError(String errorMsg);
    }

    public static User getSafeUser(User user) {
        if (user == null) {
            User safe = new User();
            safe.setAge(25);
            safe.setGender("Male");
            safe.setHeight(170);
            safe.setWeight(70.0);
            safe.setTargetWeight(70.0);
            safe.setGoal("Maintain Weight");
            safe.setActivityLevel("Moderately Active");
            safe.setDietaryPreference("Balanced");
            safe.setMedicalConditions("None");
            safe.setAllergies("None");
            safe.setDailyCaloriesGoal(2000);
            return safe;
        }
        return user;
    }

    public static void analyzeGroceryImage(Context context, Bitmap bitmap, User user, VisionCallback callback) {
        new Thread(() -> {
            try {
                User safeUser = getSafeUser(user);
                String apiKey = getApiKey(context);
                if (apiKey != null && !apiKey.trim().isEmpty() && bitmap != null) {
                    try {
                        String userGoal = safeUser.getGoal() != null ? safeUser.getGoal() : "General Fitness";
                        String prompt = "You are a professional AI Food Vision Recognition assistant. Analyze the image provided.\n"
                                + "User Goal: " + userGoal + "\n"
                                + "Identify all visible food, drink, or ingredient items. Return a JSON array of objects. Each object must have these exact keys:\n"
                                + "- 'name' (string, e.g. 'Apple', 'Water', 'Chicken Breast')\n"
                                + "- 'category' (string, e.g. 'Drink', 'Fruit', 'Vegetable', 'Meat', 'Dairy', 'Pantry', 'Packaged')\n"
                                + "- 'confidence' (int, 0 to 100 representing confidence score)\n"
                                + "- 'estimatedQuantity' (string, e.g. '1 item', '500g', '1 glass')\n"
                                + "- 'calories' (int)\n"
                                + "- 'protein' (double)\n"
                                + "- 'carbs' (double)\n"
                                + "- 'fat' (double)\n"
                                + "- 'fiber' (double)\n"
                                + "- 'sugar' (double)\n"
                                + "- 'sodium' (double)\n"
                                + "- 'vitamins' (string, e.g. 'Vitamin A, C', or 'None')\n"
                                + "- 'minerals' (string, e.g. 'Calcium, Iron', or 'None')\n"
                                + "- 'freshness' (string: 'Fresh', 'Good', 'Near Expiry', 'Expired', or 'Unable to Determine')\n"
                                + "- 'expiryDate' (string: 'YYYY-MM-DD' representing estimated expiration date, or empty if unable to determine)\n"
                                + "STRICT CLASSIFICATION:\n"
                                + "1. WATER: If image contains water, name='Water', category='Drink', calories=0, protein=0, carbs=0, fat=0, sugar=0.\n"
                                + "2. NON-FOOD: If image contains non-food (phone, shoes, chair, laptop, wall, person, etc.), return empty array [].\n"
                                + "3. UNCLEAR: If image is blurry or unrecognizable, return empty array [].";

                        String jsonResponse = callGeminiVisionAPI(apiKey, bitmap, prompt);
                        if (jsonResponse != null && !jsonResponse.trim().isEmpty()) {
                            JSONArray arr = new JSONArray(jsonResponse);
                            List<ScannedIngredient> ingredients = new ArrayList<>();
                            long now = System.currentTimeMillis();

                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject obj = arr.getJSONObject(i);
                                String name = obj.optString("name", "Unknown Food");
                                String category = obj.optString("category", "General");
                                int confidence = obj.optInt("confidence", 80);
                                String qty = obj.optString("estimatedQuantity", "1 unit");
                                int cals = obj.optInt("calories", 0);
                                double prot = obj.optDouble("protein", 0.0);
                                double carbs = obj.optDouble("carbs", 0.0);
                                double fat = obj.optDouble("fat", 0.0);
                                double fiber = obj.optDouble("fiber", 0.0);
                                double sugar = obj.optDouble("sugar", 0.0);
                                double sodium = obj.optDouble("sodium", 0.0);
                                String vitamins = obj.optString("vitamins", "None");
                                String minerals = obj.optString("minerals", "None");
                                String freshness = obj.optString("freshness", "Unable to Determine");
                                String exp = obj.optString("expiryDate", "");

                                if (exp.isEmpty()) {
                                    Calendar c = Calendar.getInstance();
                                    c.add(Calendar.DAY_OF_YEAR, 5);
                                    exp = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(c.getTime());
                                }

                                ingredients.add(new ScannedIngredient(
                                        UUID.randomUUID().toString(),
                                        name, category, confidence, qty,
                                        cals, prot, carbs, fat, fiber, sugar, sodium,
                                        vitamins, minerals, freshness, exp, now
                                ));
                            }

                            if (ingredients.isEmpty()) {
                                new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                                        callback.onError("No recognized food or drink items found in this image. Please upload a clearer photo of your food or drink.")
                                );
                                return;
                            }

                            List<GroceryRecipe> recipes = generateRecipesFromIngredientsAi(apiKey, ingredients, safeUser);
                            
                            new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                                    callback.onSuccess(ingredients, recipes)
                            );
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                        callback.onError("Unable to analyze image right now. Please check your internet connection and try again.")
                );

            } catch (Exception e) {
                if (callback != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                            callback.onError("Vision processing error: " + e.getMessage())
                    );
                }
            }
        }).start();
    }

    public static void analyzeProductScan(Context context, Bitmap bitmap, ProductScanCallback callback) {
        analyzeProductScan(context, bitmap, null, callback);
    }

    public static void analyzeProductScan(Context context, Bitmap bitmap, User user, ProductScanCallback callback) {
        new Thread(() -> {
            try {
                User safeUser = getSafeUser(user);
                String userGoal = safeUser.getGoal() != null ? safeUser.getGoal() : "Maintain Weight";
                int age = safeUser.getAge() > 0 ? safeUser.getAge() : 25;
                double weight = safeUser.getWeight() > 0 ? safeUser.getWeight() : 70.0;
                String dietPref = safeUser.getDietaryPreference() != null ? safeUser.getDietaryPreference() : "Balanced";
                String medical = safeUser.getMedicalConditions() != null ? safeUser.getMedicalConditions() : "None";

                String apiKey = getApiKey(context);
                if (apiKey != null && !apiKey.trim().isEmpty() && bitmap != null) {
                    String prompt = "You are an expert AI Food, Drink & Nutrition Recognition System.\n"
                            + "USER PROFILE CONTEXT:\n"
                            + "- Goal: " + userGoal + "\n"
                            + "- Age: " + age + "\n"
                            + "- Weight: " + weight + " kg\n"
                            + "- Dietary Preference: " + dietPref + "\n"
                            + "- Medical Conditions: " + medical + "\n\n"
                            + "FOLLOW THIS SEQUENTIAL ANALYSIS:\n"
                            + "STEP 1: Identify if the image contains: 'drink', 'food', 'ingredient', 'packaged_food', 'non_food', or 'unclear'.\n"
                            + "STEP 2: Identify the item name.\n\n"
                            + "STRICT RULES:\n"
                            + "1. WATER: If image contains water (bottle, glass, tap, pitcher, mineral water), set productName='Pure Water', foodCategory='Drink', ingredients='Pure Drinking Water', calories=0, protein=0, carbs=0, fat=0, sugar=0, sodium=5. Explain hydration role for goal (" + userGoal + ") and daily target (" + String.format(Locale.US, "%.1f", weight * 35 / 1000.0) + "L). Do NOT classify water as food or assign food calories!\n"
                            + "2. NON-FOOD OBJECTS: If image contains non-food (phone, shoes, chair, laptop, wall, person, clothes, etc.), set productName='Non-Food Object', foodCategory='Non-Food', healthScore=0, nutritionScore=0, aiRecommendation='Not Applicable', whyRecommended='This image does not contain any food or drink item suitable for nutrition analysis.'\n"
                            + "3. UNCLEAR IMAGES: If image is blurry or unrecognizable, set productName='Unclear Image', foodCategory='Unclear', whyRecommended='I couldn't identify this item clearly. Please upload a clearer image.'\n"
                            + "4. ACTUAL FOOD/DRINK: Provide precise item identification (e.g. Apple, Banana, Rice, Chicken Breast, Eggs, Milk, Bread, Juice) and tailor portion guidance, best timing, and goal suitability for " + userGoal + ".\n\n"
                            + "RETURN A JSON OBJECT WITH THESE EXACT KEYS:\n"
                            + "- 'productName' (string)\n"
                            + "- 'brand' (string)\n"
                            + "- 'foodCategory' (string: 'Drink', 'Fruit', 'Vegetable', 'Meat', 'Dairy', 'Pantry', 'Non-Food', or 'Unclear')\n"
                            + "- 'ingredients' (string)\n"
                            + "- 'calories' (int)\n"
                            + "- 'protein' (double)\n"
                            + "- 'carbs' (double)\n"
                            + "- 'fat' (double)\n"
                            + "- 'sugar' (double)\n"
                            + "- 'fiber' (double)\n"
                            + "- 'sodium' (double)\n"
                            + "- 'vitaminsMinerals' (string)\n"
                            + "- 'isHealthy' (boolean)\n"
                            + "- 'isHighlyProcessed' (boolean)\n"
                            + "- 'isOrganic' (boolean)\n"
                            + "- 'isSuitableWeightLoss' (boolean)\n"
                            + "- 'isSuitableWeightGain' (boolean)\n"
                            + "- 'isSuitableMuscleGain' (boolean)\n"
                            + "- 'isSuitableDiabetic' (boolean)\n"
                            + "- 'healthScore' (int 0-100)\n"
                            + "- 'nutritionScore' (int 0-100)\n"
                            + "- 'aiRecommendation' (string: 'Highly Recommended', 'Recommended', 'Eat in Moderation', 'Avoid', or 'Not Applicable')\n"
                            + "- 'whyRecommended' (string)\n"
                            + "- 'alternativeProducts' (string)";

                    String jsonResponse = callGeminiVisionAPI(apiKey, bitmap, prompt);
                    if (jsonResponse != null && !jsonResponse.trim().isEmpty()) {
                        JSONObject obj = new JSONObject(jsonResponse);
                        String productName = obj.optString("productName", "Unknown Item");
                        String category = obj.optString("foodCategory", "General");
                        String why = obj.optString("whyRecommended", "");

                        if ("Non-Food Object".equalsIgnoreCase(productName) || "Non-Food".equalsIgnoreCase(category) || why.contains("does not contain any food")) {
                            new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                                    callback.onError("This image does not contain any food or drink item suitable for nutrition analysis. Please scan a valid food or drink item.")
                            );
                            return;
                        }

                        if ("Unclear Image".equalsIgnoreCase(productName) || "Unclear".equalsIgnoreCase(category) || why.contains("couldn't identify")) {
                            new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                                    callback.onError("I couldn't identify this item clearly. Please upload a clearer image.")
                            );
                            return;
                        }

                        GroceryProductScan scan = new GroceryProductScan();
                        scan.setProductName(productName);
                        scan.setBrand(obj.optString("brand", "Fresh Natural"));
                        scan.setFoodCategory(category);
                        scan.setIngredients(obj.optString("ingredients", productName));

                        // Force water safety
                        if ("Pure Water".equalsIgnoreCase(productName) || productName.toLowerCase().contains("water") || "Drink".equalsIgnoreCase(category) && productName.toLowerCase().contains("water")) {
                            scan.setProductName("Pure Water");
                            scan.setFoodCategory("Drink");
                            scan.setCalories(0);
                            scan.setProtein(0.0);
                            scan.setCarbs(0.0);
                            scan.setFat(0.0);
                            scan.setSugar(0.0);
                            scan.setFiber(0.0);
                            scan.setSodium(5.0);
                            scan.setVitaminsMinerals("Essential Hydration Minerals (Calcium, Magnesium)");
                            scan.setHealthy(true);
                            scan.setHighlyProcessed(false);
                            scan.setOrganic(true);
                            scan.setSuitableWeightLoss(true);
                            scan.setSuitableWeightGain(true);
                            scan.setSuitableMuscleGain(true);
                            scan.setSuitableDiabetic(true);
                            scan.setHealthScore(100);
                            scan.setNutritionScore(100);
                            scan.setAiRecommendation("Highly Recommended");
                            
                            double waterTargetL = Math.round((weight * 35 / 1000.0) * 10.0) / 10.0;
                            scan.setWhyRecommended("Pure water is essential for your fitness goal (" + userGoal + "). Hydration supports metabolic efficiency, digestion, joint lubrication, and physical performance. Daily Target: ~" + waterTargetL + " Liters.");
                            scan.setAlternativeProducts("1. Lemon Infused Water\n2. Sparkling Mineral Water\n3. Green Tea (Unsweetened)");
                        } else {
                            scan.setCalories(obj.optInt("calories", 100));
                            scan.setProtein(obj.optDouble("protein", 0.0));
                            scan.setCarbs(obj.optDouble("carbs", 0.0));
                            scan.setFat(obj.optDouble("fat", 0.0));
                            scan.setSugar(obj.optDouble("sugar", 0.0));
                            scan.setFiber(obj.optDouble("fiber", 0.0));
                            scan.setSodium(obj.optDouble("sodium", 0.0));
                            scan.setVitaminsMinerals(obj.optString("vitaminsMinerals", "Essential Nutrients"));
                            scan.setHealthy(obj.optBoolean("isHealthy", true));
                            scan.setHighlyProcessed(obj.optBoolean("isHighlyProcessed", false));
                            scan.setOrganic(obj.optBoolean("isOrganic", false));
                            scan.setSuitableWeightLoss(obj.optBoolean("isSuitableWeightLoss", true));
                            scan.setSuitableWeightGain(obj.optBoolean("isSuitableWeightGain", true));
                            scan.setSuitableMuscleGain(obj.optBoolean("isSuitableMuscleGain", true));
                            scan.setSuitableDiabetic(obj.optBoolean("isSuitableDiabetic", true));
                            scan.setHealthScore(obj.optInt("healthScore", 80));
                            scan.setNutritionScore(obj.optInt("nutritionScore", 80));
                            scan.setAiRecommendation(obj.optString("aiRecommendation", "Recommended"));
                            scan.setWhyRecommended(why);
                            scan.setAlternativeProducts(obj.optString("alternativeProducts", ""));
                        }
                        
                        scan.setTimestamp(System.currentTimeMillis());

                        new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                                callback.onSuccess(scan)
                        );
                        return;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Error fallback - never fabricate peanut butter for non-food
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                    callback.onError("Unable to analyze image right now. Please check your internet connection and try again.")
            );
        }).start();
    }

    public static void generateRecipesAi(Context context, List<ScannedIngredient> ingredients, User user, String mealFilter, String dietFilter, RecipesCallback callback) {
        new Thread(() -> {
            try {
                User safeUser = getSafeUser(user);
                String apiKey = getApiKey(context);
                if (apiKey != null && !apiKey.trim().isEmpty()) {
                    StringBuilder ingredientNames = new StringBuilder();
                    if (ingredients != null) {
                        for (ScannedIngredient ing : ingredients) {
                            ingredientNames.append(ing.getName()).append(", ");
                        }
                    }

                    String prompt = "You are an expert chef. Generate a JSON array of 4 healthy recipes using primarily these ingredients: " + ingredientNames.toString() + ". "
                            + "The user's goal is: " + (safeUser.getGoal() != null ? safeUser.getGoal() : "General Fitness") + ". "
                            + "Meal type filter: " + mealFilter + ", Diet filter: " + dietFilter + ". "
                            + "Each recipe must have these exact keys:\n"
                            + "- 'name' (string)\n"
                            + "- 'mealType' (string: 'Breakfast', 'Lunch', 'Dinner', or 'Snack')\n"
                            + "- 'prepTimeMinutes' (int)\n"
                            + "- 'cookTimeMinutes' (int)\n"
                            + "- 'difficulty' (string: 'Easy', 'Medium', 'Hard')\n"
                            + "- 'calories' (int)\n"
                            + "- 'protein' (double)\n"
                            + "- 'carbs' (double)\n"
                            + "- 'fat' (double)\n"
                            + "- 'ingredientsJson' (stringified JSON array of strings)\n"
                            + "- 'stepsJson' (stringified JSON array of strings)\n"
                            + "- 'healthBenefits' (string)\n"
                            + "- 'missingIngredients' (string: optional ingredients to buy)\n"
                            + "- 'servingSize' (string)\n"
                            + "- 'healthierReplacements' (string)\n"
                            + "- 'recipeImage' (string: blank or onboarding illustration)";

                    String json = callGeminiTextAPI(apiKey, prompt, true);
                    if (json != null && !json.trim().isEmpty()) {
                        JSONArray arr = new JSONArray(json);
                        List<GroceryRecipe> list = new ArrayList<>();
                        long now = System.currentTimeMillis();

                        for (int i = 0; i < arr.length(); i++) {
                           JSONObject obj = arr.getJSONObject(i);
                           list.add(new GroceryRecipe(
                                   UUID.randomUUID().toString(),
                                   obj.optString("name", "Healthy Recipe"),
                                   obj.optString("mealType", "Lunch"),
                                   obj.optInt("prepTimeMinutes", 10),
                                   obj.optInt("cookTimeMinutes", 15),
                                   obj.optString("difficulty", "Easy"),
                                   obj.optInt("calories", 300),
                                   obj.optDouble("protein", 20.0),
                                   obj.optDouble("carbs", 20.0),
                                   obj.optDouble("fat", 10.0),
                                   obj.optString("ingredientsJson", "[]"),
                                   obj.optString("stepsJson", "[]"),
                                   obj.optString("healthBenefits", ""),
                                   obj.optString("missingIngredients", ""),
                                   false,
                                   obj.optString("servingSize", "1 serving"),
                                   obj.optString("healthierReplacements", ""),
                                   obj.optString("recipeImage", ""),
                                   now
                           ));
                        }
                        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onSuccess(list));
                        return;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Local fallback
            try {
                User safeUser = getSafeUser(user);
                List<GroceryRecipe> list = generateRecipesFromIngredients(ingredients, safeUser);
                List<GroceryRecipe> filtered = new ArrayList<>();
                for (GroceryRecipe r : list) {
                    boolean matchesMeal = "All Meals".equalsIgnoreCase(mealFilter) || r.getMealType().equalsIgnoreCase(mealFilter);
                    if (matchesMeal) {
                        r.setServingSize("2 servings");
                        r.setHealthierReplacements("Use fresh herbs instead of salt seasonings.");
                        filtered.add(r);
                    }
                }
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onSuccess(filtered.isEmpty() ? list : filtered));
            } catch (Exception ignored) {}
        }).start();
    }

    public static void generateWeeklyMealPlanAi(Context context, User user, MealPlanCallback callback) {
        new Thread(() -> {
            try {
                User safeUser = getSafeUser(user);
                String apiKey = getApiKey(context);
                if (apiKey != null && !apiKey.trim().isEmpty()) {
                    String prompt = "You are an expert AI Dietitian. Generate a personalized 7-day meal plan for a user with these parameters:\n"
                           + "- Age: " + safeUser.getAge() + "\n"
                           + "- Gender: " + safeUser.getGender() + "\n"
                           + "- Height: " + safeUser.getHeight() + "cm\n"
                           + "- Weight: " + safeUser.getWeight() + "kg\n"
                           + "- Target Weight: " + safeUser.getTargetWeight() + "kg\n"
                           + "- Goal: " + safeUser.getGoal() + "\n"
                           + "- Activity Level: " + safeUser.getActivityLevel() + "\n"
                           + "- Medical Conditions: " + safeUser.getMedicalConditions() + "\n"
                           + "- Allergies: " + safeUser.getAllergies() + "\n"
                           + "- Dietary Preference: " + safeUser.getDietaryPreference() + "\n"
                           + "- Calories Goal: " + (safeUser.getDailyCaloriesGoal() > 0 ? safeUser.getDailyCaloriesGoal() : 2000) + " kcal\n"
                           + "Return a JSON array of 7 objects (Monday to Sunday). Each object must have these exact keys:\n"
                           + "- 'dayOfWeek' (int: 1 to 7)\n"
                           + "- 'dayName' (string: 'Monday', 'Tuesday', etc.)\n"
                           + "- 'breakfastRecipeName' (string)\n"
                           + "- 'breakfastCalories' (int)\n"
                           + "- 'breakfastDetailsJson' (stringified JSON)\n"
                           + "- 'morningSnackRecipeName' (string)\n"
                           + "- 'morningSnackCalories' (int)\n"
                           + "- 'morningSnackDetailsJson' (stringified JSON)\n"
                           + "- 'lunchRecipeName' (string)\n"
                           + "- 'lunchCalories' (int)\n"
                           + "- 'lunchDetailsJson' (stringified JSON)\n"
                           + "- 'eveningSnackRecipeName' (string)\n"
                           + "- 'eveningSnackCalories' (int)\n"
                           + "- 'eveningSnackDetailsJson' (stringified JSON)\n"
                           + "- 'dinnerRecipeName' (string)\n"
                           + "- 'dinnerCalories' (int)\n"
                           + "- 'dinnerDetailsJson' (stringified JSON)\n"
                           + "- 'totalCalories' (int)\n"
                           + "- 'totalProtein' (double)\n"
                           + "- 'totalCarbs' (double)\n"
                           + "- 'totalFat' (double)\n"
                           + "- 'waterIntakeMl' (int: estimated daily water target)\n"
                           + "- 'expectedNutrition' (string)\n"
                           + "Ensure different recipes every day.";

                    String json = callGeminiTextAPI(apiKey, prompt, true);
                    if (json != null && !json.trim().isEmpty()) {
                        JSONArray arr = new JSONArray(json);
                        List<WeeklyMealPlanDay> list = new ArrayList<>();
                        long now = System.currentTimeMillis();

                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            list.add(new WeeklyMealPlanDay(
                                    UUID.randomUUID().toString(),
                                    obj.optInt("dayOfWeek", i + 1),
                                    obj.optString("dayName", "Monday"),
                                    obj.optString("breakfastRecipeName", ""),
                                    obj.optInt("breakfastCalories", 0),
                                    obj.optString("lunchRecipeName", ""),
                                    obj.optInt("lunchCalories", 0),
                                    obj.optString("dinnerRecipeName", ""),
                                    obj.optInt("dinnerCalories", 0),
                                    obj.optString("morningSnackRecipeName", obj.optString("eveningSnackRecipeName", "")),
                                    obj.optInt("morningSnackCalories", obj.optInt("eveningSnackCalories", 0)),
                                    obj.optInt("totalCalories", 2000),
                                    obj.optDouble("totalProtein", 100.0),
                                    obj.optInt("waterIntakeMl", (int)(safeUser.getWeight() * 35)),
                                    obj.optDouble("totalCarbs", 220.0),
                                    obj.optDouble("totalFat", 65.0),
                                    obj.optString("expectedNutrition", "Protein: 100g, Carbs: 220g"),
                                    obj.optString("breakfastDetailsJson", "{}"),
                                    obj.optString("morningSnackDetailsJson", "{}"),
                                    obj.optString("lunchDetailsJson", "{}"),
                                    obj.optString("eveningSnackDetailsJson", "{}"),
                                    obj.optString("dinnerDetailsJson", "{}"),
                                    now
                             ));
                        }
                        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onSuccess(list));
                        return;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Local fallback
            try {
                User safeUser = getSafeUser(user);
                List<WeeklyMealPlanDay> fallbackList = generateWeeklyMealPlan(safeUser);
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onSuccess(fallbackList));
            } catch (Exception ignored) {}
        }).start();
    }

    public static String getApiKey(Context context) {
        if (context == null) return null;
        SharedPreferences prefs = context.getSharedPreferences("ai_prefs", Context.MODE_PRIVATE);
        String key = prefs.getString("gemini_api_key", null);
        if (key == null || key.trim().isEmpty()) {
            try {
                com.google.android.gms.tasks.Task<com.google.firebase.firestore.DocumentSnapshot> task =
                        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                .collection("config").document("ai").get();
                com.google.firebase.firestore.DocumentSnapshot snapshot = com.google.android.gms.tasks.Tasks.await(task);
                if (snapshot != null && snapshot.exists()) {
                    key = snapshot.getString("gemini_api_key");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return key;
    }

    private static String callGeminiVisionAPI(String apiKey, Bitmap bitmap, String prompt) throws Exception {
        URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(20000);
        conn.setReadTimeout(20000);

        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
        byte[] byteArray = bos.toByteArray();
        String base64Image = android.util.Base64.encodeToString(byteArray, android.util.Base64.NO_WRAP);

        JSONObject bodyJson = new JSONObject();
        JSONArray contents = new JSONArray();
        JSONObject contentObj = new JSONObject();
        contentObj.put("role", "user");
        JSONArray parts = new JSONArray();

        JSONObject textPart = new JSONObject();
        textPart.put("text", prompt);
        parts.put(textPart);

        JSONObject imagePart = new JSONObject();
        JSONObject inlineData = new JSONObject();
        inlineData.put("mimeType", "image/jpeg");
        inlineData.put("data", base64Image);
        imagePart.put("inlineData", inlineData);
        parts.put(imagePart);

        contentObj.put("parts", parts);
        contents.put(contentObj);
        bodyJson.put("contents", contents);

        JSONObject genConfig = new JSONObject();
        genConfig.put("responseMimeType", "application/json");
        bodyJson.put("generationConfig", genConfig);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = bodyJson.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int code = conn.getResponseCode();
        if (code == 200) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line.trim());
                }
                JSONObject resp = new JSONObject(sb.toString());
                JSONArray candidates = resp.getJSONArray("candidates");
                if (candidates.length() > 0) {
                    JSONObject first = candidates.getJSONObject(0);
                    JSONObject content = first.getJSONObject("content");
                    JSONArray partsArray = content.getJSONArray("parts");
                    if (partsArray.length() > 0) {
                        return partsArray.getJSONObject(0).getString("text");
                    }
                }
            }
        } else {
            throw new RuntimeException("HTTP error: " + code);
        }
        return null;
    }

    public static String callGeminiTextAPI(String apiKey, String prompt, boolean forceJson) throws Exception {
        URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);

        JSONObject bodyJson = new JSONObject();
        JSONArray contents = new JSONArray();
        JSONObject contentObj = new JSONObject();
        contentObj.put("role", "user");
        JSONArray parts = new JSONArray();

        JSONObject textPart = new JSONObject();
        textPart.put("text", prompt);
        parts.put(textPart);

        contentObj.put("parts", parts);
        contents.put(contentObj);
        bodyJson.put("contents", contents);

        if (forceJson) {
            JSONObject genConfig = new JSONObject();
            genConfig.put("responseMimeType", "application/json");
            bodyJson.put("generationConfig", genConfig);
        }

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = bodyJson.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int code = conn.getResponseCode();
        if (code == 200) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line.trim());
                }
                JSONObject resp = new JSONObject(sb.toString());
                JSONArray candidates = resp.getJSONArray("candidates");
                if (candidates.length() > 0) {
                    JSONObject first = candidates.getJSONObject(0);
                    JSONObject content = first.getJSONObject("content");
                    JSONArray partsArray = content.getJSONArray("parts");
                    if (partsArray.length() > 0) {
                        return partsArray.getJSONObject(0).getString("text");
                    }
                }
            }
        } else {
            throw new RuntimeException("HTTP error: " + code);
        }
        return null;
    }

    private static List<GroceryRecipe> generateRecipesFromIngredientsAi(String apiKey, List<ScannedIngredient> ingredients, User user) {
        try {
            User safeUser = getSafeUser(user);
            StringBuilder names = new StringBuilder();
            if (ingredients != null) {
                for (ScannedIngredient ing : ingredients) {
                    names.append(ing.getName()).append(", ");
                }
            }

            String prompt = "You are an expert chef and nutritionist. Generate a JSON array of 3 healthy meal recipes using ONLY or primarily these ingredients: " + names.toString() + ". "
                    + "Each recipe object must have these exact keys:\n"
                    + "- 'name' (string)\n"
                    + "- 'mealType' (string: 'Breakfast', 'Lunch', 'Dinner', or 'Snack')\n"
                    + "- 'prepTimeMinutes' (int)\n"
                    + "- 'cookTimeMinutes' (int)\n"
                    + "- 'difficulty' (string: 'Easy', 'Medium', 'Hard')\n"
                    + "- 'calories' (int)\n"
                    + "- 'protein' (double)\n"
                    + "- 'carbs' (double)\n"
                    + "- 'fat' (double)\n"
                    + "- 'ingredientsJson' (stringified JSON array of strings)\n"
                    + "- 'stepsJson' (stringified JSON array of strings)\n"
                    + "- 'healthBenefits' (string)\n"
                    + "- 'missingIngredients' (string)\n"
                    + "- 'servingSize' (string)\n"
                    + "- 'healthierReplacements' (string)\n"
                    + "- 'recipeImage' (string)";

            String json = callGeminiTextAPI(apiKey, prompt, true);
            if (json != null && !json.trim().isEmpty()) {
                JSONArray arr = new JSONArray(json);
                List<GroceryRecipe> list = new ArrayList<>();
                long now = System.currentTimeMillis();

                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    list.add(new GroceryRecipe(
                            UUID.randomUUID().toString(),
                            obj.optString("name", "Healthy Meal"),
                            obj.optString("mealType", "Lunch"),
                            obj.optInt("prepTimeMinutes", 10),
                            obj.optInt("cookTimeMinutes", 15),
                            obj.optString("difficulty", "Easy"),
                            obj.optInt("calories", 300),
                            obj.optDouble("protein", 20.0),
                            obj.optDouble("carbs", 20.0),
                            obj.optDouble("fat", 10.0),
                            obj.optString("ingredientsJson", "[]"),
                            obj.optString("stepsJson", "[]"),
                            obj.optString("healthBenefits", "Nutritious meal"),
                            obj.optString("missingIngredients", ""),
                            false,
                            obj.optString("servingSize", "1 serving"),
                            obj.optString("healthierReplacements", ""),
                            obj.optString("recipeImage", ""),
                            now
                    ));
                }
                return list;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return generateRecipesFromIngredients(ingredients, user);
    }

    public static List<ScannedIngredient> generateDefaultDetectedIngredients() {
        List<ScannedIngredient> list = new ArrayList<>();
        long now = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        String[][] pool = {
            {"Eggs", "Dairy & Protein", "6 items", "140", "12", "1", "10", "Vitamin D", "Calcium", "7"},
            {"Spinach", "Vegetables", "200g pack", "46", "6", "7", "1", "Vitamin A, C, K", "Calcium, Iron", "2"},
            {"Tomatoes", "Vegetables", "4 items", "36", "2", "8", "0", "Vitamin C", "Potassium", "5"},
            {"Chicken Breast", "Meat & Poultry", "400g pack", "440", "92", "0", "7", "Vitamin B6, B12", "Phosphorus", "3"},
            {"Low-Fat Milk", "Dairy", "1 Liter bottle", "120", "8", "12", "2.5", "Vitamin D", "Calcium", "5"},
            {"Brown Rice", "Grains & Pantry", "1 kg bag", "215", "5", "45", "1.6", "Vitamin B", "Iron", "14"}
        };

        for (String[] item : pool) {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_YEAR, Integer.parseInt(item[9]));
            
            list.add(new ScannedIngredient(
                UUID.randomUUID().toString(),
                item[0], item[1], 95, item[2],
                Integer.parseInt(item[3]), Double.parseDouble(item[4]), Double.parseDouble(item[5]),
                Double.parseDouble(item[6]), 1.0, 1.0, 60.0,
                item[7], item[8], "Fresh", sdf.format(c.getTime()), now
            ));
        }

        return list;
    }

    public static List<GroceryRecipe> generateRecipesFromIngredients(List<ScannedIngredient> ingredients, User user) {
        User safeUser = getSafeUser(user);
        List<GroceryRecipe> recipes = new ArrayList<>();
        long now = System.currentTimeMillis();

        String goal = safeUser.getGoal() != null ? safeUser.getGoal() : "Maintain";
        int bCal = 350; int lCal = 550; int dCal = 500; int sCal = 150;
        if ("Lose Fat".equalsIgnoreCase(goal) || "Weight Loss".equalsIgnoreCase(goal)) {
            bCal = 300; lCal = 450; dCal = 450; sCal = 100;
        } else if ("Gain Muscle".equalsIgnoreCase(goal) || "Weight Gain".equalsIgnoreCase(goal)) {
            bCal = 500; lCal = 750; dCal = 700; sCal = 300;
        }

        recipes.add(new GroceryRecipe(
            UUID.randomUUID().toString(),
            "Spinach & Tomato Egg Scramble",
            "Breakfast", 5, 5, "Easy",
            bCal, Math.round(bCal * 0.28 / 4.0), Math.round(bCal * 0.47 / 4.0), Math.round(bCal * 0.25 / 9.0),
            "[\"3 Eggs\", \"Handful of Spinach\", \"1 Tomato\", \"1 tsp Olive Oil\"]",
            "[\"Whisk eggs in a bowl with salt & pepper.\", \"Sauté spinach and tomatoes in oil for 2 minutes.\", \"Add whisked eggs, scramble until fully cooked.\"]",
            "Excellent low-carb breakfast packed with lutein, choline, and lean proteins.",
            "Buy: Olive Oil, Salt & Pepper", true, "1 serving", "Use almond milk to reduce calories.", "", now
        ));

        recipes.add(new GroceryRecipe(
            UUID.randomUUID().toString(),
            "Grilled Chicken & Rice Power Bowl",
            "Lunch", 10, 15, "Medium",
            lCal, Math.round(lCal * 0.32 / 4.0), Math.round(lCal * 0.43 / 4.0), Math.round(lCal * 0.25 / 9.0),
            "[\"200g Chicken Breast\", \"1 cup Brown Rice\", \"Fresh Spinach\", \"1 tsp Olive Oil\"]",
            "[\"Season chicken breast with garlic & pepper.\", \"Grill chicken on high heat for 6 mins per side.\", \"Serve over warm brown rice and fresh sautéed spinach.\"]",
            "High-protein recovery lunch supporting muscle protein synthesis.",
            "Buy: Olive Oil, Garlic seasoning", true, "1 plate", "Substitute brown rice with quinoa for extra protein.", "", now
        ));

        recipes.add(new GroceryRecipe(
            UUID.randomUUID().toString(),
            "Baked Lemon Salmon & Asparagus",
            "Dinner", 15, 20, "Medium",
            dCal, Math.round(dCal * 0.30 / 4.0), Math.round(dCal * 0.45 / 4.0), Math.round(dCal * 0.25 / 9.0),
            "[\"180g Salmon Fillet\", \"100g Asparagus\", \"Garlic & Lemon juice\"]",
            "[\"Bake salmon and asparagus at 400F for 12-15 minutes.\", \"Season with lemon juice and minced garlic before serving.\"]",
            "High-quality lean fats and fiber. Promotes good sleep and muscle recovery.",
            "Buy: Salmon, Asparagus", false, "1 serving", "Top with avocado slices for heart-healthy fats.", "", now
        ));

        recipes.add(new GroceryRecipe(
            UUID.randomUUID().toString(),
            "Greek Yogurt Berry Parfait",
            "Snack", 3, 0, "Easy",
            sCal, Math.round(sCal * 0.20 / 4.0), Math.round(sCal * 0.50 / 4.0), Math.round(sCal * 0.30 / 9.0),
            "[\"150g Greek Yogurt\", \"Handful of Mixed Berries\", \"Honey\"]",
            "[\"Scoop yogurt into a glass bowl.\", \"Top with fresh mixed berries and drizzle with honey.\"]",
            "Probiotics support digestive health and calcium strengthens bone structure.",
            "Buy: Greek Yogurt, Berries", false, "1 serving", "Use organic honey or omit for lower calorie intake.", "", now
        ));

        return recipes;
    }

    public static List<WeeklyMealPlanDay> generateWeeklyMealPlan(User user) {
        User safeUser = getSafeUser(user);
        List<WeeklyMealPlanDay> days = new ArrayList<>();
        String[] dayNames = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        long now = System.currentTimeMillis();

        double weight = safeUser.getWeight() > 0 ? safeUser.getWeight() : 70.0;
        double height = safeUser.getHeight() > 0 ? safeUser.getHeight() : 170.0;
        int age = safeUser.getAge() > 0 ? safeUser.getAge() : 25;
        String gender = safeUser.getGender() != null ? safeUser.getGender() : "Male";
        String goal = safeUser.getGoal() != null ? safeUser.getGoal() : "Maintain Weight";
        String activity = safeUser.getActivityLevel() != null ? safeUser.getActivityLevel() : "Moderately Active";

        double bmr = "Female".equalsIgnoreCase(gender) ?
            (10 * weight + 6.25 * height - 5 * age - 161) :
            (10 * weight + 6.25 * height - 5 * age + 5);

        double mult = 1.375;
        if (activity != null) {
            String a = activity.toLowerCase(Locale.getDefault());
            if (a.contains("sedentary")) mult = 1.2;
            else if (a.contains("light")) mult = 1.375;
            else if (a.contains("mod")) mult = 1.55;
            else if (a.contains("very") || a.contains("active")) mult = 1.725;
        }

        int targetCalories = (int) Math.round(bmr * mult);
        if (goal != null) {
            String g = goal.toLowerCase(Locale.getDefault());
            if (g.contains("lose") || g.contains("fat") || g.contains("weight loss")) targetCalories -= 500;
            else if (g.contains("gain") || g.contains("muscle") || g.contains("bulk")) targetCalories += 350;
        }
        if (targetCalories < 1200) targetCalories = 1200;
        if (safeUser.getDailyCaloriesGoal() > 0) {
            targetCalories = safeUser.getDailyCaloriesGoal();
        }

        double protRatio = goal.toLowerCase().contains("gain") ? 0.32 : (goal.toLowerCase().contains("loss") ? 0.35 : 0.25);
        double fatRatio = 0.25;
        double carbRatio = 1.0 - protRatio - fatRatio;

        double protGrams = Math.round((targetCalories * protRatio) / 4.0);
        double fatGrams = Math.round((targetCalories * fatRatio) / 9.0);
        double carbsGrams = Math.round((targetCalories * carbRatio) / 4.0);

        String[] breakfasts = {
            "Spinach & Tomato Egg Omelette",
            "Protein-Packed Blueberry Oatmeal",
            "Avocado Toast with Poached Eggs",
            "Strawberry Protein Smoothie Bowl",
            "Peanut Butter & Banana Chia Pudding",
            "Turkey Bacon & Egg White Wrap",
            "Greek Yogurt Parfait with Almonds"
        };
        int[] bCalories = {280, 320, 310, 290, 340, 270, 250};

        String[] lunches = {
            "Grilled Chicken Quinoa Bowl",
            "Tuna Salad Whole Wheat Sandwich",
            "Turkey & Sweet Potato Mash Bowl",
            "Salmon Rice Bowl with Steamed Broccoli",
            "Lean Beef Steak & Cauliflower Mash",
            "Lentil & Chickpea Protein Bowl",
            "Mediterranean Quinoa & Veggie Wrap"
        };
        int[] lCalories = {490, 420, 480, 520, 560, 440, 410};

        String[] dinners = {
            "Baked Lemon Herb Salmon & Asparagus",
            "Garlic Butter Chicken & Zucchini Noodles",
            "Stuffed Bell Peppers with Ground Turkey",
            "Shrimp Stir-Fry with Cauliflower Rice",
            "Tofu & Mixed Vegetable Quinoa Stir-Fry",
            "Baked Cod with Roasted Brussels Sprouts",
            "Lean Turkey Meatballs with Marinara"
        };
        int[] dCalories = {450, 430, 460, 390, 410, 420, 480};

        String[] snacks = {
            "Apple Slices with Peanut Butter",
            "Mixed Nuts and Seeds Power Mix",
            "Greek Yogurt with Mixed Berries",
            "Cottage Cheese with Pineapple Slices",
            "Carrot & Cucumber Sticks with Hummus",
            "Boiled Eggs with Black Pepper",
            "Protein Shake with Almond Milk"
        };
        int[] sCalories = {180, 220, 150, 160, 120, 140, 200};

        for (int i = 0; i < 7; i++) {
            double ratio = (double) targetCalories / 1400.0;
            int bCal = (int) Math.round(bCalories[i] * ratio);
            int lCal = (int) Math.round(lCalories[i] * ratio);
            int dCal = (int) Math.round(dCalories[i] * ratio);
            int sCal = (int) Math.round(sCalories[i] * ratio);
            int dayTotalCal = bCal + lCal + dCal + sCal;

            String bJson = buildMealDetailJson(breakfasts[i], bCal, "Breakfast", safeUser);
            String lJson = buildMealDetailJson(lunches[i], lCal, "Lunch", safeUser);
            String dJson = buildMealDetailJson(dinners[i], dCal, "Dinner", safeUser);
            String sJson = buildMealDetailJson(snacks[i], sCal, "Snack", safeUser);

            days.add(new WeeklyMealPlanDay(
                UUID.randomUUID().toString(),
                i + 1,
                dayNames[i],
                breakfasts[i], bCal,
                lunches[i], lCal,
                dinners[i], dCal,
                snacks[i], sCal,
                dayTotalCal, protGrams,
                (int) Math.round(weight * 35),
                carbsGrams, fatGrams,
                String.format(Locale.US, "Protein: %.0fg, Carbs: %.0fg, Fat: %.0fg", protGrams, carbsGrams, fatGrams),
                bJson, sJson, lJson, sJson, dJson,
                now
            ));
        }
        return days;
    }

    public static String buildMealDetailJson(String recipeName, int calories, String mealType, User user) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("recipe", recipeName != null ? recipeName : "Healthy Meal");
            obj.put("calories", calories);

            double protein = Math.round(calories * 0.30 / 4.0);
            double carbs = Math.round(calories * 0.45 / 4.0);
            double fat = Math.round(calories * 0.25 / 9.0);

            obj.put("protein", protein);
            obj.put("carbs", carbs);
            obj.put("fat", fat);
            obj.put("prepTimeMinutes", 10);
            obj.put("cookTimeMinutes", 15);
            obj.put("servingSize", "1 serving");
            obj.put("difficulty", "Easy");

            JSONArray ingredients = new JSONArray();
            JSONArray steps = new JSONArray();

            if (recipeName != null && recipeName.contains("Oatmeal")) {
                ingredients.put("Oats (50g)");
                ingredients.put("Low-fat milk (200ml)");
                ingredients.put("Honey (1 tbsp)");
                ingredients.put("Fresh berries");
                steps.put("Bring milk to a boil.");
                steps.put("Add oats and simmer for 5 minutes.");
                steps.put("Top with honey and berries.");
            } else if (recipeName != null && (recipeName.contains("Omelette") || recipeName.contains("Scramble"))) {
                ingredients.put("Eggs (3 items)");
                ingredients.put("Spinach (50g)");
                ingredients.put("Tomatoes (1 medium)");
                ingredients.put("Olive oil (1 tsp)");
                steps.put("Whisk eggs in a bowl.");
                steps.put("Sauté spinach and tomatoes in olive oil.");
                steps.put("Pour in eggs and cook until firm.");
            } else if (recipeName != null && recipeName.contains("Chicken")) {
                ingredients.put("Chicken breast (150g)");
                ingredients.put("Quinoa or brown rice (100g)");
                ingredients.put("Mixed greens");
                ingredients.put("Olive oil");
                steps.put("Season chicken breast with herbs.");
                steps.put("Pan-sear chicken for 6 minutes each side.");
                steps.put("Serve over cooked rice or quinoa with greens.");
            } else if (recipeName != null && recipeName.contains("Salmon")) {
                ingredients.put("Salmon fillet (180g)");
                ingredients.put("Asparagus or broccoli");
                ingredients.put("Olive oil");
                ingredients.put("Lemon & garlic");
                steps.put("Preheat oven to 400F.");
                steps.put("Brush salmon and veggies with olive oil.");
                steps.put("Bake for 12-15 minutes until salmon flakes easily.");
            } else {
                ingredients.put("Healthy ingredient 1");
                ingredients.put("Healthy ingredient 2");
                steps.put("Prepare ingredients.");
                steps.put("Mix and enjoy fresh.");
            }

            obj.put("ingredients", ingredients);
            obj.put("steps", steps);
            obj.put("healthierReplacements", "Swap butter for olive oil.");
            obj.put("healthBenefits", "Rich in proteins and essential micronutrients.");
            obj.put("storageTips", "Eat fresh or keep in fridge for up to 2 days.");

            return obj.toString();
        } catch (Exception e) {
            return "{}";
        }
    }

    public static String processVoiceQuery(Context context, String query, List<ScannedIngredient> scannedItems, User user, String historyContext) {
        try {
            User safeUser = getSafeUser(user);
            String apiKey = getApiKey(context);
            if (apiKey != null && !apiKey.trim().isEmpty()) {
                StringBuilder ingredientNames = new StringBuilder();
                if (scannedItems != null) {
                    for (ScannedIngredient i : scannedItems) {
                        ingredientNames.append(i.getName()).append(", ");
                    }
                }

                String prompt = "You are a highly intelligent cooking, recipe, and nutrition AI assistant in the FitTrain app.\n"
                        + "Answer this user query: '" + query + "'.\n"
                        + "User Profile Context: Age: " + safeUser.getAge() + ", Goal: " + safeUser.getGoal() + ", Weight: " + safeUser.getWeight() + " kg, Target Weight: " + safeUser.getTargetWeight() + " kg, Allergies: " + safeUser.getAllergies() + ", Medical Conditions: " + safeUser.getMedicalConditions() + ", Dietary Preference: " + safeUser.getDietaryPreference() + "\n"
                        + "Scanned Groceries Context: " + (ingredientNames.length() > 0 ? ingredientNames.toString() : "None scanned yet.") + "\n"
                        + "Conversation History:\n" + historyContext + "\n"
                        + "Rules:\n"
                        + "1. Understand intent. If user asks for a recipe suggestion, suggest a recipe using ONLY or PRIMARILY their scanned groceries (if any).\n"
                        + "2. Every suggested recipe MUST include: Name, Calories, Protein, Carbs, Fat, Prep/Cook time, Difficulty, Serving size, Ingredients list, step-by-step Cooking steps, Health benefits, Storage tips, and Healthier alternatives.\n"
                        + "3. Answer general food nutrition or diet advice questions accurately. Never return repetitive/fake values.\n"
                        + "4. Respond naturally and in detail in the same language as the user query.";

                String resp = callGeminiTextAPI(apiKey, prompt, false);
                if (resp != null && !resp.trim().isEmpty()) {
                    return resp;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (query == null || query.trim().isEmpty()) {
            return "How can I help with your meal planning or grocery scan today?";
        }

        return "Based on your fitness profile and groceries, focus on balanced whole foods rich in lean proteins, complex carbs, and healthy fats. Stay hydrated with pure water throughout the day!";
    }

    public static void generateEquipmentWorkout(Context context, User user, String goal, List<String> selectedEquipment, EquipmentWorkoutCallback callback) {
        new Thread(() -> {
            try {
                User safeUser = getSafeUser(user);
                String apiKey = getApiKey(context);
                StringBuilder equipSb = new StringBuilder();
                if (selectedEquipment != null) {
                    for (String e : selectedEquipment) {
                        equipSb.append(e).append(", ");
                    }
                }
                String equipStr = equipSb.length() > 0 ? equipSb.substring(0, equipSb.length() - 2) : "Bodyweight";

                if (apiKey != null && !apiKey.trim().isEmpty()) {
                    String prompt = "You are a world-class personal fitness coach. Generate a structured equipment-compatible workout plan for a user with these profile parameters:\n"
                            + "- Goal: " + (goal != null ? goal : safeUser.getGoal()) + "\n"
                            + "- Fitness Level: " + safeUser.getFitnessExperience() + "\n"
                            + "- Age: " + safeUser.getAge() + "\n"
                            + "- Gender: " + safeUser.getGender() + "\n"
                            + "- Weight: " + safeUser.getWeight() + "kg\n"
                            + "- Selected Equipment: " + equipStr + "\n"
                            + "Generate a JSON object with these exact keys:\n"
                            + "- 'title' (string, e.g. '" + equipStr + " Shred Workout')\n"
                            + "- 'difficulty' (string: 'Beginner', 'Intermediate', 'Advanced')\n"
                            + "- 'calories' (int)\n"
                            + "- 'duration' (int)\n"
                            + "- 'exercises' (JSON array of objects, each having: 'name', 'sets' (int), 'reps' (int), 'durationSeconds' (int), 'description', 'targetMuscle', 'equipmentUsed')";

                    String json = callGeminiTextAPI(apiKey, prompt, true);
                    if (json != null && !json.trim().isEmpty()) {
                        JSONObject obj = new JSONObject(json);
                        com.fitness.app.models.EquipmentWorkoutPlan plan = new com.fitness.app.models.EquipmentWorkoutPlan();
                        plan.setTitle(obj.optString("title", equipStr + " Workout"));
                        plan.setDifficulty(obj.optString("difficulty", "Intermediate"));
                        plan.setCalories(obj.optInt("calories", 220));
                        plan.setDuration(obj.optInt("duration", 25));
                        plan.setExercisesJson(obj.optJSONArray("exercises") != null ? obj.optJSONArray("exercises").toString() : "[]");
                        plan.setDateCreated(System.currentTimeMillis());

                        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onSuccess(plan));
                        return;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Adaptive local fallback matching selected equipment
            try {
                User safeUser = getSafeUser(user);
                com.fitness.app.models.EquipmentWorkoutPlan fallback = new com.fitness.app.models.EquipmentWorkoutPlan();
                String eq = (selectedEquipment != null && !selectedEquipment.isEmpty()) ? selectedEquipment.get(0) : "Bodyweight";
                fallback.setTitle(eq + " Adaptive Workout");
                fallback.setDifficulty("Intermediate");
                fallback.setCalories(200);
                fallback.setDuration(20);

                JSONArray exArr = new JSONArray();
                JSONObject ex1 = new JSONObject();
                ex1.put("name", eq.equalsIgnoreCase("Chair") ? "Chair Squats" : (eq.equalsIgnoreCase("Dumbbells") ? "Dumbbell Goblet Squats" : "Bodyweight Squats"));
                ex1.put("sets", 3);
                ex1.put("reps", 12);
                ex1.put("durationSeconds", 0);
                ex1.put("description", "Controlled squat movement using " + eq);
                ex1.put("targetMuscle", "Quads & Glutes");
                ex1.put("equipmentUsed", eq);
                exArr.put(ex1);

                JSONObject ex2 = new JSONObject();
                ex2.put("name", eq.equalsIgnoreCase("Chair") ? "Chair Dips" : (eq.equalsIgnoreCase("Resistance Band") ? "Banded Chest Press" : "Push-ups"));
                ex2.put("sets", 3);
                ex2.put("reps", 10);
                ex2.put("durationSeconds", 0);
                ex2.put("description", "Upper body push using " + eq);
                ex2.put("targetMuscle", "Chest & Triceps");
                ex2.put("equipmentUsed", eq);
                exArr.put(ex2);

                fallback.setExercisesJson(exArr.toString());
                fallback.setDateCreated(System.currentTimeMillis());

                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onSuccess(fallback));
            } catch (Exception ignored) {}
        }).start();
    }
}

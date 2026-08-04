package com.fitness.app.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DietPlan implements Serializable {
    private String id;
    private int targetCalories;
    private int targetProteinGrams;
    private int targetCarbsGrams;
    private int targetFatGrams;
    private int targetWaterMl;
    private String dietType;
    private List<Meal> meals;
    private long createdAt;

    // New target values for personalized nutrition
    private int targetFiberGrams;
    private int targetSugarGrams;
    private int targetSodiumMg;

    public DietPlan() {
        this.meals = new ArrayList<>();
        this.targetFiberGrams = 25;
        this.targetSugarGrams = 50;
        this.targetSodiumMg = 2300;
    }

    public DietPlan(String id, int targetCalories, int targetProteinGrams, int targetCarbsGrams, int targetFatGrams, int targetWaterMl, String dietType, List<Meal> meals, long createdAt) {
        this(id, targetCalories, targetProteinGrams, targetCarbsGrams, targetFatGrams, targetWaterMl, dietType, meals, createdAt, 25, 50, 2300);
    }

    public DietPlan(String id, int targetCalories, int targetProteinGrams, int targetCarbsGrams, int targetFatGrams, int targetWaterMl, String dietType, List<Meal> meals, long createdAt, int targetFiberGrams, int targetSugarGrams, int targetSodiumMg) {
        this.id = id;
        this.targetCalories = targetCalories;
        this.targetProteinGrams = targetProteinGrams;
        this.targetCarbsGrams = targetCarbsGrams;
        this.targetFatGrams = targetFatGrams;
        this.targetWaterMl = targetWaterMl;
        this.dietType = dietType;
        this.meals = meals;
        this.createdAt = createdAt;
        this.targetFiberGrams = targetFiberGrams;
        this.targetSugarGrams = targetSugarGrams;
        this.targetSodiumMg = targetSodiumMg;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getTargetCalories() { return targetCalories; }
    public void setTargetCalories(int targetCalories) { this.targetCalories = targetCalories; }

    public int getTargetProteinGrams() { return targetProteinGrams; }
    public void setTargetProteinGrams(int targetProteinGrams) { this.targetProteinGrams = targetProteinGrams; }

    public int getTargetCarbsGrams() { return targetCarbsGrams; }
    public void setTargetCarbsGrams(int targetCarbsGrams) { this.targetCarbsGrams = targetCarbsGrams; }

    public int getTargetFatGrams() { return targetFatGrams; }
    public void setTargetFatGrams(int targetFatGrams) { this.targetFatGrams = targetFatGrams; }

    public int getTargetWaterMl() { return targetWaterMl; }
    public void setTargetWaterMl(int targetWaterMl) { this.targetWaterMl = targetWaterMl; }

    public String getDietType() { return dietType; }
    public void setDietType(String dietType) { this.dietType = dietType; }

    public List<Meal> getMeals() { return meals; }
    public void setMeals(List<Meal> meals) { this.meals = meals; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public int getTargetFiberGrams() { return targetFiberGrams; }
    public void setTargetFiberGrams(int targetFiberGrams) { this.targetFiberGrams = targetFiberGrams; }

    public int getTargetSugarGrams() { return targetSugarGrams; }
    public void setTargetSugarGrams(int targetSugarGrams) { this.targetSugarGrams = targetSugarGrams; }

    public int getTargetSodiumMg() { return targetSodiumMg; }
    public void setTargetSodiumMg(int targetSodiumMg) { this.targetSodiumMg = targetSodiumMg; }

    // JSON Serialization
    public JSONObject toJsonObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("targetCalories", targetCalories);
        obj.put("targetProteinGrams", targetProteinGrams);
        obj.put("targetCarbsGrams", targetCarbsGrams);
        obj.put("targetFatGrams", targetFatGrams);
        obj.put("targetWaterMl", targetWaterMl);
        obj.put("dietType", dietType);
        obj.put("createdAt", createdAt);
        obj.put("targetFiberGrams", targetFiberGrams);
        obj.put("targetSugarGrams", targetSugarGrams);
        obj.put("targetSodiumMg", targetSodiumMg);

        JSONArray arr = new JSONArray();
        for (Meal m : meals) {
            arr.put(m.toJsonObject());
        }
        obj.put("meals", arr);
        return obj;
    }

    // JSON Deserialization
    public static DietPlan fromJsonObject(JSONObject obj) throws JSONException {
        DietPlan plan = new DietPlan();
        plan.setId(obj.optString("id", ""));
        plan.setTargetCalories(obj.optInt("targetCalories", 0));
        plan.setTargetProteinGrams(obj.optInt("targetProteinGrams", 0));
        plan.setTargetCarbsGrams(obj.optInt("targetCarbsGrams", 0));
        plan.setTargetFatGrams(obj.optInt("targetFatGrams", 0));
        plan.setTargetWaterMl(obj.optInt("targetWaterMl", 0));
        plan.setDietType(obj.optString("dietType", ""));
        plan.setCreatedAt(obj.optLong("createdAt", 0));
        plan.setTargetFiberGrams(obj.optInt("targetFiberGrams", 25));
        plan.setTargetSugarGrams(obj.optInt("targetSugarGrams", 50));
        plan.setTargetSodiumMg(obj.optInt("targetSodiumMg", 2300));

        JSONArray arr = obj.optJSONArray("meals");
        List<Meal> list = new ArrayList<>();
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                list.add(Meal.fromJsonObject(arr.getJSONObject(i)));
            }
        }
        plan.setMeals(list);
        return plan;
    }

    public static class Meal implements Serializable {
        private String type; // e.g. Breakfast, Lunch, Dinner, Snack
        private String name;
        private int calories;
        private String ingredients;
        private String description;

        // New detailed nutritional fields
        private double protein;
        private double carbs;
        private double fat;
        private double fiber;
        private String servingSize;
        private String portionSize;

        public Meal() {}

        public Meal(String type, String name, int calories, String ingredients, String description) {
            this(type, name, calories, ingredients, description, 0.0, 0.0, 0.0, 0.0, "", "");
        }

        public Meal(String type, String name, int calories, String ingredients, String description, double protein, double carbs, double fat, double fiber, String servingSize, String portionSize) {
            this.type = type;
            this.name = name;
            this.calories = calories;
            this.ingredients = ingredients;
            this.description = description;
            this.protein = protein;
            this.carbs = carbs;
            this.fat = fat;
            this.fiber = fiber;
            this.servingSize = servingSize;
            this.portionSize = portionSize;
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getCalories() { return calories; }
        public void setCalories(int calories) { this.calories = calories; }

        public String getIngredients() { return ingredients; }
        public void setIngredients(String ingredients) { this.ingredients = ingredients; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public double getProtein() { return protein; }
        public void setProtein(double protein) { this.protein = protein; }

        public double getCarbs() { return carbs; }
        public void setCarbs(double carbs) { this.carbs = carbs; }

        public double getFat() { return fat; }
        public void setFat(double fat) { this.fat = fat; }

        public double getFiber() { return fiber; }
        public void setFiber(double fiber) { this.fiber = fiber; }

        public String getServingSize() { return servingSize; }
        public void setServingSize(String servingSize) { this.servingSize = servingSize; }

        public String getPortionSize() { return portionSize; }
        public void setPortionSize(String portionSize) { this.portionSize = portionSize; }

        public JSONObject toJsonObject() throws JSONException {
            JSONObject obj = new JSONObject();
            obj.put("type", type);
            obj.put("name", name);
            obj.put("calories", calories);
            obj.put("ingredients", ingredients);
            obj.put("description", description);
            obj.put("protein", protein);
            obj.put("carbs", carbs);
            obj.put("fat", fat);
            obj.put("fiber", fiber);
            obj.put("servingSize", servingSize);
            obj.put("portionSize", portionSize);
            return obj;
        }

        public static Meal fromJsonObject(JSONObject obj) throws JSONException {
            Meal meal = new Meal(
                obj.optString("type", ""),
                obj.optString("name", ""),
                obj.optInt("calories", 0),
                obj.optString("ingredients", ""),
                obj.optString("description", "")
            );
            meal.setProtein(obj.optDouble("protein", 0.0));
            meal.setCarbs(obj.optDouble("carbs", 0.0));
            meal.setFat(obj.optDouble("fat", 0.0));
            meal.setFiber(obj.optDouble("fiber", 0.0));
            meal.setServingSize(obj.optString("servingSize", ""));
            meal.setPortionSize(obj.optString("portionSize", ""));
            return meal;
        }
    }
}

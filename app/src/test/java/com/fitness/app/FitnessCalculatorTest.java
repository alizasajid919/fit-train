package com.fitness.app;

import com.fitness.app.utils.FitnessCalculator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FitnessCalculatorTest {

    @Test
    public void testBmiCalculation() {
        double weight = 70.0; // kg
        double height = 175.0; // cm -> 1.75m
        double expectedBmi = 70.0 / (1.75 * 1.75); // ~22.857

        double calculatedBmi = FitnessCalculator.calculateBmi(weight, height);
        assertEquals(expectedBmi, calculatedBmi, 0.01);
        assertEquals("Normal weight", FitnessCalculator.getBmiCategory(calculatedBmi));
    }

    @Test
    public void testBmiCategoryUnderweightAndObese() {
        assertEquals("Underweight", FitnessCalculator.getBmiCategory(16.5));
        assertEquals("Normal weight", FitnessCalculator.getBmiCategory(22.0));
        assertEquals("Overweight", FitnessCalculator.getBmiCategory(27.5));
        assertEquals("Obese", FitnessCalculator.getBmiCategory(32.0));
    }

    @Test
    public void testBmrAndTdeeCalculation() {
        double bmrMale = FitnessCalculator.calculateBmr(80.0, 180.0, 25, "Male");
        // (10*80) + (6.25*180) - (5*25) + 5 = 800 + 1125 - 125 + 5 = 1805
        assertEquals(1805.0, bmrMale, 1.0);

        double tdee = FitnessCalculator.calculateTdee(bmrMale, "Moderate");
        // 1805 * 1.55 = 2797.75
        assertEquals(2797.75, tdee, 2.0);

        int fatLossCal = FitnessCalculator.calculateDailyCalorieTarget(tdee, "Lose Weight");
        assertEquals(2297, fatLossCal, 2.0);

        int muscleGainCal = FitnessCalculator.calculateDailyCalorieTarget(tdee, "Build Muscle");
        assertEquals(3197, muscleGainCal, 2.0);
    }

    @Test
    public void testProteinAndWaterGoal() {
        int protein = FitnessCalculator.calculateProteinTarget(70.0, "Build Muscle");
        assertEquals(140, protein); // 70 * 2.0

        int water = FitnessCalculator.calculateDailyWaterGoal(70.0);
        assertEquals(2450, water); // 70 * 35
    }

    @Test
    public void testWeightProgressPercent() {
        double start = 90.0;
        double current = 80.0;
        double target = 70.0;

        double progress = FitnessCalculator.calculateWeightProgressPercent(start, current, target);
        assertEquals(50.0, progress, 0.01);
    }
}

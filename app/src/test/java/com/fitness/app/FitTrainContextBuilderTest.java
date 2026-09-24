package com.fitness.app;

import com.fitness.app.models.User;
import com.fitness.app.utils.FitTrainContextBuilder;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FitTrainContextBuilderTest {

    @Test
    public void testSystemInstructionContainsUserAndFeatureDirectory() {
        User user = new User("u123", "Zain", "Ali", "zain@example.com", System.currentTimeMillis());
        user.setWeight(75.0);
        user.setHeight(178.0);
        user.setGoal("Fat Loss");
        user.setAge(26);
        user.setGender("Male");
        user.setAvailableEquipment("Dumbbells, Chair");

        String systemPrompt = FitTrainContextBuilder.buildSystemInstruction(null, user);

        assertNotNull(systemPrompt);
        assertTrue(systemPrompt.contains("FitTrain AI Fitness Coach"));
        assertTrue(systemPrompt.contains("Zain"));
        assertTrue(systemPrompt.contains("75.0 kg"));
        assertTrue(systemPrompt.contains("178.0 cm"));
        assertTrue(systemPrompt.contains("Dumbbells, Chair"));
        assertTrue(systemPrompt.contains("Form Check"));
        assertTrue(systemPrompt.contains("Equipment Workout Generator"));
        assertTrue(systemPrompt.contains("Smart Meal Planner"));
        assertTrue(systemPrompt.contains("ANTI-HALLUCINATION RULES"));
    }
}

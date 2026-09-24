package com.fitness.app;

import com.fitness.app.models.User;
import com.fitness.app.utils.FitTrainAiEngine;

import org.junit.Test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class StaticAnswerBugTest {

    private User createTestUser() {
        User user = new User("test1", "Ali", "Raza", "ali@example.com", System.currentTimeMillis());
        user.setWeight(74.0);
        user.setHeight(176.0);
        user.setGoal("Fat Loss");
        user.setAge(25);
        user.setGender("Male");
        return user;
    }

    @Test
    public void testEveryUserQuestionProducesUniqueRelevantResponse() {
        User user = createTestUser();

        String q1 = "What is BMI?";
        String q2 = "Give me a chair workout.";
        String q3 = "How do I lose fat?";
        String q4 = "Explain squats.";
        String q5 = "What should I eat for breakfast?";
        String q6 = "What is my workout today?";
        String q7 = "What should I eat before workout?";
        String q8 = "How do I target upper chest?";
        String q9 = "What is creatine?";

        String r1 = FitTrainAiEngine.processQuery(q1, user, null, null);
        String r2 = FitTrainAiEngine.processQuery(q2, user, null, null);
        String r3 = FitTrainAiEngine.processQuery(q3, user, null, null);
        String r4 = FitTrainAiEngine.processQuery(q4, user, null, null);
        String r5 = FitTrainAiEngine.processQuery(q5, user, null, null);
        String r6 = FitTrainAiEngine.processQuery(q6, user, null, null);
        String r7 = FitTrainAiEngine.processQuery(q7, user, null, null);
        String r8 = FitTrainAiEngine.processQuery(q8, user, null, null);
        String r9 = FitTrainAiEngine.processQuery(q9, user, null, null);

        assertNotNull(r1);
        assertNotNull(r2);
        assertNotNull(r3);
        assertNotNull(r4);
        assertNotNull(r5);
        assertNotNull(r6);
        assertNotNull(r7);
        assertNotNull(r8);
        assertNotNull(r9);

        // Assert no two responses are identical
        assertNotEquals(r1, r2);
        assertNotEquals(r2, r3);
        assertNotEquals(r3, r4);
        assertNotEquals(r4, r5);
        assertNotEquals(r5, r6);
        assertNotEquals(r6, r7);
        assertNotEquals(r7, r8);
        assertNotEquals(r8, r9);

        // Assert semantic relevance to corresponding question
        assertTrue("R1 should discuss BMI", r1.contains("BMI") || r1.contains("Body Mass Index"));
        assertTrue("R2 should discuss Chair workout", r2.contains("Chair") || r2.contains("Dips"));
        assertTrue("R3 should discuss Fat Loss", r3.contains("Fat Loss") || r3.contains("Caloric Deficit"));
        assertTrue("R4 should discuss Squat form", r4.contains("Squat") || r4.contains("Stance"));
        assertTrue("R5 should discuss Breakfast options", r5.contains("Breakfast") || r5.contains("Oats") || r5.contains("Egg"));
        assertTrue("R6 should discuss Workout", r6.contains("Workout") || r6.contains("HIIT") || r6.contains("Routine"));
        assertTrue("R7 should discuss Pre-Workout Nutrition", r7.contains("Pre-Workout") || r7.contains("Carbs"));
        assertTrue("R8 should discuss Chest exercises", r8.contains("Chest") || r8.contains("Bench Press"));
        assertTrue("R9 should discuss Creatine supplement", r9.contains("Creatine") || r9.contains("ATP"));
    }
}

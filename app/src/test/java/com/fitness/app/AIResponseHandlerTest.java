package com.fitness.app;

import com.fitness.app.models.User;
import com.fitness.app.utils.AIResponseHandler;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AIResponseHandlerTest {

    private User createSampleUser() {
        User user = new User("101", "Sara", "Khan", "sara@example.com", System.currentTimeMillis());
        user.setWeight(65.0);
        user.setHeight(165.0);
        user.setGoal("Build Lean Muscle");
        user.setAge(24);
        user.setGender("Female");
        return user;
    }

    @Test
    public void testWorkoutQueryReturnsStructuredInfo() {
        User user = createSampleUser();
        String response = AIResponseHandler.generateResponse("What workout should I do today?", user);

        assertNotNull(response);
        assertTrue(response.contains("Sets & Reps"));
        assertTrue(response.contains("Rest:"));
        assertTrue(response.contains("Form Tips"));
    }

    @Test
    public void testEquipmentWorkoutQuery() {
        User user = createSampleUser();
        String response = AIResponseHandler.generateResponse("How can I do a chair or bottle workout?", user);

        assertNotNull(response);
        assertTrue(response.contains("Equipment Workout Generator"));
        assertTrue(response.contains("Chair Dips"));
        assertTrue(response.contains("Water Bottles"));
    }

    @Test
    public void testFormCheckGuideQuery() {
        User user = createSampleUser();
        String response = AIResponseHandler.generateResponse("How do I use Form Check?", user);

        assertNotNull(response);
        assertTrue(response.contains("Form Check"));
        assertTrue(response.contains("computer vision"));
    }

    @Test
    public void testAntiHallucinationForStepsWithoutData() {
        User user = createSampleUser();
        String response = AIResponseHandler.generateResponse("What are my steps today?", user);

        assertNotNull(response);
        assertTrue(response.contains("don't have enough data to determine your steps count accurately"));
    }

    @Test
    public void testCalculatedBmiResponse() {
        User user = createSampleUser();
        String response = AIResponseHandler.generateResponse("What is my BMI?", user);

        assertNotNull(response);
        assertTrue(response.contains("BMI is 23.9"));
        assertTrue(response.contains("Normal weight"));
    }
}

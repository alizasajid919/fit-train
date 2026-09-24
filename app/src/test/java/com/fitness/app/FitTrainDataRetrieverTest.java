package com.fitness.app;

import com.fitness.app.models.User;
import com.fitness.app.utils.FitTrainDataRetriever;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FitTrainDataRetrieverTest {

    @Test
    public void testGetUserProfileFormatting() {
        User user = new User("u200", "Usman", "Tariq", "usman@example.com", System.currentTimeMillis());
        user.setWeight(78.0);
        user.setHeight(180.0);
        user.setGoal("Build Muscle");
        user.setActivityLevel("High");
        user.setAvailableEquipment("Dumbbells, Resistance Bands");

        String profileStr = FitTrainDataRetriever.getUserProfile(null, user);

        assertNotNull(profileStr);
        assertTrue(profileStr.contains("Usman"));
        assertTrue(profileStr.contains("78.0 kg"));
        assertTrue(profileStr.contains("180.0 cm"));
        assertTrue(profileStr.contains("Build Muscle"));
        assertTrue(profileStr.contains("Dumbbells, Resistance Bands"));
    }
}

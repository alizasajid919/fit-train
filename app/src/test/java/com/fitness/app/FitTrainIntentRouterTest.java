package com.fitness.app;

import com.fitness.app.utils.FitTrainIntentRouter;
import com.fitness.app.utils.FitTrainIntentRouter.Intent;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FitTrainIntentRouterTest {

    @Test
    public void testWorkoutTodayIntentDetection() {
        assertEquals(Intent.WORKOUT_TODAY, FitTrainIntentRouter.detectIntent("aj mera workout kya hai?"));
        assertEquals(Intent.WORKOUT_TODAY, FitTrainIntentRouter.detectIntent("what am I supposed to train today?"));
        assertEquals(Intent.WORKOUT_TODAY, FitTrainIntentRouter.detectIntent("today's exercise?"));
        assertEquals(Intent.WORKOUT_TODAY, FitTrainIntentRouter.detectIntent("mujhe aaj kya karna hai?"));
        assertEquals(Intent.WORKOUT_TODAY, FitTrainIntentRouter.detectIntent("what to do at the gym today"));
    }

    @Test
    public void testDietTodayIntentDetection() {
        assertEquals(Intent.DIET_TODAY, FitTrainIntentRouter.detectIntent("mera diet plan dikhao"));
        assertEquals(Intent.DIET_TODAY, FitTrainIntentRouter.detectIntent("what should I eat today?"));
        assertEquals(Intent.DIET_TODAY, FitTrainIntentRouter.detectIntent("aj kya khana hai"));
        assertEquals(Intent.DIET_TODAY, FitTrainIntentRouter.detectIntent("show today's meals"));
        assertEquals(Intent.DIET_TODAY, FitTrainIntentRouter.detectIntent("what did I eat today"));
    }

    @Test
    public void testCaloriesAndStepsIntentDetection() {
        assertEquals(Intent.CALORIES_BURNED, FitTrainIntentRouter.detectIntent("how many calories did I burn today?"));
        assertEquals(Intent.STEPS_ACTIVITY, FitTrainIntentRouter.detectIntent("my steps today"));
        assertEquals(Intent.WATER_HYDRATION, FitTrainIntentRouter.detectIntent("how much water did I drink?"));
    }

    @Test
    public void testEquipmentAndFormCheckIntentDetection() {
        assertEquals(Intent.EQUIPMENT_WORKOUT, FitTrainIntentRouter.detectIntent("how can I do a chair or bottle workout?"));
        assertEquals(Intent.FORM_CHECK_HELP, FitTrainIntentRouter.detectIntent("how to use Form Check pose detection?"));
    }
}

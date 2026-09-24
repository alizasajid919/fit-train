package com.fitness.app.utils;

import android.content.Context;

import com.fitness.app.models.ChatMessage;
import com.fitness.app.models.User;

import java.util.List;

public class AIResponseHandler {

    public static String generateResponse(String query, User user) {
        return generateResponse(query, user, null, null);
    }

    public static String generateResponse(String query, User user, List<ChatMessage> history) {
        return generateResponse(query, user, history, null);
    }

    public static String generateResponse(String query, User user, List<ChatMessage> history, Context context) {
        return FitTrainAiEngine.processQuery(query, user, history, context);
    }
}

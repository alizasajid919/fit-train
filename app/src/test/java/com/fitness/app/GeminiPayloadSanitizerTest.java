package com.fitness.app;

import com.fitness.app.models.ChatMessage;
import com.fitness.app.repositories.AIRepository;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class GeminiPayloadSanitizerTest {

    @Test
    public void testStrictRoleAlternationInGeminiPayload() throws Exception {
        List<ChatMessage> history = new ArrayList<>();
        history.add(new ChatMessage("1", "USER", "Hello", System.currentTimeMillis()));
        history.add(new ChatMessage("2", "USER", "What is my workout today?", System.currentTimeMillis())); // Consecutive USER message
        history.add(new ChatMessage("3", "AI", "Here is your workout", System.currentTimeMillis()));
        history.add(new ChatMessage("4", "USER", "Make it easier", System.currentTimeMillis()));

        JSONArray sanitized = AIRepository.sanitizeHistoryForGemini(history, "How many reps?");

        assertNotNull(sanitized);
        assertTrue(sanitized.length() >= 2);

        // Verify that roles strictly alternate: user -> model -> user ...
        String prevRole = "";
        for (int i = 0; i < sanitized.length(); i++) {
            JSONObject turn = sanitized.getJSONObject(i);
            String role = turn.getString("role");

            if (i == 0) {
                assertEquals("user", role); // First turn must be user
            } else {
                assertNotEquals("Roles must strictly alternate!", prevRole, role);
            }
            prevRole = role;
        }

        // Verify final turn is 'user' containing current query
        JSONObject lastTurn = sanitized.getJSONObject(sanitized.length() - 1);
        assertEquals("user", lastTurn.getString("role"));
    }
}

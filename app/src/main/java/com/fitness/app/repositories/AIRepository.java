package com.fitness.app.repositories;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import com.fitness.app.data.network.AICallback;
import com.fitness.app.data.network.AINetworkInterface;
import com.fitness.app.data.room.AppDatabase;
import com.fitness.app.data.room.ChatDao;
import com.fitness.app.models.ChatMessage;
import com.fitness.app.models.User;
import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.utils.FitTrainAiEngine;
import com.fitness.app.utils.FitTrainContextBuilder;
import com.fitness.app.utils.FitTrainDataRetriever;
import com.fitness.app.utils.FitTrainIntentRouter;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AIRepository implements AINetworkInterface {

    private final ChatDao chatDao;
    private final Context context;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public AIRepository(Context context) {
        this.context = context;
        AppDatabase db = AppDatabase.getInstance(context);
        this.chatDao = db.chatDao();
    }

    @Override
    public void queryAI(String query, String userProfileContext, AICallback callback) {
        queryAIWithUser(query, null, callback);
    }

    public void queryAIWithUser(String query, User user, AICallback callback) {
        new Thread(() -> {
            try {
                // 1. Check local SharedPreferences for key first (User Override)
                SharedPreferences prefs = context.getSharedPreferences("ai_prefs", Context.MODE_PRIVATE);
                String apiKey = prefs.getString("gemini_api_key", null);

                // 2. Fallback to Firestore for key if not in local prefs
                if (apiKey == null || apiKey.trim().isEmpty()) {
                    apiKey = getGeminiApiKeyFromFirestore();
                }

                // 3. If API key exists, attempt to call real Gemini API
                if (apiKey != null && !apiKey.trim().isEmpty()) {
                    try {
                        String geminiResponse = callGeminiAPI(apiKey, query, user);
                        if (geminiResponse != null && !geminiResponse.trim().isEmpty()) {
                            mainHandler.post(() -> callback.onSuccess(geminiResponse));
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        // Continue to local engine fallback if remote call throws network exception
                    }
                }

                // 4. FitTrain AI Engine Fallback if key missing or remote fails
                Thread.sleep(800); // Simulate realistic response delay
                List<ChatMessage> history = chatDao.getAllMessages();
                String engineResponse = FitTrainAiEngine.processQuery(query, user, history, context);
                mainHandler.post(() -> callback.onSuccess(engineResponse));

            } catch (Exception e) {
                mainHandler.post(() -> callback.onFailure(e));
            }
        }).start();
    }

    private String getGeminiApiKeyFromFirestore() {
        try {
            Task<DocumentSnapshot> task = FirebaseFirestore.getInstance()
                    .collection("config").document("ai").get();
            DocumentSnapshot snapshot = Tasks.await(task);
            if (snapshot.exists()) {
                return snapshot.getString("gemini_api_key");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String callGeminiAPI(String apiKey, String currentQuery, User user) throws Exception {
        URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(15000);

        JSONObject bodyJson = new JSONObject();

        // 1. Detect Intent and fetch specific data for context injection
        FitTrainIntentRouter.Intent intent = FitTrainIntentRouter.detectIntent(currentQuery);
        String intentRetrievedData = "";

        switch (intent) {
            case WORKOUT_TODAY:
            case WORKOUT_PLAN:
                intentRetrievedData = "RETRIEVED WORKOUT DATA: " + FitTrainDataRetriever.getTodayWorkout(context, user);
                break;
            case WORKOUT_HISTORY:
                intentRetrievedData = "RETRIEVED WORKOUT HISTORY: " + FitTrainDataRetriever.getWorkoutHistory(context);
                break;
            case DIET_TODAY:
            case MEAL_SCHEDULE:
                intentRetrievedData = "RETRIEVED DIET DATA: " + FitTrainDataRetriever.getTodayDiet(context);
                break;
            case CALORIES_BURNED:
            case STEPS_ACTIVITY:
            case WATER_HYDRATION:
            case SLEEP_RECOVERY:
                intentRetrievedData = "RETRIEVED DAILY ACTIVITY: " + FitTrainDataRetriever.getDailyActivity(context);
                break;
            case PROGRESS_STREAK:
                intentRetrievedData = "RETRIEVED PROGRESS/STREAK: " + FitTrainDataRetriever.getStreakAndProgress(context, user);
                break;
            default:
                break;
        }

        // System Instruction Config via FitTrainContextBuilder
        LocalDataManager localDb = new LocalDataManager(context);
        boolean noBodyShaming = localDb.isBodyShamingProtectionEnabled();
        String currentLang = localDb.sharedPreferences.getString("app_language_code", "en");

        String shamingInstruction = noBodyShaming ? 
            "You must ALWAYS respond with highly supportive, motivational, empathetic, and respectful language. Avoid negative, critical, or judgmental wording." :
            "Use your standard coaching style, remaining professional, direct, and respectful.";

        String languageInstruction = "You MUST write your entire response ONLY in ";
        if ("ur".equals(currentLang)) {
            languageInstruction += "Urdu language using Urdu characters (Persian-Arabic script). Do not use English text.";
        } else if ("es".equals(currentLang)) {
            languageInstruction += "Spanish language.";
        } else if ("de".equals(currentLang)) {
            languageInstruction += "German language.";
        } else {
            languageInstruction += "English language.";
        }

        String baseSystemPrompt = FitTrainContextBuilder.buildSystemInstruction(context, user);
        String finalSystemPrompt = baseSystemPrompt + "\n"
                + "DETECTED USER INTENT: " + intent.name() + "\n"
                + intentRetrievedData + "\n"
                + shamingInstruction + " " + languageInstruction;

        JSONObject systemInstruction = new JSONObject();
        JSONArray parts = new JSONArray();
        JSONObject textObj = new JSONObject();
        textObj.put("text", finalSystemPrompt);
        parts.put(textObj);
        systemInstruction.put("parts", parts);
        bodyJson.put("systemInstruction", systemInstruction);

        // Fetch and sanitize history to ensure strictly alternating roles ('user', 'model', 'user', 'model'...)
        List<ChatMessage> rawHistory = chatDao.getAllMessages();
        JSONArray contentsArray = sanitizeHistoryForGemini(rawHistory, currentQuery);
        bodyJson.put("contents", contentsArray);

        // Send payload
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = bodyJson.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int code = conn.getResponseCode();
        if (code == 200) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line.trim());
                }
                JSONObject respObj = new JSONObject(response.toString());
                JSONArray candidates = respObj.getJSONArray("candidates");
                if (candidates.length() > 0) {
                    JSONObject firstCand = candidates.getJSONObject(0);
                    JSONObject content = firstCand.getJSONObject("content");
                    JSONArray responseParts = content.getJSONArray("parts");
                    if (responseParts.length() > 0) {
                        return responseParts.getJSONObject(0).getString("text");
                    }
                }
            }
        } else {
            // Throw exception to initiate fallback
            throw new RuntimeException("HTTP error code: " + code);
        }
        return null;
    }

    public static JSONArray sanitizeHistoryForGemini(List<ChatMessage> rawHistory, String currentQuery) throws Exception {
        JSONArray contentsArray = new JSONArray();
        List<JSONObject> turns = new ArrayList<>();

        if (rawHistory != null) {
            int startIndex = Math.max(0, rawHistory.size() - 15);
            for (int i = startIndex; i < rawHistory.size(); i++) {
                ChatMessage msg = rawHistory.get(i);
                if (msg == null || "AI_TYPING".equals(msg.getSender()) || "TYPING_ID".equals(msg.getId())) {
                    continue;
                }

                String role = "USER".equalsIgnoreCase(msg.getSender()) ? "user" : "model";
                String text = msg.getText() != null ? msg.getText().trim() : "";
                if (text.isEmpty()) continue;

                if (turns.isEmpty()) {
                    if ("user".equals(role)) {
                        JSONObject turn = new JSONObject();
                        turn.put("role", "user");
                        turn.put("text", text);
                        turns.add(turn);
                    }
                } else {
                    JSONObject lastTurn = turns.get(turns.size() - 1);
                    String lastRole = lastTurn.getString("role");

                    if (role.equals(lastRole)) {
                        // Merge consecutive same-role messages
                        String prevText = lastTurn.getString("text");
                        lastTurn.put("text", prevText + "\n" + text);
                    } else {
                        JSONObject turn = new JSONObject();
                        turn.put("role", role);
                        turn.put("text", text);
                        turns.add(turn);
                    }
                }
            }
        }

        // Handle currentQuery
        if (currentQuery != null && !currentQuery.trim().isEmpty()) {
            String cleanQuery = currentQuery.trim();
            if (turns.isEmpty()) {
                JSONObject turn = new JSONObject();
                turn.put("role", "user");
                turn.put("text", cleanQuery);
                turns.add(turn);
            } else {
                JSONObject lastTurn = turns.get(turns.size() - 1);
                String lastRole = lastTurn.getString("role");
                if ("user".equals(lastRole)) {
                    String prevText = lastTurn.getString("text");
                    if (!prevText.contains(cleanQuery)) {
                        lastTurn.put("text", prevText + "\n" + cleanQuery);
                    }
                } else {
                    JSONObject turn = new JSONObject();
                    turn.put("role", "user");
                    turn.put("text", cleanQuery);
                    turns.add(turn);
                }
            }
        }

        // Format into Gemini API structure
        for (JSONObject t : turns) {
            JSONObject contentObj = new JSONObject();
            contentObj.put("role", t.getString("role"));

            JSONArray contentParts = new JSONArray();
            JSONObject contentTextObj = new JSONObject();
            contentTextObj.put("text", t.getString("text"));
            contentParts.put(contentTextObj);
            contentObj.put("parts", contentParts);

            contentsArray.put(contentObj);
        }

        return contentsArray;
    }
}

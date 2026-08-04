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
import com.fitness.app.utils.AIResponseHandler;
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
                        // Continue to local fallback if remote call throws network exception
                    }
                }

                // 4. Local Emulator Fallback if key missing or remote fails
                Thread.sleep(1200); // Simulate network latency
                // Get the N latest history messages from Room database to pass into fallback memory
                List<ChatMessage> history = chatDao.getAllMessages();
                String fallbackResponse = AIResponseHandler.generateResponse(query, user, history);
                mainHandler.post(() -> callback.onSuccess(fallbackResponse));

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

        // System Instruction Config
        LocalDataManager localDb = new LocalDataManager(context);
        boolean noBodyShaming = localDb.isBodyShamingProtectionEnabled();
        String currentLang = localDb.sharedPreferences.getString("app_language_code", "en");

        String shamingInstruction = noBodyShaming ? 
            "You must ALWAYS respond with highly supportive, motivational, empathetic, and respectful language. Avoid negative, critical, or judgmental wording. Show encouraging workout and nutrition suggestions, and display positive achievement messages." :
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

        String systemPrompt = "You are an intelligent, professional, and friendly AI Fitness Coach and virtual assistant. "
                + shamingInstruction + " " + languageInstruction + " "
                + "Your goal is to provide fitness guidance, workout recommendations, diet planning, health tips, motivation, "
                + "but ALSO intelligently and helpfully answer general knowledge, educational, tech, programming, or everyday questions. "
                + "Do not restrict yourself to fitness if asked about something else, but you can gently relate it to health if contextually appropriate.";

        JSONObject systemInstruction = new JSONObject();
        JSONArray parts = new JSONArray();
        JSONObject textObj = new JSONObject();
        textObj.put("text", systemPrompt);
        parts.put(textObj);
        systemInstruction.put("parts", parts);
        bodyJson.put("systemInstruction", systemInstruction);

        // Fetch last 15 historical messages for context memory
        List<ChatMessage> history = chatDao.getAllMessages();
        int historySize = history.size();
        int startIndex = Math.max(0, historySize - 15);

        JSONArray contentsArray = new JSONArray();

        // User profile context to pass with first conversation block
        String userContext = "";
        if (user != null) {
            userContext = "User Context Profile - Name: " + user.getFirstName()
                    + ", goal: " + user.getGoal()
                    + ", current weight: " + user.getWeight() + " kg, height: " + user.getHeight() + " cm. ";
        }

        for (int i = startIndex; i < historySize; i++) {
            ChatMessage msg = history.get(i);
            if ("AI_TYPING".equals(msg.getSender()) || "TYPING_ID".equals(msg.getId())) {
                continue;
            }
            JSONObject contentObj = new JSONObject();
            contentObj.put("role", "USER".equals(msg.getSender()) ? "user" : "model");
            
            JSONArray contentParts = new JSONArray();
            JSONObject contentTextObj = new JSONObject();
            
            String msgText = msg.getText();
            if ("USER".equals(msg.getSender()) && i == startIndex && !userContext.isEmpty()) {
                msgText = "[" + userContext + "] " + msgText;
            }
            
            contentTextObj.put("text", msgText);
            contentParts.put(contentTextObj);
            contentObj.put("parts", contentParts);
            
            contentsArray.put(contentObj);
        }

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
}

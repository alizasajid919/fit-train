package com.fitness.app.repositories;

import android.net.Uri;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.fitness.app.data.local.LocalDataManager;
import com.fitness.app.models.DietPlan;
import com.fitness.app.models.ProgressLog;
import com.fitness.app.models.User;
import com.fitness.app.models.WorkoutPlan;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.fitness.app.models.WorkoutLog;
import com.fitness.app.data.room.LoggedMeal;
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.Map;

public class UserRepository {
    private static final String TAG = "UserRepository";
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;
    private final FirebaseStorage firebaseStorage;

    public UserRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public void logout() {
        firebaseAuth.signOut();
    }

    public boolean isEmailVerified() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            return user.isEmailVerified();
        }
        return false;
    }

    public LiveData<Resource<String>> resendEmailVerification() {
        MutableLiveData<Resource<String>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        result.setValue(Resource.success("Verification email sent successfully. Please check your inbox."));
                    } else {
                        String msg = task.getException() != null ? task.getException().getMessage() : "Failed to send verification email.";
                        result.setValue(Resource.error(msg));
                    }
                });
        } else {
            result.setValue(Resource.error("No active session found."));
        }
        return result;
    }

    // Silent Anonymous Authentication
    public LiveData<Resource<FirebaseUser>> signInAnonymously(LocalDataManager localDb) {
        MutableLiveData<Resource<FirebaseUser>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        firebaseAuth.signInAnonymously()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    FirebaseUser firebaseUser = task.getResult().getUser();
                    if (firebaseUser != null) {
                        // Create a default local user profile if none exists
                        User localUser = localDb.getUser();
                        if (localUser == null) {
                            localUser = new User(firebaseUser.getUid(), "Guest", "User", "", System.currentTimeMillis());
                            localDb.saveUser(localUser);
                        } else {
                            // Update UID if it changed
                            localUser.setUid(firebaseUser.getUid());
                            localDb.saveUser(localUser);
                        }
                        
                        // Try to upload to firestore under anonymous account (silent backup)
                        saveUserToFirestore(localUser, new MutableLiveData<>(), firebaseUser);
                        
                        result.setValue(Resource.success(firebaseUser));
                    } else {
                        result.setValue(Resource.error("Anonymous sign-in returned null user."));
                    }
                } else {
                    String errorMsg = task.getException() != null ? task.getException().getMessage() : "Anonymous login failed.";
                    result.setValue(Resource.error(errorMsg));
                }
            });

        return result;
    }

    // Account Creation/Linking (for Guest -> Real Account backup)
    public LiveData<Resource<FirebaseUser>> registerUser(String firstName, String lastName, String email, String password, LocalDataManager localDb) {
        MutableLiveData<Resource<FirebaseUser>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null && currentUser.isAnonymous()) {
            // Link anonymous account to email/password
            AuthCredential credential = EmailAuthProvider.getCredential(email, password);
            currentUser.linkWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        FirebaseUser firebaseUser = task.getResult().getUser();
                        if (firebaseUser != null) {
                            User localUser = localDb.getUser();
                            if (localUser == null) {
                                localUser = new User(firebaseUser.getUid(), firstName, lastName, email, System.currentTimeMillis());
                            } else {
                                localUser.setUid(firebaseUser.getUid());
                                localUser.setFirstName(firstName);
                                localUser.setLastName(lastName);
                                localUser.setEmail(email);
                            }
                            localUser.setProfileCompleted(true);
                            localDb.saveUser(localUser);

                            // Send Verification
                            firebaseUser.sendEmailVerification();

                            // Upload all local data to Firestore
                            syncLocalDataToFirestore(localDb);
                            
                            // Save profile to Firestore
                            saveUserToFirestore(localUser, result, firebaseUser);
                        } else {
                            result.setValue(Resource.error("Linking failed: User is null."));
                        }
                    } else {
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Linking failed.";
                        result.setValue(Resource.error(errorMsg));
                    }
                });
        } else {
            // Create user from scratch (if not logged in anonymously, though onboarding normally does this)
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        FirebaseUser firebaseUser = task.getResult().getUser();
                        if (firebaseUser != null) {
                            User user = new User(firebaseUser.getUid(), firstName, lastName, email, System.currentTimeMillis());
                            user.setProfileCompleted(true);
                            localDb.saveUser(user);
                            firebaseUser.sendEmailVerification();
                            saveUserToFirestore(user, result, firebaseUser);
                        } else {
                            result.setValue(Resource.error("Registration failed: User is null."));
                        }
                    } else {
                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Registration failed.";
                        result.setValue(Resource.error(errorMsg));
                    }
                });
        }

        return result;
    }

    private void saveUserToFirestore(User user, MutableLiveData<Resource<FirebaseUser>> resultLiveData, FirebaseUser firebaseUser) {
        firestore.collection("users").document(user.getUid())
            .set(user)
            .addOnSuccessListener(aVoid -> resultLiveData.setValue(Resource.success(firebaseUser)))
            .addOnFailureListener(e -> {
                Log.e(TAG, "Firestore write failed", e);
                // Non-crashing error handling
                if (e instanceof FirebaseFirestoreException && 
                    ((FirebaseFirestoreException) e).getCode() == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                    resultLiveData.setValue(Resource.error("Access Denied: Missing permissions in Firestore."));
                } else {
                    resultLiveData.setValue(Resource.error("Cloud sync failed: " + e.getMessage()));
                }
            });
    }

    // Login and Merge data
    public LiveData<Resource<FirebaseUser>> loginUser(String email, String password, LocalDataManager localDb) {
        MutableLiveData<Resource<FirebaseUser>> loginResult = new MutableLiveData<>();
        loginResult.setValue(Resource.loading());

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    FirebaseUser user = task.getResult().getUser();
                    if (user != null) {
                        // Download profile and merge
                        fetchAndMergeUserData(user.getUid(), localDb, loginResult, user);
                    } else {
                        loginResult.setValue(Resource.error("User is null."));
                    }
                } else {
                    String errorMsg = task.getException() != null ? task.getException().getMessage() : "Login failed.";
                    loginResult.setValue(Resource.error(errorMsg));
                }
            });

        return loginResult;
    }

    private void fetchAndMergeUserData(String uid, LocalDataManager localDb, MutableLiveData<Resource<FirebaseUser>> loginResult, FirebaseUser firebaseUser) {
        firestore.collection("users").document(uid).get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        User cloudUser = doc.toObject(User.class);
                        User localUser = localDb.getUser();
                        
                        // Merge profile: prefer the one with the latest updatedAt timestamp
                        if (cloudUser != null) {
                            if (localUser == null || cloudUser.getUpdatedAt() >= localUser.getUpdatedAt()) {
                                localDb.saveUser(cloudUser);
                            } else {
                                // Local is newer, upload local to Firestore
                                saveUserToFirestore(localUser, new MutableLiveData<>(), firebaseUser);
                            }
                        }
                    }
                    
                    // Fetch and merge workouts, diets, and progress logs from Firestore
                    fetchWorkoutsAndDietsFromFirestore(uid, localDb);
                    
                    loginResult.setValue(Resource.success(firebaseUser));
                } else {
                    // Even if firestore read fails, we don't crash, we let user enter the app
                    Log.e(TAG, "Failed to read profile from Firestore", task.getException());
                    loginResult.setValue(Resource.success(firebaseUser));
                }
            });
    }

    private void fetchWorkoutsAndDietsFromFirestore(String uid, LocalDataManager localDb) {
        // Fetch active workout plan
        firestore.collection("users").document(uid).collection("workouts").document("active").get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    try {
                        String jsonStr = doc.getString("json");
                        if (jsonStr != null) {
                            WorkoutPlan cloudPlan = WorkoutPlan.fromJsonObject(new JSONObject(jsonStr));
                            WorkoutPlan localPlan = localDb.getWorkoutPlan();
                            if (localPlan == null || cloudPlan.getCreatedAt() >= localPlan.getCreatedAt()) {
                                localDb.saveWorkoutPlan(cloudPlan);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing cloud workout plan", e);
                    }
                }
            }).addOnFailureListener(e -> Log.e(TAG, "Workout download failed: " + e.getMessage()));

        // Fetch active diet plan
        firestore.collection("users").document(uid).collection("diets").document("active").get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    try {
                        String jsonStr = doc.getString("json");
                        if (jsonStr != null) {
                            DietPlan cloudPlan = DietPlan.fromJsonObject(new JSONObject(jsonStr));
                            DietPlan localPlan = localDb.getDietPlan();
                            if (localPlan == null || cloudPlan.getCreatedAt() >= localPlan.getCreatedAt()) {
                                localDb.saveDietPlan(cloudPlan);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing cloud diet plan", e);
                    }
                }
            }).addOnFailureListener(e -> Log.e(TAG, "Diet download failed: " + e.getMessage()));

        // Fetch progress logs
        firestore.collection("users").document(uid).collection("progress_logs").get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                Map<String, ProgressLog> localLogs = localDb.getAllProgressLogs();
                boolean changed = false;
                for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                    try {
                        ProgressLog cloudLog = ProgressLog.fromJsonObject(new JSONObject(doc.getData()));
                        ProgressLog localLog = localLogs.get(cloudLog.getDate());
                        if (localLog == null || cloudLog.getUpdatedAt() >= localLog.getUpdatedAt()) {
                            localLogs.put(cloudLog.getDate(), cloudLog);
                            changed = true;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing cloud progress log", e);
                    }
                }
                if (changed) {
                    localDb.saveAllProgressLogs(localLogs);
                }
            }).addOnFailureListener(e -> Log.e(TAG, "Progress logs download failed: " + e.getMessage()));

        // Fetch workout logs
        firestore.collection("users").document(uid).collection("workout_logs").get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<WorkoutLog> localLogs = localDb.getAllWorkoutLogs();
                boolean changed = false;
                for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                    try {
                        WorkoutLog cloudLog = WorkoutLog.fromJsonObject(new JSONObject(doc.getData()));
                        boolean exists = false;
                        for (int i = 0; i < localLogs.size(); i++) {
                            if (localLogs.get(i).getId().equals(cloudLog.getId())) {
                                if (cloudLog.getTimestamp() > localLogs.get(i).getTimestamp()) {
                                    localLogs.set(i, cloudLog);
                                    changed = true;
                                }
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            localLogs.add(cloudLog);
                            changed = true;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing cloud workout log", e);
                    }
                }
                if (changed) {
                    localDb.saveAllWorkoutLogs(localLogs);
                }
            }).addOnFailureListener(e -> Log.e(TAG, "Workout logs download failed: " + e.getMessage()));

        // Restore preferences settings
        firestore.collection("users").document(uid).collection("settings").document("preferences").get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    Boolean dark = doc.getBoolean("darkThemeEnabled");
                    Boolean metric = doc.getBoolean("metricUnitsEnabled");
                    Boolean shaming = doc.getBoolean("bodyShamingProtectionEnabled");
                    String lang = doc.getString("languageCode");
                    
                    if (dark != null) localDb.setDarkThemeEnabled(dark);
                    if (metric != null) localDb.setMetricUnitsEnabled(metric);
                    if (shaming != null) localDb.setBodyShamingProtectionEnabled(shaming);
                    if (lang != null) {
                        localDb.sharedPreferences.edit().putString("app_language_code", lang).apply();
                        // Apply locale using AppCompatDelegate
                        androidx.core.os.LocaleListCompat locales = androidx.core.os.LocaleListCompat.forLanguageTags(lang);
                        androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(locales);
                    }
                }
            }).addOnFailureListener(e -> Log.e(TAG, "Preferences download failed: " + e.getMessage()));

        // Restore Chat History
        firestore.collection("users").document(uid).collection("chat_history").get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    try {
                        List<com.fitness.app.models.ChatMessage> list = new ArrayList<>();
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            String jsonStr = doc.getString("json");
                            if (jsonStr != null) {
                                list.add(com.fitness.app.models.ChatMessage.fromJsonObject(new org.json.JSONObject(jsonStr)));
                            }
                        }
                        if (!list.isEmpty()) {
                            localDb.saveChatMessages(list);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error restoring cloud chat", e);
                    }
                }
            }).addOnFailureListener(e -> Log.e(TAG, "Chat history download failed: " + e.getMessage()));

        // Restore Challenges & Stats (Room DB)
        android.content.Context context = localDb.getContext();
        if (context != null) {
            firestore.collection("users").document(uid).collection("stats").document("challenges").get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        new Thread(() -> {
                            try {
                                com.fitness.app.models.UserChallengeStats stats = new com.fitness.app.models.UserChallengeStats();
                                stats.setId(doc.getString("id"));
                                stats.setTotalXp(doc.getLong("totalXp").intValue());
                                stats.setUnlockedBadgesJson(doc.getString("unlockedBadgesJson"));
                                stats.setChallengesCompleted(doc.getLong("challengesCompleted").intValue());
                                stats.setCurrentStreak(doc.getLong("currentStreak").intValue());
                                stats.setMaxStreak(doc.getLong("maxStreak").intValue());
                                stats.setMissedDays(doc.getLong("missedDays").intValue());
                                stats.setDailyLogsJson(doc.getString("dailyLogsJson"));

                                com.fitness.app.data.room.AppDatabase.getInstance(context).fitnessDao().insertUserChallengeStats(stats);
                            } catch (Exception e) {
                                Log.e(TAG, "Error restoring stats", e);
                            }
                        }).start();
                    }
                });

            firestore.collection("users").document(uid).collection("challenges").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        new Thread(() -> {
                            try {
                                com.fitness.app.data.room.AppDatabase db = com.fitness.app.data.room.AppDatabase.getInstance(context);
                                for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                    com.fitness.app.models.FitnessChallenge ch = new com.fitness.app.models.FitnessChallenge();
                                    ch.setId(doc.getString("id"));
                                    ch.setName(doc.getString("name"));
                                    ch.setDescription(doc.getString("description"));
                                    ch.setDuration(doc.getString("duration"));
                                    ch.setDifficulty(doc.getString("difficulty"));
                                    ch.setXpReward(doc.getLong("xpReward").intValue());
                                    ch.setBadgeName(doc.getString("badgeName"));
                                    ch.setTasksJson(doc.getString("tasksJson"));
                                    ch.setProgressPercent(doc.getLong("progressPercent").intValue());
                                    ch.setDaysRemaining(doc.getLong("daysRemaining").intValue());
                                    ch.setStartDate(doc.getLong("startDate"));
                                    ch.setEndDate(doc.getLong("endDate"));
                                    ch.setStatus(doc.getString("status"));
                                    ch.setCaloriesEstimate(doc.getLong("caloriesEstimate").intValue());
                                    ch.setEstimatedWorkoutTime(doc.getLong("estimatedWorkoutTime").intValue());
                                    ch.setHealthBenefits(doc.getString("healthBenefits"));

                                    db.fitnessDao().insertFitnessChallenge(ch);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error restoring challenges", e);
                            }
                        }).start();
                    }
                });

            // Restore Logged Meals from Cloud
            firestore.collection("users").document(uid).collection("logged_meals").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        new Thread(() -> {
                            try {
                                com.fitness.app.data.room.AppDatabase db = com.fitness.app.data.room.AppDatabase.getInstance(context);
                                for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                    LoggedMeal m = new LoggedMeal(
                                            doc.getString("mealType"),
                                            doc.getString("name"),
                                            safeGetInt(doc, "calories"),
                                            safeGetInt(doc, "protein"),
                                            safeGetInt(doc, "carbs"),
                                            safeGetInt(doc, "fat"),
                                            doc.getString("date"),
                                            safeGetLong(doc, "timestamp"),
                                            doc.getString("mealTime"),
                                            doc.getString("notes"),
                                            doc.getBoolean("isChecked") != null ? doc.getBoolean("isChecked") : false,
                                            safeGetInt(doc, "iconResId"),
                                            safeGetInt(doc, "fiber"),
                                            safeGetInt(doc, "sugar"),
                                            doc.getString("servingSize"),
                                            doc.getString("ingredients"),
                                            doc.getString("steps")
                                    );
                                    m.id = Integer.parseInt(doc.getId());
                                    db.fitnessDao().insertLoggedMeal(m);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error restoring logged meals", e);
                            }
                        }).start();
                    }
                });
        }
    }

    // Sync all cached local data to cloud (called when internet returns or on account creation)
    public void syncLocalDataToFirestore(LocalDataManager localDb) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) return;

        String uid = currentUser.getUid();
        User localUser = localDb.getUser();
        if (localUser != null) {
            localUser.setUid(uid);
            firestore.collection("users").document(uid).set(localUser)
                .addOnFailureListener(e -> Log.e(TAG, "Sync profile failed: " + e.getMessage()));
        }

        WorkoutPlan localWorkout = localDb.getWorkoutPlan();
        if (localWorkout != null) {
            try {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("json", localWorkout.toJsonObject().toString());
                firestore.collection("users").document(uid).collection("workouts").document("active")
                    .set(map)
                    .addOnFailureListener(e -> Log.e(TAG, "Sync workout failed: " + e.getMessage()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        DietPlan localDiet = localDb.getDietPlan();
        if (localDiet != null) {
            try {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("json", localDiet.toJsonObject().toString());
                firestore.collection("users").document(uid).collection("diets").document("active")
                    .set(map)
                    .addOnFailureListener(e -> Log.e(TAG, "Sync diet failed: " + e.getMessage()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Map<String, ProgressLog> logs = localDb.getAllProgressLogs();
        if (!logs.isEmpty()) {
            WriteBatch batch = firestore.batch();
            for (ProgressLog log : logs.values()) {
                try {
                    Map<String, Object> data = new HashMap<>();
                    data.put("date", log.getDate());
                    data.put("waterConsumedMl", log.getWaterConsumedMl());
                    data.put("caloriesBurned", log.getCaloriesBurned());
                    data.put("caloriesConsumed", log.getCaloriesConsumed());
                    data.put("stepsCount", log.getStepsCount());
                    data.put("sleepDurationMinutes", log.getSleepDurationMinutes());
                    data.put("currentWeight", log.getCurrentWeight());
                    data.put("currentHeight", log.getCurrentHeight());
                    data.put("currentBmi", log.getCurrentBmi());
                    data.put("currentBodyFatPercentage", log.getCurrentBodyFatPercentage());
                    data.put("updatedAt", log.getUpdatedAt());

                    batch.set(firestore.collection("users").document(uid).collection("progress_logs").document(log.getDate()), data);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            batch.commit().addOnFailureListener(e -> Log.e(TAG, "Sync logs batch failed: " + e.getMessage()));
        }

        // Sync Workout Logs
        List<WorkoutLog> workoutLogs = localDb.getAllWorkoutLogs();
        if (!workoutLogs.isEmpty()) {
            WriteBatch batch = firestore.batch();
            for (WorkoutLog log : workoutLogs) {
                try {
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", log.getId());
                    data.put("exerciseName", log.getExerciseName());
                    data.put("sets", log.getSets());
                    data.put("reps", log.getReps());
                    data.put("weight", log.getWeight());
                    data.put("notes", log.getNotes());
                    data.put("date", log.getDate());
                    data.put("timestamp", log.getTimestamp());
                    
                    batch.set(firestore.collection("users").document(uid).collection("workout_logs").document(log.getId()), data);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            batch.commit().addOnFailureListener(e -> Log.e(TAG, "Sync workout logs batch failed: " + e.getMessage()));
        }

        // Sync Preferences settings
        try {
            Map<String, Object> prefs = new HashMap<>();
            prefs.put("darkThemeEnabled", localDb.isDarkThemeEnabled());
            prefs.put("metricUnitsEnabled", localDb.isMetricUnitsEnabled());
            prefs.put("bodyShamingProtectionEnabled", localDb.isBodyShamingProtectionEnabled());
            prefs.put("languageCode", localDb.sharedPreferences.getString("app_language_code", "en"));
            
            firestore.collection("users").document(uid).collection("settings").document("preferences").set(prefs)
                .addOnFailureListener(e -> Log.e(TAG, "Sync preferences failed: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Sync Chat History
        try {
            List<com.fitness.app.models.ChatMessage> chatMsgs = localDb.getChatMessages();
            if (chatMsgs != null && !chatMsgs.isEmpty()) {
                WriteBatch batch = firestore.batch();
                for (com.fitness.app.models.ChatMessage msg : chatMsgs) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", msg.getId());
                    data.put("json", msg.toJsonObject().toString());
                    batch.set(firestore.collection("users").document(uid).collection("chat_history").document(msg.getId()), data);
                }
                batch.commit().addOnFailureListener(e -> Log.e(TAG, "Sync chat batch failed: " + e.getMessage()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Sync Challenges & Challenge Stats (Room Database)
        android.content.Context context = localDb.getContext();
        if (context != null) {
            new Thread(() -> {
                try {
                    com.fitness.app.data.room.AppDatabase db = com.fitness.app.data.room.AppDatabase.getInstance(context);
                    
                    // Sync stats
                    com.fitness.app.models.UserChallengeStats stats = db.fitnessDao().getUserChallengeStats();
                    if (stats != null) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("id", stats.getId());
                        data.put("totalXp", stats.getTotalXp());
                        data.put("unlockedBadgesJson", stats.getUnlockedBadgesJson());
                        data.put("challengesCompleted", stats.getChallengesCompleted());
                        data.put("currentStreak", stats.getCurrentStreak());
                        data.put("maxStreak", stats.getMaxStreak());
                        data.put("missedDays", stats.getMissedDays());
                        data.put("dailyLogsJson", stats.getDailyLogsJson());
                        
                        firestore.collection("users").document(uid).collection("stats").document("challenges").set(data)
                            .addOnFailureListener(e -> Log.e(TAG, "Sync stats failed: " + e.getMessage()));
                    }

                    // Sync challenges list
                    List<com.fitness.app.models.FitnessChallenge> challengesList = db.fitnessDao().getAllFitnessChallenges();
                    if (challengesList != null && !challengesList.isEmpty()) {
                        WriteBatch batch = firestore.batch();
                        for (com.fitness.app.models.FitnessChallenge ch : challengesList) {
                            Map<String, Object> data = new HashMap<>();
                            data.put("id", ch.getId());
                            data.put("name", ch.getName());
                            data.put("description", ch.getDescription());
                            data.put("duration", ch.getDuration());
                            data.put("difficulty", ch.getDifficulty());
                            data.put("xpReward", ch.getXpReward());
                            data.put("badgeName", ch.getBadgeName());
                            data.put("tasksJson", ch.getTasksJson());
                            data.put("progressPercent", ch.getProgressPercent());
                            data.put("daysRemaining", ch.getDaysRemaining());
                            data.put("startDate", ch.getStartDate());
                            data.put("endDate", ch.getEndDate());
                            data.put("status", ch.getStatus());
                            data.put("caloriesEstimate", ch.getCaloriesEstimate());
                            data.put("estimatedWorkoutTime", ch.getEstimatedWorkoutTime());
                            data.put("healthBenefits", ch.getHealthBenefits());
                            
                            batch.set(firestore.collection("users").document(uid).collection("challenges").document(ch.getId()), data);
                        }
                        batch.commit().addOnFailureListener(e -> Log.e(TAG, "Sync challenges batch failed: " + e.getMessage()));
                    }

                    // Sync Logged Meals list to Cloud
                    List<LoggedMeal> mealsList = db.fitnessDao().getAllLoggedMeals();
                    if (mealsList != null && !mealsList.isEmpty()) {
                        WriteBatch batch = firestore.batch();
                        for (LoggedMeal m : mealsList) {
                            Map<String, Object> data = new HashMap<>();
                            data.put("id", m.id);
                            data.put("mealType", m.mealType);
                            data.put("name", m.name);
                            data.put("calories", m.calories);
                            data.put("protein", m.protein);
                            data.put("carbs", m.carbs);
                            data.put("fat", m.fat);
                            data.put("date", m.date);
                            data.put("timestamp", m.timestamp);
                            data.put("mealTime", m.mealTime);
                            data.put("notes", m.notes);
                            data.put("isChecked", m.isChecked);
                            data.put("iconResId", m.iconResId);
                            data.put("fiber", m.fiber);
                            data.put("sugar", m.sugar);
                            data.put("servingSize", m.servingSize);
                            data.put("ingredients", m.ingredients);
                            data.put("steps", m.steps);
                            
                            batch.set(firestore.collection("users").document(uid).collection("logged_meals").document(String.valueOf(m.id)), data);
                        }
                        batch.commit().addOnFailureListener(e -> Log.e(TAG, "Sync logged meals batch failed: " + e.getMessage()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    public LiveData<Resource<String>> resetPassword(String email) {
        MutableLiveData<Resource<String>> resetResult = new MutableLiveData<>();
        resetResult.setValue(Resource.loading());

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    resetResult.setValue(Resource.success("Password reset email sent."));
                } else {
                    String errorMsg = task.getException() != null ? task.getException().getMessage() : "Error sending email.";
                    resetResult.setValue(Resource.error(errorMsg));
                }
            });

        return resetResult;
    }

    public LiveData<Resource<User>> getUserProfile(String uid) {
        MutableLiveData<Resource<User>> profileResult = new MutableLiveData<>();
        profileResult.setValue(Resource.loading());

        firestore.collection("users").document(uid)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        User user = doc.toObject(User.class);
                        profileResult.setValue(Resource.success(user));
                    } else {
                        profileResult.setValue(Resource.error("User profile not found."));
                    }
                } else {
                    String errorMsg = task.getException() != null ? task.getException().getMessage() : "Failed to load profile.";
                    profileResult.setValue(Resource.error(errorMsg));
                }
            });

        return profileResult;
    }

    public String validateUserProfilePayload(User user) {
        if (user == null) return "User object is null";

        if (user.getHeight() > 0 && !com.fitness.app.utils.ValidationUtils.isValidHeight(user.getHeight())) {
            return "Invalid height data. Must be between 50 and 250 cm.";
        }

        if (user.getWeight() > 0 && !com.fitness.app.utils.ValidationUtils.isValidWeight(user.getWeight())) {
            return "Invalid weight data. Must be between 20 and 300 kg.";
        }

        if (user.getCountry() != null && !user.getCountry().isEmpty()) {
            if (user.getCity() != null && !user.getCity().isEmpty()) {
                if (!com.fitness.app.utils.LocationUtils.isValidCityForCountry(user.getCountry(), user.getCity())) {
                    return "Invalid city for selected country (" + user.getCountry() + ").";
                }
            }
        }

        if (user.getMedicalConditions() != null && !user.getMedicalConditions().isEmpty()) {
            if (!com.fitness.app.utils.ValidationUtils.isValidMedicalCondition(user.getMedicalConditions())) {
                return "Invalid medical condition option selected.";
            }
        }

        return null; // Valid
    }

    public LiveData<Resource<Void>> updateUserProfile(User user) {
        MutableLiveData<Resource<Void>> updateResult = new MutableLiveData<>();
        updateResult.setValue(Resource.loading());

        String validationErr = validateUserProfilePayload(user);
        if (validationErr != null) {
            updateResult.setValue(Resource.error("Backend validation failed: " + validationErr));
            return updateResult;
        }

        user.setUpdatedAt(System.currentTimeMillis());

        firestore.collection("users").document(user.getUid())
            .set(user)
            .addOnSuccessListener(aVoid -> updateResult.setValue(Resource.success(null)))
            .addOnFailureListener(e -> {
                Log.e(TAG, "Update failed", e);
                updateResult.setValue(Resource.error("Update failed: " + e.getMessage()));
            });

        return updateResult;
    }

    public LiveData<Resource<String>> uploadProfileImage(String uid, Uri imageUri) {
        MutableLiveData<Resource<String>> uploadResult = new MutableLiveData<>();
        uploadResult.setValue(Resource.loading());

        StorageReference fileRef = firebaseStorage.getReference().child("profile_images/" + uid);
        com.google.firebase.storage.StorageMetadata metadata = new com.google.firebase.storage.StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build();

        fileRef.putFile(imageUri, metadata)
            .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    
                    Map<String, Object> update = new HashMap<>();
                    update.put("profileImageUrl", imageUrl);
                    update.put("updatedAt", System.currentTimeMillis());
                    
                    firestore.collection("users").document(uid)
                        .update(update)
                        .addOnSuccessListener(aVoid -> uploadResult.setValue(Resource.success(imageUrl)))
                        .addOnFailureListener(e -> uploadResult.setValue(Resource.error("Failed to update photo URL: " + e.getMessage())));
                })
                .addOnFailureListener(e -> uploadResult.setValue(Resource.error("Failed to fetch image download URL: " + e.getMessage()))))
            .addOnFailureListener(e -> uploadResult.setValue(Resource.error("Storage upload failed: " + e.getMessage())));

        return uploadResult;
    }

    private static int safeGetInt(DocumentSnapshot doc, String field) {
        Object val = doc.get(field);
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        return 0;
    }

    private static long safeGetLong(DocumentSnapshot doc, String field) {
        Object val = doc.get(field);
        if (val instanceof Number) {
            return ((Number) val).longValue();
        }
        return 0L;
    }

    public static class Resource<T> {
        public enum Status { SUCCESS, ERROR, LOADING }
        public final Status status;
        public final T data;
        public final String message;

        private Resource(Status status, T data, String message) {
            this.status = status;
            this.data = data;
            this.message = message;
        }

        public static <T> Resource<T> success(T data) {
            return new Resource<>(Status.SUCCESS, data, null);
        }

        public static <T> Resource<T> error(String msg) {
            return new Resource<>(Status.ERROR, null, msg);
        }

        public static <T> Resource<T> loading() {
            return new Resource<>(Status.LOADING, null, null);
        }
    }
}

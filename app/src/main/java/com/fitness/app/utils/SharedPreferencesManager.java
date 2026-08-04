package com.fitness.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {
    private static final String PREF_NAME = "fitness_app_prefs";
    private static final String KEY_ONBOARDING_SEEN = "onboarding_seen";
    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_SAVED_EMAIL = "saved_email";
    private static final String KEY_SAVED_PASSWORD = "saved_password";

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    public SharedPreferencesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void setOnboardingSeen(boolean seen) {
        editor.putBoolean(KEY_ONBOARDING_SEEN, seen);
        editor.apply();
    }

    public boolean isOnboardingSeen() {
        return sharedPreferences.getBoolean(KEY_ONBOARDING_SEEN, false);
    }

    public void setRememberMe(boolean remember, String email, String password) {
        editor.putBoolean(KEY_REMEMBER_ME, remember);
        if (remember) {
            editor.putString(KEY_SAVED_EMAIL, email);
            editor.putString(KEY_SAVED_PASSWORD, password);
        } else {
            editor.remove(KEY_SAVED_EMAIL);
            editor.remove(KEY_SAVED_PASSWORD);
        }
        editor.apply();
    }

    public boolean isRememberMeEnabled() {
        return sharedPreferences.getBoolean(KEY_REMEMBER_ME, false);
    }

    public String getSavedEmail() {
        return sharedPreferences.getString(KEY_SAVED_EMAIL, "");
    }

    public String getSavedPassword() {
        return sharedPreferences.getString(KEY_SAVED_PASSWORD, "");
    }

    public void setSelectedTab(int tabId) {
        editor.putInt("last_selected_tab", tabId);
        editor.apply();
    }

    public int getSelectedTab(int defaultTabId) {
        return sharedPreferences.getInt("last_selected_tab", defaultTabId);
    }

    public void clearAll() {
        editor.clear();
        editor.apply();
    }
}

package com.fitness.app;

import android.app.Activity;
import android.app.Application;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.fitness.app.utils.TranslationHelper;
import java.util.Locale;

public class FitnessApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Restore theme preference globally on app startup
        com.fitness.app.data.local.LocalDataManager localDb = new com.fitness.app.data.local.LocalDataManager(this);
        boolean isDark = localDb.isDarkThemeEnabled();
        AppCompatDelegate.setDefaultNightMode(
            isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        // Restore language preference globally on app startup configuration
        String langCode = localDb.sharedPreferences.getString("app_language_code", "en");
        if (langCode != null && !langCode.trim().isEmpty() && !langCode.equalsIgnoreCase("en")) {
            try {
                Locale newLocale = new Locale(langCode);
                Locale.setDefault(newLocale);
                Configuration config = getResources().getConfiguration();
                config.setLocale(newLocale);
                getResources().updateConfiguration(config, getResources().getDisplayMetrics());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Enforce LTR layout direction, apply localization and dark mode adjustment globally
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                // Sync theme state early on activity creation
                com.fitness.app.data.local.LocalDataManager localDb = new com.fitness.app.data.local.LocalDataManager(activity);
                boolean isDark = localDb.isDarkThemeEnabled();
                AppCompatDelegate.setDefaultNightMode(
                    isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
                );
                applyGlobalSettings(activity);
            }

            @Override
            public void onActivityStarted(Activity activity) {}

            @Override
            public void onActivityResumed(Activity activity) {
                com.fitness.app.data.local.LocalDataManager localDb = new com.fitness.app.data.local.LocalDataManager(activity);
                
                // 1. Verify and enforce global night mode alignment to prevent theme resets during navigation
                boolean isDark = localDb.isDarkThemeEnabled();
                int currentNightMode = activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                boolean isActivityDark = (currentNightMode == Configuration.UI_MODE_NIGHT_YES);
                if (isDark != isActivityDark) {
                    AppCompatDelegate.setDefaultNightMode(
                        isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
                    );
                    activity.recreate();
                    return;
                }

                // 2. Verify if activity locale configuration is up-to-date with preference
                String langCode = localDb.sharedPreferences.getString("app_language_code", "en");
                String activityLang = activity.getResources().getConfiguration().locale.getLanguage();
                if (langCode != null && !langCode.trim().isEmpty()) {
                    String prefix = langCode.length() > 2 ? langCode.substring(0, 2) : langCode;
                    if (!activityLang.equalsIgnoreCase(prefix)) {
                        // Force update locale in Configuration programmatically
                        try {
                            Locale newLocale = new Locale(langCode);
                            Locale.setDefault(newLocale);
                            
                            Configuration config = activity.getResources().getConfiguration();
                            config.setLocale(newLocale);
                            activity.getResources().updateConfiguration(config, activity.getResources().getDisplayMetrics());

                            Configuration appConfig = activity.getApplicationContext().getResources().getConfiguration();
                            appConfig.setLocale(newLocale);
                            activity.getApplicationContext().getResources().updateConfiguration(appConfig, activity.getApplicationContext().getResources().getDisplayMetrics());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        
                        // Force clean refresh of the activity to load correct localized resource folders
                        activity.recreate();
                        return;
                    }
                }

                applyGlobalSettings(activity);
            }

            @Override
            public void onActivityPaused(Activity activity) {}

            @Override
            public void onActivityStopped(Activity activity) {}

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

            @Override
            public void onActivityDestroyed(Activity activity) {}
        });
    }

    private void applyGlobalSettings(Activity activity) {
        if (activity.getWindow() != null && activity.getWindow().getDecorView() != null) {
            // Force Left-to-Right layout direction
            activity.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);

            com.fitness.app.data.local.LocalDataManager localDb = new com.fitness.app.data.local.LocalDataManager(activity);
            
            // 1. Handle Dark Mode Adjustments
            boolean isDark = localDb.isDarkThemeEnabled();
            if (isDark) {
                View rootContainer = activity.findViewById(android.R.id.content);
                if (rootContainer != null) {
                    rootContainer.setBackgroundColor(0xFF0F172A);
                }
                adjustDarkMode(activity.getWindow().getDecorView(), isDark);
            }

            // 2. Handle Language Localization
            String langCode = localDb.sharedPreferences.getString("app_language_code", "en");
            if (langCode != null && !langCode.trim().isEmpty() && !langCode.equalsIgnoreCase("en")) {
                // Ensure locale config is forced on base context
                try {
                    Locale newLocale = new Locale(langCode);
                    Locale.setDefault(newLocale);
                    Configuration config = activity.getResources().getConfiguration();
                    if (!config.locale.getLanguage().equalsIgnoreCase(langCode)) {
                        config.setLocale(newLocale);
                        activity.getResources().updateConfiguration(config, activity.getResources().getDisplayMetrics());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Translate standard TextView, Button, Checkbox, etc. (excluding user inputs)
                translateView(activity.getWindow().getDecorView(), langCode);

                // Translate ActionBar title
                if (activity instanceof AppCompatActivity) {
                    androidx.appcompat.app.ActionBar actionBar = ((AppCompatActivity) activity).getSupportActionBar();
                    if (actionBar != null && actionBar.getTitle() != null) {
                        String transTitle = TranslationHelper.getTranslation(actionBar.getTitle().toString(), langCode);
                        actionBar.setTitle(transTitle);
                    }
                }

                // Translate Toolbar titles
                translateToolbar(activity.getWindow().getDecorView(), langCode);

                // Translate Bottom Navigation menu items programmatically
                translateBottomNav(activity.getWindow().getDecorView(), langCode);
            }
        }
    }

    private void adjustDarkMode(View view, boolean isDark) {
        if (view == null) return;

        if (isDark) {
            // Adjust CardViews
            if (view instanceof CardView) {
                CardView card = (CardView) view;
                int cardBgColor = card.getCardBackgroundColor().getDefaultColor();
                if (isLightColor(cardBgColor)) {
                    card.setCardBackgroundColor(ColorStateList.valueOf(0xFF1E293B));
                }
            } else if (view instanceof MaterialCardView) {
                MaterialCardView card = (MaterialCardView) view;
                int cardBgColor = card.getCardBackgroundColor().getDefaultColor();
                if (isLightColor(cardBgColor)) {
                    card.setCardBackgroundColor(ColorStateList.valueOf(0xFF1E293B));
                }
            }

            // Override view background tint lists if they have hardcoded white backgroundTint
            ColorStateList tintList = view.getBackgroundTintList();
            if (tintList != null) {
                int tintColor = tintList.getDefaultColor();
                if (isLightColor(tintColor)) {
                    view.setBackgroundTintList(ColorStateList.valueOf(0xFF1E293B));
                }
            }

            // Adjust backgrounds
            Drawable background = view.getBackground();
            if (background instanceof ColorDrawable) {
                int bgColor = ((ColorDrawable) background).getColor();
                if (isLightColor(bgColor)) {
                    if (view.getId() == android.R.id.content) {
                        view.setBackgroundColor(0xFF0F172A);
                    } else {
                        view.setBackgroundColor(0xFF1E293B);
                    }
                }
            }

            // Adjust TextViews (including buttons, edit texts, checkboxes, switches, text inputs)
            if (view instanceof TextView) {
                TextView tv = (TextView) view;
                int textColor = tv.getCurrentTextColor();
                if (textColor != 0 && textColor != Color.TRANSPARENT) {
                    int r = Color.red(textColor);
                    int g = Color.green(textColor);
                    int b = Color.blue(textColor);
                    int max = Math.max(r, Math.max(g, b));
                    int min = Math.min(r, Math.min(g, b));
                    int saturation = max - min;
                    
                    double luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0;
                    
                    // Saturation filter to detect neutral colors (slate, gray, black, charcoal)
                    if (saturation < 45) {
                        if (luminance < 0.35) {
                            tv.setTextColor(0xFFF8FAFC); // Deep black/gray -> crisp white
                        } else if (luminance < 0.65) {
                            tv.setTextColor(0xFFCBD5E1); // Medium slate -> light gray
                        }
                    }
                }

                int hintColor = tv.getCurrentHintTextColor();
                if (hintColor != 0 && hintColor != Color.TRANSPARENT) {
                    int r = Color.red(hintColor);
                    int g = Color.green(hintColor);
                    int b = Color.blue(hintColor);
                    double luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0;
                    if (luminance < 0.6) {
                        tv.setHintTextColor(0xFF94A3B8);
                    }
                }
            }

            // Adjust Bottom Navigation Tints
            if (view instanceof BottomNavigationView) {
                BottomNavigationView nav = (BottomNavigationView) view;
                nav.setBackgroundColor(0xFF1E293B);
                nav.setItemIconTintList(ColorStateList.valueOf(0xFF6C63FF));
            }

            // Adjust Custom Toolbar backgrounds
            if (view instanceof Toolbar) {
                view.setBackgroundColor(0xFF1E293B);
            }
        }

        // Traverse layout hierarchy children recursively
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                adjustDarkMode(group.getChildAt(i), isDark);
            }
        }
    }

    private boolean isLightColor(int color) {
        if (color == 0 || color == Color.TRANSPARENT) return false;
        double luminance = (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255.0;
        return luminance > 0.75;
    }

    private boolean isDarkColor(int color) {
        if (color == 0 || color == Color.TRANSPARENT) return false;
        double luminance = (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255.0;
        return luminance < 0.4;
    }

    private void translateView(View view, String langCode) {
        if (view == null) return;

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                translateView(group.getChildAt(i), langCode);
            }
        } else if (view instanceof TextView) {
            TextView tv = (TextView) view;
            // Skip modifying user input field texts directly, only modify static text and Hints
            if (!(view instanceof EditText)) {
                CharSequence text = tv.getText();
                if (text != null && text.length() > 0) {
                    String trans = TranslationHelper.getTranslation(text.toString(), langCode);
                    if (!trans.equals(text.toString())) {
                        tv.setText(trans);
                    }
                }
            }
            CharSequence hint = tv.getHint();
            if (hint != null && hint.length() > 0) {
                String trans = TranslationHelper.getTranslation(hint.toString(), langCode);
                if (!trans.equals(hint.toString())) {
                    tv.setHint(trans);
                }
            }
        }
    }

    private void translateToolbar(View view, String langCode) {
        if (view == null) return;

        if (view instanceof Toolbar) {
            Toolbar toolbar = (Toolbar) view;
            if (toolbar.getTitle() != null) {
                String transTitle = TranslationHelper.getTranslation(toolbar.getTitle().toString(), langCode);
                toolbar.setTitle(transTitle);
            }
        } else if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                translateToolbar(group.getChildAt(i), langCode);
            }
        }
    }

    private void translateBottomNav(View view, String langCode) {
        if (view == null) return;

        if (view instanceof BottomNavigationView) {
            BottomNavigationView nav = (BottomNavigationView) view;
            for (int i = 0; i < nav.getMenu().size(); i++) {
                android.view.MenuItem item = nav.getMenu().getItem(i);
                if (item.getTitle() != null) {
                    String transTitle = TranslationHelper.getTranslation(item.getTitle().toString(), langCode);
                    item.setTitle(transTitle);
                }
            }
        } else if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                translateBottomNav(group.getChildAt(i), langCode);
            }
        }
    }
}

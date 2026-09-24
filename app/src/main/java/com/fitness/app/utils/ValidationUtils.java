package com.fitness.app.utils;

import android.text.TextUtils;
import android.util.Patterns;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

public class ValidationUtils {

    // Strict Email Regex requiring name@domain.tld (alphanumeric domain parts, 2+ letter TLD, no leading/trailing dot domain)
    private static final Pattern STRICT_EMAIL_PATTERN = 
            Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9]+([.-][a-zA-Z0-9]+)*\\.[a-zA-Z]{2,}$");

    // Name Regex (Unicode letters, spaces, hyphens, apostrophes, 2 to 50 chars)
    private static final Pattern NAME_PATTERN = 
            Pattern.compile("^[\\p{L} .'-]{2,50}$");

    public static boolean isEmpty(String text) {
        return TextUtils.isEmpty(text) || text.trim().isEmpty();
    }

    public static boolean isValidName(String name) {
        if (isEmpty(name)) return false;
        String trimmed = name.trim();
        return NAME_PATTERN.matcher(trimmed).matches();
    }

    public static boolean isValidEmail(String email) {
        if (isEmpty(email)) return false;
        String trimmed = email.trim();
        if (trimmed.contains(" ") || trimmed.startsWith(".") || trimmed.endsWith(".")) return false;
        return Patterns.EMAIL_ADDRESS.matcher(trimmed).matches() && STRICT_EMAIL_PATTERN.matcher(trimmed).matches();
    }

    public static boolean isValidPassword(String password) {
        return !isEmpty(password) && password.length() >= 8;
    }

    public static double parseHeight(String heightStr) {
        if (isEmpty(heightStr)) return -1;
        String cleaned = heightStr.trim().toLowerCase().replaceAll("\\s+", "");

        // Feet & Inches format (e.g., 5'7" or 5'7)
        if (cleaned.contains("'")) {
            try {
                String[] parts = cleaned.split("'");
                if (parts.length == 0) return -1;
                double feet = Double.parseDouble(parts[0]);
                double inches = 0;
                if (parts.length > 1) {
                    String inchStr = parts[1].replaceAll("\"", "").replaceAll("in", "");
                    if (!inchStr.isEmpty()) {
                        inches = Double.parseDouble(inchStr);
                    }
                }
                if (feet < 0 || inches < 0) return -1;
                return (feet * 30.48) + (inches * 2.54);
            } catch (Exception e) {
                return -1;
            }
        }

        // CM Format
        cleaned = cleaned.replaceAll("cm", "");
        try {
            double val = Double.parseDouble(cleaned);
            return val > 0 ? val : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static boolean isValidHeight(double heightCm) {
        // Realistic human height range: 50 cm to 250 cm (approx 1'8" to 8'2")
        return heightCm >= 50 && heightCm <= 250;
    }

    public static double parseWeight(String weightStr) {
        if (isEmpty(weightStr)) return -1;
        String cleaned = weightStr.trim().toLowerCase().replaceAll("kg", "").replaceAll("lbs", "").replaceAll("lb", "").replaceAll("\\s+", "");
        try {
            double val = Double.parseDouble(cleaned);
            return val > 0 ? val : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static boolean isValidWeight(double weightKg) {
        // Realistic weight range: 20 kg to 300 kg (approx 44 lbs to 660 lbs)
        return weightKg >= 20 && weightKg <= 300;
    }

    public static int parseAge(String ageStr) {
        if (isEmpty(ageStr)) return -1;
        try {
            int val = Integer.parseInt(ageStr.trim());
            return val > 0 ? val : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static boolean isValidAge(int age) {
        // Realistic human age range for fitness app: 10 to 100 years
        return age >= 10 && age <= 100;
    }

    public static int calculateAge(String dobStr) {
        if (isEmpty(dobStr)) return -1;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date birthDate = sdf.parse(dobStr);
            if (birthDate == null) return -1;

            Calendar birth = Calendar.getInstance();
            birth.setTime(birthDate);

            Calendar today = Calendar.getInstance();

            int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);

            if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }
            return age;
        } catch (ParseException e) {
            return -1;
        }
    }

    public static boolean isValidCountry(String country) {
        if (isEmpty(country)) return false;
        String trimmed = country.trim().toLowerCase();
        return !trimmed.equals("choose country") && !trimmed.equals("select country");
    }

    public static boolean isValidCity(String country, String city) {
        return LocationUtils.isValidCityForCountry(country, city);
    }

    public static boolean isValidMedicalCondition(String condition) {
        if (isEmpty(condition)) return false;
        String trimmed = condition.trim();
        if (trimmed.equalsIgnoreCase("choose medical condition") || trimmed.equalsIgnoreCase("select medical condition")) {
            return false;
        }
        String[] allowed = {
            "None", "Hypertension (High BP)", "Diabetes (Type 1 / Type 2)",
            "Asthma / Respiratory", "Heart Condition", "Joint / Arthritis",
            "Thyroid Disorder", "Other"
        };
        for (String option : allowed) {
            if (option.equalsIgnoreCase(trimmed)) {
                return true;
            }
        }
        return false;
    }
}

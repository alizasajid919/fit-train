package com.fitness.app.utils;

import android.text.TextUtils;
import android.util.Patterns;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ValidationUtils {

    public static boolean isEmpty(String text) {
        return TextUtils.isEmpty(text) || text.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        return !isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        return !isEmpty(password) && password.length() >= 8;
    }

    // Parses height string (e.g. 170, 170cm, 5'7", 5'7) into cm.
    // Returns -1 if formatting is invalid.
    public static double parseHeight(String heightStr) {
        if (isEmpty(heightStr)) return -1;
        String cleaned = heightStr.trim().toLowerCase().replaceAll("\\s+", "");

        // Check feet and inches format (e.g., 5'7" or 5'7)
        if (cleaned.contains("'")) {
            try {
                String[] parts = cleaned.split("'");
                double feet = Double.parseDouble(parts[0]);
                double inches = 0;
                if (parts.length > 1) {
                    String inchStr = parts[1].replaceAll("\"", "").replaceAll("in", "");
                    if (!inchStr.isEmpty()) {
                        inches = Double.parseDouble(inchStr);
                    }
                }
                // 1 foot = 30.48 cm, 1 inch = 2.54 cm
                return (feet * 30.48) + (inches * 2.54);
            } catch (NumberFormatException e) {
                return -1;
            }
        }

        // Clean metric unit suffix
        cleaned = cleaned.replaceAll("cm", "");
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static boolean isValidHeight(double heightCm) {
        // Human height bounds: 50 cm to 250 cm
        return heightCm >= 50 && heightCm <= 250;
    }

    public static boolean isValidWeight(double weightKg) {
        return weightKg >= 20 && weightKg <= 300;
    }

    // Calculates age from Date of Birth string "yyyy-MM-dd"
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

    public static boolean isValidAge(int age) {
        return age >= 10 && age <= 100;
    }
}

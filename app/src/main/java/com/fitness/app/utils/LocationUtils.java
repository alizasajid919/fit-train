package com.fitness.app.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationUtils {

    private static final Map<String, List<String>> COUNTRY_CITIES_MAP = new HashMap<>();

    static {
        // Pakistan Major Cities
        COUNTRY_CITIES_MAP.put("Pakistan", Arrays.asList(
                "Lahore", "Karachi", "Islamabad", "Rawalpindi", "Peshawar",
                "Quetta", "Multan", "Faisalabad", "Sialkot", "Gujranwala",
                "Hyderabad", "Bahawalpur", "Sargodha", "Sukkur", "Abbottabad"
        ));

        // United States Major Cities
        COUNTRY_CITIES_MAP.put("United States", Arrays.asList(
                "New York", "Los Angeles", "Chicago", "Houston", "Phoenix",
                "Philadelphia", "San Antonio", "San Diego", "Dallas", "Austin",
                "San Jose", "San Francisco", "Seattle", "Denver", "Miami"
        ));

        // Canada Major Cities
        COUNTRY_CITIES_MAP.put("Canada", Arrays.asList(
                "Toronto", "Montreal", "Vancouver", "Calgary", "Edmonton",
                "Ottawa", "Winnipeg", "Quebec City", "Hamilton", "Kitchener"
        ));

        // United Kingdom Major Cities
        COUNTRY_CITIES_MAP.put("United Kingdom", Arrays.asList(
                "London", "Birmingham", "Manchester", "Glasgow", "Liverpool",
                "Bristol", "Edinburgh", "Leeds", "Sheffield", "Belfast"
        ));

        // Australia Major Cities
        COUNTRY_CITIES_MAP.put("Australia", Arrays.asList(
                "Sydney", "Melbourne", "Brisbane", "Perth", "Adelaide",
                "Gold Coast", "Canberra", "Newcastle", "Hobart", "Darwin"
        ));

        // Germany Major Cities
        COUNTRY_CITIES_MAP.put("Germany", Arrays.asList(
                "Berlin", "Munich", "Frankfurt", "Hamburg", "Cologne",
                "Stuttgart", "Dusseldorf", "Dortmund", "Essen", "Leipzig"
        ));

        // India Major Cities
        COUNTRY_CITIES_MAP.put("India", Arrays.asList(
                "Mumbai", "Delhi", "Bengaluru", "Hyderabad", "Ahmedabad",
                "Chennai", "Kolkata", "Surat", "Pune", "Jaipur"
        ));
    }

    public static List<String> getCountries() {
        return new ArrayList<>(Arrays.asList(
                "Pakistan", "United States", "Canada", "United Kingdom",
                "Australia", "Germany", "India", "Other"
        ));
    }

    public static List<String> getCitiesForCountry(String country) {
        if (ValidationUtils.isEmpty(country)) {
            return Collections.emptyList();
        }

        for (Map.Entry<String, List<String>> entry : COUNTRY_CITIES_MAP.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(country.trim())) {
                return entry.getValue();
            }
        }

        // Generic fallback list for "Other" or unspecified countries
        return Arrays.asList("Capital City", "Central City", "Metro City", "North District", "South District");
    }

    public static boolean isValidCityForCountry(String country, String city) {
        if (ValidationUtils.isEmpty(country) || ValidationUtils.isEmpty(city)) {
            return false;
        }

        String cleanedCountry = country.trim();
        String cleanedCity = city.trim();

        // Reject obvious placeholders, non-location strings, numbers, or short names
        if (cleanedCity.equalsIgnoreCase("choose city") ||
            cleanedCity.equalsIgnoreCase("select city") ||
            cleanedCity.equalsIgnoreCase("john") ||
            cleanedCity.equalsIgnoreCase("hello") ||
            cleanedCity.equalsIgnoreCase("abc") ||
            cleanedCity.equalsIgnoreCase("xyz") ||
            cleanedCity.matches(".*\\d+.*") ||
            cleanedCity.length() < 3) {
            return false;
        }

        List<String> validCities = getCitiesForCountry(cleanedCountry);
        for (String valid : validCities) {
            if (valid.equalsIgnoreCase(cleanedCity)) {
                return true;
            }
        }

        // If country is "Other", allow valid alphabetic city strings
        if (cleanedCountry.equalsIgnoreCase("Other")) {
            return cleanedCity.matches("^[\\p{L} .'-]{3,40}$");
        }

        // For defined countries (e.g. Pakistan), city MUST match recognized list
        return false;
    }
}

package com.fitness.app.models;

import java.io.Serializable;

public class User implements Serializable {
    private String uid;
    private String firstName;
    private String lastName;
    private String email;
    private String gender;
    private String dob;
    private double height;
    private double weight;
    private String goal;
    private String activityLevel;
    private boolean profileCompleted;
    private String profileImageUrl;
    private long createdAt;
    private long updatedAt;

    private String mobileNumber;
    private int age;
    private String bloodGroup;
    private String country;
    private String city;
    private String fitnessExperience;
    private double targetWeight;
    private int dailyWaterGoal;
    private int dailyCaloriesGoal;
    private int dailyStepGoal;
    private int dailySleepGoal;
    private String medicalConditions;
    private String injuries;
    private String allergies;
    private String dietaryPreference;
    private String emergencyContact;
    private String availableEquipment;

    // Required empty constructor for Firestore serialization
    public User() {
    }

    public User(String uid, String firstName, String lastName, String email, long createdAt) {
        this.uid = uid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
        this.profileCompleted = false;
    }

    // Getters and Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public String getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(String activityLevel) {
        this.activityLevel = activityLevel;
    }

    public boolean isProfileCompleted() {
        return profileCompleted;
    }

    public void setProfileCompleted(boolean profileCompleted) {
        this.profileCompleted = profileCompleted;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getFitnessExperience() { return fitnessExperience; }
    public void setFitnessExperience(String fitnessExperience) { this.fitnessExperience = fitnessExperience; }

    public double getTargetWeight() { return targetWeight; }
    public void setTargetWeight(double targetWeight) { this.targetWeight = targetWeight; }

    public int getDailyWaterGoal() { return dailyWaterGoal; }
    public void setDailyWaterGoal(int dailyWaterGoal) { this.dailyWaterGoal = dailyWaterGoal; }

    public int getDailyCaloriesGoal() { return dailyCaloriesGoal; }
    public void setDailyCaloriesGoal(int dailyCaloriesGoal) { this.dailyCaloriesGoal = dailyCaloriesGoal; }

    public int getDailyStepGoal() { return dailyStepGoal; }
    public void setDailyStepGoal(int dailyStepGoal) { this.dailyStepGoal = dailyStepGoal; }

    public int getDailySleepGoal() { return dailySleepGoal; }
    public void setDailySleepGoal(int dailySleepGoal) { this.dailySleepGoal = dailySleepGoal; }

    public String getMedicalConditions() { return medicalConditions; }
    public void setMedicalConditions(String medicalConditions) { this.medicalConditions = medicalConditions; }

    public String getInjuries() { return injuries; }
    public void setInjuries(String injuries) { this.injuries = injuries; }

    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }

    public String getDietaryPreference() { return dietaryPreference; }
    public void setDietaryPreference(String dietaryPreference) { this.dietaryPreference = dietaryPreference; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }

    private String workoutLocation;
    private int workoutDuration;
    private int workoutDaysPerWeek;
    private String dislikedFoods;
    private int mealsPerDay;
    private String targetPace;
    private String preferredWorkoutTime;

    public String getAvailableEquipment() { return availableEquipment; }
    public void setAvailableEquipment(String availableEquipment) { this.availableEquipment = availableEquipment; }

    public String getWorkoutLocation() { return workoutLocation; }
    public void setWorkoutLocation(String workoutLocation) { this.workoutLocation = workoutLocation; }

    public int getWorkoutDuration() { return workoutDuration; }
    public void setWorkoutDuration(int workoutDuration) { this.workoutDuration = workoutDuration; }

    public int getWorkoutDaysPerWeek() { return workoutDaysPerWeek; }
    public void setWorkoutDaysPerWeek(int workoutDaysPerWeek) { this.workoutDaysPerWeek = workoutDaysPerWeek; }

    public String getDislikedFoods() { return dislikedFoods; }
    public void setDislikedFoods(String dislikedFoods) { this.dislikedFoods = dislikedFoods; }

    public int getMealsPerDay() { return mealsPerDay; }
    public void setMealsPerDay(int mealsPerDay) { this.mealsPerDay = mealsPerDay; }

    public String getTargetPace() { return targetPace; }
    public void setTargetPace(String targetPace) { this.targetPace = targetPace; }

    public String getPreferredWorkoutTime() { return preferredWorkoutTime; }
    public void setPreferredWorkoutTime(String preferredWorkoutTime) { this.preferredWorkoutTime = preferredWorkoutTime; }

    private String eatingEnvironment;
    private String eatingOutFrequency;

    public String getEatingEnvironment() { return eatingEnvironment; }
    public void setEatingEnvironment(String eatingEnvironment) { this.eatingEnvironment = eatingEnvironment; }

    public String getEatingOutFrequency() { return eatingOutFrequency; }
    public void setEatingOutFrequency(String eatingOutFrequency) { this.eatingOutFrequency = eatingOutFrequency; }
}

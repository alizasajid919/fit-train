package com.fitness.app.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.UUID;

@Entity(tableName = "user_challenge_stats")
public class UserChallengeStats implements Serializable {

    @PrimaryKey
    @NonNull
    private String id;
    private int totalXp;
    private String unlockedBadgesJson; // List of badge strings
    private int challengesCompleted;
    private int currentStreak;
    private int maxStreak;
    private int missedDays;
    private String dailyLogsJson; // JSON representation of daily challenge activity logs

    public UserChallengeStats() {
        this.id = UUID.randomUUID().toString();
        this.totalXp = 0;
        this.unlockedBadgesJson = "[]";
        this.challengesCompleted = 0;
        this.currentStreak = 0;
        this.maxStreak = 0;
        this.missedDays = 0;
        this.dailyLogsJson = "[]";
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public int getTotalXp() { return totalXp; }
    public void setTotalXp(int totalXp) { this.totalXp = totalXp; }

    public String getUnlockedBadgesJson() { return unlockedBadgesJson; }
    public void setUnlockedBadgesJson(String unlockedBadgesJson) { this.unlockedBadgesJson = unlockedBadgesJson; }

    public int getChallengesCompleted() { return challengesCompleted; }
    public void setChallengesCompleted(int challengesCompleted) { this.challengesCompleted = challengesCompleted; }

    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }

    public int getMaxStreak() { return maxStreak; }
    public void setMaxStreak(int maxStreak) { this.maxStreak = maxStreak; }

    public int getMissedDays() { return missedDays; }
    public void setMissedDays(int missedDays) { this.missedDays = missedDays; }

    public String getDailyLogsJson() { return dailyLogsJson; }
    public void setDailyLogsJson(String dailyLogsJson) { this.dailyLogsJson = dailyLogsJson; }
}

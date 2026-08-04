package com.fitness.app.data.room;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "daily_steps")
public class DailySteps {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String date;
    public int count;
}

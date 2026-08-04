package com.fitness.app.data.room;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "calories_burned")
public class CaloriesBurned {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String date;
    public int calories;
}

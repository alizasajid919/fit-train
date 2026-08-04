package com.fitness.app.data.room;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "water_intake")
public class WaterIntake {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String date;
    public int amountMl;
}

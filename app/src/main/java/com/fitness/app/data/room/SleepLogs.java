package com.fitness.app.data.room;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sleep_logs")
public class SleepLogs {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String date;
    public int minutes;
}

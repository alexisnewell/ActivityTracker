package com.example.activitytracker.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "daily_steps")
public class DailyStepsEntity {

    @PrimaryKey
    @NonNull
    public String date; // "2026-05-15"

    public int steps;
}
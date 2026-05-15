package com.example.activitytracker.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DailyStepsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DailyStepsEntity entity);

    @Query("SELECT * FROM daily_steps ORDER BY date DESC")
    List<DailyStepsEntity> getAll();

    @Query("SELECT * FROM daily_steps WHERE date = :date")
    DailyStepsEntity getByDate(String date);

    @Query("SELECT AVG(steps) FROM daily_steps")
    float getAverageSteps();

    @Query("SELECT MAX(steps) FROM daily_steps")
    int getMaxSteps();
}
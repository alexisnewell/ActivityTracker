package com.example.activitytracker.analytics;

import com.example.activitytracker.data.DailyStepsDao;

public class StepAnalyticsManager {

    private final DailyStepsDao dao;

    public StepAnalyticsManager(DailyStepsDao dao) {
        this.dao = dao;
    }

    public float getAverageSteps() {
        return dao.getAverageSteps();
    }

    public int getMaxSteps() {
        return dao.getMaxSteps();
    }
}

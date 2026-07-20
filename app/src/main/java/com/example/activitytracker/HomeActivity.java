package com.example.activitytracker;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.activitytracker.workouts.WorkoutActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

/**
 * HomeActivity
 *
 * Landing screen for the app. Lets the user jump into
 * Steps tracking or Workouts tracking.
 */
public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        MaterialCardView cardSteps = findViewById(R.id.cardSteps);
        MaterialCardView cardWorkouts = findViewById(R.id.cardWorkouts);

        cardSteps.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, MainActivity.class)));

        cardWorkouts.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, WorkoutActivity.class)));

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        // Highlight current tab
        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                return true;
            } else if (item.getItemId() == R.id.nav_steps) {
                startActivity(new Intent(HomeActivity.this, MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (item.getItemId() == R.id.nav_workouts) {
                startActivity(new Intent(HomeActivity.this, WorkoutActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }
}
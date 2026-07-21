package com.example.activitytracker.workouts;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.activitytracker.R;

import java.util.ArrayList;
import java.util.List;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

public class WorkoutActivity extends AppCompatActivity {

    private List<Workout> workoutList;
    private WorkoutAdapter adapter;

    private EditText inputName, inputSets, inputReps, inputWeight;
    private Button addButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);

        // Initialize list
        SharedPreferences prefs = getSharedPreferences("workouts", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString("workout_list", null);

        Type type = new TypeToken<ArrayList<Workout>>() {}.getType();

        if (json != null) {
            workoutList = gson.fromJson(json, type);
        } else {
            workoutList = new ArrayList<>();
        }

        // RecyclerView setup
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WorkoutAdapter(workoutList, this);
        recyclerView.setAdapter(adapter);

        // Inputs
        inputName = findViewById(R.id.inputName);
        inputSets = findViewById(R.id.inputSets);
        inputReps = findViewById(R.id.inputReps);
        inputWeight = findViewById(R.id.inputWeight);
        addButton = findViewById(R.id.addWorkoutButton);

        // Button click
        addButton.setOnClickListener(v -> {

            String name = inputName.getText().toString().trim();
            String setsText = inputSets.getText().toString().trim();
            String repsText = inputReps.getText().toString().trim();
            String weightText = inputWeight.getText().toString().trim();

            // Check for empty fields
            if (name.isEmpty()) {
                inputName.setError("Exercise name is required");
                inputName.requestFocus();
                return;
            }

            if (setsText.isEmpty()) {
                inputSets.setError("Enter number of sets");
                inputSets.requestFocus();
                return;
            }

            if (repsText.isEmpty()) {
                inputReps.setError("Enter number of reps");
                inputReps.requestFocus();
                return;
            }

            if (weightText.isEmpty()) {
                inputWeight.setError("Enter weight");
                inputWeight.requestFocus();
                return;
            }

            try {

                int sets = Integer.parseInt(setsText);
                int reps = Integer.parseInt(repsText);
                float weight = Float.parseFloat(weightText);

                // Validate values
                if (sets <= 0) {
                    inputSets.setError("Sets must be greater than 0");
                    inputSets.requestFocus();
                    return;
                }

                if (reps <= 0) {
                    inputReps.setError("Reps must be greater than 0");
                    inputReps.requestFocus();
                    return;
                }

                if (weight < 0) {
                    inputWeight.setError("Weight cannot be negative");
                    inputWeight.requestFocus();
                    return;
                }

                Workout workout = new Workout(name, sets, reps, weight);
                workoutList.add(workout);

                adapter.notifyItemInserted(workoutList.size() - 1);

                // Clear fields
                inputName.setText("");
                inputSets.setText("");
                inputReps.setText("");
                inputWeight.setText("");

            } catch (NumberFormatException e) {
                Toast.makeText(
                        WorkoutActivity.this,
                        "Please enter valid numbers.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });



        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        // Highlight current tab
        bottomNav.setSelectedItemId(R.id.nav_workouts);

        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                startActivity(new Intent(WorkoutActivity.this, com.example.activitytracker.HomeActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (item.getItemId() == R.id.nav_steps) {
                startActivity(new Intent(WorkoutActivity.this, com.example.activitytracker.MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (item.getItemId() == R.id.nav_workouts) {
                return true;
            }
            return false;
        });
    }
    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences prefs = getSharedPreferences("workouts", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new Gson();
        String json = gson.toJson(workoutList);

        editor.putString("workout_list", json);
        editor.apply();
    }
}
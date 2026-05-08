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
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = inputName.getText().toString();
                int sets = Integer.parseInt(inputSets.getText().toString());
                int reps = Integer.parseInt(inputReps.getText().toString());
                float weight = Float.parseFloat(inputWeight.getText().toString());

                Workout workout = new Workout(name, sets, reps, weight);
                workoutList.add(workout);

                adapter.notifyDataSetChanged();

                // Clear inputs
                inputName.setText("");
                inputSets.setText("");
                inputReps.setText("");
                inputWeight.setText("");
            }
        });



        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

// Highlight current tab
        bottomNav.setSelectedItemId(R.id.nav_workouts);

        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_steps) {
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
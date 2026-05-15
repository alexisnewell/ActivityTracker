package com.example.activitytracker;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.activitytracker.analytics.StepHistoryActivity;
import com.example.activitytracker.data.AppDatabase;
import com.example.activitytracker.data.DailyStepsEntity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.content.Intent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;


/**
 * MainActivity
 *
 * Loads the native library, starts the sensor pipeline via JNI,
 * then polls C++ state every 200 ms and updates the UI.
 *
 * All heavy sensor work lives in C++.  Java is the display layer.
 */
public class MainActivity extends AppCompatActivity {

    //Load native library
    static { System.loadLibrary("activitytracker"); }

    //JNI declarations (implemented in sensor_bridge.cpp)
    private native boolean nativeInit();
    private native void    nativeShutdown();
    private native int     nativeGetSteps();
    private native void    nativeResetSteps();
    private native float   nativeGetPitch();
    private native float   nativeGetRoll();
    private native int     nativeGetCarryMode();   // 0=HAND 1=POCKET 2=BAG 3=UNKNOWN

    // UI refs
    private TextView tvSteps, tvPitch, tvRoll, tvCarry;
    private Button   btnReset;

    // Poll loop
    private final Handler   uiHandler   = new Handler(Looper.getMainLooper());
    private static final int POLL_MS    = 200;

    private final Runnable pollRunnable = new Runnable() {
        @Override public void run() {
            updateUI();
            uiHandler.postDelayed(this, POLL_MS);
        }
    };

    // Lifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvSteps = findViewById(R.id.tv_steps);
        tvPitch = findViewById(R.id.tv_pitch);
        tvRoll  = findViewById(R.id.tv_roll);
        tvCarry = findViewById(R.id.tv_carry);
        btnReset = findViewById(R.id.btn_reset);

        btnReset.setOnClickListener(v -> nativeResetSteps());

        boolean ok = nativeInit();
        if (!ok) {
            tvSteps.setText("Sensor init failed!");
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

// Highlight current tab
        bottomNav.setSelectedItemId(R.id.nav_steps);

        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_steps) {
                return true;
            } else if (item.getItemId() == R.id.nav_workouts) {
                startActivity(new Intent(MainActivity.this, com.example.activitytracker.workouts.WorkoutActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
        ImageButton historyButton = findViewById(R.id.historyButton);
        historyButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StepHistoryActivity.class);
            startActivity(intent);
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        uiHandler.post(pollRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        uiHandler.removeCallbacks(pollRunnable);
        Executors.newSingleThreadExecutor().execute(() -> {
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(new Date());
            DailyStepsEntity entity = new DailyStepsEntity();
            entity.date = today;
            entity.steps = nativeGetSteps();
            AppDatabase.getInstance(getApplicationContext())
                    .dailyStepsDao()
                    .insert(entity);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        nativeShutdown();
    }

    // UI update

    private static final String[] CARRY_LABELS = { "Hand 🖐", "Pocket 👖", "Bag 🎒", "Unknown ❓" };

    private void updateUI() {
        int steps = nativeGetSteps();
        float pitch = nativeGetPitch();
        float roll  = nativeGetRoll();
        int   carry = nativeGetCarryMode();

        tvSteps.setText(String.valueOf(steps));
        tvPitch.setText(String.format("%.1f°", pitch));
        tvRoll.setText(String.format("%.1f°", roll));
        tvCarry.setText(carry >= 0 && carry < CARRY_LABELS.length
                ? CARRY_LABELS[carry] : "Unknown");
    }
}

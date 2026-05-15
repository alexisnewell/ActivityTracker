package com.example.activitytracker.analytics;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.activitytracker.data.AppDatabase;
import com.example.activitytracker.data.DailyStepsEntity;
import com.example.activitytracker.R;

import java.util.List;
import java.util.concurrent.Executors;

public class StepHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private StepHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_history);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerViewSteps);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new StepHistoryAdapter();
        recyclerView.setAdapter(adapter);

        loadData();
    }

    private void loadData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                List<DailyStepsEntity> data = db.dailyStepsDao().getAll();

                runOnUiThread(() -> {
                    if (adapter != null) {
                        adapter.setData(data);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
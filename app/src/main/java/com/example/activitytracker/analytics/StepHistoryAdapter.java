

package com.example.activitytracker.analytics;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.activitytracker.R;
import com.example.activitytracker.data.DailyStepsEntity;

import java.util.ArrayList;
import java.util.List;

public class StepHistoryAdapter extends RecyclerView.Adapter<StepHistoryAdapter.ViewHolder> {

    private List<DailyStepsEntity> data = new ArrayList<>();

    public void setData(List<DailyStepsEntity> newData) {
        this.data = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_step_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DailyStepsEntity item = data.get(position);

        holder.date.setText(item.date);
        holder.steps.setText(String.valueOf(item.steps));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView date, steps;

        ViewHolder(View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.textDate);
            steps = itemView.findViewById(R.id.textSteps);
        }
    }
}
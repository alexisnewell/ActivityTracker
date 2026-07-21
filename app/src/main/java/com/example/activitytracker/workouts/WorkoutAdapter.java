package com.example.activitytracker.workouts;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.activitytracker.R;

import java.util.List;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {

    private List<Workout> workoutList;
    private Context context;

    public WorkoutAdapter(List<Workout> workoutList, Context context) {
        this.workoutList = workoutList;
        this.context = context;
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout, parent, false);

        return new WorkoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {

        Workout workout = workoutList.get(position);

        holder.exerciseName.setText(workout.getExerciseName());

        holder.details.setText(
                "Sets: " + workout.getSets() +
                        " | Reps: " + workout.getReps() +
                        " | Weight: " + workout.getWeight() + " lbs"
        );

        // DELETE
        holder.deleteButton.setOnClickListener(v -> {

            new AlertDialog.Builder(context)
                    .setTitle("Delete Workout")
                    .setMessage("Are you sure you want to delete this workout?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Delete", (dialog, which) -> {

                        workoutList.remove(position);
                        notifyItemRemoved(position);

                        if (context instanceof WorkoutActivity) {
                            ((WorkoutActivity) context).updateEmptyState();
                        }

                    })
                    .show();

        });
        // EDIT
        holder.editButton.setOnClickListener(v -> {

            View dialogView = LayoutInflater.from(context)
                    .inflate(R.layout.dialog_edit_workout, null);

            EditText nameInput = dialogView.findViewById(R.id.editName);
            EditText setsInput = dialogView.findViewById(R.id.editSets);
            EditText repsInput = dialogView.findViewById(R.id.editReps);
            EditText weightInput = dialogView.findViewById(R.id.editWeight);

            nameInput.setText(workout.getExerciseName());
            setsInput.setText(String.valueOf(workout.getSets()));
            repsInput.setText(String.valueOf(workout.getReps()));
            weightInput.setText(String.valueOf(workout.getWeight()));

            new AlertDialog.Builder(context)
                    .setTitle("Edit Workout")
                    .setView(dialogView)
                    .setPositiveButton("Save", (dialog, which) -> {

                        workout.setExerciseName(nameInput.getText().toString());
                        workout.setSets(Integer.parseInt(setsInput.getText().toString()));
                        workout.setReps(Integer.parseInt(repsInput.getText().toString()));
                        workout.setWeight(Float.parseFloat(weightInput.getText().toString()));

                        notifyItemChanged(position);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return workoutList.size();
    }

    static class WorkoutViewHolder extends RecyclerView.ViewHolder {

        TextView exerciseName, details;
        Button editButton, deleteButton;

        public WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);

            exerciseName = itemView.findViewById(R.id.exerciseName);
            details = itemView.findViewById(R.id.exerciseDetails);

            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
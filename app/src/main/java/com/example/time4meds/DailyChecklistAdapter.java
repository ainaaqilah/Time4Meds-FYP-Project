package com.example.time4meds;

// DailyChecklistAdapter: Adapter for displaying and managing the daily medication checklist

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class DailyChecklistAdapter extends RecyclerView.Adapter<DailyChecklistAdapter.ViewHolder> {

    private Context context;
    private List<Reminder> reminders;
    private String elderlyId;
    private String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();

    public DailyChecklistAdapter(Context context, List<Reminder> reminders, String elderlyId) {
        this.context = context;
        this.reminders = reminders;
        this.elderlyId = elderlyId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_daily_checklist, parent, false);
        return new ViewHolder(view);
    }

    // binds data to each item in the RecyclerView
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reminder reminder = reminders.get(position);

        holder.tvMedName.setText(reminder.getMedicationName());
        holder.tvDosage.setText(reminder.getDosage());
        holder.tvTime.setText(reminder.getTime());

        // if medication already taken
        if (reminder.isTaken()) { // set green status indicator
            holder.viewStatus.setBackgroundResource(R.drawable.status_circle_green);
            holder.btnTaken.setText("Taken"); // update button text
            holder.btnTaken.setEnabled(false); // disable button (cannot click again)
        }
        else if (reminder.isOverdue()) { // if reminder is overdue but not yet taken
            holder.viewStatus.setBackgroundResource(R.drawable.status_circle_red); // set red status indicator
            holder.btnTaken.setText("Mark Taken"); // allow marking as taken
            holder.btnTaken.setEnabled(true);
        }
        else { // upcoming reminder (not overdue, not taken)
            holder.viewStatus.setBackgroundResource(R.drawable.status_circle_gray); // set gray status indicator
            holder.btnTaken.setText("Mark Taken"); // allow marking as taken
            holder.btnTaken.setEnabled(true);
        }

        // =========================
        // ON CLICK - MARK TAKEN
        // =========================
        holder.btnTaken.setOnClickListener(v -> {
            // do nothing if already taken
            if (reminder.isTaken()) return;

            DatabaseReference dailyChecklistRef = FirebaseDatabase.getInstance()
                    .getReference("elderly")
                    .child(userId)
                    .child(elderlyId)
                    .child("dailyChecklist")
                    .child(Utils.getTodayDate())
                    .child(reminder.getId());

            // update reminder status locally
            reminder.setTaken(true);
            reminder.setOverdue(false);
            dailyChecklistRef.setValue(reminder); // save reminder into dailyChecklist history

            DatabaseReference reminderRef = FirebaseDatabase.getInstance()
                    .getReference("elderly")
                    .child(userId)
                    .child(elderlyId)
                    .child("reminders")
                    .child(reminder.getId());

            // update "taken" status in reminders table
            reminderRef.child("taken").setValue(true);

            // update UI after marking taken button
            holder.btnTaken.setEnabled(false);
            holder.btnTaken.setText("Taken");
            holder.viewStatus.setBackgroundResource(R.drawable.status_circle_green);
        });
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMedName, tvDosage, tvTime;
        Button btnTaken;
        View viewStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMedName = itemView.findViewById(R.id.tvMedName);
            tvDosage = itemView.findViewById(R.id.tvDosage);
            tvTime = itemView.findViewById(R.id.tvTime);
            btnTaken = itemView.findViewById(R.id.btnTaken); // link button
            viewStatus = itemView.findViewById(R.id.viewStatus); // link status indicator
        }
    }
}

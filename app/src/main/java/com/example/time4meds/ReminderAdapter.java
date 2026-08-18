package com.example.time4meds;

// ReminderAdapter: RecyclerView Adapter for displaying a list of medication reminders
// supports filtering/updating the list via updateList() method.

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {

    private List<Reminder> reminders; // list of reminders to display
    private Context context;
    private String elderlyId;

    public ReminderAdapter(List<Reminder> reminders, Context context, String elderlyId) {
        this.reminders = reminders;
        this.context = context;
        this.elderlyId = elderlyId;
    }

    // for filtering method to update the list
    public void updateList(List<Reminder> newList) {
        this.reminders = newList; // replace current list with new filtered list
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate item layout (item_reminder.xml)
        View view = LayoutInflater.from(context).inflate(R.layout.item_reminder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reminder reminder = reminders.get(position);

        List<Long> occurrences = DateUtils.generateReminderOccurrences(reminder); // generate all reminder timestamps
        long now = System.currentTimeMillis(); // current time
        long nextTime = -1; // placeholder for next upcoming occurrence

        // find the first upcoming occurrence
        for (long t : occurrences) {
            if (t >= now) {
                nextTime = t;
                break; // first upcoming occurrence
            }
        }

        // set medication name and dosage text
        holder.tvMedName.setText(reminder.getMedicationName());
        holder.tvDosage.setText(reminder.getDosage());

        if (nextTime != -1) {
            // show next occurrence time with AM/PM
            holder.tvTime.setText(android.text.format.DateFormat.format("hh:mm a", nextTime));
        } else {
            // fallback: show original reminder time, but formatted to AM/PM
            try {
                SimpleDateFormat sdf24 = new SimpleDateFormat("HH:mm", Locale.getDefault());
                SimpleDateFormat sdf12 = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                holder.tvTime.setText(sdf12.format(sdf24.parse(reminder.getTime())));
            } catch (Exception e) {
                // fallback just in case parsing fails
                holder.tvTime.setText(reminder.getTime());
            }
        }

        try {
            // parse reminder start and end dates and format it for display (dd.MM.yyyy)
            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()); // style saved in the db
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()); // style saved in the UI

            String formattedStart = displayFormat.format(dbFormat.parse(reminder.getStartDate()));
            String formattedEnd = displayFormat.format(dbFormat.parse(reminder.getEndDate()));

            holder.tvDateRange.setText(formattedStart + " → " + formattedEnd);
        } catch (Exception e) {
            e.printStackTrace();
            // fallback: display raw date strings if parsing fails
            holder.tvDateRange.setText(reminder.getStartDate() + " → " + reminder.getEndDate());
        }


        // clicking reminder opens AddReminderActivity in edit mode
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddReminderActivity.class);
            intent.putExtra("elderlyId", elderlyId);
            intent.putExtra("reminderId", reminder.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return reminders.size(); } // return number of reminders in the list

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMedName, tvDosage, tvTime, tvDateRange;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMedName = itemView.findViewById(R.id.tvMedName);
            tvDosage = itemView.findViewById(R.id.tvDosage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvDateRange = itemView.findViewById(R.id.tvDateRange);
        }
    }
}

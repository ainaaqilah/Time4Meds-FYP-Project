package com.example.time4meds;

// DailyChecklistAdapterForHistory: Adapter for displaying historical daily checklist items

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DailyChecklistAdapterForHistory extends RecyclerView.Adapter<DailyChecklistAdapterForHistory.ViewHolder> {

    private final Context context;
    private final List<ChecklistItem> checklistItems = new ArrayList<>();

    public DailyChecklistAdapterForHistory(Context context, List<ChecklistItem> checklistItems) {
        this.context = context;
        if (checklistItems != null) this.checklistItems.addAll(checklistItems);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_daily_checklist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChecklistItem item = checklistItems.get(position);

        holder.tvMedName.setText(item.getMedName() != null ? item.getMedName() : "Unknown");
        holder.tvDosage.setText(item.getDosage() != null ? item.getDosage() : "-");

        // show full timestamp if time exists
        String displayTime = item.getDate();
        if (item.getTime() != null && !item.getTime().isEmpty()) {
            displayTime += " " + item.getTime();
        }
        holder.tvTime.setText(displayTime != null ? displayTime : "-");

        // set status UI based on whether medicine was taken
        if (item.isTaken()) {
            holder.viewStatus.setBackgroundResource(R.drawable.status_circle_green); // green indicator
            holder.tvTaken.setText("Taken"); // show Taken
            holder.tvMedName.setTextColor(Color.BLACK); // normal text colour
        } else {
            holder.viewStatus.setBackgroundResource(R.drawable.status_circle_gray); // grey indicator
            holder.tvTaken.setText("Not Taken"); // show Not Taken
            // if overdue, show red text
            if (item.isOverdue()) {
                holder.tvMedName.setTextColor(Color.RED); // overdue text
            } else {
                holder.tvMedName.setTextColor(Color.BLACK); // normal text
            }
        }
    }

    @Override
    public int getItemCount() {
        return checklistItems.size();
    }

    public void updateData(List<ChecklistItem> newList) {
        if (newList == null) return;
        checklistItems.clear();
        checklistItems.addAll(newList);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMedName, tvDosage, tvTime, tvTaken;
        View viewStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMedName = itemView.findViewById(R.id.tvMedName);
            tvDosage = itemView.findViewById(R.id.tvDosage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvTaken = itemView.findViewById(R.id.btnTaken); // reuse button as text
            viewStatus = itemView.findViewById(R.id.viewStatus);
        }
    }
}

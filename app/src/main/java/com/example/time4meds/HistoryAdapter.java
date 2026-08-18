package com.example.time4meds;

// HistoryAdapter: Adapter for displaying historical daily checklist items

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private final List<ChecklistItem> historyList;

    public HistoryAdapter(List<ChecklistItem> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        ChecklistItem item = historyList.get(position);
        if (item == null) return; // safety check

        holder.tvMedName.setText(item.getMedName() != null ? item.getMedName() : "Unknown");
        holder.tvDosage.setText(item.getDosage() != null ? item.getDosage() : "-");
        holder.tvDate.setText(item.getDate() != null ? item.getDate() : "Unknown");

        // store taken status for readability
        boolean taken = item.isTaken(); // primitive boolean, safe
        holder.ivTaken.setImageResource(taken ? R.drawable.ic_check : R.drawable.ic_close);

        // change text color of medication name based on taken status
        holder.tvMedName.setTextColor(taken ?
                holder.itemView.getResources().getColor(android.R.color.black) :
                holder.itemView.getResources().getColor(android.R.color.holo_red_dark));
    }


    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvMedName, tvDosage, tvDate;
        ImageView ivTaken;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMedName = itemView.findViewById(R.id.tvMedName);
            tvDosage = itemView.findViewById(R.id.tvDosage);
            tvDate = itemView.findViewById(R.id.tvDate);
            ivTaken = itemView.findViewById(R.id.ivTaken);
        }
    }

    public void updateList(List<ChecklistItem> newList) {
        historyList.clear();
        historyList.addAll(newList);
        notifyDataSetChanged();
    }
}

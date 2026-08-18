package com.example.time4meds;

// MedicationAdapter: Connects a list of Medication objects to a RecyclerView
// displays each medication’s name, dosage, frequency, and provides buttons for viewing details or deleting the medication
// Handles click events via an OnItemClickListener interface to delegate actions to the parent activity

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button; //new
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.MedViewHolder> {

    private final List<Medication> medicationList;
    private final Context context;
    private final OnItemClickListener listener;

    public interface OnItemClickListener { // to trigger events
        void onViewClick(Medication medication);
        void onDeleteClick(Medication medication, int position);
    }

    public MedicationAdapter(List<Medication> medicationList, Context context, OnItemClickListener listener) { // to create adapter
        this.medicationList = medicationList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { // to create and returns a ViewHolder object
        View view = LayoutInflater.from(context).inflate(R.layout.item_medication, parent, false); // turn XML into a View object
        return new MedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedViewHolder holder, int position) { // to fill this row with the correct data
        Medication med = medicationList.get(position); // get data object

        // set text to TextView
        holder.tvName.setText(med.getName());
        holder.tvDoseFreq.setText("Dosage: " + med.getDosage());
        holder.tvFrequency.setText("Frequency: " + med.getFrequency());

        // handle button click listeners
        holder.btnViewMed.setOnClickListener(v -> listener.onViewClick(med));
        holder.btnDeleteMed.setOnClickListener(v -> listener.onDeleteClick(med, position));
    }

    @Override
    public int getItemCount() { return medicationList.size(); } // tell RecyclerView how many items to display

    static class MedViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDoseFreq, tvFrequency;
        Button btnViewMed, btnDeleteMed;

        public MedViewHolder(@NonNull View itemView) {
            super(itemView);
            // link all components with the UI elements
            tvName = itemView.findViewById(R.id.tvMedName);
            tvDoseFreq = itemView.findViewById(R.id.tvMedDoseFreq);
            tvFrequency = itemView.findViewById(R.id.tvFrequency);
            btnViewMed = itemView.findViewById(R.id.btnViewMed);
            btnDeleteMed = itemView.findViewById(R.id.btnDeleteMed);
        }
    }

}



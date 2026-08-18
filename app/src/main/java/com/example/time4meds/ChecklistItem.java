package com.example.time4meds;

// ChecklistItem: represents a single medication record in the daily checklist or history

import com.google.firebase.database.PropertyName;

public class ChecklistItem {
    private String medName;
    private String dosage;
    private boolean taken;
    private String date; // for history filtering
    private String time;
    private boolean overdue;

    public ChecklistItem() { }

    public ChecklistItem(String medName, String dosage, boolean taken, String date) {
        this.medName = medName;
        this.dosage = dosage;
        this.taken = taken;
        this.date = date;
        this.time = "";
        this.overdue = false;
    }

    // Getters
    @PropertyName("medName")
    public String getMedName() { return medName; }
    public String getDosage() { return dosage; }
    public boolean isTaken() { return taken; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public boolean isOverdue() { return overdue; }

    // Setters
    @PropertyName("medName")
    public void setMedName(String medName) { this.medName = medName; }
    @PropertyName("medicationName") // maps Firebase "medicationName" to medName
    public void setMedicationName(String medicationName) { this.medName = medicationName; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public void setTaken(boolean taken) { this.taken = taken; }
    public void setDate(String date) { this.date = date; }
    public void setTime(String time) { this.time = time; }
    public void setOverdue(boolean overdue) { this.overdue = overdue; }
}

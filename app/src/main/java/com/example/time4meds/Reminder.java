package com.example.time4meds;

// Reminder class: represents a single medication reminder for an elderly user
// tracks whether the dose has been taken (isTaken) and if it is overdue (overdue).

public class Reminder {
    private String id;
    private String medicationName;
    private String dosage;
    private String time;
    private String startDate;
    private String endDate;
    private boolean isTaken;
    private boolean overdue = false;  // to track overdue status

    public Reminder() { }

    public Reminder(String medicationName, String dosage, String time, String startDate, String endDate) {
        this.medicationName = medicationName;
        this.dosage = dosage;
        this.time = time;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isTaken = false; // taken by default is FALSE
    }

    // accessor (getter) and mutator (setter) methods
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMedicationName() { return medicationName; }
    public void setMedicationName(String medicationName) { this.medicationName = medicationName; }
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public boolean isTaken() { return isTaken; }
    public void setTaken(boolean taken) { isTaken = taken; }
    public boolean isOverdue() { return overdue; }
    public void setOverdue(boolean overdue) { this.overdue = overdue; }
}

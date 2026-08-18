package com.example.time4meds;

// Medication: Data model representing a single medication entry for an elderly profile
// Stores medication details such as name, dosage, frequency, form, notes, quantity, and the next scheduled dose timestamp

public class Medication {

    private String id;
    private String name;
    private String dosage;
    private String frequency;
    private String form; // "Tablet", "Capsule", "Syrup", "Injection", "Cream"
    private String notes;
    private int quantity;
    private long nextDoseTime; // store as timestamp in milliseconds

    // default constructor
    public Medication() {
    }

    // accessor (getter) and mutator (setter) method
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDosage() {
        return dosage;
    }
    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getFrequency() {
        return frequency;
    }
    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getForm() {
        return form;
    }
    public void setForm(String form) {
        this.form = form;
    }

    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public long getNextDoseTime() { return nextDoseTime; }
    public void setNextDoseTime(long nextDoseTime) { this.nextDoseTime = nextDoseTime; }
}

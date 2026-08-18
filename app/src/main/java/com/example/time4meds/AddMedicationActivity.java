package com.example.time4meds;

// AddMedicationActivity: Activity to add a new medication for a selected elderly profile
// allows input for name, dosage, frequency, form, quantity, and notes, validates required fields
// generates a unique ID, and saves the medication to Firebase Realtime Database under the corresponding elderly profile

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;


public class AddMedicationActivity extends AppCompatActivity {

    private EditText etName, etDosage, etFrequency, etNotes, etQuantity;
    private Spinner spinnerForm;
    private Button btnSave;
    private Button btnCancel;
    private String elderlyId;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);

        // link all the components to the UI elements
        etName = findViewById(R.id.etName);
        etDosage = findViewById(R.id.etDosage);
        etFrequency = findViewById(R.id.etFrequency);
        etNotes = findViewById(R.id.etNotes);
        etQuantity = findViewById(R.id.etQuantity);
        spinnerForm = findViewById(R.id.spinnerForm);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        // get elderlyId from previous activity
        elderlyId = getIntent().getStringExtra("elderlyId");

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // setup spinner for pill form types
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.medication_forms, // e.g., "Tablet", "Capsule", "Syrup", "Injection", "Cream"
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // set layout style for dropdown list
        spinnerForm.setAdapter(adapter); // attach adapter to spinner

        // click save -> save meds
        btnSave.setOnClickListener(v -> saveMedication());

        // click cancel -> go to add meds back
        btnCancel.setOnClickListener(v -> finish());
    }

    private void saveMedication() { // to save medication
        String name = etName.getText().toString().trim();
        String dosage = etDosage.getText().toString().trim();
        String frequency = etFrequency.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();
        String quantityStr = etQuantity.getText().toString().trim();
        String form = spinnerForm.getSelectedItem().toString();

        // to validate required fields
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(dosage) || TextUtils.isEmpty(frequency)) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity = 0;
        if (!TextUtils.isEmpty(quantityStr)) {
            try {
                quantity = Integer.parseInt(quantityStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid quantity", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // get Firebase reference for medications
        DatabaseReference medsRef = FirebaseDatabase.getInstance()
                .getReference("elderly")
                .child(userId)
                .child(elderlyId)
                .child("medications");


        // generate a unique medication ID
        String medId = medsRef.push().getKey();
        if (medId == null) {
            Toast.makeText(this, "Error generating ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // create medication object
        Medication medication = new Medication();
        medication.setId(medId);
        medication.setName(name);
        medication.setDosage(dosage);
        medication.setFrequency(frequency);
        medication.setForm(form);
        medication.setNotes(notes);
        medication.setQuantity(quantity);

        medsRef.child(medId).setValue(medication)
                .addOnSuccessListener(aVoid -> { // if success
                    Toast.makeText(this, "Medication added", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> { // if not
                    Toast.makeText(this, "Failed to add medication", Toast.LENGTH_SHORT).show();
                });
    }
}

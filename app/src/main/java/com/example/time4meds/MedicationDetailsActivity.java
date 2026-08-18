package com.example.time4meds;

// MedicationDetailsActivity: Displays and allows editing of a single medication record
// loads medication data from Firebase using elderlyId and medId
// validates user input and updates the medication record back to Firebase

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MedicationDetailsActivity extends AppCompatActivity {

    private EditText etName, etDosage, etFrequency, etNotes, etQuantity;
    private Spinner spinnerForm;
    private Button btnUpdate;
    private Button btnCancel;
    private String elderlyId;
    private String medId;
    private String userId;
    private DatabaseReference medRef; // points to medication data in Firebase

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication_details);

        // link all the components to the UI elements
        etName = findViewById(R.id.etName);
        etDosage = findViewById(R.id.etDosage);
        etFrequency = findViewById(R.id.etFrequency);
        etNotes = findViewById(R.id.etNotes);
        etQuantity = findViewById(R.id.etQuantity);
        spinnerForm = findViewById(R.id.spinnerForm);
        btnUpdate = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        // get/retrieve IDs via intent
        elderlyId = getIntent().getStringExtra("elderlyId");
        medId = getIntent().getStringExtra("medId");

        // get the current logged-in user’s unique ID
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (elderlyId == null || medId == null) { // ensure required IDs exist before continuing
            Toast.makeText(this, "Invalid medication info", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Firebase reference
        medRef = FirebaseDatabase.getInstance()
                .getReference("elderly")
                .child(userId)
                .child(elderlyId)
                .child("medications")
                .child(medId);


        // setup spinner for form types
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.medication_forms,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerForm.setAdapter(adapter);

        // load medication details
        loadMedication();

        // update changes to medication info
        btnUpdate.setOnClickListener(v -> updateMedication());

        // go back previous page
        btnCancel.setOnClickListener(v -> finish());
    }

    // loads a single medication record from Firebase Realtime Database
    private void loadMedication() {
        medRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Medication med = snapshot.getValue(Medication.class);
                if (med == null) {
                    Toast.makeText(MedicationDetailsActivity.this, "Medication not found", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                // puts the medication data into the corresponding EditText fields
                etName.setText(med.getName());
                etDosage.setText(med.getDosage());
                etFrequency.setText(med.getFrequency());
                etNotes.setText(med.getNotes());
                etQuantity.setText(String.valueOf(med.getQuantity()));

                // sets the spinner to display the current value
                String form = med.getForm();
                if (form != null) {
                    int spinnerPosition = ((ArrayAdapter<String>) spinnerForm.getAdapter()).getPosition(form);
                    spinnerForm.setSelection(spinnerPosition);
                }
            }

            // is triggered if Firebase fails to fetch data
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MedicationDetailsActivity.this, "Failed to load medication", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // collects and validates medication data from the UI before updating Firebase
    private void updateMedication() {
        // reads the text from EditText field
        // removes extra spaces at start/end
        // spinner selection is converted to String
        String name = etName.getText().toString().trim();
        String dosage = etDosage.getText().toString().trim();
        String frequency = etFrequency.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();
        String quantityStr = etQuantity.getText().toString().trim();
        String form = spinnerForm.getSelectedItem().toString();

        // checks if required fields are empty
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(dosage) || TextUtils.isEmpty(frequency)) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // if quantity field not null, try to turn it into a number. If not a valid number, show a warning and stop. If empty, just use 0
        int quantity = 0;
        if (!TextUtils.isEmpty(quantityStr)) {
            try {
                quantity = Integer.parseInt(quantityStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid quantity", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Update Firebase
        Medication updatedMed = new Medication(); // saves the updated medication info to Firebase

        // creates a new Medication object to hold the updated info
        updatedMed.setId(medId);
        updatedMed.setName(name);
        updatedMed.setDosage(dosage);
        updatedMed.setFrequency(frequency);
        updatedMed.setNotes(notes);
        updatedMed.setForm(form);
        updatedMed.setQuantity(quantity);

        medRef.setValue(updatedMed) // points to the exact location in Firebase for this medication
                .addOnSuccessListener(aVoid -> { // if succeeded
                    Toast.makeText(MedicationDetailsActivity.this, "Medication updated", Toast.LENGTH_SHORT).show();
                    finish();
                }) // if fails
                .addOnFailureListener(e -> Toast.makeText(MedicationDetailsActivity.this, "Failed to update medication", Toast.LENGTH_SHORT).show());
    }
}

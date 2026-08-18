package com.example.time4meds;

// AddReminderActivity: Allows adding or editing medication reminders for a selected elderly profile
// loads all medications from Firebase and populates a spinner for selection
// saves or updates the reminder in Firebase Realtime Database
// schedules notification if the current date is within the start-end date range (via NotificationUtils).

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import android.app.DatePickerDialog;

public class AddReminderActivity extends AppCompatActivity {

    private Spinner spinnerMedName;
    private EditText etDosage, etTime, etStartDate, etEndDate;
    private Button btnSave, btnCancel, btnDelete;
    private String startDateStr, endDateStr;
    private String elderlyId, reminderId;
    private boolean isEditMode = false;
    private DatabaseReference medicationsRef, remindersRef;
    private String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private List<Medication> medications = new ArrayList<>();
    private String selectedMedName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reminder);

        spinnerMedName = findViewById(R.id.spinnerMedName);
        etDosage = findViewById(R.id.etDosage);
        etTime = findViewById(R.id.etTime);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnDelete = findViewById(R.id.btnDelete);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);

        // get/retrieve IDs via intent
        elderlyId = getIntent().getStringExtra("elderlyId");
        reminderId = getIntent().getStringExtra("reminderId");
        isEditMode = reminderId != null;

        // Firebase references for medications
        medicationsRef = FirebaseDatabase.getInstance()
                .getReference("elderly")
                .child(userId)
                .child(elderlyId)
                .child("medications");

        // Firebase references for reminders
        remindersRef = FirebaseDatabase.getInstance()
                .getReference("elderly")
                .child(userId)
                .child(elderlyId)
                .child("reminders");

        loadMedications();

        etTime.setOnClickListener(v -> showTimePicker());
        etStartDate.setOnClickListener(v -> showDatePicker(etStartDate, true));
        etEndDate.setOnClickListener(v -> showDatePicker(etEndDate, false));
        btnSave.setOnClickListener(v -> saveReminder());
        btnCancel.setOnClickListener(v -> finish());

        btnDelete.setVisibility(isEditMode ? Button.VISIBLE : Button.GONE);
        btnDelete.setOnClickListener(v -> deleteReminder());

        if (isEditMode) prefillReminder(); // if click view details, it prefilled the form with existed data
    }

    // load all medications from Firebase
    private void loadMedications() {

        medicationsRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {

                medications.clear();
                List<String> medNames = new ArrayList<>(); // clears the current list of medications to avoid duplicates

                for (DataSnapshot medSnap : snapshot.getChildren()) {
                    Medication med = medSnap.getValue(Medication.class);
                    if (med != null) {
                        medications.add(med);
                        medNames.add(med.getName());
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>( // creates an ArrayAdapter to display added medication names in a spinner (dropdown)
                        AddReminderActivity.this,
                        android.R.layout.simple_spinner_item,
                        medNames
                );

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerMedName.setAdapter(adapter);

                spinnerMedName.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                        selectedMedName = medications.get(position).getName();
                    }

                    @Override
                    public void onNothingSelected(android.widget.AdapterView<?> parent) {
                    }
                });

                if (isEditMode) { // if editing an existing reminder, pre-select the medication that was already chosen
                    for (int i = 0; i < medications.size(); i++) {
                        if (medications.get(i).getName().equals(selectedMedName)) {
                            spinnerMedName.setSelection(i);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    // opens a time picker dialog for the user to choose reminder time
    private void showTimePicker() {

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        new TimePickerDialog(
                this,
                (view, hourOfDay, minute1) ->
                        etTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1)),
                hour,
                minute,
                true
        ).show();
    }

    // fetches a specific reminder from Firebase by its ID
    private void prefillReminder() {

        remindersRef.child(reminderId).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                etDosage.setText(snapshot.child("dosage").getValue(String.class));
                etTime.setText(snapshot.child("time").getValue(String.class));
                selectedMedName = snapshot.child("medicationName").getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    // saves the reminder to Firebase
    private void saveReminder() {

        String dosage = etDosage.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String medName = selectedMedName;

        if (TextUtils.isEmpty(medName) || TextUtils.isEmpty(dosage) || TextUtils.isEmpty(time)
                || TextUtils.isEmpty(startDateStr) || TextUtils.isEmpty(endDateStr)) {

            // for validation input (empty)
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // validate startDate <= endDate
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            if (sdf.parse(startDateStr).after(sdf.parse(endDateStr))) {
                Toast.makeText(this, "Start Date cannot be after End Date", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // parse hour/minute
        String[] hm = time.split(":");
        int hour = Integer.parseInt(hm[0]);
        int minute = Integer.parseInt(hm[1]);

        String id = isEditMode ? reminderId : remindersRef.push().getKey();
        Reminder reminder = new Reminder(medName, dosage, time, startDateStr, endDateStr);
        reminder.setId(id);
        if (!isEditMode) reminder.setTaken(false);

        remindersRef.child(id).setValue(reminder)
                .addOnSuccessListener(aVoid -> {

                    Toast.makeText(
                            this,
                            isEditMode ? "Reminder updated" : "Reminder added",
                            Toast.LENGTH_SHORT
                    ).show();

                    // schedule notification only if today is within range
                    Calendar today = Calendar.getInstance();

                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        if (!today.getTime().before(sdf.parse(startDateStr))
                                && !today.getTime().after(sdf.parse(endDateStr))) {

                            // the notification only runs if today is strictly within the start and end date range
                            // since the check uses full timestamps, so if start and end date is same, the condition may evaluate to false
                            // so the notification will not be pushed
                            NotificationUtils.scheduleReminderNotification(
                                    this,
                                    medName,
                                    dosage,
                                    hour,
                                    minute,
                                    startDateStr,
                                    endDateStr,
                                    elderlyId
                            );
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to save reminder", Toast.LENGTH_SHORT).show()
                );
    }

    // deletes a specific reminder from Firebase if in edit mode
    private void deleteReminder() {

        if (!isEditMode) return;

        remindersRef.child(reminderId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Reminder deleted", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    // opens a date picker dialog for the user to choose reminder date start and end
    private void showDatePicker(EditText editText, boolean isStartDate) {

        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog picker = new DatePickerDialog(
                this,
                (view, y, m, d) -> {
                    m += 1; // month is 0-based

                    String displayDate = String.format(Locale.getDefault(), "%02d.%02d.%04d", d, m, y);
                    editText.setText(displayDate);

                    String firebaseDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m, d);
                    if (isStartDate) startDateStr = firebaseDate;
                    else endDateStr = firebaseDate;
                },
                year,
                month,
                day
        );

        picker.show();
    }
}

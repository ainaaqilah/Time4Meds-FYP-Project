package com.example.time4meds;

// MedicationListActivity: Displays all medications for a selected elderly profile
// loads medication data from Firebase and shows it in a RecyclerView

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth; //new


import java.util.ArrayList;
import java.util.List;

public class MedicationListActivity extends AppCompatActivity {

    private static final String TAG = "MedicationListActivity"; // labels log messages for this class.

    private RecyclerView recyclerView;
    private TextView tvNoMeds;
    private MedicationAdapter adapter;
    private List<Medication> medicationList;
    private String elderlyId;
    private String elderlyName;
    private String userId;
    private DatabaseReference medsRef;
    private ValueEventListener medsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication_list);

        // link all the components to the UI elements
        recyclerView = findViewById(R.id.recyclerMedications);
        tvNoMeds = findViewById(R.id.tvNoMedications);

        // get/retrieve IDs via intent
        Intent intent = getIntent();
        elderlyId = intent.getStringExtra("elderlyId");
        elderlyName = intent.getStringExtra("elderlyName");

        medicationList = new ArrayList<>(); // creates a new empty list to store all the medications
        adapter = new MedicationAdapter(medicationList, this, new MedicationAdapter.OnItemClickListener() { // creates a RecyclerView adapter for displaying medications
            @Override
            public void onViewClick(Medication medication) { // passes elderly ID and medication ID to the next screen
                Intent detailIntent = new Intent(MedicationListActivity.this, MedicationDetailsActivity.class);
                detailIntent.putExtra("elderlyId", elderlyId);
                detailIntent.putExtra("medId", medication.getId());
                startActivity(detailIntent); // open medication details page
            }

            @Override
            public void onDeleteClick(Medication medication, int position) { // is triggered when the “Delete” button for a medication is clicked
                String medId = medication.getId(); // get ID of the medication to delete
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // get current user’s ID from Firebase authentication

                // creates a reference to this exact medication in Firebase
                DatabaseReference medRef = FirebaseDatabase.getInstance()
                        .getReference("elderly")
                        .child(userId)
                        .child(elderlyId)
                        .child("medications")
                        .child(medId);

                medRef.removeValue() // deletes the medication from Firebase
                        .addOnSuccessListener(aVoid -> { // if successfully
                            Toast.makeText(MedicationListActivity.this, "Medication deleted", Toast.LENGTH_SHORT).show();

                        })
                        .addOnFailureListener(e -> { // if fails
                            Toast.makeText(MedicationListActivity.this, "Failed to delete", Toast.LENGTH_SHORT).show();
                        });
            }
        });


        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // tells the RecyclerView to arrange its items in a vertical list (one item per row)
        recyclerView.setAdapter(adapter); // connects the adapter to the RecyclerView

        Button btnAddMedication = findViewById(R.id.btnAddMedication);
        btnAddMedication.setOnClickListener(v -> {
            Intent addIntent = new Intent(MedicationListActivity.this, AddMedicationActivity.class);
            addIntent.putExtra("elderlyId", elderlyId); // passes elderly ID so knows which elderly profile new medication belongs to
            startActivity(addIntent);
        });

        // bottom navbar
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navProfiles = findViewById(R.id.navProfiles);
        LinearLayout navHelp = findViewById(R.id.navHelp);

        navHome.setOnClickListener(v -> { // goes back to welcomepage
            Intent i = new Intent(MedicationListActivity.this, ElderlyWelcomepageActivity.class);
            startActivity(i);
            finish();
        });

        navProfiles.setOnClickListener(v -> { // goes back to profile list
            startActivity(new Intent(this, ProfileListActivity.class));
        });

        navHelp.setOnClickListener(v -> { // goes back to help page
            startActivity(new Intent(this, HelpActivity.class));
        });

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // gets current logged-in user’s ID from Firebase Authentication

        // checks if the elderly profile ID exists
        if (elderlyId != null) {
            // creates a Firebase Database reference to the medications of this elderly person
            medsRef = FirebaseDatabase.getInstance()
                    .getReference("elderly")
                    .child(userId)
                    .child(elderlyId)
                    .child("medications");
            loadMedications();
        } else {
            tvNoMeds.setVisibility(View.VISIBLE);
            tvNoMeds.setText("No medications found.");
        }
    }

    // fetch all medications for specific elderly profile from Firebase and update RecyclerView list
    private void loadMedications() {
        if (medsListener != null) {
            medsRef.removeEventListener(medsListener);
        }

        medsListener = new ValueEventListener() { // create new listener tp watch the changes in meds node
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                medicationList.clear();

                if (!snapshot.exists()) { // checks if there are no medications
                    tvNoMeds.setVisibility(View.VISIBLE);
                    tvNoMeds.setText("No medications available. Please add a medication.");
                    adapter.notifyDataSetChanged();
                    return;
                }

                // converts into a Medication object
                for (DataSnapshot medSnap : snapshot.getChildren()) {
                    Medication med = medSnap.getValue(Medication.class);
                    if (med != null) {
                        med.setId(medSnap.getKey());
                        medicationList.add(med);
                    }
                }

                // shows “No medications” message if the list is empty
                tvNoMeds.setVisibility(medicationList.isEmpty() ? View.VISIBLE : View.GONE);
                adapter.notifyDataSetChanged();
            }

            // called if Firebase fails to load data
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load medications", error.toException());
                Toast.makeText(MedicationListActivity.this, "Failed to load medications", Toast.LENGTH_SHORT).show();
            }
        };
        medsRef.addValueEventListener(medsListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // remove listener to avoid memory leaks
        if (medsRef != null && medsListener != null) {
            medsRef.removeEventListener(medsListener);
        }
    }
}

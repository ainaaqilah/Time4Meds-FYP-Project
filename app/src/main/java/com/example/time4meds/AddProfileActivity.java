package com.example.time4meds;

// AddProfileActivity: Allows users to add or edit elderly profiles
// and saves the data to Firebase Realtime Database and Firebase Storage

import android.os.Bundle;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.net.Uri;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class AddProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1001;

    private EditText etName, etAge, etMedicalInfo, etEmergencyContact;
    private Button btnSave, btnCancel;
    private Spinner spinnerGender;
    private ImageView ivPhoto;
    private TextView tvHeaderTitle;
    private Uri selectedImageUri = null;
    private DatabaseReference dbRef;

    private String elderlyId = null;  // for editing existing profile

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_profile);

        // link all the components to the UI elements
        etName = findViewById(R.id.edtName);
        etAge = findViewById(R.id.edtAge);
        etMedicalInfo = findViewById(R.id.edtMedicalInfo);
        etEmergencyContact = findViewById(R.id.edtEmergencyContact);
        spinnerGender = findViewById(R.id.spinnerGender);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        ivPhoto = findViewById(R.id.ivPhoto);
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);

        // unique profile for each users (Firebase reference)
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            dbRef = FirebaseDatabase.getInstance().getReference("elderly").child(uid);
        }

        // spinner setup
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);

        // pick a photo
        ivPhoto.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        });

        // button functions
        btnSave.setOnClickListener(v -> saveOrUpdateProfile());
        btnCancel.setOnClickListener(v -> finish());

        // load data if click view details of a profile
        if (getIntent() != null && getIntent().hasExtra("elderlyId")) {
            elderlyId = getIntent().getStringExtra("elderlyId");
            etName.setText(getIntent().getStringExtra("name"));
            etAge.setText(getIntent().getStringExtra("age"));
            etMedicalInfo.setText(getIntent().getStringExtra("medicalInfo"));
            spinnerGender.setSelection(adapter.getPosition(getIntent().getStringExtra("gender")));

            // load emergency contact correctly from Intent
            String contact = getIntent().getStringExtra("emergencyContact");
            if (contact != null && !contact.isEmpty()) {
                etEmergencyContact.setText(contact);
            }

            // load photo
            String photoUrl = getIntent().getStringExtra("photoUrl"); // get from previous activity
            if (photoUrl != null && !photoUrl.isEmpty()) {
                Glide.with(this)
                        .load(photoUrl)
                        .placeholder(R.drawable.placeholder)
                        .into(ivPhoto); // display the image
            }

            // if edit mode
            tvHeaderTitle.setText("Edit Elderly Profile");
            btnSave.setText("Save");
        } else {
            // for add mode
            tvHeaderTitle.setText("Add Elderly Profile");
            btnSave.setText("Add");
    }}

    @Override
    // pass the result to the parent class for better handling
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // preview selected photo
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            ivPhoto.setImageURI(selectedImageUri);
        }
    }

    private void saveOrUpdateProfile() {
        String name = etName.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String medicalInfo = etMedicalInfo.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();
        String contact = etEmergencyContact.getText().toString().trim();

        // validations process
        if (name.isEmpty()) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }
        if (age.isEmpty()) {
            etAge.setError("Age is required");
            etAge.requestFocus();
            return;
        }
        if (contact.isEmpty()) {
            etEmergencyContact.setError("Emergency contact is required");
            etEmergencyContact.requestFocus();
            return;
        }

        // save emergency contact in a list (for Firebase structure)
        List<String> emergencyContact = new ArrayList<>();
        emergencyContact.add(contact);

        // to create new profile
        if (elderlyId == null) {
            String newId = dbRef.push().getKey();
            Elderly elderly = new Elderly(newId, name, age, medicalInfo, gender, emergencyContact);
            dbRef.child(newId).setValue(elderly).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    uploadPhoto(newId);
                    Toast.makeText(this, "Profile added successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Failed to add profile", Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            // update existing profile
            dbRef.child(elderlyId).child("name").setValue(name);
            dbRef.child(elderlyId).child("age").setValue(age);
            dbRef.child(elderlyId).child("medicalInfo").setValue(medicalInfo);
            dbRef.child(elderlyId).child("gender").setValue(gender);
            dbRef.child(elderlyId).child("emergencyContact").setValue(emergencyContact)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            uploadPhoto(elderlyId);
                            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    //to upload the profile photo
    private void uploadPhoto(String elderlyId) {
        if (selectedImageUri == null) return;

        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference()
                .child("elderly_photos/" + elderlyId + ".jpg");

        storageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            // Save this URL in the database
                            dbRef.child(elderlyId).child("photoUrl").setValue(uri.toString());
                        })
                )
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Photo upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}

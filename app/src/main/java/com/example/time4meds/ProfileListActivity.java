package com.example.time4meds;

// ProfileListActivity: Displays a list of elderly profiles for the current user using RecyclerView
// allows adding new profiles, and fetches profile data from Firebase Realtime Database

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ProfileListActivity extends AppCompatActivity {

    private RecyclerView recyclerElderly;
    private Button btnAddElderly;
    private ElderlyAdapter elderlyAdapter;
    private List<Elderly> elderlyList;
    private DatabaseReference dbRef;
    private TextView tvNoProfiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_list);

        // link all the components to the UI elements
        recyclerElderly = findViewById(R.id.rvElderly);
        btnAddElderly = findViewById(R.id.btnAddElderly);
        tvNoProfiles = findViewById(R.id.tvNoProfiles);

        recyclerElderly.setLayoutManager(new LinearLayoutManager(this)); // set RecyclerView to vertical list
        elderlyList = new ArrayList<>(); // initialize the list of Elderly objects

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); // get current user
        if (user == null) {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
            finish();
            return;
        }

        // load user data in the profile list page
        String uid = user.getUid(); // get current user ID
        dbRef = FirebaseDatabase.getInstance().getReference("elderly").child(uid); // reference to user's elderly data

        // initialize adapter
        elderlyAdapter = new ElderlyAdapter(ProfileListActivity.this, elderlyList);
        recyclerElderly.setAdapter(elderlyAdapter);

        // fetch data from Firebase
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                elderlyList.clear(); // clear existing list

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Elderly elderly = dataSnapshot.getValue(Elderly.class);
                    if (elderly != null) {
                        elderlyList.add(elderly); // add to list
                    }
                }
                elderlyAdapter.notifyDataSetChanged(); // refresh RecyclerView

                // show message if theres no profiles in the interface
                if (elderlyList.isEmpty()) {
                    tvNoProfiles.setVisibility(View.VISIBLE); // show "no profiles" TextView
                    recyclerElderly.setVisibility(View.GONE); // hide RecyclerView
                } else {
                    tvNoProfiles.setVisibility(View.GONE); // hide "no profiles" TextView
                    recyclerElderly.setVisibility(View.VISIBLE); // show RecyclerView
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {  // only show toast if a user is logged in
                    Toast.makeText(ProfileListActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // add new profile
        btnAddElderly.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileListActivity.this, AddProfileActivity.class);
            startActivity(intent);
        });

        // shows a welcome message when the user enters the profile screen after login/register
        if (getIntent().getBooleanExtra("SHOW_WELCOME_POPUP", false)) {
            showWelcomeDialog();
        }
    }

    private void showWelcomeDialog() { // show the alert dialog
        String message = "To learn how to use the app, click <b>Help > Tutorial</b> in the navigation bar.<br><br>" +
                "<i>Note: You must create at least one elderly profile first to see the navigation bar.</i>";

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Welcome to Time4Meds!")
                .setMessage(android.text.Html.fromHtml(message, android.text.Html.FROM_HTML_MODE_LEGACY))
                .setPositiveButton("Got it", (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }
}

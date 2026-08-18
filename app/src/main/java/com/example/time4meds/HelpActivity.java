package com.example.time4meds;

// HelpActivity: Activity for help page with tutorial, FAQ, about, and logout

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class HelpActivity extends AppCompatActivity {

    private String elderlyId;
    private String elderlyName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        // get elderly info from intent (optional)
        Intent intent = getIntent();
        elderlyId = intent.getStringExtra("elderlyId");
        elderlyName = intent.getStringExtra("elderlyName");

        // buttons
        LinearLayout btnTutorial = findViewById(R.id.btnTutorial);
        LinearLayout btnFAQ = findViewById(R.id.btnFAQ);
        LinearLayout btnAboutUs = findViewById(R.id.btnAboutUs);
        LinearLayout btnLogout = findViewById(R.id.btnLogout);

        // voice switch - toggle button
        Switch switchVoice = findViewById(R.id.switch_voice);
        String uid = FirebaseAuth.getInstance().getUid();
        SharedPreferences prefs = getSharedPreferences("app_settings_" + uid, MODE_PRIVATE);

        switchVoice.setChecked(prefs.getBoolean("voice_notifications", true)); // by default is true if no value has been saved yet

        // when the switch is toggled, save the new value in SharedPreferences
        switchVoice.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("voice_notifications", isChecked).apply();
        });

        // go to Tutorial
        btnTutorial.setOnClickListener(v -> {
            Intent tutorialIntent = new Intent(HelpActivity.this, TutorialActivity.class);
            startActivity(tutorialIntent);
        });

        // go to FAQ
        btnFAQ.setOnClickListener(v -> {
            Intent faqIntent = new Intent(HelpActivity.this, FAQActivity.class);
            startActivity(faqIntent);
        });

        // go to About Us
        btnAboutUs.setOnClickListener(v -> {
            Intent aboutIntent = new Intent(HelpActivity.this, AboutUsActivity.class);
            startActivity(aboutIntent);
        });

        // Logout click
        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Yes", (dialog, which) -> {

                        // 🔴 CLEAR SELECTED PROFILE
                        getSharedPreferences("SelectedProfile", MODE_PRIVATE)
                                .edit()
                                .clear()
                                .apply();

                        // sign out Firebase
                        FirebaseAuth.getInstance().signOut();

                        // go to LoginActivity
                        Intent logoutIntent = new Intent(HelpActivity.this, LoginActivity.class);
                        logoutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(logoutIntent);
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });



        // Bottom navbar
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navProfiles = findViewById(R.id.navProfiles);
        LinearLayout navHelp = findViewById(R.id.navHelp);

        navHome.setOnClickListener(v -> {
            Intent homeIntent = new Intent(HelpActivity.this, ElderlyWelcomepageActivity.class);
            startActivity(homeIntent);
            finish();
        });


        navProfiles.setOnClickListener(v -> {
            Intent profileIntent = new Intent(HelpActivity.this, ProfileListActivity.class);
            startActivity(profileIntent);
        });

        navHelp.setOnClickListener(v -> {
            Toast.makeText(this, "You are already on Help", Toast.LENGTH_SHORT).show();
        });

    }

}

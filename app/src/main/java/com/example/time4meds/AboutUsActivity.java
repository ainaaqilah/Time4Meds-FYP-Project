package com.example.time4meds;

// AboutUsActivity: Activity to display About Us page

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class AboutUsActivity extends AppCompatActivity {

    private String elderlyId;
    private String elderlyName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        Intent intent = getIntent();
        // this is to get data from previous activity
        elderlyId = intent.getStringExtra("elderlyId");
        elderlyName = intent.getStringExtra("elderlyName");

        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navProfiles = findViewById(R.id.navProfiles);
        LinearLayout navHelp = findViewById(R.id.navHelp);

        // home - welcome page
        navHome.setOnClickListener(v -> {
            Intent i = new Intent(AboutUsActivity.this, ElderlyWelcomepageActivity.class);
            i.putExtra("elderlyId", elderlyId);
            i.putExtra("elderlyName", elderlyName);
            startActivity(i);
        });

        // profiles - profile list
        navProfiles.setOnClickListener(v -> {
            startActivity(new Intent(AboutUsActivity.this, ProfileListActivity.class));
        });

        // help - help page
        navHelp.setOnClickListener(v -> {
            startActivity(new Intent(AboutUsActivity.this, HelpActivity.class));
        });
    }
}

package com.example.time4meds;

// FAQActivity: Activity to display Frequently Asked Questions (FAQ) page

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class FAQActivity extends AppCompatActivity {

    private String elderlyId;
    private String elderlyName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);

        Intent intent = getIntent();
        elderlyId = intent.getStringExtra("elderlyId");
        elderlyName = intent.getStringExtra("elderlyName");

        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navProfiles = findViewById(R.id.navProfiles);
        LinearLayout navHelp = findViewById(R.id.navHelp);

        navHome.setOnClickListener(v -> {
            Intent i = new Intent(FAQActivity.this, ElderlyWelcomepageActivity.class);
            i.putExtra("elderlyId", elderlyId);
            i.putExtra("elderlyName", elderlyName);
            startActivity(i);
        });

        navProfiles.setOnClickListener(v -> {
            startActivity(new Intent(FAQActivity.this, ProfileListActivity.class));
        });

        navHelp.setOnClickListener(v -> {
            startActivity(new Intent(FAQActivity.this, HelpActivity.class));
        });
    }
}

package com.example.time4meds;

// TutorialActivity: Activity to show step-by-step tutorial for app features

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

//navbar
import android.content.Intent;
import android.widget.LinearLayout;

public class TutorialActivity extends AppCompatActivity {

    private RecyclerView rvTutorial;
    private TutorialAdapter adapter;
    private List<TutorialModel> tutorialList;

    //navbar
    private String elderlyId;
    private String elderlyName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        // get elderly info from intent
        Intent intent = getIntent();
        elderlyId = intent.getStringExtra("elderlyId");
        elderlyName = intent.getStringExtra("elderlyName");

        rvTutorial = findViewById(R.id.rvTutorial);
        rvTutorial.setLayoutManager(new LinearLayoutManager(this));

        prepareTutorialData();

        adapter = new TutorialAdapter(tutorialList);
        rvTutorial.setAdapter(adapter);

        // -------------------------
        // NAVBAR FUNCTION
        // -------------------------
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navProfiles = findViewById(R.id.navProfiles);
        LinearLayout navHelp = findViewById(R.id.navHelp);

        navHome.setOnClickListener(v -> {
            Intent i = new Intent(TutorialActivity.this, ElderlyWelcomepageActivity.class);
            i.putExtra("elderlyId", elderlyId);
            i.putExtra("elderlyName", elderlyName);
            startActivity(i);
        });

        navProfiles.setOnClickListener(v -> {
            startActivity(new Intent(TutorialActivity.this, ProfileListActivity.class));
        });

        navHelp.setOnClickListener(v -> {
            startActivity(new Intent(TutorialActivity.this, HelpActivity.class));
        });

    }

    private void prepareTutorialData() {
        tutorialList = new ArrayList<>();

        tutorialList.add(new TutorialModel("Step 1: Profile List",
                "View all elderly profiles you’ve created. Tap a profile card to access that elderly’s Welcome Page.",
                "Tip : Use 'Add Profile' to create a new profile. Tap 'View Details' to edit or 'Delete' to remove it.",
                R.drawable.profile));

        tutorialList.add(new TutorialModel("Step 2: Welcome Page Overview",
                "See elderly photo, name, profile info, and next medications. Four main buttons let you access Medication, Reminders, Daily Checklist, or SOS Alert.",
                "Tip: Tap navbar buttons (Home, Profiles, Help) to navigate between pages.",
                R.drawable.welcome));

        tutorialList.add(new TutorialModel("Step 3: Medication Management",
                "View all medications for the selected elderly. Tap 'Add' to create new medications. Each medication card lets you view details or delete it.",
                "Tip: In the details page, you can edit dosage, timing, and other info.",
                R.drawable.medication));

        tutorialList.add(new TutorialModel("Step 4: Reminder Management",
                "View all set reminders, filtered by All/Today/Upcoming. Tap 'Add' to create a new reminder based on existing medications.",
                "Tip: Make sure the reminder is saved correctly for notifications to work.",
                R.drawable.reminders));

        tutorialList.add(new TutorialModel("Step 5: Daily Checklist",
                "View all medications that need to be taken today. Indicators: Green = Taken, Grey = Not Taken, Red = Missed.",
                "Tip: Filter by today, last week, or last month to review adherence.",
                R.drawable.checklist));

        tutorialList.add(new TutorialModel("Step 6: SOS Alert",
                "Long press the SOS button to dial emergency number (999) and notify your emergency contacts with location via WhatsApp.",
                "Tip: Use only in emergencies.",
                R.drawable.sos));

        tutorialList.add(new TutorialModel("Step 7: Help Page",
                "Access Tutorial, About Us, FAQ, toggle voice notifications, or logout.",
                "Tip: The tutorial button here opens this step-by-step guide anytime.",
                R.drawable.help));
    }
}
package com.example.time4meds;

// ReminderListActivity: Displays a list of reminders for a specific elderly user with filtering options
// supports three filters:
//    - "Today": reminders occurring today
//    - "Upcoming": reminders with future occurrences
//    - "All": includes all reminders (past, today, future)

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class ReminderListActivity extends AppCompatActivity { // this is for the filteration of reminders

    private RecyclerView recyclerView;
    private ReminderAdapter adapter;
    private TextView tvEmpty;
    private List<Reminder> allReminders = new ArrayList<>();
    private List<Reminder> filteredReminders = new ArrayList<>();
    private String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private String elderlyId;
    private DatabaseReference remindersRef;
    private Button btnAll, btnToday, btnUpcoming;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_list);

        recyclerView = findViewById(R.id.recyclerViewReminders);
        tvEmpty = findViewById(R.id.tvEmpty);

        // get elderly ID passed from previous activity
        elderlyId = getIntent().getStringExtra("elderlyId");

        // reference to Firebase "reminders" node for this elderly
        remindersRef = FirebaseDatabase.getInstance()
                .getReference("elderly")
                .child(userId)
                .child(elderlyId)
                .child("reminders");

        adapter = new ReminderAdapter(filteredReminders, this, elderlyId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // filter buttons
        Button btnAll = findViewById(R.id.btnAll);
        Button btnToday = findViewById(R.id.btnToday);
        Button btnUpcoming = findViewById(R.id.btnUpcoming);

        btnAll.setOnClickListener(v -> {
            applyFilter("all");
            setActiveButton(btnAll, btnAll, btnToday, btnUpcoming); // highlight active button
        });

        btnToday.setOnClickListener(v -> {
            applyFilter("today");
            setActiveButton(btnToday, btnAll, btnToday, btnUpcoming);
        });

        btnUpcoming.setOnClickListener(v -> {
            applyFilter("upcoming");
            setActiveButton(btnUpcoming, btnAll, btnToday, btnUpcoming);
        });

        // fet default active button
        setActiveButton(btnToday, btnAll, btnToday, btnUpcoming);

        // add reminder button
        findViewById(R.id.btnAddReminder).setOnClickListener(v -> {
            Intent intent = new Intent(ReminderListActivity.this, AddReminderActivity.class);
            intent.putExtra("elderlyId", elderlyId);
            startActivity(intent);
        });

        // bottom navbar
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navProfiles = findViewById(R.id.navProfiles);
        LinearLayout navHelp = findViewById(R.id.navHelp);

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(ReminderListActivity.this, ElderlyWelcomepageActivity.class));
            finish(); // optional: removes ReminderListActivity from back stack
        });

        navProfiles.setOnClickListener(v -> {
            startActivity(new Intent(ReminderListActivity.this, ProfileListActivity.class));
        });

        navHelp.setOnClickListener(v -> {
            startActivity(new Intent(ReminderListActivity.this, HelpActivity.class));
        });

        remindersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allReminders.clear(); // clear previous list

                for (DataSnapshot reminderSnap : snapshot.getChildren()) {
                    Reminder reminder = reminderSnap.getValue(Reminder.class);
                    if (reminder == null) continue;
                    reminder.setId(reminderSnap.getKey());
                    allReminders.add(reminder); // add to all reminders list
                }

                applyFilter("today"); // by default for the filter is TODAY
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { // do nothing on error
            }
        });
    }

    // filter logic
    private void applyFilter(String filter) {
        filteredReminders.clear();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());  // clear previous filtered list

        Calendar todayCal = Calendar.getInstance();
        resetTime(todayCal); // remove time part to compare only dates

        for (Reminder reminder : allReminders) {
            try {
                Calendar start = Calendar.getInstance();
                start.setTime(sdf.parse(reminder.getStartDate()));
                resetTime(start); // reset time for comparison

                Calendar end = Calendar.getInstance();
                end.setTime(sdf.parse(reminder.getEndDate()));
                resetTime(end);

                List<Long> occurrences;

                switch (filter) {
                    case "today": // add reminder if today is between start and end
                        if (!todayCal.before(start) && !todayCal.after(end)) {
                            filteredReminders.add(reminder);
                        }
                        break;

                    case "upcoming": // generate all occurrences and add if any is in future
                        occurrences = DateUtils.generateReminderOccurrences(reminder);
                        long now = System.currentTimeMillis();
                        for (long t : occurrences) {
                            if (t >= now) {
                                filteredReminders.add(reminder);
                                break;
                            }
                        }
                        break;

                    case "all": // include today or any future occurrences
                        if (!todayCal.before(start) && !todayCal.after(end)) {
                            filteredReminders.add(reminder);
                        } else {
                            occurrences = DateUtils.generateReminderOccurrences(reminder);
                            now = System.currentTimeMillis();
                            for (long t : occurrences) {
                                if (t >= now) {
                                    filteredReminders.add(reminder);
                                    break;
                                }
                            }
                        }
                        break;
                }

                // DEBUG
                android.util.Log.d("ReminderDebug", reminder.getMedicationName() +
                        " | start: " + reminder.getStartDate() +
                        " | end: " + reminder.getEndDate() +
                        " | today: " + new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(todayCal.getTime()) +
                        " | today >= start: " + (!todayCal.before(start)) +
                        " | today <= end: " + (!todayCal.after(end)));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // show empty TextView if no reminders match
        if (filteredReminders.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        adapter.updateList(filteredReminders); // update RecyclerView with filtered list
    }

    // helper to remove time part from Calendar
    private void resetTime(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }


    // highlight active button
    private void setActiveButton(Button selectedBtn, Button... allButtons) {
        for (Button btn : allButtons) {
            if (btn == selectedBtn) {
                btn.setBackgroundResource(R.drawable.button_selected);
                btn.setTextColor(getResources().getColor(android.R.color.white));
            } else {
                btn.setBackgroundResource(R.drawable.button_default);
                btn.setTextColor(getResources().getColor(R.color.black));
            }
        }
    }
}

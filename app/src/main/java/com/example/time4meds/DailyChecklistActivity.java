package com.example.time4meds;

// DailyChecklistActivity: Displays the daily medication checklist and history for a specific elderly user

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class DailyChecklistActivity extends AppCompatActivity {

    private RecyclerView rvDailyChecklist;
    private TextView tvDate, tvSubtitle;
    private DailyChecklistAdapter adapter;
    private List<Reminder> reminderList = new ArrayList<>();
    private String elderlyId;
    private String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
    private DatabaseReference remindersRef;
    private Reminder firstUpcoming = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_checklist);

        rvDailyChecklist = findViewById(R.id.rvDailyChecklist);
        tvDate = findViewById(R.id.tvDate);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        Button btnFilter = findViewById(R.id.btnFilter);

        // bottom navbar
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navProfiles = findViewById(R.id.navProfiles);
        LinearLayout navHelp = findViewById(R.id.navHelp);

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, ElderlyWelcomepageActivity.class));
            finish();
        });

        navProfiles.setOnClickListener(v -> startActivity(new Intent(this, ProfileListActivity.class)));
        navHelp.setOnClickListener(v -> startActivity(new Intent(this, HelpActivity.class)));

        // for setting the greeting & default subtitle
        setGreeting();
        updateSubtitle("TODAY"); // by default

        btnFilter.setOnClickListener(v -> {
            androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(DailyChecklistActivity.this, v);
            popup.getMenuInflater().inflate(R.menu.filter_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                String choice = item.getTitle().toString();
                btnFilter.setText(choice);

                switch (choice) {
                    case "Today":
                        updateSubtitle("TODAY");
                        adapter = new DailyChecklistAdapter(DailyChecklistActivity.this, reminderList, elderlyId);
                        rvDailyChecklist.setAdapter(adapter);
                        loadTodayReminders();
                        break;
                    case "Last 1 Week":
                        updateSubtitle("WEEK");
                        loadChecklistHistory(7);
                        break;
                    case "Last 1 Month":
                        updateSubtitle("MONTH");
                        loadChecklistHistory(30);
                        break;
                    case "Last 3 Months":
                        updateSubtitle("THREE_MONTHS");
                        loadChecklistHistory(90);
                        break;
                }
                return true;
            });
            popup.show();
        });

        // retrieves elderly ID from previous activity
        elderlyId = getIntent().getStringExtra("elderlyId");

        // help to make sure the reminder data can be shown in the RecyclerView
        adapter = new DailyChecklistAdapter(this, reminderList, elderlyId);
        rvDailyChecklist.setLayoutManager(new LinearLayoutManager(this));
        rvDailyChecklist.setAdapter(adapter);

        remindersRef = FirebaseDatabase.getInstance()
                .getReference("elderly")
                .child(userId)
                .child(elderlyId)
                .child("reminders");

        loadTodayReminders();
    }

    // greetings in the daily page
    private void setGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour < 12) {
            tvDate.setText("Good Morning!");
        } else if (hour < 18) {
            tvDate.setText("Good Afternoon!");
        } else {
            tvDate.setText("Good Evening!");
        }
    }

    // this one for the subs according to the filter choices
    private void updateSubtitle(String filterType) {
        switch (filterType) {
            case "TODAY":
                tvSubtitle.setText("Here’s your daily medication checklist for today");
                break;
            case "WEEK":
                tvSubtitle.setText("Here’s your daily medication checklist for the past week");
                break;
            case "MONTH":
                tvSubtitle.setText("Here’s your daily medication checklist for the past month");
                break;
            case "THREE_MONTHS":
                tvSubtitle.setText("Here’s your daily medication checklist for the past 3 months");
                break;
            default:
                tvSubtitle.setText("Here’s your daily medication checklist");
        }
    }

    // checks if today is between start and end date
    private static boolean isDateWithin(String start, String end, String today) {
        return today.compareTo(start) >= 0 && today.compareTo(end) <= 0;
    }

    // loads today's reminders from Firebase
    private void loadTodayReminders() {
        remindersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                reminderList.clear(); // clears old data
                firstUpcoming = null; // resets upcoming reminder
                String today = Utils.getTodayDate(); // to gets today's date

                for (DataSnapshot snap : snapshot.getChildren()) {
                    Reminder reminder = snap.getValue(Reminder.class);
                    if (reminder == null) continue;

                    if (isDateWithin(reminder.getStartDate(), reminder.getEndDate(), today)) {

                        // Flag overdue doses
                        if (!reminder.isTaken() && isTimePassed(reminder.getTime())) {
                            reminder.setOverdue(true); // mark as overdue if time has passed = missed
                        } else {
                            reminder.setOverdue(false); // mark as not overdue if not
                        }

                        reminderList.add(reminder); // add reminder to list

                        // Find first upcoming
                        if (!reminder.isTaken() && firstUpcoming == null && !isTimePassed(reminder.getTime())) {
                            firstUpcoming = reminder;
                        }
                    }
                }

                adapter.notifyDataSetChanged(); // refresh the RecyclerView

                // scroll to first upcoming dose
                if (firstUpcoming != null) {
                    int position = reminderList.indexOf(firstUpcoming);
                    if (position >= 0) rvDailyChecklist.scrollToPosition(position);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) { }
        });
    }

    // checks if a reminder time has passed today
    private boolean isTimePassed(String timeStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Calendar now = Calendar.getInstance();
            Calendar reminderTime = Calendar.getInstance();
            reminderTime.setTime(sdf.parse(timeStr));

            return now.get(Calendar.HOUR_OF_DAY) > reminderTime.get(Calendar.HOUR_OF_DAY)
                    || (now.get(Calendar.HOUR_OF_DAY) == reminderTime.get(Calendar.HOUR_OF_DAY)
                    && now.get(Calendar.MINUTE) > reminderTime.get(Calendar.MINUTE));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // loads checklist history for given number of days
    private void loadChecklistHistory(int days) {
        DailyChecklistAdapterForHistory historyAdapter =
                new DailyChecklistAdapterForHistory(DailyChecklistActivity.this, new ArrayList<>());
        rvDailyChecklist.setAdapter(historyAdapter);

        DatabaseReference historyRef = FirebaseDatabase.getInstance()
                .getReference("elderly")
                .child(userId)
                .child(elderlyId)
                .child("dailyChecklist");

        // to calculates cutoff time (for filter)
        // days → number of days (e.g. 7, 30, 90)
        //24L → hours in one day
        //60 → minutes in one hour
        //60 → seconds in one minute
        //1000 → milliseconds in one second
        long periodMillis = System.currentTimeMillis() - (days * 24L * 60 * 60 * 1000);

        historyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<ChecklistItem> historyList = new ArrayList<>();

                for (DataSnapshot dateSnap : snapshot.getChildren()) {
                    String dateStr = dateSnap.getKey();
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        long dateMillis = sdf.parse(dateStr).getTime();

                        if (dateMillis >= periodMillis) {
                            for (DataSnapshot itemSnap : dateSnap.getChildren()) {
                                ChecklistItem item = itemSnap.getValue(ChecklistItem.class);
                                if (item == null) continue;

                                if (item.getMedName() == null) {
                                    String medNameFromFirebase = itemSnap.child("medicationName").getValue(String.class);
                                    if (medNameFromFirebase != null) item.setMedName(medNameFromFirebase);
                                }

                                String time = itemSnap.child("time").getValue(String.class);
                                if (time != null) item.setTime(time);

                                Boolean overdue = itemSnap.child("overdue").getValue(Boolean.class);
                                if (overdue != null) item.setOverdue(overdue);

                                historyList.add(item); // add history item

                                Log.d("HistoryDebug", "Loaded item: " + item.getMedName() + ", " + item.getDosage() + ", " + item.getTime() + ", overdue=" + item.isOverdue());
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                historyAdapter.updateData(historyList); // update RecyclerView
            }

            @Override
            public void onCancelled(DatabaseError error) { }
        });
    }

}

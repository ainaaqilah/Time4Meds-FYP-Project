package com.example.time4meds;

// HistoryActivity: Activity for viewing and filtering historical daily checklist

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private Spinner spinnerFilterRange;
    private Button btnCustomRange;
    private HistoryAdapter adapter;
    private List<ChecklistItem> historyList = new ArrayList<>();
    private List<ChecklistItem> fullHistoryList = new ArrayList<>();
    private String elderlyId;
    private String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private DatabaseReference checklistRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // CHECK USER LOGIN
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish(); // or redirect to login page
            return;
        }
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        rvHistory = findViewById(R.id.rvHistory);
        spinnerFilterRange = findViewById(R.id.spinnerFilterRange);
        btnCustomRange = findViewById(R.id.btnCustomRange);

        elderlyId = getIntent().getStringExtra("elderlyId");
        if (elderlyId == null) {
            Toast.makeText(this, "No elderly selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter(historyList);
        rvHistory.setAdapter(adapter);

        checklistRef = FirebaseDatabase.getInstance()
                .getReference("elderly")
                .child(userId)
                .child(elderlyId)
                .child("dailyChecklist");

        setupSpinner(); // setup predefined filter ranges
        setupCustomRangeButton();
        loadHistory(7); // by default: last 7 days
    }

    private void setupSpinner() {
        String[] options = {"Last 7 days", "Last 1 month", "Last 3 months"}; // filter options
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, options);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilterRange.setAdapter(spinnerAdapter);

        spinnerFilterRange.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                switch (position) { // determine which range is selected
                    case 0: loadHistory(7); break; // last 7 days
                    case 1: loadHistory(30); break; // last 1 month
                    case 2: loadHistory(90); break; // last 3 months
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void setupCustomRangeButton() {
        btnCustomRange.setOnClickListener(v -> showStartDatePicker()); // open start date picker on click
    }

    private void showStartDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog startDatePicker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> { // callback when date selected
                    Calendar startCal = Calendar.getInstance();
                    startCal.set(year, month, dayOfMonth);
                    showEndDatePicker(startCal.getTimeInMillis());
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        startDatePicker.show(); // show date picker dialog
    }

    private void showEndDatePicker(long startMillis) {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog endDatePicker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar endCal = Calendar.getInstance();
                    endCal.set(year, month, dayOfMonth);
                    filterHistoryByCustomRange(startMillis, endCal.getTimeInMillis()); // filter history
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        endDatePicker.show();
    }

    private void filterHistoryByCustomRange(long startMillis, long endMillis) {
        List<ChecklistItem> filteredList = new ArrayList<>();
        for (ChecklistItem item : fullHistoryList) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                long itemMillis = sdf.parse(item.getDate()).getTime();
                if (itemMillis >= startMillis && itemMillis <= endMillis) {
                    filteredList.add(item);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        adapter.updateList(filteredList); // update adapter with filtered data
    }

    private void loadHistory(int daysBack) {
        checklistRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                historyList.clear();
                fullHistoryList.clear();

                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_YEAR, -daysBack);
                Date startDate = cal.getTime();

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                for (DataSnapshot daySnapshot : snapshot.getChildren()) {
                    String dateStr = daySnapshot.getKey();
                    try {
                        Date entryDate = sdf.parse(dateStr);
                        if (entryDate != null && !entryDate.before(startDate)) {
                            for (DataSnapshot itemSnap : daySnapshot.getChildren()) {
                                ChecklistItem item = itemSnap.getValue(ChecklistItem.class);
                                if (item != null) {
                                    historyList.add(item);
                                    fullHistoryList.add(item);
                                }
                            }
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}

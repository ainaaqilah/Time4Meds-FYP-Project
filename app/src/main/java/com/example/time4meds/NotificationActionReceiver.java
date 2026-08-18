package com.example.time4meds;

// NotificationActionReceiver: Handles actions when user interacts with a medication reminder notification
// marks the dose as taken in Firebase, saves it to the daily checklist, cancels the notification
// and optionally fetches history from the last 3 months for display or processing

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

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

public class NotificationActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent == null) return;

        String elderlyId = intent.getStringExtra("elderlyId");
        String medName = intent.getStringExtra("medicationName");
        String dosage = intent.getStringExtra("dosage");
        String elderlyName = intent.getStringExtra("elderlyName");
        int notificationId = intent.getIntExtra("notificationId", -1);
        String reminderId = intent.getStringExtra("reminderId"); // reminderId is correctly passed

        if (elderlyName == null) elderlyName = "Elderly"; // Default name if elderlyName is null
        final String finalElderlyName = elderlyName;

        if (elderlyId == null || medName == null || reminderId == null) return; // exit if data is missing

        String uid = FirebaseAuth.getInstance().getUid(); // get the current user's UID from Firebase Auth
        if (uid == null) return; // exit if user is not logged in

        // update 'taken' in reminders
        DatabaseReference reminderRef = FirebaseDatabase.getInstance()
                .getReference("elderly")
                .child(uid)
                .child(elderlyId)
                .child("reminders")
                .child(reminderId);

        reminderRef.child("taken").setValue(true); // mark the reminder as taken

        // save the action in daily checklist
        DatabaseReference checklistRef = FirebaseDatabase.getInstance()
                .getReference("elderly")
                .child(uid)
                .child(elderlyId)
                .child("dailyChecklist");

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Calendar.getInstance().getTime());

        String key = checklistRef.child(today).push().getKey();

        if (key != null) {

            ChecklistItem item = new ChecklistItem(medName, dosage, true, today); // create a checklist item

            // navigate to today's checklist
            checklistRef.child(today)
                    .child(key)
                    .setValue(item)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(
                                    context,
                                    medName + " marked as taken for " + finalElderlyName, // display confirmation toast
                                    Toast.LENGTH_SHORT
                            ).show()
                    );
        }

        // cancel the notification
        if (notificationId != -1) {
            NotificationManagerCompat.from(context).cancel(notificationId);
        }

        long now = System.currentTimeMillis(); // current time in milliseconds
        long threeMonthsAgo = now - (90L * 24 * 60 * 60 * 1000); // calculate timestamp for 90 days ago

        // get Firebase reference for history
        DatabaseReference historyRef = FirebaseDatabase.getInstance()
                .getReference("elderly")
                .child(uid)
                .child(elderlyId)
                .child("history");

        historyRef.orderByKey()
                .startAt(String.valueOf(threeMonthsAgo)) // start from 3 months ago
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) { // called when data is fetched

                        List<ChecklistItem> filteredList = new ArrayList<>(); // prepare a list to store filtered items
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ChecklistItem item = ds.getValue(ChecklistItem.class);
                            if (item != null) filteredList.add(item);
                        }
                        // pass filteredList to the DailyChecklist RecyclerView adapter
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { // called if data fetch is cancelled
                    }
                });
    }
}

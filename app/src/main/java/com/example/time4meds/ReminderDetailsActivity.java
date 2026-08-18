package com.example.time4meds;

// ReminderDetailsActivity: A simple activity that immediately opens AddReminderActivity in edit mode

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class ReminderDetailsActivity extends AppCompatActivity {

    private String elderlyId, reminderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        elderlyId = getIntent().getStringExtra("elderlyId");
        reminderId = getIntent().getStringExtra("reminderId");

        // directly open AddReminderActivity in edit mode
        Intent intent = new Intent(this, AddReminderActivity.class);
        intent.putExtra("elderlyId", elderlyId);
        intent.putExtra("reminderId", reminderId);
        startActivity(intent);
        finish();
    }
}

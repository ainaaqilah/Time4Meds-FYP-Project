package com.example.time4meds;

// ElderlyWelcomepageActivity: Main dashboard for a selected elderly profile
// displays profile details and next medication
// provides navigation to core features (Medication, Reminders, Checklist)
// and includes SOS functionality with location and emergency contacts

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import com.bumptech.glide.Glide;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ElderlyWelcomepageActivity extends AppCompatActivity {

    private static final String TAG = "ElderlyHomepage"; // tag for logging/debugging
    private static final int REQUEST_SOS_PERMISSION = 101; // request for SOS permission
    private static final int REQUEST_CALL_PERMISSION = 102; // request for call permission
    private static final String EMERGENCY_NUMBER = "999"; // emergency contact number

    private TextView tvElderlyName, tvNextMedication;
    private TextView tvProfileAge, tvProfileGender, tvProfileMedical, tvProfileContact;
    private LinearLayout cardMedication, cardReminders, cardChecklist, cardSOS;
    private LinearLayout navHome, navProfiles, navHelp;
    private ImageView ivPhoto;

    // Data
    private String elderlyId;
    private String elderlyName;
    private FusedLocationProviderClient fusedLocationClient; // used to get device location

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elderly_welcomepage);

        // link all the components to the UI profile card
        tvElderlyName = findViewById(R.id.tvElderlyName);
        ivPhoto = findViewById(R.id.ivPhoto);
        tvNextMedication = findViewById(R.id.tvNextMedication);
        tvProfileAge = findViewById(R.id.tvProfileAge);
        tvProfileGender = findViewById(R.id.tvProfileGender);
        tvProfileMedical = findViewById(R.id.tvProfileMedical);
        tvProfileContact = findViewById(R.id.tvProfileContact);

        // link all the components (core features)
        cardMedication = findViewById(R.id.cardMedication);
        cardReminders = findViewById(R.id.cardReminders);
        cardChecklist = findViewById(R.id.cardChecklist);
        cardSOS = findViewById(R.id.cardSOS);

        // link all the components (bottom navbar)
        navHome = findViewById(R.id.navHome);
        navProfiles = findViewById(R.id.navProfiles);
        navHelp = findViewById(R.id.navHelp);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this); // initialize location client

        // read selected profile
        SharedPreferences prefs = getSharedPreferences("SelectedProfile", MODE_PRIVATE);
        elderlyId = prefs.getString("profileId", null);
        elderlyName = prefs.getString("profileName", null);

        // set the name in UI
        tvElderlyName.setText(elderlyName != null ? elderlyName : "Unknown");

        // load the profile details
        loadProfileDetails();
        loadNextMedication();

        // navbar click listener
        cardMedication.setOnClickListener(v -> safeStart(MedicationListActivity.class));
        cardReminders.setOnClickListener(v -> safeStart(ReminderListActivity.class));
        cardChecklist.setOnClickListener(v -> safeStart(DailyChecklistActivity.class));

        // SOS long press listener
        cardSOS.setOnLongClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED || // check location permission
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                            != PackageManager.PERMISSION_GRANTED) { // check call permission
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CALL_PHONE},
                            REQUEST_SOS_PERMISSION); // request location and call permissions
            } else {
                triggerSOS(); // permissions granted, perform SOS action
            }
            return true;
        });

        // click bottom navbar
        navProfiles.setOnClickListener(v -> safeStart(ProfileListActivity.class));
        navHelp.setOnClickListener(v -> safeStart(HelpActivity.class));
    }

    private void safeStart(Class<?> cls) {
        try {
            startActivity(new Intent(this, cls).putExtra("elderlyId", elderlyId)); // start activity and pass elderlyId
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Feature unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    // ================= PROFILE =================
    private void loadProfileDetails() {
        if (elderlyId == null) return; // exit if no elderlyId provided

        // reference to the specific elderly profile in Firebase
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("elderly")
                .child(FirebaseAuth.getInstance().getUid())
                .child(elderlyId);

        ref.get().addOnSuccessListener(snapshot -> {
            // get data fields from Firebase
            String age = snapshot.child("age").getValue(String.class);
            String gender = snapshot.child("gender").getValue(String.class);
            String medicalInfo = snapshot.child("medicalInfo").getValue(String.class);
            String photoUrl = snapshot.child("photoUrl").getValue(String.class);

            // become a default value if fields are missing
            if (age == null || age.isEmpty()) age = "-";
            if (gender == null || gender.isEmpty()) gender = "-";
            if (medicalInfo == null || medicalInfo.isEmpty()) medicalInfo = "None";

            // load emergency contacts if available
            StringBuilder contact = new StringBuilder();
            if (snapshot.child("emergencyContact").exists()) {
                for (DataSnapshot s : snapshot.child("emergencyContact").getChildren()) {
                    contact.append(s.getValue(String.class)).append(", ");
                }
                if (contact.length() > 0) contact.setLength(contact.length() - 2);
            } else {
                contact.append("None");
            }

            // display data in UI
            tvProfileAge.setText("Age: " + age);
            tvProfileGender.setText("Gender: " + gender);
            tvProfileMedical.setText("Medical Info: " + medicalInfo);
            tvProfileContact.setText("Emergency Contact: " + contact);

            // load profile photo
            if (photoUrl != null && !photoUrl.isEmpty()) {
                Glide.with(this)
                        .load(photoUrl)
                        .placeholder(R.drawable.placeholder)
                        .into(ivPhoto);
            } else {
                ivPhoto.setImageResource(R.drawable.placeholder); // if no photo, use default placeholder
            }
        });

    }

    // ================= NEXT MEDS =================
    private void loadNextMedication() {
        if (elderlyId == null) {
            tvNextMedication.setText("No upcoming medication"); // no elderly selected
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("elderly")
                .child(FirebaseAuth.getInstance().getUid())
                .child(elderlyId)
                .child("reminders");

        ref.get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                tvNextMedication.setText("No upcoming medication"); // no reminders exist
                return;
            }

            long now = System.currentTimeMillis(); // current time
            long closestTime = Long.MAX_VALUE; // earliest upcoming reminder
            List<Reminder> nextMeds = new ArrayList<>(); // list of meds at earliest time

            // find all reminders with the earliest upcoming time
            for (DataSnapshot remSnap : snapshot.getChildren()) {
                Reminder rem = remSnap.getValue(Reminder.class);
                if (rem == null) continue;

                List<Long> occurrences = DateUtils.generateReminderOccurrences(rem); // all reminder times

                for (long time : occurrences) {
                    if (time < now) continue; // skip past reminders
                    if (time < closestTime) {  // found new earliest time
                        closestTime = time;
                        nextMeds.clear();
                        nextMeds.add(rem);
                    } else if (time == closestTime) { // multiple meds at same time
                        nextMeds.add(rem);
                    }
                }
            }

            // update TextView with next medication info
            if (!nextMeds.isEmpty()) {
                Reminder first = nextMeds.get(0);
                String timeText = android.text.format.DateFormat.format("hh:mm a", closestTime).toString();

                if (nextMeds.size() == 1) {
                    // if only one med
                    tvNextMedication.setText(
                            first.getMedicationName() + "\nDosage: " + first.getDosage() +
                                    "\nTime: " + timeText
                    );
                } else {
                    // if there are multiple meds at 1 time
                    int moreCount = nextMeds.size() - 1;
                    tvNextMedication.setText(
                            first.getMedicationName() + " + " + moreCount + " more" +
                                    "\nDosage: " + first.getDosage() +
                                    "\nTime: " + timeText
                    );
                }
            } else {
                tvNextMedication.setText("No upcoming medication");
            }

        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to load reminders", e);
            tvNextMedication.setText("No upcoming medication");
        });
    }

    // ================= SOS =================
    private void triggerSOS() {
        fetchContactAndSendSMS(); // get contacts and send SOS messages via WhatsApp
        callEmergencyNumber(); // make emergency phone call
    }

    private void fetchContactAndSendSMS() {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("elderly")
                .child(FirebaseAuth.getInstance().getUid())
                .child(elderlyId)
                .child("emergencyContact");

        ref.get().addOnSuccessListener(snapshot -> {
            List<String> contact = new ArrayList<>();

            // fetch contacts from snapshot
            if (snapshot.exists()) {
                Object value = snapshot.getValue();
                if (value instanceof List) {
                    for (Object obj : (List<?>) value) {
                        if (obj != null) contact.add(obj.toString());
                    }
                } else {
                    for (DataSnapshot s : snapshot.getChildren()) {
                        String num = s.getValue(String.class);
                        if (num != null) contact.add(num);
                    }
                }
            }

            // if emergency contact is not available
            if (contact.isEmpty()) {
                Toast.makeText(this, "No emergency contact available", Toast.LENGTH_SHORT).show();
                return;
            }

            // convert local numbers to international format (+60)
            for (int i = 0; i < contact.size(); i++) {
                String num = contact.get(i).trim();
                if (num.startsWith("0")) contact.set(i, "+60" + num.substring(1));
            }

            // get location if permission granted
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(location -> sendSOSViaWhatsApp(contact, location)) // send with location
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Location fetch failed", e);
                            sendSOSViaWhatsApp(contact, null); // fallback without location
                        });
            } else {
                sendSOSViaWhatsApp(contact, null); // fallback without location
            }

        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to fetch emergency contact", e);
            Toast.makeText(this, "Unable to send SOS", Toast.LENGTH_SHORT).show();
        });
    }

    private void sendSOSViaWhatsApp(List<String> contact, Location location) {

        // prepare location text and Google Maps link
        String locationText = "Location unavailable";
        String mapLink = "";
        if (location != null) {
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            locationText = lat + ", " + lng;
            mapLink = "https://maps.google.com/?q=" + lat + "," + lng;
        }

        // build SOS message style
        String message = "🚨 *EMERGENCY ALERT* 🚨\n\n" +
                "👤 *Elderly:* " + elderlyName + "\n" +
                "⚠️ *Immediate Help Needed!*\n\n" +
                "📍 *Location:*\n" + locationText + "\n" +
                mapLink + "\n\n" +
                "⏰ Please respond *ASAP!*";

        // send message to the saved contact via WhatsApp
        for (String number : contact) {
            try {
                String waNumber = number.startsWith("+") ? number.substring(1) : number;
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://wa.me/" + waNumber + "?text=" + Uri.encode(message)));
                intent.setPackage("com.whatsapp");
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Failed to open WhatsApp for " + number, e);
                Toast.makeText(this, "Cannot open WhatsApp for " + number, Toast.LENGTH_SHORT).show();
            }
        }
    }

    // after long-pressed, it will go directly to phone's dialer
    private void callEmergencyNumber() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    REQUEST_CALL_PERMISSION); // request permission if not granted
        } else {
            // make the emergency call
            Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + EMERGENCY_NUMBER));
            startActivity(callIntent);
        }
    }
}

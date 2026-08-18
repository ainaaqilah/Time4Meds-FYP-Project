package com.example.time4meds;

// NotificationWorker: Worker class responsible for showing medication reminder notifications
// builds a notification with a "Taken" action button that triggers NotificationActionReceiver
// if voice notifications are enabled, uses TextToSpeech to read out the reminder.
// schedules a MissedDoseWorker to alert the user if the medication is not taken after a set delay.

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.database.DataSnapshot;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class NotificationWorker extends Worker {

    public static final String CHANNEL_ID = "ReminderChannel";

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {

        // retrieve data passed from the WorkRequest
        String medName = getInputData().getString("medicationName");
        String dosage = getInputData().getString("dosage");
        // if WorkRequest didn’t pass the hour/minute, the Worker will still have a safe default time of 9:00 AM
        int hour = getInputData().getInt("hour", 9);
        int minute = getInputData().getInt("minute", 0);
        String startDateStr = getInputData().getString("startDate");
        String endDateStr = getInputData().getString("endDate");
        String elderlyId = getInputData().getString("elderlyId");

        // fail early if any critical info is missing
        if (medName == null || dosage == null || startDateStr == null || endDateStr == null || elderlyId == null) {
            return Result.failure();
        }

        // check if today is within the reminder date range
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date today = sdf.parse(sdf.format(new Date()));
            Date startDate = sdf.parse(startDateStr);
            Date endDate = sdf.parse(endDateStr);

            if (today.before(startDate) || today.after(endDate)) { // skip if outside range
                return Result.success();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure();
        }

        createNotificationChannel(); // ensure notification channel exists

        int notificationId = (int) System.currentTimeMillis();
        String elderlyName = fetchElderlyName(elderlyId);

        // current user ID
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();

        // check if voice notifications are enabled in SharedPreferences
        boolean voiceEnabled = getApplicationContext()
                .getSharedPreferences("app_settings_" + uid, Context.MODE_PRIVATE)
                .getBoolean("voice_notifications", true); 


        try {
            // get all reminders for this elderly
            Task<DataSnapshot> task = FirebaseDatabase.getInstance()
                    .getReference("elderly")
                    .child(uid)
                    .child(elderlyId)
                    .child("reminders")
                    .get();

            DataSnapshot snapshot = Tasks.await(task);

            for (DataSnapshot reminderSnapshot : snapshot.getChildren()) {
                String reminderId = reminderSnapshot.getKey();
                String reminderStartDate = reminderSnapshot.child("startDate").getValue(String.class);
                String reminderEndDate = reminderSnapshot.child("endDate").getValue(String.class);
                Boolean taken = reminderSnapshot.child("taken").getValue(Boolean.class);

                if (reminderId != null && reminderStartDate != null && reminderEndDate != null) {
                    Date start = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(reminderStartDate);
                    Date end = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(reminderEndDate);

                    // check if today is in range and medication is not taken
                    if (!new Date().before(start) && !new Date().after(end) && (taken == null || !taken)) {
                        String medNameToday = reminderSnapshot.child("medicationName").getValue(String.class);
                        String dosageToday = reminderSnapshot.child("dosage").getValue(String.class);

                        // build Intent for "Taken" button on notification
                        Intent takenIntent = new Intent(getApplicationContext(), NotificationActionReceiver.class);
                        takenIntent.putExtra("elderlyId", elderlyId);
                        takenIntent.putExtra("medicationName", medNameToday != null ? medNameToday : medName);
                        takenIntent.putExtra("dosage", dosageToday != null ? dosageToday : dosage);
                        takenIntent.putExtra("elderlyName", elderlyName);
                        takenIntent.putExtra("notificationId", notificationId);
                        takenIntent.putExtra("reminderId", reminderId); // pass correct reminderId

                        // creates a clickable “Taken” button on the notification, so when the user taps it, Android sends the Intent to your NotificationActionReceiver, which marks the medication as taken in Firebase
                        PendingIntent takenPendingIntent = PendingIntent.getBroadcast(
                                getApplicationContext(),
                                notificationId,
                                takenIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                        );

                        // build the notification
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                .setSmallIcon(R.drawable.ic_reminder)
                                .setContentTitle("⏰ Medication Reminder for " + elderlyName)
                                .setContentText("Time to take " + (medNameToday != null ? medNameToday : medName) + " (" +
                                        (dosageToday != null ? dosageToday : dosage) + "dosage)")
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setAutoCancel(true)
                                .addAction(R.drawable.ic_check, "Taken", takenPendingIntent);

                        // show notification
                        NotificationManagerCompat.from(getApplicationContext()).notify(notificationId, builder.build());

                        // TTS: reminder will have voice if voice notifications are on
                        if (voiceEnabled) {
                            TextToSpeech[] ttsHolder = new TextToSpeech[1];

                            ttsHolder[0] = new TextToSpeech(getApplicationContext(), status -> {
                                if (status == TextToSpeech.SUCCESS) {
                                    ttsHolder[0].setLanguage(Locale.getDefault());
                                    ttsHolder[0].setPitch(1.0f);
                                    ttsHolder[0].setSpeechRate(1.0f);

                                    // text voice (reminder notification)
                                    String speakText = "Hello! Kindly check on " + elderlyName +
                                            ". It’s time for their " + (medNameToday != null ? medNameToday : medName) +
                                            ". Please give " + (dosageToday != null ? dosageToday : dosage) + " dosage.";

                                    ttsHolder[0].speak(speakText, TextToSpeech.QUEUE_FLUSH, null, "MED_REMINDER_ID");
                                }
                            });

                            // clean up TTS after speaking
                            ttsHolder[0].setOnUtteranceProgressListener(new UtteranceProgressListener() {
                                @Override
                                public void onStart(String utteranceId) { }

                                @Override
                                public void onDone(String utteranceId) {
                                    ttsHolder[0].stop();
                                    ttsHolder[0].shutdown();
                                }

                                @Override
                                public void onError(String utteranceId) {
                                    ttsHolder[0].stop();
                                    ttsHolder[0].shutdown();
                                }
                            });
                        }

                        // schedule missed dose alert
                        scheduleMissedDoseAlert(
                                medNameToday != null ? medNameToday : medName,
                                dosageToday != null ? dosageToday : dosage,
                                elderlyId,
                                elderlyName,
                                notificationId,
                                reminderId
                        );
                    }
                }
            }

        } catch (ExecutionException | InterruptedException | java.text.ParseException e) {
            e.printStackTrace();
        }

        // Reschedule next notification (tomorrow)
        NotificationUtils.scheduleReminderNotification(
                getApplicationContext(),
                medName,
                dosage,
                hour,
                minute,
                startDateStr,
                endDateStr,
                elderlyId
        );

        return Result.success();
    }

    // schedule a follow-up Worker to alert if dose was missed
    private void scheduleMissedDoseAlert(String medName, String dosage, String elderlyId, String elderlyName, int originalNotificationId, String reminderId) {
        OneTimeWorkRequest missedDoseWork = new OneTimeWorkRequest.Builder(MissedDoseWorker.class)
                .setInitialDelay(20, TimeUnit.SECONDS) // the timer for the missed dosed alert to popup
                .setInputData(
                        new androidx.work.Data.Builder()
                                .putString("medicationName", medName)
                                .putString("dosage", dosage)
                                .putString("elderlyId", elderlyId)
                                .putString("elderlyName", elderlyName)
                                .putInt("originalNotificationId", originalNotificationId)
                                .putString("reminderId", reminderId)
                                .build()
                )
                .build();

        WorkManager.getInstance(getApplicationContext()).enqueue(missedDoseWork); // schedule the missed dose Worker
    }

    // fetch elderly's name from Firebase
    private String fetchElderlyName(String elderlyId) {
        try {
            String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
            Task<DataSnapshot> task = FirebaseDatabase.getInstance()
                    .getReference("elderly")
                    .child(uid)
                    .child(elderlyId)
                    .child("name")
                    .get();

            DataSnapshot snapshot = Tasks.await(task);
            String name = snapshot.getValue(String.class);

            return (name != null) ? name : "Elderly"; // default to "Elderly" if name not found
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return "Elderly";
        }
    }

    // create notification channel
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Medication Reminder";
            String description = "Channel for medication reminders";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager manager = getApplicationContext().getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }
}

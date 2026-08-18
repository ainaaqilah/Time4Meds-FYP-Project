package com.example.time4meds;

// MissedDoseWorker: Handles checking missed medication doses and sending urgent notifications with optional TTS alert

import static android.content.Context.MODE_PRIVATE;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.speech.tts.TextToSpeech;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class MissedDoseWorker extends Worker {

    public static final String CHANNEL_ID = "MissedDoseChannel";

    public MissedDoseWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        // to retrieve these components to passed to the worker
        String elderlyId = getInputData().getString("elderlyId");
        String elderlyName = getInputData().getString("elderlyName");
        String reminderId = getInputData().getString("reminderId");

        // validate required data
        if (elderlyId == null || elderlyName == null || reminderId == null) {
            return Result.failure(); // stop worker if data is missing
        }

        createNotificationChannel(); // ensure notification channel exists ( for Android 8 and above)

        // variables to store medication info
        String medName = "";
        String dosage = "";

        try {
            // get current logged-in user ID
            String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();

            // to fetch reminder data from Firebase
            Task<DataSnapshot> task = FirebaseDatabase.getInstance()
                    .getReference("elderly")
                    .child(uid)
                    .child(elderlyId)
                    .child("reminders")
                    .child(reminderId)
                    .get();

            // wait until Firebase data is retrieved
            DataSnapshot snapshot = Tasks.await(task);
            // extract medication name from Firebase
            medName = snapshot.child("medicationName").getValue(String.class);
            // extract dosage from Firebase
            dosage = snapshot.child("dosage").getValue(String.class);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return Result.failure(); // stop worker if error occurs
        }

        // check if medication details exist
        if (medName == null || dosage == null) {
            return Result.failure(); // stop if data is incomplete
        }

        final String medNameFinal = medName;
        final String dosageFinal = dosage;
        final String elderlyNameFinal = elderlyName;

        // check Firebase if the dose was already taken
        boolean isTaken = checkIfTaken(elderlyId, reminderId);

        // if medication was not taken
        if (!isTaken) {

            // generate unique notification ID
            int missedNotificationId = (int) System.currentTimeMillis();

            // define vibration pattern for more strong alert
            long[] vibrationPattern = {0, 700, 400, 700, 400, 700};

            Intent intent = new Intent(getApplicationContext(), DailyChecklistActivity.class);
            intent.putExtra("elderlyId", elderlyId);
            intent.putExtra("reminderId", reminderId);

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    getApplicationContext(),
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // build missed dose notification text
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_reminder) // this is the notification icon
                            .setContentTitle("⚠️ MISSED DOSE ALERT") // the notification title
                            .setContentText(
                                    elderlyNameFinal + " missed " + dosageFinal + " dosage of " + medNameFinal
                            ) // the noti text
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setVibrate(vibrationPattern) // enable vibration
                            .setAutoCancel(true) // removes the notification from noti list
                            .setCategory(NotificationCompat.CATEGORY_ALARM) // set as alarm notification
                             .setContentIntent(pendingIntent); // open daily checklist

            // display the notification
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
            notificationManager.notify(missedNotificationId, builder.build());

            // play alarm sound using MediaPlayer
            MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.alarm_sound);
            mediaPlayer.setOnCompletionListener(mp -> {
                mp.release(); // free resources

                // read app settings (voice notification toggle)
                SharedPreferences prefs = getApplicationContext()
                        .getSharedPreferences("app_settings_" + FirebaseAuth.getInstance().getUid(), MODE_PRIVATE);

                boolean isVoiceOn = prefs.getBoolean("voice_notifications", true);

                // only start TTS if toggle is ON
                if (isVoiceOn) {
                    startTTS(elderlyNameFinal);
                }
            });
            mediaPlayer.start();
        }

        return Result.success();
    }

    private boolean checkIfTaken(String elderlyId, String reminderId) { // to check whether a medication dose has been marked as taken in Firebase
        try {
            // retrieves current logged-in user’s unique ID to access their data in Firebase
            String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();

            // creates a Firebase task to read data from the Realtime Database
            Task<DataSnapshot> task = FirebaseDatabase.getInstance()
                    .getReference("elderly")
                    .child(uid) // navigate to logged-in user’s profile
                    .child(elderlyId)
                    .child("reminders")
                    .child(reminderId)
                    .child("taken")
                    .get();

            DataSnapshot snapshot = Tasks.await(task);
            Boolean taken = snapshot.getValue(Boolean.class);

            return taken != null && taken;

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return false; // treat as missed if error occurs
        }
    }

    private void startTTS(String elderlyNameFinal) { // a method to start Text-to-Speech for a missed dose alert
        final TextToSpeech[] ttsHolder = new TextToSpeech[1];
        ttsHolder[0] = new TextToSpeech(getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) { // ensures the Text-to-Speech engine was successfully initialized
                ttsHolder[0].setLanguage(Locale.getDefault()); // set language to default based on the phone
                ttsHolder[0].setPitch(1.0f); // voice pitch (normal)
                ttsHolder[0].setSpeechRate(1.0f); // voice speed (normal)

                // spoken alert message
                String speakText = "URGENT ⚠️ MISSED DOSE ALERT. " + elderlyNameFinal +
                        " has missed a medication dose. Please check immediately.";

                ttsHolder[0].speak(speakText, TextToSpeech.QUEUE_FLUSH, null, "MISSED_DOSE_ID");
            }
        });

        // to track the speech progress
        ttsHolder[0].setOnUtteranceProgressListener(new android.speech.tts.UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {}

            // to ensure the text-to-speech engine is properly closed after use so the app does not waste memory or slow down the device
            @Override
            public void onDone(String utteranceId) {
                ttsHolder[0].stop(); // to stop the tts
                ttsHolder[0].shutdown();
            }

            @Override
            public void onError(String utteranceId) {
                ttsHolder[0].stop();
                ttsHolder[0].shutdown();
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            Uri alarmSound = Uri.parse("android.resource://"
                    + getApplicationContext().getPackageName()
                    + "/"
                    + R.raw.alarm_sound);

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Missed Dose Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );

            channel.setDescription("Channel for missed medication doses");
            channel.setSound(alarmSound, audioAttributes);
            channel.enableVibration(true); // enables vibration for notifications
            channel.setVibrationPattern(new long[]{0, 700, 400, 700, 400, 700}); // strong vibration pattern for urgent alerts
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC); // allows notification content to be shown on the lock screen

            NotificationManager manager =
                    getApplicationContext().getSystemService(NotificationManager.class); // retrieves the system Notification Manager

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }

        }
    }

}

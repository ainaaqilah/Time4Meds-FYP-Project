package com.example.time4meds;

// NotificationUtils: Utility class to schedule medication reminder notifications
// uses WorkManager to schedule a one-time notification at a specific time (hour:minute)
// calculates delay from current time. If the time already passed today, schedules for tomorrow

import android.content.Context;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class NotificationUtils {

    public static void scheduleReminderNotification(Context context,
                                                    String medName,
                                                    String dosage,
                                                    int hour,
                                                    int minute,
                                                    String startDate,
                                                    String endDate,
                                                    String elderlyId) {

        Calendar now = Calendar.getInstance();
        Calendar scheduleTime = Calendar.getInstance();
        scheduleTime.set(Calendar.HOUR_OF_DAY, hour);
        scheduleTime.set(Calendar.MINUTE, minute);
        scheduleTime.set(Calendar.SECOND, 0);
        scheduleTime.set(Calendar.MILLISECOND, 0);

        long delay = scheduleTime.getTimeInMillis() - now.getTimeInMillis();
        if (delay < 0) {
            // if time has passed today, schedule for tomorrow
            delay += TimeUnit.DAYS.toMillis(1);
        }

        // pass all these data to Worker
        Data inputData = new Data.Builder()
                .putString("medicationName", medName)
                .putString("dosage", dosage)
                .putInt("hour", hour)
                .putInt("minute", minute)
                .putString("startDate", startDate)
                .putString("endDate", endDate)
                .putString("elderlyId", elderlyId)
                .build();

        // build the work request
        OneTimeWorkRequest workRequest =
                new OneTimeWorkRequest.Builder(NotificationWorker.class)
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .setInputData(inputData) // attach input data
                        .build();

        // schedule the work request
        WorkManager.getInstance(context).enqueue(workRequest);
    }
}
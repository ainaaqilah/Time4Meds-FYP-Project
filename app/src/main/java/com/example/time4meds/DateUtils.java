package com.example.time4meds;

// DateUtils: Generates timestamps for a Reminder between start and end dates for scheduling notifications

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DateUtils {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()); // to parse/display only the date (year-month-day) without time
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()); // to parse/display both date and time (year-month-day hour:minute)

    public static List<Long> generateReminderOccurrences(Reminder reminder) {
        List<Long> occurrences = new ArrayList<>(); // create a new list to store all reminder timestamps

        // if reminder is null, or start/end date is missing, return empty list
        if (reminder == null || reminder.getStartDate() == null || reminder.getEndDate() == null) return occurrences;

        try {
            // create a calendar instance for the start date
            Calendar start = Calendar.getInstance();
            start.setTime(DATE_FORMAT.parse(reminder.getStartDate()));

            // create a calendar instance for the end date
            Calendar end = Calendar.getInstance();
            end.setTime(DATE_FORMAT.parse(reminder.getEndDate()));

            // get the reminder time (HH:mm)
            String timeStr = reminder.getTime();
            if (timeStr == null || timeStr.isEmpty()) {
                timeStr = "09:00"; // default time if not set
            }

            // Loop through all dates from start to end (keep looping as long as the start date is not after the end date)
            while (!start.after(end)) {
                // combine current date with time to form a full datetime string
                String dateTimeStr = DATE_FORMAT.format(start.getTime()) + " " + timeStr;
                try {
                    // parse datetime string to a timestamp (milliseconds)
                    long timestamp = DATE_TIME_FORMAT.parse(dateTimeStr).getTime();
                    // add the timestamp to the list
                    occurrences.add(timestamp);
                } catch (ParseException e) {
                    // print error if datetime parsing fails
                    e.printStackTrace();
                }
                // move to the next day
                start.add(Calendar.DATE, 1);
            }

        } catch (ParseException e) { // to catch any error if parsing the date/time string fails
            e.printStackTrace(); // to print the error stack trace to help debug the issue
        }

        return occurrences; // return the list of generated timestamps (could be empty if parsing failed)
    }
}

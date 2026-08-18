package com.example.time4meds;

// TimeUtils: Helper class for converting time between 24-hour and 12-hour formats

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeUtils {

    public static String convertTo12Hour(String time24) { // converts a time string from 24-hour format (HH:mm) to 12-hour format (hh:mm a)
        try {
            SimpleDateFormat sdf24 = new SimpleDateFormat("HH:mm", Locale.getDefault());
            SimpleDateFormat sdf12 = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            Date date = sdf24.parse(time24);
            return date != null ? sdf12.format(date) : time24;
        } catch (ParseException e) {
            e.printStackTrace();
            return time24;
        }
    }

    public static String convertTo24Hour(String time12) { // converts a time string from 12-hour format (hh:mm a) to 24-hour format (HH:mm)
        try {
            SimpleDateFormat sdf12 = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            SimpleDateFormat sdf24 = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date date = sdf12.parse(time12);
            return date != null ? sdf24.format(date) : time12;
        } catch (ParseException e) {
            e.printStackTrace();
            return time12;
        }
    }
}

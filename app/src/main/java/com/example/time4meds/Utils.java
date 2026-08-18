package com.example.time4meds;

// Utils: Helper class for date-related tasks
// act as helper class that centralizes date formatting and date range checking,
// so reminders and notifications can consistently determine whether a medication is active on a given day

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {

    public static String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    public static boolean isDateWithin(String start, String end, String check) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date dStart = sdf.parse(start);
            Date dEnd = sdf.parse(end);
            Date dCheck = sdf.parse(check);

            return !dCheck.before(dStart) && !dCheck.after(dEnd);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

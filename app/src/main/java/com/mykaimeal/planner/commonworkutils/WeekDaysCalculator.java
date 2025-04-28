package com.mykaimeal.planner.commonworkutils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
public class WeekDaysCalculator {

    public static List<String> getWeekDays(String inputDate) {
        List<String> weekDays = new ArrayList<>();
        try {
            // Input date format
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            Date date = sdf.parse(inputDate);

            // Initialize Calendar with the given date
            Calendar calendar = Calendar.getInstance();
            // Set Monday as the first day of the week
            calendar.setFirstDayOfWeek(Calendar.MONDAY);
            calendar.setTime(date);

            // Move to the first day of the week (e.g., Sunday or Monday depending on locale)
            calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());

            // Get all 7 days of the week
            for (int i = 0; i < 7; i++) {
                String dayName = new SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.getTime());
                String formattedDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(calendar.getTime());
                weekDays.add(dayName + " - " + formattedDate);

                // Move to the next day
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return weekDays;
    }
}

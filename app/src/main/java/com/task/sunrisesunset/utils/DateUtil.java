package com.task.sunrisesunset.utils;

import android.app.DatePickerDialog;
import android.content.Context;

import com.task.sunrisesunset.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtil {

    public static String convertUtcToLocal(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.US);
        Date date;
        try {
            date = sdf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
        sdf.applyPattern("hh:mm:ss a");
        return sdf.format(date);
    }

    public static String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM.dd.yyyy", Locale.US);
        return sdf.format(date);
    }

    public static Date parseDate(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM.dd.yyyy", Locale.US);
        Date date;
        try {
            date = sdf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        return date;
    }

    public static String formatDateForApi(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return sdf.format(date);
    }

    public static void showDatePickerDialog(Context context, DatePickerDialog.OnDateSetListener datePickerListener, Calendar calendar) {
        new DatePickerDialog(context, R.style.DatePickerTheme, datePickerListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))
                .show();
    }
}

package com.task.sunrisesunset.utils;

import android.app.Activity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

public class UIUtil {

    public static void showProgressBar(ProgressBar progressBar, ScrollView content) {
        progressBar.setVisibility(View.VISIBLE);
        content.setVisibility(View.INVISIBLE);
    }

    public static void hideProgressBar(ProgressBar progressBar, ScrollView content) {
        progressBar.setVisibility(View.INVISIBLE);
        content.setVisibility(View.VISIBLE);
    }

    public static void showEmptySearchLabel(ProgressBar progressBar, TextView emptySearchLabel) {
        progressBar.setVisibility(View.INVISIBLE);
        emptySearchLabel.setVisibility(View.VISIBLE);
    }

    public static void hideEmptySearchLabel(TextView emptySearchLabel) {
        emptySearchLabel.setVisibility(View.INVISIBLE);
    }

    public static void makeUnTouchable(Activity context) {
        context.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public static void makeTouchable(Activity context) {
        context.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

//    public static class DatePickerFragment extends DialogFragment {
//
//        private Context mContext;
//        private Calendar mCalendar;
//        private DatePickerDialog.OnDateSetListener datePickerListener;
//
//        @Override
//        public void onAttach(Context context) {
//            super.onAttach(context);
//                mContext = context;
//        }
//
//        @Override
//        public Dialog onCreateDialog(Bundle savedInstanceState) {
//            int year = mCalendar.get(Calendar.YEAR);
//            int month = mCalendar.get(Calendar.MONTH);
//            int day = mCalendar.get(Calendar.DAY_OF_MONTH);
//
//            return new DatePickerDialog(mContext, datePickerListener, year, month, day);
//        }
//
//        public void setCalendar(Calendar calendar) {
//            this.mCalendar = calendar;
//        }
//
//        public void setDatePickerListener(DatePickerDialog.OnDateSetListener datePickerListener) {
//            this.datePickerListener = datePickerListener;
//        }
//    }
}
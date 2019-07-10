package com.task.sunrisesunset.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.provider.SearchRecentSuggestions;
import android.support.v7.app.AlertDialog;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.task.sunrisesunset.R;
import com.task.sunrisesunset.data.SunriseSunsetSuggestionProvider;

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

    public static void showConfirmationDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.AlertDialogCustom));
        builder.setMessage(R.string.message_clear_search_history)
                .setPositiveButton(R.string.message_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(context,
                                SunriseSunsetSuggestionProvider.AUTHORITY, SunriseSunsetSuggestionProvider.MODE);
                        suggestions.clearHistory();
                    }
                })
                .setNegativeButton(R.string.message_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        builder.create().show();
    }

//    public static class NoticeDialogFragment extends DialogFragment {
//
//        private Context mContext;
//
//        @Override
//        public void onAttach(Context context) {
//            super.onAttach(context);
//                mContext = context;
//        }
//
//        @Override
//        public Dialog onCreateDialog(Bundle savedInstanceState) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//            builder.setMessage(R.string.message_clear_search_history)
//                    .setPositiveButton(R.string.message_ok, new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(mContext,
//                                    SunriseSunsetSuggestionProvider.AUTHORITY, SunriseSunsetSuggestionProvider.MODE);
//                            suggestions.clearHistory();
//                        }
//                    })
//                    .setNegativeButton(R.string.message_cancel, new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            // User cancelled the dialog
//                        }
//                    });
//            return builder.create();
//
//        }
//    }

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

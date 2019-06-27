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
}

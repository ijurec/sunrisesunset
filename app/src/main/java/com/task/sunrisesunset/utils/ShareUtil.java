package com.task.sunrisesunset.utils;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.ShareCompat;
import android.widget.TextView;

public class ShareUtil {

    private static final String FORECAST_SHARE_HASHTAG = " #SunriseSunset";

    public static Intent createShareForecastIntent(Activity activity, TextView currentLocation,
                                                   TextView date, TextView sunrise, TextView sunset) {
        String shareTextSummary = "In " + currentLocation.getText() + " on date " + date.getText() + " sunrise is at "
                + sunrise.getText() + " and sunset  at " + sunset.getText();
        Intent shareIntent = ShareCompat.IntentBuilder.from(activity)
                .setType("text/plain")
                .setText(shareTextSummary + FORECAST_SHARE_HASHTAG)
                .getIntent();
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        return shareIntent;
    }
}

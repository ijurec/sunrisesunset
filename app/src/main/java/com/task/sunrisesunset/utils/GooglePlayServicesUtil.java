package com.task.sunrisesunset.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.task.sunrisesunset.MainActivity;

public class GooglePlayServicesUtil {

    private static final int REQUEST_GOOGLE_PLAY_SERVICES_AVAILABILITY = 3;
    private static final String TAG = MainActivity.class.getSimpleName();

    public static boolean isGooglePlayServicesAvailable(final Activity activity) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                Dialog errorDialog;
                errorDialog = googleApiAvailability.getErrorDialog(activity, status, REQUEST_GOOGLE_PLAY_SERVICES_AVAILABILITY);
                errorDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        activity.finish();
                    }
                });
                errorDialog.show();
            } else {
                Log.i(TAG, "This device is not supported.");
                activity.finish();
            }
            return false;
        }
        return true;
    }
}

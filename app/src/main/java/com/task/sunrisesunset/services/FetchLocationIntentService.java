package com.task.sunrisesunset.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.task.sunrisesunset.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class FetchLocationIntentService extends IntentService {

    private static final String TAG = FetchLocationIntentService.class.getSimpleName();

    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME =
            "package com.task.sunrisesunset.services";
    public static final String RECEIVER = PACKAGE_NAME + ".LOCATION_RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME +
            ".RESULT_DATA_KEY";
    public static final String RESULT_ERROR_MESSAGE_KEY = PACKAGE_NAME +
            ".RESULT_ERROR_MESSAGE_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME +
            ".LOCATION_DATA_EXTRA";

    private ResultReceiver mReceiver;

    public FetchLocationIntentService() {
        super("FetchLocationIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        Geocoder geocoder = new Geocoder(this, Locale.US);
        String errorMessage = "";

        mReceiver = intent.getParcelableExtra(RECEIVER);
        String locationAddress = intent.getStringExtra(
                LOCATION_DATA_EXTRA);

        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocationName(locationAddress, 1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            errorMessage = getString(R.string.service_location_not_available);
            Log.e(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid address value.
            errorMessage = getString(R.string.invalid_address_param_used);
            Log.e(TAG, errorMessage + ". " +
                    "Address = " + locationAddress, illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size()  == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = getString(R.string.no_address_found);
                Log.e(TAG, errorMessage);
            }
            deliverResultToReceiver(FAILURE_RESULT, errorMessage, null);
        } else {
            Address address = addresses.get(0);
            Location location = new Location("");
            location.setLatitude(address.getLatitude());
            location.setLongitude(address.getLongitude());
            Log.i(TAG, getString(R.string.coordinates_found));
            deliverResultToReceiver(SUCCESS_RESULT, null, location);
        }
    }

    private void deliverResultToReceiver(int resultCode, String message, Location location) {
        Bundle bundle = new Bundle();
        bundle.putString(RESULT_ERROR_MESSAGE_KEY, message);
        bundle.putParcelable(RESULT_DATA_KEY, location);
        mReceiver.send(resultCode, bundle);
    }

    public static void startFetchLocationIntentService(Context context, ResultReceiver resultReceiver, String address) {
        Intent intent = new Intent(context, FetchLocationIntentService.class);
        intent.putExtra(RECEIVER, resultReceiver);
        intent.putExtra(LOCATION_DATA_EXTRA, address);
        context.startService(intent);
    }
}

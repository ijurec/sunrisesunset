package com.task.sunrisesunset;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.task.sunrisesunset.data.SunriseSunsetInfoResult;
import com.task.sunrisesunset.utils.DateUtil;
import com.task.sunrisesunset.utils.NumberUtil;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements Callback<SunriseSunsetInfoResult> {

    public static final int RESPONSE_WITHOUT_FORMATTING = 0;
    public static final int REQUEST_CHECK_SETTINGS = 1;
    public static final int PERMISSION_REQUEST_FINE_LOCATION = 2;
    public static final int REQUEST_GOOGLE_PLAY_SERVICES_AVAILABILITY = 3;
    public static final String DATE_KEY = "date_key";

    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView mCurrentLocation;
    private TextView mLatitude;
    private TextView mLongitude;
    private TextView mSunrise;
    private TextView mSunset;
    private TextView mAttributionLink;
    private TextView mLabelSearchEmpty;
    private TextView mDate;
    private ScrollView mContent;
    private ProgressBar mProgressContent;

    private boolean isBackToExitClickedTwice;
    private boolean isRequestingLocationUpdates;

    private Calendar mDateCalendar = Calendar.getInstance();

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private Location mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mCurrentLocation = findViewById(R.id.text_current_location);
        mLatitude = findViewById(R.id.text_latitude);
        mLongitude = findViewById(R.id.text_longitude);
        mSunrise = findViewById(R.id.text_sunrise_time);
        mSunset = findViewById(R.id.text_sunset_time);
        mAttributionLink = findViewById(R.id.text_attribution_link);
        mLabelSearchEmpty = findViewById(R.id.label_search_empty);
        mDate = findViewById(R.id.text_date);
        mContent = findViewById(R.id.content);
        mProgressContent = findViewById(R.id.loading_indicator_sunrise_sunset);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;

                mLocation = locationResult.getLastLocation();
                if (mLocation != null) {
                    mLatitude.setText(NumberUtil.roundingRank(mLocation.getLatitude()));
                    mLongitude.setText(NumberUtil.roundingRank(mLocation.getLongitude()));
                    requestSunriseSunsetTimes();
                } else {
                    Toast.makeText(MainActivity.this, "Please, turn on location", Toast.LENGTH_SHORT).show();
                }
            }
        };

        if (savedInstanceState != null && savedInstanceState.containsKey(DATE_KEY)) {
            mDateCalendar = (Calendar) savedInstanceState.getSerializable(DATE_KEY);
        }

        setInitialDate();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mAttributionLink.setClickable(true);
        mAttributionLink.setMovementMethod(LinkMovementMethod.getInstance());
        String text = "<a href='" + getString(R.string.attribution_url) + "'>  " + "Visit Sunrise Sunset" + "  </a>";
        mAttributionLink.setText(Html.fromHtml(text));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isRequestingLocationUpdates) {
            if (isGooglePlayServicesAvailable(this)) {
                startLocationUpdates();
            } else {
                mProgressContent.setVisibility(View.INVISIBLE);
                mLabelSearchEmpty.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        isRequestingLocationUpdates = false;
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_show_locations) {
            Intent intent = new Intent(this, LocationsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startLocationUpdates() {
        isRequestingLocationUpdates = true;
        mProgressContent.setVisibility(View.VISIBLE);
        mContent.setVisibility(View.INVISIBLE);

        final LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        final Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, null);
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSION_REQUEST_FINE_LOCATION);
                }
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MainActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        Log.d(TAG, "Error during settings adjustment");
                        sendEx.printStackTrace();
                    }
                }
            }
        });
    }

    private void requestSunriseSunsetTimes() {
        Call<SunriseSunsetInfoResult> sunriseSunsetInfoResult = SunriseSunsetApp.getSunriseSunsetApi()
                .getSunriseSunsetInfo(mLatitude.getText().toString(), mLongitude.getText().toString(),
                        DateUtil.formatDateForApi(mDateCalendar.getTime()), RESPONSE_WITHOUT_FORMATTING);
        sunriseSunsetInfoResult.enqueue(this);
    }

    @Override
    public void onResponse(Call<SunriseSunsetInfoResult> call, Response<SunriseSunsetInfoResult> response) {
        SunriseSunsetInfoResult sunriseSunsetResult = response.body();
        mSunrise.setText(DateUtil.convertUtcToLocal(sunriseSunsetResult.getResult().getSunrise()));
        mSunset.setText(DateUtil.convertUtcToLocal(sunriseSunsetResult.getResult().getSunset()));
        startIntentService();
    }

    @Override
    public void onFailure(Call<SunriseSunsetInfoResult> call, Throwable t) {
        mProgressContent.setVisibility(View.INVISIBLE);
        mLabelSearchEmpty.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Network issue occurred", Toast.LENGTH_SHORT).show();
    }

    private void setInitialDate() {
        mDate.setText(DateUtil.formatDate(mDateCalendar.getTime()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            startLocationUpdates();
        } else {
            mProgressContent.setVisibility(View.INVISIBLE);
            mLabelSearchEmpty.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates();
                } else {
                    mProgressContent.setVisibility(View.INVISIBLE);
                    mLabelSearchEmpty.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public void setDate(View view) {
        new DatePickerDialog(this, datePickerListener,
                mDateCalendar.get(Calendar.YEAR),
                mDateCalendar.get(Calendar.MONTH),
                mDateCalendar.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mDateCalendar.set(Calendar.YEAR, year);
            mDateCalendar.set(Calendar.MONTH, monthOfYear);
            mDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            setInitialDate();
            startLocationUpdates();
        }
    };

    private boolean isGooglePlayServicesAvailable(Activity activity) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                Dialog errorDialog;
                errorDialog = googleApiAvailability.getErrorDialog(activity, status, REQUEST_GOOGLE_PLAY_SERVICES_AVAILABILITY);
                errorDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finish();
                    }
                });
                errorDialog.show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(DATE_KEY, mDateCalendar);
        super.onSaveInstanceState(outState);
    }

    private void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(FetchAddressIntentService.RECEIVER, resultReceiver);
        intent.putExtra(FetchAddressIntentService.LOCATION_DATA_EXTRA, mLocation);
        startService(intent);
    }

    private ResultReceiver resultReceiver = new ResultReceiver(new Handler()) {
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultData == null) {
                return;
            }

            String addressOutput = resultData.getString(FetchAddressIntentService.RESULT_DATA_KEY);
            if (addressOutput == null) {
                addressOutput = "";
            }
            mCurrentLocation.setText(addressOutput);
            mProgressContent.setVisibility(View.INVISIBLE);
            mContent.setVisibility(View.VISIBLE);
        }
    };

    @Override
    public void onBackPressed() {
        if (isBackToExitClickedTwice) {
            super.onBackPressed();
            return;
        }

        isBackToExitClickedTwice = true;
        Toast.makeText(this, "Please, click BACK again to exit", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isBackToExitClickedTwice = false;
            }
        }, 2000);
    }
}

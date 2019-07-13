package com.task.sunrisesunset;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
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

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.task.sunrisesunset.data.SunriseSunsetInfoResult;
import com.task.sunrisesunset.utils.DateUtil;
import com.task.sunrisesunset.utils.GooglePlayServicesUtil;
import com.task.sunrisesunset.utils.NumberUtil;
import com.task.sunrisesunset.utils.ShareUtil;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.task.sunrisesunset.utils.UIUtil.*;

public class MainActivity extends AppCompatActivity implements Callback<SunriseSunsetInfoResult>,
        SwipeRefreshLayout.OnRefreshListener {

    public static final int RESPONSE_WITHOUT_FORMATTING = 0;
    public static final int REQUEST_CHECK_SETTINGS = 1;
    public static final int PERMISSION_REQUEST_FINE_LOCATION = 2;
    public static final String DATE_KEY = "date_key";
    public static final String PLACE_DATA_KEY = "place_data";
    public static final int AUTOCOMPLETE_REQUEST_CODE = 5;

    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView mCurrentLocation;
    private TextView mLatitude;
    private TextView mLongitude;
    private TextView mSunrise;
    private TextView mSunset;
    private TextView mAttributionLink;
    private TextView mEmptySearchLabel;
    private TextView mDate;
    private ScrollView mContent;
    private ProgressBar mProgressBar;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private boolean isBackToExitClickedTwice;
    private boolean isRefreshingData;
    private boolean isDataLoaded;
    private boolean isDatePickerUsed;

    private Calendar mDateCalendar;

    private PlacesClient mPlacesClient;
    private LatLng mLocation;
    private SunriseSunsetInfoResult mSunriseSunsetResult;
    private String mLocationAddress;

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
        mEmptySearchLabel = findViewById(R.id.label_search_empty);
        mDate = findViewById(R.id.text_date);
        mContent = findViewById(R.id.content);
        mProgressBar = findViewById(R.id.loading_indicator_sunrise_sunset);
        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);

        mAttributionLink.setClickable(true);
        mAttributionLink.setMovementMethod(LinkMovementMethod.getInstance());
        String text = "<a href='" + getString(R.string.attribution_url) + "'>  " + "Visit Sunrise Sunset" + "  </a>";
        mAttributionLink.setText(Html.fromHtml(text));

        if (savedInstanceState != null && savedInstanceState.containsKey(DATE_KEY)) {
            mDateCalendar = (Calendar) savedInstanceState.getSerializable(DATE_KEY);
        } else {
            mDateCalendar = Calendar.getInstance();
        }
        setInitialDate();

        mSwipeRefreshLayout.setOnRefreshListener(this);

        Places.initialize(getApplicationContext(), SunriseSunsetApp.invokeNativeFunction());
        mPlacesClient = Places.createClient(this);

        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)) {
            startLocationUpdates();
        } else {
            showEmptySearchLabel(mProgressBar, mEmptySearchLabel);
        }
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
        switch (id) {
            case R.id.action_search_location:
                showAutocompleteWidget();
                return true;
            case R.id.action_show_locations:
                Intent intent = new Intent(this, LocationsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_share_location:
                if (mLocation != null && mSunriseSunsetResult != null) {
                    startActivity(ShareUtil.createShareForecastIntent(this,
                            mCurrentLocation, mDate, mSunrise, mSunset));
                } else {
                    Toast.makeText(this, R.string.message_share_issue, Toast.LENGTH_SHORT).show();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startLocationUpdates() {
        makeUnTouchable(this);
        if (!isRefreshingData) {
            showProgressBar(mProgressBar, mContent);
        }

        final LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(10 * 60 * 60 * 1000);
        locationRequest.setFastestInterval(3 * 60 * 60 * 1000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        final Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
                FindCurrentPlaceRequest request =
                        FindCurrentPlaceRequest.builder(placeFields).build();
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Task<FindCurrentPlaceResponse> placeResponse = mPlacesClient.findCurrentPlace(request);
                    placeResponse.addOnCompleteListener(new OnCompleteListener<FindCurrentPlaceResponse>() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                FindCurrentPlaceResponse response = (FindCurrentPlaceResponse) task.getResult();
                                PlaceLikelihood placeLikelihood = response.getPlaceLikelihoods().get(0);
                                mLocationAddress = placeLikelihood.getPlace().getAddress();
                                mLocation = placeLikelihood.getPlace().getLatLng();
                                if (mLocation != null) {
                                    requestSunriseSunsetTimes();
                                } else {
                                    if (!isDataLoaded) {
                                        showEmptySearchLabel(mProgressBar, mEmptySearchLabel);
                                    } else if (isDatePickerUsed) {
                                        setCorrectPickerDateValue();
                                    }
                                    makeTouchable(MainActivity.this);
                                    mSwipeRefreshLayout.setRefreshing(false);
                                    Toast.makeText(MainActivity.this, "Location issue has occurred", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Exception exception = task.getException();
                                if (exception instanceof ApiException) {
                                    ApiException apiException = (ApiException) exception;
                                    String exceptionMessage = "Place not found: " + apiException.getStatusCode();
                                    Log.e(TAG, exceptionMessage);
                                    onRequestFailed(exceptionMessage);
                                } else {
                                    onRequestFailed(exception.getMessage());
                                }
                            }
                        }});
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
                .getSunriseSunsetInfo(mLocation.latitude, mLocation.longitude,
                        DateUtil.formatDateForApi(mDateCalendar.getTime()), RESPONSE_WITHOUT_FORMATTING);
        sunriseSunsetInfoResult.enqueue(this);
    }

    @Override
    public void onResponse(Call<SunriseSunsetInfoResult> call, Response<SunriseSunsetInfoResult> response) {
        mSunriseSunsetResult = response.body();
        mCurrentLocation.setText(mLocationAddress);
        mLatitude.setText(NumberUtil.roundingRank(mLocation.latitude));
        mLongitude.setText(NumberUtil.roundingRank(mLocation.longitude));
        mSunrise.setText(DateUtil.convertUtcToLocal(mSunriseSunsetResult.getResult().getSunrise()));
        mSunset.setText(DateUtil.convertUtcToLocal(mSunriseSunsetResult.getResult().getSunset()));
        isRefreshingData = false;
        isDataLoaded = true;
        if (isDatePickerUsed) {
            isDatePickerUsed = false;
            setInitialDate();
        }
        mSwipeRefreshLayout.setRefreshing(false);
        hideProgressBar(mProgressBar, mContent);
        hideEmptySearchLabel(mEmptySearchLabel);
        makeTouchable(MainActivity.this);
    }

    @Override
    public void onFailure(Call<SunriseSunsetInfoResult> call, Throwable t) {
        onRequestFailed(getString(R.string.message_network_issue));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    Place place = Autocomplete.getPlaceFromIntent(data);
                    Intent intent = new Intent(this, NewLocationActivity.class);
                    intent.setAction(Intent.ACTION_SEARCH);
                    intent.putExtra(PLACE_DATA_KEY, place);
                    startActivity(intent);
                    Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
                } else {
                    Toast.makeText(this, R.string.message_location_issue, Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                String statusMessage = status.getStatusMessage();
                Log.i(TAG, statusMessage);
                Toast.makeText(this, statusMessage, Toast.LENGTH_SHORT).show();
            }
        } else {
            if (resultCode == RESULT_OK) {
                startLocationUpdates();
            } else {
                if (!isDataLoaded) {
                    showEmptySearchLabel(mProgressBar, mEmptySearchLabel);
                } else if (isDatePickerUsed) {
                    setCorrectPickerDateValue();
                }
                mSwipeRefreshLayout.setRefreshing(false);
                makeTouchable(this);
            }
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
                    showEmptySearchLabel(mProgressBar, mEmptySearchLabel);
                    makeTouchable(this);
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }
        }
    }

    @Override
    public void onRefresh() {
        isRefreshingData = true;
        startLocationUpdates();
    }

    private void setInitialDate() {
        mDate.setText(DateUtil.formatDate(mDateCalendar.getTime()));
    }

    public void setDate(View view) {
        DateUtil.showDatePickerDialog(this, datePickerListener, mDateCalendar);
    }

    DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mDateCalendar.set(Calendar.YEAR, year);
            mDateCalendar.set(Calendar.MONTH, monthOfYear);
            mDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            isDatePickerUsed = true;
            startLocationUpdates();
        }
    };

    private void showAutocompleteWidget() {
        List<Place.Field> fields = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG);
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields)
                .build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    private void setCorrectPickerDateValue() {
        isDatePickerUsed = false;
        Date selectedDate = DateUtil.parseDate(mDate.getText().toString());
        if (selectedDate == null) {
            Date date = new Date();
            mDateCalendar.setTime(date);
            setInitialDate();
        } else {
            mDateCalendar.setTime(selectedDate);
        }
        hideProgressBar(mProgressBar, mContent);
    }

    private void onRequestFailed(String messageError) {
        if (isDataLoaded) {
            hideProgressBar(mProgressBar, mContent);
        } else {
            showEmptySearchLabel(mProgressBar, mEmptySearchLabel);
        }
        if (isDatePickerUsed) {
            setCorrectPickerDateValue();
        }
        makeTouchable(MainActivity.this);
        mSwipeRefreshLayout.setRefreshing(false);
        Toast.makeText(this, messageError, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(DATE_KEY, mDateCalendar);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (isBackToExitClickedTwice) {
            super.onBackPressed();
            return;
        }

        isBackToExitClickedTwice = true;
        Snackbar.make(mContent, "Please, click BACK again to exit", Snackbar.LENGTH_LONG).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isBackToExitClickedTwice = false;
            }
        }, 2000);
    }
}
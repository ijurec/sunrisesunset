package com.task.sunrisesunset;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.task.sunrisesunset.data.SunriseSunsetInfoResult;
import com.task.sunrisesunset.utils.DateUtil;
import com.task.sunrisesunset.utils.NumberUtil;
import com.task.sunrisesunset.utils.ShareUtil;

import java.util.Calendar;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.task.sunrisesunset.MainActivity.*;
import static com.task.sunrisesunset.utils.UIUtil.*;

public class NewLocationActivity extends AppCompatActivity implements Callback<SunriseSunsetInfoResult>,
        SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = NewLocationActivity.class.getSimpleName();

    private TextView mLocationLabel;
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

    private boolean isDatePickerUsed;
    private boolean isRefreshingData;
    private boolean isDataLoaded;

    private Calendar mDateCalendar;
    private LatLng mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_location);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mLocationLabel = findViewById(R.id.text_current_location_label);
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

        mLocationLabel.setText(R.string.label_location);

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

        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            Place place = intent.getParcelableExtra(PLACE_DATA_KEY);
            String address = place.getAddress();
            mLocation = place.getLatLng();
            mCurrentLocation.setText(address);
            mLatitude.setText(NumberUtil.roundingRank(mLocation.latitude));
            mLongitude.setText(NumberUtil.roundingRank(mLocation.longitude));
            requestSunriseSunsetTimes();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_location, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_add_location:

                return true;
            case R.id.action_share_location:
                if (mLocation != null && mSunrise.getText() != "" && mSunset.getText() != "") {
                    startActivity(ShareUtil.createShareForecastIntent(this,
                            mCurrentLocation, mDate, mSunrise, mSunset));
                } else {
                    Toast.makeText(this, R.string.message_share_issue, Toast.LENGTH_SHORT).show();
                }
                return true;
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(DATE_KEY, mDateCalendar);
        super.onSaveInstanceState(outState);
    }

    private void requestSunriseSunsetTimes() {
        if(!isRefreshingData || isDatePickerUsed) {
            showProgressBar(mProgressBar, mContent);
        }
        makeUnTouchable(this);

        Call<SunriseSunsetInfoResult> sunriseSunsetInfoResult = SunriseSunsetApp.getSunriseSunsetApi()
                .getSunriseSunsetInfo(mLocation.latitude, mLocation.longitude,
                        DateUtil.formatDateForApi(mDateCalendar.getTime()), RESPONSE_WITHOUT_FORMATTING);
        sunriseSunsetInfoResult.enqueue(this);
    }

    @Override
    public void onResponse(Call<SunriseSunsetInfoResult> call, Response<SunriseSunsetInfoResult> response) {
        SunriseSunsetInfoResult sunriseSunsetResult = response.body();
        mSunrise.setText(DateUtil.convertUtcToLocal(sunriseSunsetResult.getResult().getSunrise()));
        mSunset.setText(DateUtil.convertUtcToLocal(sunriseSunsetResult.getResult().getSunset()));
        isRefreshingData = false;
        isDataLoaded = true;
        if (isDatePickerUsed) {
            isDatePickerUsed = false;
            setInitialDate();
        }
        mSwipeRefreshLayout.setRefreshing(false);
        hideProgressBar(mProgressBar, mContent);
        hideEmptySearchLabel(mEmptySearchLabel);
        makeTouchable(this);
    }

    @Override
    public void onFailure(Call<SunriseSunsetInfoResult> call, Throwable t) {
        isRefreshingData = false;
        mSwipeRefreshLayout.setRefreshing(false);
        if (!isDataLoaded) {
            showEmptySearchLabel(mProgressBar, mEmptySearchLabel);
        }
        if (isDatePickerUsed) {
            isDatePickerUsed = false;
            hideProgressBar(mProgressBar, mContent);
            Date selectedDate = DateUtil.parseDate(mDate.getText().toString());
            if (selectedDate == null) {
                Date date = new Date();
                mDateCalendar.setTime(date);
                setInitialDate();
            } else {
                mDateCalendar.setTime(selectedDate);
            }
        }
        Log.e(TAG, t.getMessage());
        makeTouchable(NewLocationActivity.this);
        Toast.makeText(this, R.string.message_network_issue, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRefresh() {
        isRefreshingData = true;
        requestSunriseSunsetTimes();
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
            requestSunriseSunsetTimes();
        }
    };
}
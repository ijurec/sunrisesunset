package com.task.sunrisesunset;

import android.app.DatePickerDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.SearchRecentSuggestions;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.task.sunrisesunset.data.SunriseSunsetInfoResult;
import com.task.sunrisesunset.data.SunriseSunsetSuggestionProvider;
import com.task.sunrisesunset.services.FetchLocationIntentService;
import com.task.sunrisesunset.utils.DateUtil;
import com.task.sunrisesunset.utils.NumberUtil;
import com.task.sunrisesunset.utils.ShareUtil;

import java.util.Calendar;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.task.sunrisesunset.MainActivity.DATE_KEY;
import static com.task.sunrisesunset.MainActivity.RESPONSE_WITHOUT_FORMATTING;
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

    private String mQuery;
    private Calendar mDateCalendar;

    private Location mLocation;

    private boolean isError;
    private String mErrorMessage;

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
            mQuery = intent.getStringExtra(SearchManager.QUERY).trim();
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    SunriseSunsetSuggestionProvider.AUTHORITY, SunriseSunsetSuggestionProvider.MODE);
            suggestions.saveRecentQuery(mQuery, null);
            getLocationCoordinates(mQuery);
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

    private void getLocationCoordinates(String query) {
        if(!isRefreshingData) {
            showProgressBar(mProgressBar, mContent);
        }
        makeUnTouchable(this);
        FetchLocationIntentService.startFetchLocationIntentService(this, resultLocationReceiver, query);
    }

    private ResultReceiver resultLocationReceiver = new ResultReceiver(new Handler()) {
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultData == null) {
                return;
            }

            mLocation = resultData.getParcelable(FetchLocationIntentService.RESULT_DATA_KEY);
            if (mLocation != null) {
                isError = false;
                mCurrentLocation.setText(mQuery);
                mLatitude.setText(NumberUtil.roundingRank(mLocation.getLatitude()));
                mLongitude.setText(NumberUtil.roundingRank(mLocation.getLongitude()));
                requestSunriseSunsetTimes();
            } else {
                isError = true;
                mErrorMessage = resultData.getString(FetchLocationIntentService.RESULT_ERROR_MESSAGE_KEY);
                if (!isDataLoaded) {
                    showEmptySearchLabel(mProgressBar, mEmptySearchLabel);
                }
                makeTouchable(NewLocationActivity.this);
                isRefreshingData = false;
                mSwipeRefreshLayout.setRefreshing(false);
                Toast.makeText(NewLocationActivity.this, mErrorMessage, Toast.LENGTH_LONG).show();
            }
        }
    };

    private void requestSunriseSunsetTimes() {
        if (isDatePickerUsed) {
            showProgressBar(mProgressBar, mContent);
            makeUnTouchable(this);
        }

        if (!isError) {
            Call<SunriseSunsetInfoResult> sunriseSunsetInfoResult = SunriseSunsetApp.getSunriseSunsetApi()
                    .getSunriseSunsetInfo(mLocation.getLatitude(), mLocation.getLongitude(),
                            DateUtil.formatDateForApi(mDateCalendar.getTime()), RESPONSE_WITHOUT_FORMATTING);
            sunriseSunsetInfoResult.enqueue(this);
        } else {
            hideProgressBar(mProgressBar, mContent);
            makeTouchable(this);
            Toast.makeText(NewLocationActivity.this, mErrorMessage +
                    ", please refresh before changing date.", Toast.LENGTH_LONG).show();
        }
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
        makeTouchable(NewLocationActivity.this);
        Toast.makeText(this, R.string.message_network_issue, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRefresh() {
        isRefreshingData = true;
        getLocationCoordinates(mQuery);
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

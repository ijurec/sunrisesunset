package com.task.sunrisesunset;

import android.app.Application;
import com.task.sunrisesunset.sync.SunriseSunsetApi;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SunriseSunsetApp extends Application {

    private static SunriseSunsetApi sSunriseSunsetApi;

    static {
        System.loadLibrary("native-lib");
    }

    public native static String invokeNativeFunction();

    @Override
    public void onCreate() {
        super.onCreate();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getApplicationContext().getString(R.string.api_url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        sSunriseSunsetApi = retrofit.create(SunriseSunsetApi.class);
    }

    public static SunriseSunsetApi getSunriseSunsetApi() {
        return sSunriseSunsetApi;
    }
}
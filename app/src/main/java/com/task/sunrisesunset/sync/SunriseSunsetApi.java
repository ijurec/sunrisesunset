package com.task.sunrisesunset.sync;

import com.task.sunrisesunset.data.SunriseSunsetInfoResult;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SunriseSunsetApi {

    String LAT_PARAM = "lat";
    String LNG_PARAM = "lng";
    String DATE_PARAM = "date";
    String FORMATTED_PARAM = "formatted";

    @GET("json")
    Call<SunriseSunsetInfoResult> getSunriseSunsetInfo(@Query(LAT_PARAM) double lat, @Query(LNG_PARAM) double lng,
                                                       @Query(DATE_PARAM) String date, @Query(FORMATTED_PARAM) int formatted);
}

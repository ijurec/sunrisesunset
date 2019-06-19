package com.task.sunrisesunset.data;

import com.google.gson.annotations.SerializedName;

public class Result {

    @SerializedName("sunrise")
    private String sunrise;
    @SerializedName("sunset")
    private String sunset;
    @SerializedName("solar_noon")
    private String solarNoon;
    @SerializedName("day_length")
    private String dayLength;
    @SerializedName("civil_twilight_begin")
    private String civilTwilightBegin;
    @SerializedName("civil_twilight_end")
    private String civilTwilightEnd;
    @SerializedName("nautical_twilight_begin")
    private String nauticalTwilightBegin;
    @SerializedName("nautical_twilight_end")
    private String nauticalTwilightEnd;
    @SerializedName("astronomical_twilight_begin")
    private String astronomicalTwilightBegin;
    @SerializedName("astronomical_twilight_end")
    private String astronomicalTwilightEnd;

    public String getSunrise() {
        return sunrise;
    }

    public void setSunrise(String sunrise) {
        this.sunrise = sunrise;
    }

    public String getSunset() {
        return sunset;
    }

    public void setSunset(String sunset) {
        this.sunset = sunset;
    }

    public String getSolarNoon() {
        return solarNoon;
    }

    public void setSolarNoon(String solarNoon) {
        this.solarNoon = solarNoon;
    }

    public String getDayLength() {
        return dayLength;
    }

    public void setDayLength(String dayLength) {
        this.dayLength = dayLength;
    }

    public String getCivilTwilightBegin() {
        return civilTwilightBegin;
    }

    public void setCivilTwilightBegin(String civilTwilightBegin) {
        this.civilTwilightBegin = civilTwilightBegin;
    }

    public String getCivilTwilightEnd() {
        return civilTwilightEnd;
    }

    public void setCivilTwilightEnd(String civilTwilightEnd) {
        this.civilTwilightEnd = civilTwilightEnd;
    }

    public String getNauticalTwilightBegin() {
        return nauticalTwilightBegin;
    }

    public void setNauticalTwilightBegin(String nauticalTwilightBegin) {
        this.nauticalTwilightBegin = nauticalTwilightBegin;
    }

    public String getNauticalTwilightEnd() {
        return nauticalTwilightEnd;
    }

    public void setNauticalTwilightEnd(String nauticalTwilightEnd) {
        this.nauticalTwilightEnd = nauticalTwilightEnd;
    }

    public String getAstronomicalTwilightBegin() {
        return astronomicalTwilightBegin;
    }

    public void setAstronomicalTwilightBegin(String astronomicalTwilightBegin) {
        this.astronomicalTwilightBegin = astronomicalTwilightBegin;
    }

    public String getAstronomicalTwilightEnd() {
        return astronomicalTwilightEnd;
    }

    public void setAstronomicalTwilightEnd(String astronomicalTwilightEnd) {
        this.astronomicalTwilightEnd = astronomicalTwilightEnd;
    }
}

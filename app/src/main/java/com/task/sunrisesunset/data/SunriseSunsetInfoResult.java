package com.task.sunrisesunset.data;

import com.google.gson.annotations.SerializedName;

public class SunriseSunsetInfoResult {

    @SerializedName("results")
    private Result result;
    @SerializedName("status")
    private String status;

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

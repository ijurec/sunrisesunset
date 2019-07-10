package com.task.sunrisesunset.data;

import android.content.SearchRecentSuggestionsProvider;

public class SunriseSunsetSuggestionProvider extends SearchRecentSuggestionsProvider {

    public final static String AUTHORITY = "com.task.sunrisesunset.data.SunriseSunsetSuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public SunriseSunsetSuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}

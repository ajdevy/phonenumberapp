package com.phonenumber.app.phonenumbers;

import android.content.SearchRecentSuggestionsProvider;

public class PhoneNumberSearchSuggestionProvider extends SearchRecentSuggestionsProvider {

    public final static String AUTHORITY = "com.phonenumber.app.PhoneNumberSearchSuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public PhoneNumberSearchSuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
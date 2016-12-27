package com.phonenumber.app.util;

import android.app.Activity;

public class UiAvailabilityChecker {
    public static boolean isUiAvailable(Activity activity) {
        return activity != null && !activity.isFinishing();
    }
}
